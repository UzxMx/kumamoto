package com.cleverloop.kumamoto.utils;

import android.content.Context;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.telephony.TelephonyManager;

/**
 * Created by xuemingxiang on 16/5/9.
 */
public class DeviceUtils {

    private static final String TAG = DeviceUtils.class.getSimpleName();

    /**
     *
     * 先尝试DeviceId(一般含有SIM卡的设备返回值不为空)，再尝试Serial Number，若都失败，则产生UUID
     */
    public static String getDeviceIdentity(Context context) {
        TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        String deviceId = tm.getDeviceId();
        if (StringUtils.isBlank(deviceId)) {
            deviceId = tm.getSubscriberId();
            if (StringUtils.isBlank(deviceId)) {
                WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
                WifiInfo wifiInfo = wifiManager.getConnectionInfo();
                if (wifiInfo != null) {
                    deviceId = wifiInfo.getMacAddress();
                }

                // TODO
//                if (StringUtils.isBlank(deviceId)) {
//                    deviceId = UUID.randomUUID().toString();
//                }
            }
        }
        return deviceId;
    }
}
