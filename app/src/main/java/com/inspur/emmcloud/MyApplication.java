package com.inspur.emmcloud;

import com.inspur.emmcloud.basemodule.application.BaseApplication;
import com.inspur.emmcloud.login.communication.CommunicationService;
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
        Router router = Router.getInstance();
        if (router.getService(CommunicationService.class.getSimpleName()) != null) {
            CommunicationService service = (CommunicationService) router.getService(CommunicationService.class.getSimpleName());
            service.startWebSocket();
        }
    }

}
