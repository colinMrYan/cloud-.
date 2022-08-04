package com.inspur.emmcloud.basemodule.media.record.utils;

import android.text.TextUtils;
import android.util.Log;


import com.inspur.emmcloud.basemodule.application.BaseApplication;
import com.inspur.emmcloud.basemodule.config.Constant;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * 视频路径生成器
 */
public class VideoPathUtil {
    private static final String TAG = "VideoPathUtil";

    /**
     * 生成编辑后输出视频路径
     *
     * @param name
     * @return 路径
     */
    public static String generateVideoPath(String name) {
        File sdcardDir = BaseApplication.getInstance().getExternalFilesDir(null);
        if (sdcardDir == null) {
            Log.e(TAG, "generateVideoPath sdcardDir is null");
            return "";
        }
        String outputPath = sdcardDir + File.separator + Constant.DEFAULT_MEDIA_PACK_FOLDER;
        File outputFolder = new File(outputPath);

        if (!outputFolder.exists()) {
            outputFolder.mkdirs();
        }
        String current = String.valueOf(System.currentTimeMillis() / 1000);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_HHmmss");
        String time = sdf.format(new Date(Long.valueOf(current + "000")));
        String saveFileName = String.format(name + "CompressVideo_%s.mp4", time);
        return outputFolder + "/" + saveFileName;
    }

    public static String getCustomVideoOutputPath() {
        return getCustomVideoOutputPath(null);
    }

    public static String getCustomVideoOutputPath(String fileNamePrefix) {
        long currentTime = System.currentTimeMillis();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_HHmmssSSS");
        String time = sdf.format(new Date(currentTime));

        File sdcardDir = BaseApplication.getInstance().getExternalFilesDir(null);
        if (sdcardDir == null) {
            Log.e(TAG, "sdcardDir is null");
            return null;
        }

        String outputDir = sdcardDir + File.separator + Constant.OUTPUT_DIR_NAME;
        File outputFolder = new File(outputDir);
        if (!outputFolder.exists()) {
            outputFolder.mkdir();
        }
        String tempOutputPath;
        if (TextUtils.isEmpty(fileNamePrefix)) {
            tempOutputPath = outputDir + File.separator + "TXUGC_" + time + ".mp4";
        } else {
            tempOutputPath = outputDir + File.separator + "TXUGC_" + fileNamePrefix + time + ".mp4";
        }
        return tempOutputPath;
    }
}
