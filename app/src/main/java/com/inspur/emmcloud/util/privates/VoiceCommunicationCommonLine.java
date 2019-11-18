package com.inspur.emmcloud.util.privates;

import com.inspur.emmcloud.basemodule.application.BaseApplication;
import com.inspur.emmcloud.basemodule.config.Constant;
import com.inspur.emmcloud.bean.chat.GetVoiceAndVideoResult;
import com.inspur.emmcloud.bean.chat.VoiceCommunicationJoinChannelInfoBean;
import com.inspur.emmcloud.ui.chat.VoiceCommunicationActivity;

/**
 * socket和声网的交叉逻辑和Invite逻辑（声网没有invite）
 * Created by: yufuchang
 * Date: 2019/11/14
 */
public class VoiceCommunicationCommonLine {

    /**
     * 处理socket邀请消息
     *
     * @param customProtocol
     * @param getVoiceAndVideoResult
     */
    public static void onInvite(CustomProtocol customProtocol, GetVoiceAndVideoResult getVoiceAndVideoResult) {
        String agoraChannelId = customProtocol.getParamMap().get(Constant.COMMAND_ROOM_ID);
        //判断如果在通话中就不再接听新的来电
        if (!VoiceCommunicationManager.getInstance().isVoiceBusy()) {
            VoiceCommunicationManager.getInstance().getChannelInfoByChannelId(agoraChannelId, getVoiceAndVideoResult.getContextParamsType(), getVoiceAndVideoResult.getChannel());
        } else {
            String channelId = customProtocol.getParamMap().get(Constant.COMMAND_CHANNEL_ID);
            String fromUid = customProtocol.getParamMap().get(Constant.COMMAND_UID);
            VoiceCommunicationManager.getInstance().sendRefuseMessageToInviter(channelId, agoraChannelId, fromUid);
        }
    }

    /**
     * 处理socket的refuse，destroy，声网的onUserOffline
     *
     * @param agoraId
     * @param state
     */
    public static void onUserReject(int agoraId, int state) {
        boolean isReject = getAgoraIdAllReadyReject(agoraId);
        //防止声网，socket同时走这里，isReject为true，说明socket或者声网已经有一个走了这里，不需要再走一遍
        if (!isReject) {
            VoiceCommunicationManager.getInstance().changeUserConnectStateByAgoraUid(state, agoraId);
            VoiceCommunicationToastUtil.showToast(false, getCloudPlusUidByAgoraId(agoraId));
            if (VoiceCommunicationManager.getInstance().getWaitAndConnectedNumber() < 2) {
                VoiceCommunicationManager.getInstance().handleDestroy();
                //防止在声网回调和小窗打开Activity同步进行，接收到回调没关上Activity的情况
                if (BaseApplication.getInstance().isActivityExist(VoiceCommunicationActivity.class)) {
                    BaseApplication.getInstance().closeActivity(VoiceCommunicationActivity.class.getSimpleName());
                }
            }
            if (VoiceCommunicationManager.getInstance().getOnVoiceCommunicationCallbacks() != null) {
                VoiceCommunicationManager.getInstance().getOnVoiceCommunicationCallbacks().onRefreshUserState();
            }
        }
    }

    /**
     * 根据声网uid获取云+uid
     *
     * @param agoraId
     * @return
     */
    private static String getCloudPlusUidByAgoraId(int agoraId) {
        for (VoiceCommunicationJoinChannelInfoBean voiceCommunicationJoinChannelInfoBean : VoiceCommunicationManager.getInstance().getVoiceCommunicationMemberList()) {
            if (voiceCommunicationJoinChannelInfoBean.getAgoraUid() == agoraId) {
                return voiceCommunicationJoinChannelInfoBean.getUserId();
            }
        }
        return "";
    }

    /**
     * 获取这个agordId是不是已经拒绝或者离开，防止socket和声网都有回调，提示两次
     *
     * @param agoraId
     * @return
     */
    private static boolean getAgoraIdAllReadyReject(int agoraId) {
        for (VoiceCommunicationJoinChannelInfoBean voiceCommunicationJoinChannelInfoBean : VoiceCommunicationManager.getInstance().getVoiceCommunicationMemberList()) {
            int state = voiceCommunicationJoinChannelInfoBean.getConnectState();
            if (voiceCommunicationJoinChannelInfoBean.getAgoraUid() == agoraId && (
                    state == VoiceCommunicationJoinChannelInfoBean.CONNECT_STATE_REFUSE ||
                            state == VoiceCommunicationJoinChannelInfoBean.CONNECT_STATE_LEAVE)) {
                return true;
            }
        }
        return false;
    }
}
