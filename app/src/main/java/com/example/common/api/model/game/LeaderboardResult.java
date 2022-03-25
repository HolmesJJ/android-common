package com.example.common.api.model.game;

import androidx.annotation.NonNull;

import java.util.List;

public class LeaderboardResult {

    private List<MemberResult> list;

    public LeaderboardResult() {
    }

    public List<MemberResult> getList() {
        return list;
    }

    public void setList(List<MemberResult> list) {
        this.list = list;
    }

    @NonNull
    @Override
    public String toString() {
        return "LeaderboardResult{" +
                "list=" + list +
                '}';
    }
}
