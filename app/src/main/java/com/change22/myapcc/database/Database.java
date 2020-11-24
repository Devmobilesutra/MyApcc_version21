package com.change22.myapcc.database;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;

import com.change22.myapcc.R;
import com.change22.myapcc.config.MyApp;
import com.change22.myapcc.dtoModel.DTOMarkerData;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * Created by Ganesh Borse on 03/08/2016.
 */
public class Database extends SQLiteOpenHelper {

    static final String DATABASE_NAME = "MyAPCC.db";
    static final int DATABASE_VERSION = 1;
    static String TAG = "In Database.java ",
            LOG_TAG = "Database";

    public Database(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        MyApp.log(LOG_TAG, "In onCreate");
        MyApp.log(TAG + "Constructor");
        try {
            MyApp.log(TAG, TABLE_LOCATIONS.NAME + "" + TABLE_LOCATIONS.CREATE_TABLE);
            db.execSQL(TABLE_LOCATIONS.CREATE_TABLE);

            MyApp.log(TAG, TABLE_GARBAGE.NAME + "" + TABLE_GARBAGE.CREATE_TABLE);
            db.execSQL(TABLE_GARBAGE.CREATE_TABLE);

            MyApp.log(TAG, TABLE_TRUCK.NAME + "" + TABLE_TRUCK.CREATE_TABLE);
            db.execSQL(TABLE_TRUCK.CREATE_TABLE);

            MyApp.log(TAG, TABLE_BINS.NAME + "" + TABLE_BINS.CREATE_TABLE);
            db.execSQL(TABLE_BINS.CREATE_TABLE);
        } catch (SQLiteException e) {
            MyApp.log(LOG_TAG, "SQLiteException is " + e.getMessage());
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

    public ArrayList<DTOMarkerData> getMarkersList() {
        ArrayList<DTOMarkerData> dataArrayList = null;
        SQLiteDatabase db = getReadableDatabase();


        // No need to fetch bin list and display them in map
        /*String sql_bin = " SELECT * FROM " + TABLE_BINS.NAME;
        Cursor c = db.rawQuery(sql_bin, null);
        MyApp.log(LOG_TAG,"Bin count is " + c.getCount());
        if (c.getCount() > 0) {
            c.moveToFirst();
            dataArrayList = new ArrayList<>();
            do {
                String str_lat = c.getString(c.getColumnIndexOrThrow(TABLE_BINS.COL_LATITUDE));
                String str_long = c.getString(c.getColumnIndexOrThrow(TABLE_BINS.COL_LONGITUDE));
                String str_title = c.getString(c.getColumnIndexOrThrow(TABLE_BINS.COL_LOCALITY));

                String str_snippet = c.getString(c.getColumnIndexOrThrow(TABLE_BINS.COL_AREA));
                String split[] = str_snippet.split(",");
                MyApp.log(Arrays.deepToString(split));
                MyApp.log("len->"+split.length);
                if(split.length > 3) {
                    str_snippet = split[0]+","+split[1]+",\n"+split[2];
                }
                MyApp.log(str_snippet);
                int id = R.drawable.green;
                double latitude = Double.parseDouble(str_lat);
                double longitude = Double.parseDouble(str_long);

                DTOMarkerData dtoMarkerData = new DTOMarkerData(latitude, longitude, str_title, str_snippet, id);
                dataArrayList.add(dtoMarkerData);

            } while (c.moveToNext());
        }
        c.close();*/

        String sql_truck = "SELECT * FROM " + TABLE_TRUCK.NAME;
        Cursor c1 = db.rawQuery(sql_truck, null);
        MyApp.log(LOG_TAG,"Truck count is " + c1.getCount());
        if (c1.getCount() > 0) {
            c1.moveToFirst();
            if (dataArrayList == null)
                dataArrayList = new ArrayList<>();
            do {
                String str_lat = c1.getString(c1.getColumnIndexOrThrow(TABLE_TRUCK.COL_LATITUDE));
                String str_long = c1.getString(c1.getColumnIndexOrThrow(TABLE_TRUCK.COL_LONGITUDE));
                String str_title = c1.getString(c1.getColumnIndexOrThrow(TABLE_TRUCK.COL_TRUCK_NO));
                String truck_type = c1.getString(c1.getColumnIndexOrThrow(TABLE_TRUCK.COL_TRUCK_TYPE));
                String snippet = c1.getString(c1.getColumnIndexOrThrow(TABLE_TRUCK.COL_TRUCK_TYPE));
                String truck_img = c1.getString(c1.getColumnIndexOrThrow(TABLE_TRUCK.COL_TRUCK_IMG));
                int id = R.drawable.red;
                if (truck_type.equalsIgnoreCase("Glutton"))
                    id = R.drawable.blue;
                double latitude = Double.parseDouble(str_lat);
                double longitude = Double.parseDouble(str_long);

                DTOMarkerData dtoMarkerData = new DTOMarkerData(latitude, longitude, str_title, snippet, id, truck_img);
                dataArrayList.add(dtoMarkerData);
            } while (c1.moveToNext());
        }
        c1.close();
        return dataArrayList;
    }
}
