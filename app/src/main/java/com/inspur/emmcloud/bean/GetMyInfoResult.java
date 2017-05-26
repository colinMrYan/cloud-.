package com.inspur.emmcloud.bean;

import org.json.JSONObject;

import java.io.Serializable;

/**
 * 获取到的个人信息数据
 * {
 "avatar": "/img/headimg/5af5e6b0-aa18-11e5-826d-d954fe083969",
 "code": "yufuchang",
 "creation_date": 1464281933000,
 "enterprise": {
 "code": "inspur_esg",
 "creation_date": 1464340157000,
 "ent_license_copy": "123",
 "ent_license_sn": "123",
 "id": 10000,
 "last_update": 1484808046358,
 "name": "浪潮集团"
 },
 "enterprises": [
 {
 "$ref": "$.enterprise"
 }
 ],
 "first_name": "于",
 "gender": "MALE",
 "has_password": true,
 "id": 99999,
 "instance_credentials_available": false,
 "last_name": "富昌",
 "last_update": 1491465514834,
 "locale": "zh-cn",
 "mail": "yufuchang@inspur.com",
 "old_id": "688aba0c-7a23-4849-9b89-3e206336ba7d",
 "partners": [],
 "phone": "15165155920"
 }
 */
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
