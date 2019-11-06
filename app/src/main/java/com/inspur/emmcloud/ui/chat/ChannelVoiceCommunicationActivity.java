package com.inspur.emmcloud.ui.chat;

import android.bluetooth.BluetoothHeadset;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Build;
import android.os.CountDownTimer;
import android.os.SystemClock;
import android.provider.Settings;
import android.support.annotation.RequiresApi;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
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
import com.inspur.emmcloud.api.apiservice.ChatAPIService;
import com.inspur.emmcloud.api.apiservice.WSAPIService;
import com.inspur.emmcloud.baselib.util.DensityUtil;
import com.inspur.emmcloud.baselib.util.LogUtils;
import com.inspur.emmcloud.baselib.util.ResolutionUtils;
import com.inspur.emmcloud.baselib.util.StringUtils;
import com.inspur.emmcloud.baselib.util.TimeUtils;
import com.inspur.emmcloud.baselib.util.ToastUtils;
import com.inspur.emmcloud.baselib.widget.CircleTextImageView;
import com.inspur.emmcloud.baselib.widget.LoadingDialog;
import com.inspur.emmcloud.baselib.widget.dialogs.CustomDialog;
import com.inspur.emmcloud.basemodule.application.BaseApplication;
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
import com.inspur.emmcloud.util.privates.CustomProtocol;
import com.inspur.emmcloud.util.privates.MediaPlayerManagerUtils;
import com.inspur.emmcloud.util.privates.NotifyUtil;
import com.inspur.emmcloud.util.privates.SuspensionWindowManagerUtils;
import com.inspur.emmcloud.util.privates.VoiceCommunicationManager;
import com.inspur.emmcloud.util.privates.cache.ContactUserCacheUtils;
import com.inspur.emmcloud.util.privates.cache.ConversationCacheUtils;
import com.inspur.emmcloud.widget.ECMChatInputMenu;
import com.inspur.emmcloud.widget.ECMSpaceItemDecoration;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
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
     * agora的channelId
     */
    public static final String VOICE_VIDEO_CALL_AGORA_ID = "channelId";
    /**
     * 通话类型ECMChatInputMenu.VIDEO_CALL或者ECMChatInputMenu.VOICE_CALL
     */
    public static final String VOICE_VIDEO_CALL_TYPE = "voice_video_call_type";
    /**
     * 通话中来自schema的uid，这个uid表示来自云+中的哪个人
     */
    public static final String SCHEMA_FROM_UID = "voice_video_UID";
    /**
     * 传递页面布局样式的
     */
    public static final String VOICE_COMMUNICATION_STATE = "voice_communication_state";
    /**
     * 传递页面是否来自小窗
     */
    public static final String VOICE_IS_FROM_SMALL_WINDOW = "voice_is_from_window";
    /**
     * 屏幕宽度
     */
    public static final String SCREEN_SIZE = "screen_size";
    /**
     * 邀请人状态布局
     */
    public static final int INVITER_LAYOUT_STATE = 0;
    /**
     * 被邀请人状态布局
     */
    public static final int INVITEE_LAYOUT_STATE = 1;
    /**
     * 通话中布局状态
     */
    public static final int COMMUNICATION_LAYOUT_STATE = 2;
    /**
     * 异常状态，主要给自己用，如果有地方调用本页面，又没有传状态值是这个默认状态
     */
    private static final int EXCEPTION_STATE = -1;
    /**
     * 请求悬浮窗权限
     */
    private static final int REQUEST_WINDOW_PERMISSION = 100;
    private static final int REQUEST_BACKGROUND_WINDOWS = 101;
    /**
     * 表示当前布局状态
     */
    private int layoutState = -1;
    /**
     * 是否来自小窗
     */
    private boolean isFromSmallWindow = false;
    @BindView(R.id.ll_voice_communication_invite_members)
    LinearLayout inviteMembersGroupLinearLayout;
    /**
     * 禁言按钮，没有开放
     */
    @BindView(R.id.img_an_excuse)
    ImageView excuseImg;
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
     * 是否离开频道，杀死activity，在本页面有效，如果需要在应用全局判断通话状态，应该查看
     *
     * @see VoiceCommunicationManager#communicationState
     * 修改这个变量的位置有：
     * @see ChannelVoiceCommunicationActivity#afterRefuse()，
     * @see ChannelVoiceCommunicationActivity#afterLeave() ，
     * @see ChannelVoiceCommunicationActivity#onCreate()
     */
    private boolean isLeaveChannel = false;
    private CountDownTimer countDownOnlyOneConnectLeftTimer;
    /**
     * 本地视频初始x坐标
     */
    private int initX;
    @BindView(R.id.ll_voice_communication_invite)
    LinearLayout inviteeLinearLayout;
    @BindView(R.id.img_user_head)
    CircleTextImageView userHeadImg;
    @BindView(R.id.tv_user_name)
    TextView userNameTv;
    /**
     * 本地视频初始y坐标
     */
    private int initY;
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
    boolean needTimerStartFlag = true;
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
    private ChatAPIService apiService;
    private VoiceCommunicationJoinChannelInfoBean inviteeInfoBean;
    /**
     * 当前频道里在线的人数
     */
//    private int userCount = 1;
    private VoiceCommunicationMemberAdapter voiceCommunicationMemberAdapterFirst;
    private VoiceCommunicationMemberAdapter voiceCommunicationMemberAdapterSecond;
    private MediaPlayerManagerUtils mediaPlayerManagerUtils;
    private VoiceCommunicationManager voiceCommunicationManager;
    private VoiceCommunicationHeadSetReceiver receiver;
    private LoadingDialog loadingDialog;
    /**
     * 60s内无响应挂断 总时长：millisInFuture，隔多长时间回调一次countDownInterval
     */
    private long millisInFuture = 60 * 1000L, countDownInterval = 1000;

    @Override
    public void onCreate() {
        EventBus.getDefault().register(this);
        ButterKnife.bind(this);
        voiceCommunicationManager = VoiceCommunicationManager.getInstance();
        voiceCommunicationManager.initializeAgoraEngine();
        init();
        registerReceiver();
    }

    @Override
    protected void onStart() {
        super.onStart();
        checkHasPermission();
        NotifyUtil.deleteNotify(this);
    }

    private void init() {
        cloudPlusChannelId = getIntent().getStringExtra(ConversationActivity.CLOUD_PLUS_CHANNEL_ID);
        communicationType = getIntent().getStringExtra(VOICE_VIDEO_CALL_TYPE);
        LogUtils.YfcDebug("云+channelId：" + cloudPlusChannelId);
        directOrGroupType = ConversationCacheUtils.getConversationType(this, cloudPlusChannelId);
        LogUtils.YfcDebug("type的类型：" + directOrGroupType);
        //如果是邀请者能收到从外面传进来的人员列表
        List<VoiceCommunicationJoinChannelInfoBean> list = (List<VoiceCommunicationJoinChannelInfoBean>) getIntent().getSerializableExtra("userList");
        if (list != null) {
            voiceCommunicationManager.setVoiceCommunicationMemberList(list);
        }
        //当是-1时，先赋初始值，然后后面
        if (voiceCommunicationManager.getCommunicationState() == -1) {
            voiceCommunicationManager.setCommunicationState(COMMUNICATION_STATE_PRE);
        } else {
            //TODO 是否去掉-1
            voiceCommunicationManager.setCommunicationState(voiceCommunicationManager.getCommunicationState());
        }
        layoutState = getIntent().getIntExtra(VOICE_COMMUNICATION_STATE, EXCEPTION_STATE);
        voiceCommunicationManager.setLayoutState(layoutState);
        isFromSmallWindow = getIntent().getBooleanExtra(VOICE_IS_FROM_SMALL_WINDOW, false);
        recoverData();
        initViews();
        SuspensionWindowManagerUtils.getInstance().hideCommunicationSmallWindow();
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
        voiceCommunicationManager.startCountDownTimer();
        PermissionRequestManagerUtils.getInstance().requestRuntimePermission(this, Permissions.RECORD_AUDIO, new PermissionRequestCallback() {
            @Override
            public void onPermissionRequestSuccess(List<String> permissions) {
            }

            @Override
            public void onPermissionRequestFail(List<String> permissions) {
                ToastUtils.show(ChannelVoiceCommunicationActivity.this, PermissionRequestManagerUtils.getInstance().getPermissionToast(ChannelVoiceCommunicationActivity.this, permissions));
                if (!isFinishing()) {
                    voiceCommunicationManager.setCommunicationState(COMMUNICATION_STATE_OVER);
                    finish();
                }
            }
        });
    }

    @Override
    public int getLayoutResId() {
        return R.layout.activity_voice_channel;
    }

    /**
     * 如果是从小窗口来的，则恢复通话数据
     */
    private void recoverData() {
        Log.d("zhang", "recoverData: layoutState = " + layoutState);
        if (isFromSmallWindow) {
            voiceCommunicationManager.setCommunicationState(voiceCommunicationManager.getCommunicationState());
            layoutState = voiceCommunicationManager.getLayoutState();
            List<VoiceCommunicationJoinChannelInfoBean> totalList = voiceCommunicationManager.getVoiceCommunicationMemberList();
            agoraChannelId = voiceCommunicationManager.getAgoraChannelId();
            communicationType = voiceCommunicationManager.getCommunicationType();
            refreshCommunicationMemberAdapter();
            inviteeInfoBean = voiceCommunicationManager.getInviteeInfoBean();
            cloudPlusChannelId = voiceCommunicationManager.getCloudPlusChannelId();
            if (layoutState == INVITER_LAYOUT_STATE || layoutState == INVITEE_LAYOUT_STATE) {
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
                            initX = (int) event.getRawX();
                            initY = (int) event.getRawY();
                            break;
                        case MotionEvent.ACTION_MOVE:
                            int dx = (int) event.getRawX() - initX;
                            int dy = (int) event.getRawY() - initY;

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
                            v.setLayoutParams(layoutParams);
                            initX = (int) event.getRawX();
                            initY = (int) event.getRawY();
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
     * 初始化功能模块的初始值，如果正在通话中则需要判断是不是要恢复语音通话状态
     * 如是否免提，是否静音
     */
    private void initFunctionState() {
        if (voiceCommunicationManager.getLayoutState() == COMMUNICATION_LAYOUT_STATE) {
            int colorUnSelected = ContextCompat.getColor(this, R.color.voice_communication_function_default);
            int colorSelected = ContextCompat.getColor(this, R.color.voice_communication_function_select);
            handsFreeImg.setSelected(voiceCommunicationManager.isHandsFree());
            muteImg.setSelected(voiceCommunicationManager.isMute());
            handsFreeImg.setClickable(voiceCommunicationManager.getLayoutState() == COMMUNICATION_LAYOUT_STATE);
            muteImg.setClickable(voiceCommunicationManager.getLayoutState() == COMMUNICATION_LAYOUT_STATE);
            handsFreeImg.setImageResource(voiceCommunicationManager.isHandsFree() ? R.drawable.icon_hands_free_selected : R.drawable.icon_hands_free_unselected);
            handsFreeTv.setTextColor(voiceCommunicationManager.isHandsFree() ? colorSelected : colorUnSelected);
            muteImg.setImageResource(voiceCommunicationManager.isMute() ? R.drawable.icon_mute_selected : R.drawable.icon_mute_unselcected);
            muteTv.setTextColor(voiceCommunicationManager.isMute() ? colorSelected : colorUnSelected);
        }
        voiceCommunicationManager.muteLocalAudioStream(voiceCommunicationManager.isMute());
        voiceCommunicationManager.muteAllRemoteAudioStreams(false);
        voiceCommunicationManager.onSwitchSpeakerphoneClicked(voiceCommunicationManager.isHandsFree());
    }

    /**
     * 通过agoraChannelId获取Channel信息
     *
     * @param agoraChannelId
     */
    private void getChannelInfoByChannelId(String agoraChannelId) {
        if (NetUtils.isNetworkConnected(this)) {
            loadingDialog.show();
            apiService.getAgoraChannelInfo(agoraChannelId);
        }
    }

    /**
     * 创建频道，由主叫方调用
     */
    private void createChannel() {
        Log.d("zhang", "createChannel: ");
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
                loadingDialog.show();
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
    private void initCommunicationViews(int state) {
        if (state == EXCEPTION_STATE) {
            finish();
            return;
        }
        layoutState = state;
        voiceCommunicationManager.setLayoutState(layoutState);
        changeFunctionState(state);
        if (communicationType.equals(ECMChatInputMenu.VOICE_CALL) && (state == COMMUNICATION_LAYOUT_STATE
                || state == INVITEE_LAYOUT_STATE || state == INVITER_LAYOUT_STATE)) {
            if (voiceCommunicationManager.getVoiceCommunicationMemberList().size() <= 0) {
                return;
            }
            refreshCommunicationMemberAdapter();
        }
    }

    /**
     * 修改功能键的方法点击事件时调用
     *
     * @param state
     */
    private void changeFunctionState(int state) {
        if (communicationType.equals(ECMChatInputMenu.VOICE_CALL)) {
            inviteeLinearLayout.setVisibility(state == INVITEE_LAYOUT_STATE ? View.VISIBLE : View.GONE);
            inviteMembersGroupLinearLayout.setVisibility((state == INVITER_LAYOUT_STATE || state == COMMUNICATION_LAYOUT_STATE) ? View.VISIBLE : View.GONE);
            communicationMembersLinearLayout.setVisibility(state == INVITEE_LAYOUT_STATE ? View.VISIBLE : View.GONE);
            functionLinearLayout.setVisibility((state == INVITER_LAYOUT_STATE || state == COMMUNICATION_LAYOUT_STATE) ? View.VISIBLE : View.GONE);
            communicationStateTv.setVisibility((state == INVITER_LAYOUT_STATE || state == COMMUNICATION_LAYOUT_STATE) ? View.VISIBLE : View.GONE);
            communicationTimeChronometer.setVisibility(state == COMMUNICATION_LAYOUT_STATE ? View.VISIBLE : View.GONE);

            //悬浮窗控制按钮
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
                switch (state) {
                    case INVITER_LAYOUT_STATE:
                        mediaPlayerManagerUtils.changeToEarpieceModeNoStop();
                        break;
                    case INVITEE_LAYOUT_STATE:
                        mediaPlayerManagerUtils.changeToSpeakerMode();
                        break;
                }
                mediaPlayerManagerUtils.play(R.raw.voice_communication_watting_answer, null, true);
            } else {
                mediaPlayerManagerUtils.stop();
            }
        } else if (communicationType.equals(ECMChatInputMenu.VIDEO_CALL)) {
            voiceCommunicationManager.adjustPlaybackSignalVolume(400);
            voiceCommunicationManager.setEnableSpeakerphone(true);
            turnToVoiceLayout.setVisibility(state == COMMUNICATION_LAYOUT_STATE ? View.VISIBLE : View.GONE);
            videoHungUp.setVisibility((state == COMMUNICATION_LAYOUT_STATE || state == INVITER_LAYOUT_STATE || state == INVITEE_LAYOUT_STATE) ? View.VISIBLE : View.GONE);
            answerVideoPhoneLayout.setVisibility(state == INVITEE_LAYOUT_STATE ? View.VISIBLE : View.GONE);
            switchCameraLayout.setVisibility(state == COMMUNICATION_LAYOUT_STATE ? View.VISIBLE : View.GONE);
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
    //多人通话中，收到refuse消息，则弹出相关提示，并检查是否应该finish，收到destroy消息则改变状态，并结束通话
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onReceiveVoiceOrVideoCall(final GetVoiceAndVideoResult getVoiceAndVideoResult) {
        CustomProtocol customProtocol = new CustomProtocol(getVoiceAndVideoResult.getContextParamsSchema());
        String cmd = customProtocol.getParamMap().get("cmd");
        if (!StringUtils.isBlank(cmd) && getVoiceAndVideoResult.getContextParamsRoom().equals(agoraChannelId)) {
            String uid = customProtocol.getParamMap().get("uid");
            if (cmd.equals("destroy")) {
                changeUserConnectStateByUid(VoiceCommunicationJoinChannelInfoBean.CONNECT_STATE_LEAVE, uid);
                voiceCommunicationManager.setCommunicationState(COMMUNICATION_STATE_OVER);
                SuspensionWindowManagerUtils.getInstance().hideCommunicationSmallWindow();
                finish();
            } else if (cmd.equals("refuse")) {
                String name = ContactUserCacheUtils.getUserName(uid);
                if (!StringUtils.isBlank(name)) {
                    if (layoutState == COMMUNICATION_LAYOUT_STATE) {
                        ToastUtils.show(name + getString(R.string.communication_has_leave));
                    } else if (layoutState == INVITER_LAYOUT_STATE) {
                        //拨打方
                        ToastUtils.show(name + getString(R.string.meeting_has_refused));
                    } else if (layoutState == INVITEE_LAYOUT_STATE &&
                            answerPhoneImg.getVisibility() != View.VISIBLE) {
                        //接听方
                        ToastUtils.show(name + getString(R.string.meeting_has_refused));
                    }
                }
                changeUserConnectStateByUid(VoiceCommunicationJoinChannelInfoBean.CONNECT_STATE_REFUSE, uid);
                checkCommunicationFinish();
            }
        }
    }

    /**
     * 检查是否需要退出，当通话中的人数和等待人数之和小于2则退出
     */
    private boolean checkCommunicationFinish() {
        if (voiceCommunicationManager.getWaitAndConnectedNumber() < 2) {
            voiceCommunicationManager.setCommunicationState(COMMUNICATION_STATE_OVER);
            Log.d("zhang", "COMMUNICATION_STATE_OVER: 000000 ");
            refuseOrLeaveChannel(COMMUNICATION_LEAVE);
            return true;
        }
        return false;
    }

    /**
     * 初始化Views
     */
    private void initViews() {
        loadingDialog = new LoadingDialog(this);
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
        voiceCommunicationMemberAdapterFirst = new VoiceCommunicationMemberAdapter(this,
                voiceCommunicationManager.getVoiceCommunicationMemberListTop(), 0);
        voiceCommunicationMemberAdapterSecond = new VoiceCommunicationMemberAdapter(this,
                voiceCommunicationManager.getVoiceCommunicationMemberListBottom(), 0);
        //初始化声网的callBacks
        initAgoraCallbacks();
        //响铃控制
        mediaPlayerManagerUtils = MediaPlayerManagerUtils.getManager();
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
        firstRecyclerview.setLayoutManager(layoutManager);
        firstRecyclerview.addItemDecoration(new ECMSpaceItemDecoration(DensityUtil.dip2px(this, 8)));
        firstRecyclerview.setAdapter(voiceCommunicationMemberAdapterFirst);

        LinearLayoutManager layoutManager2 = new LinearLayoutManager(this);
        layoutManager2.setOrientation(LinearLayoutManager.HORIZONTAL);
        secondRecyclerview.setLayoutManager(layoutManager2);
        secondRecyclerview.addItemDecoration(new ECMSpaceItemDecoration(DensityUtil.dip2px(this, 8)));
        secondRecyclerview.setAdapter(voiceCommunicationMemberAdapterSecond);

        LinearLayoutManager layoutManagerMembersSecond = new LinearLayoutManager(this);
        layoutManagerMembersSecond.setOrientation(LinearLayoutManager.HORIZONTAL);
        communicationMemberSecondRecyclerview.addItemDecoration(new ECMSpaceItemDecoration(DensityUtil.dip2px(this, 8)));
        communicationMemberSecondRecyclerview.setLayoutManager(layoutManagerMembersSecond);

        LinearLayoutManager layoutManagerMemebersFirst = new LinearLayoutManager(this);
        layoutManagerMemebersFirst.setOrientation(LinearLayoutManager.HORIZONTAL);
        communicationMembersFirstRecyclerview.addItemDecoration(new ECMSpaceItemDecoration(DensityUtil.dip2px(this, 8)));
        communicationMembersFirstRecyclerview.setLayoutManager(layoutManagerMemebersFirst);

        directFunctionLayout.setVisibility(directOrGroupType.equals(Conversation.TYPE_DIRECT) ? View.VISIBLE : View.GONE);
        groupAnswerOrHungUpLayout.setVisibility(directOrGroupType.equals(Conversation.TYPE_GROUP) ? View.VISIBLE : View.GONE);
        functionLinearLayout.setVisibility(directOrGroupType.equals(Conversation.TYPE_GROUP) ? View.VISIBLE : View.GONE);

        Log.d("zhang", "initViews: layoutState = " + layoutState);
        initCommunicationViews(layoutState);
        initFunctionState();
        //第一次打开ChannelVoiceCommunicationActivity时，如果是邀请人状态，则刷新Adapter并创建频道
        //如果是被邀请人状态则获取声网的channelId获取频道信息
        if (!isFromSmallWindow) {
            switch (layoutState) {
                case INVITER_LAYOUT_STATE:
                    refreshCommunicationMemberAdapter();
                    createChannel();
                    break;
                case INVITEE_LAYOUT_STATE:
                    String agoraChannelId = getIntent().getStringExtra(VOICE_VIDEO_CALL_AGORA_ID);
                    voiceCommunicationManager.setEncryptionSecret(agoraChannelId);
                    getChannelInfoByChannelId(agoraChannelId);
                    break;
                default:
                    break;
            }
        }
        //如果是来自小窗口，则取得已经通话的时长，继续计时
//        if (getIntent().getLongExtra(VOICE_TIME, 0) > 0) {
        //通话相差时间
        if (isFromSmallWindow) {
            long duration = System.currentTimeMillis() - voiceCommunicationManager.getConnectStartTime();
            communicationTimeChronometer.setBase(SystemClock.elapsedRealtime() - duration);
            communicationTimeChronometer.start();
        }
//        }
//        dragLocalVideoView();
    }

    /**
     * 初始化回调
     */
    private void initAgoraCallbacks() {
        voiceCommunicationManager.setOnVoiceCommunicationCallbacks(new OnVoiceCommunicationCallbacksImpl() {
            @Override
            public void onUserOffline(int uid, int reason) {
//                userCount = userCount - 1;
                if (voiceCommunicationManager.getUserCount() < 2) {
                    boolean isFinish = checkCommunicationFinish();
                    if (!isFinish) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                startCountDown();
                            }
                        });
                    }
                }
            }

            @Override
            public void onUserJoined(final int uid, int elapsed) {
                if (mediaPlayerManagerUtils != null) {
                    mediaPlayerManagerUtils.stop();
                }
                if (voiceCommunicationManager.getUserCount() >= 2) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (voiceCommunicationManager.isInviter() && needTimerStartFlag && voiceCommunicationManager.getUserCount() == 2) {
                                needTimerStartFlag = false;
                                communicationTimeChronometer.setBase(SystemClock.elapsedRealtime());
                                communicationTimeChronometer.start();
                            }
                            if (layoutState == INVITER_LAYOUT_STATE || isMySelf(uid) || voiceCommunicationManager.getCommunicationState() == COMMUNICATION_STATE_ING) {
                                voiceCommunicationManager.setCommunicationState(COMMUNICATION_STATE_ING);
                                Log.d("zhang", "onUserJoined run: layoutState = " + layoutState);
                                layoutState = COMMUNICATION_LAYOUT_STATE;
                                voiceCommunicationManager.setLayoutState(layoutState);
                                changeFunctionState(COMMUNICATION_LAYOUT_STATE);
                                refreshCommunicationMemberAdapter();
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
                        //邀请成功后再发出邀请消息
                        if (voiceCommunicationManager.isInviter()) {
                            sendCommunicationCommand("invite");
                        }
                        //检查加入的如果是自己才启动计时器
                        for (int i = 0; i < voiceCommunicationManager.getVoiceCommunicationMemberList().size(); i++) {
                            if (voiceCommunicationManager.getVoiceCommunicationMemberList().get(i).getUserId().equals(MyApplication.getInstance().getUid())
                                    && voiceCommunicationManager.getVoiceCommunicationMemberList().get(i).getAgoraUid() == uid) {
                                communicationTimeChronometer.setBase(SystemClock.elapsedRealtime());
                                communicationTimeChronometer.start();
                                voiceCommunicationManager.setConnectStartTime(System.currentTimeMillis());
                            }
                        }
                        refreshCommunicationMemberAdapter();
                    }
                });
            }

            @Override
            public void onRejoinChannelSuccess(String channel, int uid, int elapsed) {
                refreshCommunicationMemberAdapter();
            }

            @Override
            public void onUserMuteAudio(int uid, boolean muted) {

            }

            @Override
            public void onError(int err) {
                Log.d("zhang", "agoraException onError: err = " + err);
                agoraException();
            }

            @Override
            public void onConnectionLost() {
                Log.d("zhang", "agoraException onConnectionLost: ");
                agoraException();
            }

            @Override
            public void onNetworkQuality(final int uid, final int txQuality, int rxQuality) {
                if (layoutState == COMMUNICATION_LAYOUT_STATE) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            communicationStateTv.setText((uid == 0 && txQuality >= 5) ? getString(R.string.voice_communication_quality) : "");
                        }
                    });
                }
            }

            @Override
            public void onAudioVolumeIndication(final VoiceCommunicationAudioVolumeInfo[] speakers, final int totalVolume) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
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
                        //屏蔽视频
//                        setupRemoteVideo(uid);
                    }
                });
            }

            @Override
            public void onCountDownTimerFinish() {
                //如果是邀请或被邀请状态，倒计时结束时挂断电话
                if (layoutState == INVITEE_LAYOUT_STATE || layoutState == INVITER_LAYOUT_STATE || voiceCommunicationManager.getConnectedNumber() < 2) {
                    isLeaveChannel = true;
                    refuseOrLeaveChannel(COMMUNICATION_LEAVE);
                }
                //把仍在等待中的人置为离开状态
                List<VoiceCommunicationJoinChannelInfoBean> totalList = voiceCommunicationManager.getVoiceCommunicationMemberList();
                for (int i = 0; i < totalList.size(); i++) {
                    if (totalList.get(i).getConnectState() == VoiceCommunicationJoinChannelInfoBean.CONNECT_STATE_INIT) {
                        totalList.get(i).setConnectState(VoiceCommunicationJoinChannelInfoBean.CONNECT_STATE_LEAVE);
                    }
                }
                refreshCommunicationMemberAdapter();
            }
        });
    }

    /**
     * 如果加入频道的是自己
     *
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
     * 当在通话中，只剩下一个人在频道中，开始倒计时，倒计时结束时，仍然没有其他人加入，则关闭频道
     */
    private void startCountDown() {
        if (countDownOnlyOneConnectLeftTimer != null) {
            countDownOnlyOneConnectLeftTimer.cancel();
            countDownOnlyOneConnectLeftTimer = null;
        }
        countDownOnlyOneConnectLeftTimer = new CountDownTimer(millisInFuture, countDownInterval) {
            @Override
            public void onTick(long millisUntilFinished) {
            }

            @Override
            public void onFinish() {
                //如果是邀请或被邀请状态，倒计时结束时挂断电话
                if (layoutState == COMMUNICATION_LAYOUT_STATE && voiceCommunicationManager.getUserCount() < 2) {
                    voiceCommunicationManager.setCommunicationState(COMMUNICATION_STATE_OVER);
                    Log.d("zhang", "COMMUNICATION_STATE_OVER: 22222222 ");
                    refuseOrLeaveChannel(COMMUNICATION_LEAVE);
                }
            }
        };
        countDownOnlyOneConnectLeftTimer.start();
    }

    /**
     * 处理声网的异常，不能通过socket处理的异常都通过这个方法来处理，依赖声网
     */
    private void agoraException() {
        voiceCommunicationManager.setCommunicationState(COMMUNICATION_STATE_OVER);
        Log.d("zhang", "COMMUNICATION_STATE_OVER: 33333333 ");
        finish();
    }

    /**
     * 修改用户的链接状态
     * 通过云+uid
     *
     * @param connectStateConnected
     */
    private void changeUserConnectStateByUid(int connectStateConnected, String uid) {
        if (voiceCommunicationManager.getVoiceCommunicationMemberList().size() > 0) {
            for (int i = 0; i < voiceCommunicationManager.getVoiceCommunicationMemberList().size(); i++) {
                if (voiceCommunicationManager.getVoiceCommunicationMemberList().get(i).getUserId().equals(uid)) {
                    voiceCommunicationManager.getVoiceCommunicationMemberList().get(i).setConnectState(connectStateConnected);
                    if (connectStateConnected > 1) {
                        voiceCommunicationManager.getVoiceCommunicationMemberList().remove(i);
                    }
                    break;
                }
            }
            refreshCommunicationMemberAdapter();
        }
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
        if (layoutState == INVITER_LAYOUT_STATE) {
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
    private void refreshCommunicationMemberAdapter() {
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
                excuseImg.setImageResource(excuseImg.isSelected() ? R.drawable.icon_excuse_selected : R.drawable.icon_excuse_unselected);
                break;
            case R.id.tv_hands_free:
            case R.id.img_hands_free:
                switchFunctionViewUIState(handsFreeImg, handsFreeTv);
                voiceCommunicationManager.setHandsFree(handsFreeImg.isSelected());
                voiceCommunicationManager.onSwitchSpeakerphoneClicked(handsFreeImg.isSelected());
                handsFreeImg.setImageResource(handsFreeImg.isSelected() ? R.drawable.icon_hands_free_selected : R.drawable.icon_hands_free_unselected);
                break;
            case R.id.tv_mute:
            case R.id.img_mute:
                switchFunctionViewUIState(muteImg, muteTv);
                voiceCommunicationManager.setMute(muteImg.isSelected());
                voiceCommunicationManager.muteLocalAudioStream(muteImg.isSelected());
                muteImg.setImageResource(muteImg.isSelected() ? R.drawable.icon_mute_selected : R.drawable.icon_mute_unselcected);
                break;
            case R.id.img_tran_video:
                switchFunctionViewUIState(tranVideoImg, tranVideoTv);
                tranVideoImg.setImageResource(tranVideoImg.isSelected() ? R.drawable.icon_trans_video : R.drawable.icon_trans_video);
                break;
            case R.id.img_answer_the_phone:
                voiceCommunicationManager.setCommunicationState(COMMUNICATION_STATE_ING);
                initCommunicationViews(COMMUNICATION_LAYOUT_STATE);
                communicationTimeChronometer.setBase(SystemClock.elapsedRealtime());
                communicationTimeChronometer.start();
                voiceCommunicationManager.setConnectStartTime(System.currentTimeMillis());
                int joinState = voiceCommunicationManager.joinChannel(inviteeInfoBean.getToken(),
                        agoraChannelId, inviteeInfoBean.getUserId(), inviteeInfoBean.getAgoraUid());
                if (joinState != 0) {
                    refuseOrLeaveChannel(COMMUNICATION_REFUSE);
                }
                break;
            case R.id.ll_video_hung_up:
            case R.id.img_hung_up:
                if (answerPhoneImg.getVisibility() == View.VISIBLE) {
                    refuseOrLeaveChannel(COMMUNICATION_REFUSE);
                } else {
                    refuseOrLeaveChannel(COMMUNICATION_LEAVE);
                }
                voiceCommunicationManager.setCommunicationState(COMMUNICATION_STATE_OVER);
                Log.d("zhang", "COMMUNICATION_STATE_OVER: 4444444 ");
                break;
            case R.id.img_voice_communication_pack_up:
                saveCommunicationData();
                pickUpVoiceCommunication();
                break;
            case R.id.ll_video_switch_camera:
                LogUtils.YfcDebug("转换摄像头");
                voiceCommunicationManager.switchCamera();
                break;
            case R.id.ll_video_answer_phone:
                LogUtils.YfcDebug("接听视频电话");
                setupLocalVideo();
                initCommunicationViews(COMMUNICATION_LAYOUT_STATE);
                voiceCommunicationManager.joinChannel(inviteeInfoBean.getToken(),
                        agoraChannelId, inviteeInfoBean.getUserId(), inviteeInfoBean.getAgoraUid());
                break;
            case R.id.ll_turn_to_voice:
                voiceCommunicationManager.disableVideo();
                break;
            default:
                break;
        }
    }

    /**
     * 修改Image选中状态和textView属性
     *
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
     * 这里改为四步同时进行，不再等待EmmServer的返回
     * 1，告知EmmServer已经离开频道，2，发出refuse或者destroy命令消息，3，销毁声网资源，4，关闭页面
     *
     * @param type
     */
    private void refuseOrLeaveChannel(int type) {
        if (ClickRuleUtil.isFastClick()) {
            return;
        }
        switch (type) {
            case COMMUNICATION_REFUSE:
                if (NetUtils.isNetworkConnected(ChannelVoiceCommunicationActivity.this)) {
                    apiService.refuseAgoraChannel(agoraChannelId);
                }
                afterRefuse();
                break;
            case COMMUNICATION_LEAVE:
                afterLeave();
                break;
            default:
                break;
        }
    }

    /**
     * 应用进入小窗口状态，出发时机是onPause和用户自己点击小窗口
     */
    private void pickUpVoiceCommunication() {
        if (voiceCommunicationManager.getCommunicationState() != COMMUNICATION_STATE_OVER && voiceCommunicationManager.getCommunicationState() != -1) {
            if (Build.VERSION.SDK_INT >= 23) {
                if (Settings.canDrawOverlays(this)) {
                    Log.d("zhang", "pickUpVoiceCommunication: ");
                    checkCanBackGroundStart();
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
                            }).setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    })
                            .show();
                }
            } else {
                if (Build.VERSION.SDK_INT >= 19) {
                    checkCanBackGroundStart();
                } else {
                    showSmallWindowAndCloseActivity();
                }
            }
        }
    }

    /**
     * 针对小米的判断
     */
    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    private void checkCanBackGroundStart() {
        if (AppUtils.canBackgroundStart(this)) {
            showSmallWindowAndCloseActivity();
        } else {
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
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_WINDOW_PERMISSION) {
            if (Build.VERSION.SDK_INT >= 23 && Settings.canDrawOverlays(this)) {
                checkCanBackGroundStart();
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

    /**
     * 进入小窗状态，保存状态
     */
    private void saveCommunicationData() {
        voiceCommunicationManager.setLayoutState(layoutState);
        Log.d("zhang", "saveCommunicationData: layoutState = " + layoutState);
        voiceCommunicationManager.setCommunicationState(voiceCommunicationManager.getCommunicationState());
        voiceCommunicationManager.setAgoraChannelId(agoraChannelId);
        voiceCommunicationManager.setCommunicationType(communicationType);
        voiceCommunicationManager.setInviteeInfoBean(inviteeInfoBean);
        voiceCommunicationManager.setCloudPlusChannelId(cloudPlusChannelId);
    }

    @Override
    protected void onStop() {
        super.onStop();
        //已接通，停止音乐，关闭activity，打开小窗口
        if (voiceCommunicationManager.getCommunicationState() == COMMUNICATION_STATE_ING) {
            if (mediaPlayerManagerUtils != null) {
                mediaPlayerManagerUtils.stop();
            }
        }
        Log.d("zhang", "onPause: layoutState = " + layoutState);
        Log.d("zhang", "onPause: voiceCommunicationManager.getCommunicationState() = " + voiceCommunicationManager.getCommunicationState());
        if (voiceCommunicationManager.getCommunicationState() != COMMUNICATION_STATE_OVER && voiceCommunicationManager.getCommunicationState() != -1) {
            saveCommunicationData();
            if (Build.VERSION.SDK_INT >= 23) {
                if (Settings.canDrawOverlays(this) && AppUtils.canBackgroundStart(this)) {
                    showSmallWindowAndCloseActivity();
                }
            } else {
                if (Build.VERSION.SDK_INT >= 19) {
                    if (AppUtils.canBackgroundStart(this)) {
                        showSmallWindowAndCloseActivity();
                    }
                } else {
                    showSmallWindowAndCloseActivity();
                }
            }
        }
    }

    /**
     * 展示小窗，关闭Activity
     */
    private void showSmallWindowAndCloseActivity() {
        SuspensionWindowManagerUtils.getInstance().showCommunicationSmallWindow(ResolutionUtils.getWidth(this),
                Long.parseLong(TimeUtils.getChronometerSeconds(communicationTimeChronometer.getText().toString())));
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (countDownOnlyOneConnectLeftTimer != null) {
            countDownOnlyOneConnectLeftTimer.cancel();
            countDownOnlyOneConnectLeftTimer = null;
        }
        //恢复状态
        if (voiceCommunicationManager.getCommunicationState() == COMMUNICATION_STATE_OVER) {
            voiceCommunicationManager.setCommunicationState(-1);
        }
        if (receiver != null) {
            unregisterReceiver(receiver);
        }
        EventBus.getDefault().unregister(this);
        if (mediaPlayerManagerUtils != null) {
            mediaPlayerManagerUtils.stop();
        }
    }

    /**
     * 设置邀请者信息
     *
     * @param getVoiceCommunicationResult
     */
    private void setInviterInfo(GetVoiceCommunicationResult getVoiceCommunicationResult) {
        VoiceCommunicationJoinChannelInfoBean infoBean = getVoiceCommunicationResult.getVoiceCommunicationJoinChannelInfoBeanList().get(0);
        ImageDisplayUtils.getInstance().displayImage(userHeadImg, infoBean.getHeadImageUrl(), R.drawable.icon_person_default);
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
     * 向socket发送指令消息
     *
     * @param commandType
     */
    private void sendCommunicationCommand(String commandType) {
        WSAPIService.getInstance().sendStartVoiceAndVideoCallMessage(cloudPlusChannelId, agoraChannelId,
                voiceCommunicationManager.getSchema(commandType, cloudPlusChannelId, agoraChannelId), voiceCommunicationManager.getVoiceVideoCommunicationType(), voiceCommunicationManager.getUidArray(
                        voiceCommunicationManager.getVoiceCommunicationMemberList()), voiceCommunicationManager.getActionByCommandType(commandType));
    }

    /**
     * 拒绝之后的后续逻辑处理
     */
    private void afterRefuse() {
        voiceCommunicationManager.destroy();
        if (!isLeaveChannel) {
            sendCommunicationCommand("refuse");
            isLeaveChannel = true;
        }
        SuspensionWindowManagerUtils.getInstance().hideCommunicationSmallWindow();
        if (mediaPlayerManagerUtils != null) {
            mediaPlayerManagerUtils.stop();
        }
        Log.d("zhang", "afterRefuse: 7777777777777");
        voiceCommunicationManager.setCommunicationState(COMMUNICATION_STATE_OVER);
        finish();
    }

    /**
     * 销毁之后的逻辑处理
     */
    private void afterLeave() {
        voiceCommunicationManager.destroy();
        if (layoutState == COMMUNICATION_LAYOUT_STATE && voiceCommunicationManager.getUserCount() >= 2) {
            if (!isLeaveChannel) {
                sendCommunicationCommand("refuse");
                isLeaveChannel = true;
            }
        } else if (voiceCommunicationManager.getUserCount() < 2) {
            //当群里只剩一人时，发出此消息，此时声网channel已经不存在，告知其他人关闭页面
            if (!isLeaveChannel) {
                sendCommunicationCommand("destroy");
                isLeaveChannel = true;
            }
        }
        SuspensionWindowManagerUtils.getInstance().hideCommunicationSmallWindow();
        if (mediaPlayerManagerUtils != null) {
            mediaPlayerManagerUtils.stop();
        }
        Log.d("zhang", "afterLeave: 8888888888");
        voiceCommunicationManager.setCommunicationState(COMMUNICATION_STATE_OVER);
        finish();
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
     * 主叫方创建频道失败，或者被叫方获取频道信息失败的处理
     *
     * @param error
     * @param errorCode
     */
    private void createChannelOrGetChannelInfoFail(String error, int errorCode) {
        LoadingDialog.dimissDlg(loadingDialog);
        SuspensionWindowManagerUtils.getInstance().hideCommunicationSmallWindow();
        voiceCommunicationManager.destroy();
        WebServiceMiddleUtils.hand(ChannelVoiceCommunicationActivity.this, error, errorCode);
        finish();
    }

    class WebService extends APIInterfaceInstance {
        @Override
        public void returnGetVoiceCommunicationResultSuccess(GetVoiceCommunicationResult getVoiceCommunicationResult) {
            LoadingDialog.dimissDlg(loadingDialog);
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
                    refreshCommunicationMemberAdapter();
                } else {
                    voiceCommunicationManager.destroy();
                    finish();
                }
            } else {
                voiceCommunicationManager.destroy();
                finish();
            }
        }

        @Override
        public void returnGetVoiceCommunicationResultFail(String error, int errorCode) {
            createChannelOrGetChannelInfoFail(error, errorCode);
        }

        @Override
        public void returnGetVoiceCommunicationChannelInfoSuccess(GetVoiceCommunicationResult getVoiceCommunicationResult) {
            LoadingDialog.dimissDlg(loadingDialog);
            //当所有人都不处于接通状态，就认为点通知进来也应该关闭通话
            boolean isChannelExist = false;
            for (VoiceCommunicationJoinChannelInfoBean voiceCommunicationJoinChannelInfoBean : getVoiceCommunicationResult.getVoiceCommunicationJoinChannelInfoBeanList()) {
                if (voiceCommunicationJoinChannelInfoBean.getConnectState() == VoiceCommunicationJoinChannelInfoBean.CONNECT_STATE_CONNECTED) {
                    isChannelExist = true;
                    break;
                }
            }
            if (!isChannelExist) {
                refuseOrLeaveChannel(COMMUNICATION_REFUSE);
            } else {
                agoraChannelId = getVoiceCommunicationResult.getChannelId();
                voiceCommunicationManager.setAgoraChannelId(agoraChannelId);
                setInviterInfo(getVoiceCommunicationResult);
                voiceCommunicationManager.getVoiceCommunicationMemberList().clear();
                voiceCommunicationManager.getVoiceCommunicationMemberList().addAll(getVoiceCommunicationResult.getVoiceCommunicationJoinChannelInfoBeanList());
                inviteeInfoBean = getMyCommunicationInfoBean(getVoiceCommunicationResult);
                if (voiceCommunicationManager.getVoiceCommunicationMemberList().size() <= 5) {
                    communicationMembersFirstRecyclerview.setAdapter(new VoiceCommunicationMemberAdapter(
                            ChannelVoiceCommunicationActivity.this, voiceCommunicationManager.getVoiceCommunicationMemberList(), 3));
                } else if (voiceCommunicationManager.getVoiceCommunicationMemberList().size() <= 9) {
                    List<VoiceCommunicationJoinChannelInfoBean> list1 = voiceCommunicationManager.getVoiceCommunicationMemberList().subList(0, 5);
                    List<VoiceCommunicationJoinChannelInfoBean> list2 = voiceCommunicationManager.getVoiceCommunicationMemberList().subList(5,
                            voiceCommunicationManager.getVoiceCommunicationMemberList().size());
                    communicationMembersFirstRecyclerview.setAdapter(new VoiceCommunicationMemberAdapter(ChannelVoiceCommunicationActivity.this, list1, 3));
                    communicationMemberSecondRecyclerview.setAdapter(new VoiceCommunicationMemberAdapter(ChannelVoiceCommunicationActivity.this, list2, 3));
                }
                refreshCommunicationMemberAdapter();
            }
        }

        @Override
        public void returnGetVoiceCommunicationChannelInfoFail(String error, int errorCode) {
            createChannelOrGetChannelInfoFail(error, errorCode);
        }
    }
}
