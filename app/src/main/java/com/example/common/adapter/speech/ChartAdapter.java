package com.example.common.adapter.speech;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

import com.example.common.ui.fragment.speech.Speech;

public class ChartAdapter extends FragmentPagerAdapter {

    private static final int PAGE_SIZE = 4;

    public ChartAdapter(FragmentManager fragmentManager) {
        super(fragmentManager);
    }

    @NonNull
    @Override
    public Fragment getItem(int position) {
        return Speech.newInstance();
    }

    @Override
    public int getCount() {
        return PAGE_SIZE;
    }
}
