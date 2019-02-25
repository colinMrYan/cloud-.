package com.inspur.emmcloud.bean.chat;

import com.inspur.emmcloud.util.common.JSONUtils;
import com.inspur.emmcloud.util.common.PinyinUtils;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class GetConversationListResult {
	private List<Conversation> conversationList = new ArrayList<>();

	public GetConversationListResult(String response) {
		JSONArray array = JSONUtils.getJSONArray(response,new JSONArray());
		for (int i = 0; i < array.length(); i++){
			JSONObject obj = JSONUtils.getJSONObject(array,i,new JSONObject());
			Conversation conversation = new Conversation(obj);
			String pinYin = PinyinUtils.getPingYin(conversation.getName());
			conversation.setpyFull(pinYin);
			conversationList.add(conversation);
		}
	}

	public List<Conversation> getConversationList() {
		return conversationList;
	}
}
