package com.inspur.emmcloud.application.serviceimpl;


import android.app.Activity;
import android.content.Context;
import android.net.Uri;

import com.inspur.emmcloud.application.api.ApplicationAPIService;
import com.inspur.emmcloud.application.api.ApplicationApiInterfaceImpl;
import com.inspur.emmcloud.application.bean.App;
import com.inspur.emmcloud.application.bean.AppCommonlyUse;
import com.inspur.emmcloud.application.bean.AppGroupBean;
import com.inspur.emmcloud.application.ui.MyAppFragment;
import com.inspur.emmcloud.application.util.AppCacheUtils;
import com.inspur.emmcloud.application.util.AppId2AppAndOpenAppUtils;
import com.inspur.emmcloud.application.util.MyAppCacheUtils;
import com.inspur.emmcloud.application.util.MyAppWidgetUtils;
import com.inspur.emmcloud.application.util.WebAppUtils;
import com.inspur.emmcloud.baselib.router.Router;
import com.inspur.emmcloud.baselib.util.JSONUtils;
import com.inspur.emmcloud.basemodule.application.BaseApplication;
import com.inspur.emmcloud.basemodule.config.Constant;
import com.inspur.emmcloud.basemodule.util.NetUtils;
import com.inspur.emmcloud.basemodule.util.PreferencesByUserAndTanentUtils;
import com.inspur.emmcloud.componentservice.appcenter.ApplicationService;
import com.inspur.emmcloud.componentservice.communication.OnFinishActivityListener;
import com.inspur.emmcloud.componentservice.communication.OnGetWebAppRealUrlListener;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by: yufuchang
 * Date: 2019/11/27
 */
public class ApplicationServiceImpl implements ApplicationService {

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

    @Override
    public void getAppInfoById(Activity activity, Uri uri, OnFinishActivityListener listener) {
        AppId2AppAndOpenAppUtils appId2AppAndOpenAppUtils = new AppId2AppAndOpenAppUtils(activity);
        appId2AppAndOpenAppUtils.setOnFinishActivityListener(listener);
        appId2AppAndOpenAppUtils.getAppInfoById(uri);
    }

    @Override
    public int getAppCommonlyUseSize() {
        return AppCacheUtils.getCommonlyUseList(BaseApplication.getInstance()).size();
    }

    @Override
    public void saveAppCommonlyUseList(Context context, String commonAppListJson) {
        List<AppCommonlyUse> commonAppList = JSONUtils.parseArray(commonAppListJson, AppCommonlyUse.class);
        AppCacheUtils.saveAppCommonlyUseList(context, commonAppList);
    }


    @Override
    public void syncCommonApp() {
        if (NetUtils.isNetworkConnected(BaseApplication.getInstance()) && BaseApplication.getInstance().isHaveLogin()) {
            List<AppCommonlyUse> commonAppList = AppCacheUtils.getUploadCommonlyUseAppList(BaseApplication.getInstance());
            if (commonAppList.size() > 0) {
                String commonAppListJson = JSONUtils.toJSONString(commonAppList);
                ApplicationAPIService apiService = new ApplicationAPIService(BaseApplication.getInstance());
                apiService.setAPIInterface(new WebService());
                apiService.syncCommonApp(commonAppListJson);
            }
        }
    }

    @Override
    public void getMyAppRecommendWidgets() {
        if (MyAppWidgetUtils.checkNeedUpdateMyAppWidget(BaseApplication.getInstance())) {
            MyAppWidgetUtils.getInstance(BaseApplication.getInstance()).getMyAppWidgetsFromNet();
        }
    }

    @Override
    public void getWebAppRealUrl(OnGetWebAppRealUrlListener listener, String url) {
        new WebAppUtils(BaseApplication.getInstance(), listener).getWebAppRealUrl(url);
    }

    @Override
    public void startSyncCommonAppService() {
        Router router = Router.getInstance();
        if (router.getService(ApplicationService.class) != null) {
            ApplicationService service = router.getService(ApplicationService.class);
            service.syncCommonApp();
        }

    }




    class WebService extends ApplicationApiInterfaceImpl {
        @Override
        public void returnSaveConfigSuccess() {
            //暂时没有处理
        }

        @Override
        public void returnSaveConfigFail() {
            //暂时没有处理
        }
    }
}
