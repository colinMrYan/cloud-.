package com.inspur.emmcloud.web.util;

import com.inspur.emmcloud.basemodule.util.DbCacheUtils;
import com.inspur.emmcloud.web.bean.WebFileDownloadBean;

import java.util.ArrayList;
import java.util.List;

/**
 * Date：2021/8/25
 * Author：wang zhen
 * Description web文件存储工具
 */
public class WebFileDownloadCacheUtils {
    public static List<WebFileDownloadBean> getWebFileListInDownloading() {
        List<WebFileDownloadBean> webFileDownloadInfoList = null;
        try {
            webFileDownloadInfoList = DbCacheUtils.getDb().findAll(WebFileDownloadBean.class);
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (webFileDownloadInfoList == null) {
            webFileDownloadInfoList = new ArrayList<>();
        }
        return webFileDownloadInfoList;
    }

    // 保持web文件列表信息
    public static void saveWebFileList(List<WebFileDownloadBean> webFileDownloadList) {
        try {
            if (webFileDownloadList != null && webFileDownloadList.size() > 0) {
                DbCacheUtils.getDb().saveOrUpdate(webFileDownloadList);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // 保存文件数据bean
    public synchronized static void saveWebFile(WebFileDownloadBean webFileDownloadBean) {
        try {
            if (webFileDownloadBean != null) {
                DbCacheUtils.getDb().saveOrUpdate(webFileDownloadBean);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
