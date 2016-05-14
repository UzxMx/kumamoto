package com.cleverloop.kumamoto;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.BatteryManager;
import android.util.Log;

import com.cleverloop.kumamoto.utils.JSONUtils;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by xuemingxiang on 16/5/13.
 */
public class BatteryInfoManager {

    private static final String TAG = BatteryInfoManager.class.getSimpleName();

    private static BatteryInfoManager instance;

    private ScheduleMan scheduleMan;

    private int scheduleFreq;

    private BatteryStatInfo lastLastInfo;

    private BatteryStatInfo lastInfo;

    private Object batteryStatInfoLock = new Object();

    public static BatteryInfoManager getInstance() {
        if (instance == null) {
            synchronized (BatteryInfoManager.class) {
                if (instance == null) {
                    instance = new BatteryInfoManager();
                }
            }
        }
        return instance;
    }

    public int getScheduleFreq() {
        return scheduleFreq;
    }

    public void setScheduleFreq(int scheduleFreq) {
        this.scheduleFreq = scheduleFreq;
    }

    public void createScheduleAndStart() {
        if (scheduleMan == null) {
            scheduleMan = new ScheduleMan(getClass().getSimpleName(), new ScheduleCallback(), scheduleFreq * 1000, true);
            scheduleMan.start();
        }
    }

    private Float getConsumingRate() {
        synchronized (batteryStatInfoLock) {
            if (lastLastInfo == null || lastInfo == null) {
                return null;
            }
            return lastInfo.percent - lastLastInfo.percent;
        }
    }

    public String getBatteryInfo() {
        BatteryStatInfo currentInfo = readBatteryStatInfo();

        Float rate = getConsumingRate();

        JSONObject jsonObject = JSONUtils.buildJSONObject("percent", currentInfo.percent,
                "power_source_type", currentInfo.powerSourceType.val());

        if (rate != null) {
            try {
                jsonObject.put("amount_consumed", rate);
                jsonObject.put("duration", scheduleFreq / 60);
            } catch (JSONException e) {
            }
        }

        return jsonObject.toString();
    }

    private BatteryStatInfo readBatteryStatInfo() {
        Context context = Kumamoto.getInstance().getContext();
        if (context == null) {
            return null;
        }

        Intent batteryIntent = context.registerReceiver(null, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
        int level = batteryIntent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
        int scale = batteryIntent.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
        int plugged = batteryIntent.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1);

        Log.d(TAG, "level: " + level + " scale: " + scale);

        BatteryStatInfo info = new BatteryStatInfo();
        info.level = level;
        info.scale = scale;

        info.percent = (float) (level * 100.0 / scale);

        switch (plugged) {
            case 0:
                info.powerSourceType = PowerSourceType.NoSource;
                break;
            case BatteryManager.BATTERY_PLUGGED_AC:
                info.powerSourceType = PowerSourceType.AC;
                break;
            case BatteryManager.BATTERY_PLUGGED_USB:
                info.powerSourceType = PowerSourceType.USB;
                break;
            case BatteryManager.BATTERY_PLUGGED_WIRELESS:
                info.powerSourceType = PowerSourceType.WIRELESS;
                break;
            default:
                info.powerSourceType = PowerSourceType.Unknown;
                break;
        }

        return info;
    }

    private class ScheduleCallback implements ScheduleMan.Callback {

        @Override
        public void onSchedule() {
            BatteryStatInfo currentInfo = readBatteryStatInfo();

            synchronized (batteryStatInfoLock) {
                if (lastLastInfo == null) {
                    lastLastInfo = currentInfo;
                    return;
                }

                if (lastInfo == null) {
                    lastInfo = currentInfo;
                    return;
                }

                lastLastInfo = lastInfo;
                lastInfo = currentInfo;
            }
        }
    }

    private class BatteryStatInfo {
        public int level;
        public int scale;
        public PowerSourceType powerSourceType;

        public float percent;
    }

    public enum PowerSourceType {

        Unknown(0), NoSource(1), AC(2), USB(3), WIRELESS(4);

        private int val;

        PowerSourceType(int val) {
            this.val = val;
        }

        public int val() {
            return this.val;
        }
    }
}
