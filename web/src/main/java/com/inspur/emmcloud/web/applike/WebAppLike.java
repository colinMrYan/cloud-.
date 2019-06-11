package com.inspur.emmcloud.web.applike;

import com.inspur.emmcloud.login.login.LoginService;
import com.inspur.emmcloud.login.servcieimpl.LoginServiceImpl;
import com.luojilab.component.componentlib.applicationlike.IApplicationLike;
import com.luojilab.component.componentlib.router.Router;
import com.luojilab.component.componentlib.router.ui.UIRouter;

/**
 * Created by chenmch on 2019/5/7.
 */

public class WebAppLike implements IApplicationLike {
    UIRouter uiRouter = UIRouter.getInstance();
    Router router = Router.getInstance();

    @Override
    public void onCreate() {
        uiRouter.registerUI("web");
        router.addService(LoginService.class.getSimpleName(), new LoginServiceImpl());
    }

    @Override
    public void onStop() {
        uiRouter.unregisterUI("web");
        router.addService(LoginService.class.getSimpleName(), new LoginServiceImpl());
    }
}
