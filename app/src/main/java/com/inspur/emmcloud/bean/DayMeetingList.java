/**
 * 
 * MeetingList.java
 * classes : com.inspur.emmcloud.bean.MeetingList
 * V 1.0.0
 * Create at 2016年9月30日 下午5:19:15
 */
package com.inspur.emmcloud.bean;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * com.inspur.emmcloud.bean.MeetingList
 * create at 2016年9月30日 下午5:19:15
 */
public class DayMeetingList implements Comparator{
	private List<Meeting> meetingList = new ArrayList<Meeting>();
	public DayMeetingList(){
		
	}
	public List<Meeting>  getMeetingList(){
		return meetingList;
	}
	
	public void setMeetingList(List<Meeting> meetingList){
		this.meetingList = meetingList;
	}
	
	@Override
	public int compare(Object lhs, Object rhs) {
		DayMeetingList dayMeetingListA = (DayMeetingList) lhs;
		DayMeetingList dayMeetingListB = (DayMeetingList) rhs;
		Long fromA = Long.parseLong(dayMeetingListA.getMeetingList().get(0).getFrom());
		Long fromB = Long.parseLong(dayMeetingListB.getMeetingList().get(0).getFrom());
		if (fromA < fromB) {
			return 1;
		} else if (fromA > fromB) {
			return -1;
		} else {
			return 0;
		}
	}
}
