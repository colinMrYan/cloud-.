package com.inspur.emmcloud.ui.chat;

import android.bluetooth.BluetoothHeadset;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Build;
import android.os.SystemClock;
import android.provider.Settings;
import android.support.annotation.RequiresApi;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Chronometer;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.inspur.emmcloud.MyApplication;
import com.inspur.emmcloud.R;
import com.inspur.emmcloud.adapter.VoiceCommunicationMemberAdapter;
import com.inspur.emmcloud.api.APIInterfaceInstance;
import com.inspur.emmcloud.api.apiservice.ChatAPIService;
import com.inspur.emmcloud.baselib.util.DensityUtil;
import com.inspur.emmcloud.baselib.util.ResolutionUtils;
import com.inspur.emmcloud.baselib.util.TimeUtils;
import com.inspur.emmcloud.baselib.util.ToastUtils;
import com.inspur.emmcloud.baselib.widget.CircleTextImageView;
import com.inspur.emmcloud.baselib.widget.dialogs.CustomDialog;
import com.inspur.emmcloud.basemodule.application.BaseApplication;
import com.inspur.emmcloud.basemodule.config.Constant;
import com.inspur.emmcloud.basemodule.ui.BaseActivity;
import com.inspur.emmcloud.basemodule.util.AppUtils;
import com.inspur.emmcloud.basemodule.util.ClickRuleUtil;
import com.inspur.emmcloud.basemodule.util.ImageDisplayUtils;
import com.inspur.emmcloud.basemodule.util.NetUtils;
import com.inspur.emmcloud.basemodule.util.WebServiceMiddleUtils;
import com.inspur.emmcloud.basemodule.util.systool.emmpermission.Permissions;
import com.inspur.emmcloud.basemodule.util.systool.permission.PermissionRequestCallback;
import com.inspur.emmcloud.basemodule.util.systool.permission.PermissionRequestManagerUtils;
import com.inspur.emmcloud.bean.chat.Conversation;
import com.inspur.emmcloud.bean.chat.GetVoiceAndVideoResult;
import com.inspur.emmcloud.bean.chat.GetVoiceCommunicationResult;
import com.inspur.emmcloud.bean.chat.VoiceCommunicationAudioVolumeInfo;
import com.inspur.emmcloud.bean.chat.VoiceCommunicationJoinChannelInfoBean;
import com.inspur.emmcloud.broadcastreceiver.VoiceCommunicationHeadSetReceiver;
import com.inspur.emmcloud.ui.AppSchemeHandleActivity;
import com.inspur.emmcloud.util.privates.MediaPlayerManagerUtils;
import com.inspur.emmcloud.util.privates.NotifyUtil;
import com.inspur.emmcloud.util.privates.SuspensionWindowManagerUtils;
import com.inspur.emmcloud.util.privates.VoiceCommunicationManager;
import com.inspur.emmcloud.util.privates.VoiceCommunicationToastUtil;
import com.inspur.emmcloud.util.privates.cache.ConversationCacheUtils;
import com.inspur.emmcloud.widget.ECMChatInputMenu;
import com.inspur.emmcloud.widget.ECMSpaceItemDecoration;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.agora.rtc.RtcEngine;
import io.agora.rtc.video.VideoCanvas;

/**
 * Created by yufuchang on 2018/8/14.
 * 截止191018
 * 能进入这个页面的入口有：
 *
 * @see ConversationActivity#startVoiceOrVideoCall(String, List)
 * @see SuspensionWindowManagerUtils#goBackVoiceCommunicationActivity()
 * @see NotifyUtil#sendNotifyMsg(Context)
 * @see CommunicationFragment#onReceiveVoiceOrVideoCall(GetVoiceAndVideoResult)
 * @see AppSchemeHandleActivity#openScheme()
 */
public class ChannelVoiceCommunicationActivity extends BaseActivity {
    /**
     * 通话三种状态pre代表正在邀请未接通，ing代表通话中，over代表通话结束，或者来了邀请未点击接听的状态，存储在变量
     * 默认是over状态
     *
     * @see VoiceCommunicationManager#communicationState
     */
    public static final int COMMUNICATION_STATE_PRE = 0;
    public static final int COMMUNICATION_STATE_ING = 4;
    public static final int COMMUNICATION_STATE_OVER = 8;
    /**
     * refuse状态
     */
    public static final int COMMUNICATION_REFUSE = 1;
    /**
     * leave状态
     */
    public static final int COMMUNICATION_LEAVE = 2;
    /**
     * 请求悬浮窗权限
     */
    private static final int REQUEST_WINDOW_PERMISSION = 100;
    private static final int REQUEST_BACKGROUND_WINDOWS = 101;
    /**
     * 是否来自小窗
     */
    private boolean isFromSmallWindow = false;
    /**
     * 声网的channelId
     */
    private String agoraChannelId = "";
    /**
     * 云+的Id
     */
    private String cloudPlusChannelId = "";
    /**
     * 指明发起此会话的channel是单聊还是群聊
     */
    private String directOrGroupType = "";
    /**
     * 会话类型 VOICE_CALL或者VIDEO_CALL
     */
    private String communicationType = "";
    /**
     * 视频会话小视图
     */
    private SurfaceView agoraLocalView;
    /**
     * 视频会话大视图
     */
    private SurfaceView agoraRemoteView;
    /**
     * 本地视频初始x坐标
     */
    private int initX;
    boolean needTimerStartFlag = true;
    @BindView(R.id.img_an_excuse)
    ImageView excuseImg;
    @BindView(R.id.ll_voice_communication_invite)
    LinearLayout inviteeLinearLayout;
    @BindView(R.id.img_user_head)
    CircleTextImageView userHeadImg;
    @BindView(R.id.tv_user_name)
    TextView userNameTv;
    @BindView(R.id.ll_voice_communication_invite_members)
    LinearLayout inviteMembersGroupLinearLayout;
    /**
     * 本地视频初始y坐标
     */
    private int initY;
    @BindView(R.id.recyclerview_voice_communication_first)
    RecyclerView firstRecyclerView;
    @BindView(R.id.recyclerview_voice_communication_second)
    RecyclerView secondRecyclerView;
    @BindView(R.id.ll_voice_communication_memebers)
    LinearLayout communicationMembersLinearLayout;
    @BindView(R.id.recyclerview_voice_communication_memebers_first)
    RecyclerView communicationMembersFirstRecyclerview;
    @BindView(R.id.recyclerview_voice_communication_memebers_second)
    RecyclerView communicationMemberSecondRecyclerview;
    @BindView(R.id.tv_voice_communication_state)
    TextView communicationStateTv;
    @BindView(R.id.tv_voice_communication_time)
    Chronometer communicationTimeChronometer;
    @BindView(R.id.ll_voice_communication_function_group)
    LinearLayout functionLinearLayout;
    @BindView(R.id.tv_an_excuse)
    TextView excuseTv;
    @BindView(R.id.img_hands_free)
    ImageView handsFreeImg;
    @BindView(R.id.tv_hands_free)
    TextView handsFreeTv;
    @BindView(R.id.img_mute)
    ImageView muteImg;
    @BindView(R.id.tv_mute)
    TextView muteTv;
    @BindView(R.id.img_tran_video)
    ImageView tranVideoImg;
    @BindView(R.id.tv_tran_video)
    TextView tranVideoTv;
    @BindView(R.id.img_answer_the_phone)
    ImageView answerPhoneImg;
    @BindView(R.id.img_hung_up)
    ImageView hungUpImg;
    @BindView(R.id.img_voice_communication_pack_up)
    ImageView packUpImg;
    @BindView(R.id.rl_remote_video_view_container)
    RelativeLayout remoteVideoContainer;
    @BindView(R.id.fl_local_video_view_container)
    FrameLayout localVideoContainer;
    @BindView(R.id.rl_video_call_layout)
    RelativeLayout videoCallLayout;
    @BindView(R.id.rl_voice_call_layout)
    RelativeLayout voiceCallLayout;
    @BindView(R.id.rl_root)
    RelativeLayout rootLayout;
    @BindView(R.id.ll_turn_to_voice)
    LinearLayout turnToVoiceLayout;
    @BindView(R.id.ll_video_hung_up)
    LinearLayout videoHungUp;
    @BindView(R.id.ll_video_answer_phone)
    LinearLayout answerVideoPhoneLayout;
    @BindView(R.id.ll_video_switch_camera)
    LinearLayout switchCameraLayout;
    @BindView(R.id.img_video_communication_pack_up)
    ImageView videoPackUpImg;
    @BindView(R.id.rl_person_info)
    RelativeLayout personInfoLayout;
    @BindView(R.id.rl_voice_communication_operate)
    RelativeLayout groupAnswerOrHungUpLayout;
    @BindView(R.id.ll_voice_communication_function_direct)
    LinearLayout directFunctionLayout;
    @BindView(R.id.ll_mute_direct)
    LinearLayout directMuteLayout;
    @BindView(R.id.ll_hung_up_direct)
    LinearLayout directHungUpLayout;
    @BindView(R.id.ll_answer_phone_direct)
    LinearLayout directAnswerPhoneLayout;
    @BindView(R.id.ll_hands_free_direct)
    LinearLayout directHansFreeLayout;
    @BindView(R.id.img_mute_direct)
    ImageView directMuteImg;
    @BindView(R.id.tv_mute_direct)
    TextView directMuteTv;
    @BindView(R.id.img_hands_free_direct)
    ImageView directHandFreeImg;
    @BindView(R.id.tv_hands_free_direct)
    TextView directHandFreeTv;
    private ChatAPIService apiService;
    private VoiceCommunicationMemberAdapter voiceCommunicationMemberAdapterFirst;
    private VoiceCommunicationMemberAdapter voiceCommunicationMemberAdapterSecond;
    private MediaPlayerManagerUtils mediaPlayerManagerUtils;
    private VoiceCommunicationManager voiceCommunicationManager;
    private VoiceCommunicationHeadSetReceiver receiver;

    @Override
    public void onCreate() {
        ButterKnife.bind(this);
        voiceCommunicationManager = VoiceCommunicationManager.getInstance();
        voiceCommunicationManager.initializeAgoraEngine();
        init();
        registerReceiver();
        NotifyUtil.sendNotifyMsg(this);
    }

    @Override
    protected void onStart() {
        super.onStart();
        voiceCommunicationManager.startCountDownTimer();
        checkHasPermission();
    }

    private void init() {
        isFromSmallWindow = getIntent().getBooleanExtra(Constant.VOICE_IS_FROM_SMALL_WINDOW, false);
        initData();
        setInviterInfo();
        initAgoraCallbacks();
        initViews();
        SuspensionWindowManagerUtils.getInstance().hideCommunicationSmallWindow();
    }

    private void initData() {
        if (!isFromSmallWindow) {
            cloudPlusChannelId = getIntent().getStringExtra(ConversationActivity.CLOUD_PLUS_CHANNEL_ID);
            voiceCommunicationManager.setCloudPlusChannelId(cloudPlusChannelId);
            communicationType = getIntent().getStringExtra(Constant.VOICE_VIDEO_CALL_TYPE);
            voiceCommunicationManager.setCommunicationType(communicationType);
            directOrGroupType = ConversationCacheUtils.getConversationType(this, cloudPlusChannelId);
            List<VoiceCommunicationJoinChannelInfoBean> list = (List<VoiceCommunicationJoinChannelInfoBean>) getIntent().getSerializableExtra("userList");
            int communicationState = getIntent().getIntExtra(Constant.VOICE_COMMUNICATION_STATE, -1);
            voiceCommunicationManager.setCommunicationState(communicationState);
            if (list != null) {
                voiceCommunicationManager.setVoiceCommunicationMemberList(list);
            }
        } else {
            recoverData();
        }
    }

    /**
     * 如果是从小窗口来的，则恢复通话数据
     */
    private void recoverData() {
        agoraChannelId = voiceCommunicationManager.getAgoraChannelId();
        communicationType = voiceCommunicationManager.getCommunicationType();
        refreshCommunicationMembersAdapterWithState();
        cloudPlusChannelId = voiceCommunicationManager.getCloudPlusChannelId();
        directOrGroupType = ConversationCacheUtils.getConversationType(this, cloudPlusChannelId);
        refreshCommunicationMembersAdapterWithoutState();
    }

    /**
     * 进入小窗状态，保存状态
     */
    private void saveCommunicationData() {
        voiceCommunicationManager.setAgoraChannelId(agoraChannelId);
        voiceCommunicationManager.setCommunicationType(communicationType);
        voiceCommunicationManager.setCloudPlusChannelId(cloudPlusChannelId);
    }

    /**
     * 注册耳机插拔监听
     */
    public void registerReceiver() {
        receiver = new VoiceCommunicationHeadSetReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_HEADSET_PLUG);
        filter.addAction(AudioManager.ACTION_AUDIO_BECOMING_NOISY);
        filter.addAction(BluetoothHeadset.ACTION_CONNECTION_STATE_CHANGED);
        registerReceiver(receiver, filter);
    }

    /**
     * 检查权限
     */
    private void checkHasPermission() {
        PermissionRequestManagerUtils.getInstance().requestRuntimePermission(this, Permissions.RECORD_AUDIO, new PermissionRequestCallback() {
            @Override
            public void onPermissionRequestSuccess(List<String> permissions) {
            }

            @Override
            public void onPermissionRequestFail(List<String> permissions) {
                ToastUtils.show(ChannelVoiceCommunicationActivity.this, PermissionRequestManagerUtils.getInstance().getPermissionToast(ChannelVoiceCommunicationActivity.this, permissions));
                if (!isFinishing()) {
                    voiceCommunicationManager.handleDestroy();
                    finish();
                }
            }
        });
    }

    @Override
    public int getLayoutResId() {
        return R.layout.activity_voice_channel;
    }

    private void refreshCommunicationMembersAdapterWithoutState() {
        if (voiceCommunicationManager.isInviterPre() || voiceCommunicationManager.isInviteePre()) {
            List<VoiceCommunicationJoinChannelInfoBean> totalList = voiceCommunicationManager.getVoiceCommunicationMemberList();
            ImageDisplayUtils.getInstance().displayImage(userHeadImg, totalList.get(0).getHeadImageUrl(), R.drawable.icon_person_default);
            userNameTv.setText(totalList.get(0).getUserName());
            if (totalList.size() <= 5) {
                communicationMembersFirstRecyclerview.setAdapter(new VoiceCommunicationMemberAdapter(ChannelVoiceCommunicationActivity.this, totalList, 3));
            } else if (totalList.size() <= 9) {
                List<VoiceCommunicationJoinChannelInfoBean> list1 = totalList.subList(0, 5);
                List<VoiceCommunicationJoinChannelInfoBean> list2 = totalList.subList(5, totalList.size());
                communicationMembersFirstRecyclerview.setAdapter(new VoiceCommunicationMemberAdapter(ChannelVoiceCommunicationActivity.this, list1, 3));
                communicationMemberSecondRecyclerview.setAdapter(new VoiceCommunicationMemberAdapter(ChannelVoiceCommunicationActivity.this, list2, 3));
            }
        }
    }

    /**
     * 初始化功能模块的初始值，如果正在通话中则需要判断是不是要恢复语音通话状态
     * 如是否免提，是否静音
     */
    private void handleFunctionState() {
        if (voiceCommunicationManager.isCommunicationIng()) {
            int colorUnSelected = ContextCompat.getColor(this, R.color.voice_communication_function_default);
            int colorSelected = ContextCompat.getColor(this, R.color.voice_communication_function_select);
            if (directOrGroupType.equals(Conversation.TYPE_GROUP)) {
                handsFreeImg.setSelected(voiceCommunicationManager.isHandsFree());
                muteImg.setSelected(voiceCommunicationManager.isMute());
                handsFreeImg.setImageResource(voiceCommunicationManager.isHandsFree() ? R.drawable.icon_hands_free_selected : R.drawable.icon_hands_free_unselected);
                handsFreeTv.setTextColor(voiceCommunicationManager.isHandsFree() ? colorSelected : colorUnSelected);
                muteImg.setImageResource(voiceCommunicationManager.isMute() ? R.drawable.icon_mute_selected : R.drawable.icon_mute_unselected);
                muteTv.setTextColor(voiceCommunicationManager.isMute() ? colorSelected : colorUnSelected);
            } else if (directOrGroupType.equals(Conversation.TYPE_DIRECT)) {
                directHandFreeImg.setSelected(voiceCommunicationManager.isHandsFree());
                directHandFreeImg.setImageResource(voiceCommunicationManager.isHandsFree() ? R.drawable.icon_direct_hands_free_selected : R.drawable.icon_direct_hands_free);
                directHandFreeTv.setTextColor(voiceCommunicationManager.isHandsFree() ? colorSelected : colorUnSelected);
                directMuteImg.setImageResource(voiceCommunicationManager.isMute() ? R.drawable.icon_direct_mute_selected : R.drawable.icon_direct_mute);
                directMuteTv.setTextColor(voiceCommunicationManager.isMute() ? colorSelected : colorUnSelected);
                directMuteImg.setSelected(voiceCommunicationManager.isMute());
            }
        }
        voiceCommunicationManager.muteLocalAudioStream(voiceCommunicationManager.isMute());
        voiceCommunicationManager.muteAllRemoteAudioStreams(false);
        voiceCommunicationManager.onSwitchSpeakerphoneClicked(voiceCommunicationManager.isHandsFree());
    }

    /**
     * 创建频道，由主叫方调用
     */
    private void createChannel() {
        if (NetUtils.isNetworkConnected(this)) {
            try {
                JSONArray jsonArray = new JSONArray();
                List<VoiceCommunicationJoinChannelInfoBean> totalList = voiceCommunicationManager.getVoiceCommunicationMemberList();
                for (int i = 0; i < totalList.size(); i++) {
                    JSONObject jsonObjectUserInfo = new JSONObject();
                    jsonObjectUserInfo.put("id", totalList.get(i).getUserId());
                    jsonObjectUserInfo.put("name", totalList.get(i).getUserName());
                    jsonArray.put(jsonObjectUserInfo);
                }
                apiService.getAgoraParams(jsonArray);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 根据状态改变布局可见性
     */
    private void handleCommunicationViewsState() {
        changeFunctionState();
        if (communicationType.equals(ECMChatInputMenu.VOICE_CALL)) {
            refreshCommunicationMembersAdapterWithState();
        }
    }

    /**
     * 修改功能键的方法点击事件时调用
     */
    private void changeFunctionState() {
        if (communicationType.equals(ECMChatInputMenu.VOICE_CALL)) {
            directFunctionLayout.setVisibility(directOrGroupType.equals(Conversation.TYPE_DIRECT) ? View.VISIBLE : View.GONE);
            groupAnswerOrHungUpLayout.setVisibility(directOrGroupType.equals(Conversation.TYPE_GROUP) ? View.VISIBLE : View.GONE);
            functionLinearLayout.setVisibility(directOrGroupType.equals(Conversation.TYPE_GROUP) ? View.VISIBLE : View.GONE);

            inviteeLinearLayout.setVisibility(voiceCommunicationManager.isInviteePre() ? View.VISIBLE : View.GONE);
            inviteMembersGroupLinearLayout.setVisibility((voiceCommunicationManager.isInviterPre() || voiceCommunicationManager.isCommunicationIng()) ? View.VISIBLE : View.GONE);
            communicationMembersLinearLayout.setVisibility(voiceCommunicationManager.isInviteePre() && directOrGroupType.equals(Conversation.TYPE_GROUP) ? View.VISIBLE : View.GONE);
            functionLinearLayout.setVisibility((voiceCommunicationManager.isInviterPre() || voiceCommunicationManager.isCommunicationIng()) && directOrGroupType.equals(Conversation.TYPE_GROUP) ? View.VISIBLE : View.GONE);
            communicationStateTv.setVisibility((voiceCommunicationManager.isInviterPre() || voiceCommunicationManager.isCommunicationIng()) ? View.VISIBLE : View.GONE);
            communicationTimeChronometer.setVisibility(voiceCommunicationManager.isCommunicationIng() ? View.VISIBLE : View.GONE);

            //悬浮窗控制按钮
            packUpImg.setVisibility(voiceCommunicationManager.isCommunicationIng() ? View.VISIBLE : View.GONE);
            communicationStateTv.setText(voiceCommunicationManager.isInviterPre() ? getString(R.string.voice_communication_dialog) : (voiceCommunicationManager.isInviteePre() ? getString(R.string.voice_communication_waitting_answer) : (voiceCommunicationManager.isCommunicationIng() ? getString(R.string.voice_communicaiton_watting_talking) : "")));
            int colorNormal = ContextCompat.getColor(this, R.color.voice_communication_function_default);
            int colorUnavailiable = ContextCompat.getColor(this, R.color.voice_communication_function_unavailiable_text);
            if (directOrGroupType.equals(Conversation.TYPE_GROUP)) {
                answerPhoneImg.setVisibility((voiceCommunicationManager.isInviteePre()) ? View.VISIBLE : View.GONE);
                excuseImg.setImageResource(voiceCommunicationManager.isCommunicationIng() ? R.drawable.icon_mute_unselected : R.drawable.icon_mute_unavailable);
                excuseTv.setTextColor(voiceCommunicationManager.isCommunicationIng() ? colorNormal : colorUnavailiable);
                handsFreeImg.setImageResource(voiceCommunicationManager.isCommunicationIng() ? R.drawable.icon_hands_free_unselected : R.drawable.icon_hands_free_unavailable);
                handsFreeTv.setTextColor(voiceCommunicationManager.isCommunicationIng() ? colorNormal : colorUnavailiable);
                muteImg.setImageResource(voiceCommunicationManager.isCommunicationIng() ? R.drawable.icon_mute_unselected : R.drawable.icon_mute_unavailable);
                muteTv.setTextColor(voiceCommunicationManager.isCommunicationIng() ? colorNormal : colorUnavailiable);
            } else if (directOrGroupType.equals(Conversation.TYPE_DIRECT)) {
                directMuteLayout.setVisibility(voiceCommunicationManager.isCommunicationIng() || voiceCommunicationManager.isInviterPre() ? View.VISIBLE : View.GONE);
                directHansFreeLayout.setVisibility(voiceCommunicationManager.isCommunicationIng() || voiceCommunicationManager.isInviterPre() ? View.VISIBLE : View.GONE);
                directAnswerPhoneLayout.setVisibility(voiceCommunicationManager.isInviteePre() ? View.VISIBLE : View.GONE);
                directHandFreeImg.setImageResource(voiceCommunicationManager.isCommunicationIng() ? R.drawable.icon_direct_hands_free : R.drawable.icon_direct_hands_free_unavailable);
                directHandFreeTv.setTextColor(voiceCommunicationManager.isCommunicationIng() ? colorNormal : colorUnavailiable);
                directMuteImg.setImageResource(voiceCommunicationManager.isCommunicationIng() ? R.drawable.icon_direct_mute : R.drawable.icon_direct_mute_unavailable);
                directMuteTv.setTextColor(voiceCommunicationManager.isCommunicationIng() ? colorNormal : colorUnavailiable);
            }
            //如果是通话中则“通话中”文字显示一下就不再显示
            communicationStateTv.setText(voiceCommunicationManager.isCommunicationIng() ? "" : communicationStateTv.getText());
            changeMediaPlayState();
        } else if (communicationType.equals(ECMChatInputMenu.VIDEO_CALL)) {
            voiceCommunicationManager.adjustPlaybackSignalVolume(400);
            voiceCommunicationManager.setEnableSpeakerphone(true);
            turnToVoiceLayout.setVisibility(voiceCommunicationManager.isCommunicationIng() ? View.VISIBLE : View.GONE);
            videoHungUp.setVisibility((voiceCommunicationManager.isCommunicationIng() || voiceCommunicationManager.isInviterPre() || voiceCommunicationManager.isInviteePre()) ? View.VISIBLE : View.GONE);
            answerVideoPhoneLayout.setVisibility(voiceCommunicationManager.isInviteePre() ? View.VISIBLE : View.GONE);
            switchCameraLayout.setVisibility(voiceCommunicationManager.isCommunicationIng() ? View.VISIBLE : View.GONE);
            videoPackUpImg.setVisibility(View.GONE);
            personInfoLayout.setVisibility((voiceCommunicationManager.isInviterPre() || voiceCommunicationManager.isInviteePre()) ? View.VISIBLE : View.GONE);
            if (voiceCommunicationManager.isCommunicationIng()) {
                localVideoContainer.getLayoutParams().height = DensityUtil.dip2px(this, 167);
                localVideoContainer.getLayoutParams().width = DensityUtil.dip2px(this, 94);
            }
        }
    }

    private void changeMediaPlayState() {
        if (voiceCommunicationManager.getCommunicationState() == COMMUNICATION_STATE_PRE) {
            mediaPlayerManagerUtils.changeToSpeakerMode();
            mediaPlayerManagerUtils.play(R.raw.voice_communication_watting_answer, null, true);
        } else {
            mediaPlayerManagerUtils.stop();
        }
    }

    @Override
    protected int getStatusType() {
        return super.getStatusType();
    }


    /**
     * 初始化Views
     */
    private void initViews() {
        //根据communicationType切换语音通话和视频通话布局
        if (communicationType.equals(ECMChatInputMenu.VIDEO_CALL)) {
            videoCallLayout.setVisibility(View.VISIBLE);
            voiceCallLayout.setVisibility(View.GONE);
        } else if (communicationType.equals(ECMChatInputMenu.VOICE_CALL)) {
            voiceCallLayout.setVisibility(View.VISIBLE);
            videoCallLayout.setVisibility(View.GONE);
        }
        apiService = new ChatAPIService(this);
        apiService.setAPIInterface(new WebService());
        mediaPlayerManagerUtils = MediaPlayerManagerUtils.getManager();
        initLayoutAndRecyclerViews();
        initTimeAndTextLocation();
        handleCommunicationViewsState();
        handleFunctionState();
        handlePreState();
        handleComebackFromSmallWindowTime();
    }

    private void handleComebackFromSmallWindowTime() {
        //通话相差时间
        if (isFromSmallWindow) {
            long duration = System.currentTimeMillis() - voiceCommunicationManager.getConnectStartTime();
            communicationTimeChronometer.setBase(SystemClock.elapsedRealtime() - duration);
            communicationTimeChronometer.start();
        }
    }

    private void initLayoutAndRecyclerViews() {
        voiceCommunicationMemberAdapterFirst = new VoiceCommunicationMemberAdapter(this, voiceCommunicationManager.getVoiceCommunicationMemberListTop(), 0);
        voiceCommunicationMemberAdapterSecond = new VoiceCommunicationMemberAdapter(this, voiceCommunicationManager.getVoiceCommunicationMemberListBottom(), 0);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
        firstRecyclerView.setLayoutManager(layoutManager);
        firstRecyclerView.addItemDecoration(new ECMSpaceItemDecoration(DensityUtil.dip2px(this, 8)));
        firstRecyclerView.setAdapter(voiceCommunicationMemberAdapterFirst);

        LinearLayoutManager layoutManager2 = new LinearLayoutManager(this);
        layoutManager2.setOrientation(LinearLayoutManager.HORIZONTAL);
        secondRecyclerView.setLayoutManager(layoutManager2);
        secondRecyclerView.addItemDecoration(new ECMSpaceItemDecoration(DensityUtil.dip2px(this, 8)));
        secondRecyclerView.setAdapter(voiceCommunicationMemberAdapterSecond);

        LinearLayoutManager layoutManagerMembersSecond = new LinearLayoutManager(this);
        layoutManagerMembersSecond.setOrientation(LinearLayoutManager.HORIZONTAL);
        communicationMemberSecondRecyclerview.addItemDecoration(new ECMSpaceItemDecoration(DensityUtil.dip2px(this, 8)));
        communicationMemberSecondRecyclerview.setLayoutManager(layoutManagerMembersSecond);

        LinearLayoutManager layoutManagerMemebersFirst = new LinearLayoutManager(this);
        layoutManagerMemebersFirst.setOrientation(LinearLayoutManager.HORIZONTAL);
        communicationMembersFirstRecyclerview.addItemDecoration(new ECMSpaceItemDecoration(DensityUtil.dip2px(this, 8)));
        communicationMembersFirstRecyclerview.setLayoutManager(layoutManagerMemebersFirst);
    }

    private void handlePreState() {
        if (!isFromSmallWindow) {
            if (voiceCommunicationManager.isInviterPre()) {
                refreshCommunicationMembersAdapterWithState();
                createChannel();
            } else if (voiceCommunicationManager.isInviteePre()) {
                agoraChannelId = getIntent().getStringExtra(Constant.VOICE_VIDEO_CALL_AGORA_ID);
                voiceCommunicationManager.setEncryptionSecret(agoraChannelId);
                refreshCommunicationMembersAdapterWithoutState();
            }
        }
    }

    /**
     * 设置计时器和提示语的位置
     */
    private void initTimeAndTextLocation() {
        RelativeLayout.LayoutParams tvParams = (RelativeLayout.LayoutParams) communicationStateTv.getLayoutParams();
        RelativeLayout.LayoutParams chronometerParams = (RelativeLayout.LayoutParams) communicationTimeChronometer.getLayoutParams();
        if (directOrGroupType.equals(Conversation.TYPE_DIRECT)) {
            tvParams.setMargins(0, 0, 0, DensityUtil.dip2px(136));
            chronometerParams.setMargins(0, 0, 0, DensityUtil.dip2px(136));
        } else if (directOrGroupType.equals(Conversation.TYPE_GROUP)) {
            tvParams.setMargins(0, 0, 0, DensityUtil.dip2px(235));
            chronometerParams.setMargins(0, 0, 0, DensityUtil.dip2px(235));
        }
        communicationStateTv.setLayoutParams(tvParams);
        communicationTimeChronometer.setLayoutParams(chronometerParams);
    }

    /**
     * 初始化回调
     */
    private void initAgoraCallbacks() {
        voiceCommunicationManager.setOnVoiceCommunicationCallbacks(new OnVoiceCommunicationCallbacksImpl() {
            @Override
            public void onUserJoined(final int uid, int elapsed) {
                if (voiceCommunicationManager.getConnectedNumber() >= 2) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (mediaPlayerManagerUtils != null) {
                                mediaPlayerManagerUtils.stop();
                            }
                            if (voiceCommunicationManager.isInviter() && needTimerStartFlag && voiceCommunicationManager.getConnectedNumber() == 2) {
                                needTimerStartFlag = false;
                                startChronometer();
                                changeFunctionState();
                            }
                            if (voiceCommunicationManager.isCommunicationIng()) {
                                refreshCommunicationMembersAdapterWithState();
                            }
                        }
                    });
                }
            }

            @Override
            public void onJoinChannelSuccess(final String channel, final int uid, int elapsed) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        //检查加入的如果是自己且不是邀请者启动计时器
                        if (!voiceCommunicationManager.isInviter() && isMySelf(uid)) {
                            startChronometer();
                            changeFunctionState();
                        }
                        refreshCommunicationMembersAdapterWithState();
                    }
                });
            }

            @Override
            public void onNetworkQuality(final int uid, final int txQuality, int rxQuality) {
                if (voiceCommunicationManager.isCommunicationIng()) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            communicationStateTv.setText((uid == 0 && txQuality >= 5) ? getString(R.string.voice_communication_quality) : "");
                            communicationTimeChronometer.setVisibility((uid == 0 && txQuality >= 5) ? View.GONE : View.VISIBLE);
                        }
                    });
                }
            }

            @Override
            public void onAudioVolumeIndication(final VoiceCommunicationAudioVolumeInfo[] speakers, final int totalVolume) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        //声音高低变化只在界面上有用，把这个逻辑写在这里
                        List<VoiceCommunicationJoinChannelInfoBean> totalList = voiceCommunicationManager.getVoiceCommunicationMemberList();
                        if (speakers != null && speakers.length > 0) {
                            for (int i = 0; i < speakers.length; i++) {
                                int agoraId = speakers[i].uid;
                                for (int j = 0; j < totalList.size(); j++) {
                                    if (totalList.get(j).getAgoraUid() == agoraId) {
                                        totalList.get(j).setVolume(speakers[i].volume);
                                    }
                                }
                            }
                            if (totalVolume == 0) {
                                for (int i = 0; i < totalList.size(); i++) {
                                    totalList.get(i).setVolume(0);
                                }
                            }
                            refreshCommunicationMembersAdapterWithState();
                        }
                    }
                });
            }

            @Override
            public void onRemoteVideoStateChanged(final int uid, int state, int reason, int elapsed) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        //屏蔽视频
//                        setupRemoteVideo(uid);
                    }
                });
            }

            @Override
            public void onActivityFinish() {
                finish();
            }

            @Override
            public void onRefreshUserState() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        refreshCommunicationMembersAdapterWithState();
                    }
                });
            }
        });
    }

    private void startChronometer() {
        communicationTimeChronometer.setBase(SystemClock.elapsedRealtime());
        communicationTimeChronometer.start();
        voiceCommunicationManager.setConnectStartTime(System.currentTimeMillis());
    }

    /**
     * 如果加入频道的是自己
     * @param uid
     * @return
     */
    private boolean isMySelf(int uid) {
        for (VoiceCommunicationJoinChannelInfoBean voiceCommunicationJoinChannelInfoBean : voiceCommunicationManager.getVoiceCommunicationMemberList()) {
            if (voiceCommunicationJoinChannelInfoBean.getAgoraUid() == uid) {
                return voiceCommunicationJoinChannelInfoBean.getUserId().equals(BaseApplication.getInstance().getUid());
            }
        }
        return false;
    }

    /**
     * 设置本地视频显示
     */
    private void setupLocalVideo() {
        if (!communicationType.equals(ECMChatInputMenu.VIDEO_CALL)) {
            return;
        }
        //设置本地视图
        agoraLocalView = RtcEngine.CreateRendererView(getBaseContext());
        agoraLocalView.setZOrderMediaOverlay(true);
        if (voiceCommunicationManager.isInviterPre()) {
            localVideoContainer.getLayoutParams().height = FrameLayout.LayoutParams.MATCH_PARENT;
            localVideoContainer.getLayoutParams().width = FrameLayout.LayoutParams.MATCH_PARENT;
        }
        localVideoContainer.addView(agoraLocalView);
        voiceCommunicationManager.getRtcEngine().setupLocalVideo(new VideoCanvas(agoraLocalView, VideoCanvas.RENDER_MODE_HIDDEN, 0));
    }

    private void setupRemoteVideo(int uid) {
        //设置远程视图
        int count = remoteVideoContainer.getChildCount();
        View view = null;
        for (int i = 0; i < count; i++) {
            View v = remoteVideoContainer.getChildAt(i);
            if (v.getTag() instanceof Integer && ((int) v.getTag()) == uid) {
                view = v;
            }
        }
        if (view != null) {
            return;
        }
        agoraRemoteView = RtcEngine.CreateRendererView(getBaseContext());
        remoteVideoContainer.addView(agoraRemoteView);
        voiceCommunicationManager.getRtcEngine().setupRemoteVideo(new VideoCanvas(agoraRemoteView, VideoCanvas.RENDER_MODE_HIDDEN, uid));
        agoraRemoteView.setTag(uid);
    }

    /**
     * 刷新成员adapter，有人加入，退出，声音变化都通过这个方法来刷新
     */
    private void refreshCommunicationMembersAdapterWithState() {
        voiceCommunicationManager.handleVoiceCommunicationMemberList();
        if (voiceCommunicationManager.getVoiceCommunicationMemberList().size() <= 5) {
            if (voiceCommunicationMemberAdapterFirst != null) {
                voiceCommunicationMemberAdapterFirst.setMemberDataAndRefresh(
                        voiceCommunicationManager.getVoiceCommunicationMemberListTop(), 1);
            }
        } else if (voiceCommunicationManager.getVoiceCommunicationMemberList().size() <= 9) {
            if (voiceCommunicationMemberAdapterFirst != null) {
                voiceCommunicationMemberAdapterFirst.setMemberDataAndRefresh(
                        voiceCommunicationManager.getVoiceCommunicationMemberListTop(), 1);
            }
            if (voiceCommunicationMemberAdapterSecond != null) {
                voiceCommunicationMemberAdapterSecond.setMemberDataAndRefresh(
                        voiceCommunicationManager.getVoiceCommunicationMemberListBottom(), 2);
            }
        }
    }

    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.img_an_excuse:
                switchFunctionViewUIState(excuseImg, excuseTv);
                voiceCommunicationManager.muteAllRemoteAudioStreams(excuseImg.isSelected());
                excuseImg.setImageResource(excuseImg.isSelected() ? R.drawable.icon_mute_selected : R.drawable.icon_mute_unselected);
                break;
            case R.id.ll_hands_free_direct:
                handleFunctionState(directHandFreeImg, directHandFreeTv, R.drawable.icon_direct_hands_free_selected, R.drawable.icon_direct_hands_free, 2);
                break;
            case R.id.ll_group_hands_free:
                handleFunctionState(handsFreeImg, handsFreeTv, R.drawable.icon_hands_free_selected, R.drawable.icon_hands_free_unselected, 2);
                break;
            case R.id.ll_group_mute:
                handleFunctionState(muteImg, muteTv, R.drawable.icon_mute_selected, R.drawable.icon_mute_unselected, 1);
                break;
            case R.id.ll_mute_direct:
                handleFunctionState(directMuteImg, directMuteTv, R.drawable.icon_direct_mute_selected, R.drawable.icon_direct_mute, 1);
                break;
            case R.id.img_tran_video:
                switchFunctionViewUIState(tranVideoImg, tranVideoTv);
                tranVideoImg.setImageResource(tranVideoImg.isSelected() ? R.drawable.icon_trans_video : R.drawable.icon_trans_video);
                break;
            case R.id.ll_answer_phone_direct:
            case R.id.img_answer_the_phone:
                voiceCommunicationManager.setCommunicationState(COMMUNICATION_STATE_ING);
                if (NetUtils.isNetworkConnected(this)) {
                    int joinState = voiceCommunicationManager.joinChannel(voiceCommunicationManager.getInviteeInfoBean().getToken(),
                            voiceCommunicationManager.getAgoraChannelId(), voiceCommunicationManager.getInviteeInfoBean().getUserId(),
                            voiceCommunicationManager.getInviteeInfoBean().getAgoraUid());
                    if (joinState != 0) {
                        refuseOrLeaveChannel(COMMUNICATION_REFUSE, true);
                    }
                }
                break;
            case R.id.ll_hung_up_direct:
            case R.id.ll_video_hung_up:
            case R.id.img_hung_up:
                VoiceCommunicationToastUtil.showToast(true, BaseApplication.getInstance().getUid());
                if (voiceCommunicationManager.isInviteePre()) {
                    refuseOrLeaveChannel(COMMUNICATION_REFUSE, true);
                } else {
                    refuseOrLeaveChannel(COMMUNICATION_LEAVE, true);
                }
                break;
            case R.id.img_voice_communication_pack_up:
                saveCommunicationData();
                pickUpVoiceCommunication(true);
                break;
            case R.id.ll_video_switch_camera:
                voiceCommunicationManager.switchCamera();
                break;
            case R.id.ll_video_answer_phone:
                setupLocalVideo();
                handleCommunicationViewsState();
                voiceCommunicationManager.joinChannel(voiceCommunicationManager.getInviteeInfoBean().getToken(),
                        agoraChannelId, voiceCommunicationManager.getInviteeInfoBean().getUserId(),
                        voiceCommunicationManager.getInviteeInfoBean().getAgoraUid());
                break;
            case R.id.ll_turn_to_voice:
                voiceCommunicationManager.disableVideo();
                break;
            default:
                break;
        }
    }

    private void handleFunctionState(ImageView imageView, TextView textView, int selected, int unselected, int type) {
        switchFunctionViewUIState(imageView, textView);
        switch (type) {
            case 1:
                voiceCommunicationManager.setMute(imageView.isSelected());
                voiceCommunicationManager.muteLocalAudioStream(imageView.isSelected());
                break;
            case 2:
                voiceCommunicationManager.setHandsFree(imageView.isSelected());
                voiceCommunicationManager.onSwitchSpeakerphoneClicked(imageView.isSelected());
                break;
            default:
                break;
        }
        imageView.setImageResource(imageView.isSelected() ? selected : unselected);
    }

    /**
     * 修改Image选中状态和textView属性
     * @param imageView
     * @param textView
     */
    private void switchFunctionViewUIState(ImageView imageView, TextView textView) {
        imageView.setSelected(!imageView.isSelected());
        textView.setTextColor(imageView.isSelected() ? ContextCompat.getColor(this, R.color.voice_communication_function_select)
                : ContextCompat.getColor(this, R.color.voice_communication_function_default));
    }

    /**
     * 拒绝接听或者挂断电话的处理
     * 主动点击发socket消息，被动触发没发消息
     * @param type
     */
    private void refuseOrLeaveChannel(int type, boolean isMyClick) {
        if (ClickRuleUtil.isFastClick()) {
            return;
        }
        if (mediaPlayerManagerUtils != null) {
            mediaPlayerManagerUtils.stop();
        }
        //主动点击调用handleDestroy，不是主动点击在manager里调用，leave的情况等得到leave回调再发消息
        if (isMyClick) {
            if (type == COMMUNICATION_REFUSE) {
                voiceCommunicationManager.sendCommunicationCommand(Constant.COMMAND_REFUSE);
            }
            voiceCommunicationManager.handleDestroy();
        }
        finish();
    }

    /**
     * 应用进入小窗口状态，出发时机是onPause和用户自己点击小窗口
     */
    private void pickUpVoiceCommunication(boolean isNeedGuid) {
        if (voiceCommunicationManager.getCommunicationState() != COMMUNICATION_STATE_OVER) {
            if (Build.VERSION.SDK_INT >= 23) {
                if (Settings.canDrawOverlays(this)) {
                    checkCanBackGroundStart(isNeedGuid);
                } else {
                    if (isNeedGuid) {
                        showSmallWindowPermissionDialog();
                    } else {
                        finish();
                    }
                }
            } else {
                if (Build.VERSION.SDK_INT >= 19) {
                    checkCanBackGroundStart(isNeedGuid);
                } else {
                    showSmallWindowAndCloseActivity();
                }
            }
        }
    }

    private void showSmallWindowPermissionDialog() {
        new CustomDialog.MessageDialogBuilder(ChannelVoiceCommunicationActivity.this)
                .setMessage(getString(R.string.permission_grant_window_alert, AppUtils.getAppName(ChannelVoiceCommunicationActivity.this)))
                .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Uri uri = Uri.parse("package:" + getPackageName());
                        Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, uri);
                        startActivityForResult(intent, REQUEST_WINDOW_PERMISSION);
                    }
                }).setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        }).show();
    }

    /**
     * 针对小米的判断
     */
    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    private void checkCanBackGroundStart(boolean isNeedGuid) {
        if (AppUtils.canBackgroundStart(this)) {
            showSmallWindowAndCloseActivity();
        } else {
            if (isNeedGuid) {
                new CustomDialog.MessageDialogBuilder(ChannelVoiceCommunicationActivity.this)
                        .setMessage(getString(R.string.permission_grant_background_start, AppUtils.getAppName(ChannelVoiceCommunicationActivity.this)))
                        .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).setData(Uri.parse("package:" + getPackageName()));
                                startActivityForResult(intent, REQUEST_BACKGROUND_WINDOWS);
                            }
                        }).setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                }).show();
            } else {
                finish();
            }
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_WINDOW_PERMISSION) {
            if (Build.VERSION.SDK_INT >= 23 && Settings.canDrawOverlays(this)) {
                checkCanBackGroundStart(true);
            } else if (requestCode == REQUEST_BACKGROUND_WINDOWS) {
                if (AppUtils.canBackgroundStart(this)) {
                    showSmallWindowAndCloseActivity();
                } else {
                    ToastUtils.show(getString(R.string.permission_grant_background_start_fail, AppUtils.getAppName(ChannelVoiceCommunicationActivity.this)));
                }
            } else {
                ToastUtils.show(getString(R.string.permission_grant_window_fail, AppUtils.getAppName(ChannelVoiceCommunicationActivity.this)));
            }
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mediaPlayerManagerUtils != null) {
            mediaPlayerManagerUtils.stop();
        }
        if (voiceCommunicationManager.getCommunicationState() != COMMUNICATION_STATE_OVER) {
            saveCommunicationData();
            pickUpVoiceCommunication(false);
        }
    }

    /**
     * 展示小窗，关闭Activity
     */
    private void showSmallWindowAndCloseActivity() {
        if (voiceCommunicationManager.getCommunicationState() != COMMUNICATION_STATE_OVER) {
            SuspensionWindowManagerUtils.getInstance().showCommunicationSmallWindow(ResolutionUtils.getWidth(this),
                    Long.parseLong(TimeUtils.getChronometerSeconds(communicationTimeChronometer.getText().toString())));
            finish();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (receiver != null) {
            unregisterReceiver(receiver);
        }
    }

    /**
     * 设置邀请者信息
     */
    private void setInviterInfo() {
        if (voiceCommunicationManager.isInviteePre()) {
            VoiceCommunicationJoinChannelInfoBean infoBean = voiceCommunicationManager.getVoiceCommunicationMemberList().get(0);
            ImageDisplayUtils.getInstance().displayImage(userHeadImg, infoBean.getHeadImageUrl(), R.drawable.icon_person_default);
            userNameTv.setText(infoBean.getUserName());
        }
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

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
    }

    @Override
    public void onBackPressed() {
    }

    /**
     * 主叫方创建频道失败
     * @param error
     * @param errorCode
     */
    private void createChannelOrGetChannelInfoFail(String error, int errorCode) {
        voiceCommunicationManager.handleDestroy();
        WebServiceMiddleUtils.hand(ChannelVoiceCommunicationActivity.this, error, errorCode);
        finish();
    }

    class WebService extends APIInterfaceInstance {
        @Override
        public void returnGetVoiceCommunicationResultSuccess(GetVoiceCommunicationResult getVoiceCommunicationResult) {
            agoraChannelId = getVoiceCommunicationResult.getChannelId();
            voiceCommunicationManager.setAgoraChannelId(agoraChannelId);
            VoiceCommunicationJoinChannelInfoBean voiceCommunicationJoinChannelInfoBean = getMyCommunicationInfoBean(getVoiceCommunicationResult);
            if (voiceCommunicationJoinChannelInfoBean != null) {
                voiceCommunicationManager.setEncryptionSecret(agoraChannelId);
                //屏蔽视频通话
//                setupLocalVideo();
                int isJoinChannelSuccess = voiceCommunicationManager.joinChannel(voiceCommunicationJoinChannelInfoBean.getToken(),
                        agoraChannelId, voiceCommunicationJoinChannelInfoBean.getUserId(), voiceCommunicationJoinChannelInfoBean.getAgoraUid());
                voiceCommunicationManager.getVoiceCommunicationMemberList().clear();
                voiceCommunicationManager.getVoiceCommunicationMemberList().addAll(getVoiceCommunicationResult.getVoiceCommunicationJoinChannelInfoBeanList());
                if (isJoinChannelSuccess == 0) {
                    refreshCommunicationMembersAdapterWithState();
                } else {
                    voiceCommunicationManager.destroyResourceAndState();
                    finish();
                }
            } else {
                voiceCommunicationManager.destroyResourceAndState();
                finish();
            }
        }

        @Override
        public void returnGetVoiceCommunicationResultFail(String error, int errorCode) {
            createChannelOrGetChannelInfoFail(error, errorCode);
        }
    }
}
