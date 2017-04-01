package com.inspur.emmcloud.util;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;

import android.content.Context;

import com.inspur.emmcloud.bean.Channel;
import com.lidroid.xutils.DbUtils;
import com.lidroid.xutils.db.sqlite.Selector;
import com.lidroid.xutils.db.sqlite.WhereBuilder;
import com.lidroid.xutils.exception.DbException;

/**
 * 频道列表缓存处理类
 * 
 * @author Administrator
 *
 */
public class ChannelCacheUtils {
	private static DbUtils db;
	private static final int SEARCH_ALL = 0;
	private static final int SEARCH_CONTACT = 2;
	private static final int SEARCH_CHANNELGROUP = 1;
	private static final int SEARCH_NOTHIING = 4;


	/**
	 * 存储频道列表
	 * 
	 * @param context
	 * @param channelList
	 */
	public static void saveChannelList(final Context context,
			final List<Channel> channelList) {

		// TODO Auto-generated method stub
		try {
			if (channelList == null || channelList.size() == 0) {
				return;
			}
			DbCacheUtils.getDb(context).saveOrUpdateAll(channelList);
		} catch (Exception e) {
			// TODO: handle exception
			LogUtils.debug("yfcLog", "机器人保存有异常："+e.getMessage());
			e.printStackTrace();
		}
	}
	
	/**
	 * 清除Channel表信息
	 * @param context
	 */
	public static void clearChannel(Context context) {
		try {
			DbCacheUtils.getDb(context).deleteAll(Channel.class);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * 存储频道列表
	 * 
	 * @param context
	 * @param channel
	 */
	public static void saveChannel(Context context, Channel channel) {
		try {
			if (channel == null) {
				return;
			}
			DbCacheUtils.getDb(context).saveOrUpdate(channel);
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}

	}


	/**
	 * 通过频道id获取频道名称
	 * 
	 * @param context
	 * @param cidArray
	 * @return
	 */
	public static JSONArray getChannelsName(Context context, JSONArray cidArray) {
		JSONArray jsonArray = new JSONArray();
		try {
			for (int i = 0; i < cidArray.length(); i++) {
				String name =DbCacheUtils.getDb(context).findById(Channel.class, cidArray.getString(i))
						.getTitle();
				jsonArray.put(i, name);
			}
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}

		return jsonArray;
	}

	/**
	 * 判断频道是否已经被置顶
	 * 
	 * @param context
	 * @param cid
	 * @return
	 */
	public static boolean getIsChannelSetTop(Context context, String cid) {
		boolean isChannelSetTop = false;
		try {
			isChannelSetTop = DbCacheUtils.getDb(context).findById(Channel.class, cid).getIsSetTop();
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
		return isChannelSetTop;

	}
	
	/**
	 * 获取频道的类型
	 *
	 * @param context
	 * @param cid
	 * @return
	 */
	public static String getChannelType(Context context, String cid){
		String type = "";
		try {
			if (!StringUtils.isBlank(cid)){
				type = DbCacheUtils.getDb(context).findById(Channel.class, cid).getType();
			}
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
		return type;
	}

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
			Channel channel = DbCacheUtils.getDb(context).findById(Channel.class, cid);
			channel.setIsSetTop(isChanelSetTop);
			if (isChanelSetTop) {
				channel.setTopTime(System.currentTimeMillis());
			}
			DbCacheUtils.getDb(context).update(channel, WhereBuilder.b("cid", "=", cid), "isSetTop",
					"setTopTime");
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
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
			Channel channel = db.findById(Channel.class, cid);
			channel.setIsHide(isChanelHide);
			DbCacheUtils.getDb(context).update(channel, WhereBuilder.b("cid", "=", cid), "isHide");
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
	}

	public static List<Channel> getCacheChannelList(Context context) {
		List<Channel> channelList = new ArrayList<Channel>();
		try {
			channelList = DbCacheUtils.getDb(context).findAll(Selector.from(Channel.class));
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
		if (channelList == null) {
			channelList = new ArrayList<Channel>();
		}
		return channelList;
	}
	

	//
	// /**
	// * 获取置顶的频道列表
	// *
	// * @param context
	// * @return
	// */
	// public static List<Channel> getSetTopChannelList(Context context) {
	// List<Channel> setTopChannelList = null;
	// try {
	// initDb(context);
	// setTopChannelList = db.findAll(Selector.from(Channel.class)
	// .where("isSetTop", "=", true).and("isHide", "=", false)
	// .orderBy("setTopTime", true));
	// } catch (Exception e) {
	// // TODO: handle exception
	// e.printStackTrace();
	// }
	// return setTopChannelList;
	// }

	// /**
	// * 获取隐藏的频道列表
	// *
	// * @param context
	// * @return
	// */
	// public static List<Channel> getHideChannelList(Context context) {
	// List<Channel> hideChannelList = null;
	// try {
	// initDb(context);
	// hideChannelList = db.findAll(Selector.from(Channel.class).where(
	// "isHide", "=", true));
	// } catch (Exception e) {
	// // TODO: handle exception
	// e.printStackTrace();
	// }
	// return hideChannelList;
	// }

	/**
	 * 获取缓存中的频道
	 * 
	 * @param context
	 * @param cid
	 * @return
	 */
	public static Channel getChannel(Context context, String cid) {
		Channel channel = null;
		try {
			channel = DbCacheUtils.getDb(context).findById(Channel.class, cid);
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
		return channel;
	}

	// /**
	// * 将缓存中的频道操作信息赋值给目标频道
	// *
	// * @param context
	// * @param channel
	// * @return
	// */
	// public static Channel setChannelOperationInfo(Context context,
	// Channel channel) {
	// Channel resultChannel = channel;
	// try {
	// initDb(context);
	// Channel cacheChannel = db.findById(Channel.class,
	// resultChannel.getCid());
	// if (cacheChannel != null) {
	// resultChannel.setIsHide(cacheChannel.getIsHide());
	// resultChannel.setIsSetTop(cacheChannel.getIsSetTop());
	// resultChannel.setTopTime(cacheChannel.getTopTime());
	// }
	//
	// } catch (Exception e) {
	// // TODO: handle exception
	// e.printStackTrace();
	// }
	// return resultChannel;
	// }

	// /**
	// * 将缓存中的频道操作信息赋值给目标频道列表
	// *
	// * @param context
	// * @param channelList
	// * @return
	// */
	// public static List<Channel> setChannelsOperationInfo(Context context,
	// List<Channel> channelList) {
	// if (channelList == null || channelList.size()==0) {
	// return null;
	// }
	// List<Channel> resultChannelList = new ArrayList<Channel>();
	// resultChannelList = channelList;
	// try {
	// initDb(context);
	// List<Channel> cacheChannelList = db.findAll(Selector
	// .from(Channel.class));
	// if (cacheChannelList != null) {
	// for (int i = 0; i < resultChannelList.size(); i++) {
	// Channel resultChannel = resultChannelList.get(i);
	// for (int j = 0; j < cacheChannelList.size(); j++) {
	// Channel cacheChannel = cacheChannelList.get(i);
	// if (resultChannel.getCid().equals(cacheChannel.getCid())) {
	// resultChannel.setIsHide(cacheChannel.getIsHide());
	// resultChannel.setIsSetTop(cacheChannel.getIsSetTop());
	// resultChannel.setTopTime(cacheChannel.getTopTime());
	// break;
	// }
	// }
	//
	// }
	// }
	// } catch (Exception e) {
	// // TODO: handle exception
	// e.printStackTrace();
	// }
	//
	// return resultChannelList;
	// }

	/**
	 * 搜索会话列表
	 * 
	 * @param context
	 * @param searchText
	 * @return
	 */
	public static List<Channel> getSearchChannelList(Context context,
			String searchText, int selectContent) {
		List<Channel> searchChannelList = null;
		if (!StringUtils.isBlank(searchText)) {

			try {
				String searchStr = "";
				for (int i = 0; i < searchText.length(); i++) {
					if (i < searchText.length() - 1) {
						searchStr += "%" + searchText.charAt(i);
					} else {
						searchStr += "%" + searchText.charAt(i) + "%";
					}
				}
				switch (selectContent) {
				case SEARCH_ALL:
				case SEARCH_NOTHIING:

					searchChannelList = DbCacheUtils.getDb(context).findAll(Selector
							.from(Channel.class)
							.where("type", " = ", "GROUP")
							.and(WhereBuilder.b("pyFull", "like", searchStr)
									.or("title", "like", searchStr)));
					break;
				case SEARCH_CHANNELGROUP:
					searchChannelList = DbCacheUtils.getDb(context).findAll(Selector
							.from(Channel.class)
							.where(WhereBuilder.b("type", "=", "GROUP"))
							.and(WhereBuilder.b("pyFull", "like", searchStr)
									.or("title", "like", searchStr)));
					break;
				case SEARCH_CONTACT:
//					searchChannelList = db.findAll(Selector
//							.from(Channel.class)
//							.where(WhereBuilder.b("type", "=", "DIRECT"))
//							.and(WhereBuilder.b("pyFull", "like", searchStr)
//									.or("title", "like", searchStr)));
				default:
					break;
				}

			} catch (Exception e) {
				// TODO: handle exception
				e.printStackTrace();
			}
		}
		if (searchChannelList == null) {
			searchChannelList = new ArrayList<Channel>();
		}
		return searchChannelList;

	}

	/**
	 * 判断此频道是否已存在
	 * 
	 * @param context
	 * @param channelID
	 * @return
	 */
	public static boolean isChannelExist(Context context, String channelID) {
		boolean isExist = false;
		try {
			Channel channel = DbCacheUtils.getDb(context).findById(Channel.class, channelID);
			if (channel != null) {
				isExist = true;
			}
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}

		return isExist;
	}
	
	
	
	/**
	 * 获取缓存中Channel的消息免打扰状态
	 * 
	 * @param context
	 * @param cid
	 * @return
	 */
	public static Boolean isChannelNotDisturb(Context context, String cid) {
		boolean isChannelNotDisturb = false;
		Channel channel = null;
		try {
			channel = DbCacheUtils.getDb(context).findById(Channel.class, cid);
			if (channel != null) {
				isChannelNotDisturb =channel.getDnd();
			}
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
		return isChannelNotDisturb;
	}
	

}
