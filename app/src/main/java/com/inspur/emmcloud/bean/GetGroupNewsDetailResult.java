package com.inspur.emmcloud.bean;

import android.text.TextUtils;

import com.inspur.emmcloud.util.LogUtils;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class GetGroupNewsDetailResult {

	private static final String TAG = "GetNewsTitleResult";
//	private String author="";
//	private String category="";
//	private String digest="";
//	private String needpush="";
//	private String nid="";
//	private String posttime="";
//	private String publisher="";
//	private String title="";
//	private String url="";

	private JSONArray jsonArray;
	private List<GroupNews> groupNewsList;

	public GetGroupNewsDetailResult(String response) {
		try {
			LogUtils.YfcDebug("每一个category下的新闻信息："+response);
			JSONObject jsonObject = new JSONObject(response);
//			jsonArray = new JSONArray(response);
			if(jsonObject.has("content")){
				jsonArray = jsonObject.getJSONArray("content");
			}
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
