package com.inspur.emmcloud.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import com.inspur.emmcloud.MyApplication;
import com.inspur.emmcloud.api.APIInterfaceInstance;
import com.inspur.emmcloud.api.apiservice.AppAPIService;
import com.inspur.emmcloud.util.ContactCacheUtils;
import com.inspur.emmcloud.util.LogUtils;
import com.inspur.emmcloud.util.NetUtils;
import com.inspur.emmcloud.util.PVCollectModelCacheUtils;

import org.json.JSONArray;
import org.json.JSONObject;

public class PVCollectService extends Service {

    private AppAPIService apiService;
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        init();
        return START_REDELIVER_INTENT;
    }

    private void init() {

        if (apiService == null) {
            apiService = new AppAPIService(getApplicationContext());
            apiService.setAPIInterface(new WebService());
        }

        if (NetUtils.isNetworkConnected(getApplicationContext(), false)) {
            JSONArray collectInfos = PVCollectModelCacheUtils.getCollectModelListJson(getApplicationContext());
            if (collectInfos.length() > 0) {
                JSONObject jsonObject = new JSONObject();
                try {
                    jsonObject.put("userContent", collectInfos);
                    String userId = ((MyApplication) getApplication()).getUid();
                    jsonObject.put("userID", userId);
                    jsonObject.put("userName", ContactCacheUtils.
                            getUserContact(PVCollectService.this, userId)
                            .getName());
                } catch (Exception e) {
                    e.printStackTrace();
                }
                apiService.uploadPVCollect(jsonObject.toString());
                return;
            }
        }
        onDestroy();
    }



    private class WebService extends APIInterfaceInstance {

        @Override
        public void returnUploadCollectSuccess() {
            PVCollectModelCacheUtils.deleteAllCollectModel(getApplicationContext());
            onDestroy();
        }

        @Override
        public void returnUploadCollectFail(String error, int errorCode) {
            onDestroy();
        }

    }
}
