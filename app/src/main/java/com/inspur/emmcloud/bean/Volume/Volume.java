package com.inspur.emmcloud.bean.Volume;

import com.inspur.emmcloud.util.JSONUtils;

import org.json.JSONObject;

import java.io.Serializable;

/**
 * Created by chenmch on 2017/11/15.
 */

public class Volume implements Serializable {
    private String id;
    private String name;
    private String type;
    private String owner;
    private long maxSize;
    private long usedSize;

    public Volume() {
    }

    public Volume(JSONObject obj) {
        id = JSONUtils.getString(obj, "id", "");
        name = JSONUtils.getString(obj, "name", "");
        type = JSONUtils.getString(obj, "type", "");
        owner = JSONUtils.getString(obj, "owner", "");
        maxSize = JSONUtils.getLong(obj, "maxSize", 0L);
        usedSize = JSONUtils.getLong(obj, "usedSize", 0L);
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    public long getMaxSize() {
        return maxSize;
    }

    public void setMaxSize(long maxSize) {
        this.maxSize = maxSize;
    }

    public long getUserdSize() {
        return usedSize;
    }

    public void setUserdSize(long userdSize) {
        this.usedSize = userdSize;
    }
}
