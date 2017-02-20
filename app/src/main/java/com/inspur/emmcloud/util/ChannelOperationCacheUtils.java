package com.inspur.emmcloud.util;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;

import com.inspur.emmcloud.bean.Channel;
import com.inspur.emmcloud.bean.ChannelOperationInfo;
import com.lidroid.xutils.db.sqlite.Selector;
import com.lidroid.xutils.db.sqlite.WhereBuilder;
import com.lidroid.xutils.db.table.DbModel;

/**
 * 频道列表缓存处理类
 * 
 * @author Administrator
 *
 */
public class ChannelOperationCacheUtils {

	/**
	 * 设置是否被置顶
	 * 
	 * @param context
	 * @param cid
	 * @param isChanelSetTop
	 * @param setTopTime
	 *            被置顶的时间
	 */
	public static void setChannelTop(Context context, String cid,
			boolean isChanelSetTop) {
		try {

			ChannelOperationInfo opInfo = DbCacheUtils.getDb(context).findById(
					ChannelOperationInfo.class, cid);
			if (opInfo == null) {
				opInfo = new ChannelOperationInfo();
				opInfo.setCid(cid);
				opInfo.setIsSetTop(isChanelSetTop);
				if (isChanelSetTop) {
					opInfo.setTopTime(System.currentTimeMillis());
				}
				DbCacheUtils.getDb(context).save(opInfo);
			} else {
				opInfo.setIsSetTop(isChanelSetTop);
				if (isChanelSetTop) {
					opInfo.setTopTime(System.currentTimeMillis());
				}
				DbCacheUtils.getDb(context).update(opInfo,
						WhereBuilder.b("cid", "=", cid), "isSetTop",
						"setTopTime");
			}

		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
	}

	/**
	 * 判断此频道是否被置顶
	 * 
	 * @param context
	 * @param cid
	 * @return
	 */
	public static boolean isChannelSetTop(Context context, String cid) {
		boolean isChannelSetTop = false;
		try {

			ChannelOperationInfo opInfo = DbCacheUtils.getDb(context).findById(
					ChannelOperationInfo.class, cid);
			if (opInfo != null) {
				isChannelSetTop = opInfo.getIsSetTop();
			}

		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}

		return isChannelSetTop;

	}

	/**
	 * 设置频道是否被隐藏
	 * 
	 * @param context
	 * @param cid
	 * @param isChanelHide
	 */
	public static void setChannelHide(Context context, String cid,
			boolean isChanelHide) {
		try {
			ChannelOperationInfo opInfo = DbCacheUtils.getDb(context).findById(
					ChannelOperationInfo.class, cid);
			if (opInfo == null) {
				opInfo = new ChannelOperationInfo();
				opInfo.setCid(cid);
				opInfo.setIsHide(isChanelHide);
				DbCacheUtils.getDb(context).save(opInfo);
			} else {
				opInfo.setIsHide(isChanelHide);
				DbCacheUtils.getDb(context).update(opInfo,
						WhereBuilder.b("cid", "=", cid), "isHide");
			}

		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
	}

	/**
	 * 获取置顶的频道操作信息列表
	 * 
	 * @param context
	 * @return
	 */
	public static List<ChannelOperationInfo> getSetTopChannelOpList(
			Context context) {
		List<ChannelOperationInfo> setTopChannelOpList = null;
		try {
			setTopChannelOpList = DbCacheUtils.getDb(context).findAll(
					Selector.from(ChannelOperationInfo.class)
							.where("isSetTop", "=", true)
							.and("isHide", "=", false)
							.orderBy("setTopTime", true));
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
		return setTopChannelOpList;
	}

//	/**
//	 * 获得置顶的频道列表
//	 * 
//	 * @param context
//	 * @return
//	 */
//	public static List<Channel> getSetTopChannelList(Context context) {
//		List<Channel> setTopChannelList = new ArrayList<Channel>();
//		try {
//			List<DbModel> dbModelList = DbCacheUtils.getDb(context)
//					.findDbModelAll(
//							Selector.from(ChannelOperationInfo.class)
//									.select("cid").where("isSetTop", "=", true)
//									.and("isHide", "=", false)
//									.orderBy("setTopTime", true));
//			for (int i = 0; i < dbModelList.size(); i++) {
//				Channel channel = ChannelCacheUtils.getChannel(context,
//						dbModelList.get(i).getString("cid"));
//				if (channel != null) {
//					setTopChannelList.add(channel);
//				}
//			}
//		} catch (Exception e) {
//			// TODO: handle exception
//			e.printStackTrace();
//		}
//		return setTopChannelList;
//	}

	/**
	 * 获取隐藏的频道操作信息列表
	 * 
	 * @param context
	 * @return
	 */
	public static List<ChannelOperationInfo> getHideChannelOpList(
			Context context) {
		List<ChannelOperationInfo> hideChannelOpList = null;
		try {
			hideChannelOpList = DbCacheUtils.getDb(context).findAll(
					Selector.from(ChannelOperationInfo.class).where("isHide",
							"=", true));
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
		return hideChannelOpList;
	}

//	/**
//	 * 获取隐藏的频道列表
//	 * 
//	 * @param context
//	 * @return
//	 */
//	public static List<Channel> getHideChannelList(Context context) {
//		List<Channel> hideChannelList = new ArrayList<Channel>();
//		try {
//			List<DbModel> dbModelList = DbCacheUtils.getDb(context)
//					.findDbModelAll(
//							Selector.from(ChannelOperationInfo.class)
//									.select("cid").where("isHide", "=", true));
//
//			for (int i = 0; i < dbModelList.size(); i++) {
//				String cid = dbModelList.get(i).getString("cid");
//				Channel channel = ChannelCacheUtils.getChannel(context, cid);
//
//				if (channel != null) {
//					// 如果隐藏的频道中有未读消息则取消隐藏
//					if (channel.getNewestMid() != null
//							&& !MsgReadIDCacheUtils.isMsgHaveRead(context, cid,
//									channel.getNewestMid())) {
//						setChannelHide(context, cid, false);
//					} else {
//						hideChannelList.add(channel);
//					}
//				}
//
//			}
//		} catch (Exception e) {
//			// TODO: handle exception
//			e.printStackTrace();
//		}
//		return hideChannelList;
//	}

}
