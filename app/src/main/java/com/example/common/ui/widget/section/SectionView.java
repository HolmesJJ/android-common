package com.example.common.ui.widget.section;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.cardview.widget.CardView;

import com.example.common.R;
import com.example.common.ui.widget.RoundImageView;

public class SectionView extends LinearLayout {

    private Drawable mIcon;
    private String mTitle;
    private int mBgColor;

    private final CardView cvContainer;
    private final RoundImageView rivIcon;
    private final TextView tvTitle;

    public SectionView(Context context, AttributeSet attrs) {
        super(context, attrs);
        // 绑定布局
        LayoutInflater.from(context).inflate(R.layout.layout_section, this);
        cvContainer = findViewById(R.id.cv_container);
        rivIcon = findViewById(R.id.riv_icon);
        tvTitle = findViewById(R.id.tv_title);
        // 获取自定义的属性
        initAttrs(context, attrs);
    }

    // 属性
    private void initAttrs(Context context, AttributeSet attrs) {
        TypedArray typeArray = context.getTheme().obtainStyledAttributes(attrs,
                R.styleable.SectionView, 0, 0);
        mTitle = typeArray.getString(R.styleable.SectionView_sv_title);
        mIcon = typeArray.getDrawable(R.styleable.SectionView_sv_src);
        mBgColor = typeArray.getColor(R.styleable.SectionView_sv_bg_color, 0xFFFFFFFF);
        cvContainer.setCardBackgroundColor(mBgColor);
        rivIcon.setImageDrawable(mIcon);
        tvTitle.setText(mTitle);
    }
}
