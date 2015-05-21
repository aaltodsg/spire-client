package com.spire.authentication;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.IntentSender;
import android.os.Bundle;
import android.view.ContextThemeWrapper;
import android.view.View;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.common.Scopes;
import com.google.android.gms.plus.PlusClient;
import com.spire.FragmentActivitySurvey;
import com.spire.debug.Debug;
import com.spire.model.Communications;
import com.spire.model.struct.User;

/* Copyright (C) Aalto University 2014
 *
 * Created by evgeniy on 10.09.13.
 */
public class AuthActivity extends FragmentActivitySurvey implements GooglePlayServicesClient.ConnectionCallbacks, GooglePlayServicesClient.OnConnectionFailedListener, View.OnClickListener{

    private static final String TAG = "AuthActivity";
    protected PlusClient mPlusClient = null;
    private static final int REQUEST_CODE_RESOLVE_ERR = 9000;
    private ConnectionResult mConnectionResult;
    protected Communications com;

    private boolean is_progress_dialog_show = false;

    ProgressDialog mConnectionProgressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Debug.log(TAG, "onCreate");

        mConnectionProgressDialog = new ProgressDialog(new ContextThemeWrapper(this, android.R.style.Theme_Holo_Light_Dialog));
        mConnectionProgressDialog.setCancelable(false);


        com = Communications.getInstance(this);
        /*  Replaced with newer code by MJR 3.2.2014:
    Taken from: http://stackoverflow.com/questions/19887187/setvisibleactivities-undefined
        mPlusClient = new PlusClient.Builder(this, this, this)
                .setVisibleActivities("http://schemas.google.com/AddActivity", "http://schemas.google.com/BuyActivity")
                .build(); */
        mPlusClient = new PlusClient.Builder(this, this, this)
                .setActions("http://schemas.google.com/AddActivity", "http://schemas.google.com/BuyActivity")
                .setScopes(Scopes.PLUS_LOGIN) // Space separated list of scopes
                .build();

        if (!mPlusClient.isConnected()){
            mPlusClient.connect();
        }

    }

    @Override
    public void onConnected(Bundle bundle) {
        Debug.log(TAG, "onConnected");
        initUser();
        //registerGCM();

        showProgressDialog(false);

        // Debug.alert(this, "onConnected");
        Debug.toast(getApplicationContext(),"AuthActivity.java: onConnected");

    }

    @Override
    public void onDisconnected() {
        Debug.log(TAG, "onDisconnected");

    }

    @Override
    public void onClick(View view) {
        Debug.log(TAG, "onClick");

    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Debug.log(TAG, "onConnectionFailed");

        if (mConnectionProgressDialog.isShowing()) {
            if (connectionResult.hasResolution()) {
                try {
                    connectionResult.startResolutionForResult(this, REQUEST_CODE_RESOLVE_ERR);
                } catch (IntentSender.SendIntentException e) {
                    mPlusClient.connect();
                }
            }
        } else {
            Intent intent = new Intent(getApplication(), Auth.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP |
            Intent.FLAG_ACTIVITY_CLEAR_TASK |
            Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);

        }


    }


    private void initUser(){
        Debug.log(TAG, "initUser");
        // MJR: This is the 3rd time of initiating a new user during startup
        // This one is called when resuming from background
        com.setUser(new User());
        com.getUser().setEmail(mPlusClient.getAccountName());
        if (mPlusClient.getCurrentPerson() != null)
            com.getUser().setUsername(mPlusClient.getCurrentPerson().getDisplayName());
        com.getUser().setmPlusClient(mPlusClient);
        //com.getUser().save(this);

    }

    @Override
    protected void onStart() {
        super.onStart();
        Debug.log(TAG,"onStart (connect PlusClient, if needed)");
        // MJR: added the test; earlier GmsClient was complaining about calling connect while still connected.
        if (!mPlusClient.isConnected()) mPlusClient.connect();
    }

    @Override
    protected void onStop() {
        super.onStop();
        Debug.log(TAG,"onStop (disconnect PlusClient)");
        mPlusClient.disconnect();
    }

    /*private void registerGCM(){
        GCMRegistrar.checkDevice(this);
        GCMRegistrar.checkManifest(this);
        final String regId = GCMRegistrar.getRegistrationId(this);
        if (regId.equals("")) {
            GCMRegistrar.register(this, "56557016300");
        } else {
            Debug.log("Auht", "Already registered: " + regId);
            com.profileCheck(regId);
        }
    }*/

    /*private void StartMainActivity (  ) {

        showProgressDialog(false);

        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP |
                Intent.FLAG_ACTIVITY_CLEAR_TASK |
                Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.setAction(IntentActions.ACTION_LOG_IN);
        this.startActivity(intent);


    }*/


   /* public BroadcastReceiver receiver_check_profile = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            if (intent.getBooleanExtra("auth",false)){
                Debug.log("receiver_check_profile","true");

                showProgressDialog(false);
//StartMainActivity();
            } else {
                showProgressDialog(false);
                showRegisterDialog(intent.getStringExtra(JsonVars.registration_uri));

                Debug.log("receiver_check_profile", "false");
            }
            finish();
        }
    };*/

    public void showProgressDialog(boolean is_show){
       /* Intent intent = new Intent(this.getString(R.string.receiver_progress_dialog));
        intent.putExtra("is_show", is_show);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
*/

        Debug.log(TAG,"showProgressDialog");
        if (is_show){
            mConnectionProgressDialog.show();
        } else {
            mConnectionProgressDialog.dismiss();
        }
        is_progress_dialog_show = is_show;
    }

     private void showRegisterDialog(String uri){
         Debug.log(TAG,"showRegisterDialog");
        FragmentActivitySurvey.showRegisterDialog(uri, this);
    }

}

    /*public void checkProfile(){
        if (com.getUser() != null && com.getUser().getmPlusClient() != null){
            if (com.getUser().getmPlusClient().isConnected()){
                if (!com.getUser().isAuth()){
                    com.profileCheck(GCMRegistrar.getRegistrationId(this));
                    Debug.log("Auth","false");
                }
            }
        }

    }/*


    public PlusClient getmPlusClient() {
        return mPlusClient;
    }


    @Override
    protected void onStart() {
        super.onStart();
        mPlusClient.connect();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mPlusClient.disconnect();
    }
}

//
//
//
//
//package com.spire;
//
//        import android.app.AlertDialog;
//        import android.app.Dialog;
//        import android.app.ProgressDialog;
//        import android.content.BroadcastReceiver;
//        import android.content.Context;
//        import android.content.DialogInterface;
//        import android.content.Intent;
//        import android.content.IntentFilter;
//        import android.graphics.Point;
//        import android.net.Uri;
//        import android.os.Bundle;
//        import android.support.v4.app.FragmentActivity;
//        import android.support.v4.content.LocalBroadcastManager;
//        import android.text.method.LinkMovementMethod;
//        import android.view.ContextThemeWrapper;
//        import android.view.Display;
//        import android.view.KeyEvent;
//        import android.view.LayoutInflater;
//        import android.view.View;
//        import android.view.WindowManager;
//        import android.widget.ImageView;
//        import android.widget.RelativeLayout;
//        import android.widget.TextView;
//
//        import com.spire.authentication.AuthSingleton;
//        import com.spire.debug.Debug;
//        import com.spire.model.Communications;
//        import com.spire.model.struct.GcmVars;
//
///**
// * Created by evgeniy on 23.08.13.
// */
//
//public class FragmentActivitySurvey extends FragmentActivity {
//    AuthSingleton authSingleton;
//    ProgressDialog mConnectionProgressDialog;
//    protected Dialog dialog;
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//
//        authSingleton = AuthSingleton.getInstance(this);
//
//        LocalBroadcastManager.getInstance(this).registerReceiver(authSingleton.receiver_check_profile, new IntentFilter(getString(R.string.receiver_profile_check)));
//
//
//        mConnectionProgressDialog = new ProgressDialog(new ContextThemeWrapper(this, android.R.style.Theme_Holo_Light_Dialog));
//        mConnectionProgressDialog.setCancelable(false);
//        mConnectionProgressDialog.setMessage("Signing in...");
//
//    }
//
//    @Override
//    protected void onStart() {
//        LocalBroadcastManager.getInstance(this).registerReceiver(receiver_show_survey_dialog, new IntentFilter(getString(R.string.receiver_show_survey_dialog)));
//        LocalBroadcastManager.getInstance(this).registerReceiver(receiver_progress_dialog, new IntentFilter(getString(R.string.receiver_progress_dialog)));
//
//        Debug.log("ActivitySurvey", "onStart");
//        super.onStart();
//
//    }
//
//
//
//
//
//    @Override
//    protected void onStop() {
//        Debug.log("ActivitySurvey","onStop");
//
//        LocalBroadcastManager.getInstance(this).unregisterReceiver(receiver_show_survey_dialog);
//        LocalBroadcastManager.getInstance(this).unregisterReceiver(receiver_progress_dialog);
//
//        super.onStop();
//    }
//
//    private BroadcastReceiver receiver_show_survey_dialog = new BroadcastReceiver() {
//        @Override
//        public void onReceive(Context context, Intent intent) {
//            showSurveyDialog(intent.getStringExtra(GcmVars.gcm_uri), FragmentActivitySurvey.this);
//        }
//    };
//
//
//    public static void showSurveyDialog (final String uri, final Context context ){
//
//        AlertDialog.Builder bulder = new AlertDialog.Builder(new ContextThemeWrapper(context, android.R.style.Theme_Holo_Light_Dialog));
//
////        AlertDialog.Builder bulder = new AlertDialog.Builder(context);
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
////                        Intent emailIntent = new Intent( android.content.Intent.ACTION_SEND );
////                        String aEmailList[] = { "free_space-feedback@list.aalto.fi" };
////                        emailIntent.putExtra(android.content.Intent.EXTRA_EMAIL, aEmailList);
////                        emailIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, "User feedback");
////                        emailIntent.setType("plain/text");
////                        context.startActivity(Intent.createChooser(emailIntent, ""));
//
//                        if (uri != null && uri.length() > 0){
////                            Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
//                            Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
//
//                            context.startActivity(browserIntent);
//                            dialog.dismiss();
//                        }
//
//                    }
//                });
//        bulder.show();
//    }
//
//    public static void showRegisterDialog (final String uri, final Context context ){
//
//        AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(context, android.R.style.Theme_Holo_Light_Dialog));
//
////        AlertDialog.Builder bulder = new AlertDialog.Builder(context);
//        builder  .setTitle(R.string.title_registr_dialog)
//                .setMessage(R.string.content_registr_dialog)
//                .setCancelable(false)
//                .setPositiveButton(R.string.btn_registr_dialog_register_via_browser, new DialogInterface.OnClickListener() {
//                    @Override
//                    public void onClick(DialogInterface dialog, int which) {
//                        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
//                        context.startActivity(browserIntent);
//                        dialog.dismiss();
//                    }
//                })
//                .setNegativeButton(R.string.btn_registr_dialog_close_application, new DialogInterface.OnClickListener() {
//                    @Override
//                    public void onClick(DialogInterface dialog, int which) {
//
//                        if (Communications.getInstance(context).getUser().getmPlusClient().isConnected()) {
//                            Communications.getInstance(context).getUser().getmPlusClient().clearDefaultAccount();
//                            Communications.getInstance(context).getUser().getmPlusClient().disconnect();
//                            //Communications.getInstance(getApplicationContext()).getUser().getmPlusClient().connect();
//                            Communications.getInstance(context).setUser(null);
//                        }
//                        System.exit(0);
//                    }
//                });
//        builder.show();
//    }
//
//    @Override
//    protected void onDestroy() {
//        LocalBroadcastManager.getInstance(this).unregisterReceiver (authSingleton.receiver_check_profile);
//        super.onDestroy();
//    }
//
//
//
//    public BroadcastReceiver receiver_progress_dialog = new BroadcastReceiver() {
//        @Override
//        public void onReceive(Context context, Intent intent) {
//            if (intent.getBooleanExtra("is_show",false)){
//                mConnectionProgressDialog.show();
//            } else {
//                mConnectionProgressDialog.dismiss();
//            }
//        }
//    };
//
//
//
//    private void DialogWindow(){
//        LayoutInflater inflater= LayoutInflater.from(this);
//        View view = inflater.inflate(R.layout.authentication, null);
//        View title = inflater.inflate(R.layout.authentication_title, null);
//        AlertDialog.Builder builder = new AlertDialog.Builder(this);
//
//        builder
//                .setCustomTitle(title)
//                .setCancelable(false)
//                .setView(view);
//        dialog = builder.create();
//        ImageView img = (ImageView)findViewById(R.id.imageView2);
//
//        if (img != null){
//            WindowManager.LayoutParams wmlp = dialog.getWindow().getAttributes();
//            Display display = getWindowManager().getDefaultDisplay();
//            Point size = new Point();
//            display.getSize(size);
//            int iScreenHeight = size.y;
//            RelativeLayout.LayoutParams lp = ( RelativeLayout.LayoutParams ) img.getLayoutParams();
//            int ImgMargin = lp.topMargin;
//            wmlp.y = (ImgMargin +  img.getHeight()  + ( ImgMargin + img.getHeight() / 2 ) ) - iScreenHeight / 2;
//            wmlp.x = 0;
//        }
//        dialog.show();
//        dialog.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
//
//
//        TextView txt = (TextView) dialog.findViewById(R.id.textView);
//        //txt.setText(Html.fromHtml(getString(R.string.help)));
//        txt.setMovementMethod(LinkMovementMethod.getInstance());
//
//
//        com.google.android.gms.common.SignInButton LogInButton =
//                ( com.google.android.gms.common.SignInButton ) dialog.findViewById(R.id.btn_sign_in);
//
//        LogInButton.setOnClickListener ( new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
///*
//
//                if (view.getId() == R.id.btn_sign_in && !authSingleton.getmPlusClient().isConnected()) {
//                    if (mConnectionResult == null) {
//                        mConnectionProgressDialog.show();
//                        mPlusClient.connect();
//                    } else {
//                        try {
//                            mConnectionResult.startResolutionForResult( this, REQUEST_CODE_RESOLVE_ERR );
//                        } catch (IntentSender.SendIntentException e) {
//                            // Try connecting again.
//                            mConnectionResult = null;
//                            mPlusClient.connect();
//                        }
//                    }
//                }*/
//
//
//
//            }
//        });
//
//
//        dialog.setOnKeyListener(new DialogInterface.OnKeyListener() {
//            @Override
//            public boolean onKey(DialogInterface dialogInterface, int i, KeyEvent keyEvent) {
//                finish();
//                Debug.log("finish", "finish");
//                return false;
//            }
//        });
//    }
//
//
//}
