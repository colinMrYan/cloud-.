package com.inspur.emmcloud.util.privates;

import com.inspur.emmcloud.baselib.util.StringUtils;
import com.inspur.emmcloud.baselib.util.ToastUtils;
import com.inspur.emmcloud.basemodule.application.BaseApplication;
import com.inspur.emmcloud.bean.chat.Conversation;
import com.inspur.emmcloud.ui.chat.ChannelVoiceCommunicationActivity;
import com.inspur.emmcloud.util.privates.cache.ContactUserCacheUtils;
import com.inspur.emmcloud.util.privates.cache.ConversationCacheUtils;

public class VoiceCommunicationToastUtil {

    public static void showToast(boolean isMySelfHungUp, String cloudPlusUid) {
        String directOrGroup = ConversationCacheUtils.getConversationType(BaseApplication.getInstance(),
                VoiceCommunicationManager.getInstance().getCloudPlusChannelId());
        if (isMySelfHungUp) {
            switch (directOrGroup) {
                case Conversation.TYPE_DIRECT:
                    switch (VoiceCommunicationManager.getInstance().getCommunicationState()) {
                        case ChannelVoiceCommunicationActivity.COMMUNICATION_STATE_OVER:
                        case ChannelVoiceCommunicationActivity.COMMUNICATION_STATE_PRE:
                            //自己挂断，单聊，未接通
                            ToastUtils.show("聊天已取消");
                            break;
                        case ChannelVoiceCommunicationActivity.COMMUNICATION_STATE_ING:
                            //自己挂断，单聊，通话中
                            ToastUtils.show("聊天结束");
                            break;
                        default:
                            break;
                    }
                    break;
                case Conversation.TYPE_GROUP:
                    switch (VoiceCommunicationManager.getInstance().getCommunicationState()) {
                        case ChannelVoiceCommunicationActivity.COMMUNICATION_STATE_OVER:
                        case ChannelVoiceCommunicationActivity.COMMUNICATION_STATE_PRE:
                            //自己挂断，群聊，未接通
                            ToastUtils.show("通话已结束");
                            break;
                        case ChannelVoiceCommunicationActivity.COMMUNICATION_STATE_ING:
//                            ToastUtils.show("无提示");
                            break;
                        default:
                            break;
                    }
                    break;
                default:
                    break;
            }
        } else {
            switch (directOrGroup) {
                case Conversation.TYPE_DIRECT:
                    switch (VoiceCommunicationManager.getInstance().getCommunicationState()) {
                        case ChannelVoiceCommunicationActivity.COMMUNICATION_STATE_OVER:
                        case ChannelVoiceCommunicationActivity.COMMUNICATION_STATE_PRE:
                        case ChannelVoiceCommunicationActivity.COMMUNICATION_STATE_ING:
                            //对方挂断，单聊
                            ToastUtils.show("对方已挂断，通话结束");
                            break;
                        default:
                            break;
                    }
                    break;
                case Conversation.TYPE_GROUP:
                    switch (VoiceCommunicationManager.getInstance().getCommunicationState()) {
                        case ChannelVoiceCommunicationActivity.COMMUNICATION_STATE_OVER:
                        case ChannelVoiceCommunicationActivity.COMMUNICATION_STATE_PRE:
                            String name = ContactUserCacheUtils.getUserName(cloudPlusUid);
                            if (!StringUtils.isBlank(name)) {
                                ToastUtils.show(name + "正忙");
                            }
                            break;
                        case ChannelVoiceCommunicationActivity.COMMUNICATION_STATE_ING:
//                            ToastUtils.show("无提示");
                            break;
                        default:
                            break;
                    }
                    break;
                default:
                    break;
            }
        }
    }
}
