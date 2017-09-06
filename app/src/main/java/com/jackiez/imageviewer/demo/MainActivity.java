package com.jackiez.imageviewer.demo;

import android.content.res.AssetManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

import java.io.IOException;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        final TestView tv = (TestView) findViewById(R.id.v_pic);
        tv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        tv.mockClick();
                    }
                }).start();
            }
        });
        try {
            tv.setImage(getAssets().open("111.jpg", AssetManager.ACCESS_STREAMING));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
