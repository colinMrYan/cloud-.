package com.inspur.emmcloud.bean; 

import com.lidroid.xutils.db.annotation.Id;

/**
 * classes : com.inspur.emmcloud.bean.AppOrder
 * Create at 2016年12月17日 下午2:53:19
 */
public class AppOrder {

	@Id
	private String appID = "";
	private String orderId = "";
	private String categoryID = "";
	public AppOrder() {
	}
	public String getAppID() {
		return appID;
	}
	public void setAppID(String appID) {
		this.appID = appID;
	}
	public String getOrderId() {
		return orderId;
	}
	public void setOrderId(String orderId) {
		this.orderId = orderId;
	}
	public String getCategoryID() {
		return categoryID;
	}
	public void setCategoryID(String categoryID) {
		this.categoryID = categoryID;
	}
	@Override
	public boolean equals(Object other) {
		if(this == other){
			return true;
		}
		if(other == null){
			return false;
		}
		if(!(other instanceof AppOrder)){
			return false;
		}
		AppOrder appOrder = (AppOrder) other;
		if(!getAppID().equals(appOrder.getAppID())){
			return false;
		}
		return true;
	}
}
 