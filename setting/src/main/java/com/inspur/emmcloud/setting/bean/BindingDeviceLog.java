package com.inspur.emmcloud.setting.bean;

import com.inspur.emmcloud.baselib.util.JSONUtils;

import org.json.JSONObject;

import java.io.Serializable;

/**
 * Created by Administrator on 2017/5/10.
 */

public class BindingDeviceLog implements Serializable {
    private String id;
    private String desc;
    private long time;

    public BindingDeviceLog() {

    }

    public BindingDeviceLog(JSONObject obj) {
        id = JSONUtils.getString(obj, "_id", "");
        desc = JSONUtils.getString(obj, "desc", "");
        time = JSONUtils.getLong(obj, "createTime", 0L);

    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }
}
