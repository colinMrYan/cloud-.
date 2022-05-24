package com.inspur.emmcloud.application.util;

import android.content.Context;

import com.inspur.emmcloud.application.R;
import com.inspur.emmcloud.application.bean.App;
import com.inspur.emmcloud.application.bean.AppCommonlyUse;
import com.inspur.emmcloud.application.bean.AppGroupBean;
import com.inspur.emmcloud.baselib.util.JSONUtils;
import com.inspur.emmcloud.baselib.util.PreferencesUtils;
import com.inspur.emmcloud.baselib.util.StringUtils;
import com.inspur.emmcloud.basemodule.application.BaseApplication;
import com.inspur.emmcloud.basemodule.config.Constant;
import com.inspur.emmcloud.basemodule.util.AppConfigCacheUtils;
import com.inspur.emmcloud.basemodule.util.PreferencesByUserAndTanentUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Created by yufuchang on 2017/8/15.
 */

public class MyAppCacheUtils {

    /**
     * 获取应用列表
     *
     * @param context
     * @return
     */
    public static List<AppGroupBean> getMyAppList(Context context) {
        List<AppGroupBean> appGroupList = getMyAppListFromNet(context);
        if (MyAppCacheUtils.getNeedCommonlyUseApp()) {
            AppGroupBean appGroupBean = getCommonlyUserAppGroup();
            if (appGroupBean != null && appGroupList != null) {
                appGroupList.add(0, appGroupBean);
            }
        }
        if (appGroupList == null) {
            appGroupList = new ArrayList<>();
        }
        return appGroupList;
    }

    /**
     * 获取常用应用组
     *
     * @return
     */
    public static AppGroupBean getCommonlyUserAppGroup() {
        AppGroupBean appGroupBean = new AppGroupBean();
        List<AppGroupBean> appGroupList = MyAppCacheUtils.getMyAppListFromNet(BaseApplication.getInstance());
        List<AppCommonlyUse> appCommonlyUseList =
                AppCacheUtils.getCommonlyUseList(BaseApplication.getInstance());//这里换成获取所有
        if (appCommonlyUseList.size() > 0) {
            appGroupBean.setCategoryID("commonly");
            appGroupBean.setCategoryName(BaseApplication.getInstance().getString(R.string.commoly_use_app));
            List<App> myCommonlyUseAppList = new ArrayList<App>();
            for (int i = 0; i < appGroupList.size(); i++) {
                List<App> appItemList = appGroupList.get(i).getAppItemList();
                int appGroupSize = appItemList.size();
                for (int j = 0; j < appGroupSize; j++) {
                    App app = appItemList.get(j);
                    AppCommonlyUse appCommonlyUse = new AppCommonlyUse();
                    appCommonlyUse.setAppID(app.getAppID());
                    int index = appCommonlyUseList.indexOf(appCommonlyUse);
                    int allreadHas = myCommonlyUseAppList.indexOf(app);
                    if (index != -1 && allreadHas == -1) {
                        AppCommonlyUse appCommonlyUseTemp = appCommonlyUseList.get(index);
                        app.setWeight(appCommonlyUseTemp.getWeight());
                        myCommonlyUseAppList.add(app);
                    }
                }
            }
            //先排序再取前四个
            Collections.sort(myCommonlyUseAppList, new SortCommonlyUseAppClass());
            if (myCommonlyUseAppList.size() > 8) {
                myCommonlyUseAppList = myCommonlyUseAppList.subList(0, 8);
            }
            //取完前四个再排序一次
            Collections.sort(myCommonlyUseAppList, new SortCommonlyUseAppClass());
            //需要调试常用应用权重时解开
//            for (int i = 0; i < myCommonlyUseAppList.size(); i++) {
//                LogUtils.YfcDebug("app名称：" + myCommonlyUseAppList.get(i).getAppName() + "常用应用的权重" + myCommonlyUseAppList.get(i).getWeight());
//            }
            if (myCommonlyUseAppList.size() > 0) {
                appGroupBean.setAppItemList(myCommonlyUseAppList);
            }
        }
        return appGroupBean.getAppItemList().size() > 0 ? appGroupBean : null;
    }

    /**
     * 删除缓存里的App
     *
     * @param context
     * @param deleteApp
     */
    public static void deleteAppInCache(Context context, App deleteApp) {
        List<AppGroupBean> appGroupList = MyAppCacheUtils.getMyAppListFromNet(BaseApplication.getInstance());
        for (AppGroupBean appGroupBean : appGroupList) {
            List<App> appList = appGroupBean.getAppItemList();
            if (appList.contains(deleteApp)) {
                appList.remove(deleteApp);
                break;
            }
        }
        saveMyAppListFromNet(context, appGroupList);
    }

    /**
     * 存储是否需要显示常用app
     *
     * @param isNeedCommonlyUseApp
     */
    public static void saveNeedCommonlyUseApp(boolean isNeedCommonlyUseApp) {
        String userId = BaseApplication.getInstance().getUid();
        PreferencesUtils.putBoolean(BaseApplication.getInstance(), BaseApplication.getInstance().getTanent()
                        + userId + "needCommonlyUseApp",
                isNeedCommonlyUseApp);
    }

    /**
     * 获取是否需要显示常用app
     * isShowCommAppFromSer  服务端拉取是否显示常用应用  true：显示本地最近常用操作按钮 然后根据 isShowCommAppFromNative 显示最近常用应用UI false：隐藏本地操作按钮
     * isShowCommAppFromNative 最近常用操作按钮本地存储状态
     *
     * @return
     */
    public static boolean getNeedCommonlyUseApp() {
        String userId = BaseApplication.getInstance().getUid();
        boolean isCommonUseAppShowResult = true;
        boolean isContactCommState = PreferencesUtils.isKeyExist(BaseApplication.getInstance(), BaseApplication.getInstance().getTanent() + userId + "needCommonlyUseApp");
        if (!isContactCommState) {
            isCommonUseAppShowResult = AppConfigCacheUtils.getAppConfigValue(BaseApplication.getInstance(), "EnableCommonFunction", "true").equals("true");
        } else {
            isCommonUseAppShowResult = PreferencesUtils.getBoolean(BaseApplication.getInstance(), BaseApplication.getInstance().getTanent()
                    + userId + "needCommonlyUseApp", true)
                    &&  AppConfigCacheUtils.getAppConfigValue(BaseApplication.getInstance(), "EnableCommonFunction", "true").equals("true");
        }
        return isCommonUseAppShowResult;
    }

    /**
     * 保存从网络获取的应用列表数据
     *
     * @param context
     * @param appGroupList
     */
    public static void saveMyAppListFromNet(Context context, List<AppGroupBean> appGroupList) {
        String appListJson = JSONUtils.toJSONString(appGroupList);
        if (!appListJson.equals("null") && !StringUtils.isBlank(appListJson)) {
            PreferencesByUserAndTanentUtils.putString(context, Constant.APP_MYAPP_LIST_FROM_NET, appListJson);
        }
    }

    /**
     * 获取网络返回的应用列表
     *
     * @param context
     * @return
     */
    public static List<AppGroupBean> getMyAppListFromNet(Context context) {
        String appListJson = PreferencesByUserAndTanentUtils.getString(context, Constant.APP_MYAPP_LIST_FROM_NET, "");
        List<AppGroupBean> appGroupList = null;
        if (!StringUtils.isBlank(appListJson)) {
            appGroupList = JSONUtils.parseArray(appListJson, AppGroupBean.class);
        }
        if (appGroupList == null) {
            appGroupList = new ArrayList<>();
        }
        return appGroupList;
    }

    /**
     * 清除应用缓存
     *
     * @param context
     * @return
     */
    public static boolean clearMyAppList(Context context) {
        return PreferencesByUserAndTanentUtils.putString(context, Constant.APP_MYAPP_LIST_FROM_NET, "");
    }

    /**
     * 应用排序接口，比较权重，用于展示APP
     */
    public static class SortCommonlyUseAppClass implements Comparator<App> {
        @Override
        public int compare(App arg0, App arg1) {
            double appSortA = arg0.getWeight();
            double appSortB = arg1.getWeight();
            if (appSortA > appSortB) {
                return -1;
            } else if (appSortA < appSortB) {
                return 1;
            } else {
                return 0;
            }
        }
    }

}
