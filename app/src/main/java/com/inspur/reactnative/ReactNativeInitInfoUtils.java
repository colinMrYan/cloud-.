package com.inspur.reactnative;

import android.content.Context;

import com.inspur.emmcloud.MyApplication;
import com.inspur.emmcloud.bean.AndroidBundleBean;
import com.inspur.emmcloud.bean.Enterprise;
import com.inspur.emmcloud.util.AppUtils;
import com.inspur.emmcloud.util.PreferencesUtils;
import com.inspur.emmcloud.util.StringUtils;

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
//    public static String getSystemVersion(Context context){
//        return AppUtils.
//    }

    /**
     * 获取系统语言
     * @return
     */
    public static String getLocalLanguage(){
        return "";
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
     * 获取apptoken
     * @param context
     * @return
     */
    public static String getAppToken(Context context){
        return ((MyApplication)context.getApplicationContext()).getToken();
    }

    /**
     * 获取当前企业
     * @param context
     * @return
     */
    public static Enterprise getCurrentEnterprise(Context context){
        return  ((MyApplication)context.getApplicationContext()).getCurrentEnterprise();
    }

    /**
     * 获取pushid
     * @param context
     * @return
     */
    public static String getPushId(Context context){
        String pushid = "";
        if(AppUtils.getIsHuaWei()){
            //需要对华为单独推送的时候解开这里
            String hwtoken = PreferencesUtils.getString(context,"huawei_push_token","");
            if(!StringUtils.isBlank(hwtoken)){
                pushid = hwtoken + "@push.huawei.com";
            }
        }else{
            pushid = PreferencesUtils.getString(context, "JpushRegId", "");
        }
        return pushid;
    }

    /**
     * 获取推送类型
     * @return
     */
    public static String getPushType(){
        String pushType = "";
        if(AppUtils.getIsHuaWei()){
            pushType = "huawei";
        }else {
            pushType = "jiguang";
        }
        return pushType;
    }
}
