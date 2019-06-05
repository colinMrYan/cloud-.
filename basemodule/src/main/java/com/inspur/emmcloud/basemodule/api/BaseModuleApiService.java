package com.inspur.emmcloud.basemodule.api;

import android.content.Context;

import com.inspur.emmcloud.basemodule.application.BaseApplication;
import com.inspur.emmcloud.basemodule.bean.GetAllConfigVersionResult;
import com.inspur.emmcloud.basemodule.bean.GetLanguageResult;
import com.inspur.emmcloud.basemodule.bean.PVCollectModel;
import com.inspur.emmcloud.basemodule.util.AppUtils;

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
                OauthUtils.getInstance().refreshToken(
                        oauthCallBack, requestTime);
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
                OauthUtils.getInstance().refreshToken(oauthCallBack, requestTime);
            }
        });
    }

}
