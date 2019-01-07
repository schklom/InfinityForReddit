package ml.docilealligator.infinityforreddit;

import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import SubredditDatabase.SubredditData;

class ParseSubredditData {
    interface ParseSubredditDataListener {
        void onParseSubredditDataSuccess(SubredditData subredditData, int nCurrentOnlineSubscribers);
        void onParseSubredditDataFail();
    }

    static void parseSubredditData(String response, ParseSubredditDataListener parseSubredditDataListener) {
        new ParseSubredditDataAsyncTask(response, parseSubredditDataListener).execute();
    }

    private static class ParseSubredditDataAsyncTask extends AsyncTask<Void, Void, Void> {
        private JSONObject jsonResponse;
        private boolean parseFailed;
        private ParseSubredditDataListener parseSubredditDataListener;
        private SubredditData subredditData;
        private int mNCurrentOnlineSubscribers;

        ParseSubredditDataAsyncTask(String response, ParseSubredditDataListener parseSubredditDataListener){
            this.parseSubredditDataListener = parseSubredditDataListener;
            try {
                jsonResponse = new JSONObject(response);
                parseFailed = false;
            } catch (JSONException e) {
                Log.i("comment json error", e.getMessage());
                parseSubredditDataListener.onParseSubredditDataFail();
            }
        }

        @Override
        protected Void doInBackground(Void... voids) {
            try {
                JSONObject data = jsonResponse.getJSONObject(JSONUtils.DATA_KEY);
                String id = data.getString(JSONUtils.NAME_KEY);
                String subredditFullName = data.getString(JSONUtils.DISPLAY_NAME);
                String description = data.getString(JSONUtils.PUBLIC_DESCRIPTION_KEY).trim();
                String bannerImageUrl = data.getString(JSONUtils.BANNER_BACKGROUND_IMAGE_KEY);
                if(bannerImageUrl.equals("") || bannerImageUrl.equals("null")) {
                    bannerImageUrl= data.getString(JSONUtils.BANNER_IMG_KEY);
                    if(bannerImageUrl.equals("null")) {
                        bannerImageUrl = "";
                    }
                }
                String iconUrl = data.getString(JSONUtils.COMMUNITY_ICON_KEY);
                if(iconUrl.equals("") || iconUrl.equals("null")) {
                    iconUrl = data.getString(JSONUtils.ICON_IMG_KEY);
                    if(iconUrl.equals("null")) {
                        iconUrl = "";
                    }
                }
                int nSubscribers = data.getInt(JSONUtils.SUBSCRIBERS_KEY);
                int nCurrentOnlineSubscribers = data.getInt(JSONUtils.ACTIVE_USER_COUNT_KEY);
                subredditData = new SubredditData(id, subredditFullName, iconUrl, bannerImageUrl, description, nSubscribers);
                mNCurrentOnlineSubscribers = nCurrentOnlineSubscribers;
            } catch (JSONException e) {
                parseFailed = true;
                Log.i("parse", "SubredditData error");
                parseSubredditDataListener.onParseSubredditDataFail();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            if(!parseFailed) {
                parseSubredditDataListener.onParseSubredditDataSuccess(subredditData, mNCurrentOnlineSubscribers);
            } else {
                parseSubredditDataListener.onParseSubredditDataFail();
            }
        }
    }
}
