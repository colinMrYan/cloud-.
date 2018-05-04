package com.inspur.emmcloud.api;

import android.content.Context;

import com.inspur.emmcloud.R;
import com.inspur.emmcloud.widget.dialogs.MyQMUIDialog;
import com.qmuiteam.qmui.widget.dialog.QMUIDialog;
import com.qmuiteam.qmui.widget.dialog.QMUIDialogAction;

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
                distributeRequest(context,HttpMethod.GET,params,callback);
                break;
            case POST:
                distributeRequest(context,HttpMethod.POST,params,callback);
                break;
            case PUT:
                distributeRequest(context,HttpMethod.PUT,params,callback);
                break;
            case PATCH:
                distributeRequest(context,HttpMethod.PATCH,params,callback);
                break;
            case HEAD:
                distributeRequest(context,HttpMethod.HEAD,params,callback);
                break;
            case MOVE:
                distributeRequest(context,HttpMethod.MOVE,params,callback);
                break;
            case COPY:
                distributeRequest(context,HttpMethod.COPY,params,callback);
                break;
            case DELETE:
                distributeRequest(context,HttpMethod.DELETE,params,callback);
                break;
            case OPTIONS:
                distributeRequest(context,HttpMethod.OPTIONS,params,callback);
                break;
            case TRACE:
                distributeRequest(context,HttpMethod.TRACE,params,callback);
                break;
            case CONNECT:
                distributeRequest(context,HttpMethod.CONNECT,params,callback);
                break;
        }
    }

    /**
     * 分发的请求
     * @param httpMethod
     * @param params
     * @param callback
     */
    private static void distributeRequest(Context context,HttpMethod httpMethod, RequestParams params,  APICallback callback) {
        if(isValidUrl(params)){
            x.http().request(httpMethod,params,callback);
        }else{
            new MyQMUIDialog.MessageDialogBuilder(context)
                    .setMessage(context.getString(R.string.cluster_no_permission))
                    .addAction(R.string.ok, new QMUIDialogAction.ActionListener() {
                        @Override
                        public void onClick(QMUIDialog dialog, int index) {
                            dialog.dismiss();
                        }
                    })
                    .show();
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
