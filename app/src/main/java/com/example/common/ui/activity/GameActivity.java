package com.example.common.ui.activity;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import com.example.common.BR;
import com.example.common.R;
import com.example.common.base.BaseActivity;
import com.example.common.databinding.ActivityGameBinding;
import com.example.common.listener.OnMultiClickListener;
import com.example.common.ui.viewmodel.GameViewModel;
import com.example.common.ui.widget.game.MoveControl;
import com.example.common.utils.ListenerUtils;

public class GameActivity extends BaseActivity<ActivityGameBinding, GameViewModel> {

    private static final String TAG = GameActivity.class.getSimpleName();

    private MoveControl mMoveControl;

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
        mMoveControl = new MoveControl(GameActivity.this);
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
        ListenerUtils.setOnClickListener(getBinding().rivStart, new OnMultiClickListener() {
            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void onMultiClick(View v) {
                if (mMoveControl == null) {
                    return;
                }
                mMoveControl.setScore(0);
                if(!mMoveControl.isGameStart()) {
                    getBinding().tvScore.setText(String.valueOf(mMoveControl.getScore()));
                    mMoveControl.startGame();
                }
                else {
                    getBinding().tvScore.setText(String.valueOf(mMoveControl.getScore()));
                    mMoveControl.stopGame();
                }
            }
        });
    }

    public void uploadScore(int score) {
        if (getViewModel() != null) {
            getViewModel().uploadScore(score);
        }
    }

    @SuppressLint("ResourceType")
    public void updateStatusBar(boolean isShowed) {
        Window window = getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        if (isShowed) {
            window.setStatusBarColor(Color.parseColor(getResources().getString(R.color.shade)));
            window.getDecorView().setSystemUiVisibility(0);
        } else {
            window.setStatusBarColor(Color.WHITE);
            window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        }
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
