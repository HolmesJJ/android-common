package com.example.common.ui.activity;

import android.annotation.SuppressLint;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;

import com.example.common.BR;
import com.example.common.R;
import com.example.common.base.BaseActivity;
import com.example.common.databinding.ActivitySpeechBinding;
import com.example.common.listener.OnMultiClickListener;
import com.example.common.ui.viewmodel.SpeechViewModel;

import java.io.File;

public class SpeechActivity extends BaseActivity<ActivitySpeechBinding, SpeechViewModel> {

    private static final String TAG = SpeechActivity.class.getSimpleName();
    private int mEnglishId;

    @Override
    public int initContentView(Bundle savedInstanceState) {
        return R.layout.activity_speech;
    }

    @Override
    public int initVariableId() {
        return BR.viewModel;
    }

    @Override
    public Class<SpeechViewModel> getViewModelClazz() {
        return SpeechViewModel.class;
    }

    @Override
    public void initData() {
        super.initData();
        if (getIntent() != null && getIntent().getExtras() != null) {
            Bundle bundle = getIntent().getExtras();
            mEnglishId = bundle.getInt("englishId");
        }
    }

    @Override
    public void initViewObservable() {
        super.initViewObservable();
        doIsShowLoading();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    /**
     * 控制进度圈显示
     */
    public void doIsShowLoading() {
        if (getViewModel() == null) {
            return;
        }
        getViewModel().isShowLoading().observe(this, isShowing -> {
            if (isShowing) {
                showLoading(false);
            } else {
                stopLoading();
            }
        });
    }
}
