package com.example.common;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.LinearSmoothScroller;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.common.adapter.main.DateAdapter;
import com.example.common.adapter.main.TaskAdapter;
import com.example.common.base.BaseActivity;
import com.example.common.config.Config;
import com.example.common.constants.Constants;
import com.example.common.databinding.ActivityMainBinding;
import com.example.common.listener.OnMultiClickListener;
import com.example.common.model.main.DateOfMonth;
import com.example.common.utils.ContextUtils;
import com.example.common.utils.DateUtils;
import com.example.common.utils.ListenerUtils;
import com.example.common.utils.ToastUtils;

import java.util.List;

public class MainActivity extends BaseActivity<ActivityMainBinding, MainViewModel> {

    private static final String TAG = MainActivity.class.getSimpleName();

    private long mCurrentTime = System.currentTimeMillis();
    private int mSelectedDate;
    private DateAdapter mDateAdapter;
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
        String avatar = Constants.HTTPS_SERVER_URL + "images/profile/" + Config.getUserId() + ".png";
        Glide.with(this).load(avatar).circleCrop().into(getBinding().rivAvatar);
    }

    @Override
    public void initViewObservable() {
        super.initViewObservable();
        setObserveListener();
        setOnClickListener();
        doIsShowLoading();
    }

    @Override
    protected void onResume() {
        super.onResume();
        initCalendar();
        if (getViewModel() != null) {
            getViewModel().initData();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void setObserveListener() {
        getViewModel().getTasks().observe(this, tasks -> {
            if (tasks != null) {
                mTaskAdapter = new TaskAdapter(ContextUtils.getContext(), tasks, new TaskAdapter.OnItemListener() {
                    @Override
                    public void onItemListener(int position) {

                    }
                });
                getBinding().rvTasks.setAdapter(mTaskAdapter);
                LinearLayoutManager layoutManager = new LinearLayoutManager(ContextUtils.getContext());
                layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
                getBinding().rvTasks.setLayoutManager(layoutManager);
            }
        });
        getViewModel().getProgress().observe(this, progress -> {
            if (progress != null) {
                getBinding().bcvProgress.setProgress(progress);
            }
        });
    }

    private void setOnClickListener() {
        ListenerUtils.setOnClickListener(getBinding().gvJumpForFun, new OnMultiClickListener() {
            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void onMultiClick(View v) {
                String packageName = "com.debug_version.TiaoYiTiao";
                Intent launchIntent = getPackageManager().getLaunchIntentForPackage(packageName);
                if(launchIntent != null) {
                    startActivity(launchIntent);
                }
                else {
                    ToastUtils.showShortSafe("Launch Failed");
                }
            }
        });
        ListenerUtils.setOnClickListener(getBinding().gvCubeHub, new OnMultiClickListener() {
            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void onMultiClick(View v) {
                String packageName = "com.example.hp";
                Intent launchIntent = getPackageManager().getLaunchIntentForPackage(packageName);
                if(launchIntent != null) {
                    startActivity(launchIntent);
                }
                else {
                    ToastUtils.showShortSafe("Launch Failed");
                }
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

        RecyclerView.SmoothScroller smoothScroller = new LinearSmoothScroller(ContextUtils.getContext()) {
            @Override protected int getVerticalSnapPreference() {
                return LinearSmoothScroller.SNAP_TO_START;
            }
        };
        int pos = DateUtils.getDate(mCurrentTime) + 2;
        smoothScroller.setTargetPosition(Math.min(pos, datesOfMonth.size() - 1));
        layoutManager.startSmoothScroll(smoothScroller);
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

    /**
     * 控制进度圈显示
     */
    public void doIsShowLoading() {
        getViewModel().isShowLoading().observe(this, isShowing -> {
            if (isShowing) {
                showLoading(false);
            } else {
                stopLoading();
            }
        });
    }
}
