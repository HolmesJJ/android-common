package com.example.common.ui.widget.dialog.mouth;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.example.common.R;
import com.example.common.listener.OnMultiClickListener;
import com.example.common.model.mouth.Frame;
import com.example.common.model.mouth.Mouths;
import com.example.common.player.IMediaPlayer;
import com.example.common.player.SlackAudioPlayer;
import com.example.common.ui.widget.RoundImageView;
import com.example.common.ui.widget.dialog.BaseDialog;
import com.example.common.utils.ContextUtils;
import com.example.common.utils.FileUtils;
import com.example.common.utils.ListenerUtils;
import com.example.common.utils.ToastUtils;

import java.io.File;

public class ResultDialog extends BaseDialog {

    private static final String TAG = ResultDialog.class.getSimpleName();
    private final Mouths mMouths;
    private final int max;

    private TextView mTvTitle;
    private ImageView mIvMouth;
    private SeekBar mSbProgress;
    private TextView mTvMessage;
    private RoundImageView mRivWarn;
    private RoundImageView mRivPlay;
    private Button mBtnConfirm;

    private SlackAudioPlayer mSlackAudioPlayer;

    private Handler mHandler;

    private boolean isPlaying = false;
    private int mCountFrame = 0;
    private int mCountMarkedFrame = 0;

    private int mEnglishId;
    private String mContent;

    public ResultDialog(@NonNull Context context, Mouths mouths, int englishId, String content) {
        super(context);
        mMouths = mouths;
        max = mouths.getFrames().size();
        mEnglishId = englishId;
        mContent = content;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.createDialogView(savedInstanceState, R.layout.dialog_result);
        Window window = getWindow();
        if (window != null) {
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams
                    .FLAG_ALT_FOCUSABLE_IM);
            getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE | WindowManager
                    .LayoutParams.SOFT_INPUT_STATE_VISIBLE);
        }
        if (mHandler == null) {
            mHandler = new Handler(Looper.getMainLooper());
        }
        // 只能点击确认关闭弹框
        setCanceledOnTouchOutside(false);
    }

    @Override
    protected void initView() {
        mTvTitle = findViewById(R.id.tv_title);
        mIvMouth = findViewById(R.id.iv_mouth);
        mTvMessage = findViewById(R.id.tv_message);
        mSbProgress = findViewById(R.id.sb_progress);
        mRivWarn = findViewById(R.id.riv_warn);
        mRivPlay = findViewById(R.id.riv_play);
        mBtnConfirm = findViewById(R.id.btn_confirm);
        if (max > 0) {
            mSbProgress.setMax(max - 1);
            mSbProgress.setProgress(0);
        }
        setOnClickListener();
        setOnTouchListener();
    }

    @Override
    public void show() {
        super.show();
        if (max > 0) {
            Frame frame = mMouths.getFrames().get(0);
            Bitmap bitmap = frame.getBitmap();
            mIvMouth.setImageBitmap(bitmap);
        }
        initPlayer();
    }

    @Override
    public void dismiss() {
        releasePlayer();
        if (mHandler != null) {
            mHandler.removeCallbacksAndMessages(null);
            mHandler = null;
        }
        super.dismiss();
    }

    private void setOnClickListener() {
        ListenerUtils.setOnClickListener(mRivWarn, new OnMultiClickListener() {
            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void onMultiClick(View v) {
                pausePlayer();
                mHandler.removeCallbacks(mPlayBitmapsRunnable);
                int markedSize = mMouths.getMarkedFrames().size();
                if (markedSize == 0) {
                    mTvTitle.setText(R.string.no_warning);
                    return;
                }
                if (mCountMarkedFrame < markedSize) {
                    Frame frame = mMouths.getMarkedFrames().get(mCountMarkedFrame);
                    mTvTitle.setText(frame.getTitle());
                    mIvMouth.setImageBitmap(frame.getMarkedBitmap());
                    mTvMessage.setText(frame.getMessage());
                    mSbProgress.setProgress(frame.getId());
                    mCountMarkedFrame++;
                }
                if (mCountMarkedFrame >= markedSize) {
                    mCountMarkedFrame = 0;
                }
                isPlaying = false;
            }
        });
        ListenerUtils.setOnClickListener(mRivPlay, new OnMultiClickListener() {
            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void onMultiClick(View v) {
                if (isPlaying) {
                    return;
                }
                mTvTitle.setText("");
                mTvMessage.setText("");
                mSbProgress.setProgress(0);
                mCountFrame = 0;
                startPlayer();
                mHandler.post(mPlayBitmapsRunnable);
                isPlaying = true;
            }
        });
        ListenerUtils.setOnClickListener(mBtnConfirm, new OnMultiClickListener() {
            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void onMultiClick(View v) {
                dismiss();
            }
        });
    }

    private void setOnTouchListener() {
        ListenerUtils.setOnTouchListener(mSbProgress, new View.OnTouchListener() {
            @SuppressLint("ClickableViewAccessibility")
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                return true;
            }
        });
    }

    private final Runnable mPlayBitmapsRunnable = new Runnable() {
        @Override
        public void run() {
            if (max > 0) {
                Frame frame = mMouths.getFrames().get(mCountFrame);
                Bitmap bitmap = frame.getBitmap();
                mIvMouth.setImageBitmap(bitmap);
                mCountFrame++;
                mSbProgress.setProgress(mCountFrame);
            }
            if (mCountFrame < max) {
                // TODO 目前帧率在90毫秒左右和音频差不多同步，需要进一步优化
                mHandler.postDelayed(mPlayBitmapsRunnable, 90);
            } else {
                mCountFrame = 0;
                mSbProgress.setProgress(0);
                isPlaying = false;
            }
        }
    };

    private void initPlayer() {
        File audioFile = new File(FileUtils.CAPTURE_DIR + mEnglishId + "/audio.mp3");
        if (!audioFile.exists() || !audioFile.isFile()) {
            mRivWarn.setImageResource(R.drawable.ic_unwarn);
            mRivWarn.setEnabled(false);
            mRivWarn.setClickable(false);
            mRivPlay.setImageResource(R.drawable.ic_unplay);
            mRivPlay.setEnabled(false);
            mRivPlay.setClickable(false);
            ToastUtils.showShortSafe("File not found");
            return;
        }
        try {
            mSlackAudioPlayer = new SlackAudioPlayer(ContextUtils.getContext());
            mSlackAudioPlayer.setDataSource(audioFile.getAbsolutePath());
            mSlackAudioPlayer.setOnMusicDurationListener(new IMediaPlayer.OnMusicDurationListener() {
                @Override
                public void onMusicDuration(IMediaPlayer mp, float duration) {
                    Log.i(TAG, "onMusicDuration: " + duration);
                }
            });
            mSlackAudioPlayer.setOnCompletionListener(new IMediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(IMediaPlayer mp) {
                    Log.i(TAG, "onCompletion");
                }
            });
            mSlackAudioPlayer.setOnErrorListener(new IMediaPlayer.OnErrorListener() {
                @Override
                public void onError(IMediaPlayer mp, @IMediaPlayer.AudioPlayError int what, String msg) {
                    Log.i(TAG, "Error, what: " + what + " msg: " + msg);
                }
            });
            mSlackAudioPlayer.prepareAsync();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void startPlayer() {
        if (mSlackAudioPlayer != null && !mSlackAudioPlayer.isPlaying()) {
            mSlackAudioPlayer.start();
        }
    }

    private void pausePlayer() {
        if (mSlackAudioPlayer != null && mSlackAudioPlayer.isPlaying()) {
            mSlackAudioPlayer.pause();
        }
    }

    private void releasePlayer() {
        pausePlayer();
        if (mSlackAudioPlayer != null) {
            mSlackAudioPlayer.release();
            mSlackAudioPlayer = null;
        }
    }
}
