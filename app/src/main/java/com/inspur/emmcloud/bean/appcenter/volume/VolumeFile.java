package com.inspur.emmcloud.bean.appcenter.volume;

import com.alibaba.fastjson.TypeReference;
import com.inspur.emmcloud.baselib.util.JSONUtils;
import com.inspur.emmcloud.baselib.util.StringUtils;
import com.inspur.emmcloud.baselib.widget.progressbar.CircleProgressBar;
import com.inspur.emmcloud.basemodule.util.FileUtils;
import com.inspur.emmcloud.interf.ProgressCallback;

import org.json.JSONObject;
import org.xutils.common.Callback;
import org.xutils.db.annotation.Column;
import org.xutils.db.annotation.Table;

import java.io.File;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Created by chenmch on 2017/11/16.
 */

@Table(name = "VolumeFile")
public class VolumeFile implements Serializable {
    public static final String FILE_TYPE_REGULAR = "regular";
    public static final String FILE_TYPE_DIRECTORY = "directory";

    public static final String FILTER_TYPE_DOCUNMENT = "filter_docunment";
    public static final String FILTER_TYPE_IMAGE = "filter_image";
    public static final String FILTER_TYPE_AUDIO = "filter_application";
    public static final String FILTER_TYPE_VIDEO = "filter_video";
    public static final String FILTER_TYPE_OTHER = "filter_other";
    public static final String STATUS_NORMAL = "normal";
    public static final String STATUS_UPLOAD_IND = "uploading";
    public static final String STATUS_UPLOAD_FAIL = "upload_fail";
    public static final String STATUS_UPLOAD_PAUSE = "upload_pause";
    public static final String STATUS_DOWNLOAD_IND = "downloading";
    public static final String STATUS_DOWNLOAD_FAIL = "download_fail";
    public static final String STATUS_DOWNLOAD_PAUSE = "download_pause";
    @Column(name = "id", isId = true)
    private String id = "";
    @Column(name = "type")
    private String type = "";
    @Column(name = "name")
    private String name = "";
    @Column(name = "resource")
    private String resource = "";
    @Column(name = "parent")
    private String parent = "";
    @Column(name = "creationDate")
    private long creationDate = 0L;
    @Column(name = "lastUpdate")
    private long lastUpdate = 0L;
    @Column(name = "privilege")
    private String privilege = "";
    @Column(name = "format")
    private String format = "";
    @Column(name = "size")
    private long size = 0L;
    @Column(name = "volume")
    private String volume = "";
    @Column(name = "status")
    private String status = STATUS_NORMAL;
    @Column(name = "groups")
    private String groups = "";
    @Column(name = "ownerPrivilege")
    private int ownerPrivilege = 0;
    @Column(name = "othersPrivilege")
    private int othersPrivilege = 0;
    @Column(name = "owner")
    private String owner = "";
    @Column(name = "path")
    private String path = "";
    @Column(name = "progress")
    private int progress = -1;
    @Column(name = "volumeFileAbsolutePath")
    private String volumeFileAbsolutePath = "";
    long lastRecordTime = 0;    //记录上次下载上传时间
    private Map<String, Integer> groupPrivilegeMap = new HashMap<>();
    Callback.Cancelable cancelable;
    long completeSize = 0;
    @Column(name = "localFilePath")
    private String localFilePath = "";
    /**
     * VolumeFile本身的callback，监听下载进度
     */
    private ProgressCallback progressCallback;
    /**
     * 业务的callback
     */
    private ProgressCallback businessProgressCallback;

    public VolumeFile() {
    }

    public VolumeFile(String response) {
        this(JSONUtils.getJSONObject(response));
    }

    public VolumeFile(JSONObject object) {
        this.type = JSONUtils.getString(object, "type", "");
        this.name = JSONUtils.getString(object, "name", "");
        this.resource = JSONUtils.getString(object, "resource", "");
        this.parent = JSONUtils.getString(object, "parent", "");
        this.creationDate = JSONUtils.getLong(object, "creationDate", 0L);
        this.lastUpdate = JSONUtils.getLong(object, "lastUpdate", 0L);
        this.privilege = JSONUtils.getString(object, "privilege", "");
        this.format = JSONUtils.getString(object, "format", "");
        this.size = JSONUtils.getLong(object, "size", 0L);
        this.id = JSONUtils.getString(object, "id", "");
        this.volume = JSONUtils.getString(object, "volume", "");
        this.ownerPrivilege = JSONUtils.getInt(object, "ownerPrivilege", 0);
        this.othersPrivilege = JSONUtils.getInt(object, "othersPrivilege", 0);
        this.owner = JSONUtils.getString(object, "owner", "");
        groups = JSONUtils.getString(object, "groups", "");
        if (!StringUtils.isBlank(groups)) {
            groupPrivilegeMap = JSONUtils.parseObject(groups, new TypeReference<Map<String, Integer>>() {
            });
            if (groupPrivilegeMap == null) {
                groupPrivilegeMap = new HashMap<>();
            }
        }
        path = JSONUtils.getString(object, "path", "");
    }

    public static VolumeFile getMockVolumeFile(VolumeFileUpload volumeFileUpload) {
        String filename = FileUtils.getFileName(volumeFileUpload.getLocalFilePath());
        VolumeFile volumeFile = new VolumeFile();
        volumeFile.setType(VolumeFile.FILE_TYPE_REGULAR);
        volumeFile.setId(volumeFileUpload.getId());
        volumeFile.setCreationDate(System.currentTimeMillis());
        volumeFile.setName(filename);
        volumeFile.setStatus(volumeFileUpload.getStatus());
        volumeFile.setProgress(volumeFileUpload.getProgress());
        volumeFile.setVolume(volumeFileUpload.getVolumeId());
        volumeFile.setFormat(FileUtils.getMimeType(filename));
        volumeFile.setSize(FileUtils.getFileSize(volumeFileUpload.getLocalFilePath()));
        volumeFile.setLocalFilePath(volumeFileUpload.getLocalFilePath());
        return volumeFile;
    }

    /**
     * 生成一个用于上传展示的数据
     *
     * @param file
     * @return
     */
    public static VolumeFile getMockVolumeFile(File file, String volumeId) {
        VolumeFile volumeFile = new VolumeFile();
        volumeFile.setType(VolumeFile.FILE_TYPE_REGULAR);
        volumeFile.setId(UUID.randomUUID() + "");
        volumeFile.setCreationDate(System.currentTimeMillis());
        volumeFile.setName(file.getName());
        volumeFile.setStatus(VolumeFile.STATUS_UPLOAD_IND);
        volumeFile.setVolume(volumeId);
        volumeFile.setFormat(FileUtils.getMimeType(file.getName()));
        volumeFile.setLocalFilePath(file.getAbsolutePath());
        volumeFile.setSize(FileUtils.getFileSize(file.getAbsolutePath()));
        return volumeFile;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getResource() {
        return resource;
    }

    public void setResource(String resource) {
        this.resource = resource;
    }

    public String getParent() {
        return parent;
    }

    public void setParent(String parent) {
        this.parent = parent;
    }

    public long getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(long creationDate) {
        this.creationDate = creationDate;
    }

    public long getLastUpdate() {
        return lastUpdate;
    }

    public void setLastUpdate(long lastUpdate) {
        this.lastUpdate = lastUpdate;
    }

    public String getPrivilege() {
        return privilege;
    }

    public void setPrivilege(String privilege) {
        this.privilege = privilege;
    }

    public String getFormat() {
        return format;
    }

    public void setFormat(String format) {
        this.format = format;
    }

    public long getSize() {
        return size;
    }

    public void setSize(long size) {
        this.size = size;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public Map<String, Integer> getGroupPrivilegeMap() {
        return groupPrivilegeMap;
    }

    public void setGroupPrivilegeMap(Map<String, Integer> groupPrivilegeMap) {
        this.groupPrivilegeMap = groupPrivilegeMap;
    }

    public int getOwnerPrivilege() {
        return ownerPrivilege;
    }

    public void setOwnerPrivilege(int ownerPrivilege) {
        this.ownerPrivilege = ownerPrivilege;
    }

    public int getOthersPrivilege() {
        return othersPrivilege;
    }

    public void setOthersPrivilege(int othersPrivilege) {
        this.othersPrivilege = othersPrivilege;
    }

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getVolume() {
        return volume;
    }

    public void setVolume(String volume) {
        this.volume = volume;
    }

    public String getLocalFilePath() {
        return localFilePath;
    }

    public void setLocalFilePath(String localFilePath) {
        this.localFilePath = localFilePath;
    }

    public String getGroups() {
        return groups;
    }

    public void setGroups(String groups) {
        this.groups = groups;
    }

    public int getProgress() {
        return progress;
    }

    public void setProgress(int progress) {
        this.progress = progress;
    }

    public String getVolumeFileAbsolutePath() {
        return volumeFileAbsolutePath;
    }

    public void setVolumeFileAbsolutePath(String volumeFileAbsolutePath) {
        this.volumeFileAbsolutePath = volumeFileAbsolutePath;
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

    public Callback.Cancelable getCancelable() {
        return cancelable;
    }

    public void setCancelable(Callback.Cancelable cancelable) {
        this.cancelable = cancelable;
    }

    public long getLastRecordTime() {
        return lastRecordTime;
    }

    public void setLastRecordTime(long lastRecordTime) {
        this.lastRecordTime = lastRecordTime;
    }

    public long getCompleteSize() {
        return completeSize;
    }

    public void setCompleteSize(long completeSize) {
        this.completeSize = completeSize;
    }

    public CircleProgressBar.Status transfer2ProgressStatus(String status) {
        CircleProgressBar.Status pbStatus = CircleProgressBar.Status.Starting;
        switch (status) {
            case STATUS_NORMAL:
                pbStatus = CircleProgressBar.Status.Starting;
                break;
            case STATUS_UPLOAD_IND:
                pbStatus = CircleProgressBar.Status.Uploading;
                break;
            case STATUS_DOWNLOAD_IND:
                pbStatus = CircleProgressBar.Status.Downloading;
                break;
            case STATUS_UPLOAD_FAIL:
            case STATUS_DOWNLOAD_FAIL:
                pbStatus = CircleProgressBar.Status.End;
                break;
            case STATUS_UPLOAD_PAUSE:
            case STATUS_DOWNLOAD_PAUSE:
                pbStatus = CircleProgressBar.Status.Pause;
                break;
            default:
                pbStatus = CircleProgressBar.Status.Starting;
                break;
        }

        return pbStatus;
    }

    public boolean equals(Object other) { // 重写equals方法，后面最好重写hashCode方法

        if (this == other) // 先检查是否其自反性，后比较other是否为空。这样效率高
            return true;
        if (other == null)
            return false;
        if (!(other instanceof VolumeFile))
            return false;

        final VolumeFile otherVolumeFile = (VolumeFile) other;
        return getId().equals(otherVolumeFile.getId());
    }
}
