package com.inspur.emmcloud.basemodule.util;

import com.inspur.emmcloud.baselib.util.PreferencesUtils;
import com.inspur.emmcloud.basemodule.api.BaseModuleAPIInterfaceInstance;
import com.inspur.emmcloud.basemodule.api.BaseModuleApiService;
import com.inspur.emmcloud.basemodule.application.BaseApplication;
import com.inspur.emmcloud.basemodule.bean.PVCollectModel;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.List;

/**
 * Created by chenmch on 2019/12/13.
 * App 异常上传
 */

public class AppPVManager extends BaseModuleAPIInterfaceInstance {
    private static final int UPLOAD_PV_SIZE = 500;

    public void uploadPV() {
        if (NetUtils.isNetworkConnected(BaseApplication.getInstance(), false) && (!DbCacheUtils.isDbNull())) {
            BaseModuleApiService apiService = new BaseModuleApiService(BaseApplication.getInstance());
            apiService.setAPIInterface(this);
            List<PVCollectModel> collectModelList = PVCollectModelCacheUtils.getCollectModelList(BaseApplication.getInstance(), UPLOAD_PV_SIZE);
            if (collectModelList.size() > 0) {
                JSONArray array = PVCollectModelCacheUtils.getCollectModelListJson(BaseApplication.getInstance(), collectModelList);
                JSONObject jsonObject = new JSONObject();
                try {
                    jsonObject.put("userContent", array);
                    jsonObject.put("userID", BaseApplication.getInstance().getUid());
                    jsonObject.put("clientType", "Android");
                    jsonObject.put("appVersion", AppUtils.getVersion(BaseApplication.getInstance()));
                    if (BaseApplication.getInstance().getCurrentEnterprise() != null) {
                        jsonObject.put("enterpriseID", BaseApplication.getInstance().getCurrentEnterprise().getId());
                    } else {
                        jsonObject.put("enterpriseID", "");
                    }
                    jsonObject.put("userName", PreferencesUtils.getString(BaseApplication.getInstance(), "userRealName", ""));
                } catch (Exception e) {
                    e.printStackTrace();
                }
                apiService.uploadPVCollect(jsonObject.toString(), collectModelList);
            }
        }
    }


    @Override
    public void returnUploadCollectSuccess(List<PVCollectModel> collectModelList) {
        PVCollectModelCacheUtils.deleteCollectModel(BaseApplication.getInstance(), collectModelList);
        if (collectModelList.size() == UPLOAD_PV_SIZE) {
            uploadPV();
        }
    }

    @Override
    public void returnUploadCollectFail(String error, int errorCode) {
    }
}
