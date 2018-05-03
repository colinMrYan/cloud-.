package com.inspur.emmcloud.api;

import android.content.Context;

import com.inspur.emmcloud.util.common.LogUtils;

import org.xutils.http.HttpMethod;
import org.xutils.http.RequestParams;
import org.xutils.x;

/**
 * Created by yufuchang on 2018/5/3.
 */

public class HttpUtils {
    public static void request(Context context,CloudHttpMethod cloudHttpMethod, RequestParams params,  APICallback callback){
        switch (cloudHttpMethod){
            case GET:
                distributeRequest(HttpMethod.GET,params,callback);
                break;
            case POST:
                distributeRequest(HttpMethod.POST,params,callback);
                break;
            case PUT:
                distributeRequest(HttpMethod.PUT,params,callback);
                break;
            case PATCH:
                distributeRequest(HttpMethod.PATCH,params,callback);
                break;
            case HEAD:
                distributeRequest(HttpMethod.HEAD,params,callback);
                break;
            case MOVE:
                distributeRequest(HttpMethod.MOVE,params,callback);
                break;
            case COPY:
                distributeRequest(HttpMethod.COPY,params,callback);
                break;
            case DELETE:
                distributeRequest(HttpMethod.DELETE,params,callback);
                break;
            case OPTIONS:
                distributeRequest(HttpMethod.OPTIONS,params,callback);
                break;
            case TRACE:
                distributeRequest(HttpMethod.TRACE,params,callback);
                break;
            case CONNECT:
                distributeRequest(HttpMethod.CONNECT,params,callback);
                break;
        }
    }

    /**
     * 分发的请求
     * @param httpMethod
     * @param params
     * @param callback
     */
    private static void distributeRequest(HttpMethod httpMethod, RequestParams params,  APICallback callback) {
        if(isValidUrl(params)){
            x.http().request(httpMethod,params,callback);
        }else{
            LogUtils.YfcDebug("不是一个有效的地址");
        }
    }

    /**
     * 判断是不是一个有效的地址
     * @param params
     * @return
     */
    private static boolean isValidUrl(RequestParams params){
        return params.getUri().startsWith("http");
    }

}
