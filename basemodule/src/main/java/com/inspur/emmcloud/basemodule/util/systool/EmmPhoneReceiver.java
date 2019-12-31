package com.inspur.emmcloud.basemodule.util.systool;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;

import com.inspur.emmcloud.baselib.router.Router;
import com.inspur.emmcloud.componentservice.communication.CommunicationService;

/**
 * 小米6上测试，来电，去电，接听action都是：android.intent.action.PHONE_STATE
 * 此广播需要授予android.permission.READ_PHONE_STATE,否则监听不到广播，如果有权限能监听到广播，以下代码有效
 * 无权限接收不到广播则不关心系统电话对云+语音通话的影响
 * Created by: yufuchang
 * Date: 2019/11/29
 */
public class EmmPhoneReceiver extends BroadcastReceiver {
    //微信7.0.9语音通话中，有系统电话拨入，当系统电话接听时，挂断微信语音通话
    //微信7.0.9语音通话中，给别人拨打系统电话，微信语音通话立即挂断
    @Override
    public void onReceive(Context context, Intent intent) {
        //如果是去电
        if (intent.getAction().equals(Intent.ACTION_NEW_OUTGOING_CALL)) {
            Router router = Router.getInstance();
            if (router.getService(CommunicationService.class) != null) {
                CommunicationService service = router.getService(CommunicationService.class);
                service.stopVoiceCommunication();
            }
        } else {
            //注释来自网络资料
            //查了下android文档，貌似没有专门用于接收来电的action,所以，非去电即来电.
            //如果我们想要监听电话的拨打状况，需要这么几步 :
            //第一：获取电话服务管理器TelephonyManager manager = this.getSystemService(TELEPHONY_SERVICE);
            //第二：通过TelephonyManager注册我们要监听的电话状态改变事件。manager.listen(new EmmPhoneStateLinstener(),
            //PhoneStateListener.LISTEN_CALL_STATE);这里的PhoneStateListener.LISTEN_CALL_STATE就是我们想要
            //监听的状态改变事件，除此之外，还有很多其他事件。
            //第三步：通过extends PhoneStateListener来定制自己的规则。将其对象传递给第二步作为参数。
            //第四步：这一步很重要，那就是给应用添加权限。android.permission.READ_PHONE_STATE
            TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Service.TELEPHONY_SERVICE);
            telephonyManager.listen(new EmmPhoneStateLinstener(), PhoneStateListener.LISTEN_CALL_STATE);
        }
    }
}
