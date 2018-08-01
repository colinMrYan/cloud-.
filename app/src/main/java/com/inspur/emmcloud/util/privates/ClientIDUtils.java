package com.inspur.emmcloud.util.privates;

import android.content.Context;

import com.inspur.emmcloud.MyApplication;
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

public class ClientIDUtils {
    private Context context;
    private OnGetClientIdListener callBack;

    public ClientIDUtils(Context context) {
        this.context = context;
    }

    public ClientIDUtils(Context context, OnGetClientIdListener callBack) {
        this.context = context;
        this.callBack = callBack;
    }


    public void upload() {
        if (!MyApplication.getInstance().isHaveLogin()){
            return;
        }
        String pushTracer = AppUtils.getPushId(context);
        if (NetUtils.isNetworkConnected(context, false)) {
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

    public void getClientId() {
        String clientId = PreferencesByUserAndTanentUtils.getString(context, Constant.PREF_CLIENTID, "");
        if (StringUtils.isBlank(clientId)) {
            upload();
        } else {
            callbackClientIdSuccess(clientId);
        }

    }

    private void callbackClientIdSuccess(String clientId) {
        if (callBack != null) {
            callBack.getClientIdSuccess(clientId);
        }
    }

    private void callbackClientIdFail() {
        if (callBack != null) {
            String clientId = PreferencesByUserAndTanentUtils.getString(context, Constant.PREF_CLIENTID, "");
            if (!StringUtils.isBlank(clientId)) {
                callBack.getClientIdSuccess(clientId);
            } else {
                callBack.getClientIdFail();
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

    public interface OnGetClientIdListener {
        void getClientIdSuccess(String clientId);

        void getClientIdFail();
    }

    private class WebService extends APIInterfaceInstance {
        @Override
        public void returnUploadPushInfoResultSuccess(GetUploadPushInfoResult getUploadPushInfoResult) {
            String clientId = getUploadPushInfoResult.getChatClientId();
            PreferencesByUserAndTanentUtils.putString(context, Constant.PREF_CLIENTID, clientId);
            callbackClientIdSuccess(clientId);
        }

        @Override
        public void returnUploadPushInfoResultFail(String error, int errorCode) {
            callbackClientIdFail();
        }
    }
}
