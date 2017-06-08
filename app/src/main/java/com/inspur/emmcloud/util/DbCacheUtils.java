/**
 * 
 * DbCacheUtils.java
 * classes : com.inspur.emmcloud.util.DbCacheUtils
 * V 1.0.0
 * Create at 2016年10月25日 上午10:02:19
 */
package com.inspur.emmcloud.util;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.inspur.emmcloud.MyApplication;
import com.inspur.emmcloud.bean.AppCommonlyUse;
import com.inspur.emmcloud.bean.Channel;
import com.inspur.emmcloud.bean.ChannelGroup;
import com.inspur.emmcloud.bean.Contact;
import com.lidroid.xutils.DbUtils;
import com.lidroid.xutils.DbUtils.DbUpgradeListener;

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
		db = DbUtils.create(context, dbCachePath, "emm.db", 6, new DbUpgradeListener() {
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
					if (oldVersion <6 ){
						if (tableIsExist("com_inspur_emmcloud_bean_Contact",arg0.getDatabase())){
							arg0.execNonQuery("alter table com_inspur_emmcloud_bean_Contact rename to Contact");
						}
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
		db.configAllowTransaction(true);
		db.configDebug(false);
	}

	private static  boolean tableIsExist(String tabName, SQLiteDatabase db) {
		boolean result = false;
		if (tabName == null) {
			return false;
		}
		Cursor cursor = null;
		try {

			String sql = "select count(*) as c from sqlite_master where type ='table' and name ='" + tabName.trim() + "' ";
			cursor = db.rawQuery(sql, null);
			if (cursor.moveToNext()) {
				int count = cursor.getInt(0);
				if (count > 0) {
					result = true;
				}
			}

		} catch (Exception e) {
			// TODO: handle exception
		}
		return result;
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
