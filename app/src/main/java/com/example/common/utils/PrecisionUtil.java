package com.example.common.utils;

public class PrecisionUtil {

    /**
     * float的精度， 1: 10.2; 2: 10.21;...
     *
     * 10.2145 * 100 / 100 = 10.21
     */
    private static final int PRECISION = 1;

    public static String formTextByPrecision(float value) {
        return formTextByPrecision(PRECISION, value);
    }

    public static String formTextByPrecision(int precision, float value) {
        int key = (int) Math.pow(10, precision);
        return ((float) (Math.round(value*key)) / key) + "";
    }
}
