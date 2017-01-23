package com.github.joaogalli.capstonepluris.widget;

import android.content.Context;
import android.database.Cursor;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.github.joaogalli.capstonepluris.R;
import com.github.joaogalli.capstonepluris.model.PostColumns;

/**
 * Created by joaog on 22/01/2017.
 */

public class PostCursorAdapter extends RecyclerView.Adapter<PostCursorAdapter.ViewHolder> {
    public static final String ACTION_DATA_UPDATED =
            "com.github.joaogalli.capstonepluris.ACTION_DATA_UPDATED";

    private Context mContext;

    private Cursor mCursor;

    public PostCursorAdapter(Context mContext, Cursor mCursor) {
        this.mContext = mContext;
        this.mCursor = mCursor;
    }

    @Override
    public PostCursorAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.widget_post_item, parent, false);
        return new PostCursorAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(PostCursorAdapter.ViewHolder holder, int position) {
        mCursor.moveToPosition(position);

        holder.mTitleView.setText(mCursor.getString(mCursor.getColumnIndex(PostColumns.TITLE)));
    }

    @Override
    public int getItemCount() {
        if (mCursor != null)
            return mCursor.getCount();
        return 0;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public final View mView;
        public final TextView mTitleView;
        public final ImageView mTypeImage;

        public ViewHolder(View view) {
            super(view);
            mView = view;
            mTitleView = (TextView) view.findViewById(R.id.title);
            mTypeImage = (ImageView) view.findViewById(R.id.typeImageView);
        }

        @Override
        public String toString() {
            return super.toString() + " '" + mTitleView.getText() + "'";
        }
    }
}
