package com.inspur.emmcloud.bean.chat;


import com.inspur.emmcloud.util.common.JSONUtils;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class GetNewMessagesResult {
	private JSONObject messageObj;
	public GetNewMessagesResult(String response) {
		messageObj = JSONUtils.getJSONObject(response);
	}

	public List<Message> getNewMessageList(String cid) {
		List<Message> messageList = new ArrayList<>();
//		JSONArray array = JSONUtils.getJSONArray(messageObj,cid,new JSONArray());
//		for (int i = 0; i < array.length(); i++) {
//			JSONObject obj = JSONUtils.getJSONObject(array,i,new JSONObject());
//			messageList.add(new Message(obj));
//		}
		return messageList;
	}
}
