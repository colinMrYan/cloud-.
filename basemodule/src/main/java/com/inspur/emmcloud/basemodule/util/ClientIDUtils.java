package com.inspur.emmcloud.basemodule.util;

import android.content.Context;

import com.inspur.emmcloud.baselib.util.StringUtils;
import com.inspur.emmcloud.basemodule.api.BaseModuleAPIInterfaceInstance;
import com.inspur.emmcloud.basemodule.api.BaseModuleApiService;
import com.inspur.emmcloud.basemodule.application.BaseApplication;
import com.inspur.emmcloud.basemodule.bean.GetUploadPushInfoResult;
import com.inspur.emmcloud.basemodule.config.Constant;
import com.inspur.emmcloud.basemodule.push.PushManagerUtils;

/**
 * 推送信息
 */

public class ClientIDUtils extends BaseModuleAPIInterfaceInstance {
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
        if (!BaseApplication.getInstance().isHaveLogin()) {
            return;
        }
        String pushTracer = PushManagerUtils.getInstance().getPushId(context);
        if (NetUtils.isNetworkConnected(context, false)) {
            BaseModuleApiService apiService = new BaseModuleApiService(context);
            apiService.setAPIInterface(this);
            String deviceId = AppUtils.getMyUUID(context);
            String deviceName = AppUtils.getDeviceName(context);
            String pushProvider = PushManagerUtils.getInstance().getPushProvider(context);
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

    public interface OnGetClientIdListener {
        void getClientIdSuccess(String clientId);

        void getClientIdFail();
    }
}
