package com.inspur.emmcloud.bean.appcenter.webex;

import com.inspur.emmcloud.bean.chat.Message;
import com.inspur.emmcloud.util.common.JSONUtils;
import com.inspur.emmcloud.util.privates.TimeUtils;

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

public class WebexMeeting implements Serializable{
    private String meetingID;
    private String confName;
    private String meetingPassword;
    private String hostUserName;
    private String hostKey;
    private Calendar startDateCalendar;
    private int duration;
    private List<String> attendeesList;
    private String hostWebExID;
    public WebexMeeting(){

    }

    public WebexMeeting(String response){
        this(JSONUtils.getJSONObject(response));
    }
    public WebexMeeting(JSONObject obj){
        meetingID = JSONUtils.getString(obj,"meetingID","");
        confName = JSONUtils.getString(obj,"confName","");
        meetingPassword = JSONUtils.getString(obj,"meetingPassword","");
        hostUserName = JSONUtils.getString(obj,"hostUserName","");
        hostKey = JSONUtils.getString(obj,"hostKey","");
        String startDate = JSONUtils.getString(obj,"startDate","");
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        startDateCalendar = TimeUtils.timeString2Calendar(startDate,simpleDateFormat);
        duration = JSONUtils.getInt(obj,"duration",0);
        attendeesList =  JSONUtils.getStringList(obj,"attendees",new ArrayList<String>());
        meetingID = JSONUtils.getString(obj,"meetingID","");
        hostWebExID = JSONUtils.getString(obj,"hostWebExID","");
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

    public List<String> getAttendeesList() {
        return attendeesList;
    }

    public void setAttendeesList(List<String> attendeesList) {
        this.attendeesList = attendeesList;
    }

    public JSONObject toJsonObject(){
        JSONObject object = new JSONObject();
        try {
            object.put("confName",confName);
            //object.put("meetingID",meetingID);
            object.put("meetingPassword",meetingPassword);
            object.put("agenda","");
            object.put("duration",duration);
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
            String time = TimeUtils.getTime(startDateCalendar.getTimeInMillis(),simpleDateFormat);
            object.put("startDate",time);
            object.put("agenda","");
            JSONArray array = new JSONArray();
            for (String attendees:attendeesList){
                array.put(attendees);
            }
            object.put("attendees",array);
        }catch (Exception e){
            e.printStackTrace();
        }
        return  object;
    }

    public boolean equals(Object other) { // 重写equals方法，后面最好重写hashCode方法

        if (this == other) // 先检查是否其自反性，后比较other是否为空。这样效率高
            return true;
        if (other == null)
            return false;
        if (!(other instanceof Message))
            return false;

        final WebexMeeting otherWebexMeeting = (WebexMeeting) other;
        return getMeetingID().equals(otherWebexMeeting.getMeetingID());
    }
}
