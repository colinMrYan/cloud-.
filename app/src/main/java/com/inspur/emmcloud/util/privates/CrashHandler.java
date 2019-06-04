package com.inspur.emmcloud.util.privates;

import android.content.Context;
import android.util.Log;

import com.inspur.emmcloud.MyApplication;
import com.inspur.emmcloud.basemodule.util.AppExceptionCacheUtils;

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
    private static CrashHandler mInstance;
    private UncaughtExceptionHandler mDefaultHandler;
    private Context mContext;

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
        Log.d("jason", "errorInfo=" + errorInfo);
        Log.e("AndroidRuntime", errorInfo);
        AppExceptionCacheUtils.saveAppException(mContext, 1, "", errorInfo, 0);
        //如果系统提供了默认的异常处理器，则交给系统去结束我们的程序，否则就由我们自己结束自己
        if (mDefaultHandler != null) {
            mDefaultHandler.uncaughtException(thread, throwable);
        } else {
            try {
                Thread.sleep(2000);
            } catch (Exception e) {
                e.printStackTrace();
            }
            MyApplication.getInstance().exit();
            // 干掉当前的程序
            android.os.Process.killProcess(android.os.Process.myPid());
            System.exit(1);
        }
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
