/**
 * OfficeBuilding.java
 * classes : com.inspur.emmcloud.bean.schedule.meeting.OfficeBuilding
 * V 1.0.0
 * Create at 2016年10月14日 下午4:40:54
 */
package com.inspur.emmcloud.bean.schedule.meeting;

import com.inspur.emmcloud.util.common.JSONUtils;

import org.json.JSONObject;

import java.io.Serializable;

public class Building implements Serializable {
    private String id = "";
    private String name = "";
    private MeetingLocation location;

    public Building() {

    }

    public Building(JSONObject obj) {
        id = JSONUtils.getString(obj, "id", "");
        name = JSONUtils.getString(obj, "name", "");
        JSONObject officeLocationObj = JSONUtils.getJSONObject(obj, "location", new JSONObject());
        location = new MeetingLocation(officeLocationObj);
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

    public MeetingLocation getLocation() {
        return location;
    }

    public void setLocation(MeetingLocation location) {
        this.location = location;
    }
}
