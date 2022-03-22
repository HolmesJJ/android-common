package com.example.common.model.mouth;

import androidx.annotation.NonNull;

import java.util.List;

public class Mouths {

    private final int englishId;
    private final List<Frame> frames;
    private final List<Frame> markedFrames;

    public Mouths(int englishId, List<Frame> frames, List<Frame> markedFrames) {
        this.englishId = englishId;
        this.frames = frames;
        this.markedFrames = markedFrames;
    }

    public int getEnglishId() {
        return englishId;
    }

    public List<Frame> getFrames() {
        return frames;
    }

    public List<Frame> getMarkedFrames() {
        return markedFrames;
    }

    @NonNull
    @Override
    public String toString() {
        return "Result{" +
                "englishId=" + englishId +
                ", frames=" + frames +
                ", markedFrames=" + markedFrames +
                '}';
    }
}
