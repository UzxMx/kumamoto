package com.cleverloop.kumamoto.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import com.cleverloop.kumamoto.Logger;

/**
 * Created by xuemingxiang on 16/5/14.
 */
public class CoreService extends Service {

    private static final String TAG = CoreService.class.getSimpleName();

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Logger.d(TAG, "onStartCommand");

        return START_STICKY;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        Logger.d(TAG, "onCreate");
    }
}
