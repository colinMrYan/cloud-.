package com.inspur.emmcloud.bean;

import com.inspur.emmcloud.util.JSONUtils;

import org.json.JSONObject;

import java.io.Serializable;

/**
 * Created by Administrator on 2017/5/24.
 */

public class Enterprise implements Serializable {
	private String code ;
	private String id ;
	private String name;
	private String creationDate;
	private String entLicenseCopy;
	private String entLicenseSn;
	private String lastUpdate;
	public Enterprise(){};
	public Enterprise(JSONObject object){
		code = JSONUtils.getString(object,"code","");
		id = JSONUtils.getString(object,"id","");
		name = JSONUtils.getString(object,"name","");
		creationDate = JSONUtils.getString(object,"creation_date","");
		entLicenseCopy = JSONUtils.getString(object,"ent_license_copy","");
		entLicenseSn = JSONUtils.getString(object,"ent_license_sn","");
		lastUpdate = JSONUtils.getString(object,"last_update","");

	}

	public String getCode(){
		return code;
	}

	public String getName(){
		return name;
	}

	public String getId(){
		return id;
	}

	public JSONObject toJSONObject(){
		JSONObject obj = new JSONObject();
		try {
			obj.put("code",code);
			obj.put("id",id);
			obj.put("name",name);
			obj.put("creation_date",creationDate);
			obj.put("ent_license_copy",entLicenseCopy);
			obj.put("ent_license_sn",entLicenseSn);
			obj.put("last_update",lastUpdate);

		}catch (Exception e){
			e.printStackTrace();
		}

		return obj;
	}
}
