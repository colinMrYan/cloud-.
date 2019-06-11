package com.inspur.emmcloud.bean.appcenter.volume;

import com.inspur.emmcloud.interf.ProgressCallback;
import com.inspur.emmcloud.interf.VolumeFileUploadService;

/**
 * 云盘文件上传信息类
 */

public class VolumeFileUploadInfo {
    private VolumeFileUploadService volumeFileUploadService;
    private VolumeFile volumeFile;
    private String volumeFileParentPath;
    private ProgressCallback progressCallback;
    private String localFilePath;

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
}
