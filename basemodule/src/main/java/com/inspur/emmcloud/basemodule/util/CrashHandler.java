package com.inspur.emmcloud.basemodule.util;

import android.content.Context;
import android.os.Looper;
import android.util.Log;

import com.inspur.emmcloud.baselib.util.PreferencesUtils;
import com.inspur.emmcloud.basemodule.api.BaseModuleApiService;
import com.inspur.emmcloud.basemodule.application.BaseApplication;
import com.inspur.emmcloud.basemodule.bean.AppException;
import com.inspur.emmcloud.basemodule.config.MyAppConfig;
import com.inspur.emmcloud.basemodule.interf.ExceptionUploadInterface;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.lang.Thread.UncaughtExceptionHandler;
import java.text.SimpleDateFormat;

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
        if (mInstance == null) {
            synchronized (CrashHandler.class) {
                if (mInstance == null) {
                    mInstance = new CrashHandler();
                }
            }
        }
        return mInstance;
    }

    /**
     * 发生异常后先保存异常，然后当场上传，上传完成返回成功根据时间戳和内容删除异常记录
     *
     * @param thread
     * @param throwable
     */
    @Override
    public void uncaughtException(Thread thread, Throwable throwable) {
        // 把错误的堆栈信息 获取出来
        String errorInfo = getErrorInfo(throwable);
        if (thread != Looper.getMainLooper().getThread()) {
            ImageDisplayUtils.getInstance().clearAllCache();
        }
        Log.d("jason", "errorInfo=" + errorInfo);
        Log.e("AndroidRuntime", errorInfo);
        uploadError(errorInfo);
        if (AppUtils.isApkDebugable(mContext)) {
            saveCrashInfoFile(throwable);
        }
        //如果系统提供了默认的异常处理器，则交给系统去结束我们的程序，否则就由我们自己结束自己
        //这里如果系统处理，可能只崩溃一个页面，如果都交给自己处理，则发生异常必定整个退出
        if (mDefaultHandler != null) {
            mDefaultHandler.uncaughtException(thread, throwable);
        } else {
            try {
                Thread.sleep(2000);
            } catch (Exception e) {
                e.printStackTrace();
            }
            BaseApplication.getInstance().exit();
            // 干掉当前的程序
            android.os.Process.killProcess(android.os.Process.myPid());
            System.exit(1);
        }
    }


    /**
     * 保存错误信息到文件中
     *
     * @param ex 异常信息
     * @return 返回文件名称, 便于将文件传送到服务器
     */
    private boolean saveCrashInfoFile(Throwable ex) {
        StringBuffer sb = new StringBuffer();
        try {
            SimpleDateFormat sDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            String date = sDateFormat.format(new java.util.Date());
            sb.append("\r\n").append(date).append("\n");
            Writer writer = new StringWriter();
            PrintWriter printWriter = new PrintWriter(writer);
            ex.printStackTrace(printWriter);
            Throwable cause = ex.getCause();
            while (cause != null) {
                cause.printStackTrace(printWriter);
                cause = cause.getCause();
            }
            printWriter.flush();
            printWriter.close();
            String result = writer.toString();
            sb.append(result);
            return FileUtils.writeFile(MyAppConfig.LOCAL_IMP_CRASH, sb.toString());
        } catch (Exception e) {
            Log.e(TAG, "an error occured while writing file...", e);
        }
        return true;
    }

    private String getErrorInfo(Throwable arg1) {
        Writer writer = new StringWriter();
        PrintWriter pw = new PrintWriter(writer);
        arg1.printStackTrace(pw);
        pw.close();
        return writer.toString();
    }

    public void uploadError(Throwable throwable) {
        uploadError(getErrorInfo(throwable));
    }

    public void uploadError(String errorInfo) {
        if (NetUtils.isNetworkConnected(mContext, false) && !AppUtils.isApkDebugable(mContext)) {
            final AppException appException = new AppException(System.currentTimeMillis(), AppUtils.getVersion(mContext), 1, "", errorInfo, 0);
            AppExceptionCacheUtils.saveAppException(mContext, appException);
            new BaseModuleApiService(mContext).uploadException(mContext, getUploadContentJSONObj(appException), new ExceptionUploadInterface() {
                @Override
                public void uploadExceptionFinish(JSONObject uploadResultJSONObject) {
                    //只处理成功，其他发生任何情况都等进入后台时统一上传
                    try {
                        if (uploadResultJSONObject.getString("status").equals("success")) {
                            AppExceptionCacheUtils.deleteAppExceptionByContentAndHappenTime(mContext,
                                    appException.getHappenTime(), appException.getErrorInfo());
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
        }
    }

    public void init(Context context) {
        mContext = context;
        mDefaultHandler = Thread.getDefaultUncaughtExceptionHandler();
        Thread.setDefaultUncaughtExceptionHandler(this);
    }


    /**
     * 组织异常数据
     *
     * @param appException
     * @return
     */
    private JSONObject getUploadContentJSONObj(AppException appException) {
        JSONObject contentObj = new JSONObject();
        try {
            contentObj.put("appID", 1);
            contentObj.put("userCode", PreferencesUtils.getString(mContext, "userID", ""));
            if (BaseApplication.getInstance().getCurrentEnterprise() != null) {
                contentObj.put("enterpriseCode", BaseApplication.getInstance().getCurrentEnterprise().getId());
            } else {
                contentObj.put("enterpriseCode", "");
            }
            contentObj.put("deviceOS", "Android");
            contentObj.put("deviceOSVersion", android.os.Build.VERSION.RELEASE);
            contentObj.put("deviceModel", android.os.Build.MODEL);
            JSONArray errorDataArray = new JSONArray();
            errorDataArray.put(appException.toJSONObject());
            contentObj.put("errorData", errorDataArray);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return contentObj;
    }
}
