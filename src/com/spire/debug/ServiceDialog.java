package com.spire.debug;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;

/**
 * Created by evgeniy on 17.08.13.
 */
public class ServiceDialog extends Activity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent intent=getIntent();
        String text = "";
        if(intent.hasExtra("text")) text = intent.getStringExtra("text");

        AlertDialog.Builder alert = new AlertDialog.Builder(this);
        alert.setTitle("Alert");
        alert.setIcon(android.R.drawable.ic_dialog_info);
        alert.setMessage(text);
        alert.setPositiveButton(android.R.string.ok,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        ServiceDialog.this.finish();
                    }
                });
        alert.show();
    }

}
