package com.inspur.emmcloud.bean.contact;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class GetAllContactResult {

	private List<Contact> allContactList = new ArrayList<Contact>();
	private List<Contact> modifyContactList = new ArrayList<Contact>();
	private List<String> deleteContactList = new ArrayList<String>();
	private JSONArray deleteIdArray;
	private String lastUpdateTime = "";
	private String unitID = "";

	public GetAllContactResult(String response) {
		try {
			JSONObject obj = new JSONObject(response);
			if (obj.has("lastQueryTime")) {
				lastUpdateTime = obj.getString("lastQueryTime");
			}
			if (obj.has("contacts")) {
				JSONArray contactArray = obj.getJSONArray("contacts");
				for (int i = 0; i < contactArray.length(); i++) {
					allContactList.add(new Contact(contactArray
							.getJSONObject(i),lastUpdateTime));
				}
			}
			if (obj.has("changed")) {
				JSONArray modifyArray = obj.getJSONArray("changed");
				for (int i = 0; i < modifyArray.length(); i++) {
					modifyContactList.add(new Contact(modifyArray
							.getJSONObject(i),lastUpdateTime));
				}
			}
			if(obj.has("deleted")){
				JSONArray deleteArray = obj.getJSONArray("deleted");
				for (int i = 0; i < deleteArray.length(); i++) {
					deleteContactList.add(deleteArray
							.getString(i));
				}
			}
			if (obj.has("deleted")) {
				deleteIdArray = obj.getJSONArray("deleted");
			}

			if (obj.has("unitID")) {
				unitID = obj.getString("unitID");
			}

		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
	}

	public List<Contact> getAllContactList() {
		return allContactList;
	}

	public String getLastUpdateTime() {
		return lastUpdateTime;
	}

	public List<Contact> getModifyContactList(){
		return modifyContactList;
	}

	public JSONArray getDeleteIdArray(){
		return deleteIdArray;
	}

	public List<String> getDeleteContactIdList() {
		return deleteContactList;
	}

	public String getUnitID() {
		return unitID;
	}

	public void setUnitID(String unitID) {
		this.unitID = unitID;
	}
}
