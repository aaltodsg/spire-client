package com.spire;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;

import com.spire.debug.Debug;
import com.spire.model.struct.GcmVars;

/**
 * Created by evgeniy on 23.08.13.
 */

public class ActivitySurvey extends Activity{

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void onStart() {
        LocalBroadcastManager.getInstance(this).registerReceiver(receiver_show_survey_dialog, new IntentFilter(getString(R.string.receiver_show_survey_dialog)));
        Debug.log("ActivitySurvey","onStart");
        super.onStart();

    }

    @Override
    protected void onStop() {
        Debug.log("ActivitySurvey","onStop");

        LocalBroadcastManager.getInstance(this).unregisterReceiver(receiver_show_survey_dialog);

        super.onStop();
    }

    private BroadcastReceiver receiver_show_survey_dialog = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            FragmentActivitySurvey.showSurveyDialog(intent.getStringExtra(GcmVars.gcm_uri),ActivitySurvey.this);
        }
    };


//    private void showSurveyDialog (final String uri ){
//        AlertDialog.Builder bulder = new AlertDialog.Builder(this);
//        bulder  .setTitle(R.string.alert_main_improve_title)
//                .setIcon(R.drawable.green)
//                .setMessage(R.string.alert_main_improve_message)
//                .setCancelable(false)
//                .setNegativeButton(R.string.alert_main_improve_btn_negative, new DialogInterface.OnClickListener() {
//                    @Override
//                    public void onClick(DialogInterface dialog, int which) {
//
//                    }
//                })
//                .setPositiveButton(R.string.alert_main_improve_btn_positive, new DialogInterface.OnClickListener() {
//                    @Override
//                    public void onClick(DialogInterface dialog, int which) {
//
//                        Intent i = new Intent(Intent.ACTION_VIEW);
//                        i.setData(Uri.parse(uri));
//                        startActivity(i);
//                    }
//                });
//        bulder.show();
//    }

}
