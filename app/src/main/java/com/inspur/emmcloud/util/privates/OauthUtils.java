package com.inspur.emmcloud.util.privates;

import com.inspur.emmcloud.MyApplication;
import com.inspur.emmcloud.R;
import com.inspur.emmcloud.api.APIInterfaceInstance;
import com.inspur.emmcloud.api.apiservice.LoginAPIService;
import com.inspur.emmcloud.bean.login.GetLoginResult;
import com.inspur.emmcloud.interf.OauthCallBack;
import com.inspur.emmcloud.push.WebSocketPush;
import com.inspur.emmcloud.ui.login.LoginActivity;
import com.inspur.emmcloud.util.common.PreferencesUtils;
import com.inspur.emmcloud.util.common.ToastUtils;
import com.inspur.emmcloud.util.privates.cache.AppExceptionCacheUtils;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;


public class OauthUtils {

    private static OauthUtils mInstance;
    private long tokenGetTime = 0L;
    private boolean isTokenRefreshing = false;
    private List<OauthCallBack> callBackList = new ArrayList<OauthCallBack>();

    public static OauthUtils getInstance() {
        if (mInstance == null) {
            synchronized (OauthUtils.class) {
                if (mInstance == null) {
                    mInstance = new OauthUtils();
                }
            }
        }
        return mInstance;
    }

    public void refreshToken(OauthCallBack callBack, long requestTime) {
        //当请求时间小于新token的获取时间时，代表token已经更新，重新执行该请求
        if (requestTime < tokenGetTime) {
            callBack.reExecute();
        } else {
            callBackList.add(callBack);
            // 防止多次刷新token
            if (!isTokenRefreshing) {
                isTokenRefreshing = true;
                LoginAPIService apiService = new LoginAPIService(MyApplication.getInstance());
                apiService.setAPIInterface(new WebService());
                apiService.refreshToken();
            }
        }
    }

    private class WebService extends APIInterfaceInstance {
        @Override
        public void returnOauthSigninSuccess(GetLoginResult getLoginResult) {
            // TODO Auto-generated method stub
            String accessToken = getLoginResult.getAccessToken();
            String refreshToken = getLoginResult.getRefreshToken();
            int keepAlive = getLoginResult.getKeepAlive();
            String tokenType = getLoginResult.getTokenType();
            int expiresIn = getLoginResult.getExpiresIn();
            PreferencesUtils.putString(MyApplication.getInstance(), "accessToken", accessToken);
            PreferencesUtils.putString(MyApplication.getInstance(), "refreshToken", refreshToken);
            PreferencesUtils.putInt(MyApplication.getInstance(), "keepAlive", keepAlive);
            PreferencesUtils.putString(MyApplication.getInstance(), "tokenType", tokenType);
            PreferencesUtils.putInt(MyApplication.getInstance(), "expiresIn", expiresIn);
            MyApplication.getInstance().setAccessToken(accessToken);
            MyApplication.getInstance().setRefreshToken(refreshToken);
            WebSocketPush.getInstance().startWebSocket(false);
            tokenGetTime = System.currentTimeMillis();
            isTokenRefreshing = false;
            for (OauthCallBack oauthCallBack : callBackList) {
                oauthCallBack.reExecute();
            }
            callBackList.clear();
        }

        /**
         * 将刷新token失败的异常进行记录
         *
         * @param error
         * @param errorCode
         */
        private void saveRefreshTokenException(String error, int errorCode) {
            JSONObject object = new JSONObject();
            try {
                object.put("error", error);
                object.put("AT", MyApplication.getInstance().getAccessToken());
                object.put("RT", MyApplication.getInstance().getRefreshToken());
                AppExceptionCacheUtils.saveAppException(MyApplication.getInstance(), 6, "刷新token失败", object.toString(), errorCode);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @Override
        public void returnOauthSigninFail(String error, int errorCode) {
            // TODO Auto-generated method stub
            saveRefreshTokenException(error, errorCode);
            //当errorCode为400时代表refreshToken也失效，需要重新登录
            if (errorCode != 400) {
                for (OauthCallBack oauthCallBack : callBackList) {
                    oauthCallBack.executeFailCallback();
                }
            } else if (!(MyApplication.getInstance().getActivityLifecycleCallbacks().getCurrentActivity() instanceof LoginActivity)) {
                ToastUtils.show(MyApplication.getInstance(), R.string.authorization_expired);
                MyApplication.getInstance().signout();
            }
            callBackList.clear();
            isTokenRefreshing = false;
        }

    }
}
