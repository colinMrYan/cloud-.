package com.inspur.emmcloud.ui.chat;

import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Color;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import android.view.View;
import android.widget.AdapterView;
import android.widget.TextView;

import com.inspur.emmcloud.MyApplication;
import com.inspur.emmcloud.R;
import com.inspur.emmcloud.api.APIInterfaceInstance;
import com.inspur.emmcloud.api.apiservice.ChatAPIService;
import com.inspur.emmcloud.baselib.util.IntentUtils;
import com.inspur.emmcloud.baselib.util.JSONUtils;
import com.inspur.emmcloud.baselib.util.ToastUtils;
import com.inspur.emmcloud.baselib.widget.LoadingDialog;
import com.inspur.emmcloud.baselib.widget.NoScrollGridView;
import com.inspur.emmcloud.basemodule.ui.BaseActivity;
import com.inspur.emmcloud.basemodule.ui.DarkUtil;
import com.inspur.emmcloud.basemodule.util.AppTabUtils;
import com.inspur.emmcloud.basemodule.util.NetUtils;
import com.inspur.emmcloud.basemodule.util.WebServiceMiddleUtils;
import com.inspur.emmcloud.componentservice.communication.Conversation;
import com.inspur.emmcloud.ui.chat.mvp.adapter.ConversationMembersHeadAdapter;
import com.inspur.emmcloud.ui.chat.mvp.view.ConversationInfoActivity;
import com.inspur.emmcloud.ui.contact.ContactSearchFragment;
import com.inspur.emmcloud.ui.contact.RobotInfoActivity;
import com.inspur.emmcloud.ui.contact.UserInfoActivity;
import com.inspur.emmcloud.util.privates.cache.ConversationCacheUtils;

import org.json.JSONArray;

import java.util.ArrayList;
import java.util.List;

import static com.inspur.emmcloud.ui.chat.ConversationMemberManagerIndexActivity.INTENT_ADMIN_LIST;

/**
 * 群管理页面
 */
public class ConversationGroupAdministratorActivity extends BaseActivity {

    private static final int QEQUEST_ADD_ADMINISTRATOR = 6;
    private static final int QEQUEST_DEL_ADMINISTRATOR = 7;

    private Conversation mConversation;
    private LoadingDialog mLoadingDlg;
    private NoScrollGridView mGroupAdministratorGrid;
    private ConversationMembersHeadAdapter mConversationMembersHeadAdapter;

    @Override
    public void onCreate() {
        initData();
        initUI();
    }

    private void initData() {
        String id = getIntent().getStringExtra("cid");
        mConversation = ConversationCacheUtils.getConversation(MyApplication.getInstance(), id);
        if (mConversation == null) {
            ToastUtils.show(getApplicationContext(), getString(R.string.net_request_failed));
            finish();
        }
    }

    private void initUI() {
        mLoadingDlg = new LoadingDialog(ConversationGroupAdministratorActivity.this);
        mGroupAdministratorGrid = findViewById(R.id.group_administrator_grid);
        TextView introText = findViewById(R.id.group_administrator_intro);
        introText.setTextColor(Color.parseColor(DarkUtil.isDarkTheme() ? "#999999" : "#666666"));
        initList();
    }

    private void initList() {
        final List<String> uiUidList = getUIUidListFromDataList(mConversation.getAdministratorList());
        mConversationMembersHeadAdapter = new ConversationMembersHeadAdapter(this,
                uiUidList, mConversation.getOwner(), new ArrayList<String>(), mConversation.getMembersDetail());
        Configuration configuration = getResources().getConfiguration();
        // 适配横屏头像显示
        if (configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            mGroupAdministratorGrid.setNumColumns(8);

        } else if (configuration.orientation == Configuration.ORIENTATION_PORTRAIT) {
            mGroupAdministratorGrid.setNumColumns(5);

        }
        mGroupAdministratorGrid.setAdapter(mConversationMembersHeadAdapter);
        mGroupAdministratorGrid.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                if (mConversation == null) {
                    ToastUtils.show(ConversationGroupAdministratorActivity.this, getString(R.string.net_request_failed));
                    return;
                }
                Intent intent = new Intent();
                if (ConversationMembersHeadAdapter.TYPE_DELETE_USER.equals(mConversationMembersHeadAdapter.getUIUidFromIndex(i))) {
                    intent.putExtra("memberUidList", mConversation.getAdministratorList());
                    intent.setClass(getApplicationContext(),
                            ChannelMembersDelActivity.class);
                    intent.putExtra("membersDetail", mConversation.getMembersDetail());
                    intent.putExtra(ContactSearchFragment.EXTRA_TITLE, getString(R.string.remove_group_administrator));
                    startActivityForResult(intent, QEQUEST_DEL_ADMINISTRATOR);
                } else if (ConversationMembersHeadAdapter.TYPE_ADD_USER.equals(mConversationMembersHeadAdapter.getUIUidFromIndex(i))
                        && AppTabUtils.hasContactPermission(ConversationGroupAdministratorActivity.this)) {
                    ArrayList<String> notAdministratorList = new ArrayList<>(mConversation.getMemberList());
                    notAdministratorList.removeAll(mConversation.getAdministratorList());
                    intent.putExtra("memberUidList", notAdministratorList);
                    intent.setClass(getApplicationContext(),
                            ChannelMembersDelActivity.class);
                    intent.putExtra("membersDetail", mConversation.getMembersDetail());
                    intent.putExtra(ContactSearchFragment.EXTRA_TITLE, getString(R.string.add_group_administrator));
                    startActivityForResult(intent, QEQUEST_ADD_ADMINISTRATOR);
                } else {
                    String uid = uiUidList.get(i);
                    Bundle bundle = new Bundle();
                    bundle.putString("uid", uid);
                    IntentUtils.startActivity(ConversationGroupAdministratorActivity.this, uid.startsWith("BOT") ?
                            RobotInfoActivity.class : UserInfoActivity.class, bundle);
                }
            }
        });
    }

    @Override
    public int getLayoutResId() {
        return R.layout.activity_conversation_group_administrator;
    }

    public void onClick(View v) {
        if (v.getId() == R.id.ibt_back) {
            finish();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode != -1) {
            return;
        }
        switch (requestCode) {
            case ConversationInfoActivity.QEQUEST_GROUP_MANAGE:
                setResult(RESULT_OK, data);
                finish();
                break;
            case QEQUEST_ADD_ADMINISTRATOR:
                List<String> addMemberList = (List<String>) data.getSerializableExtra("selectMemList");
                if (addMemberList.size() > 0) {
                    addAdministrator(addMemberList);
                }
                break;
            case QEQUEST_DEL_ADMINISTRATOR:
                List<String> removeMemberList = (List<String>) data.getSerializableExtra("selectMemList");
                if (removeMemberList.size() > 0) {
                    removeAdministrator(removeMemberList);
                }
                break;
            default:
                break;
        }
    }

    private List<String> getUIUidListFromDataList(List<String> dataList) {
        List<String> uiUidList = new ArrayList<>(dataList);
        uiUidList.remove(mConversation.getOwner());
        uiUidList.add("addUser");
        if (uiUidList.size() > 1) {
            uiUidList.add("deleteUser");
        }
        return uiUidList;
    }

    private void addAdministrator(final List<String> uidList) {
        if (NetUtils.isNetworkConnected(getApplicationContext())) {
            mLoadingDlg.show();
            ChatAPIService apiService = new ChatAPIService(ConversationGroupAdministratorActivity.this);
            apiService.setAPIInterface(new APIInterfaceInstance() {
                @Override
                public void returnAddAdministratorSuccess(String result) {
                    LoadingDialog.dimissDlg(mLoadingDlg);
                    List<String> administrators = mConversation.getAdministratorList();
                    administrators.addAll(uidList);
                    mConversation.setAdministrators(JSONUtils.toJSONString(administrators));
                    List<String> uiUidList = ConversationGroupAdministratorActivity.this.getUIUidListFromDataList(administrators);
                    mConversationMembersHeadAdapter.setUIUidList(uiUidList);
                    mConversationMembersHeadAdapter.notifyDataSetChanged();
                }

                @Override
                public void returnAddAdministratorFail(String error, int errorCode) {
                    LoadingDialog.dimissDlg(mLoadingDlg);
                    WebServiceMiddleUtils.hand(ConversationGroupAdministratorActivity.this, error, errorCode);
                }
            });
            JSONArray jsonArray = new JSONArray();
            for (String uid : uidList) {
                jsonArray.put(uid);
            }
            apiService.addConversationAdministrator(mConversation.getId(), jsonArray);
        }
    }

    private void removeAdministrator(@NonNull final List<String> uidList) {
        if (NetUtils.isNetworkConnected(getApplicationContext())) {
            mLoadingDlg.show();
            ChatAPIService apiService = new ChatAPIService(ConversationGroupAdministratorActivity.this);
            apiService.setAPIInterface(new APIInterfaceInstance() {
                @Override
                public void returnRemoveAdministratorSuccess(String result) {
                    LoadingDialog.dimissDlg(mLoadingDlg);
                    List<String> administrators = mConversation.getAdministratorList();
                    administrators.removeAll(uidList);
                    mConversation.setAdministrators(JSONUtils.toJSONString(administrators));
                    List<String> uiUidList = ConversationGroupAdministratorActivity.this.getUIUidListFromDataList(mConversation.getAdministratorList());
                    mConversationMembersHeadAdapter.setUIUidList(uiUidList);
                    mConversationMembersHeadAdapter.notifyDataSetChanged();
                }

                @Override
                public void returnRemoveAdministratorFail(String error, int errorCode) {
                    LoadingDialog.dimissDlg(mLoadingDlg);
                    WebServiceMiddleUtils.hand(ConversationGroupAdministratorActivity.this, error, errorCode);
                }
            });
            JSONArray jsonArray = new JSONArray();
            for (String uid : uidList) {
                jsonArray.put(uid);
            }
            apiService.removeConversationAdministrator(mConversation.getId(), jsonArray);
        }
    }

    @Override
    public void finish() {
        Intent data = new Intent();
        data.putStringArrayListExtra(INTENT_ADMIN_LIST, mConversation.getAdministratorList());
        setResult(RESULT_OK, data);
        super.finish();
    }
}
