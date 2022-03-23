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
import com.example.common.databinding.FragmentCompareBinding;
import com.example.common.ui.activity.SpeechActivity;
import com.example.common.ui.viewmodel.CompareViewModel;
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

public class CompareFragment extends BaseFragment<FragmentCompareBinding, CompareViewModel>
        implements SpeechActivity.ISpeechDataUpdated {

    private static final String TAG = CompareFragment.class.getSimpleName();
    private static final String ENGLISH_ID = "englishId";
    private static final String CONTENT = "content";
    private static final float POSITION_LINE = 0.4f;

    private final List<Double> mStandardPoints = new ArrayList<>();
    private final List<Integer> mStandardSplits = new ArrayList<>();
    private final List<Integer> mStandardPositions = new ArrayList<>();

    private final List<Double> mTestPoints = new ArrayList<>();
    private final List<Integer> mTestSplits = new ArrayList<>();
    private final List<Integer> mTestPositions = new ArrayList<>();

    private final List<String> mWords = new ArrayList<>(); // No need to clear

    private int mEnglishId;
    private String mContent;

    public static CompareFragment newInstance(int englishId, String content) {
        Bundle args = new Bundle();
        CompareFragment fragment = new CompareFragment();
        args.putInt(ENGLISH_ID, englishId);
        args.putString(CONTENT, content);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public int initContentView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return R.layout.fragment_compare;
    }

    @Override
    public int initVariableId() {
        return BR.viewModel;
    }

    @Override
    public Class<CompareViewModel> getViewModelClazz() {
        return CompareViewModel.class;
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
        if (getActivity() != null && getActivity() instanceof SpeechActivity) {
            SpeechActivity activity = (SpeechActivity) getActivity();
            activity.setSpeechDataUpdated(this);
        }
        initStandardLineChart();
        initTestLineChart();
        clearStandardData();
        clearTestData();
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
            clearStandardData();
            clearTestData();
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
            JSONArray standardPoints = jsonObject.getJSONArray("d1");
            for (int i = 0; i < standardPoints.length(); i++) {
                mStandardPoints.add(standardPoints.getDouble(i));
            }
            JSONArray standardSplits = jsonObject.getJSONArray("step1");
            for (int i = 0; i < standardSplits.length(); i++) {
                mStandardSplits.add(standardSplits.getInt(i));
            }
            JSONArray testPoints = jsonObject.getJSONArray("d2");
            for (int i = 0; i < testPoints.length(); i++) {
                mTestPoints.add(testPoints.getDouble(i));
            }
            JSONArray testSplits = jsonObject.getJSONArray("step2");
            for (int i = 0; i < testSplits.length(); i++) {
                mTestSplits.add(testSplits.getInt(i));
            }
            Collections.sort(mStandardSplits);
            Collections.sort(mTestSplits);
            setStandardData();
            setTestData();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void initStandardLineChart() {
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

    private void initTestLineChart() {
        getBinding().lcTest.getDescription().setEnabled(false);
        getBinding().lcTest.setDrawGridBackground(false);
        getBinding().lcTest.setBackgroundColor(Color.TRANSPARENT);
        getBinding().lcTest.setDragEnabled(true);
        getBinding().lcTest.setScaleEnabled(true);
        getBinding().lcTest.setPinchZoom(true);
        getBinding().lcTest.setHighlightPerDragEnabled(true);
        getBinding().lcTest.animateX(1000);
        getBinding().lcTest.setTouchEnabled(true);
        getBinding().lcTest.setDragDecelerationEnabled(true);
        getBinding().lcTest.setDragDecelerationFrictionCoef(0.9f);
        Legend legend = getBinding().lcTest.getLegend();
        legend.setForm(Legend.LegendForm.LINE);
        legend.setTextColor(Color.BLACK);

        // YAxis: leftAxis
        XAxis xAxis = getBinding().lcTest.getXAxis();
        xAxis.setTextColor(Color.rgb(0, 0, 0));
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setTextSize(10f);
        xAxis.setDrawLabels(true);
        xAxis.setDrawGridLines(false);
        xAxis.setGranularityEnabled(true);

        YAxis leftAxis = getBinding().lcTest.getAxisLeft();
        leftAxis.setTextColor(Color.rgb(0, 0, 0));
        leftAxis.setTextSize(10f);
        leftAxis.setAxisMaximum(0.6f);
        leftAxis.setAxisMinimum(-0.6f);
        leftAxis.setDrawGridLines(false);
        leftAxis.setGranularityEnabled(true);

        // YAxis: rightAxis
        YAxis rightAxis = getBinding().lcTest.getAxisRight();
        rightAxis.setEnabled(false);
    }

    private void setStandardData() {
        final ArrayList<Entry> points = new ArrayList<>();
        final ArrayList<Entry> splits = new ArrayList<>();
        final ArrayList<Entry> positions = new ArrayList<>();
        final ArrayList<String> words = new ArrayList<>();

        for (int i = 0; i < mStandardPoints.size(); i++) {
            points.add(new Entry(i, mStandardPoints.get(i).floatValue(), i + "F"));
        }

        for (int i = 0; i < mStandardSplits.size(); i++) {
            if (i % 2 == 0) {
                splits.add(new Entry(mStandardSplits.get(i), 1f));
                splits.add(new Entry(mStandardSplits.get(i), -1f));
            } else {
                splits.add(new Entry(mStandardSplits.get(i), -1f));
                splits.add(new Entry(mStandardSplits.get(i), 1f));
            }
        }

        for (int i = 0; i < mStandardSplits.size(); i++) {
            if (i % 2 == 1) {
                int position = (mStandardSplits.get(i - 1) + mStandardSplits.get(i)) / 2;
                positions.add(new Entry(position, POSITION_LINE));
                mStandardPositions.add(position);
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
        getBinding().lcStandard.setVisibleXRangeMaximum(mStandardPoints.size());
        getBinding().lcStandard.fitScreen();
        getBinding().lcStandard.setData(data);

        // words的长度和mPositions要一致
        int size = mWords.size();
        if (size <= mStandardPositions.size()) {
            words.addAll(mWords);
            for (int i = size; i < mStandardPositions.size(); i++) {
                words.add("Nil");
            }
        } else {
            for (int i = 0; i < mStandardPositions.size(); i++) {
                words.add(mWords.get(i));
            }
        }

        CustomMarkerView cmv = new CustomMarkerView(ContextUtils.getContext(), mStandardPositions, words, POSITION_LINE, Color.GREEN);
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

    private void setTestData() {
        final ArrayList<Entry> points = new ArrayList<>();
        final ArrayList<Entry> splits = new ArrayList<>();
        final ArrayList<Entry> positions = new ArrayList<>();
        final ArrayList<String> words = new ArrayList<>();

        for (int i = 0; i < mTestPoints.size(); i++) {
            points.add(new Entry(i, mTestPoints.get(i).floatValue(), i + "F"));
        }

        for (int i = 0; i < mTestSplits.size(); i++) {
            if (i % 2 == 0) {
                splits.add(new Entry(mTestSplits.get(i), 1f));
                splits.add(new Entry(mTestSplits.get(i), -1f));
            } else {
                splits.add(new Entry(mTestSplits.get(i), -1f));
                splits.add(new Entry(mTestSplits.get(i), 1f));
            }
        }

        for (int i = 0; i < mTestSplits.size(); i++) {
            if (i % 2 == 1) {
                int position = (mTestSplits.get(i - 1) + mTestSplits.get(i)) / 2;
                positions.add(new Entry(position, POSITION_LINE));
                mTestPositions.add(position);
            }
        }

        LineDataSet pointsDataSet = new LineDataSet(points, getString(R.string.test_line));
        pointsDataSet.setAxisDependency(YAxis.AxisDependency.LEFT);
        pointsDataSet.setColor(Color.RED);
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
        positionsDataSet.setCircleColor(Color.RED);
        positionsDataSet.setCircleHoleColor(Color.WHITE);
        positionsDataSet.setCircleRadius(4f);

        LineData data = new LineData(pointsDataSet, splitsDataSet, positionsDataSet);
        getBinding().lcTest.setVisibleXRangeMaximum(mTestPoints.size());
        getBinding().lcTest.fitScreen();
        getBinding().lcTest.setData(data);

        // words的长度和mPositions要一致
        int size = mWords.size();
        if (size <= mTestPositions.size()) {
            words.addAll(mWords);
            for (int i = size; i < mTestPositions.size(); i++) {
                words.add("Nil");
            }
        } else {
            for (int i = 0; i < mTestPositions.size(); i++) {
                words.add(mWords.get(i));
            }
        }

        CustomMarkerView cmv = new CustomMarkerView(ContextUtils.getContext(), mTestPositions, words, POSITION_LINE, Color.RED);
        cmv.setChartView(getBinding().lcTest);
        getBinding().lcTest.setMarker(cmv);

        XAxis xAxis = getBinding().lcTest.getXAxis();
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

        getBinding().lcTest.notifyDataSetChanged();
        getBinding().lcTest.invalidate();
    }

    private void clearStandardData() {
        mStandardPoints.clear();
        mStandardSplits.clear();
        mStandardPositions.clear();
        if (getBinding().lcStandard.getData() != null) {
            getBinding().lcStandard.getData().clearValues();
        }
        getBinding().lcStandard.notifyDataSetChanged();
        getBinding().lcStandard.clear();
        getBinding().lcStandard.invalidate();
    }

    private void clearTestData() {
        mTestPoints.clear();
        mTestSplits.clear();
        mTestPositions.clear();
        if (getBinding().lcTest.getData() != null) {
            getBinding().lcTest.getData().clearValues();
        }
        getBinding().lcTest.notifyDataSetChanged();
        getBinding().lcTest.clear();
        getBinding().lcTest.invalidate();
    }
}
