package com.inspur.emmcloud.componentservice.appcenter;

import android.app.Activity;
import android.os.Bundle;

import com.inspur.emmcloud.componentservice.CoreService;

/**
 * Created by chenmch on 2019/5/31.
 */

public interface AppcenterService extends CoreService {
    void startSyncCommonAppService();

    void startReactNativeApp(Activity activity, Bundle bundle);
}
