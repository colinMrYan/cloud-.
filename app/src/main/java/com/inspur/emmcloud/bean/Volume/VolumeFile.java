package com.inspur.emmcloud.bean.Volume;

import com.inspur.emmcloud.util.JSONUtils;

import org.json.JSONObject;

/**
 * Created by chenmch on 2017/11/16.
 */

public class VolumeFile {
    private String type;
    private String name;
    private String resource;
    private String parent;
    private long creationDate;
    private long lastUpdate;
    private String privilege;
    private String format;
    private long size;

    public VolumeFile(){}

    public VolumeFile(JSONObject object){
        this.type = JSONUtils.getString(object,"type","");
        this.name = JSONUtils.getString(object,"name","");
        this.resource = JSONUtils.getString(object,"resource","");
        this.parent = JSONUtils.getString(object,"parent","");
        this.creationDate = JSONUtils.getLong(object,"creationDate",0L);
        this.lastUpdate = JSONUtils.getLong(object,"lastUpdate",0L);
        this.privilege = JSONUtils.getString(object,"privilege","");
        this.format = JSONUtils.getString(object,"format","");
        this.size = JSONUtils.getLong(object,"size",0L);
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
}
