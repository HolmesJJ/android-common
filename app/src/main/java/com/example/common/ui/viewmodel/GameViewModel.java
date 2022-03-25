package com.example.common.ui.viewmodel;

import androidx.annotation.NonNull;
import androidx.databinding.ViewDataBinding;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.MutableLiveData;

import com.example.common.api.ApiClient;
import com.example.common.api.model.speech.UploadResult;
import com.example.common.base.BaseActivity;
import com.example.common.base.BaseViewModel;
import com.example.common.constants.Constants;
import com.example.common.network.http.Result;
import com.example.common.thread.ThreadManager;
import com.example.common.utils.RefreshTokenUtils;
import com.example.common.utils.ToastUtils;

public class GameViewModel extends BaseViewModel {

    private final MutableLiveData<Boolean> mIsShowLoading = new MutableLiveData<>();

    @Override
    public void onCreate(@NonNull LifecycleOwner owner) {

    }

    @Override
    public void onDestroy(@NonNull LifecycleOwner owner) {

    }

    public MutableLiveData<Boolean> isShowLoading() {
        return mIsShowLoading;
    }

    public void uploadScore(int score) {
        mIsShowLoading.postValue(true);
        ThreadManager.getThreadPollProxy().execute(new Runnable() {
            @Override
            public void run() {
                Result<UploadResult> uploadResult = ApiClient.upload(score);
                if (uploadResult.isTokenTimeout() || uploadResult.isForbidden()) {
                    RefreshTokenUtils.refreshToken();
                    mIsShowLoading.postValue(false);
                    uploadScore(score);
                    return;
                }
                if (!uploadResult.isSuccess()) {
                    mIsShowLoading.postValue(false);
                    ToastUtils.showShortSafe("Please upload score again");
                    return;
                }
                UploadResult uploadResultBody = uploadResult.getBody(UploadResult.class);
                mIsShowLoading.postValue(false);
                if (!uploadResultBody.getStatus().equals(Constants.SUCCESS)) {
                    ToastUtils.showShortSafe("Please upload score again");
                }
            }
        });
    }
}
