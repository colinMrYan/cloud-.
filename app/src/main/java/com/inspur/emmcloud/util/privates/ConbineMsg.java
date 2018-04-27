package com.inspur.emmcloud.util.privates;

import android.content.Context;

import com.inspur.emmcloud.MyApplication;
import com.inspur.emmcloud.api.APIUri;
import com.inspur.emmcloud.bean.chat.Email;
import com.inspur.emmcloud.bean.chat.Message;
import com.inspur.emmcloud.bean.chat.Msg;
import com.inspur.emmcloud.bean.chat.MsgContentAttachmentCard;
import com.inspur.emmcloud.bean.chat.MsgContentTextPlain;
import com.inspur.emmcloud.bean.chat.Phone;
import com.inspur.emmcloud.bean.contact.Contact;
import com.inspur.emmcloud.util.common.PreferencesUtils;
import com.inspur.emmcloud.util.privates.cache.RobotCacheUtils;

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
		msgSend.setType(type);
		msgSend.setUid(userID);
		msgSend.setAvatar("");
		String UTCNow = TimeUtils.getCurrentUTCTimeString();
		msgSend.setTime(UTCNow);
		msgSend.setBody(body);
		return msgSend;
	}


	public static Msg conbineRobotMsg(Context context,String body, String robotUid, String type,String fakeMessageId) {
		Msg msgSend = new Msg();
		String title = RobotCacheUtils.getRobotById(context, robotUid).getName();
		msgSend.setMid(fakeMessageId);
		msgSend.setTitle(title);
		msgSend.setType(type);
		msgSend.setUid(robotUid);
		msgSend.setAvatar("");
		String UTCNow = TimeUtils.getCurrentUTCTimeString();
		msgSend.setTime(UTCNow);
		msgSend.setBody(body);
		return msgSend;
	}


	public static Message conbineTextPlainMsgRobot(String text, String cid, String fakeMessageId){
		Message msgRobot = new Message();
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
		msgRobot.setCreationDate(System.currentTimeMillis());
		MsgContentTextPlain msgContentTextPlain = new MsgContentTextPlain();
		msgContentTextPlain.setText(text);
		msgRobot.setContent(msgContentTextPlain.toString());
		return  msgRobot;
	}

	public static Message conbineReplyAttachmentCardMsg(Contact contact, String cid, String from, String fakeMessageId){
		Message msgRobot = new Message();
		msgRobot.setChannel(cid);
		msgRobot.setMessage("1.0");
		msgRobot.setId(fakeMessageId);
		msgRobot.setCreationDate(System.currentTimeMillis());
		msgRobot.setType("attachment/card");
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
		msgContentAttachmentCard.setAvatar(APIUri.getChannelImgUrl(MyApplication.getInstance(), contact.getInspurID()));
		msgContentAttachmentCard.setFirstName(contact.getRealName());
		msgContentAttachmentCard.setLastName("");
		msgContentAttachmentCard.setOrganization(contact.getOrgName());
		msgContentAttachmentCard.setTitle("");
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
