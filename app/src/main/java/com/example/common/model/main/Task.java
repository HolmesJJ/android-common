package com.example.common.model.main;

public class Task {

    private final int englishId;
    private final String content;
    private final boolean isFinish;

    public Task(int englishId, String content, boolean isFinish) {
        this.englishId = englishId;
        this.content = content;
        this.isFinish = isFinish;
    }

    public int getEnglishId() {
        return englishId;
    }

    public String getContent() {
        return content;
    }

    public boolean isFinish() {
        return isFinish;
    }
}
