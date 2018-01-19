package com.inspur.emmcloud.util.privates;

import android.content.Context;

import com.inspur.emmcloud.bean.chat.TransparentBean;
import com.inspur.emmcloud.util.common.JSONUtils;

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
        sendTabBadgeNumber(transparent);
        if(ECMShortcutBadgeNumberManagerUtils.isHasBadge(transparent)){
            ECMShortcutBadgeNumberManagerUtils.setDesktopBadgeNumber(context,JSONUtils.getInt(transparent,"badge",0));
        }
    }

    /**
     * 来自华为的透传消息
     * @param context
     * @param transparent
     */
    public static void handleTransparentMsg(Context context, byte[] transparent){
        if(transparent != null){
            try {
                sendTabBadgeNumber(new String(transparent,"UTF-8"));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if(ECMShortcutBadgeNumberManagerUtils.isHasBadge(transparent)){
            ECMShortcutBadgeNumberManagerUtils.setDesktopBadgeNumber(context,getDesktopBadgeNumber(transparent));
        }
    }

    /**
     * 发送底部tab的数字
     * @param transparent
     */
    private static void sendTabBadgeNumber(String transparent) {
        TransparentBean transparentBean = new TransparentBean(transparent);
        EventBus.getDefault().post(transparentBean);
    }

    /**
     * 获取桌面badge的数字
     * @param msg
     * @return
     */
    private static int getDesktopBadgeNumber(byte[] msg) {
        int badageNumber = 0;
        try {
            String message = new String(msg,"UTF-8");
            badageNumber = JSONUtils.getInt(message,"badge",0);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return badageNumber;
    }
}
