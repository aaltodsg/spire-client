package com.spire.model.struct;

import com.google.android.gms.maps.model.LatLng;

/**
 * Created by evgeniy on 12.08.13.
 */
public class DistinationMarker {
    private LatLng coordinats;
    private String areaid;

    public LatLng getCoordinats() {
        return coordinats;
    }

    public String getAreaid() {
        return areaid;
    }

    public void setCoordinats(LatLng coordinats) {
        this.coordinats = coordinats;
    }

    public void setAreaid(String areaid) {
        this.areaid = areaid;
    }
}
