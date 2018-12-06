package com.inspur.emmcloud.util.privates;

import android.content.Context;

import com.inspur.emmcloud.api.APIInterfaceInstance;
import com.inspur.emmcloud.api.apiservice.AppAPIService;
import com.inspur.emmcloud.bean.system.badge.BadgeBodyModel;

import org.greenrobot.eventbus.EventBus;

/**
 * Created by chenmch on 2018/12/5.
 */

public class AppBadgeUtils {
    private AppAPIService appAPIService;
    public AppBadgeUtils(Context context){
        appAPIService = new AppAPIService(context);
        appAPIService.setAPIInterface(new WebService());
    }

    public void getAppBadgeCountFromServer(){
        appAPIService.getBadgeCount();
    }

    class WebService extends APIInterfaceInstance{
        @Override
        public void returnBadgeCountSuccess(BadgeBodyModel badgeBodyModel) {
            EventBus.getDefault().post(badgeBodyModel);
        }

        @Override
        public void returnBadgeCountFail(String error, int errorCode) {
            EventBus.getDefault().post(new BadgeBodyModel(""));
        }
    }
}
