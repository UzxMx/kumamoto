package com.cleverloop.kumamoto;

import android.content.Context;

import com.cleverloop.kumamoto.utils.DeviceUtils;

import java.lang.ref.WeakReference;

/**
 * Created by xuemingxiang on 16/5/6.
 */
public class Kumamoto {

    private static Kumamoto instance;

    private WeakReference<Context> contextWeakReference;

    private String appId;

    private String deviceId;

    private Kumamoto() {
    }

    public static void init(Context context, String appId) {
        init(context, appId, new Options());
    }

    public static void init(Context context, String appId, Options options) {
        if (instance != null) {
            return;
        }

        instance = new Kumamoto();
        instance.contextWeakReference = new WeakReference<Context>(context);
        instance.appId = appId;
        instance.deviceId = DeviceUtils.getDeviceIdentity(context);

        ConnectionManager2.getInstance().init();

        ConnectionManager.getInstance();

        LoggerManager loggerManager = LoggerManager.getInstance();
        loggerManager.setCurrentLogLevel(options.logLevel);
        loggerManager.setLogSentFrequence(options.logSentFreq);
        loggerManager.setConsoleOutputEnabled(options.consoleOutputEnabled);
        loggerManager.createScheduleAndStart();

        BatteryInfoManager batteryInfoManager = BatteryInfoManager.getInstance();
        batteryInfoManager.setScheduleFreq(options.batteryStatScheduleFreq);
        batteryInfoManager.createScheduleAndStart();
    }

    public static Kumamoto getInstance() {
        return instance;
    }

    public String getAppId() {
        return appId;
    }

    public Context getContext() {
        return contextWeakReference.get();
    }

    public String getDeviceId() {
        return deviceId;
    }

    public void bindUser(String username) {
        ConnectionManager.getInstance().setUsername(username);
    }

    public static class Options {
        public int logLevel = LoggerManager.LEVEL_MAX;
        public boolean consoleOutputEnabled = true;

        // in minutes
        public int logSentFreq = 5 * 60;
        public int batteryStatScheduleFreq = 20 * 60;
    }
}
