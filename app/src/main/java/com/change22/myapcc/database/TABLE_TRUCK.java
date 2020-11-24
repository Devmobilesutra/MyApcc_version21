package com.change22.myapcc.database;

import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;

import com.change22.myapcc.config.MyApp;

/**
 * Created by Pramod Kale on 04/08/2016.
 */
public class TABLE_TRUCK {
    public static String NAME = "table_truck";

    public static String
            COL_ID = "location_id",
            COL_TRUCK_NO = "truck_no",
            COL_TRUCK_TYPE = "truck_type",
            COL_TRUCK_IMG = "truck_img",
            COL_LATITUDE = "latitude",
            COL_LONGITUDE = "longitude",
            COL_ICON_IMAGE = "icon_image";

    public static String CREATE_TABLE = "CREATE TABLE IF NOT EXISTS "
            + TABLE_TRUCK.NAME + " ("
            + TABLE_TRUCK.COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
            + TABLE_TRUCK.COL_TRUCK_NO + " TEXT, "
            + TABLE_TRUCK.COL_TRUCK_TYPE + " TEXT, "
            + TABLE_TRUCK.COL_TRUCK_IMG + " TEXT, "
            + TABLE_TRUCK.COL_LATITUDE + " TEXT, "
            + TABLE_TRUCK.COL_LONGITUDE + " TEXT, "
            + TABLE_TRUCK.COL_ICON_IMAGE + " TEXT)";

    public static void insert_truck_list(String id, String truck_no, String truck_type, String truck_img, String latitude, String longitude, String icon_img) {
        SQLiteDatabase db = MyApp.db.getReadableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(COL_ID, id);
        cv.put(COL_TRUCK_NO, truck_no);
        cv.put(COL_TRUCK_TYPE, truck_type);
        cv.put(COL_TRUCK_IMG, truck_img);
        cv.put(COL_LATITUDE, latitude);
        cv.put(COL_LONGITUDE, longitude);
        cv.put(COL_ICON_IMAGE, icon_img);

        long row_id = -1;
        try {
            row_id = db.insert(NAME, null, cv);
        } catch (SQLiteException e) {
            MyApp.log("TABLE_TRUCK", "Exception text is " + e.getMessage());
        }
        if (row_id == -1) {
            long count = db.update(NAME, cv, COL_ID + "=?", new String[]{id});
            MyApp.log("TABLE_TRUCK", "Updated count is " + count);
        } else {
            MyApp.log("TABLE_TRUCK", "insert id " + row_id);
        }
    }

    public static void update_truck_list(String id, String truck_no, String latitude, String longitude) {
        SQLiteDatabase db = MyApp.db.getReadableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(COL_ID, id);
        cv.put(COL_TRUCK_NO, truck_no);
        cv.put(COL_LATITUDE, latitude);
        cv.put(COL_LONGITUDE, longitude);
        MyApp.log("TABLE_TRUCK", " update_truck_list cv " + cv);
        try {
            long count = db.update(NAME, cv, COL_TRUCK_NO + "=?", new String[]{truck_no});
            MyApp.log("TABLE_TRUCK", "Updated count is " + count);
        } catch (SQLiteException e){
            MyApp.log("TABLE_TRUCK","Exception is " + e.getMessage());
        }

    }

    public static void delete_all_records() {
        SQLiteDatabase db = MyApp.db.getReadableDatabase();
        try {
            long rowCount = db.delete(NAME, null, null);
            MyApp.log("TABLE_TRUCK","Deleted rows are " + rowCount);
        } catch(SQLiteException e){
            MyApp.log("TABLE_TRUCK","SQLITEException is " + e.getMessage());
        }
    }

}
