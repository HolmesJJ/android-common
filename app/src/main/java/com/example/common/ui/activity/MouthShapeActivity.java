package com.example.common.ui.activity;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.hardware.Camera;
import android.os.Bundle;
import android.renderscript.Allocation;
import android.renderscript.Element;
import android.renderscript.RenderScript;
import android.renderscript.ScriptIntrinsicYuvToRGB;
import android.renderscript.Type;
import android.util.Log;
import android.view.SurfaceHolder;

import androidx.annotation.NonNull;

import com.example.common.BR;
import com.example.common.R;
import com.example.common.base.BaseActivity;
import com.example.common.camera.CameraOverlap;
import com.example.common.databinding.ActivityMouthShapeBinding;
import com.example.common.landmark.EGLUtils;
import com.example.common.landmark.GLBitmap;
import com.example.common.landmark.GLFrame;
import com.example.common.landmark.GLFramebuffer;
import com.example.common.landmark.GLPoints;
import com.example.common.thread.CustomThreadPool;
import com.example.common.ui.viewmodel.MouthShapeViewModel;
import com.example.common.utils.ContextUtils;
import com.example.common.utils.FileUtils;
import com.zeusee.main.hyperlandmark.jni.Face;
import com.zeusee.main.hyperlandmark.jni.FaceTracking;

import java.util.List;

public class MouthShapeActivity extends BaseActivity<ActivityMouthShapeBinding, MouthShapeViewModel>
        implements SurfaceHolder.Callback, Camera.PreviewCallback {

    private static final String TAG = MouthShapeActivity.class.getSimpleName();
    private final Object lockObj = new Object();

    private CameraOverlap mCameraOverlap;
    private ScriptIntrinsicYuvToRGB mScriptIntrinsicYuvToRGB;
    private Allocation mInAllocation, mOutAllocation;
    private Bitmap mSourceBitmap;
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

    private byte[] mRGBCameraTrackNv21;
    private boolean mIsRGBCameraNv21Ready = false;

    private int mEnglishId;

    private static final CustomThreadPool sThreadPoolRGBTrack = new CustomThreadPool(Thread.NORM_PRIORITY);
    private static final CustomThreadPool sThreadPoolNv21ToBtm = new CustomThreadPool(Thread.MAX_PRIORITY);

    @Override
    public int initContentView(Bundle savedInstanceState) {
        return R.layout.activity_mouth_shape;
    }

    @Override
    public int initVariableId() {
        return BR.viewModel;
    }

    @Override
    public Class<MouthShapeViewModel> getViewModelClazz() {
        return MouthShapeViewModel.class;
    }

    @Override
    public void initData() {
        super.initData();
        if (getIntent() != null && getIntent().getExtras() != null) {
            Bundle bundle = getIntent().getExtras();
            mEnglishId = bundle.getInt("englishId");
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
        sThreadPoolRGBTrack.execute(() -> {
            mCameraOverlap = new CameraOverlap(MouthShapeActivity.this);
            mCameraOverlap.setPreviewCallback(this);
        });
    }

    @Override
    public void surfaceChanged(@NonNull SurfaceHolder surfaceHolder, int i, int i1, int i2) {
        Log.i(TAG, "surfaceChanged");
        sThreadPoolRGBTrack.execute(() -> {
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
        sThreadPoolRGBTrack.execute(() -> {
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
        mBitmap = new GLBitmap(MouthShapeActivity.this, R.drawable.ic_avatar); // 任意定义一张图
        mMatrix = new Matrix();
        getBinding().svCamera.getHolder().addCallback(this);
    }

    private void startTrackRGBTask() {
        sThreadPoolRGBTrack.execute(() -> {
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
        float[] points = new float[24 * 2];
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
                points[0] = view2openglX(x);
                points[1] = view2openglY(y);
                points[24] = view2openglX(x);
                points[25] = view2openglY(y);
            }
            if (i == 37) {
                points[2] = view2openglX(x);
                points[3] = view2openglY(y);
            }
            if (i == 39) {
                points[4] = view2openglX(x);
                points[5] = view2openglY(y);
            }
            if (i == 38) {
                points[6] = view2openglX(x);
                points[7] = view2openglY(y);
            }
            if (i == 26) {
                points[8] = view2openglX(x);
                points[9] = view2openglY(y);
            }
            if (i == 33) {
                points[10] = view2openglX(x);
                points[11] = view2openglY(y);
            }
            if (i == 50) {
                points[12] = view2openglX(x);
                points[13] = view2openglY(y);
                points[36] = view2openglX(x);
                points[37] = view2openglY(y);
            }
            if (i == 42) {
                points[14] = view2openglX(x);
                points[15] = view2openglY(y);
                points[38] = view2openglX(x);
                points[39] = view2openglY(y);
            }
            if (i == 25) {
                points[16] = view2openglX(x);
                points[17] = view2openglY(y);
            }
            if (i == 36) {
                points[18] = view2openglX(x);
                points[19] = view2openglY(y);
            }
            if (i == 40) {
                points[20] = view2openglX(x);
                points[21] = view2openglY(y);
            }
            if (i == 61) {
                points[22] = view2openglX(x);
                points[23] = view2openglY(y);
                points[46] = view2openglX(x);
                points[47] = view2openglY(y);
            }
            if (i == 65) {
                points[26] = view2openglX(x);
                points[27] = view2openglY(y);
            }
            if (i == 64) {
                points[28] = view2openglX(x);
                points[29] = view2openglY(y);
            }
            if (i == 32) {
                points[30] = view2openglX(x);
                points[31] = view2openglY(y);
            }
            if (i == 30) {
                points[32] = view2openglX(x);
                points[33] = view2openglY(y);
            }
            if (i == 4) {
                points[34] = view2openglX(x);
                points[35] = view2openglY(y);
            }
            if (i == 2) {
                points[40] = view2openglX(x);
                points[41] = view2openglY(y);
            }
            if (i == 103) {
                points[42] = view2openglX(x);
                points[43] = view2openglY(y);
            }
            if (i == 63) {
                points[44] = view2openglX(x);
                points[45] = view2openglY(y);
            }
        }
        mPoints.setPoints(points);
        mPoints.drawLipPoints();
        mPoints.drawLipLine();
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

    private void initRenderScript(int width, int height) {
        RenderScript mRenderScript = RenderScript.create(ContextUtils.getContext());
        mScriptIntrinsicYuvToRGB = ScriptIntrinsicYuvToRGB.create(mRenderScript,
                Element.U8_4(mRenderScript));

        Type.Builder yuvType = new Type.Builder(mRenderScript, Element.U8(mRenderScript))
                .setX(width * height * 3 / 2);
        mInAllocation = Allocation.createTyped(mRenderScript,
                yuvType.create(),
                Allocation.USAGE_SCRIPT);

        Type.Builder rgbaType = new Type.Builder(mRenderScript, Element.RGBA_8888(mRenderScript))
                .setX(width).setY(height);
        mOutAllocation = Allocation.createTyped(mRenderScript,
                rgbaType.create(),
                Allocation.USAGE_SCRIPT);
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
