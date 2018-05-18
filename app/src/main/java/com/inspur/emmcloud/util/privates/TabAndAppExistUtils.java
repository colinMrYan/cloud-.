package com.inspur.emmcloud.util.privates;

import android.content.Context;

import com.inspur.emmcloud.bean.appcenter.App;
import com.inspur.emmcloud.bean.appcenter.AppGroupBean;
import com.inspur.emmcloud.bean.system.AppTabAutoBean;
import com.inspur.emmcloud.bean.system.AppTabDataBean;
import com.inspur.emmcloud.util.privates.cache.MyAppCacheUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by yufuchang on 2018/5/14.
 */

public class TabAndAppExistUtils {

    /**
     * 判断是否存在某个tab，通过tabId判断
     * @param context
     * @param tabId
     * @return
     */
    public static boolean isTabExist(Context context, String tabId){
        String appTabs = PreferencesByUserAndTanentUtils.getString(context, "app_tabbar_info_current", "");
        AppTabAutoBean appTabAutoBean = new AppTabAutoBean(appTabs);
        //发送到MessageFragment
        ArrayList<AppTabDataBean> appTabList = (ArrayList<AppTabDataBean>) appTabAutoBean.getPayload().getTabs();
        for (int i = 0; i < appTabList.size(); i++) {
            if(appTabList.get(i).getTabId().equals(tabId)){
                return true;
            }
        }
        return false;
    }

    /**
     * 判断某个app是否存在，通过appId判断
     * @param context
     * @param scheme
     * @return
     */
    public static boolean isAppExist(Context context,String scheme){
        List<AppGroupBean> appGroupList = MyAppCacheUtils.getMyAppList(context);
        for (int i = 0; i < appGroupList.size(); i++) {
            AppGroupBean appGroupBean = appGroupList.get(i);
            List<App> appList = appGroupBean.getAppItemList();
            for (int j = 0; j < appList.size(); j++) {
                if(appList.get(j).getUri().contains(scheme)){
                    return true;
                }
            }
        }
        return false;
    }

    /**
     *  获取网盘图标图标url
     * @param context
     * @param appId
     * @return
     */
    public static String getVolumeImgUrl(Context context,String appId){
        App app = new App();
        app.setAppID(appId);
        List<AppGroupBean> appGroupList = MyAppCacheUtils.getMyAppList(context);
        for (int i = 0; i < appGroupList.size(); i++) {
            AppGroupBean appGroupBean = appGroupList.get(i);
            int index = appGroupBean.getAppItemList().indexOf(app);
            if(index != -1){
                return appGroupBean.getAppItemList().get(index).getAppIcon();
            }
        }
        return "";
    }
}
