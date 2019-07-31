package com.inspur.emmcloud.mail.runalone.application;


import com.inspur.emmcloud.basemodule.application.BaseApplication;
import com.inspur.emmcloud.basemodule.config.Constant;

/**
 * Created by chenmch on 2019/5/7.
 */

public class MailApplication extends BaseApplication {

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public String getIntentClassRouterAfterLogin() {
        return Constant.AROUTER_CLASS_MAIL_LOGIN;
    }
}
