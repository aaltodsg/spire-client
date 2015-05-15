package com.spire.model.orm;


import com.j256.ormlite.field.DataType;
import com.j256.ormlite.field.DatabaseField;

import com.j256.ormlite.table.DatabaseTable;

/**
 * Created by volodymyr on 02.08.13.
 */
@DatabaseTable(tableName = "Parkings")
public class Parking_old {

    @DatabaseField( id = true )
    private int id;

    @DatabaseField(dataType = DataType.STRING)
    private String areatype;

    @DatabaseField(dataType = DataType.STRING)
    private String parkid;

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

    @DatabaseField(dataType = DataType.STRING)
    private String info;

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
}
