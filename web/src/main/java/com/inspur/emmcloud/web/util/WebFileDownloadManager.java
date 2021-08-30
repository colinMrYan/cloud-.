package com.inspur.emmcloud.web.util;

import com.inspur.emmcloud.baselib.util.LogUtils;
import com.inspur.emmcloud.baselib.util.PreferencesUtils;
import com.inspur.emmcloud.basemodule.api.APIDownloadCallBack;
import com.inspur.emmcloud.basemodule.application.BaseApplication;
import com.inspur.emmcloud.basemodule.bean.DownloadFileCategory;
import com.inspur.emmcloud.basemodule.util.FileDownloadManager;
import com.inspur.emmcloud.basemodule.util.FileUtils;
import com.inspur.emmcloud.web.api.WebProgressCallback;
import com.inspur.emmcloud.web.bean.WebFileDownloadBean;

import org.xutils.common.Callback;
import org.xutils.http.HttpMethod;
import org.xutils.http.RequestParams;
import org.xutils.http.app.RedirectHandler;
import org.xutils.http.request.UriRequest;
import org.xutils.x;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Date：2021/8/25
 * Author：wang zhen
 * Description web文件下载管理器
 */
public class WebFileDownloadManager {
    private static volatile WebFileDownloadManager instance;
    private List<WebFileDownloadBean> webFileDownloadList = new ArrayList<>();

    public static WebFileDownloadManager getInstance() {
        if (instance == null) {
            synchronized (WebFileDownloadManager.class) {
                if (instance == null) {
                    instance = new WebFileDownloadManager();
                }
            }
        }
        return instance;
    }

    public WebFileDownloadManager() {
        webFileDownloadList = WebFileDownloadCacheUtils.getWebFileListInDownloading();
        boolean isNeedUpdateVolumeFileDownloadStatus = false;
        for (WebFileDownloadBean webFileDownloadBean : webFileDownloadList) {
            if (webFileDownloadBean.getStatus().equals(WebFileDownloadBean.STATUS_LOADING)) {
                webFileDownloadBean.setStatus(WebFileDownloadBean.STATUS_PAUSE);
                isNeedUpdateVolumeFileDownloadStatus = true;
            }
            if (webFileDownloadBean.getStatus().equals(WebFileDownloadBean.STATUS_SUCCESS) ||
                    webFileDownloadBean.getStatus().equals(WebFileDownloadBean.STATUS_FAIL)) {
                isNeedUpdateVolumeFileDownloadStatus = true;
            }
        }
        if (isNeedUpdateVolumeFileDownloadStatus) {
            WebFileDownloadCacheUtils.saveWebFileList(webFileDownloadList);
        }
    }

    /**
     * 获取文件下载状态
     *
     * @return 下载状态status
     */
    public String getFileStatus(String fileId) {
        for (int i = 0; i < webFileDownloadList.size(); i++) {
            WebFileDownloadBean webFileDownloadBean = webFileDownloadList.get(i);
            if (fileId.equals(webFileDownloadBean.getFileId())) {
                return webFileDownloadBean.getStatus();
            }
        }

        return WebFileDownloadBean.STATUS_NORMAL;
    }

    public List<WebFileDownloadBean> getAllDownloadWebFile() {
        return webFileDownloadList;
    }

    /**
     * 重新下载
     */
    public void reDownloadFile(WebFileDownloadBean webFileDownloadBean) {
        downloadFile(webFileDownloadBean, true);
    }

    /**
     * 下载文件入口
     */
    public void downloadFile(WebFileDownloadBean webFileDownloadBean) {
        downloadFile(webFileDownloadBean, false);
    }

    public void downloadFile(WebFileDownloadBean webFileDownloadBean, boolean isReload) {
        //文件已存在
        if (FileDownloadManager.getInstance().isDownloadFileExists(DownloadFileCategory.CATEGORY_WEB,
                webFileDownloadBean.getFileId(), webFileDownloadBean.getFileName())) {
            return;
        }
        final String fileSavePath = FileDownloadManager.getInstance().getFilePath(
                DownloadFileCategory.CATEGORY_WEB, webFileDownloadBean.getFileId(), webFileDownloadBean.getFileName());
        webFileDownloadBean.setLocalPath(fileSavePath);
        webFileDownloadBean.setType(WebFileDownloadBean.TYPE_WEB);
        webFileDownloadBean.setStatus(WebFileDownloadBean.STATUS_LOADING);
        webFileDownloadBean.setLastUpdateTime(System.currentTimeMillis());
        webFileDownloadBean.setCompleted(webFileDownloadBean.getProgress() / 100 * webFileDownloadBean.getFileSize());
        if (!isReload) {
            webFileDownloadList.add(0, webFileDownloadBean);
            WebFileDownloadCacheUtils.saveWebFileList(webFileDownloadList);
        } else {
            for (int i = 0; i < webFileDownloadList.size(); i++) {
                WebFileDownloadBean bean = webFileDownloadList.get(i);
                if (webFileDownloadBean.getFileId().equals(bean.getFileId())) {
                    //防止外部删除文件  还存留缓存数据库
                    if (bean.getCancelable() != null) {
                        bean.setCancelable(null);
                    }
                    bean.setType(WebFileDownloadBean.TYPE_WEB);
                    bean.setStatus(WebFileDownloadBean.STATUS_LOADING);
                    bean.setLastUpdateTime(webFileDownloadBean.getLastUpdateTime());
                    bean.setCompleted(webFileDownloadBean.getCompleted());
                    WebFileDownloadCacheUtils.saveWebFile(bean);
                    break;
                }
            }
        }

        String source = webFileDownloadBean.getDownloadUrl();
        APIDownloadCallBack callBack = getAPIDownloadCallBack(source, webFileDownloadBean, fileSavePath);
        RequestParams params = new RequestParams(source);
        params.setUseCookie(true);
        params.setAutoResume(true);// 断点下载
        params.setSaveFilePath(fileSavePath);
        params.setCancelFast(true);
        String cookie = PreferencesUtils.getString(BaseApplication.getInstance(), "web_cookie", "");
        params.addHeader("Cookie", cookie);
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
        WebFileDownloadBean downloadInfo = getManagerDownloadInfo(webFileDownloadBean);
        if (downloadInfo != null && downloadInfo.getCancelable() == null) {
            Callback.Cancelable cancelable = x.http().get(params, callBack);
            downloadInfo.setCancelable(cancelable);
            WebFileDownloadCacheUtils.saveWebFile(downloadInfo);
        }
    }

    public void resetWebFileStatus(WebFileDownloadBean bean) {
        if (bean.getCancelable() != null) {
            bean.setCancelable(null);
        }
        if (bean.getBusinessProgressCallback() != null) {
            bean.setBusinessProgressCallback(null);
        }

        for (int i = 0; i < webFileDownloadList.size(); i++) {
            WebFileDownloadBean downloadBean = webFileDownloadList.get(i);
            if (bean.getFileId().equals(downloadBean.getFileId())) {
                webFileDownloadList.remove(downloadBean);
            }
        }
    }

    // 网络请求回调
    private APIDownloadCallBack getAPIDownloadCallBack(String source, final WebFileDownloadBean info,
                                                       final String fileSavePath) {
        APIDownloadCallBack callBack = new APIDownloadCallBack(BaseApplication.getInstance(), source) {
            @Override
            public void callbackStart() {
//                progressBar.setProgress(0);
//                progressText.setText("");
            }

            @Override
            public void callbackLoading(long total, long current, boolean isUploading) {
                WebFileDownloadBean downloadInfo = getManagerDownloadInfo(info);
                int progress = (int) (current * 100.0 / info.getFileSize());

                String speed = FileUtils.formatFileSize((current - downloadInfo.getCompleted()) * 1000 /
                        (System.currentTimeMillis() - downloadInfo.getLastUpdateTime())) + "/S";

                if (downloadInfo.getBusinessProgressCallback() != null && current > 0) {
                    downloadInfo.getBusinessProgressCallback().onLoading(progress, current, speed);
                }
                downloadInfo.setCompleted(current);
                downloadInfo.setProgress(progress);
            }

            @Override
            public void callbackSuccess(File file) {
                WebFileDownloadBean downloadInfo = getManagerDownloadInfo(info);
                downloadInfo.setStatus(WebFileDownloadBean.STATUS_SUCCESS);
                WebFileDownloadCacheUtils.saveWebFile(downloadInfo);
                FileDownloadManager.getInstance().saveDownloadFileInfo(DownloadFileCategory.CATEGORY_WEB,
                        downloadInfo.getFileId(), downloadInfo.getFileName(), downloadInfo.getLocalPath());
                if (downloadInfo.getBusinessProgressCallback() != null) {
                    downloadInfo.getBusinessProgressCallback().onSuccess(file);
                }
            }

            @Override
            public void callbackError(Throwable arg0, boolean arg1) {
                WebFileDownloadBean downloadInfo = getManagerDownloadInfo(info);
                downloadInfo.setStatus(WebFileDownloadBean.STATUS_FAIL);
                WebFileDownloadCacheUtils.saveWebFile(downloadInfo);
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
     * 通过文件Id获取对应得下载管理信息
     */
    public WebFileDownloadBean getManagerDownloadInfo(WebFileDownloadBean info) {
        for (int i = 0; i < webFileDownloadList.size(); i++) {
            WebFileDownloadBean downloadInfo = webFileDownloadList.get(i);
            if (info.getFileId().equals(downloadInfo.getFileId()) && info.getType().equals(downloadInfo.getType())) {
                return downloadInfo;
            }
        }
        return null;
    }

    public void setBusinessProgressCallback(WebFileDownloadBean info, WebProgressCallback callback) {
        for (int i = 0; i < webFileDownloadList.size(); i++) {
            WebFileDownloadBean downloadInfo = webFileDownloadList.get(i);
            if (info.getFileId().equals(downloadInfo.getFileId())) {
                if (callback != null) {
                    downloadInfo.setBusinessProgressCallback(callback);
                }
            }
        }
    }

    /**
     * 暂停下载
     */
    public void cancelDownloadFile(WebFileDownloadBean info) {
        WebFileDownloadBean downloadInfo = getManagerDownloadInfo(info);
        if (downloadInfo != null) {
            if (downloadInfo.getCancelable() != null) {
                downloadInfo.getCancelable().cancel();
            }
            downloadInfo.setCancelable(null);
            downloadInfo.setStatus(WebFileDownloadBean.STATUS_PAUSE);
            WebFileDownloadCacheUtils.saveWebFile(downloadInfo);
        }
    }
}
