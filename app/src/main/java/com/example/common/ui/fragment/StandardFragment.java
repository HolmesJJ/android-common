package com.example.common.ui.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.Nullable;

import com.example.common.BR;
import com.example.common.R;
import com.example.common.base.BaseFragment;
import com.example.common.databinding.FragmentStandardBinding;
import com.example.common.ui.viewmodel.StandardViewModel;

public class StandardFragment extends BaseFragment<FragmentStandardBinding, StandardViewModel> {

    private static final String TAG = StandardFragment.class.getSimpleName();

    public static StandardFragment newInstance() {
        return new StandardFragment();
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
    public void initParam() {
        super.initParam();
    }

    @Override
    public void initData() {
        super.initData();
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
