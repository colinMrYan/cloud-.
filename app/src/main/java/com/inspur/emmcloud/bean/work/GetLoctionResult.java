package com.inspur.emmcloud.bean.work;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

public class GetLoctionResult {
    private ArrayList<MeetingLocation> locationList = new ArrayList<MeetingLocation>();

    public GetLoctionResult(String response) {
        try {
            JSONArray array = new JSONArray(response);
            for (int i = 0; i < array.length(); i++) {
                JSONObject jsonLocation = null;
                jsonLocation = array.getJSONObject(i);
                locationList.add(new MeetingLocation(jsonLocation));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public ArrayList<MeetingLocation> getLocList() {
        return locationList;
    }

    public void setLocList(ArrayList<MeetingLocation> locList) {
        this.locationList = locList;
    }

}
