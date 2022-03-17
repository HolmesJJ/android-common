package com.example.common.api.model.main;

import androidx.annotation.NonNull;

public class DownloadParameter {

    private final String folder;
    private final String file;
    private final String path;

    public DownloadParameter(String folder, String file, String path) {
        this.folder = folder;
        this.file = file;
        this.path = path;
    }

    public String getFolder() {
        return folder;
    }

    public String getFile() {
        return file;
    }

    public String getPath() {
        return path;
    }

    @NonNull
    @Override
    public String toString() {
        return "DownloadParameters{" +
                "folder='" + folder + '\'' +
                ", file='" + file + '\'' +
                ", path='" + path + '\'' +
                '}';
    }
}
