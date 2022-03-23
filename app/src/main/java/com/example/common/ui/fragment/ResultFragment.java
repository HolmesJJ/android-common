package com.example.common.ui.fragment;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.Nullable;

import com.example.common.BR;
import com.example.common.R;
import com.example.common.base.BaseFragment;
import com.example.common.config.Config;
import com.example.common.databinding.FragmentResultBinding;
import com.example.common.listener.OnMultiClickListener;
import com.example.common.player.IMediaPlayer;
import com.example.common.player.SlackAudioPlayer;
import com.example.common.ui.activity.SpeechActivity;
import com.example.common.ui.viewmodel.ResultViewModel;
import com.example.common.ui.widget.RangeSeekBar;
import com.example.common.ui.widget.speech.CustomMarkerView;
import com.example.common.utils.ContextUtils;
import com.example.common.utils.FileUtils;
import com.example.common.utils.ListenerUtils;
import com.example.common.utils.ToastUtils;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.ValueFormatter;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class ResultFragment extends BaseFragment<FragmentResultBinding, ResultViewModel>
        implements SpeechActivity.ISpeechDataUpdated {

    private static final String TAG = ResultFragment.class.getSimpleName();
    private static final String ENGLISH_ID = "englishId";
    private static final String CONTENT = "content";
    private static final float BASE_LINE = 4.8f;
    private static final float POSITION_LINE = 15f;
    private static final double PASS = 0.8;

    private final List<Double> mPoints = new ArrayList<>();
    private final List<Integer> mSplits = new ArrayList<>();
    private final List<Integer> mPositions = new ArrayList<>();
    private final List<Double> mScores = new ArrayList<>();
    private final List<String> mWords = new ArrayList<>(); // No need to clear

    private int mEnglishId;
    private String mContent;
    private double mFinalScore;

    SlackAudioPlayer mSlackAudioPlayer;

    public static ResultFragment newInstance(int englishId, String content) {
        Bundle args = new Bundle();
        ResultFragment fragment = new ResultFragment();
        args.putInt(ENGLISH_ID, englishId);
        args.putString(CONTENT, content);
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
            mContent = bundle.getString(CONTENT);
            mWords.clear();
            try {
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
        initLineChart();
        clearData();
        initSpeechData();
    }

    @Override
    public void initViewObservable() {
        super.initViewObservable();
        setObserveListener();
        setOnClickListener();
        getBinding().rsbProgress.setOnRangeChangedListener(new RangeSeekBar.OnRangeChangedListener() {
            @Override
            public void onRangeChanged(RangeSeekBar view, float min, float max, boolean isFromUser, boolean changeFinished) {
                pausePlayer();
                if(isFromUser && changeFinished && mSlackAudioPlayer != null && !mSlackAudioPlayer.isPlaying()) {
                    mSlackAudioPlayer.updateRange(min, max);
                }
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.i(TAG, "onResume");
        initPlayer();
        if (getActivity() != null && getActivity() instanceof SpeechActivity) {
            SpeechActivity activity = (SpeechActivity) getActivity();
            activity.setScanScroll(false);
        }
    }

    @Override
    public void onPause() {
        Log.i(TAG, "onPause");
        releasePlayer();
        super.onPause();
    }

    @Override
    public void onDestroy() {
        releasePlayer();
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

    private void setOnClickListener() {
        ListenerUtils.setOnClickListener(getBinding().rivPlay, new OnMultiClickListener() {
            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void onMultiClick(View v) {
                // TODO 需要修改一个BUG：若在移动了进度条后播放，则只能播放一次，即每次播放前都要移动一次进度条
                startPlayer();
            }
        });
        ListenerUtils.setOnClickListener(getBinding().ivForward, new OnMultiClickListener() {
            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void onMultiClick(View v) {
                if (getActivity() != null && getActivity() instanceof SpeechActivity) {
                    SpeechActivity activity = (SpeechActivity) getActivity();
                    activity.forward();
                    activity.setScanScroll(true);
                }
            }
        });
        ListenerUtils.setOnClickListener(getBinding().ivBackward, new OnMultiClickListener() {
            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void onMultiClick(View v) {
                if (getActivity() != null && getActivity() instanceof SpeechActivity) {
                    SpeechActivity activity = (SpeechActivity) getActivity();
                    activity.backward();
                    activity.setScanScroll(true);
                }
            }
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
            JSONArray points = jsonObject.getJSONArray("diff");
            for (int i = 0; i < points.length(); i++) {
                mPoints.add(points.getDouble(i));
            }
            JSONArray splits = jsonObject.getJSONArray("stepDiff");
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
        final ArrayList<Entry> splits = new ArrayList<>();
        final ArrayList<Entry> positions = new ArrayList<>();
        final ArrayList<Integer> colors = new ArrayList<>();

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
            if (i % 2 == 1) {
                int position = (mSplits.get(i - 1) + mSplits.get(i)) / 2;
                positions.add(new Entry(position, POSITION_LINE));
                mPositions.add(position);

                double actualScore = 0;
                double totalScore = 0;
                for (int j = mSplits.get(i - 1); j < mSplits.get(i); j++) {
                    totalScore++;
                    if (mPoints.get(j) <= BASE_LINE) {
                        actualScore++;
                    }
                }
                mScores.add(actualScore / totalScore);
            }
        }
        Log.i(TAG, "Splits: " + mSplits.toString() + ", Scores: " + mScores.toString());
        mFinalScore = mScores.stream().mapToDouble(Double::doubleValue).average().orElse(0);
        int color = mFinalScore > PASS ? Color.GREEN : Color.RED;

        LineDataSet pointsDataSet = new LineDataSet(points, getString(R.string.difference_line));
        pointsDataSet.setAxisDependency(YAxis.AxisDependency.LEFT);
        pointsDataSet.setColors(colors);
        pointsDataSet.setLineWidth(1f);
        pointsDataSet.setDrawCircles(false);
        pointsDataSet.setDrawValues(false);

        LineDataSet splitsDataSet = new LineDataSet(splits, getString(R.string.split_line));
        splitsDataSet.setColor(Color.rgb(0, 255, 255));
        splitsDataSet.setLineWidth(1f);
        splitsDataSet.setDrawCircles(false);
        splitsDataSet.setDrawValues(false);

        LineDataSet basePointsDataSet = new LineDataSet(basePoints, getString(R.string.base_line));
        basePointsDataSet.setColor(Color.rgb(255, 208, 0));
        basePointsDataSet.setLineWidth(1f);
        basePointsDataSet.setDrawCircles(false);
        basePointsDataSet.setDrawValues(false);

        LineDataSet positionsDataSet = new LineDataSet(positions, "");
        positionsDataSet.setColor(Color.TRANSPARENT);
        positionsDataSet.setLineWidth(1f);
        positionsDataSet.setCircleColor(color);
        positionsDataSet.setCircleHoleColor(Color.WHITE);
        positionsDataSet.setCircleRadius(4f);

        LineData data = new LineData(pointsDataSet, splitsDataSet, basePointsDataSet, positionsDataSet);
        getBinding().lcResult.setVisibleXRangeMaximum(mPoints.size());
        getBinding().lcResult.fitScreen();
        getBinding().lcResult.setData(data);

        Log.i(TAG, "Words: " + mWords.toString());
        List<String> mWordScores = IntStream.range(0, mWords.size())
                .mapToObj(i -> mWords.get(i) + ": " + String.format(Locale.US, "%.2f", mScores.get(i) * 100) + "%")
                .collect(Collectors.toList());

        Log.i(TAG, "Words and Scores: " + mWordScores.toString());
        CustomMarkerView cmv = new CustomMarkerView(ContextUtils.getContext(), mPositions, mWordScores, POSITION_LINE, color);
        cmv.setChartView(getBinding().lcResult);
        getBinding().lcResult.setMarker(cmv);

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
        mScores.clear();
        mPositions.clear();
        if (getBinding().lcResult.getData() != null) {
            getBinding().lcResult.getData().clearValues();
        }
        getBinding().lcResult.notifyDataSetChanged();
        getBinding().lcResult.clear();
        getBinding().lcResult.invalidate();
    }

    private void initPlayer() {
        File audioFile = new File(FileUtils.AUDIO_DIR + mEnglishId + "/record.mp3");
        if (!audioFile.exists() || !audioFile.isFile()) {
            getBinding().rivPlay.setImageResource(R.drawable.ic_unplay);
            getBinding().rivPlay.setEnabled(false);
            getBinding().rivPlay.setClickable(false);
            ToastUtils.showShortSafe("File not found");
            return;
        }
        try {
            mSlackAudioPlayer = new SlackAudioPlayer(ContextUtils.getContext());
            mSlackAudioPlayer.setDataSource(audioFile.getAbsolutePath());
            mSlackAudioPlayer.setOnMusicDurationListener(new IMediaPlayer.OnMusicDurationListener() {
                @Override
                public void onMusicDuration(IMediaPlayer mp, float duration) {
                    getBinding().rsbProgress.setRange(0, duration);
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

    private void showLoading() {
        if (getActivity() != null && getActivity() instanceof SpeechActivity) {
            SpeechActivity activity = (SpeechActivity) getActivity();
            activity.showLoading(false);
        }
    }

    private void stopLoading() {
        stopLoading(0);
    }

    private void stopLoading(long millisecond) {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if (getActivity() != null && getActivity() instanceof SpeechActivity) {
                    SpeechActivity activity = (SpeechActivity) getActivity();
                    activity.stopLoading();
                }
            }
        }, millisecond);
    }
}
