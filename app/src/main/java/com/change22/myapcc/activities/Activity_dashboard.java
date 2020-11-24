package com.change22.myapcc.activities;

import android.Manifest;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.app.TabActivity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.provider.Settings;

import com.change22.myapcc.BuildConfig;
import com.google.android.material.snackbar.Snackbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import android.text.SpannableString;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TabHost;
import android.widget.TextView;

import com.change22.myapcc.R;
import com.change22.myapcc.config.AppService;
import com.change22.myapcc.config.GPSTracker1;
import com.change22.myapcc.config.MyApp;
import com.change22.myapcc.database.TABLE_GARBAGE;
import com.change22.myapcc.database.TABLE_LOCATIONS;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;


public class Activity_dashboard extends TabActivity implements TabHost.OnTabChangeListener, ActivityCompat.OnRequestPermissionsResultCallback {

    private static final String LOG_TAG = "Activity_Dashboard";
    TabHost tabHost = null;
    Context context = null;
    ImageButton btn_setting = null;
    private int permission_count = 3;
    ImageView img_upload_new = null;


    public static boolean isPaused = true;


    /* FloatingActionMenu fab_menu = null;*/


    public static Dialog dialog1;
    GPSTracker1 new_gps = null;
    private ProgressDialog progressDialog = null;

    File fileImage = null;
    String str_path = "";
    int TAKE_IMAGE = 100, PICK_IMAGE = 200;

    public static String new_str_path = "";
    String str_pic_path = "";


    Handler hMin5 = new Handler();
    Handler hMin10 = new Handler();
    Handler hMin15 = new Handler();
    Handler hMin20 = new Handler();
    Runnable runMin5 = null;
    Runnable runMin10 = null;
    Runnable runMin15 = null;
    Runnable runMin20 = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
       // Thread.setDefaultUncaughtExceptionHandler(new ExceptionHandler(this));
        setContentView(R.layout.activity_dashboard);
        context = this;

        if (((MyApp) getApplicationContext()).getRegistrationId(context).length() == 0) {
            ((MyApp) getApplicationContext()).getRegistrationGCMID();
        } else {
            if (!MyApp.get_session(MyApp.SESSION_IS_GCM_REGISTRED_TO_SERVER).equalsIgnoreCase("Y")) {
                if (((MyApp) getApplication()).isNetworkConnectionAvailable()) {
                    Intent intent1 = new Intent(Intent.ACTION_SYNC, null, context, AppService.class);
                    intent1.putExtra("Flag", "register_gcm");
                    startService(intent1);
                }
            }
        }

        initComponents();
        initComponentListener();

        /*if (TABLE_LOCATIONS.getCount()) {
            //insert hardcode fellowship by json
            MyApp.log(LOG_TAG, "start insert hardcode data service");
            Intent intent1 = new Intent(Intent.ACTION_SYNC, null, context, AppService.class);
            intent1.putExtra("Flag", "JSON");
            startService(intent1);
        }*/

        new_gps = new GPSTracker1(context);

        /*if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (getPermissionCount() > 0)
                check_app_persmission();
            else {
                if (MyApp.get_session(MyApp.SESSION_FROM_SEARCH).equalsIgnoreCase("Y")) {
                    if (new_gps.canGetLocation())
                        new_gps.getLocation();
                    else
                        new_gps.showSettingsAlert(context);
                }
            }
        } else {
            if (MyApp.get_session(MyApp.SESSION_FROM_SEARCH).equalsIgnoreCase("Y")) {
                if (new_gps.canGetLocation())
                    new_gps.getLocation();
                else
                    new_gps.showSettingsAlert(context);
            }
        }*/

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (getPermissionCount() > 0)
                check_app_persmission();
            else {
                if (MyApp.get_session(MyApp.SESSION_FROM_SEARCH).equalsIgnoreCase("Y")) {
                    if (new_gps.canGetLocation())
                        new_gps.getLocation();
                    else {
                       /* if (!MyApp.get_session(MyApp.SESSION_IS_GPS_ENABLED).equalsIgnoreCase("Y"))
                            new Activity_map().showSettingsAlert(context);
                        else{
                            if (Activity_map.dialog1!=null)
                                if (Activity_map.dialog1.isShowing())
                                    Activity_map.dialog1.dismiss();
                        }*/
                    }
                }
            }
        } else {
            if (MyApp.get_session(MyApp.SESSION_FROM_SEARCH).equalsIgnoreCase("Y")) {
                if (new_gps.canGetLocation())
                    new_gps.getLocation();
                else {
                   /* if (!MyApp.get_session(MyApp.SESSION_IS_GPS_ENABLED).equalsIgnoreCase("Y"))
                        new Activity_map().showSettingsAlert(context);
                    else{
                        if (Activity_map.dialog1!=null)
                            if (Activity_map.dialog1.isShowing())
                                Activity_map.dialog1.dismiss();
                    }*/
                }
            }
        }


    }

    private void initComponents() {
        tabHost = (TabHost) findViewById(android.R.id.tabhost);
        btn_setting = (ImageButton) findViewById(R.id.btn_setting);

        //Creating tab menu.
        TabHost.TabSpec TabMenu1 = tabHost.newTabSpec("Dashboard");
        TabHost.TabSpec TabMenu2 = tabHost.newTabSpec("NearbyIssue");

        //Setting up tab 1 name.
        TabMenu1.setIndicator("Home", getResources().getDrawable(R.drawable.dashboard_selected));
        //Set tab 1 activity to tab 1 menu.
        TabMenu1.setContent(new Intent(this, Activity_map.class));

        //Setting up tab 2 name.
        TabMenu2.setIndicator("Reported Garbage", getResources().getDrawable(R.drawable.search_default));
        //Set tab 2 activity to tab 1 menu.
        TabMenu2.setContent(new Intent(this, Activity_issues.class));

        //Adding tab1, tab2, tab3 to tabhost view.

        tabHost.addTab(TabMenu1);
        tabHost.addTab(TabMenu2);


        for (int i = 0; i < tabHost.getTabWidget().getChildCount(); i++) {
            if (i == 0) {

                TextView tv = (TextView) tabHost.getTabWidget().getChildAt(i).findViewById(android.R.id.title);
                tv.setTextColor(getResources().getColor(R.color.colorPrimary));
                // tv.setPadding(10, 10, 10, 15);
                tv.setGravity(Gravity.CENTER_VERTICAL);
                tv.setTextSize((float) 17.0);
                tv.setTypeface(null, Typeface.BOLD);
                tv.setAllCaps(false);
                tv.setCompoundDrawablesWithIntrinsicBounds(getResources().getDrawable(R.drawable.dashboard_selected), null, null, null);
                //  tv.setWidth(100);
            } else if (i == 1) {

                TextView tv = (TextView) tabHost.getTabWidget().getChildAt(i).findViewById(android.R.id.title);
                tv.setTextColor(getResources().getColor(R.color.material_grey));
                // tv.setPadding(10, 10, 10, 15);
                tv.setGravity(Gravity.CENTER_VERTICAL);
                tv.setTextSize((float) 17.0);
                tv.setTypeface(null, Typeface.BOLD);
                tv.setAllCaps(false);
                //tv.setBackgroundResource(R.drawable.dashboard);
                tv.setCompoundDrawablesWithIntrinsicBounds(getResources().getDrawable(R.drawable.search_default), null, null, null);
            }

        }

        tabHost.setOnTabChangedListener(new TabHost.OnTabChangeListener() {
            @Override
            public void onTabChanged(String tabId) {

                for (int i = 0; i < tabHost.getTabWidget().getChildCount(); i++) {
                    //tabHost.getTabWidget().getChildAt(i).setBackgroundColor(Color.parseColor("#FF0000")); // unselected
                    TextView tv = (TextView) tabHost.getTabWidget().getChildAt(i).findViewById(android.R.id.title); //Unselected Tabs
                    tv.setTextColor(getResources().getColor(R.color.material_grey));
                    if (i == 0)
                        tv.setCompoundDrawablesWithIntrinsicBounds(getResources().getDrawable(R.drawable.dashboard_default), null, null, null);
                    else
                        tv.setCompoundDrawablesWithIntrinsicBounds(getResources().getDrawable(R.drawable.search_default), null, null, null);

                }

                //tabHost.getTabWidget().getChildAt(tabHost.getCurrentTab()).setBackgroundColor(Color.parseColor("#0000FF")); // selected
                TextView tv = (TextView) tabHost.getCurrentTabView().findViewById(android.R.id.title); //for Selected Tab
                tv.setTextColor(getResources().getColor(R.color.colorPrimary));
                tv.setCompoundDrawablesWithIntrinsicBounds(getResources().getDrawable(R.drawable.dashboard_selected), null, null, null);
                if (tabId.equalsIgnoreCase("Dashboard")) {
                    tv.setCompoundDrawablesWithIntrinsicBounds(getResources().getDrawable(R.drawable.dashboard_selected), null, null, null);
                    //    tabHost.getTabWidget().setBackgroundColor(getResources().getColor(R.color.yellow));
                } else {
                    tv.setCompoundDrawablesWithIntrinsicBounds(getResources().getDrawable(R.drawable.search_selected), null, null, null);
                    //  tabHost.getTabWidget().setBackgroundColor(getResources().getColor(R.color.yellow));
                }


            }
        });

        img_upload_new = (ImageView) findViewById(R.id.imageView1);
       /* fab_menu = (FloatingActionMenu) findViewById(R.id.filter_menu);
        fab_menu.setClosedOnTouchOutside(true);*/
        img_upload_new.bringToFront();
        if (MyApp.get_session(MyApp.SESSION_PHOTO_CAPTURE).equals("Y")) {
            tabHost.setCurrentTab(1);
        }
    }


    private void initComponentListener() {
        btn_setting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                View v1 = btn_setting;
                PopupMenu popup = new PopupMenu(Activity_dashboard.this, v1);
                popup.getMenuInflater().inflate(R.menu.menu_dashboard_setting, popup.getMenu());
                popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        if (item.getTitle().toString().equalsIgnoreCase("About Us")) {
                            Intent intent = new Intent(Activity_dashboard.this, ActivityAboutUs.class);
                            startActivity(intent);
                        } else if (item.getTitle().toString().equalsIgnoreCase("Contact Us")) {
                            Intent intent = new Intent(Activity_dashboard.this, ActivityContactUs.class);
                            startActivity(intent);
                        } else if (item.getTitle().toString().equalsIgnoreCase("Areas We Cover")) {
                            Intent intent = new Intent(Activity_dashboard.this, ActivityAreasWeCover.class);
                            startActivity(intent);
                        }
                        return false;
                    }
                });

                popup.show();
            }
        });

        img_upload_new.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {





                Log.d("", "onClickFlage: "+   isPaused );


                /* fab_menu.close(true);*/
                if (MyApp.get_session(MyApp.SESSION_IS_REGISTERED).equalsIgnoreCase("Y")) {
                    /*Intent i = new Intent(Activity_dashboard.this, Activity_capture_photo.class);
                    startActivity(i);*/
                    if (MyApp.get_session(MyApp.SESSION_FETCHING_LOCATIONS).equalsIgnoreCase("N")) {
                        MyApp.log("Image Clicked");
                        String current_date = ((MyApp) getApplication()).get_current_date();
                        MyApp.log(LOG_TAG, "current date is: " + current_date);
                        try {
                            MyApp.log("In try");
                            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                            File imageStorageDir = new File(Environment.getExternalStorageDirectory(), "MYAPPCC");
                            if (!imageStorageDir.exists()) {
                                imageStorageDir.mkdirs();
                            }
                            str_path = imageStorageDir + File.separator + "IMG-" + String.valueOf(System.currentTimeMillis()) + ".jpg";
                            fileImage = new File(str_path);
                            Uri photoURI = FileProvider.getUriForFile(Activity_dashboard.this,
                                    BuildConfig.APPLICATION_ID + ".provider",
                                    fileImage);
                            intent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);

                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                if (getPermissionCount() > 0)
                                    check_app_persmission();
                                else {
                                    startActivityForResult(intent, TAKE_IMAGE);
                                }
                            } else {
                                startActivityForResult(intent, TAKE_IMAGE);
                            }
                        } catch (Exception e) {

                            isPaused = true;
                            MyApp.log("In catch");
                            MyApp.log("Camera Exception is " + e.getMessage());
                            Snackbar.make(img_upload_new, "Unable to get Camera, Please try again later!", Snackbar.LENGTH_SHORT).show();
                        }
                    } else {



                        final AlertDialog alertDialog = new AlertDialog.Builder(context).create();
                        alertDialog.setTitle(getResources().getString(R.string.app_name));
                        alertDialog.setMessage(getResources().getString(R.string.fetching_data_msg));
                        alertDialog.setIcon(R.mipmap.ic_launcher);
                        alertDialog.setButton("Ok", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                alertDialog.dismiss();
                                isPaused = true;
                            }
                        });
                        alertDialog.show();
                    }
                } else {
                    //if (MyApp.get_session(MyApp.SESSION_FETCHING_LOCATIONS).equalsIgnoreCase("N")) {
                        Intent i = new Intent(Activity_dashboard.this, Activity_Login.class);
                        startActivity(i);
                    /*} else {
                        final AlertDialog alertDialog = new AlertDialog.Builder(context).create();
                        alertDialog.setTitle(getResources().getString(R.string.app_name));
                        alertDialog.setMessage(getResources().getString(R.string.fetching_data_msg));
                        alertDialog.setIcon(R.mipmap.ic_launcher);
                        alertDialog.setButton("Ok", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                alertDialog.dismiss();
                            }
                        });
                        alertDialog.show();
                    }*/
                }
            }
        });
    }

    @Override
    public void onTabChanged(String tabId) {

    }

    boolean isOnResume = true;

    @Override
    protected void onResume() {
        super.onResume();
        MyApp.log("Activity_dashboard", "onResume of Dashboard");
        //Toast.makeText(context, "In onResume of Activity_dashboard", Toast.LENGTH_SHORT).show();
        if (MyApp.get_session(MyApp.SESSION_PHOTO_CAPTURE).equals("Y")) {
            tabHost.setCurrentTab(1);
            MyApp.set_session(MyApp.SESSION_PHOTO_CAPTURE, "");
        }
        if (MyApp.get_session(MyApp.SESSION_FROM_SEARCH).equals("Y")) {
            tabHost.setCurrentTab(0);
            MyApp.set_session(MyApp.SESSION_FROM_SEARCH, "");
        }

        if (!new_gps.canGetLocation() && isOnResume) {
            isOnResume = false;
            MyApp.set_session(MyApp.SESSION_GET_GPS, "Y");
            new_gps.showSettingsAlert(context);
        } else {
            if (MyApp.get_session(MyApp.SESSION_GET_GPS).equalsIgnoreCase("Y")) {
                isOnResume = true;
                MyApp.set_session(MyApp.SESSION_GET_GPS, "");
                new_gps.getLocation();
            }
        }


      /*  if (new_gps==null) {
            new_gps = new GPSTracker1(context);
        }

        try {
            if (new_gps.canGetLocation()) {
                MyApp.log("Activity_map", "onResume of Activity_map canGetLocation");
                MyApp.set_session(MyApp.SESSION_IS_GPS_ENABLED,"Y");
               *//* if (dialog1!=null)
                    if(dialog1.isShowing());
                dialog1.dismiss();*//*

                //new_gps.getLocation();
            } else
                showSettingsAlert(context);
        } catch (SecurityException e){
            MyApp.log("exception onResume Activity_map is " + e.getMessage());
        }*/

    }

    private void check_app_persmission() {
        permission_count = 3;
        int permission_granted = PackageManager.PERMISSION_GRANTED;
        MyApp.log(LOG_TAG, "PersmissionGrantedCode->" + permission_granted);

        int storage_permission = ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        MyApp.log(LOG_TAG, "StoragePermission->" + storage_permission);
        if (storage_permission == permission_granted)
            permission_count -= 1;

        int camera_permission = ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA);
        MyApp.log(LOG_TAG, "CameraPermission->" + camera_permission);
        if (camera_permission == permission_granted)
            permission_count -= 1;

        int location_permission = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION);
        MyApp.log(LOG_TAG, "location_permission->" + location_permission);
        if (location_permission == permission_granted)
            permission_count -= 1;

        /*int sms_permission = ContextCompat.checkSelfPermission(context, Manifest.permission.READ_SMS);
        MyApp.log(LOG_TAG, "sms_permission->" + sms_permission);
        if (sms_permission == permission_granted)
            permission_count -= 1;*/

       /* int location2_permission = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION);
        MyApp.log(LOG_TAG, "location_permission->" + location_permission);
        if(location2_permission == permission_granted)
            permission_count -= 1;*/


        MyApp.log(LOG_TAG, "check_app_permission PermissionCount->" + permission_count);

        if (permission_count > 0) {
            String permissionArray[] = new String[permission_count];

            for (int i = 0; i < permission_count; i++) {
                MyApp.log(LOG_TAG, "i->" + i);

                if (storage_permission != permission_granted) {
                    if (!Arrays.asList(permissionArray).contains(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                        permissionArray[i] = Manifest.permission.WRITE_EXTERNAL_STORAGE;
                        MyApp.log(LOG_TAG, "i->WRITE_EXTERNAL_STORAGE");
                        // break;
                    }
                }

                if (camera_permission != permission_granted) {
                    if (!Arrays.asList(permissionArray).contains(Manifest.permission.CAMERA)) {
                        permissionArray[i] = Manifest.permission.CAMERA;
                        MyApp.log(LOG_TAG, "i->CAMERA");
                        //break;
                    }
                }
                if (location_permission != permission_granted) {
                    if (!Arrays.asList(permissionArray).contains(Manifest.permission.ACCESS_FINE_LOCATION)) {
                        permissionArray[i] = Manifest.permission.ACCESS_FINE_LOCATION;
                        MyApp.log(LOG_TAG, "i->ACCESS_FINE_LOCATION");
                        //break;
                    }
                }

                /*if (sms_permission != permission_granted) {
                    if (!Arrays.asList(permissionArray).contains(Manifest.permission.READ_SMS)) {
                        permissionArray[i] = Manifest.permission.READ_SMS;
                        MyApp.log(LOG_TAG, "i->READ_SMS");
                        //break;
                    }
                }*/

                /*if (location2_permission != permission_granted) {
                    if(!Arrays.asList(permissionArray).contains(Manifest.permission.ACCESS_COARSE_LOCATION)) {
                        permissionArray[i] = Manifest.permission.ACCESS_COARSE_LOCATION;
                        MyApp.log(LOG_TAG, "i->ACCESS_COARSE_LOCATION");
                        //break;
                    }
                }*/


            }
            MyApp.log(LOG_TAG, "PermissionArray->" + Arrays.deepToString(permissionArray));

            ActivityCompat.requestPermissions(Activity_dashboard.this, permissionArray, permission_count);//requestPermissions(permissionArray, permission_count);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions,
                                           int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        permission_count = permissions.length;
        MyApp.log(LOG_TAG, "In onRequestPermissionsResult");
        MyApp.log(LOG_TAG, "requestCode->" + requestCode);
        MyApp.log(LOG_TAG, "permissions->" + Arrays.deepToString(permissions));
        int len = grantResults.length;
        MyApp.log(LOG_TAG, "permissionsLength->" + len);

        int permission_granted = PackageManager.PERMISSION_GRANTED;
        MyApp.log(LOG_TAG, "PermissionGrantedCode->" + permission_granted);
        String str = "";
        for (int i = 0; i < len; i++) {

            if (permissions[i].equalsIgnoreCase(Manifest.permission.ACCESS_FINE_LOCATION)) {
                int location_permission = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION);
                MyApp.log(LOG_TAG, "AccessCore->" + location_permission);
                if (location_permission == permission_granted) {
                    permission_count -= 1;
                } else {
                    str += "Location, ";
                }
            }


            if (permissions[i].equalsIgnoreCase(Manifest.permission.CAMERA)) {
                int camera_permission = ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA);
                MyApp.log(LOG_TAG, "AccessCore->" + camera_permission);
                if (camera_permission == permission_granted) {
                    permission_count -= 1;
                } else {
                    str += "Camera, ";
                }
            }

            if (permissions[i].equalsIgnoreCase(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                int storage_permission = ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE);
                MyApp.log(LOG_TAG, "AccessCore->" + storage_permission);
                if (storage_permission == permission_granted) {
                    permission_count -= 1;
                } else {
                    str += "Storage, ";
                }
            }

            /*if (permissions[i].equalsIgnoreCase(Manifest.permission.READ_SMS)) {
                int sms_permission = ContextCompat.checkSelfPermission(context, Manifest.permission.READ_SMS);
                MyApp.log(LOG_TAG, "AccessCore->" + sms_permission);
                if (sms_permission == permission_granted) {
                    permission_count -= 1;
                } else {
                    str += "sms, ";
                }
            }*/

            MyApp.log(LOG_TAG, "onRequestPermissionsResult PermissionCount->" + permission_count);
        }

        if (permission_count > 0) {
            Snackbar.make(img_upload_new, "My APCC needs permissions : " + str,
                    Snackbar.LENGTH_INDEFINITE).setAction("OK", new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent();
                    String SCHEME = "package";
                    intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                    Uri uri = Uri.fromParts(SCHEME, "com.change22.myapcc", null);
                    intent.setData(uri);
                    startActivity(intent);
                }
            }).show();
        } else {
            if (new_gps.canGetLocation())
                new_gps.getLocation();
           /* else
                new_gps.showSettingsAlert(context);*/
        }
    }

    public int getPermissionCount() {
        int count = 3;
        int permission_granted = PackageManager.PERMISSION_GRANTED;
        int camera_permission = ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA);
        if (camera_permission == permission_granted)
            count -= 1;
        int storage_permission = ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        if (storage_permission == permission_granted)
            count -= 1;
        int access_permission = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION);
        if (access_permission == permission_granted)
            count -= 1;
        /*int sms_permission = ContextCompat.checkSelfPermission(context, Manifest.permission.READ_SMS);
        if (sms_permission == permission_granted)
            count -= 1;*/
        return count;
    }

    //This is the handler that will manager to process the broadcast intent
    private BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            MyApp.log("messege recieved");
            // Extract data included in the Intent
            //String service = intent.getStringExtra("service");
            if (intent != null) {
                String status = intent.getStringExtra("response_code");
                String message = intent.getStringExtra("response_message");
                MyApp.log("CheckSyncUp", "In broadcast receiver of Splash Activity");
                MyApp.log("CheckSyncUp", "Status is " + status + " and message is " + message);

                // Toast.makeText(context,"Status:"+status+"\nMessage:"+message,Toast.LENGTH_SHORT).show();
                if (status != null) {
                    if (status.equals("0")) {
                        MyApp.log("CheckSyncUp", "In if status = 0 of broadcast receiver of Splash Activity");
                        if (progressDialog != null)
                            if (progressDialog.isShowing())
                                progressDialog.hide();
                    }
                    if (status.equals("1") && message.equals("")) {
                        MyApp.log("CheckSyncUp", "In if status = 1 and message = BannerMaster of broadcast receiver of Splash Activity");
                        if (progressDialog != null)
                            if (progressDialog.isShowing())
                                progressDialog.dismiss();

                    } else {
                        MyApp.log("CheckSyncUp", "In else status != 1 of broadcast receiver of Splash Activity");
                        if (progressDialog != null)
                            if (progressDialog.isShowing())
                                progressDialog.setMessage(message);
                    }
                }
            }
        }
    };

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        System.exit(0);
        finish();
       /* if(fab_menu.isOpened()) {
            fab_menu.close(true);
        }*/
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        MyApp.log(LOG_TAG, "onDestroy");
        //MyApp.set_session(MyApp.SESSION_FIRST_LOCATION,"");
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            MyApp.deleteCache(context);
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        MyApp.log(LOG_TAG, "In onSaveInstanceState");
        outState.putString("str_path", str_path);
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        MyApp.log(LOG_TAG, "In onRestoreInstanceState");
        if (savedInstanceState != null) {
            if (savedInstanceState.containsKey("str_path")) {
                str_path = savedInstanceState.getString("str_path");
            }
        }
        fileImage = new File(str_path);
        super.onRestoreInstanceState(savedInstanceState);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        MyApp.log(LOG_TAG, "onActivityResult");
        MyApp.log(LOG_TAG, "RequestCode->" + requestCode);//profile_pic_1 = 501,profile_pic_2 = 502
        MyApp.log(LOG_TAG, "ResultCode->" + resultCode);
        if (data != null) {
            Bundle extras = data.getExtras();
            if (extras != null)
                MyApp.log_bundle(extras);
        }
        if (resultCode != 0) {

            if (requestCode == TAKE_IMAGE) {

                MyApp.log(LOG_TAG, "In request code = 100");
                File f = new File(Environment.getExternalStorageDirectory().toString());
                MyApp.log(LOG_TAG, "IsFileExists->" + fileImage.exists());
                if (fileImage.exists()) {
                    f = fileImage;

                }
                try {
                    Bitmap bitmap;
                    BitmapFactory.Options bitmapOptions = new BitmapFactory.Options();

                    bitmap = compressImage(str_path);
                    //Bitmap bmp = BitmapFactory.decodeFile(str_path);
                    //img_photo_1.setImageBitmap(bitmap);
                    MyApp.log(LOG_TAG, "Image url is " + f.getAbsolutePath() + ", " + f.getAbsoluteFile());

                    if (((MyApp) getApplication()).isNetworkConnectionAvailable()) {

                        MyApp.log(LOG_TAG, "In request code = 100");

                        if (MyApp.get_session(MyApp.SESSION_FETCHING_LOCATIONS).equalsIgnoreCase("N")) {


                            AsyncReportGarbage report_garbage = new AsyncReportGarbage();
                            report_garbage.execute(new_str_path, "from_camera", "", "");


                        } else {
                            final AlertDialog alertDialog = new AlertDialog.Builder(context).create();
                            alertDialog.setTitle(getResources().getString(R.string.app_name));
                            alertDialog.setMessage(getResources().getString(R.string.fetching_data_msg));
                            alertDialog.setIcon(R.mipmap.ic_launcher);

                            alertDialog.setButton("Ok", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    alertDialog.dismiss();
                                }
                            });

                            alertDialog.show();
                        }

                        /*String title = getResources().getString(R.string.app_name);
                        SpannableString ss1 = new SpannableString(title);
                        ss1.setSpan(new ForegroundColorSpan(Color.BLACK), 0, ss1.length(), 0);
                        progressDialog = ProgressDialog.show(context, ss1, "Please wait. Your image is uploading.", false, false);*/


                        /*runMin5 = new Runnable() {
                            @Override
                            public void run() {
                                MyApp.log(LOG_TAG, "In runMin5");
                                if (MyApp.get_session(MyApp.SESSION_FETCHING_LOCATIONS).equalsIgnoreCase("N")) {
                                    hMin10.removeCallbacks(runMin10);
                                    hMin15.removeCallbacks(runMin15);
                                    hMin20.removeCallbacks(runMin20);
                                    AsyncReportGarbage report_garbage = new AsyncReportGarbage();
                                    report_garbage.execute(new_str_path, "from_camera", "", "");
                                }

                            }
                        };
                        hMin5.postDelayed(runMin5, 5000);

                        runMin10 = new Runnable() {
                            @Override
                            public void run() {
                                MyApp.log(LOG_TAG, "In runMin10");
                                if (MyApp.get_session(MyApp.SESSION_FETCHING_LOCATIONS).equalsIgnoreCase("N")) {
                                    hMin15.removeCallbacks(runMin15);
                                    hMin20.removeCallbacks(runMin20);
                                    AsyncReportGarbage report_garbage = new AsyncReportGarbage();
                                    report_garbage.execute(new_str_path, "from_camera", "", "");
                                }
                            }
                        };
                        hMin10.postDelayed(runMin10, 10000);

                        runMin15 = new Runnable() {
                            @Override
                            public void run() {
                                MyApp.log(LOG_TAG, "In runMin15");
                                if (MyApp.get_session(MyApp.SESSION_FETCHING_LOCATIONS).equalsIgnoreCase("N")) {
                                    hMin20.removeCallbacks(runMin20);
                                    AsyncReportGarbage report_garbage = new AsyncReportGarbage();
                                    report_garbage.execute(new_str_path, "from_camera", "", "");
                                }
                            }
                        };
                        hMin15.postDelayed(runMin15, 15000);

                        runMin20 = new Runnable() {
                            @Override
                            public void run() {
                                MyApp.log(LOG_TAG, "In runMin20");
                                if (MyApp.get_session(MyApp.SESSION_FETCHING_LOCATIONS).equalsIgnoreCase("N")) {
                                    AsyncReportGarbage report_garbage = new AsyncReportGarbage();
                                    report_garbage.execute(new_str_path, "from_camera", "", "");
                                } else {
                                    final AlertDialog alertDialog = new AlertDialog.Builder(context).create();
                                    alertDialog.setTitle(getResources().getString(R.string.app_name));
                                    alertDialog.setMessage(getResources().getString(R.string.poor_internet_message));
                                    alertDialog.setIcon(R.mipmap.ic_launcher);

                                    alertDialog.setButton("Ok", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            alertDialog.dismiss();
                                        }
                                    });

                                    alertDialog.show();
                                }
                            }
                        };
                        hMin20.postDelayed(runMin20, 20000);*/

                        //report_garbage.execute(str_path);
                    } else {
                        showInternetDialog("");
                    }

                   /* String path = Environment
                            .getExternalStorageDirectory()
                            + File.separator
                            + "Phoenix" + File.separator + "default";
                    f.delete();
                    OutputStream outFile = null;
                    File file = fileImage;//new File(path, String.valueOf(System.currentTimeMillis()) + ".jpg");
                    try {
                        outFile = new FileOutputStream(file);
                        bitmap.compress(Bitmap.CompressFormat.JPEG, 85, outFile);
                        outFile.flush();
                        outFile.close();
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }*/
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
        }
    }

    public static Bitmap compressImage(String filePath) {

        Bitmap scaledBitmap = null;

        BitmapFactory.Options options = new BitmapFactory.Options();

//      by setting this field as true, the actual bitmap pixels are not loaded in the memory. Just the bounds are loaded. If
//      you try the use the bitmap here, you will get null.
        options.inJustDecodeBounds = true;
        Bitmap bmp = BitmapFactory.decodeFile(filePath, options);

        int actualHeight = options.outHeight;
        int actualWidth = options.outWidth;
        Log.i(LOG_TAG, "BactualWidth->" + actualWidth);
        Log.i(LOG_TAG, "BactualHeight->" + actualHeight);
//      max Height and width values of the compressed image is taken as 816x612

        float maxHeight = 768.0f;
        float maxWidth = 1024.0f;
        float imgRatio = actualWidth / actualHeight;
        float maxRatio = maxWidth / maxHeight;

//      width and height values are set maintaining the aspect ratio of the image

        if (actualHeight > maxHeight || actualWidth > maxWidth) {
            if (imgRatio < maxRatio) {
                imgRatio = maxHeight / actualHeight;
                actualWidth = (int) (imgRatio * actualWidth);
                actualHeight = (int) maxHeight;
            } else if (imgRatio > maxRatio) {
                imgRatio = maxWidth / actualWidth;
                actualHeight = (int) (imgRatio * actualHeight);
                actualWidth = (int) maxWidth;
            } else {
                actualHeight = (int) maxHeight;
                actualWidth = (int) maxWidth;

            }
        }
        Log.i(LOG_TAG, "AactualWidth->" + actualWidth);
        Log.i(LOG_TAG, "AactualHeight->" + actualHeight);
//      setting inSampleSize value allows to load a scaled down version of the original image

        options.inSampleSize = calculateInSampleSize(options, actualWidth, actualHeight);

//      inJustDecodeBounds set to false to load the actual bitmap
        options.inJustDecodeBounds = false;

//      this options allow android to claim the bitmap memory if it runs low on memory
        options.inPurgeable = true;
        options.inInputShareable = true;
        options.inTempStorage = new byte[16 * 1024];

        try {
//          load the bitmap from its path
            bmp = BitmapFactory.decodeFile(filePath, options);
        } catch (OutOfMemoryError exception) {
            exception.printStackTrace();

        }
        try {
            scaledBitmap = Bitmap.createBitmap(actualWidth, actualHeight, Bitmap.Config.ARGB_8888);
        } catch (OutOfMemoryError exception) {
            exception.printStackTrace();
        }

        float ratioX = actualWidth / (float) options.outWidth;
        float ratioY = actualHeight / (float) options.outHeight;
        float middleX = actualWidth / 2.0f;
        float middleY = actualHeight / 2.0f;

        Matrix scaleMatrix = new Matrix();
        scaleMatrix.setScale(ratioX, ratioY, middleX, middleY);

        Canvas canvas = new Canvas(scaledBitmap);
        canvas.setMatrix(scaleMatrix);
        canvas.drawBitmap(bmp, middleX - bmp.getWidth() / 2, middleY - bmp.getHeight() / 2, new Paint(Paint.FILTER_BITMAP_FLAG));

//      check the rotation of the image and display it properly
        ExifInterface exif;
        try {
            exif = new ExifInterface(filePath);

            int orientation = exif.getAttributeInt(
                    ExifInterface.TAG_ORIENTATION, 0);
            Log.i(LOG_TAG, "Exif: " + orientation);
            Matrix matrix = new Matrix();
            if (orientation == 6) {
                matrix.postRotate(90);
                Log.i(LOG_TAG, "Exif: " + orientation);
            } else if (orientation == 3) {
                matrix.postRotate(180);
                Log.i(LOG_TAG, "Exif: " + orientation);
            } else if (orientation == 8) {
                matrix.postRotate(270);
                Log.i(LOG_TAG, "Exif: " + orientation);
            }
            scaledBitmap = Bitmap.createBitmap(scaledBitmap, 0, 0, scaledBitmap.getWidth(), scaledBitmap.getHeight(), matrix, true);
        } catch (IOException e) {
            e.printStackTrace();
        }

        FileOutputStream out = null;
        new_str_path = getFilename();
        try {
            out = new FileOutputStream(new_str_path);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

//          write the compressed bitmap at the destination specified by filename.
        scaledBitmap.compress(Bitmap.CompressFormat.JPEG, 80, out);

        return scaledBitmap;

    }

    public static int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {
            final int heightRatio = Math.round((float) height / (float) reqHeight);
            final int widthRatio = Math.round((float) width / (float) reqWidth);
            inSampleSize = heightRatio < widthRatio ? heightRatio : widthRatio;
        }
        final float totalPixels = width * height;
        final float totalReqPixelsCap = reqWidth * reqHeight * 2;
        while (totalPixels / (inSampleSize * inSampleSize) > totalReqPixelsCap) {
            inSampleSize++;
        }

        return inSampleSize;
    }

    public static String getFilename() {
        File imageStorageDir = new File(Environment.getExternalStorageDirectory(), "MyAPCC");
        if (!imageStorageDir.exists()) {
            imageStorageDir.mkdirs();
        }
        String new_str_path = imageStorageDir + File.separator + "IMG-" + String.valueOf(System.currentTimeMillis()) + ".jpg";
        return new_str_path;
    }

    public void showInternetDialog(String title) {
        final AlertDialog alertDialog = new AlertDialog.Builder(context).create();
        alertDialog.setTitle(getResources().getString(R.string.app_name));
        alertDialog.setMessage(getResources().getString(R.string.no_internet_message));
        alertDialog.setIcon(R.mipmap.ic_launcher);

        alertDialog.setButton("Ok", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                alertDialog.dismiss();
            }
        });

        alertDialog.show();
    }

    public class AsyncReportGarbage extends AsyncTask<String, Void, String> {
        String response = "-0";

        String name = "", email_id = "", device_id = "", gcm_id = "", image_file = "", latitude = "", longitude = "",
                date = "", user_id;


        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            String title = getResources().getString(R.string.app_name);
            SpannableString ss1 = new SpannableString(title);
            ss1.setSpan(new ForegroundColorSpan(Color.BLACK), 0, ss1.length(), 0);
            progressDialog = ProgressDialog.show(context, ss1, "Please wait. Your image is uploading.", false, false);
        }

        @Override
        protected String doInBackground(String... params) {

            name = MyApp.get_session(MyApp.SESSION_USER_NAME);
            email_id = MyApp.get_session(MyApp.SESSION_USER_EMAIL);
            device_id = MyApp.get_device_id();
            gcm_id = MyApp.regid;//MyApp.get_session(MyApp.PREFS_PROPERTY_REG_ID);
            image_file = params[0];
            user_id = MyApp.get_session(MyApp.SESSION_USER_ID);

            String status_flag = params[1];
            if (status_flag.equalsIgnoreCase("from_camera")) {
                latitude = MyApp.get_session(MyApp.SESSION_USER_LATITUDE);
                longitude = MyApp.get_session(MyApp.SESSION_USER_LONGITUDE);
            } else {
                latitude = params[2];
                longitude = params[3];
            }

            /*latitude = MyApp.get_session(MyApp.SESSION_USER_LATITUDE);
            longitude = MyApp.get_session(MyApp.SESSION_USER_LONGITUDE);*/
            date = MyApp.get_current_date();

            MyApp.log(LOG_TAG, "name: " + name);
            MyApp.log(LOG_TAG, "email_id: " + email_id);
            MyApp.log(LOG_TAG, "device_id: " + device_id);
            MyApp.log(LOG_TAG, "gcm_id: " + gcm_id);
            MyApp.log(LOG_TAG, "image_file: " + image_file);
            MyApp.log(LOG_TAG, "latitude: " + latitude);
            MyApp.log(LOG_TAG, "longitude: " + longitude);
            MyApp.log(LOG_TAG, "date: " + date);
            MyApp.log(LOG_TAG, "user_id : " + user_id);

            if (latitude.equals("null") || latitude.equals("")) {
                latitude = "0.0";
            }

            if (longitude.equals("null") || longitude.equals("")) {
                longitude = "0.0";
            }

            boolean flag = TABLE_LOCATIONS.compare_lat_long(Double.parseDouble(latitude), Double.parseDouble(longitude));
            MyApp.log(LOG_TAG, "flag after comparing: " + flag);
            if (flag) {
                MyApp.log(LOG_TAG, "in if flag after comparing: " + flag);
                response = MyApp.add_report_garbage(name, email_id, device_id, gcm_id, image_file, latitude, longitude, date, user_id);
                return response;
            } else {
                //given co-ordinates are not in 40 feets with existing co-ordinates
                MyApp.log(LOG_TAG, "in else flag after comparing: " + flag);
                response = "-1";
                return response;
            }
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            MyApp.log(LOG_TAG, "Add report garbage response is " + response);
            if (progressDialog != null)
                if (progressDialog.isShowing())
                    progressDialog.dismiss();


            isPaused = true;


            if (!response.equalsIgnoreCase("-1")) {
                if (!response.equalsIgnoreCase("-0")) {
                    try {
                        JSONObject jsonObject = new JSONObject(response);
                        if (jsonObject.has("status")) {
                            String status = jsonObject.getString("status");
                            if (status.equalsIgnoreCase("true")) {
                                String response_message = jsonObject.getString("message");



                                showDialog(response_message);

                                JSONObject jsonObject1 = jsonObject.getJSONObject("data");
                                String garbage_id = jsonObject1.getString("garbage_id");
                                String image_url = jsonObject1.getString("image_url");

                                JSONObject jsonObject2 = jsonObject1.getJSONObject("locality");
                                String area = jsonObject2.getString("area");
                                String locality = jsonObject2.getString("locality");
                                String city = jsonObject2.getString("city");
                                String state = jsonObject2.getString("state");
                                String pincode = jsonObject2.getString("pincode");

                                if (TextUtils.isEmpty(MyApp.get_session(MyApp.SESSION_USER_AREA)) || TextUtils.isEmpty(MyApp.get_session(MyApp.SESSION_USER_AREA))) {
                                    String address = area + ", " + locality + ", " + city + ", " + state + " " + pincode;
                                    MyApp.set_session(MyApp.SESSION_USER_AREA, address);
                                    MyApp.set_session(MyApp.SESSION_GPS_CITY, city);

                                    //updateMyActivity(context, "3", "location", "Activity_map");
                                    Intent intent = new Intent("Activity_map");
                                    //put whatever data you want to send, if any
                                    intent.putExtra("response_code", "3");
                                    intent.putExtra("response_message", response_message);
                                    //send broadcast
                                    context.sendBroadcast(intent);
                                }

                                MyApp.log(LOG_TAG, "response_message: " + response_message);
                                MyApp.log(LOG_TAG, "garbage_id: " + garbage_id);
                                MyApp.log(LOG_TAG, "image_url: " + image_url);
                                MyApp.log(LOG_TAG, "area: " + area);
                                MyApp.log(LOG_TAG, "locality: " + locality);
                                MyApp.log(LOG_TAG, "city: " + city);
                                MyApp.log(LOG_TAG, "state: " + state);
                                MyApp.log(LOG_TAG, "pincode: " + pincode);

                                TABLE_GARBAGE.insert_garbage_list(garbage_id, user_id, "", "Garbage", name, image_url, area, locality, city, state, pincode, latitude, longitude, date, "REPORTED");


                            } else if (status.equalsIgnoreCase("false")) {
                                /*String response_message = jsonObject.getString("message");
                                // Snackbar.make(btn_click_camera, response_message, Snackbar.LENGTH_SHORT).show();
                                final AlertDialog alertDialog = new AlertDialog.Builder(context).create();
                                alertDialog.setTitle(getResources().getString(R.string.app_name));
                                alertDialog.setMessage(response_message);
                                alertDialog.setIcon(R.mipmap.ic_launcher);

                                alertDialog.setButton("Ok", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        alertDialog.dismiss();

                                    }
                                });

                                alertDialog.show();*/

                                String response_message = jsonObject.getString("message");
                                if (response_message.equalsIgnoreCase("Your email id is not validated yet.")) {

                                    MyApp.set_session(MyApp.SESSION_USER_EMAIL, "");
                                    MyApp.set_session(MyApp.SESSION_IS_REGISTERED, "");
                                    showSuccessDialog(response_message);

                                } else if (response_message.equalsIgnoreCase("Failed to save image")) {

                                    showDialog(response_message);

                                } else if (response_message.equalsIgnoreCase("Photo is required.")) {

                                    showDialog(response_message);

                                } else if (response_message.equalsIgnoreCase("Failed to save complaint.")) {

                                    showDialog(response_message);

                                }
                            }

                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                } else {
                    showInternetDialog("");
                }
            } else
                showMessageDialog();
        }
    }

    public void showDialog(String title) {
        final AlertDialog alertDialog = new AlertDialog.Builder(context).create();
        alertDialog.setTitle(getResources().getString(R.string.app_name));
        alertDialog.setMessage(title);
        alertDialog.setIcon(R.mipmap.ic_launcher);

        alertDialog.setButton("Ok", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                alertDialog.dismiss();
                MyApp.log(LOG_TAG, "In success response dialog");
                MyApp.set_session(MyApp.SESSION_PHOTO_CAPTURE, "Y");
                //finish();
            }
        });

        alertDialog.show();
    }

    public void showSuccessDialog(String title) {
        final AlertDialog alertDialog = new AlertDialog.Builder(context).create();
        alertDialog.setTitle(getResources().getString(R.string.app_name));
        alertDialog.setMessage(title);
        alertDialog.setIcon(R.mipmap.ic_launcher);

        alertDialog.setButton("Ok", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                alertDialog.dismiss();
                MyApp.set_session(MyApp.SESSION_USER_EMAIL, "");
                MyApp.set_session(MyApp.SESSION_IS_REGISTERED, "");
                //finish();
            }
        });

        alertDialog.show();
    }

    public void showMessageDialog() {
        final AlertDialog alertDialog = new AlertDialog.Builder(context).create();
        alertDialog.setTitle(getResources().getString(R.string.app_name));
        alertDialog.setMessage("APCC does not cover this area yet. Coming Soon!");
        alertDialog.setIcon(R.mipmap.ic_launcher);

        alertDialog.setButton("Ok", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                alertDialog.dismiss();
            }
        });

        alertDialog.show();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
    }
}
