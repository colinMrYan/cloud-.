package com.inspur.emmcloud.util.privates;

import com.inspur.emmcloud.basemodule.util.DbCacheUtils;
import com.inspur.emmcloud.bean.DownloadInfo;

import org.xutils.ex.DbException;

import java.util.ArrayList;
import java.util.List;

public class DownloadCacheUtils {
    /**
     * 获取正在下载中的文件
     */
    public static List<DownloadInfo> getAllDownloadingList() {
        List<DownloadInfo> list = new ArrayList<>();
        try {
            list = DbCacheUtils.getDb().findAll(DownloadInfo.class);
        } catch (DbException e) {
            e.printStackTrace();
        }
        if (list == null) {
            list = new ArrayList<>();
        }
        return list;
    }

    public static void saveDownloadFile(DownloadInfo downloadInfo) {
        try {
            if (downloadInfo != null) {
                DbCacheUtils.getDb().saveOrUpdate(downloadInfo);
            }
        } catch (DbException e) {
            e.printStackTrace();
        }
    }

    public static void deleteDownloadFile(DownloadInfo downloadInfo) {
        try {
            if (downloadInfo != null) {
                DbCacheUtils.getDb().delete(downloadInfo);
            }
        } catch (DbException e) {
            e.printStackTrace();
        }
    }

    public static void saveDownloadFileList(List<DownloadInfo> downloadInfoList) {
        try {
            if (downloadInfoList != null && downloadInfoList.size() > 0) {
                DbCacheUtils.getDb().saveOrUpdate(downloadInfoList);
            }
        } catch (DbException e) {
            e.printStackTrace();
        }
    }
}
