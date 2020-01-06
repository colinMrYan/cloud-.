package com.inspur.emmcloud.basemodule.util;

import com.inspur.emmcloud.componentservice.download.FileDownloadInfo;

import org.xutils.db.sqlite.WhereBuilder;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by chenmch on 2019/9/13.
 */

public class FileDownloadInfoCacheUtils {
    public static void saveFileDownloadInfo(final FileDownloadInfo fileDownloadInfo) {
        try {
            DbCacheUtils.getDb().saveOrUpdate(fileDownloadInfo);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void deleteFileDownloadInfoByFilePath(String filePath) {
        try {
            DbCacheUtils.getDb().delete(FileDownloadInfo.class, WhereBuilder.b("filePath", "=", filePath));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void deleteFileDownloadInfoByFilePath(List<String> filePathList) {
        try {
            DbCacheUtils.getDb().delete(FileDownloadInfo.class, WhereBuilder.b("filePath", "in", filePathList));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void deleteFileDownloadInfo(FileDownloadInfo fileDownloadInfo) {
        try {
            DbCacheUtils.getDb().delete(fileDownloadInfo);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void deleteFileDownloadInfoList(List<FileDownloadInfo> fileDownloadInfoList) {
        try {
            if (fileDownloadInfoList == null || fileDownloadInfoList.size() == 0) {
                return;
            }
            DbCacheUtils.getDb().delete(fileDownloadInfoList);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static FileDownloadInfo getFileDownloadInfo(String category, String categoryId, String categoryFileName) {
        try {
            return DbCacheUtils.getDb().selector(FileDownloadInfo.class).where("category", "=", category).and("categoryId", "=", categoryId).and("categoryFileName", "=", categoryFileName).findFirst();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }


    public static List<FileDownloadInfo> getFileDownloadInfoList(String category) {
        List<FileDownloadInfo> fileDownloadInfoList = null;
        try {
            fileDownloadInfoList = DbCacheUtils.getDb().selector(FileDownloadInfo.class).where("category", "=", category).findAll();
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (fileDownloadInfoList == null) {
            fileDownloadInfoList = new ArrayList<>();
        }
        return fileDownloadInfoList;
    }

}
