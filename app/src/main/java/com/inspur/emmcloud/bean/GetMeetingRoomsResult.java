package com.inspur.emmcloud.bean;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class GetMeetingRoomsResult{

	private JSONArray meetingRoomsArray;
	private ArrayList<MeetingArea> meetingAreas = new ArrayList<MeetingArea>();
	public GetMeetingRoomsResult(String response){
		try {
			meetingRoomsArray = new JSONArray(response);
			for (int i = 0; i < meetingRoomsArray.length(); i++) {
				JSONObject obj = meetingRoomsArray.getJSONObject(i);
				meetingAreas.add(new MeetingArea(obj));
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public ArrayList<MeetingArea> getMeetingAreas(){
		return meetingAreas;
	}
	
}