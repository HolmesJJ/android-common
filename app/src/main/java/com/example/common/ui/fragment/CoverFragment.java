package com.example.common.ui.fragment;

import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.common.BR;
import com.example.common.R;
import com.example.common.base.BaseFragment;
import com.example.common.databinding.FragmentCoverBinding;
import com.example.common.ui.viewmodel.CoverViewModel;

public class CoverFragment extends BaseFragment<FragmentCoverBinding, CoverViewModel> {

    private static final String TAG = CoverFragment.class.getSimpleName();

    public static CoverFragment newInstance() {
        return new CoverFragment();
    }

    @Override
    public int initContentView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return R.layout.fragment_cover;
    }

    @Override
    public int initVariableId() {
        return BR.viewModel;
    }

    @Override
    public Class<CoverViewModel> getViewModelClazz() {
        return CoverViewModel.class;
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
