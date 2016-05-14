package com.cleverloop.kumamoto;

import android.app.Application;

/**
 * Created by xuemingxiang on 16/5/6.
 */
public class MyApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        Kumamoto.init(this, "kumamoto");
    }
}
