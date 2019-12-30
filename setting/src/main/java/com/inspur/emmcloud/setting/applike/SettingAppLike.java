package com.inspur.emmcloud.setting.applike;

import com.inspur.emmcloud.baselib.applicationlike.IApplicationLike;
import com.inspur.emmcloud.baselib.router.Router;
import com.inspur.emmcloud.componentservice.setting.SettingService;
import com.inspur.emmcloud.setting.serviceimpl.SettingServiceImpl;

/**
 * Created by libaochao on 2019/12/25.
 */

public class SettingAppLike implements IApplicationLike {

    Router router = Router.getInstance();
    @Override
    public void onCreate() {
        router.addService(SettingService.class, new SettingServiceImpl());
    }

    @Override
    public void onStop() {

    }

}
