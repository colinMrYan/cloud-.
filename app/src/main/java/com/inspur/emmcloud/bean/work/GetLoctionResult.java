package com.inspur.emmcloud.bean.work;

import com.inspur.emmcloud.bean.schedule.meeting.OfficeLocation;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

public class GetLoctionResult {
    private ArrayList<OfficeLocation> locationList = new ArrayList<OfficeLocation>();

    public GetLoctionResult(String response) {
        try {
            JSONArray array = new JSONArray(response);
            for (int i = 0; i < array.length(); i++) {
                JSONObject jsonLocation = null;
                jsonLocation = array.getJSONObject(i);
                locationList.add(new OfficeLocation(jsonLocation));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public ArrayList<OfficeLocation> getLocList() {
        return locationList;
    }

    public void setLocList(ArrayList<OfficeLocation> locList) {
        this.locationList = locList;
    }

}
