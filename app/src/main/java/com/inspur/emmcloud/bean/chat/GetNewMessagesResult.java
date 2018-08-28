package com.inspur.emmcloud.bean.chat;


import com.inspur.emmcloud.util.common.JSONUtils;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class GetNewMessagesResult {
	private JSONObject messageObj;
	private List<Message> allMessageList = new ArrayList<>();
	public GetNewMessagesResult(String response) {
		messageObj = JSONUtils.getJSONObject(response);
		Iterator<String> keys = messageObj.keys();
		while(keys.hasNext()){
			String key = keys.next();
			JSONArray array = JSONUtils.getJSONArray(messageObj,key,new JSONArray());
			for (int i = 0; i < array.length(); i++) {
				JSONObject obj = JSONUtils.getJSONObject(array,i,new JSONObject());
				allMessageList.add(new Message(obj));
			}
		}
	}

	public List<Message> getAllMessageList() {
		return allMessageList;
	}

	public void setAllMessageList(List<Message> allMessageList) {
		this.allMessageList = allMessageList;
	}

	public List<Message> getNewMessageList(String cid) {
		List<Message> messageList = new ArrayList<>();
		JSONArray array = JSONUtils.getJSONArray(messageObj,cid,new JSONArray());
		for (int i = 0; i < array.length(); i++) {
			JSONObject obj = JSONUtils.getJSONObject(array,i,new JSONObject());
			messageList.add(new Message(obj));
		}
		return messageList;
	}
}
