package com.inspur.emmcloud.basemodule.util;

import android.content.Context;
import android.os.Build;
import android.os.StrictMode;
import android.util.Log;

import com.inspur.emmcloud.baselib.util.PreferencesUtils;
import com.inspur.emmcloud.basemodule.api.BaseModuleApiUri;
import com.inspur.emmcloud.basemodule.application.BaseApplication;
import com.inspur.emmcloud.basemodule.bean.AppException;

import org.json.JSONArray;
import org.json.JSONObject;
import org.xutils.http.HttpMethod;
import org.xutils.http.RequestParams;
import org.xutils.x;

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
        AppException appException = new AppException(System.currentTimeMillis(), AppUtils.getVersion(mContext), 1, "", errorInfo, 0);
//        AppException appException = AppExceptionCacheUtils.getAppExceptionListByLevel(mContext, 1);
        uploadException(mContext, appException);
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
     * 上传异常,修改严格模式设置使异常上传放在主线程执行
     * @param mContext
     */
    private void uploadException(final Context mContext, final AppException appException) {
        JSONObject jsonObject = getUploadContentJSONObj(appException);
        if (NetUtils.isNetworkConnected(mContext, false) && !AppUtils.isApkDebugable(mContext)) {
            final String completeUrl = BaseModuleApiUri.getUploadExceptionUrl();
            RequestParams params = ((BaseApplication) mContext.getApplicationContext()).getHttpRequestParams(completeUrl);
            params.setAsJsonContent(true);
            params.setBodyContent(jsonObject.toString());

            //由于Android3.0之后已经不能在主线程发起网络请求，因为会造成ANR，但此处情况特殊，需临时关闭StrictMode
            StrictMode.ThreadPolicy oldThreadPolicy = StrictMode.getThreadPolicy();
            StrictMode.VmPolicy oldVmPolicy = StrictMode.getVmPolicy();
            if (Build.VERSION.SDK_INT >= 11) {
                StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder().detectDiskReads().detectDiskWrites().detectNetwork().penaltyLog().build());
                StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder().detectLeakedSqlLiteObjects().detectLeakedClosableObjects().penaltyLog().penaltyDeath().build());
            }
            try {
                x.http().requestSync(HttpMethod.POST, params, JSONObject.class);
                AppExceptionCacheUtils.deleteAppExceptionByContentAndHappenTime(mContext, appException.getHappenTime(), appException.getErrorInfo());
            } catch (Throwable throwable) {
                throwable.printStackTrace();
            }
            //时候后改回StrictMode
            StrictMode.setThreadPolicy(oldThreadPolicy);
            StrictMode.setVmPolicy(oldVmPolicy);
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
            errorDataArray.put(appException.toJSONObject());
            contentObj.put("errorData", errorDataArray);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return contentObj;
    }




}
