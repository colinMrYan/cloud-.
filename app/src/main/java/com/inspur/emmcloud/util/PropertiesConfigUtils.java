package com.inspur.emmcloud.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import android.content.Context;
import android.util.Log;

/**
 * 
 * PropertiesConfig工具类
 * 
 */
public class PropertiesConfigUtils extends Properties {

	private static final String TAG = "PropertiesConfigUtils";
	private String fileName = "";
	private Context context;
	private String subFilePath;

	private PropertiesConfigUtils(String fileName, Context context) {
		this.fileName = fileName;
		this.context = context;
	};

	private PropertiesConfigUtils(String subFilePath, String fileName,
			Context context) {
		this.fileName = fileName;
		this.context = context;
		this.subFilePath = subFilePath;
	};

	public static PropertiesConfigUtils getInstance(String fileName,
			Context context) {
		return getInstance(null, fileName, context);
	}

	/**
	 * 
	 * @param subFilePath
	 *            添加二级路径
	 * @param fileName
	 * @param context
	 * @return
	 */
	public static PropertiesConfigUtils getInstance(String subFilePath,
			String fileName, Context context) {
		{
			PropertiesConfigUtils pro = new PropertiesConfigUtils(subFilePath,
					fileName, context);
			FileInputStream stream = null;
			try {
				File file = null;
				if (subFilePath != null && !StringUtils.isBlank(subFilePath)) {
					File dir = new File("/data/data/"
							+ context.getPackageName() + "/files/"
							+ subFilePath);
					if (!dir.exists()) {
						dir.mkdirs();
					}
					file = new File("/data/data/" + context.getPackageName()
							+ "/files/" + subFilePath + fileName);
				} else {
					file = new File("/data/data/" + context.getPackageName()
							+ "/files/" + fileName);
				}
				if (!file.exists()) {
					file.createNewFile();
				}
				stream = new FileInputStream(file);
				pro.load(stream);
			} catch (Exception e) {
				LogUtils.exceptionDebug(TAG, e.toString());
			} finally {
				if (stream != null) {
					try {
						stream.close();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					stream = null;

				}
			}
			return pro;
		}

	}

	public static PropertiesConfigUtils getAssetsInstance(String fileName,
			Context context) {
		{
			PropertiesConfigUtils pro = new PropertiesConfigUtils(fileName,
					context);
			InputStream stream = null;
			try {
				stream = context.getAssets().open(fileName);
				;
				pro.load(stream);
			} catch (Exception e) {
				LogUtils.exceptionDebug(TAG, e.toString());
			} finally {
				if (stream != null) {
					try {
						stream.close();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					stream = null;

				}
			}
			return pro;
		}

	}

	@Override
	public Object setProperty(String key, String value) {
		super.setProperty(key, value);
		FileOutputStream fileos = null;
		try {
			String filePath = "";
			if (subFilePath != null && !StringUtils.isBlank(subFilePath)) {
				filePath = "/data/data/" + context.getPackageName() + "/files/"
						+ subFilePath + fileName;
			} else {
				filePath = "/data/data/" + context.getPackageName() + "/files/"
						+ fileName;
			}
			fileos = new FileOutputStream(filePath);
			// fileos = context.openFileOutput(fileName, context.MODE_PRIVATE);
			this.store(fileos, "utf-8");
		} catch (Exception e) {
			LogUtils.exceptionDebug(TAG, e.toString());
		} finally {
			if (fileos != null) {
				try {
					fileos.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				fileos = null;

			}
		}
		return value;
	}

	@Override
	public synchronized Object remove(Object key) {
		// TODO Auto-generated method stub
		super.remove(key);
		FileOutputStream fileos = null;
		try {
			fileos = context.openFileOutput(fileName, context.MODE_PRIVATE);
			this.store(fileos, "utf-8");
		} catch (Exception e) {
			LogUtils.exceptionDebug(TAG, e.toString());
		} finally {
			if (fileos != null) {
				try {
					fileos.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				fileos = null;

			}
		}
		return super.remove(key);

	}
}
