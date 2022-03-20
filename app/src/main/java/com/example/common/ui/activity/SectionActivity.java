package com.example.common.ui.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.example.common.BR;
import com.example.common.R;
import com.example.common.base.BaseActivity;
import com.example.common.databinding.ActivitySectionBinding;
import com.example.common.listener.OnMultiClickListener;
import com.example.common.ui.viewmodel.SectionViewModel;
import com.example.common.utils.ContextUtils;
import com.example.common.utils.ListenerUtils;
import com.example.common.utils.ToastUtils;

public class SectionActivity extends BaseActivity<ActivitySectionBinding, SectionViewModel> {

    private static final String TAG = SectionActivity.class.getSimpleName();
    private int mEnglishId;

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
        if (getIntent() != null && getIntent().getExtras() != null) {
            Bundle bundle = getIntent().getExtras();
            mEnglishId = bundle.getInt("englishId");
        }
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
        ListenerUtils.setOnClickListener(getBinding().svTutorial, new OnMultiClickListener() {
            @Override
            public void onMultiClick(View v) {
                Intent intent = new Intent(ContextUtils.getContext(), TutorialActivity.class);
                intent.putExtra("englishId", mEnglishId);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
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
                Intent intent = new Intent(ContextUtils.getContext(), SpeechActivity.class);
                intent.putExtra("englishId", mEnglishId);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
            }
        });
        ListenerUtils.setOnClickListener(getBinding().svMouth, new OnMultiClickListener() {
            @Override
            public void onMultiClick(View v) {
                Intent intent = new Intent(ContextUtils.getContext(), MouthActivity.class);
                intent.putExtra("englishId", mEnglishId);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
            }
        });
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
