package com.inspur.emmcloud.applike;

import com.inspur.emmcloud.login.appcenter.AppcenterService;
import com.inspur.emmcloud.login.web.WebService;
import com.inspur.emmcloud.servcieimpl.AppServiceImpl;
import com.inspur.emmcloud.servcieimpl.AppcenterServiceImpl;
import com.inspur.emmcloud.servcieimpl.WebServiceImpl;
import com.inspur.imp.plugin.app.AppService;
import com.luojilab.component.componentlib.applicationlike.IApplicationLike;
import com.luojilab.component.componentlib.router.Router;
import com.luojilab.component.componentlib.router.ui.UIRouter;

/**
 * Created by chenmch on 2019/5/31.
 */

public class AppApplike implements IApplicationLike {
    UIRouter uiRouter = UIRouter.getInstance();
    Router router = Router.getInstance();

    @Override
    public void onCreate() {
        uiRouter.registerUI("app");
        router.addService(AppcenterService.class.getSimpleName(), new AppcenterServiceImpl());
        router.addService(WebService.class.getSimpleName(), new WebServiceImpl());
        router.addService(AppService.class.getSimpleName(), new AppServiceImpl());
    }

    @Override
    public void onStop() {
        uiRouter.unregisterUI("app");
    }
}
