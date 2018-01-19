package com.inspur.emmcloud.util.privates;

import android.content.Context;

import com.inspur.emmcloud.bean.chat.TransparentBean;

import org.greenrobot.eventbus.EventBus;

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
            sendTabBadgeNumber(transparentBean);
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
                TransparentBean transparentBean = new TransparentBean(transparentStr);
                sendTabBadgeNumber(transparentBean);
                ECMShortcutBadgeNumberManagerUtils.setDesktopBadgeNumber(context,transparentBean.getBadgeNumber());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 发送底部tab的数字
     * @param transparentBean
     */
    private static void sendTabBadgeNumber(TransparentBean transparentBean) {
        EventBus.getDefault().post(transparentBean);
    }

}
