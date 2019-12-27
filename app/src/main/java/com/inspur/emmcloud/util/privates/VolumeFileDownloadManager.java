package com.inspur.emmcloud.util.privates;

import android.util.Log;

import com.inspur.emmcloud.api.APIUri;
import com.inspur.emmcloud.baselib.util.LogUtils;
import com.inspur.emmcloud.basemodule.api.APIDownloadCallBack;
import com.inspur.emmcloud.basemodule.application.BaseApplication;
import com.inspur.emmcloud.basemodule.bean.DownloadFileCategory;
import com.inspur.emmcloud.basemodule.bean.FileDownloadInfo;
import com.inspur.emmcloud.basemodule.bean.SimpleEventMessage;
import com.inspur.emmcloud.basemodule.config.Constant;
import com.inspur.emmcloud.basemodule.util.FileDownloadManager;
import com.inspur.emmcloud.basemodule.util.FileUtils;
import com.inspur.emmcloud.bean.appcenter.volume.VolumeFile;
import com.inspur.emmcloud.interf.ProgressCallback;
import com.inspur.emmcloud.ui.appcenter.volume.observe.LoadObservable;

import org.greenrobot.eventbus.EventBus;
import org.xutils.common.Callback;
import org.xutils.http.HttpMethod;
import org.xutils.http.RequestParams;
import org.xutils.http.app.RedirectHandler;
import org.xutils.http.request.UriRequest;
import org.xutils.x;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class VolumeFileDownloadManager {
    private static volatile VolumeFileDownloadManager instance;
    private List<VolumeFile> volumeFileDownloadList = new ArrayList<>();
    private List<VolumeFile> volumeFileDownloadingList = new ArrayList<>();
    private List<VolumeFile> volumeFileDownloadedList = new ArrayList<>();

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
        volumeFileDownloadList = VolumeFileDownloadCacheUtils.getVolumeFileListInDownloading();
        boolean isNeedUpdateVolumeFileDownloadStatus = false;
        for (VolumeFile volumeFile : volumeFileDownloadList) {
            if (volumeFile.getStatus().equals(VolumeFile.STATUS_LOADING)) {
                volumeFile.setStatus(VolumeFile.STATUS_PAUSE);
                isNeedUpdateVolumeFileDownloadStatus = true;
            }

            if (volumeFile.getStatus().equals(VolumeFile.STATUS_SUCCESS) ||
                    volumeFile.getStatus().equals(VolumeFile.STATUS_FAIL)) {
                isNeedUpdateVolumeFileDownloadStatus = true;
            }
        }
        if (isNeedUpdateVolumeFileDownloadStatus) {
            VolumeFileDownloadCacheUtils.saveVolumeFileList(volumeFileDownloadList);
        }
    }

    private void refreshCache() {
//        volumeFileDownloadList.clear();
//        volumeFileDownloadList = VolumeFileDownloadCacheUtils.getVolumeFileListInDownloading();
    }

    /**
     * 获取云盘此文件夹目录下正在下载的云盘文件
     *
     * @return
     */
    public List<VolumeFile> getAllDownloadVolumeFile() {
//        Iterator<VolumeFile> iterator = volumeFileDownloadList.iterator();
//        while (iterator.hasNext()) {
//            VolumeFile item = iterator.next();
//            if (FileDownloadManager.getInstance().isDownloadFileExists(DownloadFileCategory.CATEGORY_VOLUME_FILE, item.getId(), item.getName())) {
//                //防止重复下载
//                iterator.remove();
//                resetVolumeFileStatus(item);
//            }
//        }
        refreshCache();
        return volumeFileDownloadList;
    }

    /**
     * 获取下载中的文件列表 (包含下载中 or 下载暂停 or 下载失败)
     *
     * @return
     */
    public synchronized List<VolumeFile> getUnFinishDownloadList() {
        refreshCache();
        volumeFileDownloadingList.clear();
        for (VolumeFile volumeFile : volumeFileDownloadList) {
            if (volumeFile.getStatus().equals(VolumeFile.STATUS_LOADING) || volumeFile.getStatus().equals(VolumeFile.STATUS_PAUSE)
                    || volumeFile.getStatus().equals(VolumeFile.STATUS_FAIL)) {
                volumeFileDownloadingList.add(volumeFile);
            }
        }
        return volumeFileDownloadingList;
    }

    /**
     * 获取已下载完成的文件列表  (下载成功 or 下载失败)
     */
    public synchronized List<VolumeFile> getFinishDownloadList() {
        refreshCache();
        volumeFileDownloadedList.clear();
//        for (VolumeFile volumeFile : volumeFileDownloadList) {
//            if (volumeFile.getStatus().equals(VolumeFile.STATUS_NORMAL)) {
//                volumeFileDownloadedList.add(volumeFile);
//            }
//        }

        List<FileDownloadInfo> fileList = FileDownloadManager.getInstance().getFileDownloadInfoFileList(DownloadFileCategory.CATEGORY_VOLUME_FILE);
        for (FileDownloadInfo downloadInfo : fileList) {
            VolumeFile volumeFile = VolumeFile.getMockDownloadVolumeFile(downloadInfo, "1234");
            volumeFile.setStatus(VolumeFile.STATUS_SUCCESS);
            volumeFileDownloadedList.add(volumeFile);
        }

        return volumeFileDownloadedList;
    }

    public void deleteDownloadInfo(VolumeFile volumeFile) {
        VolumeFile item = getManagerVolumeFileInfo(volumeFile);
        if (item != null) {
            volumeFileDownloadList.remove(item);
        }
    }

    public void setBusinessProgressCallback(final VolumeFile volumeFile, final ProgressCallback businessProgressCallback) {
        for (int i = 0; i < volumeFileDownloadList.size(); i++) {
            final VolumeFile downloadVolumeFile = volumeFileDownloadList.get(i);
            if (downloadVolumeFile.getId().equals(volumeFile.getId())) {
                if (businessProgressCallback != null) {
                    if (downloadVolumeFile.getStatus().equals(VolumeFile.STATUS_LOADING)) {
                        businessProgressCallback.onLoading(downloadVolumeFile.getProgress(), downloadVolumeFile.getCompleteSize(), "");
                    } else if (downloadVolumeFile.getStatus().equals(VolumeFile.STATUS_FAIL)) {
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
                    downloadVolumeFile.setStatus(VolumeFile.STATUS_PAUSE);
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
        final String fileSavePath = FileDownloadManager.getInstance().getFilePath(
                DownloadFileCategory.CATEGORY_VOLUME_FILE, volumeFile.getId(), volumeFile.getName());
        if (FileDownloadManager.getInstance().isDownloadFileExists(
                DownloadFileCategory.CATEGORY_VOLUME_FILE, volumeFile.getId(), volumeFile.getName())) {
            //防止重复下载
            return;
        }
        Log.d("zhang", "downloadFile: fileSavePath = " + fileSavePath);
        volumeFile.setLocalFilePath(fileSavePath);
        volumeFile.setLoadType(VolumeFile.TYPE_DOWNLOAD);
        volumeFile.setStatus(VolumeFile.STATUS_LOADING);
        volumeFile.setLastRecordTime(System.currentTimeMillis());
        volumeFile.setCompleteSize(volumeFile.getProgress() / 100 * volumeFile.getSize());
        if (!isReload) {
            volumeFile.setProgress(0);
            volumeFile.setCompleteSize(0);
            volumeFileDownloadList.add(0, volumeFile);
            VolumeFileDownloadCacheUtils.saveVolumeFileList(volumeFileDownloadList);
        } else {
            for (int i = 0; i < volumeFileDownloadList.size(); i++) {
                VolumeFile downloadVolumeFile = volumeFileDownloadList.get(i);
                if (volumeFile.getId().equals(downloadVolumeFile.getId())) {
                    //防止外部删除文件  还存留缓存数据库
                    if (downloadVolumeFile.getCancelable() != null) {
                        downloadVolumeFile.setCancelable(null);
                    }
                    downloadVolumeFile.setLoadType(VolumeFile.TYPE_DOWNLOAD);
                    downloadVolumeFile.setStatus(VolumeFile.STATUS_LOADING);
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
                    Log.d("zhang", "downloadFile: downloadVolumeFile = " + downloadVolumeFile.getName());
                    Callback.Cancelable cancelable = x.http().get(params, callBack);
                    downloadVolumeFile.setCancelable(cancelable);
                    VolumeFileDownloadCacheUtils.saveVolumeFile(downloadVolumeFile);
                }
                break;
            }
        }
    }

    /**
     * 获取文件下载状态
     *
     * @param volumeFile
     * @return
     */
    public String getFileStatus(VolumeFile volumeFile) {
        for (int i = 0; i < volumeFileDownloadList.size(); i++) {
            VolumeFile downloadVolumeFile = volumeFileDownloadList.get(i);
            if (volumeFile.getId().equals(downloadVolumeFile.getId())) {
                return downloadVolumeFile.getStatus();
            }
        }

        return VolumeFile.STATUS_NORMAL;
    }

    /**
     * 通过文件Id获取对应得下载管理信息
     */
    private VolumeFile getManagerVolumeFileInfo(VolumeFile volumeFile) {
        for (int i = 0; i < volumeFileDownloadList.size(); i++) {
            VolumeFile info = volumeFileDownloadList.get(i);
            if (info.getId().equals(volumeFile.getId()) && info.getType().equals(volumeFile.getType())) {
                return info;
            }
        }

        return null;
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
                        if (downloadVolumeFile.getBusinessProgressCallback() != null && downloadVolumeFile.getCompleteSize() >= 0) {
                            downloadVolumeFile.getBusinessProgressCallback().onLoading(progress, current, speed);
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
//                ToastUtils.show(BaseApplication.getInstance(), R.string.download_success);
                for (int i = 0; i < volumeFileDownloadList.size(); i++) {
                    final VolumeFile downloadVolumeFile = volumeFileDownloadList.get(i);
                    if (volumeFile.getId().equals(downloadVolumeFile.getId())) {
                        downloadVolumeFile.setStatus(VolumeFile.STATUS_SUCCESS);
                        if (downloadVolumeFile.getBusinessProgressCallback() != null) {
                            downloadVolumeFile.getBusinessProgressCallback().onSuccess(volumeFile);
                        }
                        new android.os.Handler().post(new Runnable() {
                            @Override
                            public void run() {
                                SimpleEventMessage simpleEventMessage = new SimpleEventMessage(Constant.EVENTBUS_TAG_VOLUME_FILE_DOWNLOAD_SUCCESS, volumeFile);
                                EventBus.getDefault().post(simpleEventMessage);
                            }
                        });
                        volumeFileDownloadList.remove(downloadVolumeFile);
                        VolumeFileDownloadCacheUtils.saveVolumeFile(downloadVolumeFile);
                        break;
                    }
                }
                LoadObservable.getInstance().notifyDateChange();
//                resetVolumeFileStatus(volumeFile);
            }

            @Override
            public void callbackError(Throwable arg0, boolean arg1) {
//                ToastUtils.show(BaseApplication.getInstance(), R.string.download_fail);
                Log.d("zhang", "callbackError: 下载失败");
                for (int i = 0; i < volumeFileDownloadList.size(); i++) {
                    VolumeFile downloadVolumeFile = volumeFileDownloadList.get(i);
                    if (volumeFile.getId().equals(downloadVolumeFile.getId())) {
                        if (downloadVolumeFile.getBusinessProgressCallback() != null) {
                            if (downloadVolumeFile.getCancelable() != null) {
                                downloadVolumeFile.setCancelable(null);
                            }
                            downloadVolumeFile.setStatus(VolumeFile.STATUS_FAIL);
                            VolumeFileDownloadCacheUtils.saveVolumeFile(downloadVolumeFile);
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

    /**
     * 重置状态
     *
     * @param volumeFile
     */
    public void resetVolumeFileStatus(VolumeFile volumeFile) {
        Log.d("zhang", "resetVolumeFileStatus: ");
        if (volumeFile.getCancelable() != null) {
            volumeFile.setCancelable(null);
        }
        if (volumeFile.getBusinessProgressCallback() != null) {
            volumeFile.setBusinessProgressCallback(null);
        }
        VolumeFile downloadInfo = getManagerVolumeFileInfo(volumeFile);
        deleteDownloadInfo(volumeFile);
        VolumeFileDownloadCacheUtils.deleteVolumeFile(downloadInfo);

        if (downloadInfo != null) {
            if (downloadInfo.getCancelable() != null) {
                downloadInfo.getCancelable().cancel();
                downloadInfo.setCancelable(null);
            }
            if (downloadInfo.getBusinessProgressCallback() != null) {
                downloadInfo.setBusinessProgressCallback(null);
            }
        }
    }
}
