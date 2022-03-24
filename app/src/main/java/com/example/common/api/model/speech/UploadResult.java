package com.example.common.api.model.speech;

import androidx.annotation.NonNull;

public class UploadResult {

    private String status;

    public UploadResult() {
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
        return "UploadResult{" +
                "status='" + status + '\'' +
                '}';
    }
}
