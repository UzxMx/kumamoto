package com.cleverloop.kumamoto;

import android.content.Context;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by xuemingxiang on 16/5/11.
 */
public class FileManager {

    private static FileManager instance;

    private String currentLogFilename;

    private Object currentLogFileLock = new Object();

    private FileManager() {
    }

    public static FileManager getInstance() {
        if (instance == null) {
            synchronized (FileManager.class) {
                if (instance == null) {
                    instance = new FileManager();
                }
            }
        }
        return instance;
    }

    public File getSdkDir() {
        Context context = Kumamoto.getInstance().getContext();
        File file = context.getFilesDir();
        File sdkDir = new File(file, "kumamoto");
        if (!sdkDir.exists()) {
            sdkDir.mkdirs();
            setDirMode(sdkDir);
        }
        return sdkDir;
    }

    private void setDirMode(File dir) {
        dir.setReadable(true, false);
        dir.setWritable(true, false);
        dir.setExecutable(true, false);
    }

    public File getLogDir() {
        File logDir = new File(getSdkDir(), "logs");
        if (!logDir.exists()) {
            logDir.mkdirs();
            setDirMode(logDir);
        }
        return logDir;
    }

    public File createNewLogFile() {
        File dir = getLogDir();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");

        synchronized (currentLogFileLock) {
            currentLogFilename = sdf.format(new Date()) + ".log";
            File file = new File(dir, currentLogFilename);
            if (!file.exists()) {
                try {
                    file.createNewFile();
                    file.setReadable(true, false);
                } catch (IOException e) {
                }
            }
            return file;
        }
    }

    public File[] listCreatedLogFiles() {
        File logDir = getLogDir();
        synchronized (currentLogFileLock) {
            return logDir.listFiles(new FilenameFilter() {
                @Override
                public boolean accept(File dir, String filename) {
                    return !filename.equals(currentLogFilename);
                }
            });
        }
    }
}
