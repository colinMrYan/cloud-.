package com.inspur.emmcloud.basemodule.util;

import com.inspur.emmcloud.basemodule.bean.DownloadFileCategory;
import com.inspur.emmcloud.basemodule.bean.FileDownloadInfo;

/**
 * Created by chenmch on 2019/9/13.
 * 下载文件的存储路径
 */

public class FileDownloadManager {
    private static FileDownloadManager mInstance;

    private FileDownloadManager() {
    }

    public static FileDownloadManager getInstance() {
        if (mInstance == null) {
            synchronized (LanguageManager.class) {
                if (mInstance == null) {
                    mInstance = new FileDownloadManager();
                }
            }
        }
        return mInstance;
    }

    public String getDownloadFilePath(DownloadFileCategory downloadFileCategory, String categoryId, String fileName) {
        FileDownloadInfo fileDownloadInfo = FileDownloadInfoCacheUtils.getFileDownloadInfo(downloadFileCategory.getValue(), categoryId, fileName);
        if (fileDownloadInfo != null) {
            String filePath = fileDownloadInfo.getFilePath();
            if (FileUtils.isFileExist(filePath)) {
                return filePath;
            }
            FileDownloadInfoCacheUtils.deleteFileDownloadInfo(fileDownloadInfo);
        }
        return "";
    }

    public boolean isDownloadFileExists(DownloadFileCategory downloadFileCategory, String categoryId, String fileName) {
        FileDownloadInfo fileDownloadInfo = FileDownloadInfoCacheUtils.getFileDownloadInfo(downloadFileCategory.getValue(), categoryId, fileName);
        if (fileDownloadInfo != null) {
            String filePath = fileDownloadInfo.getFilePath();
            if (FileUtils.isFileExist(filePath)) {
                return true;
            }
            FileDownloadInfoCacheUtils.deleteFileDownloadInfo(fileDownloadInfo);
        }
        return false;
    }

    public void saveDownloadFileInfo(DownloadFileCategory downloadFileCategory, String categoryId, String fileName, String filePath) {
        FileDownloadInfoCacheUtils.deleteFileDownloadInfoByFilePath(filePath);
        FileDownloadInfo fileDownloadInfo = new FileDownloadInfo(downloadFileCategory.getValue(), categoryId, fileName, filePath);
        FileDownloadInfoCacheUtils.saveFileDownloadInfo(fileDownloadInfo);
    }
}
