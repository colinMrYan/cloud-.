package com.inspur.emmcloud.bean;


import org.xutils.db.annotation.Column;
import org.xutils.db.annotation.Table;

@Table(name = "MsgReadId")
public class MsgReadId {
	@Column(name = "channelId",isId = true)
	private String channelId = "";
	@Column(name = "msgReadId")
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
