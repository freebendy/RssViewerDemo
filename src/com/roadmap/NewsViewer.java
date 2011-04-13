package com.roadmap;

import android.net.Uri;
import android.provider.BaseColumns;

public final class NewsViewer {
    
    public static final String AUTHORITY = "com.roadmap.db.FeedsProvider";
    
    // This class cannot be instantiated
    private NewsViewer() {}
    
    /**
     * Notes table
     */
    public static final class FeedColumns implements BaseColumns {
        // This class cannot be instantiated
        private FeedColumns() {}
        
        /**
         * The content:// style URL for this table
         */
        public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/feeds");
        
        /**
         * The default sort order for this table
         */
        public static final String DEFAULT_SORT_ORDER = "date DESC";
        
        /**
         * The MIME type of {@link #CONTENT_URI} providing a directory of feeds.
         */
        public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.roadmap.feed";
        
        /**
         * The MIME type of a {@link #CONTENT_URI} sub-directory of a single feed.
         */
        public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.roadmap.feed";
        
        /**
         * The title of the feed
         * <P>Type: TEXT</P>
         */
        public static final String TITLE = "title";
        
        /**
         * The link of the feed
         * <P>Type: TEXT</P>
         */
        public static final String LINK = "link";
        
        /**
         * The source of the feed
         * <P>Type: TEXT</P>
         */
        public static final String SOURCE = "source";
        
        /**
         * The category of the feed
         * <P>Type: TEXT</P>
         */
        public static final String CATEGORY = "category";
        
        /**
         * The timestamp for when the feed was published
         * <P>Type: INTEGER (long from Date.parse())</P>
         */
        public static final String DATE = "date";
        
        /**
         * The description of the feed
         * <P>Type: TEXT</P>
         */
        public static final String DESCRIPTION = "description";
        
        /**
         * The image URL of the feed
         * <P>Type: TEXT</P>
         */
        public static final String IMAGEURL = "imageurl";
        
        /**
         * The image text of the feed
         * <P>Type: TEXT</P>
         */
        public static final String IMAGETEXT = "imagetext";
        
        /**
         * All columns's name of this table
         */
        public static final String[] ALL_COLUMNS = new String[] {
            _ID, TITLE, LINK, SOURCE, CATEGORY, 
            DATE, DESCRIPTION, IMAGEURL, IMAGETEXT
            };
        
    }
}
