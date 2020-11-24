package com.change22.myapcc.activities;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageButton;

import com.change22.myapcc.R;

/**
 * Created by Rasika on 02/08/2016.
 */
public class Activity_upload_new_report extends AppCompatActivity {

    ImageButton img_back = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
      //  Thread.setDefaultUncaughtExceptionHandler(new ExceptionHandler(this));
        setContentView(R.layout.activity_upload_new_report);

        initComponents();
        initComponentListener();
    }

    public void initComponents() {

        img_back = (ImageButton) findViewById(R.id.img_back);
    }

    public void initComponentListener() {

        img_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                finish();
            }
        });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }
}
