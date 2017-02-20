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
public class GetFindSearchResult {
	private List<FindSearchContacts> findSearchContactList = new ArrayList<FindSearchContacts>();
	private List<FindSearchNews> findSearchNewsList = new ArrayList<FindSearchNews>();
	private List<FindSearchMsgHistory> findSearchMsgHistoryList = new ArrayList<FindSearchMsgHistory>();

	public GetFindSearchResult(String response) {
		try {
			JSONObject jsonObject = new JSONObject(response);
			if (jsonObject.has("response")) {
				JSONObject responseObj = jsonObject.getJSONObject("response");
				if (responseObj.has("docs")) {
					JSONArray docsArray = responseObj.getJSONArray("docs");
					for (int i = 0; i < docsArray.length(); i++) {
						JSONObject obj = docsArray.getJSONObject(i);
						if (obj.has("datatype")) {
							String dataType = obj.getString("datatype");
							if (dataType.equals("news")) {
								findSearchNewsList.add(new FindSearchNews(obj));
							} else if (dataType.equals("user")) {
								findSearchContactList
										.add(new FindSearchContacts(obj));
							} else if (dataType.equals("msg")) {
								findSearchMsgHistoryList.add(new FindSearchMsgHistory(obj));
							}
						}
					}
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

	public List<FindSearchMsgHistory> getFindSearchMsgHistoryList() {
		return findSearchMsgHistoryList;

	};

	public List<String> getFindSearchItemList() {
		List<String> findSearchItemList = new ArrayList<String>();
		if (findSearchContactList.size() > 0) {
			findSearchItemList
					.add("user");
		}
		if (findSearchMsgHistoryList.size() > 0) {
			findSearchItemList
			.add("msg");
		}
		if (findSearchNewsList.size() > 0) {
			findSearchItemList
			.add("news");
		}
		return findSearchItemList;
	}

}
