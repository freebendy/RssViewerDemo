package com.roadmap.db;

import java.util.List;

import com.roadmap.NewsViewer.FeedColumns;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

public class FeedsDbAdapter {
    
    private static final String LOG_TAG = "roadmap.FeedsDbAdapter";
    
    private DatabaseHelper mDbHelper;
    private SQLiteDatabase mDb;

    private final Context mCtx;

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
     * Clear the table
     * 
     * @return true if the table was clear, false otherwise
     */
    public boolean clear() {
        Log.v(LOG_TAG, "clear");
        return mDb.delete(DatabaseHelper.DATABASE_TABLE, null, null) > 0;
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
        initialValues.put(FeedColumns.TITLE, message.getTitle());
        initialValues.put(FeedColumns.LINK, message.getLink());
        initialValues.put(FeedColumns.SOURCE, message.getSource());
        initialValues.put(FeedColumns.CATEGORY, message.getCategory());
        initialValues.put(FeedColumns.DATE, message.getDate());
        initialValues.put(FeedColumns.DESCRIPTION, message.getDescription());
        initialValues.put(FeedColumns.IMAGEURL, message.getImageUrl());
        initialValues.put(FeedColumns.IMAGETEXT, message.getImageText());

        return mDb.insert(DatabaseHelper.DATABASE_TABLE, null, initialValues);
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
            initialValues.put(FeedColumns.TITLE, message.getTitle());
            initialValues.put(FeedColumns.LINK, message.getLink());
            initialValues.put(FeedColumns.SOURCE, message.getSource());
            initialValues.put(FeedColumns.CATEGORY, message.getCategory());
            initialValues.put(FeedColumns.DATE, message.getDate());
            initialValues.put(FeedColumns.DESCRIPTION, message.getDescription());
            initialValues.put(FeedColumns.IMAGEURL, message.getImageUrl());
            initialValues.put(FeedColumns.IMAGETEXT, message.getImageText());
            
            if (mDb.insert(DatabaseHelper.DATABASE_TABLE, null, initialValues) == -1) {
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

        return mDb.delete(DatabaseHelper.DATABASE_TABLE, 
                FeedColumns._ID + "=" + rowId, null) > 0;
    }

    /**
     * Return a Cursor over the list of all messages in the database
     * 
     * @return Cursor over all messages
     */
    public Cursor fetchAllMessages() {
        Log.v(LOG_TAG, "fetchAllMessages");
        return mDb.query(DatabaseHelper.DATABASE_TABLE, 
                FeedColumns.ALL_COLUMNS, null, null, null, null, null);
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
            mDb.query(true, DatabaseHelper.DATABASE_TABLE, 
                    FeedColumns.ALL_COLUMNS, FeedColumns._ID + "=" + rowId,
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
        args.put(FeedColumns.TITLE, message.getTitle());
        args.put(FeedColumns.LINK, message.getLink());
        args.put(FeedColumns.SOURCE, message.getSource());
        args.put(FeedColumns.CATEGORY, message.getCategory());
        args.put(FeedColumns.DATE, message.getDate());
        args.put(FeedColumns.DESCRIPTION, message.getDescription());
        args.put(FeedColumns.IMAGEURL, message.getImageUrl());
        args.put(FeedColumns.IMAGETEXT, message.getImageText());

        return mDb.update(DatabaseHelper.DATABASE_TABLE, args, 
                FeedColumns._ID + "=" + rowId, null) > 0;
    }
    
}
