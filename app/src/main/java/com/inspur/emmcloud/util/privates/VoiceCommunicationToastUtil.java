package com.inspur.emmcloud.util.privates;

import com.inspur.emmcloud.R;
import com.inspur.emmcloud.baselib.util.ToastUtils;
import com.inspur.emmcloud.basemodule.application.BaseApplication;
import com.inspur.emmcloud.ui.chat.ChannelVoiceCommunicationActivity;

public class VoiceCommunicationToastUtil {

    public static void showToast(String type) {
        if (BaseApplication.getInstance().isActivityExist(ChannelVoiceCommunicationActivity.class)) {
            return;
        }

        String endTip = BaseApplication.getInstance().getString(R.string.voice_communication_end);
        if (type.equals("destroy")) {
            ToastUtils.show(endTip);
        } else if (type.equals("refuse")) {
            if (VoiceCommunicationManager.getInstance().getVoiceCommunicationMemberList().size() == 2) {    //单聊时
                ToastUtils.show(endTip);
            } else if (!SuspensionWindowManagerUtils.getInstance().isShowing()) {
                ToastUtils.show(endTip);
            }
        }
//        TODO 统一整理状态
//        if (type.equals("refuse")) {        //收到拒绝消息
//            if (VoiceCommunicationManager.getInstance().isInviter()) {      //邀请者
//                if (VoiceCommunicationManager.getInstance().getVoiceCommunicationMemberList().size() == 2) {    //单聊时
//                    switch (VoiceCommunicationManager.getInstance().getCommunicationState()) {
//                        case COMMUNICATION_STATE_PRE:
//                            ToastUtils.show("聊天已取消");
//                            break;
//                        case COMMUNICATION_STATE_ING:
//                            ToastUtils.show("聊天结束");
//                            break;
//                        case COMMUNICATION_STATE_OVER:
//                            ToastUtils.show("通话已结束");
//                            break;
//                        default:
//                            break;
//                    }
//                } else {    //TODO：群聊时暂不提示
//                    switch (VoiceCommunicationManager.getInstance().getCommunicationState()) {
//                        case COMMUNICATION_STATE_OVER:
//                            ToastUtils.show("通话已结束");
//                            break;
//                    }
//                }
//            } else {        //被邀请者
//                if (VoiceCommunicationManager.getInstance().getVoiceCommunicationMemberList().size() == 2) {
//                    ToastUtils.show("对方以挂断，通话结束");
//                }
//            }
//
//        } else if (type.equals("destroy")) {        //收到拒绝消息
//            if (VoiceCommunicationManager.getInstance().isInviter()) {      //邀请者
//                switch (VoiceCommunicationManager.getInstance().getCommunicationState()) {
//                    case COMMUNICATION_STATE_PRE:
//                        ToastUtils.show("聊天已取消");
//                        break;
//                    case COMMUNICATION_STATE_ING:
//                        //单聊时提示   TODO：群聊时暂不提示
//                        if (VoiceCommunicationManager.getInstance().getVoiceCommunicationMemberList().size() == 2) {
//                            ToastUtils.show("聊天结束");
//                        }
//                        break;
//                    case COMMUNICATION_STATE_OVER:
//                        ToastUtils.show("聊天结束");
//                        break;
//                    default:
//                        break;
//                }
//
//            } else {            //被邀请者
//                if (VoiceCommunicationManager.getInstance().getVoiceCommunicationMemberList().size() == 2) {
//                    ToastUtils.show("对方以挂断，通话结束");
//                }
//            }
//        }
    }
}
