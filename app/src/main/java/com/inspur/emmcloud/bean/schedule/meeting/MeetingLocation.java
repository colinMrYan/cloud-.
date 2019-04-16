package com.inspur.emmcloud.bean.schedule.meeting;

import com.inspur.emmcloud.util.common.JSONUtils;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class MeetingLocation implements Serializable {
    private String id = "";
    private String name = "";
    private List<Building> officeBuildingList = new ArrayList<>();

    public MeetingLocation(){

    }

    public MeetingLocation(JSONObject obj) {
        id = JSONUtils.getString(obj,"id","");
        name = JSONUtils.getString(obj,"name","");
        JSONArray array = JSONUtils.getJSONArray(obj,"buildings",new JSONArray());
        for (int i=0;i<array.length();i++){
            Building officeBuilding = new Building(JSONUtils.getJSONObject(array,i,new JSONObject()));
            officeBuildingList.add(officeBuilding);
        }
    }

    public void setOfficeBuildingList(List<Building> officeBuildingList) {
        this.officeBuildingList = officeBuildingList;
    }

    public List<Building> getOfficeBuildingList() {
        return officeBuildingList;
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

}