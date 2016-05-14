package com.cleverloop.kumamoto;

/**
 * Created by xuemingxiang on 16/5/13.
 */
public class LogUploadLock {

    private long lastTimeUpload;

    public void uploadSucceed() {
        lastTimeUpload = System.currentTimeMillis();
    }

    public boolean shouldUploadLogNow() {
        long now = System.currentTimeMillis();
        if (now - lastTimeUpload > 10 * 1000) {
            return true;
        } else {
            return false;
        }
    }
}
