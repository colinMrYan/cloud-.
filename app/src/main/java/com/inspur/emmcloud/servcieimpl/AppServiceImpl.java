package com.inspur.emmcloud.servcieimpl;

import com.inspur.emmcloud.basemodule.application.BaseApplication;
import com.inspur.emmcloud.login.app.AppService;
import com.inspur.emmcloud.util.privates.AppBadgeUtils;

/**
 * Created by chenmch on 2019/6/6.
 */

public class AppServiceImpl implements AppService {
    @Override
    public void getAppBadgeCountFromServer() {
        new AppBadgeUtils(BaseApplication.getInstance()).getAppBadgeCountFromServer();
    }

}
