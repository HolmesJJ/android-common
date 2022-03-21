package com.example.common.ui.activity;

import android.annotation.SuppressLint;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.PixelFormat;
import android.graphics.PorterDuff;
import android.hardware.Camera;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.View;

import androidx.annotation.NonNull;

import com.example.common.BR;
import com.example.common.R;
import com.example.common.base.BaseActivity;
import com.example.common.camera.CameraOverlap;
import com.example.common.databinding.ActivityMouthBinding;
import com.example.common.landmark.EGLUtils;
import com.example.common.landmark.GLBitmap;
import com.example.common.landmark.GLFrame;
import com.example.common.landmark.GLFramebuffer;
import com.example.common.model.mouth.Mouth;
import com.example.common.thread.CustomThreadPool;
import com.example.common.ui.viewmodel.MouthViewModel;
import com.example.common.utils.ContextUtils;
import com.example.common.utils.FileUtils;
import com.example.common.utils.ListenerUtils;
import com.zeusee.main.hyperlandmark.jni.Face;
import com.zeusee.main.hyperlandmark.jni.FaceTracking;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MouthActivity extends BaseActivity<ActivityMouthBinding, MouthViewModel>
        implements SurfaceHolder.Callback, Camera.PreviewCallback {

    private static final String TAG = MouthActivity.class.getSimpleName();

    private static final CustomThreadPool THREAD_POOL_CAMERA = new CustomThreadPool(Thread.NORM_PRIORITY);

    private final Object lockObj = new Object();
    private final ArrayList<Mouth> mouths = new ArrayList<>();

    private CameraOverlap mCameraOverlap;
    private Matrix mMatrix;

    private EGLUtils mEglUtils;
    private GLFramebuffer mFramebuffer;
    private GLFrame mFrame;
    private GLBitmap mBitmap;

    private Handler mHandler;

    private byte[] mRGBCameraTrackNv21;
    private float[] mMouthPoints;
    private boolean mIsRGBCameraNv21Ready = false;
    private boolean mIsRecording = false;
    private boolean mIsMouthOutOfBounds = false;

    private int mEnglishId;
    private int mCurProgress;

    @Override
    public int initContentView(Bundle savedInstanceState) {
        return R.layout.activity_mouth;
    }

    @Override
    public int initVariableId() {
        return BR.viewModel;
    }

    @Override
    public Class<MouthViewModel> getViewModelClazz() {
        return MouthViewModel.class;
    }

    @Override
    public void initData() {
        super.initData();
        if (getIntent() != null && getIntent().getExtras() != null) {
            Bundle bundle = getIntent().getExtras();
            mEnglishId = bundle.getInt("englishId");
        }
        if (mHandler == null) {
            mHandler = new Handler();
        }
        // Load Model
        FaceTracking.getInstance().FaceTrackingInit(FileUtils.MODEL_PATH, CameraOverlap.PREVIEW_HEIGHT, CameraOverlap.PREVIEW_WIDTH);
        getBinding().svOverlap.getHolder().setFormat(PixelFormat.TRANSLUCENT);
        initCamera();
    }

    @Override
    public void initViewObservable() {
        super.initViewObservable();
        setObserveListener();
        setOnTouchListener();
        doIsShowLoading();
    }

    @Override
    public void onBackPressed() {
        showLoading(false);
        releaseCamera();
        stopLoading();
        super.onBackPressed();
    }

    @Override
    public void onResume() {
        super.onResume();
        mIsRGBCameraNv21Ready = false;
    }

    @Override
    protected void onDestroy() {
        releaseCamera();
        if (mHandler != null) {
            mHandler.removeCallbacksAndMessages(null);
            mHandler = null;
        }
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
        if (mMouthPoints == null) {
            // 嘴巴只有20个点，需要一笔画完整个嘴巴，因此多出了4个重复点
            mMouthPoints = new float[24 * 2];
        }
        if (!mIsRGBCameraNv21Ready) {
            synchronized (lockObj) {
                System.arraycopy(data, 0, mRGBCameraTrackNv21, 0, data.length);
            }
             mIsRGBCameraNv21Ready = true;
            startTrackRGBTask();
        }
    }

    private void setObserveListener() {
        if (getViewModel() == null) {
            return;
        }
        getViewModel().getMessage().observe(this, message -> {
            getBinding().tvMessage.setText(message);
        });
    }

    private void setOnTouchListener() {
        ListenerUtils.setOnTouchListener(getBinding().lpvRecord, new View.OnTouchListener() {
            @SuppressLint("ClickableViewAccessibility")
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                switch (motionEvent.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        mouths.clear();
                        mIsRecording = true;
                        mHandler.postDelayed(mProgressRunnable, 50);
                        break;
                    case MotionEvent.ACTION_UP:
                        if (mIsRecording) {
                            mHandler.removeCallbacks(mProgressRunnable);
                            getBinding().lpvRecord.setProgress(0);
                            mCurProgress = 0;
                            mIsRecording = false;
                            if (getViewModel() != null) {
                                getViewModel().process(mEnglishId, mouths);
                            }
                        }
                        break;
                    default:
                        break;
                }
                return true;
            }
        });
    }

    private final Runnable mProgressRunnable = new Runnable() {
        @Override
        public void run() {
            getBinding().lpvRecord.setProgress(mCurProgress);
            int MAX_PROGRESS = 100;
            if (mCurProgress < MAX_PROGRESS) {
                mCurProgress++;
                mHandler.postDelayed(mProgressRunnable, 50);
            } else {
                if (mIsRecording) {
                    mHandler.removeCallbacks(mProgressRunnable);
                    getBinding().lpvRecord.setProgress(0);
                    mCurProgress = 0;
                    mIsRecording = false;
                    if (getViewModel() != null) {
                        getViewModel().process(mEnglishId, mouths);
                    }
                }
            }
        }
    };

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

            FaceTracking.getInstance().Update(mRGBCameraTrackNv21, CameraOverlap.PREVIEW_HEIGHT, CameraOverlap.PREVIEW_WIDTH);

            List<Face> faceActions = FaceTracking.getInstance().getTrackingInfo();
            // Start draw frame, rectangle, points, faces, lips
            int tid = 0;
            mFrame.drawFrame(tid, mFramebuffer.drawFrameBuffer(), mFramebuffer.getMatrix());
            if (faceActions == null || faceActions.size() == 0) {
                clearCanvas();
            }
            mIsMouthOutOfBounds = false;
            draw(faceActions);
            if (mIsMouthOutOfBounds) {
                if (getViewModel() != null) {
                    getViewModel().getMessage().postValue(getString(R.string.mouth_out_of_bounds));
                }
            } else {
                if (getViewModel() != null) {
                    getViewModel().getMessage().postValue("");
                }
            }
            if (mIsRecording) {
                byte[] data = new byte[CameraOverlap.PREVIEW_WIDTH * CameraOverlap.PREVIEW_HEIGHT * 2];
                System.arraycopy(mRGBCameraTrackNv21, 0, data, 0, mRGBCameraTrackNv21.length);
                float[] mouthPoints = new float[24 * 2];
                System.arraycopy(mMouthPoints, 0, mouthPoints, 0, mMouthPoints.length);
                mouths.add(new Mouth(data, CameraOverlap.PREVIEW_WIDTH, CameraOverlap.PREVIEW_HEIGHT, mouthPoints));
            }
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

    private void clearCanvas() {
        Canvas canvas = getBinding().svOverlap.getHolder().lockCanvas();
        if (canvas != null) {
            canvas.drawColor(0, PorterDuff.Mode.CLEAR);
            getBinding().svOverlap.getHolder().unlockCanvasAndPost(canvas);
        }
    }

    private void draw(List<Face> faceActions) {
        if (faceActions != null && faceActions.size() > 0) {
            Canvas canvas = getBinding().svOverlap.getHolder().lockCanvas();
            if (canvas == null) {
                return;
            }
            canvas.drawColor(0, PorterDuff.Mode.CLEAR);
            drawBorder(canvas);
            drawFaces(canvas, faceActions);
            getBinding().svOverlap.getHolder().unlockCanvasAndPost(canvas);
        }
    }

    private void drawBorder(Canvas canvas) {
        // 正方形框
        int left = CameraOverlap.PREVIEW_HEIGHT / 3;
        int top = CameraOverlap.PREVIEW_WIDTH / 8 * 5;
        int right = CameraOverlap.PREVIEW_HEIGHT / 3 * 2;
        int bottom = CameraOverlap.PREVIEW_WIDTH / 8 * 5 + (right - left);
        if (getViewModel() != null) {
            getViewModel().drawBorder(canvas, mMatrix, left, top, right, bottom);
        }
    }

    private void drawFaces(Canvas canvas, List<Face> faceActions) {
        if (faceActions != null && faceActions.size() > 0) {
            for (Face face : faceActions) {
                drawLip(canvas, face);
            }
        }
    }

    private void drawLip(Canvas canvas, Face face) {
        boolean rotate270 = mCameraOverlap.getOrientation() == 270;
        // 嘴巴只有20个点，需要一笔画完整个嘴巴，因此多出了4个重复点
        Arrays.fill(mMouthPoints, 0);
        for (int i = 0; i < 106; i++) {
            int x;
            if (rotate270) {
                x = face.landmarks[i * 2];
            } else {
                x = CameraOverlap.PREVIEW_HEIGHT - face.landmarks[i * 2];
            }
            int y = face.landmarks[i * 2 + 1];
            // 画出嘴巴特征点
            // 嘴巴左边
            // i == 45, i == 61
            // 嘴巴右边
            // i == 42, i == 50
            // 上嘴唇上半部分
            // i == 37, i == 39, i == 38, i == 26, i == 33
            // 上嘴唇下半部分
            // i == 40, i == 36, i == 25
            // 下嘴唇上半部分
            // i == 63, i == 103, i == 2
            // 下嘴唇下半部分
            // i == 65, i == 64, i == 32, i == 30, i == 4
            if (i == 45) {
                mMouthPoints[0] = x;
                mMouthPoints[1] = y;
                mMouthPoints[24] = x;
                mMouthPoints[25] = y;
                if (!mIsMouthOutOfBounds) {
                    mIsMouthOutOfBounds = checkLipLocation(x, y);
                }
            }
            if (i == 37) {
                mMouthPoints[2] = x;
                mMouthPoints[3] = y;
                if (!mIsMouthOutOfBounds) {
                    mIsMouthOutOfBounds = checkLipLocation(x, y);
                }
            }
            if (i == 39) {
                mMouthPoints[4] = x;
                mMouthPoints[5] = y;
                if (!mIsMouthOutOfBounds) {
                    mIsMouthOutOfBounds = checkLipLocation(x, y);
                }
            }
            if (i == 38) {
                mMouthPoints[6] = x;
                mMouthPoints[7] = y;
                if (!mIsMouthOutOfBounds) {
                    mIsMouthOutOfBounds = checkLipLocation(x, y);
                }
            }
            if (i == 26) {
                mMouthPoints[8] = x;
                mMouthPoints[9] = y;
                if (!mIsMouthOutOfBounds) {
                    mIsMouthOutOfBounds = checkLipLocation(x, y);
                }
            }
            if (i == 33) {
                mMouthPoints[10] = x;
                mMouthPoints[11] = y;
                if (!mIsMouthOutOfBounds) {
                    mIsMouthOutOfBounds = checkLipLocation(x, y);
                }
            }
            if (i == 50) {
                mMouthPoints[12] = x;
                mMouthPoints[13] = y;
                mMouthPoints[36] = x;
                mMouthPoints[37] = y;
                if (!mIsMouthOutOfBounds) {
                    mIsMouthOutOfBounds = checkLipLocation(x, y);
                }
            }
            if (i == 42) {
                mMouthPoints[14] = x;
                mMouthPoints[15] = y;
                mMouthPoints[38] = x;
                mMouthPoints[39] = y;
                if (!mIsMouthOutOfBounds) {
                    mIsMouthOutOfBounds = checkLipLocation(x, y);
                }
            }
            if (i == 25) {
                mMouthPoints[16] = x;
                mMouthPoints[17] = y;
                if (!mIsMouthOutOfBounds) {
                    mIsMouthOutOfBounds = checkLipLocation(x, y);
                }
            }
            if (i == 36) {
                mMouthPoints[18] = x;
                mMouthPoints[19] = y;
                if (!mIsMouthOutOfBounds) {
                    mIsMouthOutOfBounds = checkLipLocation(x, y);
                }
            }
            if (i == 40) {
                mMouthPoints[20] = x;
                mMouthPoints[21] = y;
                if (!mIsMouthOutOfBounds) {
                    mIsMouthOutOfBounds = checkLipLocation(x, y);
                }
            }
            if (i == 61) {
                mMouthPoints[22] = x;
                mMouthPoints[23] = y;
                mMouthPoints[46] = x;
                mMouthPoints[47] = y;
                if (!mIsMouthOutOfBounds) {
                    mIsMouthOutOfBounds = checkLipLocation(x, y);
                }
            }
            if (i == 65) {
                mMouthPoints[26] = x;
                mMouthPoints[27] = y;
                if (!mIsMouthOutOfBounds) {
                    mIsMouthOutOfBounds = checkLipLocation(x, y);
                }
            }
            if (i == 64) {
                mMouthPoints[28] = x;
                mMouthPoints[29] = y;
                if (!mIsMouthOutOfBounds) {
                    mIsMouthOutOfBounds = checkLipLocation(x, y);
                }
            }
            if (i == 32) {
                mMouthPoints[30] = x;
                mMouthPoints[31] = y;
                if (!mIsMouthOutOfBounds) {
                    mIsMouthOutOfBounds = checkLipLocation(x, y);
                }
            }
            if (i == 30) {
                mMouthPoints[32] = x;
                mMouthPoints[33] = y;
                if (!mIsMouthOutOfBounds) {
                    mIsMouthOutOfBounds = checkLipLocation(x, y);
                }
            }
            if (i == 4) {
                mMouthPoints[34] = x;
                mMouthPoints[35] = y;
                if (!mIsMouthOutOfBounds) {
                    mIsMouthOutOfBounds = checkLipLocation(x, y);
                }
            }
            if (i == 2) {
                mMouthPoints[40] = x;
                mMouthPoints[41] = y;
                if (!mIsMouthOutOfBounds) {
                    mIsMouthOutOfBounds = checkLipLocation(x, y);
                }
            }
            if (i == 103) {
                mMouthPoints[42] = x;
                mMouthPoints[43] = y;
                if (!mIsMouthOutOfBounds) {
                    mIsMouthOutOfBounds = checkLipLocation(x, y);
                }
            }
            if (i == 63) {
                mMouthPoints[44] = x;
                mMouthPoints[45] = y;
                if (!mIsMouthOutOfBounds) {
                    mIsMouthOutOfBounds = checkLipLocation(x, y);
                }
            }
        }
        if (getViewModel() != null) {
            getViewModel().drawLip(canvas, mMatrix, mMouthPoints);
        }
    }

    private boolean checkLipLocation(int x, int y) {
        // 正方形框
        int left = CameraOverlap.PREVIEW_HEIGHT / 3;
        int top = CameraOverlap.PREVIEW_WIDTH / 8 * 5;
        int right = CameraOverlap.PREVIEW_HEIGHT / 3 * 2;
        int bottom = CameraOverlap.PREVIEW_WIDTH / 8 * 5 + (right - left);
        return x < left || x > right || y < top || y > bottom;
    }

    /**
     * 控制进度圈显示
     */
    private void doIsShowLoading() {
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
