package com.spire.parking_procedure;

/* Copyright (C) Aalto University 2014
 *
 * Created by evgeniy on 13.08.13.
 */

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.LocationManager;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.location.DetectedActivity;
import com.google.android.gms.location.Geofence;
import com.spire.R;
import com.spire.activity_recognition.ActivityRecognitionIntentService;
import com.spire.activity_recognition.DetectionRemover;
import com.spire.activity_recognition.DetectionRequester;
import com.spire.debug.Debug;
import com.spire.geofence.GeofenceRemover;
import com.spire.geofence.GeofenceRequester;
import com.spire.geofence.ReceiveTransitionsIntentService;
import com.spire.geofence.SimpleGeofence;
import com.spire.model.Communications;
import com.spire.model.struct.Parking;

import java.util.ArrayList;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: evgeniy
 * Date: 01.06.13
 * Time: 14:07
 * To change this template use File | Settings | File Templates.
 */

// This service is managing geofences, location updating and activity recognition

public class TrackingService extends Service {

    private static final String TAG = "TrackingService";

    private LocationManager manager;
    private LocListener listener;
    // private PowerManager powerManager;
    // private PowerManager.WakeLock w1;

    private List<Geofence> mCurrentGeofences;
    private GeofenceRequester mGeofenceRequester;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Debug.log(TAG,"onCreate (register updateMap, startTracker, receiver_activity_recognition)");
//        Toast toast = Toast.makeText(getApplicationContext(), "service start", Toast.LENGTH_SHORT);
//        toast.show();

        mCurrentGeofences = new ArrayList<Geofence>();
        mGeofenceRequester = new GeofenceRequester(getApplicationContext());

        LocalBroadcastManager.getInstance(getApplicationContext()).registerReceiver(updateMap,
                new IntentFilter(getString(R.string.receiver_update)));

        LocalBroadcastManager.getInstance(getApplicationContext()).registerReceiver(startTracker,
                new IntentFilter(getString(R.string.receiver_start_tracker)));

        LocalBroadcastManager.getInstance(getApplicationContext()).registerReceiver(receiver_activity_recognition,
                new IntentFilter(getString(R.string.receiver_activity_recognition)));

        // MJR: Why GeofenceRequester (above) uses getApplicationContext() and the two below use "this"???
        // MJR: What is the difference in returned context?

        mDetectionRequester = new DetectionRequester(getApplicationContext());
        // mDetectionRemover = new DetectionRemover(this);


    }

    // Map<String,Geofence> map_geofence = new HashMap<String, Geofence>();
    ArrayList<String> arr_id_parking_geofence = new ArrayList<String>();

    private void addGeofense(){
        Debug.log(TAG, "addGeofense");
        ArrayList<Parking> parkings = Communications.getInstance(getApplicationContext()).getParkingArrayList();

        ArrayList<String> parking_ids = new ArrayList<String>();

        // Delete not visible geofence
        for (Parking current_parking : parkings){
            parking_ids.add(current_parking.getArea());
        }

        List<String> parking_ids_for_delete = new ArrayList<String>();


        for (String parking_id : arr_id_parking_geofence){
            if (!parking_ids.contains(parking_id)){
                parking_ids_for_delete.add(parking_id);
                if (ReceiveTransitionsIntentService.getCurrentGeofences().contains(parking_id)){
                    stopTracker();
                    ReceiveTransitionsIntentService.removeCurrentGeofences(parking_id);
                }
            }
        }

        arr_id_parking_geofence.removeAll(parking_ids_for_delete);


        for (String id_for_delete : parking_ids_for_delete){

            Debug.log("Delete","Delete geofence:" + id_for_delete + " " + parking_ids_for_delete.size());

        }



        if (parking_ids_for_delete.size() > 0){
            GeofenceRemover rem = new GeofenceRemover(getApplicationContext());
            rem.removeGeofencesById(parking_ids_for_delete);
        }


            //delete not visible geofence



        mCurrentGeofences.clear();


        for (Parking current : parkings){
            if (current.getRadius() != 0){

                if (!arr_id_parking_geofence.contains(current.getArea())){
                    arr_id_parking_geofence.add(current.getArea());
                    SimpleGeofence smg = new SimpleGeofence(current, Geofence.NEVER_EXPIRE, Geofence.GEOFENCE_TRANSITION_ENTER | Geofence.GEOFENCE_TRANSITION_EXIT );
                    mCurrentGeofences.add(smg.CreateGeofence());



                }

            }

        }



        if (mCurrentGeofences.size() > 0){
            if ( !mGeofenceRequester.getInProgressFlag() ){
                mGeofenceRequester.addGeofences(mCurrentGeofences);


                Debug.log(TAG,"add array geofence");
            }
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        // MJR: PARTIAL_WAKE_LOCK keeps CPU on, but lets screen and keyboard turn off
        // MJR: I don't know why we need these - forcing the GPS on based on activity seems to work also when screen is off.
        // powerManager = (PowerManager) getSystemService(Context.POWER_SERVICE);
        // w1 = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,"PARTIAL_WAKE_LOCK");
        // w1.acquire();

        listener = new LocListener(getApplicationContext());
        manager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        // Start Activity Recognition
        // Early start added by MJR to improve activity recognition performance
        if (!isActivityRecognitionRunning){
            startActivityRecognitionUpdates();
        }

        Debug.log(TAG, "onStartCommand (LocListener, location service manager allocated, activityrecognition started)");

        return super.onStartCommand(intent, flags, startId);
    }

    public void registerLocationUpdate(){
        if (!listener.isReportingGPS()) {
            Debug.log(TAG,"registerLocationUpdate");
            manager.requestLocationUpdates(LocationManager.GPS_PROVIDER,5000,0,listener);
            listener.setReportingGPS(true);
        }
    }

    public void unregisterLocationUpdate(){
        if (listener.isReportingGPS()) {
            Debug.log(TAG,"unregisterLocationUpdate");
            manager.removeUpdates(listener);
            listener.setReportingGPS(false);
        }
    }

    private BroadcastReceiver updateMap = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            switch (intent.getExtras().getString("cmd")){
                case "update":
                    Debug.log(TAG,"updateMap running addGeofense()");
                    addGeofense();
                    break;
                case "update_status":

                    break;
                default:
            }

        }
    };


    private BroadcastReceiver startTracker = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            boolean startState = intent.getExtras().getBoolean("start");

            if (startState){
                Debug.log(TAG, "startTracker starting");
                Debug.activityToast(getApplicationContext(), "Tracking starting (first geofence entered)");
                registerLocationUpdate();

                // MJR: Keeping Activity Recognition on all the time
                /* if (!isActivityRecognitionRunning){
                    startActivityRecognitionUpdates();
                } */
            } else {
                Debug.log(TAG, "startTracker ending");
                Debug.activityToast(getApplicationContext(), "Tracking ending (last geofence exited)");
                unregisterLocationUpdate();

                // MJR: Keeping Activity Recognition on all the time
                /* if (isActivityRecognitionRunning){
                    lastType = DetectedActivity.UNKNOWN;
                    stopActivityRecognitionUpdates();
                } */
            }

        }
    };

    //останавливаем трекер, если геофенс в которой находимся удалена
    private void stopTracker(){
        Debug.log(TAG, "stopTracker");
        unregisterLocationUpdate();
        // MJR: Keep activity recognition running
        /* if (isActivityRecognitionRunning){
            lastType = DetectedActivity.UNKNOWN;
            stopActivityRecognitionUpdates();
        } */
    }

    // Receive and forward recognized activities
    private static int lastType = DetectedActivity.UNKNOWN; // Memorize the previous recognized activity so we know, when activity changes
    private static long GPSStartTimer = 0; // Aux variable to delay before starting background GPS
    private BroadcastReceiver receiver_activity_recognition = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            int type = intent.getExtras().getInt("type");

            // MJR: Some debugging code to monitor, whether geofence detection is really working correctly
            /* if (ReceiveTransitionsIntentService.insideGeofence())
                Debug.activityToast(getApplicationContext(), "Inside geofence");
            else
                Debug.activityToast(getApplicationContext(), "Outside all geofences"); */


            // Start GPS on the background if the user is moving to improve geofence recognition
            if (isMovingWithDevice(type)) { // Device moving
                if (GPSStartTimer == 0) { // Create some starting hysteresis: if timer hasn't been started yet, start it
                    GPSStartTimer = System.currentTimeMillis();
                } else { // Timer already running
                    if ((System.currentTimeMillis()-GPSStartTimer)>15000) { // Moving over 15 secs
                        if (!LocListener.isReportingGPS()) {             // GPS reporting not on
                            registerLocationUpdate(); // Start periodic GPS reading
                        }
                    }
                }
            } else { // User is not moving
                GPSStartTimer = 0; // Reset GPS start delay
                if (LocListener.isReportingGPS()) { // GPS reporting on
                    if (!ReceiveTransitionsIntentService.insideGeofence()) { // Even though we are not inside a geofence
                        unregisterLocationUpdate(); // Stop periodic GPS reading
                    }
                }
            }

            // Report new recognized activity, if needed
            if (ReceiveTransitionsIntentService.insideGeofence()) { // Are we inside a reporting area?
                if (type == DetectedActivity.IN_VEHICLE || type == DetectedActivity.ON_FOOT || type == DetectedActivity.ON_BICYCLE){
                    if (lastType != type){ // Is the type compliant and did it change after previous recognition?
                        Debug.activityToast(getApplicationContext(), "New activity:" + ActivityRecognitionIntentService.getNameFromType(type) +
                                "\nPrevious: " + ActivityRecognitionIntentService.getNameFromType(lastType));
                        // Report activity to server
                        Communications.getInstance(getApplicationContext()).sendUserActivity(ActivityRecognitionIntentService.getNameFromType(type));
                        lastType = type;
                    }
                }

            } else { // Outside reporting area
                lastType = DetectedActivity.UNKNOWN; // Reset lastType to unknown
            }
        }
    };

    /**
     * Determine if the user is moving
     *
     * @param type The type of activity the user is doing (see DetectedActivity constants)
     * @return true if the user seems to be moving from one location to another, otherwise false
     */
    private boolean isMovingWithDevice(int type) {
        switch (type) {
            // These types mean that the user is probably not moving
            case DetectedActivity.STILL :
// Uncomment to consider TILTING=not moving            case DetectedActivity.TILTING :
            case DetectedActivity.UNKNOWN :
// Uncomment to only detect car and bicycle as moving devices         case DetectedActivity.ON_FOOT :
                return false;
            default:
                return true;
        }
    }


    @Override
    public void onDestroy() {

        Debug.log(TAG, "onDestroy");

        unregisterLocationUpdate();

        manager = null;
        listener = null;

        // MJR: CPU Wake lock is currently disabled - enable from class constructor before uncommenting these
        // w1.release();
        // w1 = null;

        LocalBroadcastManager.getInstance(getApplicationContext()).unregisterReceiver(updateMap);
        LocalBroadcastManager.getInstance(getApplicationContext()).unregisterReceiver(startTracker);
        LocalBroadcastManager.getInstance(getApplicationContext()).unregisterReceiver(receiver_activity_recognition);

        GeofenceRemover rem = new GeofenceRemover(getApplicationContext());
        rem.removeGeofencesByIntent(mGeofenceRequester.getRequestPendingIntent());

        lastType = DetectedActivity.UNKNOWN;

        if (isActivityRecognitionRunning){
            stopActivityRecognitionUpdates();
        }

        super.onDestroy();

    }

    public static int getLastType() {
        return lastType;
    }

    //
    // Activity Recognition
    // 
    // The activity recognition update request object
    private DetectionRequester mDetectionRequester;

    // The activity recognition update removal object
    private DetectionRemover mDetectionRemover;

    // Store the current request type (ADD or REMOVE)
    public enum REQUEST_TYPE {START, STOP}
    public static REQUEST_TYPE mRequestType;


    private boolean isActivityRecognitionRunning = false;

    public void startActivityRecognitionUpdates() {

        Debug.log(TAG, "startActivityRecognitionUpdates");

        // Check for Google Play services
        if (!servicesConnected()) {

            return;
        }

        /*
         * Set the request type. If a connection error occurs, and Google Play services can
         * handle it, then onActivityResult will use the request type to retry the request
         */

        mRequestType = REQUEST_TYPE.START;

        // Pass the update request to the requester object
        mDetectionRequester.requestUpdates();
        isActivityRecognitionRunning = true;

    }

    public void stopActivityRecognitionUpdates() {

        Debug.log(TAG, "stopActivityRecognitionUpdates");

        // Check for Google Play services
        if (!servicesConnected()) {

            return;
        }

        /*
         * Set the request type. If a connection error occurs, and Google Play services can
         * handle it, then onActivityResult will use the request type to retry the request
         */

        Debug.toast(this, "TrackingService / Stop updates");

        mRequestType = REQUEST_TYPE.STOP;

        // Pass the remove request to the remover object
        // MJR REPLACED the code below with GOOGLE sample code
        // mDetectionRemover.removeUpdates(mDetectionRequester.getRequestPendingIntent());

        /*
         * Cancel the PendingIntent. Even if the removal request fails, canceling the PendingIntent
         * will stop the updates.
         */

        // MJR REPLACED with GOOGLE sample code
        // mDetectionRequester.getRequestPendingIntent().cancel();

        // MJR took from: http://developer.android.com/training/location/activity-recognition.html
        mDetectionRequester.requestUpdates();

        isActivityRecognitionRunning = false;

    }

    private boolean servicesConnected() {
        Debug.log(TAG, "servicesConnected (Google Play)");

        // Check that Google Play services is available
        int resultCode =
                GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);

        // If Google Play services is available
        if (ConnectionResult.SUCCESS == resultCode) {

            // Continue
            return true;

            // Google Play services was not available for some reason
        } else {

            // Display an error dialog
            //GooglePlayServicesUtil.getErrorDialog(resultCode, this, 0).show();
            return false;
        }
    }



}
