package com.inspur.emmcloud.bean;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;


/**
 * classes : com.inspur.emmcloud.bean.AppGroupBean Create at 2016年12月14日
 * 下午5:36:46
 */
public class AppGroupBean {

	private String categoryID = "";
	private String categoryName = "";
//	private List<App> appList = new ArrayList<App>();
	private List<App> appItemList = new ArrayList<App>();

	public AppGroupBean() {
	}
	public AppGroupBean(String response) {
		try {
			JSONObject jsonObject = new JSONObject(response);
			if (jsonObject.has("categoryID")) {
				this.categoryID = jsonObject.getString("categoryID");
			}

			if (jsonObject.has("categoryName")) {
				this.categoryName = jsonObject.getString("categoryName");
			}

			if (jsonObject.has("appList")) {
				JSONArray jsonArray = jsonObject.getJSONArray("appList");
				for (int i = 0; i < jsonArray.length(); i++) {
					App app = new App(jsonArray.getJSONObject(i));
					app.setCategoryID(categoryID);
					app.setOrderId(1000);
					appItemList.add(app);
				}
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public AppGroupBean(JSONObject jsonObject) {
		try {
			if (jsonObject.has("categoryID")) {
				this.categoryID = jsonObject.getString("categoryID");
			}

			if (jsonObject.has("categoryName")) {
				this.categoryName = jsonObject.getString("categoryName");
			}

			if (jsonObject.has("appList")) {
				JSONArray jsonArray = jsonObject.getJSONArray("appList");
				for (int i = 0; i < jsonArray.length(); i++) {
					App app = new App(jsonArray.getJSONObject(i));
					app.setCategoryID(categoryID);
					app.setOrderId(1000);
					appItemList.add(app);
				}
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public String getCategoryID() {
		return categoryID;
	}

	public void setCategoryID(String categoryID) {
		this.categoryID = categoryID;
	}

	public String getCategoryName() {
		return categoryName;
	}

	public void setCategoryName(String categoryName) {
		this.categoryName = categoryName;
	}

//	public List<App> getAppList() {
//		return appList;
//	}

	public List<App> getAppItemList() {
		return appItemList;
	}

	public void setAppItemList(List<App> appItemList) {
		this.appItemList = appItemList;
	}

}
