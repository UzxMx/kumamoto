package com.cleverloop.kumamoto;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;

import java.util.Locale;

/**
 * Created by xuemingxiang on 16/5/10.
 */
public class DeviceInfo {

    private int appVersionCode;

    private String appVersionName;

    private boolean rooted = false;

    private DeviceInfo() {
    }

    public static DeviceInfo getDeviceInfo() {
        DeviceInfo info = new DeviceInfo();
        Context context = Kumamoto.getInstance().getContext();
        if (context != null) {
            try {
                PackageInfo packageInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), PackageManager.GET_META_DATA);
                info.appVersionCode = packageInfo.versionCode;
                info.appVersionName = packageInfo.versionName;
            } catch (PackageManager.NameNotFoundException e) {
            }
        }
        return info;
    }

    private static String capitalize(String s) {
        if (s == null || s.length() == 0) {
            return "";
        }
        char first = s.charAt(0);
        if (Character.isUpperCase(first)) {
            return s;
        } else {
            return Character.toUpperCase(first) + s.substring(1);
        }
    }

    public String getOS() {
        String os = "Android " + Build.VERSION.RELEASE + " " + Build.VERSION.SDK_INT;
        return os;
    }

    public String getManufacturer() {
        String manufacturer = Build.MANUFACTURER;
        String model = Build.MODEL;
        if (model.startsWith(manufacturer)) {
            return capitalize(model);
        } else {
            return capitalize(manufacturer) + " " + model;
        }
    }

    public boolean isRooted() {
        return rooted;
    }

    public String getLocale() {
        Locale locale = Locale.getDefault();
        return locale.getCountry() + " " + locale.getLanguage();
    }

    public String getAppVersion() {
        return appVersionName + " " + appVersionCode;
    }
}
