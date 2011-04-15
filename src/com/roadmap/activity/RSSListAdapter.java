package com.roadmap.activity;

import com.roadmap.NewsViewer.FeedColumns;
import com.roadmap.R;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class RSSListAdapter extends BaseAdapter {
    
    private Cursor mCursor;
    
    private Context mContext;
    
    private final ImageDownloader imageDownloader;
    
    public RSSListAdapter(Context aContext, Cursor aCursor) {
        super();
        mContext = aContext;
        imageDownloader = new ImageDownloader(mContext);
        mCursor = aCursor;
    }

    public int getCount() {
        if (mCursor != null) {
            return mCursor.getCount();
        } else {
            return 0;
        }
    }

    public Cursor getItem(int aPosition) {
        return mCursor != null && mCursor.moveToPosition(aPosition) ? mCursor : null;
    }

    public long getItemId(int aPosition) {
        return mCursor != null && mCursor.moveToPosition(aPosition) ? 
                mCursor.getLong(mCursor.getColumnIndex(FeedColumns._ID)) : 0;
    }

    public View getView(int aPosition, View aConvertView, ViewGroup aParent) {
        if (aConvertView == null) {
            
            LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            aConvertView = inflater.inflate(R.layout.feeds_row, aParent, false);
        }

        TextView titleText = (TextView) aConvertView.findViewById(R.id.title);
        titleText.setText(getItem(aPosition).getString(mCursor.getColumnIndex(FeedColumns.TITLE)));
        
        imageDownloader.download(getItem(aPosition).getString(mCursor.getColumnIndex(FeedColumns.IMAGEURL)), 
                (ImageView) aConvertView.findViewById(R.id.image));
        
        return aConvertView;
    }

}
