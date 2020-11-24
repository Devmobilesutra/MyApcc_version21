package com.change22.myapcc.dtoModel;

/**
 * Created by Ganesh Borse on 02/08/2016.
 */
public class DTOIssue {

    String image_url;
    String title;
    String address;
    String str_date;
    String name;
    String area;



    String issue_status;
    double latitude, longitude;
    int item_type;
    int row_count;

    public DTOIssue(String area, int item_type) {
        this.area = area;
        this.item_type = item_type;
    }

    public DTOIssue(String area, int item_type,int row_count) {
        this.area = area;
        this.item_type = item_type;
        this.row_count=row_count;
    }

    public DTOIssue(String image_url, String title, String address, String str_date, double latitude, double longitude, String name,String issue_status) {
        this.image_url = image_url;
        this.title = title;
        this.address = address;
        this.str_date = str_date;
        this.latitude = latitude;
        this.longitude = longitude;
        this.name = name;
        this.issue_status=issue_status;
        item_type = 1;
    }

    public String getIssue_status() {
        return issue_status;
    }

    public void setIssue_status(String issue_status) {
        this.issue_status = issue_status;
    }
    public int getRow_count() {
        return row_count;
    }

    public void setRow_count(int row_count) {
        this.row_count = row_count;
    }

    public DTOIssue() {
    }

    public int getItem_type() {
        return item_type;
    }

    public String getArea() {
        return area;
    }

    public void setArea(String area) {
        this.area = area;
    }

    public void setItem_type(int item_type) {
        this.item_type = item_type;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getImage_url() {
        return image_url;
    }

    public void setImage_url(String image_url) {
        this.image_url = image_url;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getStr_date() {
        return str_date;
    }

    public void setStr_date(String str_date) {
        this.str_date = str_date;
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
}
