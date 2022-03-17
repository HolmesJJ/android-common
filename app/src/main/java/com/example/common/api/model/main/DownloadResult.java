package com.example.common.api.model.main;

import androidx.annotation.NonNull;

public class DownloadResult {

    private String status;

    public DownloadResult() {
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
        return "DownloadResult{" +
                "status='" + status + '\'' +
                '}';
    }
}
