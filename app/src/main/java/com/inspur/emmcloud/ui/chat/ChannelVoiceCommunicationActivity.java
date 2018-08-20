package com.inspur.emmcloud.ui.chat;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Chronometer;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.inspur.emmcloud.BaseActivity;
import com.inspur.emmcloud.R;
import com.inspur.emmcloud.adapter.VoiceCommunicationMemberAdapter;
import com.inspur.emmcloud.bean.chat.VoiceCommunicationJoinChannelInfoBean;
import com.inspur.emmcloud.bean.contact.SearchModel;
import com.inspur.emmcloud.util.common.DensityUtil;
import com.inspur.emmcloud.util.common.LogUtils;
import com.inspur.emmcloud.util.common.StateBarUtils;
import com.inspur.emmcloud.util.common.ToastUtils;
import com.inspur.emmcloud.util.privates.VoiceCommunicationUtils;
import com.inspur.emmcloud.widget.CircleTextImageView;
import com.inspur.emmcloud.widget.ECMSpaceItemDecoration;

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
    private VoiceCommunicationUtils voiceCommunicationUtils;
    private List<SearchModel> selectMemList = new ArrayList<SearchModel>();
    private static final int CHOOSE_MEMBERS = 1;
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
    private VoiceCommunicationMemberAdapter voiceCommunicationMemberAdapter;
    private List<VoiceCommunicationJoinChannelInfoBean> voiceCommunicationJoinChannelInfoBeanList = new ArrayList<>();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        StateBarUtils.changeStateBarColor(this,R.color.content_bg);
        initViews();
    }

    /**
     * 初始化Views
     */
    private void initViews() {
        voiceCommunicationUtils = new VoiceCommunicationUtils(this);
        voiceCommunicationUtils.initializeAgoraEngine();
        initCallbacks();
        voiceCommunicationUtils.joinChannel("00636f73eb839f440a3a297a5c3b3977c13IABj5plqAhdjL9l3t2DKcp/dWHhdZ4FS0TfBnYndpbDc1V7mcSq5GPdSEAB7yAgAGtB3WwEAAQAAAAAA", "a392e5047f39430b9a920b45f4c4bd6d", "Extra Optional Data", 763702);
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
        initCommunicationViewsVisibility(INVITEE_LAYOUT_STATE);
        initFunctionState();
    }

    /**
     * 设置禁言，静音等
     */
    private void initFunctionState() {
        if(!IS_EXCUSE_AVAILIABLE){
            imgExcuse.setImageResource(R.drawable.icon_excuse_unavailable);
            imgExcuse.setClickable(false);
        }
        if(!IS_HANDS_FREE_AVAILIABLE){
            imgHandsFree.setImageResource(R.drawable.icon_hands_free_unavailable);
            imgHandsFree.setClickable(false);
        }
        if(!IS_MUTE_AVAILIABLE){
            imgMute.setImageResource(R.drawable.icon_mute_unavaiable);
            imgMute.setClickable(false);
        }
    }

    /**
     * 根据状态改变布局可见性
     * @param state
     */
    private void initCommunicationViewsVisibility(int state) {
        linearLayoutInvitee.setVisibility(state == INVITEE_LAYOUT_STATE?View.VISIBLE:View.INVISIBLE);
        linearLayoutInviteMemebersGroup.setVisibility((state == INVITER_LAYOUT_STATE || state == COMMUNICATION_LAYOUT_STATE)?View.VISIBLE:View.INVISIBLE);
        linearLayoutCommunicationMembers.setVisibility(state == INVITEE_LAYOUT_STATE?View.VISIBLE:View.INVISIBLE);
        linearLayoutFunction.setVisibility((state == INVITER_LAYOUT_STATE || state == COMMUNICATION_LAYOUT_STATE)? View.VISIBLE:View.INVISIBLE);
        tvCommunicationState.setVisibility((state == INVITER_LAYOUT_STATE || state == COMMUNICATION_LAYOUT_STATE)?View.VISIBLE:View.INVISIBLE);
        chronometerCommunicationTime.setVisibility(state == COMMUNICATION_LAYOUT_STATE ? View.VISIBLE:View.INVISIBLE);
        imgAnswerPhone.setVisibility((state == INVITEE_LAYOUT_STATE)?View.VISIBLE:View.GONE);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode == RESULT_OK && requestCode == CHOOSE_MEMBERS){
            if (data.getExtras().containsKey("selectMemList")) {
                selectMemList = (List<SearchModel>) data.getExtras()
                        .getSerializable("selectMemList");
            }
            if(selectMemList != null){
                voiceCommunicationJoinChannelInfoBeanList.clear();
                for (int i = 0; i < selectMemList.size(); i++) {
                    VoiceCommunicationJoinChannelInfoBean voiceCommunicationJoinChannelInfoBean = new VoiceCommunicationJoinChannelInfoBean("");
                    voiceCommunicationJoinChannelInfoBean.setHeadImageUrl(selectMemList.get(i).getIcon());
                    voiceCommunicationJoinChannelInfoBean.setUserId(selectMemList.get(i).getId());
                    voiceCommunicationJoinChannelInfoBeanList.add(voiceCommunicationJoinChannelInfoBean);
                }
                if(voiceCommunicationJoinChannelInfoBeanList.size() <= 5){
//                    LogUtils.YfcDebug("小于等于五个人");
                    recyclerViewFirst.setAdapter(new VoiceCommunicationMemberAdapter(this,voiceCommunicationJoinChannelInfoBeanList,1));
                }else if(voiceCommunicationJoinChannelInfoBeanList.size() <= 9){
//                    LogUtils.YfcDebug("大于五个人小于九个人"+voiceCommunicationJoinChannelInfoBeanList.subList(0,5).size());
//                    LogUtils.YfcDebug("大于五个人小于九个人"+voiceCommunicationJoinChannelInfoBeanList.subList(5,voiceCommunicationJoinChannelInfoBeanList.size()).size());
                    List<VoiceCommunicationJoinChannelInfoBean> list1 = voiceCommunicationJoinChannelInfoBeanList.subList(0,5);
                    List<VoiceCommunicationJoinChannelInfoBean> list2 = voiceCommunicationJoinChannelInfoBeanList.subList(5,voiceCommunicationJoinChannelInfoBeanList.size());
                    recyclerViewFirst.setAdapter(new VoiceCommunicationMemberAdapter(this,list1,1));
                    recyclerViewSecond.setAdapter(new VoiceCommunicationMemberAdapter(this,list2,2));
                }else{
                    ToastUtils.show(ChannelVoiceCommunicationActivity.this,"超出限制");
                }

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

            }

            @Override
            public void onUserJoined(int uid, int elapsed) {

            }

            @Override
            public void onJoinChannelSuccess(String channel, int uid, int elapsed) {
                LogUtils.YfcDebug("onJoinChannelSuccess  channel:"+channel);
                LogUtils.YfcDebug("onJoinChannelSuccess uid:"+uid);
                LogUtils.YfcDebug("onJoinChannelSuccess eslapsed:"+elapsed);
            }

            @Override
            public void onRejoinChannelSuccess(String channel, int uid, int elapsed) {

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
        });
    }

    public void onClick(View view){
        switch (view.getId()){
            case R.id.back_layout:
                finish();
                break;
            case R.id.img_an_excuse:
                switchFunctionViewUIState(imgExcuse,tvExcuse);
                imgExcuse.setImageResource(imgExcuse.isSelected()?R.drawable.icon_excuse_selected:R.drawable.icon_excuse_unselected);
                break;
            case R.id.img_hands_free:
                switchFunctionViewUIState(imgHandsFree,tvHandsFree);
                imgHandsFree.setImageResource(imgHandsFree.isSelected()?R.drawable.icon_hands_free_selected:R.drawable.icon_hands_free_unselected);
                break;
            case R.id.img_mute:
                switchFunctionViewUIState(imgMute,tvMute);
                imgMute.setImageResource(imgMute.isSelected()?R.drawable.icon_mute_selected:R.drawable.icon_mute_unselcected);
                break;
            case R.id.img_tran_video:
                switchFunctionViewUIState(imgTranVideo,tvTranVideo);
                imgTranVideo.setImageResource(imgTranVideo.isSelected()?R.drawable.icon_trans_video:R.drawable.icon_trans_video);
                break;
            case R.id.img_answer_the_phone:
                initCommunicationViewsVisibility(COMMUNICATION_LAYOUT_STATE);
                chronometerCommunicationTime.start();
                break;
            case R.id.img_hung_up:
                finish();
                break;
//            case R.id.btn_add_member:
//                Intent intent = new Intent();
//                intent.putExtra("select_content", 2);
//                intent.putExtra("isMulti_select", true);
//                intent.putExtra("isContainMe", true);
//                intent.putExtra("title", "选择人员");
//                if (selectMemList != null) {
//                    intent.putExtra("hasSearchResult", (Serializable) selectMemList);
//                }
//                intent.setClass(getApplicationContext(), ContactSearchActivity.class);
//                startActivityForResult(intent, CHOOSE_MEMBERS);
//                break;
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
    protected void onDestroy() {
        super.onDestroy();
        voiceCommunicationUtils.destroy();
    }
}
