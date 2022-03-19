package com.example.common.ui.activity;

import android.annotation.SuppressLint;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.PorterDuff;
import android.graphics.Rect;
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
import com.example.common.landmark.GLPoints;
import com.example.common.model.mouth.Mouth;
import com.example.common.thread.CustomThreadPool;
import com.example.common.ui.viewmodel.MouthViewModel;
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
    private Paint mRectPaint;
    private Paint mBorderPaint;
    private Paint mLinePaint;
    private Paint mPointPaint;
    private Matrix mMatrix;

    private EGLUtils mEglUtils;
    private GLFramebuffer mFramebuffer;
    private GLFrame mFrame;
    private GLPoints mPoints;
    private GLBitmap mBitmap;

    private Handler mHandler;

    private byte[] mRGBCameraTrackNv21;
    private float[] mMouthPoints;
    private boolean mIsRGBCameraNv21Ready = false;
    private boolean isRecording = false;

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
        initPaint();
        initCamera();
    }

    @Override
    public void initViewObservable() {
        super.initViewObservable();
        setOnTouchListener();
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
            mCameraOverlap = new CameraOverlap(MouthActivity.this);
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
            mPoints.initPoints();
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

    private void setOnTouchListener() {
        ListenerUtils.setOnTouchListener(getBinding().lpvRecord, new View.OnTouchListener() {
            @SuppressLint("ClickableViewAccessibility")
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                switch (motionEvent.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        mouths.clear();
                        isRecording = true;
                        mHandler.postDelayed(mProgressRunnable, 50);
                        break;
                    case MotionEvent.ACTION_UP:
                        if (isRecording) {
                            mHandler.removeCallbacks(mProgressRunnable);
                            getBinding().lpvRecord.setProgress(0);
                            mCurProgress = 0;
                            isRecording = false;
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
                if (isRecording) {
                    mHandler.removeCallbacks(mProgressRunnable);
                    getBinding().lpvRecord.setProgress(0);
                    mCurProgress = 0;
                    isRecording = false;
                    if (getViewModel() != null) {
                        getViewModel().process(mEnglishId, mouths);
                    }
                }
            }
        }
    };

    private void initPaint() {
        int strokeWidth = Math.max(CameraOverlap.PREVIEW_HEIGHT / 240, 2);
        // Rectangle
        mRectPaint = new Paint();
        mRectPaint.setColor(Color.BLACK);
        mRectPaint.setStyle(Paint.Style.FILL);
        // Border
        mBorderPaint = new Paint();
        mBorderPaint.setColor(Color.GREEN);
        mBorderPaint.setStrokeWidth(strokeWidth);
        mBorderPaint.setStyle(Paint.Style.STROKE);
        // Line
        mLinePaint = new Paint();
        mLinePaint.setColor(Color.RED);
        mLinePaint.setStrokeWidth(strokeWidth);
        mLinePaint.setStyle(Paint.Style.STROKE);
        // Point
        mPointPaint = new Paint();
        mPointPaint.setColor(Color.RED);
        mPointPaint.setStyle(Paint.Style.FILL);
    }

    private void initCamera() {
        mFramebuffer = new GLFramebuffer();
        mFrame = new GLFrame();
        mPoints = new GLPoints();
        mBitmap = new GLBitmap(MouthActivity.this, R.drawable.ic_avatar); // 任意定义一张图
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
            draw(faceActions);
            if (isRecording) {
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
        if (mPoints != null) {
            mPoints.release();
            mPoints = null;
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
            // drawRectangle(canvas);
            drawBorder(canvas);
            drawFaces(canvas, faceActions);
            getBinding().svOverlap.getHolder().unlockCanvasAndPost(canvas);
        }
    }

    private void drawBorder(Canvas canvas) {
        canvas.save();
        canvas.setMatrix(mMatrix);
        // 正方形框
        int left = CameraOverlap.PREVIEW_HEIGHT / 3;
        int top = CameraOverlap.PREVIEW_WIDTH / 8 * 5;
        int right = CameraOverlap.PREVIEW_HEIGHT / 3 * 2;
        int bottom = CameraOverlap.PREVIEW_WIDTH / 8 * 5 + (right - left);
        Rect rectangle = new Rect(left, top, right, bottom);
        canvas.drawRect(rectangle, mBorderPaint);
        canvas.restore();
    }

    private void drawFaces(Canvas canvas, List<Face> faceActions) {
        if (faceActions != null && faceActions.size() > 0) {
            for (Face face : faceActions) {
                drawLip(face);
            }
        }
    }

    private void drawLip(Face face) {
        boolean rotate270 = mCameraOverlap.getOrientation() == 270;
        // 嘴巴只有20个点，需要一笔画完整个嘴巴，因此多出了4个重复点
        Arrays.fill(mMouthPoints, 0);
        boolean mouthOutOfBounds = false;
        for(int i = 0 ; i < 106 ; i++) {
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
                mMouthPoints[0] = view2openglX(x);
                mMouthPoints[1] = view2openglY(y);
                mMouthPoints[24] = view2openglX(x);
                mMouthPoints[25] = view2openglY(y);
                if (!mouthOutOfBounds) {
                    mouthOutOfBounds = checkLipLocation(x, y);
                }
            }
            if (i == 37) {
                mMouthPoints[2] = view2openglX(x);
                mMouthPoints[3] = view2openglY(y);
                if (!mouthOutOfBounds) {
                    mouthOutOfBounds = checkLipLocation(x, y);
                }
            }
            if (i == 39) {
                mMouthPoints[4] = view2openglX(x);
                mMouthPoints[5] = view2openglY(y);
                if (!mouthOutOfBounds) {
                    mouthOutOfBounds = checkLipLocation(x, y);
                }
            }
            if (i == 38) {
                mMouthPoints[6] = view2openglX(x);
                mMouthPoints[7] = view2openglY(y);
                if (!mouthOutOfBounds) {
                    mouthOutOfBounds = checkLipLocation(x, y);
                }
            }
            if (i == 26) {
                mMouthPoints[8] = view2openglX(x);
                mMouthPoints[9] = view2openglY(y);
                if (!mouthOutOfBounds) {
                    mouthOutOfBounds = checkLipLocation(x, y);
                }
            }
            if (i == 33) {
                mMouthPoints[10] = view2openglX(x);
                mMouthPoints[11] = view2openglY(y);
                if (!mouthOutOfBounds) {
                    mouthOutOfBounds = checkLipLocation(x, y);
                }
            }
            if (i == 50) {
                mMouthPoints[12] = view2openglX(x);
                mMouthPoints[13] = view2openglY(y);
                mMouthPoints[36] = view2openglX(x);
                mMouthPoints[37] = view2openglY(y);
                if (!mouthOutOfBounds) {
                    mouthOutOfBounds = checkLipLocation(x, y);
                }
            }
            if (i == 42) {
                mMouthPoints[14] = view2openglX(x);
                mMouthPoints[15] = view2openglY(y);
                mMouthPoints[38] = view2openglX(x);
                mMouthPoints[39] = view2openglY(y);
                if (!mouthOutOfBounds) {
                    mouthOutOfBounds = checkLipLocation(x, y);
                }
            }
            if (i == 25) {
                mMouthPoints[16] = view2openglX(x);
                mMouthPoints[17] = view2openglY(y);
                if (!mouthOutOfBounds) {
                    mouthOutOfBounds = checkLipLocation(x, y);
                }
            }
            if (i == 36) {
                mMouthPoints[18] = view2openglX(x);
                mMouthPoints[19] = view2openglY(y);
                if (!mouthOutOfBounds) {
                    mouthOutOfBounds = checkLipLocation(x, y);
                }
            }
            if (i == 40) {
                mMouthPoints[20] = view2openglX(x);
                mMouthPoints[21] = view2openglY(y);
                if (!mouthOutOfBounds) {
                    mouthOutOfBounds = checkLipLocation(x, y);
                }
            }
            if (i == 61) {
                mMouthPoints[22] = view2openglX(x);
                mMouthPoints[23] = view2openglY(y);
                mMouthPoints[46] = view2openglX(x);
                mMouthPoints[47] = view2openglY(y);
                if (!mouthOutOfBounds) {
                    mouthOutOfBounds = checkLipLocation(x, y);
                }
            }
            if (i == 65) {
                mMouthPoints[26] = view2openglX(x);
                mMouthPoints[27] = view2openglY(y);
                if (!mouthOutOfBounds) {
                    mouthOutOfBounds = checkLipLocation(x, y);
                }
            }
            if (i == 64) {
                mMouthPoints[28] = view2openglX(x);
                mMouthPoints[29] = view2openglY(y);
                if (!mouthOutOfBounds) {
                    mouthOutOfBounds = checkLipLocation(x, y);
                }
            }
            if (i == 32) {
                mMouthPoints[30] = view2openglX(x);
                mMouthPoints[31] = view2openglY(y);
                if (!mouthOutOfBounds) {
                    mouthOutOfBounds = checkLipLocation(x, y);
                }
            }
            if (i == 30) {
                mMouthPoints[32] = view2openglX(x);
                mMouthPoints[33] = view2openglY(y);
                if (!mouthOutOfBounds) {
                    mouthOutOfBounds = checkLipLocation(x, y);
                }
            }
            if (i == 4) {
                mMouthPoints[34] = view2openglX(x);
                mMouthPoints[35] = view2openglY(y);
                if (!mouthOutOfBounds) {
                    mouthOutOfBounds = checkLipLocation(x, y);
                }
            }
            if (i == 2) {
                mMouthPoints[40] = view2openglX(x);
                mMouthPoints[41] = view2openglY(y);
                if (!mouthOutOfBounds) {
                    mouthOutOfBounds = checkLipLocation(x, y);
                }
            }
            if (i == 103) {
                mMouthPoints[42] = view2openglX(x);
                mMouthPoints[43] = view2openglY(y);
                if (!mouthOutOfBounds) {
                    mouthOutOfBounds = checkLipLocation(x, y);
                }
            }
            if (i == 63) {
                mMouthPoints[44] = view2openglX(x);
                mMouthPoints[45] = view2openglY(y);
                if (!mouthOutOfBounds) {
                    mouthOutOfBounds = checkLipLocation(x, y);
                }
            }
        }
        mPoints.setPoints(mMouthPoints);
        mPoints.drawLipPoints();
        mPoints.drawLipLine();
        if (mouthOutOfBounds) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    getBinding().tvMessage.setText(R.string.mouth_out_of_bounds);
                }
            });
        } else {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    getBinding().tvMessage.setText("");
                }
            });
        }
    }

    private float view2openglX(int x) {
        float centerX = CameraOverlap.PREVIEW_HEIGHT / 2.0f;
        float t = x - centerX;
        return t / centerX;
    }

    private float view2openglY(int y) {
        float centerY = CameraOverlap.PREVIEW_WIDTH / 2.0f;
        float s = centerY - y;
        return s / centerY;
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
