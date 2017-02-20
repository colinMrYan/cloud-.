package com.inspur.emmcloud.bean;

import com.lidroid.xutils.db.annotation.Id;

public class MsgReadId {
	@Id
	private String channelId = "";
	private String msgReadId = ""; //已读最新消息的id
	public MsgReadId(){
		
	}

	public MsgReadId(String channelId,String msgReadId){
		this.channelId =channelId;
		this.msgReadId = msgReadId;
	}
	public String getChannelId() {
		return channelId;
	}

	public void setChannelId(String channelId) {
		this.channelId = channelId;
	}

	public String getMsgReadId() {
		return msgReadId;
	}

	public void setMsgReadId(String msgReadId) {
		this.msgReadId = msgReadId;
	}
}
