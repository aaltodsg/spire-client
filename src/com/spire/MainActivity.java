package com.spire;

import android.app.ActivityManager;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.google.analytics.tracking.android.EasyTracker;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.plus.PlusClient;
import com.spire.authentication.Auth;
import com.spire.authentication.AuthActivity;
import com.spire.debug.Debug;
import com.spire.drawerlist.DrawerAdapter;
import com.spire.drawerlist.DrawerItem;
import com.spire.favorites.MyFavorites;
import com.spire.mapview.MapView;
import com.spire.model.Communications;
import com.spire.model.IntentActions;
import com.spire.model.struct.Parking;
import com.spire.parking_procedure.TrackingService;

import java.util.ArrayList;

public class MainActivity extends AuthActivity {
    
    private DrawerLayout mDrawerLayout;
    private ListView mDrawerList;
    private ActionBarDrawerToggle mDrawerToggle;

    private ArrayList <DrawerItem> mDrawerItems = new ArrayList<DrawerItem>();
    com.spire.Notification mNotification;
    DrawerAdapter mDrawerAdapter;

    private MapView mMap;

    AutoCompleteTextAdapter adapter;

    // The activity recognition update request object
    //private DetectionRequester mDetectionRequester;

    // The activity recognition update removal object
    //private DetectionRemover mDetectionRemover;

    // Store the current request type (ADD or REMOVE)
    //private ActivityUtils.REQUEST_TYPE mRequestType;


    CustomAutoCompleteTextView customAutoCompleteTextView;

    private static final String TAG = "MainActivity";

    private void fillData(){

        mDrawerItems.add ( new DrawerItem( 0, Communications.getInstance(this).getUser().getUsername()));
        mDrawerItems.add ( new DrawerItem( R.drawable.location_map, getString(R.string.drawer_map_view)));
        mDrawerItems.add ( new DrawerItem( R.drawable.rating_not_important, getString(R.string.drawer_favorites)));
        mDrawerItems.add ( new DrawerItem( R.drawable.action_settings, getString(R.string.drawer_preferences)));
        mDrawerItems.add ( new DrawerItem( R.drawable.content_email, getString(R.string.drawer_general_feedback)));


        //mDrawerItems.add ( new DrawerItem(0," Survey Test"));
    }



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Debug.log(TAG, "onCreate");
        super.onCreate(savedInstanceState);

        // MJR: This setContentView causes a runtime error (activity_main.xml cannot be rendered with the current IDE & rendering package - related or not)
        // but not starting it will result in a fatal error later on.
        setContentView(R.layout.activity_main);

        Communications com = Communications.getInstance(this);

//        if (!isTrackingServiceRunning())
//            startService(new Intent(getApplicationContext(), TrackingService.class));

        LocalBroadcastManager.getInstance( this ).registerReceiver(receive_notif_stat, new IntentFilter(getString(R.string.receiver_parked_notif)));
        LocalBroadcastManager.getInstance( this ).registerReceiver(receiver_start_service, new IntentFilter(getString(R.string.receiver_service_start)));

        //LocalBroadcastManager.getInstance( this ).registerReceiver(receiver_show_survey_dialog, new IntentFilter(getString(R.string.receiver_show_survey_dialog)));

        // Get detection requester and remover objects
       // mDetectionRequester = new DetectionRequester(this);
       // mDetectionRemover = new DetectionRemover(this);

//        startActivityRecognitionUpdates();

        //gcm
        //com.sendGeofenceCrossed("lat48_345115_long33_506569", DirectionStruct._exit);


        mMap = new MapView();

        createDrawer();
        getActionBar().setDisplayHomeAsUpEnabled(true);
        /*ActionBar actionBar = getActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        getActionBar().setDisplayShowHomeEnabled(false);
        actionBar.setDisplayShowCustomEnabled(true);

        /**/

        //LayoutInflater inflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        View v = getActionBar().getCustomView();//inflater.inflate(R.layout.action_bar, null);
//
      /*  android.app.ActionBar.LayoutParams params = new android.app.ActionBar.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.MATCH_PARENT, Gravity.RIGHT );*/

        /// Converts 4 dip into its equivalent px
       /* Resources r = getResources();
        int px = ( int ) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 4, r.getDisplayMetrics());
*/

        // it's for testing
        /*
        Button btn_for_test = (Button)v.findViewById(R.id.btn_for_test);
        btn_for_test.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showDebugAlert();
            }
        }); */
        // it's for testing

        /*params.setMargins( px, px, px, px );
        //actionBar.setCustomView(v, params);*/
//


//        ArrayAdapter <String> adapter = new ArrayAdapter<String>(this,android.R.layout.simple_dropdown_item_1line, /** putMass here */);
        customAutoCompleteTextView = (CustomAutoCompleteTextView) findViewById(R.id.tv_actionbar_AutoComplete);
        customAutoCompleteTextView.imgClearIcon = getResources().getDrawable(android.R.drawable.ic_menu_close_clear_cancel);

//
        adapter = new AutoCompleteTextAdapter(this, android.R.layout.simple_dropdown_item_1line );


//

        customAutoCompleteTextView.setAdapter( adapter );
        adapter.setNotifyOnChange(true);

        customAutoCompleteTextView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                   Communications com = Communications.getInstance(getApplicationContext());
                    com.getPlaceCoordinats(v.getText().toString());
                    Debug.log(TAG, "setOnEditorActionListener: " + v.getText().toString());

                    return true;
                }
                return false;
            }
        });/**/

        customAutoCompleteTextView.addTextChangedListener(new TextWatcher()
        {
            public void onTextChanged(CharSequence s, int start, int before, int count)
            {
                customAutoCompleteTextView.clrButtonHandler();
                if (customAutoCompleteTextView.justCleared)
                {
                    customAutoCompleteTextView.justCleared = false;
                }
            }
            public void beforeTextChanged(CharSequence s, int start, int count,   int after)
            {
                //unimplemented but required by TextWatcher
                return;
            }

            public void afterTextChanged(Editable s)
            {
                //unimplemented but required by TextWatcher
                return;
            }
        });

        customAutoCompleteTextView.setOnItemClickListener( new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                Communications com = Communications.getInstance(getApplicationContext());
                com.getPlaceCoordinats ( adapter.getItem ( position ));

                hideKeyboard(customAutoCompleteTextView);
            }
        });

        if (savedInstanceState == null) {

            Debug.log(TAG, "savedInstanceState == null");

            FragmentManager fragmentManager = getSupportFragmentManager();
            fragmentManager.beginTransaction().replace(R.id.content_frame, mMap ).commit();

        }
    }

    public BroadcastReceiver updateList = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            Debug.log(TAG,"Broadcast...." + intent.getExtras());
            Communications com = Communications.getInstance(context);
            if ( intent.getExtras() != null )
                if ( intent.getExtras().getString(getString(R.string.receiver_update_search_dest_marker)) != null ){
                        Debug.log(TAG,"Broadcast (coordinates)...." + com.getPlaceCoordinats());
                        if ( com.getPlaceCoordinats()!= null && mMap.getMap() != null ){
                            mMap.CreateDestinationMarker(com.getPlaceCoordinats(), intent.getExtras().getString(getString(R.string.receiver_update_search_dest_marker)));

                            customAutoCompleteTextView.dismissDropDown();
                        }
                }
            adapter.updateList( );
        }
    };

    /*public BroadcastReceiver ShowActivityType = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.e(TAG,"ShowActivityType on recieve: " + intent.getExtras().getString(getString(R.string.receiver_show_activity_type)) );
            if ( intent.getExtras() != null )
                if ( intent.getExtras().getString(getString(R.string.receiver_show_activity_type)) != null ){
                    Log.e(TAG,"ShowActivityType recieved");

                    Toast.makeText( getApplicationContext(),  intent.getExtras().getString(getString(R.string.receiver_show_activity_type)), Toast.LENGTH_LONG ).show();
                }
        }
    };*/

    private void createDrawer(){

        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);

        mDrawerList = (ListView) findViewById(R.id.lv_fragment_drawer);

        // set a custom shadow that overlays the main content when the drawer opens
        mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow, GravityCompat.START);
        // set up the drawer's list view with items and click listener

        //set into field items;
        fillData();

        mDrawerAdapter = new DrawerAdapter( this, mDrawerItems );
        mDrawerList.setAdapter(mDrawerAdapter);

        mDrawerList.setOnItemClickListener(new DrawerItemClickListener());

        // enable ActionBar app icon to behave as action to toggle nav drawer


        Button logout = (Button)findViewById(R.id.log_out_button);
        logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Communications.getInstance(getApplicationContext()).logout();

                if (Communications.getInstance(getApplicationContext()).getUser().getmPlusClient().isConnected()) {
                    Communications.getInstance(getApplicationContext()).getUser().getmPlusClient().clearDefaultAccount();


                    mPlusClient.revokeAccessAndDisconnect(new PlusClient.OnAccessRevokedListener() {
                        @Override
                        public void onAccessRevoked(ConnectionResult status) {

                        }
                    });

                    Communications.getInstance(getApplicationContext()).getUser().getmPlusClient().disconnect();
                    mPlusClient.disconnect();


                    stopService(new Intent(getApplicationContext(), TrackingService.class));
                    login();
                }

            }
        });

        // ActionBarDrawerToggle ties together the the proper interactions
        // between the sliding drawer and the action bar app icon
        mDrawerToggle = new ActionBarDrawerToggle(
                this,                  /* host Activity */
                mDrawerLayout,         /* DrawerLayout object */
                R.drawable.ic_drawer,  /* nav drawer image to replace 'Up' caret */
                R.string.drawer_open,  /* "open drawer" description for accessibility */
                R.string.drawer_close  /* "close drawer" description for accessibility */
        ) {
            /**/
            public void onDrawerClosed(View view) {
//                getActionBar().setTitle("open");
                invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()

                getActionBar().getCustomView().setVisibility(View.VISIBLE);

            }

            public void onDrawerOpened(View drawerView) {
//                getActionBar().setTitle("close");

                invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()

                getActionBar().getCustomView().setVisibility(View.INVISIBLE);

                hideKeyboard(customAutoCompleteTextView);
//
            }  /**/

            @Override
            public void onDrawerSlide(View drawerView, float slideOffset) {
                supportInvalidateOptionsMenu();
                mDrawerLayout.bringChildToFront(drawerView);



                super.onDrawerSlide(drawerView, slideOffset);
            }
        };

        getActionBar().setDisplayHomeAsUpEnabled(true);

        getActionBar().setDisplayShowHomeEnabled(false);

        mDrawerLayout.setDrawerListener(mDrawerToggle);

    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if (mDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }
        
        switch ( item.getItemId() ){
        
        	case R.id.search_item:
        		onSearchRequested();
        		return true;
        
        	default:
        		return false;
        }


    }


    /* The click listner for ListView in the navigation drawer */
    private class DrawerItemClickListener implements ListView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            Debug.log(TAG, "OnClick: pos.: " + position);
           selectItem(position);
        }
    }
    /**/
    private void selectItem(int position) {
        Debug.log(TAG, "Switcher position is: " + position);
        switch ( position ){
            case 0:
                break;
            case 1:
                Debug.log(TAG, "ItemSelected #1");
//                MapView fragment = new MapView();
                if ( mMap != null ){
//                	mDrawerToggle;
                    mDrawerLayout.closeDrawers();
                	break;
                	
                }else {
                	Debug.log (TAG, "fragment is not vissible");
                }

                FragmentManager fragmentManager = getSupportFragmentManager();
//                FragmentManager fragmentManager = getFragmentManager();
                fragmentManager.beginTransaction().replace(R.id.content_frame, mMap ).commit();

                break;

            case 2:
                Debug.log(TAG, "ItemSelected #2");

                Intent myIntent = new Intent(this, MyFavorites.class);
                startActivityForResult(myIntent, 0);

                // hide navigation Drawer
                mDrawerLayout.closeDrawer(Gravity.START);

                break;
            case 4:
                send_email();
                break;

            case 5:

                Communications.getInstance(this).testRequestSurvey();
                break;

            default:
                break;
        }


    }

    /**
     * When using the ActionBarDrawerToggle, you must call it during
     * onPostCreate() and onConfigurationChanged()...
     */

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        // Sync the toggle state after onRestoreInstanceState has occurred.

        Debug.log(TAG, "onPostCreate");


        mDrawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        // Pass any configuration change to the drawer toggls

        Debug.log(TAG, "onConfigurationChanged");

        mDrawerToggle.onConfigurationChanged(newConfig);
    }


    @Override
    protected void onStart() {
        Debug.log(TAG, "onStart (register receiver_update_search_list, start RequestTimer)");
        super.onStart();

        LocalBroadcastManager.getInstance( this ).registerReceiver(updateList, new IntentFilter(getString(R.string.receiver_update_search_list)));

        Communications.getInstance(this).startRequestTimer();

        // Google Analytics V2 start code
        EasyTracker.getInstance().activityStart(this);
    }

    @Override
    protected void onStop() {
        super.onStop();
        Communications.getInstance(this).stopRequestTimer();

        // Google Analytics V2 stop code
        EasyTracker.getInstance().activityStop(this);
    }

    @Override
    protected void onResume() {

        Debug.log(TAG, "onResume");

        if ( !mMap.isVisible() ) {
            Debug.log(TAG, "onResume: Display map, if not visible otherwise.");
            FragmentManager fragmentManager = getSupportFragmentManager();
//            FragmentManager fragmentManager = getFragmentManager();
            fragmentManager.beginTransaction().replace(R.id.content_frame, mMap ).commit();
        }

        super.onResume();
    }


    @Override
    protected void onDestroy() {

        Debug.log(TAG, "onDestroy");

        start_service_status(false);

        LocalBroadcastManager.getInstance( this ).unregisterReceiver(receive_notif_stat);
        LocalBroadcastManager.getInstance( this ).unregisterReceiver(receiver_start_service);


        Communications.getInstance(this).stopRequestTimer();

        super.onDestroy();
    }

    @Override
    protected void onPause() {
        LocalBroadcastManager.getInstance( this ).unregisterReceiver(updateList);

        Debug.log(TAG, "onPause (unregister updateList)");


        super.onPause();

    }

    public void hideKeyboard( TextView textedit ){
        InputMethodManager imm = (InputMethodManager)getSystemService(
                Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(textedit.getWindowToken(), 0 );
    }



    private boolean isTrackingServiceRunning() {
        ActivityManager manager = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {

            // MJR: This lists all the currently running services
            // Debug.log("Service",service.service.getClassName());

            if (TrackingService.class.getName().equals(service.service.getClassName())){
                Debug.log(TAG, "isTrackingServiceRunning: true");
                return true;
            }
        }
        return false;
    }



    public BroadcastReceiver receive_notif_stat = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            showNotif(intent.getExtras().getString("message"));

        }
    };

    @Override
    protected void onNewIntent(Intent intent) {

        Debug.log(TAG, "onNewIntent");

        handleIntent(intent);
    }

    private void handleIntent(Intent intent) {

        Debug.log(TAG, "handleIntent");

        if (intent.getAction() != null)

        switch ( intent.getAction() ){

            case IntentActions.ACTION_CREATE_DESTINATION_MARKER:

                if ( intent.getExtras().getParcelable(IntentActions.EXTRA_POSITION) != null
                        && intent.getExtras().getString(IntentActions.EXTRA_MARKER_NAME) != null ) {

                    LatLng position = intent.getExtras().getParcelable(IntentActions.EXTRA_POSITION);
                    String name = intent.getExtras().getString(IntentActions.EXTRA_MARKER_NAME);

                    mMap.CreateDestinationMarker ( position, name );

                }

                break;

            case IntentActions.ACTION_SHOW_SELECTED_PARKING:
                mMap.selectParking((Parking) intent.getExtras().getSerializable(IntentActions.EXTRA_PARKING));
                break;
            default:
                break;


        }

    }


    private BroadcastReceiver receiver_start_service = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            Debug.log(TAG, "BroadcastReceiver receiver_start_service");


            start_service_status(intent.getExtras().getBoolean("state"));
        }
    };

    public void start_service_status(boolean state){
        if (state){
            if (!isTrackingServiceRunning()){
                startService(new Intent(getApplicationContext(), TrackingService.class));
//                startActivityRecognitionUpdates();
                Debug.alert(this,"startService");
            }
        } else {

            if (isTrackingServiceRunning()){
                //TODO Check later, whether we really want to stop tracking after parking - MJR commenting out for now to make sure server gets a complete log
                //MJR: At the moment the problem is that tracking is stopped when parking is detected -> Geofence exits are not received in the server and
                //MJR: ReceiveTransitionsIntentService still thinks the device is inside a geofence
                // stopService(new Intent(getApplicationContext(), TrackingService.class));
//                stopActivityRecognitionUpdates();
                Debug.alert(this,"stopService");
            }
        }

        Debug.log(TAG, String.format("Service: %s", state));
    }

    public void showNotif(String message){
//        AlertDialog.Builder builder = new AlertDialog.Builder(this);
//        builder.setTitle("Title").setMessage(message)
//                .setCancelable(false)
//                .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
//                    public void onClick(DialogInterface dialog, int id) {
//                        dialog.cancel();
//                    }
//                }).setNegativeButton("Cancel",
//                new DialogInterface.OnClickListener() {
//                    public void onClick(DialogInterface dialog, int id) {
//                    }
//                });
//        builder.create().show();

    }

    public void showDebugAlert(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Title").setMessage("Test")
                .setCancelable(false)
                .setPositiveButton("ON_FOOT", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {

                        Communications.getInstance(getApplicationContext()).sendUserActivity("ON_FOOT");

                        dialog.cancel();
                    }
                }).setNegativeButton("IN_VEHICLE",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        Communications.getInstance(getApplicationContext()).sendUserActivity("ON_FOOT");

                        dialog.cancel();
                    }
                });
        builder.create().show();

    }

    public void send_email(){
        Intent emailIntent = new Intent( android.content.Intent.ACTION_SEND );
        String aEmailList[] = { "free_space-feedback@list.aalto.fi" };
        emailIntent.putExtra(android.content.Intent.EXTRA_EMAIL, aEmailList);
        emailIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, "User feedback");
        emailIntent.setType("plain/text");
        startActivity(emailIntent);

    }

    public void login(){
        Intent intent = new Intent(getApplication(), Auth.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP |
                Intent.FLAG_ACTIVITY_CLEAR_TASK |
                Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);


    }

    private void CreateImproveAlert ( ){
        AlertDialog.Builder bulder = new AlertDialog.Builder(this);
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
                        String url = "http://www.google.com/search?q=android+how+to+send+intent+go+to+web+page";
                        Intent i = new Intent(Intent.ACTION_VIEW);
                        i.setData(Uri.parse(url));
                        startActivity(i);

                    }
                });
        bulder.show();
    }

}