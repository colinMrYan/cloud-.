package com.inspur.emmcloud.api;

import android.content.Context;
import android.content.Intent;

import com.inspur.emmcloud.baselib.util.LogUtils;
import com.inspur.emmcloud.baselib.util.PreferencesUtils;
import com.inspur.emmcloud.basemodule.util.AppExceptionCacheUtils;
import com.inspur.emmcloud.basemodule.util.AppUtils;
import com.inspur.emmcloud.ui.mine.setting.NoPermissionDialogActivity;

import org.xutils.http.HttpMethod;
import org.xutils.http.RequestParams;
import org.xutils.x;

/**
 * Created by yufuchang on 2018/5/3.
 */

public class HttpUtils {
    public static void request(Context context, CloudHttpMethod cloudHttpMethod, RequestParams params, APICallback callback) {
        switch (cloudHttpMethod) {
            case GET:
                distributeRequest(context, HttpMethod.GET, params, callback);
                break;
            case POST:
                distributeRequest(context, HttpMethod.POST, params, callback);
                break;
            case PUT:
                distributeRequest(context, HttpMethod.PUT, params, callback);
                break;
            case PATCH:
                distributeRequest(context, HttpMethod.PATCH, params, callback);
                break;
            case HEAD:
                distributeRequest(context, HttpMethod.HEAD, params, callback);
                break;
            case MOVE:
                distributeRequest(context, HttpMethod.MOVE, params, callback);
                break;
            case COPY:
                distributeRequest(context, HttpMethod.COPY, params, callback);
                break;
            case DELETE:
                distributeRequest(context, HttpMethod.DELETE, params, callback);
                break;
            case OPTIONS:
                distributeRequest(context, HttpMethod.OPTIONS, params, callback);
                break;
            case TRACE:
                distributeRequest(context, HttpMethod.TRACE, params, callback);
                break;
            case CONNECT:
                distributeRequest(context, HttpMethod.CONNECT, params, callback);
                break;
        }
    }

    /**
     * 分发的请求
     *
     * @param httpMethod
     * @param params
     * @param callback
     */
    private static void distributeRequest(Context context, HttpMethod httpMethod, RequestParams params, APICallback callback) {
        if (isValidUrl(params)) {
            x.http().request(httpMethod, params, callback);
        } else {
            LogUtils.jasonDebug("params.getUri()=" + params.getUri());
            AppExceptionCacheUtils.saveAppException(context, 8, "", params.getUri(), 0);
            AppExceptionCacheUtils.saveAppClusterException(context, 8, PreferencesUtils.getString(context,
                    "myInfo", ""), "clusters", 0);
            callback.callbackFail("", -1);
            if (AppUtils.isAppOnForeground(context)) {
                Intent intent = new Intent();
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.setClass(context, NoPermissionDialogActivity.class);
                context.startActivity(intent);
            }

        }
    }

    /**
     * 判断是不是一个有效的地址
     *
     * @param params
     * @return
     */
    private static boolean isValidUrl(RequestParams params) {
        return params.getUri().startsWith("http");
    }

}
