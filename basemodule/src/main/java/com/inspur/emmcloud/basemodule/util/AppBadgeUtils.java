package com.inspur.emmcloud.basemodule.util;

import android.content.Context;

import com.inspur.emmcloud.basemodule.api.BaseModuleAPIInterfaceInstance;
import com.inspur.emmcloud.basemodule.api.BaseModuleApiService;
import com.inspur.emmcloud.basemodule.bean.badge.AppBadgeModel;
import com.inspur.emmcloud.basemodule.bean.badge.BadgeBodyModel;

import org.greenrobot.eventbus.EventBus;

/**
 * Created by chenmch on 2018/12/5.
 */

public class AppBadgeUtils {
    private Context context;
    private BaseModuleApiService appAPIService;

    public AppBadgeUtils(Context context) {
        this.context = context;
        appAPIService = new BaseModuleApiService(context);
        appAPIService.setAPIInterface(new WebService());
    }

    public void getAppBadgeCountFromServer() {
        if (NetUtils.isNetworkConnected(context, false)) {
            appAPIService.getBadgeCount();
            appAPIService.getBadgeCountFromBadgeServer();
        }
    }

    class WebService extends BaseModuleAPIInterfaceInstance {
        @Override
        public void returnBadgeCountSuccess(BadgeBodyModel badgeBodyModel) {
            EventBus.getDefault().post(badgeBodyModel);
        }

        @Override
        public void returnBadgeCountFail(String error, int errorCode) {
        }

        @Override
        public void returnBadgeCountFromBadgeServerSuccess(AppBadgeModel appBadgeModel) {
            EventBus.getDefault().post(appBadgeModel);
        }

        @Override
        public void returnBadgeCountFromBadgeServerFail(String error, int errorCode) {
        }
    }
}
