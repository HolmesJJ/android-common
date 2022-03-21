package com.example.common.ui.activity;

import android.annotation.SuppressLint;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.MediaController;

import com.example.common.BR;
import com.example.common.R;
import com.example.common.base.BaseActivity;
import com.example.common.databinding.ActivityTutorialBinding;
import com.example.common.listener.OnMultiClickListener;
import com.example.common.ui.viewmodel.TutorialViewModel;
import com.example.common.utils.FileUtils;
import com.example.common.utils.ListenerUtils;

import java.io.File;

public class TutorialActivity extends BaseActivity<ActivityTutorialBinding, TutorialViewModel> {

    private static final String TAG = TutorialActivity.class.getSimpleName();
    private int mEnglishId;
    private boolean playing = false;

    @Override
    public int initContentView(Bundle savedInstanceState) {
        return R.layout.activity_tutorial;
    }

    @Override
    public int initVariableId() {
        return BR.viewModel;
    }

    @Override
    public Class<TutorialViewModel> getViewModelClazz() {
        return TutorialViewModel.class;
    }

    @Override
    public void initData() {
        super.initData();
        if (getIntent() != null && getIntent().getExtras() != null) {
            Bundle bundle = getIntent().getExtras();
            mEnglishId = bundle.getInt("englishId");
        }
        initPlayer();
        playVideo("tutorial");
        getBinding().btnSwitch.setText("Play Organ");
    }

    @Override
    public void initViewObservable() {
        super.initViewObservable();
        setObserveListener();
        setOnClickListener();
        doIsShowLoading();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (getViewModel() != null) {
            getViewModel().initData(mEnglishId);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    private void setObserveListener() {
        if (getViewModel() == null) {
            return;
        }
        getViewModel().getDetail().observe(this, detail -> {
            if (detail != null) {
                getBinding().tvDetail.setText(detail.replace("\\n", "\n\n"));
            }
        });
    }

    private void setOnClickListener() {
        ListenerUtils.setOnClickListener(getBinding().btnSwitch, new OnMultiClickListener() {
            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void onMultiClick(View v) {
                if (playing) {
                    playVideo("tutorial");
                    getBinding().btnSwitch.setText("Play Organ");
                } else {
                    playVideo("organ");
                    getBinding().btnSwitch.setText("Play Tutorial");
                }
                playing = !playing;
            }
        });
    }

    private void initPlayer() {
        MediaController mediaController = new MediaController(TutorialActivity.this);
        mediaController.setAnchorView(getBinding().vvPlayer);
        mediaController.setMediaPlayer(getBinding().vvPlayer);
        getBinding().vvPlayer.setMediaController(mediaController);
        getBinding().vvPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mPlayer) {
                mPlayer.start();
                mPlayer.setLooping(true);
            }
        });
    }

    private void playVideo(String videoType) {
        String path = FileUtils.VIDEO_DIR + mEnglishId + File.separator + videoType + ".mp4";
        getBinding().vvPlayer.setVideoURI(Uri.parse(path));
        getBinding().vvPlayer.start();
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
