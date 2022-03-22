package com.example.common.model.mouth;

import androidx.annotation.NonNull;

import java.util.Arrays;

public class Mouth {

    private final int id;
    private final byte[] data;
    private final int width;
    private final int height;
    private final float[] points;

    public Mouth(int id, byte[] data, int width, int height, float[] points) {
        this.id = id;
        this.data = data;
        this.width = width;
        this.height = height;
        this.points = points;
    }

    public int getId() {
        return id;
    }

    public byte[] getData() {
        return data;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public float[] getPoints() {
        return points;
    }

    public double getMaxVertDist() {
        // 6和7：嘴巴上边的坐标
        // 30和31：嘴巴下边的坐标
        // 放大10000倍
        return (points[6] * 10000 - points[30] * 10000) * (points[6] * 10000 - points[30] * 10000)
                + (points[7] * 10000 - points[31] * 10000) * (points[7] * 10000 - points[31] * 10000);
    }

    public double getMaxHorizDist() {
        // 0和1：嘴巴左边的坐标
        // 12和13：嘴巴右边的坐标
        // 放大10000倍
        return (points[0] * 10000 - points[12] * 10000) * (points[0] * 10000 - points[12] * 10000)
                + (points[1] * 10000 - points[13] * 10000) * (points[1] * 10000 - points[13] * 10000);
    }

    @NonNull
    @Override
    public String toString() {
        return "Mouth{" +
                "id=" + id +
                ", data=" + Arrays.toString(data) +
                ", width=" + width +
                ", height=" + height +
                ", points=" + Arrays.toString(points) +
                '}';
    }
}
