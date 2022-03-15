package com.example.common.network.http.interceptor;

import androidx.annotation.NonNull;

import com.example.common.network.http.BaseConnection;
import com.example.common.network.http.HttpConnection;
import com.example.common.network.http.HttpsConnection;
import com.example.common.network.http.Request;
import com.example.common.network.http.Result;

import java.io.IOException;

/**
 * 基础的拦截器实现类，最终是由该类来实现请求
 */
public class BaseInterceptor implements Interceptor {

    public static final String HTTPS = "https:";

    @NonNull
    @Override
    public Result intercept(@NonNull Chain chain) throws IOException {
        Request request = chain.request();
        BaseConnection connection;
        String url = request.getPath();
        if (url.startsWith(HTTPS)) {
            connection = new HttpsConnection(url);
        } else {
            connection = new HttpConnection(url);
        }
        return connection.doRequest(request);
    }
}
