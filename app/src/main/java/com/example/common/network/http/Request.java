package com.example.common.network.http;

import androidx.annotation.NonNull;

import java.util.HashMap;

/**
 * 网络请求实例类的基类，所有具体的网络请求都是他的实现
 */
public class Request {

    public enum RequestMethod {

        /**
         * GET方法
         */
        GET("GET"),
        /**
         * 获取图片
         */
        GET_IMAGE("GET_IMAGE"),
        /**
         * POST方法
         */
        POST("POST");

        private final String value;

        RequestMethod(String value) {
            this.value = value;
        }

        public String value() {
            return value;
        }

        @NonNull
        @Override
        public String toString() {
            return value;
        }
    }

    private HashMap<String, String> mCookieInfo;
    private HashMap<String, String> mHeaderMap;
    private String mPath;
    private String method = "";
    private long mRequestTime = 0;
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
    private Object body;

    public HashMap<String, String> getCookieInfo() {
        return mCookieInfo;
    }

    public Request setCookieInfo(HashMap<String, String> mCookieInfo) {
        this.mCookieInfo = mCookieInfo;
        return this;
    }

    public HashMap<String, String> getHeaderMap() {
        return mHeaderMap;
    }

    public Request setHeaderMap(HashMap<String, String> mHeaderMap) {
        this.mHeaderMap = mHeaderMap;
        return this;
    }

    public String getPath() {
        return mPath;
    }

    public Request setPath(String mPath) {
        this.mPath = mPath;
        return this;
    }

    public String getMethod() {
        return method;
    }

    public Request setMethod(String method) {
        this.method = method;
        return this;
    }

    public long getRequestTime() {
        return mRequestTime;
    }

    public Request setRequestTime(long mRequestTime) {
        this.mRequestTime = mRequestTime;
        return this;
    }

    public int getConnectTimeout() {
        return mConnectTimeout;
    }

    public Request setConnectTimeout(int mConnectTimeout) {
        this.mConnectTimeout = mConnectTimeout;
        return this;
    }

    public int getReadTimeout() {
        return mReadTimeout;
    }

    public Request setReadTimeout(int mReadTimeout) {
        this.mReadTimeout = mReadTimeout;
        return this;
    }

    public int getWriteTimeout() {
        return mWriteTimeout;
    }

    public Request setWriteTimeout(int mWriteTimeout) {
        this.mWriteTimeout = mWriteTimeout;
        return this;
    }

    public Object getBody() {
        return body;
    }

    public Request setBody(Object body) {
        this.body = body;
        return this;
    }
}
