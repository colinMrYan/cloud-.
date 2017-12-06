package com.inspur.emmcloud.util;

import android.content.Context;

import com.inspur.emmcloud.bean.MsgReadId;

/**
 * 标记消息已读、未读的工具类
 * 
 * @author Administrator
 *
 */
public class MsgReadIDCacheUtils {
	/**
	 * 将消息标示为已读消息，将此消息存储在最后一条已读消息preference中。
	 * 
	 * @param context
	 * @param cid
	 * @param msgID
	 */
	public static void saveReadedMsg(Context context, String cid, String msgID) {
		try {
            DbCacheUtils.getDb(context).saveOrUpdate(new MsgReadId(cid, msgID));
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}

	}

	/***
	 * 判断消息已读或未读
	 * 
	 * @param context
	 * @param senderUid
	 *            消息发送人uid
	 * @param cid
	 * @param msgID
	 * @return
	 */
	public static boolean isMsgHaveRead(Context context, String cid,
			String msgID) {
		try {
			
			MsgReadId msgReadId = DbCacheUtils.getDb(context).findById(MsgReadId.class, cid);
			if (msgReadId == null) {
				return false;
			}
			String readedMid = msgReadId.getMsgReadId();
			Long readedMidMathe = Long.valueOf(readedMid);
			long msgIDMathe = Long.valueOf(msgID);
			if (readedMidMathe < msgIDMathe) {
				return false;
			}

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();

		}
		return true;
	}

	/**
	 * 获取未读消息条数
	 * 
	 * @param context
	 * @param cid
	 * @return
	 */
	public static int getNotReadMsgCount(Context context, String cid) {
		try {
			
			String targetId = "0";
			MsgReadId msgReadId = DbCacheUtils.getDb(context).findById(MsgReadId.class, cid);
			if (msgReadId != null) {
				targetId = msgReadId.getMsgReadId();
			}
			return MsgCacheUtil.getNewerMsgCount(context, cid, targetId);
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
			return 0;
		}

	}


}
