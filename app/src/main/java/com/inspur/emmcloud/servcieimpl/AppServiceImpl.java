package com.inspur.emmcloud.servcieimpl;

import com.inspur.emmcloud.basemodule.application.BaseApplication;
import com.inspur.emmcloud.basemodule.config.Constant;
import com.inspur.emmcloud.basemodule.util.AppBadgeUtils;
import com.inspur.emmcloud.componentservice.app.AppService;
import com.inspur.emmcloud.util.privates.TabAndAppExistUtils;
import com.inspur.emmcloud.util.privates.cache.AppConfigCacheUtils;

/**
 * Created by chenmch on 2019/6/6.
 */

public class AppServiceImpl implements AppService {
    @Override
    public void getAppBadgeCountFromServer() {
        new AppBadgeUtils(BaseApplication.getInstance()).getAppBadgeCountFromServer();
    }

    @Override
    public String getAppConfig(String configId, String defaultValue) {
        return AppConfigCacheUtils.getAppConfigValue(BaseApplication.getInstance(), configId, defaultValue);
    }

    @Override
    public boolean isTabExist(String tabId) {
        return TabAndAppExistUtils.isTabExist(BaseApplication.getInstance(), Constant.APP_TAB_BAR_COMMUNACATE);
    }
}
