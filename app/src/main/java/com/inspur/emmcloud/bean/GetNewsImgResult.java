package com.inspur.emmcloud.bean;


public class GetNewsImgResult {

	private static final String TAG = "GetNewsImgResult";
	
	private String imgMsgBody="";
	public GetNewsImgResult(String response){
		imgMsgBody = response;
	}
	public String getImgMsgBody() {
		return imgMsgBody;
	}
//	public void setNewsImgPath(String newsImgPath) {
//		this.newsImgPath = newsImgPath;
//	}
	
	
}
