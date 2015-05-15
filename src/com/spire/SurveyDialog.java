package com.spire;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

/**
 * Created by evgeniy on 23.08.13.
 */
public class SurveyDialog extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        showSurveyDialog("google.ru");
    }

    private void showSurveyDialog (final String uri ){
        AlertDialog.Builder bulder = new AlertDialog.Builder(this);
        bulder  .setTitle(R.string.alert_main_improve_title)
                .setIcon(R.drawable.green)
                .setMessage(R.string.alert_main_improve_message)
                .setCancelable(false)
                .setNegativeButton(R.string.alert_main_improve_btn_negative, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        SurveyDialog.this.finish();
                    }
                })
                .setPositiveButton(R.string.alert_main_improve_btn_positive, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        Intent i = new Intent(Intent.ACTION_VIEW);
                        i.setData(Uri.parse(uri));
                        startActivity(i);

                        SurveyDialog.this.finish();


                    }
                });
        bulder.show();
    }

}
