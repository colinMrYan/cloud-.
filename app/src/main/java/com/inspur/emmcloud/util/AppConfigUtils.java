package com.inspur.emmcloud.util;

import android.content.Context;
import android.content.Intent;

import com.inspur.emmcloud.api.APIInterfaceInstance;
import com.inspur.emmcloud.api.apiservice.AppAPIService;
import com.inspur.emmcloud.bean.AppConfig;
import com.inspur.emmcloud.bean.GetAppConfigResult;
import com.inspur.emmcloud.service.LocationService;

import java.util.List;

/**
 * Created by chenmch on 2017/10/12.
 */

public class AppConfigUtils {
    private Context context;
    public AppConfigUtils(Context context){
        this.context = context;
    }

    public void getAppConfig(){
        if (NetUtils.isNetworkConnected(context,false)){
            AppAPIService apiService = new AppAPIService(context);
            apiService.setAPIInterface(new WebService());
            apiService.getAppConfig();
        }
    }

    /**
     * 打开位置收集服务
     */
    private void startLocationService(){
        Intent intent = new Intent();
        intent.setClass(context, LocationService.class);
        context.startService(intent);
    }


    private class WebService extends APIInterfaceInstance{
        @Override
        public void returnAppConfigSuccess(GetAppConfigResult getAppConfigResult) {
            List<AppConfig> appConfigList = getAppConfigResult.getAppConfigList();
            AppConfigCacheUtils.clearAndSaveAppConfigList(context,appConfigList);
            startLocationService();
        }

        @Override
        public void returnAppConfigFail(String error, int errorCode) {
            WebServiceMiddleUtils.hand(context,error,errorCode);
        }
    }
}
