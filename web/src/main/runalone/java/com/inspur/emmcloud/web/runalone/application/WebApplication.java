package com.inspur.emmcloud.web.runalone.application;


import com.inspur.emmcloud.basemodule.application.BaseApplication;
import com.inspur.emmcloud.basemodule.config.Constant;

/**
 * Created by chenmch on 2019/5/7.
 */

public class WebApplication extends BaseApplication {

    @Override
    public String getIntentClassRouterAfterLogin() {
        return Constant.AROUTER_CLASS_WEB_MAIN_TEST;
    }
}
