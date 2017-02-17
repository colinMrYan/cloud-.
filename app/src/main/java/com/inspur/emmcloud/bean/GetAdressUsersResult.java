package com.inspur.emmcloud.bean;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


public class GetAdressUsersResult implements Serializable{
	private List<AdressUser> adressUserList = new ArrayList<AdressUser>();
	
	public GetAdressUsersResult(String response) {
		try {
			JSONArray jsonArray = new JSONArray(response);
			for (int i = 0; i < jsonArray.length(); i++) {
				JSONObject obj = jsonArray.getJSONObject(i);
				adressUserList.add(new AdressUser(obj));
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
	
	public List<AdressUser> getAdressUserList(){
		return adressUserList;
	}
	
	public class AdressUser implements Serializable{
		private String user_id = "";
		private String name = "";
		private String orgname = "";
//		private String code = "";
		private String mobile = "";
		private String email = "";
//		private String head = "";
//		private String office = "";
//		private int state = -1;
		private String inspurId="";

		public AdressUser(JSONObject obj) {
			try {
				if (obj.has("user_id")) {
					this.user_id = obj.getString("user_id");
				}
				if (obj.has("real_name")) {
					this.name = obj.getString("real_name");
				}
//				if (obj.has("code")) {
//					this.code = obj.getString("code");
//				}
				if (obj.has("mobile")) {
					this.mobile = obj.getString("mobile");
				}
				if (obj.has("email")) {
					this.email = obj.getString("email");
				}
//				if (obj.has("head")) {
//					this.head = obj.getString("head");
//				}
				if (obj.has("inspur_id")) {
					this.inspurId = obj.getString("inspur_id");
				}
//				if (obj.has("state")) {
//					this.state = obj.getInt("state");
//				}
				if(obj.has("org_name")){
					this.orgname = obj.getString("org_name");
				}
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		public String getId(){
			return user_id;
		}
		public String getName(){
			return name;
		}
//		public String getCode(){
//			return code;
//		}
		public String getMobile(){
			return mobile;
		}
		public String getEmail(){
			return email;
		}
//		public String getHead(){
//			return head;
//		}
//		public String getOffice(){
//			return office;
//		}
		public String getInspurId(){
			return inspurId;
		}
//		public int getState(){
//			return state;
//		}
		public String getOrgname() {
			return orgname;
		}
		
		
	}
}
