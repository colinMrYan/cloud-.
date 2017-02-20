package com.inspur.emmcloud.bean;

import java.io.Serializable;

import org.json.JSONObject;



public class CardPackage implements Serializable {
	private String id;
	private String type;
	private String name;
	private String number;
	private String address;
	private String title;
	private String tel;
	private String bank;
	private String account;
	public CardPackage() {

	}

	public CardPackage(JSONObject obj) {
		try {
			if (obj.has("id")) {
				this.id = obj.getString("id");
			}
			if (obj.has("title")) {
				this.title = obj.getString("title");
			}
			if (obj.has("props")) {
				JSONObject propsObj = obj.getJSONObject("props");
				
				if (propsObj.has("type")) {
					this.type = propsObj.getString("type");
				}
				if (propsObj.has("name")) {
					this.name = propsObj.getString("name");
				}
				if (propsObj.has("number")) {
					this.number = propsObj.getString("number");
				}
				if (propsObj.has("address")) {
					this.address = propsObj.getString("address");
				}
				if (propsObj.has("tel")) {
					this.tel = propsObj.getString("tel");
				}
				if (propsObj.has("bank")) {
					this.bank = propsObj.getString("bank");
				}
				if (propsObj.has("account")) {
					this.account = propsObj.getString("account");
				}
			}
	
			
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
		
	}
	
	public String getId(){
		return id;
	}
	public String getTitle(){
		return title;
	}
	public String getType(){
		return type;
	}
	public String getName(){
		return name;
	}
	public String getNumber(){
		return number;
	}
	public String getAddress(){
		return address;
	}
	public String getTel(){
		return tel;
	}
	public String getBank(){
		return bank;
	}
	public String getAccount(){
		return account;
	}
	
	
}
