package com.inspur.emmcloud.bean.schedule;


import com.inspur.emmcloud.bean.work.RemindEvent;
import com.inspur.emmcloud.util.common.JSONUtils;
import com.inspur.emmcloud.util.privates.TimeUtils;
import com.inspur.emmcloud.widget.calendardayview.Event;

import org.json.JSONException;
import org.json.JSONObject;
import org.xutils.db.annotation.Column;
import org.xutils.db.annotation.Table;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

/**
 * Created by chenmch on 2019/4/6.
 */
@Table(name = "Schedule")
public class Schedule implements Serializable{
    @Column(name = "id", isId = true)
    private String id="";// 唯一标识
    @Column(name = "title")
    private String title="" ;//日程标题
    @Column(name = "type")
    private String type= "";//日程类型（出差、会议等，可以自定义）
    @Column(name = "owner")
    private String owner="";//创建人的inspurId
    @Column(name = "startTime")
    private long startTime=0L;//开始时间
    @Column(name = "endTime")
    private long endTime=0L;// 结束时间
    @Column(name = "creationTime")
    private long creationTime=0L;//创建时间
    @Column(name = "lastTime")
    private long lastTime=0L;// 最后修改时间
    @Column(name = "isAllDay")
    private Boolean isAllDay=false;//是否全天
    @Column(name = "isCommunity")
    private Boolean isCommunity=false;//是否公开（别人可以关注你的日程，此属性决定了当前日程是否对别人可见）
    @Column(name = "syncToLocal")
    private Boolean syncToLocal=false ;//是否将日程信息同步到 移动设备日历里边。
    @Column(name = "remindEvent")
    private String remindEvent="";
    @Column(name = "state")
    private int state=-1;//日程的状态，客户端暂时可忽略该属性
    @Column(name = "location")
    private String location="";
    @Column(name = "note")
    private String note="";
    @Column(name = "participants")
    private String participants="";

    public Schedule(){

    }
    public Schedule(JSONObject object){
        id = JSONUtils.getString(object,"id","");
        title  = JSONUtils.getString(object,"title ","");
        type = JSONUtils.getString(object,"type","");
        owner = JSONUtils.getString(object,"owner","");
        startTime = JSONUtils.getLong(object,"startTime",0L);
        endTime = JSONUtils.getLong(object,"endTime",0L);
        creationTime = JSONUtils.getLong(object,"creationTime",0L);
        lastTime = JSONUtils.getLong(object,"lastTime",0L);
        isAllDay = JSONUtils.getBoolean(object,"isAllDay",false);
        isCommunity = JSONUtils.getBoolean(object,"isCommunity",false);
        syncToLocal = JSONUtils.getBoolean(object,"syncToLocal",false);
        remindEvent = JSONUtils.getString(object,"remindEvent","");
        state = JSONUtils.getInt(object,"state",-1);
        location  = JSONUtils.getString(object,"location","");
        participants  = JSONUtils.getString(object,"participants ","");
        note = JSONUtils.getString(object,"note","");
    }


    private List<String> getParticipantList = new ArrayList<>();

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    public long getStartTime() {
        return startTime;
    }

    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }

    public long getEndTime() {
        return endTime;
    }

    public void setEndTime(long endTime) {
        this.endTime = endTime;
    }

    public long getCreationTime() {
        return creationTime;
    }

    public void setCreationTime(long creationTime) {
        this.creationTime = creationTime;
    }

    public long getLastTime() {
        return lastTime;
    }

    public void setLastTime(long lastTime) {
        this.lastTime = lastTime;
    }

    public Calendar getStartTimeCalendar() {
        return TimeUtils.timeLong2Calendar(startTime);
    }


    public Calendar getEndTimeCalendar() {
        return TimeUtils.timeLong2Calendar(endTime);
    }


    public Calendar getCreationTimeCalendar() {
        return TimeUtils.timeLong2Calendar(creationTime);
    }


    public Calendar getLastTimeCalendar() {
        return TimeUtils.timeLong2Calendar(lastTime);
    }


    public Boolean getAllDay() {
        return isAllDay;
    }

    public void setAllDay(Boolean allDay) {
        isAllDay = allDay;
    }

    public Boolean getCommunity() {
        return isCommunity;
    }

    public void setCommunity(Boolean community) {
        isCommunity = community;
    }

    public Boolean getSyncToLocal() {
        return syncToLocal;
    }

    public void setSyncToLocal(Boolean syncToLocal) {
        this.syncToLocal = syncToLocal;
    }

    public String getRemindEvent() {
        return remindEvent;
    }

    public void setRemindEvent(String remindEvent) {
        this.remindEvent = remindEvent;
    }

    public RemindEvent getRemindEventObj() {
        return new RemindEvent(remindEvent);
    }


    public int getState() {
        return state;
    }

    public void setState(int state) {
        this.state = state;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public Location getScheduleLocationObj() {
        return new Location(location);
    }


    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public String getParticipants() {
        return participants;
    }

    public void setParticipants(String participants) {
        this.participants = participants;
    }

    public List<String> getGetParticipantList() {
        return JSONUtils.parseArray(participants,String.class);
    }

    public static List<Event> calendarEvent2EventList(List<Schedule> scheduleList, Calendar selectCalendar) {
        List<Event> eventList = new ArrayList<>();
        for (Schedule schedule : scheduleList) {
            Calendar scheduleStartTime =  schedule.getStartTimeCalendar();
            Calendar scheduleEndTime = schedule.getEndTimeCalendar();
            if (TimeUtils.isContainTargentCalendarDay(selectCalendar, scheduleStartTime, scheduleEndTime)) {
                Calendar dayBeginCalendar = TimeUtils.getDayBeginCalendar(selectCalendar);
                Calendar dayEndCalendar = TimeUtils.getDayEndCalendar(selectCalendar);
                if (scheduleStartTime.before(dayBeginCalendar)) {
                    scheduleStartTime = dayBeginCalendar;
                }
                if (scheduleEndTime.after(dayEndCalendar)) {
                    scheduleEndTime = dayEndCalendar;
                }
                Event event = new Event(schedule.getId(), Event.TYPE_CALENDAR,schedule.getTitle(),schedule.getScheduleLocationObj().getDisplayName(), scheduleStartTime, scheduleEndTime,schedule);
                event.setAllDay(schedule.getAllDay());
                eventList.add(event);
            }
        }
        return eventList;
    }

    public static JSONObject getCalendarEventJson() throws JSONException {
        JSONObject jsonObject= new JSONObject();
        jsonObject.put("id","");
        jsonObject.put("title","");
        jsonObject.put("type","");
        jsonObject.put("owner","");
        jsonObject.put("startTime","");
        jsonObject.put("endTime","");
        jsonObject.put("creationTime","");
        jsonObject.put("lastTime","");
        jsonObject.put("isAllDay","");
        jsonObject.put("isCommunity","");
        jsonObject.put("syncToLocal","");
        jsonObject.put("remindEvent","");
        jsonObject.put("state","");
        jsonObject.put("location","");
        jsonObject.put("participants","");
        return jsonObject;
    }

}
