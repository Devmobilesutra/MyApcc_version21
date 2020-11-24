package com.change22.myapcc.activities;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import com.change22.myapcc.R;

public class ActivityAboutUs extends AppCompatActivity {

    TextView txt_toolbar_title;
    ImageButton img_back;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
      //  Thread.setDefaultUncaughtExceptionHandler(new ExceptionHandler(this));
        setContentView(R.layout.activity_about_us);
        txt_toolbar_title = (TextView) findViewById(R.id.txt_toolbar_title);
        txt_toolbar_title.setText("About Us");

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
}
