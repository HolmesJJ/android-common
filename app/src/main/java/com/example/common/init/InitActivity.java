package com.example.common.init;

import android.content.Intent;
import android.os.Bundle;

import com.example.common.BR;
import com.example.common.R;
import com.example.common.base.BaseActivity;
import com.example.common.constants.Constants;
import com.example.common.databinding.ActivityInitBinding;
import com.example.common.utils.ContextUtils;
import com.example.common.utils.PermissionsUtils;

import pub.devrel.easypermissions.EasyPermissions;

public class InitActivity extends BaseActivity<ActivityInitBinding, InitViewModel> {

    private static final String TAG = InitActivity.class.getSimpleName();

    private static final String[] PERMISSIONS = new String[]{
            android.Manifest.permission.INTERNET,
            android.Manifest.permission.CAMERA,
    };

    @Override
    public int initContentView(Bundle savedInstanceState) {
        return R.layout.activity_init;
    }

    @Override
    public int initVariableId() {
        return BR.viewModel;
    }

    @Override
    public Class<InitViewModel> getViewModelClazz() {
        return InitViewModel.class;
    }

    @Override
    public void initData() {
        super.initData();
        PermissionsUtils.doSomeThingWithPermission(this, () -> {
            if (getViewModel() != null) {
                getViewModel().initData();
            }
        }, PERMISSIONS, Constants.PERMISSION_REQUEST_CODE, R.string.rationale_init);
    }

    @Override
    public void initViewObservable() {
        super.initViewObservable();
        showLoading(false);
        setObserveListener();
    }

    @Override
    protected void onPause() {
        super.onPause();
        stopLoading();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    protected boolean onHasPermissions() {
        return EasyPermissions.hasPermissions(ContextUtils.getContext(), PERMISSIONS);
    }

    @Override
    protected void onPermissionSuccessCallbackFromSetting() {
        super.onPermissionSuccessCallbackFromSetting();
        if (getViewModel() != null) {
            getViewModel().initData();
        }
    }

    private void setObserveListener() {
        getViewModel().getActivityAction().observe(this, activityAction -> {
            stopLoading();
            Intent intent = new Intent(ContextUtils.getContext(), activityAction);
            startActivity(intent);
            finish();
        });
        getViewModel().isInitSuccess().observe(this, isInitSuccess -> {
            if (!isInitSuccess) {
                stopLoading();
                exitApp();
            }
        });
    }
}
