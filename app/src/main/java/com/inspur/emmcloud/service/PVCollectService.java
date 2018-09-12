package com.inspur.emmcloud.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import com.inspur.emmcloud.MyApplication;
import com.inspur.emmcloud.api.APIInterfaceInstance;
import com.inspur.emmcloud.api.apiservice.AppAPIService;
import com.inspur.emmcloud.util.common.LogUtils;
import com.inspur.emmcloud.util.common.NetUtils;
import com.inspur.emmcloud.util.common.PreferencesUtils;
import com.inspur.emmcloud.util.privates.cache.PVCollectModelCacheUtils;

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
        LogUtils.LbcDebug("上传数据post");
        uploadPV();
        return super.onStartCommand(intent, flags, startId);
    }

    private void uploadPV() {
        LogUtils.LbcDebug("上传数据post");
        if (apiService == null) {
            apiService = new AppAPIService(getApplicationContext());
            apiService.setAPIInterface(new WebService());
        }
        if (NetUtils.isNetworkConnected(getApplicationContext(), false)) {
            JSONArray collectInfos = PVCollectModelCacheUtils.getPartCollectModelListJson(getApplicationContext(),50);
            if (collectInfos.length() > 0) {
                JSONObject jsonObject = new JSONObject();
                try {
                    jsonObject.put("userContent", collectInfos);
                    jsonObject.put("userID", MyApplication.getInstance().getUid());
                    if (MyApplication.getInstance().getCurrentEnterprise() != null) {
                        jsonObject.put("enterpriseID", MyApplication.getInstance().getCurrentEnterprise().getId());
                    } else {
                        jsonObject.put("enterpriseID", "");
                    }
                    jsonObject.put("userName", PreferencesUtils.getString(MyApplication.getInstance(), "userRealName", ""));
                } catch (Exception e) {
                    e.printStackTrace();
                }
                apiService.uploadPVCollect(jsonObject.toString());
                LogUtils.LbcDebug("上传数据post");
                return;
            }
            LogUtils.LbcDebug("数据长度为 0");
            stopSelf();
        }
    }



    private class WebService extends APIInterfaceInstance {

        @Override
        public void returnUploadCollectSuccess() {
            int  CurrentSize = PVCollectModelCacheUtils.deletePartCollectModel(getApplicationContext(),50);
                           if(CurrentSize<50){
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
