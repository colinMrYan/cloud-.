package com.inspur.emmcloud.bean;

import com.inspur.emmcloud.util.JSONUtils;

import org.json.JSONObject;

import java.io.Serializable;

/**
 * Created by Administrator on 2017/5/10.
 */

public class BindingDevice implements Serializable{
	private String deviceId="";
	private String deviceModel="";
	private long deviceLastUserTime =0L;

	public BindingDevice(){

	}

	public BindingDevice(JSONObject obj) {
		deviceId = JSONUtils.getString(obj, "udid", "");
		deviceModel = JSONUtils.getString(obj, "device_model", "");
		deviceLastUserTime =JSONUtils.getLong(obj,"last_use_time",0L);
	}

	public String getDeviceId(){
		return  deviceId;
	}

	public String getDeviceModel(){
		return deviceModel;
	}

	public long getDeviceLastUserTime(){return deviceLastUserTime;}
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
