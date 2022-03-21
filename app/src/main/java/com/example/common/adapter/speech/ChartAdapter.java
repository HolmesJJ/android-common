package com.example.common.adapter.speech;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

import com.example.common.ui.fragment.CoverFragment;
import com.example.common.ui.fragment.ResultFragment;
import com.example.common.ui.fragment.StandardFragment;

public class ChartAdapter extends FragmentPagerAdapter {

    private static final int PAGE_SIZE = 3;

    public ChartAdapter(FragmentManager fragmentManager) {
        super(fragmentManager);
    }

    @NonNull
    @Override
    public Fragment getItem(int position) {
        switch (position) {
            case 1:
                return StandardFragment.newInstance();
            case 2:
                return ResultFragment.newInstance();
            default:
                return CoverFragment.newInstance();
        }
    }

    @Override
    public int getCount() {
        return PAGE_SIZE;
    }
}
