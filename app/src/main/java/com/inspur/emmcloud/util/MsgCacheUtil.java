package com.inspur.emmcloud.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import android.content.Context;

import com.inspur.emmcloud.bean.MatheSet;
import com.inspur.emmcloud.bean.Msg;
import com.lidroid.xutils.db.sqlite.Selector;
import com.lidroid.xutils.db.sqlite.WhereBuilder;
import com.lidroid.xutils.exception.DbException;

/**
 * 消息缓存处理类
 * 
 * @author Administrator
 *
 */
public class MsgCacheUtil {

	/**
	 * 
	 * @param context
	 * @param msg
	 */
	public static void saveMsg(final Context context, final Msg msg) {
		try {
			
			DbCacheUtils.getDb(context).saveOrUpdate(msg); // 存储消息
			// String cid = msg.getCid();
			// String msgStarId = msg.getMid();
			// String channelMsgStar = PreferencesUtils.getString(context,
			// "msgIDSet", "");
			// if (!StringUtils.isBlank(channelMsgStar)) {
			// JSONObject jsonObject = new JSONObject(channelMsgStar);
			// if (jsonObject.has(cid)
			// && !StringUtils.isBlank(jsonObject.getString(cid))) {
			// msgStarId = jsonObject.getString(cid);
			// }
			// }
			// MsgMatheSetCacheUtils.add(context, cid,
			// new MatheSet(msgStarId, msg.getMid())); // 存储消息id片段
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * 
	 * @param context
	 * @param msgList
	 * @param targetMsgId
	 */
	public static void saveMsgList(final Context context,
			final List<Msg> msgList, final String targetMsgId) {

		Runnable runnable = new Runnable() {

			@Override
			public void run() {
				// TODO Auto-generated method stub
				try {
					if (msgList == null || msgList.size() == 0) {
						return;
					}
					
					DbCacheUtils.getDb(context).saveOrUpdateAll(msgList);
					MatheSet matheSet = new MatheSet();
					if (StringUtils.isBlank(targetMsgId)) {
						matheSet.setStart(msgList.get(0).getMid());
						matheSet.setEnd(msgList.get(msgList.size() - 1)
								.getMid());
					} else {
						matheSet.setStart(msgList.get(0).getMid());
						matheSet.setEnd(targetMsgId);
					}
					MsgMatheSetCacheUtils.add(context, msgList.get(0).getCid(),
							matheSet);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		};
		new Thread(runnable).start();

	}

	/**
	 * 
	 * @param context
	 * @param channelID
	 *            所属的频道
	 * @param targetID
	 *            目表消息的id
	 * @param num
	 *            获取消息记录的条数
	 * @return
	 */
	public static List<Msg> getHistoryMsgList(Context context,
			String channelID, String targetID, int num) {
		List<Msg> msgList = null;
		try {
			
			if (StringUtils.isBlank(targetID)) {
				msgList = DbCacheUtils.getDb(context).findAll(Selector.from(Msg.class)
						.where("cid", "=", channelID).orderBy("mid", true)
						.limit(num));
			} else {
				msgList = DbCacheUtils.getDb(context).findAll(Selector.from(Msg.class)
						.where("mid", "<", targetID).and("cid", "=", channelID)
						.orderBy("mid", true).limit(num));
			}
			if (msgList != null && msgList.size() > 1) {
				Collections.reverse(msgList);
			}

		} catch (DbException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if (msgList == null) {
			msgList = new ArrayList<Msg>();
		}
		return msgList;
	}

	/**
	 * 获取频道最新的消息
	 *
	 * @param context
	 * @param channelID
	 * @return
	 */
	public static Msg getNewMsg(Context context, String channelID) {
		Msg msg = null;
		try {
			
			 msg = DbCacheUtils.getDb(context).findFirst(Selector.from(Msg.class)
					.where("cid", "=", channelID).orderBy("mid", true));
		} catch (DbException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return msg;
	}


	/**
	 * @param context
	 * @param channelID
	 *            所属频道
	 * @param targetId
	 *            目标消息id
	 * @param num
	 *            加载历史记录的条数
	 * @return 本地是否有足够多的缓存的数据
	 */
	public static boolean isDataInLocal(Context context, String channelID,
			String targetId, int num) {
		// TODO Auto-generated method stub
		try {
			
			MatheSet matheSet = MsgMatheSetCacheUtils.getInMatheSet(context,
					channelID, targetId);
			if (matheSet == null) {
				return false;
			}
			long mathSetStart = matheSet.getStart();
			// 此处获取count后减1，因为要获取targetID以前的数据不能包含自己
			long continuousCount = DbCacheUtils.getDb(context).count(Selector
					.from(Msg.class)
					.where("mid", "between",
							new String[] { mathSetStart + "", targetId })
					.and("mid", "!=", targetId).and("cid", "=", channelID));
			if (continuousCount >= num) {
				return true;
			}
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}

		return false;
	}

	/**
	 * 通过消息id获取消息
	 * 
	 * @param context
	 * @param mid
	 * @return
	 */
	public static Msg getCacheMsg(Context context, String mid) {
		Msg cacheMsg = null;
		try {
			
			cacheMsg = DbCacheUtils.getDb(context).findById(Msg.class, mid);
		} catch (DbException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return cacheMsg;
	}

	/**
	 * 获取频道内比目标消息更新的消息的条数
	 * 
	 * @param context
	 * @param cid
	 * @param mid
	 * @return
	 */
	public static int getNewerMsgCount(Context context, String cid, String mid) {
		int count = 0;
		try {
			
			count = (int) DbCacheUtils.getDb(context).count(Selector.from(Msg.class)
					.where("mid", ">", mid).and("cid", "=", cid));
		} catch (DbException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return count;

	}

	/**
	 * 获取特定频道图片类型的消息
	 * 
	 * @param context
	 * @param cid
	 * @return
	 */
	public static List<Msg> getImgTypeMsgList(Context context, String cid) {
		List<Msg> imgTypeMsgList = null;
		try {
			
			imgTypeMsgList = DbCacheUtils.getDb(context).findAll(Selector
					.from(Msg.class)
					.where("cid", "=", cid)
					.and(WhereBuilder.b("type", "=", "image").or("type", "=",
							"res_image")).orderBy("mid", true));
		} catch (DbException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if (imgTypeMsgList == null) {
			imgTypeMsgList = new ArrayList<Msg>();
		}
		return imgTypeMsgList;

	}

	/**
	 * 获取特定频道图片类型的消息
	 * 
	 * @param context
	 * @param cid
	 * @return
	 */
	public static List<Msg> getFileTypeMsgList(Context context, String cid) {
		List<Msg> fileTypeMsgList = null;
		try {
			
			fileTypeMsgList = DbCacheUtils.getDb(context).findAll(Selector.from(Msg.class)
					.where("cid", "=", cid).and("type", "=", "res_file")
					.orderBy("mid", true));
		} catch (DbException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if (fileTypeMsgList == null) {
			fileTypeMsgList = new ArrayList<Msg>();
		}
		return fileTypeMsgList;

	}

}
