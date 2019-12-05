package com.inspur.emmcloud.application.util;

import android.content.Context;

import com.inspur.emmcloud.application.api.ApplicationAPIService;
import com.inspur.emmcloud.application.api.ApplicationApiInterfaceImpl;
import com.inspur.emmcloud.application.bean.BadgeBodyModel;
import com.inspur.emmcloud.basemodule.util.NetUtils;

import org.greenrobot.eventbus.EventBus;

/**
 * Created by chenmch on 2018/12/5.
 */

public class AppBadgeUtils {
    private Context context;
    private ApplicationAPIService appAPIService;

    public AppBadgeUtils(Context context) {
        this.context = context;
        appAPIService = new ApplicationAPIService(context);
        appAPIService.setAPIInterface(new WebService());
    }

    public void getAppBadgeCountFromServer() {
        if (NetUtils.isNetworkConnected(context, false)) {
            appAPIService.getBadgeCount();
        }
    }

    class WebService extends ApplicationApiInterfaceImpl {
        @Override
        public void returnBadgeCountSuccess(BadgeBodyModel badgeBodyModel) {
            EventBus.getDefault().post(badgeBodyModel);
        }

        @Override
        public void returnBadgeCountFail(String error, int errorCode) {
        }
    }
}
