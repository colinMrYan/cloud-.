package com.inspur.emmcloud.bean;

import java.io.Serializable;

import org.json.JSONException;
import org.json.JSONObject;

public class Attachment implements Serializable{

	private String id = "";
	private String name = "";
	private String uri = "";
	private String category = "";
	private String type = "";
	
	public Attachment(String response){
		try {
			JSONObject jsonObject = new JSONObject(response);
				if(jsonObject.has("id")){
					this.id = jsonObject.getString("id");
				}
				if(jsonObject.has("name")){
					this.name = jsonObject.getString("name");
				}
				if(jsonObject.has("id")){
					this.uri = jsonObject.getString("uri");
				}
				if(jsonObject.has("id")){
					this.category = jsonObject.getString("category");
				}
				if(jsonObject.has("type")){
					this.type = jsonObject.getString("type");
				}
		} catch (JSONException e1) {
			e1.printStackTrace();
		}
		
	}
	public Attachment(JSONObject jsonObject){
		try {
			if(jsonObject.has("id")){
				this.id = jsonObject.getString("id");
			}
			if(jsonObject.has("name")){
				this.name = jsonObject.getString("name");
			}
			if(jsonObject.has("id")){
				this.uri = jsonObject.getString("uri");
			}
			if(jsonObject.has("id")){
				this.category = jsonObject.getString("category");
			}
			if(jsonObject.has("type")){
				this.type = jsonObject.getString("type");
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
	public String getUri() {
		return uri;
	}
	public void setUri(String uri) {
		this.uri = uri;
	}
	public String getCategory() {
		return category;
	}
	public void setCategory(String category) {
		this.category = category;
	}
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	
	
}
