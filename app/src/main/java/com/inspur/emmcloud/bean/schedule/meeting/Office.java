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
    private Building officeBuilding ;

    public Office(JSONObject obj){
        id = JSONUtils.getString(obj,"id","");
        floor = JSONUtils.getString(obj,"floor","");
        name = JSONUtils.getString(obj,"name","");
        JSONObject officeBuildingObj = JSONUtils.getJSONObject(obj,"building",new JSONObject());
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


}
