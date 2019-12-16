package com.inspur.emmcloud.application.serviceimpl;


import android.content.Context;

import com.inspur.emmcloud.application.bean.App;
import com.inspur.emmcloud.application.bean.AppGroupBean;
import com.inspur.emmcloud.application.ui.MyAppFragment;
import com.inspur.emmcloud.application.util.MyAppCacheUtils;
import com.inspur.emmcloud.basemodule.application.BaseApplication;
import com.inspur.emmcloud.basemodule.config.Constant;
import com.inspur.emmcloud.basemodule.util.PreferencesByUserAndTanentUtils;
import com.inspur.emmcloud.componentservice.appcenter.ApplicationService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by: yufuchang
 * Date: 2019/11/27
 */
public class AppCenterServiceImpl implements ApplicationService {

    @Override
    public Class getMyAppFragment() {
        return MyAppFragment.class;
    }

    @Override
    public boolean isAppExist(Context context, String scheme) {
        List<AppGroupBean> appGroupList = MyAppCacheUtils.getMyAppList(context);
        for (int i = 0; i < appGroupList.size(); i++) {
            AppGroupBean appGroupBean = appGroupList.get(i);
            List<App> appList = appGroupBean.getAppItemList();
            for (int j = 0; j < appList.size(); j++) {
                if (appList.get(j).getUri().equals(scheme)) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public String getVolumeIconUrl(Context context, String scheme) {
        List<AppGroupBean> appGroupList = MyAppCacheUtils.getMyAppList(context);
        for (int i = 0; i < appGroupList.size(); i++) {
            AppGroupBean appGroupBean = appGroupList.get(i);
            List<App> appList = appGroupBean.getAppItemList();
            for (int j = 0; j < appList.size(); j++) {
                if (appList.get(j).getUri().equals(scheme)) {
                    return appList.get(j).getAppIcon();
                }
            }
        }
        return "";
    }

    @Override
    public boolean clearMyAppList(Context context) {
        return PreferencesByUserAndTanentUtils.putString(context, Constant.APP_MYAPP_LIST_FROM_NET, "");
    }

    @Override
    public int getFilterAppStoreBadgeNum(Map<String, Integer> appBadgeMap) {
        Map<String, Integer> appBadgeMapSum = new HashMap<>();
        appBadgeMapSum.putAll(appBadgeMap);
        int appStoreBadgeNum = 0;
        List<AppGroupBean> appGroupBeanList = MyAppCacheUtils.getMyAppList(BaseApplication.getInstance());
        for (AppGroupBean appGroupBean : appGroupBeanList) {
            List<App> appList = appGroupBean.getAppItemList();
            for (App app : appList) {
                Integer num = appBadgeMapSum.get(app.getAppID());
                if (num != null) {
                    appStoreBadgeNum = appStoreBadgeNum + num;
                    appBadgeMapSum.remove(app.getAppID());
                }

            }
        }
        return appStoreBadgeNum;
    }
}
