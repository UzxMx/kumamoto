package com.cleverloop.kumamoto.activity;

import android.app.Activity;
import android.os.Bundle;

import com.cleverloop.kumamoto.Kumamoto;
import com.cleverloop.kumamoto.Logger;
import com.cleverloop.kumamoto.R;

/**
 * Created by xuemingxiang on 16/5/6.
 */
public class MainActivity extends Activity {

    private static final String TAG = MainActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Logger.d(TAG, "onCreate");

        setContentView(R.layout.activity_main);

        Kumamoto.getInstance().bindUser("327110424@163.com");

        new Thread(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    if (Logger.isErrorEnabled()) {
                        Logger.e(TAG, "Error ouput");
                    }
                    if (Logger.isWarnEnabled()) {
                        Logger.w(TAG, "Warn output");
                    }
                    if (Logger.isInfoEnabled()) {
                        Logger.i(TAG, "Info output");
                    }
                    if (Logger.isDebugEnabled()) {
                        Logger.d(TAG, "Debug output");
                    }

                    try {
                        Thread.sleep(3000);
                    } catch (InterruptedException e) {
                    }
                }
            }
        }).start();

//        Intent intent = new Intent(getApplicationContext(), CoreService.class);
//        startService(intent);
    }

    @Override
    protected void onStop() {
        super.onStop();
        Logger.d(TAG, "onStop");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        Logger.d(TAG, "onDestroy");
    }
}
