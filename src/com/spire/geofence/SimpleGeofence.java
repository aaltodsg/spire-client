package com.spire.geofence;

import com.spire.model.struct.Parking;

public class SimpleGeofence {

    private long mExpirationDuration;
    private int mTransitionType;

    private Parking parking;


    public SimpleGeofence(Parking parking, long expiration, int transition) {
        this.mExpirationDuration = expiration;
        this.mTransitionType = transition;
        this.parking = parking;
    }

    public long getExpirationDuration() {
        return mExpirationDuration;
    }
    public int getTransitionType() {
        return mTransitionType;
    }

    public Parking getParking() {
        return parking;
    }

    public com.google.android.gms.location.Geofence CreateGeofence() {
        // Build a new Geofence object
        int radius;
        if (getParking().getRadius() == 0){
            radius = 0;
        } else {
            radius = getParking().getRadius();
        }
        return new com.google.android.gms.location.Geofence.Builder()
                .setRequestId(getParking().getArea())
                .setTransitionTypes(mTransitionType)
                .setCircularRegion(
                        getParking().getLatitude(),
                        getParking().getLongitude(),
                        radius)
                .setExpirationDuration(mExpirationDuration)
                .build();
    }

}
