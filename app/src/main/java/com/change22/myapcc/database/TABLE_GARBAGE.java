package com.change22.myapcc.database;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;

import com.change22.myapcc.config.MyApp;
import com.change22.myapcc.dtoModel.DTOIssue;

import java.util.ArrayList;

/**
 * Created by Ganesh Borse on 04/08/2016.
 */
public class TABLE_GARBAGE {

    public static String NAME = "table_garbage";

    public static String
            COL_ID = "garbage_id",
            COL_USER_ID = "user_id",
            COL_ISSUE_NO = "issue_no",
            COL_COMPLAINT_TYPE = "complaint_type",
            COL_NAME = "user_name",
            COL_IMG_URL = "image_url",
            COL_AREA = "area",
            COL_LOCALITY = "locality",
            COL_CITY = "city",
            COL_STATE = "state",
            COL_PINCODE = "pincode",
            COL_LATITUDE = "garbage_latitude",
            COL_LONGITUDE = "garbage_longitude",
            COL_REPORTED_DATE = "reported_date",
            COL_ISSUE_STATUS = "issue_status";

    public static String CREATE_TABLE = "CREATE TABLE IF NOT EXISTS "
            + TABLE_GARBAGE.NAME + " ("
            + TABLE_GARBAGE.COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
            + TABLE_GARBAGE.COL_USER_ID + " TEXT, "
            + TABLE_GARBAGE.COL_ISSUE_NO + " TEXT, "
            + TABLE_GARBAGE.COL_COMPLAINT_TYPE + " TEXT, "
            + TABLE_GARBAGE.COL_NAME + " TEXT, "
            + TABLE_GARBAGE.COL_IMG_URL + " TEXT, "
            + TABLE_GARBAGE.COL_AREA + " TEXT, "
            + TABLE_GARBAGE.COL_LOCALITY + " TEXT, "
            + TABLE_GARBAGE.COL_CITY + " TEXT, "
            + TABLE_GARBAGE.COL_STATE + " TEXT, "
            + TABLE_GARBAGE.COL_PINCODE + " TEXT, "
            + TABLE_GARBAGE.COL_LONGITUDE + " TEXT, "
            + TABLE_GARBAGE.COL_LATITUDE + " TEXT, "
            + TABLE_GARBAGE.COL_REPORTED_DATE + " TEXT,"
            + TABLE_GARBAGE.COL_ISSUE_STATUS + " TEXT)";

    public static void insert_garbage_list(String id, String user_id, String issue_no, String complaint_type, String reported_by,
                                           String complaint_image, String area, String locality, String city, String state,
                                           String pincode, String latitude, String longitude, String complaint_date,String issue_status) {


        SQLiteDatabase db = MyApp.db.getReadableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(COL_ID, id);
        cv.put(COL_USER_ID, user_id);
        cv.put(COL_ISSUE_NO, issue_no);
        cv.put(COL_COMPLAINT_TYPE, complaint_type);
        cv.put(COL_NAME, reported_by);
        cv.put(COL_IMG_URL, complaint_image);
        cv.put(COL_AREA, area);
        cv.put(COL_LOCALITY, locality);
        cv.put(COL_CITY, city);
        cv.put(COL_STATE, state);
        cv.put(COL_PINCODE, pincode);
        cv.put(COL_LATITUDE, latitude);
        cv.put(COL_LONGITUDE, longitude);
        cv.put(COL_REPORTED_DATE, complaint_date);
        cv.put(COL_ISSUE_STATUS, issue_status);

        long row_id = -1;
        try {
            row_id = db.insert(NAME, null, cv);
        } catch (SQLiteException e) {
            MyApp.log("TABLE_GARBAGE", "Exception text is " + e.getMessage());
        }
        if (row_id == -1) {
            long count = db.update(NAME, cv, COL_ID + "=?", new String[]{id});
            MyApp.log("TABLE_GARBAGE", "Updated count is " + count);
        } else {
            MyApp.log("TABLE_GARBAGE", "insert id " + row_id);
        }
    }

    public static ArrayList<DTOIssue> load_more_items(String area, String page_no) {
        MyApp.log("in load_more_items()");
        SQLiteDatabase db = MyApp.db.getReadableDatabase();
        String sql = "select * from " + NAME + " where " + COL_LOCALITY + " =? order by " + COL_ID + " DESC";
        Cursor c = db.rawQuery(sql, new String[]{area});
        int count = c.getCount();
        MyApp.log("in load_more_items() count:" + count);
        DTOIssue dtoIssue = null;
        ArrayList<DTOIssue> list_more_items = new ArrayList<>();
        int i = 0;
        if (count > 0) {
            c.moveToFirst();

            do {
                if (i < 2) {
                    MyApp.log("in load_more_items() i:" + i);
                    i++;
                } else {

                    MyApp.log("in load_more_items() i:" + i);
                    String image_url = c.getString(c.getColumnIndexOrThrow(COL_IMG_URL));
                    String address = c.getString(c.getColumnIndexOrThrow(COL_AREA));
                    String date = c.getString(c.getColumnIndexOrThrow(COL_REPORTED_DATE));
                    String latitude1 = c.getString(c.getColumnIndexOrThrow(COL_LATITUDE));
                    String issue_status = c.getString(c.getColumnIndexOrThrow(COL_ISSUE_STATUS));
                    if(latitude1.equals("null"))
                    {
                        latitude1 = "0.0";
                    }

                    String longitude1 = c.getString(c.getColumnIndexOrThrow(COL_LONGITUDE));
                    if(longitude1.equals("null"))
                    {
                        longitude1 = "0.0";
                    }

                    double latitude = Double.parseDouble(latitude1);
                    double longitude = Double.parseDouble(longitude1);
                    String name = c.getString(c.getColumnIndexOrThrow(COL_NAME));

                    MyApp.log("in load_more_items() image_url:" + image_url + " address:" + address + " date:" + date + " latitude:" + latitude
                            + " longitude:" + longitude + " name:" + name);


                    dtoIssue = new DTOIssue(image_url, "", address, date, latitude, longitude, name,issue_status);
                    list_more_items.add(dtoIssue);
                    i++;
                }
            } while (c.moveToNext());
        }
        c.close();
        return list_more_items;
    }

    public static ArrayList<DTOIssue> load_issue_items() {
        MyApp.log("in load_issue_items()");
        SQLiteDatabase db = MyApp.db.getReadableDatabase();
        String sql = "select distinct " + COL_LOCALITY + " from " + NAME;
        Cursor c = db.rawQuery(sql, null);
        int count = c.getCount();
        MyApp.log("in load_issue_items() count:" + count);
        DTOIssue dtoIssue = null;
        ArrayList<DTOIssue> list_dtoIssue = new ArrayList<>();
        if (count > 0) {
            c.moveToFirst();
            do {
                String area = c.getString(c.getColumnIndexOrThrow(COL_LOCALITY));
                MyApp.log("in load_issue_items() area:" + area);
                dtoIssue = new DTOIssue(area, 0);
                list_dtoIssue.add(dtoIssue);

                String sql1 = "select * from " + NAME + " where " + COL_LOCALITY + " =? order by " + COL_ID + " DESC";
                Cursor c1 = db.rawQuery(sql1, new String[]{area});
                int count_issue = c1.getCount();
                MyApp.log("in load_issue_items() count_issue:" + count_issue);
                if (count_issue > 0) {
                    c1.moveToFirst();
                    int i = 0;
                    do {
                        if (i < 2) {
                            String image_url = c1.getString(c1.getColumnIndexOrThrow(COL_IMG_URL));
                            String address = c1.getString(c1.getColumnIndexOrThrow(COL_AREA));
                            String date = c1.getString(c1.getColumnIndexOrThrow(COL_REPORTED_DATE));
                            double latitude = Double.parseDouble(c1.getString(c1.getColumnIndexOrThrow(COL_LATITUDE)));
                            double longitude = Double.parseDouble(c1.getString(c1.getColumnIndexOrThrow(COL_LONGITUDE)));
                            String name = c1.getString(c1.getColumnIndexOrThrow(COL_NAME));
                            String issue_status = c1.getString(c1.getColumnIndexOrThrow(COL_ISSUE_STATUS));

                            MyApp.log("in load_more_items() image_url:" + image_url + " address:" + address + " date:" + date + " latitude:" + latitude
                                    + " longitude:" + longitude + " name:" + name);
                            dtoIssue = new DTOIssue(image_url, "", address, date, latitude, longitude, name,issue_status);
                            list_dtoIssue.add(dtoIssue);
                            MyApp.log("in load_more_items() i before ++:" + i);
                            i++;
                            MyApp.log("in load_more_items() i after ++:" + i);

                        }
                    } while (c1.moveToNext());
                }
                c1.close();
                if (count_issue > 2) {
                    dtoIssue = new DTOIssue(area, 2);
                    list_dtoIssue.add(dtoIssue);
                }

            } while (c.moveToNext());
        }

        c.close();
        return list_dtoIssue;
    }

    public static void change_status(String complaints_id) {
        SQLiteDatabase db = MyApp.db.getReadableDatabase();
        ContentValues cv;

        String[] array_complaints = complaints_id.split(",");
        if (array_complaints!=null && array_complaints.length>0){
            cv = new ContentValues();
            cv.put(COL_ISSUE_STATUS, "RESOLVED");

            for (int i = 0; i< array_complaints.length; i++){
                String complaint_id = array_complaints[i];
                try {
                    long row_id = db.update(NAME, cv, COL_ID + "=?", new String[]{complaint_id});
                    MyApp.log("TABLE_GARBAGE", "updated rwo id is " + row_id);
                } catch (SQLiteException e){
                    MyApp.log("TABLE_GARBAGE", "update exception is " + e.getMessage());
                }
            }
        }
    }

    public static void delete_all_records(String user_id) {
        SQLiteDatabase db = MyApp.db.getReadableDatabase();
        /*try {
            long rowCount = db.delete(NAME, COL_USER_ID + "!=?", new String[]{user_id});
            MyApp.log("TABLE_GARBAGE","Deleted rows are " + rowCount);
        } catch(SQLiteException e){
            MyApp.log("TABLE_GARBAGE","SQLITEException is " + e.getMessage());
        }*/
        try {
            long rowCount = db.delete(NAME, null, null);
            MyApp.log("TABLE_GARBAGE","Deleted rows are " + rowCount);
        } catch(SQLiteException e){
            MyApp.log("TABLE_GARBAGE","SQLITEException is " + e.getMessage());
        }
    }

    public static boolean getCount() {
        boolean flag = true;
        SQLiteDatabase db = MyApp.db.getReadableDatabase();
        String sql = "SELECT * FROM " + NAME;
        Cursor c = db.rawQuery(sql,null);
        if (c.getCount()>0)
            flag = false;
        c.close();
        return flag;
    }

    public static boolean check_user_complaint(String complaints_id) {
        boolean flag  = false;
        SQLiteDatabase db = MyApp.db.getReadableDatabase();
        String[] arr_complaint_id = complaints_id.split(",");
        if (arr_complaint_id!=null && arr_complaint_id.length>0){
            String user_id = MyApp.get_session(MyApp.SESSION_USER_ID);
            String complaint_id = "";
            for (int i = 0; i<arr_complaint_id.length; i++){
                complaint_id = arr_complaint_id[i];
                String sql = "SELECT " + COL_USER_ID + " FROM " + NAME + " WHERE " + COL_ID + "=?";
                Cursor c = db.rawQuery(sql,new String[]{complaint_id});
                if (c.getCount()>0){
                    c.moveToFirst();
                    String c_user_id = c.getString(c.getColumnIndexOrThrow(COL_USER_ID));
                    if (c_user_id.equalsIgnoreCase(user_id)){
                        flag = true;
                        c.close();
                        break;
                    }
                }
            }
        }
        return flag;
    }

    public static void delete_complaint(String complaint_id) {
        SQLiteDatabase db = MyApp.db.getReadableDatabase();
        try {
            long rowCount = db.delete(NAME, COL_ID + "=?", new String[]{complaint_id});
            MyApp.log("TABLE_GARBAGE","Deleted rows are " + rowCount);
        } catch(SQLiteException e){
            MyApp.log("TABLE_GARBAGE","SQLITEException is " + e.getMessage());
        }
    }
}
