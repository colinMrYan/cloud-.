package com.inspur.emmcloud.web.plugin.file;

import android.os.Environment;
import android.util.Log;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.NumberFormat;

/**
 * 文件工具类
 *
 * @author 浪潮移动应用平台(IMP)产品组
 */
public class FileUtil {
    private static final String TAG = "FileUtil";

    /**
     * 获取SD路径
     **/
    public static String getSDPath() {

        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            File sdDir = Environment.getExternalStorageDirectory();// 获取根目录
            return sdDir.getPath();
        }
        return "/sdcard";
    }

    /**
     * 获取MIME类型
     **/
    public static String getMIMEType(String name) {
        String type = "";
        String end = name.substring(name.lastIndexOf(".") + 1, name.length()).toLowerCase();
        if (end.equals("apk")) {
            return "application/vnd.android.package-archive";
        } else if (end.equals("mp4") || end.equals("avi") || end.equals("3gp")
                || end.equals("rmvb")) {
            type = "video";
        } else if (end.equals("mp3") || end.equals("mid") || end.equals("wav")) {
            type = "audio";
        } else if (end.equals("jpg") || end.equals("gif") || end.equals("png")
                || end.equals("jpeg") || end.equals("bmp")) {
            type = "image";
        } else if (end.equals("txt") || end.equals("log")) {
            type = "text";
        } else {
            type = "*";
        }
        type += "/*";
        return type;
    }

    /**
     * 获取文件信息
     **/
    public static FileInfo getFileInfo(File f) {
        FileInfo info = new FileInfo();
        info.Name = f.getName();
        info.IsDirectory = f.isDirectory();
        calcFileContent(info, f);
        return info;
    }

    /**
     * 计算文件内容
     **/
    private static void calcFileContent(FileInfo info, File f) {
        if (f.isFile()) {
            info.Size += f.length();
        }
        if (f.isDirectory()) {
            File[] files = f.listFiles();
            if (files != null && files.length > 0) {
                for (int i = 0; i < files.length; ++i) {
                    File tmp = files[i];
                    if (tmp.isDirectory()) {
                        info.FolderCount++;
                    } else if (tmp.isFile()) {
                        info.FileCount++;
                    }
                    if (info.FileCount + info.FolderCount >= 10000) { // 超过一万不计算
                        break;
                    }
                    calcFileContent(info, tmp);
                }
            }
        }
    }


    /**
     * 转换文件大小(M为最小单位)
     *
     * @param fileSize
     * @return
     */
    public static String formetFileSizeMinM(long fileSize) {
        NumberFormat nf = NumberFormat.getInstance();
        nf.setMaximumFractionDigits(1);
        String formatFileSize = "";
        if (fileSize < 1024 * 1024 * 1024) {
            formatFileSize = nf.format((float) fileSize / 1024 / 1024) + " M";
        } else {
            formatFileSize = nf.format((double) fileSize / 1024 / 1024 / 1024) + " G";
        }
        return formatFileSize;
    }

    /**
     * 合并路径
     **/
    public static String combinPath(String path, String fileName) {
        Log.i(TAG, "" + (path.endsWith(File.separator) ? "" : File.separator));
        return path + (path.endsWith(File.separator) ? "" : File.separator) + fileName;
    }


    /**
     * 删除文件,同时也要删除该文件夹中的内容(文件或文件夹)
     **/
    public static void deleteFile(File f) {
        if (f.isDirectory()) {
            File[] files = f.listFiles();
            if (files != null && files.length > 0) {
                for (int i = 0; i < files.length; ++i) {
                    deleteFile(files[i]);
                }
            }
        }
        f.delete();//不为文件夹才可以删除该文件
    }

    /**
     * 复制文件
     **/
    public static boolean copyFile(File src, File tar) throws Exception {
        //如果复制为一个文件
        if (src.isFile()) {
            InputStream is = new FileInputStream(src);
            OutputStream op = new FileOutputStream(tar);
            BufferedInputStream bis = new BufferedInputStream(is);
            BufferedOutputStream bos = new BufferedOutputStream(op);
            byte[] bt = new byte[1024 * 8];
            int len = bis.read(bt);
            while (len != -1) {
                bos.write(bt, 0, len);
                len = bis.read(bt);
            }
            bis.close();
            bos.close();
        }
        //如果复制为一个目录则将目录下的所有文件复制
        if (src.isDirectory()) {
            File[] f = src.listFiles();
            tar.mkdir();
            for (int i = 0; i < f.length; i++) {
                copyFile(f[i].getAbsoluteFile(), new File(tar.getAbsoluteFile() + File.separator
                        + f[i].getName()));
            }
        }
        return true;
    }

    /**
     * 移动文件 就是把原来的删除，然后复制即可
     **/
    public static boolean moveFile(File src, File tar) throws Exception {
        if (copyFile(src, tar)) {
            deleteFile(src);
            return true;
        }
        return false;
    }
}
