package com.inspur.emmcloud.util.privates;

import android.os.Handler;

import com.inspur.emmcloud.MyApplication;
import com.inspur.emmcloud.api.APIInterfaceInstance;
import com.inspur.emmcloud.api.apiservice.MyAppAPIService;
import com.inspur.emmcloud.basemodule.application.BaseApplication;
import com.inspur.emmcloud.basemodule.bean.SimpleEventMessage;
import com.inspur.emmcloud.basemodule.config.Constant;
import com.inspur.emmcloud.basemodule.util.FileUtils;
import com.inspur.emmcloud.basemodule.util.NetUtils;
import com.inspur.emmcloud.bean.appcenter.volume.GetVolumeFileUploadTokenResult;
import com.inspur.emmcloud.bean.appcenter.volume.VolumeFile;
import com.inspur.emmcloud.bean.appcenter.volume.VolumeFileUpload;
import com.inspur.emmcloud.interf.ProgressCallback;
import com.inspur.emmcloud.interf.VolumeFileUploadService;
import com.inspur.emmcloud.util.privates.cache.VolumeFileUploadCacheUtils;
import com.inspur.emmcloud.util.privates.oss.OssService;
import com.inspur.emmcloud.util.privates.s3.S3Service;

import org.greenrobot.eventbus.EventBus;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * 云盘文件上传管理类
 */

public class VolumeFileUploadManager extends APIInterfaceInstance {
    private static VolumeFileUploadManager instance;
    private List<VolumeFileUpload> volumeFileUploadList = new ArrayList<>();
    private MyAppAPIService apiService;

    public VolumeFileUploadManager() {
        apiService = new MyAppAPIService(MyApplication.getInstance());
        apiService.setAPIInterface(this);
        volumeFileUploadList = VolumeFileUploadCacheUtils.getVolumeFileUploadList();
        boolean isNeedUpdateVolumeFileUploadStatus = false;
        for (VolumeFileUpload volumeFileUpload : volumeFileUploadList) {
            if (volumeFileUpload.getStatus().equals(VolumeFile.STATUS_UPLOADIND)) {
                volumeFileUpload.setStatus(VolumeFile.STATUS_UPLOAD_PAUSE);
                isNeedUpdateVolumeFileUploadStatus = true;
            }
        }
        if (isNeedUpdateVolumeFileUploadStatus) {
            VolumeFileUploadCacheUtils.saveVolumeFileUploadList(volumeFileUploadList);
        }

    }

    public static VolumeFileUploadManager getInstance() {
        if (instance == null) {
            synchronized (VolumeFileUploadManager.class) {
                if (instance == null) {
                    instance = new VolumeFileUploadManager();
                }
            }
        }
        return instance;
    }

    private static VolumeFile getMockVolumeFile(VolumeFileUpload volumeFileUpload) {
        long time = System.currentTimeMillis();
        String filename = FileUtils.getFileName(volumeFileUpload.getLocalFilePath());
        VolumeFile volumeFile = new VolumeFile();
        volumeFile.setType(VolumeFile.FILE_TYPE_REGULAR);
        volumeFile.setId(volumeFileUpload.getId());
        volumeFile.setCreationDate(time);
        volumeFile.setName(filename);
        volumeFile.setStatus(volumeFileUpload.getStatus());
        volumeFile.setVolume(volumeFileUpload.getVolumeId());
        volumeFile.setFormat(FileUtils.getMimeType(filename));
        return volumeFile;
    }

    /**
     * 上传文件
     *
     * @param mockVolumeFile
     * @param localFilePath
     * @param volumeFileParentPath
     */
    public void uploadFile(VolumeFile mockVolumeFile, String localFilePath, String volumeFileParentPath) {
        File file = new File(localFilePath);
        VolumeFileUpload volumeFileUpload = new VolumeFileUpload(mockVolumeFile, localFilePath, volumeFileParentPath);
        if (FileUtils.isFileExist(volumeFileUpload.getLocalFilePath()) && NetUtils.isNetworkConnected(BaseApplication.getInstance())) {
            volumeFileUpload.setStatus(VolumeFile.STATUS_UPLOADIND);
            apiService.getVolumeFileUploadToken(file.getName(), volumeFileUpload, mockVolumeFile);
        } else {
            volumeFileUpload.setStatus(VolumeFile.STATUS_UPLOAD_FAIL);
        }
        volumeFileUploadList.add(volumeFileUpload);
        VolumeFileUploadCacheUtils.saveVolumeFileUpload(volumeFileUpload);

    }

    /**
     * 重新上传
     *
     * @param mockVolumeFile
     */
    public void reUploadFile(VolumeFile mockVolumeFile) {
        for (int i = 0; i < volumeFileUploadList.size(); i++) {
            VolumeFileUpload volumeFileUpload = volumeFileUploadList.get(i);
            if (volumeFileUpload.getId().equals(mockVolumeFile.getId())) {
                if (FileUtils.isFileExist(volumeFileUpload.getLocalFilePath())) {
                    volumeFileUpload.setStatus(VolumeFile.STATUS_UPLOADIND);
                    //上传文件
                    apiService.getVolumeFileUploadToken(mockVolumeFile.getName(), volumeFileUpload, mockVolumeFile);
                } else {
                    volumeFileUpload.setStatus(VolumeFile.STATUS_UPLOAD_FAIL);
                }
                VolumeFileUploadCacheUtils.saveVolumeFileUpload(volumeFileUpload);
                break;
            }
        }
    }

    /**
     * 获取云盘此文件夹目录下正在上传的云盘文件
     *
     * @param volumeId
     * @param volumeFileParentPath
     * @return
     */
    public List<VolumeFile> getCurrentFolderUploadVolumeFile(String volumeId, String volumeFileParentPath) {
        List<VolumeFile> volumeFileList = new ArrayList<>();
        for (int i = 0; i < volumeFileUploadList.size(); i++) {
            VolumeFileUpload volumeFileUpload = volumeFileUploadList.get(i);
            if (volumeFileUpload.getVolumeId().equals(volumeId) && volumeFileUpload.getVolumeFileParentPath().equals(volumeFileParentPath)) {
                VolumeFile mockVolumeFile = getMockVolumeFile(volumeFileUpload);
                volumeFileList.add(mockVolumeFile);
            }
        }
        return volumeFileList;
    }

    /**
     * 获取云盘此文件夹目录下正在上传的云盘文件
     *
     * @return
     */
    public List<VolumeFile> getAllUploadVolumeFile() {
        List<VolumeFile> volumeFileList = new ArrayList<>();
        for (int i = 0; i < volumeFileUploadList.size(); i++) {
            VolumeFileUpload volumeFileUpload = volumeFileUploadList.get(i);
            VolumeFile mockVolumeFile = getMockVolumeFile(volumeFileUpload);
            volumeFileList.add(mockVolumeFile);

        }
        return volumeFileList;
    }

    /**
     * 移除上传服务
     *
     * @param mockVolumeFile
     */
    public void cancelVolumeFileUploadService(VolumeFile mockVolumeFile) {
        if (mockVolumeFile != null) {
            for (int i = 0; i < volumeFileUploadList.size(); i++) {
                VolumeFileUpload volumeFileUpload = volumeFileUploadList.get(i);
                if (volumeFileUpload.getId().equals(mockVolumeFile.getId())) {
                    VolumeFileUploadService volumeFileUploadService = volumeFileUpload.getVolumeFileUploadService();
                    if (volumeFileUploadService != null) {
                        volumeFileUploadService.onDestroy();
                    }
                    volumeFileUploadList.remove(i);
                    VolumeFileUploadCacheUtils.deleteVolumeFileUpload(volumeFileUpload);
                    break;
                }
            }
        }
    }

    public void pauseVolumeFileUploadService(VolumeFile mockVolumeFile) {
        if (mockVolumeFile != null) {
            for (int i = 0; i < volumeFileUploadList.size(); i++) {
                VolumeFileUpload volumeFileUpload = volumeFileUploadList.get(i);
                if (volumeFileUpload.getId().equals(mockVolumeFile.getId())) {
                    VolumeFileUploadService volumeFileUploadService = volumeFileUpload.getVolumeFileUploadService();
                    if (volumeFileUploadService != null) {
                        volumeFileUploadService.onPause();
                    }
                    volumeFileUpload.setStatus(VolumeFile.STATUS_UPLOAD_PAUSE);
                    VolumeFileUploadCacheUtils.saveVolumeFileUpload(volumeFileUpload);
                    break;
                }
            }
        }
    }

    /**
     * 设置上传callback
     *
     * @param volumeFile
     * @param businessProgressCallback
     */
    public void setBusinessProgressCallback(VolumeFile volumeFile, final ProgressCallback businessProgressCallback) {
        for (int i = 0; i < volumeFileUploadList.size(); i++) {
            VolumeFileUpload volumeFileUpload = volumeFileUploadList.get(i);
            if (volumeFileUpload.getId().equals(volumeFile.getId())) {
                if (businessProgressCallback != null) {
                    if (volumeFileUpload.getStatus().equals(VolumeFile.STATUS_UPLOADIND)) {
                        businessProgressCallback.onLoading(volumeFileUpload.getProgress(), "");
                    } else if (volumeFileUpload.getStatus().equals(VolumeFile.STATUS_UPLOAD_FAIL)) {
                        new Handler().post(new Runnable() {
                            @Override
                            public void run() {
                                businessProgressCallback.onFail();
                            }
                        });

                    }
                }
                volumeFileUpload.setBusinessProgressCallback(businessProgressCallback);
                break;
            }
        }
    }

    /**
     * 根据不同的storage选择不同的存储服务
     *
     * @param volumeFileUpload
     * @param mockVolumeFile
     * @return
     */
    private VolumeFileUploadService getVolumeFileUploadService(VolumeFileUpload volumeFileUpload, VolumeFile mockVolumeFile) {
        VolumeFileUploadService volumeFileUploadService = null;
        switch (volumeFileUpload.getGetVolumeFileUploadTokenResult().getStorage()) {
            case "ali_oss":  //阿里云
                try {
                    volumeFileUploadService = new OssService(volumeFileUpload.getGetVolumeFileUploadTokenResult(), mockVolumeFile);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
            case "aws_s3":
                volumeFileUploadService = new S3Service(volumeFileUpload);
                break;
            default:
                break;
        }
        return volumeFileUploadService;
    }

    @Override
    public void returnVolumeFileUploadTokenSuccess(GetVolumeFileUploadTokenResult getVolumeFileUploadTokenResult, String fileLocalPath, VolumeFile mockVolumeFile, int transferObserverId) {
        for (int i = 0; i < volumeFileUploadList.size(); i++) {
            VolumeFileUpload volumeFileUpload = volumeFileUploadList.get(i);
            if (volumeFileUpload.getId().equals(mockVolumeFile.getId())) {
                ProgressCallback progressCallback = volumeFileUpload.getProgressCallback();
                if (progressCallback == null) {
                    progressCallback = new MyProgressCallback(volumeFileUpload);
                    volumeFileUpload.setProgressCallback(progressCallback);
                }
                volumeFileUpload.setGetVolumeFileUploadTokenResult(getVolumeFileUploadTokenResult);
                VolumeFileUploadService volumeFileUploadService = getVolumeFileUploadService(volumeFileUpload, mockVolumeFile);
                if (volumeFileUploadService != null) {
                    volumeFileUploadService.setProgressCallback(progressCallback);
                    volumeFileUpload.setTransferObserverId(transferObserverId);
                    volumeFileUpload.setVolumeFileUploadService(volumeFileUploadService);
                    volumeFileUploadService.uploadFile(getVolumeFileUploadTokenResult.getFileName(), fileLocalPath);
                } else {  //如果没有获取相应的上传服务 返回上传失败
                    progressCallback.onFail();
                }
                break;
            }
        }
    }

    @Override
    public void returnVolumeFileUploadTokenFail(VolumeFile mockVolumeFile, String error, int errorCode, String filePath) {
        for (int i = 0; i < volumeFileUploadList.size(); i++) {
            VolumeFileUpload volumeFileUpload = volumeFileUploadList.get(i);
            if (volumeFileUpload.getId().equals(mockVolumeFile.getId())) {
                //如果ProgressCallback已经从ui传递进来，则给volumeFileUploadService设置ProgressCallback
                ProgressCallback progressCallback = volumeFileUpload.getProgressCallback();
                progressCallback.onFail();
                break;
            }
        }
    }

    private class MyProgressCallback implements ProgressCallback {
        private VolumeFileUpload volumeFileUpload;

        public MyProgressCallback(VolumeFileUpload volumeFileUpload) {
            this.volumeFileUpload = volumeFileUpload;
        }

        @Override
        public void onSuccess(VolumeFile volumeFile) {
            volumeFileUploadList.remove(volumeFileUpload);
            VolumeFileUploadCacheUtils.deleteVolumeFileUpload(volumeFileUpload);
            if (volumeFileUpload.getBusinessProgressCallback() != null) {
                volumeFileUpload.getBusinessProgressCallback().onSuccess(volumeFile);
            }
            EventBus.getDefault().post(new SimpleEventMessage(Constant.EVENTBUS_TAG_VOLUME_UPLOAD, volumeFile));
        }

        @Override
        public void onLoading(int progress, String uploadSpeed) {
            volumeFileUpload.setProgress(progress);
            volumeFileUpload.setStatus(VolumeFile.STATUS_UPLOADIND);
            VolumeFileUploadCacheUtils.saveVolumeFileUpload(volumeFileUpload);
            if (volumeFileUpload.getBusinessProgressCallback() != null) {
                volumeFileUpload.getBusinessProgressCallback().onLoading(progress, uploadSpeed);
            }
        }

        @Override
        public void onFail() {
            volumeFileUpload.setStatus(VolumeFile.STATUS_UPLOAD_FAIL);
            VolumeFileUploadCacheUtils.saveVolumeFileUpload(volumeFileUpload);
            if (volumeFileUpload.getBusinessProgressCallback() != null) {
                volumeFileUpload.getBusinessProgressCallback().onFail();
            }
        }
    }
}
