package com.example.common.ui.fragment;

import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.Nullable;

import com.example.common.BR;
import com.example.common.R;
import com.example.common.base.BaseFragment;
import com.example.common.config.Config;
import com.example.common.databinding.FragmentResultBinding;
import com.example.common.ui.activity.SpeechActivity;
import com.example.common.ui.viewmodel.ResultViewModel;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;
import com.github.mikephil.charting.formatter.ValueFormatter;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class ResultFragment extends BaseFragment<FragmentResultBinding, ResultViewModel>
        implements SpeechActivity.ISpeechDataUpdated {

    private static final String TAG = ResultFragment.class.getSimpleName();
    private static final String ENGLISH_ID = "englishId";
    private static final float BASE_LINE = 5.0f;

    private final List<Double> mPoints = new ArrayList<>();
    private final List<Integer> mSplits = new ArrayList<>();

    private int mEnglishId;

    public static ResultFragment newInstance(int englishId) {
        Bundle args = new Bundle();
        ResultFragment fragment = new ResultFragment();
        args.putInt(ENGLISH_ID, englishId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public int initContentView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return R.layout.fragment_result;
    }

    @Override
    public int initVariableId() {
        return BR.viewModel;
    }

    @Override
    public Class<ResultViewModel> getViewModelClazz() {
        return ResultViewModel.class;
    }

    @Override
    public void initData() {
        super.initData();
        Bundle bundle = this.getArguments();
        if (bundle != null) {
            mEnglishId = bundle.getInt(ENGLISH_ID);
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
            JSONArray diff = jsonObject.getJSONArray("diff");
            for (int i = 0; i < diff.length(); i++) {
                mPoints.add(diff.getDouble(i));
            }
            JSONArray stepDiff = jsonObject.getJSONArray("stepDiff");
            for (int i = 0; i < stepDiff.length(); i++) {
                mSplits.add(stepDiff.getInt(i));
            }
            Collections.sort(mSplits);
            setData();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void initLineChart() {
        getBinding().lcResult.getDescription().setEnabled(false);
        getBinding().lcResult.setDrawGridBackground(false);
        getBinding().lcResult.setBackgroundColor(Color.TRANSPARENT);
        getBinding().lcResult.setDragEnabled(true);
        getBinding().lcResult.setScaleEnabled(true);
        getBinding().lcResult.setPinchZoom(true);
        getBinding().lcResult.setHighlightPerDragEnabled(true);
        getBinding().lcResult.animateX(1000);
        getBinding().lcResult.setTouchEnabled(true);
        getBinding().lcResult.setDragDecelerationEnabled(true);
        getBinding().lcResult.setDragDecelerationFrictionCoef(0.9f);
        Legend legend = getBinding().lcResult.getLegend();
        legend.setEnabled(false);

        // XAxis
        XAxis xAxis = getBinding().lcResult.getXAxis();
        xAxis.setTextColor(Color.rgb(0, 0, 0));
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setTextSize(10f);
        xAxis.setDrawLabels(true);
        xAxis.setDrawGridLines(false);
        xAxis.setGranularityEnabled(true);

        // YAxis: leftAxis
        YAxis leftAxis = getBinding().lcResult.getAxisLeft();
        leftAxis.setTextColor(Color.rgb(0, 0, 0));
        leftAxis.setTextSize(10f);
        leftAxis.setAxisMaximum(20f);
        leftAxis.setAxisMinimum(0f);
        leftAxis.setDrawGridLines(false);
        leftAxis.setGranularityEnabled(true);

        // YAxis: rightAxis
        YAxis rightAxis = getBinding().lcResult.getAxisRight();
        rightAxis.setEnabled(false);
    }

    private void setData() {
        final ArrayList<Entry> points = new ArrayList<>();
        final ArrayList<Entry> basePoints = new ArrayList<>();
        final ArrayList<Integer> colors = new ArrayList<>();
        final ArrayList<Entry> splits = new ArrayList<>();

        for (int i = 0; i < mPoints.size(); i++) {
            basePoints.add(new Entry(i, BASE_LINE, ""));
            points.add(new Entry(i, mPoints.get(i).floatValue(), i + "F"));
            if (mPoints.get(i) > BASE_LINE) {
                if (i > 0 && mPoints.get(i - 1) <= BASE_LINE) {
                    colors.set(i - 1, Color.RED);
                }
                colors.add(Color.RED);
            } else {
                colors.add(Color.GREEN);
            }
        }

        for (int i = 0; i < mSplits.size(); i++) {
            if (i % 2 == 0) {
                splits.add(new Entry(mSplits.get(i), 21f));
                splits.add(new Entry(mSplits.get(i), -1f));
            } else {
                splits.add(new Entry(mSplits.get(i), -1f));
                splits.add(new Entry(mSplits.get(i), 21f));
            }
        }

        for (int i = 0; i < mSplits.size(); i++) {
            if(i % 2 == 1) {
                int position = (mSplits.get(i - 1) + mSplits.get(i)) / 2;
                double actualScore = 0;
                double totalScore = 0;
                for (int j = mSplits.get(i-1); j < mSplits.get(i); j++) {
                    totalScore++;
                    if(mPoints.get(j) <= 6) {
                        actualScore++;
                    }
                }
            }
        }

        LineDataSet pointsDataSet = new LineDataSet(points, "Standard Line");
        pointsDataSet.setAxisDependency(YAxis.AxisDependency.LEFT);
        pointsDataSet.setColors(colors);
        pointsDataSet.setLineWidth(1f);
        pointsDataSet.setDrawCircles(false);

        LineDataSet splitsDataSet = new LineDataSet(splits, "Split Line");
        splitsDataSet.setColor(Color.rgb(0, 255, 255));
        splitsDataSet.setLineWidth(1f);
        splitsDataSet.setDrawCircles(false);
        splitsDataSet.setDrawValues(false);

        LineDataSet basePointsDataSet = new LineDataSet(basePoints, "Base Line");
        basePointsDataSet.setColor(Color.rgb(255, 208, 0));
        basePointsDataSet.setLineWidth(1f);
        basePointsDataSet.setDrawCircles(false);
        basePointsDataSet.setDrawValues(false);

        LineData data = new LineData(pointsDataSet, splitsDataSet, basePointsDataSet);
        getBinding().lcResult.setVisibleXRangeMaximum(mPoints.size());
        getBinding().lcResult.fitScreen();
        getBinding().lcResult.setData(data);

        XAxis xAxis = getBinding().lcResult.getXAxis();
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
        getBinding().lcResult.notifyDataSetChanged();
        getBinding().lcResult.invalidate();
    }

    private void clearData() {
        mPoints.clear();
        mSplits.clear();
        if (getBinding().lcResult.getData() != null) {
            getBinding().lcResult.getData().clearValues();
        }
        getBinding().lcResult.notifyDataSetChanged();
        getBinding().lcResult.clear();
        getBinding().lcResult.invalidate();
    }
}
