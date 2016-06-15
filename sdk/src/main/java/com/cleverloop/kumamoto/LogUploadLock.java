package com.cleverloop.kumamoto;

/**
 * Created by xuemingxiang on 16/5/31.
 */
public class LogUploadLock {

    private boolean uploadingLogs;

    private long startUploadingLogsTime = 0;

    public boolean isUploadingLogs() {
        if (uploadingLogs && System.currentTimeMillis() - startUploadingLogsTime > 5 * 60 * 1000) {
            uploadingLogs = false;
        }
        return uploadingLogs;
    }

    public void setUploadingLogs(boolean uploadingLogs) {
        this.uploadingLogs = uploadingLogs;
        if (uploadingLogs) {
            startUploadingLogsTime = System.currentTimeMillis();
        }
    }
}
