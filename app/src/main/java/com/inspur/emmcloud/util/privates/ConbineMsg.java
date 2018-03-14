package com.inspur.emmcloud.util.privates;

import android.content.Context;

import com.inspur.emmcloud.MyApplication;
import com.inspur.emmcloud.bean.chat.Email;
import com.inspur.emmcloud.bean.chat.Msg;
import com.inspur.emmcloud.bean.chat.MsgContentAttachmentCard;
import com.inspur.emmcloud.bean.chat.MsgContentTextPlain;
import com.inspur.emmcloud.bean.chat.MsgRobot;
import com.inspur.emmcloud.bean.chat.Phone;
import com.inspur.emmcloud.bean.contact.Contact;
import com.inspur.emmcloud.util.common.LogUtils;
import com.inspur.emmcloud.util.common.PreferencesUtils;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

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

	public static MsgRobot conbineTextPlainMsgRobot(String text,String cid,String fakeMessageId){
		MsgRobot msgRobot = new MsgRobot();
		msgRobot.setChannel(cid);
		msgRobot.setMessage("1.0");
		msgRobot.setId(fakeMessageId);
		msgRobot.setType("text/plain");
		JSONObject fromObj = new JSONObject();
		try {
			fromObj.put("user",MyApplication.getInstance().getUid());
			fromObj.put("enterprise",MyApplication.getInstance().getTanent());
		}catch (Exception e){
			e.printStackTrace();
		}
		msgRobot.setFrom(fromObj.toString());
		msgRobot.setTo("");
		msgRobot.setState("");
		msgRobot.setCreationDate(TimeUtils.getCurrentUTCTimeString());
		MsgContentTextPlain msgContentTextPlain = new MsgContentTextPlain();
		msgContentTextPlain.setText(text);
		msgRobot.setContent(msgContentTextPlain.toString());
		return  msgRobot;
	}

	public static  MsgRobot conbineReplyAttachmentCardMsg(Contact contact,String cid,String from,String fakeMessageId){
		MsgRobot msgRobot = new MsgRobot();
		msgRobot.setChannel(cid);
		msgRobot.setMessage("1.0");
		msgRobot.setId(fakeMessageId);
		msgRobot.setCreationDate(TimeUtils.getCurrentUTCTimeString());
		msgRobot.setType("attachment/card");
		msgRobot.setCreationDate(TimeUtils.getCurrentUTCTimeString());
		JSONObject fromObj = new JSONObject();
		try {
			fromObj.put("user",from);
			fromObj.put("enterprise",MyApplication.getInstance().getTanent());
		}catch (Exception e){
			e.printStackTrace();
		}
		msgRobot.setFrom(fromObj.toString());
		msgRobot.setTo("");
		msgRobot.setState("");
		MsgContentAttachmentCard msgContentAttachmentCard = new MsgContentAttachmentCard();
		msgContentAttachmentCard.setAvatar(MyApplication.getInstance().getUserPhotoUrl(contact.getInspurID()));
		msgContentAttachmentCard.setFirstName(contact.getRealName());
		msgContentAttachmentCard.setLastName("");
		msgContentAttachmentCard.setOrganization(contact.getOrgName());
		msgContentAttachmentCard.setTitle("");
		LogUtils.jasonDebug("contact.getInspurID()="+contact.getInspurID());
		msgContentAttachmentCard.setUid(contact.getInspurID());
		Email email = new Email("工作",contact.getEmail());
		List<Email> emailList = new ArrayList<>();
		emailList.add(email);
		msgContentAttachmentCard.setEmailList(emailList);
		Phone phone = new Phone("工作",contact.getMobile());
		List<Phone> phoneList = new ArrayList<>();
		phoneList.add(phone);
		msgContentAttachmentCard.setPhoneList(phoneList);
		msgRobot.setContent(msgContentAttachmentCard.toString());
		return msgRobot;
	}
}
