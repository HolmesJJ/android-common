package com.example.common.ui.widget.dialog;

import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.example.common.R;
import com.example.common.listener.OnMultiClickListener;

public class TextDialog extends BaseDialog {

    private TextView mTvTitle;
    private TextView mTvContent;
    private TextView mTvConfirm;

    private DialogEventListener mDialogEventListener;

    public TextDialog(@NonNull Context context) {
        super(context);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.createDialogView(savedInstanceState, R.layout.dialog_text);
        Window window = getWindow();
        if (window != null) {
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams
                    .FLAG_ALT_FOCUSABLE_IM);
            getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE | WindowManager
                    .LayoutParams.SOFT_INPUT_STATE_VISIBLE);
        }

    }

    @Override
    protected void initView() {
        mTvTitle = findViewById(R.id.tv_title);
        mTvContent = findViewById(R.id.tv_content);
        mTvConfirm = findViewById(R.id.tv_confirm);
        setConfirmListener();
    }

    public TextDialog setDialogTitle(String title) {
        if (mTvTitle != null) {
            mTvTitle.setText(title);
        }
        return this;
    }

    public TextDialog setDialogTitle(int resId) {
        if (mTvTitle != null) {
            mTvTitle.setText(resId);
        }
        return this;
    }

    public TextDialog setText(String text) {
        if (mTvContent != null) {
            mTvContent.setText(text);
        }
        return this;
    }

    public TextDialog setText(int resId) {
        if (mTvContent != null) {
            mTvContent.setText(resId);
        }
        return this;
    }

    public TextDialog setDialogEventListener(DialogEventListener listener) {
        mDialogEventListener = listener;
        return this;
    }

    private void setConfirmListener() {
        mTvConfirm.setOnClickListener(new OnMultiClickListener() {
            @Override
            public void onMultiClick(View v) {
                dismiss();
            }
        });
    }

    public interface DialogEventListener {
        /**
         * 点击取消按钮后的回调
         */
        void confirm(String text);
    }
}
