package com.example.common.network.http;

import android.annotation.SuppressLint;

import java.net.HttpURLConnection;
import java.net.URL;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

/**
 * HTTPS 请求类
 */
public class HttpsConnection extends BaseConnection {

    private HttpsURLConnection mConn = null;

    public HttpsConnection(String url) {
        super();
        try {
            trustAllHosts();
            mConn = (HttpsURLConnection) new URL(url).openConnection();
            HostnameVerifier doNotVerify = new HostnameVerifier() {
                @Override
                public boolean verify(String hostname, SSLSession session) {
                    return true;
                }
            };
            mConn.setHostnameVerifier(doNotVerify);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected HttpURLConnection getURLConnection() {
        return mConn;
    }

    @SuppressLint("CustomX509TrustManager")
    public void trustAllHosts() {
        // Create a trust manager that does not validate certificate chains
        // Android use X509 cert
        TrustManager[] trustAllCerts = new TrustManager[]{
                new X509TrustManager() {
                    @SuppressLint("TrustAllX509TrustManager")
                    @Override
                    public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {

                    }
                    @SuppressLint("TrustAllX509TrustManager")
                    @Override
                    public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {

                    }
                    @Override
                    public X509Certificate[] getAcceptedIssuers() {
                        return new X509Certificate[]{};
                    }
                }
        };

        // Install the all-trusting trust manager
        try {
            SSLContext sc = SSLContext.getInstance("TLS");
            sc.init(null, trustAllCerts, new java.security.SecureRandom());
            HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
