package com.cleverloop.kumamoto;

import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by xuemingxiang on 16/5/9.
 */
class Message {

    private static final String TAG = Message.class.getSimpleName();

    private JSONObject message;

    private Message() {
    }

    @Override
    public String toString() {
        if (message != null) {
            return message.toString();
        }

        return "";
    }

    public static Message build(Long id, Type type, Command command, JSONObject content) {
        Message message = new Message();
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("id", id);
            jsonObject.put("type", type.toString());
            jsonObject.put("command", command.toString());
            if (content != null) {
                jsonObject.put("content", content);
            }
        } catch (JSONException e) {
            Log.e(TAG, Log.getStackTraceString(e));
        }

        message.message = jsonObject;

        return message;
    }

    public enum Type {
        Info("info"), Query("query"), Resp("resp");

        private String value;

        Type(String value) {
            this.value = value;
        }

        @Override
        public String toString() {
            return this.value;
        }
    }
}
