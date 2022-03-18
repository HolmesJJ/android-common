package com.example.common.ui.viewmodel;

import androidx.annotation.NonNull;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.MutableLiveData;

import com.example.common.api.ApiClient;
import com.example.common.api.model.tutorial.EnglishResult;
import com.example.common.base.BaseViewModel;
import com.example.common.network.http.Result;
import com.example.common.thread.ThreadManager;
import com.example.common.utils.ToastUtils;

public class TutorialViewModel extends BaseViewModel {

    private final MutableLiveData<String> mDetail = new MutableLiveData<>();
    private final MutableLiveData<Boolean> mIsShowLoading = new MutableLiveData<>();

    @Override
    public void onCreate(@NonNull LifecycleOwner owner) {

    }

    @Override
    public void onDestroy(@NonNull LifecycleOwner owner) {

    }

    public MutableLiveData<String> getDetail() {
        return mDetail;
    }

    public MutableLiveData<Boolean> isShowLoading() {
        return mIsShowLoading;
    }

    public void initData(int englishId) {
        mIsShowLoading.postValue(true);
        ThreadManager.getThreadPollProxy().execute(new Runnable() {
            @Override
            public void run() {
                Result<EnglishResult> englishResult = ApiClient.getEnglish(englishId);
                if (!englishResult.isSuccess()) {
                    mIsShowLoading.postValue(false);
                    ToastUtils.showShortSafe("Get English Failed");
                    return;
                }
                EnglishResult englishResultBody = englishResult.getBody(EnglishResult.class);
                mIsShowLoading.postValue(false);
                mDetail.postValue(englishResultBody.getDetail());
            }
        });
    }
}
