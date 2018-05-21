package com.inspur.emmcloud.util.privates;

import android.content.Context;

import com.inspur.emmcloud.api.APIInterfaceInstance;
import com.inspur.emmcloud.api.apiservice.ChatAPIService;
import com.inspur.emmcloud.bean.chat.GetUploadPushInfoResult;
import com.inspur.emmcloud.config.Constant;
import com.inspur.emmcloud.util.common.NetUtils;
import com.inspur.emmcloud.util.common.PreferencesUtils;
import com.inspur.emmcloud.util.common.StringUtils;

/**
 * 推送信息
 */

public class PushInfoUtils {
    private Context context;
    private OnGetChatClientIdListener callBack;

    public PushInfoUtils(Context context) {
        this.context = context;
    }

    public PushInfoUtils(Context context, OnGetChatClientIdListener callBack) {
        this.context = context;
        this.callBack = callBack;
    }


    private void upload() {
        String pushTracer = AppUtils.getPushId(context);
        if (NetUtils.isNetworkConnected(context, false) && !StringUtils.isBlank(pushTracer)) {
            ChatAPIService apiService = new ChatAPIService(context);
            apiService.setAPIInterface(new WebService());
            String deviceId = AppUtils.getMyUUID(context);
            String deviceName = AppUtils.getDeviceName(context);
            String pushProvider = getPushProvider();
            apiService.uploadPushInfo(deviceId, deviceName, pushProvider, pushTracer);
        }else {
            callbackClientIdFail();
        }
    }

    public void getChatClientId(boolean isForceNew) {
        String chatClientId = PreferencesByUserAndTanentUtils.getString(context, Constant.PREF_CHAT_CLIENTID, "");
        if (isForceNew || StringUtils.isBlank(chatClientId)) {
            upload();
        } else {
            callbackClientIdSuccess(chatClientId);
        }

    }

    private void callbackClientIdSuccess(String chatClientId) {
        if (callBack != null) {
            callBack.getChatClientIdSuccess(chatClientId);
        }
    }

    private void callbackClientIdFail() {
        if (callBack != null) {
            String chatClientId = PreferencesByUserAndTanentUtils.getString(context, Constant.PREF_CHAT_CLIENTID, "");
            if (!StringUtils.isBlank(chatClientId)) {
                callBack.getChatClientIdSuccess(chatClientId);
            } else {
                callBack.getChatClientIdFail();
            }
        }
    }

    /**
     * 获取pushProvider
     *
     * @param context
     * @return
     */
    public String getPushProvider() {
        // 华为  com.hicloud.push
        // 极光  cn.jpush
        // 小米  com.xiaomi.xmpush
        // 魅族  com.meizu.api - push
        String pushProvider = "";
        String pushFlag = PreferencesUtils.getString(context, "pushFlag", "");
        switch (pushFlag) {
            case "huawei":
                pushProvider = "com.hicloud.push";
                break;
            case "xiaomi":
                pushProvider = "com.xiaomi.xmpush";
                break;
            case "meizu":
                pushProvider = "com.meizu.api-push";
                break;
            default:
                pushProvider = "cn.jpush";
                break;
        }
        return pushProvider;
    }

    public interface OnGetChatClientIdListener {
        void getChatClientIdSuccess(String chatClientId);

        void getChatClientIdFail();
    }

    private class WebService extends APIInterfaceInstance {
        @Override
        public void returnUploadPushInfoResultSuccess(GetUploadPushInfoResult getUploadPushInfoResult) {
            String chatClientId = getUploadPushInfoResult.getChatClientId();
            PreferencesByUserAndTanentUtils.putString(context, Constant.PREF_CHAT_CLIENTID, chatClientId);
            callbackClientIdSuccess(chatClientId);
        }

        @Override
        public void returnUploadPushInfoResultFail(String error, int errorCode) {
            callbackClientIdFail();
        }
    }
}
