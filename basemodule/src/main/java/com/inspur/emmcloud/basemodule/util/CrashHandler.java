package com.inspur.emmcloud.basemodule.util;

import android.content.Context;
import android.util.Log;

import com.inspur.emmcloud.baselib.util.PreferencesUtils;
import com.inspur.emmcloud.basemodule.api.BaseModuleAPICallback;
import com.inspur.emmcloud.basemodule.api.BaseModuleApiUri;
import com.inspur.emmcloud.basemodule.api.CloudHttpMethod;
import com.inspur.emmcloud.basemodule.api.HttpUtils;
import com.inspur.emmcloud.basemodule.application.BaseApplication;
import com.inspur.emmcloud.basemodule.bean.AppException;

import org.json.JSONArray;
import org.json.JSONObject;
import org.xutils.http.RequestParams;

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
        AppException appException = AppExceptionCacheUtils.getAppExceptionListByLevel(mContext, 1);
        uploadException(mContext, getUploadContentJSONObj(appException), appException);
        //如果系统提供了默认的异常处理器，则交给系统去结束我们的程序，否则就由我们自己结束自己
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
     * 上传异常
     *
     * @param mContext
     * @param exception
     */
    private void uploadException(final Context mContext, final JSONObject exception, final AppException appException) {
//
        if (NetUtils.isNetworkConnected(mContext, false) && !AppUtils.isApkDebugable(mContext)) {
            final String completeUrl = BaseModuleApiUri.getUploadExceptionUrl();
            RequestParams params = ((BaseApplication) mContext.getApplicationContext()).getHttpRequestParams(completeUrl);
            params.setAsJsonContent(true);
            params.setBodyContent(exception.toString());
            HttpUtils.request(mContext, CloudHttpMethod.POST, params, new BaseModuleAPICallback(mContext, completeUrl) {

                @Override
                public void callbackTokenExpire(long requestTime) {
                    // TODO Auto-generated method stub
                }

                @Override
                public void callbackSuccess(byte[] arg0) {
                    // TODO Auto-generated method stub
                    AppExceptionCacheUtils.deleteAppException(mContext, appException);
                }

                @Override
                public void callbackFail(String error, int responseCode) {
                    // TODO Auto-generated method stub
                }
            });
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
            errorDataArray.put(appException);
            contentObj.put("errorData", errorDataArray);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return contentObj;
    }




}
