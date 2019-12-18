package com.inspur.emmcloud.componentservice.appcenter;

import android.app.Activity;
import android.content.Context;
import android.net.Uri;

import com.inspur.emmcloud.componentservice.CoreService;
import com.inspur.emmcloud.componentservice.communication.OnFinishActivityListener;
import com.inspur.emmcloud.componentservice.communication.OnGetWebAppRealUrlListener;

import java.util.Map;

/**
 * Created by: yufuchang
 * Date: 2019/12/6
 */
public interface ApplicationService extends CoreService {
    Class getMyAppFragment();

    boolean isAppExist(Context context, String scheme);

    String getVolumeIconUrl(Context context, String scheme);

    boolean clearMyAppList(Context context);

    int getFilterAppStoreBadgeNum(Map<String, Integer> appBadgeMap);

    void getAppInfoById(Activity activity, Uri uri, OnFinishActivityListener listener);

    int getAppCommonlyUseSize();

    void saveAppCommonlyUseList(Context context, String appListJson);

    void syncCommonApp();

    void getMyAppRecommendWidgets();

    void getWebAppRealUrl(OnGetWebAppRealUrlListener listener, String url);

}
