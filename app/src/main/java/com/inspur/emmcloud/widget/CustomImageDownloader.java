package com.inspur.emmcloud.widget;

import android.content.Context;

import com.inspur.emmcloud.MyApplication;
import com.inspur.emmcloud.util.privates.AppUtils;
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
        if (MyApplication.getInstance().getToken() != null) {
            connection.setRequestProperty("Authorization", MyApplication.getInstance().getToken());
        }
        connection.setRequestProperty(
                "User-Agent",
                "Android/" + AppUtils.getReleaseVersion() + "("
                        + AppUtils.GetChangShang() + " " + AppUtils.GetModel()
                        + ") " + "CloudPlus_Phone/"
                        + AppUtils.getVersion(MyApplication.getInstance()));
        connection.setRequestProperty("X-Device-ID",
                AppUtils.getMyUUID(MyApplication.getInstance()));
        if (MyApplication.getInstance().getCurrentEnterprise() != null) {
            connection.setRequestProperty("X-ECC-Current-Enterprise", MyApplication.getInstance().getCurrentEnterprise().getId());
        }
        return connection;
    }
}
