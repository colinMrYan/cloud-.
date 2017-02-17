package com.inspur.emmcloud.util;

import android.content.Context;

import com.inspur.emmcloud.bean.Contact;
import com.inspur.emmcloud.bean.Robot;

public class DirectChannelUtils {

	/**
	 * 获取单聊频道的title
	 *
	 * @param context
	 * @param msgTitle
	 * @return
	 */
	public static String getDirectChannelTitle(Context context, String msgTitle) {
		String channelTitle = msgTitle;
		Contact otherContact = getDirctChannelOtherContact(context, msgTitle);
		if (otherContact != null) {
			channelTitle = otherContact.getRealName();
		}
		return channelTitle;
	}

	/**
	 * 获取单聊频道icon的url
	 *
	 * @param context
	 * @param msgTitle
	 * @return
	 */
	public static String getDirectChannelIcon(Context context, String msgTitle) {
		String channelIcon = "";
		Contact otherContact = getDirctChannelOtherContact(context, msgTitle);
		if (otherContact != null) {
			String otherInspurID = otherContact.getInspurID();
			channelIcon = UriUtils.getChannelImgUri(otherInspurID);
		}
		return channelIcon;
	}
	
	/**
	 * 机器人头像
	 * @param context
	 * @param title
	 * @return
	 */
	public static String getRobotIcon(Context context,String title){
		String robotIcon = "";
		Robot robot = getRobotInfo(context, title);
		if(robot != null){
			robotIcon = UriUtils.getRobotIconUri(robot.getAvatar());
		}
		return robotIcon;
	}

	/**
	 * 获取单聊对方的contact
	 *
	 * @param context
	 * @param title
	 * @return
	 */
	public static Contact getDirctChannelOtherContact(Context context,
			String title) {
		Contact contact = null;
		try {
			String[] uidArray = title.split("-");
			String myUid = PreferencesUtils.getString(context, "userID");
			String otherUid = "";
			if (uidArray[0].equals(myUid)) {
				otherUid = uidArray[1];
			} else {
				otherUid = uidArray[0];
			}
			contact = ContactCacheUtils.getUserContact(context, otherUid);
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
		return contact;
	}
	
	/**
	 * 
	 * @return
	 */
	public static Robot getRobotInfo(Context context,
			String title){
		Robot robot = null;
		try {
			String[] uidArray = title.split("-");
			String myUid = PreferencesUtils.getString(context, "userID");
			String otherUid = "";
			if (uidArray[0].equals(myUid)) {
				otherUid = uidArray[1];
			} else {
				otherUid = uidArray[0];
			}
			robot = RobotCacheUtils.getRobotById(context, otherUid);
		} catch (Exception e) {
			e.printStackTrace();
		}
		if(robot == null){
			robot = new Robot();
		}
		return robot;
	}

}
