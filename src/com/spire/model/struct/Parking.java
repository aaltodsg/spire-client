package com.spire.model.struct;

/**
 * Created by evgeniy on 25.07.13.
 */

import com.j256.ormlite.field.DataType;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import java.io.Serializable;

@DatabaseTable(tableName = "Parkings")
public class Parking implements Serializable{

    @DatabaseField( generatedId = true )
    private int id;

    @DatabaseField(dataType = DataType.STRING)
    private String areatype;

    @DatabaseField(dataType = DataType.STRING)
    private String parkid; // title of parking;

    @DatabaseField(dataType = DataType.STRING)
    private String area;

    @DatabaseField(dataType = DataType.DOUBLE)
    private double longitude;

    @DatabaseField(dataType = DataType.DOUBLE)
    private double latitude;

    @DatabaseField(dataType = DataType.STRING)
    private String status;

    @DatabaseField(dataType = DataType.INTEGER)
    private int size;

    @DatabaseField(dataType = DataType.INTEGER)
    private int radius;


    @DatabaseField(dataType = DataType.STRING)
    private String info;

    @DatabaseField(dataType = DataType.BOOLEAN )
    private boolean isparking = true;

    @DatabaseField(dataType = DataType.STRING)
    private String email;


    public int getId() {
        return id;
    }

    public String getAreatype() {
        return areatype;
    }

    public String getParkid() {
        return parkid;
    }

    public String getArea() {
        return area;
    }

    public double getLongitude() {
        return longitude;
    }

    public double getLatitude() {
        return latitude;
    }

    public String getStatus() {
        return status;
    }

    public int getSize() {
        return size;
    }

    public boolean getIsParking(){
        return isparking;
    }

    public void setId ( int id ){
        this.id = id;
    }

    public void setAreatype(String areatype) {
        this.areatype = areatype;
    }

    public void setParkid(String parkid) {
        this.parkid = parkid;
    }

    public void setArea(String area) {
        this.area = area;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public void setInfo(String info) {
        this.info = info;
    }

    public String getInfo() {
        return info;
    }

    public void setIsParking ( boolean isParking ){
        this.isparking = isParking;
    }

    public int getRadius() {
        return radius;
    }

    public void setRadius(int radius) {
        this.radius = radius;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}

