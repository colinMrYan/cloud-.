/**
 * OfficeBuilding.java
 * classes : com.inspur.emmcloud.bean.schedule.meeting.OfficeBuilding
 * V 1.0.0
 * Create at 2016年10月14日 下午4:40:54
 */
package com.inspur.emmcloud.bean.schedule.meeting;

import com.inspur.emmcloud.util.common.JSONUtils;

import org.json.JSONObject;

public class OfficeBuilding {
    private String id = "";
    private String name = "";

    public OfficeBuilding(JSONObject obj) {
        id = JSONUtils.getString(obj,"id","");
        name = JSONUtils.getString(obj,"name","");
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }
}
