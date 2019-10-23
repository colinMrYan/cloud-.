package com.inspur.emmcloud.ui.chat;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.CountDownTimer;
import android.os.SystemClock;
import android.provider.Settings;
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
import com.inspur.emmcloud.bean.chat.GetVoiceAndVideoResult;
import com.inspur.emmcloud.bean.chat.GetVoiceCommunicationResult;
import com.inspur.emmcloud.bean.chat.VoiceCommunicationAudioVolumeInfo;
import com.inspur.emmcloud.bean.chat.VoiceCommunicationJoinChannelInfoBean;
import com.inspur.emmcloud.bean.system.GetBoolenResult;
import com.inspur.emmcloud.ui.AppSchemeHandleActivity;
import com.inspur.emmcloud.util.privates.CustomProtocol;
import com.inspur.emmcloud.util.privates.MediaPlayerManagerUtils;
import com.inspur.emmcloud.util.privates.NotifyUtil;
import com.inspur.emmcloud.util.privates.SuspensionWindowManagerUtils;
import com.inspur.emmcloud.util.privates.VoiceCommunicationUtils;
import com.inspur.emmcloud.util.privates.cache.ContactUserCacheUtils;
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
 * 截止191018
 * 能进入这个页面的入口有：
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
     * @see VoiceCommunicationUtils#communicationState
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
     * 通话时长，用来记录Chronometer控件的时间
     */
    public static final String VOICE_TIME = "voice_time";
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
     * @see VoiceCommunicationUtils#communicationState
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
    /**
     * 30s内无响应挂断 总时长：millisInFuture，隔多长时间回调一次countDownInterval
     */
    private long millisInFuture = 30 * 1000L, countDownInterval = 1000;
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
    private List<VoiceCommunicationJoinChannelInfoBean> voiceCommunicationMemberList = new ArrayList<>();
    private List<VoiceCommunicationJoinChannelInfoBean> voiceCommunicationMemberList1 = new ArrayList<>();
    private List<VoiceCommunicationJoinChannelInfoBean> voiceCommunicationMemberList2 = new ArrayList<>();
    private VoiceCommunicationJoinChannelInfoBean inviteeInfoBean;
    /**
     * 当前频道里在线的人数
     */
    private int userCount = 1;
    private VoiceCommunicationMemberAdapter voiceCommunicationMemberAdapterFirst;
    private VoiceCommunicationMemberAdapter voiceCommunicationMemberAdapterSecond;
    private MediaPlayerManagerUtils mediaPlayerManagerUtils;
    private VoiceCommunicationUtils voiceCommunicationUtils;

    @Override
    public void onCreate() {
        EventBus.getDefault().register(this);
        ButterKnife.bind(this);
        voiceCommunicationUtils = VoiceCommunicationUtils.getInstance();
        voiceCommunicationUtils.initializeAgoraEngine();
        cloudPlusChannelId = getIntent().getStringExtra(ConversationActivity.CLOUD_PLUS_CHANNEL_ID);
        communicationType = getIntent().getStringExtra(VOICE_VIDEO_CALL_TYPE);
        //如果是邀请者能收到从外面传进来的人员列表
        List<VoiceCommunicationJoinChannelInfoBean> list = (List<VoiceCommunicationJoinChannelInfoBean>) getIntent().getSerializableExtra("userList");
        if (list != null) {
            voiceCommunicationMemberList = list;
        }
        if (voiceCommunicationUtils.getCommunicationState() == -1) {
            voiceCommunicationUtils.setCommunicationState(COMMUNICATION_STATE_PRE);
        }
        voiceCommunicationUtils.setCommunicationState(voiceCommunicationUtils.getCommunicationState());
        layoutState = getIntent().getIntExtra(VOICE_COMMUNICATION_STATE, EXCEPTION_STATE);
        voiceCommunicationUtils.setLayoutState(layoutState);
        isFromSmallWindow = getIntent().getBooleanExtra(VOICE_IS_FROM_SMALL_WINDOW, false);
        SuspensionWindowManagerUtils.getInstance().hideCommunicationSmallWindow();
        recoverData();
        initViews();
        checkHasPermission();
    }

    /**
     * 检查权限
     */
    private void checkHasPermission() {
        PermissionRequestManagerUtils.getInstance().requestRuntimePermission(this, Permissions.RECORD_AUDIO, new PermissionRequestCallback() {
            @Override
            public void onPermissionRequestSuccess(List<String> permissions) {
                voiceCommunicationUtils.startCountDownTimer();
            }

            @Override
            public void onPermissionRequestFail(List<String> permissions) {
                ToastUtils.show(ChannelVoiceCommunicationActivity.this, PermissionRequestManagerUtils.getInstance().getPermissionToast(ChannelVoiceCommunicationActivity.this, permissions));
                if (!isFinishing()) {
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
            voiceCommunicationUtils.setCommunicationState(voiceCommunicationUtils.getCommunicationState());
            layoutState = voiceCommunicationUtils.getLayoutState();
            voiceCommunicationMemberList.clear();
            voiceCommunicationMemberList = voiceCommunicationUtils.getVoiceCommunicationMemberList();
            agoraChannelId = voiceCommunicationUtils.getAgoraChannelId();
            communicationType = voiceCommunicationUtils.getCommunicationType();
            refreshCommunicationMemberAdapter();
            inviteeInfoBean = voiceCommunicationUtils.getInviteeInfoBean();
            userCount = voiceCommunicationUtils.getUserCount();
            cloudPlusChannelId = voiceCommunicationUtils.getCloudPlusChannelId();
            if (layoutState == INVITER_LAYOUT_STATE || layoutState == INVITEE_LAYOUT_STATE) {
                //头像源数据修改为本地，注释掉的是从接口中读取的url，约定的，第0个为邀请者
                ImageDisplayUtils.getInstance().displayImage(userHeadImg, voiceCommunicationMemberList.get(0).getHeadImageUrl(), R.drawable.icon_person_default);
                //ImageDisplayUtils.getInstance().displayImage(userHeadImg, APIUri.getUserIconUrl(this, infoBean.getUserId()), R.drawable.icon_person_default);
                userNameTv.setText(voiceCommunicationMemberList.get(0).getUserName());
                if (voiceCommunicationMemberList.size() <= 5) {
                    communicationMembersFirstRecyclerview.setAdapter(new VoiceCommunicationMemberAdapter(ChannelVoiceCommunicationActivity.this, voiceCommunicationMemberList, 3));
                } else if (voiceCommunicationMemberList.size() <= 9) {
                    List<VoiceCommunicationJoinChannelInfoBean> list1 = voiceCommunicationMemberList.subList(0, 5);
                    List<VoiceCommunicationJoinChannelInfoBean> list2 = voiceCommunicationMemberList.subList(5, voiceCommunicationMemberList.size());
                    communicationMembersFirstRecyclerview.setAdapter(new VoiceCommunicationMemberAdapter(ChannelVoiceCommunicationActivity.this, list1, 3));
                    communicationMemberSecondRecyclerview.setAdapter(new VoiceCommunicationMemberAdapter(ChannelVoiceCommunicationActivity.this, list2, 3));
                }
            }
        }
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
        voiceCommunicationMemberAdapterFirst = new VoiceCommunicationMemberAdapter(this, voiceCommunicationMemberList1, 0);
        voiceCommunicationMemberAdapterSecond = new VoiceCommunicationMemberAdapter(this, voiceCommunicationMemberList2, 0);
        //初始化声网的callBacks
        initAgoraCallbacks();
        //响铃控制
        mediaPlayerManagerUtils = MediaPlayerManagerUtils.getManager();
        mediaPlayerManagerUtils.setMediaPlayerLooping(true);
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
                    voiceCommunicationUtils.setEncryptionSecret(agoraChannelId);
                    getChannelInfoByChannelId(agoraChannelId);
                    break;
                default:
                    break;
            }
        }
        //如果是来自小窗口，则取得已经通话的时长，继续计时
//        if (getIntent().getLongExtra(VOICE_TIME, 0) > 0) {
        //通话相差时间
        long duration = System.currentTimeMillis() - voiceCommunicationUtils.getConnectStartTime();
        communicationTimeChronometer.setBase(SystemClock.elapsedRealtime() - duration);
        communicationTimeChronometer.start();
//        }
//        dragLocalVideoView();
    }

    /**
     * 分解通话成员，小于等于5人和多于5人时list有所不同为了适应通话人数两行且居中的UI
     */
    private void handleVoiceCommunicationMemberList() {
        if (voiceCommunicationMemberList != null) {
            if (voiceCommunicationMemberList.size() <= 5) {
                voiceCommunicationMemberList1 = voiceCommunicationMemberList;
                voiceCommunicationMemberList2.clear();
            } else if (voiceCommunicationMemberList.size() <= 9) {
                voiceCommunicationMemberList1 = voiceCommunicationMemberList.subList(0, 5);
                voiceCommunicationMemberList2 = voiceCommunicationMemberList.subList(5, voiceCommunicationMemberList.size());
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
     * 初始化功能模块的初始值
     * 如是否免提，是否静音
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
        Log.d("zhang", "createChannel: ");
        if (NetUtils.isNetworkConnected(this)) {
            try {
                JSONArray jsonArray = new JSONArray();
                for (int i = 0; i < voiceCommunicationMemberList.size(); i++) {
                    JSONObject jsonObjectUserInfo = new JSONObject();
                    jsonObjectUserInfo.put("id", voiceCommunicationMemberList.get(i).getUserId());
                    jsonObjectUserInfo.put("name", voiceCommunicationMemberList.get(i).getUserName());
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
    private void initCommunicationViews(int state) {
        if (state == EXCEPTION_STATE) {
            finish();
        }
        layoutState = state;
        voiceCommunicationUtils.setLayoutState(layoutState);
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
    //多人通话中，收到refuse消息，则弹出相关提示，并检查是否应该finish，收到destroy消息则改变状态，并结束通话
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onReceiveVoiceOrVideoCall(final GetVoiceAndVideoResult getVoiceAndVideoResult) {

        CustomProtocol customProtocol = new CustomProtocol(getVoiceAndVideoResult.getContextParamsSchema());
        String cmd = customProtocol.getParamMap().get("cmd");
        if (!StringUtils.isBlank(cmd) && getVoiceAndVideoResult.getContextParamsRoom().equals(agoraChannelId)) {
            String uid = customProtocol.getParamMap().get("uid");
            if (cmd.equals("destroy")) {
                changeUserConnectStateByUid(VoiceCommunicationJoinChannelInfoBean.CONNECT_STATE_LEAVE, uid);
                remindEmmServerLeaveChannel(agoraChannelId);
                voiceCommunicationUtils.setCommunicationState(COMMUNICATION_STATE_OVER);
                SuspensionWindowManagerUtils.getInstance().hideCommunicationSmallWindow();
                finish();
            } else if (cmd.equals("refuse")) {
                if (layoutState == COMMUNICATION_LAYOUT_STATE) {
                    ToastUtils.show(ContactUserCacheUtils.getUserName(uid) + getString(R.string.communication_has_leave));
                } else if (layoutState == INVITER_LAYOUT_STATE) {
                    //拨打方
                    ToastUtils.show(ContactUserCacheUtils.getUserName(uid) + getString(R.string.meeting_has_refused));
                } else if (layoutState == INVITEE_LAYOUT_STATE &&
                        answerPhoneImg.getVisibility() != View.VISIBLE) {
                    //接听方
                    ToastUtils.show(ContactUserCacheUtils.getUserName(uid) + getString(R.string.meeting_has_refused));
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
        if (voiceCommunicationMemberList != null && voiceCommunicationMemberList.size() > 0) {
            int waitAndCommunicationSize = 0;
            for (int i = 0; i < voiceCommunicationMemberList.size(); i++) {
                if (voiceCommunicationMemberList.get(i).getConnectState() == VoiceCommunicationJoinChannelInfoBean.CONNECT_STATE_CONNECTED ||
                        voiceCommunicationMemberList.get(i).getConnectState() == VoiceCommunicationJoinChannelInfoBean.CONNECT_STATE_INIT) {
                    waitAndCommunicationSize = waitAndCommunicationSize + 1;
                }
            }
            if (waitAndCommunicationSize < 2) {
                voiceCommunicationUtils.setCommunicationState(COMMUNICATION_STATE_OVER);
                Log.d("zhang", "COMMUNICATION_STATE_OVER: 000000 ");
                refuseOrLeaveChannel(COMMUNICATION_LEAVE);
                return true;
            }
        }
        return false;
    }

    /**
     * 初始化回调
     */
    private void initAgoraCallbacks() {
        voiceCommunicationUtils.setOnVoiceCommunicationCallbacks(new OnVoiceCommunicationCallbacksImpl() {
            @Override
            public void onUserOffline(int uid, int reason) {
                userCount = userCount - 1;
                if (userCount < 2) {
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
            public void onUserJoined(int uid, int elapsed) {
                for (int i = 0; i < voiceCommunicationMemberList.size(); i++) {
                    if (voiceCommunicationMemberList.get(i).getAgoraUid() == uid) {
                        voiceCommunicationMemberList.get(i).setConnectState(VoiceCommunicationJoinChannelInfoBean.CONNECT_STATE_CONNECTED);
                    }
                }
                userCount = userCount + 1;
                if (userCount >= 2) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            voiceCommunicationUtils.setCommunicationState(COMMUNICATION_STATE_ING);
                            Log.d("zhang", "onUserJoined run: layoutState = " + layoutState);
                            layoutState = COMMUNICATION_LAYOUT_STATE;
                            voiceCommunicationUtils.setLayoutState(layoutState);
                            changeFunctionState(COMMUNICATION_LAYOUT_STATE);
                            communicationTimeChronometer.setBase(SystemClock.elapsedRealtime());
                            communicationTimeChronometer.start();
                            voiceCommunicationUtils.setConnectStartTime(System.currentTimeMillis());
                            refreshCommunicationMemberAdapter();
                        }
                    });
                }
            }

            @Override
            public void onJoinChannelSuccess(final String channel, final int uid, int elapsed) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        changeUserConnectStateByAgoraUid(VoiceCommunicationJoinChannelInfoBean.CONNECT_STATE_CONNECTED, uid);
                        remindEmmServerJoinChannel(channel);
                    }
                });
            }

            @Override
            public void onRejoinChannelSuccess(String channel, int uid, int elapsed) {
                changeUserConnectStateByAgoraUid(VoiceCommunicationJoinChannelInfoBean.CONNECT_STATE_CONNECTED, uid);
                userCount = userCount + 1;
                remindEmmServerJoinChannel(channel);
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
                        //屏蔽视频
//                        setupRemoteVideo(uid);
                    }
                });
            }

            @Override
            public void onCountDownTimerFinish() {
                //如果是邀请或被邀请状态，倒计时结束时挂断电话
                if (layoutState == INVITEE_LAYOUT_STATE || layoutState == INVITER_LAYOUT_STATE) {
                    isLeaveChannel = true;
                    refuseOrLeaveChannel(COMMUNICATION_REFUSE);
                }
            }
        });
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
                if (layoutState == COMMUNICATION_LAYOUT_STATE && userCount < 2) {
                    voiceCommunicationUtils.setCommunicationState(COMMUNICATION_STATE_OVER);
                    Log.d("zhang", "COMMUNICATION_STATE_OVER: 22222222 ");
                    refuseOrLeaveChannel(COMMUNICATION_REFUSE);
                }
            }
        };
        countDownOnlyOneConnectLeftTimer.start();
    }

    /**
     * 处理声网的异常，不能通过socket处理的异常都通过这个方法来处理，依赖声网
     */
    private void agoraException() {
        voiceCommunicationUtils.setCommunicationState(COMMUNICATION_STATE_OVER);
        Log.d("zhang", "COMMUNICATION_STATE_OVER: 33333333 ");
        voiceCommunicationUtils.destroy();
        SuspensionWindowManagerUtils.getInstance().hideCommunicationSmallWindow();
        finish();
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
                    if (connectStateConnected > 1) {
                        voiceCommunicationMemberList.remove(i);
                    }
                    break;
                }
            }
            refreshCommunicationMemberAdapter();
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
                    if (connectStateConnected > 1) {
                        voiceCommunicationMemberList.remove(i);
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
        voiceCommunicationUtils.getRtcEngine().setupLocalVideo(new VideoCanvas(agoraLocalView, VideoCanvas.RENDER_MODE_HIDDEN, 0));
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
        voiceCommunicationUtils.getRtcEngine().setupRemoteVideo(new VideoCanvas(agoraRemoteView, VideoCanvas.RENDER_MODE_HIDDEN, uid));
        agoraRemoteView.setTag(uid);
    }

    /**
     * 刷新成员adapter，有人加入，退出，声音变化都通过这个方法来刷新
     */
    private void refreshCommunicationMemberAdapter() {
        handleVoiceCommunicationMemberList();
        if (voiceCommunicationMemberList != null) {
            if (voiceCommunicationMemberList.size() <= 5) {
                if (voiceCommunicationMemberAdapterFirst != null) {
                    voiceCommunicationMemberAdapterFirst.setMemberDataAndRefresh(voiceCommunicationMemberList1, 1);
                }
            } else if (voiceCommunicationMemberList.size() <= 9) {
                if (voiceCommunicationMemberAdapterFirst != null) {
                    voiceCommunicationMemberAdapterFirst.setMemberDataAndRefresh(voiceCommunicationMemberList1, 1);
                }
                if (voiceCommunicationMemberAdapterSecond != null) {
                    voiceCommunicationMemberAdapterSecond.setMemberDataAndRefresh(voiceCommunicationMemberList2, 2);
                }
            }
        }

    }

    /**
     * 通知EmmServer用户加入频道
     *
     * @param agoraChannelId
     */
    private void remindEmmServerJoinChannel(String agoraChannelId) {
        if (NetUtils.isNetworkConnected(this)) {
            apiService.remindServerJoinChannelSuccess(agoraChannelId);
        }
    }

    /**
     * 通知EmmServer用户离开频道
     *
     * @param agoraChannelId
     */
    private void remindEmmServerLeaveChannel(String agoraChannelId) {
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
                voiceCommunicationUtils.setCommunicationState(COMMUNICATION_STATE_ING);
                initCommunicationViews(COMMUNICATION_LAYOUT_STATE);
                communicationTimeChronometer.setBase(SystemClock.elapsedRealtime());
                communicationTimeChronometer.start();
                voiceCommunicationUtils.setConnectStartTime(System.currentTimeMillis());
                voiceCommunicationUtils.joinChannel(inviteeInfoBean.getToken(),
                        agoraChannelId, inviteeInfoBean.getUserId(), inviteeInfoBean.getAgoraUid());
                break;
            case R.id.ll_video_hung_up:
            case R.id.img_hung_up:
                if (answerPhoneImg.getVisibility() == View.VISIBLE) {
                    refuseOrLeaveChannel(COMMUNICATION_REFUSE);
                } else {
                    refuseOrLeaveChannel(COMMUNICATION_LEAVE);
                }
                voiceCommunicationUtils.setCommunicationState(COMMUNICATION_STATE_OVER);
                Log.d("zhang", "COMMUNICATION_STATE_OVER: 4444444 ");
                break;
            case R.id.img_voice_communication_pack_up:
                pickUpVoiceCommunication();
                break;
            case R.id.ll_video_switch_camera:
                LogUtils.YfcDebug("转换摄像头");
                voiceCommunicationUtils.switchCamera();
                break;
            case R.id.ll_video_answer_phone:
                LogUtils.YfcDebug("接听视频电话");
                setupLocalVideo();
                initCommunicationViews(COMMUNICATION_LAYOUT_STATE);
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
                remindEmmServerLeaveChannel(agoraChannelId);
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
        saveCommunicationData();
        if (Build.VERSION.SDK_INT >= 23) {
            if (Settings.canDrawOverlays(this)) {
                Log.d("zhang", "pickUpVoiceCommunication: ");
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
        } else {
            SuspensionWindowManagerUtils.getInstance().showCommunicationSmallWindow(this, ResolutionUtils.getWidth(this),
                    Long.parseLong(TimeUtils.getChronometerSeconds(communicationTimeChronometer.getText().toString())));
            finish();
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
     * 进入小窗状态，保存状态
     */
    private void saveCommunicationData() {
        voiceCommunicationUtils.setLayoutState(layoutState);
        Log.d("zhang", "saveCommunicationData: layoutState = " + layoutState);
        voiceCommunicationUtils.setCommunicationState(voiceCommunicationUtils.getCommunicationState());
        voiceCommunicationUtils.setAgoraChannelId(agoraChannelId);
        voiceCommunicationUtils.setCommunicationType(communicationType);
        voiceCommunicationUtils.setVoiceCommunicationMemberList(voiceCommunicationMemberList);
        voiceCommunicationUtils.setInviteeInfoBean(inviteeInfoBean);
        voiceCommunicationUtils.setUserCount(userCount);
        voiceCommunicationUtils.setCloudPlusChannelId(cloudPlusChannelId);
    }

    @Override
    protected void onPause() {
        super.onPause();
        //已接通，停止音乐，关闭activity，打开小窗口
        if (voiceCommunicationUtils.getCommunicationState() == COMMUNICATION_STATE_ING) {
            if (mediaPlayerManagerUtils != null) {
                mediaPlayerManagerUtils.stop();
            }
        }

        Log.d("zhang", "onPause: layoutState = " + layoutState);
        Log.d("zhang", "onPause: voiceCommunicationUtils.getCommunicationState() = " + voiceCommunicationUtils.getCommunicationState());
        if (voiceCommunicationUtils.getCommunicationState() != COMMUNICATION_STATE_OVER && voiceCommunicationUtils.getCommunicationState() != -1) {
            if (PermissionRequestManagerUtils.getInstance().isHasPermission(this, Permissions.RECORD_AUDIO)) {
                pickUpVoiceCommunication();
            }
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        NotifyUtil.deleteNotify(this);
    }

    @Override
    protected void onStop() {
        super.onStop();
        NotifyUtil.sendNotifyMsg(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (countDownOnlyOneConnectLeftTimer != null) {
            countDownOnlyOneConnectLeftTimer.cancel();
            countDownOnlyOneConnectLeftTimer = null;
        }
        //恢复状态
        if (voiceCommunicationUtils.getCommunicationState() == COMMUNICATION_STATE_OVER) {
            voiceCommunicationUtils.setCommunicationState(-1);
        }
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
        ImageDisplayUtils.getInstance().displayImage(userHeadImg, infoBean.getHeadImageUrl(), R.drawable.icon_person_default);
//        ImageDisplayUtils.getInstance().displayImage(userHeadImg, APIUri.getUserIconUrl(this, infoBean.getUserId()), R.drawable.icon_person_default);
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
     * 获取通信类型，默认是语音通话
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
     *
     * @return
     */
    private String getSchema(String cmd, String channelId, String roomId) {
        return "ecc-cloudplus-cmd://voice_channel?cmd=" + cmd + "&channelid=" + channelId + "&roomid=" + roomId + "&uid=" + BaseApplication.getInstance().getUid();
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
     * 向socket发送指令消息
     *
     * @param commandType
     */
    private void sendCommunicationCommand(String commandType) {
        WSAPIService.getInstance().sendStartVoiceAndVideoCallMessage(cloudPlusChannelId, agoraChannelId,
                getSchema(commandType, cloudPlusChannelId, agoraChannelId), getCommunicationType(), getUidArray(voiceCommunicationMemberList), getActionByCommandType(commandType));
    }

    /**
     * 根据命令类型获取action类型
     *
     * @param commandType
     * @return
     */
    private String getActionByCommandType(String commandType) {
        switch (commandType) {
            case "invite":
                return Constant.VIDEO_CALL_INVITE;
            case "refuse":
                return Constant.VIDEO_CALL_REFUSE;
            case "destroy":
                return Constant.VIDEO_CALL_HANG_UP;
        }
        return "";
    }

    /**
     * 拒绝之后的后续逻辑处理
     */
    private void afterRefuse() {
        voiceCommunicationUtils.destroy();
        if (!isLeaveChannel) {
            sendCommunicationCommand("refuse");
            isLeaveChannel = true;
        }
        SuspensionWindowManagerUtils.getInstance().hideCommunicationSmallWindow();
        if (mediaPlayerManagerUtils != null) {
            mediaPlayerManagerUtils.stop();
        }
        Log.d("zhang", "afterRefuse: 7777777777777");
        voiceCommunicationUtils.setCommunicationState(COMMUNICATION_STATE_OVER);
        finish();
    }

    /**
     * 销毁之后的逻辑处理
     */
    private void afterLeave() {
        voiceCommunicationUtils.destroy();
        if (layoutState == COMMUNICATION_LAYOUT_STATE && userCount >= 2) {
            if (!isLeaveChannel) {
                sendCommunicationCommand("refuse");
                isLeaveChannel = true;
            }
        } else if (userCount < 2) {
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
        voiceCommunicationUtils.setCommunicationState(COMMUNICATION_STATE_OVER);
        finish();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        Log.d("zhang", "onNewIntent: ");
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
            } else {
                finish();
            }
            voiceCommunicationMemberList.clear();
            voiceCommunicationMemberList.addAll(getVoiceCommunicationResult.getVoiceCommunicationJoinChannelInfoBeanList());
            sendCommunicationCommand("invite");
            refreshCommunicationMemberAdapter();
        }

        @Override
        public void returnGetVoiceCommunicationResultFail(String error, int errorCode) {
            WebServiceMiddleUtils.hand(ChannelVoiceCommunicationActivity.this, error, errorCode);
            finish();
        }

        @Override
        public void returnGetVoiceCommunicationChannelInfoSuccess(GetVoiceCommunicationResult getVoiceCommunicationResult) {
            agoraChannelId = getVoiceCommunicationResult.getChannelId();
            setInviterInfo(getVoiceCommunicationResult);
            voiceCommunicationMemberList.clear();
            voiceCommunicationMemberList.addAll(getVoiceCommunicationResult.getVoiceCommunicationJoinChannelInfoBeanList());
            inviteeInfoBean = getMyCommunicationInfoBean(getVoiceCommunicationResult);
            if (voiceCommunicationMemberList.size() <= 5) {
                communicationMembersFirstRecyclerview.setAdapter(new VoiceCommunicationMemberAdapter(ChannelVoiceCommunicationActivity.this, voiceCommunicationMemberList, 3));
            } else if (voiceCommunicationMemberList.size() <= 9) {
                List<VoiceCommunicationJoinChannelInfoBean> list1 = voiceCommunicationMemberList.subList(0, 5);
                List<VoiceCommunicationJoinChannelInfoBean> list2 = voiceCommunicationMemberList.subList(5, voiceCommunicationMemberList.size());
                communicationMembersFirstRecyclerview.setAdapter(new VoiceCommunicationMemberAdapter(ChannelVoiceCommunicationActivity.this, list1, 3));
                communicationMemberSecondRecyclerview.setAdapter(new VoiceCommunicationMemberAdapter(ChannelVoiceCommunicationActivity.this, list2, 3));
            }
            refreshCommunicationMemberAdapter();
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

    }
}
