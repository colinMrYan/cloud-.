package com.inspur.emmcloud.util.privates;

import com.inspur.emmcloud.R;
import com.inspur.emmcloud.baselib.util.StringUtils;
import com.inspur.emmcloud.baselib.util.ToastUtils;
import com.inspur.emmcloud.basemodule.application.BaseApplication;
import com.inspur.emmcloud.componentservice.communication.Conversation;
import com.inspur.emmcloud.ui.chat.VoiceCommunicationActivity;
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
                        case VoiceCommunicationActivity.COMMUNICATION_STATE_PRE:
                            //自己挂断，单聊，未接通
                            if (VoiceCommunicationManager.getInstance().isInviter()) {
                                ToastUtils.show(R.string.voice_communication_direct_call_canceled);
                            } else {
                                ToastUtils.show(R.string.voice_communication_direct_calling_reject);
                            }
                            break;
                        case VoiceCommunicationActivity.COMMUNICATION_STATE_ING:
                            //自己挂断，单聊，通话中
                            if (VoiceCommunicationManager.getInstance().isInviter()) {
                                ToastUtils.show(R.string.voice_communication_direct_calling_ended);
                            } else {
                                ToastUtils.show(R.string.voice_communication_direct_calling_reject);
                            }
                            break;
                        default:
                            break;
                    }
                    break;
                case Conversation.TYPE_GROUP:
                    switch (VoiceCommunicationManager.getInstance().getCommunicationState()) {
                        case VoiceCommunicationActivity.COMMUNICATION_STATE_PRE:
                            //自己挂断，群聊，未接通
                            ToastUtils.show(R.string.voice_communication_group_calling_ended);
                            break;
                        case VoiceCommunicationActivity.COMMUNICATION_STATE_ING:
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
                        case VoiceCommunicationActivity.COMMUNICATION_STATE_PRE:
                            if (VoiceCommunicationManager.getInstance().isInviter()) {
                                ToastUtils.show(R.string.voice_communication_direct_call_audio_call_request_declined);
                            } else {
                                ToastUtils.show(R.string.voice_communication_direct_calling_canceled);
                            }
                            break;
                        case VoiceCommunicationActivity.COMMUNICATION_STATE_ING:
                            //对方挂断，单聊
                            if (VoiceCommunicationManager.getInstance().isInviter()) {
                                ToastUtils.show(R.string.voice_communication_direct_calling_canceled);
                            } else {
                                ToastUtils.show(R.string.voice_communication_direct_calling_ended);
                            }
                            break;
                        default:
                            break;
                    }
                    break;
                case Conversation.TYPE_GROUP:
                    switch (VoiceCommunicationManager.getInstance().getCommunicationState()) {
                        case VoiceCommunicationActivity.COMMUNICATION_STATE_PRE:
                            String name = ContactUserCacheUtils.getUserName(cloudPlusUid);
                            if (VoiceCommunicationManager.getInstance().isInviter() && !StringUtils.isBlank(name)) {
                                ToastUtils.show(BaseApplication.getInstance().getString(R.string.voice_communication_group_call_busy, name));
                            }
                            break;
                        case VoiceCommunicationActivity.COMMUNICATION_STATE_ING:
                            if (cloudPlusUid.equals(BaseApplication.getInstance().getUid())) {
                                ToastUtils.show(R.string.voice_communication_group_call_canceled);
                            }
                            break;
//                            ToastUtils.show("无提示");
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
