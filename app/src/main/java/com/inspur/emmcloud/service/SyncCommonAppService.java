package com.inspur.emmcloud.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import com.alibaba.fastjson.JSON;
import com.inspur.emmcloud.api.APIInterfaceInstance;
import com.inspur.emmcloud.api.apiservice.MyAppAPIService;
import com.inspur.emmcloud.bean.AppCommonlyUse;
import com.inspur.emmcloud.util.AppCacheUtils;
import com.inspur.emmcloud.util.NetUtils;

import java.util.List;

/**
 * 跟服务端同步常用应用数据
 */

public class SyncCommonAppService extends Service {
    @Override
    public IBinder onBind(Intent intent) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        syncCommonApp();
        return super.onStartCommand(intent, flags, startId);
    }

    private void syncCommonApp(){
        if (!NetUtils.isNetworkConnected(getApplicationContext(),false)){
            stopSelf();
        }

        List<AppCommonlyUse> commonAppList = AppCacheUtils.getCommonlyUseAppList(getApplicationContext());
        if (commonAppList.size()>0){
            String commonAppListJson = JSON.toJSONString(commonAppList);
            MyAppAPIService apiService = new MyAppAPIService(getApplicationContext());
            apiService.setAPIInterface(new WebService());
            apiService.syncCommonApp(commonAppListJson);
        }else {
            stopSelf();
        }


    }


    private class WebService extends APIInterfaceInstance{
        @Override
        public void returnSaveConfigSuccess() {
            stopSelf();
        }

        @Override
        public void returnSaveConfigFail() {
            stopSelf();
        }
    }
}
