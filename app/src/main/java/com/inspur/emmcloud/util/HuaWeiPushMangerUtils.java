package com.inspur.emmcloud.util;

import android.content.Context;
import android.os.Handler;
import android.support.annotation.NonNull;

import com.huawei.hms.api.ConnectionResult;
import com.huawei.hms.api.HuaweiApiClient;
import com.huawei.hms.api.HuaweiApiClient.ConnectionCallbacks;
import com.huawei.hms.api.HuaweiApiClient.OnConnectionFailedListener;
import com.huawei.hms.support.api.client.PendingResult;
import com.huawei.hms.support.api.push.HuaweiPush;
import com.huawei.hms.support.api.push.TokenResult;
import com.inspur.emmcloud.MyApplication;
import com.inspur.emmcloud.push.WebSocketPush;

import static android.os.Looper.getMainLooper;

/**
 * Created by yufuchang on 2017/6/20.
 * 华为推送模块，单独模块封装
 */

public class HuaWeiPushMangerUtils implements ConnectionCallbacks, OnConnectionFailedListener {
    private boolean mResolvingError = false;
    private static final int REQUEST_RESOLVE_ERROR = 1001;
    private static HuaWeiPushMangerUtils huaWeiPushMangerUtils;
    private HuaweiApiClient client;
    private Context contextLocal;

    public static HuaWeiPushMangerUtils getInstance(Context context) {
        if (huaWeiPushMangerUtils == null) {
            synchronized (HuaWeiPushMangerUtils.class) {
                if (huaWeiPushMangerUtils == null) {
                    huaWeiPushMangerUtils = new HuaWeiPushMangerUtils(context);

                }
            }
        }
        return huaWeiPushMangerUtils;
    }

    private HuaWeiPushMangerUtils(final Context context) {
        contextLocal = context;
//        HuaweiIdSignInOptions options = new HuaweiIdSignInOptions.Builder(HuaweiIdSignInOptions.DEFAULT_SIGN_IN)
//                .build();
//            client = new HuaweiApiClient.Builder(activity) //
//                    .addApi(HuaweiId.SIGN_IN_API, options) //
//                    .addScope(HuaweiId.HUAEWEIID_BASE_SCOPE) //
//                    .addConnectionCallbacks(this) //
//                    .addOnConnectionFailedListener(this) //
//                    .build();

        client = new HuaweiApiClient.Builder(context)
                .addApi(HuaweiPush.PUSH_API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();
    }

    /**
     * 连接方法单列
     */
    public void connect() {
        client.connect();
    }


    @Override
    public void onConnected() {
        PreferencesUtils.putString(contextLocal, "pushFlag", "huawei");
        getToken();
        if (((MyApplication) contextLocal.getApplicationContext()).isIndexActivityRunning()) {
            WebSocketPush.getInstance(contextLocal).start();
        }
        setPassByMsg(true);
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        int errorCode = connectionResult.getErrorCode();
        LogUtils.YfcDebug("huaweiErrorCode" + errorCode);
        new Handler(getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                startJpush();
            }
        });

//        保留下来做参照的代码
//        if (mResolvingError) {
//            return;
//        }
//        HuaweiApiAvailability availability = HuaweiApiAvailability.getInstance();
//        if (availability.isUserResolvableError(errorCode)) {
//            mResolvingError = true;
//            //尝试重新连接推送
////            availability.resolveError((Activity) contextLocal, errorCode, REQUEST_RESOLVE_ERROR, this);
//        }
    }

    /**
     * 连接Jpush
     */
    private void startJpush() {
        if (client != null) {
            delToken();
            client.disconnect();
        }
        PreferencesUtils.putString(contextLocal, "pushFlag", "Jpush");
        ((MyApplication) contextLocal.getApplicationContext()).startPush();
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
        return (client != null && client.isConnected());
    }

    /**
     * 注销token
     */
    public void delToken() {
        new Thread() {
            @Override
            public void run() {
                try {
                    String deltoken = PreferencesUtils.getString(contextLocal, "huawei_push_token", "");
                    if (!StringUtils.isEmpty(deltoken) && null != client) {
                        HuaweiPush.HuaweiPushApi.deleteToken(client, deltoken);
//                        //清除本地token
//                        PreferencesUtils.putString(contextLocal, "huawei_push_token","");
                    }
                } catch (Exception e) {
                    e.printStackTrace();
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
