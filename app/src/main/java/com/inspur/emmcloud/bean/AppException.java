package com.inspur.emmcloud.bean;


import org.json.JSONObject;
import org.xutils.db.annotation.Column;
import org.xutils.db.annotation.Table;

import java.io.Serializable;


/**
 * Created by Administrator on 2017/4/25.
 */

@Table(name = "AppException")
public class AppException implements Serializable {
	@Column(name = "Id",isId = true)
	private int Id;
	@Column(name = "HappenTime")
	private long HappenTime = 0L;
	@Column(name = "AppVersion")
	private String AppVersion = "";
	@Column(name = "ErrorLevel")
	private int ErrorLevel = 3;
	@Column(name = "ErrorUrl")
	private String ErrorUrl = "";
	@Column(name = "ErrorInfo")
	private String ErrorInfo = "";
	@Column(name = "ErrorCode")
	private int ErrorCode = -1;

	public AppException() {
	}

	public AppException(long HappenTime, String AppVersion, int ErrorLevel, String ErrorUrl, String ErrorInfo, int ErrorCode) {
		this.HappenTime = HappenTime;
		this.AppVersion = AppVersion;
		this.ErrorLevel = ErrorLevel;
		this.ErrorUrl = ErrorUrl;
		this.ErrorInfo = ErrorInfo;
		this.ErrorCode = ErrorCode;
	}

	public int getId() {
		return Id;
	}

	public long getHappenTime() {
		return HappenTime;
	}

	public String getAppVersion() {
		return AppVersion;
	}

	public int getErrorLevel() {
		return ErrorLevel;
	}

	public String getErrorUrl() {
		return ErrorUrl;
	}

	public String getErrorInfo() {
		return ErrorInfo;
	}

	public int getErrorCode() {
		return ErrorCode;
	}

	public void setId(int Id) {
		this.Id = Id;
	}

	public void setHappenTime(long HappenTime) {
		this.HappenTime = HappenTime;
	}

	public void setAppVersion(String AppVersion) {
		this.AppVersion = AppVersion;
	}

	public void setErrorLevel(int ErrorLevel) {
		this.ErrorLevel = ErrorLevel;
	}

	public void setErrorUrl(String ErrorUrl) {
		this.ErrorUrl = ErrorUrl;
	}

	public void setErrorInfo(String ErrorInfo) {
		this.ErrorInfo = ErrorInfo;
	}

	public void setErrorCode(int ErrorCode) {
		this.ErrorCode = ErrorCode;
	}

	public JSONObject toJSONObject(){
		JSONObject object = new JSONObject();
		try {
			object.put("HappenTime",HappenTime);
			object.put("AppVersion",AppVersion);
			object.put("ErrorLevel",ErrorLevel);
			object.put("ErrorUrl",ErrorUrl);
			object.put("ErrorInfo",ErrorInfo);
			object.put("ErrorCode",ErrorCode);
		}catch (Exception e){
			e.printStackTrace();
		}
		return  object;
	}
}
