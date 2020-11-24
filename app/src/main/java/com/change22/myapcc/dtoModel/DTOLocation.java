package com.change22.myapcc.dtoModel;

/**
 * Created by Ganesh Borse on 04/08/2016.
 */
public class DTOLocation {

    public String address, latitude, longitude;
    int row_id;

    public DTOLocation() {
    }

    public DTOLocation(String address, String latitude, String longitude) {
        this.address = address;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public DTOLocation(int row_id, String address, String latitude, String longitude) {
        this.row_id = row_id;
        this.address = address;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public int getRow_id() {
        return row_id;
    }

    public void setRow_id(int row_id) {
        this.row_id = row_id;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getLatitude() {
        return latitude;
    }

    public void setLatitude(String latitude) {
        this.latitude = latitude;
    }

    public String getLongitude() {
        return longitude;
    }

    public void setLongitude(String longitude) {
        this.longitude = longitude;
    }
}
