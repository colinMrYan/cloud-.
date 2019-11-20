package com.inspur.emmcloud.ui.chat;

import com.inspur.emmcloud.bean.chat.VoiceCommunicationAudioVolumeInfo;
import com.inspur.emmcloud.bean.chat.VoiceCommunicationRtcStats;
import com.inspur.emmcloud.interf.OnVoiceCommunicationCallbacks;

/**
 * Created by yufuchang on 2018/8/16.
 */

public abstract class OnVoiceCommunicationCallbacksImpl implements OnVoiceCommunicationCallbacks {
//    public abstract void onUserOffline(int uid, int reason);

    public abstract void onUserJoined(int uid, int elapsed);

    public abstract void onJoinChannelSuccess(String channel, int uid, int elapsed);

//    public abstract void onRejoinChannelSuccess(String channel, int uid, int elapsed);

    @Override
    public void onRtcStats(VoiceCommunicationRtcStats stats) {

    }

//    public abstract void onUserMuteAudio(int uid, boolean muted);

//    @Override
//    public void onWarning(int warn) {
//
//    }

//    public abstract void onError(int err);

//    public abstract void onConnectionLost();

    public abstract void onNetworkQuality(int uid, int txQuality, int rxQuality);

    @Override
    public void onConnectionBanned() {

    }

    @Override
    public void onLastmileQuality(int quality) {

    }

    @Override
    public void onAudioVolumeIndication(VoiceCommunicationAudioVolumeInfo[] speakers, int totalVolume) {

    }

    @Override
    public void onRefreshUserState() {

    }

    @Override
    public void onActivityFinish() {

    }

    @Override
    public void onRemoteVideoStateChanged(int uid, int state, int reason, int elapsed) {

    }
}
