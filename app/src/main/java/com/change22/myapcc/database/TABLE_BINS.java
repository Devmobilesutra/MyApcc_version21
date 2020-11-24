package com.change22.myapcc.database;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;

import com.change22.myapcc.config.MyApp;

/**
 * Created by Ganesh Borse on 05/08/2016.
 */
public class TABLE_BINS {
    public static String NAME = "table_bins";

    public static String
            COL_ID = "bin_id",
            COL_LATITUDE = "latitude",
            COL_LONGITUDE = "longitude",
            COL_AREA = "area",
            COL_LOCALITY = "locality",
            COL_CITY = "city",
            COL_STATE = "state",
            COL_IMG_URL = "bin_img_url",
            COL_ICON_IMG = "icon_img";

    public static String CREATE_TABLE = "CREATE TABLE IF NOT EXISTS "
            + TABLE_BINS.NAME + " ("
            + TABLE_BINS.COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
            + TABLE_BINS.COL_LONGITUDE + " TEXT, "
            + TABLE_BINS.COL_LATITUDE + " TEXT, "
            + TABLE_BINS.COL_AREA + " TEXT, "
            + TABLE_BINS.COL_LOCALITY + " TEXT, "
            + TABLE_BINS.COL_CITY + " TEXT, "
            + TABLE_BINS.COL_STATE + " TEXT, "
            + TABLE_BINS.COL_IMG_URL + " TEXT, "
            + TABLE_BINS.COL_ICON_IMG + " TEXT)";


    public static void insert_bin_list(String id, String latitude, String longitude, String area, String locality,
                                       String city, String state, String bin_image, String icon_image, String last_update) {
        SQLiteDatabase db = MyApp.db.getReadableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(COL_ID, id);
        cv.put(COL_LATITUDE, latitude);
        cv.put(COL_LONGITUDE, longitude);
        cv.put(COL_AREA, area);
        cv.put(COL_LOCALITY, locality);
        cv.put(COL_CITY, city);
        cv.put(COL_STATE, state);
        cv.put(COL_IMG_URL, bin_image);
        cv.put(COL_ICON_IMG, icon_image);

        long row_id = -1;
        try {
            row_id = db.insert(NAME, null, cv);
        } catch (SQLiteException e) {
            MyApp.log("TABLE_BINS", "Exception text is " + e.getMessage());
        }
        if (row_id == -1) {
            long count = db.update(NAME, cv, COL_ID + "=?", new String[]{id});
            MyApp.log("TABLE_BINS", "Updated count is " + count);
        } else {
            MyApp.log("TABLE_BINS", "insert id " + row_id);
        }
    }

    public static void delete_all_records() {
        SQLiteDatabase db = MyApp.db.getReadableDatabase();
        try {
            long rowCount = db.delete(NAME, null, null);
            MyApp.log("TABLE_BINS","Deleted rows are " + rowCount);
        } catch(SQLiteException e){
            MyApp.log("TABLE_BINS","SQLITEException is " + e.getMessage());
        }
    }

}
