package com.example.common.ui.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.example.common.BR;
import com.example.common.R;
import com.example.common.base.BaseActivity;
import com.example.common.databinding.ActivityMenuBinding;
import com.example.common.listener.OnMultiClickListener;
import com.example.common.ui.viewmodel.MenuViewModel;
import com.example.common.utils.ListenerUtils;
import com.example.common.utils.ToastUtils;

public class MenuActivity extends BaseActivity<ActivityMenuBinding, MenuViewModel> {

    private static final String TAG = MenuActivity.class.getSimpleName();

    @Override
    public int initContentView(Bundle savedInstanceState) {
        return R.layout.activity_menu;
    }

    @Override
    public int initVariableId() {
        return BR.viewModel;
    }

    @Override
    public Class<MenuViewModel> getViewModelClazz() {
        return MenuViewModel.class;
    }

    @Override
    public void initData() {
        super.initData();
    }

    @Override
    public void initViewObservable() {
        super.initViewObservable();
        setOnClickListener();
        doIsShowLoading();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    private void setOnClickListener() {
        ListenerUtils.setOnClickListener(getBinding().svLeaderboard, new OnMultiClickListener() {
            @Override
            public void onMultiClick(View v) {

            }
        });
        ListenerUtils.setOnClickListener(getBinding().svPhysicalGame, new OnMultiClickListener() {
            @Override
            public void onMultiClick(View v) {
                String packageName = "com.example.hp";
                Intent launchIntent = getPackageManager().getLaunchIntentForPackage(packageName);
                if (launchIntent != null) {
                    startActivity(launchIntent);
                } else {
                    ToastUtils.showShortSafe("Please launch again");
                }
            }
        });
        ListenerUtils.setOnClickListener(getBinding().svStart, new OnMultiClickListener() {
            @Override
            public void onMultiClick(View v) {

            }
        });
    }

    /**
     * 控制进度圈显示
     */
    private void doIsShowLoading() {
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
