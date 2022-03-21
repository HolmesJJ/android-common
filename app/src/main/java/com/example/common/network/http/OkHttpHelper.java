package com.example.common.network.http;

import androidx.annotation.NonNull;

import com.example.common.constants.Constants;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class OkHttpHelper {

    private static final String HTTP_REQ_MULTIPART_VALUE_CONTENT_TYPE = "multipart/form-data";

    private final OkHttpClient client;
    private final IOkHttpTaskCompleted listener;

    public OkHttpHelper(IOkHttpTaskCompleted listener) {
        this.client = new OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .build();;
        this.listener = listener;
    }

    public void sendRequest(HashMap<String, String> params, String name, File file) {
        MultipartBody.Builder builder = new MultipartBody.Builder();
        builder.setType(MultipartBody.FORM);
        for (Map.Entry<String, String> entry : params.entrySet()) {
            builder.addFormDataPart(entry.getKey(), entry.getValue());
        }
        String path = file.getAbsolutePath();
        RequestBody fileBody = RequestBody.create(file, MediaType.parse(HTTP_REQ_MULTIPART_VALUE_CONTENT_TYPE));
        builder.addFormDataPart(name, path.substring(path.lastIndexOf("/") + 1), fileBody);
        RequestBody requestBody = builder.build();

        okhttp3.Request request = new Request.Builder()
                .url(Constants.HTTPS_SERVER_URL + "api/upload")
                .post(requestBody)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                listener.onFailure(call, e);
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) {
                listener.onResponse(call, response);
            }
        });
    }

    public interface IOkHttpTaskCompleted {
        void onFailure(@NonNull Call call, @NonNull IOException e);
        void onResponse(@NonNull Call call, @NonNull Response response);
    }
}
