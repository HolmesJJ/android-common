package com.example.common.ui.viewmodel;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.MutableLiveData;

import com.example.common.api.ApiClient;
import com.example.common.api.model.speech.UploadResult;
import com.example.common.base.BaseViewModel;
import com.example.common.constants.Constants;
import com.example.common.network.http.Result;
import com.example.common.thread.ThreadManager;
import com.example.common.utils.RefreshTokenUtils;
import com.example.common.utils.ToastUtils;

public class ResultViewModel extends BaseViewModel {

    private static final String TAG = ResultViewModel.class.getSimpleName();

    private final MutableLiveData<String> mSpeechData = new MutableLiveData<>();
    private final MutableLiveData<Boolean> mIsShowLoading = new MutableLiveData<>();

    @Override
    public void onCreate(@NonNull LifecycleOwner owner) {

    }

    @Override
    public void onDestroy(@NonNull LifecycleOwner owner) {

    }

    public MutableLiveData<String> getSpeechData() {
        return mSpeechData;
    }

    public MutableLiveData<Boolean> isShowLoading() {
        return mIsShowLoading;
    }

    public void uploadScore(int englishId, int score) {
        mIsShowLoading.postValue(true);
        ThreadManager.getThreadPollProxy().execute(new Runnable() {
            @Override
            public void run() {
                Result<UploadResult> uploadResult = ApiClient.upload(englishId, score);
                if (uploadResult.isTokenTimeout() || uploadResult.isForbidden()) {
                    RefreshTokenUtils.refreshToken();
                    mIsShowLoading.postValue(false);
                    uploadScore(englishId, score);
                    return;
                }
                Log.i(TAG, uploadResult.toString());
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
