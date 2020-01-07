package com.inspur.emmcloud.util.privates;

import com.inspur.emmcloud.baselib.util.LogUtils;
import com.inspur.emmcloud.basemodule.api.APIDownloadCallBack;
import com.inspur.emmcloud.basemodule.application.BaseApplication;
import com.inspur.emmcloud.basemodule.bean.DownloadFileCategory;
import com.inspur.emmcloud.basemodule.util.AppUtils;
import com.inspur.emmcloud.basemodule.util.FileDownloadManager;
import com.inspur.emmcloud.basemodule.util.FileUtils;
import com.inspur.emmcloud.basemodule.util.NetUtils;
import com.inspur.emmcloud.bean.DownloadInfo;
import com.inspur.emmcloud.interf.ChatProgressCallback;

import org.xutils.common.Callback;
import org.xutils.http.HttpMethod;
import org.xutils.http.RequestParams;
import org.xutils.http.app.RedirectHandler;
import org.xutils.http.request.UriRequest;
import org.xutils.x;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class ChatFileDownloadManager {
    private volatile static ChatFileDownloadManager instance;
    private List<DownloadInfo> downloadInfoList = new ArrayList<>();

    public ChatFileDownloadManager() {
        refreshCache();
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

    private void refreshCache() {
        downloadInfoList = DownloadCacheUtils.getAllDownloadingList();
        boolean isNeedUpdateFileDownloadStatus = false;
        for (DownloadInfo downloadInfo : downloadInfoList) {
            if (downloadInfo.getStatus().equals(DownloadInfo.STATUS_LOADING)) {
                downloadInfo.setStatus(DownloadInfo.STATUS_PAUSE);
                isNeedUpdateFileDownloadStatus = true;
            }

            if (downloadInfo.getStatus().equals(DownloadInfo.STATUS_SUCCESS) ||
                    downloadInfo.getStatus().equals(DownloadInfo.STATUS_FAIL)) {
                isNeedUpdateFileDownloadStatus = true;
            }
        }

        if (isNeedUpdateFileDownloadStatus) {
            DownloadCacheUtils.saveDownloadFileList(downloadInfoList);
        }
    }

    /**
     * 获取聊天下载文件列表
     */
    public List<DownloadInfo> getAllChatFileDownloadList() {
        refreshCache();
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
        refreshCache();
        List<DownloadInfo> resultList = new ArrayList<>();
        for (DownloadInfo info : downloadInfoList) {
            if (info.getType().equals(DownloadInfo.TYPE_VOLUME)) {
                resultList.add(info);
            }
        }

        return resultList;
    }

    public String getFileStatus(DownloadInfo info) {
        DownloadInfo downloadInfo = getManagerDownloadInfo(info);
        if (downloadInfo != null) {
            return downloadInfo.getStatus();
        }
        return "";
    }

    public void setBusinessProgressCallback(DownloadInfo info, ChatProgressCallback businessProgressCallback) {
        for (int i = 0; i < downloadInfoList.size(); i++) {
            DownloadInfo downloadInfo = downloadInfoList.get(i);
            if (info.getFileId().equals(downloadInfo.getFileId())) {
                if (businessProgressCallback != null) {
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

        final String fileSavePath = FileDownloadManager.getInstance().getFilePath(
                DownloadFileCategory.CATEGORY_MESSAGE, info.getFileId(), info.getFileName());
        String source = info.getUrl();
        info.setLocalPath(fileSavePath);
        info.setType(DownloadInfo.TYPE_MESSAGE);
        info.setStatus(DownloadInfo.STATUS_LOADING);
        info.setLastUpdateTime(System.currentTimeMillis());
        info.setCompleted(info.getProgress() / 100 * info.getSize());

        if (!isReload) {
            downloadInfoList.add(0, info);
            DownloadCacheUtils.saveDownloadFileList(downloadInfoList);
        } else {
            DownloadInfo downloadInfo = getManagerDownloadInfo(info);
            //防止外部删除文件  还存留缓存数据库
            if (downloadInfo.getCancelable() != null) {
                downloadInfo.setCancelable(null);
            }
            downloadInfo.setType(DownloadInfo.TYPE_MESSAGE);
            downloadInfo.setStatus(DownloadInfo.STATUS_LOADING);
            downloadInfo.setLastUpdateTime(downloadInfo.getLastUpdateTime());
            downloadInfo.setCompleted(downloadInfo.getCompleted());
            DownloadCacheUtils.saveDownloadFile(downloadInfo);
        }

        APIDownloadCallBack callBack = getAPIDownloadCallBack(source, info, fileSavePath);
        RequestParams params = BaseApplication.getInstance().getHttpRequestParams(source);
        params.setAutoResume(true);// 断点下载
        params.setSaveFilePath(fileSavePath);
        params.setCancelFast(true);
        LogUtils.jasonDebug("params==" + params.toString());
        params.setRedirectHandler(new RedirectHandler() {
            @Override
            public RequestParams getRedirectParams(UriRequest uriRequest) throws Throwable {
                String locationUrl = uriRequest.getResponseHeader("Location");
                RequestParams params = new RequestParams(locationUrl);
                params.setAutoResume(true);// 断点下载
                params.setSaveFilePath(fileSavePath);
                params.setCancelFast(true);
                params.setMethod(HttpMethod.GET);
                return params;
            }
        });

        DownloadInfo downloadInfo = getManagerDownloadInfo(info);
        if (downloadInfo != null && downloadInfo.getCancelable() == null) {
            Callback.Cancelable cancelable = x.http().get(params, callBack);
            downloadInfo.setCancelable(cancelable);
            DownloadCacheUtils.saveDownloadFile(downloadInfo);
        }
    }

    /**
     * 通过文件Id获取对应得下载管理信息
     */
    public DownloadInfo getManagerDownloadInfo(DownloadInfo info) {
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
    public void cancelDownloadFile(DownloadInfo info) {
        DownloadInfo downloadInfo = getManagerDownloadInfo(info);
        if (downloadInfo != null) {
            if (downloadInfo.getCancelable() != null) {
                downloadInfo.getCancelable().cancel();
            }
            downloadInfo.setCancelable(null);
            downloadInfo.setStatus(DownloadInfo.STATUS_PAUSE);
            DownloadCacheUtils.saveDownloadFile(downloadInfo);
        }
    }

    private APIDownloadCallBack getAPIDownloadCallBack(String source, final DownloadInfo info,
                                                       final String fileSavePath) {
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
                DownloadInfo downloadInfo = getManagerDownloadInfo(info);
                downloadInfo.setStatus(DownloadInfo.STATUS_SUCCESS);
                DownloadCacheUtils.saveDownloadFile(downloadInfo);
                FileDownloadManager.getInstance().saveDownloadFileInfo(DownloadFileCategory.CATEGORY_MESSAGE,
                        downloadInfo.getFileId(), downloadInfo.getFileName(), downloadInfo.getLocalPath());
//                ToastUtils.show(BaseApplication.getInstance(), R.string.download_success);
                if (downloadInfo.getBusinessProgressCallback() != null) {
                    downloadInfo.getBusinessProgressCallback().onSuccess(file);
                }
            }

            @Override
            public void callbackError(Throwable arg0, boolean arg1) {
//                ToastUtils.show(BaseApplication.getInstance(), R.string.download_fail);
                DownloadInfo downloadInfo = getManagerDownloadInfo(info);
                downloadInfo.setStatus(DownloadInfo.STATUS_FAIL);
                DownloadCacheUtils.saveDownloadFile(downloadInfo);
                if (downloadInfo.getBusinessProgressCallback() != null) {
                    downloadInfo.getBusinessProgressCallback().onFail();
                }
            }

            @Override
            public void callbackCanceled(CancelledException e) {

            }
        };

        return callBack;
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
