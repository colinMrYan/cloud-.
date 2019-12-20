package com.inspur.emmcloud.bean;

import com.inspur.emmcloud.api.APIUri;
import com.inspur.emmcloud.baselib.widget.progressbar.CircleProgressBar;
import com.inspur.emmcloud.bean.chat.Message;
import com.inspur.emmcloud.interf.ChatProgressCallback;

import org.xutils.common.Callback;
import org.xutils.db.annotation.Column;
import org.xutils.db.annotation.Table;

import java.io.Serializable;

@Table(name = "DownloadInfo")
public class DownloadInfo implements Serializable {
    public static final String TYPE_MESSAGE = "message";
    public static final String TYPE_VOLUME = "volume";
    public static final String STATUS_NORMAL = "normal";
    public static final String STATUS_LOADING = "loading";
    public static final String STATUS_PAUSE = "pause";
    public static final String STATUS_SUCCESS = "success";
    public static final String STATUS_FAIL = "fail";
    transient Callback.Cancelable cancelable;
    @Column(name = "id", isId = true, autoGen = true)
    private int id;
    @Column(name = "fileId")
    private String fileId;
    @Column(name = "fileName")
    private String fileName;
    @Column(name = "url")
    private String url;
    @Column(name = "path")
    private String path;
    @Column(name = "localPath")
    private String localPath;
    @Column(name = "status")
    private String status = STATUS_NORMAL;
    @Column(name = "progress")
    private int progress;
    @Column(name = "createTime")
    private String createTime;
    @Column(name = "lastUpdateTime")
    private Long lastUpdateTime = 0L;
    @Column(name = "completed")
    private Long completed = 0L;
    @Column(name = "size")
    private Long size;
    @Column(name = "type")
    private String type;

    /**
     * 业务的callback
     */
    private transient ChatProgressCallback businessProgressCallback;

    public static DownloadInfo message2DownloadInfo(Message message) {
        DownloadInfo info = new DownloadInfo();
        info.setFileId(message.getId());
        info.setFileName(message.getMsgContentAttachmentFile().getName());
        info.setLocalPath(message.getLocalPath());
        info.setType(TYPE_MESSAGE);
        info.setUrl(APIUri.getChatFileResourceUrl(message));
        info.setLastUpdateTime(System.currentTimeMillis());
        info.setSize(message.getMsgContentAttachmentFile().getSize());

        return info;
    }

    public String getFileId() {
        return fileId;
    }

    public void setFileId(String fileId) {
        this.fileId = fileId;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
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

    public Long getLastUpdateTime() {
        return lastUpdateTime;
    }

    public void setLastUpdateTime(Long lastUpdateTime) {
        this.lastUpdateTime = lastUpdateTime;
    }

    public Long getCompleted() {
        return completed;
    }

    public void setCompleted(Long completed) {
        this.completed = completed;
    }

    public static CircleProgressBar.Status transfer2ProgressStatus(String status) {
        CircleProgressBar.Status pbStatus;
        switch (status) {
            case STATUS_NORMAL:
                pbStatus = CircleProgressBar.Status.Starting;
                break;
            case STATUS_LOADING:
                pbStatus = CircleProgressBar.Status.Loading;
                break;
            case STATUS_FAIL:
                pbStatus = CircleProgressBar.Status.Fail;
                break;
            case STATUS_PAUSE:
                pbStatus = CircleProgressBar.Status.Pause;
                break;
            case STATUS_SUCCESS:
                pbStatus = CircleProgressBar.Status.Success;
                break;
            default:
                pbStatus = CircleProgressBar.Status.Starting;
                break;
        }

        return pbStatus;
    }

    public long getSize() {
        return size;
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

    public void setSize(long size) {
        this.size = size;
    }

    public ChatProgressCallback getBusinessProgressCallback() {
        return businessProgressCallback;
    }

    public void setBusinessProgressCallback(ChatProgressCallback businessProgressCallback) {
        this.businessProgressCallback = businessProgressCallback;
    }
}
