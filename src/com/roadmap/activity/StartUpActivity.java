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
import android.view.Window;
import android.widget.Toast;

public class StartUpActivity extends Activity {

    private static final String LOG_TAG = "roadmap.StartUpActivity";

    private static final int DIALOG_DL_FAILED_ID = 0;
    private static final int DIALOG_PARSE_FAILED_ID = 1;
    private static final int DIALOG_PERSIST_FAILED_ID = 2;

    public BroadcastReceiver mDownloadReceiver;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        hideTitle();

        setContentView(R.layout.startup);

        mDownloadReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                Log.v(LOG_TAG, "mDownloadReceiver.onReceive: " + action);

                if (action.equals(RSSDownLoadService.ACTION_DOWMLOAD)) {
                    int stateCode = intent.getIntExtra(
                            RSSDownLoadService.STATE_CODE,
                            RSSDownLoadService.OPERATION_FAILED);
                    if (stateCode == RSSDownLoadService.OPERATION_SUCCESS) {
                        Toast.makeText(StartUpActivity.this, R.string.dl_finished, Toast.LENGTH_SHORT).show();
                    } else {
                        showDialog(DIALOG_DL_FAILED_ID);
                    }
                } else if (action.equals(RSSDownLoadService.ACTION_XML_PARSE)) {
                    int stateCode = intent.getIntExtra(
                            RSSDownLoadService.STATE_CODE,
                            RSSDownLoadService.OPERATION_FAILED);
                    if (stateCode == RSSDownLoadService.OPERATION_SUCCESS) {
                        Toast.makeText(StartUpActivity.this, R.string.xml_parse_success, Toast.LENGTH_SHORT).show();
                    } else {
                        showDialog(DIALOG_PARSE_FAILED_ID);
                    }
                } else if (action.equals(RSSDownLoadService.ACTION_DATA_PERSIST)) {
                    int stateCode = intent.getIntExtra(
                            RSSDownLoadService.STATE_CODE,
                            RSSDownLoadService.OPERATION_FAILED);
                    if (stateCode == RSSDownLoadService.OPERATION_SUCCESS) {
                        Toast.makeText(StartUpActivity.this, R.string.persist_success, Toast.LENGTH_SHORT).show();
                        Intent rssViewIntent = new Intent(StartUpActivity.this, RSSViewerActivity.class);
                        startActivity(rssViewIntent);
                        StartUpActivity.this.finish();
                    } else {
                        showDialog(DIALOG_PERSIST_FAILED_ID);
                    }
                }
            }
        };

        IntentFilter filter = new IntentFilter();
        filter.addAction(RSSDownLoadService.ACTION_DOWMLOAD);
        filter.addAction(RSSDownLoadService.ACTION_XML_PARSE);
        filter.addAction(RSSDownLoadService.ACTION_DATA_PERSIST);
        registerReceiver(mDownloadReceiver, filter);

        Intent intent = new Intent(this, RSSDownLoadService.class);
        intent.setAction(RSSDownLoadService.ACTION_DOWMLOAD);
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
        int messageId = -1;

        switch (id) {
        case DIALOG_DL_FAILED_ID:
            messageId = R.string.dl_failed;
            break;
        case DIALOG_PARSE_FAILED_ID:
            messageId = R.string.xml_parse_failed;
            break;
        case DIALOG_PERSIST_FAILED_ID:
            messageId = R.string.persist_failed;
            break;
        default:
            break;
        }

        Dialog dialog = null;
        if ( messageId > -1 ) {
            AlertDialog.Builder builder = new AlertDialog.Builder(StartUpActivity.this);
            builder.setMessage(messageId)
                   .setCancelable(false)
                   .setPositiveButton("Close",
                           new DialogInterface.OnClickListener() {
                               public void onClick(DialogInterface dialog, int id) {
                                   StartUpActivity.this.finish();
                               }
                   });
            dialog = builder.create();
        }
        return dialog;
    }

    private void hideTitle() {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
    }
}