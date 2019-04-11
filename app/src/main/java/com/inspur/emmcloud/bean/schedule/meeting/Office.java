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
    private OfficeBuilding officeBuilding ;
    private OfficeLocation officeLocation;
    public Office(JSONObject obj){
        id = JSONUtils.getString(obj,"id","");
        floor = JSONUtils.getString(obj,"floor","");
        name = JSONUtils.getString(obj,"name","");
        JSONObject officeBuildingObj = JSONUtils.getJSONObject(obj,"building",new JSONObject());
        officeBuilding = new OfficeBuilding(officeBuildingObj);
        JSONObject officeLocationObj = JSONUtils.getJSONObject(officeBuildingObj,"location",new JSONObject());
        officeLocation = new OfficeLocation(officeLocationObj);
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

    public OfficeBuilding getOfficeBuilding() {
        return officeBuilding;
    }

    public void setOfficeBuilding(OfficeBuilding officeBuilding) {
        this.officeBuilding = officeBuilding;
    }

    public OfficeLocation getOfficeLocation() {
        return officeLocation;
    }

    public void setOfficeLocation(OfficeLocation officeLocation) {
        this.officeLocation = officeLocation;
    }
}
