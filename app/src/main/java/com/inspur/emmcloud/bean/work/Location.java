package com.inspur.emmcloud.bean.work;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

public class Location {
	private String id = "";
	private String name = "";
	private List<OfficeBuilding> offoceBuildingList = new ArrayList<OfficeBuilding>();

	public Location(JSONObject jsonObject) {
		try {
			if (jsonObject.has("buildings")) {
				JSONArray array = jsonObject.getJSONArray("buildings");
				for (int i = 0; i < array.length(); i++) {
					offoceBuildingList.add(new OfficeBuilding(array.getJSONObject(i)));
				}
			}
			if (jsonObject.has("id")) {
				this.id = jsonObject.getString("id");
			}
			if (jsonObject.has("name")) {
				this.name = jsonObject.getString("name");
			}
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}

	}

	public List<OfficeBuilding> getOfficeBuildingList() {
		return offoceBuildingList;
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

}