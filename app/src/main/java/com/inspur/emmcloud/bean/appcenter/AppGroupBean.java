package com.inspur.emmcloud.bean.appcenter;

import com.inspur.emmcloud.util.common.JSONUtils;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;


/**
 * classes : com.inspur.emmcloud.bean.appcenter.AppGroupBean Create at 2016年12月14日
 * 下午5:36:46
 */
public class AppGroupBean {

	private String categoryID = "";
	private String categoryName = "";
//	private List<App> appList = new ArrayList<App>();
	private List<App> appItemList = new ArrayList<App>();
	private String categoryIco = "";

	public AppGroupBean() {
	}
	public AppGroupBean(String response) {
			JSONObject jsonObject = JSONUtils.getJSONObject(response);
			categoryID = JSONUtils.getString(jsonObject,"categoryID","");
			categoryName = JSONUtils.getString(jsonObject,"categoryName","");
			if (JSONUtils.isJsonObjHasKey(jsonObject,"appList")) {
				JSONArray jsonArray = JSONUtils.getJSONArray(jsonObject,"appList",new JSONArray());
				for (int i = 0; i < jsonArray.length(); i++) {
					App app = new App(JSONUtils.getJSONObjectFromJSONArray(jsonArray,i));
					app.setCategoryID(categoryID);
					app.setOrderId(1000);
					appItemList.add(app);
				}
			}
			categoryIco = JSONUtils.getString(response,"category_ico","");
	}
	
	public AppGroupBean(JSONObject jsonObject) {
			categoryID = JSONUtils.getString(jsonObject,"categoryID","");
			categoryName = JSONUtils.getString(jsonObject,"categoryName","");
			if (jsonObject.has("appList")) {
				JSONArray jsonArray = JSONUtils.getJSONArray(jsonObject,"appList",new JSONArray());
				for (int i = 0; i < jsonArray.length(); i++) {
					App app = new App(JSONUtils.getJSONObjectFromJSONArray(jsonArray,i));
					app.setCategoryID(categoryID);
					app.setOrderId(1000);
					appItemList.add(app);
				}
			}
			categoryIco = JSONUtils.getString(jsonObject,"category_ico","");
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

	public String getCategoryIco() {
		return categoryIco;
	}

	public void setCategoryIco(String categoryIco) {
		this.categoryIco = categoryIco;
	}

	@Override
	public boolean equals(Object other) {
		if(this == other){
			return true;
		}
		if(other == null){
			return false;
		}
		if(!(other instanceof AppGroupBean)){
			return false;
		}
		AppGroupBean appGroupBean = (AppGroupBean) other;
		//此处从==判断是否相等  改为equals
		if(!(getCategoryID().equals(appGroupBean.getCategoryID()))){
			return false;
		}
		return true;
	}
}
