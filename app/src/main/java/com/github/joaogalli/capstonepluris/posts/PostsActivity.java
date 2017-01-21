package com.github.joaogalli.capstonepluris.posts;

import android.app.LoaderManager;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.OnScrollListener;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.github.joaogalli.capstonepluris.FirebaseUtils;
import com.github.joaogalli.capstonepluris.R;
import com.github.joaogalli.capstonepluris.contentprovider.PostsProvider;
import com.github.joaogalli.capstonepluris.model.Subreddit;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

/**
 * Codes from https://developer.android.com/guide/components/loaders.html
 */
public class PostsActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final String TAG = PostsActivity.class.getSimpleName();

    public static final String SUBREDDIT_PARAM = "subredditParam";
    private Subreddit subreddit;

    private DatabaseReference mDatabase;

    private PostsRecyclerViewAdapter adapter;

    private LinearLayoutManager mLayoutManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_posts);

        Intent intent = getIntent();

        if (intent != null && intent.getParcelableExtra(SUBREDDIT_PARAM) != null) {
            subreddit = intent.getParcelableExtra(SUBREDDIT_PARAM);
        } else {
            finishActivity(RESULT_CANCELED);
        }

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setTitle(subreddit.getTitle());

        mDatabase = FirebaseDatabase.getInstance().getReference();

        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        mLayoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(mLayoutManager);
        DividerItemDecoration decoration = new DividerItemDecoration(recyclerView.getContext(), DividerItemDecoration.VERTICAL);
        recyclerView.addItemDecoration(decoration);
        recyclerView.addOnScrollListener(new OnScrollListener() {

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                if (dy > 0) {
                    int visibleItemCount = mLayoutManager.getChildCount();
                    int totalItemCount = mLayoutManager.getItemCount();
                    int pastVisibleItems = mLayoutManager.findFirstVisibleItemPosition();

                    if ((visibleItemCount + pastVisibleItems) >= totalItemCount) {
                        String lastItemIdInReddit = adapter.getLastItemIdInReddit();
                        if (lastItemIdInReddit != null) {
                            new PostsBySubredditAsyncTask(PostsActivity.this, PostsBySubredditAsyncTask.SearchType.OLDER_THAN).execute("space", lastItemIdInReddit);
                        }
                    }
                }

            }
        });

        adapter = new PostsRecyclerViewAdapter(this);
        recyclerView.setAdapter(adapter);

        getLoaderManager().initLoader(0, null, this);

        // First load
        // TODO optimize this
        new PostsBySubredditAsyncTask(PostsActivity.this).execute("space");
    }

    public void onClick(View view) {
//        switch(view.getId()) {
//            case R.id.load: {
//                Cursor cursor = getContentResolver().query(PostsProvider.CONTENT_URI, null, null, null, null);
//                Toast.makeText(this, "Count: " + cursor.getCount(), Toast.LENGTH_SHORT).show();
//                cursor.close();
//            }
//            break;
//            case R.id.insert: {
//                new PostsBySubredditAsyncTask(this).execute("space");
//            }
//            break;
//        }
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
        } else if (id == R.id.action_count) {
                Cursor cursor = getContentResolver().query(PostsProvider.CONTENT_URI, null, null, null, null);
                Toast.makeText(this, "Count: " + cursor.getCount(), Toast.LENGTH_SHORT).show();
                cursor.close();
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

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        Uri baseUri;
//        if (mCurFilter != null) {
//            baseUri = Uri.withAppendedPath(PostsProvider.CONTENT_URI,
//                    Uri.encode(mCurFilter));
//        } else {
        baseUri = PostsProvider.CONTENT_URI;
//        }

        // Now create and return a CursorLoader that will take care of
        // creating a Cursor for the data being displayed.
//        String select = "((" + Contacts.DISPLAY_NAME + " NOTNULL) AND ("
//                + Contacts.HAS_PHONE_NUMBER + "=1) AND ("
//                + Contacts.DISPLAY_NAME + " != '' ))";

        // new String[] {PostColumns.TITLE}
        return new CursorLoader(this, baseUri, null, null, null, null);

//        return new CursorLoader(getActivity(), baseUri,
//                CONTACTS_SUMMARY_PROJECTION, select, null,
//                Contacts.DISPLAY_NAME + " COLLATE LOCALIZED ASC");
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        int count = cursor.getCount();
        adapter.swapCursor(cursor);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        adapter.swapCursor(null);
    }
}
