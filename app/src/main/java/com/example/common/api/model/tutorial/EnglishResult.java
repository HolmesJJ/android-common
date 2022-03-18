package com.example.common.api.model.tutorial;

import androidx.annotation.NonNull;

public class EnglishResult {

    private int id;
    private String content;
    private String detail;
    private String type;

    public EnglishResult() {
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getDetail() {
        return detail;
    }

    public void setDetail(String detail) {
        this.detail = detail;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    @NonNull
    @Override
    public String toString() {
        return "EnglishResult{" +
                "id=" + id +
                ", content='" + content + '\'' +
                ", detail='" + detail + '\'' +
                ", type='" + type + '\'' +
                '}';
    }
}
