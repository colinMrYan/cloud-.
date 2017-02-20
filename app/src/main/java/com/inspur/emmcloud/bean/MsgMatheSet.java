package com.inspur.emmcloud.bean;

import com.lidroid.xutils.db.annotation.Id;

public class MsgMatheSet {
	@Id
	private String channelId = "";
	private String matheSetStr = "";
	public MsgMatheSet(){
		
	}

	public MsgMatheSet(String channelId,String matheSetStr){
		this.channelId =channelId;
		this.matheSetStr = matheSetStr;
	}
	public String getChannelId() {
		return channelId;
	}

	public void setChannelId(String channelId) {
		this.channelId = channelId;
	}

	public String getMatheSetStr() {
		return matheSetStr;
	}

	public void setMathSetStr(String matheSetStr) {
		this.matheSetStr = matheSetStr;
	}
}
