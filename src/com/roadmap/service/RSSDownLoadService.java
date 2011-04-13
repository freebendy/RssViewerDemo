package com.roadmap.service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import android.app.IntentService;
import android.content.Intent;
import android.util.Log;

import com.roadmap.db.FeedsDbAdapter;
import com.roadmap.db.Message;

public class RSSDownLoadService extends IntentService {
    
    public static final String TAG_CHANNEL = "channel";
    public static final String TAG_ITEM = "item";
    public static final String TAG_TITLE = "title";
    public static final String TAG_LINK = "link";
    public static final String TAG_SOURCE = "source";
    public static final String TAG_CATEGORY = "category";
    public static final String TAG_PUBDATE = "pubdate";
    public static final String TAG_DESCRIPTION = "description";
    public static final String TAG_MEDIA_CONTENT = "media:content";
    public static final String TAG_MEDIA_TEXT = "media:text";
    
    public static final String ACTION_DOWMLOAD = "com.roadmap.service.ACTION_DOWMLOAD";
    
    public static final String ACTION_XML_PARSE = "com.roadmap.service.ACTION_XML_PARSE";
    
    public static final String ACTION_DATA_PERSIST = "com.roadmap.service.ACTION_DATA_PERSIST";
    
    public static final String STATE_CODE = "statecode";
    
    public static final int OPERATION_FAILED = -1;
    public static final int OPERATION_SUCCESS = 0;
    
    private static final String LOG_TAG = "roadmap.RSSDownLoadService";
    
    private static final String RSS_URL_STRING = "http://rss.news.yahoo.com/rss/topstories";
    
    private String mRssContent = "";
    
    private FeedsDbAdapter mFeedsDbAdapter;
    
    public RSSDownLoadService() {
        super("RSSDownLoadService");
        mFeedsDbAdapter = new FeedsDbAdapter(this);
    }
    
    @Override
    protected void onHandleIntent(Intent intent) {
        String action = intent.getAction();
        
        Log.v(LOG_TAG, "onHandleIntent: " + action);
        
        if (action.equals(ACTION_DOWMLOAD)) {
            boolean success = downloadRss();
            
            Intent downloadIntent = new Intent();
            downloadIntent.setAction(ACTION_DOWMLOAD);
            downloadIntent.putExtra(STATE_CODE,
                    success ? OPERATION_SUCCESS : OPERATION_FAILED);
            sendBroadcast(downloadIntent);
            
            try {
                List<Message> messageList = parseXml();
                Intent parseIntent = new Intent();
                parseIntent.setAction(ACTION_XML_PARSE);
                parseIntent.putExtra(STATE_CODE, OPERATION_SUCCESS);
                sendBroadcast(parseIntent);
                persistData(messageList);
            } catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }       
    }
    
    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.v(LOG_TAG, "onDestroy");
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
//                Log.v(LOG_TAG, mRssContent);
                
            } 
        } catch (IOException e1) {
            e1.printStackTrace();
        }
        return success;
    }
    
    private List<Message> parseXml() throws XmlPullParserException, IOException {
        Log.v(LOG_TAG, "parseXml");
        XmlPullParserFactory xppFactory = XmlPullParserFactory.newInstance();
        
        XmlPullParser xpp = xppFactory.newPullParser();
        xpp.setInput( new StringReader(mRssContent) );
        
        List<Message> messageList = null;
        String currentTagName = null;
        Message currentMessage = null;
        
        int eventType = xpp.getEventType();
        boolean done = false;
        while (eventType != XmlPullParser.END_DOCUMENT && !done) {
            
            if(eventType == XmlPullParser.START_DOCUMENT) {
                messageList = new ArrayList<Message>();
            } else if(eventType == XmlPullParser.START_TAG) {
                currentTagName = xpp.getName(); 
                if (currentTagName.equalsIgnoreCase(TAG_ITEM) ) {
                 currentMessage = new Message();
                } else if (currentMessage!= null) {
                    if (currentTagName.equalsIgnoreCase(TAG_MEDIA_CONTENT)) {
                        String imageUrl = xpp.getAttributeValue(null, "url");
                        currentMessage.setImageUrl(imageUrl);
                    } else if (currentTagName.equalsIgnoreCase(TAG_TITLE)) {
                        currentMessage.setTitle(xpp.nextText());
                    } else if (currentTagName.equalsIgnoreCase(TAG_LINK)) {
                        currentMessage.setLink(xpp.nextText());
                    } else if (currentTagName.equalsIgnoreCase(TAG_SOURCE)) {
                        currentMessage.setSource(xpp.nextText());
                    } else if (currentTagName.equalsIgnoreCase(TAG_CATEGORY)) {
                        currentMessage.setCategory(xpp.nextText());
                    } else if (currentTagName.equalsIgnoreCase(TAG_PUBDATE)) {
                        currentMessage.setDate(xpp.nextText());
                    } else if (currentTagName.equalsIgnoreCase(TAG_DESCRIPTION)) {
                        currentMessage.setDescription(xpp.nextText());
                    } else if (currentTagName.equalsIgnoreCase(TAG_MEDIA_TEXT)) {
                        currentMessage.setImageText(xpp.nextText());
                    }
                }
            } else if(eventType == XmlPullParser.END_TAG) {
                currentTagName = xpp.getName();
                if (currentTagName.equalsIgnoreCase(TAG_ITEM) ) {
                    messageList.add(currentMessage);
                    currentMessage = null;
                } else if (currentTagName.equalsIgnoreCase(TAG_CHANNEL)) {
                    done = true;
                    break;
                }
            }
            eventType = xpp.next();
        }

        return messageList;
    }
    
    private void persistData(List<Message> messages) {
        Log.v(LOG_TAG, "persistData begin");
        mFeedsDbAdapter.open();
        mFeedsDbAdapter.clear();
        boolean success = mFeedsDbAdapter.createMessages(messages);
        mFeedsDbAdapter.close();
        Intent persistIntent = new Intent();
        persistIntent.setAction(ACTION_DATA_PERSIST);
        persistIntent.putExtra(STATE_CODE,
                success ? OPERATION_SUCCESS : OPERATION_FAILED);
        sendBroadcast(persistIntent);
        Log.v(LOG_TAG, "persistData end");
    }

}
