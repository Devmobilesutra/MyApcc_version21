package com.change22.myapcc.activities;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Handler;
import com.google.android.material.snackbar.Snackbar;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import com.change22.myapcc.R;
import com.change22.myapcc.config.MyApp;

import org.json.JSONException;
import org.json.JSONObject;

public class ActivityContactUs extends AppCompatActivity {

    private static final java.lang.String LOG_TAG = "ActivityContactUs";
    TextView txt_toolbar_title;
    Button btn_send;
    ImageButton img_back;
    Context context = null;
    EditText edit_feedback = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    //    Thread.setDefaultUncaughtExceptionHandler(new ExceptionHandler(this));
        setContentView(R.layout.activity_contact_us);
        context = this;
        txt_toolbar_title = (TextView) findViewById(R.id.txt_toolbar_title);
        txt_toolbar_title.setText("Contact Us");

        btn_send = (Button) findViewById(R.id.btn_send);
        edit_feedback = (EditText) findViewById(R.id.edit_feedback);

        btn_send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               /* btn_send.setText("SENT!");
                btn_send.setEnabled(false);
                final AlertDialog alertDialog = new AlertDialog.Builder(context).create();
                alertDialog.setTitle(getResources().getString(R.string.app_name));
                alertDialog.setMessage("Thank you for your feedback.");
                alertDialog.setIcon(R.mipmap.ic_launcher);
                alertDialog.setButton("Ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        alertDialog.dismiss();
                        finish();
                    }
                });
                alertDialog.show();*/
                String body = edit_feedback.getText().toString().trim();
                if (body.length() > 0) {
                    String user = MyApp.get_session(MyApp.SESSION_USER_NAME);
                    String email = MyApp.get_session(MyApp.SESSION_USER_EMAIL);
                    MyApp.log(LOG_TAG, "user is " + user);
                    MyApp.log(LOG_TAG, "mobile is " + email);
                    if (user != null || user.equalsIgnoreCase("")) {
                        body = body + "\n\n From," + "\n\n" + user + "\n\n" + email;
                    } else {
                        body = body + "\n\n From," + "\n\n" + "Unknown Sender";
                    }
                    if (((MyApp) getApplication()).isNetworkConnectionAvailable()) {

                        AsyncFeedback asyncFeedback = new AsyncFeedback();
                        //asyncFeedback.execute("developer@mobilesutra.com",body);
                        asyncFeedback.execute("navrisham@gmail.com", body);

                        final ProgressDialog progressDialog = ProgressDialog.show(context, "User Feedback", "Please wait. We are sending your feedback", false, false);

                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                if (progressDialog != null)
                                    if (progressDialog.isShowing())
                                        progressDialog.dismiss();
                                showMessageDialog("Feedback sent successfully");
                            }
                        }, 7000);

                    }
                } else {
                    Snackbar.make(edit_feedback, "Please insert your feedback", Snackbar.LENGTH_SHORT).show();
                }
            }
        });

        img_back = (ImageButton) findViewById(R.id.img_back);
        img_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }


    @Override
    public void onBackPressed() {
        finish();
    }

    public class AsyncFeedback extends AsyncTask<String, Void, String> {

        ProgressDialog progressDialog = null;
        String response = "-0";
        String message = "";
        String email = "";

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            //progressDialog = ProgressDialog.show(context, "User Feedback", "Please wait. We are sending your feedback", false, false);
        }

        @Override
        protected String doInBackground(String... params) {
            email = params[0];
            message = params[1];

            response = MyApp.send_feedback(email, message);
            return response;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);

            if (!response.equalsIgnoreCase("-0")) {
                try {
                    JSONObject jsonObject = new JSONObject(response);
                    if (jsonObject.has("response_status")) {
                        boolean response_status = jsonObject.getBoolean("response_status");
                        if (response_status) {
                            String message = "Feedback sent successfully";//jsonObject.getString("response_message");
                            //showMessageDialog(message);
                        } else {
                            String message = jsonObject.getString("response_message");
                            //showMessageDialog(message);
                        }
                    }
                } catch (JSONException e) {
                    MyApp.log(LOG_TAG, "Json exception is " + e.getMessage());
                    e.printStackTrace();
                }
            }
        }
    }

    public void showMessageDialog(String message) {
        final AlertDialog alertDialog = new AlertDialog.Builder(context).create();
        alertDialog.setTitle(getResources().getString(R.string.app_name));
        alertDialog.setMessage(message);
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
}
