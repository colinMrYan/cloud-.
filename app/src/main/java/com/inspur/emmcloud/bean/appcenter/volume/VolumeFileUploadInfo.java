package com.inspur.emmcloud.bean.appcenter.volume;

import com.inspur.emmcloud.componentservice.download.ProgressCallback;
import com.inspur.emmcloud.componentservice.volume.GetVolumeFileUploadTokenResult;
import com.inspur.emmcloud.componentservice.volume.VolumeFile;
import com.inspur.emmcloud.componentservice.volume.VolumeFileUploadService;

/**
 * 云盘文件上传信息类
 */

public class VolumeFileUploadInfo {
    private VolumeFileUploadService volumeFileUploadService;
    private VolumeFile volumeFile;
    private String volumeFileParentPath;
    private ProgressCallback progressCallback;
    private String localFilePath;
    private GetVolumeFileUploadTokenResult getVolumeFileUploadTokenResult;
    private int transferObserverId = -1;
    public VolumeFileUploadInfo(VolumeFileUploadService volumeFileUploadService, VolumeFile volumeFile, String volumeFileParentPath, ProgressCallback progressCallback, String localFilePath) {
        this.volumeFileUploadService = volumeFileUploadService;
        this.volumeFile = volumeFile;
        this.volumeFileParentPath = volumeFileParentPath;
        this.progressCallback = progressCallback;
        this.localFilePath = localFilePath;
    }

    public VolumeFileUploadService getVolumeFileUploadService() {
        return volumeFileUploadService;
    }

    public void setVolumeFileUploadService(VolumeFileUploadService volumeFileUploadService) {
        this.volumeFileUploadService = volumeFileUploadService;
    }

    public VolumeFile getVolumeFile() {
        return volumeFile;
    }

    public void setVolumeFile(VolumeFile volumeFile) {
        this.volumeFile = volumeFile;
    }

    public String getVolumeFileParentPath() {
        return volumeFileParentPath;
    }

    public void setVolumeFileParentPath(String volumeFileParentPath) {
        this.volumeFileParentPath = volumeFileParentPath;
    }

    public ProgressCallback getProgressCallback() {
        return progressCallback;
    }

    public void setProgressCallback(ProgressCallback progressCallback) {
        this.progressCallback = progressCallback;
    }

    public String getLocalFilePath() {
        return localFilePath;
    }

    public void setLocalFilePath(String localFilePath) {
        this.localFilePath = localFilePath;
    }

    public GetVolumeFileUploadTokenResult getGetVolumeFileUploadTokenResult() {
        return getVolumeFileUploadTokenResult;
    }

    public void setGetVolumeFileUploadTokenResult(GetVolumeFileUploadTokenResult getVolumeFileUploadTokenResult) {
        this.getVolumeFileUploadTokenResult = getVolumeFileUploadTokenResult;
    }

    public int getTransferObserverId() {
        return transferObserverId;
    }

    public void setTransferObserverId(int transferObserverId) {
        this.transferObserverId = transferObserverId;
    }
}
