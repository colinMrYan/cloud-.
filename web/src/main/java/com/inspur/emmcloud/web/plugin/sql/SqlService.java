package com.inspur.emmcloud.web.plugin.sql;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.inspur.emmcloud.baselib.util.JSONUtils;
import com.inspur.emmcloud.baselib.util.LogUtils;
import com.inspur.emmcloud.baselib.util.StringUtils;
import com.inspur.emmcloud.baselib.util.ToastUtils;
import com.inspur.emmcloud.basemodule.application.BaseApplication;
import com.inspur.emmcloud.web.plugin.ImpPlugin;
import com.inspur.emmcloud.web.plugin.filetransfer.FilePathUtils;
import com.inspur.emmcloud.web.util.StrUtil;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;


/**
 * 数据库操作的接口
 *
 * @author 浪潮移动应用平台(IMP)产品组
 */
public class SqlService extends ImpPlugin {

    // 常量
    private static final String SELECT = "select";
    // 数据库对象
    private SQLiteDatabase database = null;
    // 成功回调
    private String successCb = "";
    // 失败回调
    private String failCb = "";

    @Override
    public void execute(String action, JSONObject paramsObject) {
        if (action.equals("executeSql")) {
            operateDataBase(paramsObject);
        } else if (action.equals("close")) {
            closeDataBase(paramsObject);
        } else if (action.equals("executeTransaction")) {
            transactionDataBase(paramsObject);
        } else if (action.equals("delete")) {
            deleteDataBase(paramsObject);
        } else {
            showCallIMPMethodErrorDlg();
        }

    }

    @Override
    public String executeAndReturn(String action, JSONObject paramsObject) {
        showCallIMPMethodErrorDlg();
        return "";
    }

    /**
     * 操作数据库的逻辑
     *
     * @param paramsObject
     */
    private void operateDataBase(JSONObject paramsObject) {
        JSONObject options = JSONUtils.getJSONObject(paramsObject, "options", new JSONObject());
        String dbName = JSONUtils.getString(options, "dbName", "");
        successCb = JSONUtils.getString(paramsObject, "success", "");
        failCb = JSONUtils.getString(paramsObject, "fail", "");
        if (!StringUtils.isEmpty(dbName) && (dbName.equals("default.db") || (dbName.equals("emm.db")))) {
            jsCallback(failCb, "database name conflict ！！");
            return;
        }
        this.database = getSQLiteDatabase(dbName);
        if (database != null) {
            executeSql(paramsObject);
        } else {
            ToastUtils.show("database connect error");
        }
    }

    /**
     * 关闭数据库的逻辑
     *
     * @param paramsObject
     */
    private void closeDataBase(JSONObject paramsObject) {
        JSONObject options = JSONUtils.getJSONObject(paramsObject, "options", new JSONObject());
        this.database = getSQLiteDatabase(JSONUtils.getString(options, "dbName", ""));
        successCb = JSONUtils.getString(paramsObject, "success", "");
        failCb = JSONUtils.getString(paramsObject, "fail", "");
        if (database != null) {
            database.close();
            jsCallback(successCb, "");
        } else {
            ToastUtils.show("database not find");
            jsCallback(failCb, "database not find");
        }
    }

    /**
     * 删除数据库的逻辑
     *
     * @param paramsObject
     */
    private void deleteDataBase(JSONObject paramsObject) {
        JSONObject options = JSONUtils.getJSONObject(paramsObject, "options", new JSONObject());
        String dbName = JSONUtils.getString(options, "dbName", "");
        successCb = JSONUtils.getString(paramsObject, "success", "");
        failCb = JSONUtils.getString(paramsObject, "fail", "");
        if (StringUtils.isEmpty(dbName) || dbName.equals("default.db") || (dbName.equals("emm.db"))) {
            jsCallback(failCb, "local database cannot be deleted");
            return;
        }
        this.database = getSQLiteDatabase(dbName);
        if (database != null) {
            if (getActivity().getApplicationContext().deleteDatabase(dbName)) {
                jsCallback(successCb, "");
            } else {
                jsCallback(successCb, "");
            }
        } else {
            jsCallback(failCb, "database not found");
        }
    }

    /**
     * 数据库执行事务的逻辑
     *
     * @param paramsObject
     */
    private void transactionDataBase(JSONObject paramsObject) {
        JSONObject optionsJsonObject = JSONUtils.getJSONObject(paramsObject, "options", new JSONObject());
        String sqlList = JSONUtils.getString(optionsJsonObject, "sqls", "");
        String dbName = JSONUtils.getString(optionsJsonObject, "dbName", "");
        successCb = JSONUtils.getString(paramsObject, "success", "");
        failCb = JSONUtils.getString(paramsObject, "fail", "");
        if (!StringUtils.isEmpty(dbName) && (dbName.equals("default.db") || (dbName.equals("emm.db")))) {
            jsCallback(failCb, "database name conflict ！！");
            return;
        }
        this.database = getSQLiteDatabase(dbName);
        if (database != null) {
            executeTransaction(sqlList);
        } else {
            jsCallback(failCb, "database not found");
        }

    }

    /**
     * 获取数据库操作对象
     *
     * @param dbName
     * @return
     */
    private SQLiteDatabase getSQLiteDatabase(String dbName) {
        if (StrUtil.strIsNotNull(dbName)) {
            String dbPath = FilePathUtils.getRealPath(dbName);
            LogUtils.YfcDebug("数据库的真是路径：" + dbPath);
            try {
                return SQLiteDatabase.openOrCreateDatabase(dbPath, null);
            } catch (Exception e) {
                if (this.database != null) {
                    this.database.close();
                    this.database = null;
                }
                e.printStackTrace();
                return null;
            }
        } else {
            // 初始化，只需要调用一次
            AssetsDatabaseManager.initManager(getFragmentContext());
            // 获取管理对象，因为数据库需要通过管理对象才能够获取
            AssetsDatabaseManager mg = AssetsDatabaseManager.getManager();
            // 通过管理对象获取数据库
            return mg.getDatabase("default.db");
        }
    }

    private void executeTransaction(String sqlList) {
        Cursor myCursor = null;
        try {
            if (isSelectSqlList(sqlList)) {
                myCursor = this.database.rawQuery(sqlList, null);
                this.processResults(myCursor);
                if (myCursor != null) {
                    myCursor.close();
                }
            } else {
                database.beginTransaction();
                this.database.execSQL(sqlList);
                database.setTransactionSuccessful();
                jsCallback(successCb, "");
            }
        } catch (Exception e) {
            e.printStackTrace();
            if (myCursor != null) {
                myCursor.close();
                myCursor = null;
            }
            // 将错误信息反馈回前台
            jsCallback(failCb, getErrorJson(e.getMessage()));
        } finally {
            database.endTransaction();
        }
    }

    /**
     * 数据库操作
     *
     * @param jsonObject
     */
    private void executeSql(JSONObject jsonObject) {
        JSONObject optionsJsonObject = JSONUtils.getJSONObject(jsonObject, "options", new JSONObject());
        String sql = JSONUtils.getString(optionsJsonObject, "sql", "");
//        sql = "INSERT INTO Robot ('id','avatar','mode','name','support','title') VALUES('BOT6005','A8GJ2B6A4JB.png','','云+客服1111','yisiqi@inspur.com','投诉建议专用');";
//        sql = "update Robot set name='yunjiakefu' where name like '%云%';";
//        sql = "select * from Robot";
//        sql = "delete from Robot where title like '%投诉%';";
//        sql = "create table testTable ('id' int unique, name varchar(20))";
//        sql = "drop table testTable";
//        sql = "alter table testTable  add address varchar(40)";
//        sql = "alter table testTable drop column address";//目前sqlite不支持drop column方法
//        sql = "create database myDatabase";    //sqlite不支持Sql语句创建数据库  只支持命令创建数据库
        Cursor myCursor = null;
        try {
            if (isSelectSql(sql)) {
                myCursor = this.database.rawQuery(sql, null);
                this.processResults(myCursor);
                if (myCursor != null) {
                    myCursor.close();
                }
            } else {
                this.database.execSQL(sql);
                // 将查询结果传回前台
                jsCallback(successCb, "");
            }
        } catch (Exception e) {
            e.printStackTrace();
            if (myCursor != null) {
                myCursor.close();
                myCursor = null;
            }
            // 将错误信息反馈回前台
            jsCallback(failCb, getErrorJson(e.getMessage()));
        }
    }


    /**
     * 组装错误信息
     *
     * @param message
     * @return
     */
    private JSONObject getErrorJson(String message) {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("errorMessage", message);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return jsonObject;
    }

    /**
     * 判断是否查询SQL语句
     *
     * @param sql
     * @return
     */
    private boolean isSelectSql(String sql) {
        String cmd = sql.toLowerCase();
        if (cmd.startsWith(SELECT)) {
            return true;
        }
        return false;
    }

    /**
     * 判断是否查询SQL语句
     *
     * @param sqlList
     * @return
     */
    private boolean isSelectSqlList(String sqlList) {
        String cmd = sqlList.toLowerCase();
        if (cmd.contains(SELECT)) {
            return true;
        }
        return false;
    }

    /**
     * 解析结果，把条查询到的记录包装成一个json对象
     *
     * @param cursor
     */
    private void processResults(Cursor cursor) {
        JSONArray result = new JSONArray();
        JSONObject resultJsonObject = new JSONObject();
        try {
            if (cursor.moveToFirst()) {
                String key = "";
                String value = "";
                //一共有多少条记录
                int colCount = cursor.getColumnCount();
                //第一层循环取出每条记录
                do {
                    JSONObject row = new JSONObject();

                    //第二层循环取出记录中的字段名和字段值包装成json
                    for (int i = 0; i < colCount; ++i) {
                        key = cursor.getColumnName(i);
                        value = cursor.getString(i);
                        row.put(key, value);
                    }
                    //每个json是一条记录，result是整个结果
                    result.put(row);

                } while (cursor.moveToNext());
            }
            resultJsonObject.put("result", result);
        } catch (Exception e) {
            if (cursor != null) {
                cursor.close();
                cursor = null;
            }
            e.printStackTrace();
        }
        // 将查询结果传回前台
        jsCallback(successCb, resultJsonObject);
    }

    @Override
    public void onDestroy() {
        if (this.database != null) {
            this.database.close();
            this.database = null;
        }
    }
}
