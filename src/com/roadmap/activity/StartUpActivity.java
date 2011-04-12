package com.roadmap.activity;

import com.roadmap.R;
import com.roadmap.service.RSSDownLoadService;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

public class StartUpActivity extends Activity {
    
    private static final String LOG_TAG = "roadmap.StartUpActivity";
    
    private static final int DIALOG_DL_FAILED_ID = 0;
    
    public BroadcastReceiver mDownloadReceiver;
    
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        mDownloadReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                Log.v(LOG_TAG, "mDownloadReceiver.onReceive: " + action);

                if (action.equals(RSSDownLoadService.ACTION_DOWMLOAD_SUCCESS)) {
                    Toast.makeText(StartUpActivity.this, R.string.dl_finished, Toast.LENGTH_SHORT).show();
                } else if (action.equals(RSSDownLoadService.ACTION_DOWMLOAD_FAILED)) {
                    showDialog(DIALOG_DL_FAILED_ID);
                }
            }
        };
        
        IntentFilter filter = new IntentFilter();
        filter.addAction(RSSDownLoadService.ACTION_DOWMLOAD_SUCCESS);
        filter.addAction(RSSDownLoadService.ACTION_DOWMLOAD_FAILED);
        registerReceiver(mDownloadReceiver, filter);
        
        Intent intent = new Intent(this, RSSDownLoadService.class);
        startService(intent);
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.v(LOG_TAG, "onDestroy");
        unregisterReceiver(mDownloadReceiver);
    }
    
    @Override
    protected Dialog onCreateDialog(int id) {
        Log.v(LOG_TAG, "onCreateDialog");
        Dialog dialog = null;
        switch (id) {
        case DIALOG_DL_FAILED_ID:
            AlertDialog.Builder builder = new AlertDialog.Builder(StartUpActivity.this);
            builder.setMessage(R.string.dl_failed)
                   .setCancelable(false)
                   .setPositiveButton("Close", 
                           new DialogInterface.OnClickListener() {
                               public void onClick(DialogInterface dialog, int id) {
                                   StartUpActivity.this.finish();
                               }
                   });
            dialog = builder.create();
            break;
        default:
            break;
        }
        return dialog;
    }
}