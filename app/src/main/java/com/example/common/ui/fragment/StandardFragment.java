package com.example.common.ui.fragment;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.Nullable;

import com.example.common.BR;
import com.example.common.R;
import com.example.common.base.BaseFragment;
import com.example.common.config.Config;
import com.example.common.databinding.FragmentStandardBinding;
import com.example.common.ui.activity.SpeechActivity;
import com.example.common.ui.viewmodel.StandardViewModel;
import com.example.common.ui.widget.speech.CustomMarkerView;
import com.example.common.utils.ContextUtils;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.ValueFormatter;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class StandardFragment extends BaseFragment<FragmentStandardBinding, StandardViewModel>
        implements SpeechActivity.ISpeechDataUpdated {

    private static final String TAG = StandardFragment.class.getSimpleName();
    private static final String ENGLISH_ID = "englishId";
    private static final String CONTENT = "content";
    private static final float POSITION_LINE = 0.4f;

    private final List<Double> mPoints = new ArrayList<>();
    private final List<Integer> mSplits = new ArrayList<>();
    private final List<Integer> mPositions = new ArrayList<>();
    private final List<String> mWords = new ArrayList<>(); // No need to clear

    private int mEnglishId;
    private String mContent;

    public static StandardFragment newInstance(int englishId, String content) {
        Bundle args = new Bundle();
        StandardFragment fragment = new StandardFragment();
        args.putInt(ENGLISH_ID, englishId);
        args.putString(CONTENT, content);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public int initContentView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return R.layout.fragment_standard;
    }

    @Override
    public int initVariableId() {
        return BR.viewModel;
    }

    @Override
    public Class<StandardViewModel> getViewModelClazz() {
        return StandardViewModel.class;
    }

    @Override
    public void initData() {
        super.initData();
        Bundle bundle = this.getArguments();
        if (bundle != null) {
            mEnglishId = bundle.getInt(ENGLISH_ID);
            mContent = bundle.getString(CONTENT);
            mWords.clear();
            try {
                // TODO 把内容分割成单词或音标
                mWords.addAll(Arrays.asList(mContent.split(",")));
            } catch (Exception e) {
                e.printStackTrace();
                mWords.add(mContent);
            }
        }
        if (getActivity() instanceof SpeechActivity) {
            SpeechActivity activity = (SpeechActivity) getActivity();
            activity.setSpeechDataUpdated(this);
        }
        initLineChart();
        clearData();
        initSpeechData();
    }

    @Override
    public void initViewObservable() {
        super.initViewObservable();
        setObserveListener();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onSpeechDataUpdated(String speechData) {
        if (getViewModel() != null) {
            getViewModel().getSpeechData().postValue(speechData);
        }
    }

    private void setObserveListener() {
        if (getViewModel() == null) {
            return;
        }
        getViewModel().getSpeechData().observe(this, speechData -> {
            clearData();
            initSpeechData(speechData);
        });
    }

    private void initSpeechData() {
        String data = Config.getSpeechData();
        initSpeechData(data);
    }

    private void initSpeechData(String data) {
        if (data.equals("")) {
            return;
        }
        try {
            JSONObject jsonObject = new JSONObject(data);
            JSONArray points = jsonObject.getJSONArray("d1");
            for (int i = 0; i < points.length(); i++) {
                mPoints.add(points.getDouble(i));
            }
            JSONArray splits = jsonObject.getJSONArray("step1");
            for (int i = 0; i < splits.length(); i++) {
                mSplits.add(splits.getInt(i));
            }
            Collections.sort(mSplits);
            setData();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void initLineChart() {
        getBinding().lcStandard.getDescription().setEnabled(false);
        getBinding().lcStandard.setDrawGridBackground(false);
        getBinding().lcStandard.setBackgroundColor(Color.TRANSPARENT);
        getBinding().lcStandard.setDragEnabled(true);
        getBinding().lcStandard.setScaleEnabled(true);
        getBinding().lcStandard.setPinchZoom(true);
        getBinding().lcStandard.setHighlightPerDragEnabled(true);
        getBinding().lcStandard.animateX(1000);
        getBinding().lcStandard.setTouchEnabled(true);
        getBinding().lcStandard.setDragDecelerationEnabled(true);
        getBinding().lcStandard.setDragDecelerationFrictionCoef(0.9f);
        Legend legend = getBinding().lcStandard.getLegend();
        legend.setForm(Legend.LegendForm.LINE);
        legend.setTextColor(Color.BLACK);

        XAxis xAxis = getBinding().lcStandard.getXAxis();
        xAxis.setTextColor(Color.rgb(0, 0, 0));
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setTextSize(10f);
        xAxis.setDrawLabels(true);
        xAxis.setDrawGridLines(false);
        xAxis.setGranularityEnabled(true);

        // YAxis: leftAxis
        YAxis leftAxis = getBinding().lcStandard.getAxisLeft();
        leftAxis.setTextColor(Color.rgb(0, 0, 0));
        leftAxis.setTextSize(10f);
        leftAxis.setAxisMaximum(0.8f);
        leftAxis.setAxisMinimum(-0.8f);
        leftAxis.setGranularity(0.25f);
        leftAxis.setDrawGridLines(true);
        leftAxis.setGranularityEnabled(true);

        // YAxis: rightAxis
        YAxis rightAxis = getBinding().lcStandard.getAxisRight();
        rightAxis.setEnabled(false);
    }

    private void setData() {
        final ArrayList<Entry> points = new ArrayList<>();
        final ArrayList<Entry> splits = new ArrayList<>();
        final ArrayList<Entry> positions = new ArrayList<>();
        final ArrayList<String> words = new ArrayList<>();

        for (int i = 0; i < mPoints.size(); i++) {
            points.add(new Entry(i, mPoints.get(i).floatValue(), i + "F"));
        }

        for (int i = 0; i < mSplits.size(); i++) {
            if (i % 2 == 0) {
                splits.add(new Entry(mSplits.get(i), 1f));
                splits.add(new Entry(mSplits.get(i), -1f));
            } else {
                splits.add(new Entry(mSplits.get(i), -1f));
                splits.add(new Entry(mSplits.get(i), 1f));
            }
        }

        for (int i = 0; i < mSplits.size(); i++) {
            if (i % 2 == 1) {
                int position = (mSplits.get(i - 1) + mSplits.get(i)) / 2;
                positions.add(new Entry(position, POSITION_LINE));
                mPositions.add(position);
            }
        }

        LineDataSet pointsDataSet = new LineDataSet(points, getString(R.string.standard_line));
        pointsDataSet.setAxisDependency(YAxis.AxisDependency.LEFT);
        pointsDataSet.setColor(Color.GREEN);
        pointsDataSet.setLineWidth(1f);
        pointsDataSet.setDrawCircles(false);
        pointsDataSet.setDrawValues(false);

        LineDataSet splitsDataSet = new LineDataSet(splits, getString(R.string.split_line));
        splitsDataSet.setColor(Color.rgb(0, 255, 255));
        splitsDataSet.setLineWidth(1f);
        splitsDataSet.setDrawCircles(false);
        splitsDataSet.setDrawValues(false);

        LineDataSet positionsDataSet = new LineDataSet(positions, "");
        positionsDataSet.setColor(Color.TRANSPARENT);
        positionsDataSet.setLineWidth(1f);
        positionsDataSet.setCircleColor(Color.GREEN);
        positionsDataSet.setCircleHoleColor(Color.WHITE);
        positionsDataSet.setCircleRadius(4f);

        LineData data = new LineData(pointsDataSet, splitsDataSet, positionsDataSet);
        getBinding().lcStandard.setVisibleXRangeMaximum(mPoints.size());
        getBinding().lcStandard.fitScreen();
        getBinding().lcStandard.setData(data);

        // words的长度和mPositions要一致
        int size = mWords.size();
        if (size <= mPositions.size()) {
            words.addAll(mWords);
            for (int i = size; i < mPositions.size(); i++) {
                words.add("Nil");
            }
        } else {
            for (int i = 0; i < mPositions.size(); i++) {
                words.add(mWords.get(i));
            }
        }

        CustomMarkerView cmv = new CustomMarkerView(ContextUtils.getContext(), mPositions, words, POSITION_LINE, Color.GREEN);
        cmv.setChartView(getBinding().lcStandard);
        getBinding().lcStandard.setMarker(cmv);

        XAxis xAxis = getBinding().lcStandard.getXAxis();
        xAxis.setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                String xLabel = "";
                try {
                    xLabel = String.valueOf(points.get((int) value).getData());
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return xLabel;
            }
        });

        getBinding().lcStandard.notifyDataSetChanged();
        getBinding().lcStandard.invalidate();
    }

    private void clearData() {
        mPoints.clear();
        mSplits.clear();
        mPositions.clear();
        if (getBinding().lcStandard.getData() != null) {
            getBinding().lcStandard.getData().clearValues();
        }
        getBinding().lcStandard.notifyDataSetChanged();
        getBinding().lcStandard.clear();
        getBinding().lcStandard.invalidate();
    }
}
