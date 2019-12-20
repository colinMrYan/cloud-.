package com.inspur.emmcloud.schedule.bean;

import com.inspur.emmcloud.baselib.util.JSONUtils;
import com.inspur.emmcloud.schedule.bean.calendar.AccountType;
import com.inspur.emmcloud.schedule.bean.calendar.ScheduleCalendar;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by chenmch on 2019/4/11.
 */

public class GetScheduleListResult {
    private List<Schedule> scheduleList = new ArrayList<>();
    private boolean isForward = false;
    private boolean isExchangeAccount = false;

    public GetScheduleListResult(String response, ScheduleCalendar scheduleCalendar) {
        isExchangeAccount = (scheduleCalendar != null && scheduleCalendar.getAcType().equals(AccountType.EXCHANGE.toString()));
        if (isExchangeAccount) {
            JSONObject object = JSONUtils.getJSONObject(response, "exCalendar", new JSONObject());
            String command = JSONUtils.getString(object, "command", "");
            isForward = command.equals("FORWARD");
            String array = JSONUtils.getString(object, "list", "[]");
            scheduleList = JSONUtils.parseArray(array, Schedule.class);
            for (Schedule schedule : scheduleList) {
                schedule.setScheduleCalendar(scheduleCalendar.getId());
            }
        } else {
            isForward = true;
            String scheduleJson = JSONUtils.getString(response, "calendar", "");
            String meetingJson = JSONUtils.getString(response, "meeting", "");
            String scheduleCommand = JSONUtils.getString(scheduleJson, "command", "");
            String meetingCommand = JSONUtils.getString(meetingJson, "command", "");
            if (scheduleCommand.equals("FORWARD")) {
                String array = JSONUtils.getString(scheduleJson, "list", "[]");
                scheduleList = JSONUtils.parseArray(array, Schedule.class);
            }
            if (meetingCommand.equals("FORWARD")) {
                String array = JSONUtils.getString(meetingJson, "list", "[]");
                List<Schedule> meetingScheduleList = JSONUtils.parseArray(array, Schedule.class);
                scheduleList.addAll(meetingScheduleList);
            }

        }

    }

    public List<Schedule> getScheduleList() {
        return scheduleList;
    }

    public void setScheduleList(List<Schedule> scheduleList) {
        this.scheduleList = scheduleList;
    }

    public boolean isForward() {
        return isForward;
    }
}
