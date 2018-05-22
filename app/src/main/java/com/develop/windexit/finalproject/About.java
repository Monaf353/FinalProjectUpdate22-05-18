package com.develop.windexit.finalproject;

import android.content.Context;
import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import uk.co.chrisjenx.calligraphy.CalligraphyConfig;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class About extends AppCompatActivity {

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //Font
        CalligraphyConfig.initDefault(new CalligraphyConfig.Builder()
                .setDefaultFontPath("fonts/Roboto_Regular.ttf")
                .setFontAttrId(R.attr.fontPath)
                .build());


        setContentView(R.layout.activity_about);

         /* toolbar =  findViewById(R.id.sylBack);
        setSupportActionBar(toolbar);*/
        getSupportActionBar().setTitle("About");
        setTitleColor(Color.WHITE);
        // toolbar.setTitleTextColor(Color.WHITE);

        //setTitleColor();
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }
}
