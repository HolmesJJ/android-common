package com.example.common.api;

import android.util.Pair;

import androidx.annotation.NonNull;

import com.alibaba.fastjson.JSON;
import com.example.common.api.model.login.DeviceUUIDResult;
import com.example.common.api.model.login.LoginResult;
import com.example.common.api.model.login.PublicKeyResult;
import com.example.common.api.model.main.DownloadResult;
import com.example.common.api.model.main.TasksResult;
import com.example.common.api.model.mouth.MouthsResult;
import com.example.common.api.model.speech.UploadResult;
import com.example.common.api.model.token.RefreshTokenResult;
import com.example.common.api.model.tutorial.EnglishResult;
import com.example.common.config.Config;
import com.example.common.constants.Constants;
import com.example.common.network.http.Request;
import com.example.common.network.http.Result;
import com.example.common.utils.RSAUtils;

import java.util.HashMap;
import java.util.List;

/**
 * 标准http接口请求管理类
 */
public final class ApiClient {

    private ApiClient() {
    }

    @SuppressWarnings("unchecked")
    @NonNull
    public static Result<PublicKeyResult> getPublicKey() {
        Request request = new Request().setPath(Constants.HTTPS_SERVER_URL + "api/getPublicKey")
                .setMethod(Request.RequestMethod.GET.value());
        return ExecutorRequest.execute(request);
    }

    @SuppressWarnings("unchecked")
    @NonNull
    public static Result<DeviceUUIDResult> postDeviceUUID(String deviceUUID) {
        HashMap<String, String> parameters = new HashMap<>();
        parameters.put("deviceId", RSAUtils.encryptByPublicKey(deviceUUID, Config.getPublicKey()));
        Request request = new Request().setPath(Constants.HTTPS_SERVER_URL + "api/deviceIdValidation")
                .setMethod(Request.RequestMethod.FORM_POST.value())
                .setBody(parameters);
        return ExecutorRequest.execute(request);
    }

    @SuppressWarnings("unchecked")
    @NonNull
    public static Result<RefreshTokenResult> refreshToken() {
        HashMap<String, String> parameters = new HashMap<>();
        parameters.put("accessToken", Config.getToken());
        parameters.put("refreshToken", Config.getRefreshToken());
        parameters.put("grant_type", "refresh_token");
        Request request = new Request().setPath(Constants.HTTPS_SERVER_URL + "api/refreshToken")
                .setMethod(Request.RequestMethod.FORM_POST.value())
                .setBody(parameters);
        return ExecutorRequest.execute(request);
    }

    @SuppressWarnings("unchecked")
    @NonNull
    public static Result<LoginResult> login(String username, String password, String deviceId) {
        HashMap<String, String> parameters = new HashMap<>();
        parameters.put("username", RSAUtils.encryptByPublicKey(username, Config.getPublicKey()));
        parameters.put("password", RSAUtils.encryptByPublicKey(password, Config.getPublicKey()));
        parameters.put("deviceId", RSAUtils.encryptByPublicKey(deviceId, Config.getPublicKey()));
        parameters.put("grant_type", "password");
        Request request = new Request().setPath(Constants.HTTPS_SERVER_URL + "api/login")
                .setMethod(Request.RequestMethod.FORM_POST.value())
                .setBody(parameters);
        return ExecutorRequest.execute(request);
    }

    @SuppressWarnings("unchecked")
    @NonNull
    public static Result<TasksResult> getTasks() {
        HashMap<String, String> headers = new HashMap<>();
        headers.put("Authorization", "bearer " + Config.getToken());
        Request request = new Request().setPath(Constants.HTTPS_SERVER_URL + "api/userId/" + Config.getUserId() + "/schedule/progress")
                .setHeaderMap(headers)
                .setMethod(Request.RequestMethod.GET.value());
        return ExecutorRequest.execute(request);
    }

    @SuppressWarnings("unchecked")
    @NonNull
    public static Result<DownloadResult> download(String folder, String file, String path) {
        HashMap<String, String> headers = new HashMap<>();
        headers.put("Authorization", "bearer " + Config.getToken());
        Pair<String, String> parameters = new Pair<>(folder, file);
        Request request = new Request().setPath(Constants.HTTPS_SERVER_URL + "api/" + path)
                .setHeaderMap(headers)
                .setMethod(Request.RequestMethod.DOWNLOAD.value())
                .setBody(parameters);
        return ExecutorRequest.execute(request);
    }

    @SuppressWarnings("unchecked")
    @NonNull
    public static Result<EnglishResult> getEnglish(int englishId) {
        HashMap<String, String> headers = new HashMap<>();
        headers.put("Authorization", "bearer " + Config.getToken());
        Request request = new Request().setPath(Constants.HTTPS_SERVER_URL + "api/english/" + englishId)
                .setHeaderMap(headers)
                .setMethod(Request.RequestMethod.GET.value());
        return ExecutorRequest.execute(request);
    }

    @SuppressWarnings("unchecked")
    @NonNull
    public static Result<UploadResult> upload(int englishId, int score) {
        HashMap<String, String> headers = new HashMap<>();
        headers.put("Authorization", "bearer " + Config.getToken());
        HashMap<String, String> parameters = new HashMap<>();
        parameters.put("userId", String.valueOf(Config.getUserId()));
        parameters.put("englishId", String.valueOf(englishId));
        parameters.put("grade", String.valueOf(score));
        Request request = new Request().setPath(Constants.HTTPS_SERVER_URL + "api/finishProgress")
                .setHeaderMap(headers)
                .setMethod(Request.RequestMethod.FORM_POST.value())
                .setBody(parameters);
        return ExecutorRequest.execute(request);
    }

    @SuppressWarnings("unchecked")
    @NonNull
    public static Result<MouthsResult> analysisMouth(int englishId, List<String> images) {
        HashMap<String, String> parameters = new HashMap<>();
        parameters.put("englishId", String.valueOf(englishId));
        parameters.put("images", JSON.toJSONString(images));
        Request request = new Request().setPath(Constants.ANALYSIS_SERVER_URL + "api/analysis")
                .setMethod(Request.RequestMethod.JSON_POST.value())
                .setBody(parameters);
        return ExecutorRequest.execute(request);
    }
}
