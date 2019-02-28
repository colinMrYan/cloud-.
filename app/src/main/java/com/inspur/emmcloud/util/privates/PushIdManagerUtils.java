package com.inspur.emmcloud.util.privates;

import android.content.Context;

import com.inspur.emmcloud.MyApplication;
import com.inspur.emmcloud.api.apiservice.AppAPIService;
import com.inspur.emmcloud.util.common.NetUtils;

/**
 * Created by yufuchang on 2018/9/25.
 */

public class PushIdManagerUtils {
    private Context context;

    public PushIdManagerUtils(Context context) {
        this.context = context;
    }

    /**
     * 向Emm注册pushId，如果进入应用不为空则已经注册过，直接上传
     * 如果为空则等待Jpush或者其他推送注册成功后上传token
     */
    public void registerPushId2Emm() {
        if (!MyApplication.getInstance().isHaveLogin()) {
            return;
        }
        if (NetUtils.isNetworkConnected(context, false)) {
            String pushId = AppUtils.getPushId(context);
            if (!pushId.equals("UNKNOWN")) {
                AppAPIService appAPIService = new AppAPIService(context);
                appAPIService.registerPushToken();
            }
        }
    }

    /**
     * 向Emm解除注册pushId
     */
    public void unregisterPushId2Emm() {
        if (NetUtils.isNetworkConnected(context, false)) {
            AppAPIService appAPIService = new AppAPIService(context);
            appAPIService.unregisterPushToken();
        }
    }
}
