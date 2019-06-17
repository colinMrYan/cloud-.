package com.inspur.emmcloud.bean.schedule;

import com.inspur.emmcloud.baselib.util.JSONUtils;
import com.inspur.emmcloud.bean.schedule.meeting.Meeting;
import com.inspur.emmcloud.bean.schedule.task.Task;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by chenmch on 2019/4/11.
 */

public class GetScheduleListResult {
    private List<Schedule> scheduleList = new ArrayList<>();
    private List<Meeting> meetingList = new ArrayList<>();
    private List<Task> taskList = new ArrayList<>();
    private boolean isScheduleForward = false;
    private boolean isMeetingForward = false;
    private boolean isTaskForward = false;

    public GetScheduleListResult(String response) {
        String scheduleJson = JSONUtils.getString(response, "calendar", "");
        String meetingJson = JSONUtils.getString(response, "meeting", "");
        String taskJson = JSONUtils.getString(response, "task", "");
        String scheduleCommand = JSONUtils.getString(scheduleJson, "command", "");
        String meetingCommand = JSONUtils.getString(meetingJson, "command", "");
        String taskCommand = JSONUtils.getString(taskJson, "command", "");
        if (scheduleCommand.equals("FORWARD")) {
            isScheduleForward = true;
            String array = JSONUtils.getString(scheduleJson, "list", "[]");
            scheduleList = JSONUtils.parseArray(array, Schedule.class);
        }
        if (meetingCommand.equals("FORWARD")) {
            isMeetingForward = true;
            String array = JSONUtils.getString(meetingJson, "list", "[]");
            meetingList = JSONUtils.parseArray(array, Meeting.class);
        }
        if (taskCommand.equals("FORWARD")) {
            isTaskForward = true;
            String array = JSONUtils.getString(taskJson, "list", "[]");
            taskList = JSONUtils.parseArray(array, Task.class);
        }

    }

    public List<Schedule> getScheduleList() {
        return scheduleList;
    }

    public void setScheduleList(List<Schedule> scheduleList) {
        this.scheduleList = scheduleList;
    }

    public List<Meeting> getMeetingList() {
        return meetingList;
    }

    public void setMeetingList(List<Meeting> meetingList) {
        this.meetingList = meetingList;
    }

    public List<Task> getTaskList() {
        return taskList;
    }

    public void setTaskList(List<Task> taskList) {
        this.taskList = taskList;
    }

    public boolean isScheduleForward() {
        return isScheduleForward;
    }

    public boolean isMeetingForward() {
        return isMeetingForward;
    }

    public boolean isTaskForward() {
        return isTaskForward;
    }

    public boolean isForward() {
        return isScheduleForward || isMeetingForward || isTaskForward;
    }
}
