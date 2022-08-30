/**
 * DbCacheUtils.java
 * classes : com.inspur.emmcloud.util.privates.db.DbCacheUtils
 * V 1.0.0
 * Create at 2016年10月25日 上午10:02:19
 */
package com.inspur.emmcloud.basemodule.util;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.inspur.emmcloud.basemodule.application.BaseApplication;
import com.inspur.emmcloud.basemodule.bean.SimpleEventMessage;
import com.inspur.emmcloud.basemodule.config.Constant;

import org.greenrobot.eventbus.EventBus;
import org.xutils.DbManager;
import org.xutils.ex.DbException;
import org.xutils.x;

import java.io.File;

/**
 * com.inspur.emmcloud.util.privates.db.DbCacheUtils
 * create at 2016年10月25日 上午10:02:19
 */
public class DbCacheUtils {
    private static DbManager db;

    public static void initDb(Context context) {
        String userID = BaseApplication.getInstance().getUid();
        String tanentID = BaseApplication.getInstance().getTanent();
        String dbCachePath = "/data/data/" + context.getPackageName()
                + "/databases/" + userID + "/" + tanentID + "/" + "db/";
        DbManager.DaoConfig daoConfig = new DbManager.DaoConfig()
                .setDbName("emm.db")
                // 不设置dbDir时, 默认存储在app的私有目录.
                .setDbDir(new File(dbCachePath))
                .setDbVersion(31)
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
                            if (oldVersion < 8) {
                                if (tableIsExist(db, "Contact")) {
                                    db.execNonQuery("CREATE INDEX contactindex ON Contact(inspurID)");
                                }

                            }
                            if (oldVersion < 9) {
                                db.execNonQuery("DROP TABLE IF EXISTS Msg");
                                db.execNonQuery("DROP TABLE IF EXISTS MsgReadId");
                                db.execNonQuery("DROP TABLE IF EXISTS MsgMatheSet");
                            }
                            if (oldVersion < 14) {
                                db.execNonQuery("DROP TABLE IF EXISTS Contact");
                                db.execNonQuery("DROP TABLE IF EXISTS ContactUser");
//                                ContactUserCacheUtils.setLastQueryTime(0);
                                //lastQueryTime清零
                                PreferencesByUserAndTanentUtils.putLong(BaseApplication.getInstance(), Constant.PREF_CONTACT_USER_LASTQUERYTIME, 0);
                                db.execNonQuery("DROP TABLE IF EXISTS Message");
                                db.execNonQuery("DROP TABLE IF EXISTS MessageMatheSet");
                            }
                            if (oldVersion < 15) {
                                db.execNonQuery("ALTER TABLE Message ADD COLUMN sendStatus INTEGER DEFAULT 1");
                                db.execNonQuery("ALTER TABLE Message ADD COLUMN localPath TEXT DEFAULT ''");
                            }
                            if (oldVersion < 16) {
                                db.execNonQuery("DROP TABLE IF EXISTS Mail");
                            }
                            if (oldVersion < 17) {
                                if (tableIsExist(db, "ChannelGroup")) {
                                    db.execNonQuery("ALTER TABLE ChannelGroup ADD COLUMN action TEXT DEFAULT ''");
                                    db.execNonQuery("ALTER TABLE ChannelGroup ADD COLUMN avatar TEXT DEFAULT ''");
                                }
                                if (tableIsExist(db, "Channel")) {
                                    db.execNonQuery("ALTER TABLE Channel ADD COLUMN action TEXT DEFAULT ''");
                                    db.execNonQuery("ALTER TABLE Channel ADD COLUMN avatar TEXT DEFAULT ''");
                                }
                                if (tableIsExist(db, "Conversation")) {
                                    db.execNonQuery("ALTER TABLE Conversation ADD COLUMN action TEXT DEFAULT ''");
                                }
                            }
                            if (oldVersion < 18) {
                                if (tableIsExist(db, "Conversation")) {
                                    db.execNonQuery("ALTER TABLE Conversation ADD COLUMN pyFull TEXT DEFAULT ''");
                                }
                            }
                            if (oldVersion < 19) {
                                db.execNonQuery("DROP TABLE IF EXISTS Schedule");
                                db.execNonQuery("DROP TABLE IF EXISTS Meeting");
                            }
                            if (oldVersion < 20) {
                                if (tableIsExist(db, "Message")) {
                                    db.execNonQuery("ALTER TABLE Message ADD COLUMN showContent TEXT DEFAULT ''");
                                    EventBus.getDefault().post(new SimpleEventMessage(Constant.EVENTBUS_TAG_MESSAGE_ADD_SHOW_CONTENT));
                                }
                            }
                            if (oldVersion < 21) {
                                if (tableIsExist(db, "Conversation")) {
                                    db.execNonQuery("ALTER TABLE Conversation ADD COLUMN showName TEXT DEFAULT ''");
                                    EventBus.getDefault().post(new SimpleEventMessage(Constant.EVENTBUS_TAG_CONVERSATION_ADD_SHOW_CONTENT));
                                }
                            }
                            if (oldVersion < 22) {
                                if (tableIsExist(db, "Message")) {
                                    db.execNonQuery("ALTER TABLE Message ADD COLUMN isWaitingSendRetry INTEGER DEFAULT 0");
                                }
                            }
                            if (oldVersion < 23) {
                                if (tableIsExist(db, "Message")) {
                                    db.execNonQuery("ALTER TABLE Message ADD COLUMN recallFrom INTEGER DEFAULT ''");
                                }
                            }

                            //由于DbVersion 23时旧Conversation和Message展示内容字段设置不成功，此处改为24
                            if (oldVersion < 24) {
                                if (tableIsExist(db, "Message")) {
                                    EventBus.getDefault().post(new SimpleEventMessage(Constant.EVENTBUS_TAG_MESSAGE_ADD_SHOW_CONTENT));
                                }
                                if (tableIsExist(db, "Conversation")) {
                                    EventBus.getDefault().post(new SimpleEventMessage(Constant.EVENTBUS_TAG_CONVERSATION_ADD_SHOW_CONTENT));
                                }
                            }
                            if (oldVersion < 25) {
                                if (tableIsExist(db, "Message")) {
                                    db.execNonQuery("ALTER TABLE Message ADD COLUMN lifeCycleState INTEGER DEFAULT 1");
                                }
                            }
                            if (oldVersion < 27) {
                                if (tableIsExist(db, "VolumeFile")) {
                                    db.execNonQuery("DROP TABLE IF EXISTS VolumeFile");
                                }
                                if (tableIsExist(db, "VolumeFileUpload")) {
                                    db.execNonQuery("DROP TABLE IF EXISTS VolumeFileUpload");
                                }
                                if (tableIsExist(db, "DownloadFile")) {
                                    db.execNonQuery("DROP TABLE IF EXISTS DownloadFile");
                                }
                            }

                            if(oldVersion < 28){
                                if (tableIsExist(db, "CardPackageBean")) {
                                    db.execNonQuery("ALTER TABLE CardPackageBean ADD COLUMN barcodeUrl TEXT DEFAULT ''");
                                }
                            }

                            if (oldVersion < 29) {
                                if (tableIsExist(db, "Message")) {
                                    db.execNonQuery("ALTER TABLE Message ADD COLUMN states TEXT DEFAULT ''");
                                }
                            }

                            if (oldVersion < 30) {
                                if (tableIsExist(db, "Conversation")) {
                                    db.execNonQuery("ALTER TABLE Conversation ADD COLUMN serviceId TEXT DEFAULT ''");
                                }
                            }

                            if (oldVersion < 30) {
                                if (tableIsExist(db, "Conversation")) {
                                    db.execNonQuery("ALTER TABLE Conversation ADD COLUMN administrators TEXT DEFAULT ''");
                                    db.execNonQuery("ALTER TABLE Conversation ADD COLUMN silent INTEGER DEFAULT 0 ");
                                }
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                });
        try {
            db = x.getDb(daoConfig);
        } catch (DbException e) {
            e.printStackTrace();
        }
    }

    /**
     * 判断数据库是否为空
     *
     * @return
     */
    public static boolean isDbNull() {
        return db == null;
    }

    public static boolean tableIsExist(DbManager dbManager, String tabName) {
        boolean result = false;
        if (tabName == null) {
            return false;
        }
        SQLiteDatabase sqliteDatabase = null;
        if (dbManager != null) {
            sqliteDatabase = dbManager.getDatabase();
        } else if (db != null) {
            sqliteDatabase = db.getDatabase();
        } else {
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
        } finally {
            if (cursor != null) {
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
            initDb(BaseApplication.getInstance());
        }
        return db;
    }

    /**
     * 删除数据库
     *
     * @param context
     */
    public static void deleteDb(Context context) {
        try {
            db.dropDb();
            PreferencesByUserAndTanentUtils.putLong(BaseApplication.getInstance(), Constant.PREF_CONTACT_USER_LASTQUERYTIME, 0);
            PreferencesByUserAndTanentUtils.putLong(BaseApplication.getInstance(), Constant.PREF_CONTACT_ORG_LASTQUERYTIME, 0);
            PreferencesByUserAndTanentUtils.putString(BaseApplication.getInstance(), Constant.PREF_SCHEDULE_BASIC_DATA_VERSION, "0");
            closeDb(context);
        } catch (Exception e) {
            // TODO: handle exception
            e.printStackTrace();
        }
    }

    /**
     * 根据表名删除数据库
     */
    public static void deleteDbByTableName(Context context, String tableName) {
        try {
            if (db != null) {
                if (tableIsExist(db, tableName)) {
                    db.execNonQuery("DROP TABLE IF EXISTS " + tableName);
                }
            }
        } catch (DbException e) {
            e.printStackTrace();
        } finally {
            closeDb(context);
        }

    }

    /**
     * 关闭数据库
     *
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
