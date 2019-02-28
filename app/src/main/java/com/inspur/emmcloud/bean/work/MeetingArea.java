package com.inspur.emmcloud.bean.work;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

public class MeetingArea {
    private String areaId = "";
    private String name = "";
    private String location = "";

    private ArrayList<MeetingRoom> meetingRooms = new ArrayList<MeetingRoom>();

    public MeetingArea() {
    }

    public MeetingArea(JSONObject jsonObject) {
        try {
            if (jsonObject.has("id")) {
                this.areaId = jsonObject.getString("id");
            }
            if (jsonObject.has("name")) {
                this.name = jsonObject.getString("name");
            }
            if (jsonObject.has("location")) {
                this.location = jsonObject.getString("location");
            }
            if (jsonObject.has("rooms")) {
                String rooms = jsonObject.getString("rooms");
                JSONArray jsonArray = new JSONArray(rooms);
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject jsonRoom = jsonArray.getJSONObject(i);
                    meetingRooms.add(new MeetingRoom(jsonRoom));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public String getAreaId() {
        return areaId;
    }

    public String getName() {
        return name;
    }

    public String getLocation() {
        return location;
    }

    public ArrayList<MeetingRoom> getMeetingRooms() {
        return meetingRooms;
    }

    public void setMeetingRooms(ArrayList<MeetingRoom> meetingRooms) {
        this.meetingRooms = meetingRooms;
    }
}
