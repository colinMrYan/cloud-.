package com.inspur.emmcloud.bean.work;

import org.json.JSONException;
import org.json.JSONObject;

public class GetCreateOfficeResult {

	private String id;
	public GetCreateOfficeResult(String response){
		try {
			JSONObject obj = new JSONObject(response);
			if (obj.has("id")) {
				id = obj.getString("id");
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public String getId(){
		return id;
	}
}
