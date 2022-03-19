package com.example.common.ui.viewmodel;

import android.graphics.Bitmap;

import androidx.annotation.NonNull;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.MutableLiveData;

import com.example.common.base.BaseViewModel;
import com.example.common.model.mouth.Mouth;
import com.example.common.thread.ThreadManager;
import com.example.common.utils.BitmapUtils;
import com.example.common.utils.CameraUtils;
import com.example.common.utils.FileUtils;

import java.io.File;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class MouthViewModel extends BaseViewModel {

    private final MutableLiveData<Boolean> mIsShowLoading = new MutableLiveData<>();

    @Override
    public void onCreate(@NonNull LifecycleOwner owner) {

    }

    @Override
    public void onDestroy(@NonNull LifecycleOwner owner) {

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
                    BitmapUtils.savePhotoToSDCard(captureFolder.getAbsolutePath(), "MaxHMouth.jpg", maxHBitmap, 100);
                    // Max Vertical Distance Mouths
                    List<Mouth> sortedMaxVMouths = mouths.stream().sorted(new Comparator<Mouth>() {
                        @Override
                        public int compare(Mouth mouth1, Mouth mouth2) {
                            return Double.compare(mouth2.getMaxVertDist(), mouth1.getMaxVertDist());
                        }
                    }).collect(Collectors.toList());
                    Mouth maxVMouth = sortedMaxVMouths.get(0);
                    Bitmap maxVBitmap = CameraUtils.getSceneBtm(maxVMouth.getData(), maxVMouth.getWidth(), maxVMouth.getHeight());
                    BitmapUtils.savePhotoToSDCard(captureFolder.getAbsolutePath(), "MaxVMouth.jpg", maxVBitmap, 100);
                }
                mIsShowLoading.postValue(false);
            }
        });
    }
}
