package com.inspur.reactnative;

import android.content.Context;

import com.inspur.emmcloud.bean.AndroidBundleBean;
import com.inspur.emmcloud.bean.Language;
import com.inspur.emmcloud.util.AppUtils;
import com.inspur.emmcloud.util.PreferencesUtils;
import com.inspur.emmcloud.util.StringUtils;
import com.inspur.emmcloud.util.UriUtils;

/**
 * Created by yufuchang on 2017/7/11.
 */

public class ReactNativeInitInfoUtils {

    public final static String SYSTEM = "Android";
    /**
     * 获取用户个人信息
     * @param context
     * @return
     */
    public static String getMyProfile(Context context){
        String myInfo = PreferencesUtils.getString(context,
                "myInfo", "");
        return myInfo;
    }

    /**
     * 获取系统版本信息
     * @param context
     * @return
     */
    public static String getSystemVersion(Context context){
        return AppUtils.getSystemVersion();
    }

    /**
     * 获取当前应用语言
     * @param context
     * @return
     */
    public static String getLocalLanguage(Context context){
        String languageJson = PreferencesUtils.getString(
                context, UriUtils.tanent + "appLanguageObj");
        if (!StringUtils.isBlank(languageJson)) {
            Language language = new Language(languageJson);
            return language.getIana();
        }
        return "UNKNOWN";
    }

    /**
     * 获取ReactNativeVersion
     * @param reactAppFilePath
     * @return
     */
    public static String getReactNativeVersion(String reactAppFilePath){
        StringBuilder describeVersionAndTime = ReactNativeFlow.getBundleDotJsonFromFile(reactAppFilePath);
        AndroidBundleBean androidBundleBean = new AndroidBundleBean(describeVersionAndTime.toString());
        return androidBundleBean.getVersion();
    }

    /**
     * 获取pushid
     * @param context
     * @return
     */
    public static String getPushId(Context context){
        String hwToken = PreferencesUtils.getString(context,"huawei_push_token","");
        return AppUtils.getIsHuaWei()?(StringUtils.isBlank(hwToken)?PreferencesUtils.getString(context, "JpushRegId", "")
                :(hwToken + "@push.huawei.com")):PreferencesUtils.getString(context, "JpushRegId", "");
    }

    /**
     * 获取推送类型
     * @return
     */
    public static String getPushType(Context context){
        return (AppUtils.getIsHuaWei() && canConnectHuawei(context))?"huawei":"jiguang";
    }

    /**
     * 判断是否可以连接华为推了送
     *
     * @return
     */
    private static boolean canConnectHuawei(Context context) {
        String pushFlag = PreferencesUtils.getString(context, "pushFlag", "");
        return (StringUtils.isBlank(pushFlag) || pushFlag.equals("huawei"));
    }
}
