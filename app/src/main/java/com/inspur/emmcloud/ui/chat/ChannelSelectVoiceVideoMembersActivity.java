package com.inspur.emmcloud.ui.chat;

import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.inspur.emmcloud.BaseActivity;
import com.inspur.emmcloud.R;
import com.inspur.emmcloud.api.APIInterfaceInstance;
import com.inspur.emmcloud.api.apiservice.ChatAPIService;
import com.inspur.emmcloud.bean.chat.GetChannelInfoResult;
import com.inspur.emmcloud.bean.contact.ContactUser;
import com.inspur.emmcloud.util.common.NetUtils;
import com.inspur.emmcloud.util.privates.WebServiceMiddleUtils;
import com.inspur.emmcloud.util.privates.cache.ChannelGroupCacheUtils;
import com.inspur.emmcloud.util.privates.cache.ContactUserCacheUtils;
import com.inspur.emmcloud.widget.LoadingDialog;

import org.xutils.view.annotation.ContentView;
import org.xutils.view.annotation.ViewInject;

import java.util.List;

/**
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
    private List<String> memeberList;
    private List<ContactUser> contactUserList;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initViews();
    }

    private void initViews() {
        apiService = new ChatAPIService(this);
        apiService.setAPIInterface(new WebService());
        loadingDlg = new LoadingDialog(this);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
        recyclerViewGroupMember.setLayoutManager(layoutManager);
        cid = getIntent().getStringExtra("cid");
        memeberList =  ChannelGroupCacheUtils.getMemberUidList(this,cid,-1);
        if(memeberList.size() == 0){
            getChannelInfo();
        }else{
            contactUserList = ContactUserCacheUtils.getContactUserListById(memeberList);
        }
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
        }
    }

    class WebService extends APIInterfaceInstance{
        @Override
        public void returnChannelInfoSuccess(GetChannelInfoResult getChannelInfoResult) {
            LoadingDialog.dimissDlg(loadingDlg);
            contactUserList = ContactUserCacheUtils.getContactUserListById(getChannelInfoResult.getMemberList());
        }
        @Override
        public void returnChannelInfoFail(String error, int errorCode) {
            // TODO Auto-generated method stub
            LoadingDialog.dimissDlg(loadingDlg);
            WebServiceMiddleUtils.hand(ChannelSelectVoiceVideoMembersActivity.this, error, errorCode);
        }
    }
}
