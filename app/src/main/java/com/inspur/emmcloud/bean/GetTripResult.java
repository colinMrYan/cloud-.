package com.inspur.emmcloud.bean;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

public class GetTripResult {
	private List<Trip> tripList = new ArrayList<Trip>();
	public GetTripResult(String response){
		try {
			JSONArray tripArray = new JSONArray(response);
			for (int i = 0; i < tripArray.length(); i++) {
				JSONObject tripObj = tripArray.getJSONObject(i);
				tripList.add(new Trip(tripObj));
			}
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
	}
	
	public List<Trip> getTripList(){
		if (tripList == null) {
			tripList = new ArrayList<Trip>();
		}
		return tripList;
	}
}
