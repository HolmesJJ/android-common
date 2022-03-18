package com.example.common.utils;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.WindowManager;

public final class DensityUtils {

    private DensityUtils() {
    }

    public static float getScreenDensity() {
        return ContextUtils.getContext().getResources().getDisplayMetrics().density;
    }

    public static float getScreenScaleDensity() {
        return ContextUtils.getContext().getResources().getDisplayMetrics().scaledDensity;
    }

    public static int getScreenWidth() {
        WindowManager wm = (WindowManager) ContextUtils.getContext().getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics outMetrics = new DisplayMetrics();
        wm.getDefaultDisplay().getMetrics(outMetrics);
        return outMetrics.widthPixels;
    }

    public static int getScreenHeight() {
        WindowManager wm = (WindowManager) ContextUtils.getContext().getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics outMetrics = new DisplayMetrics();
        wm.getDefaultDisplay().getMetrics(outMetrics);
        return outMetrics.heightPixels;
    }

    public static int dip2px(float dpValue) {
        float scale = getScreenDensity();
        return (int) (dpValue * scale + 0.5F);
    }

    public static int px2dip(float pxValue) {
        float scale = getScreenDensity();
        return (int) (pxValue / scale + 0.5F);
    }

    public static int px2sp(float pxValue) {
        float fontScale = getScreenScaleDensity();
        return (int) (pxValue / fontScale + 0.5F);
    }

    public static int sp2px(float spValue) {
        float fontScale = getScreenScaleDensity();
        return (int) (spValue * fontScale + 0.5F);
    }

    /**
     * 获取当前屏幕截图，包含状态栏
     */
    public static Bitmap captureWithStatusBar(Activity activity) {
        View view = activity.getWindow().getDecorView();
        view.setDrawingCacheEnabled(true);
        view.buildDrawingCache();
        Bitmap bmp = view.getDrawingCache();
        DisplayMetrics dm = new DisplayMetrics();
        activity.getWindowManager().getDefaultDisplay().getMetrics(dm);
        Bitmap ret = Bitmap.createBitmap(bmp, 0, 0, dm.widthPixels, dm.heightPixels);
        view.destroyDrawingCache();
        return ret;
    }

    /**
     * 获取当前屏幕截图，不包含状态栏
     *
     * @param activity activity
     * @return Bitmap
     */
    public static Bitmap captureWithoutStatusBar(Activity activity) {
        View view = activity.getWindow().getDecorView();
        view.setDrawingCacheEnabled(true);
        view.buildDrawingCache();
        Bitmap bmp = view.getDrawingCache();
        int statusBarHeight = getStatusHeight(activity);
        DisplayMetrics dm = new DisplayMetrics();
        activity.getWindowManager().getDefaultDisplay().getMetrics(dm);
        Bitmap ret = Bitmap.createBitmap(bmp, 0, statusBarHeight, dm.widthPixels, dm.heightPixels - statusBarHeight);
        view.destroyDrawingCache();
        return ret;
    }

    /**
     * 获得状态栏的高度
     */
    public static int getStatusHeight(Context context) {
        return getInternalDimensionSize(context.getResources(), "status_bar_height");
    }

    private static int getInternalDimensionSize(Resources res, String key) {
        int result = 0;
        int resourceId = res.getIdentifier(key, "dimen", "android");
        if (resourceId > 0) {
            result = res.getDimensionPixelSize(resourceId);
        }
        return result;
    }
}
