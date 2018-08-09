package com.inspur.emmcloud.bean.chat;

import com.alibaba.fastjson.JSON;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class GetChannelInfoResult {
	
	private String cid = "";
	private String created = "";
	private String name = "";
	private String owner = "";
	private String type = "";
	private String mate = "";
	private boolean isPrivate = false;
	private JSONArray memberArray;

	public GetChannelInfoResult(String response){
		try {
			JSONObject obj = new JSONObject(response);
			if (obj.has("cid")) {
				this.cid = obj.getString("cid");
			}
			if (obj.has("created")) {
				this.created = obj.getString("created");
			}
			if (obj.has("name")) {
				this.name = obj.getString("name");
			}
			if (obj.has("owner")) {
				this.owner = obj.getString("owner");
			}
			if (obj.has("private")) {
				this.isPrivate = obj.getBoolean("private");
			}
			if (obj.has("type")) {
				this.type = obj.getString("type");
			}
			if (obj.has("type")) {
				this.type = obj.getString("type");
			}
			if (obj.has("members")) {
				this.memberArray = obj.getJSONArray("members");
			}
			if (obj.has("channelMembers")) {
				this.memberArray = obj.getJSONArray("channelMembers");
			}
			if (obj.has("mate")) {
				this.mate = obj.getString("mate");
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public String getCid(){
		return cid;
	}
	public String getCreated(){
		return created;
	}
	public String getName(){
		return name;
	}
	public String getOwner(){
		return owner;
	}
	public String getType(){
		return type;
	}
	public String getMate(){
		return mate;
	}
	public boolean getIsPrivate(){
		return isPrivate;
	}
	public JSONArray getMembersArray(){
		if (memberArray == null) {
			memberArray = new JSONArray();
		}
		return memberArray;
	}
	
	public List<String> getMemberList(){
		List<String> memberList = new ArrayList<String>();
		if (memberArray == null) {
			memberArray = new JSONArray();
		}
		memberList = JSON.parseArray(memberArray.toString(), String.class);
		return memberList;
	}
	
}
