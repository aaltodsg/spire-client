package com.spire.model.struct;

import com.google.android.gms.maps.model.Marker;

/**
 * Created by evgeniy on 01.08.13.
 */

public class MapMarker {

    Marker marker;
    Parking parking;


    public Marker getMarker() {
        return marker;
    }

    public Parking getParking() {
        return parking;
    }

    public void setMarker(Marker marker) {

        this.marker = marker;
    }

    public void setParking(Parking parking) {
        this.parking = parking;
    }
}
