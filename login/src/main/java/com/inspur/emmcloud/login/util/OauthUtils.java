package com.inspur.emmcloud.login.util;

import com.inspur.emmcloud.baselib.util.PreferencesUtils;
import com.inspur.emmcloud.baselib.util.StringUtils;
import com.inspur.emmcloud.baselib.util.ToastUtils;
import com.inspur.emmcloud.basemodule.application.BaseApplication;
import com.inspur.emmcloud.basemodule.util.AppExceptionCacheUtils;
import com.inspur.emmcloud.login.R;
import com.inspur.emmcloud.login.api.LoginAPIInterfaceImpl;
import com.inspur.emmcloud.login.api.LoginAPIService;
import com.inspur.emmcloud.login.bean.GetLoginResult;
import com.inspur.emmcloud.login.communication.CommunicationService;
import com.inspur.emmcloud.login.login.OauthCallBack;
import com.inspur.emmcloud.login.ui.LoginActivity;
import com.luojilab.component.componentlib.router.Router;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class OauthUtils extends LoginAPIInterfaceImpl {

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
            if (callBack != null) {
                callBack.reExecute();
            }
        } else {
            if (callBack != null) {
                callBackList.add(callBack);
            }
            // 防止多次刷新token
            synchronized (this) {
                if (!isTokenRefreshing) {
                    isTokenRefreshing = true;
                    if (!StringUtils.isBlank(BaseApplication.getInstance().getRefreshToken())) {
                        LoginAPIService apiService = new LoginAPIService(BaseApplication.getInstance());
                        apiService.setAPIInterface(this);
                        apiService.refreshToken();
                    } else {
                        if (!(BaseApplication.getInstance().getActivityLifecycleCallbacks().getCurrentActivity() instanceof LoginActivity)) {
                            ToastUtils.show(BaseApplication.getInstance(), R.string.login_authorization_expired);
                            BaseApplication.getInstance().signout();
                        }
                        callBackList.clear();
                        isTokenRefreshing = false;
                    }

                }
            }
        }
    }

    /**
     * 退出登录时注销token
     * 无后续需要根据返回内容
     */
    public void cancelToken() {
        LoginAPIService apiService = new LoginAPIService(BaseApplication.getInstance());
        apiService.cancelToken();
    }


        @Override
        public void returnRefreshTokenSuccess(GetLoginResult getLoginResult) {
            // TODO Auto-generated method stub
            String accessToken = getLoginResult.getAccessToken();
            String refreshToken = getLoginResult.getRefreshToken();
            int keepAlive = getLoginResult.getKeepAlive();
            String tokenType = getLoginResult.getTokenType();
            int expiresIn = getLoginResult.getExpiresIn();
            PreferencesUtils.putString(BaseApplication.getInstance(), "accessToken", accessToken);
            PreferencesUtils.putString(BaseApplication.getInstance(), "refreshToken", refreshToken);
            PreferencesUtils.putInt(BaseApplication.getInstance(), "keepAlive", keepAlive);
            PreferencesUtils.putString(BaseApplication.getInstance(), "tokenType", tokenType);
            PreferencesUtils.putInt(BaseApplication.getInstance(), "expiresIn", expiresIn);
            BaseApplication.getInstance().setAccessToken(accessToken);
            BaseApplication.getInstance().setRefreshToken(refreshToken);
            Router router = Router.getInstance();
            if (router.getService(CommunicationService.class.getSimpleName()) != null) {
                CommunicationService service = (CommunicationService) router.getService(CommunicationService.class.getSimpleName());
                service.startWebSocket();
            }
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
                object.put("AT", BaseApplication.getInstance().getAccessToken());
                object.put("RT", BaseApplication.getInstance().getRefreshToken());
                AppExceptionCacheUtils.saveAppException(BaseApplication.getInstance(), 6, "刷新token失败", object.toString(), errorCode);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @Override
        public void returnRefreshTokenFail(String error, int errorCode) {
            // TODO Auto-generated method stub
            saveRefreshTokenException(error, errorCode);
            //当errorCode为400时代表refreshToken也失效，需要重新登录
            if (errorCode != 400) {
                for (OauthCallBack oauthCallBack : callBackList) {
                    oauthCallBack.executeFailCallback();
                }

            } else if (!(BaseApplication.getInstance().getActivityLifecycleCallbacks().getCurrentActivity() instanceof LoginActivity)) {
                ToastUtils.show(BaseApplication.getInstance(), R.string.login_authorization_expired);
                BaseApplication.getInstance().signout();
            }
            callBackList.clear();
            isTokenRefreshing = false;
        }



}
