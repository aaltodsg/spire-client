package com.spire;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.LocalBroadcastManager;
import android.view.ContextThemeWrapper;

import com.spire.debug.Debug;
import com.spire.model.Communications;
import com.spire.model.struct.GcmVars;

// MJR: Generate dialogs for end-user surveys

/* Copyright (C) Aalto University 2014
 *
 * Created by evgeniy on 23.08.13.
 */

public class FragmentActivitySurvey extends FragmentActivity {

    private static final String TAG = "FragmentActivitySurvey";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


    }

    // MJR: Register "show_survey_dialog" intent filter

    @Override
    protected void onStart() {
        LocalBroadcastManager.getInstance(this).registerReceiver(receiver_show_survey_dialog, new IntentFilter(getString(R.string.receiver_show_survey_dialog)));
        //LocalBroadcastManager.getInstance(this).registerReceiver(receiver_show_activity_dialog, new IntentFilter(getString(R.string.receiver_progress_dialog)));
        Debug.log(TAG, "onStart (register receiver_show_survey_dialog)");
        super.onStart();




    }

    @Override
    protected void onStop() {
        Debug.log(TAG,"onStop (unregister receiver_show_survey_dialog)");

        LocalBroadcastManager.getInstance(this).unregisterReceiver(receiver_show_survey_dialog);
        //LocalBroadcastManager.getInstance(this).unregisterReceiver(receiver_show_activity_dialog);


        super.onStop();
    }

    // MJR: Receive requests to display the survey dialogue. Extract survey URI from GCM

    private BroadcastReceiver receiver_show_survey_dialog = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            showSurveyDialog(intent.getStringExtra(GcmVars.gcm_uri), FragmentActivitySurvey.this);
        }
    };


    public static void showSurveyDialog (final String uri, final Context context ){

        AlertDialog.Builder bulder = new AlertDialog.Builder(new ContextThemeWrapper(context, android.R.style.Theme_Holo_Light_Dialog));

//        AlertDialog.Builder bulder = new AlertDialog.Builder(context);
        bulder  .setTitle(R.string.alert_main_improve_title)
                .setIcon(R.drawable.green)
                .setMessage(R.string.alert_main_improve_message)
                .setCancelable(false)
                .setNegativeButton(R.string.alert_main_improve_btn_negative, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                })
                .setPositiveButton(R.string.alert_main_improve_btn_positive, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
//                        Intent emailIntent = new Intent( android.content.Intent.ACTION_SEND );
//                        String aEmailList[] = { "free_space-feedback@list.aalto.fi" };
//                        emailIntent.putExtra(android.content.Intent.EXTRA_EMAIL, aEmailList);
//                        emailIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, "User feedback");
//                        emailIntent.setType("plain/text");
//                        context.startActivity(Intent.createChooser(emailIntent, ""));

                        if (uri != null && uri.length() > 0){
//                            Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
                            Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));

                            context.startActivity(browserIntent);
                            dialog.dismiss();
                        }

                    }
                });
        bulder.show();
    }

    public static void showRegisterDialog (final String uri, final Context context ){

        AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(context, android.R.style.Theme_Holo_Light_Dialog));

//        AlertDialog.Builder bulder = new AlertDialog.Builder(context);
        builder  .setTitle(R.string.title_registr_dialog)
                .setMessage(R.string.content_registr_dialog)
                .setCancelable(false)
                .setPositiveButton(R.string.btn_registr_dialog_register_via_browser, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
                        context.startActivity(browserIntent);
                        dialog.dismiss();
                    }
                })
                .setNegativeButton(R.string.btn_registr_dialog_close_application, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        if (Communications.getInstance(context).getUser().getmPlusClient().isConnected()) {
                            Communications.getInstance(context).getUser().getmPlusClient().clearDefaultAccount();
                            Communications.getInstance(context).getUser().getmPlusClient().disconnect();
                            //Communications.getInstance(getApplicationContext()).getUser().getmPlusClient().connect();
                            Communications.getInstance(context).setUser(null);
                        }
                        System.exit(0);
                    }
                });
        builder.show();
    }

    /*private BroadcastReceiver receiver_show_activity_dialog = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getBooleanExtra("is_show",false)){
                mConnectionProgressDialog.show();
            }
        }
    };*/


}
