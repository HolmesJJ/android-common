package com.example.common.ui.viewmodel;

import androidx.annotation.NonNull;
import androidx.databinding.ObservableBoolean;
import androidx.databinding.ObservableField;
import androidx.databinding.ViewDataBinding;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.MutableLiveData;

import com.example.common.MainActivity;
import com.example.common.api.ApiClient;
import com.example.common.api.model.login.LoginResult;
import com.example.common.base.BaseActivity;
import com.example.common.base.BaseViewModel;
import com.example.common.config.Config;
import com.example.common.constants.Constants;
import com.example.common.network.http.Result;
import com.example.common.thread.ThreadManager;
import com.example.common.utils.ToastUtils;

public class SectionViewModel extends BaseViewModel {

    private final MutableLiveData<Class<? extends BaseActivity<? extends ViewDataBinding, ? extends BaseViewModel>>> mActivityAction = new MutableLiveData<>();
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

    public MutableLiveData<Boolean> isShowLoading() {
        return mIsShowLoading;
    }

}
