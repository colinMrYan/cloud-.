package com.inspur.emmcloud.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import com.inspur.emmcloud.MyApplication;
import com.inspur.emmcloud.api.APIInterfaceInstance;
import com.inspur.emmcloud.api.apiservice.AppAPIService;
import com.inspur.emmcloud.bean.system.PVCollectModel;
import com.inspur.emmcloud.util.common.NetUtils;
import com.inspur.emmcloud.util.common.PreferencesUtils;
import com.inspur.emmcloud.util.privates.cache.PVCollectModelCacheUtils;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class PVCollectService extends Service {

    private AppAPIService apiService;
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
            apiService = new AppAPIService(getApplicationContext());
            apiService.setAPIInterface(new WebService());
        }
        if (NetUtils.isNetworkConnected(getApplicationContext(), false)) {
             collectModelList = PVCollectModelCacheUtils.getCollectModelList(getApplicationContext(), 50);
            if (collectModelList.size() > 0) {
                JSONArray collectInfos = PVCollectModelCacheUtils.getCollectModelListJson(getApplicationContext(),collectModelList);
                JSONObject jsonObject = new JSONObject();
                try {
                    jsonObject.put("userContent", collectInfos);
                    jsonObject.put("userID", MyApplication.getInstance().getUid());
                    jsonObject.put("clientType","Android");
                    if (MyApplication.getInstance().getCurrentEnterprise() != null) {
                        jsonObject.put("enterpriseID", MyApplication.getInstance().getCurrentEnterprise().getId());
                    } else {
                        jsonObject.put("enterpriseID", "");
                    }
                    jsonObject.put("userName", PreferencesUtils.getString(MyApplication.getInstance(), "userRealName", ""));
                } catch (Exception e) {
                    e.printStackTrace();
                }
                apiService.uploadPVCollect(jsonObject.toString(),collectModelList);
                return;
            }
            stopSelf();
        }
    }



    private class WebService extends APIInterfaceInstance {
        @Override
        public void returnUploadCollectSuccess( List<PVCollectModel> collectModelList ) {
            int  CurrentSize = PVCollectModelCacheUtils.deleteCollectModel(getApplicationContext(),collectModelList);
                           if(CurrentSize<50) {
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
