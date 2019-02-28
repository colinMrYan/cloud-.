package com.inspur.emmcloud.bean.work;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

public class GetMeetingRoomsResult {

    private JSONArray meetingRoomsArray;
    private ArrayList<MeetingArea> meetingAreas = new ArrayList<MeetingArea>();

    public GetMeetingRoomsResult(String response) {
        try {
            meetingRoomsArray = new JSONArray(response);
            for (int i = 0; i < meetingRoomsArray.length(); i++) {
                JSONObject obj = meetingRoomsArray.getJSONObject(i);
                meetingAreas.add(new MeetingArea(obj));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public ArrayList<MeetingArea> getMeetingAreas() {
        return meetingAreas;
    }

}