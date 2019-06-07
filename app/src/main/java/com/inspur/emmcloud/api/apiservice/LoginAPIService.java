/**
 * LoginAPIService.java
 * classes : com.inspur.emmcloud.api.apiservice.LoginAPIService
 * V 1.0.0
 * Create at 2016年11月8日 下午2:34:43
 */
package com.inspur.emmcloud.api.apiservice;

import android.content.Context;

import com.inspur.emmcloud.MyApplication;
import com.inspur.emmcloud.api.APIInterface;
import com.inspur.emmcloud.api.APIUri;
import com.inspur.emmcloud.baselib.util.StringUtils;
import com.inspur.emmcloud.basemodule.api.BaseModuleAPICallback;
import com.inspur.emmcloud.basemodule.api.CloudHttpMethod;
import com.inspur.emmcloud.basemodule.api.HttpUtils;
import com.inspur.emmcloud.basemodule.bean.GetMyInfoResult;
import com.inspur.emmcloud.bean.appcenter.GetRegisterCheckResult;
import com.inspur.emmcloud.bean.login.GetLoginResult;
import com.inspur.emmcloud.login.login.OauthCallBack;
import com.inspur.emmcloud.util.privates.OauthUtils;

import org.json.JSONObject;
import org.xutils.ex.HttpException;
import org.xutils.http.RequestParams;
import org.xutils.http.app.RequestTracker;
import org.xutils.http.request.UriRequest;

import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.concurrent.TimeoutException;

/**
 * com.inspur.emmcloud.api.apiservice.LoginAPIService create at 2016年11月8日
 * 下午2:34:43
 */
public class LoginAPIService {
    private Context context;
    private APIInterface apiInterface;

    public LoginAPIService(Context context) {
        this.context = context;
    }

    public void setAPIInterface(APIInterface apiInterface) {
        this.apiInterface = apiInterface;
    }

    /**
     * 登录
     *
     * @param userName
     * @param password
     */
    public void OauthSignIn(String userName, String password) {
        String completeUrl = APIUri.getOauthSigninUrl();
        RequestParams params = new RequestParams(completeUrl);
        params.addParameter("grant_type", "password");
        params.addParameter("username", userName);
        params.addParameter("password", password);
        params.addParameter("client_id", "com.inspur.ecm.client.android");
        params.addParameter("client_secret",
                "6b3c48dc-2e56-440c-84fb-f35be37480e8");
        params.setRequestTracker(new RequestTracker() {
            @Override
            public void onWaiting(RequestParams requestParams) {

            }

            @Override
            public void onStart(RequestParams requestParams) {

            }

            @Override
            public void onRequestCreated(UriRequest uriRequest) {

            }

            @Override
            public void onCache(UriRequest uriRequest, Object o) {

            }

            @Override
            public void onSuccess(UriRequest uriRequest, Object o) {

            }

            @Override
            public void onCancelled(UriRequest uriRequest) {

            }

            @Override
            public void onError(UriRequest uriRequest, Throwable throwable, boolean b) {
                String error = "";
                int responseCode = -1;
                if (throwable instanceof TimeoutException || throwable instanceof SocketTimeoutException) {
                    error = "time out";
                    responseCode = 1001;
                } else if (throwable instanceof UnknownHostException) {
                    error = "time out";
                    responseCode = 1003;
                } else if (throwable instanceof HttpException) {
                    HttpException httpEx = (HttpException) throwable;
                    error = httpEx.getResult();
                    responseCode = httpEx.getCode();
                } else {
                    error = throwable.toString();
                }
                if (StringUtils.isBlank(error)) {
                    error = "未知错误";
                }
                String headerLimitRemaining = "";
                String headerRetryAfter= "";
                if (uriRequest != null && uriRequest.getResponseHeaders() != null){
                    headerLimitRemaining = uriRequest.getResponseHeader("X-Rate-Limit-Remaining");
                    headerRetryAfter = uriRequest.getResponseHeader("X-Rate-Limit-Retry-After-Seconds");
                }
                apiInterface.returnOauthSignInFail(error, responseCode,headerLimitRemaining,headerRetryAfter);
            }

            @Override
            public void onFinished(UriRequest uriRequest) {

            }
        });
        HttpUtils.request(context, CloudHttpMethod.POST, params, new BaseModuleAPICallback(context, completeUrl) {

            @Override
            public void callbackSuccess(byte[] arg0) {
                // TODO Auto-generated method stub
                apiInterface.returnOauthSignInSuccess(new GetLoginResult(new String(arg0)));
            }

            @Override
            public void callbackFail(String error, int responseCode) {
                // TODO Auto-generated method stub
//                apiInterface.returnOauthSignInFail(error, responseCode);
            }

            @Override
            public void callbackTokenExpire(long requestTime) {
                // TODO Auto-generated method stub
//                apiInterface.returnOauthSignInFail(new String(""), 500,-1,-1);
            }

        });
    }

    /**
     * 刷新token
     */
    public void refreshToken() {
        String completeUrl = APIUri.getOauthSigninUrl();
        String refreshToken = MyApplication.getInstance().getRefreshToken();
        RequestParams params = new RequestParams(completeUrl);
        params.setConnectTimeout(3000);
        params.addParameter("client_id", "com.inspur.ecm.client.android");
        params.addParameter("client_secret",
                "6b3c48dc-2e56-440c-84fb-f35be37480e8");
        params.addParameter("refresh_token", refreshToken);
        params.addParameter("grant_type", "refresh_token");
        HttpUtils.request(context, CloudHttpMethod.POST, params, new BaseModuleAPICallback(context, completeUrl) {

            @Override
            public void callbackSuccess(byte[] arg0) {
                // TODO Auto-generated method stub
                apiInterface.returnRefreshTokenSuccess(new GetLoginResult(new String(arg0)));
            }

            @Override
            public void callbackFail(String error, int responseCode) {
                // TODO Auto-generated method stub
                apiInterface.returnRefreshTokenFail(error, responseCode);
            }

            @Override
            public void callbackTokenExpire(long requestTime) {
                // TODO Auto-generated method stub
                apiInterface.returnRefreshTokenFail("", -1);
            }

        });

    }

    /**
     * 退出登录时取消token
     */
    public void cancelToken() {
        final String url = APIUri.getCancelTokenUrl() + "?destroy=ALL";
        RequestParams params = MyApplication.getInstance().getHttpRequestParams(url);
        HttpUtils.request(context, CloudHttpMethod.GET, params, new BaseModuleAPICallback(context, url) {
            @Override
            public void callbackSuccess(byte[] arg0) {
            }

            @Override
            public void callbackFail(String error, int responseCode) {
            }

            @Override
            public void callbackTokenExpire(long requestTime) {
            }
        });
    }


    /**
     * 短信登录-发送短信
     *
     * @param mobile
     */
    public void getLoginSMSCaptcha(String mobile) {
        String completeUrl = APIUri.getLoginSMSCaptchaUrl(mobile);
        RequestParams params = ((MyApplication) context.getApplicationContext())
                .getHttpRequestParams(completeUrl);
        HttpUtils.request(context, CloudHttpMethod.GET, params, new BaseModuleAPICallback(context, completeUrl) {

            @Override
            public void callbackTokenExpire(long requestTime) {
                // TODO Auto-generated method stub
                apiInterface.returnLoginSMSCaptchaFail("", 500);
            }

            @Override
            public void callbackSuccess(byte[] arg0) {
                // TODO Auto-generated method stub
                apiInterface.returnLoginSMSCaptchaSuccess();
            }

            @Override
            public void callbackFail(String error, int responseCode) {
                // TODO Auto-generated method stub
                apiInterface.returnLoginSMSCaptchaFail(error, responseCode);
            }
        });

    }


    /**
     * 验证短信验证码
     *
     * @param mobile
     * @param sms
     */
    public void SMSRegisterCheck(String mobile, String sms) {
        String completeUrl = APIUri.getSMSRegisterCheckUrl();
        RequestParams params = ((MyApplication) context.getApplicationContext())
                .getHttpRequestParams(completeUrl);
        params.addParameter("mobile", mobile);
        params.addParameter("sms", sms);
        HttpUtils.request(context, CloudHttpMethod.POST, params, new BaseModuleAPICallback(context, completeUrl) {

            @Override
            public void callbackTokenExpire(long requestTime) {
                // TODO Auto-generated method stub
                apiInterface.returnReisterSMSCheckFail("", -1);
            }

            @Override
            public void callbackSuccess(byte[] arg0) {
                // TODO Auto-generated method stub
                apiInterface
                        .returnReisterSMSCheckSuccess(new GetRegisterCheckResult(new String(arg0)));
            }

            @Override
            public void callbackFail(String error, int responseCode) {
                // TODO Auto-generated method stub
                apiInterface.returnReisterSMSCheckFail(error, responseCode);
            }
        });

    }


    /**
     * 获取个人信息 得到当前用户的登录信息
     */
    public void getMyInfo() {
        final String completeUrl = APIUri.getMyInfoUrl();
        RequestParams params = ((MyApplication) context.getApplicationContext())
                .getHttpRequestParams(completeUrl);
        HttpUtils.request(context, CloudHttpMethod.GET, params, new BaseModuleAPICallback(context, completeUrl) {

            @Override
            public void callbackTokenExpire(long requestTime) {
                OauthCallBack oauthCallBack = new OauthCallBack() {
                    @Override
                    public void reExecute() {
                        getMyInfo();
                    }

                    @Override
                    public void executeFailCallback() {
                        callbackFail("", -1);
                    }
                };
                OauthUtils.getInstance().refreshToken(
                        oauthCallBack, requestTime);
            }

            @Override
            public void callbackSuccess(byte[] arg0) {
                // TODO Auto-generated method stub
                apiInterface.returnMyInfoSuccess(new GetMyInfoResult(new String(arg0)));
            }

            @Override
            public void callbackFail(String error, int responseCode) {
                // TODO Auto-generated method stub
                apiInterface.returnMyInfoFail(error, responseCode);
            }
        });
    }

    /**
     * 修改密码
     *
     * @param oldpsd
     * @param newpsd
     */
    public void modifyPassword(final String oldpsd, final String newpsd) {
        final String completeUrl = APIUri.getChangePsdUrl();
        RequestParams params = ((MyApplication) context.getApplicationContext()).getHttpRequestParams(completeUrl);
        params.addQueryStringParameter("old", oldpsd);
        params.addQueryStringParameter("new", newpsd);
        params.setAsJsonContent(true);
        params.addHeader("Content-Type", "application/json");
        HttpUtils.request(context, CloudHttpMethod.PUT, params, new BaseModuleAPICallback(context, completeUrl) {

            @Override
            public void callbackTokenExpire(long requestTime) {
                OauthCallBack oauthCallBack = new OauthCallBack() {
                    @Override
                    public void reExecute() {
                        modifyPassword(oldpsd, newpsd);
                    }

                    @Override
                    public void executeFailCallback() {
                        callbackFail("", -1);
                    }
                };
                OauthUtils.getInstance().refreshToken(
                        oauthCallBack, requestTime);
            }

            @Override
            public void callbackSuccess(byte[] arg0) {
                // TODO Auto-generated method stub
                apiInterface.returnModifyPasswordSuccess();
            }

            @Override
            public void callbackFail(String error, int responseCode) {
                // TODO Auto-generated method stub
                apiInterface.returnModifyPasswordFail(error, responseCode);
            }
        });
    }


    /**
     * 通过短信验证码更新密码
     *
     * @param smsCode
     * @param newPwd
     */
    public void resetPassword(final String smsCode, final String newPwd) {
        final String completeUrl = APIUri.getChangePsdUrl();
        RequestParams params = ((MyApplication) context.getApplicationContext()).getHttpRequestParams(completeUrl);
        params.addQueryStringParameter("passcode", smsCode);
        params.addQueryStringParameter("new", newPwd);
        params.setAsJsonContent(true);
        params.addHeader("Content-Type", "application/json");
        HttpUtils.request(context, CloudHttpMethod.PUT, params, new BaseModuleAPICallback(context, completeUrl) {

            @Override
            public void callbackTokenExpire(long requestTime) {
                OauthCallBack oauthCallBack = new OauthCallBack() {
                    @Override
                    public void reExecute() {
                        resetPassword(smsCode, newPwd);
                    }

                    @Override
                    public void executeFailCallback() {
                        callbackFail("", -1);
                    }
                };
                OauthUtils.getInstance().refreshToken(
                        oauthCallBack, requestTime);
            }

            @Override
            public void callbackSuccess(byte[] arg0) {
                apiInterface.returnResetPasswordSuccess();
            }

            @Override
            public void callbackFail(String error, int responseCode) {
                apiInterface.returnResetPasswordFail(error, responseCode);
            }
        });
    }

    public void faceLoginGS(final String bitmapBase64, final String token) {
        final String completeUrl = "https://emm.inspur.com/app/imp/v6.0/Connect/FaceLogin";
        RequestParams params = ((MyApplication) context.getApplicationContext()).getHttpRequestParams(completeUrl);
        JSONObject object = new JSONObject();
        try {
            object.put("token", token);
            object.put("face", bitmapBase64);
        } catch (Exception e) {
            e.printStackTrace();
        }
        params.setBodyContent(object.toString());
        params.setAsJsonContent(true);
        HttpUtils.request(context, CloudHttpMethod.POST, params, new BaseModuleAPICallback(context, completeUrl) {
            @Override
            public void callbackSuccess(byte[] arg0) {
                apiInterface.returnFaceLoginGSSuccess();
            }

            @Override
            public void callbackFail(String error, int responseCode) {
                apiInterface.returnFaceLoginGSFail(error, responseCode);
            }

            @Override
            public void callbackTokenExpire(long requestTime) {
                OauthCallBack oauthCallBack = new OauthCallBack() {
                    @Override
                    public void reExecute() {
                        faceLoginGS(bitmapBase64, token);
                    }

                    @Override
                    public void executeFailCallback() {
                        callbackFail("", -1);
                    }
                };
                OauthUtils.getInstance().refreshToken(
                        oauthCallBack, requestTime);
            }

        });

    }
}
