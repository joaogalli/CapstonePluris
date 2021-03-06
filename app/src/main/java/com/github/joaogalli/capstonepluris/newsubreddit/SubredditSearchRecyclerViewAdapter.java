package com.github.joaogalli.capstonepluris.newsubreddit;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.github.joaogalli.capstonepluris.R;
import com.github.joaogalli.capstonepluris.model.Subreddit;
import com.github.joaogalli.capstonepluris.service.SubredditFirebaseService;
import com.github.joaogalli.capstonepluris.subredditlist.ListActivityInteraction;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by joaog on 15/01/2017.
 */

public class SubredditSearchRecyclerViewAdapter extends RecyclerView.Adapter<SubredditSearchRecyclerViewAdapter.ViewHolder> {

    private List<Subreddit> mValues = new ArrayList<>();
    private SubredditFirebaseService subredditFirebaseService;
    private ListActivityInteraction mListener;

    public SubredditSearchRecyclerViewAdapter(SubredditFirebaseService subredditFirebaseService, ListActivityInteraction listener) {
        this.subredditFirebaseService = subredditFirebaseService;
        this.mListener = listener;
    }

    @Override
    public SubredditSearchRecyclerViewAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.subreddit_search_item, parent, false);
        return new SubredditSearchRecyclerViewAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final SubredditSearchRecyclerViewAdapter.ViewHolder holder, int position) {
        holder.mItem = mValues.get(position);
        holder.mTitleView.setText(mValues.get(position).getTitle());
        holder.mDisplayNameView.setText("/r/" + mValues.get(position).getDisplayName());

        subredditFirebaseService.exists(holder.mItem, new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                holder.mSelectedView.setVisibility(
                        (dataSnapshot.getValue() == null) ? View.INVISIBLE : View.VISIBLE );
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        holder.mView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (null != mListener) {
                    // Notify the active callbacks interface (the activity, if the
                    // fragment is attached to one) that an item has been selected.
                    mListener.onListActivityInteraction(holder.mItem);
                    holder.mSelectedView.setVisibility(holder.mSelectedView.getVisibility() == View.VISIBLE ? View.INVISIBLE : View.VISIBLE );
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return mValues.size();
    }

    public void setValues(List<Subreddit> mValues) {
        this.mValues = mValues;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public final View mView;
        public final TextView mTitleView;
        public final TextView mDisplayNameView;
        public final ImageView mSelectedView;
        public Subreddit mItem;

        public ViewHolder(View view) {
            super(view);
            mView = view;
            mTitleView = (TextView) view.findViewById(R.id.title);
            mDisplayNameView = (TextView) view.findViewById(R.id.displayName);
            mSelectedView = (ImageView) view.findViewById(R.id.selected);
        }

        @Override
        public String toString() {
            return super.toString() + " '" + mTitleView.getText() + "'";
        }
    }
}
