package com.example.common.utils;

import com.example.common.api.ApiClient;
import com.example.common.api.model.token.RefreshTokenResult;
import com.example.common.config.Config;
import com.example.common.network.http.Result;

public final class RefreshTokenUtils {

    private final static int MAX_COUNT = 3;
    private static int count = 0;

    public RefreshTokenUtils() {
    }

    public static void refreshToken() {
        if (count == MAX_COUNT) {
            count = 0;
            ToastUtils.showShortSafe("Refresh Token Failed");
            Config.resetConfig();
            ActivityUtils.redirectToLogin();
        }
        count++;
        Result<RefreshTokenResult> refreshTokenResult = ApiClient.refreshToken();
        if (refreshTokenResult.isSuccess()) {
            count = 0;
            RefreshTokenResult body = refreshTokenResult.getBody(RefreshTokenResult.class);
            Config.setToken(body.getToken());
        }
    }
}
