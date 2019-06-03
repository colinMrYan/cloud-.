package com.inspur.emmcloud.util.privates;

import android.content.Context;

import com.inspur.emmcloud.R;
import com.inspur.emmcloud.baselib.util.StringUtils;
import com.inspur.emmcloud.baselib.util.ToastUtils;
import com.inspur.emmcloud.bean.system.ECMErrorBean;
import com.inspur.emmcloud.bean.system.EMMErrorBean;

/**
 * 网络请求结果中间件，处理异常
 *
 * @author Administrator
 */
public class WebServiceMiddleUtils {


    /**
     * 处理错误
     *
     * @param context
     * @param response
     * @param errorCode
     */
    public static void hand(Context context, String response,
                            int errorCode) {
        String errorMessage = "";
        if (response.equals("time out")) {
            errorMessage = context.getString(R.string.network_timeout);
        } else if (errorCode == 400) {
            ECMErrorBean ecmErrorBean = new ECMErrorBean(response);
            errorMessage = ErrorCodeUtils.getAlertByCode(context, ecmErrorBean.getErrorCode());
        } else if (errorCode == 500 || errorCode == 403) {
            /*Emm服务器上对错误处理不统一，目前只需处理500，其余返回默认提示，已与emm确认  20170512  yfc*/
            EMMErrorBean emmErrorBean = new EMMErrorBean(response);
            errorMessage = emmErrorBean.getMsg();
        }
        if (StringUtils.isBlank(errorMessage)) {
            errorMessage = context.getString(R.string.net_request_failed);
        }
        ToastUtils.show(context, errorMessage);
    }

}
