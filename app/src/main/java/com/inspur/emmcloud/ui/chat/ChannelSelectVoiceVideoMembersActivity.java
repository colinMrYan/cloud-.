package com.inspur.emmcloud.ui.chat;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;

import com.inspur.emmcloud.BaseActivity;
import com.inspur.emmcloud.MyApplication;
import com.inspur.emmcloud.R;
import com.inspur.emmcloud.adapter.MemberSelectAdapter;
import com.inspur.emmcloud.adapter.MemberSelectGridAdapter;
import com.inspur.emmcloud.api.APIInterfaceInstance;
import com.inspur.emmcloud.api.apiservice.ChatAPIService;
import com.inspur.emmcloud.bean.chat.ChannelGroup;
import com.inspur.emmcloud.bean.chat.VoiceCommunicationJoinChannelInfoBean;
import com.inspur.emmcloud.bean.contact.ContactUser;
import com.inspur.emmcloud.util.common.DensityUtil;
import com.inspur.emmcloud.util.common.NetUtils;
import com.inspur.emmcloud.util.privates.WebServiceMiddleUtils;
import com.inspur.emmcloud.util.privates.cache.ChannelGroupCacheUtils;
import com.inspur.emmcloud.util.privates.cache.ContactUserCacheUtils;
import com.inspur.emmcloud.widget.CustomEditText;
import com.inspur.emmcloud.widget.ECMSpaceItemDecoration;
import com.inspur.emmcloud.widget.LoadingDialog;

import org.xutils.view.annotation.ContentView;
import org.xutils.view.annotation.ViewInject;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * 选择人员，还没有实现搜索
 * Created by yufuchang on 2018/8/20.
 */
@ContentView(R.layout.activity_channel_member_select)
public class ChannelSelectVoiceVideoMembersActivity extends BaseActivity implements TextWatcher {

    @ViewInject(R.id.recyclerview_voice_communication_select_members)
    private RecyclerView recyclerViewSelect;
    @ViewInject(R.id.recyclerview_voice_communication_members)
    private RecyclerView recyclerViewGroupMember;
    @ViewInject(R.id.ev_voice_communication_member_search_input)
    private CustomEditText evSearchEdit;
    private LoadingDialog loadingDlg;
    private ChatAPIService apiService;
    private String cid;
    private List<String> groupMemberIdList;
    private List<ContactUser> contactUserList;
    private List<ContactUser> selectUserList = new ArrayList<>();
    private MemberSelectGridAdapter selectGridAdapter;
    private MemberSelectAdapter memberSelectAdapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initViews();
    }

    private void initViews() {
        apiService = new ChatAPIService(this);
        apiService.setAPIInterface(new WebService());
        loadingDlg = new LoadingDialog(this);
        selectUserList.add(ContactUserCacheUtils.getContactUserByUid(MyApplication.getInstance().getUid()));
        List<ContactUser> allreadySelectUserList = new ArrayList<>();
        allreadySelectUserList.add(ContactUserCacheUtils.getContactUserByUid(MyApplication.getInstance().getUid()));
        selectGridAdapter = new MemberSelectGridAdapter(this,allreadySelectUserList);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        recyclerViewGroupMember.setLayoutManager(layoutManager);
        GridLayoutManager gridLayoutManager = new GridLayoutManager(this,5);
        recyclerViewSelect.addItemDecoration(new ECMSpaceItemDecoration(DensityUtil.dip2px(this,8)));
        recyclerViewSelect.setLayoutManager(gridLayoutManager);
        recyclerViewSelect.setAdapter(selectGridAdapter);
        cid = getIntent().getStringExtra("cid");
        groupMemberIdList =  ChannelGroupCacheUtils.getMemberUidList(this,cid,-1);
        if(groupMemberIdList.size() == 0){
            getChannelInfo();
        }else{
            contactUserList = ContactUserCacheUtils.getContactUserListById(groupMemberIdList);
            initContactsView(contactUserList);
        }
        evSearchEdit.addTextChangedListener(this);
    }

    /**
     * 展示用户列表
     * @param contactUserList
     */
    private void initContactsView(List<ContactUser> contactUserList) {
        memberSelectAdapter = new MemberSelectAdapter(this,contactUserList,selectUserList);
        memberSelectAdapter.setMemberSelectedInterface(new OnMemeberSelectedListener() {
//            @Override
//            public void onMemberSelected(List<ContactUser> contactUserList) {
//                selectGridAdapter.setAndRefreshSelectMemberData(contactUserList);
//                selectUserList = contactUserList;
//            }

            @Override
            public void onMemberSelected(ContactUser contactUser, boolean isSelected) {
                if(isSelected){
                    selectUserList.add(contactUser);
                }else{
                    selectUserList.remove(contactUser);
                }
                selectGridAdapter.setAndRefreshSelectMemberData(selectUserList);
            }
        });
        recyclerViewGroupMember.setAdapter(memberSelectAdapter);
    }

    /**
     * 刷新
     * @param contactUserList
     */
    private void refreshContactView(List<ContactUser> contactUserList){
        memberSelectAdapter.setAndRefreshData(contactUserList);
    }

    /**
     * 获取频道信息
     */
    private void getChannelInfo() {
        if (NetUtils.isNetworkConnected(getApplicationContext())) {
            loadingDlg.show();
            apiService.getChannelInfo(cid);
        }
    }

    public void onClick(View view){
        switch (view.getId()){
            case R.id.back_layout:
                finish();
                break;
            case R.id.tv_ok:
                startCommunication();
                break;
        }
    }

    /**
     * 邀请开始通话
     */
    private void startCommunication() {
        List<VoiceCommunicationJoinChannelInfoBean> voiceCommunicationUserInfoBeanList = new ArrayList<>();
        for (int i = 0; i < selectUserList.size(); i++) {
            VoiceCommunicationJoinChannelInfoBean voiceCommunicationJoinChannelInfoBean = new VoiceCommunicationJoinChannelInfoBean();
            voiceCommunicationJoinChannelInfoBean.setUserName(selectUserList.get(i).getName());
            voiceCommunicationJoinChannelInfoBean.setUserId(selectUserList.get(i).getId());
            voiceCommunicationUserInfoBeanList.add(voiceCommunicationJoinChannelInfoBean);
        }
        Intent intent = new Intent();
        intent.setClass(ChannelSelectVoiceVideoMembersActivity.this,ChannelVoiceCommunicationActivity.class);
        intent.putExtra("userList", (Serializable) voiceCommunicationUserInfoBeanList);
        intent.putExtra(ChannelVoiceCommunicationActivity.VOICE_COMMUNICATION_STATE,ChannelVoiceCommunicationActivity.INVITER_LAYOUT_STATE);
        startActivity(intent);
        finish();
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
        List<ContactUser> contactUserShowList = new ArrayList<>();
        for(ContactUser contactUser : contactUserList){
            if(contactUser.getName().indexOf(s.toString()) != -1 || contactUser.getPinyin().contains(s.toString())){
                contactUserShowList.add(contactUser);
            }
        }
        refreshContactView(contactUserShowList);
    }

    @Override
    public void afterTextChanged(Editable s) {

    }

    class WebService extends APIInterfaceInstance{
        @Override
        public void returnChannelInfoSuccess(ChannelGroup getChannelInfoResult) {
            LoadingDialog.dimissDlg(loadingDlg);
            contactUserList = ContactUserCacheUtils.getContactUserListById(getChannelInfoResult.getMemberList());
            initContactsView(contactUserList);
        }
        @Override
        public void returnChannelInfoFail(String error, int errorCode) {
            // TODO Auto-generated method stub
            LoadingDialog.dimissDlg(loadingDlg);
            WebServiceMiddleUtils.hand(ChannelSelectVoiceVideoMembersActivity.this, error, errorCode);
        }
    }

    public interface OnMemeberSelectedListener{
        void onMemberSelected(ContactUser contactUser,boolean isSelected);
    }
}
