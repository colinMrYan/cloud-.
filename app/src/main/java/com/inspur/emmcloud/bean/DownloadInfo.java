package com.inspur.emmcloud.bean;

import com.inspur.emmcloud.bean.chat.Message;

import org.xutils.common.Callback;
import org.xutils.db.annotation.Column;
import org.xutils.db.annotation.Table;

import java.io.Serializable;

@Table(name = "DownloadFile")
public class DownloadInfo implements Serializable {
    public static final String TYPE_MESSAGE = "message";
    public static final String TYPE_VOLUME = "volume";
    transient Callback.Cancelable cancelable;
    @Column(name = "id", isId = true, autoGen = true)
    private int id;
    @Column(name = "fileId")
    private String fileId;
    @Column(name = "url")
    private String url;
    @Column(name = "path")
    private String path;
    @Column(name = "localPath")
    private String localPath;
    @Column(name = "status")
    private String status;
    @Column(name = "progress")
    private int progress;
    @Column(name = "createTime")
    private String createTime;
    @Column(name = "lastUpdateTime")
    private String lastUpdateTime;
    @Column(name = "completed")
    private String completed;
    @Column(name = "size")
    private String size;
    @Column(name = "type")
    private String type;

    public static DownloadInfo message2DownloadInfo(Message message) {
        DownloadInfo info = new DownloadInfo();
        info.setFileId(message.getId());
        info.setLocalPath(message.getLocalPath());
        info.setType(TYPE_MESSAGE);

        return null;
    }

    public String getFileId() {
        return fileId;
    }

    public void setFileId(String fileId) {
        this.fileId = fileId;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getLocalPath() {
        return localPath;
    }

    public void setLocalPath(String localPath) {
        this.localPath = localPath;
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

    public String getCreateTime() {
        return createTime;
    }

    public void setCreateTime(String createTime) {
        this.createTime = createTime;
    }

    public String getLastUpdateTime() {
        return lastUpdateTime;
    }

    public void setLastUpdateTime(String lastUpdateTime) {
        this.lastUpdateTime = lastUpdateTime;
    }

    public String getCompleted() {
        return completed;
    }

    public void setCompleted(String completed) {
        this.completed = completed;
    }

    public String getSize() {
        return size;
    }

    public void setSize(String size) {
        this.size = size;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Callback.Cancelable getCancelable() {
        return cancelable;
    }

    public void setCancelable(Callback.Cancelable cancelable) {
        this.cancelable = cancelable;
    }
}
