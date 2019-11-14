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
        return volumeFileDownloadList;
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
                break;
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
                    }
                    downloadVolumeFile.setCancelable(null);
                    Log.d("zhang", "cancelDownloadVolumeFile: STATUS_DOWNLOAD_PAUSE");
                    downloadVolumeFile.setStatus(VolumeFile.STATUS_DOWNLOAD_PAUSE);
                    VolumeFileDownloadCacheUtils.saveVolumeFile(downloadVolumeFile);
                    break;
                }
            }
        }
    }

    /**
     * 继续下载
     */
    public void reDownloadFile(final VolumeFile volumeFile, String currentDirAbsolutePath) {
        downloadFile(volumeFile, currentDirAbsolutePath, true);
    }

    /**
     * 下载文件入口
     */
    public void downloadFile(final VolumeFile volumeFile, String currentDirAbsolutePath) {
        downloadFile(volumeFile, currentDirAbsolutePath, false);
    }


    /**
     * 下载文件
     */
    public void downloadFile(final VolumeFile volumeFile, String currentDirAbsolutePath, boolean isReload) {
        final String fileSavePath = MyAppConfig.getFileDownloadByUserAndTanentDirPath() +
                FileUtils.getNoDuplicateFileNameInDir(MyAppConfig.getFileDownloadByUserAndTanentDirPath(), volumeFile.getName());
        Log.d("zhang", "downloadFile: fileSavePath = " + fileSavePath);
        volumeFile.setLocalFilePath(fileSavePath);
        volumeFile.setStatus(VolumeFile.STATUS_DOWNLOAD_IND);
        volumeFile.setLastRecordTime(System.currentTimeMillis());
        volumeFile.setCompleteSize(volumeFile.getProgress() / 100 * volumeFile.getSize());
        if (!isReload) {
            volumeFileDownloadList.add(volumeFile);
            VolumeFileDownloadCacheUtils.saveVolumeFileList(volumeFileDownloadList);
        } else {
            for (int i = 0; i < volumeFileDownloadList.size(); i++) {
                VolumeFile downloadVolumeFile = volumeFileDownloadList.get(i);
                if (volumeFile.getId().equals(downloadVolumeFile.getId())) {
                    downloadVolumeFile.setStatus(VolumeFile.STATUS_DOWNLOAD_IND);
                    downloadVolumeFile.setLastRecordTime(volumeFile.getLastRecordTime());
                    downloadVolumeFile.setCompleteSize(volumeFile.getCompleteSize());
                    VolumeFileDownloadCacheUtils.saveVolumeFile(downloadVolumeFile);
                    break;
                }
            }
        }

        final String volumeId = volumeFile.getVolume();
        String source = APIUri.getVolumeFileUploadSTSTokenUrl(volumeId);
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
        for (int i = 0; i < volumeFileDownloadList.size(); i++) {
            VolumeFile downloadVolumeFile = volumeFileDownloadList.get(i);
            if (volumeFile.getId().equals(downloadVolumeFile.getId())) {
                if (downloadVolumeFile.getCancelable() == null) {
                    Callback.Cancelable cancelable = x.http().get(params, callBack);
                    downloadVolumeFile.setCancelable(cancelable);
                    VolumeFileDownloadCacheUtils.saveVolumeFile(downloadVolumeFile);
                }
                break;
            }
        }
    }

    private APIDownloadCallBack getAPIDownloadCallBack(String source, final VolumeFile volumeFile,
                                                       final String fileSavePath) {
        APIDownloadCallBack callBack = new APIDownloadCallBack(BaseApplication.getInstance(), source) {
            @Override
            public void callbackStart() {
                for (int i = 0; i < volumeFileDownloadList.size(); i++) {
                    VolumeFile downloadVolumeFile = volumeFileDownloadList.get(i);
                    if (volumeFile.getId().equals(downloadVolumeFile.getId())) {
                        downloadVolumeFile.setLastRecordTime(System.currentTimeMillis());
                    }
                }
            }

            @Override
            public void callbackLoading(long total, long current, boolean isUploading) {
                int progress = (int) (current * 100.0 / total);

                for (int i = 0; i < volumeFileDownloadList.size(); i++) {
                    VolumeFile downloadVolumeFile = volumeFileDownloadList.get(i);
                    if (volumeFile.getId().equals(downloadVolumeFile.getId())) {
                        String speed = FileUtils.formatFileSize((current - downloadVolumeFile.getCompleteSize()) * 1000 /
                                (System.currentTimeMillis() - downloadVolumeFile.getLastRecordTime())) + "/S";
                        if (downloadVolumeFile.getBusinessProgressCallback() != null && downloadVolumeFile.getCompleteSize() != 0) {
                            downloadVolumeFile.getBusinessProgressCallback().onLoading(progress, speed);
                        }
                        downloadVolumeFile.setProgress(progress);
                        downloadVolumeFile.setLastRecordTime(System.currentTimeMillis());
                        downloadVolumeFile.setCompleteSize(current);
                        VolumeFileDownloadCacheUtils.saveVolumeFile(downloadVolumeFile);
                        break;
                    }
                }
            }

            @Override
            public void callbackSuccess(File file) {
                Log.d("zhang", "onSuccess: 下载成功");
                FileDownloadManager.getInstance().saveDownloadFileInfo(DownloadFileCategory.CATEGORY_VOLUME_FILE,
                        volumeFile.getId(), volumeFile.getName(), fileSavePath);
                ToastUtils.show(BaseApplication.getInstance(), R.string.download_success);
                for (int i = 0; i < volumeFileDownloadList.size(); i++) {
                    VolumeFile downloadVolumeFile = volumeFileDownloadList.get(i);
                    if (volumeFile.getId().equals(downloadVolumeFile.getId())) {
                        if (downloadVolumeFile.getBusinessProgressCallback() != null) {
                            downloadVolumeFile.getBusinessProgressCallback().onSuccess(volumeFile);
                        }
                        VolumeFileDownloadCacheUtils.deleteVolumeFile(volumeFile);
                        volumeFileDownloadList.remove(volumeFile);
                        break;
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
                        break;
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
