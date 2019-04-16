package com.inspur.emmcloud.bean.schedule.meeting;

import com.inspur.emmcloud.util.common.JSONUtils;

import org.json.JSONObject;

/**
 * Created by chenmch on 2019/4/10.
 */

public class Office {
    private String id = "";
    private String floor = "";
    private String name = "";
    private Building officeBuilding;

    public Office(String response) {
        this(JSONUtils.getJSONObject(response));
    }

    public Office(JSONObject obj) {
        id = JSONUtils.getString(obj, "id", "");
        floor = JSONUtils.getString(obj, "floor", "");
        name = JSONUtils.getString(obj, "name", "");
        JSONObject officeBuildingObj = JSONUtils.getJSONObject(obj, "building", new JSONObject());
        officeBuilding = new Building(officeBuildingObj);
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getFloor() {
        return floor;
    }

    public void setFloor(String floor) {
        this.floor = floor;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Building getOfficeBuilding() {
        return officeBuilding;
    }

    public void setOfficeBuilding(Building officeBuilding) {
        this.officeBuilding = officeBuilding;
    }

    /*
                           * 重写equals方法修饰符必须是public,因为是重写的Object的方法. 2.参数类型必须是Object.
                           */
    public boolean equals(Object other) { // 重写equals方法，后面最好重写hashCode方法

        if (this == other) // 先检查是否其自反性，后比较other是否为空。这样效率高
            return true;
        if (other == null)
            return false;
        if (!(other instanceof Office))
            return false;

        final Office otherOffice = (Office) other;
        return getId().equals(otherOffice.getId());
    }

}
