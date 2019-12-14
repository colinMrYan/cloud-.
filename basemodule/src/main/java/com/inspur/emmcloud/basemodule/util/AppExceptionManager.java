package com.inspur.emmcloud.basemodule.util;

import com.inspur.emmcloud.baselib.util.PreferencesUtils;
import com.inspur.emmcloud.basemodule.api.BaseModuleAPIInterfaceInstance;
import com.inspur.emmcloud.basemodule.api.BaseModuleApiService;
import com.inspur.emmcloud.basemodule.application.BaseApplication;
import com.inspur.emmcloud.basemodule.bean.AppException;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.List;

/**
 * Created by chenmch on 2019/12/13.
 */

public class AppExceptionManager extends BaseModuleAPIInterfaceInstance {

    public void uploadException() {
        if (NetUtils.isNetworkConnected(BaseApplication.getInstance(), false) && !AppUtils.isApkDebugable(BaseApplication.getInstance())) {
            List<AppException> appExceptionList = AppExceptionCacheUtils.getAppExceptionList(BaseApplication.getInstance(), 50);
            if (appExceptionList.size() != 0) {
                JSONObject uploadContentJSONObj = getUploadContentJSONObj(appExceptionList);
                BaseModuleApiService apiService = new BaseModuleApiService(BaseApplication.getInstance());
                apiService.setAPIInterface(this);
                apiService.uploadException(uploadContentJSONObj, appExceptionList);
            }
        }
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
            contentObj.put("userCode", PreferencesUtils.getString(BaseApplication.getInstance(), "userID", ""));
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

    @Override
    public void returnUploadExceptionSuccess(final List<AppException> appExceptionList) {
        AppExceptionCacheUtils.deleteAppException(BaseApplication.getInstance(), appExceptionList);
        if (appExceptionList.size() == 50) {
            uploadException();
        }
    }

    @Override
    public void returnUploadExceptionFail(String error, int errorCode) {
    }
}
