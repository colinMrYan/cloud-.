package com.inspur.emmcloud.util.privates;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.inspur.emmcloud.baselib.util.JSONUtils;
import com.inspur.emmcloud.baselib.util.LogUtils;
import com.inspur.emmcloud.basemodule.config.MyAppConfig;
import com.inspur.emmcloud.bean.chat.Robot;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class SqlUtils {

    public static SQLiteDatabase DBManager(String dbName) {
        String dbPath = MyAppConfig.LOCAL_DOWNLOAD_PATH + dbName;
        return SQLiteDatabase.openOrCreateDatabase(dbPath, null);
    }

    public static List<Robot> query(SQLiteDatabase sqliteDB, String[] columns, String selection, String[] selectionArgs) {
        List<Robot> robotList = new ArrayList<>();
        Robot robot = null;
        try {
            String table = "Robot";
            Cursor cursor = sqliteDB.query(table, columns, selection, selectionArgs, null, null, null);
//            while (cursor.moveToNext()) {
//                String title = cursor.getString(cursor.getColumnIndex("title"));
//                String support = cursor.getString(cursor.getColumnIndex("support"));
//                String name = cursor.getString(cursor.getColumnIndex("name"));
//                String mode = cursor.getString(cursor.getColumnIndex("mode"));
//                String avatar = cursor.getString(cursor.getColumnIndex("avatar"));
//                String id = cursor.getString(cursor.getColumnIndex("id"));
//                robot= new Robot();
//                robot.setTitle(title);
//                robot.setSupport(support);
//                robot.setName(name);
//                robot.setMode(mode);
//                robot.setAvatar(avatar);
//                robot.setId(id);
//                robotList.add(robot);
//            }
//            cursor.close();
//            return robotList;
            processResults(cursor, "");
        } catch (Exception e) {
            LogUtils.YfcDebug("异常：" + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }

    public static List<String> getAllTableName(SQLiteDatabase sqliteDB) {
        Cursor cursor = sqliteDB.rawQuery("select name from sqlite_master where type='table' order by name", null);
        while (cursor.moveToNext()) {
            //遍历出表名
            String name = cursor.getString(0);
            LogUtils.YfcDebug("表名：" + name);
        }
        return null;
    }

    public static void getTableContent(SQLiteDatabase sqLiteDatabase) {
        Cursor c = sqLiteDatabase.rawQuery("SELECT * FROM testTable  WHERE 0", null);
        try {
            String[] columnNames = c.getColumnNames();
            LogUtils.YfcDebug("表里所有字段名：" + JSONUtils.toJSONString(columnNames));
        } finally {
            c.close();
        }
    }

    /**
     * 解析结果，把条查询到的记录包装成一个json对象
     *
     * @param cursor
     * @param txId
     */
    private static void processResults(Cursor cursor, String txId) {
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
    }
}
