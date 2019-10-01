package com.inspur.emmcloud.ui.chat;

import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.SystemClock;
import android.provider.Settings;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.MotionEvent;
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
import com.inspur.emmcloud.api.APIUri;
import com.inspur.emmcloud.api.apiservice.ChatAPIService;
import com.inspur.emmcloud.api.apiservice.WSAPIService;
import com.inspur.emmcloud.baselib.util.DensityUtil;
import com.inspur.emmcloud.baselib.util.LogUtils;
import com.inspur.emmcloud.baselib.util.ResolutionUtils;
import com.inspur.emmcloud.baselib.util.StringUtils;
import com.inspur.emmcloud.baselib.util.TimeUtils;
import com.inspur.emmcloud.baselib.util.ToastUtils;
import com.inspur.emmcloud.baselib.widget.CircleTextImageView;
import com.inspur.emmcloud.baselib.widget.dialogs.CustomDialog;
import com.inspur.emmcloud.basemodule.application.BaseApplication;
import com.inspur.emmcloud.basemodule.ui.BaseActivity;
import com.inspur.emmcloud.basemodule.util.AppUtils;
import com.inspur.emmcloud.basemodule.util.ImageDisplayUtils;
import com.inspur.emmcloud.basemodule.util.NetUtils;
import com.inspur.emmcloud.basemodule.util.WebServiceMiddleUtils;
import com.inspur.emmcloud.bean.chat.GetVoiceAndVideoResult;
import com.inspur.emmcloud.bean.chat.GetVoiceCommunicationResult;
import com.inspur.emmcloud.bean.chat.VoiceCommunicationAudioVolumeInfo;
import com.inspur.emmcloud.bean.chat.VoiceCommunicationJoinChannelInfoBean;
import com.inspur.emmcloud.bean.system.GetBoolenResult;
import com.inspur.emmcloud.util.privates.CustomProtocol;
import com.inspur.emmcloud.util.privates.MediaPlayerManagerUtils;
import com.inspur.emmcloud.util.privates.SuspensionWindowManagerUtils;
import com.inspur.emmcloud.util.privates.VoiceCommunicationUtils;
import com.inspur.emmcloud.widget.ECMChatInputMenu;
import com.inspur.emmcloud.widget.ECMSpaceItemDecoration;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.agora.rtc.RtcEngine;
import io.agora.rtc.video.VideoCanvas;

/**
 * Created by yufuchang on 2018/8/14.
 */
public class ChannelVoiceCommunicationActivity extends BaseActivity {
    public static final String VOICE_VIDEO_CALL_AGORA_ID = "channelId";
    public static final String VOICE_VIDEO_CALL_TYPE = "voice_video_call_type";//通话类型
    public static final String VOICE_VIDEO_UID = "voice_video_UID";
    public static final String VOICE_COMMUNICATION_STATE = "voice_communication_state";//传递页面布局样式的
    public static final String VOICE_TIME = "voice_time";
    public static final String SCREEN_SIZE = "screen_size";
    public static final int INVITER_LAYOUT_STATE = 0;//邀请人状态布局
    public static final int INVITEE_LAYOUT_STATE = 1;//被邀请人状态布局
    public static final int COMMUNICATION_LAYOUT_STATE = 2;//通话中布局状态
    public static final int COME_BACK_FROM_SERVICE = 3;//预留从小窗口回到聊天页面的状态
    private static final int EXCEPTION_STATE = -1;
    private static int STATE = -1;
    private static final int REQUEST_WINDOW_PERMISSION = 100;//请求悬浮窗权限
    @BindView(R.id.ll_voice_communication_invite)
    LinearLayout inviteeLinearLayout;
    @BindView(R.id.img_user_head)
    CircleTextImageView userHeadImg;
    @BindView(R.id.tv_user_name)
    TextView userNameTv;
    @BindView(R.id.ll_voice_communication_invite_members)
    LinearLayout inviteMemebersGroupLinearLayout;
    @BindView(R.id.recyclerview_voice_communication_first)
    RecyclerView firstRecyclerview;
    @BindView(R.id.recyclerview_voice_communication_second)
    RecyclerView secondRecyclerview;
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
    @BindView(R.id.img_an_excuse)
    ImageView excuseImg;
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
    private ChatAPIService apiService;
    private List<VoiceCommunicationJoinChannelInfoBean> voiceCommunicationUserInfoBeanList = new ArrayList<>();
    private String agoraChannelId = "";//声网的channelId
    private String cloudPlusChannelId = "";//云+的Id
    private String communicationType = "";//会话类型 VOICE_CALL或者VIDEO_CALL
    private List<VoiceCommunicationJoinChannelInfoBean> voiceCommunicationMemberList = new ArrayList<>();
    private VoiceCommunicationJoinChannelInfoBean inviteeInfoBean;
    private int userCount = 1;
    private VoiceCommunicationMemberAdapter voiceCommunicationMemberAdapterFirst;
    private VoiceCommunicationMemberAdapter voiceCommunicationMemberAdapterSecond;
    private MediaPlayerManagerUtils mediaPlayerManagerUtils;
    private VoiceCommunicationUtils voiceCommunicationUtils;
    private SurfaceView agoraLocalView;//视频会话小视图
    private SurfaceView agoraRemoteView;//视频会话大视图

    private int initx;//本地视频初始x坐标
    private int inity;//本地视频初始y坐标


    @Override
    public void onCreate() {
        EventBus.getDefault().register(this);
        ButterKnife.bind(this);
        cloudPlusChannelId = getIntent().getStringExtra(ConversationActivity.CLOUD_PLUS_CHANNEL_ID);
        communicationType = getIntent().getStringExtra(VOICE_VIDEO_CALL_TYPE);
        voiceCommunicationUserInfoBeanList = (List<VoiceCommunicationJoinChannelInfoBean>) getIntent().getSerializableExtra("userList");
        voiceCommunicationUtils = VoiceCommunicationUtils.getVoiceCommunicationUtils(this, communicationType);
        recoverData();
        initViews();
    }

    @Override
    public int getLayoutResId() {
        return R.layout.activity_voice_channel;
    }

    /**
     * 如果是从小窗口来的，则恢复通话数据
     */
    private void recoverData() {
        STATE = getIntent().getIntExtra(VOICE_COMMUNICATION_STATE, EXCEPTION_STATE);
        if (STATE == COME_BACK_FROM_SERVICE) {
            STATE = voiceCommunicationUtils.getState();
            voiceCommunicationUserInfoBeanList = voiceCommunicationUtils.getVoiceCommunicationUserInfoBeanList();
            agoraChannelId = voiceCommunicationUtils.getChannelId();
            communicationType = voiceCommunicationUtils.getCommunicationType();
            voiceCommunicationMemberList = voiceCommunicationUtils.getVoiceCommunicationMemberList();
            inviteeInfoBean = voiceCommunicationUtils.getInviteeInfoBean();
            userCount = voiceCommunicationUtils.getUserCount();
        }
    }

    /**
     * 初始化Views
     */
    private void initViews() {
        if (communicationType.equals(ECMChatInputMenu.VIDEO_CALL)) {
            videoCallLayout.setVisibility(View.VISIBLE);
            voiceCallLayout.setVisibility(View.GONE);
        } else if (communicationType.equals(ECMChatInputMenu.VOICE_CALL)) {
            voiceCallLayout.setVisibility(View.VISIBLE);
            videoCallLayout.setVisibility(View.GONE);
        }
        apiService = new ChatAPIService(this);
        apiService.setAPIInterface(new WebService());
        voiceCommunicationMemberAdapterFirst = new VoiceCommunicationMemberAdapter(this, voiceCommunicationUserInfoBeanList, 0);
        voiceCommunicationMemberAdapterSecond = new VoiceCommunicationMemberAdapter(this, voiceCommunicationUserInfoBeanList, 0);
        initCallbacks();
        mediaPlayerManagerUtils = MediaPlayerManagerUtils.getManager();
        mediaPlayerManagerUtils.setMediaPlayerLooping(true);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
        firstRecyclerview.setLayoutManager(layoutManager);
        firstRecyclerview.addItemDecoration(new ECMSpaceItemDecoration(DensityUtil.dip2px(this, 8)));
        firstRecyclerview.setAdapter(voiceCommunicationMemberAdapterFirst);
        if (voiceCommunicationMemberList != null && voiceCommunicationMemberList.size() > 5) {
            LinearLayoutManager layoutManager2 = new LinearLayoutManager(this);
            layoutManager2.setOrientation(LinearLayoutManager.HORIZONTAL);
            secondRecyclerview.setLayoutManager(layoutManager2);
            secondRecyclerview.addItemDecoration(new ECMSpaceItemDecoration(DensityUtil.dip2px(this, 8)));
            secondRecyclerview.setAdapter(voiceCommunicationMemberAdapterSecond);

            LinearLayoutManager layoutManagerMembersSecond = new LinearLayoutManager(this);
            layoutManagerMembersSecond.setOrientation(LinearLayoutManager.HORIZONTAL);
            communicationMemberSecondRecyclerview.addItemDecoration(new ECMSpaceItemDecoration(DensityUtil.dip2px(this, 8)));
            communicationMemberSecondRecyclerview.setLayoutManager(layoutManagerMembersSecond);
        }
        LinearLayoutManager layoutManagerMemebersFirst = new LinearLayoutManager(this);
        layoutManagerMemebersFirst.setOrientation(LinearLayoutManager.HORIZONTAL);
        communicationMembersFirstRecyclerview.addItemDecoration(new ECMSpaceItemDecoration(DensityUtil.dip2px(this, 8)));
        communicationMembersFirstRecyclerview.setLayoutManager(layoutManagerMemebersFirst);

        initCommunicationViewsAndMusicByState(STATE);
        initFunctionState();
        switch (STATE) {
            case INVITER_LAYOUT_STATE:
                refreshCommunicationMemberAdapter();
                createChannel();
                break;
            case INVITEE_LAYOUT_STATE:
                String agoraChannelId = getIntent().getStringExtra(VOICE_VIDEO_CALL_AGORA_ID);
                voiceCommunicationUtils.setEncryptionSecret(agoraChannelId);
                getChannelInfoByChannelId(agoraChannelId);
                break;
        }
        if (getIntent().getLongExtra(VOICE_TIME, 0) > 0) {
            communicationTimeChronometer.setBase(SystemClock.elapsedRealtime() - getIntent().getLongExtra(VOICE_TIME, 0) * 1000);
            communicationTimeChronometer.start();
        }
//        dragLocalVideoView();
    }

    /**
     * 本地视频窗口拖动功能，目前还有问题
     */
    private void dragLocalVideoView() {
        if (communicationType.equals(ECMChatInputMenu.VIDEO_CALL)) {
            localVideoContainer.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    switch (event.getAction()) {
                        case MotionEvent.ACTION_DOWN:
                            initx = (int) event.getRawX();
                            inity = (int) event.getRawY();
                            break;
                        case MotionEvent.ACTION_MOVE:
                            int dx = (int) event.getRawX() - initx;
                            int dy = (int) event.getRawY() - inity;

                            RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) v.getLayoutParams();

                            int l = layoutParams.leftMargin + dx;
                            int t = layoutParams.topMargin + dy;
                            int b = rootLayout.getHeight() - t - v.getHeight();
                            int r = rootLayout.getWidth() - l - v.getWidth();
                            if (l < 0) {//处理布局被移动到上下左右四个边缘时的情况，决定着按钮不会被移动到屏幕外边去
                                l = 0;
                                r = rootLayout.getWidth() - v.getWidth();
                            }
                            if (t < 0) {
                                t = 0;
                                b = rootLayout.getHeight() - v.getHeight();
                            }

                            if (r < 0) {
                                r = 0;
                                l = rootLayout.getWidth() - v.getWidth();
                            }
                            if (b < 0) {
                                b = 0;
                                t = rootLayout.getHeight() - v.getHeight();
                            }
                            layoutParams.leftMargin = l;
                            layoutParams.topMargin = t;
                            layoutParams.bottomMargin = b;
                            layoutParams.rightMargin = r;
                            layoutParams.width = DensityUtil.dip2px(ChannelVoiceCommunicationActivity.this, 94);
                            layoutParams.height = DensityUtil.dip2px(ChannelVoiceCommunicationActivity.this, 167);
                            LogUtils.YfcDebug("设置参数");
                            v.setLayoutParams(layoutParams);
                            initx = (int) event.getRawX();
                            inity = (int) event.getRawY();
                            v.postInvalidate();
                            break;
                        case MotionEvent.ACTION_CANCEL:
                            break;
                        default:
                            break;
                    }
                    return true;
                }
            });
        }
    }

    /**
     * 初始化功能模块的初始值
     */
    private void initFunctionState() {
        voiceCommunicationUtils.muteLocalAudioStream(false);
        voiceCommunicationUtils.muteAllRemoteAudioStreams(false);
        voiceCommunicationUtils.onSwitchSpeakerphoneClicked(false);
    }

    /**
     * 通过agoraChannelId获取Channel信息
     *
     * @param agoraChannelId
     */
    private void getChannelInfoByChannelId(String agoraChannelId) {
        if (NetUtils.isNetworkConnected(this)) {
            apiService.getAgoraChannelInfo(agoraChannelId);
        }
    }

    /**
     * 创建频道
     */
    private void createChannel() {
        if (NetUtils.isNetworkConnected(this)) {
            try {
                JSONArray jsonArray = new JSONArray();
                for (int i = 0; i < voiceCommunicationUserInfoBeanList.size(); i++) {
                    JSONObject jsonObjectUserInfo = new JSONObject();
                    jsonObjectUserInfo.put("id", voiceCommunicationUserInfoBeanList.get(i).getUserId());
                    jsonObjectUserInfo.put("name", voiceCommunicationUserInfoBeanList.get(i).getUserName());
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
     *
     * @param state
     */
    private void initCommunicationViewsAndMusicByState(int state) {
        if (state == EXCEPTION_STATE) {
            finish();
        }
        STATE = state;
        changeFunctionState(state);
        if (communicationType.equals(ECMChatInputMenu.VOICE_CALL) && (state == COMMUNICATION_LAYOUT_STATE
                || state == INVITEE_LAYOUT_STATE || state == INVITER_LAYOUT_STATE)) {
            if (voiceCommunicationMemberList == null) {
                return;
            }
            refreshCommunicationMemberAdapter();
        }
    }

    /**
     * 修改三个功能键的UI状态
     *
     * @param state
     */
    private void changeFunctionState(int state) {
        if (communicationType.equals(ECMChatInputMenu.VOICE_CALL)) {
            voiceCommunicationUtils.adjustPlaybackSignalVolume(100);
            inviteeLinearLayout.setVisibility(state == INVITEE_LAYOUT_STATE ? View.VISIBLE : View.GONE);
            inviteMemebersGroupLinearLayout.setVisibility((state == INVITER_LAYOUT_STATE || state == COMMUNICATION_LAYOUT_STATE) ? View.VISIBLE : View.GONE);
            communicationMembersLinearLayout.setVisibility(state == INVITEE_LAYOUT_STATE ? View.VISIBLE : View.GONE);
            functionLinearLayout.setVisibility((state == INVITER_LAYOUT_STATE || state == COMMUNICATION_LAYOUT_STATE) ? View.VISIBLE : View.GONE);
            communicationStateTv.setVisibility((state == INVITER_LAYOUT_STATE || state == COMMUNICATION_LAYOUT_STATE) ? View.VISIBLE : View.GONE);
            communicationTimeChronometer.setVisibility(state == COMMUNICATION_LAYOUT_STATE ? View.VISIBLE : View.GONE);

            //启用悬浮窗打开这里
            packUpImg.setVisibility(state == COMMUNICATION_LAYOUT_STATE ? View.VISIBLE : View.GONE);
            communicationStateTv.setText(state == INVITER_LAYOUT_STATE ? getString(R.string.voice_communication_dialog) :
                    (state == INVITEE_LAYOUT_STATE ? getString(R.string.voice_communication_waitting_answer) :
                            (state == COMMUNICATION_LAYOUT_STATE ? getString(R.string.voice_communicaiton_watting_talking) : "")));
            answerPhoneImg.setVisibility((state == INVITEE_LAYOUT_STATE) ? View.VISIBLE : View.GONE);
            int colorNormal = ContextCompat.getColor(this, R.color.voice_communication_function_default);
            int colorUnavailiable = ContextCompat.getColor(this, R.color.voice_communication_function_unavailiable_text);
            excuseImg.setImageResource(state == COMMUNICATION_LAYOUT_STATE ? R.drawable.icon_excuse_unselected : R.drawable.icon_excuse_unavailable);
            excuseTv.setTextColor(state == COMMUNICATION_LAYOUT_STATE ? colorNormal : colorUnavailiable);
            excuseImg.setClickable(state == COMMUNICATION_LAYOUT_STATE);

            handsFreeImg.setImageResource(state == COMMUNICATION_LAYOUT_STATE ? R.drawable.icon_hands_free_unselected : R.drawable.icon_hands_free_unavailable);
            handsFreeTv.setTextColor(state == COMMUNICATION_LAYOUT_STATE ? colorNormal : colorUnavailiable);
            handsFreeImg.setClickable(state == COMMUNICATION_LAYOUT_STATE);

            muteImg.setImageResource(state == COMMUNICATION_LAYOUT_STATE ? R.drawable.icon_mute_unselcected : R.drawable.icon_mute_unavaiable);
            muteTv.setTextColor(state == COMMUNICATION_LAYOUT_STATE ? colorNormal : colorUnavailiable);
            muteImg.setClickable(state == COMMUNICATION_LAYOUT_STATE);

            //如果是通话中则“通话中”文字显示一下就不再显示
            communicationStateTv.setText(state == COMMUNICATION_LAYOUT_STATE ? "" : communicationStateTv.getText());
            if (state == INVITER_LAYOUT_STATE || state == INVITEE_LAYOUT_STATE) {
                mediaPlayerManagerUtils.play(R.raw.voice_communication_watting_answer, null);
            } else {
                mediaPlayerManagerUtils.stop();
            }
        } else if (communicationType.equals(ECMChatInputMenu.VIDEO_CALL)) {
            voiceCommunicationUtils.adjustPlaybackSignalVolume(400);
            voiceCommunicationUtils.setEnableSpeakerphone(true);
            turnToVoiceLayout.setVisibility(state == COMMUNICATION_LAYOUT_STATE ? View.VISIBLE : View.GONE);
            videoHungUp.setVisibility((state == COMMUNICATION_LAYOUT_STATE || state == INVITER_LAYOUT_STATE || state == INVITEE_LAYOUT_STATE) ? View.VISIBLE : View.GONE);
            answerVideoPhoneLayout.setVisibility(state == INVITEE_LAYOUT_STATE ? View.VISIBLE : View.GONE);
            switchCameraLayout.setVisibility(state == COMMUNICATION_LAYOUT_STATE ? View.VISIBLE : View.GONE);
//            videoPackUpImg.setVisibility(state == COMMUNICATION_LAYOUT_STATE ? View.VISIBLE : View.GONE);
            videoPackUpImg.setVisibility(View.GONE);
            personInfoLayout.setVisibility((state == INVITER_LAYOUT_STATE || state == INVITEE_LAYOUT_STATE) ? View.VISIBLE : View.GONE);
            if (state == COMMUNICATION_LAYOUT_STATE) {
                localVideoContainer.getLayoutParams().height = DensityUtil.dip2px(this, 167);
                localVideoContainer.getLayoutParams().width = DensityUtil.dip2px(this, 94);
            }
//            dragLocalVideoView();
        }
    }

    @Override
    protected int getStatusType() {
        return super.getStatusType();
    }


    //通话过程中状态变化，这里处理destroy和refuse，在CommunicationFrament上处理invite，因为invite时还未打开此页面
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onReceiveVoiceOrVideoCall(final GetVoiceAndVideoResult getVoiceAndVideoResult) {
        CustomProtocol customProtocol = new CustomProtocol(getVoiceAndVideoResult.getContextParamsSchema());
        String cmd = customProtocol.getParamMap().get("cmd");
        if (!StringUtils.isBlank(cmd) && cmd.equals("destroy")) {
            changeUserConnectStateByUid(VoiceCommunicationJoinChannelInfoBean.CONNECT_STATE_LEAVE,
                    customProtocol.getParamMap().get("uid"));
            leaveChannelSuccess(agoraChannelId);
            finish();
        } else if (!StringUtils.isBlank(cmd) && cmd.equals("refuse")) {
            changeUserConnectStateByUid(VoiceCommunicationJoinChannelInfoBean.CONNECT_STATE_REFUSE,
                    customProtocol.getParamMap().get("uid"));
            checkCommunicationFinish();
        }
    }

    /**
     * 检查是否需要退出
     */
    private void checkCommunicationFinish() {
        if (voiceCommunicationMemberList != null && voiceCommunicationMemberList.size() > 0) {
            int waitAndCommunicationSize = 0;
            for (int i = 0; i < voiceCommunicationMemberList.size(); i++) {
                if (voiceCommunicationMemberList.get(i).getConnectState() == VoiceCommunicationJoinChannelInfoBean.CONNECT_STATE_CONNECTED ||
                        voiceCommunicationMemberList.get(i).getConnectState() == VoiceCommunicationJoinChannelInfoBean.CONNECT_STATE_INIT) {
                    waitAndCommunicationSize = waitAndCommunicationSize + 1;
                }
            }
            if (waitAndCommunicationSize < 2) {
                leaveChannelSuccess(agoraChannelId);
            }
        }
    }

    /**
     * 初始化回调
     */
    private void initCallbacks() {
        voiceCommunicationUtils.setOnVoiceCommunicationCallbacks(new OnVoiceCommunicationCallbacksImpl() {
            @Override
            public void onUserOffline(int uid, int reason) {
                userCount = userCount - 1;
                //通过声网判断退出的方法
//                if (userCount < 2) {
//                    leaveChannelSuccess(agoraChannelId);
//                }
            }

            @Override
            public void onUserJoined(int uid, int elapsed) {
                for (int i = 0; i < voiceCommunicationMemberList.size(); i++) {
                    if (voiceCommunicationMemberList.get(i).getAgoraUid() == uid) {
                        voiceCommunicationMemberList.get(i).setUserState(1);
                    }
                }
                userCount = userCount + 1;
                if (userCount >= 2) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            STATE = COMMUNICATION_LAYOUT_STATE;
                            changeFunctionState(COMMUNICATION_LAYOUT_STATE);
                            communicationTimeChronometer.setBase(SystemClock.elapsedRealtime());
                            communicationTimeChronometer.start();
                            refreshCommunicationMemberAdapter();
                        }
                    });
                }
            }

            @Override
            public void onJoinChannelSuccess(String channel, int uid, int elapsed) {
                changeUserConnectStateByAgoraUid(VoiceCommunicationJoinChannelInfoBean.CONNECT_STATE_CONNECTED, uid);
                joinChannelSuccess(channel);
            }

            @Override
            public void onRejoinChannelSuccess(String channel, int uid, int elapsed) {
                changeUserConnectStateByAgoraUid(VoiceCommunicationJoinChannelInfoBean.CONNECT_STATE_CONNECTED, uid);
                userCount = userCount + 1;
                joinChannelSuccess(channel);
            }

            @Override
            public void onUserMuteAudio(int uid, boolean muted) {

            }

            @Override
            public void onError(int err) {

            }

            @Override
            public void onConnectionLost() {
            }

            @Override
            public void onNetworkQuality(int uid, int txQuality, int rxQuality) {
                if (STATE == COMMUNICATION_LAYOUT_STATE) {
                    communicationStateTv.setText((uid == 0 && txQuality <= 2) ? getString(R.string.voice_communication_quality) : "");
                }
            }

            @Override
            public void onAudioVolumeIndication(final VoiceCommunicationAudioVolumeInfo[] speakers, final int totalVolume) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (speakers != null && speakers.length > 0) {
                            for (int i = 0; i < speakers.length; i++) {
                                int agoraId = speakers[i].uid;
                                for (int j = 0; j < voiceCommunicationMemberList.size(); j++) {
                                    if (voiceCommunicationMemberList.get(j).getAgoraUid() == agoraId) {
                                        voiceCommunicationMemberList.get(j).setVolume(speakers[i].volume);
                                    }
                                }
                            }
                            if (totalVolume == 0) {
                                for (int i = 0; i < voiceCommunicationMemberList.size(); i++) {
                                    voiceCommunicationMemberList.get(i).setVolume(0);
                                }
                            }
                            refreshCommunicationMemberAdapter();
                        }
                    }
                });
            }


            @Override
            public void onRemoteVideoStateChanged(final int uid, int state, int reason, int elapsed) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        setupRemoteVideo(uid);
                    }
                });
            }
        });
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

    /**
     * 修改用户的链接状态
     * 通过云+uid
     *
     * @param connectStateConnected
     */
    private void changeUserConnectStateByUid(int connectStateConnected, String uid) {
        if (voiceCommunicationMemberList != null && voiceCommunicationMemberList.size() > 0) {
            for (int i = 0; i < voiceCommunicationMemberList.size(); i++) {
                if (voiceCommunicationMemberList.get(i).getUserId().equals(uid)) {
                    voiceCommunicationMemberList.get(i).setConnectState(connectStateConnected);
                    break;
                }
            }
        }
    }

    /**
     * 设置本地视频显示
     */
    private void setupLocalVideo() {
        if (!communicationType.equals(ECMChatInputMenu.VIDEO_CALL)) {
            return;
        }
        // This is used to set a local preview.
        // The steps setting local and remote view are very similar.
        // But note that if the local user do not have a uid or do
        // not care what the uid is, he can set his uid as ZERO.
        // Our server will assign one and return the uid via the event
        // handler callback function (onJoinChannelSuccess) after
        // joining the channel successfully.
        LogUtils.YfcDebug("设置本地视频");
        agoraLocalView = RtcEngine.CreateRendererView(getBaseContext());
        agoraLocalView.setZOrderMediaOverlay(true);
        if (STATE == INVITER_LAYOUT_STATE) {
            localVideoContainer.getLayoutParams().height = FrameLayout.LayoutParams.MATCH_PARENT;
            localVideoContainer.getLayoutParams().width = FrameLayout.LayoutParams.MATCH_PARENT;
        }
        localVideoContainer.addView(agoraLocalView);
        voiceCommunicationUtils.getRtcEngine().setupLocalVideo(new VideoCanvas(agoraLocalView, VideoCanvas.RENDER_MODE_HIDDEN, 0));
    }

    private void setupRemoteVideo(int uid) {
        // Only one remote video view is available for this
        // tutorial. Here we check if there exists a surface
        // view tagged as this uid.
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
        LogUtils.YfcDebug("设置远程视图");
        voiceCommunicationUtils.getRtcEngine().setupRemoteVideo(new VideoCanvas(agoraRemoteView, VideoCanvas.RENDER_MODE_HIDDEN, uid));
        agoraRemoteView.setTag(uid);
    }

    /**
     * 刷新成员adapter
     */
    private void refreshCommunicationMemberAdapter() {
        if (voiceCommunicationMemberList != null) {
            if (voiceCommunicationMemberList.size() <= 5) {
                voiceCommunicationMemberAdapterFirst.setMemberDataAndRefresh(voiceCommunicationMemberList, 1);
            } else if (voiceCommunicationMemberList.size() <= 9) {
                List<VoiceCommunicationJoinChannelInfoBean> list1 = voiceCommunicationMemberList.subList(0, 5);
                List<VoiceCommunicationJoinChannelInfoBean> list2 = voiceCommunicationMemberList.subList(5, voiceCommunicationMemberList.size());
                voiceCommunicationMemberAdapterFirst.setMemberDataAndRefresh(list1, 1);
                voiceCommunicationMemberAdapterSecond.setMemberDataAndRefresh(list2, 2);
            }
        }
    }

    /**
     * 加入频道
     *
     * @param channel
     */
    private void joinChannelSuccess(String channel) {
        if (NetUtils.isNetworkConnected(this)) {
            apiService.remindServerJoinChannelSuccess(channel);
        }
    }

    /**
     * 用户离开
     *
     * @param agoraChannelId
     */
    private void leaveChannelSuccess(String agoraChannelId) {
        if (NetUtils.isNetworkConnected(this)) {
            apiService.leaveAgoraChannel(agoraChannelId);
        }
    }

    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.img_an_excuse:
                switchFunctionViewUIState(excuseImg, excuseTv);
                voiceCommunicationUtils.muteLocalAudioStream(excuseImg.isSelected());
                excuseImg.setImageResource(excuseImg.isSelected() ? R.drawable.icon_excuse_selected : R.drawable.icon_excuse_unselected);
                break;
            case R.id.img_hands_free:
                switchFunctionViewUIState(handsFreeImg, handsFreeTv);
                voiceCommunicationUtils.onSwitchSpeakerphoneClicked(handsFreeImg.isSelected());
                handsFreeImg.setImageResource(handsFreeImg.isSelected() ? R.drawable.icon_hands_free_selected : R.drawable.icon_hands_free_unselected);
                break;
            case R.id.img_mute:
                switchFunctionViewUIState(muteImg, muteTv);
                voiceCommunicationUtils.muteAllRemoteAudioStreams(muteImg.isSelected());
                muteImg.setImageResource(muteImg.isSelected() ? R.drawable.icon_mute_selected : R.drawable.icon_mute_unselcected);
                break;
            case R.id.img_tran_video:
                switchFunctionViewUIState(tranVideoImg, tranVideoTv);
                tranVideoImg.setImageResource(tranVideoImg.isSelected() ? R.drawable.icon_trans_video : R.drawable.icon_trans_video);
                break;
            case R.id.img_answer_the_phone:
                initCommunicationViewsAndMusicByState(COMMUNICATION_LAYOUT_STATE);
                communicationTimeChronometer.setBase(SystemClock.elapsedRealtime());
                communicationTimeChronometer.start();
                voiceCommunicationUtils.joinChannel(inviteeInfoBean.getToken(),
                        agoraChannelId, inviteeInfoBean.getUserId(), inviteeInfoBean.getAgoraUid());
                break;
            case R.id.ll_video_hung_up:
            case R.id.img_hung_up:
                //先通知S，后退出声网
                if (answerPhoneImg.getVisibility() == View.VISIBLE) {
                    apiService.refuseAgoraChannel(agoraChannelId);
                } else {
                    apiService.leaveAgoraChannel(agoraChannelId);
                }
                break;
            case R.id.img_voice_communication_pack_up:
                saveCommunicationData();
                if (Build.VERSION.SDK_INT >= 23) {
                    if (Settings.canDrawOverlays(this)) {
                        SuspensionWindowManagerUtils.getInstance().showCommunicationSmallWindow(this, ResolutionUtils.getWidth(this),
                                Long.parseLong(TimeUtils.getChronometerSeconds(communicationTimeChronometer.getText().toString())));
                        finish();
                    } else {
                        new CustomDialog.MessageDialogBuilder(ChannelVoiceCommunicationActivity.this)
                                .setMessage(getString(R.string.permission_grant_window_alert, AppUtils.getAppName(ChannelVoiceCommunicationActivity.this)))
                                .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        Uri uri = Uri.parse("package:" + getPackageName());
                                        Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, uri);
                                        startActivityForResult(intent, REQUEST_WINDOW_PERMISSION);
                                    }
                                })
                                .show();
                    }
                }
                break;
            case R.id.ll_video_switch_camera:
                LogUtils.YfcDebug("转换摄像头");
                voiceCommunicationUtils.switchCamera();
                break;
            case R.id.ll_video_answer_phone:
                LogUtils.YfcDebug("接听视频电话");
                setupLocalVideo();
                initCommunicationViewsAndMusicByState(COMMUNICATION_LAYOUT_STATE);
                voiceCommunicationUtils.joinChannel(inviteeInfoBean.getToken(),
                        agoraChannelId, inviteeInfoBean.getUserId(), inviteeInfoBean.getAgoraUid());
                break;
            case R.id.ll_turn_to_voice:
                voiceCommunicationUtils.disableVideo();
                break;
            default:
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_WINDOW_PERMISSION) {
            if (Build.VERSION.SDK_INT >= 23 && Settings.canDrawOverlays(this)) {
                SuspensionWindowManagerUtils.getInstance().showCommunicationSmallWindow(this, ResolutionUtils.getWidth(this),
                        Long.parseLong(TimeUtils.getChronometerSeconds(communicationTimeChronometer.getText().toString())));
                finish();
            } else {
                ToastUtils.show(getString(R.string.permission_grant_window_fail, AppUtils.getAppName(ChannelVoiceCommunicationActivity.this)));
            }
        }
    }

    /**
     * 保存状态
     */
    private void saveCommunicationData() {
        voiceCommunicationUtils.setState(STATE);
        voiceCommunicationUtils.setVoiceCommunicationUserInfoBeanList(voiceCommunicationUserInfoBeanList);
        voiceCommunicationUtils.setChannelId(agoraChannelId);
        voiceCommunicationUtils.setCommunicationType(communicationType);
        voiceCommunicationUtils.setVoiceCommunicationMemberList(voiceCommunicationMemberList);
        voiceCommunicationUtils.setInviteeInfoBean(inviteeInfoBean);
        voiceCommunicationUtils.setUserCount(userCount);
    }

    /**
     * 修改Image选中状态和textView属性
     *
     * @param imageView
     * @param textView
     */
    private void switchFunctionViewUIState(ImageView imageView, TextView textView) {
        imageView.setSelected(imageView.isSelected() ? false : true);
        textView.setTextColor(imageView.isSelected() ? ContextCompat.getColor(this, R.color.voice_communication_function_select)
                : ContextCompat.getColor(this, R.color.voice_communication_function_default));
    }

    @Override
    public void onBackPressed() {
        //先通知S，后退出声网
        apiService.leaveAgoraChannel(agoraChannelId);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
        mediaPlayerManagerUtils.stop();
    }

    /**
     * 设置邀请者信息
     *
     * @param getVoiceCommunicationResult
     */
    private void setInviterInfo(GetVoiceCommunicationResult getVoiceCommunicationResult) {
        VoiceCommunicationJoinChannelInfoBean infoBean = getVoiceCommunicationResult.getVoiceCommunicationJoinChannelInfoBeanList().get(0);
        //头像源数据修改为本地，注释掉的是从接口中读取的url
//        ImageDisplayUtils.getInstance().displayImage(userHeadImg, infoBean.getHeadImageUrl(), R.drawable.icon_person_default);
        ImageDisplayUtils.getInstance().displayImage(userHeadImg, APIUri.getUserIconUrl(this, infoBean.getUserId()), R.drawable.icon_person_default);
        userNameTv.setText(infoBean.getUserName());
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

    /**
     * 获取通信类型
     *
     * @return
     */
    private String getCommunicationType() {
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
     * @return
     */
    private String getSchema(String cmd, String channelId, String roomId) {
        return "ecc-cloudplus-cmd://voice_channel?cmd=" + cmd + "&channelid=" + channelId + "&roomid=" + roomId + "&uid=" + BaseApplication.getInstance().getUid();
    }

    /**
     * 获取Uid
     *
     * @return
     */
    private JSONArray getUidArray(List<VoiceCommunicationJoinChannelInfoBean> voiceCommunicationUserInfoBeanList){
        JSONArray jsonArray = new JSONArray();
        for (int i = 0; i < voiceCommunicationUserInfoBeanList.size(); i++) {
            jsonArray.put(voiceCommunicationUserInfoBeanList.get(i).getUserId());
        }
        return jsonArray;
    }

    /**
     * 向socket发送指令消息
     *
     * @param commandType
     */
    private void sendCommunicationCommand(String commandType) {
        WSAPIService.getInstance().sendStartVoiceAndVideoCallMessage(cloudPlusChannelId, agoraChannelId,
                getSchema(commandType, cloudPlusChannelId, agoraChannelId), getCommunicationType(), getUidArray(voiceCommunicationMemberList));
    }

    /**
     * 拒绝之后的后续逻辑处理
     */
    private void afterRefuse() {
        voiceCommunicationUtils.destroy();
        sendCommunicationCommand("refuse");
        finish();
    }

    /**
     * 销毁之后的逻辑处理
     */
    private void afterDestory() {
        voiceCommunicationUtils.destroy();
        if (STATE == COMMUNICATION_LAYOUT_STATE) {
            sendCommunicationCommand("refuse");
        }
        if (userCount < 2) {
            sendCommunicationCommand("destroy");
        }
        finish();
    }

    class WebService extends APIInterfaceInstance {
        @Override
        public void returnGetVoiceCommunicationResultSuccess(GetVoiceCommunicationResult getVoiceCommunicationResult) {
            agoraChannelId = getVoiceCommunicationResult.getChannelId();
            VoiceCommunicationJoinChannelInfoBean voiceCommunicationJoinChannelInfoBean = getMyCommunicationInfoBean(getVoiceCommunicationResult);
            if (voiceCommunicationJoinChannelInfoBean != null) {
                voiceCommunicationUtils.setEncryptionSecret(agoraChannelId);
                //屏蔽视频通话
//                setupLocalVideo();
                voiceCommunicationUtils.joinChannel(voiceCommunicationJoinChannelInfoBean.getToken(),
                        getVoiceCommunicationResult.getChannelId(), voiceCommunicationJoinChannelInfoBean.getUserId(), voiceCommunicationJoinChannelInfoBean.getAgoraUid());
            }
            if (getIntent().getIntExtra(VOICE_COMMUNICATION_STATE, EXCEPTION_STATE) != COME_BACK_FROM_SERVICE) {
                voiceCommunicationMemberList.addAll(getVoiceCommunicationResult.getVoiceCommunicationJoinChannelInfoBeanList());
                sendCommunicationCommand("invite");
                refreshCommunicationMemberAdapter();
            }
        }

        @Override
        public void returnGetVoiceCommunicationResultFail(String error, int errorCode) {
            WebServiceMiddleUtils.hand(ChannelVoiceCommunicationActivity.this, error, errorCode);
        }

        @Override
        public void returnGetVoiceCommunicationChannelInfoSuccess(GetVoiceCommunicationResult getVoiceCommunicationResult) {
            agoraChannelId = getVoiceCommunicationResult.getChannelId();
            setInviterInfo(getVoiceCommunicationResult);
            if (getIntent().getIntExtra(VOICE_COMMUNICATION_STATE, EXCEPTION_STATE) != COME_BACK_FROM_SERVICE) {
                voiceCommunicationMemberList.addAll(getVoiceCommunicationResult.getVoiceCommunicationJoinChannelInfoBeanList());
                refreshCommunicationMemberAdapter();
            }
            for (int i = 0; i < getVoiceCommunicationResult.getVoiceCommunicationJoinChannelInfoBeanList().size(); i++) {
                VoiceCommunicationJoinChannelInfoBean voiceCommunicationJoinChannelInfoBean = getVoiceCommunicationResult.getVoiceCommunicationJoinChannelInfoBeanList().get(i);
                if (voiceCommunicationJoinChannelInfoBean.getUserId().equals(MyApplication.getInstance().getUid())) {
                    inviteeInfoBean = voiceCommunicationJoinChannelInfoBean;
                }
            }
            if (voiceCommunicationMemberList.size() <= 5) {
                communicationMembersFirstRecyclerview.setAdapter(new VoiceCommunicationMemberAdapter(ChannelVoiceCommunicationActivity.this, voiceCommunicationMemberList, 3));
            } else if (voiceCommunicationMemberList.size() <= 9) {
                List<VoiceCommunicationJoinChannelInfoBean> list1 = voiceCommunicationMemberList.subList(0, 5);
                List<VoiceCommunicationJoinChannelInfoBean> list2 = voiceCommunicationMemberList.subList(5, voiceCommunicationMemberList.size());
                communicationMembersFirstRecyclerview.setAdapter(new VoiceCommunicationMemberAdapter(ChannelVoiceCommunicationActivity.this, list1, 3));
                communicationMemberSecondRecyclerview.setAdapter(new VoiceCommunicationMemberAdapter(ChannelVoiceCommunicationActivity.this, list2, 3));
            }
        }

        @Override
        public void returnGetVoiceCommunicationChannelInfoFail(String error, int errorCode) {
            WebServiceMiddleUtils.hand(ChannelVoiceCommunicationActivity.this, error, errorCode);
            finish();
        }

        @Override
        public void returnJoinVoiceCommunicationChannelSuccess(GetBoolenResult getBoolenResult) {
        }

        @Override
        public void returnJoinVoiceCommunicationChannelFail(String error, int errorCode) {
            WebServiceMiddleUtils.hand(ChannelVoiceCommunicationActivity.this, error, errorCode);
        }

        @Override
        public void returnRefuseVoiceCommunicationChannelSuccess(GetBoolenResult getBoolenResult) {
            afterRefuse();
        }

        @Override
        public void returnRefuseVoiceCommunicationChannelFail(String error, int errorCode) {
            afterRefuse();
        }

        @Override
        public void returnLeaveVoiceCommunicationChannelSuccess(GetBoolenResult getBoolenResult) {
            afterDestory();
        }

        @Override
        public void returnLeaveVoiceCommunicationChannelFail(String error, int errorCode) {
            afterDestory();
        }
    }
}
