package com.example.common.api.model.game;

import androidx.annotation.NonNull;

public class MemberResult {

    private int rank;
    private String name;
    private int score;

    public MemberResult() {
    }

    public int getRank() {
        return rank;
    }

    public void setRank(int rank) {
        this.rank = rank;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }

    @NonNull
    @Override
    public String toString() {
        return "MemberResult{" +
                "rank=" + rank +
                ", name='" + name + '\'' +
                ", score=" + score +
                '}';
    }
}
