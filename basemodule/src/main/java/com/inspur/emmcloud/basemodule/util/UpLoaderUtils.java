package com.inspur.emmcloud.basemodule.util;

import com.inspur.emmcloud.basemodule.api.APIDownloadCallBack;
import com.inspur.emmcloud.basemodule.application.BaseApplication;

import org.xutils.common.Callback;
import org.xutils.common.Callback.Cancelable;
import org.xutils.http.RequestParams;
import org.xutils.http.app.RedirectHandler;
import org.xutils.http.request.UriRequest;
import org.xutils.x;

import java.io.File;

/**
 * 下载文件模块 需要传入
 */
public class UpLoaderUtils {

    Cancelable cancelable;

    /**
     * 开始下载方法
     *
     * @param source
     * @param callback
     */
    public Cancelable startUpLoad(String source, Callback.CommonCallback<File> callback) {

        final RequestParams params = BaseApplication.getInstance().getHttpRequestParams(source);
        params.setAutoResume(true);// 断点下载
        params.setCancelFast(true);
        params.setRedirectHandler(new RedirectHandler() {
            @Override
            public RequestParams getRedirectParams(UriRequest uriRequest) throws Throwable {
                String locationUrl = uriRequest.getResponseHeader("Location");
                params.setUri(locationUrl);
                return params;
            }
        });
        if (callback == null) {
            callback = new APIDownloadCallBack(source) {

                @Override
                public void callbackSuccess(File file) {
                }

                @Override
                public void callbackError(Throwable arg0, boolean arg1) {
                }
            };
        }
        cancelable = x.http().get(params, callback);
        return cancelable;
    }
}
