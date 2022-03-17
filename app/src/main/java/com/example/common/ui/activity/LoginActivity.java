package com.example.common.ui.activity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.example.common.BR;
import com.example.common.R;
import com.example.common.base.BaseActivity;
import com.example.common.databinding.ActivityLoginBinding;
import com.example.common.listener.OnMultiClickListener;
import com.example.common.ui.viewmodel.LoginViewModel;
import com.example.common.utils.ListenerUtils;
import com.example.common.utils.ToastUtils;

public class LoginActivity extends BaseActivity<ActivityLoginBinding, LoginViewModel> {

    private static final String TAG = LoginActivity.class.getSimpleName();

    @Override
    public int initContentView(Bundle savedInstanceState) {
        return R.layout.activity_login;
    }

    @Override
    public int initVariableId() {
        return BR.viewModel;
    }

    @Override
    public Class<LoginViewModel> getViewModelClazz() {
        return LoginViewModel.class;
    }

    @Override
    public void initData() {
        super.initData();
        if (getViewModel() != null) {
            getViewModel().getUsername().set(getBinding().etUsername.getText().toString());
            getViewModel().getPassword().set(getBinding().etPassword.getText().toString());
            getViewModel().updateSignInBtnState();
        }
    }

    @Override
    public void initViewObservable() {
        super.initViewObservable();
        // 设置文本变更监听
        setTextChangeListener();
        // 设置是否可登陆监听，以便更改登录按钮UI
        setEnableSignInListener();
        setObserveListener();
        setOnClickListener();
        doIsShowLoading();
    }

    @Override
    protected void onDestroy() {
        ListenerUtils.remove(getViewModel().getEnableSignIn());
        super.onDestroy();
    }

    private void setTextChangeListener() {
        // 设置账号输入文本变更监听
        ListenerUtils.addTextChangeListener(getBinding().etUsername, s -> {
            if (getViewModel() != null) {
                String value = s == null ? "" : s;
                getViewModel().getUsername().set(value);
                getViewModel().updateSignInBtnState();
            }
        });
        // 设置密码文本变更监听
        ListenerUtils.addTextChangeListener(getBinding().etPassword, s -> {
            if (getViewModel() != null) {
                String value = s == null ? "" : s;
                getViewModel().getPassword().set(value);
                getViewModel().updateSignInBtnState();
            }
        });
    }

    private void setEnableSignInListener() {
        ListenerUtils.addSignalOnPropertyChangeCallback(getViewModel().getEnableSignIn(), (observable, i, value) -> {
            if (value) {
                getBinding().btnSignIn.setBackgroundResource(R.drawable.bg_btn_enable_round);
                getBinding().btnSignIn.setTextColor(getResources().getColor(R.color.white, this.getTheme()));
            } else {
                getBinding().btnSignIn.setBackgroundResource(R.drawable.bg_btn_unable_round);
                getBinding().btnSignIn.setTextColor(getResources().getColor(R.color.black, this.getTheme()));
            }
        });
    }

    private void setObserveListener() {
        getViewModel().getActivityAction().observe(this, activityAction -> {
            stopLoading();
            if (activityAction != null) {
                try {
                    Intent intent = new Intent(LoginActivity.this, activityAction);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent);
                    finish();
                } catch (Exception e) {
                    ToastUtils.showShortSafe(e.getMessage());
                }
            } else {
                Log.e(TAG, "activityAction is null");
            }
        });
    }

    private void setOnClickListener() {
        ListenerUtils.setOnClickListener(getBinding().btnSignIn, new OnMultiClickListener() {
            @Override
            public void onMultiClick(View v) {
                if (getViewModel() != null && getViewModel().getEnableSignIn().get()) {
                    getViewModel().signIn();
                }
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
