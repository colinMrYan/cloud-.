package com.inspur.emmcloud.util.privates;

import com.inspur.emmcloud.R;
import com.inspur.emmcloud.baselib.util.ToastUtils;
import com.inspur.emmcloud.basemodule.api.APIDownloadCallBack;
import com.inspur.emmcloud.basemodule.application.BaseApplication;
import com.inspur.emmcloud.basemodule.bean.DownloadFileCategory;
import com.inspur.emmcloud.basemodule.util.AppUtils;
import com.inspur.emmcloud.basemodule.util.DownLoaderUtils;
import com.inspur.emmcloud.basemodule.util.FileDownloadManager;
import com.inspur.emmcloud.basemodule.util.FileUtils;
import com.inspur.emmcloud.basemodule.util.NetUtils;
import com.inspur.emmcloud.bean.DownloadInfo;
import com.inspur.emmcloud.interf.ProgressCallback;

import org.xutils.common.Callback;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class ChatFileDownloadManager {
    private volatile static ChatFileDownloadManager instance;
    private List<DownloadInfo> downloadInfoList = new ArrayList<>();

    public ChatFileDownloadManager() {

    }

    public static ChatFileDownloadManager getInstance() {
        if (instance == null) {
            synchronized (ChatFileDownloadManager.class) {
                if (instance == null) {
                    instance = new ChatFileDownloadManager();
                }
            }
        }
        return instance;
    }

    /**
     * 获取聊天下载文件列表
     */
    public List<DownloadInfo> getAllChatFileDownloadList() {
        List<DownloadInfo> resultList = new ArrayList<>();
        for (DownloadInfo info : downloadInfoList) {
            if (info.getType().equals(DownloadInfo.TYPE_MESSAGE)) {
                resultList.add(info);
            }
        }
        return resultList;
    }

    /**
     * 获取云盘下载中文件列表
     */
    public List<DownloadInfo> getAllVolumeFileDownloadList() {
        List<DownloadInfo> resultList = new ArrayList<>();
        for (DownloadInfo info : downloadInfoList) {
            if (info.getType().equals(DownloadInfo.TYPE_VOLUME)) {
                resultList.add(info);
            }
        }

        return resultList;
    }

    public void setBusinessProgressCallback(DownloadInfo info, ProgressCallback businessProgressCallback) {
        for (int i = 0; i < downloadInfoList.size(); i++) {
            DownloadInfo downloadInfo = downloadInfoList.get(i);
            if (info.getFileId().equals(downloadInfo.getFileId())) {
                if (downloadInfo.getBusinessProgressCallback() == null) {
                    downloadInfo.setBusinessProgressCallback(businessProgressCallback);
                }
            }
        }

    }

    /**
     * 重新下载
     */
    public void reDownloadFile(final DownloadInfo downloadInfo) {
        downloadFile(downloadInfo, true);
    }

    /**
     * 下载文件入口
     */
    public void downloadFile(DownloadInfo downloadInfo) {
        downloadFile(downloadInfo, false);
    }

    /**
     * 下载文件
     */
    private void downloadFile(final DownloadInfo info, boolean isReload) {
        if (!NetUtils.isNetworkConnected(BaseApplication.getInstance()) || !AppUtils.isHasSDCard(BaseApplication.getInstance())) {
            return;
        }
        //文件已存在
        if (FileDownloadManager.getInstance().isDownloadFileExists(DownloadFileCategory.CATEGORY_MESSAGE,
                info.getFileId(), info.getFileName())) {
            return;
        }

        final String fileSavePath = info.getLocalPath();
        String source = info.getUrl();
        info.setStatus(DownloadInfo.STATUS_DOWNLOADING);

        APIDownloadCallBack callBack = new APIDownloadCallBack(BaseApplication.getInstance(), source) {
            @Override
            public void callbackStart() {
//                progressBar.setProgress(0);
//                progressText.setText("");
            }

            @Override
            public void callbackLoading(long total, long current, boolean isUploading) {
                int progress = (int) (current * 100.0 / total);
                String totleSize = FileUtils.formatFileSize(total);
                String currentSize = FileUtils.formatFileSize(current);
                DownloadInfo downloadInfo = getManagerDownloadInfo(info);

                if (downloadInfo != null) {
                    String speed = FileUtils.formatFileSize((current - downloadInfo.getCompleted()) * 1000 /
                            (System.currentTimeMillis() - downloadInfo.getLastUpdateTime())) + "/S";

                    if (downloadInfo.getBusinessProgressCallback() != null && current > 0) {
                        downloadInfo.getBusinessProgressCallback().onLoading(progress, current, speed);
                    }
                    downloadInfo.setCompleted(current);
                    downloadInfo.setProgress(progress);
                }
            }

            @Override
            public void callbackSuccess(File file) {
                DownloadCacheUtils.deleteDownloadFile(info);
                FileDownloadManager.getInstance().saveDownloadFileInfo(DownloadFileCategory.CATEGORY_MESSAGE,
                        info.getFileId(), info.getFileName(), info.getLocalPath());
                ToastUtils.show(BaseApplication.getInstance(), R.string.download_success);
            }

            @Override
            public void callbackError(Throwable arg0, boolean arg1) {
                ToastUtils.show(BaseApplication.getInstance(), R.string.download_fail);
            }

            @Override
            public void callbackCanceled(CancelledException e) {

            }
        };
        if (info.getCancelable() == null) {
            Callback.Cancelable cancelable = new DownLoaderUtils().startDownLoad(source, fileSavePath, callBack);
            info.setCancelable(cancelable);
        }

        if (!isReload) {
            downloadInfoList.add(0, info);
            DownloadCacheUtils.saveDownloadFile(info);
        }
    }

    /**
     * 通过文件Id获取对应得下载管理信息
     */
    private DownloadInfo getManagerDownloadInfo(DownloadInfo info) {
        for (int i = 0; i < downloadInfoList.size(); i++) {
            DownloadInfo downloadInfo = downloadInfoList.get(i);
            if (info.getFileId().equals(downloadInfo.getFileId()) && info.getType().equals(downloadInfo.getType())) {
                return downloadInfo;
            }
        }

        return null;
    }

    /**
     * 暂停下载
     */
    public void cancelDownloadFile(DownloadInfo downloadInfo) {
        if (downloadInfo != null) {
            for (int i = 0; i < downloadInfoList.size(); i++) {
                DownloadInfo itemInfo = downloadInfoList.get(i);
                if (itemInfo.getFileId().equals(downloadInfo.getFileId())) {
                    if (itemInfo.getCancelable() != null) {
                        itemInfo.getCancelable().cancel();
                    }
                    itemInfo.setCancelable(null);
                    itemInfo.setStatus(DownloadInfo.STATUS_DOWNLOAD_PAUSE);
                    DownloadCacheUtils.saveDownloadFile(itemInfo);
                }
            }
        }
    }

    /**
     * 重置下载状态
     */
    public void resetDownloadStatus(DownloadInfo info) {
        if (info.getCancelable() != null) {
            info.setCancelable(null);
        }
        if (info.getBusinessProgressCallback() != null) {
            info.setBusinessProgressCallback(null);
        }

        for (int i = 0; i < downloadInfoList.size(); i++) {
            DownloadInfo downloadInfo = downloadInfoList.get(i);
            if (info.getFileId().equals(downloadInfo.getFileId())) {
                downloadInfoList.remove(downloadInfo);
            }
        }
    }
}
