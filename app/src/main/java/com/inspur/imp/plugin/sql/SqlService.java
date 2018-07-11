package com.inspur.imp.plugin.sql;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;

import com.inspur.imp.plugin.ImpPlugin;

import org.json.JSONArray;
import org.json.JSONException;
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
	SQLiteDatabase database = null;
	// 数据库路径
	String path = null;
	// 数据库名
	String dbName = null;

	@Override
	public void execute(String action, JSONObject jsonObject) {
		if ("openDatabase".equals(action)) {
			openDatabase(jsonObject);
		} else if ("executeSql".equals(action)) {
			executeSql(jsonObject);
		}else{
			showCallIMPMethodErrorDlg();
		}
	}

	@Override
	public String executeAndReturn(String action, JSONObject paramsObject) {
		showCallIMPMethodErrorDlg();
		return "";
	}

	/**
	 * 打开或者创建数据库
	 * 
	 * @param
	 *
	 * @param
	 *
	 * @param
	 *
	 * @param
	 *           
	 */
	public void openDatabase(JSONObject jsonObject) {
		String dbName = "";
		String displayName = "";
		int dbVersion = 0;
		long dbSize = 0;
		try {
			if (!jsonObject.isNull("dbName"))
				dbName = jsonObject.getString("dbName");
			if (!jsonObject.isNull("dbVersion"))
				dbVersion = jsonObject.getInt("dbVersion");
			if (!jsonObject.isNull("displayName"))
				displayName = jsonObject.getString("displayName");
			if (!jsonObject.isNull("dbSize"))
				dbSize = jsonObject.getLong("dbSize");
		} catch (JSONException e1) {
			e1.printStackTrace();
			return;
		}
		// 如果数据库已经打开则关闭数据库
		if (this.database != null) {
			this.database.close();
		}
		// 将数据库放在应用文件夹下
		if (this.path == null) {
			this.path = getFragmentContext().getApplicationContext()
					.getDir("database", Context.MODE_PRIVATE).getPath();
		}

		this.dbName = this.path + File.separator + dbName + ".db";

		/*
		 * 将数据库放在正确的文件夹下
		 */
		File oldDbFile = new File(this.path + File.pathSeparator + dbName
				+ ".db");
		if (oldDbFile.exists()) {
			File dbPath = new File(this.path);
			File dbFile = new File(dbName);
			dbPath.mkdirs();
			oldDbFile.renameTo(dbFile);
		}

		this.database = SQLiteDatabase.openOrCreateDatabase(this.dbName, null);
		this.database.setVersion(dbVersion);
		//用户没有设置数据库大小
		if (dbSize != 0)
			this.database.setMaximumSize(dbSize);
	}

	/**
	 * 对数据库进行操作
	 */
	public void executeSql(JSONObject jsonObject) {
		String sql = "";
		String param = "";
		// 查询或者插入的字段值以逗号隔开
		String[] params = null;
		String txId = "";
		try {
			if (!jsonObject.isNull("sql"))
				sql = jsonObject.getString("sql");
			if (!jsonObject.isNull("params"))
				param = jsonObject.getString("params");
			if (!jsonObject.isNull("txId"))
				txId = jsonObject.getString("txId");
		} catch (JSONException e1) {
			e1.printStackTrace();
			return;
		}
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
		} catch (SQLiteException ex) {
			ex.printStackTrace();
			// 将错误信息反馈回前台
			this.webview.loadUrl("javascript:failQuery('" + ex.getMessage()
					+ "','" + txId + "');");
		}
	}

	// 判断是否是数据库操作语句
	private boolean isDDL(String sql) {
		String cmd = sql.toLowerCase();
		if (!cmd.startsWith(SELECT)) {
			return true;
		}
		return false;
	}

	// 结果
	public void processResults(Cursor cur, String tx_id) {
		JSONArray result = new JSONArray();

		if (cur.moveToFirst()) {
			String key = "";
			String value = "";
			int colCount = cur.getColumnCount();

			do {
				JSONObject row = new JSONObject();
				try {
					for (int i = 0; i < colCount; ++i) {
						key = cur.getColumnName(i);
						value = cur.getString(i);
						row.put(key, value);
					}
					result.put(row);

				} catch (JSONException e) {
					e.printStackTrace();
				}

			} while (cur.moveToNext());
		}

		// 将查询结果传回前台
		this.webview.loadUrl("javascript:completeQuery('" + tx_id + "','"
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
