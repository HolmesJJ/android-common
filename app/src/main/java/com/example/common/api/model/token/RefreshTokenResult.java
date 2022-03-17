package com.example.common.api.model.token;

import androidx.annotation.NonNull;

public class RefreshTokenResult {

    private String status;
    private String token;
    private String expiration;

    public RefreshTokenResult() {
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getExpiration() {
        return expiration;
    }

    public void setExpiration(String expiration) {
        this.expiration = expiration;
    }

    @NonNull
    @Override
    public String toString() {
        return "RefreshTokenResult{" +
                "status='" + status + '\'' +
                ", token='" + token + '\'' +
                ", expiration='" + expiration + '\'' +
                '}';
    }
}
