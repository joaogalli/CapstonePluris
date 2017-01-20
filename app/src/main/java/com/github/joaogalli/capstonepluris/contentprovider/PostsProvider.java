package com.github.joaogalli.capstonepluris.contentprovider;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.database.SQLException;
import android.net.Uri;
import android.support.annotation.Nullable;

import com.github.joaogalli.capstonepluris.model.Post;
import com.github.joaogalli.capstonepluris.model.PostColumns;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by joao.galli on 20/01/2017.
 */

public class PostsProvider extends ContentProvider {
    static final String PROVIDER_NAME = "com.github.joaogalli.capstonepluris.PostsProvider";
    static final String URL = "content://" + PROVIDER_NAME + "/posts";
    public static final Uri CONTENT_URI = Uri.parse(URL);

    private List<Post> list;

    static final int POSTS = 1;
    static final int POST_ID = 2;

    static final UriMatcher uriMatcher;

    static {
        uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        uriMatcher.addURI(PROVIDER_NAME, "posts", POSTS);
        uriMatcher.addURI(PROVIDER_NAME, "posts/#", POST_ID);
    }

    @Override
    public boolean onCreate() {
        list = new ArrayList<>();

        list.add(new Post(1, "Lorem ipsum dolor sit amet, consectetur adipiscing elit."));
        list.add(new Post(2, "Nulla urna nisl, elementum a sem id, imperdiet faucibus nulla. Praesent fringilla condimentum accumsan."));

        return true;
    }

    @Nullable
    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        MatrixCursor cursor = new MatrixCursor(new String[]{"_id", "title"}, list.size());

        for (Post post : list) {
            cursor.addRow(new Object[]{post.get_id(), post.getTitle()});
        }

        return cursor;
    }

    @Nullable
    @Override
    public String getType(Uri uri) {
        switch (uriMatcher.match(uri)) {
            /**
             * Get all student records
             */
            case POSTS:
                return "vnd.android.cursor.dir/posts";
            /**
             * Get a particular student
             */
            case POST_ID:
                return "vnd.android.cursor.item/posts";
            default:
                throw new IllegalArgumentException("Unsupported URI: " + uri);
        }
    }

    @Nullable
    @Override
    public Uri insert(Uri uri, ContentValues values) {
        int id = list.size();
        Post post = new Post(id, values.getAsString(PostColumns.TITLE));
        boolean rowId = list.add(post);

        if (rowId) {
            Uri _uri = ContentUris.withAppendedId(CONTENT_URI, id);
            getContext().getContentResolver().notifyChange(_uri, null);
            return _uri;
        }

        throw new SQLException("Failed to add a record into " + uri);
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        return 0;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        return 0;
    }
}
