package com.cleverloop.kumamoto;

/**
 * Created by xuemingxiang on 16/5/11.
 */
public class Logger {

    public static boolean isErrorEnabled() {
        return LoggerManager.getInstance().isErrorEnabled();
    }

    public static boolean isWarnEnabled() {
        return LoggerManager.getInstance().isWarnEnabled();
    }

    public static boolean isInfoEnabled() {
        return LoggerManager.getInstance().isInfoEnabled();
    }

    public static boolean isDebugEnabled() {
        return LoggerManager.getInstance().isDebugEnabled();
    }

    public static void e(String tag, String msg) {
        LoggerManager.getInstance().error(tag, msg);
    }

    public static void w(String tag, String msg) {
        LoggerManager.getInstance().warn(tag, msg);
    }

    public static void i(String tag, String msg) {
        LoggerManager.getInstance().info(tag, msg);
    }

    public static void d(String tag, String msg) {
        LoggerManager.getInstance().debug(tag, msg);
    }
}
