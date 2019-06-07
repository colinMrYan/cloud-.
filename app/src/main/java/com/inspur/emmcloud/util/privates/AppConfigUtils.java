package com.inspur.emmcloud.util.privates;

import android.content.Context;
import android.os.Handler;
import android.os.Message;

import com.inspur.emmcloud.api.APIInterfaceInstance;
import com.inspur.emmcloud.api.apiservice.AppAPIService;
import com.inspur.emmcloud.baselib.util.JSONUtils;
import com.inspur.emmcloud.baselib.util.StringUtils;
import com.inspur.emmcloud.basemodule.config.Constant;
import com.inspur.emmcloud.basemodule.util.NetUtils;
import com.inspur.emmcloud.bean.appcenter.AppCommonlyUse;
import com.inspur.emmcloud.bean.system.AppConfig;
import com.inspur.emmcloud.bean.system.GetAppConfigResult;
import com.inspur.emmcloud.interf.CommonCallBack;
import com.inspur.emmcloud.util.privates.cache.AppCacheUtils;
import com.inspur.emmcloud.util.privates.cache.AppConfigCacheUtils;

import java.util.List;

/**
 * Created by chenmch on 2017/10/12.
 */

public class AppConfigUtils {
    private Context context;
    private CommonCallBack callBack;
    private Handler handler;

    public AppConfigUtils(Context context, CommonCallBack callBack) {
        this.context = context;
        this.callBack = callBack;
        handMessage();
    }

    private void handMessage() {
        handler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                if (callBack != null) {
                    callBack.execute();
                }
            }
        };
    }

    public void getAppConfig() {
        if (NetUtils.isNetworkConnected(context, false)) {
            AppAPIService apiService = new AppAPIService(context);
            apiService.setAPIInterface(new WebService());
            String commonAppListJson = AppConfigCacheUtils.getAppConfigValue(context, Constant.CONCIG_COMMON_FUNCTIONS, "null");
            boolean isGetCommonAppConfig = commonAppListJson.equals("null");
            String WorkPortletConfigJson = AppConfigCacheUtils.getAppConfigValue(context, "WorkPortlet", "null");
            boolean isGetWorkPortletAppConfig = WorkPortletConfigJson.equals("null");
            String webAutoRotateJson = AppConfigCacheUtils.getAppConfigValue(context, Constant.CONCIG_WEB_AUTO_ROTATE, "null");
            boolean isGetWebAutoRotate = webAutoRotateJson.equals("null");
            apiService.getAppConfig(isGetCommonAppConfig, isGetWorkPortletAppConfig, isGetWebAutoRotate);
        }
    }

    /**
     * 当获取到服务端常用应用后，查看本地是否有常用应用记录，如果没有的话把数据存到本地
     */
    private void syncCommonAppToLocalDb() {
        String commonAppListJson = AppConfigCacheUtils.getAppConfigValue(context, Constant.CONCIG_COMMON_FUNCTIONS, "null");
        if (!commonAppListJson.equals("null") && !StringUtils.isBlank(commonAppListJson)) {
            List<AppCommonlyUse> commonAppList = AppCacheUtils.getCommonlyUseList(context);
            if (commonAppList.size() == 0) {
                commonAppList = JSONUtils.parseArray(commonAppListJson, AppCommonlyUse.class);
                AppCacheUtils.saveAppCommonlyUseList(context, commonAppList);
            }
        }
    }

    class SyncAppConfigThread extends Thread {
        private GetAppConfigResult getAppConfigResult;

        public SyncAppConfigThread(GetAppConfigResult getAppConfigResult) {
            this.getAppConfigResult = getAppConfigResult;
        }

        @Override
        public void run() {
            try {
                List<AppConfig> appConfigList = getAppConfigResult.getAppConfigList();
                AppConfigCacheUtils.saveAppConfigList(context, appConfigList);
                syncCommonAppToLocalDb();
                if (handler != null) {
                    handler.sendEmptyMessage(1);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private class WebService extends APIInterfaceInstance {
        @Override
        public void returnAppConfigSuccess(GetAppConfigResult getAppConfigResult) {
            new SyncAppConfigThread(getAppConfigResult).start();
        }

        @Override
        public void returnAppConfigFail(String error, int errorCode) {
            // WebServiceMiddleUtils.hand(context,error,errorCode);
        }
    }
}
