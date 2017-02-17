/**
 * 
 * GetFindSearchResult.java
 * classes : com.inspur.emmcloud.bean.GetFindSearchResult
 * V 1.0.0
 * Create at 2016年10月18日 下午6:04:46
 */
package com.inspur.emmcloud.bean;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.inspur.emmcloud.util.LogUtils;

/**
 * com.inspur.emmcloud.bean.GetFindSearchResult create at 2016年10月18日 下午6:04:46
 */
public class GetFindMixSearchResult {
	private List<FindSearchContacts> findSearchContactList = new ArrayList<FindSearchContacts>();
	private List<FindSearchNews> findSearchNewsList = new ArrayList<FindSearchNews>();

	public GetFindMixSearchResult(String response) {
		try {
			JSONObject jsonObject = new JSONObject(response);
			if (jsonObject.has("news")) {
				JSONArray newsArray = jsonObject.getJSONArray("news");
				for (int i = 0; i < newsArray.length(); i++) {
					JSONObject obj = newsArray.getJSONObject(i);
					findSearchNewsList.add(new FindSearchNews(obj));
				}
			}
			if (jsonObject.has("user")) {
				JSONArray userArray = jsonObject.getJSONArray("user");
				for (int i = 0; i < userArray.length(); i++) {
					JSONObject obj = userArray.getJSONObject(i);
					findSearchContactList.add(new FindSearchContacts(obj));
				}
			}

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	public List<FindSearchContacts> getFindSearchContactList() {
		return findSearchContactList;

	};

	public List<FindSearchNews> getFindSearchNewsList() {
		return findSearchNewsList;

	};


	public List<String> getFindSearchItemList() {
		List<String> findSearchItemList = new ArrayList<String>();
		if (findSearchContactList.size() > 0) {
			findSearchItemList.add("user");
		}
		if (findSearchNewsList.size() > 0) {
			findSearchItemList.add("news");
		}
		return findSearchItemList;
	}

}
