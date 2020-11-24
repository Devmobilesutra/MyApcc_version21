package com.change22.myapcc.activities;

import android.Manifest;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Handler;
import com.google.android.material.snackbar.Snackbar;
import androidx.core.content.ContextCompat;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;

import com.change22.myapcc.R;
import com.change22.myapcc.config.MyApp;

import org.json.JSONException;
import org.json.JSONObject;

public class Activity_Login extends AppCompatActivity {

    private static final java.lang.String LOG_TAG = "Activity_Login";
    EditText edt_name = null, edt_email = null;
    Button btn_submit;
    Context context = null;
    Dialog dialog_otp = null;
    Handler hMin5 = new Handler();
    Handler hMin10 = new Handler();
    Handler hMin15 = new Handler();
    Handler hMin20 = new Handler();
    Runnable runMin5 = null;
    Runnable runMin10 = null;
    Runnable runMin15 = null;
    Runnable runMin20 = null;
    ProgressDialog progressDialog = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity__login);
        context = this;
        initComponents();
        initComponentListener();
    }

    private void initComponents() {
        edt_email = (EditText) findViewById(R.id.edt_email);
        edt_name = (EditText) findViewById(R.id.edt_name);
        btn_submit = (Button) findViewById(R.id.btn_submit);
    }

    private void initComponentListener() {
        btn_submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                String name = edt_name.getText().toString().trim();
                String mobile = edt_email.getText().toString().trim();

                if (name.length() == 0) {
                    Snackbar.make(edt_name, "Please provide your name", Snackbar.LENGTH_SHORT).show();
                } else if ((mobile.length() == 0)) {
                    Snackbar.make(edt_name, "Please provide your mobile number", Snackbar.LENGTH_SHORT).show();
                } else if (!MyApp.isValidMobile(mobile)) {
                    Snackbar.make(edt_name, "Please enter valid mobile number", Snackbar.LENGTH_SHORT).show();
                } else if ((mobile.length() != 10)) {
                    Snackbar.make(edt_name, "Please enter valid mobile number", Snackbar.LENGTH_SHORT).show();
                } else {
                    MyApp.set_session(MyApp.SESSION_USER_NAME, name);
                    MyApp.set_session(MyApp.SESSION_USER_EMAIL, mobile);


                    if (((MyApp) getApplication()).isNetworkConnectionAvailable()) {
                        AsyncVerifyMobile asyncVerifyMobile = new AsyncVerifyMobile();
                        asyncVerifyMobile.execute(mobile, name);
                    } else {
                        showInternetDialog("Verify mobile");
                    }
                   /* MyApp.set_session(MyApp.SESSION_IS_REGISTERED,"Y");

                    Intent intent =  new Intent(Activity_Login.this, Activity_capture_photo.class);
                    startActivity(intent);
                    finish();*/


                }
            }
        });
    }

    public class AsyncVerifyMobile extends AsyncTask<String, Void, String> {
        String mobile = "";
        String name = "";
        String response = "-0";

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog = ProgressDialog.show(context, getResources().getString(R.string.app_name), "Verifying your mobile number automatically in less than 20 seconds", false, false);
        }

        @Override
        protected String doInBackground(String... params) {
            mobile = params[0];
            name = params[1];
            response = MyApp.get_mobile_verification(mobile);

            return response;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);


            if (!response.equalsIgnoreCase("-0")) {
                try {
                    JSONObject jsonObject = new JSONObject(response);
                    if (jsonObject.has("status")) {
                        boolean status = jsonObject.getBoolean("status");
                        if (status) {
                            if (jsonObject.has("OTP")) {
                                final String OTP = jsonObject.getString("OTP");


                                Activity_dashboard.isPaused = true;

                                final String message = jsonObject.getString("message");

                                if (progressDialog != null)
                                    if (progressDialog.isShowing())
                                        progressDialog.dismiss();

                                MyApp.set_session(MyApp.SESSION_IS_REGISTERED, "Y");
                                showMessageDialog("Welcome " + name + "! Now you may start uploading the garbage image");


                                /*runMin5 = new Runnable() {
                                    @Override
                                    public void run() {
                                        boolean bresposne = check_message(OTP, name);
                                        MyApp.log("bresonse " + bresposne);
                                        if (bresposne) {
                                            hMin10.removeCallbacks(runMin10);
                                            hMin15.removeCallbacks(runMin15);
                                            hMin20.removeCallbacks(runMin20);
                                        }

                                    }
                                };
                                hMin5.postDelayed(runMin5, 5000);

                                runMin10 = new Runnable() {
                                    @Override
                                    public void run() {
                                        boolean bresposne = check_message(OTP, name);
                                        MyApp.log("bresonse " + bresposne);
                                        if (bresposne) {
                                            hMin15.removeCallbacks(runMin15);
                                            hMin20.removeCallbacks(runMin20);
                                        }
                                    }
                                };
                                hMin10.postDelayed(runMin10, 10000);

                                runMin15 = new Runnable() {
                                    @Override
                                    public void run() {
                                        boolean bresposne = check_message(OTP, name);
                                        MyApp.log("bresonse " + bresposne);
                                        if (bresposne) {
                                            hMin20.removeCallbacks(runMin20);
                                        }
                                    }
                                };
                                hMin15.postDelayed(runMin15, 15000);

                                runMin20 = new Runnable() {
                                    @Override
                                    public void run() {
                                        boolean bresposne = check_message(OTP, name);
                                        MyApp.log("bresonse " + bresposne);
                                        if (!bresposne) {
                                            if (progressDialog != null)
                                                if (progressDialog.isShowing())
                                                    progressDialog.dismiss();
                                            // check_otp_dialog(OTP,name);
                                            MyApp.set_session(MyApp.SESSION_IS_REGISTERED, "Y");
                                            showMessageDialog("Welcome " + name + "! Now you may start uploading the garbage image");
                                        }
                                    }
                                };
                                hMin20.postDelayed(runMin20, 20000);*/

/*
                                new Handler().postDelayed(new Runnable() {

                                    @Override
                                    public void run() {
                              *//*          Uri uri = Uri.parse("content://sms/inbox");
                                        Cursor c = getContentResolver().query(uri, null, null, null, null);
                                        startManagingCursor(c);

                                        // Read the sms data and store it in the list
                                        if (c.moveToFirst()) {
                                            for (int i = 0; i < c.getCount(); i++) {

                                                String body = c.getString(c.getColumnIndexOrThrow("body")).toString();
                                                String number = (c.getString(c.getColumnIndexOrThrow("address")).toString());
                                                if (number.equals("AM-TRNSMS")) {
                                                    i = c.getCount();
                                                    MyApp.log(LOG_TAG, "body is " + body);
                                                    //"Hello,Your My APCC verification code is " + OTP + "."
                                                   // if (body.equalsIgnoreCase(message)) {
                                                    if (body.contains(message)) {
                                                        MyApp.set_session(MyApp.SESSION_IS_REGISTERED, "Y");
                                                        if (progressDialog != null)
                                                            if (progressDialog.isShowing())
                                                                progressDialog.dismiss();

                                                        showMessageDialog("Welcome " + name + "! Now you may start uploading the garbage image");

                                                    } else {
                                                        MyApp.set_session(MyApp.SESSION_USER_NAME, "");
                                                        MyApp.set_session(MyApp.SESSION_USER_EMAIL, "");*//*

                                        if (progressDialog != null)
                                            if (progressDialog.isShowing())
                                                progressDialog.dismiss();

                                        check_otp_dialog(OTP, name);
                                        //  showMessageDialog("Sorry!! Your mobile verification failed");
                                                  *//*  }
                                                } else
                                                    c.moveToNext();
                                            }
                                        }*//*
                                        //c.close();
                                    }
                                }, 2000);*/

                            }
                        } else {
                            MyApp.set_session(MyApp.SESSION_USER_NAME, "");
                            MyApp.set_session(MyApp.SESSION_USER_EMAIL, "");
                            showMessageDialog("Can't get data. May be your internet speed is slow");
                            Activity_dashboard.isPaused = true;

                        }
                    }

                } catch (JSONException e) {
                    MyApp.log(LOG_TAG, "JSON exception is " + e.getMessage());
                    e.printStackTrace();
                }
            } else {
                if (progressDialog != null)
                    if (progressDialog.isShowing())
                        progressDialog.dismiss();
                Activity_dashboard.isPaused = true;

            }
        }
    }

    public void showMessageDialog(String msg) {
        final AlertDialog alertDialog = new AlertDialog.Builder(context).create();
        alertDialog.setTitle(getResources().getString(R.string.app_name));
        alertDialog.setMessage(msg);
        alertDialog.setIcon(R.mipmap.ic_launcher);

        alertDialog.setButton("Ok", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                alertDialog.dismiss();
                finish();
            }
        });

        alertDialog.show();
    }

    public void showInternetDialog(String title) {
        final AlertDialog alertDialog = new AlertDialog.Builder(context).create();
        alertDialog.setTitle(R.string.app_name);
        alertDialog.setMessage(getResources().getString(R.string.no_internet_message));
        alertDialog.setIcon(R.drawable.ic_launcher);

        alertDialog.setButton("Ok", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                alertDialog.dismiss();
                finish();
            }
        });

        alertDialog.show();
    }

    public void check_otp_dialog(final String otp, final String Name) {

        dialog_otp = new Dialog(Activity_Login.this);
        dialog_otp.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog_otp.setContentView(R.layout.otp);

        dialog_otp.show();
        dialog_otp.setCancelable(false);
        dialog_otp.setCanceledOnTouchOutside(false);
        RelativeLayout rl = (RelativeLayout) dialog_otp.findViewById(R.id.rl);

        Button btn_otp_login = (Button) dialog_otp.findViewById(R.id.otp_login);
        Button btn_otp_cancel = (Button) dialog_otp.findViewById(R.id.otp_cancel);
        final EditText edt_log = (EditText) dialog_otp.findViewById(R.id.edttxt_otp);
        btn_otp_login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Log.d("check_otp if", "s otp" + MyApplication.get_session("otp_session"));
                Log.d("check_otp if", "e otp" + edt_log.getText().toString());
                //  dialog_otp.dismiss();
                if (otp.equalsIgnoreCase(edt_log.getText().toString())) {
                    MyApp.set_session(MyApp.SESSION_IS_REGISTERED, "Y");
                    showMessageDialog("Welcome " + Name + "! Now you may start uploading the garbage image");
                    dialog_otp.dismiss();
                } else {
                    showMessageDialog("Sorry!! Your have entered wrong OTP ");
                    dialog_otp.dismiss();
                }
            }
        });

        btn_otp_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog_otp.dismiss();
            }
        });

    }

    public boolean check_message(String otp, String name) {
        boolean flag = false;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.READ_SMS)
                    == PackageManager.PERMISSION_GRANTED) {

                Uri uri = Uri.parse("content://sms/inbox");
                Cursor c = getContentResolver().query(uri, null, null, null, null);
                startManagingCursor(c);

                // Read the sms data and store it in the list
                if (c != null && !c.isClosed()) {
                    if (c.moveToFirst()) {
                        for (int i = 0; i < c.getCount(); i++) {

                            String body = c.getString(c.getColumnIndexOrThrow("body")).toString();
                            String number = (c.getString(c.getColumnIndexOrThrow("address")).toString());
                            if (number.contains("CHANGE")) {
                                i = c.getCount();
                                MyApp.log(LOG_TAG, "body is " + body);
                                //"Hello,Your My APCC verification code is " + OTP + "."
                                if (body.contains(otp)) {
                                    if (progressDialog != null)
                                        if (progressDialog.isShowing())
                                            progressDialog.dismiss();
                                    flag = true;
                                    MyApp.set_session(MyApp.SESSION_IS_REGISTERED, "Y");
                                    showMessageDialog("Welcome " + name + "! Now you may start uploading the garbage image");
                                }/* else {
                        MyApp.set_session(MyApp.SESSION_USER_NAME, "");
                        MyApp.set_session(MyApp.SESSION_USER_EMAIL, "");

                        showMessageDialog("Sorry!! Your mobile verification failed");
                    }*/
                            } else
                                c.moveToNext();
                        }
                    }
                }
                if (c != null && !c.isClosed())
                    c.close();

            }
        } else {
            Uri uri = Uri.parse("content://sms/inbox");
            Cursor c = getContentResolver().query(uri, null, null, null, null);
            startManagingCursor(c);

            if (c != null && !c.isClosed()) {
                if (c.moveToFirst()) {
                    for (int i = 0; i < c.getCount(); i++) {

                        String body = c.getString(c.getColumnIndexOrThrow("body")).toString();
                        String number = (c.getString(c.getColumnIndexOrThrow("address")).toString());
                        if (number.contains("CHANGE")) {
                            i = c.getCount();
                            MyApp.log(LOG_TAG, "body is " + body);
                            //"Hello,Your My APCC verification code is " + OTP + "."
                            if (body.contains(otp)) {
                                if (progressDialog != null)
                                    if (progressDialog.isShowing())
                                        progressDialog.dismiss();
                                flag = true;
                                MyApp.set_session(MyApp.SESSION_IS_REGISTERED, "Y");
                                showMessageDialog("Welcome " + name + "! Now you may start uploading the garbage image");
                            }/* else {
                        MyApp.set_session(MyApp.SESSION_USER_NAME, "");
                        MyApp.set_session(MyApp.SESSION_USER_EMAIL, "");

                        showMessageDialog("Sorry!! Your mobile verification failed");
                    }*/
                        } else
                            c.moveToNext();
                    }
                }
            }
            if (c != null && !c.isClosed())
                c.close();
        }

        return flag;
    }
}
