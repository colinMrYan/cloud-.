/**
 * DbCacheUtils.java
 * classes : com.inspur.emmcloud.util.privates.db.DbCacheUtils
 * V 1.0.0
 * Create at 2016年10月25日 上午10:02:19
 */
package com.inspur.emmcloud.util.privates.cache;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.inspur.emmcloud.MyApplication;
import com.inspur.emmcloud.bean.appcenter.AppCommonlyUse;
import com.inspur.emmcloud.bean.chat.Channel;
import com.inspur.emmcloud.bean.chat.ChannelGroup;
import com.inspur.emmcloud.bean.chat.Msg;
import com.inspur.emmcloud.bean.system.PVCollectModel;

import org.xutils.DbManager;
import org.xutils.x;

import java.io.File;

/**
 * com.inspur.emmcloud.util.privates.db.DbCacheUtils
 * create at 2016年10月25日 上午10:02:19
 */
public class DbCacheUtils {
    private static DbManager db;

    public static void initDb(Context context) {
        String userID = MyApplication.getInstance().getUid();
        String tanentID = MyApplication.getInstance().getTanent();
        String dbCachePath = "/data/data/" + context.getPackageName()
                + "/databases/" + userID + "/" + tanentID + "/" + "db/";
        DbManager.DaoConfig daoConfig = new DbManager.DaoConfig()
                .setDbName("emm.db")
                // 不设置dbDir时, 默认存储在app的私有目录.
                .setDbDir(new File(dbCachePath))
                .setDbVersion(15)
                .setAllowTransaction(true)
                .setDbOpenListener(new DbManager.DbOpenListener() {
                    @Override
                    public void onDbOpened(DbManager db) {
                        // 开启WAL, 对写入加速提升巨大
                        db.getDatabase().enableWriteAheadLogging();
                    }
                })
                .setDbUpgradeListener(new DbManager.DbUpgradeListener() {
                    @Override
                    public void onUpgrade(DbManager db, int oldVersion, int newVersion) {
                        // TODO: ...
                        try {
                            if (oldVersion<5){
                                db.dropTable(ChannelGroup.class);
                                db.dropTable(AppCommonlyUse.class);
                                db.dropTable(Channel.class);
                            }
                            if (oldVersion < 6) {
                                if (tableIsExist(db,"com_inspur_emmcloud_bean_Contact")) {
                                    db.execNonQuery("alter table com_inspur_emmcloud_bean_Contact rename to Contact");
                                    db.execNonQuery("alter table Contact add lastUpdateTime String");
                                }

                                if (tableIsExist(db,"com_inspur_emmcloud_bean_Channel")) {
                                    db.execNonQuery("alter table com_inspur_emmcloud_bean_Channel rename to Channel");
                                }

                                if (tableIsExist(db,"com_inspur_emmcloud_bean_ChannelOperationInfo")) {
                                    db.execNonQuery("alter table com_inspur_emmcloud_bean_ChannelOperationInfo rename to ChannelOperationInfo");
                                }

                                if (tableIsExist(db,"com_inspur_emmcloud_bean_SearchModel")) {
                                    db.execNonQuery("alter table com_inspur_emmcloud_bean_SearchModel rename to SearchModel");
                                }
                                if (tableIsExist(db,"com_inspur_emmcloud_bean_MyCalendarOperation")) {
                                    db.execNonQuery("alter table com_inspur_emmcloud_bean_MyCalendarOperation rename to MyCalendarOperation");
                                }

                                if (tableIsExist(db,"com_inspur_emmcloud_bean_Robot")) {
                                    db.execNonQuery("alter table com_inspur_emmcloud_bean_Robot rename to Robot");
                                }

                                if (tableIsExist(db,"com_inspur_emmcloud_bean_AppOrder")) {
                                    db.execNonQuery("alter table com_inspur_emmcloud_bean_AppOrder rename to AppOrder");
                                }

                            }
                            if (oldVersion < 7) {
                                db.dropTable(PVCollectModel.class);
                            }
                            if (oldVersion < 8) {
                                if (tableIsExist(db,"Contact")) {
                                    db.execNonQuery("CREATE INDEX contactindex ON Contact(inspurID)");
                                }

                            }
                            if (oldVersion < 9) {
                                db.dropTable(Msg.class);
                                db.execNonQuery("DROP TABLE IF EXISTS MsgReadId");
                                db.execNonQuery("DROP TABLE IF EXISTS MsgMatheSet");
                            }
                            if (oldVersion<14){
                                db.execNonQuery("DROP TABLE IF EXISTS Contact");
                                db.execNonQuery("DROP TABLE IF EXISTS ContactUser");
                                ContactUserCacheUtils.setLastQueryTime(0);
                                db.execNonQuery("DROP TABLE IF EXISTS Message");
                                db.execNonQuery("DROP TABLE IF EXISTS MessageMatheSet");
                            }
                            if(oldVersion<15){
                                db.execNonQuery("ALTER TABLE Message ADD COLUMN sendStatus INTEGER DEFAULT 1");
                                db.execNonQuery("ALTER TABLE Message ADD COLUMN localPath TEXT DEFAULT ''");
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                });
        db = x.getDb(daoConfig);
    }

    /**
     * 判断数据库是否为空
     * @return
     */
    public static boolean isDbNull() {
        return db == null;
    }

    public static boolean tableIsExist(DbManager dbManager,String tabName) {
        boolean result = false;
        if (tabName == null) {
            return false;
        }
        SQLiteDatabase sqliteDatabase =null;
        if (dbManager != null){
            sqliteDatabase = dbManager.getDatabase();
        }else if(db != null){
            sqliteDatabase = db.getDatabase();
        }else {
            return false;
        }
        Cursor cursor = null;
        try {
            String sql = "select count(*) as c from sqlite_master where type ='table' and name ='" + tabName.trim() + "' ";
            cursor = sqliteDatabase.rawQuery(sql, null);
            if (cursor.moveToNext()) {
                int count = cursor.getInt(0);
                if (count > 0) {
                    result = true;
                }
            }

        } catch (Exception e) {
            // TODO: handle exception
            e.printStackTrace();
        }finally {
            if (cursor != null){
                cursor.close();
            }
        }
        return result;
    }

    public static DbManager getDb(Context context) {
        if (db == null) {
            initDb(context.getApplicationContext());
        }
        return db;
    }

    public static DbManager getDb() {
        if (db == null) {
            initDb(MyApplication.getInstance());
        }
        return db;
    }

    /**
     * 删除数据库
     * @param context
     */
    public static void deleteDb(Context context) {
        try {
            db.dropDb();
            ContactUserCacheUtils.setLastQueryTime(0);
            ContactOrgCacheUtils.setLastQueryTime(0);
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
    public static void closeDb(Context context) {
        try {
            if (db != null) {
                db.close();
                db = null;
            }
        } catch (Exception e) {
            // TODO: handle exception
            e.printStackTrace();
        }
    }
}
