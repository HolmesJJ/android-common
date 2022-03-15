package com.example.common;

import android.util.Log;
import androidx.annotation.NonNull;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.MutableLiveData;

import com.example.common.base.BaseViewModel;
import com.example.common.network.http.ResponseCode;
import com.example.common.network.http.Result;

public class MainViewModel extends BaseViewModel {

    private static final String TAG = MainViewModel.class.getSimpleName();

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

    /**
     * 处理网络请求结果的错误信息
     */
    private void doResultErrorMsg(Result result) {
        if (result.getCode() == ResponseCode.NETWORK_ERROR) {
            Log.e(TAG, "Network Error");
        } else if (result.getCode() == ResponseCode.NOT_FOUND) {
            Log.e(TAG, "Not Found");
        } else {
            Log.e(TAG, "Unknown Error");
        }
    }
}
