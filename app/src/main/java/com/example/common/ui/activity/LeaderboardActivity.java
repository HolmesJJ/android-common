package com.example.common.ui.activity;

import android.os.Bundle;

import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.common.BR;
import com.example.common.R;
import com.example.common.adapter.game.LeaderboardAdapter;
import com.example.common.base.BaseActivity;
import com.example.common.databinding.ActivityLeaderboardBinding;
import com.example.common.ui.viewmodel.LeaderboardViewModel;
import com.example.common.utils.ContextUtils;

public class LeaderboardActivity extends BaseActivity<ActivityLeaderboardBinding, LeaderboardViewModel> {

    private static final String TAG = LeaderboardActivity.class.getSimpleName();

    private LeaderboardAdapter mLeaderboardAdapter;

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
        if (getViewModel() != null) {
            getViewModel().initData();
        }
    }

    @Override
    public void initViewObservable() {
        super.initViewObservable();
        setObserveListener();
        doIsShowLoading();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    private void setObserveListener() {
        if (getViewModel() == null) {
            return;
        }
        getViewModel().getMembers().observe(this, members -> {
            if (members != null) {
                mLeaderboardAdapter = new LeaderboardAdapter(ContextUtils.getContext(), members);
                getBinding().rvMembers.setAdapter(mLeaderboardAdapter);
                LinearLayoutManager layoutManager = new LinearLayoutManager(ContextUtils.getContext());
                layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
                getBinding().rvMembers.setLayoutManager(layoutManager);
                DividerItemDecoration verticalDecoration = new DividerItemDecoration(ContextUtils.getContext(), DividerItemDecoration.VERTICAL);
                getBinding().rvMembers.addItemDecoration(verticalDecoration);
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
