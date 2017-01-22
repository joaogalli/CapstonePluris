package com.github.joaogalli.capstonepluris.subredditlist;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.github.joaogalli.capstonepluris.FirebaseUtils;
import com.github.joaogalli.capstonepluris.R;
import com.github.joaogalli.capstonepluris.SignInActivity;
import com.github.joaogalli.capstonepluris.model.Subreddit;
import com.github.joaogalli.capstonepluris.newsubreddit.NewSubredditActivity;
import com.github.joaogalli.capstonepluris.posts.PostsActivity;
import com.github.joaogalli.capstonepluris.service.SubredditFirebaseService;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class SubredditListActivity extends AppCompatActivity implements ValueEventListener, ListActivityInteraction {

    private SubredditRecyclerViewAdapter mAdapter;

    private TextView emptyTextView;

    private GoogleApiClient mGoogleApiClient;

    private ProgressBar progressBar;

    private SubredditFirebaseService subredditFirebaseService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_subreddit_list);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(SubredditListActivity.this, NewSubredditActivity.class));
            }
        });

        subredditFirebaseService = new SubredditFirebaseService(this);
        subredditFirebaseService.registerForUidSubreddits(this);

        final RecyclerView recyclerView = (RecyclerView) findViewById(R.id.list);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        mAdapter = new SubredditRecyclerViewAdapter(this);
        recyclerView.setAdapter(mAdapter);

        emptyTextView = (TextView) findViewById(R.id.empty_view);

        mAdapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onChanged() {
                if (mAdapter.getItemCount() == 0) {
                    recyclerView.setVisibility(View.GONE);
                    emptyTextView.setVisibility(View.VISIBLE);
                } else {
                    recyclerView.setVisibility(View.VISIBLE);
                    emptyTextView.setVisibility(View.GONE);
                }
            }
        });

        recyclerView.addItemDecoration(new DividerItemDecoration(recyclerView.getContext(), DividerItemDecoration.VERTICAL));

        progressBar = (ProgressBar) findViewById(R.id.progressBar);
    }

    @Override
    public void onDataChange(DataSnapshot dataSnapshot) {
        progressBar.setVisibility(View.GONE);

        List<Subreddit> list = new ArrayList<>();

        for (DataSnapshot ds: dataSnapshot.getChildren()) {
            Subreddit subreddit = ds.getValue(Subreddit.class);
            subreddit.setKey(ds.getKey());
            list.add(subreddit);
        }

        mAdapter.setValues(list);
        mAdapter.notifyDataSetChanged();
    }

    @Override
    public void onCancelled(DatabaseError databaseError) {
        progressBar.setVisibility(View.GONE);
    }

    @Override
    public void onListActivityInteraction(Subreddit mItem) {
        Intent intent = new Intent(this, PostsActivity.class);
        intent.putExtra(PostsActivity.SUBREDDIT_PARAM, mItem);
        startActivity(intent);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_subreddit_list, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_logoff) {
            FirebaseUtils.logout(this);
            startActivity(new Intent(this, SignInActivity.class));
            finish();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

}
