package com.example.common.api;

import androidx.annotation.NonNull;

import com.example.common.api.interceptor.HeaderInterceptor;
import com.example.common.api.interceptor.ResponseInterceptor;
import com.example.common.network.http.Executor;
import com.example.common.network.http.Request;
import com.example.common.network.http.Result;
import com.example.common.network.http.interceptor.Interceptor;

import java.util.ArrayList;
import java.util.List;

/**
 * 执行http请求的具体执行者
 */
public final class ExecutorRequest {

    private ExecutorRequest() {
    }

    /**
     * 同步执行网络请求
     *
     * @param request 请求体
     *
     * @return 返回请求结果
     */
    @NonNull
    public static Result execute(@NonNull Request request) {
        LinkHelper instance = LinkHelper.getInstance();
        List<Interceptor> interceptorList = new ArrayList<>();
        interceptorList.add(new HeaderInterceptor());
        interceptorList.add(new ResponseInterceptor());
        if (request.getConnectTimeout() <= 0) {
            request.setConnectTimeout(instance.getConnectTimeout());
        }
        if (request.getReadTimeout() <= 0) {
            request.setReadTimeout(instance.getReadTimeout());
        }
        if (request.getWriteTimeout() <= 0) {
            request.setWriteTimeout(instance.getWriteTimeout());
        }
        return Executor.execute(request, interceptorList);
    }
}
