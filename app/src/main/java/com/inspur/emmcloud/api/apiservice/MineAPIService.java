/**
 * MineAPIService.java
 * classes : com.inspur.emmcloud.api.apiservice.MineAPIService
 * V 1.0.0
 * Create at 2016年11月8日 下午2:34:55
 */
package com.inspur.emmcloud.api.apiservice;

import android.content.Context;

import com.inspur.emmcloud.MyApplication;
import com.inspur.emmcloud.api.APICallback;
import com.inspur.emmcloud.api.APIInterface;
import com.inspur.emmcloud.api.APIUri;
import com.inspur.emmcloud.api.CloudHttpMethod;
import com.inspur.emmcloud.api.HttpUtils;
import com.inspur.emmcloud.bean.login.GetMDMStateResult;
import com.inspur.emmcloud.bean.mine.GetBindingDeviceResult;
import com.inspur.emmcloud.bean.mine.GetCardPackageResult;
import com.inspur.emmcloud.bean.mine.GetDeviceLogResult;
import com.inspur.emmcloud.bean.mine.GetExperienceUpgradeFlagResult;
import com.inspur.emmcloud.bean.mine.GetFaceSettingResult;
import com.inspur.emmcloud.bean.mine.GetLanguageResult;
import com.inspur.emmcloud.bean.mine.GetUploadMyHeadResult;
import com.inspur.emmcloud.bean.mine.UserProfileInfoBean;
import com.inspur.emmcloud.bean.system.GetBoolenResult;
import com.inspur.emmcloud.interf.OauthCallBack;
import com.inspur.emmcloud.util.common.PreferencesUtils;
import com.inspur.emmcloud.util.privates.AppUtils;
import com.inspur.emmcloud.util.privates.OauthUtils;

import org.xutils.http.RequestParams;

import java.io.File;


/**
 * com.inspur.emmcloud.api.apiservice.MineAPIService create at 2016年11月8日
 * 下午2:34:55
 */
public class MineAPIService {
    private Context context;
    private APIInterface apiInterface;

    public MineAPIService(Context context) {
        this.context = context;
    }

    public void setAPIInterface(APIInterface apiInterface) {
        this.apiInterface = apiInterface;
    }

    /**
     * 修改用户头像
     *
     * @param
     */
    public void updateUserHead(final String filePath) {

        final String completeUrl = APIUri.getUpdateUserHeadUrl();
        RequestParams params = ((MyApplication) context.getApplicationContext())
                .getHttpRequestParams(completeUrl);
        File file = new File(filePath);
        params.setMultipart(true);// 有上传文件时使用multipart表单, 否则上传原始文件流.
        params.addBodyParameter("head", file);
        HttpUtils.request(context, CloudHttpMethod.POST, params, new APICallback(context, completeUrl) {

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
                OauthUtils.getInstance().refreshToken(
                        oauthCallBack, requestTime);
            }

            @Override
            public void callbackSuccess(byte[] arg0) {
                // TODO Auto-generated method stub
                apiInterface
                        .returnUploadMyHeadSuccess(new GetUploadMyHeadResult(new String(arg0)),filePath);
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
        final String completeUrl = APIUri.getModifyUserInfoUrl();
        RequestParams params = ((MyApplication) context.getApplicationContext())
                .getHttpRequestParams(completeUrl);
        params.addParameter("key", key);
        params.addParameter("value", value);
        HttpUtils.request(context, CloudHttpMethod.POST, params, new APICallback(context, completeUrl) {

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
                OauthUtils.getInstance().refreshToken(
                        oauthCallBack, requestTime);
            }

            @Override
            public void callbackSuccess(byte[] arg0) {
                // TODO Auto-generated method stub
                apiInterface.returnModifyUserInfoSucces(new GetBoolenResult(new String(arg0)));
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
        RequestParams params = ((MyApplication) context.getApplicationContext())
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
        HttpUtils.request(context, CloudHttpMethod.POST, params, new APICallback(context, completeUrl) {

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
     * 获取语言
     */
    public void getLanguage() {

        final String completeUrl = APIUri.getLangUrl();
        RequestParams params = ((MyApplication) context.getApplicationContext())
                .getHttpRequestParams(completeUrl);
        HttpUtils.request(context, CloudHttpMethod.GET, params, new APICallback(context, completeUrl) {

            @Override
            public void callbackTokenExpire(long requestTime) {
                OauthCallBack oauthCallBack = new OauthCallBack() {
                    @Override
                    public void reExecute() {
                        getLanguage();
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
                apiInterface.returnLanguageSuccess(new GetLanguageResult(new String(arg0)));
            }

            @Override
            public void callbackFail(String error, int responseCode) {
                // TODO Auto-generated method stub
                apiInterface.returnLanguageFail(error, responseCode);
            }
        });
    }

    /**
     * 获取我的信息
     */
    public void getUserProfileConfigInfo() {
        final String completeUrl = APIUri.getUserProfileAndDisPlayUrl();
        RequestParams params = ((MyApplication) context.getApplicationContext())
                .getHttpRequestParams(completeUrl);
        HttpUtils.request(context, CloudHttpMethod.GET, params, new APICallback(context, completeUrl) {
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
                OauthUtils.getInstance().refreshToken(
                        oauthCallBack, requestTime);
            }

        });
    }


    /**
     * 获取当前绑定设备列表
     */
    public void getBindingDeviceList() {
        final String completeUrl = APIUri.getBindingDevicesUrl();
        RequestParams params =
                ((MyApplication) context.getApplicationContext()).getHttpRequestParams(completeUrl);
        HttpUtils.request(context, CloudHttpMethod.GET, params, new APICallback(context, completeUrl) {

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
                OauthUtils.getInstance().refreshToken(
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
        final String completeUrl = APIUri.getDeviceLogUrl();
        RequestParams params =
                ((MyApplication) context.getApplicationContext()).getHttpRequestParams(completeUrl);
        params.addParameter("udid", udid);
        HttpUtils.request(context, CloudHttpMethod.POST, params, new APICallback(context, completeUrl) {

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
                OauthUtils.getInstance().refreshToken(
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
        final String completeUrl = APIUri.getUnBindDeviceUrl();
        RequestParams params = ((MyApplication) context.getApplicationContext())
                .getHttpRequestParams(completeUrl);
        params.addParameter("udid", udid);
        HttpUtils.request(context, CloudHttpMethod.POST, params, new APICallback(context, completeUrl) {
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
                OauthUtils.getInstance().refreshToken(
                        oauthCallBack, requestTime);
            }

        });
    }

    /**
     * 获取是否启动MDM
     */
    public void getMDMState() {
        final String completeUrl = APIUri.getMDMStateUrl();
        RequestParams params = ((MyApplication) context.getApplicationContext())
                .getHttpRequestParams(completeUrl);
        HttpUtils.request(context, CloudHttpMethod.GET, params, new APICallback(context, completeUrl) {
            @Override
            public void callbackSuccess(byte[] arg0) {
                apiInterface.returnMDMStateSuccess(new GetMDMStateResult(new String(arg0)));
            }

            @Override
            public void callbackFail(String error, int responseCode) {
                apiInterface.returnMDMStateFail(error, responseCode);
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
                OauthUtils.getInstance().refreshToken(
                        oauthCallBack, requestTime);
            }

        });
    }

    /**
     * 设置脸部图像
     *
     * @param bitmapBase64
     */
    public void faceSetting(final String bitmapBase64) {
        final String completeUrl = APIUri.getFaceSettingUrl();
        RequestParams params = ((MyApplication) context.getApplicationContext())
                .getHttpRequestParams(completeUrl);
        params.addParameter("face", bitmapBase64);
        params.setAsJsonContent(true);
        HttpUtils.request(context, CloudHttpMethod.POST, params, new APICallback(context, completeUrl) {
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
                OauthUtils.getInstance().refreshToken(
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
        final String completeUrl = APIUri.getFaceVerifyUrl();
        RequestParams params = ((MyApplication) context.getApplicationContext())
                .getHttpRequestParams(completeUrl);
        params.addParameter("face", bitmapBase64);
        params.setAsJsonContent(true);
        HttpUtils.request(context, CloudHttpMethod.POST, params, new APICallback(context, completeUrl) {
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
                OauthUtils.getInstance().refreshToken(
                        oauthCallBack, requestTime);
            }

        });
    }

    /**
     * 获取卡包信息
     */
    public void getCardPackageList(){
        final String completeUrl = APIUri.getCardPackageUrl();
        RequestParams params =
                ((MyApplication) context.getApplicationContext()).getHttpRequestParams(completeUrl);
        HttpUtils.request(context, CloudHttpMethod.GET, params, new APICallback(context, completeUrl) {

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
                OauthUtils.getInstance().refreshToken(
                        oauthCallBack, requestTime);
            }

            @Override
            public void callbackSuccess(byte[] arg0) {
                apiInterface.returnCardPackageListSuccess(new GetCardPackageResult(new String(arg0)));
            }

            @Override
            public void callbackFail(String error, int responseCode) {
                apiInterface.returnCardPackageListFail(error,responseCode);
            }

        });
    }

    /**
     * 获取是否加入用户体验计划
     */
    public void getUserExperienceUpgradeFlag(){
        final String completeUrl = APIUri.getUserExperienceUpgradeFlagUrl();
        RequestParams params =
                ((MyApplication) context.getApplicationContext()).getHttpRequestParams(completeUrl);
        HttpUtils.request(context, CloudHttpMethod.GET, params, new APICallback(context, completeUrl) {

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
                OauthUtils.getInstance().refreshToken(
                        oauthCallBack, requestTime);
            }

            @Override
            public void callbackSuccess(byte[] arg0) {
                apiInterface.returnExperienceUpgradeFlagSuccess(new GetExperienceUpgradeFlagResult(new String(arg0)));
            }

            @Override
            public void callbackFail(String error, int responseCode) {
                apiInterface.returnExperienceUpgradeFlagFail(error,responseCode);
            }

        });
    }

    /**
     * 获取是否加入用户体验计划
     */
    public void updateUserExperienceUpgradeFlag(int flag){
        final String completeUrl = APIUri.getUpdateUserExperienceUpgradeFlagUrl(flag);
        RequestParams params =
                ((MyApplication) context.getApplicationContext()).getHttpRequestParams(completeUrl);
        HttpUtils.request(context, CloudHttpMethod.GET, params, new APICallback(context, completeUrl) {

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
                OauthUtils.getInstance().refreshToken(
                        oauthCallBack, requestTime);
            }

            @Override
            public void callbackSuccess(byte[] arg0) {
                apiInterface.returnUpdateExperienceUpgradeFlagSuccess();
            }

            @Override
            public void callbackFail(String error, int responseCode) {
                apiInterface.returnUpdateExperienceUpgradeFlagFail(error,responseCode);
            }

        });
    }


    /**
     * 获取个人信息卡片的menu
     */
    public void getUserCardMenus(){
        final String completeUrl = APIUri.getUserCardMenusUrl();
        RequestParams params =MyApplication.getInstance().getHttpRequestParams(completeUrl);
        HttpUtils.request(context, CloudHttpMethod.GET, params, new APICallback(context, completeUrl) {

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
                OauthUtils.getInstance().refreshToken(
                        oauthCallBack, requestTime);
            }

            @Override
            public void callbackSuccess(byte[] arg0) {
                apiInterface.returnUpdateExperienceUpgradeFlagSuccess();
            }

            @Override
            public void callbackFail(String error, int responseCode) {
                apiInterface.returnUpdateExperienceUpgradeFlagFail(error,responseCode);
            }

        });
    }
}
