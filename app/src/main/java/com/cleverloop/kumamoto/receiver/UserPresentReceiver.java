package com.cleverloop.kumamoto.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

/**
 * Created by xuemingxiang on 16/5/14.
 */
public class UserPresentReceiver extends BroadcastReceiver {

    private static final String TAG = UserPresentReceiver.class.getSimpleName();

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(TAG, "user present receiver");
    }
}
