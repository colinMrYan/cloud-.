package com.inspur.emmcloud.bean;

import java.util.ArrayList;
import java.util.List;

import javax.security.auth.PrivateCredentialPermission;

import org.json.JSONArray;

import com.inspur.emmcloud.util.LogUtils;

public class GetSearchChannelGroupResult {
	private List<ChannelGroup> searchChannelGroupList = new ArrayList<ChannelGroup>();

	public GetSearchChannelGroupResult(String response) {
		try {
			JSONArray array = new JSONArray(response);
			for (int i = 0; i < array.length(); i++) {
				ChannelGroup  searchChannelGroup = new ChannelGroup(array.getJSONObject(i));
				searchChannelGroupList.add(searchChannelGroup);
			}
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}

	}
	
	public List<ChannelGroup> getSearchChannelGroupList(){
		return searchChannelGroupList;
	}
}
