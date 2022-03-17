package com.example.common.ui.widget;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

import androidx.core.content.ContextCompat;

import com.example.common.R;

public class BarChartView extends View {

    // 画条形的画笔
    private Paint mBarPaint;
    // 画条形的画笔背景色
    private Paint mBarPaintBg;
    // 画字体的画笔
    private Paint mTextPaint;
    // 条形颜色
    private int mBarColor;
    // 条形背景颜色
    private int mBarBgColor;
    // 条形宽度
    private float mWidth;
    // 条形高度
    private float mHeight;
    // 圆角半径
    private float mCornerRadius;
    // 字的长度
    private float mTextWidth;
    // 字的高度
    private float mTextHeight;
    // 字的大小
    private float mTextSize;
    // 总进度
    private int mTotalProgress = 100;
    // 当前进度
    private int mProgress;

    public BarChartView(Context context, AttributeSet attrs) {
        super(context, attrs);
        // 获取自定义的属性
        initAttrs(context, attrs);
        initVariable(context);
    }

    // 属性
    private void initAttrs(Context context, AttributeSet attrs) {
        TypedArray typeArray = context.getTheme().obtainStyledAttributes(attrs,
                R.styleable.BarChartView, 0, 0);
        mBarColor = typeArray.getColor(R.styleable.BarChartView_bar_color, 0xFFFFFFFF);
        mBarBgColor = typeArray.getColor(R.styleable.BarChartView_bar_bg_color, 0xFFFFFFFF);
        mCornerRadius = typeArray.getDimension(R.styleable.BarChartView_corner_radius, 0xFFFFFFFF);
        mTextSize = typeArray.getDimension(R.styleable.BarChartView_text_size, 0xFFFFFFFF);
    }

    // 初始化画笔
    private void initVariable(Context context) {
        // 条形背景
        mBarPaintBg = new Paint();
        mBarPaintBg.setAntiAlias(true);
        mBarPaintBg.setColor(mBarBgColor);
        mBarPaintBg.setStyle(Paint.Style.FILL);

        // 条形
        mBarPaint = new Paint();
        mBarPaint.setAntiAlias(true);
        mBarPaint.setColor(mBarColor);
        mBarPaint.setStyle(Paint.Style.FILL);

        // 中间字
        mTextPaint = new Paint();
        mTextPaint.setAntiAlias(true);
        mTextPaint.setStyle(Paint.Style.FILL);
        mTextPaint.setColor(ContextCompat.getColor(context, R.color.white));
        mTextPaint.setTextSize(mTextSize);

        Paint.FontMetrics fm = mTextPaint.getFontMetrics();
        mTextHeight = (int) Math.ceil(fm.descent - fm.ascent);
    }

    // 画图
    @Override
    protected void onDraw(Canvas canvas) {
        mWidth = getWidth();
        mHeight = getHeight();

        // 条形背景
        canvas.drawRoundRect(0, 0, mWidth, mHeight, mCornerRadius, mCornerRadius, mBarPaintBg);

        // 字体
        String txt = mProgress + "%";
        mTextWidth = mTextPaint.measureText(txt, 0, txt.length());
        int padding = 20;

        // 条形
        if (mProgress > 0) {
            float mProgressWidth = ((float) mProgress / mTotalProgress) * mWidth;
            canvas.drawRoundRect(0, 0, mProgressWidth, mHeight, mCornerRadius, mCornerRadius, mBarPaint);
            if (mProgressWidth / 2 - mTextWidth / 2 > padding) {
                canvas.drawText(txt, mProgressWidth / 2 - mTextWidth / 2, mHeight / 2 + mTextHeight / 4, mTextPaint);
            } else {
                canvas.drawText(txt, padding, mHeight / 2 + mTextHeight / 4, mTextPaint);
            }
        } else {
            canvas.drawText(txt, padding, mHeight / 2 + mTextHeight / 4, mTextPaint);
        }
    }

    // 设置进度
    public void setProgress(int progress) {
        mProgress = progress;
        // 重绘
        postInvalidate();
    }
}
