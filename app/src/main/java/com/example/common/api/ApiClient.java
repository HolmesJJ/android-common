package com.example.common.api;

import androidx.annotation.NonNull;

import com.example.common.api.model.login.DeviceUUIDParameter;
import com.example.common.api.model.login.DeviceUUIDResult;
import com.example.common.api.model.login.LoginResult;
import com.example.common.api.model.login.PublicKeyResult;
import com.example.common.config.Config;
import com.example.common.constants.Constants;
import com.example.common.network.http.Request;
import com.example.common.network.http.Result;
import com.example.common.utils.RSAUtils;

import java.net.URLEncoder;
import java.util.HashMap;

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
                .setMethod(Request.RequestMethod.POST.value())
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
                .setMethod(Request.RequestMethod.POST.value())
                .setBody(parameters);
        return ExecutorRequest.execute(request);
    }
}
