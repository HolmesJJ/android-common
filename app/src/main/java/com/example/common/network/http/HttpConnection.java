package com.example.common.network.http;

import java.net.HttpURLConnection;
import java.net.URL;

/**
 * HTTP 请求类
 */
public class HttpConnection extends BaseConnection {

    private HttpURLConnection mConn = null;

    public HttpConnection(String url) {
        super();
        try {
            mConn = (HttpURLConnection) new URL(url).openConnection();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected HttpURLConnection getURLConnection() {
        return mConn;
    }
}
