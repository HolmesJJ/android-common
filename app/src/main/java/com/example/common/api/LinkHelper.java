package com.example.common.api;

import com.example.common.network.http.Debug;
import com.example.common.network.http.interceptor.Interceptor;

import java.util.ArrayList;
import java.util.List;

/**
 * 引入自定义UI帮助类
 */
public final class LinkHelper {

    /**
     * 连接超时时间
     */
    private int mConnectTimeout;
    /**
     * 读数据超时时间
     */
    private int mReadTimeout;
    /**
     * 写数据超时时间
     */
    private int mWriteTimeout;
    /**
     * 拦截器集合
     */
    private List<Interceptor> mInterceptorList;

    private LinkHelper() {
    }

    public static LinkHelper getInstance() {
        return Instance.INSTANCE;
    }

    private static class Instance {
        private static final LinkHelper INSTANCE = new LinkHelper();
    }

    public int getConnectTimeout() {
        return mConnectTimeout;
    }

    public LinkHelper setConnectTimeout(int mConnectTimeout) {
        this.mConnectTimeout = mConnectTimeout;
        return this;
    }

    public int getReadTimeout() {
        return mReadTimeout;
    }

    public LinkHelper setReadTimeout(int mReadTimeout) {
        this.mReadTimeout = mReadTimeout;
        return this;
    }

    public int getWriteTimeout() {
        return mWriteTimeout;
    }

    public LinkHelper setWriteTimeout(int mWriteTimeout) {
        this.mWriteTimeout = mWriteTimeout;
        return this;
    }

    public List<Interceptor> getInterceptorList() {

        return mInterceptorList;
    }

    public LinkHelper setInterceptorList(List<Interceptor> mInterceptorList) {
        this.mInterceptorList = mInterceptorList;
        return this;
    }

    public LinkHelper addInterceptor(Interceptor interceptor) {
        if (this.mInterceptorList == null) {
            this.mInterceptorList = new ArrayList<>();
        }
        this.mInterceptorList.add(interceptor);
        return this;
    }

    public LinkHelper setDebug(boolean debug) {
        Debug.setDebug(debug);
        return this;
    }
}
