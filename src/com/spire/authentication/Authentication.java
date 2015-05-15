package com.spire.authentication;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.graphics.Point;
import android.os.Bundle;
import android.text.method.LinkMovementMethod;
import android.view.ContextThemeWrapper;
import android.view.Display;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.common.Scopes;
import com.google.android.gms.plus.PlusClient;
import com.spire.ActivitySurvey;
import com.spire.R;
import com.spire.debug.Debug;
import com.spire.model.Communications;
import com.spire.model.struct.User;

import java.util.ArrayList;

/**
 * Created on 19.08.13.
 */
public class Authentication extends ActivitySurvey implements
        GooglePlayServicesClient.ConnectionCallbacks, GooglePlayServicesClient.OnConnectionFailedListener, View.OnClickListener {

    private static final String TAG = "Authentication";

    ArrayList<String> mListItems = null;
    private PlusClient mPlusClient = null;

    private static final int REQUEST_CODE_RESOLVE_ERR = 9000;

    protected ProgressDialog mConnectionProgressDialog;
    private ConnectionResult mConnectionResult;

    protected Communications com;

    protected Dialog dialog;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Debug.log(TAG, "onCreate");

        com = Communications.getInstance(this);

        //setContentView(R.layout.authentication_layout);

/*  Replaced with newer code by MJR 3.2.2014:
    Taken from: http://stackoverflow.com/questions/19887187/setvisibleactivities-undefined
        mPlusClient = new PlusClient.Builder(this, this, this)
                .setVisibleActivities("http://schemas.google.com/AddActivity", "http://schemas.google.com/BuyActivity")
                .build(); */
        mPlusClient = new PlusClient.Builder(this, this, this)
                .setActions("http://schemas.google.com/AddActivity", "http://schemas.google.com/BuyActivity")
                .setScopes(Scopes.PLUS_LOGIN) // Space separated list of scopes was: "PLUS_LOGIN"
                .build();
        mConnectionProgressDialog = new ProgressDialog(new ContextThemeWrapper(this, android.R.style.Theme_Holo_Light_Dialog));
        mConnectionProgressDialog.setCancelable(false);

        if (!mPlusClient.isConnected()){
            mConnectionProgressDialog.setMessage("Signing in...");

            //ConfigureActionBar();

            //if (!com.isUserNull())
             mPlusClient.connect();
        }
        DialogWindow();
//        LocalBroadcastManager.getInstance(this).registerReceiver(receiver_check_profile, new IntentFilter(getString(R.string.receiver_profile_check)));


//        LocalBroadcastManager.getInstance(this).registerReceiver(receiver_check_profile, new IntentFilter(getString(R.string.receiver_profile_check)));




    }

//    private void registerGCM(){
//        GCMRegistrar.checkDevice(this);
//        GCMRegistrar.checkManifest(this);
//        final String regId = GCMRegistrar.getRegistrationId(this);
//        if (regId.equals("")) {
//            GCMRegistrar.register(this, "56557016300");
//        } else {
//            Debug.log("GCM", "Already registered: " + regId);
//            com.profileCheck(regId);
//        }
//
//    }


//    private void ConfigureActionBar () {
//        getActionBar().hide();
//    }



    private float dpFromPx(float px)
    {
        return px / this.getResources().getDisplayMetrics().density;
    }


    private float pxFromDp(float dp)
    {
        return dp * this.getResources().getDisplayMetrics().density;
    }

    private void DialogWindow(){

        Debug.log(TAG,"DialogWindow");

        LayoutInflater inflater= LayoutInflater.from(this);


        View view = inflater.inflate(R.layout.authentication, null);
        View title = inflater.inflate(R.layout.authentication_title, null);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder
                .setCustomTitle(title)
                .setCancelable(false)
                .setView(view);



        dialog = builder.create();


        ImageView img = (ImageView)findViewById(R.id.imageView2);

        if (img != null){
            WindowManager.LayoutParams wmlp = dialog.getWindow().getAttributes();
            Display display = getWindowManager().getDefaultDisplay();
            Point size = new Point();
            display.getSize(size);
            int iScreenHeight = size.y;
            RelativeLayout.LayoutParams lp = ( RelativeLayout.LayoutParams ) img.getLayoutParams();
            int ImgMargin = lp.topMargin;
            wmlp.y = (ImgMargin +  img.getHeight()  + ( ImgMargin + img.getHeight() / 2 ) ) - iScreenHeight / 2;
            wmlp.x = 0;
        }

            dialog.show();
            dialog.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);



        TextView txt = (TextView) dialog.findViewById(R.id.textView);
        //txt.setText(Html.fromHtml(getString(R.string.help)));
        txt.setMovementMethod(LinkMovementMethod.getInstance());


        com.google.android.gms.common.SignInButton LogInButton =
                ( com.google.android.gms.common.SignInButton ) dialog.findViewById(R.id.btn_sign_in);

        LogInButton.setOnClickListener ( this );


        dialog.setOnKeyListener(new DialogInterface.OnKeyListener() {
            @Override
            public boolean onKey(DialogInterface dialogInterface, int i, KeyEvent keyEvent) {
                finish();
                Debug.log(TAG,"finish");
                return false;
            }
        });
    }

    @Override
    public void onClick(View v) {
        Debug.log(TAG, "onClick");
        if (v.getId() == R.id.btn_sign_in && !mPlusClient.isConnected()) {
            Debug.log(TAG, String.format("mConnectionResult: %s", mConnectionResult));
            if (mConnectionResult == null) {
                mConnectionProgressDialog.show();
                mPlusClient.connect();
            } else {
                try {
                    mConnectionResult.startResolutionForResult( this, REQUEST_CODE_RESOLVE_ERR );
                } catch (IntentSender.SendIntentException e) {
                    // Try connecting again.
                    mConnectionResult = null;
                    mPlusClient.connect();
                }
            }
        }

        //dialog.dismiss();

    }

    @Override
    public void onConnectionFailed(ConnectionResult result) {
        Debug.log(TAG, String.format("onConnectionFailed result: %s", result));
        if (mConnectionProgressDialog.isShowing()) {
            // The user clicked the sign-in button already. Start to resolve
            // connection errors. Wait until onConnected() to dismiss the
            // connection dialog.
            if (result.hasResolution()) {
                try {
                    result.startResolutionForResult(this, REQUEST_CODE_RESOLVE_ERR);
                } catch (IntentSender.SendIntentException e) {
                    mPlusClient.connect();
                }
            }
        } else {

            if (result.getErrorCode() == ConnectionResult.RESOLUTION_REQUIRED){
                try {
                    result.startResolutionForResult(this,REQUEST_CODE_RESOLVE_ERR);
                } catch (IntentSender.SendIntentException e) {
//                    mPlusClient.connect();
                    e.printStackTrace();
                }
            } else {
//                DialogWindow();
            }
        }


        Debug.log(TAG, result.toString());

        // Save the intent so that we can start an activity when the user clicks
        // the sign-in button.
        mConnectionResult = result;
    }

    @Override
    public void onConnected(Bundle connectionHint) {
        Debug.log(TAG,"onConnected");
        mConnectionProgressDialog.dismiss();

//        String accountName = mPlusClient.getAccountName();
//        Toast.makeText(this, accountName + " is connected.", Toast.LENGTH_LONG).show();



        com.setUser(new User());
        com.getUser().setEmail(mPlusClient.getAccountName());
        if (mPlusClient.getCurrentPerson() != null)
            com.getUser().setUsername(mPlusClient.getCurrentPerson().getDisplayName());
        com.getUser().setmPlusClient(mPlusClient);



        //com.getUser().save(this.getApplicationContext());



        Debug.alert(getApplicationContext(), "onConnected");

//        registerGCM();

//        mConnectionProgressDialog.show();


//        registerGCM();

    }

//    private void StartMainActivity (  ) {
//
//        Intent intent = new Intent(getApplication(), MainActivity.class);
//        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP |
//                Intent.FLAG_ACTIVITY_CLEAR_TASK |
//                Intent.FLAG_ACTIVITY_NEW_TASK);
//        intent.setAction(IntentActions.ACTION_LOG_IN);
//
////        intent.putExtra(IntentActions.EXTRA_ACCOUNT, mPlusClient.getCurrentPerson().getName().toString() );
//
//        startActivity(intent);
//    }
    @Override
    protected void onActivityResult(int requestCode, int responseCode, Intent intent) {
        if (requestCode == REQUEST_CODE_RESOLVE_ERR && responseCode == RESULT_OK) {
            mConnectionResult = null;
            mPlusClient.connect();
            Debug.log ( TAG,"onActivityResult" );
        }
    }

    @Override
    public void onDisconnected() {
        Debug.log(TAG, "onDisconnected");
//        unregisterReceiver(receiver_check_profile);

    }



//    private void registerGCM(){
//        GCMRegistrar.checkDevice(this);
//        GCMRegistrar.checkManifest(this);
//        final String regId = GCMRegistrar.getRegistrationId(this);
//        if (regId.equals("")) {
//            GCMRegistrar.register(this, "56557016300");
//        } else {
//            Debug.log("GCM", "Already registered: " + regId);
//            com.profileCheck(regId);
//        }
//
//    }

//    protected void onCheckProfile(){
//        mConnectionProgressDialog.dismiss();
//
//
//
////        StartMainActivity();
//    }


//    protected BroadcastReceiver receiver_check_profile = new BroadcastReceiver() {
//        @Override
//        public void onReceive(Context context, Intent intent) {
//
//        }
//    };

}
