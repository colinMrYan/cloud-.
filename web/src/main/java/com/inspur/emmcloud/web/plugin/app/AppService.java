package com.inspur.emmcloud.web.plugin.app;

import android.app.ActivityManager;
import android.content.Context;

import com.inspur.emmcloud.baselib.util.JSONUtils;
import com.inspur.emmcloud.basemodule.application.BaseApplication;
import com.inspur.emmcloud.web.plugin.ImpPlugin;

import org.json.JSONObject;

import java.util.List;


/**
 * 应用相关本地功能类
 *
 * @author 浪潮移动应用平台(IMP)产品组
 */
public class AppService extends ImpPlugin {
    private String appEnterBackgroundListener, appEnterForegroundListener;
    private boolean isForeground = false;

    @Override
    public void execute(String action, JSONObject paramsObject) {
        if ("close".equals(action)) {
            close();
        } else if ("addAppEnterBackgroundListener".equals(action)) {
            registerAppEnterBackgroundListener(paramsObject);
        } else if ("addAppEnterForegroundListener".equals(action)) {
            registerAppEnterForegroundListener(paramsObject);
        } else if ("removeAppEnterBackgroundListener".equals(action)) {
            removeEnterBackgroundListener(paramsObject);
        }  else if ("removeAppEnterForegroundListener".equals(action)) {
            removeEnterForegroundListener(paramsObject);
        } else {
            showCallIMPMethodErrorDlg();
        }
    }

    @Override
    public String executeAndReturn(String action, JSONObject paramsObject) {
        // TODO Auto-generated method stub
        // 退出系统
        if ("close".equals(action)) {
            close();
        } else if ("addAppEnterBackgroundListener".equals(action)) {
            registerAppEnterBackgroundListener(paramsObject);
        } else if ("addAppEnterForegroundListener".equals(action)) {
            registerAppEnterForegroundListener(paramsObject);
        }  else if ("removeAppEnterBackgroundListener".equals(action)) {
            removeEnterBackgroundListener(paramsObject);
        }  else if ("removeAppEnterForegroundListener".equals(action)) {
            removeEnterForegroundListener(paramsObject);
        } else {
            showCallIMPMethodErrorDlg();
        }
        return "";
    }


    /**
     * 关闭网页所在的Activity（当网页所在的Activity 为IndexActivity时不关闭）
     */
    private void close() {
        boolean isWebFromIndex = false;
        if (getImpCallBackInterface() != null) {
            isWebFromIndex = getImpCallBackInterface().isWebFromIndex();
        }
        if (!isWebFromIndex) {
            getActivity().finish();
        }

    }

    private void registerAppEnterBackgroundListener(JSONObject paramsObject) {
        if (appEnterBackgroundListener == null && !paramsObject.isNull("success")) {
            appEnterBackgroundListener = JSONUtils.getString(paramsObject, "success", "");
        }
    }

    private void registerAppEnterForegroundListener(JSONObject paramsObject) {
        if (appEnterForegroundListener == null && !paramsObject.isNull("success")) {
            appEnterForegroundListener = JSONUtils.getString(paramsObject, "success", "");
        }

    }

    private void removeEnterBackgroundListener(JSONObject paramsObject) {
        if (appEnterBackgroundListener != null) {
            appEnterBackgroundListener = null;
            if (!paramsObject.isNull("success")) {
                String callback = JSONUtils.getString(paramsObject, "success", "");
                jsCallback(callback, "");
            }
        }
    }

    private void removeEnterForegroundListener(JSONObject paramsObject) {
        if (appEnterForegroundListener != null) {
            appEnterForegroundListener = null;
            if (!paramsObject.isNull("success")) {
                String callback = JSONUtils.getString(paramsObject, "success", "");
                jsCallback(callback, "");
            }
        }

    }

    @Override
    public void onDestroy() {
        // TODO Auto-generated method stub
    }

    @Override
    public void onActivityResume() {
        if (!isForeground) {
            isForeground = true;
            jsCallback(appEnterForegroundListener, "");
        }
    }

    @Override
    public void onActivityPause() {
        if (!isAppOnForeground()) {
            isForeground = false;
            jsCallback(appEnterBackgroundListener, "");
        }
    }

    /**
     * 判断app是否处于前台
     * @return
     */
    public boolean isAppOnForeground() {
        ActivityManager activityManager = (ActivityManager) BaseApplication.getInstance()
                .getSystemService(Context.ACTIVITY_SERVICE);
        String packageName = BaseApplication.getInstance().getPackageName();
        List<ActivityManager.RunningAppProcessInfo> appProcesses = activityManager
                .getRunningAppProcesses();
        if (appProcesses == null)
            return false;
        for (ActivityManager.RunningAppProcessInfo appProcess : appProcesses) {
            if (appProcess.processName.equals(packageName)
                    && appProcess.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND) {
                return true;
            }
        }
        return false;
    }
}
