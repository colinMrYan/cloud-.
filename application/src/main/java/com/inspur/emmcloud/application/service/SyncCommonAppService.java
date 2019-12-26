package com.inspur.emmcloud.application.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import com.inspur.emmcloud.application.api.ApplicationAPIService;
import com.inspur.emmcloud.application.api.ApplicationApiInterfaceImpl;
import com.inspur.emmcloud.application.bean.AppCommonlyUse;
import com.inspur.emmcloud.application.util.AppCacheUtils;
import com.inspur.emmcloud.baselib.util.JSONUtils;
import com.inspur.emmcloud.baselib.util.StringUtils;
import com.inspur.emmcloud.basemodule.application.BaseApplication;
import com.inspur.emmcloud.basemodule.util.NetUtils;

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

    private void syncCommonApp() {
        if (!NetUtils.isNetworkConnected(getApplicationContext(), false) || StringUtils.isBlank(BaseApplication.getInstance().getAccessToken())) {
            stopSelf();
        }
//        String commonAppListJson = "";
//        Router router = Router.getInstance();
//        if (router.getService(ApplicationService.class) != null) {
//            ApplicationService service = router.getService(ApplicationService.class);
//            if (service.getAppCommonlyUseSize() == 0) {
//                commonAppListJson = service.getUploadCommonlyUseAppList(BaseApplication.getInstance());
//            }
//        }
//        if (!StringUtils.isBlank(commonAppListJson)) {
//            ApplicationAPIService apiService = new ApplicationAPIService(getApplicationContext());
//            apiService.setAPIInterface(new WebService());
//            apiService.syncCommonApp(commonAppListJson);
//        } else {
//            stopSelf();
//        }
        List<AppCommonlyUse> commonAppList = AppCacheUtils.getUploadCommonlyUseAppList(BaseApplication.getInstance());
        if (commonAppList.size() > 0) {
            String commonAppListJson = JSONUtils.toJSONString(commonAppList);
            ApplicationAPIService apiService = new ApplicationAPIService(getApplicationContext());
            apiService.setAPIInterface(new WebService());
            apiService.syncCommonApp(commonAppListJson);
        } else {
            stopSelf();
        }


    }


    private class WebService extends ApplicationApiInterfaceImpl {
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
