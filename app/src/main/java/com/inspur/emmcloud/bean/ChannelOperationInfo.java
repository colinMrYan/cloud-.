package com.inspur.emmcloud.bean;

import com.lidroid.xutils.db.annotation.Id;
import com.lidroid.xutils.db.annotation.Table;

/**
 * 会话的操作信息类
 * @author Administrator
 *
 */
@Table(name = "ChannelOperationInfo")
public class ChannelOperationInfo {
	@Id
	private String cid ="";
	private boolean isSetTop = false;
	private boolean isHide = false;
	private long setTopTime = 0;
	public ChannelOperationInfo(){
			
	}
	public String getCid() {
		return cid;
	}

	public void setCid(String cid) {
		this.cid = cid;
	}
	
	public boolean getIsSetTop() {
		return isSetTop;
	}

	public void setIsSetTop(boolean isSetTop) {
		this.isSetTop = isSetTop;
	}

	public boolean getIsHide() {
		return isHide;
	}

	public void setIsHide(boolean isHide) {
		this.isHide = isHide;
	}

	public void setTopTime(long setTopTime) {
		this.setTopTime = setTopTime;
	}

	public long getTopTime() {
		return setTopTime;
	}
}
