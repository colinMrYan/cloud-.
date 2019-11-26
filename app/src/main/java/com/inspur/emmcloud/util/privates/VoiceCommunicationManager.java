package com.inspur.emmcloud.util.privates;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.CountDownTimer;
import android.os.Vibrator;
import android.view.SurfaceView;

import com.inspur.emmcloud.MyApplication;
import com.inspur.emmcloud.R;
import com.inspur.emmcloud.api.APIInterfaceInstance;
import com.inspur.emmcloud.api.apiservice.ChatAPIService;
import com.inspur.emmcloud.api.apiservice.WSAPIService;
import com.inspur.emmcloud.baselib.util.LogUtils;
import com.inspur.emmcloud.baselib.util.StringUtils;
import com.inspur.emmcloud.baselib.util.ToastUtils;
import com.inspur.emmcloud.basemodule.application.BaseApplication;
import com.inspur.emmcloud.basemodule.config.Constant;
import com.inspur.emmcloud.basemodule.util.NetUtils;
import com.inspur.emmcloud.bean.chat.GetVoiceAndVideoResult;
import com.inspur.emmcloud.bean.chat.GetVoiceCommunicationResult;
import com.inspur.emmcloud.bean.chat.VoiceCommunicationAudioVolumeInfo;
import com.inspur.emmcloud.bean.chat.VoiceCommunicationJoinChannelInfoBean;
import com.inspur.emmcloud.bean.chat.VoiceCommunicationRtcStats;
import com.inspur.emmcloud.bean.system.GetBoolenResult;
import com.inspur.emmcloud.interf.OnVoiceCommunicationCallbacks;
import com.inspur.emmcloud.ui.chat.ConversationActivity;
import com.inspur.emmcloud.ui.chat.VoiceCommunicationActivity;
import com.inspur.emmcloud.widget.ECMChatInputMenu;

import org.json.JSONArray;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import io.agora.rtc.Constants;
import io.agora.rtc.IRtcEngineEventHandler;
import io.agora.rtc.RtcEngine;
import io.agora.rtc.video.BeautyOptions;
import io.agora.rtc.video.VideoEncoderConfiguration;

import static com.inspur.emmcloud.ui.chat.VoiceCommunicationActivity.COMMUNICATION_STATE_ING;
import static com.inspur.emmcloud.ui.chat.VoiceCommunicationActivity.COMMUNICATION_STATE_OVER;
import static com.inspur.emmcloud.ui.chat.VoiceCommunicationActivity.COMMUNICATION_STATE_PRE;

/**
 * 详细回调接口解释见OnVoiceCommunicationCallbacks
 * Created by yufuchang on 2018/8/13.
 */

public class VoiceCommunicationManager {
    /**
     * 30s内无响应挂断 总时长：millisInFuture，隔多长时间回调一次countDownInterval
     */
    private static final long MILLIS_IN_FUTURE = 60 * 1000L, COUNT_DOWN_INTERVAL = 1000, MILLIS_IN_FUTURE_INVITER = 65 * 1000L;
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
    /**
     * 记录通话开始时间
     */
    private long connectStartTime = 0;
    private CountDownTimer countDownTimer;
    /**
     * 通话状态类型
     * {@link VoiceCommunicationActivity}
     * 跳转到指定类的指定方法，默认状态为over
     *
     * @see VoiceCommunicationActivity#COMMUNICATION_STATE_PRE
     * @see VoiceCommunicationActivity#COMMUNICATION_STATE_ING
     * @see VoiceCommunicationActivity#COMMUNICATION_STATE_OVER
     */
    private int communicationState = COMMUNICATION_STATE_OVER;
    private boolean isHandsFree = false;
    private boolean isMute = false;
    private Vibrator vibrator;
    /**
     * 视频会话小视图
     */
    private SurfaceView agoraLocalView;
    /**
     * 视频会话大视图
     */
    private SurfaceView agoraRemoteView;
    private int videoFirstFrameUid = 0;
    private IRtcEngineEventHandler mRtcEventHandler = new IRtcEngineEventHandler() {
        //其他用户离线回调
        @Override
        public void onUserOffline(int uid, int reason) {
            VoiceCommunicationCommonLine.onUserReject(uid, VoiceCommunicationJoinChannelInfoBean.CONNECT_STATE_LEAVE);
        }

        //用户加入频道回调，如果加入之前，已经有其他用户在频道中了，新加入的用户也会收到这些已有用户加入频道的回调
        //例如主叫给被叫拨打电话，被叫加入时也会受到主叫方加入的回调，返回主叫方的id
        @Override
        public void onUserJoined(int uid, int elapsed) {
            changeUserConnectStateByAgoraUid(VoiceCommunicationJoinChannelInfoBean.CONNECT_STATE_CONNECTED, uid);
            if (communicationState == COMMUNICATION_STATE_PRE && getConnectedNumber() >= 2) {
                vibratorOnece();
                //当小窗状态对方接听电话后，刷新小窗上的时间
                if (SuspensionWindowManagerUtils.getInstance().isShowing()) {
                    SuspensionWindowManagerUtils.getInstance().refreshSmallWindow();
                }
                communicationState = COMMUNICATION_STATE_ING;
            }
            if (onVoiceCommunicationCallbacks != null) {
                onVoiceCommunicationCallbacks.onUserJoined(uid, elapsed);
            }
        }

        //加入频道成功
        @Override
        public void onJoinChannelSuccess(String channel, int uid, int elapsed) {
            changeUserConnectStateByAgoraUid(VoiceCommunicationJoinChannelInfoBean.CONNECT_STATE_CONNECTED, uid);
            if (onVoiceCommunicationCallbacks != null) {
                onVoiceCommunicationCallbacks.onJoinChannelSuccess(channel, uid, elapsed);
            }
            //加入成功后，是主叫方再发出邀请消息
            if (isInviter()) {
                sendCommunicationCommand(Constant.COMMAND_INVITE);
            } else {
                vibratorOnece();
            }
            remindEmmServerJoinChannel(channel);
        }

        //断开重连，重新加入频道成功
        @Override
        public void onRejoinChannelSuccess(String channel, int uid, int elapsed) {
            changeUserConnectStateByAgoraUid(VoiceCommunicationJoinChannelInfoBean.CONNECT_STATE_CONNECTED, uid);
            if (onVoiceCommunicationCallbacks != null) {
                onVoiceCommunicationCallbacks.onRefreshUserState();
            }
            remindEmmServerJoinChannel(channel);
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

        @Override
        public void onWarning(int warn) {
            super.onWarning(warn);
        }

        @Override
        public void onError(int err) {
            super.onError(err);
            handleDestroy();
        }

        //失去连接信息
        @Override
        public void onConnectionLost() {
            super.onConnectionLost();
            handleDestroy();
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

        //官方示例仍用的此方法
        @Override
        public void onFirstRemoteVideoDecoded(int uid, int width, int height, int elapsed) {
            super.onFirstRemoteVideoDecoded(uid, width, height, elapsed);
            if (onVoiceCommunicationCallbacks != null) {
                onVoiceCommunicationCallbacks.onFirstRemoteVideoDecoded(uid, width, height, elapsed);
            }
            //当接通视频通话，如果是在小窗状态，刷新小窗
            if (VideoSuspensionWindowManagerUtils.getInstance().isShowing()) {
                VideoSuspensionWindowManagerUtils.getInstance().refreshVideoSmallWindow();
            }
        }

        @Override
        public void onUserEnableVideo(int uid, boolean enabled) {
            super.onUserEnableVideo(uid, enabled);
            if (onVoiceCommunicationCallbacks != null) {
                onVoiceCommunicationCallbacks.onUserEnableVideo(uid, enabled);
            }
        }

        @Override
        public void onLeaveChannel(RtcStats stats) {
            super.onLeaveChannel(stats);
        }

        //        @Override
//        public void onTokenPrivilegeWillExpire(String token) {
//            super.onTokenPrivilegeWillExpire(token);
//            LogUtils.YfcDebug("onTokenPrivilegeWillExpire");
//        }
//
//        @Override
//        public void onRequestToken() {
//            super.onRequestToken();
//            LogUtils.YfcDebug("onRequestToken");
//        }
    };

    /*
     * 想设置震动大小可以通过改变pattern来设定，如果开启时间太短，震动效果可能感觉不到
     * */
    private void vibratorOnece() {
        Vibrator vibrator = (Vibrator) BaseApplication.getInstance().getSystemService(Context.VIBRATOR_SERVICE);
        long[] pattern = {100, 500, 100, 500};   // 停止 开启 停止 开启
        vibrator.vibrate(pattern, -1);   // 如果只想震动一次，repeat设为-1
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
        return voiceCommunicationManager;
    }

    private VoiceCommunicationManager() {
        this.context = BaseApplication.getInstance();
    }

    /**
     * 初始化引擎
     */
    public void initializeAgoraEngine() {
        try {
            if (mRtcEngine == null) {
                mRtcEngine = RtcEngine.create(context, context.getString(R.string.agora_app_id), mRtcEventHandler);
                mRtcEngine.enableAudioVolumeIndication(1000, 3, false);
                mRtcEngine.setChannelProfile(Constants.CHANNEL_PROFILE_COMMUNICATION);
            }
        } catch (Exception e) {
            LogUtils.YfcDebug("初始化声网异常：" + e.getMessage());
        }
    }

    /**
     * 向socket发送指令消息
     *
     * @param commandType
     */
    public void sendCommunicationCommand(String commandType) {
        WSAPIService.getInstance().sendStartVoiceAndVideoCallMessage(cloudPlusChannelId, agoraChannelId,
                getSchema(commandType, cloudPlusChannelId, agoraChannelId), getVoiceVideoCommunicationType(), getUidArray(
                        getVoiceCommunicationMemberList()), getActionByCommandType(commandType));
    }

    /**
     * 通知EmmServer用户加入频道
     *
     * @param agoraChannelId
     */
    private void remindEmmServerJoinChannel(String agoraChannelId) {
        if (NetUtils.isNetworkConnected(BaseApplication.getInstance())) {
            ChatAPIService chatAPIService = new ChatAPIService(BaseApplication.getInstance());
            chatAPIService.setAPIInterface(new WebService());
            chatAPIService.remindServerJoinChannelSuccess(agoraChannelId);
        }
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
     * 告知服务端已拒绝
     *
     * @param agoraChannelId
     */
    private void remindEmmServerRefuseChannel(String agoraChannelId) {
        if (NetUtils.isNetworkConnected(BaseApplication.getInstance())) {
            ChatAPIService chatAPIService = new ChatAPIService(BaseApplication.getInstance());
            chatAPIService.refuseAgoraChannel(agoraChannelId);
        }
    }

    /**
     * 根据命令类型获取action类型
     *
     * @param commandType
     * @return
     */
    private String getActionByCommandType(String commandType) {
        switch (commandType) {
            case Constant.COMMAND_INVITE:
                return Constant.VIDEO_CALL_INVITE;
            case Constant.COMMAND_REFUSE:
                return Constant.VIDEO_CALL_REFUSE;
            case Constant.COMMAND_DESTROY:
                return Constant.VIDEO_CALL_HANG_UP;
        }
        return "";
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


    /**
     * 获取通信类型，默认是语音通话
     *
     * @return
     */
    private String getVoiceVideoCommunicationType() {
        if (communicationType.equals(ECMChatInputMenu.VIDEO_CALL)) {
            return "VIDEO";
        } else if (communicationType.equals(ECMChatInputMenu.VOICE_CALL)) {
            return "VOICE";
        } else {
            return "VOICE";
        }
    }

    /**
     * 获取Schema
     * ecc-cloudplus-cmd:\/\/voice_channel?cmd=invite&channelid=143271038136877057&roomid=257db7ddc478429cab2d2a1ec4ed8626&uid=99999
     *
     * @return
     */
    private String getSchema(String cmd, String channelId, String roomId) {
        return "ecc-cloudplus-cmd://" + getChannelType() + "?cmd=" + cmd + "&channelid=" + channelId + "&roomid=" + roomId + "&uid=" + BaseApplication.getInstance().getUid();
    }

    private String getChannelType() {
        if (communicationType.equals(ECMChatInputMenu.VOICE_CALL)) {
            return "voice_channel";
        } else if (communicationType.equals(ECMChatInputMenu.VIDEO_CALL)) {
            return "video_channel";
        }
        return "";
    }

    /**
     * 获取等待中和通话中的人员数量
     *
     * @return
     */
    public int getWaitAndConnectedNumber() {
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
    public void changeUserConnectStateByAgoraUid(int connectStateConnected, int agroaUid) {
        if (voiceCommunicationMemberList != null && voiceCommunicationMemberList.size() > 0) {
            for (int i = 0; i < voiceCommunicationMemberList.size(); i++) {
                if (voiceCommunicationMemberList.get(i).getAgoraUid() == agroaUid) {
                    voiceCommunicationMemberList.get(i).setConnectState(connectStateConnected);
                    break;
                }
            }
        }
    }

    /**
     * CommunicationFragment接到消息后的处理
     *
     * @param getVoiceAndVideoResult
     */
    public void onReceiveCommand(GetVoiceAndVideoResult getVoiceAndVideoResult) {
        CustomProtocol customProtocol = new CustomProtocol(getVoiceAndVideoResult.getContextParamsSchema());
        //接收到消息后告知服务端
        WSAPIService.getInstance().sendReceiveStartVoiceAndVideoCallMessageSuccess(getVoiceAndVideoResult.getTracer());
        //判断命令消息是否有效，无效不处理
        if (isCommandAvailable(customProtocol)) {
            String command = customProtocol.getParamMap().get(Constant.COMMAND_CMD);
            switch (command) {
                case Constant.COMMAND_REFUSE:
                    if (customProtocol.getParamMap().get(Constant.COMMAND_ROOM_ID).equals(agoraChannelId)) {
                        VoiceCommunicationCommonLine.onUserReject(getAgoraUidByCloudUid(customProtocol.getParamMap()
                                .get(Constant.COMMAND_UID)), VoiceCommunicationJoinChannelInfoBean.CONNECT_STATE_REFUSE);
                    }
                    break;
                case Constant.COMMAND_DESTROY:
                    if (customProtocol.getParamMap().get(Constant.COMMAND_ROOM_ID).equals(agoraChannelId)) {
                        VoiceCommunicationCommonLine.onUserReject(getAgoraUidByCloudUid(customProtocol.getParamMap().
                                get(Constant.COMMAND_UID)), VoiceCommunicationJoinChannelInfoBean.CONNECT_STATE_LEAVE);
                    }
                    break;
                //正在通话中 消息是invite消息  三者打进电话 发拒绝消息
                case Constant.COMMAND_INVITE:
                    VoiceCommunicationCommonLine.onInvite(customProtocol, getVoiceAndVideoResult);
                    break;
                default:
                    break;
            }
        }
    }

    /**
     * 通过云+id获取agoraid
     *
     * @param cloudUid
     */
    private int getAgoraUidByCloudUid(String cloudUid) {
        for (VoiceCommunicationJoinChannelInfoBean voiceCommunicationJoinChannelInfoBean : voiceCommunicationMemberList) {
            if (voiceCommunicationJoinChannelInfoBean.getUserId().equals(cloudUid)) {
                return voiceCommunicationJoinChannelInfoBean.getAgoraUid();
            }
        }
        return 0;
    }

    /**
     * 通过agoraChannelId获取Channel信息
     *
     * @param agoraChannelId
     */
    public void getChannelInfoByChannelId(String agoraChannelId, String communicationType, String cloudPlusId) {
        if (NetUtils.isNetworkConnected(BaseApplication.getInstance())) {
            ChatAPIService chatAPIService = new ChatAPIService(BaseApplication.getInstance());
            WebService webService = new WebService();
            webService.setCloudPlusChannelId(cloudPlusId);
            webService.setCommunicationType(communicationType);
            chatAPIService.setAPIInterface(webService);
            chatAPIService.getAgoraChannelInfo(agoraChannelId);
        }
    }

    /**
     * 判断邀请人Pre状态
     *
     * @return
     */
    public boolean isInviterPre() {
        return getCommunicationState() == COMMUNICATION_STATE_PRE && isInviter();
    }

    /**
     * 判断被邀请人Pre状态
     *
     * @return
     */
    public boolean isInviteePre() {
        return getCommunicationState() == COMMUNICATION_STATE_PRE && !isInviter();
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
     * 判断通话中状态
     *
     * @return
     */
    public boolean isCommunicationIng() {
        return getCommunicationState() == COMMUNICATION_STATE_ING;
    }

    /**
     * 判断命令消息是否可用
     *
     * @param customProtocol
     * @return
     */
    private boolean isCommandAvailable(CustomProtocol customProtocol) {
        return customProtocol.getProtocol().equals(Constant.COMMAND_ECC_CLOUDPLUS_CMD)
                && !StringUtils.isBlank(customProtocol.getParamMap().get(Constant.COMMAND_CMD));
    }

    /**
     * 打开语音通话界面
     *
     * @param contextParamsRoom
     * @param contextParamsType
     * @param cid
     */
    private void startVoiceOrVideoCall(String contextParamsRoom, String contextParamsType, String cid) {
        Intent intent = new Intent();
        intent.setClass(BaseApplication.getInstance(), VoiceCommunicationActivity.class);
        intent.putExtra(Constant.VOICE_VIDEO_CALL_AGORA_ID, contextParamsRoom);
        intent.putExtra(ConversationActivity.CLOUD_PLUS_CHANNEL_ID, cid);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra(Constant.VOICE_VIDEO_CALL_TYPE, getCommunicationType(contextParamsType));
        intent.putExtra(Constant.VOICE_COMMUNICATION_STATE, VoiceCommunicationActivity.COMMUNICATION_STATE_PRE);
        intent.putExtra("userList", (Serializable) getVoiceCommunicationMemberList());
        BaseApplication.getInstance().startActivity(intent);
    }

    /**
     * 获取通话类型VOICE或者VIDEO
     * @param contextParamsType
     * @return
     */
    private String getCommunicationType(String contextParamsType) {
        if (contextParamsType.equals("VOICE")) {
            return ECMChatInputMenu.VOICE_CALL;
        } else if (contextParamsType.equals("VIDEO")) {
            return ECMChatInputMenu.VIDEO_CALL;
        }
        return ECMChatInputMenu.VOICE_CALL;
    }

    /**
     * 销毁声网，发出离开请求，恢复状态，如果有小窗、通知取消小窗通知，调用离开方法时先判断是否在通话中
     * 或者是不是发起者，发起者一定连接了声网
     */
    public void handleDestroy() {
        if (getCommunicationState() == COMMUNICATION_STATE_ING || isInviter()) {
            leaveChannel();
        } else {
            if (agoraChannelId != null) {
                remindEmmServerRefuseChannel(agoraChannelId);
            }
        }
        if (getWaitAndConnectedNumber() >= 2 && !isInviterPre()) {
            sendCommunicationCommand(Constant.COMMAND_REFUSE);
        } else {
            sendCommunicationCommand(Constant.COMMAND_DESTROY);
        }
        destroyResourceAndState();
        if (communicationType.equals(ECMChatInputMenu.VOICE_CALL)) {
            SuspensionWindowManagerUtils.getInstance().hideCommunicationSmallWindow();
        } else if (communicationType.equals(ECMChatInputMenu.VIDEO_CALL)) {
            VideoSuspensionWindowManagerUtils.getInstance().hideVideoCommunicationSmallWindow();
        }
    }

    /**
     * 启动计时器
     */
    public void startCountDownTimer() {
        if (countDownTimer != null) {
            return;
        }
        //主叫方计时65秒，被叫方60秒
        countDownTimer = new CountDownTimer(isInviter() ? MILLIS_IN_FUTURE_INVITER : MILLIS_IN_FUTURE, COUNT_DOWN_INTERVAL) {
            @Override
            public void onTick(long millisUntilFinished) {
            }

            @Override
            public void onFinish() {
                for (int i = 0; i < voiceCommunicationMemberList.size(); i++) {
                    if (voiceCommunicationMemberList.get(i).getConnectState() == VoiceCommunicationJoinChannelInfoBean.CONNECT_STATE_INIT) {
                        voiceCommunicationMemberList.get(i).setConnectState(VoiceCommunicationJoinChannelInfoBean.CONNECT_STATE_LEAVE);
                    }
                }
                if (onVoiceCommunicationCallbacks != null) {
                    onVoiceCommunicationCallbacks.onRefreshUserState();
                }
                if (getWaitAndConnectedNumber() < 2) {
                    handleDestroy();
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
                communicationState == VoiceCommunicationActivity.COMMUNICATION_STATE_ING;
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
            //美颜效果
            mRtcEngine.setBeautyEffectOptions(true, new
                    BeautyOptions(BeautyOptions.LIGHTENING_CONTRAST_NORMAL, 0.8f, 0.7f, 0.1f));
        }
    }

    public SurfaceView getRemoteView() {
        if (agoraRemoteView == null) {
            agoraRemoteView = RtcEngine.CreateRendererView(BaseApplication.getInstance().getBaseContext());
        }
        return agoraRemoteView;
    }

    public SurfaceView getLocalView() {
        if (agoraLocalView == null) {
            agoraLocalView = RtcEngine.CreateRendererView(BaseApplication.getInstance().getBaseContext());
            agoraLocalView.setZOrderMediaOverlay(true);
        }
        return agoraLocalView;
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
     * 离开频道，不让外部主动调用，外部可以主动调用destroy方法，调用后无论是否成功都告知Emm服务端，不等onleave调用，回调存在时间差，agoraid可能被清空
     */
    private void leaveChannel() {
        if (mRtcEngine != null) {
            mRtcEngine.leaveChannel();
            if (!StringUtils.isBlank(agoraChannelId)) {
                remindEmmServerLeaveChannel(agoraChannelId);
            }
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
    public void destroyResourceAndState() {
        if (countDownTimer != null) {
            countDownTimer.cancel();
            countDownTimer = null;
        }
        NotifyUtil.deleteNotify(BaseApplication.getInstance());
        RtcEngine.destroy();
        mRtcEngine = null;
        if (vibrator != null) {
            vibrator.cancel();
            vibrator = null;
        }
        communicationState = COMMUNICATION_STATE_OVER;
        if (onVoiceCommunicationCallbacks != null) {
            onVoiceCommunicationCallbacks.onActivityFinish();
        }
        onVoiceCommunicationCallbacks = null;
        agoraChannelId = "";
        cloudPlusChannelId = "";
        connectStartTime = 0;
        inviteeInfoBean = null;
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
            } else if (getVoiceCommunicationMemberList().size() <= 9) {
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

    public int getVideoFirstFrameUid() {
        return videoFirstFrameUid;
    }

    public void setVideoFirstFrameUid(int videoFirstFrameUid) {
        this.videoFirstFrameUid = videoFirstFrameUid;
    }

    public OnVoiceCommunicationCallbacks getOnVoiceCommunicationCallbacks() {
        return onVoiceCommunicationCallbacks;
    }

    /**
     * 获取channel信息
     *
     * @param channelId
     * @param agoraChannelId
     */
    public void sendRefuseMessageToInviter(String channelId, String agoraChannelId, String fromUid) {
        String scheme = "ecc-cloudplus-cmd://voice_channel?cmd=refuse&channelid=" + channelId + "&roomid=" + agoraChannelId + "&uid=" + BaseApplication.getInstance().getUid();
        WSAPIService.getInstance().sendStartVoiceAndVideoCallMessage(channelId, agoraChannelId, scheme, "VOICE", getUidArray( fromUid), Constant.VIDEO_CALL_REFUSE);
    }

    /**
     * 获取Uid
     * 排除掉自己防止自己给自己发命令消息
     *
     * @return
     */
    private JSONArray getUidArray(String fromUid) {
        JSONArray jsonArray = new JSONArray();
        jsonArray.put(fromUid);
        return jsonArray;
    }

    /**
     * 获取自己的加入信息，包含token，uid等
     *
     * @param getVoiceCommunicationResult
     * @return
     */
    private VoiceCommunicationJoinChannelInfoBean getMyCommunicationInfoBean(GetVoiceCommunicationResult getVoiceCommunicationResult) {
        List<VoiceCommunicationJoinChannelInfoBean> voiceCommunicationJoinChannelInfoBeanList = getVoiceCommunicationResult.getVoiceCommunicationJoinChannelInfoBeanList();
        for (int i = 0; i < voiceCommunicationJoinChannelInfoBeanList.size(); i++) {
            if (voiceCommunicationJoinChannelInfoBeanList.get(i).getUserId().equals(MyApplication.getInstance().getUid())) {
                return voiceCommunicationJoinChannelInfoBeanList.get(i);
            }
        }
        return null;
    }

    class WebService extends APIInterfaceInstance {
        private Activity activity;
        private String communicationType;
        private String cloudPlusChannelId;

        @Override
        public void returnGetVoiceCommunicationChannelInfoSuccess(GetVoiceCommunicationResult getVoiceCommunicationResult) {
            agoraChannelId = getVoiceCommunicationResult.getChannelId();
            getVoiceCommunicationMemberList().clear();
            getVoiceCommunicationMemberList().addAll(getVoiceCommunicationResult.getVoiceCommunicationJoinChannelInfoBeanList());
            inviteeInfoBean = getMyCommunicationInfoBean(getVoiceCommunicationResult);
            startVoiceOrVideoCall(agoraChannelId, communicationType, cloudPlusChannelId);
        }

        @Override
        public void returnGetVoiceCommunicationChannelInfoFail(String error, int errorCode) {
            handleDestroy();
        }

        @Override
        public void returnJoinVoiceCommunicationChannelSuccess(GetBoolenResult getBoolenResult) {
            //应对主叫方拨打电话，被叫方在前台看到后没接，退到后台，此时主叫方挂断，被叫方没有收到socket（因为在后台），也没有收到声网回调
            //这时点击进入应用点接听，此接口返回false，则挂断通话
            if (!Boolean.parseBoolean(getBoolenResult.getResponse())) {
                if (getCommunicationState() != COMMUNICATION_STATE_OVER) {
                    ToastUtils.show(R.string.voice_communication_direct_calling_canceled);
                }
                handleDestroy();
            }
        }

        @Override
        public void returnJoinVoiceCommunicationChannelFail(String error, int errorCode) {
        }

        public Activity getActivity() {
            return activity;
        }

        public void setActivity(Activity activity) {
            this.activity = activity;
        }

        public void setCommunicationType(String communicationType) {
            this.communicationType = communicationType;
        }

        public void setCloudPlusChannelId(String cloudPlusChannelId) {
            this.cloudPlusChannelId = cloudPlusChannelId;
        }
    }
}
