package com.change22.myapcc.config;

import android.app.Application;
import android.content.ContentResolver;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;

import com.change22.myapcc.R;
import com.change22.myapcc.database.Database;
import com.change22.myapcc.database.TABLE_TRUCK;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.squareup.okhttp.FormEncodingBuilder;
import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.MultipartBuilder;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.net.URLEncoder;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import okio.Buffer;

/**
 * Created by Ganesh Borse on 02/08/2016.
 */
public class MyApp extends Application {

    private static final String TAG = "MyApp";
    public static final String SESSION_GPS_CITY = "gps_city";
    public static final String SESSION_USER_LATITUDE = "user_latitude", SESSION_USER_LONGITUDE = "user_longitude",
            SESSION_USER_AREA = "user_area", SESSION_USER_ID = "user_id";
    public static final String SESSION_SEARCH_LATITUDE = "search_latitude", SESSION_SEARCH_LONGITUDE = "search_longitude", SESSION_SEARCH_AREA = "search_area", SESSION_FROM_SEARCH = "from_search";
    public static final String SESSION_USER_NAME = "user_name", SESSION_USER_EMAIL = "user_email", SESSION_IS_REGISTERED = "is_registered";
    public static final String SESSION_ACTIVITY = "session_activity", SESSION_VERSION_DATE_CHECK = "version_date_check";
    public static final String SESSION_TRUCK_LAST_UPDATE = "truck_last_update", SESSION_GARBAGE_LAST_UPDATE = "garbage_last_update", SESSION_PHOTO_CAPTURE = "session_photo_capture",
            SESSION_BIN_LAST_UPDATE = "bin_last_update", SESSION_AREA_LAST_UPDATE = "area_last_update";
    public static final String SESSION_FIRST_LOCATION = "user_location_first";
    private static final java.lang.String LOG_TAG = "MyApp";
    public static final String SESSION_COMPLAINT_NOTIFICATION = "1";
    public static final String SESSION_GET_GPS = "get_gps_flag";
    static Context context = null;
    public static String SESSION_IS_GCM_REGISTRED_TO_SERVER = "is_gcm_registered_to_server";
    public static String SESSION_IS_GPS_ENABLED = "is_gps_enabled";
    public static String SESSION_FETCHING_LOCATIONS = "session_fetching_locations";

    GPSTracker gps = null;
    String LatLog, ServiceID;
    double latitude = 0.0;
    double longitude = 0.0;

    //Session Objects
    public static SharedPreferences sharedPref;
    public static SharedPreferences.Editor editor;
    String PREFS_NAME = "dsfwr34r334_r4#23e2e";
    public static String SessionKey = "j5aD9uweHEAncWdj";//Must have 16 character session key
    public static AESAlgorithm aes;

    // Whether there is a Wi-Fi connection.
    private static boolean wifiConnected = false;
    // Whether there is a mobile connection.
    private static boolean mobileConnected = false;

    //GCM Variables
    public static String regid = "";
    public static final String GCM_SENDER_ID = "505891989185";
    public static final String SESSION_NOTIFICATION_ID = "notification_id";
    public static final String PREFS_PROPERTY_REG_ID = "registration_id";
    private static final String PROPERTY_APP_VERSION = "appVersion";
    GoogleCloudMessaging gcm;

    public static Database db = null;

    public static String
            local_url = "",
            change_bhai_url = "http://changebhai.in/service/index.php/",
            new_server_url = "http://trackmyapcc.com/service/index.php/",
            akbar_server_url = "http://148.72.214.186/akbar/service/index.php/",
            server_url = "http://pkclasses.co.in/changebhai-services/",
            base_url = new_server_url,
            url_get_garbage_list = base_url + "complaint/list",
            url_get_garbage_list_all = base_url + "complaint/all_list",
            url_report_garbage = base_url + "complaint/register_new",//"http://192.168.0.239/MyAPCC/changebhai-webservices/index.php/Complaint/register",
            url_get_truck_list = base_url + "truck/list",
            url_get_area_list = base_url + "truck/area_list",
            url_register_user = base_url + "user/register",
            url_verify_mobile = base_url + "User/send_otp",
          //  url_contact_us = server_url + "User/send_email",
            url_contact_us = new_server_url + "Complaint/send_email_contactUs",


   // http://trackmyapcc.com/service/index.php/Complaint/send_email_contactUs


            url_version_check = "http://trackmyapcc.com/service/version.php",
    //url_version_check = "http://192.168.0.246/clean_apcc/version.php",
    url_get_bin_list = base_url + "garbage/bin_list",
            url_get_near_truck_updates = new_server_url + "User/get_near_trucks";

    public void onCreate() {
        super.onCreate();
        log("In MyApp.java - onCreate");



        if (context == null)
            context = getApplicationContext();

        if (db == null)
            db = new Database(context);

        sharedPref = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        editor = sharedPref.edit();
        aes = new AESAlgorithm();

        gcm = GoogleCloudMessaging.getInstance(this);
        if (regid.length() == 0)
            regid = getRegistrationId(context);

        log(regid);

    }








    /*
     *  GCM FUNCTIONS
	 */

    public void getRegistrationGCMID() {
        if (checkPlayServices()) {
            // Retrieve registration id from local storage
            regid = getRegistrationId(context);
            log("register id on getRegistrationGCMID is " + regid);
            if (TextUtils.isEmpty(regid)) {
//		    	Log.i("Empty",regid);
                registerInBackground();

            } else {
//		    	Log.i("Not empty",regid);

            }
            Log.i("Store in database", regid);
        } else {
//		    Log.i(Globals.TAG, "No valid Google Play Services APK found.");
        }
    }

    private boolean checkPlayServices() {
        int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
        if (resultCode != ConnectionResult.SUCCESS) {
            if (GooglePlayServicesUtil.isUserRecoverableError(resultCode)) {
                //		GooglePlayServicesUtil.getErrorDialog(resultCode, this, PLAY_SERVICES_RESOLUTION_REQUEST).show();
            } else {
                //	    	Log.i(Globals.TAG, "This device is not supported.");
            }
            return false;
        }
        return true;
    }

    public String getRegistrationId(Context context) {
        final SharedPreferences prefs = getGcmPreferences(context);
        String registrationId = prefs.getString(PREFS_PROPERTY_REG_ID, "");
        Log.i(TAG, "gcm_id is " + registrationId);
        if (registrationId == null || registrationId.equals("")) {
            Log.i(TAG, "Registration not found.");
            return "";
        }
        // Check if app was updated; if so, it must clear the registration ID
        // since the existing regID is not guaranteed to work with the new
        // app version.
        int registeredVersion = prefs.getInt(PROPERTY_APP_VERSION, Integer.MIN_VALUE);
        int currentVersion = getAppVersion(context);
        if (registeredVersion != currentVersion) {
//		    Log.i(Globals.TAG, "App version changed.");
            return "";
        }
        return registrationId;
    }

    private static int getAppVersion(Context context) {
        try {
            PackageInfo packageInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
            return packageInfo.versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            // should never happen
            throw new RuntimeException("Could not get package name: " + e);
        }
    }

    private SharedPreferences getGcmPreferences(Context context) {
        // This sample app persists the registration ID in shared preferences,
        // but how you store the regID in your app is up to you.
        return getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    private void registerInBackground() {
        new AsyncTask<Void, Void, String>() {
            @Override
            protected String doInBackground(Void... params) {
                String msg = "";
                try {
                    if (gcm == null) {
                        gcm = GoogleCloudMessaging.getInstance(context);
                    }
                    regid = gcm.register(GCM_SENDER_ID);
                    msg = "Device registered, registration ID=" + regid;
                    log(msg);
                    // You should send the registration ID to your server over
                    // HTTP, so it can use GCM/HTTP or CCS to send messages to your app.
//		    sendRegistrationIdToBackend();
//		    postData(regid);
                    // For this demo: we use upstream GCM messages to send the
                    // registration ID to the 3rd party server

                    // Persist the regID - no need to register again.
                    storeRegistrationId(context, regid);
                } catch (Exception ex) {
                    msg = "Error :" + ex.getMessage();
                    // If there is an error, don't just keep trying to register.
                    // Require the user to click a button again, or perform
                    // exponential back-off.
                }
                return msg;
            }

            private void storeRegistrationId(Context context, String regId) {
                final SharedPreferences prefs = getGcmPreferences(context);
                int appVersion = getAppVersion(context);
                Log.i(TAG, "Saving regId on app version " + appVersion);
                SharedPreferences.Editor editor = prefs.edit();
                editor.putString(PREFS_PROPERTY_REG_ID, regId);
                editor.putInt(PROPERTY_APP_VERSION, appVersion);
                editor.commit();

                set_session(PREFS_PROPERTY_REG_ID, regId);
                set_session(PROPERTY_APP_VERSION, appVersion + "");
                Log.i(TAG, "Saving regId on app version " + regId);

            }

            @Override
            protected void onPostExecute(String msg) {
//	    	Log.e("","Device Registered");
//		((EditText) findViewById(R.id.txtPin)).setText(regid);
            }
        }.execute(null, null, null);
    }

    public static void log(String str) {
        if (str.length() > 10000) { //4000
            Log.i(TAG, str.substring(0, 10000));
            log(str.substring(10000));
        } else
            Log.i(TAG, str);
    }

    public static void log(String LOG_TAG, String str) {
        if (str.length() > 10000) {
            Log.i(TAG + "--" + LOG_TAG, str.substring(0, 10000));
            log(str.substring(10000));
        } else
            Log.i(TAG + "--" + LOG_TAG, str);
    }

    public static void log1(String str) {
        if (str.length() > 4000) {
            Log.i(TAG, str.substring(0, 4000));
            log(str.substring(4000));
        } else
            Log.i(TAG, str);
    }

    public boolean isNetworkConnectionAvailable() {

        ConnectivityManager connMgr =
                (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeInfo = connMgr.getActiveNetworkInfo();
        if (activeInfo != null && activeInfo.isConnected()) {
            wifiConnected = activeInfo.getType() == ConnectivityManager.TYPE_WIFI;
            mobileConnected = activeInfo.getType() == ConnectivityManager.TYPE_MOBILE;
            if (wifiConnected) {
                log(getString(R.string.wifi_connection));
            } else if (mobileConnected) {
                log(getString(R.string.mobile_connection));
            }
            return true;
        } else {
            log(getString(R.string.no_wifi_or_mobile));
            return false;
        }
        // END_INCLUDE(connect)
    }

    public static void set_session(String key, String value) {
        log("SetSession Key=" + key + "__value=" + value);
        String temp_key = aes.Encrypt(key);
        String temp_value = aes.Encrypt(value);
        MyApp.editor.putString(temp_key, temp_value);
        MyApp.editor.commit();
    }

    public static String get_session(String key) {
        String temp_key = aes.Encrypt(key);
        if (sharedPref.contains(temp_key)) {
            return aes.Decrypt(sharedPref.getString(temp_key, ""));
        } else
            return "";
    }

    public static int get_Intsession(String key) {
        String temp_key = aes.Encrypt(key);
        if (sharedPref.contains(temp_key)) {
            String str = aes.Decrypt(sharedPref.getString(temp_key, ""));
            return Integer.parseInt(str);
        } else
            return 0;
    }

    public static void log_bundle(Bundle extras) {
        Log.i("MyApp", "In log_bundle");
        if (extras != null) {
            for (String key : extras.keySet()) {
                Log.d("MyApp", key + " = " + extras.get(key) + "\"");
            }
        } else {
            Log.i("MyApp", "Bundle->" + extras);
        }
    }

    public static long get_Longsession(String key) {
        String temp_key = aes.Encrypt(key);
        if (sharedPref.contains(temp_key)) {
            String str = aes.Decrypt(sharedPref.getString(temp_key, ""));
            return Long.parseLong(str);
        } else
            return 0;
    }

    public static String get_device_id() {
        ContentResolver cr = context.getContentResolver();
        return URLEncoder.encode(Settings.Secure.getString(cr, Settings.Secure.ANDROID_ID));
    }

    public void getMyLocation(Context context) {
        log("get_location", "IN FUNCTION");
        gps = new GPSTracker(context);
        // check if GPS enabled
        if (gps.canGetLocation()) {

            latitude = gps.getLatitude();
            longitude = gps.getLongitude();
            LatLog = latitude + "," + longitude;
            Geocoder geocoder = new Geocoder(this, Locale.getDefault());
            List<Address> addresses = null;
            try {
                String cityName = "";
                addresses = geocoder.getFromLocation(latitude, longitude, 1);
                log("get_location addresses ", addresses + "");
                cityName = addresses.get(0).getLocality();
                String areaName = addresses.get(0).getAddressLine(1);
                MyApp.set_session(SESSION_USER_LATITUDE, latitude + "");
                MyApp.set_session(SESSION_USER_LONGITUDE, longitude + "");
                MyApp.set_session(SESSION_USER_AREA, areaName);
                log("get_location LatLog", LatLog);
                log("get_location cityName", cityName);
                if (!cityName.equals("")) {
                    MyApp.set_session(MyApp.SESSION_GPS_CITY, cityName);
                }

            } catch (Exception e) {
                log("get_location getMyLocation", e.getMessage() + "");
            }

        } else {
            LatLog = "";
            Log.e("LatLog", "can't get location");
            // can't get location
            // GPS or Network is not enabled
            // Ask user to enable GPS/network in settings
            gps.showSettingsAlert(context);
        }
    }

    public static boolean isValidMobile(String phone) {
        return android.util.Patterns.PHONE.matcher(phone).matches();
    }

    public static boolean isValidMail(String email) {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    public static String get_current_date() {
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        Date date = new Date();
        return dateFormat.format(date);
    }

    public static void deleteCache(Context context) {
        try {
            File dir = context.getCacheDir();
            deleteDir(dir);
        } catch (Exception e) {
        }
    }

    //Used to clear cache memory
    public static boolean deleteDir(File dir) {
        if (dir != null && dir.isDirectory()) {
            MyApp.log(TAG + "--" + LOG_TAG, "In delete cache function");
            String[] children = dir.list();
            for (int i = 0; i < children.length; i++) {
                boolean success = deleteDir(new File(dir, children[i]));
                if (!success) {
                    return false;
                }
            }
            return dir.delete();
        } else if (dir != null && dir.isFile()) {
            return dir.delete();
        } else {
            return false;
        }
    }

    //Web Services
    public static String post_server_call(String url, RequestBody formBody) {
        long REQ_TIMEOUT = 600;
        log("post_server_callUrl:" + url);

        try {
            Buffer buffer = new Buffer();
            formBody.writeTo(buffer);
            //log("post_for_body:" + buffer.readUtf8().toString());

        } catch (IOException e) {

        }

        try {
            OkHttpClient client = new OkHttpClient();
            client.setConnectTimeout(REQ_TIMEOUT, TimeUnit.SECONDS);
            client.setReadTimeout(REQ_TIMEOUT, TimeUnit.SECONDS);
            client.setWriteTimeout(REQ_TIMEOUT, TimeUnit.SECONDS);
            Request request = new Request.Builder()
                    .url(url)
                    .post(formBody)
                    .build();
            Response response = client.newCall(request).execute();

            int status_code = response.code();
            boolean status = response.isSuccessful();
            log("logtime:StatusCode:" + status_code);
            log("logtime:ResponseStatus:" + status);
            String res = response.body().string();
            log("logtime:ResponseSize:" + res.length());
            log("logtime:Response:" + res);
            log("post_server_callResponseStatus:" + response.isSuccessful());
            return res;
        } catch (Exception e) {

            log("logtime:E1->" + e + "");
            log("logtime:E2->" + e.getMessage());
            log("logtime:E3->" + e.getLocalizedMessage());
            return "-0";
        }
    }

    public static String get_truck_list(String truck_last_update) {
        RequestBody formBody = new FormEncodingBuilder()
                .add("last_update", truck_last_update)
                .build();
        MyApp.log("Form body to get truck list-- " + formBody.toString());
        return post_server_call(url_get_truck_list, formBody);
    }

    public static String get_garbage_list(String garbage_last_update, String user_id) {
        RequestBody formBody = new FormEncodingBuilder()
                .add("last_update", garbage_last_update)
                .add("user_id", user_id)
                .build();
        MyApp.log("Form body to get garbage list-- " + formBody.toString());
        return post_server_call(url_get_garbage_list, formBody);
    }

    public static String get_bin_list(String bin_last_update) {
        RequestBody formBody = new FormEncodingBuilder()
                .add("last_update", bin_last_update)
                .build();
        MyApp.log("Form body to get bin list-- " + formBody.toString());
        return post_server_call(url_get_bin_list, formBody);
    }

    public static String get_area_list(String area_last_update) {
        RequestBody formBody = new FormEncodingBuilder()
                .add("last_update", area_last_update)
                .build();
        MyApp.log("Form body to get area list-- " + formBody.toString());
        return post_server_call(url_get_area_list, formBody);
    }

    public static String add_report_garbage(String name, String email_id, String device_id, String gcm_id, String image_file, String latitude, String longitude, String date, String user_id) {

        log("MyApp", "image_file in add_report_garbage: " + image_file);
        log("MyApp", "latitude in add_report_garbage: " + latitude);
        MultipartBuilder obj = new MultipartBuilder().type(MultipartBuilder.FORM);
        File fileD = new File(image_file);
        log(TAG, "IsD FIle->" + fileD.exists());
        try {
            if (fileD.exists())
                obj.addFormDataPart("image_file", fileD.getName(), RequestBody.create(MediaType.parse("image/jpg"), fileD));//"application/pdf"
        } catch (Exception e) {
            MyApp.log(TAG, "image exception is " + e.getMessage());
            obj.addFormDataPart("image_file", "");
        }

        obj
                .addFormDataPart("name", name)
                .addFormDataPart("email", email_id)
                .addFormDataPart("device_id", device_id)
                .addFormDataPart("gcm_id", gcm_id)
                .addFormDataPart("latitude", latitude)
                .addFormDataPart("longitude", longitude)
                .addFormDataPart("date", date)
                .addFormDataPart("user_id", user_id);

        RequestBody formBody = obj
                .build();
        return post_server_call(url_report_garbage, formBody);
    }

    public static boolean compar_lat_long_withDB(double lat, double log, double ex_lat, double ex_log) {

        //calculate distance between given two co-ordinates

        Location mylocation = new Location("");
        Location dest_location = new Location("");
        if (ex_lat > 0 && lat > 0) {
            dest_location.setLatitude(lat);
            dest_location.setLongitude(log);
            Double my_loc = 0.00;
            mylocation.setLatitude(ex_lat);
            mylocation.setLongitude(ex_log);
            double distance = mylocation.distanceTo(dest_location) / 1000; //in meters
            MyApp.log(TAG, "distance: direct" + distance);
            distance = (double) Math.round(distance * 10) / 10;
            MyApp.log(TAG, "distance: round " + distance);
            distance = distance * 1000;
            MyApp.log(TAG, "distance in meters:" + distance);
            MyApp.log(TAG, "Distance: " + distance + " FromLat:" + lat + " FromLon:" + log + " ToLat:" + ex_lat + " ToLon:" + ex_log);
            if (distance <= Double.parseDouble("1000")) {
                MyApp.log(TAG, " in if distance:" + distance);
                return true;
            } else {
                MyApp.log(TAG, " in else distance:" + distance);
                return false;
            }
        } else {
            //return 0.0;
            return false;
        }
    }

    public static String get_mobile_verification(String mobile) {
        RequestBody formBody = new FormEncodingBuilder()
                .add("mobile", mobile)
                .build();
        MyApp.log("Form body to get mobile otp is -- " + formBody.toString());
        return post_server_call(url_verify_mobile, formBody);
    }

    public static String send_feedback(String email, String message) {
        RequestBody formBody = new FormEncodingBuilder()
                .add("email", email)
                .add("message", message)
                .build();
        MyApp.log("Form body to send feedback is -- " + formBody.toString());
        return post_server_call(url_contact_us, formBody);
    }

    public static String get_play_store_version() {
        RequestBody formBody = new FormEncodingBuilder().build();
        return post_server_call(url_version_check, formBody);
    }

    public static String get_near_trucks(String latitude, String longitude) {
        RequestBody formBody = new FormEncodingBuilder()
                .add("latitude", latitude)
                .add("longitude", longitude)
                .build();
        return post_server_call(url_get_near_truck_updates, formBody);
    }

    public static String get_google_address(double latitude, double longitude) {

        long REQ_TIMEOUT = 600;
        String url = "http://maps.googleapis.com/maps/api/geocode/json?latlng=" + latitude + "," + longitude + "&sensor=true";
        log("post_server_callUrl:" + url);


        try {
            OkHttpClient client = new OkHttpClient();
            client.setConnectTimeout(REQ_TIMEOUT, TimeUnit.SECONDS);
            client.setReadTimeout(REQ_TIMEOUT, TimeUnit.SECONDS);
            client.setWriteTimeout(REQ_TIMEOUT, TimeUnit.SECONDS);
            Request request = new Request.Builder()
                    .url(url)
                    .build();
            Response response = client.newCall(request).execute();

            int status_code = response.code();
            boolean status = response.isSuccessful();
            log("logtime:StatusCode:" + status_code);
            log("logtime:ResponseStatus:" + status);
            String res = response.body().string();
            log("logtime:ResponseSize:" + res.length());
            log("logtime:Response:" + res);
            log("post_server_callResponseStatus:" + response.isSuccessful());
            return res;
        } catch (Exception e) {

            log("logtime:E1->" + e + "");
            log("logtime:E2->" + e.getMessage());
            log("logtime:E3->" + e.getLocalizedMessage());
            return "-0";
        }
    }
}
