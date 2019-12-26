package com.inspur.emmcloud.util.privates;

import android.content.Context;

import com.inspur.emmcloud.baselib.router.Router;
import com.inspur.emmcloud.basemodule.config.Constant;
import com.inspur.emmcloud.basemodule.util.PreferencesByUserAndTanentUtils;
import com.inspur.emmcloud.bean.system.GetAppMainTabResult;
import com.inspur.emmcloud.bean.system.MainTabResult;
import com.inspur.emmcloud.componentservice.application.ApplicationService;

import java.util.ArrayList;

/**
 * Created by yufuchang on 2018/5/14.
 */

public class TabAndAppExistUtils {

    /**
     * 判断是否存在某个tab，通过tabId判断
     *
     * @param context
     * @param tabId
     * @return
     */
    public static boolean isTabExist(Context context, String tabId) {
        String appTabs = PreferencesByUserAndTanentUtils.getString(context, Constant.PREF_APP_TAB_BAR_INFO_CURRENT, "");
        GetAppMainTabResult getAppMainTabResult = new GetAppMainTabResult(appTabs);
        ArrayList<MainTabResult> mainTabResultList = getAppMainTabResult.getMainTabPayLoad().getMainTabResultList();
        for (int i = 0; i < mainTabResultList.size(); i++) {
            if (mainTabResultList.get(i).getUri().equals(tabId)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 判断某个app是否存在，通过appId判断
     *
     * @param context
     * @param scheme
     * @return
     */
    public static boolean isAppExist(Context context, String scheme) {
        Router router = Router.getInstance();
        if (router.getService(ApplicationService.class) != null) {
            ApplicationService service = router.getService(ApplicationService.class);
            return service.isAppExist(context, scheme);
        }
        return false;
    }

    /**
     * 获取网盘图标图标url
     *
     * @param context
     * @param scheme
     * @return
     */
    public static String getVolumeIconUrl(Context context, String scheme) {
        Router router = Router.getInstance();
        if (router.getService(ApplicationService.class) != null) {
            ApplicationService service = router.getService(ApplicationService.class);
            return service.getVolumeIconUrl(context, scheme);
        }
        return "";
    }
}
