package com.inspur.emmcloud.util.privates;

import android.content.Context;

import com.inspur.emmcloud.MyApplication;
import com.inspur.emmcloud.api.apiservice.ChatAPIService;
import com.inspur.emmcloud.util.common.NetUtils;
import com.inspur.emmcloud.util.common.PreferencesUtils;
import com.inspur.emmcloud.util.common.StringUtils;

/**
 * 推送信息
 */

public class PushInfoUtils {
    private Context context;
    public PushInfoUtils(Context context){
        this.context = context;
    }

    public void upload(){
        if (NetUtils.isNetworkConnected(context,false) && MyApplication.getInstance().isIndexActivityRunning()){
            String pushTracer = AppUtils.getPushId(context);
            if (!StringUtils.isBlank(pushTracer)){
                ChatAPIService apiService = new ChatAPIService(context);
                String deviceId = AppUtils.getMyUUID(context);
                String deviceName = AppUtils.getDeviceName(context);
                String pushProvider = getPushProvider(context);
                apiService.uploadPushInfo(deviceId,deviceName,pushProvider,pushTracer);
            }
        }
    }

    /**
     * 获取pushProvider
     * @param context
     * @return
     */
    private String getPushProvider(Context context){
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
            default:
                pushProvider = "cn.jpush";
                    break;
        }
        return pushProvider;
    }
}
