package com.inspur.emmcloud.util;

import android.app.Activity;
import android.support.annotation.NonNull;

import com.huawei.hms.api.ConnectionResult;
import com.huawei.hms.api.HuaweiApiAvailability;
import com.huawei.hms.api.HuaweiApiClient;
import com.huawei.hms.support.api.client.PendingResult;
import com.huawei.hms.support.api.push.HuaweiPush;
import com.huawei.hms.support.api.push.TokenResult;

/**
 * Created by yufuchang on 2017/6/20.
 */

public class HuaWeiPushMangerUtils implements HuaweiApiClient.ConnectionCallbacks, HuaweiApiClient.OnConnectionFailedListener,
        HuaweiApiAvailability.OnUpdateListener {
    private HuaweiApiClient client;
    private boolean mResolvingError = false;
    private static final int REQUEST_RESOLVE_ERROR = 1001;
    private Activity activityLocal;
    public HuaWeiPushMangerUtils(Activity activity) {
        activityLocal = activity;
//        HuaweiIdSignInOptions options = new HuaweiIdSignInOptions.Builder(HuaweiIdSignInOptions.DEFAULT_SIGN_IN)
//                .build();
//            client = new HuaweiApiClient.Builder(activity) //
//                    .addApi(HuaweiId.SIGN_IN_API, options) //
//                    .addScope(HuaweiId.HUAEWEIID_BASE_SCOPE) //
//                    .addConnectionCallbacks(this) //
//                    .addOnConnectionFailedListener(this) //
//                    .build();
        LogUtils.YfcDebug("创建client");
        client = new HuaweiApiClient.Builder(activity)
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
        getToken();
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        LogUtils.YfcDebug("华为推送连接失败"+connectionResult.getErrorCode());
        if (mResolvingError) {
            return;
        }

        int errorCode = connectionResult.getErrorCode();
        HuaweiApiAvailability availability = HuaweiApiAvailability.getInstance();
        if (availability.isUserResolvableError(errorCode)) {
            mResolvingError = true;
            availability.resolveError(activityLocal, errorCode, REQUEST_RESOLVE_ERROR, this);
        }
    }

    private void getToken() {
        if (!isConnected()) {
//            tv.setText("get token failed, HMS is disconnect.");
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


    public boolean isConnected() {
        if (client != null && client.isConnected()) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * 获取华为推送client
     * @return
     */
    public HuaweiApiClient getHuaWeiPushClient(){
        return client;
    }
}
