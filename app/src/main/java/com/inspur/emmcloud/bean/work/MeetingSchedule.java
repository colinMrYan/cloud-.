package com.inspur.emmcloud.bean.work;


public class MeetingSchedule {
	private Long from;
	private Long to;
	private Meeting meeting;
	public MeetingSchedule(Long from,Long to,Meeting meeting){
		this.from = from;
		this.to = to;
		this.meeting = meeting;
	}
	
	public MeetingSchedule(){
		
	}
	
	public void setFrom(Long from){
		this.from = from;
	}
	
	public void setTo(Long to){
		this.to = to;
	}
	
	public void setMeeting(Meeting meeting){
		this.meeting = meeting;
	}
	public Meeting getMeeting(){
		return meeting;
	}
	
	public Long getFrom(){
		return from;
	}
	public Long getTo(){
		return to;
	}
}
