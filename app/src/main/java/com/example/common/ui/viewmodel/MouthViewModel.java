package com.example.common.ui.viewmodel;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;

import androidx.annotation.NonNull;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.MutableLiveData;

import com.example.common.api.ApiClient;
import com.example.common.api.model.mouth.MouthResult;
import com.example.common.api.model.mouth.MouthsResult;
import com.example.common.base.BaseViewModel;
import com.example.common.model.mouth.Frame;
import com.example.common.model.mouth.Mouth;
import com.example.common.model.mouth.Mouths;
import com.example.common.network.http.Result;
import com.example.common.thread.ThreadManager;
import com.example.common.utils.BitmapUtils;
import com.example.common.utils.CameraUtils;
import com.example.common.utils.FileUtils;
import com.example.common.utils.ToastUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class MouthViewModel extends BaseViewModel {

    private static final String FULL_MAX_HORIZONTAL_MOUTH = "FullMaxHMouth.jpg";
    private static final String CROPPED_MAX_HORIZONTAL_MOUTH = "CroppedMaxHMouth.jpg";
    private static final String MAX_HORIZONTAL_MOUTH = "MaxHMouth.jpg";
    private static final String FULL_MAX_VERTICAL_MOUTH = "FullMaxVMouth.jpg";
    private static final String CROPPED_MAX_VERTICAL_MOUTH = "CroppedMaxVMouth.jpg";
    private static final String MAX_VERTICAL_MOUTH = "MaxVMouth.jpg";

    private static final int STROKE_WIDTH = 3;
    private static final int RADIUS = 4;

    private final MutableLiveData<String> mMessage = new MutableLiveData<>();
    private final MutableLiveData<Mouths> mMouths = new MutableLiveData<>();
    private final MutableLiveData<Boolean> mIsShowLoading = new MutableLiveData<>();

    @Override
    public void onCreate(@NonNull LifecycleOwner owner) {

    }

    @Override
    public void onDestroy(@NonNull LifecycleOwner owner) {

    }

    public MutableLiveData<String> getMessage() {
        return mMessage;
    }

    public MutableLiveData<Mouths> getMouths() {
        return mMouths;
    }

    public MutableLiveData<Boolean> isShowLoading() {
        return mIsShowLoading;
    }

    public void process(int englishId, List<Mouth> mouths) {
        mIsShowLoading.postValue(true);
        ThreadManager.getThreadPollProxy().execute(new Runnable() {
            @Override
            public void run() {
                if (mouths.size() > 0) {
                    File captureFolder = new File(FileUtils.CAPTURE_DIR, String.valueOf(englishId));
                    if (!captureFolder.exists()) {
                        captureFolder.mkdirs();
                    }
                    // Max Horizontal Distance Mouths
                    List<Mouth> sortedMaxHMouths = mouths.stream().sorted(new Comparator<Mouth>() {
                        @Override
                        public int compare(Mouth mouth1, Mouth mouth2) {
                            return Double.compare(mouth2.getMaxHorizDist(), mouth1.getMaxHorizDist());
                        }
                    }).collect(Collectors.toList());
                    Mouth maxHMouth = sortedMaxHMouths.get(0);
                    Bitmap maxHBitmap = CameraUtils.getSceneBtm(maxHMouth.getData(), maxHMouth.getWidth(), maxHMouth.getHeight());
                    Bitmap rotatedMaxHBitmap = BitmapUtils.rotateBitmap(maxHBitmap, 270, true, false);
                    BitmapUtils.savePhotoToSDCard(captureFolder.getAbsolutePath(), FULL_MAX_HORIZONTAL_MOUTH, rotatedMaxHBitmap, 100);
                    // Black background
                    Bitmap drawnMaxHBitmap = drawMouth(rotatedMaxHBitmap, maxHMouth.getPoints(), true, true);
                    Bitmap croppedMaxHBitmap = cropMouth(drawnMaxHBitmap);
                    BitmapUtils.savePhotoToSDCard(captureFolder.getAbsolutePath(), MAX_HORIZONTAL_MOUTH, croppedMaxHBitmap, 100);
                    // Source bitmap
                    Bitmap drawnMaxHSourceBitmap = drawMouth(rotatedMaxHBitmap, maxHMouth.getPoints(), true, false);
                    Bitmap croppedMaxHSourceBitmap = cropMouth(drawnMaxHSourceBitmap);
                    BitmapUtils.savePhotoToSDCard(captureFolder.getAbsolutePath(), CROPPED_MAX_HORIZONTAL_MOUTH, croppedMaxHSourceBitmap, 100);
                    // Max Vertical Distance Mouths
                    List<Mouth> sortedMaxVMouths = mouths.stream().sorted(new Comparator<Mouth>() {
                        @Override
                        public int compare(Mouth mouth1, Mouth mouth2) {
                            return Double.compare(mouth2.getMaxVertDist(), mouth1.getMaxVertDist());
                        }
                    }).collect(Collectors.toList());
                    Mouth maxVMouth = sortedMaxVMouths.get(0);
                    Bitmap maxVBitmap = CameraUtils.getSceneBtm(maxVMouth.getData(), maxVMouth.getWidth(), maxVMouth.getHeight());
                    Bitmap rotatedMaxVBitmap = BitmapUtils.rotateBitmap(maxVBitmap, 270, true, false);
                    BitmapUtils.savePhotoToSDCard(captureFolder.getAbsolutePath(), FULL_MAX_VERTICAL_MOUTH, rotatedMaxVBitmap, 100);
                    // Black background
                    Bitmap drawnMaxVBitmap = drawMouth(rotatedMaxVBitmap, maxVMouth.getPoints(), true, true);
                    Bitmap croppedMaxVBitmap = cropMouth(drawnMaxVBitmap);
                    BitmapUtils.savePhotoToSDCard(captureFolder.getAbsolutePath(), MAX_VERTICAL_MOUTH, croppedMaxVBitmap, 100);
                    // Source bitmap
                    Bitmap drawnMaxVSourceBitmap = drawMouth(rotatedMaxVBitmap, maxVMouth.getPoints(), true, false);
                    Bitmap croppedMaxVSourceBitmap = cropMouth(drawnMaxVSourceBitmap);
                    BitmapUtils.savePhotoToSDCard(captureFolder.getAbsolutePath(), CROPPED_MAX_VERTICAL_MOUTH, croppedMaxVSourceBitmap, 100);
                    // Convert to Frame
                    List<Frame> frames = generateFrames(mouths);
                    List<Frame> markedFrames = new ArrayList<>();
                    markedFrames.add(frames.get(maxVMouth.getId()));
                    markedFrames.add(frames.get(maxHMouth.getId()));
                    Mouths mouths = new Mouths(englishId, frames, markedFrames);
                    // Post to analysis
//                    List<String> images = new ArrayList<>();
//                    images.add(BitmapUtils.bitmapToBase64(croppedMaxVBitmap));
//                    images.add(BitmapUtils.bitmapToBase64(croppedMaxHBitmap));
//                    Result<MouthsResult> mouthsResult = ApiClient.analysisMouth(englishId, images);
//                    if (!mouthsResult.isSuccess()) {
//                        mIsShowLoading.postValue(false);
//                        ToastUtils.showShortSafe("Please try exercise again");
//                        return;
//                    }
//                    MouthsResult mouthsResultBody = mouthsResult.getBody(MouthsResult.class);
//                    List<MouthResult> mouthResultList = mouthsResultBody.getList();
//                    if (mouthResultList != null && mouthResultList.size() == 2) {
//                        MouthResult resultV = mouthResultList.get(0);
//                        frames.get(maxVMouth.getId()).setTitle("/e/").setMessage(resultV.getMessage()).setMarkedBitmap(croppedMaxVSourceBitmap).setMarked(true);
//                        MouthResult resultH = mouthResultList.get(1);
//                        frames.get(maxHMouth.getId()).setTitle("/i/").setMessage(resultH.getMessage()).setMarkedBitmap(croppedMaxHSourceBitmap).setMarked(true);
//                    }
                     // Hardcode for testing
                    frames.get(maxVMouth.getId()).setTitle("/e/").setMessage("Too big on up and down").setMarkedBitmap(croppedMaxVSourceBitmap).setMarked(true);
                    frames.get(maxHMouth.getId()).setTitle("/i/").setMessage("Too wide on left and right").setMarkedBitmap(croppedMaxHSourceBitmap).setMarked(true);
                    mMouths.postValue(mouths);
                }
                mIsShowLoading.postValue(false);
            }
        });
    }

    private Bitmap drawMouth(Bitmap sourceBitmap, float[] points, boolean isCopied, boolean isDrawRect) {
        Bitmap bitmap;
        if (isCopied) {
            bitmap = sourceBitmap.copy(sourceBitmap.getConfig(), true);
        } else {
            bitmap = sourceBitmap;
        }
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        Canvas canvas = new Canvas(bitmap);
        Matrix matrix = new Matrix();

        // 正方形框
        int left = width / 3;
        int top = height / 8 * 5;
        int right = width / 3 * 2;
        int bottom = height / 8 * 5 + (right - left);
        if (isDrawRect) {
            drawRectangle(canvas, matrix, left, top, right, bottom);
        }
        drawLip(canvas, matrix, points);
        return bitmap;
    }

    private Bitmap cropMouth(Bitmap sourceBitmap) {
        int width = sourceBitmap.getWidth();
        int height = sourceBitmap.getHeight();
        // 正方形框
        int left = width / 3;
        int top = height / 8 * 5;
        int right = width / 3 * 2;
        int bottom = height / 8 * 5 + (right - left);
        Rect rectangle = new Rect(left, top, right, bottom);
        return BitmapUtils.getCropBitmap(sourceBitmap, rectangle, 1);
    }

    public void drawBorder(Canvas canvas, Matrix matrix, int left, int top, int right, int bottom) {
        // Border
        Paint borderPaint = new Paint();
        borderPaint.setColor(Color.GREEN);
        borderPaint.setStrokeWidth(STROKE_WIDTH);
        borderPaint.setStyle(Paint.Style.STROKE);

        if (canvas == null) {
            return;
        }
        canvas.save();
        canvas.setMatrix(matrix);
        // 正方形框
        Rect rectangle = new Rect(left, top, right, bottom);
        canvas.drawRect(rectangle, borderPaint);
        canvas.restore();
    }

    public void drawRectangle(Canvas canvas, Matrix matrix, int left, int top, int right, int bottom) {
        // Rectangle
        Paint rectPaint = new Paint();
        rectPaint.setColor(Color.BLACK);
        rectPaint.setStyle(Paint.Style.FILL);

        if (canvas == null) {
            return;
        }
        canvas.save();
        canvas.setMatrix(matrix);
        // 正方形框
        Rect rectangle = new Rect(left, top, right, bottom);
        canvas.drawRect(rectangle, rectPaint);
        canvas.restore();
    }

    public void drawPoint(Canvas canvas, Matrix matrix, float x, float y) {
        // Point
        Paint pointPaint = new Paint();
        pointPaint.setColor(Color.RED);
        pointPaint.setStyle(Paint.Style.FILL);

        if (canvas == null) {
            return;
        }
        canvas.save();
        canvas.setMatrix(matrix);
        canvas.drawCircle(x, y, RADIUS, pointPaint);
        canvas.restore();
    }

    public void drawLine(Canvas canvas, Matrix matrix, float startX, float startY, float stopX, float stopY) {
        // Line
        Paint linePaint = new Paint();
        linePaint.setColor(Color.RED);
        linePaint.setStrokeWidth(2);
        linePaint.setStyle(Paint.Style.STROKE);

        if (canvas == null) {
            return;
        }
        canvas.save();
        canvas.setMatrix(matrix);
        canvas.drawLine(startX, startY, stopX, stopY, linePaint);
        canvas.restore();
    }

    public void drawLip(Canvas canvas, Matrix matrix, float[] points) {
        for (int i = 0; i < points.length - 1; i = i + 2) {
            drawPoint(canvas, matrix, points[i], points[i + 1]);
            if (i < points.length - 3) {
                drawLine(canvas, matrix, points[i], points[i + 1], points[i + 2], points[i + 3]);
            }
        }
    }

    private List<Frame> generateFrames(List<Mouth> mouths) {
        List<Frame> frames = new ArrayList<>();
        for (int i = 0; i < mouths.size(); i++) {
            Mouth mouth = mouths.get(i);
            Bitmap sceneBtm = CameraUtils.getSceneBtm(mouth.getData(), mouth.getWidth(), mouth.getHeight());
            Bitmap rotateFaceBtm = BitmapUtils.rotateBitmap(sceneBtm, 270, true, false);
            Bitmap croppedMouthBitmap = cropMouth(rotateFaceBtm);
            frames.add(new Frame(i, croppedMouthBitmap));
        }
        return frames;
    }
}
