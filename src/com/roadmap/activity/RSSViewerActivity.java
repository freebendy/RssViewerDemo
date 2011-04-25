package com.roadmap.activity;

import com.roadmap.NewsViewer.FeedColumns;
import com.roadmap.R;

import android.app.ListActivity;
import android.content.ContentUris;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ListView;

public class RSSViewerActivity extends ListActivity {

    private static final String LOG_TAG = "roadmap.RSSViewerActivity";

    /**
     * The columns we are interested in from the database
     */
    private static final String[] PROJECTION = new String[] {
        FeedColumns._ID, // 0
        FeedColumns.TITLE, // 1
        FeedColumns.IMAGEURL, // 2
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.v(LOG_TAG, "onCreate");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.feeds_list);

        // If no data was given in the intent (because we were started
        // as a MAIN activity), then use our default content provider.
        Intent intent = getIntent();
        if (intent.getData() == null) {
            intent.setData(FeedColumns.CONTENT_URI);
        }

        // Perform a managed query. The Activity will handle closing and requerying the cursor
        // when needed.
        Cursor cursor = managedQuery(intent.getData(), PROJECTION, null, null,
                FeedColumns.DEFAULT_SORT_ORDER);

        // Used to map rss entries from the database to views

        RSSListAdapter adapter = new RSSListAdapter(this, cursor);

        setListAdapter(adapter);
    }

    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        Log.v(LOG_TAG, "onListItemClick - id: " + id);

        Uri uri = ContentUris.withAppendedId(getIntent().getData(), id);
        Cursor cursor = managedQuery(uri, new String[] {FeedColumns.LINK},
                null, null, null);

        if (cursor != null && cursor.moveToFirst()) {
            String link = cursor.getString(cursor.getColumnIndex(FeedColumns.LINK));
            Intent intent = new Intent("android.intent.action.VIEW",Uri.parse(link));
            startActivity(intent);
        }
    }

}
