package com.inspur.emmcloud.application.applike;

import com.inspur.emmcloud.application.serviceimpl.AppCenterServiceImpl;
import com.inspur.emmcloud.baselib.applicationlike.IApplicationLike;
import com.inspur.emmcloud.baselib.router.Router;
import com.inspur.emmcloud.componentservice.appcenter.AppcenterService;

/**
 * Created by: yufuchang
 * Date: 2019/11/27
 */
public class ApplicationAppLike implements IApplicationLike {
    Router router = Router.getInstance();

    @Override
    public void onCreate() {
        router.addService(AppcenterService.class, new AppCenterServiceImpl());
    }

    @Override
    public void onStop() {
        router.removeService(AppcenterService.class);
    }
}
