package com.inspur.emmcloud.ui.chat;

import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.inspur.emmcloud.BaseActivity;
import com.inspur.emmcloud.MyApplication;
import com.inspur.emmcloud.R;
import com.inspur.emmcloud.adapter.MemberSelectAdapter;
import com.inspur.emmcloud.adapter.MemberSelectGridAdapter;
import com.inspur.emmcloud.api.APIInterfaceInstance;
import com.inspur.emmcloud.api.apiservice.ChatAPIService;
import com.inspur.emmcloud.bean.chat.GetChannelInfoResult;
import com.inspur.emmcloud.bean.contact.ContactUser;
import com.inspur.emmcloud.util.common.DensityUtil;
import com.inspur.emmcloud.util.common.NetUtils;
import com.inspur.emmcloud.util.privates.WebServiceMiddleUtils;
import com.inspur.emmcloud.util.privates.cache.ChannelGroupCacheUtils;
import com.inspur.emmcloud.util.privates.cache.ContactUserCacheUtils;
import com.inspur.emmcloud.widget.ECMSpaceItemDecoration;
import com.inspur.emmcloud.widget.LoadingDialog;

import org.xutils.view.annotation.ContentView;
import org.xutils.view.annotation.ViewInject;

import java.util.ArrayList;
import java.util.List;

/**
 * 选择人员，还没有实现搜索
 * Created by yufuchang on 2018/8/20.
 */
@ContentView(R.layout.activity_channel_member_select)
public class ChannelSelectVoiceVideoMembersActivity extends BaseActivity{

    @ViewInject(R.id.recyclerview_voice_communication_select_members)
    private RecyclerView recyclerViewSelect;
    @ViewInject(R.id.recyclerview_voice_communication_members)
    private RecyclerView recyclerViewGroupMember;
    private LoadingDialog loadingDlg;
    private ChatAPIService apiService;
    private String cid;
    private List<String> memberList;
    private List<ContactUser> contactUserList;
    private List<ContactUser> selectUserList;
    private MemberSelectGridAdapter selectGridAdapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initViews();
    }

    private void initViews() {
        apiService = new ChatAPIService(this);
        apiService.setAPIInterface(new WebService());
        loadingDlg = new LoadingDialog(this);
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
        memberList =  ChannelGroupCacheUtils.getMemberUidList(this,cid,-1);
        if(memberList.size() == 0){
            getChannelInfo();
        }else{
            contactUserList = ContactUserCacheUtils.getContactUserListById(memberList);
            displayContacts(contactUserList);
        }
    }

    /**
     * 展示用户列表
     * @param contactUserList
     */
    private void displayContacts(List<ContactUser> contactUserList) {
        MemberSelectAdapter memberSelectAdapter = new MemberSelectAdapter(this,contactUserList,new ArrayList<ContactUser>());
        memberSelectAdapter.setMemberSelectedInterface(new OnMemeberSelectedListener() {
            @Override
            public void onMemberSelected(List<ContactUser> contactUserList) {
                selectGridAdapter.setAndRefreshSelectMemberData(contactUserList);
                selectUserList = contactUserList;
            }
        });
        recyclerViewGroupMember.setAdapter(memberSelectAdapter);
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
                finish();
                break;
        }
    }

    class WebService extends APIInterfaceInstance{
        @Override
        public void returnChannelInfoSuccess(GetChannelInfoResult getChannelInfoResult) {
            LoadingDialog.dimissDlg(loadingDlg);
            contactUserList = ContactUserCacheUtils.getContactUserListById(getChannelInfoResult.getMemberList());
            displayContacts(contactUserList);
        }
        @Override
        public void returnChannelInfoFail(String error, int errorCode) {
            // TODO Auto-generated method stub
            LoadingDialog.dimissDlg(loadingDlg);
            WebServiceMiddleUtils.hand(ChannelSelectVoiceVideoMembersActivity.this, error, errorCode);
        }
    }

    public interface OnMemeberSelectedListener{
        void onMemberSelected(List<ContactUser> contactUserList);
    }
}
