package com.inspur.emmcloud.bean;

import com.inspur.emmcloud.util.JSONUtils;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2017/5/10.
 */

public class BindingDevice implements Serializable{
	private String deviceId="";
	private String deviceModel="";
	private long deviceBindTime=0L;
	private List<BindingDeviceLog> bindingDeviceLogList = new ArrayList<>();

	public BindingDevice(){

	}

	public BindingDevice(JSONObject obj) {
		deviceId = JSONUtils.getString(obj, "udid", "");
		deviceModel = JSONUtils.getString(obj, "device_model", "");
		deviceBindTime=JSONUtils.getLong(obj,"create_time",0L);
		JSONArray array = JSONUtils.getJSONArray(obj,"logs",new JSONArray());
		for(int i=0;i<array.length();i++){
			JSONObject jsonObject = JSONUtils.getJSONObject(array,i,new JSONObject());
			BindingDeviceLog bindingDeviceLog = new BindingDeviceLog(jsonObject);
			bindingDeviceLogList.add(bindingDeviceLog);
		}
	}

	public String getDeviceId(){
		return  deviceId;
	}

	public String getDeviceModel(){
		return deviceModel;
	}

	public List<BindingDeviceLog> getBindingDeviceLogList() {
		return bindingDeviceLogList;
	}

	public long getDeviceBindTime(){return  deviceBindTime;}
	@Override
	public boolean equals(Object other) {
		if(this == other){
			return true;
		}
		if(other == null){
			return false;
		}
		if(!(other instanceof BindingDevice)){
			return false;
		}
		BindingDevice bindingDevice = (BindingDevice) other;
		//此处从==判断是否相等  改为equals
		if(!(getDeviceId().equals(bindingDevice.getDeviceId()))){
			return false;
		}
		return true;
	}
}
