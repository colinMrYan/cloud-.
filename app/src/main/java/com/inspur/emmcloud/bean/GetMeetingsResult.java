package com.inspur.emmcloud.bean;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

public class GetMeetingsResult {


	private ArrayList<Meeting> meetingList = new ArrayList<>();

	public GetMeetingsResult(String response) {
		try {
			JSONArray jsonArray = new JSONArray(response);
			for (int i = 0; i < jsonArray.length(); i++) {
				JSONObject obj = jsonArray.getJSONObject(i);
				meetingList.add(new Meeting(obj));
			}
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}

	}

	public ArrayList<Meeting> getMeetingsList() {
		return meetingList;
	}

}
