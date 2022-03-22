package com.example.common.api.model.mouth;

import androidx.annotation.NonNull;

import java.util.List;

public class MouthsResult {

    private List<MouthResult> list;

    public MouthsResult() {
    }

    public List<MouthResult> getList() {
        return list;
    }

    public void setList(List<MouthResult> list) {
        this.list = list;
    }

    @NonNull
    @Override
    public String toString() {
        return "MouthsResult{" +
                "list=" + list +
                '}';
    }
}
