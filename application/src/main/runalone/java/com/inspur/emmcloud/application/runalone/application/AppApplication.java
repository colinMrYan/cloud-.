package com.inspur.emmcloud.application.runalone.application;


import com.inspur.emmcloud.basemodule.application.BaseApplication;
import com.inspur.emmcloud.basemodule.config.Constant;


public class AppApplication extends BaseApplication {


    @Override
    public String getIntentClassRouterAfterLogin() {
        return Constant.AROUTER_CLASS_APPCENTER;
    }
}
