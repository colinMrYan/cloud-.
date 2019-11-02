package com.inspur.emmcloud.util.privates;

import android.content.Context;
import android.os.CountDownTimer;

import com.inspur.emmcloud.R;
import com.inspur.emmcloud.api.APIInterfaceInstance;
import com.inspur.emmcloud.api.apiservice.ChatAPIService;
import com.inspur.emmcloud.api.apiservice.WSAPIService;
import com.inspur.emmcloud.baselib.util.LogUtils;
import com.inspur.emmcloud.basemodule.application.BaseApplication;
import com.inspur.emmcloud.basemodule.bean.SimpleEventMessage;
import com.inspur.emmcloud.basemodule.config.Constant;
import com.inspur.emmcloud.basemodule.util.NetUtils;
import com.inspur.emmcloud.bean.chat.GetVoiceCommunicationResult;
import com.inspur.emmcloud.bean.chat.VoiceCommunicationAudioVolumeInfo;
import com.inspur.emmcloud.bean.chat.VoiceCommunicationJoinChannelInfoBean;
import com.inspur.emmcloud.bean.chat.VoiceCommunicationRtcStats;
import com.inspur.emmcloud.interf.OnVoiceCommunicationCallbacks;
import com.inspur.emmcloud.ui.chat.ChannelVoiceCommunicationActivity;

import org.greenrobot.eventbus.EventBus;
import org.json.JSONArray;

import java.util.ArrayList;
import java.util.List;

import io.agora.rtc.IRtcEngineEventHandler;
import io.agora.rtc.RtcEngine;
import io.agora.rtc.video.VideoEncoderConfiguration;

import static com.inspur.emmcloud.ui.chat.ChannelVoiceCommunicationActivity.COMMUNICATION_STATE_ING;
import static com.inspur.emmcloud.ui.chat.ChannelVoiceCommunicationActivity.COMMUNICATION_STATE_OVER;
import static com.inspur.emmcloud.ui.chat.ChannelVoiceCommunicationActivity.COMMUNICATION_STATE_PRE;

/**
 * 详细回调接口解释见OnVoiceCommunicationCallbacks
 * Created by yufuchang on 2018/8/13.
 */

public class VoiceCommunicationManager {


    /**
     * 通话状态类型
     * {@link ChannelVoiceCommunicationActivity}
     * 跳转到指定类的指定方法
     *
     * @see ChannelVoiceCommunicationActivity#COMMUNICATION_STATE_PRE
     * @see ChannelVoiceCommunicationActivity#COMMUNICATION_STATE_ING
     * @see ChannelVoiceCommunicationActivity#COMMUNICATION_STATE_OVER
     */
    private int communicationState = -1;
    private static VoiceCommunicationManager voiceCommunicationManager;
    private Context context;
    private RtcEngine mRtcEngine;
    private OnVoiceCommunicationCallbacks onVoiceCommunicationCallbacks;
    /**
     * 声网的agoraChannelId
     */
    private String agoraChannelId = "";
    private String cloudPlusChannelId = "";
    /**
     * 会话类型
     */
    private String communicationType = "";
    private List<VoiceCommunicationJoinChannelInfoBean> voiceCommunicationMemberList = new ArrayList<>();
    private List<VoiceCommunicationJoinChannelInfoBean> voiceCommunicationMemberListTop = new ArrayList<>();
    private List<VoiceCommunicationJoinChannelInfoBean> voiceCommunicationMemberListBottom = new ArrayList<>();
    private VoiceCommunicationJoinChannelInfoBean inviteeInfoBean;
    private int userCount = 1;
    /**
     * 记录通话开始时间
     */
    private long connectStartTime = 0;
    /**
     * 布局状态
     */
    private int layoutState = -1;
    private CountDownTimer countDownTimer;
    private boolean isHandsFree = false;
    private boolean isMute = false;
    /**
     * 30s内无响应挂断 总时长：millisInFuture，隔多长时间回调一次countDownInterval
     */
    private long millisInFuture = 60 * 1000L, countDownInterval = 1000, millisInFutureInviter = 65 * 1000L;
    private IRtcEngineEventHandler mRtcEventHandler = new IRtcEngineEventHandler() {
        //其他用户离线回调
        @Override
        public void onUserOffline(int uid, int reason) {
            changeUserConnectStateByAgoraUid(VoiceCommunicationJoinChannelInfoBean.CONNECT_STATE_LEAVE, uid);
            userCount = userCount - 1;
            LogUtils.YfcDebug("频道剩余人数：" + userCount);
            if (onVoiceCommunicationCallbacks != null) {
                onVoiceCommunicationCallbacks.onUserOffline(uid, reason);
            }
            if (getWaitAndConnectedNumber() < 2) {
                communicationState = COMMUNICATION_STATE_OVER;
                SuspensionWindowManagerUtils.getInstance().hideCommunicationSmallWindow();
                destroy();
                //防止在声网回调和小窗打开Activity同步进行，接收到回调没关上Activity的情况
                if (BaseApplication.getInstance().isActivityExist(ChannelVoiceCommunicationActivity.class)) {
                    BaseApplication.getInstance().closeActivity(ChannelVoiceCommunicationActivity.class.getSimpleName());
                }
                VoiceCommunicationToastUtil.showToast("destroy");
            }
        }

        //用户加入频道回调
        @Override
        public void onUserJoined(int uid, int elapsed) {
            LogUtils.YfcDebug("用户加入频道：" + uid);
            for (int i = 0; i < voiceCommunicationMemberList.size(); i++) {
                if (voiceCommunicationMemberList.get(i).getAgoraUid() == uid) {
                    voiceCommunicationMemberList.get(i).setConnectState(VoiceCommunicationJoinChannelInfoBean.CONNECT_STATE_CONNECTED);
                }
            }
            userCount = userCount + 1;
            if (userCount >= 2 && communicationState == COMMUNICATION_STATE_PRE && SuspensionWindowManagerUtils.getInstance().isShowing()) {
                //发送到CommunicationFragment
                SimpleEventMessage simpleEventMessage = new SimpleEventMessage(Constant.EVENTBUS_TAG_REFRESH_VOICE_CALL_SMALL_WINDOW);
                EventBus.getDefault().post(simpleEventMessage);
                communicationState = COMMUNICATION_STATE_ING;
            }
            if (onVoiceCommunicationCallbacks != null) {
                onVoiceCommunicationCallbacks.onUserJoined(uid, elapsed);
            }
        }

        //加入频道成功
        @Override
        public void onJoinChannelSuccess(String channel, int uid, int elapsed) {
//            userCount = userCount + 1;
            if (onVoiceCommunicationCallbacks != null) {
                onVoiceCommunicationCallbacks.onJoinChannelSuccess(channel, uid, elapsed);
            }
        }

        //断开重连，重新加入频道成功
        @Override
        public void onRejoinChannelSuccess(String channel, int uid, int elapsed) {
            userCount = userCount + 1;
            if (onVoiceCommunicationCallbacks != null) {
                onVoiceCommunicationCallbacks.onRejoinChannelSuccess(channel, uid, elapsed);
            }
        }

        //每隔两秒钟返回一次频道内的状态信息
        @Override
        public void onRtcStats(RtcStats stats) {
            VoiceCommunicationRtcStats statsCloudPlus = new VoiceCommunicationRtcStats();
            statsCloudPlus.users = stats.users;
            if (onVoiceCommunicationCallbacks != null) {
                onVoiceCommunicationCallbacks.onRtcStats(statsCloudPlus);
            }
        }

        //静音监听
        @Override
        public void onUserMuteAudio(int uid, boolean muted) {
            if (onVoiceCommunicationCallbacks != null) {
                onVoiceCommunicationCallbacks.onUserMuteAudio(uid, muted);
            }
        }

        //warning信息
        @Override
        public void onWarning(int warn) {
            super.onWarning(warn);
            if (onVoiceCommunicationCallbacks != null) {
                onVoiceCommunicationCallbacks.onWarning(warn);
            }
        }

        //error信息
        @Override
        public void onError(int err) {
            super.onError(err);
            if (onVoiceCommunicationCallbacks != null) {
                onVoiceCommunicationCallbacks.onError(err);
            }
            destroy();
            SuspensionWindowManagerUtils.getInstance().hideCommunicationSmallWindow();
        }

        //失去连接信息
        @Override
        public void onConnectionLost() {
            super.onConnectionLost();
            onVoiceCommunicationCallbacks.onConnectionLost();
            destroy();
            SuspensionWindowManagerUtils.getInstance().hideCommunicationSmallWindow();
        }

        //当你被服务端禁掉连接的权限时，会触发该回调。意外掉线之后，SDK 会自动进行重连，重连多次都失败之后，该回调会被触发，判定为连接不可用。
        @Override
        public void onConnectionBanned() {
            super.onConnectionBanned();
            if (onVoiceCommunicationCallbacks != null) {
                onVoiceCommunicationCallbacks.onConnectionBanned();
            }
        }

        //网络质量回调
        @Override
        public void onLastmileQuality(int quality) {
            super.onLastmileQuality(quality);
            if (onVoiceCommunicationCallbacks != null) {
                onVoiceCommunicationCallbacks.onLastmileQuality(quality);
            }
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
            if (onVoiceCommunicationCallbacks != null) {
                onVoiceCommunicationCallbacks.onAudioVolumeIndication(voiceCommunicationAudioVolumeInfos, totalVolume);
            }
        }

        @Override
        public void onNetworkQuality(int uid, int txQuality, int rxQuality) {
            super.onNetworkQuality(uid, txQuality, rxQuality);
            if (onVoiceCommunicationCallbacks != null) {
                onVoiceCommunicationCallbacks.onNetworkQuality(uid, txQuality, rxQuality);
            }
        }

        @Override
        public void onRemoteVideoStateChanged(int uid, int state, int reason, int elapsed) {
            super.onRemoteVideoStateChanged(uid, state, reason, elapsed);
            if (onVoiceCommunicationCallbacks != null) {
                onVoiceCommunicationCallbacks.onRemoteVideoStateChanged(uid, state, reason, elapsed);
            }
        }
    };

    /**
     * 获取等待中和通话中的人员数量
     *
     * @return
     */
    private int getWaitAndConnectedNumber() {
        int number = 0;
        if (voiceCommunicationMemberList != null) {
            for (VoiceCommunicationJoinChannelInfoBean voiceCommunicationJoinChannelInfoBean : voiceCommunicationMemberList) {
                int state = voiceCommunicationJoinChannelInfoBean.getConnectState();
                if (state == VoiceCommunicationJoinChannelInfoBean.CONNECT_STATE_INIT || state == VoiceCommunicationJoinChannelInfoBean.CONNECT_STATE_CONNECTED) {
                    number = number + 1;
                }
            }
        }
        return number;
    }

    /**
     * 获取已经进入通话中的人员数量
     *
     * @return
     */
    public int getConnectedNumber() {
        int number = 0;
        if (voiceCommunicationMemberList != null) {
            for (VoiceCommunicationJoinChannelInfoBean voiceCommunicationJoinChannelInfoBean : voiceCommunicationMemberList) {
                if (voiceCommunicationJoinChannelInfoBean.getConnectState() == VoiceCommunicationJoinChannelInfoBean.CONNECT_STATE_CONNECTED) {
                    number = number + 1;
                }
            }
        }
        return number;
    }

    /**
     * 修改用户的链接状态
     * 通过agoraUid
     *
     * @param connectStateConnected
     */
    private void changeUserConnectStateByAgoraUid(int connectStateConnected, int agroaUid) {
        if (voiceCommunicationMemberList != null && voiceCommunicationMemberList.size() > 0) {
            for (int i = 0; i < voiceCommunicationMemberList.size(); i++) {
                if (voiceCommunicationMemberList.get(i).getAgoraUid() == agroaUid) {
                    voiceCommunicationMemberList.get(i).setConnectState(connectStateConnected);
                    break;
                }
            }
        }
    }

    private VoiceCommunicationManager() {
        this.context = BaseApplication.getInstance();
    }

    /**
     * 获得声网控制工具类
     * 默认只开启语音通话部分
     *
     * @return
     */
    public static VoiceCommunicationManager getInstance() {
        if (voiceCommunicationManager == null) {
            synchronized (VoiceCommunicationManager.class) {
                if (voiceCommunicationManager == null) {
                    voiceCommunicationManager = new VoiceCommunicationManager();
                }
            }
        }
//        voiceCommunicationManager.initializeAgoraEngine();
        return voiceCommunicationManager;
    }

    /**
     * 初始化引擎
     */
    public void initializeAgoraEngine() {
        try {
            if (mRtcEngine == null) {
                mRtcEngine = RtcEngine.create(context, context.getString(R.string.agora_app_id), mRtcEventHandler);
                mRtcEngine.enableAudioVolumeIndication(1000, 3, false);
            }
        } catch (Exception e) {
            LogUtils.YfcDebug("初始化声网异常：" + e.getMessage());
        }
    }

    /**
     * 启动计时器
     */
    public void startCountDownTimer() {
        //主叫方计时40秒，被叫方30秒
        countDownTimer = new CountDownTimer(isInviter() ? millisInFutureInviter : millisInFuture, countDownInterval) {
            @Override
            public void onTick(long millisUntilFinished) {
            }

            @Override
            public void onFinish() {
//                //如果是邀请或被邀请状态，倒计时结束时挂断电话
//                if (layoutState == INVITEE_LAYOUT_STATE || layoutState == INVITER_LAYOUT_STATE) {
//                    isLeaveChannel = true;
//                    refuseOrLeaveChannel(COMMUNICATION_REFUSE);
//                }
                for (int i = 0; i < voiceCommunicationMemberList.size(); i++) {
                    if (voiceCommunicationMemberList.get(i).getConnectState() == VoiceCommunicationJoinChannelInfoBean.CONNECT_STATE_INIT) {
                        voiceCommunicationMemberList.get(i).setConnectState(VoiceCommunicationJoinChannelInfoBean.CONNECT_STATE_LEAVE);
                    }
                }
                if (onVoiceCommunicationCallbacks != null) {
                    onVoiceCommunicationCallbacks.onCountDownTimerFinish();
                }
                if (communicationState == COMMUNICATION_STATE_PRE) {
                    remindEmmServerLeaveChannel(agoraChannelId);
                    SuspensionWindowManagerUtils.getInstance().hideCommunicationSmallWindow();
                    destroy();
                }
                if (countDownTimer != null) {
                    countDownTimer.cancel();
                    countDownTimer = null;
                }
            }
        };
        countDownTimer.start();
    }

    /**
     * 自己是否主呼叫方
     *
     * @return
     */
    public boolean isInviter() {
        if (voiceCommunicationMemberList != null && voiceCommunicationMemberList.size() > 0
                && voiceCommunicationMemberList.get(0).getUserId().equals(BaseApplication.getInstance().getUid())) {
            return true;
        }
        return false;
    }

    /**
     * 获取当前音频通话的状态
     *
     * @return
     * @see #communicationState
     */
    public int getCommunicationState() {
        return communicationState;
    }

    /**
     * 设置当前音频通话的状态
     *
     * @param communicationState
     * @see #communicationState
     */
    public void setCommunicationState(int communicationState) {
        this.communicationState = communicationState;
    }

    /**
     * 判断当前通话是否在拨号或者通话中
     *
     * @return
     */
    public boolean isVoiceBusy() {
        return communicationState == COMMUNICATION_STATE_PRE ||
                communicationState == ChannelVoiceCommunicationActivity.COMMUNICATION_STATE_ING;
    }

    /**
     * 设置video
     */
    private void setupVideoConfig() {
        if (mRtcEngine != null) {
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
    }


    /**
     * 开启视频通话的配置
     */
    public void enableVideo() {
        if (mRtcEngine != null) {
            /**配置视频通话*/
            setupVideoConfig();
        }
    }

    /**
     * 关闭视频模块
     */
    public void disableVideo() {
        if (mRtcEngine != null) {
            mRtcEngine.disableVideo();
        }
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
        if (mRtcEngine != null) {
            mRtcEngine.adjustPlaybackSignalVolume(volumeLevel);
        }
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
        if (mRtcEngine != null) {
            mRtcEngine.setEncryptionSecret(secret);
        }
    }

    /**
     * 转换摄像头
     */
    public void switchCamera() {
        if (mRtcEngine != null) {
            mRtcEngine.switchCamera();
        }
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
        if (countDownTimer != null) {
            countDownTimer.cancel();
            countDownTimer = null;
        }
        leaveChannel();
        RtcEngine.destroy();
//        new android.os.Handler().postDelayed(new Runnable() {
//            @Override
//            public void run() {
//                RtcEngine.destroy();
//            }
//        }, 1000);
        mRtcEngine = null;
        communicationState = -1;
        layoutState = -1;
        onVoiceCommunicationCallbacks = null;
        communicationState = -1;
        agoraChannelId = "";
        cloudPlusChannelId = "";
        userCount = 1;
        connectStartTime = 0;
        voiceCommunicationManager = null;
        isHandsFree = false;
        isMute = false;
    }

    /**
     * 分解通话成员，小于等于5人和多于5人时list有所不同为了适应通话人数两行且居中的UI
     */
    public void handleVoiceCommunicationMemberList() {
        if (voiceCommunicationMemberList.size() > 0) {
            if (voiceCommunicationMemberList.size() <= 5) {
                voiceCommunicationMemberListTop = voiceCommunicationMemberList;
                voiceCommunicationMemberListBottom.clear();
            } else if (voiceCommunicationManager.getVoiceCommunicationMemberList().size() <= 9) {
                voiceCommunicationMemberListTop = voiceCommunicationMemberList.subList(0, 5);
                voiceCommunicationMemberListBottom = voiceCommunicationMemberList.subList(5, voiceCommunicationMemberList.size());
            }
        }
    }

    public List<VoiceCommunicationJoinChannelInfoBean> getVoiceCommunicationMemberListTop() {
        return voiceCommunicationMemberListTop;
    }

    public List<VoiceCommunicationJoinChannelInfoBean> getVoiceCommunicationMemberListBottom() {
        return voiceCommunicationMemberListBottom;
    }

    public boolean isHandsFree() {
        return isHandsFree;
    }

    public void setHandsFree(boolean handsFree) {
        isHandsFree = handsFree;
    }

    public boolean isMute() {
        return isMute;
    }

    public void setMute(boolean mute) {
        isMute = mute;
    }

    /**
     * 设置回调
     *
     * @param l
     */
    public void setOnVoiceCommunicationCallbacks(OnVoiceCommunicationCallbacks l) {
        this.onVoiceCommunicationCallbacks = l;
    }

    public String getAgoraChannelId() {
        return agoraChannelId;
    }

    public void setAgoraChannelId(String agoraChannelId) {
        this.agoraChannelId = agoraChannelId;
    }

    public String getCloudPlusChannelId() {
        return cloudPlusChannelId;
    }

    public void setCloudPlusChannelId(String cloudPlusChannelId) {
        this.cloudPlusChannelId = cloudPlusChannelId;
    }

    public List<VoiceCommunicationJoinChannelInfoBean> getVoiceCommunicationMemberList() {
        return voiceCommunicationMemberList;
    }

    public void setVoiceCommunicationMemberList(List<VoiceCommunicationJoinChannelInfoBean> voiceCommunicationMemberList) {
        this.voiceCommunicationMemberList = voiceCommunicationMemberList;
        handleVoiceCommunicationMemberList();
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

    public int getLayoutState() {
        return layoutState;
    }

    public void setLayoutState(int layoutState) {
        this.layoutState = layoutState;
    }

    public String getCommunicationType() {
        return communicationType;
    }

    public void setCommunicationType(String communicationType) {
        this.communicationType = communicationType;
    }

    public long getConnectStartTime() {
        return connectStartTime;
    }

    public void setConnectStartTime(long connectStartTime) {
        this.connectStartTime = connectStartTime;
    }

    /**
     * 通知EmmServer用户离开频道
     *
     * @param agoraChannelId
     */
    private void remindEmmServerLeaveChannel(String agoraChannelId) {
        if (NetUtils.isNetworkConnected(BaseApplication.getInstance())) {
            ChatAPIService chatAPIService = new ChatAPIService(BaseApplication.getInstance());
            chatAPIService.leaveAgoraChannel(agoraChannelId);
        }
    }

    /**
     * 获取channel信息
     *
     * @param channelId
     * @param agoraChannelId
     */
    public void getVoiceCommunicationChannelInfoAndSendRefuseCommand(String channelId, String agoraChannelId, String fromUid) {
        ChatAPIService chatAPIService = new ChatAPIService(BaseApplication.getInstance());
        WebService webService = new WebService();
        webService.setArgoaChannelId(agoraChannelId);
        webService.setChannelId(channelId);
        webService.setFromUid(fromUid);
        chatAPIService.setAPIInterface(webService);
        chatAPIService.getAgoraChannelInfo(agoraChannelId);
    }

    /**
     * 获取Uid
     * 排除掉自己防止自己给自己发命令消息
     *
     * @return
     */
    private JSONArray getUidArray(List<VoiceCommunicationJoinChannelInfoBean> voiceCommunicationUserInfoBeanList, String fromUid) {
        JSONArray jsonArray = new JSONArray();
//        for (int i = 0; i < voiceCommunicationUserInfoBeanList.size(); i++) {
//            if (!voiceCommunicationUserInfoBeanList.get(i).getUserId().equals(BaseApplication.getInstance().getUid())) {
//                jsonArray.put(voiceCommunicationUserInfoBeanList.get(i).getUserId());
//            }
//        }
        jsonArray.put(fromUid);
        return jsonArray;
    }

    class WebService extends APIInterfaceInstance {
        private String channelId = "";
        private String argoaChannelId = "";
        private String fromUid = "";

        @Override
        public void returnGetVoiceCommunicationChannelInfoSuccess(GetVoiceCommunicationResult getVoiceCommunicationResult) {
            String scheme = "ecc-cloudplus-cmd://voice_channel?cmd=refuse&channelid=" + channelId + "&roomid=" + argoaChannelId + "&uid=" + BaseApplication.getInstance().getUid();
            if (getVoiceCommunicationResult.getChannelId().equals(argoaChannelId)) {
                WSAPIService.getInstance().sendStartVoiceAndVideoCallMessage(channelId, argoaChannelId, scheme, "VOICE", getUidArray(getVoiceCommunicationResult.getVoiceCommunicationJoinChannelInfoBeanList(), fromUid), Constant.VIDEO_CALL_REFUSE);
            }
        }

        @Override
        public void returnGetVoiceCommunicationChannelInfoFail(String error, int errorCode) {
        }

        public String getChannelId() {
            return channelId;
        }

        public void setChannelId(String channelId) {
            this.channelId = channelId;
        }

        public void setArgoaChannelId(String argoaChannelId) {
            this.argoaChannelId = argoaChannelId;
        }

        public void setFromUid(String fromUid) {
            this.fromUid = fromUid;
        }
    }
}
