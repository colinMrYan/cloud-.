package com.inspur.emmcloud.bean.schedule;

import com.inspur.emmcloud.util.common.JSONUtils;

import org.json.JSONObject;

/**
 * Created by chenmch on 2019/4/6.
 */

public class Location {
    private String id;//可选的， 地点唯一标识，如果没有ID ，表示地点是手输的
    private String building;  //楼号
    private String displayName;// 会议室名，自定义名称存放在 displayName 上

    public Location(String json) {
        this(JSONUtils.getJSONObject(json));
    }

    public Location(JSONObject obj) {
        id = JSONUtils.getString(obj, "id", "");
        building = JSONUtils.getString(obj, "building", "");
        displayName = JSONUtils.getString(obj, "displayName", "");
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
    }

    public String getBuilding() {
        return building;
    }

    public void setBuilding(String building) {
        this.building = building;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }
}
