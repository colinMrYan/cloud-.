package com.inspur.emmcloud.interf;

import com.inspur.emmcloud.bean.chat.VoiceCommunicationAudioVolumeInfo;
import com.inspur.emmcloud.bean.chat.VoiceCommunicationRtcStats;

/**
 * Created by yufuchang on 2018/8/16.
 */

public interface OnVoiceCommunicationCallbacks {
//    //提示有用户离开了频道（或掉线）。SDK 判断用户离开频道（或掉线）的依据是超时: 在一定时间内（15 秒）没有收到对方的任何数据包，判定为对方掉线。在网络较差的情况下，可能会有误报。建议可靠的掉线检测应该由信令来做。
//    void onUserOffline(int uid, int reason);

    //提示有用户加入了频道。如果该客户端加入频道时已经有人在频道中，SDK 也会向应用程序上报这些已在频道中的用户。
    void onUserJoined(int uid, int elapsed);

    //表示客户端已经登入服务器，且分配了频道 ID 和用户 ID。频道 ID 的分配是根据 join() API 中指定的频道名称。如果调用 join() 时并未指定用户 ID，服务器就会分配一个。
    void onJoinChannelSuccess(String channel, int uid, int elapsed);

//    //有时候由于网络原因，客户端可能会和服务器失去连接，SDK 会进行自动重连，自动重连成功后触发此回调方法。
//    void onRejoinChannelSuccess(String channel, int uid, int elapsed);

    //该回调定期上报 Rtc Engine 的运行时的状态，每两秒触发一次。
    void onRtcStats(VoiceCommunicationRtcStats stats);

//    //提示有其他用户将他的音频流静音/取消静音。
//    void onUserMuteAudio(int uid, boolean muted);
//
//    //该回调方法表示 SDK 运行时出现了（网络或媒体相关的）警告。通常情况下，SDK 上报的警告信息应用程序可以忽略，SDK 会自动恢复。 例如和服务器失去连接时，SDK 可能会上报 ERR_OPEN_CHANNEL_TIMEOUT 警告，同时自动尝试重连
//    void onWarning(int warn);

//    //表示 SDK 运行时出现了（网络或媒体相关的）错误。 通常情况下，SDK 上报的错误意味着 SDK 无法自动恢复，需要 APP 干预或提示用户。 例如启动通话失败时，SDK 会上报 ERR_START_CALL 错误。APP 可以提示用户启动通话失败，并调用 leaveChannel() 退出频道。
//    void onError(int err);

//    //该回调方法表示 SDK 和服务器失去了网络连接，并且尝试自动重连一段时间（默认 10 秒）后仍未连上。该回调触发后，SDK 仍然会尝试重连，重连成功后会触发 onRejoinChannelSuccess 回调。
//    void onConnectionLost();

    //当你被服务端禁掉连接的权限时，会触发该回调。意外掉线之后，SDK 会自动进行重连，重连多次都失败之后，该回调会被触发，判定为连接不可用。
    void onConnectionBanned();

    //报告本地用户的网络质量。当你调用 enableLastmileTest 之后，该回调函数每 2 秒触发一次。
    void onLastmileQuality(int quality);

    //提示谁在说话及其音量。默认禁用。可以通过 enableAudioVolumeIndication 方法设置。
    void onAudioVolumeIndication(VoiceCommunicationAudioVolumeInfo[] speakers, int totalVolume);

    //该回调定期触发，向 APP 报告频道内用户当前的上行、下行网络质量。用户 ID。表示该回调报告的是持有该 ID 的用户的网络质量。当 uid 为 0 时，返回的是本地用户的网络质量。当前版本仅报告本地用户的网络质量。 tx为上行音量，rx为下行音量
    void onNetworkQuality(int uid, int txQuality, int rxQuality);

    //远端用户视频状态已变化回调
    void onFirstRemoteVideoDecoded(int uid, int state, int reason, int elapsed);

//    //统一的倒计时，倒计时结束时回调
//    void onCountDownTimerFinish();
//
//    void onOnlyOneLeftCountDownTimerFinish();

    void onActivityFinish();

    void onRefreshUserState();
}
