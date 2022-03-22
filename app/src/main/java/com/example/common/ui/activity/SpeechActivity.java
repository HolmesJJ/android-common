package com.example.common.ui.activity;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.PixelFormat;
import android.hardware.Camera;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.View;
import android.widget.MediaController;

import androidx.annotation.NonNull;

import com.example.common.BR;
import com.example.common.R;
import com.example.common.adapter.speech.ChartAdapter;
import com.example.common.base.BaseActivity;
import com.example.common.camera.CameraOverlap;
import com.example.common.config.Config;
import com.example.common.databinding.ActivitySpeechBinding;
import com.example.common.landmark.EGLUtils;
import com.example.common.landmark.GLBitmap;
import com.example.common.landmark.GLFrame;
import com.example.common.landmark.GLFramebuffer;
import com.example.common.thread.CustomThreadPool;
import com.example.common.ui.viewmodel.SpeechViewModel;
import com.example.common.utils.ContextUtils;
import com.example.common.utils.FileUtils;
import com.example.common.utils.ListenerUtils;
import com.github.piasy.rxandroidaudio.AudioRecorder;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class SpeechActivity extends BaseActivity<ActivitySpeechBinding, SpeechViewModel>
        implements SurfaceHolder.Callback, Camera.PreviewCallback {

    private static final String TAG = SpeechActivity.class.getSimpleName();

    private static final CustomThreadPool THREAD_POOL_CAMERA = new CustomThreadPool(Thread.NORM_PRIORITY);
    private static final CustomThreadPool THREAD_POOL_RECORD = new CustomThreadPool(Thread.NORM_PRIORITY);

    private final Object lockObj = new Object();

    private CameraOverlap mCameraOverlap;
    private Matrix mMatrix;

    private EGLUtils mEglUtils;
    private GLFramebuffer mFramebuffer;
    private GLFrame mFrame;
    private GLBitmap mBitmap;

    private AudioRecorder mAudioRecorder;

    private Handler mHandler;

    private File mFramesFolder;
    private File mRecordFolder;
    private File mAudioFile;
    private int mCountFrame = 0;

    private byte[] mRGBCameraTrackNv21;
    private boolean mIsRGBCameraNv21Ready = false;
    private boolean mIsRecording = false;

    private int mEnglishId;
    private int mCurProgress;

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
        if (mHandler == null) {
            mHandler = new Handler();
        }
        initChart();
        getBinding().svOverlap.getHolder().setFormat(PixelFormat.TRANSLUCENT);
        mFramesFolder = new File(FileUtils.FRAMES_DIR + mEnglishId);
        initCamera();
        initPlayer();
        playVideo("exercise");
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
        if (!getBinding().vvPlayer.isPlaying()) {
            getBinding().vvPlayer.start();
        }
    }

    @Override
    protected void onDestroy() {
        listeners.clear();
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
        getViewModel().getSpeechData().observe(this, speechData -> {
            Log.i(TAG, "SpeechData: " + speechData);
            Config.setSpeechData(speechData);
            sendSpeechData(speechData);
            getBinding().cvpChart.setCurrentItem(1);
        });
        getViewModel().getFrameName().observe(this, frameName -> {
            Bitmap frameBitmap = BitmapFactory.decodeFile(FileUtils.FRAMES_DIR + mEnglishId + "/capture_" + frameName);
            getBinding().ivMouthFrame.setImageBitmap(frameBitmap);
        });
    }

    private void setOnTouchListener() {
        ListenerUtils.setOnTouchListener(getBinding().lpvRecord, new View.OnTouchListener() {
            @SuppressLint("ClickableViewAccessibility")
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                switch (motionEvent.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        getBinding().cvpChart.setCurrentItem(0);
                        mIsRecording = true;
                        if (getBinding().vvPlayer.isPlaying()) {
                            getBinding().vvPlayer.pause();
                        }
                        mHandler.postDelayed(mProgressRunnable, 50);
                        getBinding().rlVolumeContainer.setVisibility(View.VISIBLE);
                        mHandler.post(mVolumeRunnable);
                        THREAD_POOL_RECORD.execute(() -> {
                            startRecord();
                        });
                        break;
                    case MotionEvent.ACTION_UP:
                        if (mIsRecording) {
                            THREAD_POOL_RECORD.execute(() -> {
                                stopRecord();
                                if (getViewModel() != null) {
                                    getViewModel().uploadAudio(mAudioFile, mEnglishId);
                                }
                            });
                            mHandler.removeCallbacks(mProgressRunnable);
                            getBinding().lpvRecord.setProgress(0);
                            mCurProgress = 0;
                            mHandler.removeCallbacks(mVolumeRunnable);
                            getBinding().rlVolumeContainer.setVisibility(View.GONE);
                            mIsRecording = false;
                        }
                        break;
                    default:
                        break;
                }
                return true;
            }
        });
    }

    private final Runnable mVolumeRunnable = new Runnable() {
        @Override
        public void run() {
            updateVolume(getVolume());
            mHandler.postDelayed(mVolumeRunnable, 50);
        }
    };

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
                    THREAD_POOL_RECORD.execute(() -> {
                        stopRecord();
                        if (getViewModel() != null) {
                            getViewModel().uploadAudio(mAudioFile, mEnglishId);
                        }
                    });
                    mHandler.removeCallbacks(mProgressRunnable);
                    getBinding().lpvRecord.setProgress(0);
                    mCurProgress = 0;
                    mHandler.removeCallbacks(mVolumeRunnable);
                    getBinding().rlVolumeContainer.setVisibility(View.GONE);
                    mIsRecording = false;
                }
            }
        }
    };

    private void initChart() {
        getBinding().cvpChart.setAdapter(new ChartAdapter(getSupportFragmentManager(), mEnglishId));
        getBinding().cvpChart.setCurrentItem(0);
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
            if (getViewModel() != null) {
                getViewModel().getFrameName().postValue(getFrameName());
            }
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

    private String getFrameName() {
        String[] children = mFramesFolder.list();
        if (children == null) {
            return "";
        }
        // 减缓速度
        int countFrame = mCountFrame / 2 + 1;
        // Excluding the zip file
        int size = children.length - 1;
        final String frameName;
        if (countFrame >= 1 && countFrame < 10) {
            frameName = "0" + countFrame;
        } else if (countFrame >= 10 && countFrame <= size) {
            frameName = String.valueOf(countFrame);
        } else if (countFrame > size && countFrame <= size + 30) {
            frameName = String.valueOf(size);
        } else {
            mCountFrame = 0;
            frameName = "01";
        }
        mCountFrame++;
        return frameName + ".png";
    }

    private void initPlayer() {
        MediaController mediaController = new MediaController(SpeechActivity.this);
        mediaController.setAnchorView(getBinding().vvPlayer);
        mediaController.setMediaPlayer(getBinding().vvPlayer);
        getBinding().vvPlayer.setMediaController(mediaController);
        getBinding().vvPlayer.setZOrderOnTop(true);
        getBinding().vvPlayer.setZOrderMediaOverlay(true);
        getBinding().vvPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mPlayer) {
                mPlayer.start();
                mPlayer.setLooping(true);
            }
        });
    }

    private void playVideo(String videoType) {
        String path = FileUtils.VIDEO_DIR + mEnglishId + File.separator + videoType + ".mp4";
        getBinding().vvPlayer.setVideoURI(Uri.parse(path));
        getBinding().vvPlayer.start();
    }

    private void startRecord() {
        mRecordFolder = new File(FileUtils.AUDIO_DIR, String.valueOf(mEnglishId));
        if (!mRecordFolder.exists()) {
            mRecordFolder.mkdirs();
        }
        mAudioRecorder = AudioRecorder.getInstance();
        mAudioFile = new File(mRecordFolder.getAbsolutePath() + "/record.mp3");
        mAudioRecorder.prepareRecord(
                MediaRecorder.AudioSource.MIC,
                MediaRecorder.OutputFormat.MPEG_4,
                MediaRecorder.AudioEncoder.AAC,
                mAudioFile);
        mAudioRecorder.startRecord();
    }

    private void stopRecord() {
        if (mAudioRecorder != null) {
            mAudioRecorder.stopRecord();
            mAudioRecorder = null;
        }
    }

    // 获取音量值，只是针对录音音量
    public int getVolume() {
        int volume = 0;
        // 录音
        if (mAudioRecorder != null) {
            volume = mAudioRecorder.getMaxAmplitude() / 650;
            if (volume != 0) {
                volume = (int) (10 * Math.log10(volume)) / 3;
            }
        }
        return volume;
    }

    // 更新音量图
    private void updateVolume(int volume) {
        switch (volume) {
            case 1:
                getBinding().ivVolume.setImageResource(R.drawable.volume1);
                break;
            case 2:
                getBinding().ivVolume.setImageResource(R.drawable.volume2);
                break;
            case 3:
                getBinding().ivVolume.setImageResource(R.drawable.volume3);
                break;
            case 4:
                getBinding().ivVolume.setImageResource(R.drawable.volume4);
                break;
            case 5:
                getBinding().ivVolume.setImageResource(R.drawable.volume5);
                break;
            case 6:
                getBinding().ivVolume.setImageResource(R.drawable.volume6);
                break;
            case 7:
                getBinding().ivVolume.setImageResource(R.drawable.volume7);
                break;
            default:
                break;
        }
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

    private final List<ISpeechDataUpdated> listeners = new ArrayList<>();

    public interface ISpeechDataUpdated {
        void onSpeechDataUpdated(String speechData);
    }

    public void setSpeechDataUpdated(ISpeechDataUpdated listener) {
        this.listeners.add(listener);
    }

    public void sendSpeechData(String speechData) {
        for (int i = 0; i < listeners.size(); i++) {
            listeners.get(i).onSpeechDataUpdated(speechData);
        }
    }
}
