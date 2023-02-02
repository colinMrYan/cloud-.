package com.inspur.emmcloud.web.plugin.scaner;

import android.os.Build;

import com.inspur.emmcloud.baselib.util.JSONUtils;
import com.inspur.emmcloud.baselib.util.LogUtils;
import com.inspur.emmcloud.web.plugin.ImpPlugin;

import org.json.JSONObject;

public class PDAService extends ImpPlugin {

    private final static String TAG = "PDAService";
    public IPDA mPDA;
    private String successCb, failCb;

    @Override
    public void execute(String action, JSONObject paramsObject) {
        successCb = JSONUtils.getString(paramsObject, "success", "");
        failCb = JSONUtils.getString(paramsObject, "fail", "");
        switch (action) {
            case "init":
                initScaner();
                break;
            case "registerScanHeaderBroadcastReceiver":
                registerScanHeaderReceiver();
                break;
            case "unregisterScanHeaderBroadcastReceiver":
                unregisterScanHeaderReceiver();
                break;
            default:
                showCallIMPMethodErrorDlg();
                break;
        }

    }

    private void initScaner() {
        if ("UBX".equals(Build.MANUFACTURER)) {
            mPDA = new UBXPDA(getActivity(), this);
        } else {
            mPDA = new Mobile(this);
        }
        mPDA.init();
    }

    private void registerScanHeaderReceiver() {
        if (mPDA != null) {
            mPDA.registerScanHeaderReceiver();
        } else {
            callJsError("pda not init");
        }
    }

    private void unregisterScanHeaderReceiver() {
        if (mPDA != null) {
            mPDA.unregisterScanHeaderReceiver();
        } else {
            callJsError("pda not init");
        }
    }

    @Override
    public String executeAndReturn(String action, JSONObject paramsObject) {
        showCallIMPMethodErrorDlg();
        return "";
    }


    void callJsResult(String result) {
        LogUtils.debug(TAG, "callJsResult" + result);
        jsCallback(successCb, result);
    }

    void callJsError(String error) {
        LogUtils.debug(TAG, "callJsError" + error);
        jsCallback(failCb, error);
    }

    @Override
    public void onDestroy() {
        if (mPDA != null) {
            mPDA.destroy();
        }
    }
}
