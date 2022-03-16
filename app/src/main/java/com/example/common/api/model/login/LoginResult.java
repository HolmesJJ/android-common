package com.example.common.api.model.login;

import androidx.annotation.NonNull;

public class LoginResult {

    private String status;
    private int userId;
    private String token;
    private String refreshToken;
    private String error;

    public LoginResult() {
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

    @NonNull
    @Override
    public String toString() {
        return "LoginResult{" +
                "status='" + status + '\'' +
                ", userId=" + userId +
                ", token='" + token + '\'' +
                ", refreshToken='" + refreshToken + '\'' +
                ", error='" + error + '\'' +
                '}';
    }
}
