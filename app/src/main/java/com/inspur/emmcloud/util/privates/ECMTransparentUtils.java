package com.inspur.emmcloud.util.privates;

import android.content.Context;

import com.inspur.emmcloud.MyApplication;
import com.inspur.emmcloud.bean.chat.TransparentBean;
import com.inspur.emmcloud.push.WebSocketPush;

/**
 * Created by yufuchang on 2018/1/19.
 * 透传消息处理
 */

public class ECMTransparentUtils {

    /**
     * 来自极光的透传消息
     * @param context
     * @param transparent
     */
    public static void handleTransparentMsg(Context context, String transparent){
        if(transparent != null && ECMShortcutBadgeNumberManagerUtils.isHasBadge(transparent)){
            TransparentBean transparentBean = new TransparentBean(transparent);
            //透传改变桌面角标后不再发出Eventbus
//            EventBus.getDefault().post(transparentBean);
            if(!(WebSocketPush.getInstance().isSocketConnect() && MyApplication.getInstance().isV1xVersionChat())){
                new AppBadgeUtils(context).getAppBadgeCountFromServer();
            }
            ECMShortcutBadgeNumberManagerUtils.setDesktopBadgeNumber(context,transparentBean.getBadgeNumber());
        }
    }

    /**
     * 来自华为的透传消息
     * @param context
     * @param transparent
     */
    public static void handleTransparentMsg(Context context, byte[] transparent){
        if(transparent != null && ECMShortcutBadgeNumberManagerUtils.isHasBadge(transparent)){
            try {
                String transparentStr = new String(transparent,"UTF-8");
                handleTransparentMsg(context,transparentStr);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

}
