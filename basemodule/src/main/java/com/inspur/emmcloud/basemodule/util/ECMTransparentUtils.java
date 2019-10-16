package com.inspur.emmcloud.basemodule.util;

import android.content.Context;

import com.inspur.emmcloud.baselib.router.Router;
import com.inspur.emmcloud.baselib.util.JSONUtils;
import com.inspur.emmcloud.basemodule.application.BaseApplication;
import com.inspur.emmcloud.basemodule.config.Constant;
import com.inspur.emmcloud.componentservice.app.AppService;
import com.inspur.emmcloud.componentservice.communication.CommunicationService;

/**
 * Created by yufuchang on 2018/1/19.
 * 透传消息处理
 */

public class ECMTransparentUtils {

    /**
     * 来自极光的透传消息
     *
     * @param context
     * @param transparent
     */
    public static void handleTransparentMsg(Context context, String transparent) {
        if (BaseApplication.getInstance().isHaveLogin() && transparent != null && ECMShortcutBadgeNumberManagerUtils.isHasBadge(transparent)) {
            //透传改变桌面角标后不再发出Eventbus
//            EventBus.getDefault().post(transparentBean);
            //首先应用需要在前台，如果不在前台直接设置角标不做其他改动
            //如果应用在前台，socket连接或者v1环境中有一个条件（或两个）不为true，则发起http请求
            //如果符合条件则等服务器返回再改变桌面角标
            boolean isSocketConnect = false;
            Router router = Router.getInstance();
            if (router.getService(CommunicationService.class) != null) {
                CommunicationService service = router.getService(CommunicationService.class);
                isSocketConnect = service.isSocketConnect();
            }
            if (BaseApplication.getInstance().getIsActive() && !(isSocketConnect && WebServiceRouterManager.getInstance().isV1xVersionChat())) {
                if (router.getService(AppService.class) != null) {
                    AppService service = router.getService(AppService.class);
                    service.getAppBadgeCountFromServer();
                }
            } else {
                int badgeNumber = JSONUtils.getInt(transparent, "badge", 0);
                ECMShortcutBadgeNumberManagerUtils.setDesktopBadgeNumber(context, badgeNumber);
            }
            PVCollectModelCacheUtils.saveCollectModel(JSONUtils.getInt(transparent, "badge", 0) + "", Constant.PREF_DESKTOP_BADGE);
        }
    }

    /**
     * 来自华为的透传消息
     *
     * @param context
     * @param transparent
     */
    public static void handleTransparentMsg(Context context, byte[] transparent) {
        if (transparent != null && ECMShortcutBadgeNumberManagerUtils.isHasBadge(transparent)) {
            try {
                String transparentStr = new String(transparent, "UTF-8");
                handleTransparentMsg(context, transparentStr);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

}
