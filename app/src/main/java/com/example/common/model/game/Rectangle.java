package com.example.common.model.game;

import androidx.annotation.NonNull;

public class Rectangle {

    private float width; // 宽度
    private float minX;
    private float minY;
    private float maxX;
    private float maxY;
    private float area; // 相交面积

    public Rectangle(float width, float minX, float minY) {
        this.width = width;
        this.minX = minX;
        this.minY = minY;
        this.maxX = width + minX;
        this.maxY = width + minY;
    }

    public float getWidth() {
        return width;
    }

    public void setWidth(float width) {
        this.width = width;
    }

    public float getMinX() {
        return minX;
    }

    public void setMinX(float minX) {
        this.minX = minX;
    }

    public float getMinY() {
        return minY;
    }

    public void setMinY(float minY) {
        this.minY = minY;
    }

    public float getMaxX() {
        return maxX;
    }

    public void setMaxX(float maxX) {
        this.maxX = maxX;
    }

    public float getMaxY() {
        return maxY;
    }

    public void setMaxY(float maxY) {
        this.maxY = maxY;
    }

    public float getArea() {
        return area;
    }

    public void setArea(float area) {
        this.area = area;
    }

    /**
     * 求两个矩形的相交面积
     */
    public float calculatedArea(Rectangle other) {
        area = 0;
        if (other.minX > maxX || other.maxX < minX || other.maxY < minY || other.minY > maxY) {
            return area;
        }
        float minx = Math.max(minX, other.getMinX());
        float miny = Math.max(minY, other.getMinY());
        float maxx = Math.max(maxX, other.getMaxX());
        float maxy = Math.max(maxY, other.getMaxY());
        area = Math.abs(minx - maxx) * Math.abs(miny - maxy);
        return area;
    }

    @NonNull
    @Override
    public String toString() {
        return "Rectangle{" +
                "width=" + width +
                ", minX=" + minX +
                ", minY=" + minY +
                ", maxX=" + maxX +
                ", maxY=" + maxY +
                ", area=" + area +
                '}';
    }
}
