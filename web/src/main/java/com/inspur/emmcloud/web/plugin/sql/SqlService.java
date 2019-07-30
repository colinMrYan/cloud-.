package com.inspur.emmcloud.web.plugin.sql;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Environment;

import com.inspur.emmcloud.baselib.util.JSONUtils;
import com.inspur.emmcloud.baselib.util.LogUtils;
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
    // 数据库路径
    private String path = null;
    // 数据库名
    private String dbName = null;

    @Override
    public void execute(String action, JSONObject paramsObject) {
        if ("open".equals(action)) {
            this.database = getSQLiteDatabase("emmcloud.db");
            executeSqlSelect(paramsObject);
        }
    }

    @Override
    public String executeAndReturn(String action, JSONObject paramsObject) {
        return null;
    }

    /**
     * 获取数据库操作对象
     *
     * @param dbName
     * @return
     */
    private SQLiteDatabase getSQLiteDatabase(String dbName) {
        String dbPath = Environment.getExternalStorageDirectory() + "/" + dbName;
        return SQLiteDatabase.openOrCreateDatabase(dbPath, null);
    }

    /**
     * 对数据库进行操作
     */
    private void executeSqlSelect(JSONObject jsonObject) {
        String sql = "";
        String param = "";
        // 查询或者插入的字段值以逗号隔开
        String[] params = null;
        String txId = "";
//		sql = "select * from Robot";
        sql = JSONUtils.getString(jsonObject, "sql", "");
        param = JSONUtils.getString(jsonObject, "param", "");
        txId = JSONUtils.getString(jsonObject, "txId", "");
        String[] sqls = sql.split(";");
        try {
            if (isDDL(sql)) {
                for (String sqlitem : sqls) {
                    this.database.execSQL(sqlitem + ";");
                }
                // 将查询结果传回前台
                this.webview.loadUrl("javascript:completeQuery('" + txId
                        + "','[]');");
            } else {
                Cursor myCursor = this.database.rawQuery(sql, params);
                this.processResults(myCursor, txId);
                myCursor.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
            // 将错误信息反馈回前台
            this.webview.loadUrl("javascript:failQuery('" + e.getMessage()
                    + "','" + txId + "');");
        }
    }


    /**
     * 判断是否SQL语句
     *
     * @param sql
     * @return
     */
    private boolean isDDL(String sql) {
        String cmd = sql.toLowerCase();
        if (!cmd.startsWith(SELECT)) {
            return true;
        }
        return false;
    }

    /**
     * 解析结果，把条查询到的记录包装成一个json对象
     *
     * @param cursor
     * @param txId
     */
    private void processResults(Cursor cursor, String txId) {
        JSONArray result = new JSONArray();
        if (cursor.moveToFirst()) {
            String key = "";
            String value = "";
            //一共有多少条记录
            int colCount = cursor.getColumnCount();
            //第一层循环取出每条记录
            do {
                JSONObject row = new JSONObject();
                try {
                    //第二层循环取出记录中的字段名和字段值包装成json
                    for (int i = 0; i < colCount; ++i) {
                        key = cursor.getColumnName(i);
                        value = cursor.getString(i);
                        row.put(key, value);
                    }
                    //每个json是一条记录，result是整个结果
                    result.put(row);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } while (cursor.moveToNext());
        }
        LogUtils.YfcDebug("result：" + result.toString());
        // 将查询结果传回前台
        this.webview.loadUrl("javascript:completeQuery('" + txId + "','"
                + result.toString() + "');");
    }

    @Override
    public void onDestroy() {
        if (this.database != null) {
            this.database.close();
            this.database = null;
        }
    }
}
