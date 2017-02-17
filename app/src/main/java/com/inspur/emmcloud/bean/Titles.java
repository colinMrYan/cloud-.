package com.inspur.emmcloud.bean;

import org.json.JSONException;
import org.json.JSONObject;

public class Titles {

	private static final String TAG = "GetNewsTitleResult";
	private String ncid = "";
	private String title = "";
	private String parent = "";
	
	public Titles(JSONObject jsonObject){
		try {
			
			if(jsonObject.has("ncid")){
				this.ncid = jsonObject.getString("ncid");
			}
			if(jsonObject.has("title")){
				this.title = jsonObject.getString("title");
			}
			if(jsonObject.has("parent")){
				this.parent = jsonObject.getString("parent");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public String getNcid() {
		return ncid;
	}

	public void setNcid(String ncid) {
		this.ncid = ncid;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getParent() {
		return parent;
	}

	public void setParent(String parent) {
		this.parent = parent;
	}
	
}
