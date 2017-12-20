package com.inspur.emmcloud.bean.chat;

public class GetAddMembersSuccessResult {

	private String channelInfo = "";
	public GetAddMembersSuccessResult(String response){
		
		this.channelInfo = response;
		
	}
	public String getChannelInfo() {
		return channelInfo;
	}
	public void setChannelInfo(String channelInfo) {
		this.channelInfo = channelInfo;
	}
	
	
}
