package com.inspur.emmcloud.bean.appcenter.volume;

import com.inspur.emmcloud.MyApplication;
import com.inspur.emmcloud.baselib.util.JSONUtils;

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
    private long quotaTotal;
    private long quotaUsed;
    private long creationDate;

    public Volume() {
    }

    public Volume(String volumeJSon) {
        this(JSONUtils.getJSONObject(volumeJSon));
    }

    public Volume(JSONObject obj) {
        id = JSONUtils.getString(obj, "id", "");
        name = JSONUtils.getString(obj, "name", "");
        type = JSONUtils.getString(obj, "type", "");
        owner = JSONUtils.getString(obj, "owner", "");
        JSONObject quotaObj = JSONUtils.getJSONObject(obj, "quota", new JSONObject());
        quotaTotal = JSONUtils.getLong(quotaObj, "total", 0L);
        quotaUsed = JSONUtils.getLong(quotaObj, "used", 0L);
        creationDate = JSONUtils.getLong(obj, "creationDate", 0L);
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

    public long getQuotaTotal() {
        return quotaTotal;
    }

    public void setQuotaTotal(long quotaTotal) {
        this.quotaTotal = quotaTotal;
    }

    public long getQuotaUsed() {
        return quotaUsed;
    }

    public void setQuotaUsed(long quotaUsed) {
        this.quotaUsed = quotaUsed;
    }

    public boolean isOwner() {
        return MyApplication.getInstance().getUid().equals(owner);
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    public long getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(long creationDate) {
        this.creationDate = creationDate;
    }

    public boolean equals(Object other) { // 重写equals方法，后面最好重写hashCode方法

        if (this == other) // 先检查是否其自反性，后比较other是否为空。这样效率高
            return true;
        if (other == null)
            return false;
        if (!(other instanceof Volume))
            return false;

        final Volume otherVolume = (Volume) other;
        return getId().equals(otherVolume.getId());
    }
}
