package com.cleverloop.kumamoto;

import android.util.Log;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by xuemingxiang on 16/5/11.
 */
public class LoggerManager {

    private static final String TAG = LoggerManager.class.getSimpleName();

    private static LoggerManager instance;

    public static final int LEVEL_MIN = 0;

    public static final int LEVEL_ERROR = 1;

    public static final int LEVEL_WARN = 2;

    public static final int LEVEL_INFO = 3;

    public static final int LEVEL_DEBUG = 4;

    public static final int LEVEL_MAX = 5;

    private int currentLogLevel;

    private int logSentFrequence;

    private boolean consoleOutputEnabled;

    private BufferedWriter fileWriter;

    private Object writeLock = new Object();

    private SimpleDateFormat logDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    private ScheduleMan scheduleMan = null;

    private LoggerManager() {
        createNewLogFile();
    }

    public static LoggerManager getInstance() {
        if (instance == null) {
            synchronized (LoggerManager.class) {
                if (instance == null) {
                    instance = new LoggerManager();
                }
            }
        }
        return instance;
    }

    public int getCurrentLogLevel() {
        return this.currentLogLevel;
    }

    public void setCurrentLogLevel(int level) {
        this.currentLogLevel = level;
        Log.d(TAG, "log level changed to " + level);
    }

    public int getLogSentFrequence() {
        return this.logSentFrequence;
    }

    public void setLogSentFrequence(int freq) {
        this.logSentFrequence = freq;
        if (scheduleMan != null) {
            scheduleMan.setPeriod(freq * 1000);
        }
    }

    public boolean isConsoleOutputEnabled() {
        return consoleOutputEnabled;
    }

    public void setConsoleOutputEnabled(boolean consoleOutputEnabled) {
        this.consoleOutputEnabled = consoleOutputEnabled;
    }

    public void createScheduleAndStart() {
        if (scheduleMan == null) {
            scheduleMan = new ScheduleMan(getClass().getSimpleName(), new ScheduleCallback(), logSentFrequence * 1000, false);
            scheduleMan.start();
        }
    }

    public boolean isErrorEnabled() {
        return currentLogLevel >= LEVEL_ERROR;
    }

    public boolean isWarnEnabled() {
        return currentLogLevel >= LEVEL_WARN;
    }

    public boolean isInfoEnabled() {
        return currentLogLevel >= LEVEL_INFO;
    }

    public boolean isDebugEnabled() {
        return currentLogLevel >= LEVEL_DEBUG;
    }

    public void console(String tag, String msg) {
        if (!consoleOutputEnabled) {
            return;
        }
        Log.d(tag, msg);
    }

    public void error(String tag, String msg) {
        if (!isErrorEnabled()) {
            return;
        }

        if (consoleOutputEnabled) {
            Log.e(tag, msg);
        }

        log(tag, "ERROR", msg);
    }

    public void warn(String tag, String msg) {
        if (!isWarnEnabled()) {
            return;
        }

        if (consoleOutputEnabled) {
            Log.w(tag, msg);
        }

        log(tag, "WARN", msg);
    }

    public void info(String tag, String msg) {
        if (!isInfoEnabled()) {
            return;
        }

        if (consoleOutputEnabled) {
            Log.i(tag, msg);
        }

        log(tag, "INFO", msg);
    }

    public void debug(String tag, String msg) {
        if (!isDebugEnabled()) {
            return;
        }

        if (consoleOutputEnabled) {
            Log.d(tag, msg);
        }

        log(tag, "DEBUG", msg);
    }

    private void log(String tag, String logLevel, String msg) {
        StringBuilder builder = new StringBuilder();
        builder.append("[")
                .append(logDateFormat.format(new Date()))
                .append("]: [").append(tag).append("]")
                .append(msg).append("\n");
        String str = builder.toString();
        synchronized (writeLock) {
            try {
                fileWriter.write(str);
                fileWriter.flush();
            } catch (IOException e) {
                Log.e(TAG, "write log failed");
            }
        }
    }

    public File[] listCreatedLogFiles() {
        createNewLogFile();
        return FileManager.getInstance().listCreatedLogFiles();
    }

    private void createNewLogFile() {
        synchronized (writeLock) {
            if (fileWriter != null) {
                try {
                    fileWriter.close();
                } catch (IOException e) {
                }
            }

            File file = FileManager.getInstance().createNewLogFile();
            try {
                fileWriter = new BufferedWriter(new FileWriter(file, false));
            } catch (IOException e) {
                Log.e(TAG, "open new log file failed");
            }
        }
    }

    private class ScheduleCallback implements ScheduleMan.Callback {

        @Override
        public void onSchedule() {
            NetworkManager.NetworkType type = NetworkManager.getInstance().getCurrentNetworkType();
            if ((type == NetworkManager.NetworkType.WIFI || type == NetworkManager.NetworkType.ETHERNET)
                    && ConnectionManager.getInstance().connectionEstablished()) {
                ConnectionManager.getInstance().uploadAllLogs();
            }
        }
    }
}
