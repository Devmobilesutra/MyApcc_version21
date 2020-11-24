package com.change22.myapcc.database;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;

import com.change22.myapcc.config.MyApp;
import com.change22.myapcc.dtoModel.DTOLocation;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Ganesh Borse on 03/08/2016.
 */
public class TABLE_LOCATIONS {

    public static String NAME = "table_location_list";

    public static String
            COL_ID = "location_id",
            COL_LOCATION_NAME = "location_name",
            COL_LOCATION_LATITUDE = "location_latitude",
            COL_LOCATION_LONGITUDE = "location_longitude",
            COL_LOCATION_LAT_LOG = "location_lat_log";

    public static String CREATE_TABLE = "CREATE TABLE IF NOT EXISTS "
                        + TABLE_LOCATIONS.NAME + " ("
                        + TABLE_LOCATIONS.COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                        + TABLE_LOCATIONS.COL_LOCATION_NAME + " TEXT, "
                        + TABLE_LOCATIONS.COL_LOCATION_LATITUDE + " TEXT, "
                        + TABLE_LOCATIONS.COL_LOCATION_LONGITUDE + " TEXT, "
                        + TABLE_LOCATIONS.COL_LOCATION_LAT_LOG+ " TEXT)";

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

    public static ArrayList<DTOLocation> get_searched_locations(String search_text) {
        ArrayList<DTOLocation> locationArrayList = null;
        SQLiteDatabase db = MyApp.db.getReadableDatabase();
        String sql = "SELECT * FROM " + NAME + " WHERE " + COL_LOCATION_NAME + " LIKE '%" + search_text + "%' GROUP BY " + COL_LOCATION_NAME ;
        Cursor c = db.rawQuery(sql,null);

        if (c.getCount()>0){
            c.moveToFirst();
            locationArrayList = new ArrayList<>();
            do {
                String address = c.getString(c.getColumnIndexOrThrow(COL_LOCATION_NAME));
                String latitude = c.getString(c.getColumnIndexOrThrow(COL_LOCATION_LATITUDE));
                String longitude = c.getString(c.getColumnIndexOrThrow(COL_LOCATION_LONGITUDE));

                DTOLocation dtoLocation = new DTOLocation(address,latitude,longitude);
                locationArrayList.add(dtoLocation);

            } while (c.moveToNext());
        }

        return locationArrayList;
    }

    public static boolean compare_lat_long(double lat, double log) {
        SQLiteDatabase db = MyApp.db.getReadableDatabase();
        String sql = "select * from " + NAME;
        Cursor c = db.rawQuery(sql, null);
        int count = c.getCount();
        int ret_value = 0;
        MyApp.log("in compare_lat_long()");
        if (count > 0) {
            c.moveToFirst();
            do {
                double ex_lat = Double.parseDouble(c.getString(c.getColumnIndexOrThrow(COL_LOCATION_LATITUDE)));
                double ex_log = Double.parseDouble(c.getString(c.getColumnIndexOrThrow(COL_LOCATION_LONGITUDE)));
                boolean flag = MyApp.compar_lat_long_withDB(ex_lat, ex_log, lat, log);
                MyApp.log("in compare_lat_long() flag:"+flag);
                if (flag) {
                    ret_value = 1;
                    break;
                }

            } while (c.moveToNext());
            c.close();
            if (ret_value == 1)
                return true;
            else
                return false;
        } else
            return false;
    }

    public static void insert_area_list(String id, String address, String latitude, String longitude, String lat_long, String last_update) {
        SQLiteDatabase db = MyApp.db.getReadableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(COL_ID, id);
        cv.put(COL_LOCATION_NAME, address);
        cv.put(COL_LOCATION_LATITUDE, latitude);
        cv.put(COL_LOCATION_LONGITUDE, longitude);
        cv.put(COL_LOCATION_LAT_LOG, lat_long);

        long row_id = -1;
        try {
            row_id = db.insert(NAME, null, cv);

            if (row_id == -1) {
                long count = db.update(NAME, cv, COL_ID + "=?", new String[]{id});
                MyApp.log("TABLE_LOCATIONS", "Updated count is " + count);
            } else {
                MyApp.log("TABLE_LOCATIONS", "insert id " + row_id);
            }

        } catch (SQLiteException e) {
            MyApp.log("TABLE_LOCATIONS", "Exception text is " + e.getMessage());
        }
    }

    public static List<DTOLocation> get_covered_locations() {
        ArrayList<DTOLocation> locationArrayList = null;
        SQLiteDatabase db = MyApp.db.getReadableDatabase();
        String sql = "SELECT * FROM " + NAME + " GROUP BY " + COL_LOCATION_NAME ;
        Cursor c = db.rawQuery(sql,null);

        if (c.getCount()>0){
            c.moveToFirst();
            int row_id = 1;
            locationArrayList = new ArrayList<>();
            do {
                String address = c.getString(c.getColumnIndexOrThrow(COL_LOCATION_NAME));
                String latitude = c.getString(c.getColumnIndexOrThrow(COL_LOCATION_LATITUDE));
                String longitude = c.getString(c.getColumnIndexOrThrow(COL_LOCATION_LONGITUDE));

                DTOLocation dtoLocation = new DTOLocation(row_id,address,latitude,longitude);
                row_id ++;
                locationArrayList.add(dtoLocation);

            } while (c.moveToNext());
        }

        return locationArrayList;
    }

    public static void delete_all_locations() {
        SQLiteDatabase db = MyApp.db.getReadableDatabase();

        try {
            long deleted_count = db.delete(NAME, null, null);
            MyApp.log(TABLE_LOCATIONS.class.getSimpleName(), "areas deleted count is " + deleted_count);
        } catch (SQLiteException e){
            MyApp.log(TABLE_LOCATIONS.class.getSimpleName(), "delete location exception is " + e.getMessage());
        }
    }
}
