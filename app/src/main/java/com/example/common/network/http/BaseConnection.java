package com.example.common.network.http;

import android.text.TextUtils;
import android.util.Log;

import com.alibaba.fastjson.JSON;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

/**
 * HttpURLConnection封装基类，网络请求，设置请求协议头、发送请求
 */
public abstract class BaseConnection {

    private static final String LOG_TAG = "BaseConnection";

    public static final String CONNECTION = "Connection";
    public static final String KEEP_ALIVE = "Keep-Alive";
    public static final String CHARSET = "Charset";
    protected static final String HTTP_REQ_PROPERTY_CHARSET = "Accept-Charset";
    protected static final String HTTP_REQ_VALUE_CHARSET = "UTF-8";
    protected static final String HTTP_REQ_PROPERTY_CONTENT_TYPE = "Content-Type";
    protected static final String HTTP_REQ_FORM_VALUE_CONTENT_TYPE = "application/x-www-form-urlencoded";
    protected static final String HTTP_REQ_JSON_VALUE_CONTENT_TYPE = "application/json; " + "charset=UTF-8";
    protected static final String HTTP_REQ_COOKIE = "Cookie";
    public static final String GET = "GET";
    public static final String POST = "POST";
    public static final String BASE_CONNECTION = "BaseConnection";

    public BaseConnection() {

    }

    /**
     * 发起请求
     *
     * @param request 请求体
     *
     * @return 请求结果
     */
    public Result doRequest(Request request) {
        HttpURLConnection connection = getURLConnection();
        if (null == connection || request == null) {
            return new Result<>(ResponseCode.NETWORK_ERROR,
                    "URLConnection is null or request is null",
                    "URLConnection is null or request is null", null);
        }
        // 设置默认参数
        connection.setConnectTimeout(request.getConnectTimeout());
        connection.setReadTimeout(request.getReadTimeout());
        connection.setUseCaches(false);
        // 解决超时后重复上传的问题
        connection.setChunkedStreamingMode(0);
        connection.setRequestProperty(HTTP_REQ_PROPERTY_CHARSET, HTTP_REQ_VALUE_CHARSET);
        // 设置header
        setHeader(request.getHeaderMap());
        // 检查cookie
        if (null != request.getCookieInfo() && request.getCookieInfo().size() > 0) {
            setURLConnectionCookie(request.getCookieInfo());
        }
        if (Request.RequestMethod.GET.value().equals(request.getMethod())) {
            return doGetRequest();
        } else if (Request.RequestMethod.POST.value().equals(request.getMethod())) {
            return doPostRequest(request.getBody());
        } else if (Request.RequestMethod.GET_IMAGE.value().equals(request.getMethod())) {
            return doGetImageRequest();
        } else {
            return new Result<>(ResponseCode.NETWORK_ERROR, String.format("no support method:%s",
                    request.getMethod()), String.format("no support method:%s", request.getMethod()), null);
        }
    }

    private Result doGetImageRequest() {
        InputStream is = null;
        try {
            HttpURLConnection connection = getURLConnection();
            connection.setRequestMethod(GET);
            if (Debug.isDebug()) {
                printUrlAndHeader(connection);
            }
            connection.setDoInput(true);
            is = connection.getInputStream();
            Result result = new Result(ResponseCode.SUCCESS, "ok", "ok", null);
            ByteArrayOutputStream arrayOutputStream = new ByteArrayOutputStream();
            byte[] buff = new byte[512];
            int len;
            while ((len = is.read(buff)) > 0) {
                arrayOutputStream.write(buff, 0, len);
            }
            result.setRawData(arrayOutputStream.toByteArray());
            return result;
        } catch (Exception e) {
            Result<Object> objectResult = new Result<>(ResponseCode.NETWORK_ERROR, "network error", "network error", null);
            e.printStackTrace();
            return objectResult;
        } finally {
            try {
                if (is != null) {
                    is.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            getURLConnection().disconnect();
        }
    }

    /**
     * 设置header
     */
    private void setHeader(HashMap<String, String> header) {
        if (header == null || header.isEmpty()) {
            return;
        }
        HttpURLConnection connection = getURLConnection();
        if (null == connection) {
            return;
        }
        for (Entry<String, String> next : header.entrySet()) {
            if (next == null) {
                continue;
            }
            connection.setRequestProperty(next.getKey(), next.getValue());
        }
    }

    private void setURLConnectionCookie(HashMap<String, String> cookieInfo) {
        HttpURLConnection connection = getURLConnection();
        if (null == connection) {
            return;
        }
        StringBuilder sb = new StringBuilder();
        if (!TextUtils.isEmpty(connection.getRequestProperty(HTTP_REQ_COOKIE))) {
            sb.append(connection.getRequestProperty(HTTP_REQ_COOKIE)).append(";");
        }

        for (Entry<String, String> entry : cookieInfo.entrySet()) {
            if (TextUtils.isEmpty(entry.getKey()) || TextUtils.isEmpty(entry.getValue())) {
                Log.d(LOG_TAG, "cookie inf is bad");
            } else {
                sb.append(entry.getKey()).append("=").append(entry.getValue()).append(";");
            }
        }
        connection.setRequestProperty(HTTP_REQ_COOKIE, sb.toString());
    }

    private Result doGetRequest() {
        String resultStr = "";
        InputStream is = null;
        try {
            HttpURLConnection connection = getURLConnection();
            connection.setRequestProperty(HTTP_REQ_PROPERTY_CONTENT_TYPE,
                                          HTTP_REQ_FORM_VALUE_CONTENT_TYPE);
            connection.setRequestMethod(GET);
            if (Debug.isDebug()) {
                printUrlAndHeader(connection);
            }
            // http成功的返回code，和服务的成功code不一定一致
            int successCode = 200;
            Result result = null;
            if (connection.getResponseCode() == successCode) {
                is = connection.getInputStream();
                resultStr = readDataFromStream(is);
                if (resultStr.charAt(0) == '[') {
                    resultStr = "{" + "\"list\":" + resultStr + "}";
                    result = JSON.parseObject(resultStr, Result.class);
                } else {
                    result = JSON.parseObject(resultStr, Result.class);
                }
            } else {
                is = connection.getErrorStream();
                resultStr = readDataFromStream(is);
                result = new Result();
                result.setCode(connection.getResponseCode());
                result.setData("");
                result.setDesc(resultStr);
                result.setMessage(resultStr);
            }

            if (Debug.isDebug()) {
                String baseConnection = "BaseConnection";
                Log.d(baseConnection, "response: " + resultStr);
            }
            result.setOriginData(resultStr);
            return result;
        } catch (Exception e) {
            Result<Object> objectResult = new Result<>(ResponseCode.NETWORK_ERROR, "network error", "network error", null);
            e.printStackTrace();
            return objectResult;
        } finally {
            try {
                if (is != null) {
                    is.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            getURLConnection().disconnect();
        }
    }

    /**
     * 从流中读取数据
     *
     * @param is 输入流
     *
     * @return 流中的数据
     */
    private String readDataFromStream(InputStream is) {
        if (is == null) {
            return "";
        }
        BufferedReader br = null;
        try {
            br = new BufferedReader(new InputStreamReader(is, HTTP_REQ_VALUE_CHARSET));
            String line = null;
            StringBuilder sb = new StringBuilder();
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }
            return sb.toString();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return "";
    }

    private void printUrlAndHeader(HttpURLConnection connection) {
        String baseConnection = "BaseConnection";
        Map<String, List<String>> headerFields = connection.getRequestProperties();
        Log.d(baseConnection, "url: " + connection.getURL());
        if (headerFields != null) {
            StringBuilder sb = new StringBuilder();
            for (Entry<String, List<String>> next : headerFields.entrySet()) {
                sb.append(next.getKey()).append(":");
                List<String> value = next.getValue();
                if (value != null) {
                    for (int i = 0; i < value.size(); i++) {
                        sb.append(value.get(i));
                    }
                }
                sb.append("\n");
            }
            Log.d(baseConnection, "header: " + sb.toString());
        }
    }

    private Result doPostRequest(Object obj) {
        OutputStream out = null;
        InputStream is = null;
        try {
            // 向服务器发送post请求
            HttpURLConnection connection = getURLConnection();
            // 发送POST请求必须设置如下两行
            connection.setDoOutput(true);
            connection.setDoInput(true);
            connection.setUseCaches(false);
            connection.setRequestMethod(POST);
            connection.setRequestProperty(CONNECTION, KEEP_ALIVE);
            connection.setRequestProperty(CHARSET, HTTP_REQ_VALUE_CHARSET);
            connection.setRequestProperty(HTTP_REQ_PROPERTY_CONTENT_TYPE,
                                          HTTP_REQ_JSON_VALUE_CONTENT_TYPE);
            String body = JSON.toJSONString(obj);

            if (Debug.isDebug()) {
                printUrlAndHeader(connection);
                Log.d(BASE_CONNECTION, "body: " + body);
            }

            out = connection.getOutputStream();
            out.write(body.getBytes(HTTP_REQ_VALUE_CHARSET));
            out.flush();
            // 4. 从服务器获得回答的内容
            // http成功的返回code，和服务的成功code不一定一致
            int successCode = 200;
            Result result = null;
            String resultStr = "";
            if (connection.getResponseCode() == successCode) {
                is = connection.getInputStream();
                resultStr = readDataFromStream(is);
                if (resultStr.charAt(0) == '[') {
                    resultStr = "{" + "\"list\":" + resultStr + "}";
                    result = JSON.parseObject(resultStr, Result.class);
                } else {
                    result = JSON.parseObject(resultStr, Result.class);
                }
            } else {
                is = connection.getErrorStream();
                resultStr = readDataFromStream(is);
                result = new Result();
                result.setCode(connection.getResponseCode());
                result.setData("");
                result.setDesc(resultStr);
                result.setMessage(resultStr);
            }

            if (Debug.isDebug()) {
                String baseConnection = "BaseConnection";
                Log.d(baseConnection, "response: " + resultStr);
            }
            result.setOriginData(resultStr);
            return result;
        } catch (Exception e) {
            Result<Object> objectResult = new Result<>(ResponseCode.NETWORK_ERROR, "network error", "network error", null);
            e.printStackTrace();
            return objectResult;
        } finally {
            if (out != null) {
                try {
                    out.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            getURLConnection().disconnect();
        }
    }

    // 读取请求头
    private String getRequestHeader(HttpURLConnection conn) {
        Map<String, List<String>> requestHeaderMap = conn.getRequestProperties();
        Iterator<String> requestHeaderIterator = requestHeaderMap.keySet().iterator();
        StringBuilder sbRequestHeader = new StringBuilder();
        while (requestHeaderIterator.hasNext()) {
            String requestHeaderKey = requestHeaderIterator.next();
            String requestHeaderValue = conn.getRequestProperty(requestHeaderKey);
            sbRequestHeader.append(requestHeaderKey);
            sbRequestHeader.append(":");
            sbRequestHeader.append(requestHeaderValue);
            sbRequestHeader.append("\n");
        }
        return sbRequestHeader.toString();
    }

    // 使用表单提交时，读取请求体
    private String getBody(HashMap<String, String> params) {
        StringBuilder result = new StringBuilder();
        try {
            boolean first = true;
            for (Map.Entry<String, String> entry : params.entrySet()) {
                if (first) {
                    first = false;
                } else {
                    result.append("&");
                }
                result.append(URLEncoder.encode(entry.getKey(), HTTP_REQ_VALUE_CHARSET));
                result.append("=");
                result.append(URLEncoder.encode(entry.getValue(), HTTP_REQ_VALUE_CHARSET));
            }
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return result.toString();
    }

    /**
     * 获取connection
     *
     * @return 返回connection
     */
    protected abstract HttpURLConnection getURLConnection();
}
