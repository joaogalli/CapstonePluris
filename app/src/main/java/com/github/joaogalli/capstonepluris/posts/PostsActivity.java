package com.github.joaogalli.capstonepluris.posts;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import com.github.joaogalli.capstonepluris.R;
import com.github.joaogalli.capstonepluris.model.Subreddit;

public class PostsActivity extends AppCompatActivity {

    public static final String SUBREDDIT_PARAM = "subredditParam";
    private Subreddit subreddit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent intent = getIntent();

        if (getIntent() != null && getIntent().getParcelableExtra(SUBREDDIT_PARAM) != null) {
            subreddit = getIntent().getParcelableExtra(SUBREDDIT_PARAM);
        } else {
            finishActivity(RESULT_CANCELED);
        }

        setContentView(R.layout.activity_posts);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setTitle(subreddit.getTitle());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_posts, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_remove_subreddit) {
            // TODO remover
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

}
