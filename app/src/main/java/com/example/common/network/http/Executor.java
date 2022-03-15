package com.example.common.network.http;

import androidx.annotation.NonNull;

import com.example.common.network.http.interceptor.BaseChain;
import com.example.common.network.http.interceptor.BaseInterceptor;
import com.example.common.network.http.interceptor.Interceptor;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public final class Executor {

    private Executor() {
    }

    /**
     * 同步执行网络请求
     *
     * @param request 请求体
     *
     * @return 返回请求结果
     */
    @NonNull
    public static Result execute(@NonNull Request request, List<Interceptor> interceptorList) {

        List<Interceptor> interceptors = new ArrayList<>();
        if (interceptorList != null) {
            interceptors.addAll(interceptorList);
        }
        interceptors.add(new BaseInterceptor());
        BaseChain chain = new BaseChain(interceptors, 0, request);
        try {
            return chain.proceed(request);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return new Result(ResponseCode.NETWORK_ERROR, "NETWORK_ERROR", "NETWORK_ERROR", null);
    }
}
