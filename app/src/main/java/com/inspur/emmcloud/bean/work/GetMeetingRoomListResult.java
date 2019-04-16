package com.inspur.emmcloud.bean.work;

import com.inspur.emmcloud.bean.schedule.meeting.MeetingRoomArea;
import com.inspur.emmcloud.util.common.JSONUtils;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

public class GetMeetingRoomListResult {

    private ArrayList<MeetingRoomArea> meetingRoomAreaList = new ArrayList<>();

    public GetMeetingRoomListResult(String response) {
        JSONArray array = JSONUtils.getJSONArray(response, new JSONArray());
        for (int i = 0; i < array.length(); i++) {
            meetingRoomAreaList.add(new MeetingRoomArea(JSONUtils.getJSONObject(array, i, new JSONObject())));
        }
    }

    public ArrayList<MeetingRoomArea> getMeetingRoomAreaList() {
        return meetingRoomAreaList;
    }

}