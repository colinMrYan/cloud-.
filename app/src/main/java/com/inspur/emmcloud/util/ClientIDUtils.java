package com.inspur.emmcloud.util;

import android.content.Context;

import com.inspur.emmcloud.api.APIInterfaceInstance;
import com.inspur.emmcloud.api.apiservice.AppAPIService;
import com.inspur.emmcloud.bean.GetClientIdRsult;
import com.inspur.emmcloud.interf.CommonCallBack;
import com.inspur.emmcloud.config.Constant;

/**
 * Created by chenmch on 2017/10/10.
 */

public class ClientIDUtils {
    private Context context;
    private CommonCallBack commonCallBack;
    public ClientIDUtils(Context context,CommonCallBack commonCallBack){
        this.context = context;
        this.commonCallBack = commonCallBack;
    }

    public void getClientID(){
        if (!checkClientIdNotExit() && commonCallBack != null){
            commonCallBack.execute();
        }else {
            getClientId();
        }
    }

    /**
     * 获取clientId
     */
    private void getClientId() {
        AppAPIService appAPIService = new AppAPIService(context);
        appAPIService.setAPIInterface(new WebService());
        if (NetUtils.isNetworkConnected(context, false)) {
            appAPIService.getClientId(AppUtils.getMyUUID(context), AppUtils.GetChangShang());
        }
    }

    /**
     * 检查clientId是否已经存在
     *
     * @return
     */
    private boolean checkClientIdNotExit() {
        String clientId = PreferencesByUserAndTanentUtils.getString(context, Constant.PREF_REACT_NATIVE_CLIENTID, "");
        return StringUtils.isBlank(clientId);
    }

    private class WebService extends APIInterfaceInstance{
        @Override
        public void returnGetClientIdResultSuccess(GetClientIdRsult getClientIdRsult) {
            PreferencesByUserAndTanentUtils.putString(context, Constant.PREF_REACT_NATIVE_CLIENTID, getClientIdRsult.getClientId());
            if (commonCallBack != null){
                commonCallBack.execute();
            }
        }

        @Override
        public void returnGetClientIdResultFail(String error, int errorCode) {
        }
    }
}
