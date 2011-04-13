package com.roadmap.db;

import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class FeedsDbAdapter {
    
    private static final String LOG_TAG = "roadmap.FeedsDbAdapter";

    public static final String KEY_TITLE = "title";
    public static final String KEY_LINK = "link";
//    public static final String KEY_GUIID = "guid";
//    public static final String KEY_PERMALINK = "permalink";
    public static final String KEY_SOURCE = "source";
    public static final String KEY_CATEGORY = "category";
    public static final String KEY_DATE = "date";
    public static final String KEY_DESCRIPTION = "description";
    public static final String KEY_IMAGEURL = "imageurl";
    public static final String KEY_IMAGETEXT = "imagetext";
//    public static final String KEY_CREDIT = "credit";
//    public static final String KEY_CREDITROLE = "creditrole";
    public static final String KEY_ROWID = "_id";
    
    private String[] mAllColumns = new String[] {KEY_ROWID, KEY_TITLE,
            KEY_LINK, KEY_SOURCE, KEY_CATEGORY, KEY_DATE, KEY_DESCRIPTION, 
            KEY_IMAGEURL, KEY_IMAGETEXT};
    
    private DatabaseHelper mDbHelper;
    private SQLiteDatabase mDb;
    
    /**
     * Database creation sql statement
     */
    private static final String DATABASE_CREATE =
        "create table feeds (_id integer primary key autoincrement, "
        + "title text not null, link text not null, "
        + "source text not null, category text not null, "
        + "date text not null, description text not null, "
        + "imageurl text not null, imagetext text not null );";

    private static final String DATABASE_NAME = "feedsdb";
    private static final String DATABASE_TABLE = "feeds";
    private static final int DATABASE_VERSION = 1;

    private final Context mCtx;
    
    private static class DatabaseHelper extends SQLiteOpenHelper {

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

    /**
     * Constructor - takes the context to allow the database to be
     * opened/created
     * 
     * @param ctx the Context within which to work
     */
    public FeedsDbAdapter(Context ctx) {
        this.mCtx = ctx;
    }

    /**
     * Open the database. If it cannot be opened, try to create a new
     * instance of the database. If it cannot be created, throw an exception to
     * signal the failure
     * 
     * @return this (self reference, allowing this to be chained in an
     *         initialization call)
     * @throws SQLException if the database could be neither opened or created
     */
    public FeedsDbAdapter open() throws SQLException {
        Log.v(LOG_TAG, "open");
        mDbHelper = new DatabaseHelper(mCtx);
        mDb = mDbHelper.getWritableDatabase();
        return this;
    }

    public void close() {
        Log.v(LOG_TAG, "close");
        mDbHelper.close();
    }


    /**
     * Create a new message using the Message object provided. If the message is
     * successfully created return the new rowId for that message, otherwise 
     * return a -1 to indicate failure.
     * 
     * @param message the Message object of the message
     * @return rowId or -1 if failed
     */
    public long createMessage(Message message) {
        ContentValues initialValues = new ContentValues();
        initialValues.put(KEY_TITLE, message.getTitle());
        initialValues.put(KEY_LINK, message.getLink());
        initialValues.put(KEY_SOURCE, message.getSource());
        initialValues.put(KEY_CATEGORY, message.getCategory());
        initialValues.put(KEY_DATE, message.getDate());
        initialValues.put(KEY_DESCRIPTION, message.getDescription());
        initialValues.put(KEY_IMAGEURL, message.getImageUrl());
        initialValues.put(KEY_IMAGETEXT, message.getImageText());

        return mDb.insert(DATABASE_TABLE, null, initialValues);
    }
    
    /**
     * Create messages using the Message list object provided. If the messages 
     * are successfully created return true, otherwise return false to indicate 
     * failure.
     * 
     * @param messages the Message list object of the messages
     * @return true or false if failed
     */
    public boolean createMessages(List<Message> messages) {
        Log.v(LOG_TAG, "createMessages");
        if (messages != null && messages.isEmpty()) {
            return false;
        }
        
        mDb.beginTransaction();
        boolean ret = true;
        
        for (Message message : messages) {
            ContentValues initialValues = new ContentValues();
            initialValues.put(KEY_TITLE, message.getTitle());
            initialValues.put(KEY_LINK, message.getLink());
            initialValues.put(KEY_SOURCE, message.getSource());
            initialValues.put(KEY_CATEGORY, message.getCategory());
            initialValues.put(KEY_DATE, message.getDate());
            initialValues.put(KEY_DESCRIPTION, message.getDescription());
            initialValues.put(KEY_IMAGEURL, message.getImageUrl());
            initialValues.put(KEY_IMAGETEXT, message.getImageText());
            
            if (mDb.insert(DATABASE_TABLE, null, initialValues) == -1) {
                ret = false;
                break;
            }
        }
        
        if (ret) {
            mDb.setTransactionSuccessful();
        }
        mDb.endTransaction();

        return ret;
    }

    /**
     * Delete the message with the given rowId
     * 
     * @param rowId id of message to delete
     * @return true if deleted, false otherwise
     */
    public boolean deleteMessage(long rowId) {

        return mDb.delete(DATABASE_TABLE, KEY_ROWID + "=" + rowId, null) > 0;
    }

    /**
     * Return a Cursor over the list of all messages in the database
     * 
     * @return Cursor over all messages
     */
    public Cursor fetchAllMessages() {
        Log.v(LOG_TAG, "fetchAllMessages");
        return mDb.query(DATABASE_TABLE, mAllColumns, null, null, null, null, null);
    }

    /**
     * Return a Cursor positioned at the message that matches the given rowId
     * 
     * @param rowId id of message to retrieve
     * @return Cursor positioned to matching message, if found
     * @throws SQLException if message could not be found/retrieved
     */
    public Cursor fetchMessage(long rowId) throws SQLException {

        Cursor mCursor =
            mDb.query(true, DATABASE_TABLE, mAllColumns, KEY_ROWID + "=" + rowId,
                    null, null, null, null, null);
        if (mCursor != null) {
            mCursor.moveToFirst();
        }
        return mCursor;

    }

    /**
     * Update the message using the details provided. The message to be updated 
     * is specified using the rowId, and it is altered to use Message object
     * passed in
     * 
     * @param rowId id of note to update
     * @param message the Message object of the message
     * @return true if the message was successfully updated, false otherwise
     */
    public boolean updateMessage(long rowId, Message message) {
        ContentValues args = new ContentValues();
        args.put(KEY_TITLE, message.getTitle());
        args.put(KEY_LINK, message.getLink());
        args.put(KEY_SOURCE, message.getSource());
        args.put(KEY_CATEGORY, message.getCategory());
        args.put(KEY_DATE, message.getDate());
        args.put(KEY_DESCRIPTION, message.getDescription());
        args.put(KEY_IMAGEURL, message.getImageUrl());
        args.put(KEY_IMAGETEXT, message.getImageText());

        return mDb.update(DATABASE_TABLE, args, KEY_ROWID + "=" + rowId, null) > 0;
    }
    
    /**
     * Clear the table
     * 
     * @return true if the table was clear, false otherwise
     */
    public boolean clear() {
        Log.v(LOG_TAG, "clear");
        return mDb.delete(DATABASE_TABLE, null, null) > 0;
    }
}
