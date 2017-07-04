package com.inspur.emmcloud.util;

import android.app.Activity;
import android.content.Context;
import android.support.annotation.NonNull;
import android.text.TextUtils;

import com.huawei.hms.api.ConnectionResult;
import com.huawei.hms.api.HuaweiApiAvailability;
import com.huawei.hms.api.HuaweiApiClient;
import com.huawei.hms.support.api.client.PendingResult;
import com.huawei.hms.support.api.push.HuaweiPush;
import com.huawei.hms.support.api.push.TokenResult;

/**
 * Created by yufuchang on 2017/6/20.
 * 华为推送模块，单独模块封装
 */

public class HuaWeiPushMangerUtils implements HuaweiApiClient.ConnectionCallbacks, HuaweiApiClient.OnConnectionFailedListener,
        HuaweiApiAvailability.OnUpdateListener {
    private HuaweiApiClient client;
    private boolean mResolvingError = false;
    private static final int REQUEST_RESOLVE_ERROR = 1001;
    private Context contextLocal;

    public HuaWeiPushMangerUtils(Context context) {
        contextLocal = context;
//        HuaweiIdSignInOptions options = new HuaweiIdSignInOptions.Builder(HuaweiIdSignInOptions.DEFAULT_SIGN_IN)
//                .build();
//            client = new HuaweiApiClient.Builder(activity) //
//                    .addApi(HuaweiId.SIGN_IN_API, options) //
//                    .addScope(HuaweiId.HUAEWEIID_BASE_SCOPE) //
//                    .addConnectionCallbacks(this) //
//                    .addOnConnectionFailedListener(this) //
//                    .build();
        LogUtils.YfcDebug("创建client");
        client = new HuaweiApiClient.Builder(context)
                .addApi(HuaweiPush.PUSH_API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();
        client.connect();
    }


    @Override
    public void onUpdateFailed(@NonNull ConnectionResult connectionResult) {
        // TODO: 处理result.getErrorCode()
        mResolvingError = false;
    }

    @Override
    public void onConnected() {
        LogUtils.YfcDebug("华为推送连接成功");
        if(StringUtils.isBlank(PreferencesByUserAndTanentUtils.getString(contextLocal,""))){
            getToken();
        }
        setPassByMsg(true);
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        LogUtils.YfcDebug("华为推送连接失败" + connectionResult.getErrorCode());
        if (mResolvingError) {
            return;
        }
        int errorCode = connectionResult.getErrorCode();
        HuaweiApiAvailability availability = HuaweiApiAvailability.getInstance();
        if (availability.isUserResolvableError(errorCode)) {
            mResolvingError = true;
            availability.resolveError((Activity) contextLocal, errorCode, REQUEST_RESOLVE_ERROR, this);
        }
    }

    /**
     * 设置是否接收透传消息
     *
     * @param flag
     */
    private void setPassByMsg(boolean flag) {
        HuaweiPush.HuaweiPushApi.enableReceiveNormalMsg(client, flag);
    }

    /**
     * 获取Token
     */
    private void getToken() {
        if (!isConnected()) {
            return;
        }
        // 同步调用方式，不会返回token,通过广播的形式返回。
        new Thread(new Runnable() {
            @Override
            public void run() {
                PendingResult<TokenResult> token = HuaweiPush.HuaweiPushApi.getToken(client);
                token.await();
            }
        }).start();
    }


    /**
     * 判断是否连接
     *
     * @return
     */
    public boolean isConnected() {
        if (client != null && client.isConnected()) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * 注销token
     */
    private void delToken() {
        new Thread() {
            @Override
            public void run() {
                try {
                    String deltoken = PreferencesByUserAndTanentUtils.getString(contextLocal, "");
                    if (!TextUtils.isEmpty(deltoken) && null != client) {
                        HuaweiPush.HuaweiPushApi.deleteToken(client, deltoken);
                    } else {
                        LogUtils.YfcDebug("delete token's params is invalid.");
                    }
                } catch (Exception e) {
                    LogUtils.YfcDebug("delete token exception, " + e.toString());
                }
            }
        }.start();
    }

    /**
     * 获取状态
     */
    private void getState() {
        if (!isConnected()) {
            return;
        }
        new Thread() {
            @Override
            public void run() {
                // 状态结果通过广播返回
                HuaweiPush.HuaweiPushApi.getPushState(client);
            }
        }.start();
    }


    /**
     * 获取华为推送client
     *
     * @return
     */
    public HuaweiApiClient getHuaWeiPushClient() {
        return client;
    }

}
