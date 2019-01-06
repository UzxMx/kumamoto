package com.cleverloop.kumamoto;

import android.app.Application;
import android.content.Intent;
import android.util.Log;

import com.cleverloop.kumamoto.service.CoreService;

/**
 * Created by xuemingxiang on 16/5/6.
 */
public class MyApplication extends Application {

    private static final String TAG = MyApplication.class.getSimpleName();

    @Override
    public void onCreate() {
        super.onCreate();

        Kumamoto.init(this, "28cfef44-626e-460b-b11b-9c1fe70b3067");

        Log.d(TAG, "onCreate");

        if (CoreService.shouldStart()) {
            Log.d(TAG, "should start service");

            Intent intent = new Intent(this, CoreService.class);
            startService(intent);
        } else {
            Log.d(TAG, "shouldn't start service");
        }
    }
}
