package com.github.joaogalli.capstonepluris.newsubreddit;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
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
import com.github.joaogalli.capstonepluris.model.Subreddit;
import com.github.joaogalli.capstonepluris.subredditlist.ListActivityInteraction;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Map;

/**
 * A placeholder fragment containing a simple view.
 */
public class NewSubredditActivityFragment extends Fragment implements ListActivityInteraction {

    private DatabaseReference mDatabase;

    private EditText subredditNameEditText;

    private SubredditSearchRecyclerViewAdapter mAdapter;

    private Toast toast;

    private ProgressBar progressBar;

    public NewSubredditActivityFragment() {
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mDatabase = FirebaseDatabase.getInstance().getReference();

        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_new_subreddit, container, false);

        toast = Toast.makeText(getContext(), null, Toast.LENGTH_SHORT);

        progressBar = (ProgressBar) view.findViewById(R.id.progressBar);

//        subredditNameEditText = (EditText) view.findViewById(R.id.subredditField);
//        ((Button) view.findViewById(R.id.addButton)).setOnClickListener(this);
//
//        subredditNameEditText.setOnKeyListener(new View.OnKeyListener() {
//            @Override
//            public boolean onKey(View view, int i, KeyEvent keyEvent) {
//                search(subredditNameEditText.getText().toString());
//                return true;
//            }
//        });

        final RecyclerView recyclerView = (RecyclerView) view.findViewById(R.id.list);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        mAdapter = new SubredditSearchRecyclerViewAdapter(this);
        recyclerView.setAdapter(mAdapter);

        return view;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater menuInflater) {
//        super.onCreateOptionsMenu(menu, menuInflater);

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
            public boolean onQueryTextChange(String query) {
                if (validate(query)) {
                    search(query);
                }
                return false;
            }
        });

        mSearchView.requestFocus();
    }

    private boolean validate(String subredditName) {
        if (subredditName == null || subredditName.isEmpty()) {
            Toast.makeText(getActivity(), "You must fill a Subreddit name in the field.", Toast.LENGTH_SHORT).show();
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
    public void onListActivityInteraction(Subreddit subreddit) {
        add(subreddit);
        toast.setText(R.string.subreddit_added_to_your_list);
        toast.show();
    }

    private void add(Subreddit subreddit) {
        String uid = FirebaseUtils.getUidOrGoToLogin(getContext());

        String key = mDatabase.child("subreddits").child(uid).push().getKey();

        Map<String, Object> childUpdates = new HashMap<>();

        if (uid != null) {
            childUpdates.put("/subreddits/" + uid + "/" + key, subreddit.toMap());
            mDatabase.updateChildren(childUpdates);
        } else {
            Toast.makeText(getContext(), R.string.your_login_expired, Toast.LENGTH_SHORT).show();
            FirebaseUtils.logout(getContext());
            startActivity(new Intent(getContext(), SignInActivity.class));
        }
    }
}
