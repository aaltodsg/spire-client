package com.spire.authentication;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;

import com.google.analytics.tracking.android.EasyTracker;
import com.google.android.gcm.GCMRegistrar;
import com.spire.FragmentActivitySurvey;
import com.spire.MainActivity;
import com.spire.R;
import com.spire.debug.Debug;
import com.spire.model.Communications;
import com.spire.model.IntentActions;
import com.spire.model.struct.JsonVars;

/**
 * Created by evgeniy on 23.08.13.
 */
public class Auth extends Authentication {

    private static final String TAG = "Auth";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Debug.log(TAG, "onCreate (registers receiver_check_profile)");
        setContentView(R.layout.authentication_layout);

        LocalBroadcastManager.getInstance(this).registerReceiver(receiver_check_profile, new IntentFilter(getString(R.string.receiver_profile_check)));

//        LocalBroadcastManager.getInstance(this).registerReceiver(receiver_check_profile, new IntentFilter(getString(R.string.receiver_profile_check)));

       // getActionBar().hide();

    }

    @Override
    protected void onStart() {
        super.onStart();
        Debug.log(TAG,"onStart");

        // MJR: This starts Google Analytics V2
        // More info on GAV2: https://developers.google.com/analytics/devguides/collection/android/v2/
        EasyTracker.getInstance().activityStart(this);
    }


    @Override
    protected void onStop() {
        super.onStop();
        Debug.log(TAG,"onStop");

        // MJR: Stops Google Analytics V2
        EasyTracker.getInstance().activityStop(this);
    }


    @Override
    public void onConnected(Bundle connectionHint) {
        super.onConnected(connectionHint);
        Debug.log(TAG,"onConnected");
        registerGCM();
        mConnectionProgressDialog.show();
        com.getUser().restore(this);

    }

    private void registerGCM(){
        Debug.log(TAG, "registerGCM");
        GCMRegistrar.checkDevice(this);
        GCMRegistrar.checkManifest(this);
        final String regId = GCMRegistrar.getRegistrationId(this);
        if (regId.equals("")) {
            GCMRegistrar.register(this, "56557016300");
        } else {
            Debug.log(TAG, "Already registered: " + regId);
            com.profileCheck(regId);
        }
    }

//    private BroadcastReceiver receiver_check_profile = new BroadcastReceiver() {
//        @Override
//        public void onReceive(Context context, Intent intent) {
//            mConnectionProgressDialog.dismiss();
//            StartMainActivity();
//        }
//    };


//    @Override
//    protected void onCheckProfile() {
//        super.onCheckProfile();
//
//        if (getStart() == Type.main){
//            StartMainActivity();
//        }
//    }

    @Override
    public void onDisconnected() {
        super.onDisconnected();
        Debug.log(TAG, "onDisconnected");

    }

    private void StartMainActivity (  ) {
        Debug.log(TAG,"StartMainActivity");

        Intent intent = new Intent(getApplication(), MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP |
                Intent.FLAG_ACTIVITY_CLEAR_TASK |
                Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.setAction(IntentActions.ACTION_LOG_IN);

//        intent.putExtra(IntentActions.EXTRA_ACCOUNT, mPlusClient.getCurrentPerson().getName().toString() );

        Debug.log(TAG,"Triggering MainActivity");
        startActivity(intent);
    }


    @Override
    protected void onDestroy() {
        Debug.log(TAG,"onDestroy (unregisters receiver_check_profile)");
        LocalBroadcastManager.getInstance(this).unregisterReceiver (receiver_check_profile);

        super.onDestroy();
    }

    @Override
    protected void onResume() {
        super.onResume();
        Debug.log(TAG,"onResume");

        if (com.getUser() != null && com.getUser().getmPlusClient() != null){
            if (com.getUser().getmPlusClient().isConnected()){
                if (!com.getUser().isAuth()){
                    com.profileCheck(GCMRegistrar.getRegistrationId(this));
                    Debug.log(TAG,"onResume: false");
                }
            }
        }


    }

    protected BroadcastReceiver receiver_check_profile = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            if (intent.getBooleanExtra("auth",false)){
                Debug.log(TAG, "receiver_check_profile: true");
                mConnectionProgressDialog.dismiss();

                Communications.getInstance(context).getUser().save(context);

                StartMainActivity();
            } else {
                mConnectionProgressDialog.dismiss();
                showRegisterDialog(intent.getStringExtra(JsonVars.registration_uri));
                Debug.log(TAG, "receiver_check_profile: false");
            }
        }
    };

    private void showRegisterDialog(String uri){
        FragmentActivitySurvey.showRegisterDialog(uri, this);
    }

}
