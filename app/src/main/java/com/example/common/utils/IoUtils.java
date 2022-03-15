package com.example.common.utils;

import java.io.Closeable;
import java.io.IOException;

public final class IoUtils {

    private IoUtils() {
    }

    public static void closeQuietly(final Closeable... closeables) {
        if (closeables == null) {
            return;
        }
        for (Closeable closeable : closeables) {
            if (closeable != null) {
                try {
                    closeable.close();
                } catch (IOException ignored) {
                }
            }
        }
    }
}
