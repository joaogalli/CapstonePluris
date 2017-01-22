package com.github.joaogalli.capstonepluris.posts;

import android.content.Context;
import android.content.Intent;
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
 * Created by joaog on 21/01/2017.
 */

public class PostsRecyclerViewAdapter extends RecyclerView.Adapter<PostsRecyclerViewAdapter.ViewHolder> {

    private Context mContext;

    private Cursor cursor;

    public PostsRecyclerViewAdapter(Context mContext) {
        this.mContext = mContext;
    }

    public void swapCursor(Cursor cursor) {
        this.cursor = cursor;
        notifyDataSetChanged();
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.post_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        cursor.moveToPosition(position);

        holder.mTitleView.setText(cursor.getString(cursor.getColumnIndex(PostColumns.TITLE)));

        final String postHint = cursor.getString(cursor.getColumnIndex(PostColumns.POST_HINT));
        holder.mTypeImage.setImageDrawable(mContext.getResources().getDrawable(
                getImage(postHint)));

        final String url = cursor.getString(cursor.getColumnIndex(PostColumns.URL));
        holder.mView.setOnClickListener(new PostOnClickListener(url));
    }

    class PostOnClickListener implements View.OnClickListener {
        private String url;

        public PostOnClickListener(String url) {
            this.url = url;
        }

        @Override
        public void onClick(View view) {
            Intent intent = new Intent(mContext, WebViewActivity.class);
            intent.putExtra(WebViewActivity.URL_PARAM, url);
            mContext.startActivity(intent);
        }
    }

    private int getImage(String postHint) {
        if (postHint != null) {
            switch (postHint) {
                case "image":
                    return R.drawable.hint_image;
                case "rich:video":
                    return R.drawable.hint_video;
                default:
                    return R.drawable.hint_link;
            }
        } else {
            return R.drawable.hint_link;
        }
    }

    @Override
    public int getItemCount() {
        return (cursor != null) ? cursor.getCount() : 0;
    }

    public String getLastItemIdInReddit() {
        try {
            cursor.moveToLast();
            return cursor.getString(cursor.getColumnIndex(PostColumns.ID_IN_REDDIT));
        } catch (Throwable t) {
            return null;
        }
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
