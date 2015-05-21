package com.spire.parking_procedure;

import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;
import com.spire.debug.Debug;
import com.spire.geofence.ReceiveTransitionsIntentService;
import com.spire.model.Communications;

/* Copyright (C) Aalto University 2014
 *
 * Created by evgeniy on 13.08.13.
 */

public class LocListener implements LocationListener {

    private static final String TAG = "LocListener";

    private Context context;

    // Track, whether LocListener is currently active (= reporting GPS coordinates)
    static private boolean reportingGPS = false;

    public LocListener(Context context) {
        this.context = context;
    }

    @Override
    public void onLocationChanged(Location location) {
        sendData(new LatLng(location.getLatitude(),location.getLongitude()));
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
    }

    @Override
    public void onProviderEnabled(String provider) {
    }

    @Override
    public void onProviderDisabled(String provider) {
    }


    private void sendData(LatLng location){
        if (ReceiveTransitionsIntentService.insideGeofence()){
            Communications.getInstance(context).sendTrackCoordinate(location);
            Debug.log(TAG, "sent track coordinates to Communications");

        }
    }

    public void setReportingGPS(boolean newGPSState) {
        this.reportingGPS = newGPSState;
    }

    static public boolean isReportingGPS() { return reportingGPS; }
}
