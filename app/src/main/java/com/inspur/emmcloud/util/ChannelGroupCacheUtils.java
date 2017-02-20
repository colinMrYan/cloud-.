package com.inspur.emmcloud.util;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Environment;
import android.util.ArrayMap;
import android.util.Log;

import com.inspur.emmcloud.bean.ChannelGroup;
import com.inspur.emmcloud.bean.Contact;
import com.inspur.emmcloud.bean.Msg;
import com.inspur.emmcloud.bean.Channel;
import com.inspur.emmcloud.bean.MsgMatheSet;
import com.lidroid.xutils.DbUtils;
import com.lidroid.xutils.DbUtils.DbUpgradeListener;
import com.lidroid.xutils.db.sqlite.Selector;
import com.lidroid.xutils.db.sqlite.WhereBuilder;
import com.lidroid.xutils.db.table.Table;

/**
 * 频道列表缓存处理类
 * 
 * @author Administrator
 *
 */
public class ChannelGroupCacheUtils {

	/**
	 * 存储群组列表
	 * 
	 * @param context
	 * @param channelGroupList
	 */
	public static void saveChannelGroupList(final Context context,
			final List<ChannelGroup> channelGroupList) {
		try {
			if (channelGroupList == null || channelGroupList.size() == 0) {
				return;
			}
			DbCacheUtils.getDb(context).saveOrUpdateAll(channelGroupList);
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}

	}
	
	/**
	 * 存储群组信息
	 * 
	 * @param context
	 * @param channelGroup
	 */
	public static void saveChannelGroup(final Context context,
			final ChannelGroup channelGroup) {
		try {
			if (channelGroup == null ) {
				return;
			}
			DbCacheUtils.getDb(context).saveOrUpdate(channelGroup);
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}

	}

	/**
	 * 获取所有群组
	 * 
	 * @param context
	 * @return
	 */
	public static List<ChannelGroup> getAllChannelGroupList(Context context) {
		List<ChannelGroup> ChannelGroupList = null;

		try {
			ChannelGroupList = DbCacheUtils.getDb(context).findAll(ChannelGroup.class);
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
		if (ChannelGroupList == null) {
			ChannelGroupList = new ArrayList<ChannelGroup>();
		}
		return ChannelGroupList;

	}
	
	public static ChannelGroup getChannelGroupById(Context context,String cid){
		ChannelGroup channelGroup = null;
		try {
			 channelGroup = DbCacheUtils.getDb(context).findById(ChannelGroup.class, cid);
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
		return channelGroup;
	}

	/**
	 * 搜索群组
	 * 
	 * @param context
	 * @param searchText
	 * @return
	 */
	public static List<ChannelGroup> getSearchChannelGroupList(Context context,
			String searchText) {
		List<ChannelGroup> searchChannelGroupList = null;
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
				searchChannelGroupList = DbCacheUtils.getDb(context).findAll(Selector
						.from(ChannelGroup.class)
						.where("pyFull", "like", searchStr)
						.or("pyShort", "like", searchStr).or("channelName", "like", searchStr));
			} catch (Exception e) {
				// TODO: handle exception
				e.printStackTrace();
			}
		}
		if (searchChannelGroupList == null) {
			searchChannelGroupList = new ArrayList<ChannelGroup>();
		}
		return searchChannelGroupList;

	}

	/**
	 * 获取群组中成员列表
	 * 
	 * @param context
	 * @param cid
	 * @param limit
	 * @return
	 */
	public static List<Contact> getMembersList(Context context, String cid,
			int limit) {
		List<Contact> userList = new ArrayList<Contact>();
		try {
			ChannelGroup channelGroup = DbCacheUtils.getDb(context).findById(ChannelGroup.class, cid);
			if (channelGroup != null) {
				JSONArray memberArray = new JSONArray();
				JSONArray allMemberArray = channelGroup.getMembersArray();
				if (limit == 0 ) {
					memberArray = allMemberArray;
				}else {
					int size = allMemberArray.length();
					if (size<limit) {
						limit = size;
					}
					for (int i = 0; i < limit; i++) {
						String uid = allMemberArray.getString(i);
						memberArray.put(i, uid);
					}
				}
				
				userList = ContactCacheUtils.getSoreUserList(context, memberArray);
			}
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
		if (userList == null) {
			userList = new ArrayList<Contact>();
		}
		return userList;

	}

	public static List<Contact> getMembersList(Context context, String cid) {
		return getMembersList(context, cid, 0);
	}

	public static List<Map<String, String>> getMembersMapList(Context context,
			String cid) {
		List<Map<String, String>> userMapList = new ArrayList<Map<String, String>>();
		try {
			ChannelGroup channelGroup = DbCacheUtils.getDb(context).findById(ChannelGroup.class, cid);
			if (channelGroup != null) {
				String members = channelGroup.getMembers();
				JSONArray memberArray = new JSONArray(members);
				userMapList = ContactCacheUtils
						.getUserMap(context, memberArray);
			}
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
		if (userMapList == null) {
			userMapList = new ArrayList<Map<String, String>>();
		}
		return userMapList;

	}

	/**
	 * 更改群组名称
	 * @param context
	 * @param cid
	 * @param name
	 */
	public static void updateChannelGroupName(Context context,String cid,String name){
		try {
			ChannelGroup channelGroup = DbCacheUtils.getDb(context).findById(ChannelGroup.class, cid);
			if (channelGroup != null) {
				channelGroup.setChannelName(name);
				DbCacheUtils.getDb(context).update(channelGroup, WhereBuilder.b("cid", "=", cid), "channelName");
			}
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
	}
	
	/**
	 * 更改群组名称全拼
	 * @param context
	 * @param cid
	 * @param name
	 */
	public static void updateChannelGroupNameFull(Context context,String cid,String name){
		try {
			ChannelGroup channelGroup = DbCacheUtils.getDb(context).findById(ChannelGroup.class, cid);
			if (channelGroup != null) {
				channelGroup.setChannelName(name);
				DbCacheUtils.getDb(context).update(channelGroup, WhereBuilder.b("cid", "=", cid), "pyFull");
			}
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
	}
	
	/**
	 * 更改群组名称缩写
	 * @param context
	 * @param cid
	 * @param name
	 */
	public static void updateChannelGroupNameShort(Context context,String cid,String name){
		try {
			ChannelGroup channelGroup = DbCacheUtils.getDb(context).findById(ChannelGroup.class, cid);
			if (channelGroup != null) {
				channelGroup.setChannelName(name);
				DbCacheUtils.getDb(context).update(channelGroup, WhereBuilder.b("cid", "=", cid), "pyShort");
			}
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
	}
	
	/**
	 * 更改群组成员
	 * @param context
	 * @param cid
	 * @param name
	 */
	public static void updateChannelGroupMembers(Context context,String cid,String members){
		try {
			ChannelGroup channelGroup = DbCacheUtils.getDb(context).findById(ChannelGroup.class, cid);
			if (channelGroup != null) {
				channelGroup.setMembers(members);;
				DbCacheUtils.getDb(context).update(channelGroup, WhereBuilder.b("cid", "=", cid), "members");
			}
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
	}
	

}
