package com.inspur.emmcloud.basemodule.util;

import android.content.Context;

import com.inspur.emmcloud.baselib.util.StringUtils;
import com.inspur.emmcloud.basemodule.application.BaseApplication;
import com.nostra13.universalimageloader.core.download.BaseImageDownloader;

import java.io.IOException;
import java.net.HttpURLConnection;

/**
 * Imageloader支持添加header
 */

public class CustomImageDownloader extends BaseImageDownloader {
    // 需在application中进行配置添加此类。
    public CustomImageDownloader(Context context) {
        super(context);
    }

    @Override
    protected HttpURLConnection createConnection(String url, Object extra)
            throws IOException {
        HttpURLConnection connection = super.createConnection(url, extra);
        //HttpURLConnection设置链接不遵循重定向，因为有些url请求时重定向之前和重定向之后header不同。
        connection.setInstanceFollowRedirects(false);
        if (BaseApplication.getInstance().getToken() != null && !StringUtils.isBlank(url)) {
            if (url.contains("/file/request?path=")) {
                connection.setRequestProperty("Authorization", BaseApplication.getInstance().getToken());
                connection.setRequestProperty(
                        "User-Agent",
                        "Android/" + AppUtils.getReleaseVersion() + "("
                                + AppUtils.GetChangShang() + " " + AppUtils.GetModel()
                                + ") " + "CloudPlus_Phone/"
                                + AppUtils.getVersion(BaseApplication.getInstance()));
                connection.setRequestProperty("X-Device-ID",
                        AppUtils.getMyUUID(BaseApplication.getInstance()));
                if (BaseApplication.getInstance().getCurrentEnterprise() != null) {
                    connection.setRequestProperty("X-ECC-Current-Enterprise", BaseApplication.getInstance().getCurrentEnterprise().getId());
                }
            }
        }
        return connection;
    }
}
