package com.example.common.network.http.interceptor;

import androidx.annotation.NonNull;

import com.example.common.network.http.Request;
import com.example.common.network.http.Result;

import java.io.IOException;

/**
 * 拦截器接口
 */
public interface Interceptor {

    /**
     * 拦截器执行方法，在拦截器中应该主动调用chain.proceed(request)方法
     *
     * @param chain 拦截链
     *
     * @return 返回执行结果
     *
     * @throws IOException 当读取网络流出错时会抛出这个异常
     */
    @NonNull
    Result intercept(@NonNull Chain chain) throws IOException;

    /**
     * 拦截器链
     */
    interface Chain {

        /**
         * 获取当前请求对象
         *
         * @return 返回当前请求对象
         */
        @NonNull
        Request request();

        /**
         * 执行下一个拦截器
         *
         * @param request 当前请求对象
         *
         * @return 返回请求结果
         *
         * @throws IOException 当读取网络流出错时会抛出这个异常
         */
        @NonNull
        Result proceed(@NonNull Request request) throws IOException;
    }
}
