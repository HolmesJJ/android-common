package com.example.common.ui.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.Nullable;

import com.example.common.BR;
import com.example.common.R;
import com.example.common.base.BaseFragment;
import com.example.common.databinding.FragmentTestBinding;
import com.example.common.ui.activity.SpeechActivity;
import com.example.common.ui.viewmodel.TestViewModel;

public class TestFragment extends BaseFragment<FragmentTestBinding, TestViewModel>
        implements SpeechActivity.ISpeechDataUpdated {

    private static final String TAG = TestFragment.class.getSimpleName();
    private static final String ENGLISH_ID = "englishId";

    private int mEnglishId;

    public static TestFragment newInstance(int englishId) {
        Bundle args = new Bundle();
        TestFragment fragment = new TestFragment();
        args.putInt(ENGLISH_ID, englishId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public int initContentView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return R.layout.fragment_test;
    }

    @Override
    public int initVariableId() {
        return BR.viewModel;
    }

    @Override
    public Class<TestViewModel> getViewModelClazz() {
        return TestViewModel.class;
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
            initSpeechData();
        });
    }

    private void initSpeechData() {

    }
}
