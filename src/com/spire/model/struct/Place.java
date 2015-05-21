package com.spire.model.struct;

/* Copyright (C) Aalto University 2014
 *
 * Created by volodymyr on 05.08.13.
 */
public class Place {

    private double longitude;
    private double latitude;

    private String name;

    public void setLongitude (double longitude) {
        this.longitude = longitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public void setName ( String name ){
        this.name = name;
    }

    //

    public double getLongitude() {
        return longitude;
    }

    public double getLatitude() {
        return latitude;
    }

    public String getName (){
        return this.name;
    }

}
