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
    private final String content;

    public ChartAdapter(FragmentManager fragmentManager, int englishId, String content) {
        super(fragmentManager);
        this.englishId = englishId;
        this.content = content;
    }

    @NonNull
    @Override
    public Fragment getItem(int position) {
        switch (position) {
            case 1:
                return ResultFragment.newInstance(englishId, content);
            case 2:
                return StandardFragment.newInstance(englishId, content);
            case 3:
                return TestFragment.newInstance(englishId, content);
            case 4:
                return CompareFragment.newInstance(englishId, content);
            default:
                return CoverFragment.newInstance();
        }
    }

    @Override
    public int getCount() {
        return PAGE_SIZE;
    }
}
