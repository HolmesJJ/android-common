package com.example.common.network.http.interceptor;

import androidx.annotation.NonNull;

import com.example.common.network.http.Request;
import com.example.common.network.http.Result;

import java.io.IOException;
import java.util.List;

/**
 * 拦截器内
 */
public class BaseChain implements Interceptor.Chain {

    private final List<Interceptor> mInterceptorList;
    private final int mIndex;
    private final Request mRequest;

    public BaseChain(List<Interceptor> interceptors, int index, @NonNull Request request) {
        this.mInterceptorList = interceptors;
        this.mIndex = index;
        this.mRequest = request;
    }

    @NonNull
    @Override
    public Request request() {
        return mRequest;
    }

    @NonNull
    @Override
    public Result proceed(@NonNull Request request) throws IOException {
        if (mIndex >= mInterceptorList.size()) {
            throw new AssertionError();
        }
        // Call the next interceptor in the chain.
        BaseChain next = new BaseChain(mInterceptorList, mIndex + 1, request);
        Interceptor interceptor = mInterceptorList.get(mIndex);
        return interceptor.intercept(next);
    }
}
