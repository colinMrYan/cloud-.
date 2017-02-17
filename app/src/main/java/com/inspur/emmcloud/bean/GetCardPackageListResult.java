package com.inspur.emmcloud.bean;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;


public class GetCardPackageListResult {
	private List<CardPackage> cardPackageList = new ArrayList<CardPackage>();
	public GetCardPackageListResult(String response){
		try {
			JSONArray array = new JSONArray(response);
			for (int i = 0; i < array.length(); i++) {
				JSONObject obj = array.getJSONObject(i);
				cardPackageList.add(new CardPackage(obj));
				
			}
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
		
	}
	
	public List<CardPackage> getCardPackageList(){
		return cardPackageList;
	}
}
