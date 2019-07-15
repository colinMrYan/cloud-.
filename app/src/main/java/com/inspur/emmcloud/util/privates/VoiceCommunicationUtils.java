package com.inspur.emmcloud.util.privates;

import android.content.Context;

import com.inspur.emmcloud.R;
import com.inspur.emmcloud.baselib.util.LogUtils;
import com.inspur.emmcloud.bean.chat.VoiceCommunicationAudioVolumeInfo;
import com.inspur.emmcloud.bean.chat.VoiceCommunicationJoinChannelInfoBean;
import com.inspur.emmcloud.bean.chat.VoiceCommunicationRtcStats;
import com.inspur.emmcloud.interf.OnVoiceCommunicationCallbacks;

import java.util.ArrayList;
import java.util.List;

import io.agora.rtc.IRtcEngineEventHandler;
import io.agora.rtc.RtcEngine;

/**
 * 详细回调接口解释见OnVoiceCommunicationCallbacks
 * Created by yufuchang on 2018/8/13.
 */

public class VoiceCommunicationUtils {

    private static VoiceCommunicationUtils voiceCommunicationUtils;
    private Context context;
    private RtcEngine mRtcEngine;
    private OnVoiceCommunicationCallbacks onVoiceCommunicationCallbacks;
    private List<VoiceCommunicationJoinChannelInfoBean> voiceCommunicationUserInfoBeanList = new ArrayList<>();
    private String channelId = "";//声网的channelId
    private List<VoiceCommunicationJoinChannelInfoBean> voiceCommunicationMemberList = new ArrayList<>();
    private VoiceCommunicationJoinChannelInfoBean inviteeInfoBean;
    private int userCount = 1;
    private int state = -1;
    private IRtcEngineEventHandler mRtcEventHandler = new IRtcEngineEventHandler() {
        //其他用户离线回调
        @Override
        public void onUserOffline(int uid, int reason) {
            onVoiceCommunicationCallbacks.onUserOffline(uid, reason);
        }

        //用户加入频道回调
        @Override
        public void onUserJoined(int uid, int elapsed) {
            onVoiceCommunicationCallbacks.onUserJoined(uid, elapsed);
        }

        //加入频道成功
        @Override
        public void onJoinChannelSuccess(String channel, int uid, int elapsed) {
//            userCount = userCount + 1;
            onVoiceCommunicationCallbacks.onJoinChannelSuccess(channel, uid, elapsed);
        }

        //断开重连，重新加入频道成功
        @Override
        public void onRejoinChannelSuccess(String channel, int uid, int elapsed) {
            onVoiceCommunicationCallbacks.onRejoinChannelSuccess(channel, uid, elapsed);
        }

        //每隔两秒钟返回一次频道内的状态信息
        @Override
        public void onRtcStats(RtcStats stats) {
            VoiceCommunicationRtcStats statsCloudPlus = new VoiceCommunicationRtcStats();
            statsCloudPlus.users = stats.users;
            onVoiceCommunicationCallbacks.onRtcStats(statsCloudPlus);
        }

        //静音监听
        @Override
        public void onUserMuteAudio(int uid, boolean muted) {
            onVoiceCommunicationCallbacks.onUserMuteAudio(uid, muted);
        }

        //warning信息
        @Override
        public void onWarning(int warn) {
            super.onWarning(warn);
            onVoiceCommunicationCallbacks.onWarning(warn);
        }

        //error信息
        @Override
        public void onError(int err) {
            super.onError(err);
            onVoiceCommunicationCallbacks.onError(err);
        }

        //失去连接信息
        @Override
        public void onConnectionLost() {
            super.onConnectionLost();
            onVoiceCommunicationCallbacks.onConnectionLost();
        }

        //当你被服务端禁掉连接的权限时，会触发该回调。意外掉线之后，SDK 会自动进行重连，重连多次都失败之后，该回调会被触发，判定为连接不可用。
        @Override
        public void onConnectionBanned() {
            super.onConnectionBanned();
            onVoiceCommunicationCallbacks.onConnectionBanned();
        }

        //网络质量回调
        @Override
        public void onLastmileQuality(int quality) {
            super.onLastmileQuality(quality);
            onVoiceCommunicationCallbacks.onLastmileQuality(quality);
        }

        //提示谁在说话及其音量。默认禁用。可以通过 enableAudioVolumeIndication 方法设置。
        @Override
        public void onAudioVolumeIndication(AudioVolumeInfo[] speakers, int totalVolume) {
            VoiceCommunicationAudioVolumeInfo[] voiceCommunicationAudioVolumeInfos = new VoiceCommunicationAudioVolumeInfo[speakers.length];
            for (int i = 0; i < speakers.length; i++) {
                VoiceCommunicationAudioVolumeInfo info = new VoiceCommunicationAudioVolumeInfo();
                info.uid = speakers[i].uid;
                info.volume = speakers[i].volume;
                voiceCommunicationAudioVolumeInfos[i] = info;
            }
            onVoiceCommunicationCallbacks.onAudioVolumeIndication(voiceCommunicationAudioVolumeInfos, totalVolume);
        }

        @Override
        public void onNetworkQuality(int uid, int txQuality, int rxQuality) {
            super.onNetworkQuality(uid, txQuality, rxQuality);
            onVoiceCommunicationCallbacks.onNetworkQuality(uid, txQuality, rxQuality);
        }
    };

    public VoiceCommunicationUtils(Context context) {
        this.context = context;
    }

    /**
     * 获得声网控制工具类
     *
     * @return
     */
    public static VoiceCommunicationUtils getVoiceCommunicationUtils(Context context) {
        if (voiceCommunicationUtils == null) {
            synchronized (VoiceCommunicationUtils.class) {
                if (voiceCommunicationUtils == null) {
                    voiceCommunicationUtils = new VoiceCommunicationUtils(context);
                }
            }
        }
        voiceCommunicationUtils.initializeAgoraEngine();
        return voiceCommunicationUtils;
    }

    /**
     * 初始化引擎
     */
    public void initializeAgoraEngine() {
        try {
            mRtcEngine = RtcEngine.create(context, context.getString(R.string.agora_app_id), mRtcEventHandler);
        } catch (Exception e) {
            LogUtils.YfcDebug("初始化声网异常：" + e.getMessage());
        }
        if (mRtcEngine != null) {
            mRtcEngine.enableAudioVolumeIndication(1000, 3);
        }
//        mRtcEngine.registerLocalUserAccount(context.getString(R.string.agora_app_id),"12345");
    }

    /**
     * 加入频道
     *
     * @param token
     * @param channelName
     * @param optionalInfo
     * @param optionalUid
     * @return
     */
    public int joinChannel(String token, String channelName, String optionalInfo, int optionalUid) {
        // 如果不指定optionalUid将自动生成一个
        return (mRtcEngine != null) ? mRtcEngine.joinChannel(token, channelName, optionalInfo, optionalUid) : -1;
    }

    /**
     * 离开频道，不让外部主动调用，外部可以主动调用destroy方法
     */
    private void leaveChannel() {
        if (mRtcEngine != null) {
            mRtcEngine.leaveChannel();
        }
    }

    /**
     * 设置加密密码
     *
     * @param secret
     */
    public void setEncryptionSecret(String secret) {
        mRtcEngine.setEncryptionSecret(secret);
    }

    /**
     * 设置频道模式
     *
     * @param profile
     */
    public void setChannelProfile(int profile) {
        if (mRtcEngine != null) {
            mRtcEngine.setChannelProfile(profile);
        }
    }

    /**
     * 打开外放
     *
     * @param isSpakerphoneOpen
     */
    public void onSwitchSpeakerphoneClicked(boolean isSpakerphoneOpen) {
        if (mRtcEngine != null) {
            mRtcEngine.setEnableSpeakerphone(isSpakerphoneOpen);
        }
    }

    /**
     * 静音本地
     * 该方法用于允许/禁止往网络发送本地音频流。
     *
     * @param isMute
     */
    public void muteLocalAudioStream(boolean isMute) {
        if (mRtcEngine != null) {
            mRtcEngine.muteLocalAudioStream(isMute);
        }
    }

    /**
     * 静音远端所有用户
     *
     * @param isMuteAllUser
     */
    public void muteAllRemoteAudioStreams(boolean isMuteAllUser) {
        if (mRtcEngine != null) {
            mRtcEngine.muteAllRemoteAudioStreams(isMuteAllUser);
        }
    }

    /**
     * 刷新token
     *
     * @param token
     * @return
     */
    public int renewToken(String token) {
        return mRtcEngine != null ? mRtcEngine.renewToken(token) : -1;
    }

    /**
     * 离开频道销毁资源
     */
    public void destroy() {
        leaveChannel();
        RtcEngine.destroy();
        mRtcEngine = null;
    }

    public OnVoiceCommunicationCallbacks getOnVoiceCommunicationCallbacks() {
        return onVoiceCommunicationCallbacks;
    }

    /**
     * 设置回调
     *
     * @param l
     */
    public void setOnVoiceCommunicationCallbacks(OnVoiceCommunicationCallbacks l) {
        this.onVoiceCommunicationCallbacks = l;
    }

    public List<VoiceCommunicationJoinChannelInfoBean> getVoiceCommunicationUserInfoBeanList() {
        return voiceCommunicationUserInfoBeanList;
    }

    public void setVoiceCommunicationUserInfoBeanList(List<VoiceCommunicationJoinChannelInfoBean> voiceCommunicationUserInfoBeanList) {
        this.voiceCommunicationUserInfoBeanList = voiceCommunicationUserInfoBeanList;
    }

    public String getChannelId() {
        return channelId;
    }

    public void setChannelId(String channelId) {
        this.channelId = channelId;
    }

    public List<VoiceCommunicationJoinChannelInfoBean> getVoiceCommunicationMemberList() {
        return voiceCommunicationMemberList;
    }

    public void setVoiceCommunicationMemberList(List<VoiceCommunicationJoinChannelInfoBean> voiceCommunicationMemberList) {
        this.voiceCommunicationMemberList = voiceCommunicationMemberList;
    }

    public VoiceCommunicationJoinChannelInfoBean getInviteeInfoBean() {
        return inviteeInfoBean;
    }

    public void setInviteeInfoBean(VoiceCommunicationJoinChannelInfoBean inviteeInfoBean) {
        this.inviteeInfoBean = inviteeInfoBean;
    }

    public int getUserCount() {
        return userCount;
    }

    public void setUserCount(int userCount) {
        this.userCount = userCount;
    }

    public int getState() {
        return state;
    }

    public void setState(int state) {
        this.state = state;
    }
}
