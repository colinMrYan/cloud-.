package com.inspur.emmcloud.basemodule.util;

import android.util.Log;

import com.inspur.emmcloud.baselib.util.StringUtils;
import com.inspur.emmcloud.basemodule.bean.DownloadFileCategory;
import com.inspur.emmcloud.basemodule.bean.FileDownloadInfo;
import com.inspur.emmcloud.basemodule.config.MyAppConfig;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

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

    public String getFilePath(DownloadFileCategory downloadFileCategory, String categoryId, String fileName) {
        String fileSavePath = getDownloadFilePath(DownloadFileCategory.CATEGORY_VOLUME_FILE, categoryId, fileName);
        if (StringUtils.isBlank(fileSavePath)) {
            fileSavePath = MyAppConfig.getFileDownloadByUserAndTanentDirPath() +
                    FileUtils.getNoDuplicateFileNameInDir(MyAppConfig.getFileDownloadByUserAndTanentDirPath(), fileName);
        }

        return fileSavePath;
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

    /**
     * 根据业务类型获取所有已下载的且本地还存在的文件下载信息列表,并删除不存在的文件数据
     *
     * @param downloadFileCategory
     * @return
     */
    public List<File> getFileDownloadFileList(DownloadFileCategory downloadFileCategory) {
        List<File> downloadFileList = new ArrayList<>();
        List<FileDownloadInfo> invalidFileDownloadInfoList = new ArrayList<>();
        List<FileDownloadInfo> fileDownloadInfoList = FileDownloadInfoCacheUtils.getFileDownloadInfoList(downloadFileCategory.getValue());
        Log.d("zhang", "getFileDownloadFileList: fileDownloadInfoList.size = " + fileDownloadInfoList.size());
        for (FileDownloadInfo fileDownloadInfo : fileDownloadInfoList) {
            File file = new File(fileDownloadInfo.getFilePath());
            if (file.exists()) {
                downloadFileList.add(0, file);
            } else {
                invalidFileDownloadInfoList.add(fileDownloadInfo);
            }
        }
        FileDownloadInfoCacheUtils.deleteFileDownloadInfoList(invalidFileDownloadInfoList);
        return downloadFileList;
    }

    /**
     * 根据业务类型获取所有已下载的且本地还存在的文件下载信息列表,并删除不存在的文件数据
     *
     * @param downloadFileCategory
     * @return FileDownloadInfo 类型
     */
    public List<FileDownloadInfo> getFileDownloadInfoFileList(DownloadFileCategory downloadFileCategory) {
        List<FileDownloadInfo> resultList = new ArrayList<>();
        List<FileDownloadInfo> invalidFileDownloadInfoList = new ArrayList<>();
        List<FileDownloadInfo> fileDownloadInfoList = FileDownloadInfoCacheUtils.getFileDownloadInfoList(downloadFileCategory.getValue());
        for (FileDownloadInfo fileDownloadInfo : fileDownloadInfoList) {
            File file = new File(fileDownloadInfo.getFilePath());
            if (file.exists()) {
                resultList.add(0, fileDownloadInfo);
            } else {
                invalidFileDownloadInfoList.add(fileDownloadInfo);
            }
        }
        FileDownloadInfoCacheUtils.deleteFileDownloadInfoList(invalidFileDownloadInfoList);
        return resultList;
    }


    /**
     * 删除已下载的文件（实体文件和数据库内容都删除）
     *
     * @param filePath
     */
    public void deleteDownloadFile(String filePath) {
        FileDownloadInfoCacheUtils.deleteFileDownloadInfoByFilePath(filePath);
        File file = new File(filePath);
        if (file.exists()) {
            file.delete();
        }
    }

    /**
     * 删除已下载的文件（实体文件和数据库内容都删除）
     *
     * @param filePathList
     */
    public void deleteDownloadFile(List<String> filePathList) {
        FileDownloadInfoCacheUtils.deleteFileDownloadInfoByFilePath(filePathList);
        for (String filePath : filePathList) {
            File file = new File(filePath);
            if (file.exists()) {
                file.delete();
            }
        }
    }
}
