package com.inspur.emmcloud.bean;

import org.json.JSONObject;

import android.content.Context;

import com.inspur.emmcloud.util.FileUtils;
import com.inspur.emmcloud.util.TimeUtils;
import com.inspur.emmcloud.util.UriUtils;

public class GroupFileInfo {
	private String url = "";
	private String name = "";
	private String size = "";
	private String time = "";
	private String owner = "";
	public GroupFileInfo(Msg msg){
		owner = msg.getTitle();
		time = msg.getTime();
		try {
			JSONObject jsonObject = new JSONObject(msg.getBody());
			if (jsonObject.has("size")) {
				size = jsonObject.getString("size");
			}
			if (jsonObject.has("name")) {
				name = jsonObject.getString("name");
			}
			if (jsonObject.has("key")) {
				url = jsonObject.getString("key");
				url = UriUtils.getPreviewUri(url);
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public String getUrl(){
		return url;
	}
	
	public String getName(){
		return name;
	}
	public String getSize(){
		return FileUtils.formatFileSize(size);
	}
	
	public String getTime(Context context){
		return TimeUtils.UTCString2YMDString(context,time);
	}
	
	public String getOwner(){
		return owner;
	}
}
