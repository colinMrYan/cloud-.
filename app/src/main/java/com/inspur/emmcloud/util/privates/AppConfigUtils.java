package com.inspur.emmcloud.util.privates;

import android.content.Context;

import com.alibaba.fastjson.JSON;
import com.inspur.emmcloud.api.APIInterfaceInstance;
import com.inspur.emmcloud.api.apiservice.AppAPIService;
import com.inspur.emmcloud.bean.appcenter.AppCommonlyUse;
import com.inspur.emmcloud.bean.system.AppConfig;
import com.inspur.emmcloud.bean.system.GetAppConfigResult;
import com.inspur.emmcloud.interf.CommonCallBack;
import com.inspur.emmcloud.config.Constant;
import com.inspur.emmcloud.util.common.NetUtils;
import com.inspur.emmcloud.util.privates.cache.AppCacheUtils;
import com.inspur.emmcloud.util.privates.cache.AppConfigCacheUtils;

import java.util.List;

/**
 * Created by chenmch on 2017/10/12.
 */

public class AppConfigUtils {
    private Context context;
    private CommonCallBack callBack;
    public AppConfigUtils(Context context, CommonCallBack callBack){
        this.context = context;
        this.callBack = callBack;
    }

    public void getAppConfig(){
        if (NetUtils.isNetworkConnected(context,false)){
            AppAPIService apiService = new AppAPIService(context);
            apiService.setAPIInterface(new WebService());
            apiService.getAppConfig();
        }
    }

    /**
     * 当获取到服务端常用应用后，查看本地是否有常用应用记录，如果没有的话把数据存到本地
     */
    private void syncCommonAppToLocalDb(){
        String commonAppListJson = AppConfigCacheUtils.getAppConfigValue(context, Constant.CONCIG_COMMON_FUNCTIONS,"null");
        if (!commonAppListJson.equals("null")){
            List<AppCommonlyUse> commonAppList = AppCacheUtils.getCommonlyUseList(context);
            if (commonAppList.size() == 0){
                commonAppList = JSON.parseArray(commonAppListJson,AppCommonlyUse.class);
                AppCacheUtils.saveAppCommonlyUseList(context,commonAppList);
            }
        }
    }

    private class WebService extends APIInterfaceInstance{
        @Override
        public void returnAppConfigSuccess(GetAppConfigResult getAppConfigResult) {
            List<AppConfig> appConfigList = getAppConfigResult.getAppConfigList();
            AppConfigCacheUtils.clearAndSaveAppConfigList(context,appConfigList);
            if (callBack != null){
                callBack.execute();
            }
            syncCommonAppToLocalDb();
        }

        @Override
        public void returnAppConfigFail(String error, int errorCode) {
            WebServiceMiddleUtils.hand(context,error,errorCode);
        }
    }
}
