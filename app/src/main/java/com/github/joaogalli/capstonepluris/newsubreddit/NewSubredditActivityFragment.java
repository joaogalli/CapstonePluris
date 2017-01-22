package com.github.joaogalli.capstonepluris.newsubreddit;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.github.joaogalli.capstonepluris.FirebaseUtils;
import com.github.joaogalli.capstonepluris.R;
import com.github.joaogalli.capstonepluris.SignInActivity;
import com.github.joaogalli.capstonepluris.contentprovider.PostsProvider;
import com.github.joaogalli.capstonepluris.model.PostColumns;
import com.github.joaogalli.capstonepluris.model.Subreddit;
import com.github.joaogalli.capstonepluris.service.SubredditFirebaseService;
import com.github.joaogalli.capstonepluris.subredditlist.ListActivityInteraction;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;

/**
 * A placeholder fragment containing a simple view.
 */
public class NewSubredditActivityFragment extends Fragment implements ListActivityInteraction {

    private static final String TAG = NewSubredditActivity.class.getSimpleName();

    private SubredditSearchRecyclerViewAdapter mAdapter;

    private Toast toast;

    private ProgressBar progressBar;

    private String mQueryString;
    private Handler mHandler;

    private SubredditFirebaseService subredditFirebaseService;

    public NewSubredditActivityFragment() {
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        subredditFirebaseService = new SubredditFirebaseService(getContext());

        setHasOptionsMenu(true);
        mHandler = new Handler();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_new_subreddit, container, false);

        toast = Toast.makeText(getContext(), null, Toast.LENGTH_SHORT);

        progressBar = (ProgressBar) view.findViewById(R.id.progressBar);

        final RecyclerView recyclerView = (RecyclerView) view.findViewById(R.id.list);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        mAdapter = new SubredditSearchRecyclerViewAdapter(subredditFirebaseService, this);
        recyclerView.setAdapter(mAdapter);
        recyclerView.addItemDecoration(new DividerItemDecoration(recyclerView.getContext(), DividerItemDecoration.VERTICAL));

        return view;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater menuInflater) {
        menu.clear();
        menuInflater.inflate(R.menu.contact_list_menu, menu);

        MenuItem searchItem = menu.findItem(R.id.contact_list_menu_item_search);

        SearchView mSearchView = (SearchView) searchItem.getActionView();
        mSearchView.setMaxWidth(Integer.MAX_VALUE);
        mSearchView.setSubmitButtonEnabled(true);
        mSearchView.setIconifiedByDefault(false);
        mSearchView.setIconified(false);

        mSearchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                if (validate(query)) {
                    search(query);
                }
                return false;
            }

            @Override
            public boolean onQueryTextChange(final String query) {
                mQueryString = query;
                mHandler.removeCallbacksAndMessages(null);

                mHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        //Put your call to the server here (with mQueryString)
                        search(mQueryString);
                    }
                }, 300);
                return true;
            }
        });

        mSearchView.requestFocus();
    }

    private boolean validate(String subredditName) {
        if (subredditName == null || subredditName.isEmpty()) {
            Toast.makeText(getActivity(), R.string.subreddit_search_validation_empty, Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    private void search(String query) {
        new SearchSubredditAsyncTask(mAdapter, progressBar) {
            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                progressBar.setVisibility(View.VISIBLE);
            }

            @Override
            protected void onPostExecute(Subreddit[] subreddits) {
                progressBar.setVisibility(View.GONE);
                super.onPostExecute(subreddits);
            }
        }.execute(query);
    }

    @Override
    public void onListActivityInteraction(final Subreddit subreddit) {
        subredditFirebaseService.exists(subreddit, new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.getValue() == null) {
                    subredditFirebaseService.add(subreddit);
                    toast.setText(R.string.subreddit_added_to_your_list);
                } else {
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        Subreddit child = snapshot.getValue(Subreddit.class);
                        int deletedPosts = getContext().getContentResolver()
                                .delete(PostsProvider.CONTENT_URI, PostColumns.SUBREDDIT + " = ?", new String[]{subreddit.getDisplayName()});
                        subredditFirebaseService.remove(snapshot, null);
                        toast.setText(R.string.subreddit_removed_from_your_list);
                    }

                }
                toast.show();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

}
