/**
 * ContactAPIService.java
 * classes : com.inspur.emmcloud.api.apiservice.ContactAPIService
 * V 1.0.0
 * Create at 2016年11月8日 下午2:32:19
 */
package com.inspur.emmcloud.api.apiservice;

import android.content.Context;

import com.inspur.emmcloud.MyApplication;
import com.inspur.emmcloud.api.APIInterface;
import com.inspur.emmcloud.api.APIUri;
import com.inspur.emmcloud.basemodule.api.BaseModuleAPICallback;
import com.inspur.emmcloud.basemodule.api.CloudHttpMethod;
import com.inspur.emmcloud.basemodule.api.HttpUtils;
import com.inspur.emmcloud.bean.chat.GetAllRobotsResult;
import com.inspur.emmcloud.bean.chat.Robot;
import com.inspur.emmcloud.bean.contact.GetContactOrgListUpateResult;
import com.inspur.emmcloud.bean.contact.GetContactUserListUpateResult;
import com.inspur.emmcloud.interf.OauthCallBack;
import com.inspur.emmcloud.util.privates.OauthUtils;

import org.xutils.http.RequestParams;
import org.xutils.x;

/**
 * com.inspur.emmcloud.api.apiservice.ContactAPIService
 * create at 2016年11月8日 下午2:32:19
 */
public class ContactAPIService {
    private Context context;
    private APIInterface apiInterface;

    public ContactAPIService(Context context) {
        this.context = context;
    }

    public void setAPIInterface(APIInterface apiInterface) {
        this.apiInterface = apiInterface;
    }


    /**
     * 获取所有机器人信息
     */
    public void getAllRobotInfo() {
        final String completeUrl = APIUri.getAllBotInfo();
        RequestParams params = ((MyApplication) context.getApplicationContext()).getHttpRequestParams(completeUrl);
        HttpUtils.request(context, CloudHttpMethod.GET, params, new BaseModuleAPICallback(context, completeUrl) {

            @Override
            public void callbackTokenExpire(long requestTime) {
                OauthCallBack oauthCallBack = new OauthCallBack() {
                    @Override
                    public void reExecute() {
                        getAllRobotInfo();
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
                apiInterface.returnAllRobotsSuccess(new GetAllRobotsResult(new String(arg0)));
            }

            @Override
            public void callbackFail(String error, int responseCode) {
                apiInterface.returnAllRobotsFail(error, responseCode);
            }
        });
    }

    /**
     * 通过id获取机器人信息
     *
     * @param id
     */
    public void getRobotInfoById(final String id) {
        final String completeUrl = APIUri.getBotInfoById() + id;
        RequestParams params = ((MyApplication) context.getApplicationContext()).getHttpRequestParams(completeUrl);
        HttpUtils.request(context, CloudHttpMethod.GET, params, new BaseModuleAPICallback(context, completeUrl) {

            @Override
            public void callbackTokenExpire(long requestTime) {
                OauthCallBack oauthCallBack = new OauthCallBack() {
                    @Override
                    public void reExecute() {
                        getRobotInfoById(id);
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
                apiInterface.returnRobotByIdSuccess(new Robot(new String(arg0)));
            }

            @Override
            public void callbackFail(String error, int responseCode) {
                apiInterface.returnRobotByIdFail(error, responseCode);
            }
        });
    }

    public void getContactUserList(final String saveConfigVersion) {
        String url = APIUri.getContactUserUrl();
        RequestParams params = MyApplication.getInstance().getHttpRequestParams(url);
        x.http().post(params, new BaseModuleAPICallback(context, url) {
            @Override
            public void callbackSuccess(byte[] arg0) {
                apiInterface.returnContactUserListSuccess(arg0, saveConfigVersion);
            }

            @Override
            public void callbackFail(String error, int responseCode) {
                apiInterface.returnContactUserListFail("", -1);
            }

            @Override
            public void callbackTokenExpire(long requestTime) {
                OauthCallBack oauthCallBack = new OauthCallBack() {
                    @Override
                    public void reExecute() {
                        getContactUserList(saveConfigVersion);
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

    public void getContactUserListUpdate(final long lastQuetyTime, final String saveConfigVersion) {
        String url = APIUri.getContactUserUrlUpdate();
        RequestParams params = MyApplication.getInstance().getHttpRequestParams(url);
        params.addParameter("lastQueryTime", lastQuetyTime);
        x.http().post(params, new BaseModuleAPICallback(context, url) {
            @Override
            public void callbackSuccess(byte[] arg0) {
                apiInterface.returnContactUserListUpdateSuccess(new GetContactUserListUpateResult(new String(arg0)), saveConfigVersion);
            }

            @Override
            public void callbackFail(String error, int responseCode) {
                apiInterface.returnContactUserListUpdateFail("", -1);
            }

            @Override
            public void callbackTokenExpire(long requestTime) {
                OauthCallBack oauthCallBack = new OauthCallBack() {
                    @Override
                    public void reExecute() {
                        getContactUserListUpdate(lastQuetyTime, saveConfigVersion);
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


    public void getContactOrgList(final String saveConfigVersion) {
        String url = APIUri.getContactOrgUrl();
        RequestParams params = MyApplication.getInstance().getHttpRequestParams(url);
        x.http().post(params, new BaseModuleAPICallback(context, url) {
            @Override
            public void callbackSuccess(byte[] arg0) {
                apiInterface.returnContactOrgListSuccess(arg0, saveConfigVersion);
            }

            @Override
            public void callbackFail(String error, int responseCode) {
                apiInterface.returnContactOrgListFail("", -1);
            }

            @Override
            public void callbackTokenExpire(long requestTime) {
                OauthCallBack oauthCallBack = new OauthCallBack() {
                    @Override
                    public void reExecute() {
                        getContactOrgList(saveConfigVersion);
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


    public void getContactOrgListUpdate(final long lastQuetyTime, final String saveConfigVersion) {
        String url = APIUri.getContactOrgUrlUpdate();
        RequestParams params = MyApplication.getInstance().getHttpRequestParams(url);
        params.addParameter("lastQueryTime", lastQuetyTime);
        x.http().post(params, new BaseModuleAPICallback(context, url) {
            @Override
            public void callbackSuccess(byte[] arg0) {
                apiInterface.returnContactOrgListUpdateSuccess(new GetContactOrgListUpateResult(new String(arg0)), saveConfigVersion);
            }

            @Override
            public void callbackFail(String error, int responseCode) {
                apiInterface.returnContactOrgListUpdateFail("", -1);
            }

            @Override
            public void callbackTokenExpire(long requestTime) {
                OauthCallBack oauthCallBack = new OauthCallBack() {
                    @Override
                    public void reExecute() {
                        getContactOrgListUpdate(lastQuetyTime, saveConfigVersion);
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
