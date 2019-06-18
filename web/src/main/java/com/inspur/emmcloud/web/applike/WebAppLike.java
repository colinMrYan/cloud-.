package com.inspur.emmcloud.web.applike;

import com.inspur.emmcloud.baselib.applicationlike.IApplicationLike;
import com.inspur.emmcloud.baselib.router.Router;
import com.inspur.emmcloud.componentservice.web.WebService;
import com.inspur.emmcloud.web.servcieimpl.WebServiceImpl;

/**
 * Created by chenmch on 2019/5/7.
 */

public class WebAppLike implements IApplicationLike {

    Router router = Router.getInstance();

    @Override
    public void onCreate() {
        router.addService(WebService.class, new WebServiceImpl());
    }

    @Override
    public void onStop() {
        router.removeService(WebService.class);
    }
}