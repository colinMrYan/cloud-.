/**
 * MyAppAPIService.java
 * classes : com.inspur.emmcloud.api.apiservice.MyAppAPIService
 * V 1.0.0
 * Create at 2016年11月8日 下午2:31:55
 */
package com.inspur.emmcloud.api.apiservice;

import android.content.Context;

import com.inspur.emmcloud.MyApplication;
import com.inspur.emmcloud.api.APICallback;
import com.inspur.emmcloud.api.APIInterface;
import com.inspur.emmcloud.api.APIUri;
import com.inspur.emmcloud.api.CloudHttpMethod;
import com.inspur.emmcloud.api.HttpUtils;
import com.inspur.emmcloud.bean.appcenter.GetClientIdRsult;
import com.inspur.emmcloud.bean.appcenter.ReactNativeUpdateBean;
import com.inspur.emmcloud.bean.login.GetDeviceCheckResult;
import com.inspur.emmcloud.bean.login.LoginDesktopCloudPlusBean;
import com.inspur.emmcloud.bean.system.GetAppConfigResult;
import com.inspur.emmcloud.bean.system.GetAppMainTabResult;
import com.inspur.emmcloud.bean.system.GetAllConfigVersionResult;
import com.inspur.emmcloud.bean.system.GetUpgradeResult;
import com.inspur.emmcloud.bean.system.SplashPageBean;
import com.inspur.emmcloud.interf.OauthCallBack;
import com.inspur.emmcloud.util.common.PreferencesUtils;
import com.inspur.emmcloud.util.privates.AppUtils;
import com.inspur.emmcloud.util.privates.OauthUtils;

import org.json.JSONObject;
import org.xutils.http.RequestParams;


/**
 * com.inspur.emmcloud.api.apiservice.MyAppAPIService create at 2016年11月8日
 * 下午2:31:55
 */
public class AppAPIService {
    private Context context;
    private APIInterface apiInterface;

    public AppAPIService(Context context) {
        this.context = context;

    }

    public void setAPIInterface(APIInterface apiInterface) {
        this.apiInterface = apiInterface;
    }

    /**
     * 获取版本更新信息
     *
     * @param isManualCheck shi
     */
    public void checkUpgrade(final boolean isManualCheck) {
        String completeUrl = APIUri.checkUpgrade();
        String clientVersion = AppUtils.getVersion(context);
        RequestParams params = ((MyApplication) context.getApplicationContext())
                .getHttpRequestParams(completeUrl);
        params.addParameter("clientVersion", clientVersion);
        if (((MyApplication) context.getApplicationContext()).isVersionDev()) {
            params.addParameter("clientType", "dev_android");
        } else {
            if (AppUtils.isAppVersionStandard()){
                params.addParameter("clientType", "android");
            }else {
                String appVersionFlag = AppUtils.getAppVersionFlag(context);
                params.addParameter("clientType", "android_"+appVersionFlag);
            }

        }

        HttpUtils.request(context,CloudHttpMethod.POST,params,new APICallback(context, completeUrl) {
            @Override
            public void callbackTokenExpire(long requestTime) {
                // TODO Auto-generated method stub
                apiInterface.returnUpgradeFail(new String(""), isManualCheck, -1);
            }

            @Override
            public void callbackSuccess(byte[] arg0) {
                // TODO Auto-generated method stub
                apiInterface.returnUpgradeSuccess(new GetUpgradeResult(new String(arg0)), isManualCheck);
            }

            @Override
            public void callbackFail(String error, int responseCode) {
                // TODO Auto-generated method stub
                apiInterface.returnUpgradeFail(error, isManualCheck, responseCode);
            }
        });
    }

    /**
     * 获取ClientId
     *
     * @param deviceId
     * @param deviceName
     */
    public void getClientId(final String deviceId, final String deviceName) {
        final String completeUrl = APIUri.getClientId();
        RequestParams params = ((MyApplication) context.getApplicationContext())
                .getHttpRequestParams(completeUrl);
        params.addParameter("deviceId", deviceId);
        params.addParameter("deviceName", deviceName);
        HttpUtils.request(context,CloudHttpMethod.POST,params,new APICallback(context, completeUrl) {
            @Override
            public void callbackSuccess(byte[] arg0) {
                apiInterface.returnGetClientIdResultSuccess(new GetClientIdRsult(new String(arg0)));
            }

            @Override
            public void callbackFail(String error, int responseCode) {
                apiInterface.returnGetClientIdResultFail(error, responseCode);
            }

            @Override
            public void callbackTokenExpire(long requestTime) {
                OauthUtils.getInstance().refreshToken(
                        new OauthCallBack() {
                            @Override
                            public void reExecute() {
                                getClientId(deviceId, deviceName);
                            }

                            @Override
                            public void executeFailCallback() {
                                callbackFail("", -1);
                            }
                        }, requestTime);
            }
        });
    }


    /**
     * 获取ReactNative更新版本
     *
     * @param version
     * @param lastCreationDate
     */
    public void getReactNativeUpdate(final String version, final long lastCreationDate, final String clientId) {
        final String completeUrl = APIUri.getReactNativeUpdate() + "version=" + version + "&lastCreationDate=" + lastCreationDate
                + "&clientId=" + clientId;
        RequestParams params = ((MyApplication) context.getApplicationContext())
                .getHttpRequestParams(completeUrl);
        HttpUtils.request(context,CloudHttpMethod.GET,params,new APICallback(context, completeUrl) {
            @Override
            public void callbackSuccess(byte[] arg0) {
                apiInterface.returnReactNativeUpdateSuccess(new ReactNativeUpdateBean(new String(arg0)));
            }

            @Override
            public void callbackFail(String error, int responseCode) {
                apiInterface.returnReactNativeUpdateFail(error, responseCode);
            }


            @Override
            public void callbackTokenExpire(long requestTime) {
                OauthUtils.getInstance().refreshToken(
                        new OauthCallBack() {
                            @Override
                            public void reExecute() {
                                getReactNativeUpdate(version, lastCreationDate, clientId);
                            }

                            @Override
                            public void executeFailCallback() {
                                callbackFail("", -1);
                            }
                        }, requestTime);
            }
        });
    }


    /**
     * 回写ReactNative日志接口
     *
     * @param command
     * @param version
     * @param clientId
     */
    public void sendBackReactNativeUpdateLog(final String command, final String version, final String clientId) {
        final String completeUrl = APIUri.getClientLog() + "command=" + command + "&version=" + version
                + "&clientId=" + clientId;
        RequestParams params = ((MyApplication) context.getApplicationContext())
                .getHttpRequestParams(completeUrl);
        HttpUtils.request(context,CloudHttpMethod.PUT,params,new APICallback(context, completeUrl) {
            @Override
            public void callbackSuccess(byte[] arg0) {
            }

            @Override
            public void callbackFail(String error, int responseCode) {
            }

            @Override
            public void callbackTokenExpire(long requestTime) {
                OauthCallBack oauthCallBack = new OauthCallBack() {
                    @Override
                    public void reExecute() {
                        sendBackReactNativeUpdateLog(command, version, clientId);
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
     * 异常上传
     *
     * @param exception
     */
    public void uploadException(final JSONObject exception) {
        final String completeUrl = APIUri.uploadException();
        RequestParams params = ((MyApplication) context.getApplicationContext())
                .getHttpRequestParams(completeUrl);
        params.setAsJsonContent(true);
        params.setBodyContent(exception.toString());
        HttpUtils.request(context,CloudHttpMethod.POST,params,new APICallback(context, completeUrl) {

            @Override
            public void callbackTokenExpire(long requestTime) {
                // TODO Auto-generated method stub
                apiInterface.returnUploadExceptionFail(new String(""), -1);
            }

            @Override
            public void callbackSuccess(byte[] arg0) {
                // TODO Auto-generated method stub
                apiInterface
                        .returnUploadExceptionSuccess();
            }

            @Override
            public void callbackFail(String error, int responseCode) {
                // TODO Auto-generated method stub
                apiInterface.returnUploadExceptionFail(error, responseCode);
            }
        });
    }




    /**
     * 获取显示tab页的接口
     */
    public void getAppNewTabs(final String version, final String clientId,final String mainTabSaveConfigVersion) {
        final String completeUrl = APIUri.getAppNewTabs() + "?version=" + version + "&clientId=" + clientId;
        RequestParams params = ((MyApplication) context.getApplicationContext())
                .getHttpRequestParams(completeUrl);
        HttpUtils.request(context,CloudHttpMethod.GET,params,new APICallback(context, completeUrl) {
            @Override
            public void callbackTokenExpire(long requestTime) {
                OauthCallBack oauthCallBack = new OauthCallBack() {
                    @Override
                    public void reExecute() {
                        getAppNewTabs(version, clientId,mainTabSaveConfigVersion);
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
                apiInterface.returnAppTabAutoSuccess(new GetAppMainTabResult(new String(arg0)),mainTabSaveConfigVersion);
            }

            @Override
            public void callbackFail(String error, int responseCode) {
                apiInterface.returnAppTabAutoFail(error, responseCode);
            }
        });
    }


    /**
     * 手机应用PV信息（web应用）
     *
     * @param collectInfo
     */
    public void uploadPVCollect(String collectInfo) {
        String completeUrl = "http://u.inspur.com/analytics/api/v1.0/ECMClientPV/Post";
        RequestParams params = new RequestParams(completeUrl);
        params.setBodyContent(collectInfo);
        params.setAsJsonContent(true);
        HttpUtils.request(context,CloudHttpMethod.POST,params,new APICallback(context, completeUrl) {
            @Override
            public void callbackSuccess(byte[] arg0) {
                apiInterface.returnUploadCollectSuccess();
            }

            @Override
            public void callbackFail(String error, int responseCode) {
                apiInterface.returnUploadCollectFail(error, responseCode);
            }

            @Override
            public void callbackTokenExpire(long requestTime) {
                callbackFail("", -1);
            }
        });
    }

    /**
     * 验证行政审批密码
     *
     * @param password
     */
    public void veriryApprovalPassword(String userName, final String password) {
        String completeUrl = APIUri.getVeriryApprovalPasswordUrl();
        RequestParams params = new RequestParams(completeUrl);
        params.addQueryStringParameter("userName", userName);
        params.addQueryStringParameter("userPass", password);
        HttpUtils.request(context,CloudHttpMethod.GET,params,new APICallback(context, completeUrl) {
            @Override
            public void callbackSuccess(byte[] arg0) {
                if (arg0 != null && new String(arg0).equals("登录成功")) {
                    apiInterface.returnVeriryApprovalPasswordSuccess(password);
                } else {
                    apiInterface.returnVeriryApprovalPasswordFail("", -1);
                }

            }

            @Override
            public void callbackFail(String error, int responseCode) {
                apiInterface.returnVeriryApprovalPasswordFail(error, responseCode);
            }

            @Override
            public void callbackTokenExpire(long requestTime) {
                apiInterface.returnVeriryApprovalPasswordFail("", -1);
            }
        });
    }

    /**
     * 上传设备管理需要的一些信息
     */
    public void uploadMDMInfo() {
        final String completeUrl = APIUri.getUploadMDMInfoUrl();
        RequestParams params = ((MyApplication) context.getApplicationContext())
                .getHttpRequestParams(completeUrl);
        params.addParameter("udid", AppUtils.getMyUUID(context));
        String refreshToken = PreferencesUtils.getString(context, "refreshToken", "");
        params.addParameter("refresh_token", refreshToken);
        HttpUtils.request(context,CloudHttpMethod.POST,params,new APICallback(context, completeUrl) {
            @Override
            public void callbackSuccess(byte[] arg0) {
            }

            @Override
            public void callbackFail(String error, int responseCode) {
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
                OauthUtils.getInstance().refreshToken(
                        oauthCallBack, requestTime);
            }
        });
    }

    /**
     * 获取闪屏页信息
     * 采用新式数据解析方法
     *
     * @param clientId
     * @param versionCode
     */
    public void getSplashPageInfo(final String clientId, final String versionCode) {
        final String completeUrl = APIUri.getSplashPageUrl() + "?version=" + versionCode + "&clientId=" + clientId;
        RequestParams params = ((MyApplication) context.getApplicationContext())
                .getHttpRequestParams(completeUrl);
        HttpUtils.request(context,CloudHttpMethod.GET,params, new APICallback(context, completeUrl) {
            @Override
            public void callbackSuccess(byte[] arg0) {
                apiInterface.returnSplashPageInfoSuccess(new SplashPageBean(new String(arg0)));
            }

            @Override
            public void callbackFail(String error, int responseCode) {
                apiInterface.returnSplashPageInfoFail(error, responseCode);
            }

            @Override
            public void callbackTokenExpire(long requestTime) {
                OauthCallBack oauthCallBack = new OauthCallBack() {
                    @Override
                    public void reExecute() {
                        getSplashPageInfo(clientId, versionCode);
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
//		final String completeUrl = APIUri.getLoginDesktopCloudPlusUrl();
        final String completeUrl = url;
        RequestParams params = ((MyApplication) context.getApplicationContext())
                .getHttpRequestParams(completeUrl);
        HttpUtils.request(context,CloudHttpMethod.POST,params,new APICallback(context, completeUrl) {
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
                OauthUtils.getInstance().refreshToken(
                        oauthCallBack, requestTime);
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
        // TODO Auto-generated method stub
        String completeUrl = APIUri.getDeviceCheckUrl();
        String uuid = AppUtils.getMyUUID(context);
//        RequestParams params = new RequestParams(completeUrl);
        // params.addBodyParameter("app_mdm_id", "imp"); // 和ios约定的appid
        RequestParams params = ((MyApplication) context.getApplicationContext())
                .getHttpRequestParams(completeUrl);
        params.addBodyParameter("udid", uuid);
        params.addBodyParameter("tenant_id", tenantId);
        params.addBodyParameter("mdm_user_auth_type", "IDMUser");
        params.addBodyParameter("user_code", userCode);
        HttpUtils.request(context,CloudHttpMethod.POST,params,new APICallback(context, completeUrl) {
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
     * 获取应用的配置信息
     */
    public void getAppConfig(final boolean isGetCommonAppConfig,final boolean isGetWorkPortletAppConfig,final boolean isGetWebAutoRotate) {
        final String url = APIUri.getAppConfigUrl(isGetCommonAppConfig,isGetWorkPortletAppConfig,isGetWebAutoRotate);
        RequestParams params = ((MyApplication) context.getApplicationContext()).getHttpRequestParams(url);
        HttpUtils.request(context,CloudHttpMethod.GET,params,new APICallback(context, url) {
            @Override
            public void callbackSuccess(byte[] arg0) {
                apiInterface.returnAppConfigSuccess(new GetAppConfigResult(new String(arg0)));
            }

            @Override
            public void callbackFail(String error, int responseCode) {
                apiInterface.returnAppConfigFail(error, responseCode);
            }

            @Override
            public void callbackTokenExpire(long requestTime) {
                OauthCallBack oauthCallBack = new OauthCallBack() {
                    @Override
                    public void reExecute() {
                        getAppConfig(isGetCommonAppConfig,isGetWorkPortletAppConfig,isGetWebAutoRotate);
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
     * 保存webview是否自动旋转配置项
     *
     * @param isWebAutoRotate
     */
    public void saveWebAutoRotateConfig(final boolean isWebAutoRotate) {
        final String url = APIUri.saveAppConfigUrl("WebAutoRotate");
        RequestParams params = ((MyApplication) context.getApplicationContext()).getHttpRequestParams(url);
        params.setBodyContent(isWebAutoRotate + "");
        HttpUtils.request(context,CloudHttpMethod.POST,params,new APICallback(context, url) {
            @Override
            public void callbackSuccess(byte[] arg0) {
               // apiInterface.returnSaveWebAutoRotateConfigSuccess(isWebAutoRotate);
            }

            @Override
            public void callbackFail(String error, int responseCode) {
              //  apiInterface.returnSaveWebAutoRotateConfigFail(error, responseCode);
            }

            @Override
            public void callbackTokenExpire(long requestTime) {
                OauthCallBack oauthCallBack = new OauthCallBack() {
                    @Override
                    public void reExecute() {
                        saveWebAutoRotateConfig(isWebAutoRotate);
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
     * 上传位置信息
     *
     * @param positionJson
     */
    public void uploadPosition(final String positionJson) {
        final String url = APIUri.getUploadPositionUrl();
        RequestParams params = ((MyApplication) context.getApplicationContext()).getHttpRequestParams(url);
        params.setBodyContent(positionJson);
        params.setAsJsonContent(true);
        HttpUtils.request(context,CloudHttpMethod.POST,params,new APICallback(context, url) {
            @Override
            public void callbackSuccess(byte[] arg0) {
                apiInterface.returnUploadPositionSuccess();
            }

            @Override
            public void callbackFail(String error, int responseCode) {
                apiInterface.returnUploadPositionSuccess();
            }

            @Override
            public void callbackTokenExpire(long requestTime) {
                OauthCallBack oauthCallBack = new OauthCallBack() {
                    @Override
                    public void reExecute() {
                        uploadPosition(positionJson);
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
     * app通用检查更新
     *
     * @param positionJson
     */
    public void getAllConfigVersion(final JSONObject clientConfigVersionObj) {
        final String url = APIUri.getAllConfigVersionUrl();
        RequestParams params = MyApplication.getInstance().getHttpRequestParams(url);
        JSONObject object = new JSONObject();
        try {
            object.put("os","Android");
            object.put("osVersion",AppUtils.getReleaseVersion());
            object.put("appId",context.getPackageName());
            object.put("appVersion",AppUtils.getVersion(context));
            object.put("appCoreVersion",AppUtils.getVersion(context));
            object.put("ClientConfigVersions",clientConfigVersionObj);
        }catch (Exception e){
            e.printStackTrace();
        }
        params.setBodyContent(object.toString());
        params.setAsJsonContent(true);
        HttpUtils.request(context,CloudHttpMethod.POST,params,new APICallback(context, url) {
            @Override
            public void callbackSuccess(byte[] arg0) {
                apiInterface.returnAllConfigVersionSuccess(new GetAllConfigVersionResult(new String(arg0),clientConfigVersionObj));
            }

            @Override
            public void callbackFail(String error, int responseCode) {
                apiInterface.returnAllConfigVersionFail(error, responseCode);
            }

            @Override
            public void callbackTokenExpire(long requestTime) {
                OauthCallBack oauthCallBack = new OauthCallBack() {
                    @Override
                    public void reExecute() {
                        getAllConfigVersion(clientConfigVersionObj);
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
