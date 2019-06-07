package com.inspur.emmcloud.basemodule.api;

import android.content.Context;

import com.inspur.emmcloud.baselib.util.romadaptation.RomInfoUtils;
import com.inspur.emmcloud.basemodule.application.BaseApplication;
import com.inspur.emmcloud.basemodule.bean.GetAllConfigVersionResult;
import com.inspur.emmcloud.basemodule.bean.GetLanguageResult;
import com.inspur.emmcloud.basemodule.bean.GetUploadPushInfoResult;
import com.inspur.emmcloud.basemodule.bean.PVCollectModel;
import com.inspur.emmcloud.basemodule.push.PushManagerUtils;
import com.inspur.emmcloud.basemodule.util.AppUtils;
import com.inspur.emmcloud.login.login.LoginService;
import com.inspur.emmcloud.login.login.OauthCallBack;
import com.luojilab.component.componentlib.router.Router;

import org.json.JSONObject;
import org.xutils.http.RequestParams;

import java.util.List;

/**
 * Created by chenmch on 2019/6/5.
 */

public class BaseModuleApiService {
    private Context context;
    private BaseModuleAPIInterface apiInterface;

    public BaseModuleApiService(Context context) {
        this.context = context;

    }

    public void setAPIInterface(BaseModuleAPIInterface apiInterface) {
        this.apiInterface = apiInterface;
    }

    private void refreshToken(OauthCallBack oauthCallBack, long requestTime) {
        Router router = Router.getInstance();
        if (router.getService(LoginService.class.getSimpleName()) != null) {
            LoginService service = (LoginService) router.getService(LoginService.class.getSimpleName());
            service.refreshToken(oauthCallBack, requestTime);
        }
    }


    /**
     * 手机应用PV信息（web应用）
     *
     * @param collectInfo
     */
    public void uploadPVCollect(String collectInfo, final List<PVCollectModel> collectModelList) {
        String completeUrl = BaseModuleApiUri.getUploadPVCollectUrl();
        RequestParams params = new RequestParams(completeUrl);
        params.setBodyContent(collectInfo);
        params.setAsJsonContent(true);
        HttpUtils.request(context, CloudHttpMethod.POST, params, new BaseModuleAPICallback(context, completeUrl) {
            @Override
            public void callbackSuccess(byte[] arg0) {
                apiInterface.returnUploadCollectSuccess(collectModelList);
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
     * 获取语言
     */
    public void getLanguage(final String languageConfigVersion) {
        final String completeUrl = BaseModuleApiUri.getLangUrl();
        RequestParams params = ((BaseApplication) context.getApplicationContext())
                .getHttpRequestParams(completeUrl);


        HttpUtils.request(context, CloudHttpMethod.GET, params, new BaseModuleAPICallback(context, completeUrl) {

            @Override
            public void callbackTokenExpire(long requestTime) {
                OauthCallBack oauthCallBack = new OauthCallBack() {
                    @Override
                    public void reExecute() {
                        getLanguage(languageConfigVersion);
                    }

                    @Override
                    public void executeFailCallback() {
                        callbackFail("", -1);
                    }
                };
                Router router = Router.getInstance();
                if (router.getService(LoginService.class.getSimpleName()) != null) {
                    LoginService service = (LoginService) router.getService(LoginService.class.getSimpleName());
                    service.refreshToken(oauthCallBack, requestTime);
                }
            }

            @Override
            public void callbackSuccess(byte[] arg0) {
                // TODO Auto-generated method stub
                apiInterface.returnLanguageSuccess(new GetLanguageResult(new String(arg0)), languageConfigVersion);
            }

            @Override
            public void callbackFail(String error, int responseCode) {
                // TODO Auto-generated method stub
                apiInterface.returnLanguageFail(error, responseCode);
            }
        });
    }


    /**
     * app通用检查更新
     *
     * @param
     */
    public void getAllConfigVersion(final JSONObject clientConfigVersionObj) {
        final String url = BaseModuleApiUri.getAllConfigVersionUrl();
        RequestParams params = BaseApplication.getInstance().getHttpRequestParams(url);
        JSONObject object = new JSONObject();
        try {
            object.put("os", "Android");
            object.put("osVersion", AppUtils.getReleaseVersion());
            object.put("appId", context.getPackageName());
            object.put("appVersion", AppUtils.getVersion(context));
            object.put("appCoreVersion", AppUtils.getVersion(context));
            object.put("ClientConfigVersions", clientConfigVersionObj);
        } catch (Exception e) {
            e.printStackTrace();
        }
        params.setBodyContent(object.toString());
        params.setAsJsonContent(true);
        HttpUtils.request(context, CloudHttpMethod.POST, params, new BaseModuleAPICallback(context, url) {
            @Override
            public void callbackSuccess(byte[] arg0) {
                apiInterface.returnAllConfigVersionSuccess(
                        new GetAllConfigVersionResult(new String(arg0), clientConfigVersionObj));
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
                refreshToken(oauthCallBack, requestTime);
            }
        });
    }

    /**
     * 上传推送相关信息
     *
     * @param deviceId
     * @param deviceName
     * @param pushProvider
     * @param pushTracer
     */
    public void uploadPushInfo(final String deviceId, final String deviceName, final String pushProvider, final String pushTracer) {
        final String url = BaseModuleApiUri.getUploadPushInfoUrl();
        RequestParams params = BaseApplication.getInstance().getHttpRequestParams(url);
        params.addParameter("deviceId", deviceId);
        params.addParameter("deviceName", deviceName);
        params.addParameter("notificationProvider", pushProvider);
        params.addParameter("notificationTracer", pushTracer);
        HttpUtils.request(context, CloudHttpMethod.POST, params, new BaseModuleAPICallback(context, url) {
            @Override
            public void callbackSuccess(byte[] arg0) {
                apiInterface.returnUploadPushInfoResultSuccess(new GetUploadPushInfoResult(new String(arg0)));
            }

            @Override
            public void callbackFail(String error, int responseCode) {
                apiInterface.returnUploadPushInfoResultFail(error, responseCode);
            }

            @Override
            public void callbackTokenExpire(long requestTime) {
                OauthCallBack oauthCallBack = new OauthCallBack() {
                    @Override
                    public void reExecute() {
                        uploadPushInfo(deviceId, deviceName, pushProvider, pushTracer);
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
     * 登录、切换企业和推送token发生变化时调用解除推送token
     * 不关心服务端返回
     */
    public void registerPushToken() {
        String url = BaseModuleApiUri.getRegisterPushTokenUrl();
        RequestParams params = BaseApplication.getInstance().getHttpRequestParams(url);
        JSONObject registerPushTokenJsonObject = new JSONObject();
        try {
            registerPushTokenJsonObject.put("deviceId", AppUtils.getMyUUID(context));
            registerPushTokenJsonObject.put("appId", context.getPackageName());
            registerPushTokenJsonObject.put("appVersion", AppUtils.getVersion(context));
            registerPushTokenJsonObject.put("type", PushManagerUtils.getInstance().getPushProvider(context));
            registerPushTokenJsonObject.put("token", PushManagerUtils.getInstance().getPushId(context));
            registerPushTokenJsonObject.put("inspurId", BaseApplication.getInstance().getUid());
            registerPushTokenJsonObject.put("tenantId", BaseApplication.getInstance().getCurrentEnterprise().getId());
            registerPushTokenJsonObject.put("deviceModel", AppUtils.GetChangShang() + "/" + AppUtils.GetModel());
            registerPushTokenJsonObject.put("deviceOS", "Android");
            registerPushTokenJsonObject.put("deviceOSVersion", AppUtils.getReleaseVersion());
            registerPushTokenJsonObject.put("romInfo",
                    RomInfoUtils.getRomNameInfo() + "/" + RomInfoUtils.getRomVersionInfo());
        } catch (Exception e) {
            e.printStackTrace();
        }
        params.setBodyContent(registerPushTokenJsonObject.toString());
        params.setAsJsonContent(true);
        HttpUtils.request(context, CloudHttpMethod.POST, params, new BaseModuleAPICallback(context, url) {
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
                        registerPushToken();
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
     * 注销时调用解除推送token
     * 不关心服务端返回
     */
    public void unregisterPushToken() {
        String url = BaseModuleApiUri.getUnRegisterPushTokenUrl();
        RequestParams params = BaseApplication.getInstance().getHttpRequestParams(url);
        final JSONObject unregisterPushTokenJsonObject = new JSONObject();
        try {
            unregisterPushTokenJsonObject.put("deviceId", AppUtils.getMyUUID(context));
            unregisterPushTokenJsonObject.put("appId", context.getPackageName());
            unregisterPushTokenJsonObject.put("appVersion", AppUtils.getVersion(context));
            unregisterPushTokenJsonObject.put("type", PushManagerUtils.getInstance().getPushProvider(context));
            unregisterPushTokenJsonObject.put("token", PushManagerUtils.getInstance().getPushId(context));
            unregisterPushTokenJsonObject.put("inspurId", BaseApplication.getInstance().getUid());
            unregisterPushTokenJsonObject.put("tenantId", BaseApplication.getInstance().getCurrentEnterprise().getId());
            unregisterPushTokenJsonObject.put("deviceModel", AppUtils.GetChangShang() + "/" + AppUtils.GetModel());
            unregisterPushTokenJsonObject.put("deviceOS", "Android");
            unregisterPushTokenJsonObject.put("deviceOSVersion", AppUtils.getReleaseVersion());
            unregisterPushTokenJsonObject.put("romInfo",
                    RomInfoUtils.getRomNameInfo() + "/" + RomInfoUtils.getRomVersionInfo());
        } catch (Exception e) {
            e.printStackTrace();
        }
        params.setBodyContent(unregisterPushTokenJsonObject.toString());
        params.setAsJsonContent(true);
        HttpUtils.request(context, CloudHttpMethod.POST, params, new BaseModuleAPICallback(context, url) {
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
                        unregisterPushToken();
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
