package com.example.common.model.game;

import androidx.annotation.NonNull;

public class Ipa {

    private int ipaId;
    private String ipaContent;

    public Ipa(int ipaId, String ipaContent) {
        this.ipaId = ipaId;
        this.ipaContent = ipaContent;
    }

    public int getIpaId() {
        return ipaId;
    }

    public void setIpaId(int ipaId) {
        this.ipaId = ipaId;
    }

    public String getIpaContent() {
        return ipaContent;
    }

    public void setIpaContent(String ipaContent) {
        this.ipaContent = ipaContent;
    }

    @NonNull
    @Override
    public String toString() {
        return "Ipa{" +
                "ipaId=" + ipaId +
                ", ipaContent='" + ipaContent + '\'' +
                '}';
    }
}
