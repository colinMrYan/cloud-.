package com.inspur.emmcloud.bean.chat;


public class GetNewsImgResult {
    private String imgMsgBody = "";

    public GetNewsImgResult(String response) {
        imgMsgBody = response;
    }

    public String getImgMsgBody() {
        return imgMsgBody;
    }
//	public void setNewsImgPath(String newsImgPath) {
//		this.newsImgPath = newsImgPath;
//	}


}
