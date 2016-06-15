package com.cleverloop.kumamoto;

import android.util.Log;

import com.cleverloop.kumamoto.utils.JSONUtils;
import com.cleverloop.kumamoto.utils.URLHelper;
import com.koushikdutta.async.http.AsyncHttpClient;
import com.koushikdutta.async.http.AsyncHttpPost;
import com.koushikdutta.async.http.AsyncHttpRequest;
import com.koushikdutta.async.http.AsyncHttpResponse;
import com.koushikdutta.async.http.body.FilePart;
import com.koushikdutta.async.http.body.JSONObjectBody;
import com.koushikdutta.async.http.body.MultipartFormDataBody;

import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by xuemingxiang on 16/5/30.
 */
public class ConnectionManager {

//    private static final String TAG = ConnectionManager.class.getSimpleName();
    private static final String TAG = "ConnectionManager";

//    private static final String HOST = "console.common-projects.top";
    private static final String HOST = "192.168.1.132:9292";

    public static final String HTTP_SCHEMA = "https://" + HOST;

    private static final String WS_SCHEMA = "wss://" + HOST;

    public static final String URL_CABLE = WS_SCHEMA + "/cable";

    public static final String URL_DEVICE_INFO = HTTP_SCHEMA + "/devices/:device_id/info";

    public static final String URL_DEVICE_UPLOAD_LOGS = HTTP_SCHEMA + "/devices/:device_id/upload_logs";

    public static final String URL_DEVICE_CONFIGURE_LOGGER = HTTP_SCHEMA + "/devices/:device_id/configure_logger";

    private static ConnectionManager instance;

    private AsyncHttpClientManager asyncHttpClientManager;

    private WebSocketConnectionManager webSocketConnectionManager;

    private String username = "";

    private LogUploadLock logUploadLock = new LogUploadLock();

    private ConnectionManager() {
    }

    public static ConnectionManager getInstance() {
        if (instance == null) {
            synchronized (ConnectionManager.class) {
                if (instance == null) {
                    instance = new ConnectionManager();
                }
            }
        }
        return instance;
    }

    public void init() {
        asyncHttpClientManager = AsyncHttpClientManager.getInstance();
        asyncHttpClientManager.init();
        webSocketConnectionManager = WebSocketConnectionManager.getInstance();
        webSocketConnectionManager.init();
    }

    public boolean connectionEstablished() {
        return webSocketConnectionManager.connectionEstablished();
    }

    public void setUsername(String username) {
        // TODO may need to send to server right now
        this.username = username;
    }

    private JSONObject getDeviceInfo() {
        DeviceInfo deviceInfo = DeviceInfo.getDeviceInfo();
        NetworkManager.NetworkType networkType = NetworkManager.getInstance().getCurrentNetworkType();
        String batteryInfo = BatteryInfoManager.getInstance().getBatteryInfo();
        JSONObject jsonObject = JSONUtils.buildJSONObject("os", deviceInfo.getOS(), "manufacturer", deviceInfo.getManufacturer(),
                "rooted", deviceInfo.isRooted(), "locale", deviceInfo.getLocale(), "app_version", deviceInfo.getAppVersion(),
                "network_type", networkType.val(), "battery_info", batteryInfo, "log_level", LoggerManager.getInstance().getCurrentLogLevel(),
                "log_sent_freq", LoggerManager.getInstance().getLogSentFrequence());
        JSONObject userJsonObject = JSONUtils.buildJSONObject("username", username);
        jsonObject = JSONUtils.buildJSONObject("device_info", jsonObject, "user_info", userJsonObject);
        return jsonObject;
    }

    public void handleGetDeviceInfo(JSONObject jsonObject) {
        JSONObjectBody body = new JSONObjectBody(getDeviceInfo());
        Map<String, String> map = new HashMap<>();
        map.put("device_id", webSocketConnectionManager.getDeviceId().toString());
        String url = URLHelper.path(URL_DEVICE_INFO, map);
        AsyncHttpPost request = new AsyncHttpPost(url);
        request.setBody(body);

        Logger.d(TAG, "handleGetDeviceInfo");
        sendJSONObjectRequest(request, new AsyncHttpClient.JSONObjectCallback() {
            @Override
            public void onCompleted(Exception e, AsyncHttpResponse asyncHttpResponse, JSONObject jsonObject) {
                // TODO
                if (e != null) {
                    Log.e(TAG, Log.getStackTraceString(e));
                }

                if (asyncHttpResponse != null) {
                    Log.d(TAG, asyncHttpResponse.message());
                }
            }
        });
    }

    private void sendJSONObjectRequest(AsyncHttpRequest request, AsyncHttpClient.JSONObjectCallback callback) {
        request.setHeader("X-Authorization", webSocketConnectionManager.getAuthToken());
        asyncHttpClientManager.getAsyncHttpClient().executeJSONObject(request, callback);
    }

    /**
     *
     * @return true continue execution, false abort
     */
    private Object[] prepareAllLogs() {
        File[] files = LoggerManager.getInstance().listCreatedLogFiles();

        if (files == null || files.length == 0) {
            return new Object[]{false, null};
        }

        List<File> list = new ArrayList<>(Arrays.asList(files));
        Collections.sort(list, new Comparator<File>() {
            @Override
            public int compare(File lhs, File rhs) {
                return lhs.getName().compareTo(rhs.getName());
            }
        });

        return new Object[]{true, list};
    }

    public void uploadAllLogs() {
        synchronized (logUploadLock) {
            if (logUploadLock.isUploadingLogs()) {
                return;
            }
            logUploadLock.setUploadingLogs(true);
        }

        try {
            Object[] result = prepareAllLogs();
            if (!(boolean) result[0]) {
                logUploadLock.setUploadingLogs(false);
                return;
            }
            final List<File> list = (List<File>) result[1];

            MultipartFormDataBody body = new MultipartFormDataBody();
            for (int i = 0; i < list.size(); ++i) {
                FilePart filePart = new FilePart("log" + i, list.get(i));
                filePart.setContentType("text/plain; charset=utf-8");
                body.addPart(filePart);
            }
            body.setContentType("multipart/form-data;charset=utf-8");
            body.addStringPart("file_count", Integer.valueOf(list.size()).toString());

            Map<String, String> map = new HashMap<>();
            map.put("device_id", webSocketConnectionManager.getDeviceId().toString());
            String url = URLHelper.path(URL_DEVICE_UPLOAD_LOGS, map);
            AsyncHttpPost request = new AsyncHttpPost(url);
            request.setBody(body);

            sendJSONObjectRequest(request, new AsyncHttpClient.JSONObjectCallback() {
                @Override
                public void onCompleted(Exception e, AsyncHttpResponse asyncHttpResponse, JSONObject jsonObject) {
                    if (e != null) {
                        Log.e(TAG, Log.getStackTraceString(e));
                    }

                    if (jsonObject == null) {
                        Log.e(TAG, "upload logs response is null");

                        if (asyncHttpResponse != null) {
                            Log.e(TAG, asyncHttpResponse.message());
                        } else {
                            Log.e(TAG, "asyncHttpResponse is null");
                        }
                    }

                    if (jsonObject != null) {
                        int status = jsonObject.optInt("status");
                        if (status == 1) {
                            // succeed
                            for (File file : list) {
                                file.delete();
                            }
                        }
                    }

                    logUploadLock.setUploadingLogs(false);
                }
            });
        } catch (Throwable throwable) {
            Log.e(TAG, Log.getStackTraceString(throwable));
            logUploadLock.setUploadingLogs(false);
        }
    }

    public void handleFetchDeviceLogs(JSONObject jsonObject) {
        uploadAllLogs();
    }

    public void handleConfigureLogger(JSONObject jsonObject) {
        JSONObject content = jsonObject.optJSONObject("content");
        if (content.has("log_level")) {
            int logLevel = content.optInt("log_level");
            LoggerManager.getInstance().setCurrentLogLevel(logLevel);
        }

        if (content.has("log_sent_freq")) {
            int logSentFreq = content.optInt("log_sent_freq");
            if (logSentFreq > 0) {
                LoggerManager.getInstance().setLogSentFrequence(logSentFreq);
            }
        }

        JSONObjectBody body = new JSONObjectBody(getDeviceInfo());
        Map<String, String> map = new HashMap<>();
        map.put("device_id", webSocketConnectionManager.getDeviceId().toString());
        String url = URLHelper.path(URL_DEVICE_CONFIGURE_LOGGER, map);
        AsyncHttpPost request = new AsyncHttpPost(url);
        request.setBody(body);

        sendJSONObjectRequest(request, new AsyncHttpClient.JSONObjectCallback() {
            @Override
            public void onCompleted(Exception e, AsyncHttpResponse asyncHttpResponse, JSONObject jsonObject) {
                // TODO
            }
        });
    }
}
