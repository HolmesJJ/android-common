package com.example.common.api.model.mouth;

import androidx.annotation.NonNull;

public class MouthResult {

    private int id;
    private String message;

    public MouthResult() {
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    @NonNull
    @Override
    public String toString() {
        return "MouthResult{" +
                "id=" + id +
                ", message='" + message + '\'' +
                '}';
    }
}
