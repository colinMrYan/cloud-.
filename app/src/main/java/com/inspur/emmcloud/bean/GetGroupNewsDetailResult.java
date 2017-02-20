package com.inspur.emmcloud.bean;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.text.TextUtils;

import com.inspur.emmcloud.util.LogUtils;

public class GetGroupNewsDetailResult {

	private static final String TAG = "GetNewsTitleResult";
	private String author="";
	private String category="";
	private String digest="";
	private String needpush="";
	private String nid="";
	private String posttime="";
	private String publisher="";
	private String title="";
	private String url="";

	private JSONArray jsonArray;
	private List<GroupNews> groupNewsList;

	public GetGroupNewsDetailResult(String response) {
		try {
			jsonArray = new JSONArray(response);

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public List<GroupNews> getGroupNews() {
		groupNewsList = new ArrayList<GroupNews>();
		JSONObject jsonObject;
		try {

			for (int i = 0; i < jsonArray.length(); i++) {
				jsonObject = new JSONObject();
				jsonObject = jsonArray.getJSONObject(i);
				if(!TextUtils.isEmpty(jsonObject.toString())){
					groupNewsList.add(new GroupNews(jsonObject));
				}
				
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return groupNewsList;
	}
}
