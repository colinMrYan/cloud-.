package com.inspur.emmcloud.bean.schedule.meeting;

import com.inspur.emmcloud.util.common.JSONUtils;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class OfficeLocation {
    private String id = "";
    private String name = "";
    private List<OfficeBuilding> officeBuildingList = new ArrayList<>();

    public OfficeLocation(JSONObject obj) {
        id = JSONUtils.getString(obj,"id","");
        name = JSONUtils.getString(obj,"name","");
        JSONArray array = JSONUtils.getJSONArray(obj,"buildings",new JSONArray());
        for (int i=0;i<array.length();i++){
            OfficeBuilding officeBuilding = new OfficeBuilding(JSONUtils.getJSONObject(array,i,new JSONObject()));
            officeBuildingList.add(officeBuilding);
        }
    }

    public List<OfficeBuilding> getOfficeBuildingList() {
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