package com.github.joaogalli.capstonepluris.posts;

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.github.joaogalli.capstonepluris.FirebaseUtils;
import com.github.joaogalli.capstonepluris.R;
import com.github.joaogalli.capstonepluris.contentprovider.PostsProvider;
import com.github.joaogalli.capstonepluris.model.PostColumns;
import com.github.joaogalli.capstonepluris.model.Subreddit;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class PostsActivity extends AppCompatActivity {

    public static final String SUBREDDIT_PARAM = "subredditParam";
    private Subreddit subreddit;

    private DatabaseReference mDatabase;

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

        mDatabase = FirebaseDatabase.getInstance().getReference();
    }

    public void onClick(View view) {
        switch(view.getId()) {
            case R.id.load: {
                Cursor cursor = getContentResolver().query(PostsProvider.CONTENT_URI, null, null, null, null);
                Toast.makeText(this, "Count: " + cursor.getCount(), Toast.LENGTH_SHORT).show();
                cursor.close();
            }
            break;
            case R.id.insert: {
                ContentValues values = new ContentValues();
                values.put(PostColumns.ID, 3);
                values.put(PostColumns.TITLE, "Teste 3");

                Uri uri = getContentResolver().insert(
                        PostsProvider.CONTENT_URI, values);
            }
            break;
        }

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
            removeKey();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void removeKey() {
        String uid = FirebaseUtils.getUidOrGoToLogin(this);
        DatabaseReference subreddits = mDatabase.child("subreddits").child(uid).child(subreddit.getKey());
        subreddits.removeValue(new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                if (databaseError == null) {
                    Toast.makeText(PostsActivity.this, R.string.subreddit_removed, Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    Toast.makeText(PostsActivity.this, R.string.couldnt_remove_subreddit, Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

}
