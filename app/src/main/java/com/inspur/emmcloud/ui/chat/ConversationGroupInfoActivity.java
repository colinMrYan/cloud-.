package com.inspur.emmcloud.ui.chat;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.TextView;

import com.inspur.emmcloud.BaseActivity;
import com.inspur.emmcloud.MyApplication;
import com.inspur.emmcloud.R;
import com.inspur.emmcloud.adapter.ConversationMemberAdapter;
import com.inspur.emmcloud.api.APIInterfaceInstance;
import com.inspur.emmcloud.api.apiservice.ChatAPIService;
import com.inspur.emmcloud.bean.chat.Conversation;
import com.inspur.emmcloud.bean.contact.ContactUser;
import com.inspur.emmcloud.bean.contact.SearchModel;
import com.inspur.emmcloud.bean.system.SimpleEventMessage;
import com.inspur.emmcloud.config.Constant;
import com.inspur.emmcloud.ui.contact.ContactSearchActivity;
import com.inspur.emmcloud.ui.contact.ContactSearchFragment;
import com.inspur.emmcloud.ui.contact.RobotInfoActivity;
import com.inspur.emmcloud.ui.contact.UserInfoActivity;
import com.inspur.emmcloud.util.common.IntentUtils;
import com.inspur.emmcloud.util.common.NetUtils;
import com.inspur.emmcloud.util.privates.WebServiceMiddleUtils;
import com.inspur.emmcloud.util.privates.cache.ChannelOperationCacheUtils;
import com.inspur.emmcloud.util.privates.cache.ContactUserCacheUtils;
import com.inspur.emmcloud.util.privates.cache.ConversationCacheUtils;
import com.inspur.emmcloud.widget.LoadingDialog;
import com.inspur.emmcloud.widget.NoScrollGridView;
import com.inspur.emmcloud.widget.SwitchView;
import com.inspur.emmcloud.widget.SwitchView.OnStateChangedListener;
import com.inspur.emmcloud.widget.dialogs.MyQMUIDialog;
import com.qmuiteam.qmui.widget.dialog.QMUIDialog;
import com.qmuiteam.qmui.widget.dialog.QMUIDialogAction;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.xutils.view.annotation.ContentView;
import org.xutils.view.annotation.ViewInject;

import java.util.ArrayList;
import java.util.List;

/**
 * 群组类型会话详情
 */
@ContentView(R.layout.activity_conversation_group_info)
public class ConversationGroupInfoActivity extends BaseActivity {

    private static final int QEQUEST_ADD_MEMBER = 2;
    private static final int QEQUEST_DEL_MEMBER = 3;
    public static final String EXTRA_CID= "cid";
    @ViewInject(R.id.gv_member)
    private NoScrollGridView memberGrid;
    @ViewInject(R.id.tv_member)
    private TextView memberText;
    @ViewInject(R.id.sv_stick)
    private SwitchView stickSwitch;
    @ViewInject(R.id.sv_dnd)
    private SwitchView dndSwitch;
    @ViewInject(R.id.tv_name)
    private TextView nameText;
    @ViewInject(R.id.bt_exit)
    private Button exitBtn;

    private ChatAPIService apiService;
    private ConversationMemberAdapter adapter;
    private ArrayList<String> memberUidList = new ArrayList<>();
    private ArrayList<String> uiMemberUidList = new ArrayList<>();
    private LoadingDialog loadingDlg;
    private Conversation conversation;
    private boolean isOwner;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        EventBus.getDefault().register(this);
        String cid = getIntent().getExtras().getString(EXTRA_CID);
        conversation = ConversationCacheUtils.getConversation(MyApplication.getInstance(),cid);
        isOwner = conversation.getOwner().equals(MyApplication.getInstance().getUid());
        apiService = new ChatAPIService(ConversationGroupInfoActivity.this);
        apiService.setAPIInterface(new WebService());
        initView();
    }

    /**
     * 数据取出后显示ui
     */
    private void initView() {
        loadingDlg = new LoadingDialog(ConversationGroupInfoActivity.this);
        memberUidList = conversation.getMemberList();
        int memberSize = ContactUserCacheUtils.getContactUserListById(memberUidList).size();
        memberText.setText(getString(R.string.all_group_member,memberSize));
        nameText.setText(conversation.getName());
        filterGroupMember(memberUidList);
        adapter = new ConversationMemberAdapter(this,uiMemberUidList,isOwner);
        memberGrid.setAdapter(adapter);
        memberGrid.setOnItemClickListener(onItemClickListener);
        dndSwitch.setOpened(conversation.isDnd());
        dndSwitch.setOnStateChangedListener(onStateChangedListener);
        stickSwitch.setOpened(conversation.isStick());
        stickSwitch.setOnStateChangedListener(onStateChangedListener);
        exitBtn.setVisibility(View.VISIBLE);
    }

    /**
     * 是否将频道置顶
     *
     * @param isSetIop
     */
    private void setChannelTop(boolean isSetIop) {
        stickSwitch.toggleSwitch(isSetIop);
        ChannelOperationCacheUtils.setChannelTop(ConversationGroupInfoActivity.this, conversation.getId(),
                isSetIop);
        // 通知消息页面重新创建群组头像
        Intent intent = new Intent("message_notify");
        intent.putExtra("command", "sort_session_list");
        sendBroadcast(intent);
    }

    private OnItemClickListener onItemClickListener = new OnItemClickListener() {

        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position,
                                long id) {
            // TODO Auto-generated method stub
            Intent intent = new Intent();
            if ((position == adapter.getCount() - 1) && isOwner) {
                intent.putExtra("memberUidList", memberUidList);
                intent.setClass(getApplicationContext(),
                        ChannelMembersDelActivity.class);
                startActivityForResult(intent, QEQUEST_DEL_MEMBER);

            } else if (((position == adapter.getCount() - 2) &&isOwner)
                    || ((position == adapter.getCount() - 1) && !isOwner)) {
                intent.putExtra(ContactSearchFragment.EXTRA_TYPE, 2);
                intent.putExtra(ContactSearchFragment.EXTRA_EXCLUDE_SELECT, memberUidList);
                intent.putExtra(ContactSearchFragment.EXTRA_MULTI_SELECT, true);
                intent.putExtra(ContactSearchFragment.EXTRA_TITLE, getString(R.string.add_group_member));
                intent.setClass(getApplicationContext(),
                        ContactSearchActivity.class);
                startActivityForResult(intent, QEQUEST_ADD_MEMBER);
            } else {
                String uid = uiMemberUidList.get(position);
                Bundle bundle = new Bundle();
                bundle.putString("uid", uid);
                IntentUtils.startActivity(ConversationGroupInfoActivity.this,uid.startsWith("BOT")?
                        RobotInfoActivity.class:UserInfoActivity.class, bundle);
            }
        }
    };

    private OnStateChangedListener onStateChangedListener = new OnStateChangedListener() {

        @Override
        public void toggleToOn(View view) {
            // TODO Auto-generated method stub
            if (view.getId() == R.id.sv_dnd) {
                updateConversationDnd();
            } else {
                setConversationStick();
            }
        }

        @Override
        public void toggleToOff(View view) {
            // TODO Auto-generated method stub
            if (view.getId() == R.id.sv_dnd) {
                updateConversationDnd();
            } else {
                setConversationStick();
            }
        }
    };

    public void onClick(View v) {
        Bundle bundle = new Bundle();
        switch (v.getId()) {
            case R.id.back_layout:
                finish();
                break;
            case R.id.rl_chat_imgs:
                bundle.putString("cid", conversation.getId());
                IntentUtils.startActivity(ConversationGroupInfoActivity.this,
                        GroupAlbumActivity.class, bundle);
                break;
            case R.id.rl_chat_files:
                bundle.putString("cid", conversation.getId());
                IntentUtils.startActivity(ConversationGroupInfoActivity.this,
                        GroupFileActivity.class, bundle);
                break;
            case R.id.channel_name_layout:
                bundle.putString("cid", conversation.getId());
                IntentUtils.startActivity(ConversationGroupInfoActivity.this,
                        ConversationNameModifyActivity.class, bundle);
                break;
            case R.id.rl_member:
                bundle.putString("title", getString(R.string.group_member));
                bundle.putInt(MembersActivity.MEMBER_PAGE_STATE,MembersActivity.CHECK_STATE);
                bundle.putStringArrayList("uidList",memberUidList);
                IntentUtils.startActivity(ConversationGroupInfoActivity.this,
                        MembersActivity.class, bundle);
                break;
            case R.id.bt_exit:
                showQuitGroupWarningDlg();
                break;
            default:
                break;
        }
    }

    private void showQuitGroupWarningDlg(){
        new MyQMUIDialog.MessageDialogBuilder(ConversationGroupInfoActivity.this)
                .setMessage(getString(R.string.quit_group_warning_text))
                .addAction(getString(R.string.cancel), new QMUIDialogAction.ActionListener() {
                    @Override
                    public void onClick(QMUIDialog dialog, int index) {
                        dialog.dismiss();
                    }
                })
                .addAction(getString(R.string.ok), new QMUIDialogAction.ActionListener() {
                    @Override
                    public void onClick(QMUIDialog dialog, int index) {
                        dialog.dismiss();
                        quitChannelGroup();
                    }
                })
                .show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case QEQUEST_DEL_MEMBER:
                    ArrayList<String> delUidList = (ArrayList<String>) data.getSerializableExtra("selectMemList");
                    if (delUidList.size()>0){
                        delConversationGroupMember(delUidList);
                    }
                    break;
                case QEQUEST_ADD_MEMBER:
                    ArrayList<String> addUidList = new ArrayList<>();
                    List<SearchModel> addMemberList = (List<SearchModel>) data
                            .getSerializableExtra("selectMemList");
                    if (addMemberList.size()>0){
                        for (int i = 0; i < addMemberList.size(); i++) {
                            addUidList.add(addMemberList.get(i).getId());
                        }
                        addConversationGroupMember(addUidList);
                    }

                    break;

                default:
                    break;
            }
        }
    }


    /**
     * 过滤不存在的群成员算法
     */
    private void filterGroupMember(List<String> memberList) {
        //查三十人，如果不满三十人则查实际人数保证查到的人都是存在的群成员
        List<ContactUser> contactUserList = ContactUserCacheUtils.getContactUserListByIdListOrderBy(memberList,9);
        ArrayList<String> contactUserIdList = new ArrayList<>();
        for (ContactUser contactUser:contactUserList) {
            contactUserIdList.add(contactUser.getId());
        }
        uiMemberUidList.clear();
        uiMemberUidList.addAll(contactUserIdList);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }

    /**
     * 修改群组名称
     * @param eventMessage
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onReciverConversationNameUpdate(SimpleEventMessage eventMessage) {
        if (eventMessage.getAction().equals(Constant.EVENTBUS_TAG_UPDATE_CHANNEL_NAME)){
            String name = ((Conversation)eventMessage.getMessageObj()).getName();
            nameText.setText(name);
            conversation.setName(name);
        }
    }

    /**
     * 更改是否频道消息免打扰
     *
     * @param isNoInterruption
     */
    private void updateConversationDnd() {
        // TODO Auto-generated method stub
        if (NetUtils.isNetworkConnected(ConversationGroupInfoActivity.this)) {
            loadingDlg.show();
            apiService.updateConversationDnd(conversation.getId(), !conversation.isDnd());
        } else {
            dndSwitch.setOpened(conversation.isDnd());
        }
    }

    /**
     * 设置频道是否置顶
     *
     * @param id
     * @param isStick
     */
    private void setConversationStick() {
        if (NetUtils.isNetworkConnected(MyApplication.getInstance())) {
            loadingDlg.show();
            apiService.setConversationStick(conversation.getId(), !conversation.isStick());
        }else {
            stickSwitch.setOpened(conversation.isStick());
        }
    }


    /**
     * 添加群组成员
     *
     * @param uidList
     */
    private void addConversationGroupMember(ArrayList<String> uidList) {
        if (NetUtils.isNetworkConnected(ConversationGroupInfoActivity.this)) {
            loadingDlg.show();
            apiService.addConversationGroupMember(conversation.getId(),uidList);
        }
    }

    /**
     * 删除群成员
     *
     * @param uidList
     */
    public void delConversationGroupMember(ArrayList<String> uidList) {
        if (NetUtils.isNetworkConnected(ConversationGroupInfoActivity.this)) {
            loadingDlg.show();
            apiService.delConversationGroupMember(conversation.getId(),uidList);
        }
    }


    /**
     * 退出群聊
     */
    public void quitChannelGroup(){
        if (NetUtils.isNetworkConnected(ConversationGroupInfoActivity.this)) {
            loadingDlg.show();
            apiService.quitChannelGroup(conversation.getId());
        }
    }

    private class WebService extends APIInterfaceInstance {

        @Override
        public void returnDndSuccess() {
            // TODO Auto-generated method stub
            LoadingDialog.dimissDlg(loadingDlg);
            conversation.setDnd(!conversation.isDnd());
            dndSwitch.setOpened(conversation.isDnd());
            ConversationCacheUtils.updateConversationDnd(MyApplication.getInstance(),conversation.getId(),conversation.isDnd());
            EventBus.getDefault().post(new SimpleEventMessage(Constant.EVENTBUS_TAG_UPDATE_CHANNEL_DND,conversation));

        }

        @Override
        public void returnDndFail(String error, int errorCode) {
            // TODO Auto-generated method stub
            LoadingDialog.dimissDlg(loadingDlg);
            dndSwitch.setOpened(conversation.isDnd());
            WebServiceMiddleUtils.hand(ConversationGroupInfoActivity.this, error, errorCode);
        }

        @Override
        public void returnSetConversationStickSuccess(String id, boolean isStick) {
            LoadingDialog.dimissDlg(loadingDlg);
            conversation.setStick(isStick);
            stickSwitch.setOpened(isStick);
            ConversationCacheUtils.setConversationStick(MyApplication.getInstance(), id, isStick);
            EventBus.getDefault().post(new SimpleEventMessage(Constant.EVENTBUS_TAG_UPDATE_CHANNEL_FOCUS,conversation));
        }

        @Override
        public void returnSetConversationStickFail(String error, int errorCode) {
            LoadingDialog.dimissDlg(loadingDlg);
            WebServiceMiddleUtils.hand(MyApplication.getInstance(), error, errorCode);
            stickSwitch.setOpened(conversation.isStick());
        }

        @Override
        public void returnAddConversationGroupMemberSuccess(List<String> uidList) {
            LoadingDialog.dimissDlg(loadingDlg);
            memberUidList.addAll(uidList);
            ConversationCacheUtils.setConversationMember(MyApplication.getInstance(), conversation.getId(), memberUidList);
            memberText.setText(getString(R.string.all_group_member,memberUidList.size()));
            if (adapter.getCount() < 10){
                uiMemberUidList.addAll(uidList);
                adapter.notifyDataSetChanged();
            }

        }

        @Override
        public void returnAddConversationGroupMemberFail(String error, int errorCode) {
            LoadingDialog.dimissDlg(loadingDlg);
            WebServiceMiddleUtils.hand(ConversationGroupInfoActivity.this, error, errorCode);
        }

        @Override
        public void returnDelConversationGroupMemberSuccess(List<String> uidList) {
            LoadingDialog.dimissDlg(loadingDlg);
            memberUidList.removeAll(uidList);
            ConversationCacheUtils.setConversationMember(MyApplication.getInstance(), conversation.getId(), memberUidList);
            memberText.setText(getString(R.string.all_group_member,memberUidList.size()));
            filterGroupMember(memberUidList);
            adapter.notifyDataSetChanged();
        }

        @Override
        public void returnDelConversationGroupMemberFail(String error, int errorCode) {
            LoadingDialog.dimissDlg(loadingDlg);
            WebServiceMiddleUtils.hand(ConversationGroupInfoActivity.this, error, errorCode);
        }

        @Override
        public void returnQuitChannelGroupSuccess() {
            EventBus.getDefault().post(new SimpleEventMessage(Constant.EVENTBUS_TAG_QUIT_CHANNEL_GROUP,conversation));
            LoadingDialog.dimissDlg(loadingDlg);
            setResult(RESULT_OK);
            finish();
        }

        @Override
        public void returnQuitChannelGroupSuccessFail(String error, int errorCode) {
            LoadingDialog.dimissDlg(loadingDlg);
            WebServiceMiddleUtils.hand(ConversationGroupInfoActivity.this, error, errorCode);
        }


    }

}
