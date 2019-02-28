package com.inspur.emmcloud.bean.work;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

public class GetMeetingListResult {

    private static final String TAG = "GetMeetingListResult";

    private ArrayList<Meeting> meetings = new ArrayList<Meeting>();

    public GetMeetingListResult(String response) {
        try {
            JSONArray jsonArray = new JSONArray(response);
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject obj = jsonArray.getJSONObject(i);
                meetings.add(new Meeting(obj));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public ArrayList<Meeting> getMeetingsList() {
        return meetings;
    }

}
