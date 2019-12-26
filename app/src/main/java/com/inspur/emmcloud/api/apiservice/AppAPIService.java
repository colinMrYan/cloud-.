/**
 * MyAppAPIService.java
 * classes : com.inspur.emmcloud.api.apiservice.MyAppAPIService
 * V 1.0.0
 * Create at 2016年11月8日 下午2:31:55
 */
package com.inspur.emmcloud.api.apiservice;

import android.content.Context;

import com.inspur.emmcloud.MyApplication;
import com.inspur.emmcloud.api.APIInterface;
import com.inspur.emmcloud.api.APIUri;
import com.inspur.emmcloud.baselib.router.Router;
import com.inspur.emmcloud.baselib.util.PreferencesUtils;
import com.inspur.emmcloud.basemodule.api.BaseModuleAPICallback;
import com.inspur.emmcloud.basemodule.api.CloudHttpMethod;
import com.inspur.emmcloud.basemodule.api.HttpUtils;
import com.inspur.emmcloud.basemodule.app.maintab.GetAppMainTabResult;
import com.inspur.emmcloud.basemodule.app.navibar.NaviBarModel;
import com.inspur.emmcloud.basemodule.config.Constant;
import com.inspur.emmcloud.basemodule.util.AppUtils;
import com.inspur.emmcloud.bean.appcenter.GetClientIdRsult;
import com.inspur.emmcloud.bean.system.GetAppConfigResult;
import com.inspur.emmcloud.bean.system.GetUpgradeResult;
import com.inspur.emmcloud.bean.system.SplashPageBean;
import com.inspur.emmcloud.componentservice.login.LoginService;
import com.inspur.emmcloud.componentservice.login.OauthCallBack;
import com.inspur.reactnative.bean.ReactNativeUpdateBean;

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

    private void refreshToken(OauthCallBack oauthCallBack, long requestTime) {
        Router router = Router.getInstance();
        if (router.getService(LoginService.class) != null) {
            LoginService service = router.getService(LoginService.class);
            service.refreshToken(oauthCallBack, requestTime);
        }
    }


    /**
     * 获取版本更新信息
     *
     * @param isManualCheck shi
     */
    public void checkUpgrade(final boolean isManualCheck) {
        String completeUrl = APIUri.checkUpgrade();
        String clientVersion = AppUtils.getVersion(context);
        RequestParams params = ((MyApplication) context.getApplicationContext()).getHttpRequestParams(completeUrl);
        params.addParameter("clientVersion", clientVersion);
        if (AppUtils.isAppVersionStandard()) {
            params.addParameter("clientType", "android");
        } else {
            String appFirstLoadAlis =
                    PreferencesUtils.getString(MyApplication.getInstance(), Constant.PREF_APP_LOAD_ALIAS);
            params.addParameter("clientType", "android_" + appFirstLoadAlis);
        }
        HttpUtils.request(context, CloudHttpMethod.POST, params, new BaseModuleAPICallback(context, completeUrl) {
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
        RequestParams params = ((MyApplication) context.getApplicationContext()).getHttpRequestParams(completeUrl);
        params.addParameter("deviceId", deviceId);
        params.addParameter("deviceName", deviceName);
        HttpUtils.request(context, CloudHttpMethod.POST, params, new BaseModuleAPICallback(context, completeUrl) {
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
                refreshToken(new OauthCallBack() {
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
        final String completeUrl = APIUri.getReactNativeUpdate() + "version=" + version + "&lastCreationDate="
                + lastCreationDate + "&clientId=" + clientId;
        RequestParams params = ((MyApplication) context.getApplicationContext()).getHttpRequestParams(completeUrl);
        HttpUtils.request(context, CloudHttpMethod.GET, params, new BaseModuleAPICallback(context, completeUrl) {
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
                refreshToken(new OauthCallBack() {
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
        final String completeUrl =
                APIUri.getClientLog() + "command=" + command + "&version=" + version + "&clientId=" + clientId;
        RequestParams params = ((MyApplication) context.getApplicationContext()).getHttpRequestParams(completeUrl);
        HttpUtils.request(context, CloudHttpMethod.PUT, params, new BaseModuleAPICallback(context, completeUrl) {
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
                refreshToken(oauthCallBack, requestTime);
            }
        });
    }



    /**
     * 获取显示tab页的接口
     */
    public void getAppNewTabs(final String version, final String clientId, final String mainTabSaveConfigVersion) {
        final String completeUrl = APIUri.getAppNewTabs() + "?version=" + version + "&clientId=" + clientId;
        RequestParams params = ((MyApplication) context.getApplicationContext()).getHttpRequestParams(completeUrl);
        HttpUtils.request(context, CloudHttpMethod.GET, params, new BaseModuleAPICallback(context, completeUrl) {
            @Override
            public void callbackTokenExpire(long requestTime) {
                OauthCallBack oauthCallBack = new OauthCallBack() {
                    @Override
                    public void reExecute() {
                        getAppNewTabs(version, clientId, mainTabSaveConfigVersion);
                    }

                    @Override
                    public void executeFailCallback() {
                        callbackFail("", -1);
                    }
                };
                refreshToken(oauthCallBack, requestTime);
            }

            @Override
            public void callbackSuccess(byte[] arg0) {
                apiInterface.returnAppTabAutoSuccess(new GetAppMainTabResult(new String(arg0)),
                        mainTabSaveConfigVersion);
            }

            @Override
            public void callbackFail(String error, int responseCode) {
                apiInterface.returnAppTabAutoFail(error, responseCode);
            }
        });
    }


    /**
     * 获取显示tab页的接口
     */
    public void getAppNaviTabs(final String lastMultipleLayoutVersion) {
        final String completeUrl = APIUri.getAppNaviTabs();
        RequestParams params = ((MyApplication) context.getApplicationContext()).getHttpRequestParams(completeUrl);
        HttpUtils.request(context, CloudHttpMethod.GET, params, new BaseModuleAPICallback(context, completeUrl) {
            @Override
            public void callbackTokenExpire(long requestTime) {
                OauthCallBack oauthCallBack = new OauthCallBack() {
                    @Override
                    public void reExecute() {

                    }

                    @Override
                    public void executeFailCallback() {
                        callbackFail("", -1);
                    }
                };
                refreshToken(oauthCallBack, requestTime);
            }

            @Override
            public void callbackSuccess(byte[] arg0) {
                apiInterface.returnNaviBarModelSuccess(new NaviBarModel(new String(arg0), lastMultipleLayoutVersion));
            }

            @Override
            public void callbackFail(String error, int responseCode) {
                apiInterface.returnNaviBarModelFail(error, responseCode);
            }
        });
    }

//    /**
//     * 手机应用PV信息（web应用）
//     *
//     * @param collectInfo
//     */
//    public void uploadPVCollect(String collectInfo, final List<PVCollectModel> collectModelList) {
//        String completeUrl = APIUri.getUploadPVCollectUrl();
//        RequestParams params = new RequestParams(completeUrl);
//        params.setBodyContent(collectInfo);
//        params.setAsJsonContent(true);
//        HttpUtils.request(context, CloudHttpMethod.POST, params, new APICallback(context, completeUrl) {
//            @Override
//            public void callbackSuccess(byte[] arg0) {
//                apiInterface.returnUploadCollectSuccess(collectModelList);
//            }
//
//            @Override
//            public void callbackFail(String error, int responseCode) {
//                apiInterface.returnUploadCollectFail(error, responseCode);
//            }
//
//            @Override
//            public void callbackTokenExpire(long requestTime) {
//                callbackFail("", -1);
//            }
//        });
//    }

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
        HttpUtils.request(context, CloudHttpMethod.GET, params, new BaseModuleAPICallback(context, completeUrl) {
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
     * 获取闪屏页信息
     * 采用新式数据解析方法
     *
     * @param clientId
     * @param versionCode
     */
    public void getSplashPageInfo(final String clientId, final String versionCode) {
        final String completeUrl = APIUri.getSplashPageUrl() + "?version=" + versionCode + "&clientId=" + clientId;
        RequestParams params = ((MyApplication) context.getApplicationContext()).getHttpRequestParams(completeUrl);
        HttpUtils.request(context, CloudHttpMethod.GET, params, new BaseModuleAPICallback(context, completeUrl) {
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
                refreshToken(oauthCallBack, requestTime);
            }

        });
    }


    /**
     * 获取应用的配置信息
     */
    public void getAppConfig(final boolean isGetCommonAppConfig, final boolean isGetWebAutoRotate) {
        final String url = APIUri.getAppConfigUrl(isGetCommonAppConfig, isGetWebAutoRotate);
        RequestParams params = ((MyApplication) context.getApplicationContext()).getHttpRequestParams(url);
        HttpUtils.request(context, CloudHttpMethod.GET, params, new BaseModuleAPICallback(context, url) {
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
                        getAppConfig(isGetCommonAppConfig, isGetWebAutoRotate);
                    }

                    @Override
                    public void executeFailCallback() {
                        callbackFail("", -1);
                    }
                };
                refreshToken(oauthCallBack, requestTime);
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
        HttpUtils.request(context, CloudHttpMethod.POST, params, new BaseModuleAPICallback(context, url) {
            @Override
            public void callbackSuccess(byte[] arg0) {
                // apiInterface.returnSaveWebAutoRotateConfigSuccess(isWebAutoRotate);
            }

            @Override
            public void callbackFail(String error, int responseCode) {
                // apiInterface.returnSaveWebAutoRotateConfigFail(error, responseCode);
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
                refreshToken(oauthCallBack, requestTime);
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
        HttpUtils.request(context, CloudHttpMethod.POST, params, new BaseModuleAPICallback(context, url) {
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
                refreshToken(oauthCallBack, requestTime);
            }
        });
    }




}
