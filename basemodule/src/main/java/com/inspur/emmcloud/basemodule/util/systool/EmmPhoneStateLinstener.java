package com.inspur.emmcloud.basemodule.util.systool;

import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;

import com.inspur.emmcloud.baselib.router.Router;
import com.inspur.emmcloud.componentservice.communication.CommunicationService;

/**
 * 监听来电
 * 小米6上测试
 * 去电响铃state：2
 * 来电响铃state：1
 * 自己打，被接听：没有state
 * 别人打，我接听：state：2
 * 挂断没接到回调
 * Created by: yufuchang
 * Date: 2019/11/29
 */
public class EmmPhoneStateLinstener extends PhoneStateListener {
    @Override
    public void onCallStateChanged(int state, String incomingNumber) {
        //注意，方法必须写在super方法后面，否则incomingNumber无法获取到值，想获取到值仍然需要动态获取电话（接听或挂断电话、监听通话状态）。
        //此处只需要得到电话的忙闲状态，所以暂时不考虑获取以上权限，微信也没有获取此权限，推断微信应该也只得到了电话的忙闲状态
        super.onCallStateChanged(state, incomingNumber);
        //如果电话不是空闲状态，就挂断云+语音通话
        //如果是空闲状态则此处不做处理，走后续云+语音通话逻辑
        switch (state) {
            case TelephonyManager.CALL_STATE_IDLE:
//             System.out.println("挂断");
                break;
            case TelephonyManager.CALL_STATE_OFFHOOK:
//            System.out.println("接听");
                Router router = Router.getInstance();
                if (router.getService(CommunicationService.class) != null) {
                    CommunicationService service = router.getService(CommunicationService.class);
                    service.stopVoiceCommunication();
                }
                break;
            case TelephonyManager.CALL_STATE_RINGING:
//             System.out.println("响铃:来电号码"+incomingNumber);
                //输出来电号码
                break;
        }

//        if (state != TelephonyManager.CALL_STATE_IDLE) {
//            Router router = Router.getInstance();
//            if (router.getService(CommunicationService.class) != null) {
//                CommunicationService service = router.getService(CommunicationService.class);
//                service.stopVoiceCommunication();
//            }
//        }
    }
}
