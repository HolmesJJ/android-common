package com.example.common.adapter.speech;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

import com.example.common.ui.fragment.CompareFragment;
import com.example.common.ui.fragment.CoverFragment;
import com.example.common.ui.fragment.ResultFragment;
import com.example.common.ui.fragment.StandardFragment;
import com.example.common.ui.fragment.TestFragment;

public class ChartAdapter extends FragmentPagerAdapter {

    private static final int PAGE_SIZE = 5;

    private final int englishId;

    public ChartAdapter(FragmentManager fragmentManager, int englishId) {
        super(fragmentManager);
        this.englishId = englishId;
    }

    @NonNull
    @Override
    public Fragment getItem(int position) {
        switch (position) {
            case 1:
                return ResultFragment.newInstance(englishId);
            case 2:
                return StandardFragment.newInstance(englishId);
            case 3:
                return TestFragment.newInstance(englishId);
            case 4:
                return CompareFragment.newInstance(englishId);
            default:
                return CoverFragment.newInstance();
        }
    }

    @Override
    public int getCount() {
        return PAGE_SIZE;
    }
}
