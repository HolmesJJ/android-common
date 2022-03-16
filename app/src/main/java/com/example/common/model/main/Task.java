package com.example.common.model.main;

public class Task {

    private final int taskId;
    private final String content;
    private final boolean isFinish;

    public Task(int taskId, String content, boolean isFinish) {
        this.taskId = taskId;
        this.content = content;
        this.isFinish = isFinish;
    }

    public int getTaskId() {
        return taskId;
    }

    public String getContent() {
        return content;
    }

    public boolean isFinish() {
        return isFinish;
    }
}
