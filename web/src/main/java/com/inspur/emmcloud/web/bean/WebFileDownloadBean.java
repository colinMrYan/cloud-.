package com.inspur.emmcloud.web.bean;

import com.inspur.emmcloud.web.api.WebProgressCallback;

import org.xutils.common.Callback;
import org.xutils.db.annotation.Column;
import org.xutils.db.annotation.Table;

import java.io.Serializable;

/**
 * Date：2021/8/25
 * Author：wang zhen
 * Description web文件下载bean
 */
@Table(name = "WebFileDownload")
public class WebFileDownloadBean implements Serializable {
    public static final String TYPE_MESSAGE = "message";
    public static final String TYPE_VOLUME = "volume";
    public static final String TYPE_WEB = "web";
    public static final String STATUS_NORMAL = "normal";
    public static final String STATUS_LOADING = "loading";
    public static final String STATUS_PAUSE = "pause";
    public static final String STATUS_SUCCESS = "success";
    public static final String STATUS_FAIL = "fail";
    @Column(name = "fileId", isId = true)
    private String fileId;
    @Column(name = "type")
    private String type = "";
    @Column(name = "fileName")
    private String fileName;
    @Column(name = "downloadUrl")
    private String downloadUrl;
    @Column(name = "path")
    private String path = "";
    @Column(name = "localPath")
    private String localPath;
    @Column(name = "fileSize")
    private long fileSize;
    @Column(name = "status")
    private String status = STATUS_NORMAL;
    @Column(name = "createTime")
    private String createTime;
    @Column(name = "lastUpdateTime")
    private long lastUpdateTime = 0L;
    @Column(name = "completed")
    private Long completed = 0L;
    @Column(name = "progress")
    private int progress = -1;
    /**
     * 业务的callback
     */
    private transient WebProgressCallback businessProgressCallback;
    transient Callback.Cancelable cancelable;

    public WebFileDownloadBean() {
    }

    public WebFileDownloadBean(String fileId, long fileSize, String createTime, String downloadUrl, String fileName) {
        this.fileId = fileId;
        this.fileSize = fileSize;
        this.createTime = createTime;
        this.downloadUrl = downloadUrl;
        this.fileName = fileName;
    }

    public int getProgress() {
        return progress;
    }

    public void setProgress(int progress) {
        this.progress = progress;
    }

    public Long getLastUpdateTime() {
        return lastUpdateTime;
    }

    public void setLastUpdateTime(Long lastUpdateTime) {
        this.lastUpdateTime = lastUpdateTime;
    }

    public String getLocalPath() {
        return localPath;
    }

    public void setLocalPath(String localPath) {
        this.localPath = localPath;
    }

    public String getFileId() {
        return fileId;
    }

    public void setFileId(String fileId) {
        this.fileId = fileId;
    }

    public long getFileSize() {
        return fileSize;
    }

    public void setFileSize(long fileSize) {
        this.fileSize = fileSize;
    }

    public String getCreateTime() {
        return createTime;
    }

    public void setCreateTime(String createTime) {
        this.createTime = createTime;
    }

    public String getDownloadUrl() {
        return downloadUrl;
    }

    public void setDownloadUrl(String downloadUrl) {
        this.downloadUrl = downloadUrl;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Callback.Cancelable getCancelable() {
        return cancelable;
    }

    public void setCancelable(Callback.Cancelable cancelable) {
        this.cancelable = cancelable;
    }

    public WebProgressCallback getBusinessProgressCallback() {
        return businessProgressCallback;
    }

    public void setBusinessProgressCallback(WebProgressCallback businessProgressCallback) {
        this.businessProgressCallback = businessProgressCallback;
    }

    public Long getCompleted() {
        return completed;
    }

    public void setCompleted(Long completed) {
        this.completed = completed;
    }
}
