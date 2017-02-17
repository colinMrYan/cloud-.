package com.inspur.emmcloud.bean;

import org.json.JSONException;
import org.json.JSONObject;

public class GetSendMsgResult {

	private Msg msg;
	public GetSendMsgResult(String response) {
		
		try {
			JSONObject msgObj = new JSONObject(response);
			this.msg = new Msg(msgObj);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public Msg getMsg() {
		return msg;
	}

	public void setMsg(Msg msg) {
		this.msg = msg;
	}
	
}
