package com.inspur.reactnative;

import android.content.Context;

import com.inspur.emmcloud.MyApplication;
import com.inspur.emmcloud.bean.appcenter.AndroidBundleBean;
import com.inspur.emmcloud.bean.mine.Language;
import com.inspur.emmcloud.config.Constant;
import com.inspur.emmcloud.util.common.PreferencesUtils;
import com.inspur.emmcloud.util.common.StringUtils;
import com.inspur.emmcloud.util.privates.AppUtils;

import static com.inspur.emmcloud.config.Constant.PUSH_FLAG;

/**
 * Created by yufuchang on 2017/7/11.
 */

public class ReactNativeInitInfoUtils {

    public final static String SYSTEM = "Android";

    /**
     * 获取用户个人信息
     *
     * @param context
     * @return
     */
    public static String getMyProfile(Context context) {
        String myInfo = PreferencesUtils.getString(context,
                "myInfo", "");
        return myInfo;
    }

    /**
     * 获取系统版本信息
     *
     * @param context
     * @return
     */
    public static String getSystemVersion(Context context) {
        return AppUtils.getSystemVersion();
    }

    /**
     * 获取当前应用语言
     *
     * @param context
     * @return
     */
    public static String getLocalLanguage(Context context) {
        String languageJson = PreferencesUtils.getString(
                context, MyApplication.getInstance().getTanent() + "appLanguageObj");
        if (!StringUtils.isBlank(languageJson)) {
            Language language = new Language(languageJson);
            return language.getIana();
        }
        return "UNKNOWN";
    }

    /**
     * 获取ReactNativeVersion
     *
     * @param reactAppFilePath
     * @return
     */
    public static String getReactNativeVersion(String reactAppFilePath) {
        StringBuilder describeVersionAndTime = ReactNativeFlow.getBundleDotJsonFromFile(reactAppFilePath);
        AndroidBundleBean androidBundleBean = new AndroidBundleBean(describeVersionAndTime.toString());
        return androidBundleBean.getVersion();
    }

    /**
     * 获取pushid
     *
     * @param context
     * @return
     */
    public static String getPushId(Context context) {
        String hwToken = PreferencesUtils.getString(context, Constant.HUAWEI_PUSH_TOKEN, "");
        return AppUtils.getIsHuaWei() ? (StringUtils.isBlank(hwToken) ? PreferencesUtils.getString(context, Constant.JPUSH_REGISTER_ID, "")
                : (hwToken + Constant.PUSH_HUAWEI_COM)) : PreferencesUtils.getString(context, Constant.JPUSH_REGISTER_ID, "");
    }

    /**
     * 获取推送类型
     *
     * @return
     */
    public static String getPushType(Context context) {
        return (AppUtils.getIsHuaWei() && canConnectHuawei(context)) ? Constant.HUAWEI_FLAG : "jiguang";
    }

    /**
     * 判断是否可以连接华为推了送
     *
     * @return
     */
    private static boolean canConnectHuawei(Context context) {
        String pushFlag = PreferencesUtils.getString(context, PUSH_FLAG, "");
        return (StringUtils.isBlank(pushFlag) || pushFlag.equals(Constant.HUAWEI_FLAG));
    }
}
