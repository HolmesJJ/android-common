package com.example.common.api.model.main;

import androidx.annotation.NonNull;

import java.util.List;

public class TasksResult {

    private List<TaskResult> list;

    public TasksResult() {
    }

    public List<TaskResult> getList() {
        return list;
    }

    public void setList(List<TaskResult> list) {
        this.list = list;
    }

    @NonNull
    @Override
    public String toString() {
        return "TasksResult{" +
                "list=" + list +
                '}';
    }
}
