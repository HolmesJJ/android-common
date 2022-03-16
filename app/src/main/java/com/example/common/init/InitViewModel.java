package com.example.common.init;

import androidx.annotation.NonNull;
import androidx.databinding.ViewDataBinding;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.MutableLiveData;

import com.example.common.MainActivity;
import com.example.common.api.ApiClient;
import com.example.common.api.model.login.DeviceUUIDResult;
import com.example.common.api.model.login.PublicKeyResult;
import com.example.common.base.BaseActivity;
import com.example.common.base.BaseViewModel;
import com.example.common.config.Config;
import com.example.common.constants.Constants;
import com.example.common.network.http.Result;
import com.example.common.thread.ThreadManager;
import com.example.common.ui.activity.LoginActivity;
import com.example.common.utils.ToastUtils;

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
        ThreadManager.getThreadPollProxy().execute(new Runnable() {
            @Override
            public void run() {
                Result<PublicKeyResult> publicKeyResult = ApiClient.getPublicKey();
                if (!publicKeyResult.isSuccess()) {
                    mIsInitSuccess.postValue(false);
                    ToastUtils.showShortSafe("Public Key is Invalid");
                    return;
                }
                PublicKeyResult publicKeyResultBody = publicKeyResult.getBody(PublicKeyResult.class);
                if (!publicKeyResultBody.getStatus().equals(Constants.SUCCESS)) {
                    mIsInitSuccess.postValue(false);
                    ToastUtils.showShortSafe("Public Key is Invalid");
                    return;
                }
                Config.setPublicKey(publicKeyResultBody.getPublicKey());
                Result<DeviceUUIDResult> deviceUUIDResult = ApiClient.postDeviceUUID(Config.getDeviceUUID());
                DeviceUUIDResult deviceUUIDResultBody = deviceUUIDResult.getBody(DeviceUUIDResult.class);
                if (deviceUUIDResultBody.getStatus().equals(Constants.SUCCESS)) {
                    Config.setLogin(false);
                }
                doInitSuccess();
            }
        });
    }

    private void doInitSuccess() {
        if (Config.isLogin()) {
            mActivityAction.postValue(MainActivity.class);
        } else {
            mActivityAction.postValue(LoginActivity.class);
        }
    }
}
