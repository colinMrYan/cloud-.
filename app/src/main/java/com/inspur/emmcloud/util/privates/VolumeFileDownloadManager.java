package com.inspur.emmcloud.util.privates;

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

import org.xutils.http.HttpMethod;
import org.xutils.http.RequestParams;
import org.xutils.http.app.RedirectHandler;
import org.xutils.http.request.UriRequest;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class VolumeFileDownloadManager extends APIInterfaceInstance {
    private static volatile VolumeFileDownloadManager instance;
    private List<VolumeFile> volumeFileDownloadList = new ArrayList<>();
    private MyAppAPIService apiService;

    public VolumeFileDownloadManager() {
        apiService = new MyAppAPIService(MyApplication.getInstance());
        apiService.setAPIInterface(this);
        volumeFileDownloadList = VolumeFileDownloadCacheUtils.getVolumeFileListInDownloading();
        boolean isNeedUpdateVolumeFileDownloadStatus = false;
        for (VolumeFile volumeFile : volumeFileDownloadList) {
            if (volumeFile.getStatus().equals(VolumeFile.STATUS_DOWNLOAD_IND)) {
                volumeFile.setStatus(VolumeFile.STATUS_UPLOAD_PAUSE);
                isNeedUpdateVolumeFileDownloadStatus = true;
            }
        }
        if (isNeedUpdateVolumeFileDownloadStatus) {
            VolumeFileDownloadCacheUtils.saveVolumeFileList(volumeFileDownloadList);
        }
    }

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

    /**
     * 获取云盘此文件夹目录下正在下载的云盘文件
     *
     * @return
     */
    public List<VolumeFile> getAllUploadVolumeFile() {

        return volumeFileDownloadList;
    }

    /**
     * 下载文件
     */
    public void downloadFile(final VolumeFile volumeFile) {
        String currentDirAbsolutePath = "";
        final String fileSavePath = MyAppConfig.getFileDownloadByUserAndTanentDirPath() +
                FileUtils.getNoDuplicateFileNameInDir(MyAppConfig.getFileDownloadByUserAndTanentDirPath(), volumeFile.getName());

        final String volumeId = volumeFile.getId();
        String source = APIUri.getVolumeFileUploadSTSTokenUrl(volumeId);
        APIDownloadCallBack callBack = new APIDownloadCallBack(BaseApplication.getInstance(), source) {
            @Override
            public void callbackStart() {

            }

            @Override
            public void callbackLoading(long total, long current, boolean isUploading) {
                int progress = (int) (current * 100.0 / total);
                String totalSize = FileUtils.formatFileSize(total);
                String currentSize = FileUtils.formatFileSize(current);
            }

            @Override
            public void callbackSuccess(File file) {
                FileDownloadManager.getInstance().saveDownloadFileInfo(DownloadFileCategory.CATEGORY_VOLUME_FILE,
                        volumeFile.getId(), volumeFile.getName(), fileSavePath);
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
//        cancelable = x.http().get(params, callBack);
    }
}
