package com.change22.myapcc.config;

import android.app.IntentService;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteStatement;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.text.TextUtils;

import com.change22.myapcc.R;
import com.change22.myapcc.activities.Activity_dashboard;
import com.change22.myapcc.database.TABLE_BINS;
import com.change22.myapcc.database.TABLE_GARBAGE;
import com.change22.myapcc.database.TABLE_LOCATIONS;
import com.change22.myapcc.database.TABLE_TRUCK;
import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.squareup.okhttp.FormEncodingBuilder;
import com.squareup.okhttp.RequestBody;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;

import androidx.annotation.NonNull;
import androidx.core.app.JobIntentService;
import androidx.core.app.NotificationCompat;

/**
 * Created by Ganesh Borse on 04/08/2016.
 */

public class AppService extends IntentService {   // extends JobIntentService

    private static final String CHANNEL_ID = "complaints";

    public AppService() {
        super("AppService");
    }

    Context context = null;
    public static int NOTIFICATION_ID = 1;
    public static String LOG_TAG = "AppService";


   /* @Override
    protected void onHandleWork(@NonNull Intent intent) {
        onHandleIntent(intent);
    }*/

    @Override
    protected void onHandleIntent(Intent intent) {
        context = this;
        Bundle extras = intent.getExtras();
        register_gcm_to_server();

        if (extras != null) {
            if (!extras.isEmpty()) {
                MyApp.log("TreeServices_onHandleIntent");
                MyApp.log("Bundle->" + extras);
                GoogleCloudMessaging gcm = GoogleCloudMessaging.getInstance(this);
                String messageType = gcm.getMessageType(intent);
                MyApp.log("MessageType->" + messageType);

                for (String key : extras.keySet()) {
                    MyApp.log("Bundle Debug" + key + " = \"" + extras.get(key) + "\"");
                }

                if (GoogleCloudMessaging.MESSAGE_TYPE_MESSAGE.equals(messageType)) {
                    //sendNotification("Received: " + extras.toString());
                    appProcessNotification(extras);
                } else {
                    appProcess(extras);
                }

                AppBroadcastReceiver.completeWakefulIntent(intent);
            }
        }
    }

    private void appProcess(Bundle extras) {
        if (extras.containsKey("Flag")) {
            if (extras.get("Flag").equals("JSON")) {
                insertJsonFiles();
            } else if (extras.get("Flag").equals("getAllData")) {
                getAllData();
            } else if (extras.get("Flag").equals("getGarbageData")) {
                getGarbageData();
            } else if (extras.get("Flag").equals("getTruckData")) {
                getTruckData();
            } else if (extras.get("Flag").equals("register_gcm")) {
                register_gcm_to_server();
            } else if (extras.get("Flag").equals("near_trucks")) {
                String latitude = extras.getString("latitude");
                String longitude = extras.getString("longitude");
                get_updated_near_trucks(latitude, longitude);
            } else if (extras.get("Flag").equals("google_address")) {
                get_google_address();
            }
        }
    }

    private void get_google_address() {
        double latitude = Double.parseDouble(MyApp.get_session(MyApp.SESSION_USER_LATITUDE));
        double longitude = Double.parseDouble(MyApp.get_session(MyApp.SESSION_USER_LONGITUDE));
        String response = MyApp.get_google_address(latitude, longitude);
        MyApp.log(LOG_TAG, "Get google address api response is " + response);

        if (!response.equalsIgnoreCase("-0")) {
            try {
                JSONObject jsonObject = new JSONObject(response);
                if (jsonObject.has("results")) {
                    JSONArray jsonArray = jsonObject.getJSONArray("results");
                    if (jsonArray != null && jsonArray.length() > 1) {
                        JSONObject jsonObject1 = jsonArray.getJSONObject(1);
                        if (jsonObject1 != null) {
                            if (jsonObject1.has("formatted_address")) {
                                String formatted_address = jsonObject1.getString("formatted_address");
                                if (!TextUtils.isEmpty(formatted_address)) {
                                    MyApp.set_session(MyApp.SESSION_USER_AREA, formatted_address);

                                    String[] array_address = formatted_address.split(",");
                                    if (array_address != null && array_address.length > 1) {
                                        String city = array_address[array_address.length - 1];
                                        if (!TextUtils.isEmpty(city)) {
                                            MyApp.set_session(MyApp.SESSION_GPS_CITY, city);
                                        }
                                    }

                                    updateMyActivity(context, "3", "location", "Activity_map");
                                }
                            }
                        }
                    }
                }
            } catch (JSONException e) {
                MyApp.log(LOG_TAG, "Get google address parse exception is " + e.getMessage());
            }
        }
    }

    private void register_gcm_to_server() {
        MyApp.log("register_gcm_to_server");

        RequestBody formBody = new FormEncodingBuilder()
                .add("name", "test")
                .add("email", "test@test.com")
                .add("device_id", MyApp.get_device_id())
                .add("gcm_id", MyApp.get_session(MyApp.PREFS_PROPERTY_REG_ID))
                .build();
        MyApp.log("DeviceID-- " + MyApp.get_device_id());
        MyApp.log("gcm_id-- " + MyApp.get_session(MyApp.PREFS_PROPERTY_REG_ID));
        String response = MyApp.post_server_call(MyApp.url_register_user, formBody);
        if (!response.equalsIgnoreCase("-0")) {
            try {
                JSONObject jsonObject = new JSONObject(response);
                if (jsonObject.has("status")) {
                    boolean status = jsonObject.getBoolean("status");
                    if (status) {
                        String user_id = jsonObject.getString("user_id");
                        String message = jsonObject.getString("message");
                        MyApp.set_session(MyApp.SESSION_IS_GCM_REGISTRED_TO_SERVER, "Y");
                        MyApp.log("User id registered is " + user_id);
                        MyApp.set_session(MyApp.SESSION_USER_ID, user_id);
                    }
                }
                MyApp.log(LOG_TAG, "End");
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    private void createNotificationChannel() {
        MyApp.log("In createNotificationChannel");
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = getString(R.string.channel_name);
            String description = getString(R.string.channel_description);
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    private void appProcessNotification(Bundle bundle) {
        MyApp.log("appProcessNotification");
        createNotificationChannel();
        MyApp.log_bundle(bundle);

        if (bundle != null) {
            if (bundle.containsKey("notificatin_flag")) {

                String str_notification_flag = bundle.getString("notificatin_flag");

                //if (MyApp.get_session(MyApp.SESSION_IS_REGISTERED).equalsIgnoreCase("Y")) {
                if (str_notification_flag.equalsIgnoreCase("truck_location")) {
                    String truck_id = bundle.getString("truck_id");
                    String truck_no = bundle.getString("truck_no");
                    String lattitude = bundle.getString("lat");
                    String longitude = bundle.getString("long");
                    Bundle extras = new Bundle();
                    //put whatever data you want to send, if any
                    extras.putString("Flag", "truck_location");
                    extras.putString("truck_id", truck_id);
                    extras.putString("truck_no", truck_no);
                    extras.putString("lattitude", lattitude);
                    extras.putString("longitude", longitude);
                    TABLE_TRUCK.update_truck_list(truck_id, truck_no, lattitude, longitude);
                    updateMyActivity(context, "1", "", "Activity_map1", extras);
                } else if (str_notification_flag.equalsIgnoreCase("complaint_resolved")) {
                    String complaints_id = bundle.getString("complaint_id");
                    MyApp.log(LOG_TAG, "Complaint ids that are resolved are " + complaints_id);

                    Bundle extras = new Bundle();
                    //put whatever data you want to send, if any
                    extras.putString("Flag", "complaint_resolved");
                    extras.putString("complaint_id", complaints_id);

                    if (!complaints_id.equalsIgnoreCase("")) {
                        TABLE_GARBAGE.change_status(complaints_id);

                        boolean flag = TABLE_GARBAGE.check_user_complaint(complaints_id);
                        if (flag) {
                            Bundle extras1 = new Bundle();
                            extras1.putString("title", getResources().getString(R.string.app_name));
                            String name = MyApp.get_session(MyApp.SESSION_USER_NAME);
                            extras1.putString("sub_title", "Hi " + name + ", we cleared your reported garbage.");
                            generateNotification(extras1, "");
                        }
                        updateMyActivity(context, "1", "", "Activity_issue", extras);
                    }
                } else if (str_notification_flag.equalsIgnoreCase("new_complaint")) {
                    String response = bundle.getString("complaint");
                    if (!response.equalsIgnoreCase("")) {
                        add_new_complaint(response);
                        Bundle extras = new Bundle();
                        //put whatever data you want to send, if any
                        extras.putString("Flag", "complaint_resolved");
                        updateMyActivity(context, "1", "", "Activity_issue", extras);
                    }
                } else if (str_notification_flag.equalsIgnoreCase("near_trucks")) {
                    String latitude = "", longitude = "";
                    if (MyApp.SESSION_FROM_SEARCH.equalsIgnoreCase("Y")) {
                        latitude = MyApp.get_session(MyApp.SESSION_SEARCH_LATITUDE);
                        longitude = MyApp.get_session(MyApp.SESSION_SEARCH_LONGITUDE);
                    } else {
                        latitude = MyApp.get_session(MyApp.SESSION_USER_LATITUDE);
                        longitude = MyApp.get_session(MyApp.SESSION_USER_LONGITUDE);
                    }
                    get_updated_near_trucks(latitude, longitude);
                } else if (str_notification_flag.equalsIgnoreCase("Complaint_Rejected")) {
                    String complaint_id = bundle.getString("complaint");
                    TABLE_GARBAGE.delete_complaint(complaint_id);
                    String sub_title = bundle.getString("complaint_message");
                    Bundle extras1 = new Bundle();
                    extras1.putString("title", getResources().getString(R.string.app_name));
                    //String name = MyApp.get_session(MyApp.SESSION_USER_NAME);
                    extras1.putString("sub_title", sub_title);
                    generateNotification(extras1, "");
                }
                //}
            }
        }

    }

    private void add_new_complaint(String response) {
        try {
            JSONObject jsonObject = new JSONObject(response);
            String area = jsonObject.getString("area");
            String pin_code = jsonObject.getString("pincode");
            String issue_no = jsonObject.getString("issue_no");
            String city = jsonObject.getString("city");
            String latitude = jsonObject.getString("lattitude");
            String longitude = jsonObject.getString("longitude");
            String locality = jsonObject.getString("locality");
            String complaint_type = jsonObject.getString("complaint_type");
            String complaint_by = jsonObject.getString("complaint_by");
            String complaint_date = jsonObject.getString("complaint_date");
            String complaint_image = jsonObject.getString("compalint_image");
            String last_update = jsonObject.getString("last_update");
            String reported_by = jsonObject.getString("reported_by");
            String id = jsonObject.getString("id");
            String user_id = jsonObject.getString("user_id");
            String state = jsonObject.getString("state");
            String status = jsonObject.getString("status");

            TABLE_GARBAGE.insert_garbage_list(id, user_id, issue_no, complaint_type, reported_by, complaint_image,
                    area, locality, city, state, pin_code, latitude, longitude, complaint_date, status);
        } catch (JSONException e) {
            e.printStackTrace();
            MyApp.log(LOG_TAG, "JSON Exception is " + e.getMessage());
        }
    }

    private void generateNotification(Bundle extras, String sub_title) {

        String title = "";
        String subtitle = "";
        Intent intent;
        if (extras.containsKey("title")) {
            title = extras.getString("title");
        }
        if (extras.containsKey("sub_title")) {
            subtitle = extras.getString("sub_title");
        }
        NotificationManager mNotificationManager = (NotificationManager) context
                .getSystemService(Context.NOTIFICATION_SERVICE);

        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(context, CHANNEL_ID)
                        .setContentTitle(title).setAutoCancel(true)
                        .setSound(Settings.System.DEFAULT_NOTIFICATION_URI);

        //Call intent to open specific activity
        intent = new Intent(context, Activity_dashboard.class).setFlags(Intent.FLAG_ACTIVITY_PREVIOUS_IS_TOP);

        Bitmap largeIcon = BitmapFactory.decodeResource(getResources(), R.drawable.ic_launcher);
        mBuilder.setLargeIcon(largeIcon);

        mBuilder.setTicker(subtitle);

        androidx.core.app.NotificationCompat.BigTextStyle bigText = new androidx.core.app.NotificationCompat.BigTextStyle();
        bigText.bigText(subtitle);
        mBuilder.setStyle(bigText);

        mBuilder.setContentText(subtitle);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
            mBuilder.setColor(Color.RED);
            mBuilder.setSmallIcon(R.drawable.ic_notification);
        } else
            mBuilder.setSmallIcon(R.drawable.ic_launcher);
        //mBuilder.setPriority(Notification.PRIORITY_MAX);
        NOTIFICATION_ID = MyApp.get_Intsession(MyApp.SESSION_COMPLAINT_NOTIFICATION);
        int session_id = NOTIFICATION_ID + 1;
        MyApp.set_session(MyApp.SESSION_COMPLAINT_NOTIFICATION, session_id + "");
        PendingIntent contentIntent = PendingIntent.getActivity(context, NOTIFICATION_ID, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        mBuilder.setContentIntent(contentIntent);
        mNotificationManager.notify(NOTIFICATION_ID, mBuilder.build());
    }

    static void updateMyActivity(Context context, String response_code, String response_message, String flag) {

        Intent intent = new Intent(flag);
        //put whatever data you want to send, if any
        intent.putExtra("response_code", response_code);
        intent.putExtra("response_message", response_message);
        //send broadcast
        context.sendBroadcast(intent);

    }

    static void updateMyActivity(Context context, String response_code, String response_message, String flag, Bundle extras) {
        MyApp.log("updateMyActivity");
        MyApp.log_bundle(extras);
        Intent intent = new Intent(flag);
        intent.putExtras(extras);
        //send broadcast
        context.sendBroadcast(intent);

    }

    private void getTruckData() {
        String truck_last_update = MyApp.get_session(MyApp.SESSION_TRUCK_LAST_UPDATE);
        // String garbage_last_update = MyApp.get_session(MyApp.SESSION_GARBAGE_LAST_UPDATE);

        if (truck_last_update.equalsIgnoreCase("")) {
            truck_last_update = "2016-08-01 00:00:00";
        }
        /*if(garbage_last_update.equalsIgnoreCase("")){
            garbage_last_update = "2016-08-01 00:00:00";
        }*/

      //  updateMyActivity(context, "2", "Fetching Truck current locations", "Activity_splash");
        MyApp.log(LOG_TAG, "Start");
        String response = MyApp.get_truck_list(truck_last_update);
        if (!response.equalsIgnoreCase("-0")) {
            try {
                JSONObject jsonObject = new JSONObject(response);
                if (jsonObject.has("status")) {
                    boolean status = jsonObject.getBoolean("status");
                    if (status) {
                        if (jsonObject.has("data")) {
                            JSONArray jsonArray = jsonObject.getJSONArray("data");
                            if (jsonArray.length() > 0) {
                                for (int i = 0; i < jsonArray.length(); i++) {
                                    JSONObject jsonObject1 = jsonArray.getJSONObject(i);
                                    String id = jsonObject1.getString("id");
                                    String truck_no = jsonObject1.getString("truck_no");
                                    String truck_type = jsonObject1.getString("truck_type");
                                    String truck_img = jsonObject1.getString("truck_img");
                                    String latitude = jsonObject1.getString("latitude");
                                    String longitude = jsonObject1.getString("longitude");
                                    String icon_img = jsonObject1.getString("icon_img");
                                    //String complaint_date = jsonObject1.getString("complaint_date");

                                    TABLE_TRUCK.insert_truck_list(id, truck_no, truck_type, truck_img, latitude, longitude, icon_img);
                                }
                            }
                        }
                    }
                }
                MyApp.set_session(MyApp.SESSION_TRUCK_LAST_UPDATE, MyApp.get_current_date());
                MyApp.log(LOG_TAG, "End");

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        // updateMyActivity(context, "2", "Fetching Reported issue list", "Activity_splash");
       /* String response1 = MyApp.get_garbage_list(truck_last_update);
        if (!response.equalsIgnoreCase("-0")){
            try {
                JSONObject jsonObject = new JSONObject(response1);
                if (jsonObject.has("status")){
                    boolean status = jsonObject.getBoolean("status");
                    if (status){
                        if (jsonObject.has("data")){
                            JSONArray jsonArray = jsonObject.getJSONArray("data");
                            MyApp.log(" jsonArray:"+jsonArray);
                            if (jsonArray.length()>0){
                                for (int i = 0; i< jsonArray.length(); i++){
                                    JSONObject jsonObject1 = jsonArray.getJSONObject(i);
                                    String id = jsonObject1.getString("id");
                                    String issue_no = jsonObject1.getString("issue_no");
                                    String complaint_type = jsonObject1.getString("complaint_type");
                                    String reported_by = jsonObject1.getString("reported_by");
                                    String complaint_image = jsonObject1.getString("compalint_image");
                                    String area = jsonObject1.getString("area");
                                    String locality = jsonObject1.getString("locality");
                                    String city = jsonObject1.getString("city");
                                    String state = jsonObject1.getString("state");
                                    String pincode = jsonObject1.getString("pincode");
                                    String latitude = jsonObject1.getString("lattitude");
                                    String longitude = jsonObject1.getString("longitude");
                                    String complaint_date = jsonObject1.getString("complaint_date");

                                    MyApp.log("in load_more_items() complaint_image:" + complaint_image
                                            + " complaint_date:" + complaint_date + " latitude:" + latitude
                                            + " longitude:" + longitude + " reported_by:" + reported_by+" id:"+id+" issue_no:"+issue_no+
                                    " complaint_type:"+complaint_type+" area:"+area+" locality:"+locality+" city:"+city+" state:"+state+" pincode:"+pincode
                                    );
                                    TABLE_GARBAGE.insert_garbage_list(id, issue_no, complaint_type, reported_by, complaint_image, area, locality, city, state, pincode, latitude, longitude, complaint_date);
                                }
                            }
                        }
                    }
                }
                MyApp.set_session(MyApp.SESSION_GARBAGE_LAST_UPDATE,MyApp.get_current_date());
            } catch (JSONException e) {
                e.printStackTrace();
            }

        }*/
        Bundle bundle = new Bundle();
        //put whatever data you want to send, if any
        bundle.putString("Flag", "getTruckData");
        updateMyActivity(context, "1", "", "Activity_map1", bundle);

    }


    private void getAllData() {
        String truck_last_update = "2016-08-01 00:00:00";//MyApp.get_session(MyApp.SESSION_TRUCK_LAST_UPDATE);
        // String garbage_last_update = MyApp.get_session(MyApp.SESSION_GARBAGE_LAST_UPDATE);

        /*if(truck_last_update.equalsIgnoreCase("")){
           truck_last_update = "2016-08-01 00:00:00";
        }*/
        /*if(garbage_last_update.equalsIgnoreCase("")){
            garbage_last_update = "2016-08-01 00:00:00";
        }*/

        TABLE_TRUCK.delete_all_records();

      //  updateMyActivity(context, "2", "Fetching Truck current locations", "Activity_splash");
          updateMyActivity(context, "1", "GetAll", "Activity_splash");

        MyApp.log(LOG_TAG, "Start");
        String response = MyApp.get_truck_list(truck_last_update);
        if (!response.equalsIgnoreCase("-0")) {
            try {
                JSONObject jsonObject = new JSONObject(response);
                if (jsonObject.has("status")) {
                    boolean status = jsonObject.getBoolean("status");
                    if (status) {
                        if (jsonObject.has("data")) {
                            JSONArray jsonArray = jsonObject.getJSONArray("data");
                            if (jsonArray.length() > 0) {
                                for (int i = 0; i < jsonArray.length(); i++) {
                                    JSONObject jsonObject1 = jsonArray.getJSONObject(i);
                                    String id = jsonObject1.getString("id");
                                    String truck_no = jsonObject1.getString("truck_no");
                                    String truck_type = jsonObject1.getString("truck_type");
                                    String truck_img = jsonObject1.getString("truck_img");
                                    String latitude = jsonObject1.getString("latitude");
                                    String longitude = jsonObject1.getString("longitude");
                                    String icon_img = jsonObject1.getString("icon_img");
                                    //String complaint_date = jsonObject1.getString("complaint_date");

                                    TABLE_TRUCK.insert_truck_list(id, truck_no, truck_type, truck_img, latitude, longitude, icon_img);
                                }
                            }
                        }
                    }
                }
                MyApp.set_session(MyApp.SESSION_TRUCK_LAST_UPDATE, MyApp.get_current_date());
                MyApp.log(LOG_TAG, "End");

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        // updateMyActivity(context, "2", "Fetching Reported issue list", "Activity_splash");
       /* String response1 = MyApp.get_garbage_list(truck_last_update);
        if (!response.equalsIgnoreCase("-0")){
            try {
                JSONObject jsonObject = new JSONObject(response1);
                if (jsonObject.has("status")){
                    boolean status = jsonObject.getBoolean("status");
                    if (status){
                        if (jsonObject.has("data")){
                            JSONArray jsonArray = jsonObject.getJSONArray("data");
                            MyApp.log(" jsonArray:"+jsonArray);
                            if (jsonArray.length()>0){
                                for (int i = 0; i< jsonArray.length(); i++){
                                    JSONObject jsonObject1 = jsonArray.getJSONObject(i);
                                    String id = jsonObject1.getString("id");
                                    String issue_no = jsonObject1.getString("issue_no");
                                    String complaint_type = jsonObject1.getString("complaint_type");
                                    String reported_by = jsonObject1.getString("reported_by");
                                    String complaint_image = jsonObject1.getString("compalint_image");
                                    String area = jsonObject1.getString("area");
                                    String locality = jsonObject1.getString("locality");
                                    String city = jsonObject1.getString("city");
                                    String state = jsonObject1.getString("state");
                                    String pincode = jsonObject1.getString("pincode");
                                    String latitude = jsonObject1.getString("lattitude");
                                    String longitude = jsonObject1.getString("longitude");
                                    String complaint_date = jsonObject1.getString("complaint_date");

                                    MyApp.log("in load_more_items() complaint_image:" + complaint_image
                                            + " complaint_date:" + complaint_date + " latitude:" + latitude
                                            + " longitude:" + longitude + " reported_by:" + reported_by+" id:"+id+" issue_no:"+issue_no+
                                    " complaint_type:"+complaint_type+" area:"+area+" locality:"+locality+" city:"+city+" state:"+state+" pincode:"+pincode
                                    );
                                    TABLE_GARBAGE.insert_garbage_list(id, issue_no, complaint_type, reported_by, complaint_image, area, locality, city, state, pincode, latitude, longitude, complaint_date);
                                }
                            }
                        }
                    }
                }
                MyApp.set_session(MyApp.SESSION_GARBAGE_LAST_UPDATE,MyApp.get_current_date());
            } catch (JSONException e) {
                e.printStackTrace();
            }

        }*/
        updateMyActivity(context, "1", "", "Activity_splash");
        getBinList();
        getGarbageData();
        getAreaList();
    }


    private void getGarbageData() {

        String user_id1 = MyApp.get_session(MyApp.SESSION_USER_ID);
        TABLE_GARBAGE.delete_all_records(user_id1);

        String garbage_last_update = MyApp.get_session(MyApp.SESSION_GARBAGE_LAST_UPDATE);

        if (garbage_last_update.equalsIgnoreCase("")) {
            garbage_last_update = "2016-08-01 00:00:00";
        }

        //updateMyActivity(context, "2", "Fetching Reported issue list", "Activity_splash");
        if (TextUtils.isEmpty(user_id1)) {
            user_id1 = "xxx";
        }
        MyApp.log(LOG_TAG, "User id is " + user_id1);
        String response1 = MyApp.get_garbage_list(garbage_last_update, user_id1);
        if (!response1.equalsIgnoreCase("-0")) {
            try {
                JSONObject jsonObject = new JSONObject(response1);
                if (jsonObject.has("status")) {
                    boolean status = jsonObject.getBoolean("status");
                    if (status) {
                        if (jsonObject.has("data")) {
                            JSONArray jsonArray = jsonObject.getJSONArray("data");
                            //MyApp.log(" jsonArray:"+jsonArray);
                            if (jsonArray.length() > 0) {
                                for (int i = 0; i < jsonArray.length(); i++) {
                                    JSONObject jsonObject1 = jsonArray.getJSONObject(i);
                                    String id = jsonObject1.getString("id");
                                    String user_id = jsonObject1.getString("user_id");
                                    String issue_no = jsonObject1.getString("issue_no");
                                    String complaint_type = jsonObject1.getString("complaint_type");
                                    String reported_by = jsonObject1.getString("reported_by");
                                    String complaint_image = jsonObject1.getString("compalint_image");
                                    String area = jsonObject1.getString("area");
                                    String locality = jsonObject1.getString("locality");
                                    String city = jsonObject1.getString("city");
                                    String state = jsonObject1.getString("state");
                                    String pincode = jsonObject1.getString("pincode");
                                    String latitude = jsonObject1.getString("lattitude");
                                    String longitude = jsonObject1.getString("longitude");
                                    String complaint_date = jsonObject1.getString("complaint_date");
                                    String issue_status = jsonObject1.getString("status");

                                    MyApp.log("in load_more_items() complaint_image:" + complaint_image
                                            + " complaint_date:" + complaint_date + " latitude:" + latitude
                                            + " longitude:" + longitude + " reported_by:" + reported_by + " id:" + id + " issue_no:" + issue_no +
                                            " complaint_type:" + complaint_type + " area:" + area + " locality:" + locality + " city:" + city + " state:" + state + " pincode:" + pincode
                                            + "issue_status:" + issue_status);
                                    TABLE_GARBAGE.insert_garbage_list(id, user_id, issue_no, complaint_type, reported_by, complaint_image, area, locality, city,
                                            state, pincode, latitude, longitude, complaint_date, issue_status);
                                }
                            }
                        }
                    }
                }
                MyApp.set_session(MyApp.SESSION_GARBAGE_LAST_UPDATE, MyApp.get_current_date());
            } catch (JSONException e) {
                e.printStackTrace();
            }

        }
        updateMyActivity(context, "1", "", "Activity_Dashboard");
    }

    private void getBinList() {

        String bin_last_update = "2016-08-01 00:00:00";//MyApp.get_session(MyApp.SESSION_BIN_LAST_UPDATE);

        /*if(bin_last_update.equalsIgnoreCase("")){
            bin_last_update = "2016-08-01 00:00:00";
        }*/

        TABLE_BINS.delete_all_records();

        //updateMyActivity(context, "2", "Fetching Reported issue list", "Activity_splash");
        String response1 = MyApp.get_bin_list(bin_last_update);
        if (!response1.equalsIgnoreCase("-0")) {
            try {
                JSONObject jsonObject = new JSONObject(response1);
                if (jsonObject.has("status")) {
                    boolean status = jsonObject.getBoolean("status");
                    if (status) {
                        if (jsonObject.has("bin_list")) {
                            JSONArray jsonArray = jsonObject.getJSONArray("bin_list");
                            //MyApp.log(" jsonArray:"+jsonArray);
                            if (jsonArray.length() > 0) {
                                for (int i = 0; i < jsonArray.length(); i++) {
                                    JSONObject jsonObject1 = jsonArray.getJSONObject(i);
                                    String id = jsonObject1.getString("id");
                                    String latitude = jsonObject1.getString("latitude");
                                    String longitude = jsonObject1.getString("Longitude");
                                    String area = jsonObject1.getString("area");
                                    String locality = jsonObject1.getString("locality");
                                    String city = jsonObject1.getString("city");
                                    String state = jsonObject1.getString("state");
                                    String bin_image = jsonObject1.getString("bin_image");
                                    String icon_image = jsonObject1.getString("icon_image");
                                    String last_update = jsonObject1.getString("last_update");

                                    TABLE_BINS.insert_bin_list(id, latitude, longitude, area, locality, city, state, bin_image, icon_image, last_update);
                                }
                            }
                        }
                    }
                }
                MyApp.set_session(MyApp.SESSION_BIN_LAST_UPDATE, MyApp.get_current_date());
            } catch (JSONException e) {
                e.printStackTrace();
            }

        }
        //updateMyActivity(context, "1", "", "Activity_Dashboard");
    }

    private void getAreaList() {

        String area_last_update = MyApp.get_session(MyApp.SESSION_AREA_LAST_UPDATE);

        if (area_last_update.equalsIgnoreCase("")) {
            area_last_update = "2016-08-01 00:00:00";
        }

        //area_last_update = "2016-08-01 00:00:00";

        //updateMyActivity(context, "2", "Fetching Reported issue list", "Activity_splash");
        String response1 = MyApp.get_area_list(area_last_update);
        if (!response1.equalsIgnoreCase("-0")) {
            try {
                JSONObject jsonObject = new JSONObject(response1);
                if (jsonObject.has("status")) {
                    boolean status = jsonObject.getBoolean("status");
                    if (status) {
                        if (jsonObject.has("area_list")) {
                            JSONArray jsonArray = jsonObject.getJSONArray("area_list");
                            //MyApp.log(" jsonArray:"+jsonArray);
                            if (jsonArray.length() > 0) {
                                TABLE_LOCATIONS.delete_all_locations();
                                MyApp.set_session(MyApp.SESSION_FETCHING_LOCATIONS, "Y");
                                for (int i = 0; i < jsonArray.length(); i++) {
                                    JSONObject jsonObject1 = jsonArray.getJSONObject(i);
                                    String id = jsonObject1.getString("id");
                                    String address = jsonObject1.getString("address");
                                    String latitude = jsonObject1.getString("latitude");
                                    String longitude = jsonObject1.getString("longitude");
                                    String lat_long = jsonObject1.getString("lat_long");
                                    String last_update = jsonObject1.getString("last_update");

                                    TABLE_LOCATIONS.insert_area_list(id, address, latitude, longitude, lat_long, last_update);
                                }
                                MyApp.set_session(MyApp.SESSION_FETCHING_LOCATIONS, "N");
                            }
                        }
                    }
                }
                MyApp.set_session(MyApp.SESSION_AREA_LAST_UPDATE, MyApp.get_current_date());
            } catch (JSONException e) {
                e.printStackTrace();
            }

        }
        //updateMyActivity(context, "1", "", "Activity_Dashboard");
    }

    public void insertJsonFiles() {
        /*try {
            SQLiteDatabase db = MyApp.db.getWritableDatabase();
            MyApp.log(LOG_TAG, "In insertJsonFiles");
            String insertSQL = "INSERT INTO " + TABLE_LOCATIONS.NAME + "("
                    + TABLE_LOCATIONS.COL_ID + ", "
                    + TABLE_LOCATIONS.COL_LOCATION_NAME + ", "
                    + TABLE_LOCATIONS.COL_LOCATION_LATITUDE + ", "
                    + TABLE_LOCATIONS.COL_LOCATION_LONGITUDE + ", "
                    + TABLE_LOCATIONS.COL_LOCATION_LAT_LOG + ") VALUES(?,?,?,?,?)";

            JSONObject obj = new JSONObject(loadJSONFromAsset("areas.json"));
            JSONArray m_jArry = obj.getJSONArray("area_list");
            db.beginTransaction();
            SQLiteStatement statement = db.compileStatement(insertSQL);
            for (int i = 0; i < m_jArry.length(); i++) {
                JSONObject listObject1 = m_jArry.getJSONObject(i);

                String id = listObject1.getString("id");
                String area = listObject1.getString("address");
                String latitude = listObject1.getString("latitude");
                String longitude = listObject1.getString("longitude");
                String lat_long = listObject1.getString("lat_long");

                statement.clearBindings();
                statement.bindString(1, id);
                statement.bindString(2, area);
                statement.bindString(3, latitude);
                statement.bindString(4, longitude);
                statement.bindString(5, lat_long);

                statement.execute();
            }
            db.setTransactionSuccessful();
            db.endTransaction();
            statement.close();
            MyApp.log(LOG_TAG, "Data inserted successfully for location table");
        } catch (JSONException e) {
            e.printStackTrace();
            MyApp.log(LOG_TAG, "Hardcode exception is " + e.getMessage());
        }*/

        try {
            SQLiteDatabase db = MyApp.db.getWritableDatabase();
            MyApp.log(LOG_TAG, "In insertJsonFiles");
            String insertSQL = "INSERT INTO " + TABLE_BINS.NAME + "("
                    + TABLE_BINS.COL_ID + ", "
                    + TABLE_BINS.COL_LATITUDE + ", "
                    + TABLE_BINS.COL_LONGITUDE + ", "
                    + TABLE_BINS.COL_AREA + ", "
                    + TABLE_BINS.COL_LOCALITY + ", "
                    + TABLE_BINS.COL_CITY + ", "
                    + TABLE_BINS.COL_STATE + ", "
                    + TABLE_BINS.COL_IMG_URL + ", "
                    + TABLE_BINS.COL_ICON_IMG + ") VALUES(?,?,?,?,?,?,?,?,?)";

            JSONObject obj = new JSONObject(loadJSONFromAsset("bins.json"));
            JSONArray m_jArry = obj.getJSONArray("bins_list");
            db.beginTransaction();
            SQLiteStatement statement = db.compileStatement(insertSQL);
            for (int i = 0; i < m_jArry.length(); i++) {
                JSONObject listObject1 = m_jArry.getJSONObject(i);

                String id = listObject1.getString("id");
                String latitude = listObject1.getString("latitude");
                String longitude = listObject1.getString("longitude");
                String area = listObject1.getString("area");
                String locality = listObject1.getString("locality");
                String city = listObject1.getString("city");
                String state = listObject1.getString("state");
                String bin_image = listObject1.getString("bin_image");
                String icon_image = listObject1.getString("icon_image");

                statement.clearBindings();
                statement.bindString(1, id);
                statement.bindString(2, latitude);
                statement.bindString(3, longitude);
                statement.bindString(4, area);
                statement.bindString(5, locality);
                statement.bindString(6, city);
                statement.bindString(7, state);
                statement.bindString(8, bin_image);
                statement.bindString(9, icon_image);

                statement.execute();
            }
            db.setTransactionSuccessful();
            db.endTransaction();
            statement.close();
            MyApp.log(LOG_TAG, "Data inserted successfully for bins table");
        } catch (JSONException e) {
            e.printStackTrace();
            MyApp.log(LOG_TAG, "Hardcode exception is " + e.getMessage());
        } catch (SQLiteException e) {
            e.printStackTrace();
            MyApp.log(LOG_TAG, "SQLiteException is " + e.getMessage());
        }
    }

    public String loadJSONFromAsset(String json1) {
        String json = null;
        try {
            InputStream is = getApplicationContext().getAssets().open(json1);
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            json = new String(buffer, "UTF-8");
        } catch (IOException ex) {
            ex.printStackTrace();
            return null;
        }
        return json;
    }

    public void get_updated_near_trucks(String latitude, String longitude) {
        String response = MyApp.get_near_trucks(latitude, longitude);
        if (!response.equalsIgnoreCase("-0")) {
            try {
                JSONObject jsonObject = new JSONObject(response);
                if (jsonObject.has("status")) {
                    boolean status = jsonObject.getBoolean("status");
                    if (status) {
                        if (jsonObject.has("near_trucks")) {
                            JSONArray jsonArray = jsonObject.getJSONArray("near_trucks");
                            if (jsonArray.length() > 0) {
                                for (int i = 0; i < jsonArray.length(); i++) {
                                    JSONObject jsonObject1 = jsonArray.getJSONObject(i);
                                    String truck_id = jsonObject1.getString("id");
                                    String truck_no = jsonObject1.getString("truck_no");
                                    String str_latitude = jsonObject1.getString("latitude");
                                    String str_longitude = jsonObject1.getString("longitude");
                                    Bundle extras = new Bundle();
                                    //put whatever data you want to send, if any
                                    extras.putString("Flag", "truck_location");
                                    extras.putString("truck_id", truck_id);
                                    extras.putString("truck_no", truck_no);
                                    extras.putString("lattitude", str_latitude);
                                    extras.putString("longitude", str_longitude);
                                    TABLE_TRUCK.update_truck_list(truck_id, truck_no, str_latitude, str_longitude);
                                    updateMyActivity(context, "1", "", "Activity_map1", extras);
                                }
                            }
                        }
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

    }
}
