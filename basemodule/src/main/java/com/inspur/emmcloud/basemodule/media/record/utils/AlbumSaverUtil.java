package com.inspur.emmcloud.basemodule.media.record.utils;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.net.Uri;
import android.os.Build;
import android.os.ParcelFileDescriptor;
import android.provider.MediaStore;
import android.support.annotation.NonNull;

import com.inspur.emmcloud.basemodule.application.BaseApplication;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Date：2022/8/10
 * Author：wang zhen
 * Description 视频保存到本地相册
 */
public class AlbumSaverUtil {
    public static final String VOLUME_EXTERNAL_PRIMARY = "external_primary";
    private static final String IS_PENDING = "is_pending";
    private static AlbumSaverUtil sInstance;
    private final ContentResolver mContentResolver;
    private final Context mContext;

    public static AlbumSaverUtil getInstance(@NonNull Context context) {
        if (sInstance == null) {
            sInstance = new AlbumSaverUtil(context);
        }
        return sInstance;
    }

    private AlbumSaverUtil(@NonNull Context context) {
        mContext = context.getApplicationContext();
        mContentResolver = context.getApplicationContext().getContentResolver();
    }

    /**
     * 插入到本地相册
     */
    public void saveVideoToDCIM(String videoPath, long duration) {
        if (Build.VERSION.SDK_INT >= 29) {
            saveVideoToDCIMOnAndroid10(videoPath, duration);
        } else {
            saveVideoToDCIMBelowAndroid10(videoPath, duration);
        }
    }

    private void saveVideoToDCIMBelowAndroid10(String mVideoOutputPath, long mVideoDuration) {
        File file = new File(mVideoOutputPath);
        if (file.exists()) {
            try {
                ContentValues values = initCommonContentValues(file);
                values.put(MediaStore.Video.VideoColumns.DATE_TAKEN, System.currentTimeMillis());
                values.put(MediaStore.MediaColumns.MIME_TYPE, "video/mp4");
                values.put(MediaStore.Video.VideoColumns.DURATION, mVideoDuration);
                mContext.getContentResolver().insert(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, values);

//                if (mCoverImagePath != null) {
//                    insertVideoThumb(file.getPath(), mCoverImagePath);
//                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
        }
    }

    /**
     * Android 10(Q) 保存视频文件到本地的方法
     */
    private void saveVideoToDCIMOnAndroid10(String mVideoOutputPath, long mVideoDuration) {
        File file = new File(mVideoOutputPath);
        if (file.exists()) {
            ContentValues values = new ContentValues();
            long currentTimeInSeconds = System.currentTimeMillis();
            values.put(MediaStore.MediaColumns.TITLE, file.getName());
            values.put(MediaStore.MediaColumns.DISPLAY_NAME, file.getName());
            values.put(MediaStore.MediaColumns.DATE_MODIFIED, currentTimeInSeconds);
            values.put(MediaStore.MediaColumns.DATE_ADDED, currentTimeInSeconds);
            values.put(MediaStore.MediaColumns.SIZE, file.length());
            values.put(MediaStore.MediaColumns.MIME_TYPE, "video/mp4");
            // 时长
            values.put(MediaStore.Video.VideoColumns.DURATION, mVideoDuration);
            values.put(MediaStore.Video.VideoColumns.DATE_TAKEN, System.currentTimeMillis());
            // Android 10 插入到图库标志位
            values.put(IS_PENDING, 1);

            Uri collection = MediaStore.Video.Media.getContentUri(VOLUME_EXTERNAL_PRIMARY);
            Uri item = BaseApplication.getInstance().getContentResolver().insert(collection, values);
            ParcelFileDescriptor pfd = null;
            FileOutputStream fos = null;
            FileInputStream fis = null;
            try {
                pfd = BaseApplication.getInstance().getContentResolver().openFileDescriptor(item, "w");
                // Write data into the pending image.
                fos = new FileOutputStream(pfd.getFileDescriptor());
                fis = new FileInputStream(file);
                byte[] data = new byte[1024];
                int length = -1;
                while ((length = fis.read(data)) != -1) {
                    fos.write(data, 0, length);
                }
                fos.flush();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (pfd != null) {
                    try {
                        pfd.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                if (fos != null) {
                    try {
                        fos.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                if (fis != null) {
                    try {
                        fos.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
            // 插入成功后，更新状态，让其他 app 可以看到新的视频
            values.clear();
            values.put(IS_PENDING, 0);
            BaseApplication.getInstance().getContentResolver().update(item, values, null, null);
        } else {
        }
    }

    @NonNull
    private ContentValues initCommonContentValues(@NonNull File saveFile) {
        ContentValues values = new ContentValues();
        long currentTimeInSeconds = System.currentTimeMillis();
        values.put(MediaStore.MediaColumns.TITLE, saveFile.getName());
        values.put(MediaStore.MediaColumns.DISPLAY_NAME, saveFile.getName());
        values.put(MediaStore.MediaColumns.DATE_MODIFIED, currentTimeInSeconds);
        values.put(MediaStore.MediaColumns.DATE_ADDED, currentTimeInSeconds);
        values.put(MediaStore.MediaColumns.DATA, saveFile.getAbsolutePath());
        values.put(MediaStore.MediaColumns.SIZE, saveFile.length());
        return values;
    }
}
