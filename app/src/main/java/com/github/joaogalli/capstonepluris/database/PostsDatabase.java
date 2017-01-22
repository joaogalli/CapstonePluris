package com.github.joaogalli.capstonepluris.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.github.joaogalli.capstonepluris.model.PostColumns;

/**
 * Created by joao.galli on 20/01/2017.
 */

public class PostsDatabase extends SQLiteOpenHelper {

    static final String DATABASE_NAME = "Pluris";
    public static final String POSTS_TABLE_NAME = "posts";
    static final int DATABASE_VERSION = 5;
    static final String CREATE_POSTS_TABLE =
            " CREATE TABLE " + POSTS_TABLE_NAME +
                    " (" + PostColumns.ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    PostColumns.TITLE + " TEXT NOT NULL, " +
                    PostColumns.SUBREDDIT + " TEXT NOT NULL, " +
                    PostColumns.ID_IN_REDDIT + " TEXT UNIQUE NOT NULL," +
                    PostColumns.URL + " TEXT, " +
                    PostColumns.KIND + " TEXT " +
                    ");";

    public PostsDatabase(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_POSTS_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + PostsDatabase.POSTS_TABLE_NAME);
        db.execSQL(CREATE_POSTS_TABLE);
    }
}
