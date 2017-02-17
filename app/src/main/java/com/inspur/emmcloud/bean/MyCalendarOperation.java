package com.inspur.emmcloud.bean;

import com.lidroid.xutils.db.annotation.Id;

public class MyCalendarOperation {
	@Id
	private String myCalendarId = "";
	private boolean isHide = false;

	public MyCalendarOperation() {

	}

	public MyCalendarOperation(String myCalendarId, boolean isHide ) {
		this.myCalendarId = myCalendarId;
		this.isHide = isHide;
	}
	
	public String getMyCalendarId(){
		return myCalendarId;
	}
	public boolean getIsHide(){
		return isHide;
	}
	
	public void setMyCalendarId(String myCalendarId ){
		this.myCalendarId = myCalendarId;
	}
	
	public void setIsHide(boolean isHide ){
		this.isHide = isHide;
	}
}
