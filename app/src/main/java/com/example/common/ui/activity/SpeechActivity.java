package com.example.common.ui.activity;

import android.graphics.Matrix;
import android.graphics.PixelFormat;
import android.hardware.Camera;
import android.os.Bundle;
import android.util.Log;
import android.view.SurfaceHolder;

import androidx.annotation.NonNull;

import com.example.common.BR;
import com.example.common.R;
import com.example.common.base.BaseActivity;
import com.example.common.camera.CameraOverlap;
import com.example.common.databinding.ActivitySpeechBinding;
import com.example.common.landmark.EGLUtils;
import com.example.common.landmark.GLBitmap;
import com.example.common.landmark.GLFrame;
import com.example.common.landmark.GLFramebuffer;
import com.example.common.thread.CustomThreadPool;
import com.example.common.ui.viewmodel.SpeechViewModel;
import com.example.common.utils.ContextUtils;

public class SpeechActivity extends BaseActivity<ActivitySpeechBinding, SpeechViewModel>
        implements SurfaceHolder.Callback, Camera.PreviewCallback{

    private static final String TAG = SpeechActivity.class.getSimpleName();

    private static final CustomThreadPool THREAD_POOL_CAMERA = new CustomThreadPool(Thread.NORM_PRIORITY);

    private final Object lockObj = new Object();

    private CameraOverlap mCameraOverlap;
    private Matrix mMatrix;

    private EGLUtils mEglUtils;
    private GLFramebuffer mFramebuffer;
    private GLFrame mFrame;
    private GLBitmap mBitmap;

    private byte[] mRGBCameraTrackNv21;
    private boolean mIsRGBCameraNv21Ready = false;

    private int mEnglishId;

    @Override
    public int initContentView(Bundle savedInstanceState) {
        return R.layout.activity_speech;
    }

    @Override
    public int initVariableId() {
        return BR.viewModel;
    }

    @Override
    public Class<SpeechViewModel> getViewModelClazz() {
        return SpeechViewModel.class;
    }

    @Override
    public void initData() {
        super.initData();
        if (getIntent() != null && getIntent().getExtras() != null) {
            Bundle bundle = getIntent().getExtras();
            mEnglishId = bundle.getInt("englishId");
        }
        getBinding().svOverlap.getHolder().setFormat(PixelFormat.TRANSLUCENT);
        initCamera();
    }

    @Override
    public void initViewObservable() {
        super.initViewObservable();
        doIsShowLoading();
    }

    @Override
    public void onResume() {
        super.onResume();
        mIsRGBCameraNv21Ready = false;
    }

    @Override
    protected void onDestroy() {
        releaseCamera();
        super.onDestroy();
    }

    @Override
    public void surfaceCreated(@NonNull SurfaceHolder surfaceHolder) {
        Log.i(TAG, "surfaceCreated");
        THREAD_POOL_CAMERA.execute(() -> {
            mCameraOverlap = new CameraOverlap(ContextUtils.getContext());
            mCameraOverlap.setPreviewCallback(this);
        });
    }

    @Override
    public void surfaceChanged(@NonNull SurfaceHolder surfaceHolder, int i, int i1, int i2) {
        Log.i(TAG, "surfaceChanged");
        THREAD_POOL_CAMERA.execute(() -> {
            if (mEglUtils != null) {
                mEglUtils.release();
                mEglUtils = null;
            }
            mEglUtils = new EGLUtils();
            mEglUtils.initEGL(surfaceHolder.getSurface());
            mFramebuffer.initFramebuffer();
            mFrame.initFrame();
            mFrame.setSize(getBinding().svCamera.getWidth(), getBinding().svCamera.getHeight(), CameraOverlap.PREVIEW_HEIGHT, CameraOverlap.PREVIEW_WIDTH);
            mBitmap.initFrame(CameraOverlap.PREVIEW_HEIGHT, CameraOverlap.PREVIEW_WIDTH);
            mMatrix.setScale(getBinding().svCamera.getWidth() / (float) CameraOverlap.PREVIEW_HEIGHT, getBinding().svCamera.getHeight() / (float) CameraOverlap.PREVIEW_WIDTH);
            mCameraOverlap.openCamera(mFramebuffer.getSurfaceTexture());
        });
    }

    @Override
    public void surfaceDestroyed(@NonNull SurfaceHolder surfaceHolder) {
        Log.i(TAG, "surfaceDestroyed");
        THREAD_POOL_CAMERA.execute(() -> {
            if (mCameraOverlap != null) {
                mCameraOverlap.setPreviewCallback(null);
                mCameraOverlap.release();
                mCameraOverlap = null;
            }
        });
    }

    @Override
    public void onPreviewFrame(byte[] data, Camera camera) {
        if (data == null) {
            return;
        }
        if (mRGBCameraTrackNv21 == null) {
            mRGBCameraTrackNv21 = new byte[CameraOverlap.PREVIEW_WIDTH * CameraOverlap.PREVIEW_HEIGHT * 2];
        }
        if (!mIsRGBCameraNv21Ready) {
            synchronized (lockObj) {
                System.arraycopy(data, 0, mRGBCameraTrackNv21, 0, data.length);
            }
            mIsRGBCameraNv21Ready = true;
            startTrackRGBTask();
        }
    }

    private void initCamera() {
        mFramebuffer = new GLFramebuffer();
        mFrame = new GLFrame();
        mBitmap = new GLBitmap(ContextUtils.getContext(), R.drawable.ic_avatar); // 任意定义一张图
        mMatrix = new Matrix();
        getBinding().svCamera.getHolder().addCallback(this);
    }

    private void startTrackRGBTask() {
        THREAD_POOL_CAMERA.execute(() -> {
            if (mEglUtils == null || mFramebuffer == null) {
                mIsRGBCameraNv21Ready = false;
                return;
            }
            mFrame.setS(100 / 100.0f);
            mFrame.setH(0 / 360.0f);
            mFrame.setL(100 / 100.0f - 1);
            // Start draw frame, rectangle, points, faces, lips
            int tid = 0;
            mFrame.drawFrame(tid, mFramebuffer.drawFrameBuffer(), mFramebuffer.getMatrix());
            mEglUtils.swap();
            mIsRGBCameraNv21Ready = false;
        });
    }

    private void releaseCamera() {
        if (mCameraOverlap != null) {
            mCameraOverlap.setPreviewCallback(null);
            mCameraOverlap.release();
            mCameraOverlap = null;
        }
        if (mFramebuffer != null) {
            mFramebuffer.release();
            mFramebuffer = null;
        }
        if (mFrame != null) {
            mFrame.release();
            mFrame = null;
        }
        if (mBitmap != null) {
            mBitmap.release();
            mBitmap = null;
        }
        if (mEglUtils != null) {
            mEglUtils.release();
            mEglUtils = null;
        }
    }

    /**
     * 控制进度圈显示
     */
    public void doIsShowLoading() {
        if (getViewModel() == null) {
            return;
        }
        getViewModel().isShowLoading().observe(this, isShowing -> {
            if (isShowing) {
                showLoading(false);
            } else {
                stopLoading();
            }
        });
    }
}
