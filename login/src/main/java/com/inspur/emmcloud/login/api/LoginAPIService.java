/**
 * LoginAPIService.java
 * classes : com.inspur.emmcloud.login.api.LoginAPIService
 * V 1.0.0
 * Create at 2016年11月8日 下午2:34:43
 */
package com.inspur.emmcloud.login.api;

import android.content.Context;
import android.util.Base64;

import com.inspur.emmcloud.baselib.util.LogUtils;
import com.inspur.emmcloud.baselib.util.PreferencesUtils;
import com.inspur.emmcloud.baselib.util.StringUtils;
import com.inspur.emmcloud.basemodule.api.BaseModuleAPICallback;
import com.inspur.emmcloud.basemodule.api.CloudHttpMethod;
import com.inspur.emmcloud.basemodule.api.HttpUtils;
import com.inspur.emmcloud.basemodule.application.BaseApplication;
import com.inspur.emmcloud.basemodule.config.Constant;
import com.inspur.emmcloud.basemodule.util.AppUtils;
import com.inspur.emmcloud.basemodule.util.WebServiceRouterManager;
import com.inspur.emmcloud.componentservice.login.OauthCallBack;
import com.inspur.emmcloud.login.bean.GetDeviceCheckResult;
import com.inspur.emmcloud.login.bean.GetLoginResult;
import com.inspur.emmcloud.login.bean.GetRegisterCheckResult;
import com.inspur.emmcloud.login.bean.LoginDesktopCloudPlusBean;
import com.inspur.emmcloud.login.bean.UploadMDMInfoResult;
import com.inspur.emmcloud.login.util.OauthUtils;

import org.json.JSONException;
import org.json.JSONObject;
import org.xutils.ex.HttpException;
import org.xutils.http.RequestParams;
import org.xutils.http.app.RequestTracker;
import org.xutils.http.request.UriRequest;

import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.spec.EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.concurrent.TimeoutException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.crypto.Cipher;

/**
 * com.inspur.emmcloud.login.api.LoginAPIService create at 2016年11月8日
 * 下午2:34:43
 */
public class LoginAPIService {
    private static final String TAG = "LoginAPIService";
    private Context context;
    private LoginAPIInterface apiInterface;

    public LoginAPIService(Context context) {
        this.context = context;
    }

    public void setAPIInterface(LoginAPIInterface apiInterface) {
        this.apiInterface = apiInterface;
    }

    /**
     * 登录
     *
     * @param userName
     * @param password
     */
    public void OauthSignIn(String userName, String password) {
        String completeUrl = LoginAPIUri.getOauthSigninUrl();
        RequestParams params = BaseApplication.getInstance().getHttpRequestParams(completeUrl);
        params.addParameter("grant_type", "password");
        params.addParameter("username", userName);
        params.addParameter("password", encryptPassword(password));
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
                String headerRetryAfter = "";
                if (uriRequest != null && uriRequest.getResponseHeaders() != null) {
                    headerLimitRemaining = uriRequest.getResponseHeader("X-Rate-Limit-Remaining");
                    headerRetryAfter = uriRequest.getResponseHeader("X-Rate-Limit-Retry-After-Seconds");
                }
                apiInterface.returnOauthSignInFail(error, responseCode, headerLimitRemaining, headerRetryAfter);
            }

            @Override
            public void onFinished(UriRequest uriRequest) {

            }
        });
        HttpUtils.request(context, CloudHttpMethod.POST, params, new BaseModuleAPICallback(context, completeUrl) {

            @Override
            public void callbackSuccess(byte[] arg0) {
                apiInterface.returnOauthSignInSuccess(new GetLoginResult(new String(arg0)));
            }

            @Override
            public void callbackFail(String error, int responseCode) {
//                apiInterface.returnOauthSignInFail(error, responseCode);
            }

            @Override
            public void callbackTokenExpire(long requestTime) {
//                apiInterface.returnOauthSignInFail(new String(""), 500,-1,-1);
            }

        });
    }

    private String encryptPassword(String password) {
        if (!needEncryptPassword()) {
            return password;
        }
        String passwordTmp1 = password + ':' + System.currentTimeMillis();
        String publicKeyStr = "MFwwDQYJKoZIhvcNAQEBBQADSwAwSAJBAKn5D+lBk/cGLCuznxSmouLgd//4nnid2cz7EwsSINHIFBaRnMxIlfxmjLm7fUKWqKsN9MpW3NaEGmfKJBB7yHMCAwEAAQ==";
        String outPassword = password;
        try {
            // base64编码的公钥
            byte[] publicKeyBytes = Base64.decode(publicKeyStr, Base64.DEFAULT);
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            EncodedKeySpec publicKeySpec = new X509EncodedKeySpec(publicKeyBytes);
            PublicKey publicKey = keyFactory.generatePublic(publicKeySpec);
            // RSA加密
            Cipher encryptCipher = Cipher.getInstance("RSA/None/PKCS1Padding");
            encryptCipher.init(Cipher.ENCRYPT_MODE, publicKey);
            outPassword = "E0:" + Base64.encodeToString(encryptCipher.doFinal(passwordTmp1.getBytes("UTF-8")), Base64.DEFAULT);
        } catch (Exception e) {
            e.printStackTrace();
            return password;
        }
        Pattern p = Pattern.compile("\\s*|\t|\r|\n");
        // \n回车 \t 水平制表符 \s 空格 \r换行
        Matcher m = p.matcher(outPassword);
        outPassword = m.replaceAll("");
        return outPassword;
    }

    private boolean needEncryptPassword() {
        return LoginAPIUri.getOauthSigninUrl().startsWith(Constant.DEFAULT_CLUSTER_ID);
    }

    /**
     * 刷新token
     */
    public void refreshToken() {
        String completeUrl = LoginAPIUri.getOauthSigninUrl();
        String refreshToken = BaseApplication.getInstance().getRefreshToken();
        RequestParams params = BaseApplication.getInstance().getHttpRequestParams(completeUrl);
        params.setConnectTimeout(3000);
        params.addParameter("client_id", "com.inspur.ecm.client.android");
        params.addParameter("client_secret",
                "6b3c48dc-2e56-440c-84fb-f35be37480e8");
        params.addParameter("refresh_token", refreshToken);
        params.addParameter("grant_type", "refresh_token");
        HttpUtils.request(context, CloudHttpMethod.POST, params, new BaseModuleAPICallback(context, completeUrl) {

            @Override
            public void callbackSuccess(byte[] arg0) {
                apiInterface.returnRefreshTokenSuccess(new GetLoginResult(new String(arg0)));
            }

            @Override
            public void callbackFail(String error, int responseCode) {
                apiInterface.returnRefreshTokenFail(error, responseCode);
            }

            @Override
            public void callbackTokenExpire(long requestTime) {
                apiInterface.returnRefreshTokenFail("", -1);
            }

        });

    }

    /**
     * 退出登录时取消token
     */
    public void cancelToken() {
        final String url = LoginAPIUri.getCancelTokenUrl() + "?destroy=ALL";
        RequestParams params = BaseApplication.getInstance().getHttpRequestParams(url);
        HttpUtils.request(context, CloudHttpMethod.GET, params, new BaseModuleAPICallback(context, url) {
            @Override
            public void callbackSuccess(byte[] arg0) {
                cancelEmmToken();
                destroyEcmToken();
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
     * 退出登录时取消Emm保存的token
     */
    public void cancelEmmToken() {
        if (!WebServiceRouterManager.getInstance().getClusterEmm().equals("https://emm.inspuronline.com/"))
            return;
        final String url = LoginAPIUri.getCancelEmmTokenUrl();
        RequestParams params = BaseApplication.getInstance().getHttpRequestParams(url);
        HttpUtils.request(context, CloudHttpMethod.POST, params, new BaseModuleAPICallback(context, url) {
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

    public void destroyEcmToken() {
        final String url = LoginAPIUri.getDestroyEcmTokenUrl();
        RequestParams params = BaseApplication.getInstance().getHttpRequestParams(url);
        HttpUtils.request(context, CloudHttpMethod.DELETE, params, new BaseModuleAPICallback(context, url) {
            @Override
            public void callbackSuccess(byte[] arg0) {
                LogUtils.debug(TAG, "destroyEcmToken callbackSuccess:" + new String(arg0));
            }

            @Override
            public void callbackFail(String error, int responseCode) {
                LogUtils.debug(TAG, "destroyEcmToken callbackFail: error" + error + ",responseCode:" + responseCode);
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
        String completeUrl = LoginAPIUri.getLoginSMSCaptchaUrl(mobile);
        RequestParams params = ((BaseApplication) context.getApplicationContext())
                .getHttpRequestParams(completeUrl);
        HttpUtils.request(context, CloudHttpMethod.GET, params, new BaseModuleAPICallback(context, completeUrl) {

            @Override
            public void callbackTokenExpire(long requestTime) {
                apiInterface.returnLoginSMSCaptchaFail("", 500);
            }

            @Override
            public void callbackSuccess(byte[] arg0) {
                apiInterface.returnLoginSMSCaptchaSuccess();
            }

            @Override
            public void callbackFail(String error, int responseCode) {
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
        String completeUrl = LoginAPIUri.getSMSRegisterCheckUrl();
        RequestParams params = ((BaseApplication) context.getApplicationContext())
                .getHttpRequestParams(completeUrl);
        params.addParameter("mobile", mobile);
        params.addParameter("sms", sms);
        HttpUtils.request(context, CloudHttpMethod.POST, params, new BaseModuleAPICallback(context, completeUrl) {

            @Override
            public void callbackTokenExpire(long requestTime) {
                apiInterface.returnReisterSMSCheckFail("", -1);
            }

            @Override
            public void callbackSuccess(byte[] arg0) {
                apiInterface
                        .returnReisterSMSCheckSuccess(new GetRegisterCheckResult(new String(arg0)));
            }

            @Override
            public void callbackFail(String error, int responseCode) {
                apiInterface.returnReisterSMSCheckFail(error, responseCode);
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
        final String completeUrl = LoginAPIUri.getChangePsdUrl();
        RequestParams params = ((BaseApplication) context.getApplicationContext()).getHttpRequestParams(completeUrl);
        params.addQueryStringParameter("old", oldpsd);
        params.addQueryStringParameter("new", newpsd);
        // xUtils 3.9.0设置ContentType逻辑与3.5.0不同，put时也必须设置ContentType，post暂未发现问题
        JSONObject paramObj = new JSONObject();
        try {
            paramObj.put("old", "");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        params.setBodyContent(paramObj.toString());
        params.setBodyContentType("application/json");
//        params.setRequestBody();
//        params.setAsJsonContent(true);
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
                apiInterface.returnModifyPasswordSuccess();
            }

            @Override
            public void callbackFail(String error, int responseCode) {
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
        final String completeUrl = LoginAPIUri.getChangePsdUrl();
        RequestParams params = ((BaseApplication) context.getApplicationContext()).getHttpRequestParams(completeUrl);
        params.addQueryStringParameter("passcode", smsCode);
        params.addQueryStringParameter("new", newPwd);
        // xUtils 3.9.0设置ContentType逻辑与3.5.0不同，put时也必须设置ContentType，post暂未发现问题
        JSONObject paramObj = new JSONObject();
        try {
            paramObj.put("new", "");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        params.setBodyContent(paramObj.toString());
        params.setBodyContentType("application/json");
//        params.setAsJsonContent(true);
//        params.addHeader("Content-Type", "application/json");
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
        RequestParams params = ((BaseApplication) context.getApplicationContext()).getHttpRequestParams(completeUrl);
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


    /**
     * 扫一扫登录
     *
     * @param url
     */
    public void sendLoginDesktopCloudPlusInfo(final String url) {
        // final String completeUrl = APIUri.getLoginDesktopCloudPlusUrl();
        final String completeUrl = url;
        RequestParams params = BaseApplication.getInstance().getHttpRequestParams(completeUrl);
        HttpUtils.request(context, CloudHttpMethod.POST, params, new BaseModuleAPICallback(context, completeUrl) {
            @Override
            public void callbackSuccess(byte[] arg0) {
                apiInterface.returnLoginDesktopCloudPlusSuccess(new LoginDesktopCloudPlusBean());
            }

            @Override
            public void callbackFail(String error, int responseCode) {
                apiInterface.returnLoginDesktopCloudPlusFail(error, responseCode);
            }

            @Override
            public void callbackTokenExpire(long requestTime) {
                OauthCallBack oauthCallBack = new OauthCallBack() {
                    @Override
                    public void reExecute() {
                        sendLoginDesktopCloudPlusInfo(url);
                    }

                    @Override
                    public void executeFailCallback() {
                        callbackFail("", -1);
                    }
                };
                OauthUtils.getInstance().refreshToken(oauthCallBack, requestTime);
            }
        });
    }


    /**
     * 设备检查
     *
     * @param tenantId
     * @param userCode
     */
    public void deviceCheck(String tenantId, String userCode) {
        String completeUrl = LoginAPIUri.getDeviceCheckUrl();
        String uuid = AppUtils.getMyUUID(context);
        // RequestParams params = new RequestParams(completeUrl);
        // params.addBodyParameter("app_mdm_id", "imp"); // 和ios约定的appid
        RequestParams params = BaseApplication.getInstance().getHttpRequestParams(completeUrl);
        params.addBodyParameter("udid", uuid);
        params.addBodyParameter("tenant_id", tenantId);
        params.addBodyParameter("mdm_user_auth_type", "IDMUser");
        params.addBodyParameter("user_code", userCode);
        HttpUtils.request(context, CloudHttpMethod.POST, params, new BaseModuleAPICallback(context, completeUrl) {
            @Override
            public void callbackSuccess(byte[] arg0) {
                apiInterface.returnDeviceCheckSuccess(new GetDeviceCheckResult(new String(arg0)));
            }

            @Override
            public void callbackFail(String error, int responseCode) {
                apiInterface.returnDeviceCheckFail(error, responseCode);
            }

            @Override
            public void callbackTokenExpire(long requestTime) {
                apiInterface.returnDeviceCheckFail("", -1);
            }
        });
    }


    /**
     * 上传设备管理需要的一些信息
     */
    public void uploadMDMInfo() {
        final String completeUrl = LoginAPIUri.getUploadMDMInfoUrl();
        RequestParams params = BaseApplication.getInstance().getHttpRequestParams(completeUrl);
        params.addParameter("udid", AppUtils.getMyUUID(context));
        String refreshToken = PreferencesUtils.getString(context, "refreshToken", "");
        params.addParameter("refresh_token", refreshToken);
        HttpUtils.request(context, CloudHttpMethod.POST, params, new BaseModuleAPICallback(context, completeUrl) {
            @Override
            public void callbackSuccess(byte[] arg0) {
                apiInterface.returnUploadMDMInfoSuccess(new UploadMDMInfoResult(new String(arg0)));
            }

            @Override
            public void callbackFail(String error, int responseCode) {
                apiInterface.returnUploadMDMInfoFail();
            }


            @Override
            public void callbackTokenExpire(long requestTime) {
                OauthCallBack oauthCallBack = new OauthCallBack() {
                    @Override
                    public void reExecute() {
                        uploadMDMInfo();
                    }

                    @Override
                    public void executeFailCallback() {
                        callbackFail("", -1);
                    }
                };
                OauthUtils.getInstance().refreshToken(oauthCallBack, requestTime);
            }
        });
    }

}
