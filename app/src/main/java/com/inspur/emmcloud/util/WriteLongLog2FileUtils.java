package com.inspur.emmcloud.util;

import com.inspur.emmcloud.config.MyAppConfig;

import java.io.File;
import java.io.FileOutputStream;

/**
 * Created by yufuchang on 2017/9/13.
 */

public class WriteLongLog2FileUtils {
    private static void writeData2File(String content, String filename) {
        String filePath = MyAppConfig.LOCAL_CACHE_PATH;
        String fileName = filename;
        writeTxtToFile(content, filePath, fileName);
    }

    // 将字符串写入到文本文件中
    public static void writeTxtToFile(String strcontent, String filePath, String fileName) {
        //生成文件夹之后，再生成文件，不然会出错
        makeFilePath(filePath, fileName);
        String strFilePath = filePath + fileName;
        // 每次写入时，都换行写
        String strContent = strcontent + "\r\n";
        try {
            File file = new File(strFilePath);
            if (!file.exists()) {
                file.getParentFile().mkdirs();
                file.createNewFile();
            }
//            RandomAccessFile raf = new RandomAccessFile(file, "rwd");
//            raf.seek(file.length());
//            raf.write(strContent.getBytes());
//            raf.close();
            FileOutputStream out = null;
            out = new FileOutputStream(file);
            out.write(strContent.getBytes());
            out.flush();
        } catch (Exception e) {
            e.printStackTrace();
            LogUtils.YfcDebug("Error on write File:" + e);
        }
    }

    // 生成文件
    public static File makeFilePath(String filePath, String fileName) {
        File file = null;
        makeRootDirectory(filePath);
        try {
            file = new File(filePath + fileName);
            if (!file.exists()) {
                file.createNewFile();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return file;
    }

    // 生成文件夹
    public static void makeRootDirectory(String filePath) {
        File file = null;
        try {
            file = new File(filePath);
            if (!file.exists()) {
                file.mkdir();
            }
        } catch (Exception e) {
            e.printStackTrace();
            LogUtils.YfcDebug(e + "");
        }
    }
}
