package com.inspur.emmcloud.web.plugin.sql;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.inspur.emmcloud.baselib.util.JSONUtils;
import com.inspur.emmcloud.basemodule.config.MyAppConfig;
import com.inspur.emmcloud.web.plugin.ImpPlugin;

import org.json.JSONArray;
import org.json.JSONObject;


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
        operateDataBase(action, paramsObject);
    }

    @Override
    public String executeAndReturn(String action, JSONObject paramsObject) {
        return "";
    }

    /**
     * 操作数据库的逻辑
     *
     * @param action
     * @param paramsObject
     */
    private void operateDataBase(String action, JSONObject paramsObject) {
        if ("executeSql".equals(action)) {
            this.database = getSQLiteDatabase(JSONUtils.getString(paramsObject, "dbName", ""));
            successCb = JSONUtils.getString(paramsObject, "success", "");
            failCb = JSONUtils.getString(paramsObject, "fail", "");
            executeSql(paramsObject);
        }
    }

    /**
     * 获取数据库操作对象
     *
     * @param dbName
     * @return
     */
    private SQLiteDatabase getSQLiteDatabase(String dbName) {
        String dbPath = MyAppConfig.LOCAL_DOWNLOAD_PATH + dbName;
        return SQLiteDatabase.openOrCreateDatabase(dbPath, null);
    }

    /**
     * 数据库操作
     *
     * @param jsonObject
     */
    private void executeSql(JSONObject jsonObject) {
        String sql = JSONUtils.getString(jsonObject, "sql", "");
//        sql = "INSERT INTO Robot ('id','avatar','mode','name','support','title') VALUES('BOT6005','A8GJ2B6A4JB.png','','云+客服1111','yisiqi@inspur.com','投诉建议专用');";
//        sql = "update Robot set name='yunjiakefu' where name like '%云%';";
//        sql = "select * from Robot";
//        sql = "delete from Robot where title like '%投诉%';";
//        sql = "create table testTable ('id' int unique, name varchar(20))";
//        sql = "drop table testTable";
//        sql = "alter table testTable  add address varchar(40)";
//        sql = "alter table testTable drop column address";//目前sqlite不支持drop column方法
//        sql = "create database myDatabase";    //sqlite不支持Sql语句创建数据库  只支持命令创建数据库
        try {
            if (isSelectSql(sql)) {
                Cursor myCursor = this.database.rawQuery(sql, null);
                this.processResults(myCursor);
                myCursor.close();
            } else {
                this.database.execSQL(sql);
                // 将查询结果传回前台
                jsCallback(successCb, "");
            }
        } catch (Exception e) {
            e.printStackTrace();
            // 将错误信息反馈回前台
            JSONObject errorMessageJsonObject = new JSONObject();
            try {
                errorMessageJsonObject.put("errorMessage", e.getMessage());
                jsCallback(failCb, errorMessageJsonObject.toString());
            } catch (Exception e1) {
                e1.printStackTrace();
            }
        }
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
            e.printStackTrace();
        }
        // 将查询结果传回前台
        jsCallback(successCb, resultJsonObject.toString());
    }

    @Override
    public void onDestroy() {
        if (this.database != null) {
            this.database.close();
            this.database = null;
        }
    }
}
