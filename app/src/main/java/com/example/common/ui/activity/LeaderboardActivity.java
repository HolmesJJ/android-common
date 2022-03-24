package com.example.common.ui.activity;

import android.os.Bundle;

import com.example.common.BR;
import com.example.common.R;
import com.example.common.base.BaseActivity;
import com.example.common.databinding.ActivityLeaderboardBinding;
import com.example.common.ui.viewmodel.LeaderboardViewModel;

public class LeaderboardActivity extends BaseActivity<ActivityLeaderboardBinding, LeaderboardViewModel> {

    private static final String TAG = LeaderboardActivity.class.getSimpleName();

    @Override
    public int initContentView(Bundle savedInstanceState) {
        return R.layout.activity_leaderboard;
    }

    @Override
    public int initVariableId() {
        return BR.viewModel;
    }

    @Override
    public Class<LeaderboardViewModel> getViewModelClazz() {
        return LeaderboardViewModel.class;
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
