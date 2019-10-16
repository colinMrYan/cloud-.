package com.inspur.emmcloud.basemodule.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import com.inspur.emmcloud.baselib.util.PreferencesUtils;
import com.inspur.emmcloud.basemodule.api.BaseModuleAPIInterfaceInstance;
import com.inspur.emmcloud.basemodule.api.BaseModuleApiService;
import com.inspur.emmcloud.basemodule.application.BaseApplication;
import com.inspur.emmcloud.basemodule.bean.PVCollectModel;
import com.inspur.emmcloud.basemodule.util.AppUtils;
import com.inspur.emmcloud.basemodule.util.NetUtils;
import com.inspur.emmcloud.basemodule.util.PVCollectModelCacheUtils;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class PVCollectService extends Service {

    private BaseModuleApiService apiService;
    private static final int UPLOAD_PV_SIZE = 500;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        uploadPV();
        return super.onStartCommand(intent, flags, startId);
    }

    private void uploadPV() {
        List<PVCollectModel> collectModelList = new ArrayList<>();
        if (apiService == null) {
            apiService = new BaseModuleApiService(getApplicationContext());
            apiService.setAPIInterface(new WebService());
        }
        if (NetUtils.isNetworkConnected(getApplicationContext(), false)) {
            collectModelList = PVCollectModelCacheUtils.getCollectModelList(getApplicationContext(), UPLOAD_PV_SIZE);
            if (collectModelList.size() > 0) {
                JSONArray collectInfos = PVCollectModelCacheUtils.getCollectModelListJson(getApplicationContext(), collectModelList);
                JSONObject jsonObject = new JSONObject();
                try {
                    jsonObject.put("userContent", collectInfos);
                    jsonObject.put("userID", BaseApplication.getInstance().getUid());
                    jsonObject.put("clientType", "Android");
                    jsonObject.put("appVersion", AppUtils.getVersion(this));
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
                return;
            }
            stopSelf();
        }
    }


    private class WebService extends BaseModuleAPIInterfaceInstance {
        @Override
        public void returnUploadCollectSuccess(List<PVCollectModel> collectModelList) {
            PVCollectModelCacheUtils.deleteCollectModel(getApplicationContext(), collectModelList);
            if (collectModelList.size() < UPLOAD_PV_SIZE) {
                stopSelf();
            } else {
                uploadPV();
            }
        }

        @Override
        public void returnUploadCollectFail(String error, int errorCode) {
            stopSelf();
        }

    }
}
