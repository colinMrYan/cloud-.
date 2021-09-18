package com.inspur.emmcloud.news.api;

import android.content.Context;

import com.inspur.emmcloud.baselib.router.Router;
import com.inspur.emmcloud.basemodule.api.BaseModuleAPICallback;
import com.inspur.emmcloud.basemodule.api.CloudHttpMethod;
import com.inspur.emmcloud.basemodule.api.HttpUtils;
import com.inspur.emmcloud.basemodule.application.BaseApplication;
import com.inspur.emmcloud.componentservice.login.LoginService;
import com.inspur.emmcloud.componentservice.login.OauthCallBack;
import com.inspur.emmcloud.news.bean.GetGroupNewsDetailResult;
import com.inspur.emmcloud.news.bean.GetNewsInstructionResult;
import com.inspur.emmcloud.news.bean.GetNewsTitleResult;

import org.json.JSONException;
import org.json.JSONObject;
import org.xutils.http.RequestParams;

public class NewsApiService {

    private Context context;
    private NewsAPIInterface apiInterface;

    public NewsApiService(Context context) {
        this.context = context;
    }

    private void refreshToken(OauthCallBack oauthCallBack, long requestTime) {
        Router router = Router.getInstance();
        if (router.getService(LoginService.class) != null) {
            LoginService service = router.getService(LoginService.class);
            service.refreshToken(oauthCallBack, requestTime);
        }
    }

    public void setAPIInterface(NewsAPIInterface apiInterface) {
        this.apiInterface = apiInterface;
    }

    /**
     * 获取集团新闻标题
     */
    public void getNewsTitles() {

        final String completeUrl = NewsAPIUri.getGroupNewsUrl("/content/news/section");
        RequestParams params = ((BaseApplication) context.getApplicationContext())
                .getHttpRequestParams(completeUrl);
        HttpUtils.request(context, CloudHttpMethod.GET, params, new BaseModuleAPICallback(context, completeUrl) {

            @Override
            public void callbackTokenExpire(long requestTime) {
                OauthCallBack oauthCallBack = new OauthCallBack() {
                    @Override
                    public void reExecute() {
                        getNewsTitles();
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
                        .returnGroupNewsTitleSuccess(new GetNewsTitleResult(new String(arg0)));
            }

            @Override
            public void callbackFail(String error, int responseCode) {
                // TODO Auto-generated method stub

                apiInterface.returnGroupNewsTitleFail(error, responseCode);
            }
        });
    }


    /**
     * 请求每个标题下的新闻列表
     *
     * @param ncid
     * @param page
     */
    public void getGroupNewsDetail(final String ncid, final int page) {

        final String completeUrl = NewsAPIUri.getGroupNewsUrl("/content/news/section/" + ncid + "/post?page=" + page + "&limit=20");
        RequestParams params = ((BaseApplication) context.getApplicationContext())
                .getHttpRequestParams(completeUrl);
        HttpUtils.request(context, CloudHttpMethod.GET, params, new BaseModuleAPICallback(context, completeUrl) {

            @Override
            public void callbackTokenExpire(long requestTime) {
                OauthCallBack oauthCallBack = new OauthCallBack() {
                    @Override
                    public void reExecute() {
                        getGroupNewsDetail(ncid, page);
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
                        .returnGroupNewsDetailSuccess(new GetGroupNewsDetailResult(new String(arg0)), page);
            }

            @Override
            public void callbackFail(String error, int responseCode) {
                // TODO Auto-generated method stub
                apiInterface.returnGroupNewsDetailFail(error, responseCode, page);
            }
        });

    }

    /**
     * 新闻批示接口，传入内容为批示内容
     *
     * @param instruction
     */
    public void sendNewsInstruction(final String newsId, final String instruction) {
        final String completeUrl = NewsAPIUri.getNewsInstruction(newsId);
        RequestParams params = ((BaseApplication) context.getApplicationContext())
                .getHttpRequestParams(completeUrl);
        params.setHeader("Content-Type", "url-encoded-form");
        params.addQueryStringParameter("comment", instruction);
        // 3.9.0xUtils 默认设置content-type有问题，暂时传StringBody类型
        JSONObject paramObj = new JSONObject();
        try {
            paramObj.put("new", "");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        params.setBodyContent(paramObj.toString());
        params.setBodyContentType("url-encoded-form");
        HttpUtils.request(context, CloudHttpMethod.POST, params, new BaseModuleAPICallback(context, completeUrl) {
            @Override
            public void callbackSuccess(byte[] arg0) {
                apiInterface.returnNewsInstructionSuccess(new GetNewsInstructionResult(new String(arg0)));
            }

            @Override
            public void callbackFail(String error, int responseCode) {
                apiInterface.returnNewsInstructionFail(error, responseCode);
            }

            @Override
            public void callbackTokenExpire(long requestTime) {
                OauthCallBack oauthCallBack = new OauthCallBack() {
                    @Override
                    public void reExecute() {
                        sendNewsInstruction(newsId, instruction);
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
