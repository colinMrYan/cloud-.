package com.inspur.emmcloud.bean.chat;

import org.json.JSONObject;

import java.io.Serializable;

public class Comment implements Serializable{

	
	private Long mid = 0L;
//	private String from;
	private String uid = "";;
	private String title= "";;
	private String avatar = "";
	private String timestamp = "";
	private String order = "";
	private String type = "";
//	private String body;
	private String preview = "";
	private String content = "";
//	private JSONObject jsonObject;
//	private JSONArray jsonArray;
	private String source = "";
	private String mentions = "";
	private String urls = "";
	public Comment (String title,String content,String uid,String timestamp){
		this.title = title;
		this.source = content;
		this.uid = uid;
		this.timestamp = timestamp;
	}
	
	public Comment(Msg msg){
		this.mid = msg.getMid();
		this.uid = msg.getUid();
		this.avatar = msg.getAvatar();
		this.title = msg.getTitle();
		this.timestamp = msg.getTime();
//		this.order = msg.getOrder();
		this.type = msg.getType();
//		this.preview = msg.getPreview();
		
		String body = msg.getBody();
		try {
			JSONObject jsonObject = new JSONObject(body);
			if(jsonObject.has("source")){
				this.source = jsonObject.getString("source");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		
	}
	public Comment(JSONObject jsonObject){
		
		try {
			if (jsonObject.has("mid")) {
				mid = jsonObject.getLong("mid");
			} 
			if (jsonObject.has("from")) {
				
				JSONObject jsonFrom = jsonObject.getJSONObject("from");
				if (jsonFrom.has("uid")) {
					uid = jsonFrom.getString("uid");
				} 
				if (jsonFrom.has("title")) {
					title = jsonFrom.getString("title");
				} 
				if (jsonFrom.has("avatar")) {
					avatar = jsonFrom.getString("avatar");
				}
			} 
			if (jsonObject.has("timestamp")) {
				timestamp = jsonObject.getString("timestamp");
			} 
			if (jsonObject.has("order")) {
				order = jsonObject.getString("order");
			}
			if(jsonObject.has("type")){
				type = jsonObject.getString("type");
			}
			if(jsonObject.has("body")){
				JSONObject jsonBody = null;
				try {
					jsonBody = jsonObject.getJSONObject("body");
				} catch (Exception e) {
					e.printStackTrace();
				}
				
				if (jsonBody == null) {
					if(jsonObject.has("body")){
						jsonBody = new JSONObject(jsonObject.getString("body"));
					}
				}
				
				if (jsonBody.has("content")) {
					content = jsonBody.getString("content");
				}
				
				if(jsonBody.has("source")){
					this.source = jsonBody.getString("source");
				}
				
				if(jsonBody.has("mentions")){
					this.mentions = jsonBody.getString("mentions");
				}
				
				if(jsonBody.has("urlList")){
					this.urls = jsonBody.getString("urlList");
				}
			}
			
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	public Long getMid() {
		return mid;
	}
	public void setMid(String mid) {
		this.mid = mid;
	}
	public String getUid() {
		return uid;
	}
	public void setUid(String uid) {
		this.uid = uid;
	}
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	public String getAvatar() {
		return avatar;
	}
	public void setAvatar(String avatar) {
		this.avatar = avatar;
	}
	public String getTimestamp() {
		return timestamp;
	}
	public void setTimestamp(String timestamp) {
		this.timestamp = timestamp;
	}
	public String getOrder() {
		return order;
	}
	public void setOrder(String order) {
		this.order = order;
	}
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	public String getPreview() {
		return preview;
	}
	public void setPreview(String preview) {
		this.preview = preview;
	}
	public String getContent() {
		return content;
	}
	public void setContent(String content) {
		this.content = content;
	}
	public String getSource() {
		return source;
	}
	public void setSource(String source) {
		this.source = source;
	}

	public String getMentions() {
		return mentions;
	}

	public void setMentions(String mentions) {
		this.mentions = mentions;
	}

	public String getUrls() {
		return urls;
	}

	public void setUrls(String urls) {
		this.urls = urls;
	}
	
	
}
