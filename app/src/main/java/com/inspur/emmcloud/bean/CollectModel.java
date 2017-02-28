package com.inspur.emmcloud.bean;

import android.content.Context;

import com.inspur.emmcloud.MyApplication;
import com.inspur.emmcloud.util.UriUtils;
import com.lidroid.xutils.db.annotation.Table;

@Table(name = "WebPVCollect")
public class CollectModel {
	private int id;
	private String FunctionID = "";
	private String FunctionType = "";
	private String UserID = "";
	private String TanentID = "";
	private long CollectTime = 0L;
	
	public CollectModel(){
		
	}
	
	public CollectModel(Context context,String functionID,String functionType){
		this.FunctionID = functionID;
		this.FunctionType = functionType;
		UserID = ((MyApplication)context.getApplicationContext()).getUid();
		TanentID = UriUtils.tanent;
		CollectTime = System.currentTimeMillis();
	}
	
	public String getFunctionID(){
		return FunctionID;
	}
	
	public String getFunctionType(){
		return FunctionType;
	}
	
	public String getUserID(){
		return UserID;
	}
	
	public String getTanentID(){
		return TanentID;
	}
	
	public long getCollectTime(){
		return CollectTime;
	}
	
	
	
	public void setFunctionID(String FunctionID){
		this.FunctionID = FunctionID;
	}
	
	public void setFunctionType(String FunctionType){
		this.FunctionType = FunctionType;
	}
	
	public void setUserID(String UserID){
		this.UserID = UserID;
	}
	
	public void setTanentID(String TanentID){
		this.TanentID = TanentID;
	}
	
	public void setCollectTime(long CollectTime){
		this.CollectTime = CollectTime;
	}
}
