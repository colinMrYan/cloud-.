package com.inspur.emmcloud.ui.chat;

import android.content.Intent;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Chronometer;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;
import com.inspur.emmcloud.BaseActivity;
import com.inspur.emmcloud.MyApplication;
import com.inspur.emmcloud.R;
import com.inspur.emmcloud.adapter.VoiceCommunicationMemberAdapter;
import com.inspur.emmcloud.api.APIInterfaceInstance;
import com.inspur.emmcloud.api.APIUri;
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
import com.inspur.emmcloud.util.common.ToastUtils;
import com.inspur.emmcloud.util.privates.AppUtils;
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
    public static boolean IS_EXCUSE_AVAILIABLE = true;//禁言可用状态标识
    public static boolean IS_HANDS_FREE_AVAILIABLE = true;//免提可用状态标识
    public static boolean IS_MUTE_AVAILIABLE = true;//静音可用状态标识
    private static final int EXCEPTION_STATE = -1;
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
    @ViewInject(R.id.recyclerview_voice_communication_memebers)
    private RecyclerView recyclerViewCommunicationMembers;
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
    private List<VoiceCommunicationJoinChannelInfoBean> voiceCommunicationUserInfoBeanList = new ArrayList<>();
    private ChatAPIService apiService;
    private String channelId = "";
    private List<VoiceCommunicationJoinChannelInfoBean> voiceCommunicationMemberList = new ArrayList<>();
    private VoiceCommunicationJoinChannelInfoBean inviteeInfoBean;
    private int userCount = 1;
    private VoiceCommunicationUtils voiceCommunicationUtils;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        StateBarUtils.changeStateBarColor(this,R.color.content_bg);
        voiceCommunicationUserInfoBeanList = (List<VoiceCommunicationJoinChannelInfoBean>) getIntent().getSerializableExtra("userList");
        voiceCommunicationUtils = MyApplication.getInstance().getVoiceCommunicationUtils();
        initViews();
//        createCommunicationService();
    }

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
        initCallbacks();
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
        recyclerViewFirst.setLayoutManager(layoutManager);
        recyclerViewFirst.addItemDecoration(new ECMSpaceItemDecoration(DensityUtil.dip2px(this,8)));
        LinearLayoutManager layoutManager2 = new LinearLayoutManager(this);
        layoutManager2.setOrientation(LinearLayoutManager.HORIZONTAL);
        recyclerViewSecond.setLayoutManager(layoutManager2);
        recyclerViewSecond.addItemDecoration(new ECMSpaceItemDecoration(DensityUtil.dip2px(this,8)));
        GridLayoutManager layoutManagerMemebers = new GridLayoutManager(this,5);
        recyclerViewCommunicationMembers.setLayoutManager(layoutManagerMemebers);
        recyclerViewCommunicationMembers.addItemDecoration(new ECMSpaceItemDecoration(DensityUtil.dip2px(this,8)));
        int state = getIntent().getIntExtra(VOICE_COMMUNICATION_STATE,EXCEPTION_STATE);
        initCommunicationViewsVisibility(state);
        initFunctionState();
//        initFunctionState();
        switch (state){
            case INVITER_LAYOUT_STATE:
                if(voiceCommunicationUserInfoBeanList.size() <= 5){
                    recyclerViewFirst.setAdapter(new VoiceCommunicationMemberAdapter(this,voiceCommunicationUserInfoBeanList,1));
                }else if(voiceCommunicationUserInfoBeanList.size() <= 9){
                    List<VoiceCommunicationJoinChannelInfoBean> list1 = voiceCommunicationUserInfoBeanList.subList(0,5);
                    List<VoiceCommunicationJoinChannelInfoBean> list2 = voiceCommunicationUserInfoBeanList.subList(5,voiceCommunicationUserInfoBeanList.size());
                    recyclerViewFirst.setAdapter(new VoiceCommunicationMemberAdapter(this,list1,1));
                    recyclerViewSecond.setAdapter(new VoiceCommunicationMemberAdapter(this,list2,2));
                }else{
                    ToastUtils.show(ChannelVoiceCommunicationActivity.this,"超出限制");
                    return;
                }
                createChannel();
                break;
            case INVITEE_LAYOUT_STATE:
                String channelId = getIntent().getStringExtra("channelId");
                getChannelInfoByChannelId(channelId);
                break;
        }

        chronometerCommunicationTime.setOnChronometerTickListener(new Chronometer.OnChronometerTickListener() {
            @Override
            public void onChronometerTick(Chronometer chronometer) {
                tvCommunicationState.setText(AppUtils.getNetSpeed(ChannelVoiceCommunicationActivity.this.getApplicationInfo().uid));
            }
        });

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
            LogUtils.YfcDebug("创建channel");
            try {
                JSONArray jsonArray = new JSONArray();
                LogUtils.YfcDebug("创建频道时成员列表长度："+voiceCommunicationUserInfoBeanList.size());
                for (int i = 0; i < voiceCommunicationUserInfoBeanList.size(); i++) {
                    JSONObject jsonObjectUserInfo = new JSONObject();
                    jsonObjectUserInfo.put("id",voiceCommunicationUserInfoBeanList.get(i).getUserId());
                    jsonObjectUserInfo.put("name",voiceCommunicationUserInfoBeanList.get(i).getUserName());
                    jsonArray.put(jsonObjectUserInfo);
                }
                LogUtils.YfcDebug("传递的人员信息："+jsonArray);
                apiService.getAgoraParams(jsonArray);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

//    /**
//     * 设置禁言，静音等
//     */
//    private void initFunctionState() {
//        if(!IS_EXCUSE_AVAILIABLE){
//            imgExcuse.setImageResource(R.drawable.icon_excuse_unavailable);
//            imgExcuse.setClickable(false);
//        }
//        if(!IS_HANDS_FREE_AVAILIABLE){
//            imgHandsFree.setImageResource(R.drawable.icon_hands_free_unavailable);
//            imgHandsFree.setClickable(false);
//        }
//        if(!IS_MUTE_AVAILIABLE){
//            imgMute.setImageResource(R.drawable.icon_mute_unavaiable);
//            imgMute.setClickable(false);
//        }
//    }

    /**
     * 根据状态改变布局可见性
     * @param state
     */
    private void initCommunicationViewsVisibility(int state) {
        if(state == EXCEPTION_STATE){
            finish();
        }
        linearLayoutInvitee.setVisibility(state == INVITEE_LAYOUT_STATE?View.VISIBLE:View.GONE);
        linearLayoutInviteMemebersGroup.setVisibility((state == INVITER_LAYOUT_STATE || state == COMMUNICATION_LAYOUT_STATE)?View.VISIBLE:View.GONE);
        linearLayoutCommunicationMembers.setVisibility(state == INVITEE_LAYOUT_STATE?View.VISIBLE:View.GONE);
        linearLayoutFunction.setVisibility((state == INVITER_LAYOUT_STATE || state == COMMUNICATION_LAYOUT_STATE)? View.VISIBLE:View.GONE);
        tvCommunicationState.setVisibility((state == INVITER_LAYOUT_STATE || state == COMMUNICATION_LAYOUT_STATE)?View.VISIBLE:View.GONE);
        chronometerCommunicationTime.setVisibility(state == COMMUNICATION_LAYOUT_STATE ? View.VISIBLE:View.GONE);
        imgAnswerPhone.setVisibility((state == INVITEE_LAYOUT_STATE)?View.VISIBLE:View.GONE);

        imgExcuse.setImageResource(state == COMMUNICATION_LAYOUT_STATE?R.drawable.icon_excuse_unselected:R.drawable.icon_excuse_unavailable);
        imgExcuse.setClickable(state == COMMUNICATION_LAYOUT_STATE?true:false);

        imgHandsFree.setImageResource(state == COMMUNICATION_LAYOUT_STATE?R.drawable.icon_hands_free_unselected:R.drawable.icon_hands_free_unavailable);
        imgHandsFree.setClickable(state == COMMUNICATION_LAYOUT_STATE?true:false);

        imgMute.setImageResource(state == COMMUNICATION_LAYOUT_STATE?R.drawable.icon_mute_unselcected:R.drawable.icon_mute_unavaiable);
        imgMute.setClickable(state == COMMUNICATION_LAYOUT_STATE?true:false);

        if(state == COMMUNICATION_LAYOUT_STATE){
            if(voiceCommunicationMemberList == null){
                LogUtils.YfcDebug("列表为空");
                return;
            }
            if(voiceCommunicationMemberList.size() <= 5){
                recyclerViewFirst.setAdapter(new VoiceCommunicationMemberAdapter(this,voiceCommunicationMemberList,1));
            }else if(voiceCommunicationMemberList.size() <= 9){
                List<VoiceCommunicationJoinChannelInfoBean> list1 = voiceCommunicationMemberList.subList(0,5);
                List<VoiceCommunicationJoinChannelInfoBean> list2 = voiceCommunicationMemberList.subList(5,voiceCommunicationMemberList.size());
                recyclerViewFirst.setAdapter(new VoiceCommunicationMemberAdapter(this,list1,1));
                recyclerViewSecond.setAdapter(new VoiceCommunicationMemberAdapter(this,list2,2));
            }else{
                ToastUtils.show(ChannelVoiceCommunicationActivity.this,"超出限制");
                return;
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
                LogUtils.YfcDebug("用户离开："+uid);
                LogUtils.YfcDebug("用户离开："+reason);
                userCount = userCount - 1;
                if(userCount < 2){
                    leaveChannelSuccess(channelId);
                }
            }

            @Override
            public void onUserJoined(int uid, int elapsed) {
                userCount = userCount + 1;
                if(userCount >= 2){
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            initCommunicationViewsVisibility(COMMUNICATION_LAYOUT_STATE);
                            chronometerCommunicationTime.setBase(SystemClock.elapsedRealtime());
                            chronometerCommunicationTime.start();
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
            public void onAudioVolumeIndication(VoiceCommunicationAudioVolumeInfo[] speakers, int totalVolume) {
                LogUtils.YfcDebug("说话人员列表长度"+speakers.length);
            }
        });
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
                LogUtils.YfcDebug("点击了收起页面");
                break;
            default:
                break;
        }
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
            LogUtils.YfcDebug("创建者加入频道的信息："+ JSON.toJSONString(voiceCommunicationJoinChannelInfoBean));
            if(voiceCommunicationJoinChannelInfoBean != null){
                voiceCommunicationUtils.joinChannel(voiceCommunicationJoinChannelInfoBean.getToken(),
                        getVoiceCommunicationResult.getChannelId(),voiceCommunicationJoinChannelInfoBean.getUserId(),voiceCommunicationJoinChannelInfoBean.getAgoraUid());
            }

            for (int i = 0; i < getVoiceCommunicationResult.getVoiceCommunicationJoinChannelInfoBeanList().size(); i++) {
                VoiceCommunicationJoinChannelInfoBean voiceCommunicationJoinChannelInfoBeanTemp = getVoiceCommunicationResult.getVoiceCommunicationJoinChannelInfoBeanList().get(i);
                if(!voiceCommunicationJoinChannelInfoBeanTemp.getUserId().equals(MyApplication.getInstance().getUid())){
                    voiceCommunicationMemberList.add(voiceCommunicationJoinChannelInfoBean);
                }
            }
        }

        @Override
        public void returnGetVoiceCommunicationResultFail(String error, int errorCode) {
            WebServiceMiddleUtils.hand(ChannelVoiceCommunicationActivity.this,error,errorCode);
        }

        @Override
        public void returnGetVoiceCommunicationChannelInfoSuccess(GetVoiceCommunicationResult getVoiceCommunicationResult) {
            channelId = getVoiceCommunicationResult.getChannelId();
            VoiceCommunicationJoinChannelInfoBean infoBean = getVoiceCommunicationResult.getVoiceCommunicationJoinChannelInfoBeanList().get(0);
            String url = APIUri.getUserIconUrl(ChannelVoiceCommunicationActivity.this, infoBean.getUserId());
            ImageDisplayUtils.getInstance().displayImage(imgUserHead,url,R.drawable.icon_person_default);
            tvUserName.setText(infoBean.getUserName());

            for (int i = 0; i < getVoiceCommunicationResult.getVoiceCommunicationJoinChannelInfoBeanList().size(); i++) {
                VoiceCommunicationJoinChannelInfoBean voiceCommunicationJoinChannelInfoBean = getVoiceCommunicationResult.getVoiceCommunicationJoinChannelInfoBeanList().get(i);
                if(!voiceCommunicationJoinChannelInfoBean.getUserId().equals(MyApplication.getInstance().getUid())){
                    voiceCommunicationMemberList.add(voiceCommunicationJoinChannelInfoBean);
                }else{
                    inviteeInfoBean = voiceCommunicationJoinChannelInfoBean;
                }
            }
            LogUtils.YfcDebug("通话人员列表大小："+voiceCommunicationMemberList.size());
            recyclerViewCommunicationMembers.setAdapter(new VoiceCommunicationMemberAdapter(ChannelVoiceCommunicationActivity.this,voiceCommunicationMemberList,3));
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
        public void returnGetVoiceCommunicationChannelInfoFail(String error, int errorCode) {
            WebServiceMiddleUtils.hand(ChannelVoiceCommunicationActivity.this,error,errorCode);
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
