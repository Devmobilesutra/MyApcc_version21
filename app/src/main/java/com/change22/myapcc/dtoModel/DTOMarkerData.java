package com.change22.myapcc.dtoModel;

/**
 * Created by Ganesh Borse on 02/08/2016.
 */
public class DTOMarkerData {
    double latitude, longitude;
    String title, snippet;
    int img_id;
    String truck_img;

    public DTOMarkerData() {
    }

    public DTOMarkerData(double latitude, double longitude, String title, String snippet, int img_id) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.title = title;
        this.snippet = snippet;
        this.img_id = img_id;
    }

    public DTOMarkerData(double latitude, double longitude, String title, String snippet, int img_id, String truck_img) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.title = title;
        this.snippet = snippet;
        this.img_id = img_id;
        this.truck_img = truck_img;
    }

    public String getTruck_img() {
        return truck_img;
    }

    public void setTruck_img(String truck_img) {
        this.truck_img = truck_img;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public int getImg_id() {
        return img_id;
    }

    public void setImg_id(int img_id) {
        this.img_id = img_id;
    }

    public String getSnippet() {
        return snippet;
    }

    public void setSnippet(String snippet) {
        this.snippet = snippet;
    }
}
