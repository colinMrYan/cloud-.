package com.inspur.emmcloud.bean;

import java.io.Serializable;

import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;

public class GetMyInfoResult implements Serializable{

	private static final String TAG = "GetRegisterResult";
	
	private String response = "";
	private String avatar ="";
	private String code ="";
	private String creationDate = "";
	private String firstName = "";
	private String lastName = "";
	private String id="";
	private String gender="";
	private String mail ="";
	private String phoneNumber="";
	private String enterpriseCode="";
	private String enterpriseName="";
	private String oldId = "";
	private Boolean hasPassord = false;
	private String enterpriseId = "";
	
	public GetMyInfoResult(String response){
		this.response = response;
		try {
			JSONObject jsonObject = new JSONObject(response);
			
			if(jsonObject.has("enterprise")){
				JSONObject jObject = jsonObject.getJSONObject("enterprise");
				if(jObject.has("code")){
					this.enterpriseCode = jObject.getString("code");
				}
				if(jObject.has("name")){
					this.enterpriseName = jObject.getString("name");
				}
				if (jObject.has("id")) {
					this.enterpriseId = jObject.getString("id");
				}
				
			}
			if(jsonObject.has("avatar")){
				this.avatar = jsonObject.getString("avatar");
			}
			if (jsonObject.has("old_id")) {
				this.oldId = jsonObject.getString("old_id");
			}
			
			if(jsonObject.has("code")){
				this.code = jsonObject.getString("code");
			}
			
			if(jsonObject.has("creation_date")){
				this.creationDate = jsonObject.getString("creation_date");
			}
			
			if(jsonObject.has("first_name")){
				this.firstName = jsonObject.getString("first_name");
			}
			
			if(jsonObject.has("last_name")){
				this.lastName = jsonObject.getString("last_name");
			}
			
			if(jsonObject.has("id")){
				this.id = jsonObject.getString("id");
			}
			
			if(jsonObject.has("gender")){
				this.gender = jsonObject.getString("gender");
			}
			
			if(jsonObject.has("mail")){
				this.mail = jsonObject.getString("mail");
			}
			
			if(jsonObject.has("phone")){
				this.phoneNumber = jsonObject.getString("phone");
			}
			
			if(jsonObject.has("has_password")){
				this.hasPassord = jsonObject.getBoolean("has_password");
			}
			
			
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void setAvatar(String avatar){
		this.avatar = avatar;
	}

	public String getAvatar() {
		return avatar;
	}

	public String getCreationDate() {
		return creationDate;
	}
	
	public String getCode() {
		return code;
	}
	
	public String getName(){
		return firstName+lastName;
	}
	
	public String getID(){
		return id;
	}
	
	public String gender(){
		return gender;
	}
	
	public String getMail(){
		return mail;
	}
	
	public String getPhoneNumber(){
		return phoneNumber;
	}
	
	public String getEnterpriseCode(){
		return enterpriseCode;
	}
	
	public String getEnterpriseName(){
		return enterpriseName;
	}

	public String getResponse(){
		return response;
	}
	public String getOldId(){
		return oldId;
	}

	public Boolean getHasPassord() {
		return hasPassord;
	}

	public void setHasPassord(Boolean hasPassord) {
		this.hasPassord = hasPassord;
	}
	
	public String getEnterpriseId(){
		return  enterpriseId;
	}
	
	
}
