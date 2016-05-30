package com.cleverloop.kumamoto;

import android.util.Log;

import com.koushikdutta.async.callback.CompletedCallback;
import com.koushikdutta.async.http.AsyncHttpClient;
import com.koushikdutta.async.http.AsyncHttpGet;
import com.koushikdutta.async.http.WebSocket;

/**
 * Created by xuemingxiang on 16/5/30.
 */
public class ConnectionManager2 {

    private static final String TAG = ConnectionManager2.class.getSimpleName();

    private static final String HOST = "192.168.1.132:3000";

    private static final String HTTP_SCHEMA = "http://" + HOST;

    private static final String WS_SCHEMA = "ws://" + HOST;

    private static final String URL_CABLE = WS_SCHEMA + "/cable";

    private static ConnectionManager2 instance;

    private WebSocketConnectCallback webSocketConnectCallback = new WebSocketConnectCallback();

    private WebSocketCallback webSocketCallback = new WebSocketCallback();

    private Long deviceId;

    private String authToken;

    private ConnectionManager2() {
    }

    public static ConnectionManager2 getInstance() {
        if (instance == null) {
            synchronized (ConnectionManager2.class) {
                if (instance == null) {
                    instance = new ConnectionManager2();
                }
            }
        }
        return instance;
    }

    public void init() {
        connect();
    }

    private void connect() {
        AsyncHttpGet request = new AsyncHttpGet(URL_CABLE.replace("ws://", "http://").replace("wss://", "https://"));
        request.setHeader("X-Application-Id", Kumamoto.getInstance().getAppId());
        request.setHeader("X-Device-Id", Kumamoto.getInstance().getDeviceId());
        AsyncHttpClient.getDefaultInstance().websocket(request, "", webSocketConnectCallback);
    }

    private class WebSocketConnectCallback implements AsyncHttpClient.WebSocketConnectCallback {

        @Override
        public void onCompleted(Exception e, WebSocket webSocket) {
            Log.d(TAG, "onCompleted");

            if (e != null) {
                Log.e(TAG, Log.getStackTraceString(e));
            }

            if (webSocket != null) {
                webSocket.setStringCallback(webSocketCallback);
                webSocket.setClosedCallback(new CompletedCallback() {
                    @Override
                    public void onCompleted(Exception e) {
                        Log.d(TAG, "closed");
                    }

                });
            }
        }
    }

    private class WebSocketCallback implements WebSocket.StringCallback {

        @Override
        public void onStringAvailable(String s) {
            Log.d(TAG, "received:" + s);
        }
    }
}
