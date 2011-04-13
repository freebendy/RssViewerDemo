package com.roadmap.activity;

import com.roadmap.NewsViewer.FeedColumns;
import com.roadmap.R;

import android.app.ListActivity;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.widget.SimpleCursorAdapter;

public class RSSViewerActivity extends ListActivity {

    private static final String LOG_TAG = "roadmap.RSSViewerActivity";
    
    /**
     * The columns we are interested in from the database
     */
    private static final String[] PROJECTION = new String[] {
        FeedColumns._ID, // 0
        FeedColumns.TITLE, // 1
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
        Cursor cursor = managedQuery(getIntent().getData(), PROJECTION, null, null,
                FeedColumns.DEFAULT_SORT_ORDER);

        // Used to map notes entries from the database to views
        SimpleCursorAdapter adapter = 
            new SimpleCursorAdapter(this, R.layout.feeds_row, cursor,
                new String[] { FeedColumns.TITLE }, new int[] { R.id.title });
        setListAdapter(adapter);
    }
}
