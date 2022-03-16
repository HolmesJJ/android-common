package com.example.common.init;

import androidx.annotation.NonNull;
import androidx.databinding.ViewDataBinding;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.MutableLiveData;

import com.example.common.MainActivity;
import com.example.common.base.BaseActivity;
import com.example.common.base.BaseViewModel;
import com.example.common.config.Config;
import com.example.common.thread.ThreadManager;
import com.example.common.ui.activity.LoginActivity;

public class InitViewModel extends BaseViewModel {

    private static final String TAG = InitViewModel.class.getSimpleName();

    private final MutableLiveData<Class<? extends BaseActivity<? extends ViewDataBinding, ? extends BaseViewModel>>> mActivityAction = new MutableLiveData<>();
    private final MutableLiveData<Boolean> mIsInitSuccess = new MutableLiveData<>();

    @Override
    public void onCreate(@NonNull LifecycleOwner owner) {

    }

    @Override
    public void onDestroy(@NonNull LifecycleOwner owner) {

    }

    public MutableLiveData<Class<? extends BaseActivity<? extends ViewDataBinding, ? extends BaseViewModel>>> getActivityAction() {
        return mActivityAction;
    }

    public MutableLiveData<Boolean> isInitSuccess() {
        return mIsInitSuccess;
    }

    public void initData() {
        doInitSuccess();
    }

    private void doInitSuccess() {
        ThreadManager.getThreadPollProxy().execute(new Runnable() {
            @Override
            public void run() {
                if (Config.isLogin()) {
                    mActivityAction.postValue(MainActivity.class);
                } else {
                    mActivityAction.postValue(LoginActivity.class);
                }
            }
        });
    }
}
