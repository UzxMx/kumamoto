package com.cleverloop.kumamoto;

import android.util.Log;

import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by xuemingxiang on 16/5/13.
 */
public class ScheduleMan {

    private static final String TAG = ScheduleMan.class.getSimpleName();

    private String name;

    private Timer timer;

    private TimerTask timerTask;

    private long period;

    private Callback callback;

    private boolean running = false;

    private Object lock = new Object();

    private boolean noDelayed;

    public ScheduleMan(String name, Callback callback, long period, boolean noDelayed) {
        this.name = name;
        this.callback = callback;
        this.period = period;
        this.noDelayed = noDelayed;
    }

    public void start() {
        if (running) {
            return;
        }

        synchronized (lock) {
            if (running) {
                return;
            }

            running = true;
            createTimer();
        }
    }

    private void createTimer() {
        timer = new Timer();
        timerTask = new TimerTask() {
            @Override
            public void run() {
                Log.d(TAG, name + " schedule running");
                if (callback != null) {
                    callback.onSchedule();
                }
            }
        };

        long delay = noDelayed ? 0 : period;
        timer.scheduleAtFixedRate(timerTask, delay, period);
        Log.d(TAG, "schedule timer task, executed after " + period + " milli, period is " + period + " milli");
    }

    public void setPeriod(long period) {
        synchronized (lock) {
            this.period = period;
            if (running) {
                timer.cancel();
                createTimer();
            }
        }
        Log.d(TAG, "period changed to " + period);
    }

//    public void terminate() {
//        if (timer != null) {
//            timer.cancel();
//        }
//    }

    public interface Callback {
        void onSchedule();
    }
}
