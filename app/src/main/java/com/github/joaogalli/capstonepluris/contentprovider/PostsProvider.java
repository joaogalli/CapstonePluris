package com.github.joaogalli.capstonepluris.contentprovider;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.support.annotation.Nullable;

import com.github.joaogalli.capstonepluris.database.PostsDatabase;
import com.github.joaogalli.capstonepluris.model.PostColumns;

import java.util.HashMap;

/**
 * Created by joao.galli on 20/01/2017.
 */

public class PostsProvider extends ContentProvider {
    static final String PROVIDER_NAME = "com.github.joaogalli.capstonepluris.PostsProvider";
    static final String URL = "content://" + PROVIDER_NAME + "/posts";
    public static final Uri CONTENT_URI = Uri.parse(URL);

    static final int POSTS = 1;
    static final int POST_ID = 2;

    static final UriMatcher uriMatcher;

    static {
        uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        uriMatcher.addURI(PROVIDER_NAME, "posts", POSTS);
        uriMatcher.addURI(PROVIDER_NAME, "posts/#", POST_ID);
    }

    private PostsDatabase postsDatabase;
    private SQLiteDatabase db;

    private static HashMap<String, String> POSTS_PROJECTION_MAP;

    @Override
    public boolean onCreate() {
        postsDatabase = new PostsDatabase(getContext());
        db = postsDatabase.getWritableDatabase();
        return (db != null);
    }

    @Nullable
    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
        qb.setTables(PostsDatabase.POSTS_TABLE_NAME);

        switch (uriMatcher.match(uri)) {
            case POSTS:
                qb.setProjectionMap(POSTS_PROJECTION_MAP);
                break;
            case POST_ID:
                qb.appendWhere(PostColumns.ID + "=" + uri.getPathSegments().get(1));
                break;
        }

//        if (sortOrder == null || sortOrder == "") {
//            // TODO sort by date...
//            sortOrder = PostColumns.ID;
//        }

        Cursor c = qb.query(db, projection, selection,
                selectionArgs, null, null, sortOrder);
        /**
         * register to watch a content URI for changes
         */
        c.setNotificationUri(getContext().getContentResolver(), uri);
        return c;
    }

    @Nullable
    @Override
    public Uri insert(Uri uri, ContentValues values) {
        long rowID = db.insert(PostsDatabase.POSTS_TABLE_NAME, "", values);

        if (rowID > 0) {
            Uri _uri = ContentUris.withAppendedId(CONTENT_URI, rowID);
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

}
