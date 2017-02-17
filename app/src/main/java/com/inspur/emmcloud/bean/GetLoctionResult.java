package com.inspur.emmcloud.bean;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class GetLoctionResult {
	private ArrayList<Location> locationList = new ArrayList<Location>();

	public GetLoctionResult(String response) {
		try {
			JSONArray array = new JSONArray(response);
			for (int i = 0; i < array.length(); i++) {
				JSONObject jsonLocation = null;
				jsonLocation = array.getJSONObject(i);
				locationList.add(new Location(jsonLocation));
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public ArrayList<Location> getLocList() {
		return locationList;
	}

	public void setLocList(ArrayList<Location> locList) {
		this.locationList = locList;
	}

}
