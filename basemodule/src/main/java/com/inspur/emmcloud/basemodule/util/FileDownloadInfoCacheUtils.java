package com.inspur.emmcloud.basemodule.util;

import com.inspur.emmcloud.basemodule.bean.FileDownloadInfo;

import org.xutils.db.sqlite.WhereBuilder;

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

    public static void deleteFileDownloadInfo(FileDownloadInfo fileDownloadInfo) {
        try {
            DbCacheUtils.getDb().delete(fileDownloadInfo);
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

}
