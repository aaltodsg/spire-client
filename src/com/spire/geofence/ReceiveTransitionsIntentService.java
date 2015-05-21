/* Copyright (C) Aalto University 2014 */

package com.spire.geofence;

import android.app.IntentService;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;

import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.LocationClient;
import com.spire.R;
import com.spire.debug.Debug;
import com.spire.model.Communications;
import com.spire.parking_procedure.DirectionStruct;

import java.util.ArrayList;
import java.util.List;

public class ReceiveTransitionsIntentService extends IntentService {

    private static final String TAG = "ReceiveTransitionsIntentService";

    private static boolean insideGeofenceStatus = false;

    /**
     * Sets an identifier for this class' background thread
     */

    public ReceiveTransitionsIntentService() {
        super("ReceiveTransitionsIntentService");

    }

    private static ArrayList<String> currentGeofences = new ArrayList<String>();

    public static ArrayList<String> getCurrentGeofences() {
        return currentGeofences;
    }

    public static void addCurrentGeofences(String currentGeofences) {
        ReceiveTransitionsIntentService.currentGeofences.add(currentGeofences);
    }

    public static void removeCurrentGeofences(String currentGeofences) {
        ReceiveTransitionsIntentService.currentGeofences.remove(currentGeofences);
    }




    /**
     * Handles incoming intents
     * @param intent The Intent sent by Location Services. This Intent is provided
     * to Location Services (inside a PendingIntent) when you call addGeofences()
     */
    @Override
    protected void onHandleIntent(Intent intent) {

        // Create a local broadcast Intent
        Intent broadcastIntent = new Intent();

        // Give it the category for all intents sent by the Intent Service
        broadcastIntent.addCategory(GeofenceUtils.CATEGORY_LOCATION_SERVICES);

        // First check for errors
        if (LocationClient.hasError(intent)) {

            // If there's no error, get the transition type and create a notification
        } else {

            // Get the type of transition (entry or exit)
            int transition = LocationClient.getGeofenceTransition(intent);

            Debug.log(TAG, "transition: " + transition);
            Debug.log(TAG, "transition (categories): " + broadcastIntent.getCategories());

                    // Test that a valid transition was reported
            if ( transition == Geofence.GEOFENCE_TRANSITION_ENTER) {

                Debug.toast(getApplicationContext(), "ReceiveTransitionsIntentService / Geofence enter");

                // Post a notification
                List<Geofence> geofences = LocationClient.getTriggeringGeofences(intent);


                String[] geofenceIds = new String[geofences.size()];
                for (int index = 0; index < geofences.size() ; index++) {
                    geofenceIds[index] = geofences.get(index).getRequestId();
                }

                sendNotification(geofenceIds);
                startTracking(geofenceIds);

            } else if ( transition == Geofence.GEOFENCE_TRANSITION_EXIT ){

                Debug.toast(getApplicationContext(), "ReceiveTransitionsIntentService / Geofence exit");

                List<Geofence> geofences = LocationClient.getTriggeringGeofences(intent);
                String[] geofenceIds = new String[geofences.size()];
                for (int index = 0; index < geofences.size() ; index++) {
                    geofenceIds[index] = geofences.get(index).getRequestId();

                }


                sendNotification(geofenceIds);
                stopTracking(geofenceIds);


            }
        }
    }

    /**
     * Posts a notification in the notification bar when a transition is detected.
     * If the user clicks the notification, control goes to the main Activity.
     *
     */
    private void sendNotification(String[] area) {

        Debug.toast(getApplicationContext(),area.toString());
    }

    /**
     * Maps geofence transition types to their human-readable equivalents.
     * @param transitionType A transition type constant defined in Geofence
     * @return A String indicating the type of transition
     */
    private String getTransitionString(int transitionType) {
        switch (transitionType) {

            case Geofence.GEOFENCE_TRANSITION_ENTER:
                return "geofence_transition_entered";

            case Geofence.GEOFENCE_TRANSITION_EXIT:
                return "geofence_transition_exited";

            default:
                return "geofence_transition_unknown";
        }
    }


    private void startTracking(String[] area){
        Communications.getInstance(getApplicationContext()).sendGeofenceCrossed(area, DirectionStruct._enter);
        /*if (TrackingService.getLastType() == DetectedActivity.IN_VEHICLE || TrackingService.getLastType() == DetectedActivity.ON_FOOT || TrackingService.getLastType() == DetectedActivity.ON_BICYCLE ){
            Communications.getInstance(getApplicationContext()).sendUserActivity(ActivityRecognitionIntentService.getNameFromType(TrackingService.getLastType()));
        }*/


        //добавляем  геофенс в список
        for (int iter = 0; iter < area.length; iter++){
            addCurrentGeofences(area[iter]);
        }

        // MJR 4.3.2014: Only start tracker in case it was not running
        if (!insideGeofence()) {
            Debug.log(TAG, "startTracking - first geofence entered");
            sendNotifyStartTracker(true);
            insideGeofenceStatus = true;
        }
    }

    private void stopTracking(String[] area){
        Communications.getInstance(getApplicationContext()).sendGeofenceCrossed(area, DirectionStruct._exit);
        /*if (TrackingService.getLastType() == DetectedActivity.IN_VEHICLE || TrackingService.getLastType() == DetectedActivity.ON_FOOT || TrackingService.getLastType() == DetectedActivity.ON_BICYCLE ){
            Communications.getInstance(getApplicationContext()).sendUserActivity(ActivityRecognitionIntentService.getNameFromType(TrackingService.getLastType()));
        }*/

        //удаляем геофенс из саиска
        for (int iter = 0; iter < area.length; iter++){
            removeCurrentGeofences(area[iter]);
        }

        Debug.log(TAG, "stopTracking");
        // MJR added 4.3.2014: Suspecting that earlier activity recognition was closed down even though there were still geofences active
        if (getCurrentGeofences().isEmpty()) {
            Debug.log(TAG, "current GeoFences empty");
            if (insideGeofence()) {
                Debug.log(TAG, "user tracking was running - now stopping");
                sendNotifyStartTracker(false);
                insideGeofenceStatus = false;
            }
        }

    }

    private void sendNotifyStartTracker(boolean status){
        Intent intent = new Intent(getApplicationContext().getString(R.string.receiver_start_tracker));
        intent.putExtra("start",status);
        LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intent);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    //    private boolean isTrackingServiceRunning() {
//        ActivityManager manager = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
//        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
//            if ("com.spire.parking_procedure.TrackingService".equals(service.service.getClassName())) {
//                return true;
//            }
//        }
//        return false;
//    }

    public static boolean insideGeofence() {
        return insideGeofenceStatus;
    }

}