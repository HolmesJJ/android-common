package com.example.common.ui.viewmodel;

import androidx.annotation.NonNull;
import androidx.databinding.ViewDataBinding;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.MutableLiveData;

import com.example.common.base.BaseActivity;
import com.example.common.base.BaseViewModel;

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
}
