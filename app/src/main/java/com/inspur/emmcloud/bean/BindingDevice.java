package com.inspur.emmcloud.bean;

import com.inspur.emmcloud.util.JSONUtils;

import org.json.JSONObject;

import java.io.Serializable;

/**
 * Created by Administrator on 2017/5/10.
 */

public class BindingDevice implements Serializable{
	private String deviceId;
	private String deviceModel;
	private long deviceBindTime;

	public BindingDevice(){

	}

	public BindingDevice(JSONObject obj) {
		deviceId = JSONUtils.getString(obj, "udid", "");
		deviceModel = JSONUtils.getString(obj, "device_model", "");
	}

	public String getDeviceId(){
		return  deviceId;
	}

	public String getDeviceModel(){
		return deviceModel;
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
