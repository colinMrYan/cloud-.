package com.inspur.emmcloud.bean;

import org.json.JSONException;
import org.json.JSONObject;

public class getMsgResult {

	private Msg msg;
	public getMsgResult(String response){
		try {
			JSONObject msgObj = new JSONObject(response);
			this.msg = new Msg(msgObj);
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	public Msg getMsg(){
		return msg;
	}
}
