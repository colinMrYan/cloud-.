package com.inspur.emmcloud.componentservice.appcenter;

import android.content.Context;

import com.inspur.emmcloud.componentservice.CoreService;

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
}
