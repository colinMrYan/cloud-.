package com.inspur.emmcloud.web.plugin.sql;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.inspur.emmcloud.baselib.util.JSONUtils;
import com.inspur.emmcloud.baselib.util.LogUtils;
import com.inspur.emmcloud.baselib.util.ToastUtils;
import com.inspur.emmcloud.web.plugin.ImpPlugin;
import com.inspur.emmcloud.web.plugin.filetransfer.FilePathUtils;
import com.inspur.emmcloud.web.util.StrUtil;

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
        showCallIMPMethodErrorDlg();
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
            JSONObject options = JSONUtils.getJSONObject(paramsObject, "options", new JSONObject());
            this.database = getSQLiteDatabase(JSONUtils.getString(options, "dbName", ""));
            if (database != null) {
                successCb = JSONUtils.getString(paramsObject, "success", "");
                failCb = JSONUtils.getString(paramsObject, "fail", "");
                executeSql(paramsObject);
            } else {
                ToastUtils.show("database connect error");
            }
        } else {
            showCallIMPMethodErrorDlg();
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
