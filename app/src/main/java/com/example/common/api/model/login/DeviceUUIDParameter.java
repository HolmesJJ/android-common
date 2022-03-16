package com.example.common.api.model.login;

import androidx.annotation.NonNull;

public class DeviceUUIDParameter {

    private String deviceId;

    public DeviceUUIDParameter() {
    }

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    @NonNull
    @Override
    public String toString() {
        return "DeviceUUIDParameter{" +
                "deviceId='" + deviceId + '\'' +
                '}';
    }
}
