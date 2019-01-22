package com.inspur.emmcloud.util.privates;

import com.inspur.emmcloud.MyApplication;
import com.inspur.emmcloud.api.APIDownloadCallBack;

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
public class DownLoaderUtils {

    Cancelable cancelable;

    /**
     * 开始下载方法
     *
     * @param source
     * @param saveFilePath
     * @param callback
     */
    public Cancelable startDownLoad(String source, final String saveFilePath,
                                    Callback.CommonCallback<File> callback) {

        final RequestParams params = MyApplication.getInstance().getHttpRequestParams(source);
        params.setAutoResume(true);// 断点下载
        params.setSaveFilePath(saveFilePath);
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
