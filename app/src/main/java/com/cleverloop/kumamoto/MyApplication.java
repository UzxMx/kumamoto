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

        Kumamoto.init(this, "57813bc1-2b0a-4b16-ba6a-d1bcbd49facc");

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
