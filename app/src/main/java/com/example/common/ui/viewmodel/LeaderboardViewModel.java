package com.example.common.ui.viewmodel;

import androidx.annotation.NonNull;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.MutableLiveData;

import com.example.common.api.ApiClient;
import com.example.common.api.model.game.LeaderboardResult;
import com.example.common.api.model.game.MemberResult;
import com.example.common.base.BaseViewModel;
import com.example.common.model.game.Member;
import com.example.common.network.http.Result;
import com.example.common.thread.ThreadManager;
import com.example.common.utils.RefreshTokenUtils;
import com.example.common.utils.ToastUtils;

import java.util.List;
import java.util.stream.Collectors;

public class LeaderboardViewModel extends BaseViewModel {

    private final MutableLiveData<List<Member>> mMembers = new MutableLiveData<>();
    private final MutableLiveData<Boolean> mIsShowLoading = new MutableLiveData<>();

    @Override
    public void onCreate(@NonNull LifecycleOwner owner) {

    }

    @Override
    public void onDestroy(@NonNull LifecycleOwner owner) {

    }

    public MutableLiveData<List<Member>> getMembers() {
        return mMembers;
    }

    public MutableLiveData<Boolean> isShowLoading() {
        return mIsShowLoading;
    }

    public void initData() {
        mIsShowLoading.postValue(true);
        ThreadManager.getThreadPollProxy().execute(new Runnable() {
            @Override
            public void run() {
                Result<LeaderboardResult> leaderboardResult = ApiClient.getLeaderboard();
                if (leaderboardResult.isTokenTimeout() || leaderboardResult.isForbidden()) {
                    RefreshTokenUtils.refreshToken();
                    mIsShowLoading.postValue(false);
                    initData();
                    return;
                }
                if (!leaderboardResult.isSuccess()) {
                    mIsShowLoading.postValue(false);
                    ToastUtils.showShortSafe("Please get leaderboard again");
                    return;
                }
                LeaderboardResult leaderboardResultBody = leaderboardResult.getBody(LeaderboardResult.class);
                List<MemberResult> memberResultList = leaderboardResultBody.getList();
                List<Member> members = memberResultList.stream()
                        .map(member -> new Member(member.getRank(), member.getName(), member.getScore()))
                        .collect(Collectors.toList());
                mIsShowLoading.postValue(false);
                mMembers.postValue(members);
            }
        });
    }
}
