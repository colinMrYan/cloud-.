package com.inspur.emmcloud.webex.api;

import android.content.Context;

import com.inspur.emmcloud.baselib.router.Router;
import com.inspur.emmcloud.basemodule.api.BaseModuleAPICallback;
import com.inspur.emmcloud.basemodule.application.BaseApplication;
import com.inspur.emmcloud.componentservice.login.LoginService;
import com.inspur.emmcloud.componentservice.login.OauthCallBack;
import com.inspur.emmcloud.webex.bean.GetScheduleWebexMeetingSuccess;
import com.inspur.emmcloud.webex.bean.GetWebexMeetingListResult;
import com.inspur.emmcloud.webex.bean.GetWebexTKResult;
import com.inspur.emmcloud.webex.bean.WebexMeeting;

import org.json.JSONObject;
import org.xutils.http.HttpMethod;
import org.xutils.http.RequestParams;
import org.xutils.x;

/**
 * Created by chenmch on 2018/10/12.
 */

public class WebexAPIService {
    private Context context;
    private WebexAPIInterface apiInterface;

    public WebexAPIService(Context context) {
        this.context = context;

    }

    public void setAPIInterface(WebexAPIInterface apiInterface) {
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
     * 获取webex会议列表
     */
    public void getWebexMeetingList() {
        final String url = WebexAPIUri.getWebexMeetingListUrl();
        RequestParams params = BaseApplication.getInstance().getHttpRequestParams(url);
        params.setReadTimeout(30000);
        x.http().request(HttpMethod.GET, params, new BaseModuleAPICallback(context, url) {
            @Override
            public void callbackSuccess(byte[] arg0) {
                apiInterface.returnWebexMeetingListSuccess(new GetWebexMeetingListResult(new String(arg0)));
            }

            @Override
            public void callbackFail(String error, int responseCode) {
                apiInterface.returnWebexMeetingListFail(error, responseCode);
            }

            @Override
            public void callbackTokenExpire(long requestTime) {
                refreshToken(new OauthCallBack() {
                    @Override
                    public void reExecute() {
                        getWebexMeetingList();
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
     * 预定会议
     */
    public void scheduleWebexMeetingList(final JSONObject obj) {
        final String url = WebexAPIUri.getScheduleWebexMeetingUrl();
        RequestParams params = BaseApplication.getInstance().getHttpRequestParams(url);
        params.setReadTimeout(30000);
        params.setBodyContent(obj.toString());
        params.setAsJsonContent(true);
        x.http().request(HttpMethod.POST, params, new BaseModuleAPICallback(context, url) {
            @Override
            public void callbackSuccess(byte[] arg0) {
                apiInterface.returnScheduleWebexMeetingSuccess(new GetScheduleWebexMeetingSuccess(new String(arg0)));
            }

            @Override
            public void callbackFail(String error, int responseCode) {
                apiInterface.returnScheduleWebexMeetingFail(error, responseCode);
            }

            @Override
            public void callbackTokenExpire(long requestTime) {
                refreshToken(
                        new OauthCallBack() {
                            @Override
                            public void reExecute() {
                                scheduleWebexMeetingList(obj);
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
     * 获取webex会议
     */
    public void getWebexMeeting(final String meetingID) {
        final String url = WebexAPIUri.getWebexMeetingUrl(meetingID);
        RequestParams params = BaseApplication.getInstance().getHttpRequestParams(url);
        params.setReadTimeout(30000);
        x.http().request(HttpMethod.GET, params, new BaseModuleAPICallback(context, url) {
            @Override
            public void callbackSuccess(byte[] arg0) {
                apiInterface.returnWebexMeetingSuccess(new WebexMeeting(new String(arg0)));
            }

            @Override
            public void callbackFail(String error, int responseCode) {
                apiInterface.returnWebexMeetingFail(error, responseCode);
            }

            @Override
            public void callbackTokenExpire(long requestTime) {
                refreshToken(
                        new OauthCallBack() {
                            @Override
                            public void reExecute() {
                                getWebexMeeting(meetingID);
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
     * 获取webex会议
     */
    public void getWebexTK() {
        final String url = WebexAPIUri.getWebexTK();
        RequestParams params = BaseApplication.getInstance().getHttpRequestParams(url);
        params.setReadTimeout(30000);
        x.http().request(HttpMethod.GET, params, new BaseModuleAPICallback(context, url) {
            @Override
            public void callbackSuccess(byte[] arg0) {
                apiInterface.returnWebexTKSuccess(new GetWebexTKResult(new String(arg0)));
            }

            @Override
            public void callbackFail(String error, int responseCode) {
                apiInterface.returnWebexTKFail(error, responseCode);
            }

            @Override
            public void callbackTokenExpire(long requestTime) {
                refreshToken(
                        new OauthCallBack() {
                            @Override
                            public void reExecute() {
                                getWebexTK();
                            }

                            @Override
                            public void executeFailCallback() {
                                callbackFail("", -1);
                            }
                        }, requestTime);
            }
        });
    }

    public void removeMeeting(final String meetingID) {
        final String url = WebexAPIUri.getRemoveWebexMeetingUrl(meetingID);
        RequestParams params = BaseApplication.getInstance().getHttpRequestParams(url);
        params.setReadTimeout(30000);
        x.http().request(HttpMethod.GET, params, new BaseModuleAPICallback(context, url) {
            @Override
            public void callbackSuccess(byte[] arg0) {
                apiInterface.returnRemoveWebexMeetingSuccess();
            }

            @Override
            public void callbackFail(String error, int responseCode) {
                apiInterface.returnRemoveWebexMeetingFail(error, responseCode);
            }

            @Override
            public void callbackTokenExpire(long requestTime) {
                refreshToken(
                        new OauthCallBack() {
                            @Override
                            public void reExecute() {
                                removeMeeting(meetingID);
                            }

                            @Override
                            public void executeFailCallback() {
                                callbackFail("", -1);
                            }
                        }, requestTime);
            }
        });
    }
}
