package com.jackiez.imageviewer.demo;

import android.content.res.AssetManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import java.io.IOException;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        TestView tv = (TestView) findViewById(R.id.v_pic);
        try {
            tv.setImage(getAssets().open("111.jpg", AssetManager.ACCESS_STREAMING));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
