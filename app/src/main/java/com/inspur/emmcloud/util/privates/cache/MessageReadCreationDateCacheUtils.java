package com.inspur.emmcloud.util.privates.cache;

import android.content.Context;

import com.inspur.emmcloud.bean.chat.MessageReadCreationDate;

/**
 * 标记消息已读、未读的工具类
 * 
 * @author Administrator
 *
 */
public class MessageReadCreationDateCacheUtils {
	/**
	 * 存储某个频道的已读到的消息创建时间
	 * @param context
	 * @param cid
	 * @param msgID
	 */
	public static void saveMessageReadCreationDate(Context context, String cid, long messageCreationDate) {
		try {
            DbCacheUtils.getDb(context).saveOrUpdate(new MessageReadCreationDate(cid, messageCreationDate));
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}

	}

	/**
	 * 判断消息已读或未读
	 * @param context
	 * @param cid
	 * @param messageCreationDate
	 * @return
	 */
	public static boolean isMessageHaveRead(Context context, String cid,
										long messageCreationDate) {
		try {
			MessageReadCreationDate messageReadCreationDate = DbCacheUtils.getDb(context).findById(MessageReadCreationDate.class, cid);
			if (messageReadCreationDate == null) {
				return false;
			}
			long currentMessageReadCreationDate = messageReadCreationDate.getMessageReadCreationDate();
			return currentMessageReadCreationDate>messageCreationDate;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();

		}
		return true;
	}

	/**
	 * 获取未读消息条数
	 * @param context
	 * @param cid
	 * @return
	 */
	public static int getNotReadMessageCount(Context context, String cid) {
		try {
			
			long targetMessageReadCreationDate = 0L;
			MessageReadCreationDate messageReadCreationDate = DbCacheUtils.getDb(context).findById(MessageReadCreationDate.class, cid);
			if (messageReadCreationDate != null) {
				targetMessageReadCreationDate = messageReadCreationDate.getMessageReadCreationDate();
			}
			return MessageCacheUtil.getNewerMessageCount(context,cid,targetMessageReadCreationDate);
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
			return 0;
		}

	}


}
