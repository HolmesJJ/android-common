package com.example.common.utils;

import android.app.Activity;
import android.os.Handler;

import com.example.common.ui.widget.dialog.LoadingDialog;

public final class ActivityUtils {

    private ActivityUtils() {
    }

    public static void appExitDelayed(long milliseconds) {
        Activity context = AppManagerUtils.getAppManager().popActivity();
        Handler handler = new Handler(ContextUtils.getContext().getMainLooper());
        if (context != null) {
            final LoadingDialog loadingDialog = new LoadingDialog(context);
            loadingDialog.showLoading();
            AppManagerUtils.getAppManager().callbackExitCallbacks();
            // 提交退出信息
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    loadingDialog.dismiss();
                    AppManagerUtils.getAppManager().appExit();
                }
            }, milliseconds);
        } else {
            AppManagerUtils.getAppManager().callbackExitCallbacks();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    AppManagerUtils.getAppManager().appExit();
                }
            }, milliseconds);
        }
    }
}
