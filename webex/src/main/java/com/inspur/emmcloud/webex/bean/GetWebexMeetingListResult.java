package com.inspur.emmcloud.webex.bean;

import com.inspur.emmcloud.baselib.util.JSONUtils;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by chenmch on 2018/10/12.
 */

public class GetWebexMeetingListResult {
    private List<WebexMeeting> webexMeetingList = new ArrayList<>();

    public GetWebexMeetingListResult(String response) {
        JSONArray array = JSONUtils.getJSONArray(response, new JSONArray());
        for (int i = 0; i < array.length(); i++) {
            JSONObject obj = JSONUtils.getJSONObject(array, i, new JSONObject());
            WebexMeeting webexMeeting = new WebexMeeting(obj);
            webexMeetingList.add(webexMeeting);
        }
    }

    public List<WebexMeeting> getWebexMeetingList() {
        return webexMeetingList;
    }

    public void setWebexMeetingList(List<WebexMeeting> webexMeetingList) {
        this.webexMeetingList = webexMeetingList;
    }
}
