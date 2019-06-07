package com.inspur.emmcloud.basemodule.push;

import android.app.NotificationManager;
import android.content.Context;

import com.inspur.emmcloud.baselib.util.PreferencesUtils;
import com.inspur.emmcloud.baselib.util.StringUtils;
import com.inspur.emmcloud.basemodule.api.BaseModuleApiService;
import com.inspur.emmcloud.basemodule.application.BaseApplication;
import com.inspur.emmcloud.basemodule.config.Constant;
import com.inspur.emmcloud.basemodule.util.AppUtils;
import com.inspur.emmcloud.basemodule.util.NetUtils;
import com.xiaomi.mipush.sdk.MiPushClient;

import cn.jpush.android.api.JPushInterface;

import static android.content.Context.NOTIFICATION_SERVICE;

/**
 * Created by chenmch on 2019/3/22.
 */
public class PushManagerUtils {
    private static PushManagerUtils mInstance;
    public static PushManagerUtils getInstance() {
        if (mInstance == null) {
            synchronized (PushManagerUtils.class) {
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
            JPushInterface.init(BaseApplication.getInstance());
            if (JPushInterface.isPushStopped(BaseApplication.getInstance())) {
                JPushInterface.resumePush(BaseApplication.getInstance());
            }
            // 设置开启日志,发布时请关闭日志
            JPushInterface.setDebugMode(AppUtils.isApkDebugable(BaseApplication.getInstance()));
        }else {
            JPushInterface.stopPush(BaseApplication.getInstance());
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
            HuaWeiPushMangerUtils.getInstance(BaseApplication.getInstance()).connect();
        }else {
            HuaWeiPushMangerUtils.getInstance(BaseApplication.getInstance()).stopPush();
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
            MiPushClient.registerPush(BaseApplication.getInstance(), APP_ID, APP_KEY);
            MiPushClient.setAcceptTime(BaseApplication.getInstance(), 0, 0, 23, 59, null);
        }else {
            MiPushClient.pausePush(BaseApplication.getInstance(), null);
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
        NotificationManager notificationManager = (NotificationManager) BaseApplication.getInstance().getSystemService(NOTIFICATION_SERVICE);
        notificationManager.cancelAll();
    }


    /**
     * 向Emm注册pushId，如果进入应用不为空则已经注册过，直接上传
     * 如果为空则等待Jpush或者其他推送注册成功后上传token
     */
    public void registerPushId2Emm() {
        if (!BaseApplication.getInstance().isHaveLogin()) {
            return;
        }
        if (NetUtils.isNetworkConnected(BaseApplication.getInstance(), false)) {
            String pushId = PushManagerUtils.getInstance().getPushId(BaseApplication.getInstance());
            if (!pushId.equals("UNKNOWN")) {
                BaseModuleApiService appAPIService = new BaseModuleApiService(BaseApplication.getInstance());
                appAPIService.registerPushToken();
            }
        }
    }

    /**
     * 向Emm解除注册pushId
     */
    public void unregisterPushId2Emm() {
        if (NetUtils.isNetworkConnected(BaseApplication.getInstance(), false)) {
            BaseModuleApiService appAPIService = new BaseModuleApiService(BaseApplication.getInstance());
            appAPIService.unregisterPushToken();
        }
    }
}
