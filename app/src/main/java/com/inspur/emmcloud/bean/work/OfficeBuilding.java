/**
 * 
 * OfficeBuilding.java
 * classes : com.inspur.emmcloud.bean.work.OfficeBuilding
 * V 1.0.0
 * Create at 2016年10月14日 下午4:40:54
 */
package com.inspur.emmcloud.bean.work;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * com.inspur.emmcloud.bean.work.OfficeBuilding create at 2016年10月14日 下午4:40:54
 */
public class OfficeBuilding {
	private String id = "";
	private String name= "";

	public OfficeBuilding(JSONObject obj) {
		try {
			if (obj.has("id")) {
				id = obj.getString("id");
			}
			if (obj.has("name")) {
				name =obj.getString("name");
			}
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
	
	public String getId(){
		return id;
	}
	
	public String getName(){
		return name;
	}
}
