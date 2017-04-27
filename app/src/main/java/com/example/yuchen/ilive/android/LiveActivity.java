package com.example.yuchen.ilive.android;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

/**
 * Created by yuchen on 17/4/27.
 */

public class LiveActivity extends AppCompatActivity {
    @Override
    public void onCreate(Bundle saveBundleInstance) {
        super.onCreate(saveBundleInstance);
        setContentView(R.layout.live);

        //config init
        Config.context = this;


    }
}
