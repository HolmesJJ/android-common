package com.example.common.api.model.login;

import androidx.annotation.NonNull;

public class PublicKeyResult {

    private String status;
    private String publicKey;

    public PublicKeyResult() {
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getPublicKey() {
        return publicKey;
    }

    public void setPublicKey(String publicKey) {
        this.publicKey = publicKey;
    }

    @NonNull
    @Override
    public String toString() {
        return "PublicKeyResult{" +
                "status='" + status + '\'' +
                ", publicKey='" + publicKey + '\'' +
                '}';
    }
}
