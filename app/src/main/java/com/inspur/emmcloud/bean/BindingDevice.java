package com.inspur.emmcloud.bean;

import com.inspur.emmcloud.util.JSONUtils;

import org.json.JSONObject;

/**
 * Created by Administrator on 2017/5/10.
 */

public class BindingDevice {
	private String deviceId;
	private String deviceType;

	public BindingDevice(JSONObject obj) {
		deviceId = JSONUtils.getString(obj, "deviceId", "");
		deviceType = JSONUtils.getString(obj, "deviceType", "");
	}
}
