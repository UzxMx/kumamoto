package com.cleverloop.kumamoto.utils;

/**
 * Created by xuemingxiang on 16/5/9.
 */
public class StringUtils {

    public static boolean isBlank(String str) {
        return str == null || str.trim().equals("");
    }
}
