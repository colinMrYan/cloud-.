package com.inspur.emmcloud.util.privates;

import com.huawei.hms.api.ConnectionResult;
import com.huawei.hms.api.HuaweiApiClient;
import com.huawei.hms.api.HuaweiApiClient.ConnectionCallbacks;
import com.huawei.hms.api.HuaweiApiClient.OnConnectionFailedListener;
import com.huawei.hms.support.api.client.PendingResult;
import com.huawei.hms.support.api.push.HuaweiPush;
import com.huawei.hms.support.api.push.TokenResult;
import com.inspur.emmcloud.config.Constant;
import com.inspur.emmcloud.util.common.PreferencesUtils;
import com.inspur.emmcloud.util.common.StringUtils;

import android.content.Context;
import android.support.annotation.NonNull;

/**
 * Created by yufuchang on 2017/6/20.
 * 华为推送模块，单独模块封装
 */
public class HuaWeiPushMangerUtils implements ConnectionCallbacks, OnConnectionFailedListener {
    private static HuaWeiPushMangerUtils huaWeiPushMangerUtils;
    private HuaweiApiClient client;
    private Context contextLocal;

    private HuaWeiPushMangerUtils(final Context context) {
        contextLocal = context;
        client = new HuaweiApiClient.Builder(context)
                .addApi(HuaweiPush.PUSH_API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();
    }

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

    /**
     * 华为建立连接方法
     */
    public void connect() {
        client.connect();
    }

    @Override
    public void onConnected() {
        PushManagerUtils.getInstance().setPushFlag(contextLocal, Constant.HUAWEI_FLAG);
        getToken();
        setPassByMsg(true);
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        // startJpushInMainThread();
//        保留下来做参照的代码，这里如果华为没有连上，华为SDK有一个处理，这里改为自己处理
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

    // /**
    // * 连接Jpush
    // */
    // private void startJpush() {
    // if (client != null) {
    // stopPush();
    // client.disconnect();
    // }
    // PushManagerUtils.getInstance().setJpushStatus(true);
    // }

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
                //由于华为推送不稳定，此处加trycatch增强程序稳定性
                try {
                    PendingResult<TokenResult> token = HuaweiPush.HuaweiPushApi.getToken(client);
                    TokenResult tokenResult = token.await();
                    //处理connect成功，获取token失败的情况
                    // if (tokenResult.getTokenRes().getRetCode() != 0) {
                    // startJpushInMainThread();
                    // }
                } catch (Exception e) {
                    e.printStackTrace();
                    // startJpushInMainThread();
                }

            }
        }).start();
    }

    // /**
    // * 从主线程启动Jpush
    // */
    // private void startJpushInMainThread() {
    // new Handler(getMainLooper()).post(new Runnable() {
    // @Override
    // public void run() {
    // startJpush();
    // }
    // });
    // }


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
    public void stopPush() {
        new Thread() {
            @Override
            public void run() {
                try {
                    String deltoken = PreferencesUtils.getString(contextLocal, Constant.HUAWEI_PUSH_TOKEN, "");
                    if (!StringUtils.isEmpty(deltoken) && null != client) {
                        HuaweiPush.HuaweiPushApi.deleteToken(client, deltoken);
                        client.disconnect();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }.start();
    }

}
