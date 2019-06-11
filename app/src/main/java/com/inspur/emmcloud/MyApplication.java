package com.inspur.emmcloud;

import com.inspur.emmcloud.basemodule.application.BaseApplication;
import com.luojilab.component.componentlib.router.Router;
import com.luojilab.component.componentlib.router.ui.UIRouter;


/**
 * Application class
 */
public class MyApplication extends BaseApplication {

    public void onCreate() {
        super.onCreate();
        UIRouter.enableDebug();
        Router.registerComponent("com.inspur.emmcloud.applike.AppApplike");
        Router.registerComponent("com.inspur.emmcloud.login.applike.LoginAppLike");
    }

}
