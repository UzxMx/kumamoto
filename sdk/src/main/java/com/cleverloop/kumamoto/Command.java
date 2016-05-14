package com.cleverloop.kumamoto;

/**
 * Created by xuemingxiang on 16/5/9.
 */
enum Command {

    SendIdentity("send_identity"),

    GetDeviceInfo("get_device_info"),

    FetchAllLogs("fetch_all_logs"),

    UploadAllLogs("upload_all_logs");

    private String value;

    Command(String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return this.value;
    }
}
