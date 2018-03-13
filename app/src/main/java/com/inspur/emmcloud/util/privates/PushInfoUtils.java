package com.inspur.emmcloud.util.privates;

import android.content.Context;

import com.inspur.emmcloud.MyApplication;
import com.inspur.emmcloud.api.APIInterfaceInstance;
import com.inspur.emmcloud.api.apiservice.ChatAPIService;
import com.inspur.emmcloud.bean.chat.GetUploadPushInfoResult;
import com.inspur.emmcloud.config.Constant;
import com.inspur.emmcloud.interf.CommonCallBack;
import com.inspur.emmcloud.util.common.NetUtils;
import com.inspur.emmcloud.util.common.PreferencesUtils;
import com.inspur.emmcloud.util.common.StringUtils;

/**
 * 推送信息
 */

public class PushInfoUtils {
    private Context context;
    private CommonCallBack commonCallBack;
    public PushInfoUtils(Context context){
        this.context = context;
    }
    public PushInfoUtils(Context context,CommonCallBack commonCallBack){
        this.context = context;
        this.commonCallBack=commonCallBack;
    }


    public void upload(){
        if (NetUtils.isNetworkConnected(context,false) && MyApplication.getInstance().isIndexActivityRunning()){
            String pushTracer = AppUtils.getPushId(context);
            if (!StringUtils.isBlank(pushTracer)){
                ChatAPIService apiService = new ChatAPIService(context);
                apiService.setAPIInterface(new WebService());
                String deviceId = AppUtils.getMyUUID(context);
                String deviceName = AppUtils.getDeviceName(context);
                String pushProvider = getPushProvider();
                apiService.uploadPushInfo(deviceId,deviceName,pushProvider,pushTracer);
            }
        }
    }

    public void getChatClientId(){
        String chatClientId = PreferencesByUserAndTanentUtils.getString(context, Constant.PREF_CHAT_CLIENTID, "");
        if (StringUtils.isBlank(chatClientId)){
            upload();
        }else if (commonCallBack != null){
            commonCallBack.execute();
        }

    }

    /**
     * 获取pushProvider
     * @param context
     * @return
     */
    public String getPushProvider(){
        // 华为  com.hicloud.push
        // 极光  cn.jpush
        // 小米  com.xiaomi.xmpush
        // 魅族  com.meizu.api - push
        String pushProvider = "";
        String pushFlag = PreferencesUtils.getString(context,"pushFlag","");
        switch (pushFlag){
            case "huawei":
                pushProvider="com.hicloud.push";
                break;
            case "xiaomi":
                pushProvider="com.xiaomi.xmpush";
                break;
            case "meizu":
                pushProvider="com.meizu.api-push";
                break;
            default:
                pushProvider = "cn.jpush";
                    break;
        }
        return pushProvider;
    }

    private class  WebService extends APIInterfaceInstance{
        @Override
        public void returnUploadPushInfoResultSuccess(GetUploadPushInfoResult getUploadPushInfoResult) {
            String chatClientId = getUploadPushInfoResult.getChatClientId();
            PreferencesByUserAndTanentUtils.putString(context, Constant.PREF_CHAT_CLIENTID, chatClientId);
            if (commonCallBack != null){
                commonCallBack.execute();
            }
        }

        @Override
        public void returnUploadPushInfoResultFail(String error, int errorCode) {
        }
    }
}
