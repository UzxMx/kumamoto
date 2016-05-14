package com.cleverloop.kumamoto;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

/**
 * Created by xuemingxiang on 16/5/13.
 */
public class NetworkManager {

    private static NetworkManager instance;

    public static NetworkManager getInstance() {
        if (instance == null) {
            synchronized (NetworkManager.class) {
                if (instance == null) {
                    instance = new NetworkManager();
                }
            }
        }
        return instance;
    }

    public NetworkType getCurrentNetworkType() {
        Context context = Kumamoto.getInstance().getContext();
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        int type = networkInfo.getType();
        NetworkType networkType = NetworkType.UNKNOWN;
        switch (type) {
            case ConnectivityManager.TYPE_WIFI:
                networkType = NetworkType.WIFI;
                break;
            case ConnectivityManager.TYPE_MOBILE:
                networkType = NetworkType.MOBILE;
                break;
            case ConnectivityManager.TYPE_WIMAX:
                networkType = NetworkType.WIMAX;
                break;
            case ConnectivityManager.TYPE_ETHERNET:
                networkType = NetworkType.ETHERNET;
                break;
            case ConnectivityManager.TYPE_BLUETOOTH:
                networkType = NetworkType.BLUETOOTH;
                break;
            default:
                break;
        }
        return networkType;
    }

    public enum NetworkType {

        UNKNOWN(0), WIFI(1), MOBILE(2), WIMAX(3), ETHERNET(4), BLUETOOTH(5);

        private int val;

        NetworkType(int val) {
            this.val = val;
        }

        public int val() {
            return this.val;
        }
    }
}
