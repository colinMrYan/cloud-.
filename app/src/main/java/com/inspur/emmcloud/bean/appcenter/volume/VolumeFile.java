package com.inspur.emmcloud.bean.appcenter.volume;

import com.alibaba.fastjson.TypeReference;
import com.inspur.emmcloud.baselib.util.JSONUtils;
import com.inspur.emmcloud.baselib.util.StringUtils;

import org.json.JSONObject;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by chenmch on 2017/11/16.
 */

public class VolumeFile implements Serializable {
    public static final String FILE_TYPE_REGULAR = "regular";
    public static final String FILE_TYPE_DIRECTORY = "directory";

    public static final String FILTER_TYPE_DOCUNMENT = "filter_docunment";
    public static final String FILTER_TYPE_IMAGE = "filter_image";
    public static final String FILTER_TYPE_AUDIO = "filter_application";
    public static final String FILTER_TYPE_VIDEO = "filter_video";
    public static final String FILTER_TYPE_OTHER = "filter_other";
    public static final String STATUS_NORMAL = "normal";
    public static final String STATUS_UPLOADIND = "uploading";
    public static final String STATUS_UPLOAD_FAIL = "upload_fail";
    public static final String STATUS_UPLOAD_PAUSE = "upload_pause";
    private String id = "";
    private String type = "";
    private String name = "";
    private String resource = "";
    private String parent = "";
    private long creationDate = 0L;
    private long lastUpdate = 0L;
    private String privilege = "";
    private String format = "";
    private long size = 0L;
    private String volume = "";
    private String status = STATUS_NORMAL;
    private Map<String, Integer> groupPrivilegeMap = new HashMap<>();
    private int ownerPrivilege = 0;
    private int othersPrivilege = 0;
    private String owner = "";
    private String path = "";
    private String localFilePath = "";

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
        String groupPrivilegeJson = JSONUtils.getString(object, "groups", "");
        if (!StringUtils.isBlank(groupPrivilegeJson)) {
            groupPrivilegeMap = JSONUtils.parseObject(groupPrivilegeJson, new TypeReference<Map<String, Integer>>() {
            });
            if (groupPrivilegeMap == null) {
                groupPrivilegeMap = new HashMap<>();
            }
        }
        path = JSONUtils.getString(object, "path", "");
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
