package com.inspur.emmcloud.ui.chat;

import android.annotation.SuppressLint;
import android.content.Intent;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SwitchCompat;
import android.text.TextUtils;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.RelativeLayout;

import com.inspur.emmcloud.MyApplication;
import com.inspur.emmcloud.R;
import com.inspur.emmcloud.api.APIInterfaceInstance;
import com.inspur.emmcloud.api.apiservice.ChatAPIService;
import com.inspur.emmcloud.baselib.util.JSONUtils;
import com.inspur.emmcloud.baselib.util.ToastUtils;
import com.inspur.emmcloud.baselib.widget.LoadingDialog;
import com.inspur.emmcloud.basemodule.ui.BaseActivity;
import com.inspur.emmcloud.basemodule.util.NetUtils;
import com.inspur.emmcloud.basemodule.util.WebServiceMiddleUtils;
import com.inspur.emmcloud.componentservice.communication.Conversation;
import com.inspur.emmcloud.ui.chat.mvp.view.ConversationInfoActivity;
import com.inspur.emmcloud.util.privates.cache.ConversationCacheUtils;

import java.util.List;

/**
 * 群管理页面
 */
public class ConversationMemberManagerIndexActivity extends BaseActivity {

    public static final String MANAGER_TYPE = "managerType";
    public static final int REQUEST_GROUP_TRANSFER = 6;
    public static final int REQUEST_GROUP_ADMINISTRATOR = 7;
    public static final String INTENT_ADMIN_LIST = "intentAdminList";
    public static final String INTENT_SILENT = "intentSilent";
    public static final String INTENT_SELECT_OWNER = "selectOwner";
    private Conversation mConversation;
    private LoadingDialog loadingDlg;
    private int mType;
    private String mSelectOwner;

    @Override
    public void onCreate() {
        initData();
        initUI();
    }

    private void initData() {
        String id = getIntent().getStringExtra("cid");
        mType = getIntent().getIntExtra(MANAGER_TYPE, ConversationInfoActivity.GROUP_TYPE_ADMINISTRATOR);
        mConversation = ConversationCacheUtils.getConversation(MyApplication.getInstance(), id);
        if (mConversation == null) {
            ToastUtils.show(getApplicationContext(), getString(R.string.net_request_failed));
            finish();
        }
    }

    private void initUI() {
        loadingDlg = new LoadingDialog(ConversationMemberManagerIndexActivity.this);
        RelativeLayout ownerTransferLayout = findViewById(R.id.rl_member_manager_transfer);
        RelativeLayout memberManagerLayout = findViewById(R.id.rl_member_manager_manager);
        SwitchCompat memberForbidden = findViewById(R.id.switch_member_manager_forbidden);
        if (mType != ConversationInfoActivity.GROUP_TYPE_OWNER) {
            ownerTransferLayout.setVisibility(View.GONE);
            memberManagerLayout.setVisibility(View.GONE);
        }
        memberForbidden.setChecked(mConversation.isSilent());
        memberForbidden.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    enableSilent();
                } else {
                    disableSilent();
                }
            }
        });
    }

    @Override
    public int getLayoutResId() {
        return R.layout.activity_conversation_member_manager;
    }

    @SuppressLint("NonConstantResourceId")
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.ibt_back:
                finish();
                break;
            case R.id.rl_member_manager_transfer:
                Intent intent = new Intent();
                intent.setClass(this, MembersActivity.class);
                intent.putExtra("title", getString(R.string.voice_communication_choice_members));
                intent.putExtra(MembersActivity.MEMBER_PAGE_STATE, MembersActivity.GROUP_TRANSFER);
                intent.putExtra("cid", mConversation.getId());
                startActivityForResult(intent, REQUEST_GROUP_TRANSFER);
                break;
            case R.id.rl_member_manager_manager:
                Intent managerIntent = new Intent();
                managerIntent.setClass(this, ConversationGroupAdministratorActivity.class);
                managerIntent.putExtra("cid", mConversation.getId());
                startActivityForResult(managerIntent, REQUEST_GROUP_ADMINISTRATOR);
                break;
            default:
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == -1) {
            if (requestCode == REQUEST_GROUP_TRANSFER) {
                if (data != null) {
                    mSelectOwner = data.getStringExtra(INTENT_SELECT_OWNER);
                }
                finish();
            } else if (requestCode == REQUEST_GROUP_ADMINISTRATOR && data != null) {
                List<String> adminList = (List<String>) data.getSerializableExtra(INTENT_ADMIN_LIST);
                mConversation.setAdministrators(JSONUtils.toJSONString(adminList));
            }
        }
    }

    private void disableSilent() {
        if (NetUtils.isNetworkConnected(getApplicationContext())) {
            loadingDlg.show();
            ChatAPIService apiService = new ChatAPIService(ConversationMemberManagerIndexActivity.this);
            apiService.setAPIInterface(new WebService());
            apiService.disableConversationSilent(mConversation.getId());
        }
    }

    private void enableSilent() {
        if (NetUtils.isNetworkConnected(getApplicationContext())) {
            loadingDlg.show();
            ChatAPIService apiService = new ChatAPIService(ConversationMemberManagerIndexActivity.this);
            apiService.setAPIInterface(new WebService());
            apiService.enableConversationSilent(mConversation.getId());
        }
    }


    private class WebService extends APIInterfaceInstance {

        @Override
        public void returnEnableGroupSilentSuccess(String result) {
            LoadingDialog.dimissDlg(loadingDlg);
            mConversation.setSilent(true);
            ToastUtils.show("全员禁言已开启");
        }

        @Override
        public void returnEnableGroupSilentFail(String error, int errorCode) {
            LoadingDialog.dimissDlg(loadingDlg);
            WebServiceMiddleUtils.hand(ConversationMemberManagerIndexActivity.this, error, errorCode);
        }

        @Override
        public void returnDisableGroupSilentSuccess(String result) {
            LoadingDialog.dimissDlg(loadingDlg);
            mConversation.setSilent(false);
            ToastUtils.show("全员禁言已关闭");
        }

        @Override
        public void returnDisableGroupSilentFail(String error, int errorCode) {
            LoadingDialog.dimissDlg(loadingDlg);
            WebServiceMiddleUtils.hand(ConversationMemberManagerIndexActivity.this, error, errorCode);
        }
    }

    @Override
    public void finish() {
        Intent data = new Intent();
        if (mConversation != null) {
            if (!TextUtils.isEmpty(mSelectOwner)) {
                data.putExtra(INTENT_SELECT_OWNER, mSelectOwner);
            }
            data.putStringArrayListExtra(INTENT_ADMIN_LIST, mConversation.getAdministratorList());
            data.putExtra(INTENT_SILENT, mConversation.isSilent());
            setResult(RESULT_OK, data);
        }
        super.finish();
    }
}
