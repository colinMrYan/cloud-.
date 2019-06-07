package com.inspur.emmcloud.login.applike;

import com.luojilab.component.componentlib.applicationlike.IApplicationLike;
import com.luojilab.component.componentlib.router.ui.UIRouter;

/**
 * Created by chenmch on 2019/5/7.
 */

public class LoginAppLike implements IApplicationLike {
    UIRouter uiRouter = UIRouter.getInstance();

    @Override
    public void onCreate() {
        uiRouter.registerUI("login");
    }

    @Override
    public void onStop() {
        uiRouter.unregisterUI("login");
    }
}
