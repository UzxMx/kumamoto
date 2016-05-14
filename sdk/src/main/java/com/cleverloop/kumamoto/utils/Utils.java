package com.cleverloop.kumamoto.utils;

import java.util.Random;
import java.util.UUID;

/**
 * Created by xuemingxiang on 16/5/12.
 */
public class Utils {

    private static Utils instance;

    private Random boundaryRandom = new Random();

    public static Utils getInstance() {
        if (instance == null) {
            synchronized (Utils.class) {
                if (instance == null) {
                    instance = new Utils();
                }
            }
        }
        return instance;
    }

    public String generateBoundary() {
        StringBuilder builder = new StringBuilder();
        int dashCount = 10 + boundaryRandom.nextInt(7);
        for (int i = 0; i < dashCount; ++i) {
            builder.append('-');
        }
        builder.append(UUID.randomUUID().toString());
        return builder.toString();
    }
}
