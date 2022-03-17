package com.example.common;

import android.util.Log;
import androidx.annotation.NonNull;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.MutableLiveData;

import com.example.common.api.ApiClient;
import com.example.common.api.model.main.TaskResult;
import com.example.common.api.model.main.TasksResult;
import com.example.common.api.model.token.RefreshTokenResult;
import com.example.common.base.BaseViewModel;
import com.example.common.model.main.Task;
import com.example.common.network.http.ResponseCode;
import com.example.common.network.http.Result;
import com.example.common.thread.ThreadManager;
import com.example.common.utils.RefreshTokenUtils;
import com.example.common.utils.ToastUtils;

import java.util.List;
import java.util.stream.Collectors;

public class MainViewModel extends BaseViewModel {

    private static final String TAG = MainViewModel.class.getSimpleName();

    private final MutableLiveData<List<Task>> mTasks = new MutableLiveData<>();
    private final MutableLiveData<Integer> mProgress = new MutableLiveData<>();
    private final MutableLiveData<Boolean> mIsShowLoading = new MutableLiveData<>();

    @Override
    public void onCreate(@NonNull LifecycleOwner owner) {

    }

    @Override
    public void onDestroy(@NonNull LifecycleOwner owner) {

    }

    public MutableLiveData<List<Task>> getTasks() {
        return mTasks;
    }

    public MutableLiveData<Integer> getProgress() {
        return mProgress;
    }

    public MutableLiveData<Boolean> isShowLoading() {
        return mIsShowLoading;
    }

    public void initData() {
        mIsShowLoading.postValue(true);
        ThreadManager.getThreadPollProxy().execute(new Runnable() {
            @Override
            public void run() {
                Result<TasksResult> tasksResult = ApiClient.getTasks();
                if (tasksResult.isTokenTimeout() || tasksResult.isForbidden()) {
                    RefreshTokenUtils.refreshToken();
                    mIsShowLoading.postValue(false);
                    initData();
                    return;
                }
                if (!tasksResult.isSuccess()) {
                    mIsShowLoading.postValue(false);
                    ToastUtils.showShortSafe("Get Tasks Failed");
                    return;
                }
                TasksResult tasksResultBody = tasksResult.getBody(TasksResult.class);
                List<TaskResult> taskResultList = tasksResultBody.getList();
                if (taskResultList != null && taskResultList.size() > 0) {
                    List<Task> taskList = taskResultList.stream().map(taskResult -> new Task(
                            taskResult.getEnglishId(),
                            taskResult.getContent(),
                            taskResult.getProgressTimes() != 0)).collect(Collectors.toList()
                    );
                    int count = (int) taskResultList.stream().filter(taskResult ->
                            taskResult.getProgressTimes() != 0
                    ).count();
                    mTasks.postValue(taskList);
                    int progress = (int) ((double) count / taskResultList.size() * 100);
                    mProgress.postValue(progress);
                }
                mIsShowLoading.postValue(false);
            }
        });
    }
}
