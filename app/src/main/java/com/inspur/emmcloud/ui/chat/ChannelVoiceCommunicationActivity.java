package com.inspur.emmcloud.ui.chat;

import android.os.SystemClock;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Chronometer;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.inspur.emmcloud.BaseActivity;
import com.inspur.emmcloud.MyApplication;
import com.inspur.emmcloud.R;
import com.inspur.emmcloud.adapter.VoiceCommunicationMemberAdapter;
import com.inspur.emmcloud.api.APIInterfaceInstance;
import com.inspur.emmcloud.api.apiservice.ChatAPIService;
import com.inspur.emmcloud.baselib.util.DensityUtil;
import com.inspur.emmcloud.baselib.util.ResolutionUtils;
import com.inspur.emmcloud.bean.chat.GetVoiceCommunicationResult;
import com.inspur.emmcloud.bean.chat.VoiceCommunicationAudioVolumeInfo;
import com.inspur.emmcloud.bean.chat.VoiceCommunicationJoinChannelInfoBean;
import com.inspur.emmcloud.bean.system.GetBoolenResult;
import com.inspur.emmcloud.util.privates.ImageDisplayUtils;
import com.inspur.emmcloud.util.privates.MediaPlayerManagerUtils;
import com.inspur.emmcloud.util.privates.NetUtils;
import com.inspur.emmcloud.util.privates.SuspensionWindowManagerUtils;
import com.inspur.emmcloud.util.privates.TimeUtils;
import com.inspur.emmcloud.util.privates.VoiceCommunicationUtils;
import com.inspur.emmcloud.util.privates.WebServiceMiddleUtils;
import com.inspur.emmcloud.widget.CircleTextImageView;
import com.inspur.emmcloud.widget.ECMSpaceItemDecoration;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by yufuchang on 2018/8/14.
 */
public class ChannelVoiceCommunicationActivity extends BaseActivity {
    public static final String VOICE_COMMUNICATION_STATE = "voice_communication_state";//传递页面布局样式的
    public static final String VOICE_TIME = "voice_time";
    public static final String SCREEN_SIZE = "screen_size";
    public static final int INVITER_LAYOUT_STATE = 0;//邀请人状态布局
    public static final int INVITEE_LAYOUT_STATE = 1;//被邀请人状态布局
    public static final int COMMUNICATION_LAYOUT_STATE = 2;//通话中布局状态
    public static final int COME_BACK_FROM_SERVICE = 3;//预留从小窗口回到聊天页面的状态
    private static final int EXCEPTION_STATE = -1;
    private static int STATE = -1;
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
    private ChatAPIService apiService;
    private List<VoiceCommunicationJoinChannelInfoBean> voiceCommunicationUserInfoBeanList = new ArrayList<>();
    private String channelId = "";//声网的channelId
    private List<VoiceCommunicationJoinChannelInfoBean> voiceCommunicationMemberList = new ArrayList<>();
    private VoiceCommunicationJoinChannelInfoBean inviteeInfoBean;
    private int userCount = 1;
    private VoiceCommunicationMemberAdapter voiceCommunicationMemberAdapterFirst;
    private VoiceCommunicationMemberAdapter voiceCommunicationMemberAdapterSecond;
    private MediaPlayerManagerUtils mediaPlayerManagerUtils;
    private VoiceCommunicationUtils voiceCommunicationUtils;


    @Override
    public void onCreate() {
        ButterKnife.bind(this);
        voiceCommunicationUserInfoBeanList = (List<VoiceCommunicationJoinChannelInfoBean>) getIntent().getSerializableExtra("userList");
        voiceCommunicationUtils = VoiceCommunicationUtils.getVoiceCommunicationUtils(this);
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
            channelId = voiceCommunicationUtils.getChannelId();
            voiceCommunicationMemberList = voiceCommunicationUtils.getVoiceCommunicationMemberList();
            inviteeInfoBean = voiceCommunicationUtils.getInviteeInfoBean();
            userCount = voiceCommunicationUtils.getUserCount();
        }
    }

    /**
     * 初始化Views
     */
    private void initViews() {
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
        LinearLayoutManager layoutManager2 = new LinearLayoutManager(this);
        layoutManager2.setOrientation(LinearLayoutManager.HORIZONTAL);
        secondRecyclerview.setLayoutManager(layoutManager2);
        secondRecyclerview.addItemDecoration(new ECMSpaceItemDecoration(DensityUtil.dip2px(this, 8)));
        LinearLayoutManager layoutManagerMemebersFirst = new LinearLayoutManager(this);
        layoutManagerMemebersFirst.setOrientation(LinearLayoutManager.HORIZONTAL);
        communicationMembersFirstRecyclerview.addItemDecoration(new ECMSpaceItemDecoration(DensityUtil.dip2px(this, 8)));
        communicationMembersFirstRecyclerview.setLayoutManager(layoutManagerMemebersFirst);
        LinearLayoutManager layoutManagerMembersSecond = new LinearLayoutManager(this);
        layoutManagerMembersSecond.setOrientation(LinearLayoutManager.HORIZONTAL);
        communicationMemberSecondRecyclerview.addItemDecoration(new ECMSpaceItemDecoration(DensityUtil.dip2px(this, 8)));
        communicationMemberSecondRecyclerview.setLayoutManager(layoutManagerMembersSecond);
        initCommunicationViewsAndMusicByState(STATE);
        initFunctionState();
        switch (STATE) {
            case INVITER_LAYOUT_STATE:
                if (voiceCommunicationUserInfoBeanList.size() <= 5) {
                    firstRecyclerview.setAdapter(voiceCommunicationMemberAdapterFirst);
                    voiceCommunicationMemberAdapterFirst.setMemberDataAndRefresh(voiceCommunicationUserInfoBeanList, 1);
                } else if (voiceCommunicationUserInfoBeanList.size() <= 9) {
                    List<VoiceCommunicationJoinChannelInfoBean> list1 = voiceCommunicationUserInfoBeanList.subList(0, 5);
                    List<VoiceCommunicationJoinChannelInfoBean> list2 = voiceCommunicationUserInfoBeanList.subList(5, voiceCommunicationUserInfoBeanList.size());
                    firstRecyclerview.setAdapter(voiceCommunicationMemberAdapterFirst);
                    secondRecyclerview.setAdapter(voiceCommunicationMemberAdapterSecond);
                    voiceCommunicationMemberAdapterFirst.setMemberDataAndRefresh(list1, 1);
                    voiceCommunicationMemberAdapterSecond.setMemberDataAndRefresh(list2, 2);
                }
                createChannel();
                break;
            case INVITEE_LAYOUT_STATE:
                String channelId = getIntent().getStringExtra("channelId");
                voiceCommunicationUtils.setEncryptionSecret(channelId);
                getChannelInfoByChannelId(channelId);
                break;
        }
        if (getIntent().getLongExtra(VOICE_TIME, 0) > 0) {
            communicationTimeChronometer.setBase(SystemClock.elapsedRealtime() - getIntent().getLongExtra(VOICE_TIME, 0) * 1000);
            communicationTimeChronometer.start();
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
     * 通过channelId获取Channel信息
     *
     * @param channelId
     */
    private void getChannelInfoByChannelId(String channelId) {
        if (NetUtils.isNetworkConnected(this)) {
            apiService.getAgoraChannelInfo(channelId);
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
        inviteeLinearLayout.setVisibility(state == INVITEE_LAYOUT_STATE ? View.VISIBLE : View.GONE);
        inviteMemebersGroupLinearLayout.setVisibility((state == INVITER_LAYOUT_STATE || state == COMMUNICATION_LAYOUT_STATE) ? View.VISIBLE : View.GONE);
        communicationMembersLinearLayout.setVisibility(state == INVITEE_LAYOUT_STATE ? View.VISIBLE : View.GONE);
        functionLinearLayout.setVisibility((state == INVITER_LAYOUT_STATE || state == COMMUNICATION_LAYOUT_STATE) ? View.VISIBLE : View.GONE);
        communicationStateTv.setVisibility((state == INVITER_LAYOUT_STATE || state == COMMUNICATION_LAYOUT_STATE) ? View.VISIBLE : View.GONE);
        communicationTimeChronometer.setVisibility(state == COMMUNICATION_LAYOUT_STATE ? View.VISIBLE : View.GONE);
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

        //启用悬浮窗打开这里
        packUpImg.setVisibility(state == COMMUNICATION_LAYOUT_STATE ? View.VISIBLE : View.GONE);

        communicationStateTv.setText(state == INVITER_LAYOUT_STATE ? getString(R.string.voice_communication_dialog) : (state == INVITEE_LAYOUT_STATE ? getString(R.string.voice_communication_waitting_answer) : (state == COMMUNICATION_LAYOUT_STATE ? getString(R.string.voice_communicaiton_watting_talking) : "")));
        if (state == COMMUNICATION_LAYOUT_STATE) {
            if (voiceCommunicationMemberList == null) {
                return;
            }
            if (voiceCommunicationMemberList.size() <= 5) {
                firstRecyclerview.setAdapter(voiceCommunicationMemberAdapterFirst);
                voiceCommunicationMemberAdapterFirst.setMemberDataAndRefresh(voiceCommunicationMemberList, 1);
            } else if (voiceCommunicationMemberList.size() <= 9) {
                List<VoiceCommunicationJoinChannelInfoBean> list1 = voiceCommunicationMemberList.subList(0, 5);
                List<VoiceCommunicationJoinChannelInfoBean> list2 = voiceCommunicationMemberList.subList(5, voiceCommunicationMemberList.size());
                firstRecyclerview.setAdapter(voiceCommunicationMemberAdapterFirst);
                firstRecyclerview.addItemDecoration(new ECMSpaceItemDecoration(DensityUtil.dip2px(this, 12)));
                secondRecyclerview.setAdapter(voiceCommunicationMemberAdapterSecond);
                secondRecyclerview.addItemDecoration(new ECMSpaceItemDecoration(DensityUtil.dip2px(this, 12)));
                voiceCommunicationMemberAdapterFirst.setMemberDataAndRefresh(list1, 1);
                voiceCommunicationMemberAdapterSecond.setMemberDataAndRefresh(list2, 2);
            }
        }
        //如果是通话中则“通话中”文字显示一下就不再显示
        communicationStateTv.setText(state == COMMUNICATION_LAYOUT_STATE ? "" : communicationStateTv.getText());
        if (state == INVITER_LAYOUT_STATE || state == INVITEE_LAYOUT_STATE) {
            mediaPlayerManagerUtils.play(R.raw.voice_communication_watting_answer, null);
        } else {
            mediaPlayerManagerUtils.stop();
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
                if (userCount < 2) {
                    leaveChannelSuccess(channelId);
                }
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
                            initCommunicationViewsAndMusicByState(COMMUNICATION_LAYOUT_STATE);
                            communicationTimeChronometer.setBase(SystemClock.elapsedRealtime());
                            communicationTimeChronometer.start();
                            refreshCommunicationMemberAdapter();
                        }
                    });
                }
            }

            @Override
            public void onJoinChannelSuccess(String channel, int uid, int elapsed) {
                joinChannelSuccess(channel);
            }

            @Override
            public void onRejoinChannelSuccess(String channel, int uid, int elapsed) {
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
        });
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
     * @param channelId
     */
    private void leaveChannelSuccess(String channelId) {
        if (NetUtils.isNetworkConnected(this)) {
            apiService.leaveAgoraChannel(channelId);
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
                        channelId, inviteeInfoBean.getUserId(), inviteeInfoBean.getAgoraUid());
                break;
            case R.id.img_hung_up:
                //先通知S，后退出声网
                if (answerPhoneImg.getVisibility() == View.VISIBLE) {
                    apiService.refuseAgoraChannel(channelId);
                } else {
                    apiService.leaveAgoraChannel(channelId);
                }
                break;
            case R.id.img_voice_communication_pack_up:
                saveCommunicationData();
//                createCommunicationService();
                SuspensionWindowManagerUtils.getInstance().showCommunicationSmallWindow(this, ResolutionUtils.getWidth(this), Long.parseLong(TimeUtils.getChronometerSeconds(communicationTimeChronometer)));
                finish();
                break;
            default:
                break;
        }
    }

    /**
     * 保存状态
     */
    private void saveCommunicationData() {
        voiceCommunicationUtils.setState(STATE);
        voiceCommunicationUtils.setVoiceCommunicationUserInfoBeanList(voiceCommunicationUserInfoBeanList);
        voiceCommunicationUtils.setChannelId(channelId);
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
        apiService.leaveAgoraChannel(channelId);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mediaPlayerManagerUtils.stop();
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

    class WebService extends APIInterfaceInstance {
        @Override
        public void returnGetVoiceCommunicationResultSuccess(GetVoiceCommunicationResult getVoiceCommunicationResult) {
            channelId = getVoiceCommunicationResult.getChannelId();
            VoiceCommunicationJoinChannelInfoBean voiceCommunicationJoinChannelInfoBean = getMyCommunicationInfoBean(getVoiceCommunicationResult);
            if (voiceCommunicationJoinChannelInfoBean != null) {
                voiceCommunicationUtils.setEncryptionSecret(channelId);
                voiceCommunicationUtils.joinChannel(voiceCommunicationJoinChannelInfoBean.getToken(),
                        getVoiceCommunicationResult.getChannelId(), voiceCommunicationJoinChannelInfoBean.getUserId(), voiceCommunicationJoinChannelInfoBean.getAgoraUid());
            }
            voiceCommunicationMemberList.addAll(getVoiceCommunicationResult.getVoiceCommunicationJoinChannelInfoBeanList());
        }

        @Override
        public void returnGetVoiceCommunicationResultFail(String error, int errorCode) {
            WebServiceMiddleUtils.hand(ChannelVoiceCommunicationActivity.this, error, errorCode);
        }

        @Override
        public void returnGetVoiceCommunicationChannelInfoSuccess(GetVoiceCommunicationResult getVoiceCommunicationResult) {
            channelId = getVoiceCommunicationResult.getChannelId();
            setInviterInfo(getVoiceCommunicationResult);
            voiceCommunicationMemberList.addAll(getVoiceCommunicationResult.getVoiceCommunicationJoinChannelInfoBeanList());
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
            voiceCommunicationUtils.destroy();
            finish();
        }

        @Override
        public void returnRefuseVoiceCommunicationChannelFail(String error, int errorCode) {
            voiceCommunicationUtils.destroy();
            finish();
        }

        @Override
        public void returnLeaveVoiceCommunicationChannelSuccess(GetBoolenResult getBoolenResult) {
            voiceCommunicationUtils.destroy();
            finish();
        }

        @Override
        public void returnLeaveVoiceCommunicationChannelFail(String error, int errorCode) {
            voiceCommunicationUtils.destroy();
            finish();
        }
    }
}
