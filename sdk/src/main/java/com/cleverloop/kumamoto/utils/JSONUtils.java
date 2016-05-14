package com.cleverloop.kumamoto.utils;

import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by xuemingxiang on 16/5/9.
 */
public class JSONUtils {

    private static final String TAG = JSONUtils.class.getSimpleName();

    public static JSONObject buildJSONObject(Object... params) {
        JSONObject jsonObject = new JSONObject();
        try {
            for (int i = 0; i < params.length; i += 2) {
                jsonObject.put(params[i].toString(), params[i + 1]);
            }
        } catch (JSONException e) {
            Log.e(TAG, Log.getStackTraceString(e));
        }

        return jsonObject;
    }
}
