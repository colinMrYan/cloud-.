package com.inspur.emmcloud.servcieimpl;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;

import com.inspur.emmcloud.baselib.util.IntentUtils;
import com.inspur.emmcloud.basemodule.application.BaseApplication;
import com.inspur.emmcloud.basemodule.config.Constant;
import com.inspur.emmcloud.basemodule.util.AppBadgeUtils;
import com.inspur.emmcloud.basemodule.util.TabAndAppExistUtils;
import com.inspur.emmcloud.componentservice.app.AppService;
import com.inspur.emmcloud.componentservice.app.CommonCallBack;
import com.inspur.emmcloud.ui.appcenter.ReactNativeAppActivity;
import com.inspur.emmcloud.util.privates.ProfileUtils;
import com.inspur.emmcloud.util.privates.UpgradeUtils;
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

    @Override
    public void startReactNativeApp(Activity activity, Bundle bundle) {
        IntentUtils.startActivity(activity, ReactNativeAppActivity.class, bundle);
    }

    @Override
    public void checkAppUpdate(boolean isManualCheck, Handler handler) {
        UpgradeUtils upgradeUtils = new UpgradeUtils(BaseApplication.getInstance(),
                handler, true);
        upgradeUtils.checkUpdate(true);
    }

    @Override
    public void initProfile(Activity activity, boolean isShowLoadingDlg, CommonCallBack commonCallBack) {
        new ProfileUtils(activity, commonCallBack).initProfile(isShowLoadingDlg);
    }
}
