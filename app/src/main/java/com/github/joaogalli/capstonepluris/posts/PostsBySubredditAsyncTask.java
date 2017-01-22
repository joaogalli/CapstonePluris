package com.github.joaogalli.capstonepluris.posts;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import com.github.joaogalli.capstonepluris.contentprovider.PostsProvider;
import com.github.joaogalli.capstonepluris.model.PostColumns;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by joaog on 20/01/2017.
 */

public class PostsBySubredditAsyncTask extends AsyncTask<String, Void, String> {

    private static final String TAG = PostsBySubredditAsyncTask.class.getSimpleName();
    private static final int PAGE_SIZE = 25;

    private Context mContext;

    private String subreddit;

    private SearchType searchType = SearchType.NEWER;

    public enum SearchType {
        NEWER, OLDER_THAN
    }

    public PostsBySubredditAsyncTask(Context mContext) {
        this.mContext = mContext;
    }

    public PostsBySubredditAsyncTask(Context mContext, SearchType searchType) {
        this.mContext = mContext;
        this.searchType = searchType;
    }

    @Override
    protected String doInBackground(String... strings) {
        if (strings.length == 0)
            return null;

        subreddit = strings[0];

        String urlStr = "https://www.reddit.com/r/" + subreddit + ".json";

        if (strings.length > 1) {
            if (searchType == SearchType.OLDER_THAN) {
                urlStr += "?limit=" + PAGE_SIZE + "&after=t3_" + strings[1];
            }
        }

        HttpURLConnection urlConnection = null;
        BufferedReader reader = null;

        String jsonStr = null;
        try {
            Uri.Builder builder = Uri.parse(urlStr).buildUpon();

            URL url = new URL(builder.toString());

            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("GET");
            urlConnection.connect();

            InputStream inputStream = urlConnection.getInputStream();
            StringBuffer buffer = new StringBuffer();
            if (inputStream == null) {
                return null;
            }
            reader = new BufferedReader(new InputStreamReader(inputStream));

            String line;
            while ((line = reader.readLine()) != null) {
                buffer.append(line + "\n");
            }

            if (buffer.length() == 0) {
                return null;
            }
            jsonStr = buffer.toString();
        } catch (IOException e) {
            Log.e(TAG, "Error ", e);
            return null;
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
            if (reader != null) {
                try {
                    reader.close();
                } catch (final IOException e) {
                    Log.e(TAG, "Error closing stream", e);
                }
            }
        }

        return jsonStr;
    }

    @Override
    protected void onPostExecute(String jsonStr) {
        try {
            JSONObject jsonObject = new JSONObject(jsonStr);
            JSONArray children = jsonObject.getJSONObject("data").getJSONArray("children");

            for (int i = 0; i < children.length(); i++) {
                JSONObject childObject = children.getJSONObject(i);
                JSONObject dataObject = childObject.getJSONObject("data");

                ContentValues values = new ContentValues();
                values.put(PostColumns.KIND, childObject.getString("kind"));
                values.put(PostColumns.TITLE, dataObject.getString("title"));
                values.put(PostColumns.SUBREDDIT, subreddit);
                values.put(PostColumns.URL, dataObject.getString("url"));
                String idInReddit = dataObject.getString("id");
                values.put(PostColumns.ID_IN_REDDIT, idInReddit);

                try {
                    boolean verifyIdInRedditExists = verifyIdFromRedditExists(idInReddit);
                    if (verifyIdInRedditExists) {
                        // update
                        mContext.getContentResolver().update(PostsProvider.CONTENT_URI, values, PostColumns.ID_IN_REDDIT + " = ?", new String[]{idInReddit});
                    } else {
                        // insert
                        Uri uri = mContext.getContentResolver().insert(
                                PostsProvider.CONTENT_URI, values);
                    }
                } catch (Throwable t) {
                    t.printStackTrace();
                }
            }
        } catch (Exception exception) {
            // TODO avisar usuário do erro
            exception.printStackTrace();
            Toast.makeText(mContext, "An error occurred while loading data, please push to refresh to try again.", Toast.LENGTH_SHORT).show();
        }
    }

    private boolean verifyIdFromRedditExists(String idInReddit) {
        Cursor cursor = mContext.getContentResolver().query(PostsProvider.CONTENT_URI, null, PostColumns.ID_IN_REDDIT + " = ?", new String[]{idInReddit}, null);
        return !(cursor == null || cursor.getCount() == 0);
    }
}
