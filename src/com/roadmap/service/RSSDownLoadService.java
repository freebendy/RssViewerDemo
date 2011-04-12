package com.roadmap.service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import android.app.IntentService;
import android.content.Intent;
import android.util.Log;

public class RSSDownLoadService extends IntentService {

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
    
    private void parseXml() {
    }

}
