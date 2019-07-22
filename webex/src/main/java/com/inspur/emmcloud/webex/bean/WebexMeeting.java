package com.inspur.emmcloud.webex.bean;

import com.inspur.emmcloud.baselib.util.JSONUtils;
import com.inspur.emmcloud.baselib.util.TimeUtils;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

/**
 * Created by chenmch on 2018/10/12.
 */

public class WebexMeeting implements Serializable {
    private String meetingID;
    private String confName;
    private String meetingPassword;
    private String hostUserName;
    private String hostKey;
    private Calendar startDateCalendar;
    private int duration;
    // private List<String> attendeesList;
    private List<WebexAttendees> webexAttendeesList = new ArrayList<>();
    private String hostWebExID;
    private boolean inProgress;

    public WebexMeeting() {

    }

    public WebexMeeting(String response) {
        this(JSONUtils.getJSONObject(response));
    }

    public WebexMeeting(JSONObject obj) {
        meetingID = JSONUtils.getString(obj, "meetingID", "");
        confName = JSONUtils.getString(obj, "confName", "");
        meetingPassword = JSONUtils.getString(obj, "meetingPassword", "");
        hostUserName = JSONUtils.getString(obj, "hostUserName", "");
        hostKey = JSONUtils.getString(obj, "hostKey", "");
        String startDate = JSONUtils.getString(obj, "startDate", "");
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        startDateCalendar = TimeUtils.timeString2Calendar(startDate, simpleDateFormat);
        duration = JSONUtils.getInt(obj, "duration", 0);
        JSONArray array = JSONUtils.getJSONArray(obj, "attendees", new JSONArray());
        for (int i = 0; i < array.length(); i++) {
            WebexAttendees webexAttendees = new WebexAttendees(JSONUtils.getJSONObject(array, i, new JSONObject()));
            webexAttendeesList.add(webexAttendees);
        }
        meetingID = JSONUtils.getString(obj, "meetingID", "");
        hostWebExID = JSONUtils.getString(obj, "hostWebExID", "");
        inProgress = JSONUtils.getBoolean(obj, "inProgress", false);
    }

    public String getMeetingID() {
        return meetingID;
    }

    public void setMeetingID(String meetingID) {
        this.meetingID = meetingID;
    }

    public String getConfName() {
        return confName;
    }

    public void setConfName(String confName) {
        this.confName = confName;
    }

    public String getMeetingPassword() {
        return meetingPassword;
    }

    public void setMeetingPassword(String meetingPassword) {
        this.meetingPassword = meetingPassword;
    }

    public String getHostUserName() {
        return hostUserName;
    }

    public void setHostUserName(String hostUserName) {
        this.hostUserName = hostUserName;
    }

    public String getHostKey() {
        return hostKey;
    }

    public void setHostKey(String hostKey) {
        this.hostKey = hostKey;
    }

    public Calendar getStartDateCalendar() {
        return startDateCalendar;
    }

    public void setStartDateCalendar(Calendar startDateCalendar) {
        this.startDateCalendar = startDateCalendar;
    }

    public String getHostWebExID() {
        return hostWebExID;
    }

    public void setHostWebExID(String hostWebExID) {
        this.hostWebExID = hostWebExID;
    }

    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    public List<WebexAttendees> getWebexAttendeesList() {
        return webexAttendeesList;
    }

    public void setWebexAttendeesList(List<WebexAttendees> webexAttendeesList) {
        this.webexAttendeesList = webexAttendeesList;
    }

    public boolean isInProgress() {
        return inProgress;
    }

    public void setInProgress(boolean inProgress) {
        this.inProgress = inProgress;
    }

    public JSONObject toJsonObject() {
        JSONObject object = new JSONObject();
        try {
            object.put("confName", confName);
            //object.put("meetingID",meetingID);
            object.put("meetingPassword", meetingPassword);
            object.put("agenda", "");
            object.put("duration", duration);
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
            String time = TimeUtils.getTime(startDateCalendar.getTimeInMillis(), simpleDateFormat);
            object.put("startDate", time);
            object.put("agenda", "");
            JSONArray array = new JSONArray();
            for (WebexAttendees webexAttendees : webexAttendeesList) {
                JSONObject attendeesObj = new JSONObject();
                attendeesObj.put("email", webexAttendees.getEmail());
                array.put(attendeesObj);
            }
            object.put("attendees", array);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return object;
    }

    public boolean equals(Object other) { // 重写equals方法，后面最好重写hashCode方法

        if (this == other) // 先检查是否其自反性，后比较other是否为空。这样效率高
            return true;
        if (other == null)
            return false;
        if (!(other instanceof WebexMeeting))
            return false;

        final WebexMeeting otherWebexMeeting = (WebexMeeting) other;
        return getMeetingID().equals(otherWebexMeeting.getMeetingID());
    }
}
