package com.example.common.ui.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.Nullable;

import com.example.common.BR;
import com.example.common.R;
import com.example.common.base.BaseFragment;
import com.example.common.databinding.FragmentResultBinding;
import com.example.common.ui.viewmodel.ResultViewModel;

public class ResultFragment extends BaseFragment<FragmentResultBinding, ResultViewModel> {

    private static final String TAG = ResultFragment.class.getSimpleName();
    private static final String ENGLISH_ID = "englishId";

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
    }

    @Override
    public void initViewObservable() {
        super.initViewObservable();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }
}
