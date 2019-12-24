package com.inspur.emmcloud.componentservice.app;


import android.app.Activity;
import android.os.Bundle;

import com.inspur.emmcloud.componentservice.CoreService;

/**
 * Created by chenmch on 2019/6/6.
 */

public interface AppService extends CoreService {
    void getAppBadgeCountFromServer();

    String getAppConfig(String configId, String defaultValue);

    boolean isTabExist(String tabId);

    void startReactNativeApp(Activity activity, Bundle bundle);
}
