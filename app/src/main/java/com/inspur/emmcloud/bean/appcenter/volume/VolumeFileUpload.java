package com.inspur.emmcloud.bean.appcenter.volume;

import com.inspur.emmcloud.interf.ProgressCallback;
import com.inspur.emmcloud.interf.VolumeFileUploadService;

import org.xutils.db.annotation.Column;
import org.xutils.db.annotation.Table;

/**
 * Created by chenmch on 2019/9/16.
 */

@Table(name = "VolumeFileUpload")
public class VolumeFileUpload {
    @Column(name = "id", isId = true)
    private String id;
    /**
     * getGetVolumeFileUploadTokenResult中的filename
     */
    @Column(name = "uploadId")
    private String uploadId;
    /**
     * getGetVolumeFileUploadTokenResult中的x:path
     */
    @Column(name = "uploadPath")
    private String uploadPath;
    @Column(name = "localFilePath")
    private String localFilePath;
    @Column(name = "transferObserverId")
    private int transferObserverId;
    @Column(name = "volumeFileParentPath")
    private String volumeFileParentPath;
    @Column(name = "status")
    private String status;
    @Column(name = "progress")
    private int progress = 0;
    @Column(name = "volumeId")
    private String volumeId;
    /**
     * VolumeFileUpload本身的callback，监听上传进度
     */
    private ProgressCallback progressCallback;
    /**
     * 业务的callback
     */
    private ProgressCallback businessProgressCallback;
    private GetVolumeFileUploadTokenResult getVolumeFileUploadTokenResult;
    private VolumeFileUploadService volumeFileUploadService;

    public VolumeFileUpload() {

    }

    public VolumeFileUpload(VolumeFile mockVolumeFile, String localFilePath, String volumeFileParentPath) {
        this.volumeId = mockVolumeFile.getVolume();
        this.id = mockVolumeFile.getId();
        this.localFilePath = localFilePath;
        this.volumeFileParentPath = volumeFileParentPath;
    }

    public ProgressCallback getProgressCallback() {
        return progressCallback;
    }

    public void setProgressCallback(ProgressCallback progressCallback) {
        this.progressCallback = progressCallback;
    }

    public ProgressCallback getBusinessProgressCallback() {
        return businessProgressCallback;
    }

    public void setBusinessProgressCallback(ProgressCallback businessProgressCallback) {
        this.businessProgressCallback = businessProgressCallback;
    }

    public GetVolumeFileUploadTokenResult getGetVolumeFileUploadTokenResult() {
        return getVolumeFileUploadTokenResult;
    }

    public void setGetVolumeFileUploadTokenResult(GetVolumeFileUploadTokenResult getVolumeFileUploadTokenResult) {
        this.getVolumeFileUploadTokenResult = getVolumeFileUploadTokenResult;
        this.uploadId = getVolumeFileUploadTokenResult.getFileName();
        this.uploadPath = getVolumeFileUploadTokenResult.getXPath();
    }

    public VolumeFileUploadService getVolumeFileUploadService() {
        return volumeFileUploadService;
    }

    public void setVolumeFileUploadService(VolumeFileUploadService volumeFileUploadService) {
        this.volumeFileUploadService = volumeFileUploadService;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUploadId() {
        return uploadId;
    }

    public void setUploadId(String uploadId) {
        this.uploadId = uploadId;
    }

    public String getUploadPath() {
        return uploadPath;
    }

    public void setUploadPath(String uploadPath) {
        this.uploadPath = uploadPath;
    }

    public String getLocalFilePath() {
        return localFilePath;
    }

    public void setLocalFilePath(String localFilePath) {
        this.localFilePath = localFilePath;
    }

    public int getTransferObserverId() {
        return transferObserverId;
    }

    public void setTransferObserverId(int transferObserverId) {
        this.transferObserverId = transferObserverId;
    }

    public String getVolumeFileParentPath() {
        return volumeFileParentPath;
    }

    public void setVolumeFileParentPath(String volumeFileParentPath) {
        this.volumeFileParentPath = volumeFileParentPath;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public int getProgress() {
        return progress;
    }

    public void setProgress(int progress) {
        this.progress = progress;
    }

    public String getVolumeId() {
        return volumeId;
    }

    public void setVolumeId(String volumeId) {
        this.volumeId = volumeId;
    }
}
