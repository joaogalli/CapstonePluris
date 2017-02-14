package com.github.joaogalli.capstonepluris.posts;

import android.app.LoaderManager;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.OnScrollListener;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.github.joaogalli.capstonepluris.FirebaseUtils;
import com.github.joaogalli.capstonepluris.R;
import com.github.joaogalli.capstonepluris.contentprovider.PostsProvider;
import com.github.joaogalli.capstonepluris.model.PostColumns;
import com.github.joaogalli.capstonepluris.model.Subreddit;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Date;

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

    private SwipeRefreshLayout swipeRefreshLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_posts);

        Intent intent = getIntent();

        if (intent != null && intent.getParcelableExtra(SUBREDDIT_PARAM) != null) {
            subreddit = intent.getParcelableExtra(SUBREDDIT_PARAM);
        } else {
            finishActivity(RESULT_CANCELED);
            return;
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
                            swipeRefreshLayout.setRefreshing(true);
                            new PostsBySubredditAsyncTask(PostsActivity.this, PostsBySubredditAsyncTask.SearchType.OLDER_THAN) {
                                @Override
                                protected void onPostExecute(String jsonStr) {
                                    swipeRefreshLayout.setRefreshing(false);
                                    super.onPostExecute(jsonStr);
                                }
                            }.execute(subreddit.getDisplayName(), lastItemIdInReddit);
                        }
                    }
                }
            }
        });

        adapter = new PostsRecyclerViewAdapter(this);
        recyclerView.setAdapter(adapter);

        swipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipeRefresh);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refreshItems();
            }
        });

        getLoaderManager().initLoader(0, null, this);

        // First load
        // TODO optimize this
        refreshItems();
    }

    private void refreshItems() {
        swipeRefreshLayout.setRefreshing(true);

        new PostsBySubredditAsyncTask(PostsActivity.this) {
            @Override
            protected void onPostExecute(String jsonStr) {
                super.onPostExecute(jsonStr);
                swipeRefreshLayout.setRefreshing(false);
            }
        }.execute(subreddit.getDisplayName());
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
        } else if (id == R.id.action_zuar) {
            ContentValues values = new ContentValues();
            values.put(PostColumns.TITLE, new Date().toString());
            getContentResolver().update(PostsProvider.CONTENT_URI, values, PostColumns.SUBREDDIT + " = ?", new String[] { subreddit.getDisplayName() });
            this.getContentResolver().notifyChange(PostsProvider.CONTENT_URI, null);
        }

        return super.onOptionsItemSelected(item);
    }

    private void removeKey() {
        this.getContentResolver().delete(PostsProvider.CONTENT_URI, PostColumns.SUBREDDIT + " = ?", new String[]{subreddit.getDisplayName()});

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
        return new CursorLoader(this, PostsProvider.CONTENT_URI, null, PostColumns.SUBREDDIT + " = ?", new String[]{subreddit.getDisplayName()}, null);
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
