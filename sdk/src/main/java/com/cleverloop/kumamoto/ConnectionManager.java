package com.cleverloop.kumamoto;

import android.util.Log;

import com.cleverloop.kumamoto.utils.JSONUtils;
import com.cleverloop.kumamoto.utils.Utils;

import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by xuemingxiang on 16/5/6.
 */
class ConnectionManager {

    private static final String TAG = ConnectionManager.class.getSimpleName();

    private static ConnectionManager connectionManager;

    private static final String HOST = "101.200.140.48";

    private static final int PORT = 8081;

    private static final char[] EOL = {'\r', '\n'};

    private WorkerThread workerThread;

    private Socket socket;

    // TODO whether need to use BufferedInputStream
    private InputStream inputStream;

    private BufferedOutputStream outputStream;

    private Object outputStreamLock = new Object();

    private Long messageId = 1L;

    private Object messageIdLock = new Object();

    private Map<Long, FilesTransaction> transactions = new HashMap<>();

    private LogUploadLock logUploadLock = new LogUploadLock();

    private boolean connectionEstablished = false;

    private String username = "";

    private ConnectionManager() {
        start();
    }

    public static ConnectionManager getInstance() {
        if (connectionManager == null) {
            synchronized (ConnectionManager.class) {
                if (connectionManager == null) {
                    connectionManager = new ConnectionManager();
                }
            }
        }
        return connectionManager;
    }

    private synchronized Long getNewMessageId() {
        Long id;
        synchronized (messageIdLock) {
            id = messageId;
            messageId += 1;
        }
        return id;
    }

    private void start() {
        workerThread = new WorkerThread();
        workerThread.start();
    }

    private void sendMessage(Message message) {
        String content = message.toString();
        try {
            synchronized (outputStreamLock) {
                outputStream.write(content.getBytes());
                outputStream.write(EOL[0]);
                outputStream.write(EOL[1]);
                outputStream.flush();
            }
        } catch (IOException e) {
            Log.e(TAG, Log.getStackTraceString(e));
        }
    }

    private void writeBoundary(String boundary) {
        try {
            synchronized (outputStreamLock) {
                outputStream.write(boundary.getBytes());
                outputStream.write(EOL[0]);
                outputStream.write(EOL[1]);
            }
        } catch (IOException e) {
            Log.e(TAG, Log.getStackTraceString(e));
        }
    }

    private void writeEOFBoundary(String boundary) {
        try {
            synchronized (outputStreamLock) {
                outputStream.write(boundary.getBytes());
                outputStream.write("--".getBytes());
            }
        } catch (IOException e) {
            Log.e(TAG, Log.getStackTraceString(e));
        }
    }

    private void sendMessageWithFiles(Message message, List<File> files, String boundary) {
        String content = message.toString();
        try {
            synchronized (outputStreamLock) {
                outputStream.write(content.getBytes());
                outputStream.write(EOL[0]);
                outputStream.write(EOL[1]);
                writeBoundary(boundary);

                byte[] buf = new byte[16 * 1024];
                for (File file : files) {
                    Log.d(TAG, "file: " + file.getAbsolutePath());
                    BufferedInputStream bis = new BufferedInputStream(new FileInputStream(file));
                    int count;
                    int total_count = 0;
                    while ((count = bis.read(buf)) != -1) {
                        total_count += count;
                        outputStream.write(buf, 0, count);
                    }
                    Log.d(TAG, "read file " + file.getName() + ", total size: " + total_count);
                    bis.close();
                    writeBoundary(boundary);
                }
                writeEOFBoundary(boundary);
                outputStream.flush();
            }

            Log.d(TAG, "files sent");
        } catch (IOException e) {
            Log.e(TAG, Log.getStackTraceString(e));
        }
    }

    private void sendIdentity() {
        Kumamoto kumamoto = Kumamoto.getInstance();
        JSONObject content = JSONUtils.buildJSONObject("app_id", kumamoto.getAppId(), "device_id", kumamoto.getDeviceId());
        Message message = Message.build(getNewMessageId(), Message.Type.Info, Command.SendIdentity, content);
        sendMessage(message);
    }

    private void getDeviceInfo(Long id) {
        DeviceInfo deviceInfo = DeviceInfo.getDeviceInfo();
        NetworkManager.NetworkType networkType = NetworkManager.getInstance().getCurrentNetworkType();
        String batteryInfo = BatteryInfoManager.getInstance().getBatteryInfo();
        JSONObject jsonObject = JSONUtils.buildJSONObject("os", deviceInfo.getOS(), "manufacturer", deviceInfo.getManufacturer(),
                "rooted", deviceInfo.isRooted(), "locale", deviceInfo.getLocale(), "app_version", deviceInfo.getAppVersion(),
                "network_type", networkType.val(), "battery_info", batteryInfo, "log_level", LoggerManager.getInstance().getCurrentLogLevel(),
                "log_sent_freq", LoggerManager.getInstance().getLogSentFrequence());
        JSONObject userJsonObject = JSONUtils.buildJSONObject("username", username);
        jsonObject = JSONUtils.buildJSONObject("device_info", jsonObject, "user_info", userJsonObject);
        Message message = Message.build(id, Message.Type.Resp, Command.GetDeviceInfo, jsonObject);
        sendMessage(message);
    }

    public void setUsername(String username) {
        // TODO may need to send to server right now
        this.username = username;
    }

    public boolean connectionEstablished() {
        return connectionEstablished;
    }

    public void uploadAllLogs() {
        synchronized (logUploadLock) {
            if (!logUploadLock.shouldUploadLogNow()) {
                return;
            }

            Object[] result = prepareAllLogs();
            if (!(boolean) result[0]) {
                return;
            }
            List<File> list = (List<File>) result[1];

            String boundary = Utils.getInstance().generateBoundary();
            JSONObject jsonObject = JSONUtils.buildJSONObject("boundary", boundary);
            Long id = getNewMessageId();
            Message message = Message.build(id, Message.Type.Query, Command.UploadAllLogs, jsonObject);

            transactions.put(id, new FilesTransaction(list));
            sendMessageWithFiles(message, list, boundary);

            logUploadLock.uploadSucceed();
        }
    }

    /**
     *
     * @return true continue executtion, false abort
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

    /**
     * Format: boundary
     *
     * @param id
     */
    private void fetchAllLogs(Long id) {
        synchronized (logUploadLock) {
            if (!logUploadLock.shouldUploadLogNow()) {
                return;
            }

            Object[] result = prepareAllLogs();
            if (!(boolean) result[0]) {
                return;
            }
            List<File> list = (List<File>) result[1];

            String boundary = Utils.getInstance().generateBoundary();
            JSONObject jsonObject = JSONUtils.buildJSONObject("boundary", boundary);
            Message message = Message.build(id, Message.Type.Resp, Command.FetchAllLogs, jsonObject);

            transactions.put(id, new FilesTransaction(list));
            sendMessageWithFiles(message, list, boundary);

            logUploadLock.uploadSucceed();
        }
    }

    private void finishTransaction(Long id) {
        FilesTransaction transaction = transactions.get(id);
        transaction.finish();
    }

    private void configureLogger(JSONObject message) {
        JSONObject content = message.optJSONObject("content");
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
    }

    private class WorkerThread extends Thread {

        private static final int BUFFER_SIZE = 10240;

        private byte[] buffer = new byte[BUFFER_SIZE];

        private int tryCount = 0;

        @Override
        public void run() {
            while (true) {
                try {
                    if (tryCount++ > 2) {
                        Thread.sleep(20 * 1000);
                        tryCount = 0;
                    }
                    Log.d(TAG, "connect to server");
                    socket = new Socket();
                    socket.connect(new InetSocketAddress(HOST, PORT));
                    inputStream = socket.getInputStream();
                    outputStream = new  BufferedOutputStream(socket.getOutputStream());
                    Log.d(TAG, "connected");

                    sendIdentity();

                    connectionEstablished = true;
                    tryCount = 0;
                    doWork();
                } catch (Throwable throwable) {
                    Log.e(TAG, Log.getStackTraceString(throwable));
                    connectionEstablished = false;

                } finally {
                    if (socket != null) {
                        try {
                            socket.close();
                        } catch (IOException e) {
                        }
                    }
                }
            }
        }

        private void doWork() throws IOException {
            int offset = 0, count = 0;
            StringBuilder builder = new StringBuilder();
            while (true) {
                if (offset == count) {
                    count = inputStream.read(buffer);
                    if (count == -1) {
                        break;
                    } else if (count == 0) {
                        continue;
                    }
                    offset = 0;
                }

                int idx = findEOL(buffer, offset, count);
                if (idx == -1) {
                    builder.append(new String(buffer, offset, count - offset));
                    offset = count;
                    continue;
                }

                // a piece of message found
                builder.append(new String(buffer, offset, idx - offset));
                offset = idx + 2;

                String jsonString = builder.toString();
                Log.d(TAG, "message: " + jsonString);

                builder.delete(0, builder.length());

                try {
                    JSONObject message = new JSONObject(jsonString);
                    processMessage(message);
                } catch (Throwable throwable) {
                    Log.e(TAG, Log.getStackTraceString(throwable));
                }
            }
        }

        private void processMessage(JSONObject message) throws Exception {
            Long id = message.optLong("id");
            String type = message.optString("type");
            String command = message.optString("command");

            if (id == null || id <= 0) {
                throw new Exception("message id format is wrong");
            }

            if (type.equals("info")) {
                switch (command) {
                    case "configure_logger":
                        configureLogger(message);
                        break;
                    default:
                        Log.e(TAG, "unknown command");
                }
            } else if (type.equals("query")) {
                switch (command) {
                    case "get_device_info":
                        getDeviceInfo(id);
                        break;
                    case "fetch_all_logs":
                        fetchAllLogs(id);
                        break;
                    default:
                        Log.e(TAG, "unknown command");
                }
            } else if (type.equals("resp")) {
                switch (command) {
                    case "fetch_all_logs_return":
                    case "upload_all_logs_return":
                        finishTransaction(id);
                        break;
                    default:
                        Log.e(TAG, "unknown resp");
                }
            } else {
                throw new Exception("unknown message type");
            }
        }

        /**
         *
         * @param buf
         * @param start
         * @param len
         * @return the index of \r\n in the buf, -1 if not found
         */
        private int findEOL(byte[] buf, int start, int len) {
            for (int i = start; i < len - 1; ++i) {
                if (buf[i] == EOL[0] && buf[i + 1] == EOL[1]) {
                    return i;
                }
            }
            return -1;
        }
    }
}
