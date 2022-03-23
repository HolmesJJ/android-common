package com.example.common.ui.widget.speech;

import android.annotation.SuppressLint;
import android.content.Context;
import android.widget.TextView;

import com.example.common.R;
import com.github.mikephil.charting.components.MarkerView;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.utils.MPPointF;

import java.util.List;

@SuppressLint("ViewConstructor")
public class CustomMarkerView extends MarkerView {

    private final TextView tvContent;
    private final List<Integer> mPositions;
    private final List<String> mWords;
    private final float mPositionLine;

    public CustomMarkerView(Context context, List<Integer> positions, List<String> words, float positionLine, int color) {
        super(context, R.layout.layout_marker);
        tvContent = findViewById(R.id.tv_content);
        tvContent.setTextColor(color);
        mPositions = positions;
        mWords = words;
        mPositionLine = positionLine;
    }

    @Override
    public void refreshContent(Entry e, Highlight highlight) {
        int count = 0;
        for (int i = 0; i < mPositions.size(); i++) {
            if (e.getY() == mPositionLine && mPositions.get(i) == e.getX() && i < mWords.size()) {
                tvContent.setText(mWords.get(i));
                count++;
                break;
            }
        }
        if (count == 0) {
            tvContent.setText("");
        }
        super.refreshContent(e, highlight);
    }

    @Override
    public MPPointF getOffset() {
        return new MPPointF(-(getWidth() * 1.0f / 2), -getHeight());
    }
}
