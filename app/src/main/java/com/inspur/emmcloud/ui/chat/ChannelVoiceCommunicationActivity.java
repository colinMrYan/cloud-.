package com.inspur.emmcloud.ui.chat;

import android.content.Intent;
import android.os.Bundle;
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
import com.inspur.emmcloud.bean.chat.GetVoiceCommunicationResult;
import com.inspur.emmcloud.bean.chat.VoiceCommunicationAudioVolumeInfo;
import com.inspur.emmcloud.bean.chat.VoiceCommunicationJoinChannelInfoBean;
import com.inspur.emmcloud.bean.system.GetBoolenResult;
import com.inspur.emmcloud.service.VoiceHoldService;
import com.inspur.emmcloud.util.common.DensityUtil;
import com.inspur.emmcloud.util.common.LogUtils;
import com.inspur.emmcloud.util.common.NetUtils;
import com.inspur.emmcloud.util.common.StateBarUtils;
import com.inspur.emmcloud.util.privates.ImageDisplayUtils;
import com.inspur.emmcloud.util.privates.VoiceCommunicationUtils;
import com.inspur.emmcloud.util.privates.WebServiceMiddleUtils;
import com.inspur.emmcloud.widget.CircleTextImageView;
import com.inspur.emmcloud.widget.ECMSpaceItemDecoration;

import org.json.JSONArray;
import org.json.JSONObject;
import org.xutils.view.annotation.ContentView;
import org.xutils.view.annotation.ViewInject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by yufuchang on 2018/8/14.
 */
@ContentView(R.layout.activity_voice_channel)
public class ChannelVoiceCommunicationActivity extends BaseActivity{
    public static final String VOICE_COMMUNICATION_STATE = "voice_communication_state";//传递页面布局样式的
    public static final int INVITER_LAYOUT_STATE = 0;//邀请人状态布局
    public static final int INVITEE_LAYOUT_STATE = 1;//被邀请人状态布局
    public static final int COMMUNICATION_LAYOUT_STATE = 2;//通话中布局状态
    public static final int COME_BACK_FROM_SERVICE = 3;//预留从小窗口回到聊天页面的状态
    private static final int EXCEPTION_STATE = -1;
    private static int STATE = -1;
    @ViewInject(R.id.ll_voice_communication_invite)
    private LinearLayout linearLayoutInvitee;
    @ViewInject(R.id.img_user_head)
    private CircleTextImageView imgUserHead;
    @ViewInject(R.id.tv_user_name)
    private TextView tvUserName;
    @ViewInject(R.id.ll_voice_communication_invite_members)
    private LinearLayout linearLayoutInviteMemebersGroup;
    @ViewInject(R.id.recyclerview_voice_communication_first)
    private RecyclerView recyclerViewFirst;
    @ViewInject(R.id.recyclerview_voice_communication_second)
    private RecyclerView recyclerViewSecond;
    @ViewInject(R.id.ll_voice_communication_memebers)
    private LinearLayout linearLayoutCommunicationMembers;
    @ViewInject(R.id.recyclerview_voice_communication_memebers_first)
    private RecyclerView recyclerViewCommunicationMembersFirst;
    @ViewInject(R.id.recyclerview_voice_communication_memebers_second)
    private RecyclerView recyclerViewCommunicationMemeberSecond;
    @ViewInject(R.id.tv_voice_communication_state)
    private TextView tvCommunicationState;
    @ViewInject(R.id.tv_voice_communication_time)
    private Chronometer chronometerCommunicationTime;
    @ViewInject(R.id.ll_voice_communication_function_group)
    private LinearLayout linearLayoutFunction;
    @ViewInject(R.id.img_an_excuse)
    private ImageView imgExcuse;
    @ViewInject(R.id.tv_an_excuse)
    private TextView tvExcuse;
    @ViewInject(R.id.img_hands_free)
    private ImageView imgHandsFree;
    @ViewInject(R.id.tv_hands_free)
    private TextView tvHandsFree;
    @ViewInject(R.id.img_mute)
    private ImageView imgMute;
    @ViewInject(R.id.tv_mute)
    private TextView tvMute;
    @ViewInject(R.id.img_tran_video)
    private ImageView imgTranVideo;
    @ViewInject(R.id.tv_tran_video)
    private TextView tvTranVideo;
    @ViewInject(R.id.img_answer_the_phone)
    private ImageView imgAnswerPhone;
    @ViewInject(R.id.img_hung_up)
    private ImageView imgHungUp;
    @ViewInject(R.id.img_voice_communication_pack_up)
    private ImageView imgPackUp;
    private ChatAPIService apiService;
    private List<VoiceCommunicationJoinChannelInfoBean> voiceCommunicationUserInfoBeanList = new ArrayList<>();
    private String channelId = "";//声网的channelId
    private List<VoiceCommunicationJoinChannelInfoBean> voiceCommunicationMemberList = new ArrayList<>();
    private VoiceCommunicationJoinChannelInfoBean inviteeInfoBean;
    private int userCount = 1;
    private VoiceCommunicationUtils voiceCommunicationUtils;
    private VoiceCommunicationMemberAdapter voiceCommunicationMemberAdapterFirst;
    private VoiceCommunicationMemberAdapter voiceCommunicationMemberAdapterSecond;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        StateBarUtils.changeStateBarColor(this,R.color.content_bg);
        voiceCommunicationUserInfoBeanList = (List<VoiceCommunicationJoinChannelInfoBean>) getIntent().getSerializableExtra("userList");
        voiceCommunicationUtils = MyApplication.getInstance().getVoiceCommunicationUtils();
//        recoverData();
        initViews();
    }

    /**
     * 如果是从小窗口来的，则恢复通话数据
     */
    private void recoverData() {
        int state = getIntent().getIntExtra(VOICE_COMMUNICATION_STATE,EXCEPTION_STATE);
        if(state != EXCEPTION_STATE){
            STATE = voiceCommunicationUtils.getState();
            voiceCommunicationUserInfoBeanList = voiceCommunicationUtils.getVoiceCommunicationUserInfoBeanList();
            channelId = voiceCommunicationUtils.getChannelId();
            voiceCommunicationMemberList = voiceCommunicationUtils.getVoiceCommunicationMemberList();
            inviteeInfoBean = voiceCommunicationUtils.getInviteeInfoBean();
            userCount = voiceCommunicationUtils.getUserCount();
        }
    }

    /**
     * 创建通话小窗口
     */
    public void createCommunicationService(){
        Intent intent = new Intent(this,VoiceHoldService.class);
        startService(intent);
    }

    /**
     * 初始化Views
     */
    private void initViews() {
        apiService = new ChatAPIService(this);
        apiService.setAPIInterface(new WebService());
        voiceCommunicationMemberAdapterFirst = new VoiceCommunicationMemberAdapter(this,voiceCommunicationUserInfoBeanList,0);
        voiceCommunicationMemberAdapterSecond = new VoiceCommunicationMemberAdapter(this,voiceCommunicationUserInfoBeanList,0);
        initCallbacks();
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
        recyclerViewFirst.setLayoutManager(layoutManager);
        recyclerViewFirst.addItemDecoration(new ECMSpaceItemDecoration(DensityUtil.dip2px(this,8)));
        LinearLayoutManager layoutManager2 = new LinearLayoutManager(this);
        layoutManager2.setOrientation(LinearLayoutManager.HORIZONTAL);
        recyclerViewSecond.setLayoutManager(layoutManager2);
        recyclerViewSecond.addItemDecoration(new ECMSpaceItemDecoration(DensityUtil.dip2px(this,8)));
        LinearLayoutManager layoutManagerMemebersFirst = new LinearLayoutManager(this);
        layoutManagerMemebersFirst.setOrientation(LinearLayoutManager.HORIZONTAL);
        recyclerViewCommunicationMembersFirst.addItemDecoration(new ECMSpaceItemDecoration(DensityUtil.dip2px(this,8)));
        recyclerViewCommunicationMembersFirst.setLayoutManager(layoutManagerMemebersFirst);

        LinearLayoutManager layoutManagerMembersSecond = new LinearLayoutManager(this);
        layoutManagerMembersSecond.setOrientation(LinearLayoutManager.HORIZONTAL);
        recyclerViewCommunicationMemeberSecond.addItemDecoration(new ECMSpaceItemDecoration(DensityUtil.dip2px(this,8)));
        recyclerViewCommunicationMemeberSecond.setLayoutManager(layoutManagerMembersSecond);
        int state = getIntent().getIntExtra(VOICE_COMMUNICATION_STATE,EXCEPTION_STATE);
        initCommunicationViewsVisibility(state);
        initFunctionState();
        switch (state){
            case INVITER_LAYOUT_STATE:
                if(voiceCommunicationUserInfoBeanList.size() <= 5){
                    recyclerViewFirst.setAdapter(voiceCommunicationMemberAdapterFirst);
                    voiceCommunicationMemberAdapterFirst.setMemberDataAndRefresh(voiceCommunicationUserInfoBeanList,1);
                }else if(voiceCommunicationUserInfoBeanList.size() <= 9){
                    List<VoiceCommunicationJoinChannelInfoBean> list1 = voiceCommunicationUserInfoBeanList.subList(0,5);
                    List<VoiceCommunicationJoinChannelInfoBean> list2 = voiceCommunicationUserInfoBeanList.subList(5,voiceCommunicationUserInfoBeanList.size());
                    recyclerViewFirst.setAdapter(voiceCommunicationMemberAdapterFirst);
                    recyclerViewSecond.setAdapter(voiceCommunicationMemberAdapterSecond);
                    voiceCommunicationMemberAdapterFirst.setMemberDataAndRefresh(list1,1);
                    voiceCommunicationMemberAdapterSecond.setMemberDataAndRefresh(list2,2);
                }
                createChannel();
                break;
            case INVITEE_LAYOUT_STATE:
                String channelId = getIntent().getStringExtra("channelId");
                voiceCommunicationUtils.setEncryptionSecret(channelId);
                getChannelInfoByChannelId(channelId);
                break;
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
     * @param channelId
     */
    private void getChannelInfoByChannelId(String channelId) {
        if(NetUtils.isNetworkConnected(this)){
            apiService.getAgoraChannelInfo(channelId);
        }
    }

    /**
     * 创建频道
     */
    private void createChannel(){
        if(NetUtils.isNetworkConnected(this)){
            try {
                JSONArray jsonArray = new JSONArray();
                for (int i = 0; i < voiceCommunicationUserInfoBeanList.size(); i++) {
                    JSONObject jsonObjectUserInfo = new JSONObject();
                    jsonObjectUserInfo.put("id",voiceCommunicationUserInfoBeanList.get(i).getUserId());
                    jsonObjectUserInfo.put("name",voiceCommunicationUserInfoBeanList.get(i).getUserName());
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
     * @param state
     */
    private void initCommunicationViewsVisibility(int state) {
        if(state == EXCEPTION_STATE){
            finish();
        }
        STATE = state;
        linearLayoutInvitee.setVisibility(state == INVITEE_LAYOUT_STATE?View.VISIBLE:View.GONE);
        linearLayoutInviteMemebersGroup.setVisibility((state == INVITER_LAYOUT_STATE || state == COMMUNICATION_LAYOUT_STATE)?View.VISIBLE:View.GONE);
        linearLayoutCommunicationMembers.setVisibility(state == INVITEE_LAYOUT_STATE?View.VISIBLE:View.GONE);
        linearLayoutFunction.setVisibility((state == INVITER_LAYOUT_STATE || state == COMMUNICATION_LAYOUT_STATE)? View.VISIBLE:View.GONE);
        tvCommunicationState.setVisibility((state == INVITER_LAYOUT_STATE || state == COMMUNICATION_LAYOUT_STATE)?View.VISIBLE:View.GONE);
        chronometerCommunicationTime.setVisibility(state == COMMUNICATION_LAYOUT_STATE ? View.VISIBLE:View.GONE);
        imgAnswerPhone.setVisibility((state == INVITEE_LAYOUT_STATE)?View.VISIBLE:View.GONE);

        int colorNormal = ContextCompat.getColor(this,R.color.voice_communication_function_default);
        int colorUnavailiable = ContextCompat.getColor(this,R.color.voice_communication_function_unavailiable_text);
        imgExcuse.setImageResource(state == COMMUNICATION_LAYOUT_STATE?R.drawable.icon_excuse_unselected:R.drawable.icon_excuse_unavailable);
        tvExcuse.setTextColor(state == COMMUNICATION_LAYOUT_STATE?colorNormal:colorUnavailiable);
        imgExcuse.setClickable(state == COMMUNICATION_LAYOUT_STATE?true:false);

        imgHandsFree.setImageResource(state == COMMUNICATION_LAYOUT_STATE?R.drawable.icon_hands_free_unselected:R.drawable.icon_hands_free_unavailable);
        tvHandsFree.setTextColor(state == COMMUNICATION_LAYOUT_STATE?colorNormal:colorUnavailiable);
        imgHandsFree.setClickable(state == COMMUNICATION_LAYOUT_STATE?true:false);

        imgMute.setImageResource(state == COMMUNICATION_LAYOUT_STATE?R.drawable.icon_mute_unselcected:R.drawable.icon_mute_unavaiable);
        tvMute.setTextColor(state == COMMUNICATION_LAYOUT_STATE?colorNormal:colorUnavailiable);
        imgMute.setClickable(state == COMMUNICATION_LAYOUT_STATE?true:false);

        tvCommunicationState.setText(state == INVITER_LAYOUT_STATE? "拨号中..." : (state == INVITEE_LAYOUT_STATE?"等待接听":(state == COMMUNICATION_LAYOUT_STATE?"通话中":"")));
        if(state == COMMUNICATION_LAYOUT_STATE){
            if(voiceCommunicationMemberList == null){
                return;
            }
            if(voiceCommunicationMemberList.size() <= 5){
                recyclerViewFirst.setAdapter(voiceCommunicationMemberAdapterFirst);
                voiceCommunicationMemberAdapterFirst.setMemberDataAndRefresh(voiceCommunicationMemberList,1);
            }else if(voiceCommunicationMemberList.size() <= 9){
                List<VoiceCommunicationJoinChannelInfoBean> list1 = voiceCommunicationMemberList.subList(0,5);
                List<VoiceCommunicationJoinChannelInfoBean> list2 = voiceCommunicationMemberList.subList(5,voiceCommunicationMemberList.size());
                recyclerViewFirst.setAdapter(voiceCommunicationMemberAdapterFirst);
                recyclerViewFirst.addItemDecoration(new ECMSpaceItemDecoration(DensityUtil.dip2px(this,12)));
                recyclerViewSecond.setAdapter(voiceCommunicationMemberAdapterSecond);
                recyclerViewSecond.addItemDecoration(new ECMSpaceItemDecoration(DensityUtil.dip2px(this,12)));
                voiceCommunicationMemberAdapterFirst.setMemberDataAndRefresh(list1,1);
                voiceCommunicationMemberAdapterSecond.setMemberDataAndRefresh(list2,2);
            }
        }
        //如果是通话中则“通话中”文字显示一下就不再显示
        tvCommunicationState.setText(state == COMMUNICATION_LAYOUT_STATE?"":tvCommunicationState.getText());
    }


    /**
     * 初始化回调
     */
    private void initCallbacks() {
        voiceCommunicationUtils.setOnVoiceCommunicationCallbacks(new OnVoiceCommunicationCallbacksImpl() {
            @Override
            public void onUserOffline(int uid, int reason) {
                LogUtils.YfcDebug("用户离开："+uid);
                LogUtils.YfcDebug("用户离开："+reason);
                userCount = userCount - 1;
                if(userCount < 2){
                    leaveChannelSuccess(channelId);
                }
            }

            @Override
            public void onUserJoined(int uid, int elapsed) {
                LogUtils.YfcDebug("用户加入："+uid);
                for (int i = 0; i < voiceCommunicationMemberList.size(); i++) {
                    if(voiceCommunicationMemberList.get(i).getAgoraUid() == uid){
                        voiceCommunicationMemberList.get(i).setUserState(1);
                    }
                }
                userCount = userCount + 1;
                if(userCount >= 2){
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            initCommunicationViewsVisibility(COMMUNICATION_LAYOUT_STATE);
                            chronometerCommunicationTime.setBase(SystemClock.elapsedRealtime());
                            chronometerCommunicationTime.start();
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
                LogUtils.YfcDebug("用户断线");
            }

            @Override
            public void onNetworkQuality(int uid, int txQuality, int rxQuality) {
                if(STATE == COMMUNICATION_LAYOUT_STATE){
                    tvCommunicationState.setText((uid == 0 && txQuality <= 2)?"当前通话质量不佳":"");
                }
            }

            @Override
            public void onAudioVolumeIndication(final VoiceCommunicationAudioVolumeInfo[] speakers, final int totalVolume) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if(speakers != null && speakers.length > 0){
                            for (int i = 0; i < speakers.length; i++) {
                                int agoraId = speakers[i].uid;
                                for (int j = 0; j < voiceCommunicationMemberList.size(); j++) {
                                    if(voiceCommunicationMemberList.get(j).getAgoraUid() == agoraId){
                                        voiceCommunicationMemberList.get(j).setVolume(speakers[i].volume);
                                    }
                                }
                            }
                            if(totalVolume == 0){
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
        if(voiceCommunicationMemberList != null){
            if(voiceCommunicationMemberList.size() <= 5){
                voiceCommunicationMemberAdapterFirst.setMemberDataAndRefresh(voiceCommunicationMemberList,1);
            }else if(voiceCommunicationMemberList.size() <= 9){
                List<VoiceCommunicationJoinChannelInfoBean> list1 = voiceCommunicationMemberList.subList(0,5);
                List<VoiceCommunicationJoinChannelInfoBean> list2 = voiceCommunicationMemberList.subList(5,voiceCommunicationMemberList.size());
                voiceCommunicationMemberAdapterFirst.setMemberDataAndRefresh(list1,1);
                voiceCommunicationMemberAdapterSecond.setMemberDataAndRefresh(list2,2);
            }
        }
    }

    /**
     * 加入频道
     * @param channel
     */
    private void joinChannelSuccess(String channel) {
        if(NetUtils.isNetworkConnected(this)){
            apiService.remindServerJoinChannelSuccess(channel);
        }
    }

    /**
     * 用户离开
     * @param channelId
     */
    private void leaveChannelSuccess(String channelId){
        if(NetUtils.isNetworkConnected(this)){
            apiService.leaveAgoraChannel(channelId);
        }
    }

    public void onClick(View view){
        switch (view.getId()){
            case R.id.img_an_excuse:
                switchFunctionViewUIState(imgExcuse,tvExcuse);
                voiceCommunicationUtils.muteLocalAudioStream(imgExcuse.isSelected());
                imgExcuse.setImageResource(imgExcuse.isSelected()?R.drawable.icon_excuse_selected:R.drawable.icon_excuse_unselected);
                break;
            case R.id.img_hands_free:
                switchFunctionViewUIState(imgHandsFree,tvHandsFree);
                voiceCommunicationUtils.onSwitchSpeakerphoneClicked(imgHandsFree.isSelected());
                imgHandsFree.setImageResource(imgHandsFree.isSelected()?R.drawable.icon_hands_free_selected:R.drawable.icon_hands_free_unselected);
                break;
            case R.id.img_mute:
                switchFunctionViewUIState(imgMute,tvMute);
                voiceCommunicationUtils.muteAllRemoteAudioStreams(imgMute.isSelected());
                imgMute.setImageResource(imgMute.isSelected()?R.drawable.icon_mute_selected:R.drawable.icon_mute_unselcected);
                break;
            case R.id.img_tran_video:
                switchFunctionViewUIState(imgTranVideo,tvTranVideo);
                imgTranVideo.setImageResource(imgTranVideo.isSelected()?R.drawable.icon_trans_video:R.drawable.icon_trans_video);
                break;
            case R.id.img_answer_the_phone:
                initCommunicationViewsVisibility(COMMUNICATION_LAYOUT_STATE);
                chronometerCommunicationTime.setBase(SystemClock.elapsedRealtime());
                chronometerCommunicationTime.start();
                voiceCommunicationUtils.joinChannel(inviteeInfoBean.getToken(),
                        channelId,inviteeInfoBean.getUserId(),inviteeInfoBean.getAgoraUid());
                break;
            case R.id.img_hung_up:
                //先通知S，后退出声网
                if(imgAnswerPhone.getVisibility() == View.VISIBLE){
                    apiService.refuseAgoraChannel(channelId);
                }else{
                    apiService.leaveAgoraChannel(channelId);
                }
                break;
            case R.id.img_voice_communication_pack_up:
//                Toast.makeText(this, "点击了最小化，是否有悬浮窗权限："+ AppUtils.getAppOps(this), Toast.LENGTH_SHORT).show();
//                saveCommunicationData();
//                createCommunicationService();
//                finish();
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
     * @param imageView
     * @param textView
     */
    private void switchFunctionViewUIState(ImageView imageView, TextView textView) {
        imageView.setSelected(imageView.isSelected()?false:true);
        textView.setTextColor(imageView.isSelected()?ContextCompat.getColor(this,R.color.voice_communication_function_select)
                :ContextCompat.getColor(this,R.color.voice_communication_function_default));
    }

    @Override
    public void onBackPressed() {
        //先通知S，后退出声网
        apiService.leaveAgoraChannel(channelId);
    }

    class WebService extends APIInterfaceInstance{
        @Override
        public void returnGetVoiceCommunicationResultSuccess(GetVoiceCommunicationResult getVoiceCommunicationResult) {
            channelId = getVoiceCommunicationResult.getChannelId();
            VoiceCommunicationJoinChannelInfoBean voiceCommunicationJoinChannelInfoBean = getMyCommunicationInfoBean(getVoiceCommunicationResult);
            if(voiceCommunicationJoinChannelInfoBean != null){
                voiceCommunicationUtils.setEncryptionSecret(channelId);
                voiceCommunicationUtils.joinChannel(voiceCommunicationJoinChannelInfoBean.getToken(),
                        getVoiceCommunicationResult.getChannelId(),voiceCommunicationJoinChannelInfoBean.getUserId(),voiceCommunicationJoinChannelInfoBean.getAgoraUid());
            }
            voiceCommunicationMemberList.addAll(getVoiceCommunicationResult.getVoiceCommunicationJoinChannelInfoBeanList());
        }

        @Override
        public void returnGetVoiceCommunicationResultFail(String error, int errorCode) {
            WebServiceMiddleUtils.hand(ChannelVoiceCommunicationActivity.this,error,errorCode);
        }

        @Override
        public void returnGetVoiceCommunicationChannelInfoSuccess(GetVoiceCommunicationResult getVoiceCommunicationResult) {
            channelId = getVoiceCommunicationResult.getChannelId();
            setInviterInfo(getVoiceCommunicationResult);
            voiceCommunicationMemberList.addAll(getVoiceCommunicationResult.getVoiceCommunicationJoinChannelInfoBeanList());
            for (int i = 0; i < getVoiceCommunicationResult.getVoiceCommunicationJoinChannelInfoBeanList().size(); i++) {
                VoiceCommunicationJoinChannelInfoBean voiceCommunicationJoinChannelInfoBean = getVoiceCommunicationResult.getVoiceCommunicationJoinChannelInfoBeanList().get(i);
                if(voiceCommunicationJoinChannelInfoBean.getUserId().equals(MyApplication.getInstance().getUid())){
                    inviteeInfoBean = voiceCommunicationJoinChannelInfoBean;
                }
            }
            if(voiceCommunicationMemberList.size() <= 5){
                recyclerViewCommunicationMembersFirst.setAdapter(new VoiceCommunicationMemberAdapter(ChannelVoiceCommunicationActivity.this,voiceCommunicationMemberList,3));
            }else if(voiceCommunicationMemberList.size() <= 9){
                List<VoiceCommunicationJoinChannelInfoBean> list1 = voiceCommunicationMemberList.subList(0,5);
                List<VoiceCommunicationJoinChannelInfoBean> list2 = voiceCommunicationMemberList.subList(5,voiceCommunicationMemberList.size());
                recyclerViewCommunicationMembersFirst.setAdapter(new VoiceCommunicationMemberAdapter(ChannelVoiceCommunicationActivity.this,list1,3));
                recyclerViewCommunicationMemeberSecond.setAdapter(new VoiceCommunicationMemberAdapter(ChannelVoiceCommunicationActivity.this,list2,3));
            }
        }

        @Override
        public void returnGetVoiceCommunicationChannelInfoFail(String error, int errorCode) {
            WebServiceMiddleUtils.hand(ChannelVoiceCommunicationActivity.this,error,errorCode);
        }

        @Override
        public void returnJoinVoiceCommunicationChannelSuccess(GetBoolenResult getBoolenResult) {
            LogUtils.YfcDebug("加入群组成功");
        }

        @Override
        public void returnJoinVoiceCommunicationChannelFail(String error,int errorCode) {
            LogUtils.YfcDebug("加入群组失败");
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

    /**
     * 设置邀请者信息
     * @param getVoiceCommunicationResult
     */
    private void setInviterInfo(GetVoiceCommunicationResult getVoiceCommunicationResult) {
        VoiceCommunicationJoinChannelInfoBean infoBean = getVoiceCommunicationResult.getVoiceCommunicationJoinChannelInfoBeanList().get(0);
        ImageDisplayUtils.getInstance().displayImage(imgUserHead,infoBean.getHeadImageUrl(),R.drawable.icon_person_default);
        tvUserName.setText(infoBean.getUserName());
    }

    /**
     * 获取自己的加入信息，包含token，uid等
     * @param getVoiceCommunicationResult
     * @return
     */
    private VoiceCommunicationJoinChannelInfoBean getMyCommunicationInfoBean(GetVoiceCommunicationResult getVoiceCommunicationResult) {
        List<VoiceCommunicationJoinChannelInfoBean> voiceCommunicationJoinChannelInfoBeanList = getVoiceCommunicationResult.getVoiceCommunicationJoinChannelInfoBeanList();
        for (int i = 0; i < voiceCommunicationJoinChannelInfoBeanList.size(); i++) {
            if(voiceCommunicationJoinChannelInfoBeanList.get(i).getUserId().equals(MyApplication.getInstance().getUid())){
                return voiceCommunicationJoinChannelInfoBeanList.get(i);
            }
        }
        return null;
    }
}
