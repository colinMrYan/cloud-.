package com.inspur.emmcloud.bean;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.inspur.emmcloud.util.LogUtils;

public class GetNewsResult {

	private JSONObject jsonObject;
	private JSONArray jsonArray;
	private List<News> commentList;

	public GetNewsResult(String response) {
		
		
		try {
			JSONObject jsonObjectRes = new JSONObject(response);
			if(jsonObjectRes.has("FAKE_CHANNEL_ID_3")){
				response = jsonObjectRes.getString("FAKE_CHANNEL_ID_3");
				jsonArray = new JSONArray(response);
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		
	}
	
	public List<News> getNews(){
		if (jsonArray == null) {
			return null;
		}
		commentList = new ArrayList<News>();
		try {
			for (int i = 0; i < jsonArray.length(); i++) {
				jsonObject = new JSONObject();
				jsonObject = jsonArray.getJSONObject(i);
				commentList.add(new News(jsonObject));
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return commentList;
	}
}
