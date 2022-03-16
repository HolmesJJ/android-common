package com.example.common.api.model.login;

import androidx.annotation.NonNull;

public class DeviceUUIDResult {

    private String status;

    public DeviceUUIDResult() {
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    @NonNull
    @Override
    public String toString() {
        return "DeviceUUIDResult{" +
                "status='" + status + '\'' +
                '}';
    }
}
