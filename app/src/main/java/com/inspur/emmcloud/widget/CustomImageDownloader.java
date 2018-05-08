package com.inspur.emmcloud.widget;

import android.content.Context;

import com.inspur.emmcloud.MyApplication;
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
        connection.setRequestProperty("Authorization", MyApplication.getInstance().getToken());
        return connection;
    }
}
