package com.roadmap.service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.net.HttpURLConnection;
import java.net.URL;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import android.app.IntentService;
import android.content.Intent;
import android.util.Log;

public class RSSDownLoadService extends IntentService {
    
    public static final String TAG_ITEM = "item";
    public static final String TAG_TITLE = "title";
    public static final String TAG_LINK = "link";
    public static final String TAG_SOURCE = "source";
    public static final String TAG_CATEGORY = "category";
    public static final String TAG_PUBDATE = "pubdate";
    public static final String TAG_DESCRIPTION = "description";
    public static final String TAG_MEDIA_CONTENT = "media:content";
    public static final String TAG_MEDIA_TEXT = "media:text";
    

    public static final String ACTION_DOWMLOAD_SUCCESS = "com.roadmap.service.ACTION_DOWMLOAD_SUCCESS";
    public static final String ACTION_DOWMLOAD_FAILED = "com.roadmap.service.ACTION_DOWMLOAD_FAILED";
    
    private static final String LOG_TAG = "roadmap.RSSDownLoadService";
    
    private static final String RSS_URL_STRING = "http://rss.news.yahoo.com/rss/topstories";
    
    private String mRssContent = "";
    
    public RSSDownLoadService() {
        super("RSSDownLoadService");
    }
    
    @Override
    protected void onHandleIntent(Intent intent) {
        Log.v(LOG_TAG, "onHandleIntent");
        
        Intent broadcasetIntent = new Intent();
        
        boolean success = downloadRss();
        broadcasetIntent.setAction(
                success ? ACTION_DOWMLOAD_SUCCESS : ACTION_DOWMLOAD_FAILED );
        sendBroadcast(broadcasetIntent);
    }
    
    private boolean downloadRss() {
        Log.v(LOG_TAG, "downloadRss");
        boolean success = false;
        try {
            URL url = new URL(RSS_URL_STRING);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            int reCode = connection.getResponseCode();
            
            if (HttpURLConnection.HTTP_OK == reCode)
            {
                InputStreamReader in = new InputStreamReader(connection.getInputStream());  
                // Create the BufferedReader.
                BufferedReader buffer = new BufferedReader(in);
                mRssContent = "";
                String inputLine = null;
                // Get the data.
                while (((inputLine = buffer.readLine()) != null))  
                {  
                    mRssContent += inputLine;  
                }
                // Close InputStreamReader.
                in.close();  
                // Close http connection.
                connection.disconnect();
                
                success = true;
                Log.v(LOG_TAG, mRssContent);
            } 
        } catch (IOException e1) {
            e1.printStackTrace();
        }
        return success;
    }
    
    private boolean parseXml() throws XmlPullParserException, IOException {
        Log.v(LOG_TAG, "parseXml");
        XmlPullParserFactory xppFactory = XmlPullParserFactory.newInstance();
        xppFactory.setNamespaceAware(true);
        XmlPullParser xpp = xppFactory.newPullParser();
        
        xpp.setInput( new StringReader(mRssContent) );
        int eventType = xpp.getEventType();
        while (eventType != XmlPullParser.END_DOCUMENT) {
         if(eventType == XmlPullParser.START_DOCUMENT) {
             System.out.println("Start document");
         } else if(eventType == XmlPullParser.START_TAG) {
             if (xpp.getName().equalsIgnoreCase(TAG_ITEM) ) {
                 
             }
         } else if(eventType == XmlPullParser.END_TAG) {
             if (xpp.getName().equalsIgnoreCase(TAG_ITEM) ) {
                 
             }
         } else if(eventType == XmlPullParser.TEXT) {
             System.out.println("Text " + xpp.getText());
         }
         eventType = xpp.next();
        }

        
        return false;
    }

}
