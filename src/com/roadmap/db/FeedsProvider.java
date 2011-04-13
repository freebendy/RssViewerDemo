package com.roadmap.db;

import java.util.HashMap;

import com.roadmap.NewsViewer;
import com.roadmap.NewsViewer.FeedColumns;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;

public class FeedsProvider extends ContentProvider {

    private static final String LOG_TAG = "roadmap.FeedsProvider";
    
    private static final int FEEDS = 1;
    private static final int FEED_ID = 2;
    
    private static UriMatcher mUriMatcher;
    
    private static HashMap<String, String> mFeedsProjectionMap;
    
    private DatabaseHelper mDatabaseHelper;
    
    @Override
    public boolean onCreate() {
        Log.v(LOG_TAG, "onCreate");
        mDatabaseHelper = new DatabaseHelper(getContext());
        return true;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection,
            String[] selectionArgs, String sortOrder) {
        Log.v(LOG_TAG, "query");

        SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
        qb.setTables(DatabaseHelper.DATABASE_TABLE);

        switch (mUriMatcher.match(uri)) {
        case FEEDS:
            qb.setProjectionMap(mFeedsProjectionMap);
            break;

        case FEED_ID:
            qb.setProjectionMap(mFeedsProjectionMap);
            qb.appendWhere(FeedColumns._ID + "=" + uri.getPathSegments().get(1));
            break;

        default:
            throw new IllegalArgumentException("Unknown URI " + uri);
        }

        // If no sort order is specified use the default
        String orderBy;
        if (TextUtils.isEmpty(sortOrder)) {
            orderBy = FeedColumns.DEFAULT_SORT_ORDER;
        } else {
            orderBy = sortOrder;
        }

        // Get the database and run the query
        SQLiteDatabase db = mDatabaseHelper.getReadableDatabase();
        Cursor cursor = qb.query(db, projection, selection, selectionArgs, null, null, orderBy);

        // Tell the cursor what uri to watch, so it knows when its source data changes
        cursor.setNotificationUri(getContext().getContentResolver(), uri);
        return cursor;
    }

    @Override
    public String getType(Uri uri) {
        Log.v(LOG_TAG, "getType");
        switch (mUriMatcher.match(uri)) {
        case FEEDS:
            return FeedColumns.CONTENT_TYPE;

        case FEED_ID:
            return FeedColumns.CONTENT_ITEM_TYPE;

        default:
            throw new IllegalArgumentException("Unknown URI " + uri);
        }
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        Log.v(LOG_TAG, "insert");
        
        // Validate the requested uri
        if (mUriMatcher.match(uri) != FEEDS) {
            throw new IllegalArgumentException("Unknown URI " + uri);
        }
        
        if (values != null) {
            SQLiteDatabase db = mDatabaseHelper.getWritableDatabase();
            long rowId = db.insert(DatabaseHelper.DATABASE_TABLE, null, values);
            if (rowId > 0) {
                Uri noteUri = ContentUris.withAppendedId(FeedColumns.CONTENT_URI, rowId);
                getContext().getContentResolver().notifyChange(noteUri, null);
                return noteUri;
            }
        }

        throw new SQLException("Failed to insert row into " + uri);
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        Log.v(LOG_TAG, "delete");
        SQLiteDatabase db = mDatabaseHelper.getWritableDatabase();
        int count;
        switch (mUriMatcher.match(uri)) {
        case FEEDS:
            count = db.delete(DatabaseHelper.DATABASE_TABLE, selection, selectionArgs);
            break;

        case FEED_ID:
            String noteId = uri.getPathSegments().get(1);
            count = db.delete(DatabaseHelper.DATABASE_TABLE, FeedColumns._ID + "=" + noteId
                    + (!TextUtils.isEmpty(selection) ? " AND (" + selection + ')' : ""), selectionArgs);
            break;

        default:
            throw new IllegalArgumentException("Unknown URI " + uri);
        }

        getContext().getContentResolver().notifyChange(uri, null);
        return count;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection,
            String[] selectionArgs) {
        Log.v(LOG_TAG, "update");
        SQLiteDatabase db = mDatabaseHelper.getWritableDatabase();
        int count;
        switch (mUriMatcher.match(uri)) {
        case FEEDS:
            count = db.update(DatabaseHelper.DATABASE_TABLE, values, selection, selectionArgs);
            break;

        case FEED_ID:
            String noteId = uri.getPathSegments().get(1);
            count = db.update(DatabaseHelper.DATABASE_TABLE, values, FeedColumns._ID + "=" + noteId
                    + (!TextUtils.isEmpty(selection) ? " AND (" + selection + ')' : ""), selectionArgs);
            break;

        default:
            throw new IllegalArgumentException("Unknown URI " + uri);
        }

        getContext().getContentResolver().notifyChange(uri, null);
        return count;
    }
    
    static {
        mUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        mUriMatcher.addURI(NewsViewer.AUTHORITY, "feeds", FEEDS);
        mUriMatcher.addURI(NewsViewer.AUTHORITY, "feeds/#", FEED_ID);
        
        mFeedsProjectionMap = new HashMap<String, String>();
        mFeedsProjectionMap.put(FeedColumns._ID, FeedColumns._ID);
        mFeedsProjectionMap.put(FeedColumns.TITLE, FeedColumns.TITLE);
        mFeedsProjectionMap.put(FeedColumns.LINK, FeedColumns.LINK);
        mFeedsProjectionMap.put(FeedColumns.SOURCE, FeedColumns.SOURCE);
        mFeedsProjectionMap.put(FeedColumns.CATEGORY, FeedColumns.CATEGORY);
        mFeedsProjectionMap.put(FeedColumns.DATE, FeedColumns.DATE);
        mFeedsProjectionMap.put(FeedColumns.DESCRIPTION, FeedColumns.DESCRIPTION);
        mFeedsProjectionMap.put(FeedColumns.IMAGETEXT, FeedColumns.IMAGETEXT);
        mFeedsProjectionMap.put(FeedColumns.IMAGEURL, FeedColumns.IMAGEURL);
    }

}
