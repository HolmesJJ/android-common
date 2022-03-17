package com.example.common.network.http;

import android.util.Log;

import androidx.annotation.NonNull;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import com.alibaba.fastjson.annotation.JSONField;

import java.io.Serializable;
import java.util.ArrayList;

public class Result<T> implements Serializable {

    private static final String TAG = Result.class.getSimpleName();
    private static final long serialVersionUID = 7072819455431215931L;

    @JSONField(name = "code")
    private int code;

    @JSONField(name = "data")
    private T data;

    @JSONField(name = "message")
    private String message;

    @JSONField(name = "desc")
    private String desc;

    private String originData;
    private byte[] rawData;

    public Result() {

    }

    public Result(int code, String message, String desc, T data) {
        this(code, data, message, desc, null);
    }

    public Result(int code, T data, String message, String desc, String originData) {
        this.code = code;
        this.data = data;
        this.message = message;
        this.desc = desc;
        this.originData = originData;
    }

    public <U> U createInstance(Class<U> cls) {
        U obj = null;
        try {
            obj = cls.newInstance();
        } catch (Exception e) {
            e.printStackTrace();
            Log.e(TAG, e.getMessage());
        }
        return obj;
    }

    public boolean isSuccess() {
        return this.code == ResponseCode.SUCCESS;
    }

    public boolean isTokenTimeout() {
        return this.code == ResponseCode.TOKEN_TIMEOUT;
    }

    public boolean isForbidden() {
        return this.code == ResponseCode.FORBIDDEN;
    }

    public T getBody(Class<T> cls) {
        if (data == null) {
            try {
                data = createInstance(cls);
                data = JSONObject.parseObject(originData, cls);
            } catch (Exception e) {
                e.printStackTrace();
                Log.e(TAG, e.getMessage());
                return null;
            }
        }
        try {
            if (data instanceof JSONObject) {
                data = JSONObject.parseObject(data.toString(), cls);
            }
        } catch (Exception e) {
            e.printStackTrace();
            Log.e(TAG, e.getMessage());
            return null;
        }
        return data;
    }

    @SuppressWarnings("unchecked")
    public T getBody(TypeReference<T> typeReference) {
        if (data == null) {
            try {
                data = (T) new ArrayList<>();
            } catch (Exception e) {
                e.printStackTrace();
                Log.e(TAG, e.getMessage());
                return null;
            }
        }
        try {
            if (data instanceof JSONArray) {
                data = JSONObject.parseObject(data.toString(), typeReference.getType());
            }
        } catch (Exception e) {
            e.printStackTrace();
            Log.e(TAG, e.getMessage());
            return null;
        }
        return data;
    }

    public int getCode() {
        return code;
    }

    public Result<T> setCode(int code) {
        this.code = code;
        return this;
    }

    public T getData() {
        return data;
    }

    public Result<T> setData(T data) {
        this.data = data;
        return this;
    }

    public String getMessage() {

        return message;
    }

    public Result<T> setMessage(String message) {
        this.message = message;
        return this;
    }

    public String getDesc() {
        return desc;
    }

    public Result<T> setDesc(String desc) {
        this.desc = desc;
        return this;
    }

    public String getOriginData() {
        return originData;
    }

    public Result<T> setOriginData(String originData) {
        this.originData = originData;
        return this;
    }

    public Result<T> setRawData(byte[] rawData) {
        this.rawData = rawData;
        return this;
    }

    public byte[] getRawData() {
        return rawData;
    }

    @NonNull
    @Override
    public String toString() {
        return "Result{" + "code = '" + code + '\'' + ",data = '" + data + '\'' + ",message = '"
                + message + '\'' + ",desc = '" + desc + '\'' + "}";
    }
}
