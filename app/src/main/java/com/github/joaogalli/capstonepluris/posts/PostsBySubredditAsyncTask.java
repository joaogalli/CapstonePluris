package com.github.joaogalli.capstonepluris.posts;

import android.content.ContentValues;
import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;

import com.github.joaogalli.capstonepluris.contentprovider.PostsProvider;
import com.github.joaogalli.capstonepluris.model.Post;
import com.github.joaogalli.capstonepluris.model.PostColumns;

import org.json.JSONArray;
import org.json.JSONException;
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

    private Context mContext;

    private String subreddit;

    public PostsBySubredditAsyncTask(Context mContext) {
        this.mContext = mContext;
    }

    @Override
    protected String doInBackground(String... strings) {
        if (strings.length == 0)
            return null;

        subreddit = strings[0];

        HttpURLConnection urlConnection = null;
        BufferedReader reader = null;

        // Will contain the raw JSON response as a string.
        String jsonStr = null;

        try {
            // Construct the URL for the OpenWeatherMap query
            // Possible parameters are avaiable at OWM's forecast API page, at
            // http://openweathermap.org/API#forecast
            Uri.Builder builder = Uri.parse("https://www.reddit.com/r/" + subreddit + ".json").buildUpon();

            URL url = new URL(builder.toString());

            // Create the request to OpenWeatherMap, and open the connection
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("GET");
            urlConnection.connect();

            // Read the input stream into a String
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
                // Stream was empty.  No point in parsing.
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
                    Log.e("PlaceholderFragment", "Error closing stream", e);
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

            Post[] posts = new Post[children.length()];

            for (int i = 0; i < children.length(); i++) {
                JSONObject childObject = children.getJSONObject(i);
                JSONObject dataObject = childObject.getJSONObject("data");

                ContentValues values = new ContentValues();
                values.put(PostColumns.TITLE, dataObject.getString("title"));
                values.put(PostColumns.SUBREDDIT, subreddit);
                values.put(PostColumns.ID_IN_REDDIT, dataObject.getString("id"));

                try {
                    Uri uri = mContext.getContentResolver().insert(
                            PostsProvider.CONTENT_URI, values);
                } catch (Throwable t) {
                    t.printStackTrace();
                }
            }
        } catch(JSONException jsonException) {
            jsonException.printStackTrace();
            // TODO avisar usuário do erro
        }
    }
}
