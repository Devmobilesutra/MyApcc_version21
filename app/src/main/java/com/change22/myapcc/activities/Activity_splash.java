package com.change22.myapcc.activities;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;

import androidx.appcompat.app.AppCompatActivity;

import android.view.View;
import android.view.Window;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.change22.myapcc.R;
import com.change22.myapcc.config.AppService;
import com.change22.myapcc.config.MyApp;

import java.util.Date;

public class Activity_splash extends AppCompatActivity {

    Context context = null;
    boolean active = true;
    final int splashTime = 0000; // time to display the splash screen in m
    ProgressDialog progressDialog = null;
    ProgressBar progress_bar = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(savedInstanceState);
        context = this;
        /*GPSTracker1 gps = new GPSTracker1(context);
        if (!gps.canGetLocation()) {
            gps.showSettingsAlert(context);
        } else {
            gps.getLocation();
            //Toast.makeText(context,"Locations On Activity_Splash:\n"+gps.getLatitude()+"\n"+gps.getLongitude(),Toast.LENGTH_SHORT).show();
        }*/

       /// Thread.setDefaultUncaughtExceptionHandler(new ExceptionHandler(this));

        setContentView(R.layout.activity_splash);
        MyApp.set_session(MyApp.SESSION_FIRST_LOCATION, "");
        MyApp.set_session(MyApp.SESSION_PHOTO_CAPTURE, "");
        MyApp.set_session(MyApp.SESSION_IS_GPS_ENABLED, "");
        progress_bar = (ProgressBar) findViewById(R.id.progress_bar);

        if (MyApp.db != null)
            MyApp.db.getWritableDatabase();

        MyApp.set_session(MyApp.SESSION_FROM_SEARCH, "");
        MyApp.log("GCM registered flag is " + MyApp.get_session(MyApp.SESSION_IS_GCM_REGISTRED_TO_SERVER));
        MyApp.log("UserId is " + MyApp.get_session(MyApp.SESSION_USER_ID));

        if (((MyApp) getApplicationContext()).getRegistrationId(context).length() == 0) {
            ((MyApp) getApplicationContext()).getRegistrationGCMID();
        } else {
            if (MyApp.get_session(MyApp.SESSION_IS_GCM_REGISTRED_TO_SERVER).equalsIgnoreCase("")) {
                if (((MyApp) getApplication()).isNetworkConnectionAvailable()) {
                    Intent intent1 = new Intent(Intent.ACTION_SYNC, null, context, AppService.class);
                    intent1.putExtra("Flag", "register_gcm");
                   //new Activity_map.getTruckData_new().execute();
                    startService(intent1);
                }
            }
        }




        /*Thread splashTread = new Thread() {
            @Override
            public void run() {
                try {
                    int waited = 0;
                    while (active && (waited < splashTime)) {
                        sleep(100);
                        if (active) {
                            waited += 100;
                        }
                    }
                } catch (InterruptedException e) {
                    // do nothing
                } finally {
                    if (((MyApp) getApplication()).isNetworkConnectionAvailable()) {
                        MyApp.set_session(MyApp.SESSION_ACTIVITY, "Activity_splash");
                        progressDialog = ProgressDialog.show(Activity_splash.this, "Sync up", "Getting updated data", false, true);
                        MyApp.log("start", "service");
                        Intent intent1 = new Intent(Intent.ACTION_SYNC, null, context, AppService.class);
                        intent1.putExtra("Flag", "getAllData");
                        startService(intent1);
                    } else {
                        showInternetDialog("Sync Up");
                    }
                }
            }
        };
        splashTread.start();*/
        //chech_for_upgrade();

        //go_proceed();
    }

    private void go_proceed() {
        /*if (TABLE_LOCATIONS.getCount()) {
            //insert hardcode fellowship by json
            MyApp.log("Activity_splash", "start insert hardcode data service");
            Intent intent1 = new Intent(Intent.ACTION_SYNC, null, context, AppService.class);
            intent1.putExtra("Flag", "JSON");
            startService(intent1);
        }*/

        if (((MyApp) getApplication()).isNetworkConnectionAvailable()) {
            // progressDialog = ProgressDialog.show(Activity_splash.this, "", "", false, true);
            MyApp.log("start", "service");
            Intent intent1 = new Intent(Intent.ACTION_SYNC, null, context, AppService.class);
            intent1.putExtra("Flag", "getAllData");
            // intent1.putExtra("Flag", "getGarbageData");

          //  new Activity_map.getTruckData_new().execute();

            startService(intent1);
        } else {
            showInternetDialog("Sync Up");
        }
    }

    /*@Override
    protected void onResume() {
        super.onResume();
        context.registerReceiver(mMessageReceiver, new IntentFilter("Activity_splash"));
        chech_for_upgrade();
    }*/

    @Override
    protected void onResume() {
        super.onResume();

        if (((MyApp) getApplication()).isNetworkConnectionAvailable()) {

            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    context.registerReceiver(mMessageReceiver, new IntentFilter("Activity_splash"));
                    chech_for_upgrade();
                }
            }, 3000);

        } else {
            showInternetDialog("Sync Up");
        }
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        context.registerReceiver(mMessageReceiver, new IntentFilter("Activity_splash"));
        context.unregisterReceiver(mMessageReceiver);
    }

    //This is the handler that will manager to process the broadcast intent
    private BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(final Context context, Intent intent) {
            MyApp.log("messege recieved");
            // Extract data included in the Intent
            //String service = intent.getStringExtra("service");
            if (intent != null) {
                String status = intent.getStringExtra("response_code");
                String message = intent.getStringExtra("response_message");
                MyApp.log("CheckSyncUp", "In broadcast receiver of Splash Activity");
                MyApp.log("CheckSyncUp", "Status is " + status + " and message is " + message);


              //   Toast.makeText(context,"Status:"+status+"\nMessage:"+message,Toast.LENGTH_SHORT).show();
                if (status != null) {
                    if (status.equals("0")) {
                        MyApp.log("CheckSyncUp", "In if status = 0 of broadcast receiver of Splash Activity");
                        if (progressDialog != null)
                            if (progressDialog.isShowing())
                                progressDialog.hide();
                    }
                    if (status.equals("1") && message.equals("GetAll")) {
                        MyApp.log("CheckSyncUp", "In if status = 1 and message = BannerMaster of broadcast receiver of Splash Activity");
                        if (progressDialog != null)
                            if (progressDialog.isShowing())
                                progressDialog.dismiss();

                        progress_bar.setVisibility(View.GONE);

                        Thread splashTread = new Thread() {
                            @Override
                            public void run() {
                                try {
                                    int waited = 0;
                                    while (active && (waited < splashTime)) {
                                        sleep(100);
                                        if (active) {
                                            waited += 100;
                                        }
                                    }
                                } catch (InterruptedException e) {
                                    // do nothing
                                } finally {

                                    Intent intent1 = new Intent(Activity_splash.this, Activity_dashboard.class);
                                    startActivity(intent1);
                                    finish();

                                   /* if (((MyApp) getApplication()).isNetworkConnectionAvailable()) {
                                        MyApp.set_session(MyApp.SESSION_ACTIVITY, "Activity_splash");
                                        progressDialog = ProgressDialog.show(Activity_splash.this, "Sync up", "Getting updated data", false, true);
                                        MyApp.log("start", "service");
                                        Intent intent1 = new Intent(Intent.ACTION_SYNC, null, context, AppService.class);
                                        intent1.putExtra("Flag", "getAllData");
                                        startService(intent1);
                                    } else {
                                        showInternetDialog("Sync Up");
                                    }*/
                                }
                            }
                        };
                        splashTread.start();


                    } else {
                        MyApp.log("CheckSyncUp", "In else status != 1 of broadcast receiver of Splash Activity");
                        if (progressDialog != null)
                            if (progressDialog.isShowing())
                                progressDialog.setMessage("");
                    }
                }
            }
        }
    };

    public void showInternetDialog(String title) {
        final AlertDialog alertDialog = new AlertDialog.Builder(context).create();
        alertDialog.setTitle(R.string.app_name);
        alertDialog.setMessage(getResources().getString(R.string.no_internet_message));
        alertDialog.setIcon(R.drawable.ic_launcher);

        alertDialog.setButton("Ok", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                finish();
            }
        });

        alertDialog.show();
    }

    void chech_for_upgrade() {
        if (((MyApp) getApplication()).isNetworkConnectionAvailable()) {
            if (MyApp.get_Longsession(MyApp.SESSION_VERSION_DATE_CHECK) == 0) {
                new AsyncVersion().execute();
            } else {
                long session_time = MyApp.get_Longsession(MyApp.SESSION_VERSION_DATE_CHECK);
                Date date = new Date();
                long date_time = date.getTime();
                long diff = date_time - session_time;
                MyApp.log("TimeSession->" + session_time);
                MyApp.log("TimeCurrent->" + date_time);
                MyApp.log("TimeDiff->" + diff);
                if (diff > 21600000)//21600000
                {
                    MyApp.log("IfDiff>10000->" + (diff > 21600000));
                    new AsyncVersion().execute();
                } else {
                    go_proceed();
                }
            }
        } else {
            showInternetDialog("");
        }
    }

    public class AsyncVersion extends AsyncTask<String, Void, String> {

        protected void onPreExecute() {
            MyApp.log("AsyncVersion-onPreExecute");

        }

        @Override
        protected String doInBackground(String... urls) {
            String response = "";
            try {

                response = MyApp.get_play_store_version();
                return response;
            } catch (Exception e) {
                return "-2";
            }
        }

        @Override
        protected void onPostExecute(String result) {
            if (result != "-0") {

                int server_version = Integer.parseInt(result);
                int app_version = 0;
                PackageManager manager = context.getPackageManager();
                PackageInfo info = null;
                try {
                    info = manager.getPackageInfo(context.getPackageName(), 0);
                    app_version = info.versionCode;


                } catch (PackageManager.NameNotFoundException e) {
                    MyApp.log("VersionE->" + e);
                }
                MyApp.log("AppVersion->" + app_version);
                MyApp.log("AppVersion->" + server_version);
                if (MyApp.get_Longsession(MyApp.SESSION_VERSION_DATE_CHECK) == 0) {
                    MyApp.log("AsyncOnPostExecuteIf");
                    if (server_version > app_version) {
                        //current application is not updated
                        show_upgrade_dialog();
                    } else {
                        //current application is updated
                        Date date = new Date();
                        long date_time = date.getTime();
                        //Toast.makeText(Activity_splash.this, "New Version", Toast.LENGTH_SHORT).show();
                        MyApp.set_session(MyApp.SESSION_VERSION_DATE_CHECK, date_time + "");

                        go_proceed();
                    }

                } else {
                    MyApp.log("AsyncOnPostExecuteElse");
                    //Toast.makeText(Activity_splash.this, "In else", Toast.LENGTH_SHORT).show();
                    if (server_version > app_version) {
                        //current application is not updated
                        show_upgrade_dialog();
                    } else {
                        //current application is updated
                        Date date = new Date();
                        long date_time = date.getTime();
                        //Toast.makeText(Activity_splash.this, "New Version", Toast.LENGTH_SHORT).show();
                        MyApp.set_session(MyApp.SESSION_VERSION_DATE_CHECK, date_time + "");

                        go_proceed();
                    }
                }
            } else {
                go_proceed();
            }
        }
    }


    void show_upgrade_dialog() {
        final Dialog dialog1 = new Dialog(Activity_splash.this);
        dialog1.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog1.setContentView(R.layout.layout_upgrade);

        dialog1.show();
        dialog1.setCancelable(false);
        dialog1.setCanceledOnTouchOutside(false);

        final TextView txt_upgrade = (TextView) dialog1.findViewById(R.id.txt_upgrade);
        final TextView txt_cancel = (TextView) dialog1.findViewById(R.id.txt_cancel);
        txt_upgrade.setOnClickListener(new View.OnClickListener() {
            public void onClick(View arg0) {
                // TODO Auto-generated method stub

                Intent intent = new Intent();
                Uri uri = Uri.parse("market://details?id=" + "com.change22.myapcc&hl=en");
                Intent goToMarket = new Intent(Intent.ACTION_VIEW, uri);
                // To count with Play market backstack, After pressing back button,
                // to taken back to our application, we need to add following flags to intent.
                intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY | Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET | Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
                try {
                    MyApp.log("1");
                    startActivity(goToMarket);
                    MyApp.log("2");
                } catch (ActivityNotFoundException e) {
                    MyApp.log("3");
                    startActivity(new Intent(Intent.ACTION_VIEW,
                            Uri.parse("http://play.google.com/store/apps/details?id=" + "com.change22.myapcc&hl=en")));
                }
                dialog1.dismiss();
            }
        });

        txt_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

}
