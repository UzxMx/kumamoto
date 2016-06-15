package com.cleverloop.kumamoto;

import android.util.Log;

import com.koushikdutta.async.callback.CompletedCallback;
import com.koushikdutta.async.http.AsyncHttpClient;
import com.koushikdutta.async.http.AsyncHttpGet;
import com.koushikdutta.async.http.WebSocket;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by xuemingxiang on 16/5/30.
 */
public class WebSocketConnectionManager {

//    private static final String TAG = WebSocketConnectionManager.class.getSimpleName();
    private static final String TAG = "WebSocketConnManager";

    private static WebSocketConnectionManager instance;

    private long lastTimeReceivedMessageMillis = 0;

    private ConnectionKeeper connectionKeeper = new ConnectionKeeper();

    private WebSocket webSocket;

    private Long deviceId;

    private String authToken;

    public static WebSocketConnectionManager getInstance() {
        if (instance == null) {
            synchronized (WebSocketConnectionManager.class) {
                if (instance == null) {
                    instance = new WebSocketConnectionManager();
                }
            }
        }
        return instance;
    }

    public void init() {
        connectionKeeper.start();
    }

    public String getAuthToken() {
        return authToken;
    }

    public Long getDeviceId() {
        return deviceId;
    }

    public void networkConnected() {
        connectionKeeper.networkConnected();
    }

    public boolean connectionEstablished() {
        return webSocket != null && webSocket.isOpen();
    }

    private class ConnectionKeeper extends Thread {

        private WebSocketConnectCallback connectCallback = new WebSocketConnectCallback();

        private ClosedCallback closedCallback = new ClosedCallback();

        private WebSocketCallback webSocketCallback = new WebSocketCallback();

        private NetworkStatus networkStatus = new NetworkStatus(10 * 1000);

        public void networkConnected() {
            networkStatus.connectionRestored();
        }

        @Override
        public void run() {
            while (true) {
                if (webSocket == null || !webSocket.isOpen()) {
                    AsyncHttpGet request = new AsyncHttpGet(ConnectionManager.URL_CABLE.replace("ws://", "http://").replace("wss://", "https://"));
                    request.setHeader("X-Application-Id", Kumamoto.getInstance().getAppId());
                    request.setHeader("X-Device-Id", Kumamoto.getInstance().getDeviceId());

                    Log.d(TAG, "websocket connect");

                    synchronized (connectCallback) {
                        connectCallback.reset();
                        AsyncHttpClientManager.getInstance().getAsyncHttpClient().websocket(request, "", connectCallback);
                        try {
                            connectCallback.wait(90 * 1000);
                        } catch (InterruptedException e) {
                        }
                    }

                    if (connectCallback.failed) {
                        Log.d(TAG, "connection failed");
                        // wait some time, then continue
                        networkStatus.connectionFailed();
                        continue;
                    }

                    Log.d(TAG, "connection succeed");

                    networkStatus.connectionSucceed();

                    webSocket = connectCallback.webSocket;
                    webSocket.setClosedCallback(closedCallback);
                    webSocket.setStringCallback(webSocketCallback);
                }

                lastTimeReceivedMessageMillis = System.currentTimeMillis();
                synchronized (closedCallback) {
                    while (webSocket != null && webSocket.isOpen()) {
                        if (System.currentTimeMillis() - lastTimeReceivedMessageMillis > 2 * 60 * 1000) {
                            Log.d(TAG, "not got any message during an interval");
                            webSocket.close();
                            webSocket = null;
                            break;
                        }
                        Log.d(TAG, "websocket is open");
                        Log.d(TAG, "network connected: " + NetworkManager.getInstance().isConnected() + " type: " + NetworkManager.getInstance().getCurrentNetworkType());
                        try {
                            closedCallback.wait(30 * 1000);
                        } catch (InterruptedException e) {
                        }
                    }
                }
            }
        }

        private class NetworkStatus {
            private long initialWaitTimeInMillis;

            private int failedCount = 0;

            public NetworkStatus(long initialWaitTimeInMillis) {
                this.initialWaitTimeInMillis = initialWaitTimeInMillis;
            }

            public synchronized void reset() {
                failedCount = 0;
                notifyAll();
            }

            public synchronized void connectionRestored() {
                notifyAll();
            }

            public synchronized void connectionFailed() {
                long timeToWait = initialWaitTimeInMillis * (int) Math.pow(2, failedCount);
                timeToWait = Math.min(timeToWait, 5 * 60 * 1000);
                failedCount++;
                try {
                    wait(timeToWait);
                } catch (InterruptedException e) {
                }
            }

            public void connectionSucceed() {
                failedCount = 0;
            }
        }

        private class WebSocketConnectCallback implements AsyncHttpClient.WebSocketConnectCallback {

            private boolean failed;

            private WebSocket webSocket;

            @Override
            public void onCompleted(Exception e, WebSocket webSocket) {
                Log.d(TAG, "onCompleted");

                if (e == null && webSocket != null) {
                    failed = false;
                    this.webSocket = webSocket;
                } else {
                    failed = true;
                    if (e != null) {
                        Log.e(TAG, Log.getStackTraceString(e));
                    }
                }

                synchronized (this) {
                    notify();
                }
            }

            public void reset() {
                this.failed = true;
                this.webSocket = null;
            }
        }

        private class ClosedCallback implements CompletedCallback {

            @Override
            public void onCompleted(Exception e) {
                if (e != null) {
                    Log.e(TAG, Log.getStackTraceString(e));
                }

                onClosed();
            }

            public void onClosed() {
                synchronized (this) {
                    notify();
                }
            }
        }

        private class WebSocketCallback implements WebSocket.StringCallback {

            @Override
            public void onStringAvailable(String s) {
                // TODO remove log
                lastTimeReceivedMessageMillis = System.currentTimeMillis();
                Log.d(TAG, "received:" + s);
                // TODO May s contain multiple json content??
                JSONObject jsonObject = null;
                try {
                    jsonObject = new JSONObject(s);
                } catch (JSONException e) {
                }

                if (jsonObject == null) {
                    return;
                }

                ConnectionManager connectionManager = ConnectionManager.getInstance();
                String type = jsonObject.optString("type");
                switch (type) {
                    case "auth_info":
                        handleAuthInfo(jsonObject);
                        break;
                    case "get_device_info":
                        connectionManager.handleGetDeviceInfo(jsonObject);
                        break;
                    case "fetch_device_logs":
                        connectionManager.handleFetchDeviceLogs(jsonObject);
                        break;
                    case "configure_logger":
                        connectionManager.handleConfigureLogger(jsonObject);
                        break;
                    default:
                        Log.e(TAG, "unknown type: " + type);
                        break;
                }
            }
        }
    }

    private void handleAuthInfo(JSONObject jsonObject) {
        jsonObject = jsonObject.optJSONObject("data");
        long id = jsonObject.optLong("id");
        String authToken = jsonObject.optString("auth_token");

        deviceId = id;
        this.authToken = authToken;
    }
}
