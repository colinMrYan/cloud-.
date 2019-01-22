package com.inspur.emmcloud.bean.chat;

import org.json.JSONException;
import org.json.JSONObject;

import android.text.TextUtils;

public class GetFileUploadResult {
	
	private String response = "";
	public GetFileUploadResult(String response){
		this.response = response;
	}
	public String getFileMsgBody() {
		return response;
	}
}
