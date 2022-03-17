package com.example.common.api.model.main;

import androidx.annotation.NonNull;

public class TaskResult {

    private int scheduleId;
    private String startDate;
    private String endDate;
    private int englishId;
    private String content;
    private int progressTimes;

    public TaskResult() {
    }

    public int getScheduleId() {
        return scheduleId;
    }

    public void setScheduleId(int scheduleId) {
        this.scheduleId = scheduleId;
    }

    public String getStartDate() {
        return startDate;
    }

    public void setStartDate(String startDate) {
        this.startDate = startDate;
    }

    public String getEndDate() {
        return endDate;
    }

    public void setEndDate(String endDate) {
        this.endDate = endDate;
    }

    public int getEnglishId() {
        return englishId;
    }

    public void setEnglishId(int englishId) {
        this.englishId = englishId;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public int getProgressTimes() {
        return progressTimes;
    }

    public void setProgressTimes(int progressTimes) {
        this.progressTimes = progressTimes;
    }

    @NonNull
    @Override
    public String toString() {
        return "TaskResult{" +
                "scheduleId=" + scheduleId +
                ", startDate='" + startDate + '\'' +
                ", endDate='" + endDate + '\'' +
                ", englishId=" + englishId +
                ", content='" + content + '\'' +
                ", progressTimes=" + progressTimes +
                '}';
    }
}
