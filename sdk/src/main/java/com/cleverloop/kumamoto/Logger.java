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

    public static boolean isConsoleEnabled() {
        return LoggerManager.getInstance().isConsoleOutputEnabled();
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

    /**
     * Output to console
     *
     * @param tag
     * @param msg
     */
    public static void c(String tag, String msg) {
        LoggerManager.getInstance().console(tag, msg);
    }
}
