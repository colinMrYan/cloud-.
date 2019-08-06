package com.inspur.emmcloud.bean.schedule;


import com.inspur.emmcloud.MyApplication;
import com.inspur.emmcloud.baselib.util.JSONUtils;
import com.inspur.emmcloud.baselib.util.StringUtils;
import com.inspur.emmcloud.baselib.util.TimeUtils;
import com.inspur.emmcloud.basemodule.application.BaseApplication;
import com.inspur.emmcloud.basemodule.config.Constant;
import com.inspur.emmcloud.basemodule.util.PreferencesByUserAndTanentUtils;
import com.inspur.emmcloud.basemodule.util.WebServiceRouterManager;
import com.inspur.emmcloud.bean.schedule.calendar.AccountType;
import com.inspur.emmcloud.widget.calendardayview.Event;

import org.json.JSONArray;
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
public class Schedule implements Serializable {
    public static final String TYPE_MEETING = "schedule_meeting";
    public static final String TYPE_CALENDAR = "schedule_calendar";
    public static final String TYPE_UNKNOWN = "schedule_unknown";
    public static final String TYPE_TASK = "schedule_task";
    public static final String CALENDAR_TYPE_MEETING = "meeting";
    public static final String CALENDAR_TYPE_MY_CALENDAR = "default";
    public static final String CALENDAR_TYPE_EXCHANGE = "exchange";
    public static final String CALENDAR_TYPE_WEBEX = "webex";
    @Column(name = "id", isId = true)
    private String id = "";// 唯一标识
    @Column(name = "title")
    private String title = "";//日程标题
    @Column(name = "type")
    private String type = "";//日程类型（出差、会议等，可以自定义）
    @Column(name = "owner")
    private String owner = "";//创建人的inspurId
    @Column(name = "startTime")
    private long startTime = 0L;//开始时间
    @Column(name = "endTime")
    private long endTime = 0L;// 结束时间
    @Column(name = "creationTime")
    private long creationTime = 0L;//创建时间
    @Column(name = "lastTime")
    private long lastTime = 0L;// 最后修改时间
    @Column(name = "isAllDay")
    private Boolean isAllDay = false;//是否全天
    @Column(name = "isCommunity")
    private Boolean isCommunity = false;//是否公开（别人可以关注你的日程，此属性决定了当前日程是否对别人可见）
    @Column(name = "syncToLocal")
    private Boolean syncToLocal = false;//是否将日程信息同步到 移动设备日历里边。
    @Column(name = "remindEvent")
    private String remindEvent = "";
    @Column(name = "state")
    private int state = -1;//日程的状态，客户端暂时可忽略该属性
    @Column(name = "location")
    private String location = "";
    @Column(name = "note")
    private String note = "";
    @Column(name = "participants")
    private String participants = "";
    @Column(name = "scheduleCalendar")
    private String scheduleCalendar = "";
    @Column(name = "isMeeting")
    private boolean isMeeting = false;
    private int index = -1;
    private List<String> getParticipantList = new ArrayList<>();

    public Schedule() {

    }


    public Schedule(JSONObject object) {
        id = JSONUtils.getString(object, "id", "");
        title = JSONUtils.getString(object, "title", "");
        type = JSONUtils.getString(object, "type", "");
        owner = JSONUtils.getString(object, "owner", "");
        startTime = JSONUtils.getLong(object, "startTime", 0L);
        endTime = JSONUtils.getLong(object, "endTime", 0L);
        creationTime = JSONUtils.getLong(object, "creationTime", 0L);
        lastTime = JSONUtils.getLong(object, "lastTime", 0L);
        isAllDay = JSONUtils.getBoolean(object, "isAllDay", false);
        isCommunity = JSONUtils.getBoolean(object, "isCommunity", false);
        syncToLocal = JSONUtils.getBoolean(object, "syncToLocal", false);
        isMeeting = JSONUtils.getBoolean(object, "isMeeting", false);
        remindEvent = JSONUtils.getString(object, "remindEvent", "");
        state = JSONUtils.getInt(object, "state", -1);
        location = JSONUtils.getString(object, "location", "");
        participants = JSONUtils.getString(object, "participants", "");
        note = JSONUtils.getString(object, "note", "");
        if (!StringUtils.isBlank(type)) {
            switch (type) {
                case CALENDAR_TYPE_MY_CALENDAR:
                    scheduleCalendar = AccountType.APP_SCHEDULE.toString();
                    break;
                case CALENDAR_TYPE_MEETING:
                    scheduleCalendar = AccountType.APP_MEETING.toString();
                    break;
                default:
                    scheduleCalendar = AccountType.APP_SCHEDULE.toString();
                    break;
            }
        }
        if (StringUtils.isBlank(scheduleCalendar)) {
            scheduleCalendar = AccountType.APP_SCHEDULE.toString();
        }
    }

    public static List<Event> calendarEvent2EventList(List<Schedule> scheduleList, Calendar selectCalendar) {
        List<Event> eventList = new ArrayList<>();
        for (Schedule schedule : scheduleList) {
            Calendar scheduleStartTime = schedule.getStartTimeCalendar();
            Calendar scheduleEndTime = schedule.getEndTimeCalendar();
            if (TimeUtils.isContainTargetCalendarDay(selectCalendar, scheduleStartTime, scheduleEndTime)) {
                Calendar dayBeginCalendar = TimeUtils.getDayBeginCalendar(selectCalendar);
                Calendar dayEndCalendar = TimeUtils.getDayEndCalendar(selectCalendar);
                if (scheduleStartTime.before(dayBeginCalendar)) {
                    scheduleStartTime = dayBeginCalendar;
                }
                if (scheduleEndTime.after(dayEndCalendar)) {
                    scheduleEndTime = dayEndCalendar;
                }
                String eventType = schedule.isMeeting() ? Schedule.TYPE_MEETING : Schedule.TYPE_CALENDAR;
                Location locationObj = schedule.getScheduleLocationObj();
                String location = "";
                if (!StringUtils.isBlank(locationObj.getBuilding()) || !StringUtils.isBlank(locationObj.getDisplayName())) {
                    location = locationObj.getBuilding() + " " + locationObj.getDisplayName();
                }
                Event event = new Event(schedule.getId(), eventType, schedule.getTitle(), location, scheduleStartTime, scheduleEndTime, schedule, schedule.getType(), schedule.getOwner());
                event.setAllDay(schedule.getAllDay());
                eventList.add(event);
            }
        }
        return eventList;
    }

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

    public String getScheduleCalendar() {
        if (StringUtils.isBlank(scheduleCalendar)) {
            if (isMeeting()) {
                return AccountType.APP_MEETING.toString();
            }
            return AccountType.APP_SCHEDULE.toString();
        }
        return scheduleCalendar;
    }

    public void setScheduleCalendar(String scheduleCalendar) {
        this.scheduleCalendar = scheduleCalendar;
    }

    public boolean isMeeting() {
        return isMeeting;
    }

    public void setMeeting(boolean meeting) {
        isMeeting = meeting;
    }

    public List<Participant> getCommonParticipantList() {
        List<Participant> participantList = new ArrayList<>();
        JSONArray array = JSONUtils.getJSONArray(participants, new JSONArray());
        for (int i = 0; i < array.length(); i++) {
            Participant participant = new Participant(JSONUtils.getJSONObject(array, i, new JSONObject()));
            if (participant.getRole().equals(Participant.TYPE_COMMON)) {
                participantList.add(participant);
            }
        }
        return participantList;
    }

    public List<Participant> getRecorderParticipantList() {
        List<Participant> participantList = new ArrayList<>();
        JSONArray array = JSONUtils.getJSONArray(participants, new JSONArray());
        for (int i = 0; i < array.length(); i++) {
            Participant participant = new Participant(JSONUtils.getJSONObject(array, i, new JSONObject()));
            if (participant.getRole().equals(Participant.TYPE_RECORDER)) {
                participantList.add(participant);
            }
        }
        return participantList;
    }

    public List<Participant> getRoleParticipantList() {
        List<Participant> participantList = new ArrayList<>();
        JSONArray array = JSONUtils.getJSONArray(participants, new JSONArray());
        for (int i = 0; i < array.length(); i++) {
            Participant participant = new Participant(JSONUtils.getJSONObject(array, i, new JSONObject()));
            if (participant.getRole().equals(Participant.TYPE_CONTACT)) {
                participantList.add(participant);
            }
        }
        return participantList;
    }

    public List<Participant> getAllParticipantList() {
        List<Participant> participantList = new ArrayList<>();
        JSONArray array = JSONUtils.getJSONArray(participants, new JSONArray());
        for (int i = 0; i < array.length(); i++) {
            Participant participant = new Participant(JSONUtils.getJSONObject(array, i, new JSONObject()));
            participantList.add(participant);
        }
        return participantList;
    }

    //为了支持跨天，支持特定日期当天的会议开始时间
    public Calendar getDayStartTimeCalendar(Calendar calendar) {
        Calendar startTimeCalendar = getStartTimeCalendar();
        if (!TimeUtils.isSameDay(calendar, startTimeCalendar)) {
            return TimeUtils.getDayBeginCalendar(calendar);
        }
        return startTimeCalendar;
    }

    public Long getDayStartTime(Calendar calendar) {
        return TimeUtils.calendar2TimeLong(getDayStartTimeCalendar(calendar));
    }

    public Long getDayEndTime(Calendar calendar) {
        return TimeUtils.calendar2TimeLong(getDayEndTimeCalendar(calendar));
    }

    public Calendar getDayEndTimeCalendar(Calendar calendar) {
        Calendar endTimeCalendar = getEndTimeCalendar();
        if (!TimeUtils.isSameDay(calendar, endTimeCalendar)) {
            return TimeUtils.getDayEndCalendar(calendar);
        }
        return endTimeCalendar;
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
        return JSONUtils.parseArray(participants, String.class);
    }

    public String getCalendarEventJsonStr() throws JSONException {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("id", id);
        jsonObject.put("title", title);
        jsonObject.put("type", type);
        jsonObject.put("owner", owner);
        jsonObject.put("startTime", startTime);
        jsonObject.put("endTime", endTime);
        jsonObject.put("creationTime", creationTime);
        jsonObject.put("lastTime", lastTime);
        jsonObject.put("isAllDay", isAllDay);
        jsonObject.put("isCommunity", isCommunity);
        jsonObject.put("syncToLocal", syncToLocal);
        jsonObject.put("remindEvent", remindEvent);
        jsonObject.put("state", state);
        jsonObject.put("location", location);
        jsonObject.put("participants", participants);
        jsonObject.put("note", note);
        jsonObject.put("isMeeting", isMeeting);
        return jsonObject.toString();
    }

    public JSONObject toCalendarEventJSONObject() {
        JSONObject jsonObject = new JSONObject();
        try {
            if (!StringUtils.isBlank(id)) {
                jsonObject.put("id", id);
            }
            jsonObject.put("title", title);
            jsonObject.put("type", type);
            jsonObject.put("owner", owner);
            jsonObject.put("startTime", startTime);
            jsonObject.put("endTime", endTime);
            if (creationTime != 0) {
                jsonObject.put("creationTime", creationTime);
            }
            if (lastTime != 0) {
                jsonObject.put("lastTime", lastTime);
            }
            jsonObject.put("isAllDay", isAllDay);
            jsonObject.put("isCommunity", isCommunity);
            jsonObject.put("syncToLocal", syncToLocal);
            jsonObject.put("state", state);
            jsonObject.put("isMeeting", isMeeting);

            if (!StringUtils.isBlank(remindEvent)) {
                JSONObject remindJson = JSONUtils.getJSONObject(remindEvent);
                jsonObject.put("remindEvent", remindJson);
            }

            if (!StringUtils.isBlank(location)) {
                JSONObject locationJson = JSONUtils.getJSONObject(location);
                jsonObject.put("location", locationJson);
            }
            if (!StringUtils.isBlank(participants)) {
                JSONArray partJsonArray = JSONUtils.getJSONArray(participants, new JSONArray());
                jsonObject.put("participants", partJsonArray);
            }

            if (!StringUtils.isBlank(note)) {
                jsonObject.put("note", note);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }


        return jsonObject;
    }


    public boolean canDelete() {
        boolean canDelete = false;
        AccountType accountType = AccountType.getAccountType(getScheduleCalendar());
        switch (accountType) {
            case APP_MEETING:
                boolean isAdmin = PreferencesByUserAndTanentUtils.getBoolean(MyApplication.getInstance(), Constant.PREF_IS_MEETING_ADMIN,
                        false);
                if (isAdmin || (getOwner().equals(BaseApplication.getInstance().getUid()))) {
                    canDelete = true;
                }
                break;
            case EXCHANGE:
            case APP_SCHEDULE:
                canDelete = getOwner().equals(BaseApplication.getInstance().getUid());
                break;
        }
        return canDelete;

    }

    public boolean canModify() {
        boolean canModify = false;
        AccountType accountType = AccountType.getAccountType(getScheduleCalendar());
        switch (accountType) {
            case EXCHANGE:
                canModify = getOwner().equals(BaseApplication.getInstance().getUid());
                break;
            case APP_MEETING:
                canModify = getOwner().equals(BaseApplication.getInstance().getUid()) && getEndTimeCalendar().after(Calendar.getInstance());
                break;
            case APP_SCHEDULE:
                canModify = getOwner().equals(BaseApplication.getInstance().getUid());
                break;
        }
        return canModify;
    }

    //会议中是否显示群聊
    public boolean isShowGroupChat() {
        //如果不是相关人员  隐藏
        boolean relatedPersonFlag = false;
        List<Participant> list = getAllParticipantList();
        for (Participant item : list) {
            if (BaseApplication.getInstance().getUid().equals(item.getId())) {
                relatedPersonFlag = true;
            }
        }
        if (BaseApplication.getInstance().getUid().equals(getOwner())) {
            relatedPersonFlag = true;
        }
        return relatedPersonFlag && WebServiceRouterManager.getInstance().isV1xVersionChat();

    }

    public boolean equals(Object other) { // 重写equals方法，后面最好重写hashCode方法

        if (this == other) // 先检查是否其自反性，后比较other是否为空。这样效率高
            return true;
        if (other == null)
            return false;
        if (!(other instanceof Schedule))
            return false;

        final Schedule otherSchedule = (Schedule) other;
        return getId().equals(otherSchedule.getId());
    }

}
