package com.example.common.base;

import android.app.Application;

import com.example.common.api.LinkHelper;
import com.example.common.utils.ContextUtils;
import com.example.common.utils.FileUtils;
import com.example.common.utils.SystemUtils;

public class BaseApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        // 多进程会调用多次onCreate方法，所以需要判断是否是主进程
        if (SystemUtils.isAppMainProcess(this)) {
            ContextUtils.init(this);
            FileUtils.init();
            LinkHelper.getInstance().setConnectTimeout(10000).setReadTimeout(30000).setWriteTimeout(10000).setDebug(true);
        }
    }
}
