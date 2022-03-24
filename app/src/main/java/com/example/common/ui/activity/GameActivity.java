package com.example.common.ui.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.example.common.BR;
import com.example.common.R;
import com.example.common.base.BaseActivity;
import com.example.common.databinding.ActivityGameBinding;
import com.example.common.listener.OnMultiClickListener;
import com.example.common.ui.viewmodel.GameViewModel;
import com.example.common.utils.ListenerUtils;
import com.example.common.utils.ToastUtils;

public class GameActivity extends BaseActivity<ActivityGameBinding, GameViewModel> {

    private static final String TAG = GameActivity.class.getSimpleName();

    @Override
    public int initContentView(Bundle savedInstanceState) {
        return R.layout.activity_game;
    }

    @Override
    public int initVariableId() {
        return BR.viewModel;
    }

    @Override
    public Class<GameViewModel> getViewModelClazz() {
        return GameViewModel.class;
    }

    @Override
    public void initData() {
        super.initData();
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
