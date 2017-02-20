package com.inspur.emmcloud.bean;

import com.lidroid.xutils.db.annotation.Id;

/**
 * classes : com.inspur.emmcloud.widget.DragGrid.AppItem Create at 2016年12月15日
 * 上午8:59:08
 */
public class AppCommonlyUse {

	@Id
	private String appID = "";
	private long lastUpdateTime = 0L;
	private int clickCount = 0;
	private double weight = 0;

	public AppCommonlyUse() {
	}
	
	public AppCommonlyUse(AppItem app){
		appID = app.getAppID();
		lastUpdateTime = app.getLastUpdateTime();
	}

	public String getAppID() {
		return appID;
	}

	public void setAppID(String appID) {
		this.appID = appID;
	}

	public long getLastUpdateTime() {
		return lastUpdateTime;
	}

	public void setLastUpdateTime(long lastUpdateTime) {
		this.lastUpdateTime = lastUpdateTime;
	}
	
	public int getClickCount() {
		return clickCount;
	}

	public void setClickCount(int clickCount) {
		this.clickCount = clickCount;
	}
	
	public double getWeight() {
		return weight;
	}

	public void setWeight(double weight) {
		this.weight = weight;
	}

	@Override
	public boolean equals(Object other) {
		if(this == other){
			return true;
		}
		if(other == null){
			return false;
		}
		if(!(other instanceof AppCommonlyUse)){
			return false;
		}
		AppCommonlyUse appCommonlyUse = (AppCommonlyUse) other;
		if(!getAppID().equals(appCommonlyUse.getAppID())){
			return false;
		}
		return true;
	}

}
