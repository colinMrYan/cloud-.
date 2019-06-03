package com.inspur.emmcloud.util.privates;

import android.content.Context;

import com.inspur.emmcloud.MyApplication;
import com.inspur.emmcloud.api.APIInterfaceInstance;
import com.inspur.emmcloud.api.apiservice.ChatAPIService;
import com.inspur.emmcloud.baselib.util.StringUtils;
import com.inspur.emmcloud.bean.chat.GetUploadPushInfoResult;
import com.inspur.emmcloud.config.Constant;

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
        if (!MyApplication.getInstance().isHaveLogin()) {
            return;
        }
        String pushTracer = PushManagerUtils.getPushId(context);
        if (NetUtils.isNetworkConnected(context, false)) {
            ChatAPIService apiService = new ChatAPIService(context);
            apiService.setAPIInterface(new WebService());
            String deviceId = AppUtils.getMyUUID(context);
            String deviceName = AppUtils.getDeviceName(context);
            String pushProvider = PushManagerUtils.getPushProvider(context);
            apiService.uploadPushInfo(deviceId, deviceName, pushProvider, pushTracer);
        } else {
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
