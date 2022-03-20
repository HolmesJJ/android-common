package com.example.common.ui.viewmodel;

import androidx.annotation.NonNull;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.MutableLiveData;

import com.example.common.api.ApiClient;
import com.example.common.api.model.tutorial.EnglishResult;
import com.example.common.base.BaseViewModel;
import com.example.common.network.http.Result;
import com.example.common.thread.ThreadManager;
import com.example.common.utils.RefreshTokenUtils;
import com.example.common.utils.ToastUtils;

public class SpeechViewModel extends BaseViewModel {

    private final MutableLiveData<String> mFrameName = new MutableLiveData<>();
    private final MutableLiveData<Boolean> mIsShowLoading = new MutableLiveData<>();

    @Override
    public void onCreate(@NonNull LifecycleOwner owner) {

    }

    @Override
    public void onDestroy(@NonNull LifecycleOwner owner) {

    }

    public MutableLiveData<String> getFrameName() {
        return mFrameName;
    }

    public MutableLiveData<Boolean> isShowLoading() {
        return mIsShowLoading;
    }
}
