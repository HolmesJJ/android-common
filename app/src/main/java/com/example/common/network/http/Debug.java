package com.example.common.network.http;

public final class Debug {

    private static boolean mDebug = true;

    private Debug() {
    }

    public static boolean isDebug() {
        return mDebug;
    }

    public static void setDebug(boolean mDebug) {
        Debug.mDebug = mDebug;
    }
}
