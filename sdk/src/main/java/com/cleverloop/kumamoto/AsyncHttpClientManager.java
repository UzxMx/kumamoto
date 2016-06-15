package com.cleverloop.kumamoto;

import android.util.Log;

import com.koushikdutta.async.AsyncServer;
import com.koushikdutta.async.http.AsyncHttpClient;
import com.koushikdutta.async.http.AsyncSSLSocketMiddleware;

import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

/**
 * Created by xuemingxiang on 16/6/1.
 */
public class AsyncHttpClientManager {

    private static final String TAG = AsyncHttpClientManager.class.getSimpleName();

    private static AsyncHttpClientManager instance;

    private AsyncHttpClient asyncHttpClient;

    private AsyncHttpClientManager() {
        asyncHttpClient = new AsyncHttpClient(AsyncServer.getDefault());
    }

    public static AsyncHttpClientManager getInstance() {
        if (instance == null) {
            synchronized (AsyncHttpClientManager.class) {
                if (instance == null) {
                    instance = new AsyncHttpClientManager();
                }
            }
        }
        return instance;
    }

    public void init() {
        AsyncSSLSocketMiddleware middleware = asyncHttpClient.getSSLSocketMiddleware();
        middleware.setHostnameVerifier(new HostnameVerifier() {
            @Override
            public boolean verify(String hostname, SSLSession session) {
                return true;
            }
        });
        X509TrustManager trustManager = new X509TrustManager() {

            @Override
            public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {
            }

            @Override
            public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
            }

            @Override
            public X509Certificate[] getAcceptedIssuers() {
                return new X509Certificate[0];
            }
        };
        TrustManager[] managers = new TrustManager[]{trustManager};
        middleware.setTrustManagers(managers);
        try {
            SSLContext sslContext = SSLContext.getInstance("TLS");
            sslContext.init(null, managers, null);
            middleware.setSSLContext(sslContext);
        } catch (NoSuchAlgorithmException e) {
            Log.e(TAG, Log.getStackTraceString(e));
        } catch (KeyManagementException e) {
            e.printStackTrace();
        }
    }

    public AsyncHttpClient getAsyncHttpClient() {
        return this.asyncHttpClient;
    }
}
