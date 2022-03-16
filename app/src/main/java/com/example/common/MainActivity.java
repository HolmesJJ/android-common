package com.example.common;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.recyclerview.widget.LinearLayoutManager;

import com.bumptech.glide.Glide;
import com.example.common.adapter.main.DateAdapter;
import com.example.common.adapter.main.TaskAdapter;
import com.example.common.base.BaseActivity;
import com.example.common.config.Config;
import com.example.common.constants.Constants;
import com.example.common.databinding.ActivityMainBinding;
import com.example.common.listener.OnMultiClickListener;
import com.example.common.model.main.DateOfMonth;
import com.example.common.model.main.Task;
import com.example.common.utils.ContextUtils;
import com.example.common.utils.DateUtils;
import com.example.common.utils.ListenerUtils;

import java.util.List;

public class MainActivity extends BaseActivity<ActivityMainBinding, MainViewModel> {

    private static final String TAG = MainActivity.class.getSimpleName();

    private long mCurrentTime = System.currentTimeMillis();
    private int mSelectedDate;
    private DateAdapter mDateAdapter;

    private List<Task> mTasks;
    private TaskAdapter mTaskAdapter;

    @Override
    public int initContentView(Bundle savedInstanceState) {
        return R.layout.activity_main;
    }

    @Override
    public int initVariableId() {
        return BR.viewModel;
    }

    @Override
    public Class<MainViewModel> getViewModelClazz() {
        return MainViewModel.class;
    }

    @Override
    public void initData() {
        super.initData();
        initCalendar();
        String avatar = Constants.HTTPS_SERVER_URL + "images/profile/" + Config.getUserId() + ".png";
        Glide.with(this).load(avatar).circleCrop().into(getBinding().rivAvatar);
    }

    @Override
    public void initViewObservable() {
        super.initViewObservable();
        setOnClickListener();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void setOnClickListener() {
        ListenerUtils.setOnClickListener(getBinding().ivLeft, new OnMultiClickListener() {
            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void onMultiClick(View v) {
                if (mDateAdapter == null) {
                    return;
                }
                mCurrentTime = DateUtils.lastMonth(mCurrentTime);
                String monthYear = DateUtils.getMonth(mCurrentTime) + " " + DateUtils.getYear(mCurrentTime);
                getBinding().tvCurrentMonth.setText(monthYear);
                List<DateOfMonth> datesOfMonth = DateUtils.getDatesOfMonth(mCurrentTime);
                mDateAdapter.setData(datesOfMonth);
                mDateAdapter.notifyDataSetChanged();
                isRightVisited();
            }
        });
        ListenerUtils.setOnClickListener(getBinding().ivRight, new OnMultiClickListener() {
            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void onMultiClick(View v) {
                mCurrentTime = DateUtils.nextMonth(mCurrentTime);
                String monthYear = DateUtils.getMonth(mCurrentTime) + " " + DateUtils.getYear(mCurrentTime);
                getBinding().tvCurrentMonth.setText(monthYear);
                List<DateOfMonth> datesOfMonth = DateUtils.getDatesOfMonth(mCurrentTime);
                mDateAdapter.setData(datesOfMonth);
                mDateAdapter.notifyDataSetChanged();
                isRightVisited();
            }
        });
    }

    private void initCalendar() {
        String monthYear = DateUtils.getMonth(mCurrentTime) + " " + DateUtils.getYear(mCurrentTime);
        getBinding().tvCurrentMonth.setText(monthYear);
        List<DateOfMonth> datesOfMonth = DateUtils.getDatesOfMonth(mCurrentTime);
        mDateAdapter = new DateAdapter(ContextUtils.getContext(), datesOfMonth, new DateAdapter.OnItemListener() {
            @Override
            public void onItemListener(int position) {
                mSelectedDate = position + 1;
            }
        });
        getBinding().rvDates.setAdapter(mDateAdapter);
        LinearLayoutManager layoutManager = new LinearLayoutManager(ContextUtils.getContext());
        layoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
        getBinding().rvDates.setLayoutManager(layoutManager);
    }

    private void isRightVisited() {
        if (DateUtils.isSameYear(mCurrentTime) && DateUtils.isSameMonth(mCurrentTime)) {
            getBinding().ivRight.setClickable(false);
            getBinding().ivRight.setVisibility(View.INVISIBLE);
        } else {
            getBinding().ivRight.setVisibility(View.VISIBLE);
            getBinding().ivRight.setClickable(true);
        }
    }
}
