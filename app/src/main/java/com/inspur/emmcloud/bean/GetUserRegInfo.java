package com.inspur.emmcloud.bean;

import org.json.JSONException;
import org.json.JSONObject;

public class GetUserRegInfo {

	/**
	 * 填写验证码页面返回的用户注册信息实体类
	 * */
	private static final String TAG="GetUserRegInfo";
	private String userID;
	private String userName;
	private boolean isHasRegistered;
	
	public GetUserRegInfo(String response){
		
		try {
			JSONObject jsonObject = new JSONObject(response);
			
			if(jsonObject.has("isHasRegistered")){
				this.isHasRegistered = jsonObject.getBoolean("isHasRegistered");
			}
			if(jsonObject.has("userID")){
				this.userID=jsonObject.getString("userID");
			}
			if(jsonObject.has("userName")){
				this.userName=jsonObject.getString("userName");
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

	public String getUserID() {
		return userID;
	}

	public void setUserID(String userID) {
		this.userID = userID;
	}

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public boolean getisHasRegistered() {
		return isHasRegistered;
	}

	public void setisHasRegistered(boolean isHasRegistered) {
		this.isHasRegistered = isHasRegistered;
	}
	
	
}
