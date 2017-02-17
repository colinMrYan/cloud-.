package com.inspur.imp.util;

import android.app.AlertDialog;
import android.content.Context;
import android.os.Looper;

import com.inspur.imp.api.Res;
import com.inspur.imp.api.iLog;

import java.lang.Thread.UncaughtExceptionHandler;




/**
 * 获取系统强制退出异常
 * 
 * @author xiepp
 * 
 */
public class CrashHandler implements UncaughtExceptionHandler {

	private Context context;
	private UncaughtExceptionHandler mHandler;
	private CrashHandler crashHandler = new CrashHandler();

	/** 保证只有一个CrashHandler实例 */
	private CrashHandler() {

	}

	private CrashHandler getInatance() {

		return crashHandler;
	}

	private void init(Context context) {

		this.context = context;
		mHandler = Thread.getDefaultUncaughtExceptionHandler();
		Thread.setDefaultUncaughtExceptionHandler(this);
	}

	@Override
	public void uncaughtException(Thread thread, Throwable ex) {

		if (!handlerException(ex) && mHandler != null) {
			mHandler.uncaughtException(thread, ex);
		} else {
			try {
				Thread.sleep(3000);
			} catch (InterruptedException e) {


				iLog.e("CrashHandler", "error : ", e);
			}
			System.exit(1);
		}

	}

	private boolean handlerException(Throwable ex) {

		if (ex == null) {
			return false;
		}
		new Thread() {
			@Override
			public void run() {
				super.run();
				Looper.prepare();
				AlertDialog.Builder dialog = new AlertDialog.Builder(context);
				dialog.setPositiveButton(Res.getStringID("file_ok"), null);
			}

		};
		return true;
	}

}
