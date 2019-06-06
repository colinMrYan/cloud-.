package com.inspur.emmcloud.util.privates;

import android.content.Context;

import com.inspur.emmcloud.MyApplication;
import com.inspur.emmcloud.api.apiservice.AppAPIService;
import com.inspur.emmcloud.baselib.util.PreferencesUtils;
import com.inspur.emmcloud.baselib.util.StringUtils;
import com.inspur.emmcloud.config.Constant;
import com.xiaomi.mipush.sdk.MiPushClient;

import cn.jpush.android.api.JPushInterface;

/**
 * Created by chenmch on 2019/3/22.
 */
public class PushManagerUtils {
    private static PushManagerUtils mInstance;
    public static PushManagerUtils getInstance() {
        if (mInstance == null) {
            synchronized (OauthUtils.class) {
                if (mInstance == null) {
                    mInstance = new PushManagerUtils();
                }
            }
        }
        return mInstance;
    }

    /**
     * 通过厂商确定pushId
     *
     * @return
     */
    public String getPushId(Context context) {
        String pushId = "";
        if (AppUtils.getIsHuaWei()) {
            // 需要对华为单独推送的时候解开这里
            String hwtoken = PreferencesUtils.getString(context, Constant.HUAWEI_PUSH_TOKEN, "");
            if (!StringUtils.isBlank(hwtoken)) {
                pushId = hwtoken + Constant.PUSH_HUAWEI_COM;
            }
        } else if (AppUtils.getIsXiaoMi()) {
            String registerId =  PreferencesUtils.getString(context, Constant.MIPUSH_REGISTER_ID, "");
            if (!StringUtils.isBlank(registerId)){
                pushId = registerId + Constant.PUSH_XIAOMI_COM;
            }
        } else {
            pushId = PreferencesUtils.getString(context, Constant.JPUSH_REGISTER_ID, "");
        }
        if (StringUtils.isBlank(pushId)) {
            pushId = "UNKNOWN";
        }
        return pushId;
    }

    /**
     * 获取pushProvider
     *
     * @param context
     * @return
     */
    public String getPushProvider(Context context) {
        // 华为 com.hicloud.push
        // 极光 cn.jpush
        // 小米 com.xiaomi.xmpush
        // 魅族 com.meizu.api - push
        String pushProvider = "";
        String pushFlag = AppUtils.GetChangShang().toLowerCase();
        switch (pushFlag) {
            case Constant.HUAWEI_FLAG:
                pushProvider = "com.hicloud.push";
                break;
            case Constant.XIAOMI_FLAG:
                pushProvider = "com.xiaomi.xmpush";
                break;
            case Constant.MEIZU_FLAG:
                pushProvider = "com.meizu.api-push";
                break;
            default:
                pushProvider = "cn.jpush";
                break;
        }
        return pushProvider;
    }


    /**
     * 设置极光推送状态
     * true表示打开极光
     * false表示关闭极光
     * @param isOpen
     */
    private void setJpushStatus(boolean isOpen) {
        if (isOpen){
            // 初始化 JPush
            JPushInterface.init(MyApplication.getInstance());
            if (JPushInterface.isPushStopped(MyApplication.getInstance())) {
                JPushInterface.resumePush(MyApplication.getInstance());
            }
            // 设置开启日志,发布时请关闭日志
            JPushInterface.setDebugMode(AppUtils.isApkDebugable(MyApplication.getInstance()));
        }else {
            JPushInterface.stopPush(MyApplication.getInstance());
        }
    }

    /**
     * 设置华为推送状态
     * true表示打开
     * false表示关闭
     * @param isOpen
     */
    private void setHuaWeiPushStatus(boolean isOpen){
        if (isOpen){
            HuaWeiPushMangerUtils.getInstance(MyApplication.getInstance()).connect();
        }else {
            HuaWeiPushMangerUtils.getInstance(MyApplication.getInstance()).stopPush();
        }
    }

    /**
     * 设置小米推送状态
     * true表示打开
     * false表示关闭
     * 因为小米需要在PushReceiver中获取到权限时才能连接所以这里需要用public
     * @param isOpen
     */
    public void setMiPushStatus(boolean isOpen){
        String APP_ID = "2882303761517539689";
        String APP_KEY = "5381753921689";
        if (isOpen){
            MiPushClient.registerPush(MyApplication.getInstance(), APP_ID, APP_KEY);
            MiPushClient.setAcceptTime(MyApplication.getInstance(), 0, 0, 23, 59, null);
        }else {
            MiPushClient.pausePush(MyApplication.getInstance(), null);
        }
    }

    /**
     * 初始化推送，以后如需定制小米等厂家的推送服务可从这里定制
     * 目前使用的位置有ActionReceiver，IndexActivity 截止到181030
     */
    public void startPush() {
        if (AppUtils.getIsHuaWei()) {
            setHuaWeiPushStatus(true);
        } else if (AppUtils.getIsXiaoMi()) {
            setMiPushStatus(true);
        }else {
            setJpushStatus(true);
        }
    }

    /**
     * 关闭推送
     */
    public void stopPush() {
        if (AppUtils.getIsHuaWei()) {
            setHuaWeiPushStatus(false);
        } else if (AppUtils.getIsXiaoMi()) {
            setMiPushStatus(false);
        }else {
            setJpushStatus(false);
        }
        //清除日历提醒极光推送本地通知
        ScheduleAlertUtils.cancelAllCalEventNotification(MyApplication.getInstance());
    }


    /**
     * 向Emm注册pushId，如果进入应用不为空则已经注册过，直接上传
     * 如果为空则等待Jpush或者其他推送注册成功后上传token
     */
    public void registerPushId2Emm() {
        if (!MyApplication.getInstance().isHaveLogin()) {
            return;
        }
        if (NetUtils.isNetworkConnected(MyApplication.getInstance(), false)) {
            String pushId = PushManagerUtils.getInstance().getPushId(MyApplication.getInstance());
            if (!pushId.equals("UNKNOWN")) {
                AppAPIService appAPIService = new AppAPIService(MyApplication.getInstance());
                appAPIService.registerPushToken();
            }
        }
    }

    /**
     * 向Emm解除注册pushId
     */
    public void unregisterPushId2Emm() {
        if (NetUtils.isNetworkConnected(MyApplication.getInstance(), false)) {
            AppAPIService appAPIService = new AppAPIService(MyApplication.getInstance());
            appAPIService.unregisterPushToken();
        }
    }
}
