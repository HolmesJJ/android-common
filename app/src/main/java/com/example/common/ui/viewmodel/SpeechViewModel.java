package com.example.common.ui.viewmodel;

import androidx.annotation.NonNull;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.MutableLiveData;

import com.example.common.base.BaseViewModel;
import com.example.common.config.Config;
import com.example.common.network.http.OkHttpHelper;
import com.example.common.utils.ToastUtils;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;

import okhttp3.Call;
import okhttp3.Response;
import okhttp3.ResponseBody;

public class SpeechViewModel extends BaseViewModel implements OkHttpHelper.IOkHttpTaskCompleted {

    private final MutableLiveData<String> mFrameName = new MutableLiveData<>();
    private final MutableLiveData<String> mSpeechData = new MutableLiveData<>();
    private final MutableLiveData<Boolean> mIsShowLoading = new MutableLiveData<>();

    @Override
    public void onCreate(@NonNull LifecycleOwner owner) {

    }

    @Override
    public void onDestroy(@NonNull LifecycleOwner owner) {

    }

    @Override
    public void onFailure(@NonNull Call call, @NonNull IOException e) {
        mIsShowLoading.postValue(false);
        e.printStackTrace();
        ToastUtils.showShortSafe("Speech Analysis Failed");
    }

    @Override
    public void onResponse(@NonNull Call call, @NonNull Response response) {
        mIsShowLoading.postValue(false);
        ResponseBody responseBody = response.body();
        if (responseBody == null) {
            ToastUtils.showShortSafe("Speech Analysis Failed");
            return;
        }
        try {
            String result = responseBody.string();
            mSpeechData.postValue(result);
        } catch (Exception e) {
            e.printStackTrace();
            ToastUtils.showShortSafe("Speech Analysis Failed");
        }
    }

    public MutableLiveData<String> getFrameName() {
        return mFrameName;
    }

    public MutableLiveData<String> getSpeechData() {
        return mSpeechData;
    }

    public MutableLiveData<Boolean> isShowLoading() {
        return mIsShowLoading;
    }

    public void uploadAudio(File audioFile, int englishId) {
        if (audioFile == null || !audioFile.exists() || !audioFile.isFile()) {
            return;
        }
        mIsShowLoading.postValue(true);
        HashMap<String, String> parameters = new HashMap<>();
        parameters.put("userId", String.valueOf(Config.getUserId()));
        parameters.put("englishId", String.valueOf(englishId));
        OkHttpHelper okHttpHelper = new OkHttpHelper(this);
        okHttpHelper.sendRequest(parameters, "audioFile", audioFile);
    }
}
