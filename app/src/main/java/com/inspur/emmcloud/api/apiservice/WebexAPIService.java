package com.inspur.emmcloud.api.apiservice;

import android.content.Context;

import com.inspur.emmcloud.MyApplication;
import com.inspur.emmcloud.api.APICallback;
import com.inspur.emmcloud.api.APIInterface;
import com.inspur.emmcloud.api.APIUri;
import com.inspur.emmcloud.bean.appcenter.webex.GetScheduleWebexMeetingSuccess;
import com.inspur.emmcloud.bean.appcenter.webex.GetWebexMeetingListResult;
import com.inspur.emmcloud.interf.OauthCallBack;
import com.inspur.emmcloud.util.privates.OauthUtils;

import org.json.JSONObject;
import org.xutils.http.HttpMethod;
import org.xutils.http.RequestParams;
import org.xutils.x;

/**
 * Created by chenmch on 2018/10/12.
 */

public class WebexAPIService {
    private Context context;
    private APIInterface apiInterface;

    public WebexAPIService(Context context) {
        this.context = context;

    }

    public void setAPIInterface(APIInterface apiInterface) {
        this.apiInterface = apiInterface;
    }

    /**
     * 获取webex会议列表
     */
    public void getWebexMeetingList() {
        final String url = APIUri.getWebexMeetingListUrl();
        RequestParams params = MyApplication.getInstance().getHttpRequestParams(url);
        x.http().request(HttpMethod.GET, params, new APICallback(context, url) {
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
                OauthUtils.getInstance().refreshToken(
                        new OauthCallBack() {
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
     * 获取webex会议列表
     */
    public void scheduleWebexMeetingList(final JSONObject obj) {
        final String url = APIUri.getWebexMeetingListUrl();
        RequestParams params = MyApplication.getInstance().getHttpRequestParams(url);
        params.setBodyContent(obj.toString());
        params.setAsJsonContent(true);
        x.http().request(HttpMethod.POST, params, new APICallback(context, url) {
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
                OauthUtils.getInstance().refreshToken(
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

}
