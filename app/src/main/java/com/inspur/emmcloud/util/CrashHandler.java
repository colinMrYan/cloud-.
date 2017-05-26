package com.inspur.emmcloud.util;

import android.content.Context;
import android.os.Environment;

import com.inspur.emmcloud.bean.AppException;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.lang.Thread.UncaughtExceptionHandler;

/**
 * 程序异常处理类
 *
 * @author Administrator
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

	/**
	 * 获取CrashHandler实例 ,单例模式
	 */
	public static CrashHandler getInstance() {
		if (mInstance == null)
			mInstance = new CrashHandler();
		return mInstance;
	}

	@Override
	public void uncaughtException(Thread thread, Throwable throwable) {
		// 把错误的堆栈信息 获取出来
		String errorInfo = getErrorInfo(throwable);
		LogUtils.jasonDebug("errorInfo="+errorInfo);
		if (!AppUtils.isApkDebugable(mContext)) {
			AppException appException = new AppException(System.currentTimeMillis(),AppUtils.getVersion(mContext),1,"",errorInfo,-1);
			AppExceptionCacheUtils.saveAppException(mContext,appException);
		}
		// 干掉当前的程序
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

	public void init(Context context) {
		mContext = context;
		mDefaultHandler = Thread.getDefaultUncaughtExceptionHandler();
		Thread.setDefaultUncaughtExceptionHandler(this);
	}

}
