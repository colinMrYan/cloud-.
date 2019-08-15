package com.inspur.emmcloud.basemodule.util;

import com.inspur.emmcloud.basemodule.application.BaseApplication;

import org.xutils.common.Callback;
import org.xutils.common.Callback.Cancelable;
import org.xutils.http.RequestParams;
import org.xutils.x;

import java.io.File;

/**
 * 下载文件模块 需要传入
 */
public class UpLoaderUtils {

    private static Cancelable cancelable;

    /**
     *
     * @param url
     * @param upLoadFileKey  上传文件得key
     * @param file   需要上传得文件
     * @param callback   进度回调
     * @return
     */
    public static Cancelable uploadFile(String url, String upLoadFileKey, File file, Callback.ProgressCallback callback) {
        RequestParams params = BaseApplication.getInstance()
                .getHttpRequestParams(url);
        params.setMultipart(true);  //以表单方式上传
        params.addBodyParameter(upLoadFileKey, file);  //设置上传文件路径
        cancelable = x.http().post(params, callback);

        return cancelable;
    }
}
