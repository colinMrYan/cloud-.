package com.inspur.emmcloud.util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.lang.Thread.UncaughtExceptionHandler;

import android.content.Context;
import android.os.Environment;
import android.util.Log;

/**
 * 程序异常处理类
 * @author Administrator
 *
 */
public class CrashHandler implements UncaughtExceptionHandler {

	private static final String TAG = "CrashHandler";
	private UncaughtExceptionHandler mDefaultHandler;
	private static CrashHandler mInstance;
	private Context mContext;
	private String errorFilePath = Environment.getExternalStorageDirectory()
			+ "/IMP-Cloud/";

	private CrashHandler() {
	}

	/** 获取CrashHandler实例 ,单例模式 */
	public static CrashHandler getInstance() {
		if (mInstance == null)
			mInstance = new CrashHandler();
		return mInstance;
	}

	@Override
	public void uncaughtException(Thread thread, Throwable throwable) {
		// 把错误的堆栈信息 获取出来
		String errorinfo = getErrorInfo(throwable);
		Log.e("AndroidRuntime", errorinfo);
		PreferencesUtils.putString(mContext, "crashtime", System.currentTimeMillis()+"");
		if (Environment.getExternalStorageState().equals(
                Environment.MEDIA_MOUNTED)){
			writeErrorToFile(errorFilePath, errorinfo);
			if (!AppUtils.isApkDebugable(mContext)) {
				writeErrorToLog(errorFilePath, errorinfo);
			}
		}
		// 干掉当前的程�?
		android.os.Process.killProcess(android.os.Process.myPid());
	}

	private String getErrorInfo(Throwable arg1) {
		Writer writer = new StringWriter();
		PrintWriter pw = new PrintWriter(writer);
		arg1.printStackTrace(pw);
		pw.close();
		String error = writer.toString();
		return error;
	}

	private void writeErrorToFile(String errorFilePath, String write_str) {
		try {
			File dirFile = new File(errorFilePath);
			if (!dirFile.exists()) {
				dirFile.mkdir();
			}
			File errorFile = new File(dirFile, "error.txt");
			if (!errorFile.exists()) {
				errorFile.createNewFile();
			}
			FileOutputStream fout = new FileOutputStream(errorFile,true);
			byte[] bytes = write_str.getBytes();
			fout.write((TimeUtils.getCurrentTimeInString(mContext)+"\r\n").getBytes());
			
			fout.write(bytes);
			fout.write("\r\n".getBytes());
			fout.close();
		}

		catch (Exception e) {
			LogUtils.exceptionDebug(TAG, e.toString());
		}
	}
	
	private void writeErrorToLog(String errorFilePath, String write_str) {
		try {
			File dirFile = new File(errorFilePath);
			if (!dirFile.exists()) {
				dirFile.mkdir();
			}
			File errorFile = new File(dirFile, "errorLog.txt");
			if (!errorFile.exists()) {
				errorFile.createNewFile();
			}
			FileOutputStream fout = new FileOutputStream(errorFile,false);
			byte[] bytes = write_str.getBytes();
			fout.write((System.currentTimeMillis()+"\r\n").getBytes());
			
			fout.write(bytes);
			fout.write("\r\n".getBytes());
			fout.close();
		}

		catch (Exception e) {
			LogUtils.exceptionDebug(TAG, e.toString());
		}
	}

	public void init(Context context) {
		mContext = context;
		mDefaultHandler = Thread.getDefaultUncaughtExceptionHandler();
		Thread.setDefaultUncaughtExceptionHandler(this);
	}

}
