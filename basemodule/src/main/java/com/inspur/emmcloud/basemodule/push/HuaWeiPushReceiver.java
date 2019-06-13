package com.inspur.emmcloud.basemodule.push;

import android.app.NotificationManager;
import android.content.Context;
import android.os.Bundle;

import com.huawei.hms.support.api.push.PushReceiver;
import com.inspur.emmcloud.baselib.router.Router;
import com.inspur.emmcloud.baselib.util.PreferencesUtils;
import com.inspur.emmcloud.basemodule.config.Constant;
import com.inspur.emmcloud.basemodule.util.ClientIDUtils;
import com.inspur.emmcloud.basemodule.util.ECMTransparentUtils;
import com.inspur.emmcloud.componentservice.communication.CommunicationService;

/**
 * Created by yufuchang on 2017/6/20.
 */
public class HuaWeiPushReceiver extends PushReceiver {
    /**
     * 连接上华为服务时会调用,可以获取token值
     *
     * @param context
     * @param token
     * @param extras
     */
    @Override
    public void onToken(Context context, String token, Bundle extras) {
        PreferencesUtils.putString(context, Constant.HUAWEI_PUSH_TOKEN, token);
        PushManagerUtils.getInstance().registerPushId2Emm();
        new ClientIDUtils(context).upload();
        Router router = Router.getInstance();
        if (router.getService(CommunicationService.class) != null) {
            CommunicationService service = router.getService(CommunicationService.class);
            service.startWebSocket();
        }
    }

    /**
     * 透传消息的回调方法
     *
     * @param context
     * @param msg
     * @param bundle
     * @return
     */
    @Override
    public boolean onPushMsg(Context context, byte[] msg, Bundle bundle) {
        ECMTransparentUtils.handleTransparentMsg(context, msg);
        return false;
    }


    /**
     * 自定义的消息的回调方法，自定义消息的字数应该是没有限制，目前测试到160字
     * 通知文本的内容有字数限制80字
     *
     * @param context
     * @param event
     * @param extras
     */
    @Override
    public void onEvent(Context context, PushReceiver.Event event, Bundle extras) {
        super.onEvent(context, event, extras);
        if (Event.NOTIFICATION_OPENED.equals(event) || Event.NOTIFICATION_CLICK_BTN.equals(event)) {
            NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            manager.cancelAll();
        }
    }

    /**
     * 连接状态的回调方法
     *
     * @param context
     * @param pushState
     */
    @Override
    public void onPushState(Context context, boolean pushState) {
    }

}
