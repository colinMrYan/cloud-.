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
import com.inspur.emmcloud.basemodule.api.BaseModuleAPICallback;
import com.inspur.emmcloud.basemodule.api.CloudHttpMethod;
import com.inspur.emmcloud.basemodule.api.HttpUtils;
import com.inspur.emmcloud.basemodule.application.BaseApplication;
import com.inspur.emmcloud.bean.appcenter.volume.GetVolumeFileListResult;
import com.inspur.emmcloud.bean.appcenter.volume.GetVolumeListResult;
import com.inspur.emmcloud.componentservice.login.LoginService;
import com.inspur.emmcloud.componentservice.login.OauthCallBack;

import org.xutils.http.RequestParams;


/**
 * com.inspur.emmcloud.api.apiservice.MyAppAPIService create at 2016年11月8日
 * 下午2:31:55
 */
public class MyAppAPIService {
    private Context context;
    private APIInterface apiInterface;


    public MyAppAPIService(Context context) {
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
     * 向服务端同步常用应用数据
     *
     * @param commonAppListJson
     */
    public void syncCommonApp(final String commonAppListJson) {
        final String url = APIUri.saveAppConfigUrl("CommonFunctions");
        RequestParams params = ((MyApplication) context.getApplicationContext()).getHttpRequestParams(url);
        params.setBodyContent(commonAppListJson);
        HttpUtils.request(context, CloudHttpMethod.POST, params, new BaseModuleAPICallback(context, url) {
            @Override
            public void callbackSuccess(byte[] arg0) {
                apiInterface.returnSaveConfigSuccess();
            }

            @Override
            public void callbackFail(String error, int responseCode) {
                apiInterface.returnSaveConfigFail();
            }

            @Override
            public void callbackTokenExpire(long requestTime) {
                OauthCallBack oauthCallBack = new OauthCallBack() {
                    @Override
                    public void reExecute() {
                        syncCommonApp(commonAppListJson);
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

    public void getVolumeList() {
        final String url = APIUri.getVolumeListUrl();
        RequestParams params = ((BaseApplication) context.getApplicationContext()).getHttpRequestParams(url);
        HttpUtils.request(context, CloudHttpMethod.GET, params, new BaseModuleAPICallback(context, url) {
            @Override
            public void callbackSuccess(byte[] arg0) {
                apiInterface.returnVolumeListSuccess(new GetVolumeListResult(new String(arg0)));
            }

            @Override
            public void callbackFail(String error, int responseCode) {
                apiInterface.returnVolumeListFail(error, responseCode);
            }

            @Override
            public void callbackTokenExpire(long requestTime) {
                OauthCallBack oauthCallBack = new OauthCallBack() {
                    @Override
                    public void reExecute() {
                        getVolumeList();
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
     * 获取云盘文件列表
     *
     * @param volumeId
     * @param currentDirAbsolutePath
     */
    public void getVolumeFileList(final String volumeId, final String currentDirAbsolutePath) {
        final String url = APIUri.getVolumeFileOperationUrl(volumeId);
        RequestParams params = ((BaseApplication) context.getApplicationContext()).getHttpRequestParams(url);
        params.addParameter("path", currentDirAbsolutePath);
        HttpUtils.request(context, CloudHttpMethod.GET, params, new BaseModuleAPICallback(context, url) {
            @Override
            public void callbackSuccess(byte[] arg0) {
                apiInterface.returnVolumeFileListSuccess(new GetVolumeFileListResult(new String(arg0)));
            }

            @Override
            public void callbackFail(String error, int responseCode) {
                apiInterface.returnVolumeFileListFail(error, responseCode);
            }

            @Override
            public void callbackTokenExpire(long requestTime) {
                OauthCallBack oauthCallBack = new OauthCallBack() {
                    @Override
                    public void reExecute() {
                        getVolumeFileList(volumeId, currentDirAbsolutePath);
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
}
