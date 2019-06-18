package com.inspur.emmcloud.login.applike;

import com.inspur.emmcloud.baselib.applicationlike.IApplicationLike;
import com.inspur.emmcloud.baselib.router.Router;
import com.inspur.emmcloud.componentservice.login.LoginService;
import com.inspur.emmcloud.login.servcieimpl.LoginServiceImpl;

/**
 * Created by chenmch on 2019/5/7.
 */

public class LoginAppLike implements IApplicationLike {
    Router router = Router.getInstance();

    @Override
    public void onCreate() {
        router.addService(LoginService.class, new LoginServiceImpl());
    }

    @Override
    public void onStop() {
        router.removeService(LoginService.class);
    }
}
