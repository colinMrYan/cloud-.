package com.inspur.emmcloud.bean.schedule.meeting;


import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

public class GetLocationResult {
    private ArrayList<MeetingLocation> locationList = new ArrayList<MeetingLocation>();

    public GetLocationResult(String response) {
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


}
