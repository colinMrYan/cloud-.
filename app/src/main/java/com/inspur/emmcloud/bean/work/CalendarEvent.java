package com.inspur.emmcloud.bean.work;

import com.alibaba.fastjson.annotation.JSONField;
import com.inspur.emmcloud.util.privates.TimeUtils;

import org.json.JSONObject;

import java.io.Serializable;
import java.util.Calendar;
import java.util.Comparator;

public class CalendarEvent implements Serializable,Comparator{
	private String id;
	private Calendar creationDate;
	private Calendar lastUpdate;
	private String state;
	private String title;
	private String owner;
	private String location;
	private boolean allday;
	private Calendar startDate;
	private Calendar endDate;
	private MyCalendar calendar;
	private long creationDateLong;

	public CalendarEvent(){

	}
	public CalendarEvent(JSONObject obj){
		try {
			if (obj.has("id")) {
				id = obj.getString("id");
			}
			if (obj.has("title")) {
				title = obj.getString("title");
			}
			if (obj.has("creationDate")) {
				String creationDateStr = obj.getString("creationDate");
				if (!creationDateStr.equals("null")) {
					creationDateLong = Long.parseLong(creationDateStr);
					Long creatDateLong = obj.getLong("creationDate");
					creationDate = TimeUtils.timeLong2UTCCalendar(creatDateLong);
				}

			}
			if (obj.has("lastUpdate")) {
				String lastUpdateStr = obj.getString("lastUpdate");
				if (!lastUpdateStr.equals("null")) {
					Long lastUpdateLong = obj.getLong("lastUpdate");
					lastUpdate = TimeUtils.timeLong2UTCCalendar(lastUpdateLong);
				}
			}
			if (obj.has("startDate")) {
				String startDateStr = obj.getString("startDate");
				if (!startDateStr.equals("null")) {
					Long startDateLong = obj.getLong("startDate");
					startDate = TimeUtils.timeLong2UTCCalendar(startDateLong);
				}

			}
			if (obj.has("endDate")) {
				String endDateStr = obj.getString("endDate");
				if (!endDateStr.equals("null")) {
					Long endDateLong = obj.getLong("endDate");
					endDate = TimeUtils.timeLong2UTCCalendar(endDateLong);
				}
			}
			if (obj.has("state")) {
				state = obj.getString("state");
			}
			if (obj.has("owner")) {
				owner = obj.getString("owner");
			}
			if (obj.has("location")) {
				location = obj.getString("location");
			}
			if (obj.has("allday")) {
				allday = obj.getBoolean("allday");
			}
			if (obj.has("id")) {
				id = obj.getString("id");
			}

			if (obj.has("calendar")) {
				JSONObject calendarObj = obj.getJSONObject("calendar");
				calendar = new MyCalendar(calendarObj);
			}


		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
	}

	public void setCalendar(MyCalendar calendar){
		this.calendar = calendar;
	}

	public MyCalendar getCalendar(){
		return calendar;
	}
	public String getId(){
		return id;
	}

	public void setId(String id){
		this.id = id;
	}

	public String getState(){
		return state;
	}
	public void setState(String state){
		this.state = state;
	}

	public String getOwner(){
		return owner;
	}
	@JSONField(serialize = false)
	public Calendar getLocalCreationDate(){
		return TimeUtils.UTCCalendar2LocalCalendar(creationDate);
	}

	public Calendar getCreationDate(){
		return creationDate;
	}
	public void setCreationDate(Calendar creationDate){
		this.creationDate = creationDate;
	}

	@JSONField(serialize = false)
	public Calendar getLocalLastUpdate(){
		return TimeUtils.UTCCalendar2LocalCalendar(lastUpdate);
	}

	public void setLastUpdate(Calendar lastUpdate){
		this.lastUpdate = lastUpdate;
	}

	public Calendar getLastUpdate(){
		return lastUpdate;
	}

	public String getTitle(){
		return title;
	}
	public void setTitle(String title){
		this.title = title;
	}

	public String getLocation(){
		return location;
	}

	public void setLocation(String location){
		this.location = location;
	}

	public boolean getAllday(){
		return allday;
	}
	public void setAllday(boolean allday){
		this.allday = allday;
	}
	@JSONField(serialize = false)
	public Calendar getLocalStartDate(){
		return TimeUtils.UTCCalendar2LocalCalendar(startDate);
	}
	public Calendar getStartDate(){
		return startDate;
	}
	public void setStartDate(Calendar startDate){
		this.startDate = startDate;
	}

	@JSONField(serialize = false)
	public Calendar getLocalEndDate(){
		return TimeUtils.UTCCalendar2LocalCalendar(endDate);
	}

	@JSONField(serialize = false)
	public long getCreationDateLong(){
		return creationDateLong;
	}
	public Calendar getEndDate(){
		return endDate;
	}

	public void setEndDate(Calendar endDate){
		this.endDate = endDate;
	}

	@Override
	public int compare(Object lhs, Object rhs) {
		CalendarEvent calEventA = (CalendarEvent) lhs;
		CalendarEvent calEventB = (CalendarEvent) rhs;
		if (calEventA.getStartDate().after(calEventB.getStartDate())) {
			return 1;
		} else if (calEventA.getStartDate().before(calEventB.getStartDate())) {
			return -1;
		} else {
			return 0;
		}
	}

	/**
	 * 重写equals方法修饰符必须是public,因为是重写的Object的方法. 2.参数类型必须是Object.
	 */
	public boolean equals(Object other) { // 重写equals方法，后面最好重写hashCode方法

		if (this == other) // 先检查是否其自反性，后比较other是否为空。这样效率高
			return true;
		if (other == null)
			return false;
		if (!(other instanceof CalendarEvent))
			return false;

		final CalendarEvent otherCalendarEvent = (CalendarEvent) other;
        return getId().equals(otherCalendarEvent.getId());
    }
}
