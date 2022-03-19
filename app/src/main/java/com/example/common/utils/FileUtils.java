package com.example.common.utils;

import android.content.Context;
import android.text.TextUtils;

import com.example.common.R;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public final class FileUtils {

    // /storage/emulated/0/Android/data/com.example.<APP_NAME>/files/
    public static final String SDCARD_PATH = ContextUtils.getContext().getExternalFilesDir(null).getAbsolutePath() + File.separator;
    // /data/user/0/com.example.<APP_NAME>/files/
    public static final String DATA_PATH = ContextUtils.getContext().getFilesDir().getAbsolutePath() + File.separator;
    public static final String APP_DIR = SDCARD_PATH + ContextUtils.getContext().getString(R.string.app_name) + File.separator;
    public static final String DATA_APP_DIR = DATA_PATH + ContextUtils.getContext().getString(R.string.app_name) + File.separator;
    public static final String USERS_DATA_DIR = DATA_APP_DIR + "users" + File.separator;
    // Deaf App Folder
    public static final String AUDIO_DIR = APP_DIR + "Record" + File.separator;
    public static final String VIDEO_DIR = APP_DIR + "Video" + File.separator;
    public static final String FRAMES_DIR = APP_DIR + "Frames" + File.separator;
    public static final String CAPTURE_DIR = APP_DIR + "Capture" + File.separator;
    // Model
    public static final String MODEL_DIR = APP_DIR + "ZeuseesFaceTracking" + File.separator;
    public static final String MODEL_PATH = MODEL_DIR + "models" + File.separator;
    public static final String ASSET_PATH = "ZeuseesFaceTracking";

    private FileUtils() {
    }

    public static void init() {
        File app = new File(APP_DIR);
        if (!app.exists()) {
            app.mkdirs();
        }
        //保存用户信息
        File users = new File(USERS_DATA_DIR);
        if (!users.exists()) {
            users.mkdirs();
        }
        // Deaf App
        File audioFolder = new File(AUDIO_DIR);
        if (!audioFolder.exists()) {
            audioFolder.mkdirs();
        }
        File videoFolder = new File(VIDEO_DIR);
        if (!videoFolder.exists()) {
            videoFolder.mkdirs();
        }
        File framesFolder = new File(FRAMES_DIR);
        if (!framesFolder.exists()) {
            framesFolder.mkdirs();
        }
        File captureFolder = new File(CAPTURE_DIR);
        if (!captureFolder.exists()) {
            captureFolder.mkdirs();
        }
    }

    /**
     * 保存byte数组
     *
     * @param context context
     * @param path    保存路径
     * @param data    byte数组
     */
    public static void saveFile(Context context, String path, byte[] data) {
        OutputStream os = null;
        try {
            File file = new File(path);
            File parentFile = file.getParentFile();
            if (parentFile == null || !parentFile.exists() || !parentFile.isDirectory()) {
                parentFile.mkdirs();
            }
            os = new FileOutputStream(file);
            os.write(data, 0, data.length);
            os.flush();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (os != null) {
                try {
                    os.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * 保存字符串
     *
     * @param context context
     * @param path    文件 保存路径
     * @param content 待保存的内容
     */
    public static void saveFile(Context context, String path, String content) {
        if (TextUtils.isEmpty(path) || content == null || context == null) {
            return;
        }
        BufferedOutputStream bf = null;
        try {
            File file = new File(path);
            File parentFile = file.getParentFile();
            if (parentFile == null || !parentFile.exists() || !parentFile.isDirectory()) {
                parentFile.mkdirs();
            }
            bf = new BufferedOutputStream(new FileOutputStream(file));
            bf.write(content.getBytes(StandardCharsets.UTF_8));
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (bf != null) {
                try {
                    bf.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * 从assets目录下获取文件的byte数组
     *
     * @param context context
     * @param path    文件在assets目录下的 路径
     *
     * @return 返回文件的byte数组
     */
    public static byte[] getAssetsData(Context context, String path) {
        InputStream stream = null;
        try {
            stream = context.getAssets().open(path);
            int length = stream.available();
            byte[] data = new byte[length];
            stream.read(data);
            stream.close();
            return data;
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                stream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    /**
     * 删除文件
     *
     * @param path 文件路径
     */
    public static void deleteFile(String path) {
        if (TextUtils.isEmpty(path)) {
            return;
        }
        try {
            File file = new File(path);
            file.delete();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 删除目录
     *
     * @param path 目录路径
     */
    public static void deleteDirectory(String path) {
        if (TextUtils.isEmpty(path)) {
            return;
        }
        try {
            File dir = new File(path);
            if (dir.isDirectory()) {
                String[] children = dir.list();
                if (children != null) {
                    for (String child : children) {
                        new File(dir, child).delete();
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static byte[] getFileBytes(File file) throws Exception {
        InputStream is = null;
        try {
            is = new FileInputStream(file);
            byte[] bytes = new byte[is.available()];
            is.read(bytes);
            return bytes;
        } finally {
            IoUtils.closeQuietly(is);
        }
    }

    /**
     * 以行为单位读取文件，常用于读面向行的格式化文件
     */
    public static String readFileByLines(String fileName) {
        File file = new File(fileName);
        BufferedReader reader = null;
        StringBuilder sb = new StringBuilder();
        try {
            reader = new BufferedReader(new FileReader(file));
            String tempString = null;
            // 一次读入一行，直到读入null为文件结束
            while ((tempString = reader.readLine()) != null) {
                if (tempString.endsWith(".so")) {
                    int index = tempString.indexOf("/");
                    if (index != -1) {
                        String str = tempString.substring(index);
                        sb.append(str).append("\n");
                    }
                }
            }
            reader.close();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return sb.toString();
    }

    public static void unzip(File zipFile, File targetDirectory) {
        ZipInputStream zis = null;
        try {
            zis = new ZipInputStream(new BufferedInputStream(new FileInputStream(zipFile)));
            ZipEntry ze;
            int count;
            byte[] buffer = new byte[8192];
            while ((ze = zis.getNextEntry()) != null) {
                File file = new File(targetDirectory, ze.getName());
                File dir = ze.isDirectory() ? file : file.getParentFile();
                if (dir == null) {
                    return;
                }
                if (!dir.isDirectory() && !dir.mkdirs()) {
                    return;
                }
                if (ze.isDirectory()) {
                    continue;
                }
                FileOutputStream fout = new FileOutputStream(file);
                while ((count = zis.read(buffer)) != -1) {
                    fout.write(buffer, 0, count);
                }
                fout.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (zis != null) {
                    zis.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static void copyFilesFromAssets(Context context, String oldPath, String newPath) {
        try {
            String[] fileNames = context.getAssets().list(oldPath);
            if (fileNames.length > 0) {
                // directory
                File file = new File(newPath);
                if (!file.exists()) {
                    file.mkdirs();
                }
                for (String fileName : fileNames) {
                    copyFilesFromAssets(context, oldPath + "/" + fileName,
                            newPath + "/" + fileName);
                }
            } else {
                // file
                InputStream is = context.getAssets().open(oldPath);
                FileOutputStream fos = new FileOutputStream(new File(newPath));
                byte[] buffer = new byte[1024];
                int byteCount;
                while ((byteCount = is.read(buffer)) != -1) {
                    fos.write(buffer, 0, byteCount);
                }
                fos.flush();
                is.close();
                fos.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
