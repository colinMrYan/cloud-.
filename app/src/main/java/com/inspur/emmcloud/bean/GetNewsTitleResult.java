package com.inspur.emmcloud.bean;

import com.inspur.emmcloud.util.LogUtils;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class GetNewsTitleResult implements Serializable{

	private static final String TAG = "GetNewsTitleResult";
	private JSONArray jsonArray;
	private JSONObject jsonObject;
	private List<Titles> titlesList;
	
	public GetNewsTitleResult(String response){
		try {
			jsonArray = new JSONArray(response);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public List<Titles> getTitlesList(){
		
		titlesList = new ArrayList<Titles>();
		try {

			
			for (int i = 0; i < jsonArray.length(); i++) {
				jsonObject = new JSONObject();
				jsonObject = jsonArray.getJSONObject(i);
				titlesList.add(new Titles(jsonObject));
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return titlesList;
	}
	
	
}
