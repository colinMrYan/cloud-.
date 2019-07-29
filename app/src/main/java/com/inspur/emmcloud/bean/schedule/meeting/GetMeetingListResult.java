package com.inspur.emmcloud.bean.schedule.meeting;

import com.inspur.emmcloud.baselib.util.JSONUtils;
import com.inspur.emmcloud.bean.schedule.Schedule;
import com.inspur.emmcloud.bean.schedule.calendar.AccountType;
import com.inspur.emmcloud.bean.schedule.calendar.ScheduleCalendar;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by chenmch on 2019/4/16.
 */

public class GetMeetingListResult {
    private List<Meeting> meetingList = new ArrayList<>();

    public GetMeetingListResult(String response){
        JSONArray array = JSONUtils.getJSONArray(response,new JSONArray());
        for (int i = 0; i<array.length(); i++){
            Meeting meeting = new Meeting(JSONUtils.getJSONObject(array,i,new JSONObject()));
            meeting.setMeeting(true);
            meeting.setScheduleCalendar(AccountType.APP_MEETING.toString());
            meetingList.add(meeting);
        }
    }

    public GetMeetingListResult(String response, ScheduleCalendar scheduleCalendar) {
        JSONArray array = JSONUtils.getJSONArray(response, new JSONArray());
        for (int i = 0; i < array.length(); i++) {
            Meeting meeting = new Meeting(JSONUtils.getJSONObject(array, i, new JSONObject()));
            meeting.setMeeting(true);
            if (meeting.getType().equals(Schedule.CALENDAR_TYPE_EXCHANGE)) {
                meeting.setScheduleCalendar(scheduleCalendar.getId());
            } else if (meeting.getType().equals(Schedule.CALENDAR_TYPE_MEETING)) {
                meeting.setScheduleCalendar(AccountType.APP_MEETING.toString());
            } else {
                meeting.setScheduleCalendar(AccountType.APP_SCHEDULE.toString());
                meeting.setMeeting(false);
            }
            meetingList.add(meeting);
        }
    }

    public List<Meeting> getMeetingList() {
        return meetingList;
    }

    public void setMeetingList(List<Meeting> meetingList) {
        this.meetingList = meetingList;
    }
}
