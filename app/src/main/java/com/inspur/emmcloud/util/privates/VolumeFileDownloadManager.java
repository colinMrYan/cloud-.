package com.inspur.emmcloud.util.privates;

import android.util.Log;

import com.inspur.emmcloud.MyApplication;
import com.inspur.emmcloud.R;
import com.inspur.emmcloud.api.APIInterfaceInstance;
import com.inspur.emmcloud.api.APIUri;
import com.inspur.emmcloud.api.apiservice.MyAppAPIService;
import com.inspur.emmcloud.baselib.util.LogUtils;
import com.inspur.emmcloud.baselib.util.ToastUtils;
import com.inspur.emmcloud.basemodule.api.APIDownloadCallBack;
import com.inspur.emmcloud.basemodule.application.BaseApplication;
import com.inspur.emmcloud.basemodule.bean.DownloadFileCategory;
import com.inspur.emmcloud.basemodule.config.MyAppConfig;
import com.inspur.emmcloud.basemodule.util.FileDownloadManager;
import com.inspur.emmcloud.basemodule.util.FileUtils;
import com.inspur.emmcloud.bean.appcenter.volume.VolumeFile;
import com.inspur.emmcloud.interf.ProgressCallback;

import org.xutils.common.Callback;
import org.xutils.http.HttpMethod;
import org.xutils.http.RequestParams;
import org.xutils.http.app.RedirectHandler;
import org.xutils.http.request.UriRequest;
import org.xutils.x;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class VolumeFileDownloadManager extends APIInterfaceInstance {
    private static volatile VolumeFileDownloadManager instance;
    private List<VolumeFile> volumeFileDownloadList = new ArrayList<>();
    private MyAppAPIService apiService;

    private long lastTime = 0L;

    public static VolumeFileDownloadManager getInstance() {
        if (instance == null) {
            synchronized (VolumeFileDownloadManager.class) {
                if (instance == null) {
                    instance = new VolumeFileDownloadManager();
                }
            }
        }
        return instance;
    }

    private long completeSize = 0L;

    public VolumeFileDownloadManager() {
        apiService = new MyAppAPIService(MyApplication.getInstance());
        apiService.setAPIInterface(this);
        volumeFileDownloadList = VolumeFileDownloadCacheUtils.getVolumeFileListInDownloading();
        boolean isNeedUpdateVolumeFileDownloadStatus = false;
        for (VolumeFile volumeFile : volumeFileDownloadList) {
            if (volumeFile.getStatus().equals(VolumeFile.STATUS_DOWNLOAD_IND)) {
                volumeFile.setStatus(VolumeFile.STATUS_DOWNLOAD_PAUSE);
                isNeedUpdateVolumeFileDownloadStatus = true;
            }
        }
        if (isNeedUpdateVolumeFileDownloadStatus) {
            VolumeFileDownloadCacheUtils.saveVolumeFileList(volumeFileDownloadList);
        }
    }

    /**
     * 获取云盘此文件夹目录下正在下载的云盘文件
     *
     * @return
     */
    public List<VolumeFile> getAllDownloadVolumeFile() {
        List<VolumeFile> volumeFileList = VolumeFileDownloadCacheUtils.getVolumeFileListInDownloading();
        return volumeFileList;
    }

    public void setBusinessProgressCallback(VolumeFile volumeFile, final ProgressCallback businessProgressCallback) {
        for (int i = 0; i < volumeFileDownloadList.size(); i++) {
            VolumeFile downloadVolumeFile = volumeFileDownloadList.get(i);
            if (downloadVolumeFile.getId().equals(volumeFile.getId())) {
                if (businessProgressCallback != null) {
                    if (downloadVolumeFile.getStatus().equals(VolumeFile.STATUS_DOWNLOAD_IND)) {
                        businessProgressCallback.onLoading(downloadVolumeFile.getProgress(), "");
                    } else if (downloadVolumeFile.getStatus().equals(VolumeFile.STATUS_DOWNLOAD_FAIL)) {
                        new android.os.Handler().post(new Runnable() {
                            @Override
                            public void run() {
                                businessProgressCallback.onFail();
                            }
                        });
                    }
                    downloadVolumeFile.setBusinessProgressCallback(businessProgressCallback);
                }
            }
        }
    }

    /**
     * 移除下载服务   可用于暂停下载
     *
     * @param volumeFile
     */
    public void cancelDownloadVolumeFile(VolumeFile volumeFile) {
        if (volumeFile != null) {
            for (int i = 0; i < volumeFileDownloadList.size(); i++) {
                VolumeFile downloadVolumeFile = volumeFileDownloadList.get(i);
                if (downloadVolumeFile.getId().equals(volumeFile.getId())) {
                    if (downloadVolumeFile.getCancelable() != null) {
                        Log.d("zhang", "cancelDownloadVolumeFile: success");
                        downloadVolumeFile.getCancelable().cancel();
                        downloadVolumeFile.setCancelable(null);
                    }
                    downloadVolumeFile.setStatus(VolumeFile.STATUS_DOWNLOAD_PAUSE);
                    VolumeFileDownloadCacheUtils.saveVolumeFile(downloadVolumeFile);
                    break;
                }
            }
        }
    }

    public void reDownloadFile(final VolumeFile volumeFile, String currentDirAbsolutePath) {
        downloadFile(volumeFile, currentDirAbsolutePath, true);
    }

    public void downloadFile(final VolumeFile volumeFile, String currentDirAbsolutePath) {
        downloadFile(volumeFile, currentDirAbsolutePath, false);
    }


    /**
     * 下载文件
     */
    public void downloadFile(final VolumeFile volumeFile, String currentDirAbsolutePath, boolean isReload) {
        final String fileSavePath = MyAppConfig.getFileDownloadByUserAndTanentDirPath() +
                FileUtils.getNoDuplicateFileNameInDir(MyAppConfig.getFileDownloadByUserAndTanentDirPath(), volumeFile.getName());
        volumeFile.setLocalFilePath(fileSavePath);
        volumeFile.setStatus(VolumeFile.STATUS_DOWNLOAD_IND);
        if (!isReload) {
            volumeFileDownloadList.add(volumeFile);
        }
        final String volumeId = volumeFile.getVolume();
        String source = APIUri.getVolumeFileUploadSTSTokenUrl(volumeId);

        lastTime = System.currentTimeMillis();
        completeSize = volumeFile.getProgress() / 100 * volumeFile.getSize();
        APIDownloadCallBack callBack = getAPIDownloadCallBack(source, volumeFile, fileSavePath);

        RequestParams params = BaseApplication.getInstance().getHttpRequestParams(source);
        params.addParameter("volumeId", volumeId);
        params.addQueryStringParameter("path", currentDirAbsolutePath);
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
        Callback.Cancelable cancelable = x.http().get(params, callBack);
        for (int i = 0; i < volumeFileDownloadList.size(); i++) {
            VolumeFile downloadVolumeFile = volumeFileDownloadList.get(i);
            if (volumeFile.getId().equals(downloadVolumeFile.getId())) {
                downloadVolumeFile.setCancelable(cancelable);
                VolumeFileDownloadCacheUtils.saveVolumeFile(downloadVolumeFile);
            }
        }
    }

    private APIDownloadCallBack getAPIDownloadCallBack(String source, final VolumeFile volumeFile,
                                                       final String fileSavePath) {
        APIDownloadCallBack callBack = new APIDownloadCallBack(BaseApplication.getInstance(), source) {
            @Override
            public void callbackStart() {

            }

            @Override
            public void callbackLoading(long total, long current, boolean isUploading) {
                int progress = (int) (current * 100.0 / total);
                String totalSize = FileUtils.formatFileSize(total);
                String currentSize = FileUtils.formatFileSize(current);
                String speed = FileUtils.formatFileSize((current - completeSize) * 1000 / (System.currentTimeMillis() - lastTime)) + "/S";

                for (int i = 0; i < volumeFileDownloadList.size(); i++) {
                    VolumeFile downloadVolumeFile = volumeFileDownloadList.get(i);
                    if (volumeFile.getId().equals(downloadVolumeFile.getId())) {
                        if (downloadVolumeFile.getBusinessProgressCallback() != null) {
                            downloadVolumeFile.getBusinessProgressCallback().onLoading(progress, speed);
                        }
                    }
                }

                lastTime = System.currentTimeMillis();
                completeSize = current;
            }

            @Override
            public void callbackSuccess(File file) {
                Log.d("zhang", "onSuccess: 下载成功");
                FileDownloadManager.getInstance().saveDownloadFileInfo(DownloadFileCategory.CATEGORY_VOLUME_FILE,
                        volumeFile.getId(), volumeFile.getName(), fileSavePath);
                VolumeFileDownloadCacheUtils.deleteVolumeFile(volumeFile);
                volumeFileDownloadList.remove(volumeFile);
                ToastUtils.show(BaseApplication.getInstance(), R.string.download_success);
                for (int i = 0; i < volumeFileDownloadList.size(); i++) {
                    VolumeFile downloadVolumeFile = volumeFileDownloadList.get(i);
                    if (volumeFile.getId().equals(downloadVolumeFile.getId())) {
                        if (downloadVolumeFile.getBusinessProgressCallback() != null) {
                            downloadVolumeFile.getBusinessProgressCallback().onSuccess(volumeFile);
                        }
                        volumeFileDownloadList.remove(volumeFile);
                    }
                }

            }

            @Override
            public void callbackError(Throwable arg0, boolean arg1) {
                ToastUtils.show(BaseApplication.getInstance(), R.string.download_fail);
                for (int i = 0; i < volumeFileDownloadList.size(); i++) {
                    VolumeFile downloadVolumeFile = volumeFileDownloadList.get(i);
                    if (volumeFile.getId().equals(downloadVolumeFile.getId())) {
                        if (downloadVolumeFile.getBusinessProgressCallback() != null) {
                            downloadVolumeFile.getBusinessProgressCallback().onFail();
                        }
                    }
                }
            }

            @Override
            public void callbackCanceled(CancelledException e) {

            }
        };

        return callBack;
    }
}
