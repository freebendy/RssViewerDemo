package com.roadmap.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

class DatabaseHelper extends SQLiteOpenHelper {
    
    private static final String LOG_TAG = "roadmap.DatabaseHelper";
    
    static final String DATABASE_NAME = "feedsdb";
    static final String DATABASE_TABLE = "feeds";
    static final int DATABASE_VERSION = 1;
    
    private static final String DATABASE_CREATE =
        "create table " + DATABASE_TABLE
        + "(_id integer primary key autoincrement, "
        + "title text not null, link text not null, "
        + "source text not null, category text not null, "
        + "date integer not null, description text not null, "
        + "imageurl text not null, imagetext text not null );";

    DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

        db.execSQL(DATABASE_CREATE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.w(LOG_TAG, "Upgrading database from version " + oldVersion + " to "
                + newVersion + ", which will destroy all old data");
        db.execSQL("DROP TABLE IF EXISTS feeds");
        onCreate(db);
    }
}
