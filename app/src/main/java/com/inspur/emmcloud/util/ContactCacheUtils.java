package com.inspur.emmcloud.util;

import android.content.Context;

import com.inspur.emmcloud.bean.Contact;
import com.lidroid.xutils.db.sqlite.Selector;
import com.lidroid.xutils.db.sqlite.WhereBuilder;
import com.lidroid.xutils.exception.DbException;

import org.json.JSONArray;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ContactCacheUtils {

	/**
	 * 存储通讯录列表
	 * 
	 * @param context
	 * @param contactList
	 */
	public static void saveContactList(Context context,
			List<Contact> contactList) {
		if (contactList == null || contactList.size() == 0) {
			return;
		}
		try {
			
			DbCacheUtils.getDb(context).saveOrUpdateAll(contactList);
		} catch (DbException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * 删除通讯录
	 * 
	 * @param deleteIdArray
	 *            删除通讯录id的json数组
	 */
	public static void deleteContact(final Context context,
			final JSONArray deleteIdArray) {
		if (deleteIdArray == null || deleteIdArray.length() == 0) {
			return;
		}
		try {
			
			for (int i = 0; i < deleteIdArray.length(); i++) {
				String id = deleteIdArray.getString(i);
				DbCacheUtils.getDb(context).deleteById(Contact.class, id);
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
	
	
	/**
	 * 删除通讯录
	 * 
	 * @param deleteIdList
	 *            删除通讯录id的List
	 */
	public static void deleteContact(final Context context,
			final List<String> deleteIdList) {
		if (deleteIdList == null || deleteIdList.size() == 0) {
			return;
		}
		try {
			
			for (int i = 0; i < deleteIdList.size(); i++) {
				String id = deleteIdList.get(i);
				DbCacheUtils.getDb(context).deleteById(Contact.class, id);
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
	

	/**
	 * 保存此租户通讯录客户端最后的更新时间
	 * 
	 * @param time
	 */
	public static void saveLastUpdateTime(Context context, String time) {
		String userID = PreferencesUtils.getString(context, "userID", "");
		String tanent = UriUtils.tanent;
		String key =userID+tanent+"contactUpdateTime";
		PreferencesUtils.putString(context,key,time);
	}

	/**
	 * 获取此租户通讯录客户端最后的更新时间
	 * 
	 * @return
	 */
	public static String getLastUpdateTime(Context context) {
		String userID = PreferencesUtils.getString(context, "userID", "");
		String tanent = UriUtils.tanent;
		String key =userID+tanent+"contactUpdateTime";
		return PreferencesUtils.getString(context,key,"");
	}

	/**
	 * 获取根组织架构
	 * 
	 * @param context
	 * @return
	 * +
	 */
	public static Contact getRootContact(Context context) {
		Contact contact = null;
		try {
			
			contact = DbCacheUtils.getDb(context).findFirst(Selector.from(Contact.class).where(
					"parentId", "=", "root"));
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
		return contact;
	}

	/**
	 * 获取组织架构下的组织和人员列表
	 * 
	 * @param contactId
	 * @return
	 */
	public static List<Contact> getChildContactList(Context context,
			String contactId) {
		List<Contact> childContactList = null;
		try {
			
			// 组织下的组织架构列表
			List<Contact> childStructList = DbCacheUtils.getDb(context).findAll(Selector
					.from(Contact.class).where("parentId", "=", contactId)
					.and("type", "=", "").orderBy("sortOrder"));
			// 组织下的人员列表
			List<Contact> childUserList = DbCacheUtils.getDb(context).findAll(Selector
					.from(Contact.class).where("parentId", "=", contactId)
					.and("type", "=", "user")
					.orderBy("sortOrder"));
			if (childStructList != null && childUserList != null) {
				childStructList.addAll(childUserList);
				childContactList = childStructList;
			} else if (childStructList != null) {
				childContactList = childStructList;
			} else if (childUserList != null) {
				childContactList = childUserList;
			}
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
		if (childContactList == null) {
			childContactList = new ArrayList<Contact>();
		}
		return childContactList;
	}

	// /**
	// * 搜索通讯录
	// *
	// * @param context
	// * @param searchText
	// * @return
	// */
	// public static List<Contact> getSearchContact(Context context,
	// String searchText) {
	// // String searchStr ="%"+searchText+"%";
	// String searchStr = "";
	// for (int i = 0; i < searchText.length(); i++) {
	// if (i < searchText.length() - 1) {
	// searchStr += "%" + searchText.charAt(i);
	// } else {
	// searchStr += "%" + searchText.charAt(i) + "%";
	// }
	// }
	// List<Contact> resultContactList = null;
	// try {
	// 
	// resultContactList = DbCacheUtils.getDb(context).findAll(Selector.from(Contact.class)
	// .where("pinyin", "like", searchStr)
	// .or("code", "like", searchStr)
	// .or("realName", "like", searchStr));
	// } catch (Exception e) {
	// // TODO: handle exception
	// e.printStackTrace();
	// }
	// if (resultContactList == null) {
	// resultContactList = new ArrayList<Contact>();
	// }
	// return resultContactList;
	// }
	//

	/**
	 * 搜索子目录中符合条件的通讯录
	 * 
	 * @param context
	 * @param searchText
	 * @return
	 */
	public static List<Contact> getSearchContact(Context context,
			String searchText, String contactId, int offset, int limit) {
		// String searchStr ="%"+searchText+"%";
		String searchStr = "";
		for (int i = 0; i < searchText.length(); i++) {
			if (i < searchText.length() - 1) {
				searchStr += "%" + searchText.charAt(i);
			} else {
				searchStr += "%" + searchText.charAt(i) + "%";
			}
		}
		List<Contact> resultContactList = null;
		try {
			
			if (StringUtils.isBlank(contactId)) {
				resultContactList = DbCacheUtils.getDb(context).findAll(Selector
						.from(Contact.class)
						.where("type", "=", "user")
						
						.and(WhereBuilder.b("pinyin", "like", searchStr)
								.or("realName", "like", searchStr)
								.or("globalName", "like", searchStr)
								.or("code", "like", searchStr)).offset(offset)
						.limit(limit));
			} else {
				resultContactList = DbCacheUtils.getDb(context).findAll(Selector
						.from(Contact.class)
						.where("fullPath", "like", "%" + contactId + "%")
						.and("type", "=", "user")
						
						.and(WhereBuilder.b("pinyin", "like", searchStr)
								.or("realName", "like", searchStr)
								.or("globalName", "like", searchStr)
								.or("code", "like", searchStr)).offset(offset)
						.limit(limit));
			}

		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
		if (resultContactList == null) {
			resultContactList = new ArrayList<Contact>();
		}
		return resultContactList;
	}

	/**
	 * 获取用户名列表
	 * 
	 * @param context
	 * @param uidArray
	 * @return
	 */
	public static JSONArray getUsersName(Context context, JSONArray uidArray) {
		JSONArray jsonArray = new JSONArray();
		try {
			
			for (int i = 0; i < uidArray.length(); i++) {
				Contact contact = DbCacheUtils.getDb(context).findFirst(Selector.from(Contact.class)
						.where("inspurID", "=", uidArray.getString(i)));
				String name = contact.getRealName();
				jsonArray.put(i, name);
			}
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}

		return jsonArray;
	}
	
	
	/**
	 * 获取用户名列表
	 * 
	 * @param context
	 * @param uidList
	 * @return
	 */
	public static List<String> getUsersName(Context context, List<String> uidList) {
		List<String> userNameList = new ArrayList<String>();
		try {
			
			for (int i = 0; i < uidList.size(); i++) {
				Contact contact = DbCacheUtils.getDb(context).findFirst(Selector.from(Contact.class)
						.where("inspurID", "=", uidList.get(i)));
				String name = contact.getRealName();
				userNameList.add(name);
			}
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}

		return userNameList;
	}
	

	/**
	 * 获取用户名
	 * 
	 * @param context
	 * @param uid
	 * @return
	 */
	public static String getUserName(Context context, String uid) {
		String name = "";
		try {
			
			Contact contact = DbCacheUtils.getDb(context).findFirst(Selector.from(Contact.class).where(
					"inspurID", "=", uid));
			if (contact != null) {
				name = contact.getRealName();
			}

		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}

		return name;
	}

	/**
	 * 获取用户名
	 * 
	 * @param context
	 * @param uid
	 * @return
	 */
	public static String getUserInspurID(Context context, String uid) {
		String inspurID = "";
		try {
			
			Contact contact = DbCacheUtils.getDb(context).findFirst(Selector.from(Contact.class).where(
					"inspurID", "=", uid));
			inspurID = contact.getInspurID();

		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}

		return inspurID;
	}

	/**
	 * 通过id List获取contact对象的List
	 *
	 * @param context
	 * @param uidArray
	 * @return
	 */
	public static List<Contact> getUserList(Context context, JSONArray uidArray) {
		List<Contact> userList = new ArrayList<Contact>();
		try {
			
			List<String> uidList = new ArrayList<String>();
			for (int i = 0; i < uidArray.length(); i++) {
				uidList.add(uidArray.getString(i));
			}

			userList = DbCacheUtils.getDb(context).findAll(Selector.from(Contact.class).where("inspurID",
					"in", uidList));
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();

		}
		if (userList == null) {
			userList = new ArrayList<Contact>();
		}
		return userList;

	}
	
	
	/**
	 * 通过id List获取contact对象的List
	 *
	 * @param context
	 * @param uidList
	 * @return
	 */
	public static List<Contact> getUserList(Context context, List<String> uidList) {
		List<Contact> userList = new ArrayList<Contact>();
		try {
			userList = DbCacheUtils.getDb(context).findAll(Selector.from(Contact.class).where("inspurID",
					"in", uidList));
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();

		}
		if (userList == null) {
			userList = new ArrayList<Contact>();
		}
		return userList;

	}
	
	
	
	/**
	 * 按顺序通过id List获取contact对象的List
	 *
	 * @param context
	 * @param uidList
	 * @return
	 */
	public static List<Contact> getSoreUserList(Context context, List<String> uidList) {
		List<Contact> userList = new ArrayList<Contact>();
		try {
			
			for (int i = 0; i < uidList.size(); i++) {
				Contact contact = getUserContact(context, uidList.get(i));
				if (contact != null) {
					userList.add(contact);
				}
			}

		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();

		}
		return userList;

	}
	
	
	/**
	 * 按顺序通过id List获取contact对象的List
	 *
	 * @param context
	 * @param uidArray
	 * @return
	 */
	public static List<Contact> getSoreUserList(Context context, JSONArray uidArray) {
		List<Contact> userList = new ArrayList<Contact>();
		try {
			
			for (int i = 0; i < uidArray.length(); i++) {
				Contact contact = getUserContact(context, uidArray.getString(i));
				if (contact != null) {
					userList.add(contact);
				}
			}

		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();

		}
		return userList;

	}


	public static List<Map<String, String>> getUserMap(Context context,
			JSONArray uidArray) {
		List<Map<String, String>> userMapList = new ArrayList<Map<String, String>>();
		try {
			
			for (int i = 0; i < uidArray.length(); i++) {
				String uid = uidArray.getString(i);
				Contact contact = DbCacheUtils.getDb(context).findFirst(Selector.from(Contact.class)
						.where("inspurID", "=", uid));
				if (contact != null) {
					String name = contact.getRealName();
					Map<String, String> userMap = new HashMap<String, String>();
					userMap.put(uid, name);
					userMapList.add(userMap);
				}

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
	 * 获取通讯录
	 * 
	 * @param context
	 * @param uid
	 * @return
	 */
	public static Contact getUserContact(Context context, String uid) {
		Contact contact = null;
		try {
			
			contact = DbCacheUtils.getDb(context).findFirst(Selector.from(Contact.class).where("inspurID",
					"=", uid));
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
		return contact;
	}
	
	public static Contact getStructContact(Context context,String structId){
		Contact contact = null;
		try {
			contact = DbCacheUtils.getDb(context).findFirst(Selector.from(Contact.class).where("id",
					"=", structId).and("type", "!=", "user"));
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
		return contact;
	}

}
