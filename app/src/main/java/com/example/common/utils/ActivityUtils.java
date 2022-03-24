package com.example.common.utils;

import android.app.Activity;
import android.content.Intent;
import android.os.Handler;

import com.example.common.ui.activity.LoginActivity;
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

    public static void redirectToLogin() {
        Activity context = AppManagerUtils.getAppManager().popActivity();
        if (context != null) {
            try {
                Intent intent = new Intent(context, LoginActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                context.startActivity(intent);
                context.finish();
            } catch (Exception e) {
                ToastUtils.showShortSafe("Please launch again");
            }
        }
    }
}
