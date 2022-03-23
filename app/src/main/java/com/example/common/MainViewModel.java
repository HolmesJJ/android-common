package com.example.common;

import android.util.Pair;

import androidx.annotation.NonNull;
import androidx.databinding.ViewDataBinding;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.MutableLiveData;

import com.example.common.api.ApiClient;
import com.example.common.api.model.main.DownloadParameter;
import com.example.common.api.model.main.DownloadResult;
import com.example.common.api.model.main.TaskResult;
import com.example.common.api.model.main.TasksResult;
import com.example.common.base.BaseActivity;
import com.example.common.base.BaseViewModel;
import com.example.common.model.main.Task;
import com.example.common.network.http.Result;
import com.example.common.thread.ThreadManager;
import com.example.common.ui.activity.SectionActivity;
import com.example.common.utils.FileUtils;
import com.example.common.utils.RefreshTokenUtils;
import com.example.common.utils.ToastUtils;

import java.io.File;
import java.util.List;
import java.util.stream.Collectors;

public class MainViewModel extends BaseViewModel {

    private static final String TAG = MainViewModel.class.getSimpleName();

    private final MutableLiveData<Pair<Class<? extends BaseActivity<? extends ViewDataBinding, ? extends BaseViewModel>>, Pair<Integer, String>>> mActivityAction = new MutableLiveData<>();
    private final MutableLiveData<List<Task>> mTasks = new MutableLiveData<>();
    private final MutableLiveData<Integer> mProgress = new MutableLiveData<>();
    private final MutableLiveData<Boolean> mIsShowLoading = new MutableLiveData<>();

    @Override
    public void onCreate(@NonNull LifecycleOwner owner) {

    }

    @Override
    public void onDestroy(@NonNull LifecycleOwner owner) {

    }

    public MutableLiveData<Pair<Class<? extends BaseActivity<? extends ViewDataBinding, ? extends BaseViewModel>>, Pair<Integer, String>>> getActivityAction() {
        return mActivityAction;
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

    public void download(int englishId, String content, List<DownloadParameter> downloadParameters) {
        mIsShowLoading.postValue(true);
        ThreadManager.getThreadPollProxy().execute(new Runnable() {
            @Override
            public void run() {
                for (int i = 0; i < downloadParameters.size(); i++) {
                    String folder = downloadParameters.get(i).getFolder();
                    String file = downloadParameters.get(i).getFile();
                    String path = downloadParameters.get(i).getPath();
                    File f = new File(folder, file);
                    if (f.exists()) {
                        continue;
                    }
                    Result<DownloadResult> downloadResult = ApiClient.download(folder, file, path);
                    if (downloadResult.isTokenTimeout() || downloadResult.isForbidden()) {
                        RefreshTokenUtils.refreshToken();
                        mIsShowLoading.postValue(false);
                        download(englishId, content, downloadParameters);
                        return;
                    }
                    if (!downloadResult.isSuccess()) {
                        ToastUtils.showShortSafe( "Download " + file + " Failed");
                        return;
                    }
                    if (file.contains(".zip")) {
                        FileUtils.unzip(f, new File(folder));
                    }
                }
                mIsShowLoading.postValue(false);
                mActivityAction.postValue(new Pair<>(SectionActivity.class, new Pair<>(englishId, content)));
            }
        });
    }
}
