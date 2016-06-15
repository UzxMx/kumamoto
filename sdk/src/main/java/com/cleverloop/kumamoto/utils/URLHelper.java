package com.cleverloop.kumamoto.utils;

import java.util.Map;

/**
 * Created by xuemingxiang on 16/5/31.
 */
public class URLHelper {

    public static String path(String url, Map<String, String> map) {
        if (map != null && map.size() > 0) {
            for (String key : map.keySet()) {
                String value = map.get(key);
                url = url.replaceAll(":" + key, value);
            }
        }
        return url;
    }
}
