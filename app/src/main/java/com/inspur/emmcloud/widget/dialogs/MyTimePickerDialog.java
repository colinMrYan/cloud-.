package com.inspur.emmcloud.widget.dialogs;

import java.util.Calendar;

import android.app.TimePickerDialog;
import android.content.Context;
import android.widget.TimePicker;

public class MyTimePickerDialog extends TimePickerDialog{

	private int myHourOfDay,myMinute;
	private int myYear,myMonth,myDay;
	private int nowYear,nowMonth,nowDay,nowHour,nowMin;
	private boolean isEndTime,isBeginTime;
	private int beginTimeHour,beginTimeMin,endTimeHour,endTimeMin;
	private int initBeginTimeHour = -1,initBeginTimeMinute=-1,initEndTimeHour=-1,initEndTimeMinute=-1;
	public MyTimePickerDialog(Context context, int theme, OnTimeSetListener callBack,
			int hourOfDay, int minute, boolean is24HourView) {
		super(context, theme, callBack, hourOfDay, minute, is24HourView);
		// TODO Auto-generated constructor stub
		myHourOfDay = hourOfDay;
		myMinute = minute;
		
		Calendar now = Calendar.getInstance();  
		        nowYear = now.get(Calendar.YEAR);  
		        nowMonth = now.get(Calendar.MONTH)+1;  
		        nowDay = now.get(Calendar.DAY_OF_MONTH);  
		        
		        nowHour = now.get(Calendar.HOUR_OF_DAY);
				nowMin = now.get(Calendar.MINUTE);
				this.setCancelable(false);

	}

	@Override
	public void onTimeChanged(TimePicker view, int hourOfDay, int minute) {
		// TODO Auto-generated method stub
		super.onTimeChanged(view, hourOfDay, minute);

		if(isBeginTime){
			if(hourOfDay<initBeginTimeHour){
				this.updateTime(initBeginTimeHour, initBeginTimeMinute);
			}
			if((hourOfDay<=initBeginTimeHour)&&(minute<initBeginTimeMinute)){
				this.updateTime(initBeginTimeHour, initBeginTimeMinute);
			}
			if(hourOfDay>initEndTimeHour){
				this.updateTime(initBeginTimeHour, initBeginTimeMinute);
			}
			if((hourOfDay>=initEndTimeHour)&&(minute>initEndTimeMinute)){

				this.updateTime(initBeginTimeHour, initBeginTimeMinute);
			}
			if(hourOfDay>endTimeHour){
				this.updateTime(initBeginTimeHour, initBeginTimeMinute);
			}
			if((hourOfDay>=endTimeHour)&&(minute>=endTimeMin)){
				this.updateTime(initBeginTimeHour, initBeginTimeMinute);
			}

		}
		if(isEndTime){


			if(hourOfDay<initBeginTimeHour){
				this.updateTime(initEndTimeHour, initEndTimeMinute);
			}
			if((hourOfDay<=initBeginTimeHour)&&(minute<initBeginTimeMinute)){

				this.updateTime(initEndTimeHour, initEndTimeMinute);
			}
			if(hourOfDay>initEndTimeHour){

				this.updateTime(initEndTimeHour, initEndTimeMinute);
			}
			if((hourOfDay>=initEndTimeHour)&&(minute>initEndTimeMinute)){

				this.updateTime(initEndTimeHour, initEndTimeMinute);
			}
			if(hourOfDay<beginTimeHour){
				this.updateTime(initEndTimeHour, initEndTimeMinute);
			}
			if((hourOfDay<=beginTimeHour)&&(minute<=beginTimeMin)){
				this.updateTime(initEndTimeHour, initEndTimeMinute);
			}
		}
//		int between= 0;
//		try {
//			between = CalendarUtil.daysBetween(nowYear+"-"+nowMonth+"-"+nowDay, myYear+"-"+(myMonth+1)+"-"+myDay);
//		} catch (ParseException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//		if(between==0){
//			if(isBeginTime){
//				if(hourOfDay<nowHour){
//					this.updateTime(nowHour, nowMin);
//				}
//				if((hourOfDay<=nowHour)&&(minute <nowMin)){
//					this.updateTime(nowHour, nowMin);
//				}
//				
////				if(hourOfDay>myHourOfDay){
////					this.updateTime(myHourOfDay, myMinute);
////				}
////				
////				if(hourOfDay>=myHourOfDay&&minute > myMinute){
////					this.updateTime(myHourOfDay, myMinute);
////				}
//			}else if (isEndTime) {
//				if(hourOfDay<myHourOfDay){
//					this.updateTime(myHourOfDay, myMinute);
//				}
//				if((hourOfDay<=myHourOfDay)&&(minute <myMinute)){
//					this.updateTime(myHourOfDay, myMinute);
//				}
//			}
//			
//			
//
//		}else if(between > 0){
//			if(isEndTime){
//				if(hourOfDay<myHourOfDay){
//					this.updateTime(myHourOfDay, myMinute);
//				}
//				if((hourOfDay<=myHourOfDay)&&(minute <myMinute)){
//					this.updateTime(myHourOfDay, myMinute);
//				}
//			}else if (isBeginTime) {
//				if(hourOfDay>myHourOfDay){
//					this.updateTime(myHourOfDay, myMinute);
//				}
//				if((hourOfDay>=myHourOfDay)&&(minute >myMinute)){
//					this.updateTime(myHourOfDay, myMinute);
//				}
//			}
//			
//		}else {
//			if(isBeginTime){
//				if(hourOfDay>myHourOfDay){
//					this.updateTime(myHourOfDay, myMinute);
//				}
//				if((hourOfDay>=myHourOfDay)&&(minute >myMinute)){
//					this.updateTime(myHourOfDay, myMinute);
//				}
//			}else {
//				if(hourOfDay<myHourOfDay){
//					this.updateTime(myHourOfDay, myMinute);
//				}
//				if((hourOfDay<=myHourOfDay)&&(minute <myMinute)){
//					this.updateTime(myHourOfDay, myMinute);
//				}
//			}
//			
//		}
			
	}
	
	

	public void setEndTime(boolean isEndTime) {
		this.isEndTime = isEndTime;
	}

	public void setMyYear(int myYear) {
		this.myYear = myYear;
	}

	public void setMyMonth(int myMonth) {
		this.myMonth = myMonth;
	}

	public void setMyDay(int myDay) {
		this.myDay = myDay;
	}

	public void setBeginTime(boolean isBeginTime) {
		this.isBeginTime = isBeginTime;
	}

	public void setBeginTimeHour(int beginTimeHour) {
		this.beginTimeHour = beginTimeHour;
	}

	public void setBeginTimeMin(int beginTimeMin) {
		this.beginTimeMin = beginTimeMin;
	}

	public void setEndTimeHour(int endTimeHour) {
		this.endTimeHour = endTimeHour;
	}

	public void setEndTimeMin(int endTimeMin) {
		this.endTimeMin = endTimeMin;
	}

	public void setInitBeginTimeHour(int initBeginTimeHour) {
		this.initBeginTimeHour = initBeginTimeHour;
	}

	public void setInitBeginTimeMinute(int initBeginTimeMinute) {
		this.initBeginTimeMinute = initBeginTimeMinute;
	}

	public void setInitEndTimeHour(int initEndTimeHour) {
		this.initEndTimeHour = initEndTimeHour;
	}

	public void setInitEndTimeMinute(int initEndTimeMinute) {
		this.initEndTimeMinute = initEndTimeMinute;
	}
	
}
