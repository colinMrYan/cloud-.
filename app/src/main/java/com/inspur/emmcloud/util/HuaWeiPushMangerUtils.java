package com.inspur.emmcloud.util;

import android.app.Activity;
import android.support.annotation.NonNull;

import com.huawei.hms.api.ConnectionResult;
import com.huawei.hms.api.HuaweiApiAvailability;
import com.huawei.hms.api.HuaweiApiClient;
import com.huawei.hms.support.api.hwid.HuaweiId;
import com.huawei.hms.support.api.hwid.HuaweiIdSignInOptions;

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
        HuaweiIdSignInOptions options = new HuaweiIdSignInOptions.Builder(HuaweiIdSignInOptions.DEFAULT_SIGN_IN)
                .build();
            client = new HuaweiApiClient.Builder(activity) //
                    .addApi(HuaweiId.SIGN_IN_API, options) //
                    .addScope(HuaweiId.HUAEWEIID_BASE_SCOPE) //
                    .addConnectionCallbacks(this) //
                    .addOnConnectionFailedListener(this) //
                    .build();
    }

    @Override
    public void onUpdateFailed(@NonNull ConnectionResult connectionResult) {
        // TODO: 处理result.getErrorCode()
        mResolvingError = false;
    }

    @Override
    public void onConnected() {

    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
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

    /**
     * 获取华为推送client
     * @return
     */
    public HuaweiApiClient getHuaWeiPushClient(){
        return client;
    }
}
