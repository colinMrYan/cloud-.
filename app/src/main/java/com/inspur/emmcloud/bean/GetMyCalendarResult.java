package com.inspur.emmcloud.bean;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


public class GetMyCalendarResult {
	private List<MyCalendar> calendarList = new ArrayList<MyCalendar>();
	public GetMyCalendarResult(String response){
		try {
			JSONArray jsonArray = new JSONArray(response);
			for (int i = 0; i < jsonArray.length(); i++) {
				JSONObject obj = jsonArray.getJSONObject(i);
				MyCalendar calendar = new MyCalendar(obj);
				calendarList.add(calendar);
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	public List<MyCalendar> getCalendarList(){
		return calendarList;
	}
}
