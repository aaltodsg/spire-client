package com.spire;

import android.content.Context;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;

import com.google.android.gcm.GCMBaseIntentService;
import com.spire.debug.Debug;
import com.spire.model.Communications;
import com.spire.model.struct.GcmVars;

/**
 * Created by evgeniy on 13.08.13.
 */

public class GCMIntentService extends GCMBaseIntentService {


    private static final String TAG = "GCMIntentService";

    public GCMIntentService() {
        super("00000000000"); // INIT_CONFIG: Insert GCM sender Id here
    }


    @Override
    protected void onError(Context arg0, String arg1) {
    }

    @Override
    protected void onMessage(Context arg0, Intent arg1) {
        Debug.log(TAG, "Received " + arg1.getExtras().toString());

        Debug.log(TAG, "data " + arg1.getStringExtra("message"));

        parseMessage(arg1);


    }




    @Override
    protected void onRegistered(Context arg0, String arg1) {
        Debug.log(TAG, "GCM Device registered " + arg1);

        Communications.getInstance(arg0).profileCheck(arg1);


    }

    @Override
    protected void onUnregistered(Context arg0, String arg1) {
        Debug.log(TAG, "Device unregistered " + arg1);

    }

    protected void onDeletedMessages(Context context, int total) {
        Debug.log(TAG, "Received deleted messages notification");
    };

    @Override
    protected boolean onRecoverableError(Context context, String errorId) {
        Debug.log(TAG, "Received recoverable error: " + errorId);
        return super.onRecoverableError(context, errorId);
    }


    private void parseMessage(Intent intent){
        switch (intent.getStringExtra(GcmVars.gcm_message)){
            case GcmVars.gcm_new_state_notification:
                newStateNotification(intent);
                break;
            case GcmVars.gcm_area_update_notification:
                areaUpdateNotification(intent);
                break;
            case GcmVars.gcm_launch_survey_notification:
                launchSurveyNotification(intent);
                break;
            default:
                break;

        }
    }

    private void newStateNotification(Intent intent){
        if (intent.getStringExtra(GcmVars.gcm_state) != null){
            sendNotifyStartService(getStatService(intent.getStringExtra(GcmVars.gcm_state)));
        }
        if (intent.getStringExtra(GcmVars.gcm_reason) != null){
            if (intent.getStringExtra(GcmVars.gcm_reason).equals(GcmVars.gcm_reason_parked)){
                sendNotifyStartService(false);
            }
        }
    }

    private void areaUpdateNotification(Intent intent){
        if (intent.getStringExtra(GcmVars.gcm_area)!= null &&
                intent.getStringExtra(GcmVars.gcm_status) != null){
            Communications.getInstance(this).updateAreaStatus(intent.getStringExtra(GcmVars.gcm_area),intent.getStringExtra(GcmVars.gcm_status));
        }
    }

    private void launchSurveyNotification(Intent intent){
        ShowSurveyDialog(intent.getStringExtra(GcmVars.gcm_uri));
    }


    private void ShowSurveyDialog(String uri){
        Intent intent = new Intent(getString(R.string.receiver_show_survey_dialog));
        intent.putExtra(GcmVars.gcm_uri,uri);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }


    private void sendNotifyStartService(boolean stat){
        Intent intent = new Intent(getApplicationContext().getString(R.string.receiver_service_start));
        intent.putExtra("state",stat);
        LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intent);
    }

    private boolean getStatService(String stat){
        boolean status = false;
        switch (stat){
            case GcmVars.gcm_state_active:
                status = true;
                break;
            case GcmVars.gcm_state_passive:
                status = false;
                break;
        }
        return status;
    }

}