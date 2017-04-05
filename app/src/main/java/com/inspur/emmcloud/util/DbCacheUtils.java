/**
 * 
 * DbCacheUtils.java
 * classes : com.inspur.emmcloud.util.DbCacheUtils
 * V 1.0.0
 * Create at 2016年10月25日 上午10:02:19
 */
package com.inspur.emmcloud.util;

import android.content.Context;

import com.inspur.emmcloud.MyApplication;
import com.inspur.emmcloud.bean.AppCommonlyUse;
import com.inspur.emmcloud.bean.Channel;
import com.inspur.emmcloud.bean.ChannelGroup;
import com.inspur.emmcloud.bean.Contact;
import com.lidroid.xutils.DbUtils;
import com.lidroid.xutils.DbUtils.DbUpgradeListener;
import com.lidroid.xutils.exception.DbException;

/**
 * com.inspur.emmcloud.util.DbCacheUtils
 * create at 2016年10月25日 上午10:02:19
 */
public class DbCacheUtils {
	private static DbUtils db;
	public static void initDb(Context context) {
		String userID = ((MyApplication)context.getApplicationContext()).getUid();
		String tanentID = UriUtils.tanent;
		String dbCachePath = "/data/data/" + context.getPackageName()
				+ "/databases/" + userID + "/" + tanentID + "/" + "db/";
		//db = DbUtils.create(context, dbCachePath, "emm.db");
		db = DbUtils.create(context, dbCachePath, "emm.db", 5, new DbUpgradeListener() {
			@Override
			public void onUpgrade(DbUtils arg0, int oldVersion, int newVersion) {
				// TODO Auto-generated method stub
				try {
					if(oldVersion == 1){
						arg0.dropTable(Contact.class);
						arg0.dropTable(Channel.class);
						arg0.dropTable(ChannelGroup.class);
						arg0.dropTable(AppCommonlyUse.class);
					}else if(oldVersion == 2){
						arg0.dropTable(ChannelGroup.class);
						arg0.dropTable(Channel.class);
						arg0.dropTable(AppCommonlyUse.class);
					}else if(oldVersion == 3||oldVersion == 4){
						arg0.dropTable(AppCommonlyUse.class);
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
		db.configAllowTransaction(true);
		db.configDebug(false);
	}
	
	public static DbUtils getDb(Context context){
		if (db == null) {
			initDb(context.getApplicationContext());
		}
		return db;
	}
	
	/**
	 * 删除数据库
	 * @param context
	 */
	public static void deleteDb(Context context){
		try {
			db.dropDb();
			ContactCacheUtils.saveLastUpdateTime(context, "");
			closeDb(context);
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
	}
	
	/**
	 * 关闭数据库
	 * @param context
	 */
	public static void closeDb(Context context){
		try {
			if (db != null){
				db.close();
				db = null;
			}
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
	}
}
