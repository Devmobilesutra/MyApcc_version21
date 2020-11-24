package com.change22.myapcc.config;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.Service;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;

import com.change22.myapcc.R;

import java.util.List;
import java.util.Locale;

/**
 * Created by Pramod Kale on 05/08/2016.
 */
public class GPSTracker1 extends Service implements LocationListener {

    private final Context mContext;

    // flag for GPS status
    boolean isGPSEnabled = false;

    // flag for network status
    boolean isNetworkEnabled = false;

    // flag for GPS status
    boolean canGetLocation = false;

    Location location; // location
    double latitude; // latitude
    double longitude; // longitude
    String LatLog;

    // The minimum distance to change Updates in meters
    private static final long MIN_DISTANCE_CHANGE_FOR_UPDATES = 10; // 10 meters

    // The minimum time between updates in milliseconds
    private static final long MIN_TIME_BW_UPDATES = 10 * 1000; // 10 seconds

    // Declaring a Location Manager
    protected LocationManager locationManager;
    String LOG_TAG = "GPSTracker1";

    Dialog dialog1;

    public GPSTracker1(Context context) {
        this.mContext = context;
        getLocation();
    }

    public Location getLocation() {
        try {
            locationManager = (LocationManager) mContext
                    .getSystemService(LOCATION_SERVICE);

            // getting GPS status
            isGPSEnabled = locationManager
                    .isProviderEnabled(LocationManager.GPS_PROVIDER);

            // getting network status
            isNetworkEnabled = locationManager
                    .isProviderEnabled(LocationManager.NETWORK_PROVIDER);

            if (!isGPSEnabled && !isNetworkEnabled) {
                // no network provider is enabled
            } else {
                this.canGetLocation = true;
                // First get location from Network Provider
                if (isNetworkEnabled) {
                    locationManager.requestLocationUpdates(
                            LocationManager.NETWORK_PROVIDER,
                            MIN_TIME_BW_UPDATES,
                            MIN_DISTANCE_CHANGE_FOR_UPDATES, this);
                    Log.d(LOG_TAG, "Network");
                    if (locationManager != null) {
                        location = locationManager
                                .getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                        if (location != null) {
                            latitude = location.getLatitude();
                            longitude = location.getLongitude();
                        }
                    }
                }
                // if GPS Enabled get lat/long using GPS Services
                if (isGPSEnabled) {
                    if (location == null) {
                        locationManager.requestLocationUpdates(
                                LocationManager.GPS_PROVIDER,
                                MIN_TIME_BW_UPDATES,
                                MIN_DISTANCE_CHANGE_FOR_UPDATES, this);
                        Log.d(LOG_TAG, "GPS Enabled");
                        if (locationManager != null) {
                            location = locationManager
                                    .getLastKnownLocation(LocationManager.GPS_PROVIDER);
                            if (location != null) {
                                latitude = location.getLatitude();
                                longitude = location.getLongitude();
                            }
                        }
                    }
                }
                if (latitude != 0.0 && longitude != 0.0) {
                    setUpdatedLocation(latitude, longitude);
                }
                Log.d(LOG_TAG, "get my location latitude->" + latitude);
                Log.d(LOG_TAG, "get my location longitude->" + longitude);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return location;
    }

    @Override
    public void onLocationChanged(Location location) {
        Log.d(LOG_TAG, "onLocationChanged");
        if (location != null) {
            latitude = location.getLatitude();
            longitude = location.getLongitude();
            setUpdatedLocation(latitude, longitude);
            mContext.stopService(new Intent(mContext, GPSTracker1.class));
        }
        Log.d(LOG_TAG, "latitude->" + latitude);
        Log.d(LOG_TAG, "longitude->" + longitude);

    }

    @Override
    public void onProviderDisabled(String provider) {
        Log.d(LOG_TAG, "onProviderDisabled");
    }

    @Override
    public void onProviderEnabled(String provider) {
        Log.d(LOG_TAG, "onProviderEnabled");
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
        Log.d(LOG_TAG, "onStatusChanged");
    }

    @Override
    public IBinder onBind(Intent arg0) {
        Log.d(LOG_TAG, "onBind");
        return null;
    }

    /**
     * Function to get latitude
     */
    public double getLatitude() {
        if (location != null) {
            latitude = location.getLatitude();
        }
        Log.d(LOG_TAG, "latitude->" + latitude);
        // return latitude
        return latitude;
    }

    /**
     * Function to get longitude
     */
    public double getLongitude() {
        if (location != null) {
            longitude = location.getLongitude();
        }
        Log.d(LOG_TAG, "longitude->" + longitude);
        // return longitude
        return longitude;
    }

    @Override
    public void onDestroy() {
        Log.d(LOG_TAG, "onDestroy");
    }

    /*public void showSettingsAlert(Context context) {
        MyApp.log(LOG_TAG, "in showSettingsAlert");
        dialog1 = new Dialog(context);
        dialog1.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog1.setContentView(R.layout.gps_alert);
        dialog1.show();
        dialog1.setCancelable(false);
        dialog1.setCanceledOnTouchOutside(false);
        *//*Button btn_cancel = (Button) dialog1.findViewById(R.id.btn_cancel);
        btn_cancel.setOnClickListener(new View.OnClickListener() {
            public void onClick(View arg0) {
                // TODO Auto-generated method stub
                dialog1.dismiss();
            }
        });*//*
        TextView txt_ok = (TextView) dialog1.findViewById(R.id.txt_ok);
        txt_ok.setOnClickListener(new View.OnClickListener() {
            public void onClick(View arg0) {
                // TODO Auto-generated method stub
                Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                mContext.startActivity(intent);
                dialog1.dismiss();
            }
        });*/

    //New view
        /*final AlertDialog alertDialog = new AlertDialog.Builder(context).create();
        alertDialog.setTitle(getResources().getString(R.string.app_name));
        alertDialog.setMessage(getResources().getString(R.string.gps_setting));
        alertDialog.setIcon(R.mipmap.ic_launcher);

        alertDialog.setButton("Ok", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                mContext.startActivity(intent);
                alertDialog.dismiss();
            }
        });

        alertDialog.show();*/

    //  }


    public void showSettingsAlert(Context context) {
        MyApp.log(LOG_TAG, "in showSettingsAlert");
        dialog1 = new Dialog(context);
        dialog1.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog1.setContentView(R.layout.gps_alert);
        dialog1.show();
        dialog1.setCancelable(false);
        dialog1.setCanceledOnTouchOutside(false);
        /*Button btn_cancel = (Button) dialog1.findViewById(R.id.btn_cancel);
        btn_cancel.setOnClickListener(new View.OnClickListener() {
            public void onClick(View arg0) {
                // TODO Auto-generated method stub
                dialog1.dismiss();
            }
        });*/
        TextView txt_ok = (TextView) dialog1.findViewById(R.id.txt_ok);
        txt_ok.setOnClickListener(new View.OnClickListener() {
            public void onClick(View arg0) {
                // TODO Auto-generated method stub
                Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                dialog1.setCancelable(true);
                //dialog1.setCanceledOnTouchOutside(true);
                dialog1.dismiss();
                dialog1.cancel();
                mContext.startActivity(intent);
            }
        });

        //New view
        /*final AlertDialog alertDialog = new AlertDialog.Builder(context).create();
        alertDialog.setTitle(getResources().getString(R.string.app_name));
        alertDialog.setMessage(getResources().getString(R.string.gps_setting));
        alertDialog.setIcon(R.mipmap.ic_launcher);

        alertDialog.setButton("Ok", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                mContext.startActivity(intent);
                alertDialog.dismiss();
            }
        });
        alertDialog.show();*/

    }

    public boolean canGetLocation() {
        try {
            locationManager = (LocationManager) mContext
                    .getSystemService(LOCATION_SERVICE);

            // getting GPS status
            isGPSEnabled = locationManager
                    .isProviderEnabled(LocationManager.GPS_PROVIDER);

            // getting network status
            isNetworkEnabled = locationManager
                    .isProviderEnabled(LocationManager.NETWORK_PROVIDER);

            if (!isGPSEnabled && !isNetworkEnabled) {
                // no network provider is enabled
                this.canGetLocation = false;
            } else {
                this.canGetLocation = true;
            }
        } catch (Exception e) {

        }
        MyApp.log(LOG_TAG, "Can get location flag is " + this.canGetLocation);
        return this.canGetLocation;
    }


    public void setUpdatedLocation(double latitude, double longitude) {

        MyApp.set_session(MyApp.SESSION_USER_LATITUDE, latitude + "");
        MyApp.set_session(MyApp.SESSION_USER_LONGITUDE, longitude + "");

        String cityName = "", areaName = "";
        Geocoder geocoder = new Geocoder(mContext, Locale.getDefault());
        List<Address> addresses = null;
        try {
            LatLog = latitude + "," + longitude;

            addresses = geocoder.getFromLocation(latitude, longitude, 1);
            MyApp.log("get_location addresses ", addresses + "");
            cityName = addresses.get(0).getLocality();
            areaName = addresses.get(0).getAddressLine(0);
            MyApp.set_session(MyApp.SESSION_USER_AREA, areaName);
            MyApp.log("get_location LatLog", LatLog);
            MyApp.log("get_location cityName", cityName);
            MyApp.log("get_location address", areaName);
            if (!cityName.equals("")) {
                MyApp.set_session(MyApp.SESSION_GPS_CITY, cityName);
            } else {
                MyApp.log(LOG_TAG, "Cant fetch city name");
            }

            updateMyActivity(mContext, "3", "location", "Activity_map");

        } catch (Exception e) {
            MyApp.log("get_location getMyLocation exception is ", e.getMessage() + "");

            if (TextUtils.isEmpty(areaName)) {
                MyApp.log(LOG_TAG, "Cant fetch area");
                MyApp.set_session(MyApp.SESSION_USER_AREA, "");
                //if (((MyApp) getApplication()).isNetworkConnectionAvailable()) {
                Intent intent1 = new Intent(Intent.ACTION_SYNC, null, mContext, AppService.class);
                intent1.putExtra("Flag", "google_address");
                mContext.startService(intent1);
                //}
            }
        }
    }

    static void updateMyActivity(Context context, String response_code, String response_message, String flag) {

        Intent intent = new Intent(flag);
        //put whatever data you want to send, if any
        intent.putExtra("response_code", response_code);
        intent.putExtra("response_message", response_message);
        //send broadcast
        context.sendBroadcast(intent);

    }

    public void removeUpdates() {
        if (locationManager != null) {
            locationManager.removeUpdates(this);
            MyApp.log(LOG_TAG, "In removeUpdates");
        }
    }
}

