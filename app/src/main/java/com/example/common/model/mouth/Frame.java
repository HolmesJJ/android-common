package com.example.common.model.mouth;

import android.graphics.Bitmap;

import androidx.annotation.NonNull;

import java.util.List;

public class Frame {

    private final int id;
    private final Bitmap bitmap;

    private Bitmap markedBitmap = null;
    private String title = "";
    private String message = "";
    private boolean marked = false;

    public Frame(int id, Bitmap bitmap) {
        this.id = id;
        this.bitmap = bitmap;
    }

    public int getId() {
        return id;
    }

    public Bitmap getBitmap() {
        return bitmap;
    }

    public Bitmap getMarkedBitmap() {
        return markedBitmap;
    }

    public Frame setMarkedBitmap(Bitmap markedBitmap) {
        this.markedBitmap = markedBitmap;
        return this;
    }

    public String getTitle() {
        return title;
    }

    public Frame setTitle(String title) {
        this.title = title;
        return this;
    }

    public String getMessage() {
        return message;
    }

    public Frame setMessage(String message) {
        this.message = message;
        return this;
    }

    public boolean isMarked() {
        return marked;
    }

    public Frame setMarked(boolean marked) {
        this.marked = marked;
        return this;
    }

    @NonNull
    @Override
    public String toString() {
        return "Frame{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", bitmap=" + bitmap +
                ", markedBitmap=" + markedBitmap +
                ", message='" + message + '\'' +
                ", marked=" + marked +
                '}';
    }
}
