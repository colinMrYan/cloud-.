package com.inspur.emmcloud.util.privates;

import android.content.Context;

import com.inspur.emmcloud.R;
import com.inspur.emmcloud.api.APIInterfaceInstance;
import com.inspur.emmcloud.api.apiservice.ChatAPIService;
import com.inspur.emmcloud.api.apiservice.WSAPIService;
import com.inspur.emmcloud.baselib.util.LogUtils;
import com.inspur.emmcloud.basemodule.application.BaseApplication;
import com.inspur.emmcloud.bean.chat.GetVoiceCommunicationResult;
import com.inspur.emmcloud.bean.chat.VoiceCommunicationAudioVolumeInfo;
import com.inspur.emmcloud.bean.chat.VoiceCommunicationJoinChannelInfoBean;
import com.inspur.emmcloud.bean.chat.VoiceCommunicationRtcStats;
import com.inspur.emmcloud.interf.OnVoiceCommunicationCallbacks;

import org.json.JSONArray;

import java.util.ArrayList;
import java.util.List;

import io.agora.rtc.IRtcEngineEventHandler;
import io.agora.rtc.RtcEngine;
import io.agora.rtc.video.VideoEncoderConfiguration;

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
    private String communicationType = "";//会话类型
    private List<VoiceCommunicationJoinChannelInfoBean> voiceCommunicationMemberList = new ArrayList<>();
    private VoiceCommunicationJoinChannelInfoBean inviteeInfoBean;
    private int userCount = 1;
    private int state = -1;//布局状态
    private int communicationState = -1;//是否还在通话中的状态
    private IRtcEngineEventHandler mRtcEventHandler = new IRtcEngineEventHandler() {
        //其他用户离线回调
        @Override
        public void onUserOffline(int uid, int reason) {
            onVoiceCommunicationCallbacks.onUserOffline(uid, reason);
        }

        //用户加入频道回调
        @Override
        public void onUserJoined(int uid, int elapsed) {
            LogUtils.YfcDebug("有新的用户加入：" + uid);
            onVoiceCommunicationCallbacks.onUserJoined(uid, elapsed);
        }

        //加入频道成功
        @Override
        public void onJoinChannelSuccess(String channel, int uid, int elapsed) {
            LogUtils.YfcDebug("用户加入成功");
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

        @Override
        public void onRemoteVideoStateChanged(int uid, int state, int reason, int elapsed) {
            super.onRemoteVideoStateChanged(uid, state, reason, elapsed);
            onVoiceCommunicationCallbacks.onRemoteVideoStateChanged(uid, state, reason, elapsed);
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
    public static VoiceCommunicationUtils getVoiceCommunicationUtils(String communicationType) {
        if (voiceCommunicationUtils == null) {
            synchronized (VoiceCommunicationUtils.class) {
                if (voiceCommunicationUtils == null) {
                    voiceCommunicationUtils = new VoiceCommunicationUtils(BaseApplication.getInstance());
                }
            }
        }
        voiceCommunicationUtils.initializeAgoraEngine(communicationType);
        return voiceCommunicationUtils;
    }

    /**
     * 初始化引擎
     */
    private void initializeAgoraEngine(String communicationType) {
        try {
            mRtcEngine = RtcEngine.create(context, context.getString(R.string.agora_app_id), mRtcEventHandler);
        } catch (Exception e) {
            LogUtils.YfcDebug("初始化声网异常：" + e.getMessage());
        }
        if (mRtcEngine != null) {
            //屏蔽视频通话逻辑
//            if (communicationType.equals(ECMChatInputMenu.VIDEO_CALL)) {
//                LogUtils.YfcDebug("设置视频通话");
//                setupVideoConfig();
//            }
            mRtcEngine.enableAudioVolumeIndication(1000, 3, false);
        }
    }

    /**
     * 设置video
     */
    private void setupVideoConfig() {
        // In simple use cases, we only need to enable video capturing
        // and rendering once at the initialization step.
        // Note: audio recording and playing is enabled by default.
        mRtcEngine.enableVideo();

        // Please go to this page for detailed explanation
        // https://docs.agora.io/en/Video/API%20Reference/java/classio_1_1agora_1_1rtc_1_1_rtc_engine.html#af5f4de754e2c1f493096641c5c5c1d8f
        mRtcEngine.setVideoEncoderConfiguration(new VideoEncoderConfiguration(
                VideoEncoderConfiguration.VD_640x360,
                VideoEncoderConfiguration.FRAME_RATE.FRAME_RATE_FPS_15,
                VideoEncoderConfiguration.STANDARD_BITRATE,
                VideoEncoderConfiguration.ORIENTATION_MODE.ORIENTATION_MODE_FIXED_PORTRAIT));
    }

    /**
     * 关闭视频模块
     */
    public void disableVideo() {
        mRtcEngine.disableVideo();
    }

    /**
     * 人声的播放信号音量，可在 0~400 范围内进行调节：
     * 0：静音
     * 100：原始音量
     * 400：最大可为原始音量的 4 倍（自带溢出保护）
     *
     * @param volumeLevel
     */
    public void adjustPlaybackSignalVolume(int volumeLevel) {
        mRtcEngine.adjustPlaybackSignalVolume(volumeLevel);
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
     * 暂时去掉加密
     *
     * @param secret
     */
    public void setEncryptionSecret(String secret) {
//        int a = mRtcEngine.setEncryptionSecret("123456");
//        int b = mRtcEngine.setEncryptionMode("aes-128-ecb");
//        LogUtils.YfcDebug("setEncryptionSecret:"+a);
//        LogUtils.YfcDebug("setEncryptionMode:"+b);
        mRtcEngine.setEncryptionSecret(secret);
    }

    /**
     * 转换摄像头
     */
    public void switchCamera() {
        mRtcEngine.switchCamera();
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
     * 打开外放模式
     *
     * @param isSpakerphoneOpen
     */
    public void onSwitchSpeakerphoneClicked(boolean isSpakerphoneOpen) {
        if (mRtcEngine != null) {
            mRtcEngine.setEnableSpeakerphone(isSpakerphoneOpen);
        }
    }

    /**
     * 打开外放
     *
     * @param isSpakerphoneOpen
     */
    public void setEnableSpeakerphone(boolean isSpakerphoneOpen) {
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
     * 获取RtcEngine实例
     *
     * @return
     */
    public RtcEngine getRtcEngine() {
        return mRtcEngine;
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

    public String getCommunicationType() {
        return communicationType;
    }

    public void setCommunicationType(String communicationType) {
        this.communicationType = communicationType;
    }

    public int getCommunicationState() {
        return communicationState;
    }

    public void setCommunicationState(int communicationState) {
        this.communicationState = communicationState;
    }

    /**
     * 获取channel信息
     *
     * @param channelId
     * @param agoraChannelId
     */
    public void getVoiceCommunicationChannelInfo(String channelId, String agoraChannelId) {
        ChatAPIService chatAPIService = new ChatAPIService(BaseApplication.getInstance());
        WebService webService = new WebService();
        webService.setArgoaChannelId(agoraChannelId);
        webService.setChannelId(channelId);
        chatAPIService.setAPIInterface(webService);
        chatAPIService.getAgoraChannelInfo(agoraChannelId);
    }

    /**
     * 获取Uid
     * 排除掉自己防止自己给自己发命令消息
     *
     * @return
     */
    private JSONArray getUidArray(List<VoiceCommunicationJoinChannelInfoBean> voiceCommunicationUserInfoBeanList) {
        JSONArray jsonArray = new JSONArray();
        for (int i = 0; i < voiceCommunicationUserInfoBeanList.size(); i++) {
            if (!voiceCommunicationUserInfoBeanList.get(i).getUserId().equals(BaseApplication.getInstance().getUid())) {
                jsonArray.put(voiceCommunicationUserInfoBeanList.get(i).getUserId());
            }
        }
        return jsonArray;
    }

    class WebService extends APIInterfaceInstance {
        private String channelId = "";
        private String argoaChannelId = "";

        @Override
        public void returnGetVoiceCommunicationResultSuccess(GetVoiceCommunicationResult getVoiceCommunicationResult) {
            String scheme = "ecc-cloudplus-cmd://voice_channel?cmd=refuse&channelid=" + channelId + "&roomid=" + argoaChannelId + "&uid=" + BaseApplication.getInstance().getUid();
            WSAPIService.getInstance().sendStartVoiceAndVideoCallMessage(channelId, argoaChannelId, scheme, "VOICE", getUidArray(getVoiceCommunicationResult.getVoiceCommunicationJoinChannelInfoBeanList()));
        }

        @Override
        public void returnGetVoiceCommunicationResultFail(String error, int errorCode) {

        }

        public String getChannelId() {
            return channelId;
        }

        public void setChannelId(String channelId) {
            this.channelId = channelId;
        }

        public String getArgoaChannelId() {
            return argoaChannelId;
        }

        public void setArgoaChannelId(String argoaChannelId) {
            this.argoaChannelId = argoaChannelId;
        }
    }


}
