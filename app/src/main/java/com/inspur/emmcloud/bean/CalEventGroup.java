package com.inspur.emmcloud.bean;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import com.hp.hpl.sparta.xpath.ThisNodeTest;
import com.inspur.emmcloud.bean.CalendarEvent;

public class CalEventGroup implements Comparator {
	private int key;
	private List<CalendarEvent> calEventList = new ArrayList<CalendarEvent>();

	public CalEventGroup(){
		
	}
	public CalEventGroup(int key, List<CalendarEvent> calEventList) {
		this.key = key;
		this.calEventList = calEventList;

	}

	public void setKey(int key) {
		this.key = key;
	}

	public int getKey() {
		return key;
	}

	public List<CalendarEvent> getCalEventList() {
		if (calEventList == null) {
			calEventList = new ArrayList<CalendarEvent>();
		}
		return calEventList;
	}

	public void setCalEventList(List<CalendarEvent> calEventList) {
		this.calEventList = calEventList;
	}

	@Override
	public int compare(Object lhs, Object rhs) {
		CalEventGroup calEventGroupA = (CalEventGroup) lhs;
		CalEventGroup calEventGroupB = (CalEventGroup) rhs;
		long diff = calEventGroupA.getKey() - calEventGroupB.getKey();
		if (diff > 0) {
			return 1;
		} else if (diff == 0) {
			return 0;
		} else {
			return -1;
		}
	}
}
