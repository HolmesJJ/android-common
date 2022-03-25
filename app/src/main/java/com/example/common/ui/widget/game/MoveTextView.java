package com.example.common.ui.widget.game;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatTextView;

import com.example.common.callback.ITouchControl;

public class MoveTextView extends AppCompatTextView {

    private ITouchControl mListener;
    private View mView;
    private View mCurrentInView;

    private float lastX, lastY;

    public MoveTextView(Context context) {
        super(context);
        mView = this;
    }

    public MoveTextView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        mView = this;
    }

    public MoveTextView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mView = this;
    }

    public void setControlOnTouch(ITouchControl listener) {
        mListener = listener;
    }

    public View getCurrentInView() {
        return mCurrentInView;
    }

    public void setCurrentInView(View currentInView) {
        mCurrentInView = currentInView;
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                lastX = event.getRawX();
                lastY = event.getRawY();
                return true;
            case MotionEvent.ACTION_UP:
                if (mListener != null) {
                    mListener.onTouchControl(getX(), getY(), mView);
                }
                return true;
            case MotionEvent.ACTION_MOVE:
                //  不要直接用getX和getY,这两个获取的数据已经是经过处理的,容易出现图片抖动的情况
                float distanceX = lastX - event.getRawX();
                float distanceY = lastY - event.getRawY();
                float nextY = getY() - distanceY;
                float nextX = getX() - distanceX;

                // 不能移出屏幕
                if (nextY < 0) {
                    nextY = 0;
                } else if (nextY > (((ViewGroup) getParent()).getHeight() - getHeight())) {
                    nextY = (((ViewGroup) getParent()).getHeight() - getHeight());
                }
                if (nextX < 0) {
                    nextX = 0;
                } else if (nextX > (((ViewGroup) getParent()).getWidth() - getWidth())) {
                    nextX = (((ViewGroup) getParent()).getWidth() - getWidth());
                }

                // 属性动画移动
                ObjectAnimator y = ObjectAnimator.ofFloat(this, "y", getY(), nextY);
                ObjectAnimator x = ObjectAnimator.ofFloat(this, "x", getX(), nextX);

                AnimatorSet animatorSet = new AnimatorSet();
                animatorSet.playTogether(x, y);
                animatorSet.setDuration(0);
                animatorSet.start();

                lastX = event.getRawX();
                lastY = event.getRawY();
        }
        return false;
    }
}
