package com.example.common.ui.widget.dialog.mouth;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
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
import com.example.common.ui.widget.RoundImageView;
import com.example.common.ui.widget.dialog.BaseDialog;
import com.example.common.utils.ListenerUtils;

public class ResultDialog extends BaseDialog {

    private final Mouths mMouths;
    private final int max;

    private TextView mTvTitle;
    private ImageView mIvMouth;
    private SeekBar mSbProgress;
    private TextView mTvMessage;
    private RoundImageView mRivWarn;
    private RoundImageView mRivPlay;
    private Button mBtnConfirm;

    private Handler mHandler;

    private boolean isPlaying = true;
    private int mCountFrame = 0;
    private int mCountMarkedFrame = 0;

    public ResultDialog(@NonNull Context context, Mouths mouths) {
        super(context);
        mMouths = mouths;
        max = mouths.getFrames().size();
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
        mHandler.post(mPlayBitmapsRunnable);
    }

    @Override
    public void dismiss() {
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
                mRivPlay.setImageResource(R.drawable.ic_unplay);
                mRivWarn.setImageResource(R.drawable.ic_warn);
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
                mRivPlay.setImageResource(R.drawable.ic_play);
                mRivWarn.setImageResource(R.drawable.ic_unwarn);
                mTvTitle.setText("");
                mTvMessage.setText("");
                mSbProgress.setProgress(0);
                mCountFrame = 0;
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
            if (mCountFrame >= max) {
                mCountFrame = 0;
            }
            mHandler.postDelayed(mPlayBitmapsRunnable, 40);
        }
    };
}
