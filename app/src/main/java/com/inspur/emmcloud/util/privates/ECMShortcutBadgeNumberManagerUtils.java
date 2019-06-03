package com.inspur.emmcloud.util.privates;

import android.content.Context;

import com.inspur.emmcloud.baselib.util.StringUtils;
import com.inspur.emmcloud.config.Constant;
import com.inspur.emmcloud.util.privates.cache.AppExceptionCacheUtils;

import org.json.JSONObject;

import me.leolin.shortcutbadger.ShortcutBadger;

/**
 * Created by yufuchang on 2017/12/5.
 */

public class ECMShortcutBadgeNumberManagerUtils {

//    /**
//     * 设置桌面角标
//     * @param context
//     */
//    public static void setDesktopBadgeNumber(Context context,int count,Intent intent) {
//        if(intent != null){
//            String miuiVersionString = getSystemProperty("ro.miui.ui.version.name");
//            int miuiVersionNum = (StringUtils.isBlank(miuiVersionString))? -1 : Integer.parseInt(miuiVersionString.substring(1));
//            if(miuiVersionNum >= 6){
//                setMIUIV6PlusBadge(context,count,intent);
//                return;
//            }
//        }
//        ShortcutBadger.applyCount(context,count);
//    }

    /**
     * 判断byte里是否含有badge
     *
     * @param msg
     * @return
     */
    public static boolean isHasBadge(byte[] msg) {
        if (msg == null) {
            return false;
        }
        try {
            String message = new String(msg, "UTF-8");
            JSONObject jsonObject = new JSONObject(message);
            return jsonObject.has("badge");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * 判断字符串是否含有badge
     *
     * @param msg
     * @return
     */
    public static boolean isHasBadge(String msg) {
        if (StringUtils.isBlank(msg)) {
            return false;
        }
        try {
            JSONObject jsonObject = new JSONObject(msg);
            return jsonObject.has("badge");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * 设置桌面角标
     *
     * @param context
     */
    public static void setDesktopBadgeNumber(Context context, int count) {
        if (!AppUtils.GetChangShang().toLowerCase().startsWith(Constant.XIAOMI_FLAG)) {
            try {
                ShortcutBadger.applyCount(context, count);
            } catch (Exception e) {
                AppExceptionCacheUtils.saveAppException(context, Constant.APP_EXCEPTION_LEVEL, "Desktop badge count", e.getMessage(), -1);
                e.printStackTrace();
            }
        }
    }
}
