package com.example.common.ui.activity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.example.common.BR;
import com.example.common.R;
import com.example.common.base.BaseActivity;
import com.example.common.databinding.ActivitySectionBinding;
import com.example.common.listener.OnMultiClickListener;
import com.example.common.ui.viewmodel.SectionViewModel;
import com.example.common.utils.ListenerUtils;
import com.example.common.utils.ToastUtils;

public class SectionActivity extends BaseActivity<ActivitySectionBinding, SectionViewModel> {

    private static final String TAG = SectionActivity.class.getSimpleName();

    @Override
    public int initContentView(Bundle savedInstanceState) {
        return R.layout.activity_section;
    }

    @Override
    public int initVariableId() {
        return BR.viewModel;
    }

    @Override
    public Class<SectionViewModel> getViewModelClazz() {
        return SectionViewModel.class;
    }

    @Override
    public void initData() {
        super.initData();
    }

    @Override
    public void initViewObservable() {
        super.initViewObservable();
        setObserveListener();
        setOnClickListener();
        doIsShowLoading();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    private void setObserveListener() {
    }

    private void setOnClickListener() {
        ListenerUtils.setOnClickListener(getBinding().svTutorial, new OnMultiClickListener() {
            @Override
            public void onMultiClick(View v) {

            }
        });
        ListenerUtils.setOnClickListener(getBinding().svInteraction, new OnMultiClickListener() {
            @Override
            public void onMultiClick(View v) {
                String packageName = "xiao.haung.ren.cc.com";
                Intent launchIntent = getPackageManager().getLaunchIntentForPackage(packageName);
                if (launchIntent != null) {
                    startActivity(launchIntent);
                } else {
                    ToastUtils.showShortSafe("Launch Failed");
                }
            }
        });
        ListenerUtils.setOnClickListener(getBinding().svPronunciation, new OnMultiClickListener() {
            @Override
            public void onMultiClick(View v) {

            }
        });
        ListenerUtils.setOnClickListener(getBinding().svMouthShape, new OnMultiClickListener() {
            @Override
            public void onMultiClick(View v) {

            }
        });
    }

    /**
     * 控制进度圈显示
     */
    public void doIsShowLoading() {
        getViewModel().isShowLoading().observe(this, isShowing -> {
            if (isShowing) {
                showLoading(false);
            } else {
                stopLoading();
            }
        });
    }
}
