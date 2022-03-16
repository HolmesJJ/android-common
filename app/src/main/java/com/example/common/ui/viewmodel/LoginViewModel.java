package com.example.common.ui.viewmodel;

import androidx.annotation.NonNull;
import androidx.databinding.ObservableBoolean;
import androidx.databinding.ObservableField;
import androidx.databinding.ViewDataBinding;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.MutableLiveData;

import com.example.common.MainActivity;
import com.example.common.base.BaseActivity;
import com.example.common.base.BaseViewModel;
import com.example.common.config.Config;

public class LoginViewModel extends BaseViewModel {

    private final MutableLiveData<Class<? extends BaseActivity<? extends ViewDataBinding, ? extends BaseViewModel>>> mActivityAction = new MutableLiveData<>();
    private final ObservableField<String> mUsername = new ObservableField<>();
    private final ObservableField<String> mPassword = new ObservableField<>();
    private final ObservableBoolean mEnableSignIn = new ObservableBoolean();
    private final MutableLiveData<Boolean> mIsShowLoading = new MutableLiveData<>();

    @Override
    public void onCreate(@NonNull LifecycleOwner owner) {

    }

    @Override
    public void onDestroy(@NonNull LifecycleOwner owner) {

    }

    public MutableLiveData<Class<? extends BaseActivity<? extends ViewDataBinding, ? extends BaseViewModel>>> getActivityAction() {
        return mActivityAction;
    }

    public ObservableField<String> getUsername() {
        return mUsername;
    }

    public ObservableField<String> getPassword() {
        return mPassword;
    }

    public ObservableBoolean getEnableSignIn() {
        return mEnableSignIn;
    }

    public MutableLiveData<Boolean> isShowLoading() {
        return mIsShowLoading;
    }

    public void signIn() {
        // 不允许登录
        if (!mEnableSignIn.get()) {
            return;
        }
        mIsShowLoading.postValue(true);
        Config.setLogin(true);
        Config.setUserId(1);
        mActivityAction.postValue(MainActivity.class);
    }

    public void updateSignInBtnState() {
        // 当账号或者密码为空的时候不允许登录
        String username = mUsername.get();
        String password = mPassword.get();
        mEnableSignIn.set(username != null && !"".equals(username) && password != null && !"".equals(password));
    }
}
