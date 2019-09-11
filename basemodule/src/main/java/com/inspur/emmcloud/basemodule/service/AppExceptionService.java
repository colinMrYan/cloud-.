package com.inspur.emmcloud.basemodule.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;

import com.inspur.emmcloud.baselib.util.PreferencesUtils;
import com.inspur.emmcloud.basemodule.api.BaseModuleAPIInterfaceInstance;
import com.inspur.emmcloud.basemodule.api.BaseModuleApiService;
import com.inspur.emmcloud.basemodule.application.BaseApplication;
import com.inspur.emmcloud.basemodule.bean.AppException;
import com.inspur.emmcloud.basemodule.util.AppExceptionCacheUtils;
import com.inspur.emmcloud.basemodule.util.AppUtils;
import com.inspur.emmcloud.basemodule.util.NetUtils;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.List;

/**
 * Created by Administrator on 2017/5/3.
 */

public class AppExceptionService extends Service {
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // TODO Auto-generated method stub
        uploadException();
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        // TODO Auto-generated method stub
        super.onDestroy();
    }

    public void uploadException() {
        if (NetUtils.isNetworkConnected(AppExceptionService.this, false) && !AppUtils.isApkDebugable(AppExceptionService.this)) {
            List<AppException> appExceptionList = AppExceptionCacheUtils.getAppExceptionList(AppExceptionService.this, 50);
            if (appExceptionList.size() != 0) {
                JSONObject uploadContentJSONObj = getUploadContentJSONObj(appExceptionList);
                BaseModuleApiService apiService = new BaseModuleApiService(AppExceptionService.this);
                apiService.setAPIInterface(new WebService());
                apiService.uploadException(uploadContentJSONObj, appExceptionList);
                return;
            }
        }
        stopSelf();
    }

    /**
     * 组织异常数据
     *
     * @param appExceptionList
     * @return
     */
    private JSONObject getUploadContentJSONObj(List<AppException> appExceptionList) {
        JSONObject contentObj = new JSONObject();
        try {
            contentObj.put("appID", 1);
            contentObj.put("userCode", PreferencesUtils.getString(AppExceptionService.this, "userID", ""));
            if (BaseApplication.getInstance().getCurrentEnterprise() != null) {
                contentObj.put("enterpriseCode", BaseApplication.getInstance().getCurrentEnterprise().getId());
            } else {
                contentObj.put("enterpriseCode", "");
            }
            contentObj.put("deviceOS", "Android");
            contentObj.put("deviceOSVersion", android.os.Build.VERSION.RELEASE);
            contentObj.put("deviceModel", android.os.Build.MODEL);

            JSONArray errorDataArray = new JSONArray();
            for (int i = 0; i < appExceptionList.size(); i++) {
                errorDataArray.put(appExceptionList.get(i).toJSONObject());
            }
            contentObj.put("errorData", errorDataArray);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return contentObj;
    }

    private class WebService extends BaseModuleAPIInterfaceInstance {
        @Override
        public void returnUploadExceptionSuccess(final List<AppException> appExceptionList) {
            AppExceptionCacheUtils.deleteAppException(AppExceptionService.this, appExceptionList);
            if (appExceptionList.size() < 50) {
                stopSelf();
            } else {
                uploadException();
            }
        }

        @Override
        public void returnUploadExceptionFail(String error, int errorCode) {
            stopSelf();
        }
    }
}
