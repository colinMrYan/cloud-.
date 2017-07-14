package com.inspur.emmcloud.util;

import android.content.Context;

import com.inspur.emmcloud.MyApplication;
import com.inspur.emmcloud.bean.Msg;

public class ConbineMsg {

	/**
	 * 组织消息添加到频道消息列表
	 * 
	 * @param body
	 * @return
	 */
	public static Msg conbineMsg(Context context,String body, String title, String type,String fakeMessageId) {
		String userID = ((MyApplication)context.getApplicationContext()).getUid();
		Msg msgSend = new Msg();
		title = PreferencesUtils.getString(context,
					"userRealName");
		msgSend.setMid(fakeMessageId);
		msgSend.setTitle(title);
		if (type.equals("text")) {
			msgSend.setType("text");
		} else if (type.equals("image")) {
			msgSend.setType("image");
		}else if (type.equals("res_image")) {
			msgSend.setType("res_image");
		}else if(type.equals("res_file")){
			msgSend.setType("res_file");
		}else if(type.equals("txt_rich")){
			msgSend.setType("txt_rich");
		}

		msgSend.setUid(userID);
		msgSend.setAvatar("");
		// msgSend.setOrder(0);
		String UTCNow = TimeUtils.getCurrentUTCTimeString();
		msgSend.setTime(UTCNow);
		if(type.equals("txt_rich")){
			msgSend.setBody(body);
		}else {
			msgSend.setBody(body);
		}

		return msgSend;
	}
}
