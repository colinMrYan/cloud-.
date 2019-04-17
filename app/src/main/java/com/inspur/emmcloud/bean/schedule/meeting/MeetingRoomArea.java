package com.inspur.emmcloud.bean.schedule.meeting;

import com.inspur.emmcloud.util.common.JSONUtils;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

public class MeetingRoomArea {
    private String id = "";
    private String name = "";
    private String location = "";
    private ArrayList<MeetingRoom> meetingRoomList = new ArrayList<>();

    public MeetingRoomArea(JSONObject obj) {
        id = JSONUtils.getString(obj, "id", "");
        name = JSONUtils.getString(obj, "name", "");
        location = JSONUtils.getString(obj, "location", "");
        JSONArray array = JSONUtils.getJSONArray(obj, "rooms", new JSONArray());
        for (int i = 0; i < array.length(); i++) {
            meetingRoomList.add(new MeetingRoom(JSONUtils.getJSONObject(array, i, new JSONObject())));
        }
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getLocation() {
        return location;
    }

    public ArrayList<MeetingRoom> getMeetingRoomList() {
        return meetingRoomList;
    }

}
