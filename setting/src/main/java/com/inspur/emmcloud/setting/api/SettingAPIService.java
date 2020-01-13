package com.inspur.emmcloud.setting.api;

import android.content.Context;

import com.inspur.emmcloud.baselib.router.Router;
import com.inspur.emmcloud.baselib.util.PreferencesUtils;
import com.inspur.emmcloud.basemodule.api.BaseModuleAPICallback;
import com.inspur.emmcloud.basemodule.api.CloudHttpMethod;
import com.inspur.emmcloud.basemodule.api.HttpUtils;
import com.inspur.emmcloud.basemodule.application.BaseApplication;
import com.inspur.emmcloud.basemodule.util.AppUtils;
import com.inspur.emmcloud.componentservice.login.LoginService;
import com.inspur.emmcloud.componentservice.login.OauthCallBack;
import com.inspur.emmcloud.setting.bean.GetBindingDeviceResult;
import com.inspur.emmcloud.setting.bean.GetCardPackageResult;
import com.inspur.emmcloud.setting.bean.GetDeviceLogResult;
import com.inspur.emmcloud.setting.bean.GetExperienceUpgradeFlagResult;
import com.inspur.emmcloud.setting.bean.GetFaceSettingResult;
import com.inspur.emmcloud.setting.bean.GetMDMStateResult;
import com.inspur.emmcloud.setting.bean.GetUploadMyHeadResult;
import com.inspur.emmcloud.setting.bean.GetUserCardMenusResult;
import com.inspur.emmcloud.setting.bean.SettingGetBoolenResult;
import com.inspur.emmcloud.setting.bean.UserProfileInfoBean;

import org.xutils.http.RequestParams;

import java.io.File;

/**
 * Created by libaochao on 2019/12/25.
 */

public class SettingAPIService {
    private Context context;
    private SettingAPIInterface apiInterface;

    public SettingAPIService(Context context) {
        this.context = context;
    }

    public void setAPIInterface(SettingAPIInterface apiInterface) {
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
     * 修改用户头像
     *
     * @param
     */
    public void updateUserHead(final String filePath) {

        final String completeUrl = SettingAPIUri.getUpdateUserHeadUrl();
        RequestParams params = ((BaseApplication) context.getApplicationContext())
                .getHttpRequestParams(completeUrl);
        File file = new File(filePath);
        params.setMultipart(true);// 有上传文件时使用multipart表单, 否则上传原始文件流.
        params.addBodyParameter("head", file);
        HttpUtils.request(context, CloudHttpMethod.POST, params, new BaseModuleAPICallback(context, completeUrl) {

            @Override
            public void callbackTokenExpire(long requestTime) {
                OauthCallBack oauthCallBack = new OauthCallBack() {
                    @Override
                    public void reExecute() {
                        updateUserHead(filePath);
                    }

                    @Override
                    public void executeFailCallback() {
                        callbackFail("", -1);
                    }
                };
                refreshToken(
                        oauthCallBack, requestTime);
            }

            @Override
            public void callbackSuccess(byte[] arg0) {
                // TODO Auto-generated method stub
                apiInterface
                        .returnUploadMyHeadSuccess(new GetUploadMyHeadResult(new String(arg0)), filePath);
            }

            @Override
            public void callbackFail(String error, int responseCode) {
                // TODO Auto-generated method stub
                apiInterface.returnUploadMyHeadFail(error, responseCode);
            }
        });

    }

    /**
     * 修改用户信息
     *
     * @param key
     * @param value
     */
    public void modifyUserInfo(final String key, final String value) {
        final String completeUrl = SettingAPIUri.getModifyUserInfoUrl();
        RequestParams params = ((BaseApplication) context.getApplicationContext())
                .getHttpRequestParams(completeUrl);
        params.addParameter("key", key);
        params.addParameter("value", value);
        HttpUtils.request(context, CloudHttpMethod.POST, params, new BaseModuleAPICallback(context, completeUrl) {

            @Override
            public void callbackTokenExpire(long requestTime) {
                OauthCallBack oauthCallBack = new OauthCallBack() {
                    @Override
                    public void reExecute() {
                        modifyUserInfo(key, value);
                    }

                    @Override
                    public void executeFailCallback() {
                        callbackFail("", -1);
                    }
                };
                refreshToken(
                        oauthCallBack, requestTime);
            }

            @Override
            public void callbackSuccess(byte[] arg0) {
                // TODO Auto-generated method stub
                apiInterface.returnModifyUserInfoSucces(new SettingGetBoolenResult(new String(arg0)));
            }

            @Override
            public void callbackFail(String error, int responseCode) {
                // TODO Auto-generated method stub
                apiInterface.returnModifyUserInfoFail(error, responseCode);

            }
        });

    }

    /**
     * 上传反馈和建议接口
     *
     * @param content
     * @param contact
     * @param userName
     */
    public void uploadFeedback(final String content, final String contact,
                               final String userName) {
        final String completeUrl = "http://u.inspur.com/analytics/RestFulServiceForIMP.ashx?resource=Feedback&method=AddECMFeedback";
        RequestParams params = ((BaseApplication) context.getApplicationContext())
                .getHttpRequestParams(completeUrl);
        String AppBaseID = context.getPackageName();
        String AppVersion = AppUtils.getVersion(context);
        String Organization = PreferencesUtils
                .getString(context, "orgName", "");
        String UUID = AppUtils.getMyUUID(context);
        params.addParameter("Content", content);
        params.addParameter("AppBaseID", AppBaseID);
        params.addParameter("AppVersion", AppVersion);
        params.addParameter("System", "android");
        params.addParameter("SystemVersion", android.os.Build.VERSION.RELEASE);
        params.addParameter("UserName", userName);
        params.addParameter("Organization", Organization);
        params.addParameter("Contact", contact);
        params.addParameter("Email", "");
        params.addParameter("Telephone", "");
        params.addParameter("UUID", UUID);
        HttpUtils.request(context, CloudHttpMethod.POST, params, new BaseModuleAPICallback(context, completeUrl) {

            @Override
            public void callbackTokenExpire(long requestTime) {
                // TODO Auto-generated method stub

            }

            @Override
            public void callbackSuccess(byte[] arg0) {
                // TODO Auto-generated method stub

            }

            @Override
            public void callbackFail(String error, int responseCode) {
                // TODO Auto-generated method stub

            }
        });
    }

    /**
     * 获取我的信息
     */
    public void getUserProfileConfigInfo() {
        final String completeUrl = SettingAPIUri.getUserProfileAndDisPlayUrl();
        RequestParams params = ((BaseApplication) context.getApplicationContext())
                .getHttpRequestParams(completeUrl);
        HttpUtils.request(context, CloudHttpMethod.GET, params, new BaseModuleAPICallback(context, completeUrl) {
            @Override
            public void callbackSuccess(byte[] arg0) {
                apiInterface.returnUserProfileConfigSuccess(new UserProfileInfoBean(new String(arg0)));
            }

            @Override
            public void callbackFail(String error, int responseCode) {
                apiInterface.returnUserProfileConfigFail(error, responseCode);
            }

            @Override
            public void callbackTokenExpire(long requestTime) {
                OauthCallBack oauthCallBack = new OauthCallBack() {
                    @Override
                    public void reExecute() {
                        getUserProfileConfigInfo();
                    }

                    @Override
                    public void executeFailCallback() {
                        callbackFail("", -1);
                    }
                };
                refreshToken(
                        oauthCallBack, requestTime);
            }

        });
    }


    /**
     * 获取当前绑定设备列表
     */
    public void getBindingDeviceList() {
        final String completeUrl = SettingAPIUri.getBindingDevicesUrl();
        RequestParams params =
                ((BaseApplication) context.getApplicationContext()).getHttpRequestParams(completeUrl);
        HttpUtils.request(context, CloudHttpMethod.GET, params, new BaseModuleAPICallback(context, completeUrl) {

            @Override
            public void callbackTokenExpire(long requestTime) {
                OauthCallBack oauthCallBack = new OauthCallBack() {
                    @Override
                    public void reExecute() {
                        getBindingDeviceList();
                    }

                    @Override
                    public void executeFailCallback() {
                        callbackFail("", -1);
                    }
                };
                refreshToken(
                        oauthCallBack, requestTime);
            }

            @Override
            public void callbackSuccess(byte[] arg0) {
                // TODO Auto-generated method stub
                apiInterface
                        .returnBindingDeviceListSuccess(new GetBindingDeviceResult(new String(arg0)));
            }

            @Override
            public void callbackFail(String error, int responseCode) {
                // TODO Auto-generated method stub
                apiInterface.returnBindingDeviceListFail(error, responseCode);
            }
        });
    }


    /**
     * 获取当前绑定设备列表
     */
    public void getDeviceLogList(final String udid) {
        final String completeUrl = SettingAPIUri.getDeviceLogUrl();
        RequestParams params =
                ((BaseApplication) context.getApplicationContext()).getHttpRequestParams(completeUrl);
        params.addParameter("udid", udid);
        HttpUtils.request(context, CloudHttpMethod.POST, params, new BaseModuleAPICallback(context, completeUrl) {

            @Override
            public void callbackTokenExpire(long requestTime) {
                OauthCallBack oauthCallBack = new OauthCallBack() {
                    @Override
                    public void reExecute() {
                        getDeviceLogList(udid);
                    }

                    @Override
                    public void executeFailCallback() {
                        callbackFail("", -1);
                    }
                };
                refreshToken(
                        oauthCallBack, requestTime);
            }

            @Override
            public void callbackSuccess(byte[] arg0) {
                // TODO Auto-generated method stub
                apiInterface
                        .returnDeviceLogListSuccess(new GetDeviceLogResult(new String(arg0)));
            }

            @Override
            public void callbackFail(String error, int responseCode) {
                // TODO Auto-generated method stub
                apiInterface.returnDeviceLogListFail(error, responseCode);
            }
        });
    }

    /**
     * 解绑设备
     *
     * @param udid
     */
    public void unBindDevice(final String udid) {
        final String completeUrl = SettingAPIUri.getUnBindDeviceUrl();
        RequestParams params = ((BaseApplication) context.getApplicationContext())
                .getHttpRequestParams(completeUrl);
        params.addParameter("udid", udid);
        HttpUtils.request(context, CloudHttpMethod.POST, params, new BaseModuleAPICallback(context, completeUrl) {
            @Override
            public void callbackSuccess(byte[] arg0) {
                apiInterface.returnUnBindDeviceSuccess();
            }

            @Override
            public void callbackFail(String error, int responseCode) {
                apiInterface.returnUnBindDeviceFail(error, responseCode);
            }

            @Override
            public void callbackTokenExpire(long requestTime) {
                OauthCallBack oauthCallBack = new OauthCallBack() {
                    @Override
                    public void reExecute() {
                        unBindDevice(udid);
                    }

                    @Override
                    public void executeFailCallback() {
                        callbackFail("", -1);
                    }
                };
                refreshToken(
                        oauthCallBack, requestTime);
            }

        });
    }

//    /**
//     * 获取是否启动MDM
//     */
//    public void getMDMState() {
//        final String completeUrl = APIUri.getMDMStateUrl();
//        RequestParams params = ((MyApplication) context.getApplicationContext())
//                .getHttpRequestParams(completeUrl);
//        HttpUtils.request(context, CloudHttpMethod.GET, params, new BaseModuleAPICallback(context, completeUrl) {
//            @Override
//            public void callbackSuccess(byte[] arg0) {
//                apiInterface.returnMDMStateSuccess(new GetMDMStateResult(new String(arg0)));
//            }
//
//            @Override
//            public void callbackFail(String error, int responseCode) {
//                apiInterface.returnMDMStateFail(error, responseCode);
//            }
//
//            @Override
//            public void callbackTokenExpire(long requestTime) {
//                OauthCallBack oauthCallBack = new OauthCallBack() {
//                    @Override
//                    public void reExecute() {
//                        getMDMState();
//                    }
//
//                    @Override
//                    public void executeFailCallback() {
//                        callbackFail("", -1);
//                    }
//                };
//                refreshToken(
//                        oauthCallBack, requestTime);
//            }
//
//        });
//    }

    /**
     * 设置脸部图像
     *
     * @param bitmapBase64
     */
    public void faceSetting(final String bitmapBase64) {
        final String completeUrl = SettingAPIUri.getFaceSettingUrl();
        RequestParams params = ((BaseApplication) context.getApplicationContext())
                .getHttpRequestParams(completeUrl);
        params.addParameter("face", bitmapBase64);
        params.setAsJsonContent(true);
        HttpUtils.request(context, CloudHttpMethod.POST, params, new BaseModuleAPICallback(context, completeUrl) {
            @Override
            public void callbackSuccess(byte[] arg0) {
                apiInterface.returnFaceSettingSuccess(new GetFaceSettingResult(new String(arg0)));
            }

            @Override
            public void callbackFail(String error, int responseCode) {
                apiInterface.returnFaceSettingFail(error, responseCode);
            }

            @Override
            public void callbackTokenExpire(long requestTime) {
                OauthCallBack oauthCallBack = new OauthCallBack() {
                    @Override
                    public void reExecute() {
                        faceSetting(bitmapBase64);
                    }

                    @Override
                    public void executeFailCallback() {
                        callbackFail("", -1);
                    }
                };
                refreshToken(
                        oauthCallBack, requestTime);
            }
        });
    }

    /**
     * 刷脸比对图像
     *
     * @param bitmapBase64
     */
    public void faceVerify(final String bitmapBase64) {
        final String completeUrl = SettingAPIUri.getFaceVerifyUrl();
        RequestParams params = ((BaseApplication) context.getApplicationContext())
                .getHttpRequestParams(completeUrl);
        params.addParameter("face", bitmapBase64);
        params.setAsJsonContent(true);
        HttpUtils.request(context, CloudHttpMethod.POST, params, new BaseModuleAPICallback(context, completeUrl) {
            @Override
            public void callbackSuccess(byte[] arg0) {
                apiInterface.returnFaceVerifySuccess(new GetFaceSettingResult(new String(arg0)));
            }

            @Override
            public void callbackFail(String error, int responseCode) {
                apiInterface.returnFaceVerifyFail(error, responseCode);
            }

            @Override
            public void callbackTokenExpire(long requestTime) {
                OauthCallBack oauthCallBack = new OauthCallBack() {
                    @Override
                    public void reExecute() {
                        faceVerify(bitmapBase64);
                    }

                    @Override
                    public void executeFailCallback() {
                        callbackFail("", -1);
                    }
                };
                refreshToken(
                        oauthCallBack, requestTime);
            }

        });
    }

    /**
     * 获取卡包信息
     */
    public void getCardPackageList() {
        final String completeUrl = SettingAPIUri.getCardPackageUrl();
        RequestParams params =
                ((BaseApplication) context.getApplicationContext()).getHttpRequestParams(completeUrl);
        HttpUtils.request(context, CloudHttpMethod.GET, params, new BaseModuleAPICallback(context, completeUrl) {

            @Override
            public void callbackTokenExpire(long requestTime) {
                OauthCallBack oauthCallBack = new OauthCallBack() {
                    @Override
                    public void reExecute() {
                        getCardPackageList();
                    }

                    @Override
                    public void executeFailCallback() {
                        callbackFail("", -1);
                    }
                };
                refreshToken(
                        oauthCallBack, requestTime);
            }

            @Override
            public void callbackSuccess(byte[] arg0) {
                apiInterface.returnCardPackageListSuccess(new GetCardPackageResult(new String(arg0)));
            }

            @Override
            public void callbackFail(String error, int responseCode) {
                apiInterface.returnCardPackageListFail(error, responseCode);
            }

        });
    }

    /**
     * 获取是否加入用户体验计划
     */
    public void getUserExperienceUpgradeFlag() {
        final String completeUrl = SettingAPIUri.getUserExperienceUpgradeFlagUrl();
        RequestParams params =
                ((BaseApplication) context.getApplicationContext()).getHttpRequestParams(completeUrl);
        HttpUtils.request(context, CloudHttpMethod.GET, params, new BaseModuleAPICallback(context, completeUrl) {

            @Override
            public void callbackTokenExpire(long requestTime) {
                OauthCallBack oauthCallBack = new OauthCallBack() {
                    @Override
                    public void reExecute() {
                        getCardPackageList();
                    }

                    @Override
                    public void executeFailCallback() {
                        callbackFail("", -1);
                    }
                };
                refreshToken(
                        oauthCallBack, requestTime);
            }

            @Override
            public void callbackSuccess(byte[] arg0) {
                apiInterface.returnExperienceUpgradeFlagSuccess(new GetExperienceUpgradeFlagResult(new String(arg0)));
            }

            @Override
            public void callbackFail(String error, int responseCode) {
                apiInterface.returnExperienceUpgradeFlagFail(error, responseCode);
            }

        });
    }

    /**
     * 获取是否加入用户体验计划
     */
    public void updateUserExperienceUpgradeFlag(int flag) {
        final String completeUrl = SettingAPIUri.getUpdateUserExperienceUpgradeFlagUrl(flag);
        RequestParams params =
                ((BaseApplication) context.getApplicationContext()).getHttpRequestParams(completeUrl);
        HttpUtils.request(context, CloudHttpMethod.GET, params, new BaseModuleAPICallback(context, completeUrl) {

            @Override
            public void callbackTokenExpire(long requestTime) {
                OauthCallBack oauthCallBack = new OauthCallBack() {
                    @Override
                    public void reExecute() {
                        getCardPackageList();
                    }

                    @Override
                    public void executeFailCallback() {
                        callbackFail("", -1);
                    }
                };
                refreshToken(
                        oauthCallBack, requestTime);
            }

            @Override
            public void callbackSuccess(byte[] arg0) {
                apiInterface.returnUpdateExperienceUpgradeFlagSuccess();
            }

            @Override
            public void callbackFail(String error, int responseCode) {
                apiInterface.returnUpdateExperienceUpgradeFlagFail(error, responseCode);
            }

        });
    }


    /**
     * 获取个人信息卡片的menu
     */
    public void getUserCardMenus() {
        final String completeUrl = SettingAPIUri.getUserCardMenusUrl();
        RequestParams params = BaseApplication.getInstance().getHttpRequestParams(completeUrl);
        HttpUtils.request(context, CloudHttpMethod.GET, params, new BaseModuleAPICallback(context, completeUrl) {

            @Override
            public void callbackTokenExpire(long requestTime) {
                OauthCallBack oauthCallBack = new OauthCallBack() {
                    @Override
                    public void reExecute() {
                        getUserCardMenus();
                    }

                    @Override
                    public void executeFailCallback() {
                        callbackFail("", -1);
                    }
                };
                refreshToken(
                        oauthCallBack, requestTime);
            }

            @Override
            public void callbackSuccess(byte[] arg0) {
                apiInterface.returnUserCardMenusSuccess(new GetUserCardMenusResult(new String(arg0)));
            }

            @Override
            public void callbackFail(String error, int responseCode) {
                apiInterface.returnUserCardMenusFail(error, responseCode);
            }

        });
    }

    /**
     * 保存webview是否自动旋转配置项
     *
     * @param isWebAutoRotate
     */
    public void saveWebAutoRotateConfig(final boolean isWebAutoRotate) {
        final String url = SettingAPIUri.seetingSaveAppConfigUrl("WebAutoRotate");
        RequestParams params = ((BaseApplication) context.getApplicationContext()).getHttpRequestParams(url);
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
     * 获取是否启动MDM
     */
    public void getMDMState(){
        final String completeUrl = SettingAPIUri.getMDMStateUrl();
        RequestParams params = ((BaseApplication) context.getApplicationContext())
                .getHttpRequestParams(completeUrl);
        HttpUtils.request(context, CloudHttpMethod.GET, params, new BaseModuleAPICallback(context, completeUrl) {
            @Override
            public void callbackSuccess(byte[] arg0) {
                apiInterface.returnMDMStateSuccess(new GetMDMStateResult(new String(arg0)));
            }

            @Override
            public void callbackFail(String error, int responseCode) {
                apiInterface.returnMDMStateFail(error,responseCode);
            }

            @Override
            public void callbackTokenExpire(long requestTime) {
                OauthCallBack oauthCallBack = new OauthCallBack() {
                    @Override
                    public void reExecute() {
                        getMDMState();
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

//    /**
//     * 获取是否启动MDM
//     */
//    public void getMDMState1() {
//        final String completeUrl = SettingAPIUri.getMDMStateUrl();
//        RequestParams params = ((BaseApplication) context.getApplicationContext())
//                .getHttpRequestParams(completeUrl);
//        HttpUtils.request(context, CloudHttpMethod.GET, params, new APICallback(context, completeUrl) {
//            @Override
//            public void callbackSuccess(byte[] arg0) {
//                apiInterface.returnMDMStateSuccess(new GetMDMStateResult(new String(arg0)));
//            }
//
//            @Override
//            public void callbackFail(String error, int responseCode) {
//                apiInterface.returnMDMStateFail(error, responseCode);
//            }
//
//            @Override
//            public void callbackTokenExpire(long requestTime) {
//                OauthCallBack oauthCallBack = new OauthCallBack() {
//                    @Override
//                    public void reExecute() {
//                        getMDMState();
//                    }
//
//                    @Override
//                    public void executeFailCallback() {
//                        callbackFail("", -1);
//                    }
//                };
//                OauthUtils.getInstance().refreshToken(
//                        oauthCallBack, requestTime);
//            }
//
//        });
//    }


}
