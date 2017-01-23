package com.github.joaogalli.capstonepluris.newsubreddit;

import android.net.Uri;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;

import com.github.joaogalli.capstonepluris.model.Subreddit;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Arrays;

/**
 * Created by joaog on 15/01/2017.
 */

public class SearchSubredditAsyncTask extends android.os.AsyncTask<String, Void, Subreddit[]> {

    private static final String TAG = SearchSubredditAsyncTask.class.getSimpleName();
    private SubredditSearchRecyclerViewAdapter mAdapter;
    private ProgressBar progressBar;

    public SearchSubredditAsyncTask(SubredditSearchRecyclerViewAdapter mAdapter, ProgressBar progressBar) {
        this.mAdapter = mAdapter;
        this.progressBar = progressBar;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        progressBar.setVisibility(View.VISIBLE);
    }

    @Override
    protected Subreddit[] doInBackground(String... strings) {
        if (strings.length == 0)
            return null;

        HttpURLConnection urlConnection = null;
        BufferedReader reader = null;

        // Will contain the raw JSON response as a string.
        String jsonStr = null;

        try {
            // Construct the URL for the OpenWeatherMap query
            // Possible parameters are avaiable at OWM's forecast API page, at
            // http://openweathermap.org/API#forecast
            Uri.Builder builder = Uri.parse("https://www.reddit.com/subreddits/search.json?").buildUpon()
                    .appendQueryParameter("q", strings[0]);

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

        try {
            return getSubredditsFromJson(jsonStr);
        } catch (JSONException e) {
            return null;
        }
    }

    private Subreddit[] getSubredditsFromJson(String jsonStr) throws JSONException {
        JSONObject jsonObject = new JSONObject(jsonStr);
        JSONArray children = jsonObject.getJSONObject("data").getJSONArray("children");

        Subreddit[] subreddits = new Subreddit[children.length()];

        for(int i = 0; i < children.length(); i++) {
            JSONObject childObject = children.getJSONObject(i);
            JSONObject dataObject = childObject.getJSONObject("data");

            Subreddit s = new Subreddit();
            s.setDisplayName(dataObject.getString("display_name"));
            s.setHeaderImg(dataObject.getString("header_img"));
            s.setTitle(dataObject.getString("title"));

            subreddits[i] = s;
        }

        return subreddits;
    }

    @Override
    protected void onPostExecute(Subreddit[] subreddits) {
        progressBar.setVisibility(View.GONE);

        if (subreddits == null) {
            subreddits = new Subreddit[0];
        }

        mAdapter.setValues(Arrays.asList(subreddits));
        mAdapter.notifyDataSetChanged();
    }
}
