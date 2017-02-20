package com.inspur.emmcloud.bean;

import org.json.JSONObject;

public class Robot {

	private String id = "";
	private String name = "";
	private String avatar = "";
	private String support = "";
	private String title = "";
	private String mode = "";
	
	public Robot(){}
	public Robot(String robot){
		try {
			JSONObject jsonObject = new JSONObject(robot);
			if(jsonObject.has("id")){
				this.id = jsonObject.getString("id");
			}
			
			if(jsonObject.has("name")){
				this.name = jsonObject.getString("name");
			}
			
			if(jsonObject.has("avatar")){
				this.avatar = jsonObject.getString("avatar");
			}
			
			if(jsonObject.has("support")){
				this.support = jsonObject.getString("support");
			}
			
			if(jsonObject.has("title")){
				this.title = jsonObject.getString("title");
			}
			
			if(jsonObject.has("mode")){
				this.mode = jsonObject.getString("mode");
			}
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getAvatar() {
		return avatar;
	}
	public void setAvatar(String avatar) {
		this.avatar = avatar;
	}
	public String getSupport() {
		return support;
	}
	public void setSupport(String support) {
		this.support = support;
	}
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	public String getMode() {
		return mode;
	}
	public void setMode(String mode) {
		this.mode = mode;
	}
	
}
