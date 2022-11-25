package com.inspur.emmcloud.ui.chat.mvp.view;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.SwitchCompat;
import android.text.TextUtils;
import android.view.View;
import android.widget.AdapterView;
import android.widget.CompoundButton;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.inspur.emmcloud.MyApplication;
import com.inspur.emmcloud.R;
import com.inspur.emmcloud.baselib.util.IntentUtils;
import com.inspur.emmcloud.baselib.util.JSONUtils;
import com.inspur.emmcloud.baselib.util.PreferencesUtils;
import com.inspur.emmcloud.baselib.util.ToastUtils;
import com.inspur.emmcloud.baselib.widget.LoadingDialog;
import com.inspur.emmcloud.baselib.widget.NoScrollGridView;
import com.inspur.emmcloud.baselib.widget.dialogs.CustomDialog;
import com.inspur.emmcloud.basemodule.application.BaseApplication;
import com.inspur.emmcloud.basemodule.bean.SimpleEventMessage;
import com.inspur.emmcloud.basemodule.config.Constant;
import com.inspur.emmcloud.basemodule.ui.BaseMvpActivity;
import com.inspur.emmcloud.basemodule.util.AppTabUtils;
import com.inspur.emmcloud.bean.chat.WSCommand;
import com.inspur.emmcloud.componentservice.communication.Conversation;
import com.inspur.emmcloud.ui.chat.ChannelMembersDelActivity;
import com.inspur.emmcloud.ui.chat.CommunicationSearchMessagesActivity;
import com.inspur.emmcloud.ui.chat.ConversationActivity;
import com.inspur.emmcloud.ui.chat.ConversationCastInfoActivity;
import com.inspur.emmcloud.ui.chat.ConversationMemberManagerIndexActivity;
import com.inspur.emmcloud.ui.chat.ConversationNameModifyActivity;
import com.inspur.emmcloud.ui.chat.ConversationQrCodeActivity;
import com.inspur.emmcloud.ui.chat.ConversationNicknameModifyActivity;
import com.inspur.emmcloud.ui.chat.FileTransferDetailActivity;
import com.inspur.emmcloud.ui.chat.GroupAlbumActivity;
import com.inspur.emmcloud.ui.chat.GroupFileActivity;
import com.inspur.emmcloud.ui.chat.MembersActivity;
import com.inspur.emmcloud.ui.chat.mvp.adapter.ConversationMembersHeadAdapter;
import com.inspur.emmcloud.ui.chat.mvp.contract.ConversationInfoContract;
import com.inspur.emmcloud.ui.chat.mvp.presenter.ConversationInfoPresenter;
import com.inspur.emmcloud.ui.contact.ContactSearchActivity;
import com.inspur.emmcloud.ui.contact.ContactSearchFragment;
import com.inspur.emmcloud.ui.contact.RobotInfoActivity;
import com.inspur.emmcloud.ui.contact.UserInfoActivity;
import com.inspur.emmcloud.util.privates.cache.ConversationCacheUtils;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

import static com.inspur.emmcloud.ui.chat.ConversationMemberManagerIndexActivity.INTENT_ADMIN_LIST;
import static com.inspur.emmcloud.ui.chat.ConversationMemberManagerIndexActivity.INTENT_SELECT_OWNER;
import static com.inspur.emmcloud.ui.chat.ConversationMemberManagerIndexActivity.INTENT_SILENT;

/**
 * Created by libaochao on 2019/10/12.
 */

public class ConversationInfoActivity extends BaseMvpActivity<ConversationInfoPresenter> implements ConversationInfoContract.View
        , CompoundButton.OnCheckedChangeListener {

    public static final String EXTRA_CID = "cid";
    public static final String EXTRA_NICKNAME = "nickname";
    public static final String EXTRA_FROM_CONVERSATION = "from_conversation"; // 来自群聊->查找聊天记录
    public static final String MEMBER_SIZE = "member_size";
    private static final int QEQUEST_ADD_MEMBER = 2;
    private static final int QEQUEST_DEL_MEMBER = 3;
    private static final int QEQUEST_FILE_TRANSFER = 4;
    public static final int QEQUEST_GROUP_MANAGE = 5;
    public static final int GROUP_TYPE_OWNER = 3;
    public static final int GROUP_TYPE_ADMINISTRATOR = 2;
    public static final int GROUP_TYPE_MEMBER = 1;
    private int mUserGroupType = GROUP_TYPE_MEMBER;

    @BindView(R.id.rv_conversation_members_head)
    NoScrollGridView conversationMembersHeadRecyclerView;
    @BindView(R.id.tv_title)
    TextView titleTextView;
    @BindView(R.id.tv_conversation_name)
    TextView conversationNameTextView;
    @BindView(R.id.tv_person_name)
    TextView personNameTextView;
    @BindView(R.id.rl_more_members)
    RelativeLayout moreMembersLayout;
    @BindView(R.id.rl_conversation_mute_notification)
    RelativeLayout muteNotificationLayout;
    @BindView(R.id.switch_conversation_sticky)
    SwitchCompat conversationStickySwitch;
    @BindView(R.id.switch_conversation_mute_notification)
    SwitchCompat conversationMuteNotificationSwitch;
    @BindView(R.id.switch_conversation_show)
    SwitchCompat conversationShowSwitch;
    @BindView(R.id.tv_conversation_quit_title)
    TextView quitTextView;
    @BindView(R.id.rl_conversation_qr)
    RelativeLayout conversationQRLayout;
    @BindView(R.id.rl_conversation_member_manager)
    RelativeLayout conversationMemberManagerLayout;
    @BindView(R.id.rl_conversation_name)
    RelativeLayout conversationNameLayout;
    @BindView(R.id.rl_person_name)
    RelativeLayout personNameLayout;
    @BindView(R.id.rl_conversation_quit)
    RelativeLayout conversationQuitLayout;
    @BindView(R.id.rl_conversation_search_record)
    RelativeLayout searchRecordLayout;
    @BindView(R.id.rl_channel_search_record_have_margin)
    RelativeLayout searchRecordMarginLayout;
    @BindView(R.id.rl_conversation_report)
    RelativeLayout conversationReportRl;
    @BindView(R.id.rl_conversation_show)
    RelativeLayout conversationShow;

    private Conversation uiConversation;
    private ConversationMembersHeadAdapter channelMembersHeadAdapter;
    private List<String> uiUidList = new ArrayList<>();
    private LoadingDialog loadingDialog;
    private boolean conversationNameChanged = false;
    private String mCid;

    @Override
    public void onCreate() {
        super.onCreate();
        ButterKnife.bind(this);
        EventBus.getDefault().register(this);
        mPresenter = new ConversationInfoPresenter();
        mPresenter.attachView(this);
        loadingDialog = new LoadingDialog(this);
        refreshMyConversation();
        if (uiConversation == null) {
            ToastUtils.show(getContext(), getString(R.string.net_request_failed));
            finish();
            return;
        }
        init();
    }

    @Override
    public int getLayoutResId() {
        return R.layout.activity_conversation_info;
    }

    /**
     * 初始化
     */
    private void init() {
        if (uiConversation.getType().equals(Conversation.TYPE_GROUP)) {
            String data = getString(R.string.chat_group_info_detail_title, mPresenter.getConversationRealMemberSize());
            if (uiConversation.getOwner().equals(BaseApplication.getInstance().getUid())) {
                mUserGroupType = GROUP_TYPE_OWNER;
            } else if (uiConversation.getAdministratorList().contains(BaseApplication.getInstance().getUid())) {
                mUserGroupType = GROUP_TYPE_ADMINISTRATOR;
            } else {
                mUserGroupType = GROUP_TYPE_MEMBER;
            }
            titleTextView.setText(data);
            conversationNameTextView.setText(uiConversation.getName());
            String membersDetail = uiConversation.getMembersDetail();
            String username = "";
            // 设置昵称
            if (!TextUtils.isEmpty(membersDetail)) {
                JSONArray array = JSONUtils.getJSONArray(membersDetail, new JSONArray());
                String uid = MyApplication.getInstance().getUid();
                for (int i = 0; i < array.length(); i++) {
                    JSONObject obj = JSONUtils.getJSONObject(array, i, new JSONObject());
                    if (uid.equals(JSONUtils.getString(obj, "user", ""))) {
                        String nickname = JSONUtils.getString(obj, "nickname", "");
                        if (TextUtils.isEmpty(nickname)) {
                            username = PreferencesUtils.getString(getActivity(), "userRealName", getString(R.string.not_set));
                            personNameTextView.setText(username);
                        } else {
                            username = nickname;
                            personNameTextView.setText(nickname);
                        }
                        break;
                    }
                }
                if (TextUtils.isEmpty(username)) {
                    username = PreferencesUtils.getString(getActivity(), "userRealName", getString(R.string.not_set));
                    personNameTextView.setText(username);
                }
            } else {
                String userRealName = PreferencesUtils.getString(getActivity(), "userRealName", getString(R.string.not_set));
                personNameTextView.setText(userRealName);
            }
            uiUidList = mPresenter.getConversationUIMembersUid(uiConversation);
            conversationQRLayout.setVisibility(View.VISIBLE);
            conversationNameLayout.setVisibility(View.VISIBLE);
            personNameLayout.setVisibility(View.VISIBLE);
            conversationQuitLayout.setVisibility(View.VISIBLE);
            searchRecordLayout.setVisibility(View.VISIBLE);
            searchRecordMarginLayout.setVisibility(View.GONE);
            mPresenter.updateSearchMoreState();
            conversationMemberManagerLayout.setVisibility(mUserGroupType > GROUP_TYPE_MEMBER ? View.VISIBLE : View.GONE);
            quitTextView.setText(mUserGroupType == GROUP_TYPE_OWNER ? getString(R.string.dismiss_group) : getString(R.string.quit_group));
        } else if (uiConversation.getType().equals(Conversation.TYPE_DIRECT) || uiConversation.getType().equals(Conversation.TYPE_TRANSFER)) {
            mUserGroupType = GROUP_TYPE_MEMBER;
            uiUidList = mPresenter.getConversationSingleChatUIMembersUid(uiConversation);
            titleTextView.setText(R.string.chat_single_info_detail_title);
            ((TextView) findViewById(R.id.tv_conversation_files_title)).setText(R.string.file);
            ((TextView) findViewById(R.id.tv_conversation_images)).setText(R.string.channel_single_chat_images);
            conversationQRLayout.setVisibility(View.GONE);
            conversationMemberManagerLayout.setVisibility(View.GONE);
            conversationNameLayout.setVisibility(View.GONE);
            personNameLayout.setVisibility(View.GONE);
            conversationQuitLayout.setVisibility(View.GONE);
            searchRecordLayout.setVisibility(View.GONE);
            searchRecordMarginLayout.setVisibility(View.VISIBLE);
            muteNotificationLayout.setVisibility(uiConversation.getType().equals(Conversation.TYPE_TRANSFER) ? View.GONE : View.VISIBLE);
        }
        conversationShowSwitch.setChecked(uiConversation.isHide());
        conversationShowSwitch.setOnCheckedChangeListener(this);
        conversationShow.setVisibility(uiConversation.isHide() ? View.VISIBLE : View.GONE);
        channelMembersHeadAdapter = new ConversationMembersHeadAdapter(this, uiUidList, uiConversation.getOwner(), uiConversation.getAdministratorList(), uiConversation.getMembersDetail());
        Configuration configuration = getResources().getConfiguration();
        // 适配横屏头像显示
        if (configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            conversationMembersHeadRecyclerView.setNumColumns(8);

        } else if (configuration.orientation == Configuration.ORIENTATION_PORTRAIT) {
            conversationMembersHeadRecyclerView.setNumColumns(5);

        }
        conversationMembersHeadRecyclerView.setAdapter(channelMembersHeadAdapter);
        conversationMembersHeadRecyclerView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
//                refreshMyConversation();
                if (uiConversation == null) {
                    ToastUtils.show(getContext(), getString(R.string.net_request_failed));
                    return;
                }
                Intent intent = new Intent();
                if (uiConversation.getType().equals(Conversation.TYPE_TRANSFER) && i == uiUidList.size() - 1) {
                    intent.putExtra(ConversationCastInfoActivity.EXTRA_CID, uiConversation.getId());
                    intent.setClass(getApplicationContext(),
                            FileTransferDetailActivity.class);
                    startActivityForResult(intent, QEQUEST_FILE_TRANSFER);
                } else if (i == uiUidList.size() - 1 && mUserGroupType > GROUP_TYPE_MEMBER) {    /**刪除群成員**/
                    if (mUserGroupType == GROUP_TYPE_ADMINISTRATOR) {
                        ArrayList<String> memberList = uiConversation.getMemberList();
                        memberList.removeAll(uiConversation.getAdministratorList());
                        memberList.remove(uiConversation.getOwner());
                        intent.putExtra("memberUidList", memberList);
                    } else {
                        intent.putExtra("memberUidList", uiConversation.getMemberList());
                    }
                    intent.setClass(getApplicationContext(),
                            ChannelMembersDelActivity.class);
                    startActivityForResult(intent, QEQUEST_DEL_MEMBER);
                } else if ((i == uiUidList.size() - 2 && mUserGroupType > GROUP_TYPE_MEMBER
                        || (i == uiUidList.size() - 1 && mUserGroupType == GROUP_TYPE_MEMBER)) && AppTabUtils.hasContactPermission(ConversationInfoActivity.this)) { /**添加群成員**/
                    intent.putExtra(ContactSearchFragment.EXTRA_TYPE, 2);
                    intent.putExtra(ContactSearchFragment.EXTRA_EXCLUDE_SELECT, uiConversation.getMemberList());
                    intent.putExtra(ContactSearchFragment.EXTRA_MULTI_SELECT, true);
                    intent.putExtra(ContactSearchFragment.EXTRA_TITLE, getString(R.string.add_group_member));
                    intent.setClass(getApplicationContext(), ContactSearchActivity.class);
                    // 单聊时点击+号为创建群聊，传入Uid
                    if (uiConversation.getType().equals(Conversation.TYPE_DIRECT)) {
                        intent.putExtra(ContactSearchFragment.EXTRA_CREATE_NEW_GROUP_FROM_DIRECT, true);
                        intent.putExtra(ContactSearchFragment.EXTRA_CREATE_NEW_GROUP_UID_DIRECT, uiUidList.get(0));
                    }
                    startActivityForResult(intent, QEQUEST_ADD_MEMBER);
                } else {
                    String uid = uiUidList.get(i);
                    Bundle bundle = new Bundle();
                    bundle.putString("uid", uid);
                    IntentUtils.startActivity(ConversationInfoActivity.this, uid.startsWith("BOT") ?
                            RobotInfoActivity.class : UserInfoActivity.class, bundle);
                }
            }
        });
        conversationStickySwitch.setChecked(uiConversation.isStick());
        conversationMuteNotificationSwitch.setChecked(uiConversation.isDnd());
        conversationStickySwitch.setOnCheckedChangeListener(this);
        conversationMuteNotificationSwitch.setOnCheckedChangeListener(this);
        if ("REMOVED".equals(uiConversation.getState())) {
            conversationMembersHeadRecyclerView.setVisibility(View.GONE);
            conversationQRLayout.setVisibility(View.GONE);
            conversationQuitLayout.setVisibility(View.GONE);
            conversationMemberManagerLayout.setVisibility(View.GONE);
            muteNotificationLayout.setVisibility(View.GONE);

        }
    }

    public void onClick(View v) {
//        refreshMyConversation();
        Bundle bundle = new Bundle();
        switch (v.getId()) {
            case R.id.ibt_back:
                if (conversationNameChanged) {
                    Intent intent = new Intent();
                    intent.putExtra("operate", 0);
                    intent.putExtra(INTENT_SILENT, uiConversation.isSilent());
                    intent.putExtra(INTENT_SELECT_OWNER, uiConversation.getOwner());
                    intent.putExtra(INTENT_ADMIN_LIST, uiConversation.getAdministrators());
                    setResult(RESULT_OK, intent);
                }
                finish();
                break;
            case R.id.rl_conversation_name:
                if ("REMOVED".equals(uiConversation.getState())) {
                    return;
                }
                if (uiConversation == null) {
                    ToastUtils.show(getContext(), getString(R.string.net_request_failed));
                    return;
                }
                bundle.putString(EXTRA_CID, uiConversation.getId());
                IntentUtils.startActivity(this,
                        ConversationNameModifyActivity.class, bundle);
                break;
            case R.id.rl_person_name:
                if ("REMOVED".equals(uiConversation.getState())) {
                    return;
                }
                if (uiConversation == null) {
                    ToastUtils.show(getContext(), getString(R.string.net_request_failed));
                    return;
                }
                bundle.putString(EXTRA_CID, uiConversation.getId());
                bundle.putString(EXTRA_NICKNAME, personNameTextView.getText().toString());
                IntentUtils.startActivity(this,
                        ConversationNicknameModifyActivity.class, bundle);
                break;
            case R.id.rl_conversation_qr:
                if (uiConversation == null) {
                    ToastUtils.show(getContext(), getString(R.string.net_request_failed));
                    return;
                }
                bundle.putString("cid", uiConversation.getId());
//                bundle.putString("groupName", uiConversation.getShowName());
                bundle.putString("groupName", conversationNameTextView.getText().toString());
                bundle.putInt(MEMBER_SIZE, mPresenter.getConversationRealMemberSize());
                IntentUtils.startActivity(this,
                        ConversationQrCodeActivity.class, bundle);
                break;
            case R.id.rl_conversation_member_manager:
                if (uiConversation == null) {
                    ToastUtils.show(getContext(), getString(R.string.net_request_failed));
                    return;
                }
                Intent memberManagerIntent = new Intent();
                memberManagerIntent.setClass(this, ConversationMemberManagerIndexActivity.class);
                memberManagerIntent.putExtra("cid", uiConversation.getId());
                memberManagerIntent.putExtra(ConversationMemberManagerIndexActivity.MANAGER_TYPE, mUserGroupType);
                startActivityForResult(memberManagerIntent, QEQUEST_GROUP_MANAGE);
                break;
            case R.id.rl_conversation_images:
                if (uiConversation == null) {
                    ToastUtils.show(getContext(), getString(R.string.net_request_failed));
                    return;
                }
                bundle.putString(EXTRA_CID, uiConversation.getId());
                IntentUtils.startActivity(this,
                        GroupAlbumActivity.class, bundle);
                break;
            case R.id.rl_conversation_files:
                if (uiConversation == null) {
                    ToastUtils.show(getContext(), getString(R.string.net_request_failed));
                    return;
                }
                bundle.putString(EXTRA_CID, uiConversation.getId());
                IntentUtils.startActivity(this,
                        GroupFileActivity.class, bundle);
                break;
            case R.id.rl_channel_search_record_have_margin:
            case R.id.rl_conversation_search_record:
                if (uiConversation == null) {
                    ToastUtils.show(getContext(), getString(R.string.net_request_failed));
                    return;
                }
                bundle.putString(EXTRA_CID, uiConversation.getId());
                bundle.putBoolean(EXTRA_FROM_CONVERSATION, true);
                IntentUtils.startActivity(this, CommunicationSearchMessagesActivity.class, bundle);
                break;
            case R.id.rl_conversation_quit:
                if (uiConversation == null) {
                    ToastUtils.show(getContext(), getString(R.string.net_request_failed));
                    return;
                }
                if (mUserGroupType == GROUP_TYPE_OWNER) {
                    showDelGroupWarningDlg();
                } else {
                    showQuitGroupWarningDlg();
                }
                break;
            case R.id.rl_more_members:
                if (uiConversation == null) {
                    ToastUtils.show(getContext(), getString(R.string.net_request_failed));
                    return;
                }
                bundle.putString("title", getString(R.string.group_member));
                bundle.putInt(MembersActivity.MEMBER_PAGE_STATE, MembersActivity.CHECK_STATE);
                bundle.putString(MembersActivity.CHAT_OWNER_UID, uiConversation.getOwner());
                bundle.putStringArrayList("uidList", uiConversation.getMemberList());
                IntentUtils.startActivity(this,
                        MembersActivity.class, bundle);
                break;
            case R.id.rl_conversation_report:
                IntentUtils.startActivity(getActivity(), ConversationReportActivity.class);
                break;
            default:
                break;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        mPresenter.getConversationInfo(mCid);
    }

    private void refreshMyConversation() {
        mCid = getIntent().getExtras().getString(EXTRA_CID);
//        mPresenter.getConversationInfo(mCid);
        uiConversation = mPresenter.getConversation(mCid);
    }


    @Override
    public void initView(Conversation conversation) {
        uiConversation = conversation;
        init();
    }

    @Override
    public void onBackPressed() {
        if (conversationNameChanged) {
            Intent intent = new Intent();
            intent.putExtra("operate", 0);
            intent.putExtra(INTENT_SILENT, uiConversation.isSilent());
            intent.putExtra(INTENT_SELECT_OWNER, uiConversation.getOwner());
            intent.putExtra(INTENT_ADMIN_LIST, uiConversation.getAdministrators());
            setResult(RESULT_OK, intent);
            finish();
        }
        super.onBackPressed();
    }


    @Override
    public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
//        refreshMyConversation();
        switch (compoundButton.getId()) {
            case R.id.switch_conversation_sticky:
                if (uiConversation == null) {
                    ToastUtils.show(getContext(), getString(R.string.net_request_failed));
                    conversationStickySwitch.setChecked(!b);
                    return;
                }
                if (!b == uiConversation.isStick()) {
                    loadingDialog.show();
                    mPresenter.setConversationStick(b, uiConversation.getId());
                }
                break;
            case R.id.switch_conversation_mute_notification:
                if (uiConversation == null) {
                    ToastUtils.show(getContext(), getString(R.string.net_request_failed));
                    conversationMuteNotificationSwitch.setChecked(!b);
                    return;
                }
                if (!b == uiConversation.isDnd()) {
                    loadingDialog.show();
                    mPresenter.setMuteNotification(b, uiConversation.getId());
                }
                break;
            case R.id.switch_conversation_show:
                if (uiConversation == null) {
                    ToastUtils.show(getContext(), getString(R.string.net_request_failed));
                    conversationMuteNotificationSwitch.setChecked(!b);
                    return;
                }
                if (!b == uiConversation.isHide()) {
                    ConversationCacheUtils.updateConversationHide(MyApplication.getInstance(), uiConversation.getId(), false);
                    EventBus.getDefault().post(new SimpleEventMessage(Constant.EVENTBUS_TAG_CONVERSATION_MESSAGE_DATA_CHANGED, uiConversation.getId()));
                }
                break;
            default:
                break;
        }
    }

    @Override
    public void showGroupMembersHead(List<String> uiUidList) {
        this.uiUidList = uiUidList;
        channelMembersHeadAdapter.setUIUidList(uiUidList);
        channelMembersHeadAdapter.notifyDataSetChanged();
    }

    @Override
    public void showStickyState(boolean isSticky) {
        uiConversation.setStick(isSticky);
        conversationStickySwitch.setChecked(isSticky);
        ConversationCacheUtils.setConversationStick(MyApplication.getInstance(), uiConversation.getId(), isSticky);
        EventBus.getDefault().post(new SimpleEventMessage(Constant.EVENTBUS_TAG_CONVERSATION_SELF_DATA_CHANGED, uiConversation));
    }

    @Override
    public void showDNDState(boolean isDND) {
        uiConversation.setDnd(isDND);
        conversationMuteNotificationSwitch.setChecked(uiConversation.isDnd());
        ConversationCacheUtils.updateConversationDnd(MyApplication.getInstance(), uiConversation.getId(), uiConversation.isDnd());
        EventBus.getDefault().post(new SimpleEventMessage(Constant.EVENTBUS_TAG_UPDATE_CHANNEL_DND, uiConversation));
    }

    @Override
    public void changeConversationTitle(int memberSize) {
        String data = getString(R.string.chat_group_info_detail_title, uiConversation.getMemberList().size());
        titleTextView.setText(data);
    }

    @Override
    public void updateUiConversation(Conversation conversation) {
        uiConversation = conversation;
        moreMembersLayout.setVisibility(uiConversation.getMemberList().size() > 43 ? View.VISIBLE : View.GONE);
    }

    @Override
    public void updateMoreMembers(boolean isShow) {
        moreMembersLayout.setVisibility(isShow ? View.VISIBLE : View.GONE);
    }

    @Override
    public void quitGroupSuccess() {
        ConversationCacheUtils.deleteConversation(MyApplication.getInstance(), uiConversation.getId());
        EventBus.getDefault().post(new SimpleEventMessage(Constant.EVENTBUS_TAG_QUIT_CHANNEL_GROUP, uiConversation));
        Intent intent = new Intent();
        intent.putExtra("operate", 1);
        intent.putExtra(INTENT_SILENT, uiConversation.isSilent());
        setResult(RESULT_OK, intent);
        finish();
    }

    @Override
    public void deleteGroupSuccess() {
//        ConversationCacheUtils.deleteConversation(MyApplication.getInstance(), uiConversation.getId());
        // 解散群组后，设置群聊state：REMOVED
        ConversationCacheUtils.updateConversationState(MyApplication.getInstance(), uiConversation.getId(), "REMOVED");
//        EventBus.getDefault().post(new SimpleEventMessage(Constant.EVENTBUS_TAG_QUIT_CHANNEL_GROUP, uiConversation));
        Intent intent = new Intent();
        intent.putExtra("operate", 1);
        intent.putExtra(INTENT_SILENT, uiConversation.isSilent());
        setResult(RESULT_OK, intent);
        finish();
    }

    @Override
    public void createGroupSuccess(Conversation conversation) {
        Bundle bundle = new Bundle();
        EventBus.getDefault().post(new SimpleEventMessage(Constant.EVENTBUS_TAG_CHAT_CHANGE, conversation));
        bundle.putSerializable(ConversationActivity.EXTRA_CONVERSATION, conversation);
        IntentUtils.startActivity(getActivity(), ConversationActivity.class, bundle);
        finish();
    }

    private void showQuitGroupWarningDlg() {
        new CustomDialog.MessageDialogBuilder(ConversationInfoActivity.this)
                .setMessage(getString(R.string.quit_group_warning_text))
                .setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        loadingDialog.show();
                        mPresenter.quitGroupChannel();
                        dialog.dismiss();
                    }
                })
                .show();
    }

    private void showDelGroupWarningDlg() {
        new CustomDialog.MessageDialogBuilder(ConversationInfoActivity.this)
                .setMessage(getString(R.string.dismiss_group_warning_text))
                .setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        loadingDialog.show();
                        mPresenter.delChannel();
                        dialog.dismiss();
                    }
                })
                .show();
    }

    /**
     * 修改群组名称
     *
     * @param eventMessage
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onReceiveConversationNameUpdate(SimpleEventMessage eventMessage) {
        if (eventMessage.getAction().equals(Constant.EVENTBUS_TAG_UPDATE_CHANNEL_NAME)) {
            Conversation conversation = ((Conversation) eventMessage.getMessageObj());
            if (uiConversation.getId().equals(conversation.getId())) {
                uiConversation = mPresenter.getConversation(conversation.getId());
                init();
            }
        } else if (eventMessage.getAction().equals(Constant.EVENTBUS_TAG_GROUP_CONVERSATION_DISSOLVE)) {
            // 群解散后不可发送消息，保留消息记录
            WSCommand messageObj = (WSCommand) eventMessage.getMessageObj();
            if (messageObj.getChannel().equals(uiConversation.getId())) {
                uiConversation.setState("REMOVED");
                if ("REMOVED".equals(uiConversation.getState())) {
                    conversationMembersHeadRecyclerView.setVisibility(View.GONE);
                    conversationQRLayout.setVisibility(View.GONE);
                    conversationQuitLayout.setVisibility(View.GONE);
                    conversationMemberManagerLayout.setVisibility(View.GONE);
                }
            }
        } else if (eventMessage.getAction().equals(Constant.EVENTBUS_TAG_UPDATE_NICK_NAME)) {
            // 修改昵称
            String nickname = ((String) eventMessage.getMessageObj());
            personNameTextView.setText(nickname);
        } else if (eventMessage.getAction().equals(Constant.EVENTBUS_TAG_GROUP_CONVERSATION_MEMBER_NICKNAME_UPGRADE)) {
            // 群成员修改昵称，更新
            Conversation changeConversation = ConversationCacheUtils.getConversation(MyApplication.getInstance(), uiConversation.getId());
            if (changeConversation != null && !TextUtils.isEmpty(changeConversation.getMembersDetail()) && !changeConversation.getMembersDetail().equals(uiConversation.getMembersDetail())) {
                channelMembersHeadAdapter.updateMembersDetail(changeConversation.getMembersDetail());
            }
        }
    }

    /**
     * 添加 刪除人員
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        mPresenter.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void showLoading() {
        loadingDialog.show();
        super.showLoading();
    }

    @Override
    public void dismissLoading() {
        LoadingDialog.dimissDlg(loadingDialog);
        super.dismissLoading();
    }

    @Override
    public void activityFinish() {
        finish();
    }

    @Override
    public void updateGroupNameSuccess() {
        conversationNameChanged = true;
        conversationNameTextView.setText(uiConversation.getName());
    }

    @Override
    public void updateGroupTransferSuccess(String owner) {
        channelMembersHeadAdapter.setOwner(owner);
        channelMembersHeadAdapter.setAdminList(uiConversation.getAdministratorList());
        uiConversation.setOwner(owner);
        quitTextView.setText(getString(R.string.quit_group));
        if (uiConversation.getAdministratorList().contains(BaseApplication.getInstance().getUid())) {
            //管理员
            mUserGroupType = GROUP_TYPE_ADMINISTRATOR;
            conversationMemberManagerLayout.setVisibility(View.VISIBLE);
        } else {
            //普通员工
            mUserGroupType = GROUP_TYPE_MEMBER;
            conversationMemberManagerLayout.setVisibility(View.GONE);
            uiUidList.remove("deleteUser");
        }
        channelMembersHeadAdapter.notifyDataSetChanged();

    }

    @Override
    public void updateAdminList(List<String> adminList) {
        channelMembersHeadAdapter.setAdminList(adminList);
        channelMembersHeadAdapter.notifyDataSetChanged();
    }

    @Override
    public void updateSilent(boolean isSilent) {
        uiConversation.setSilent(isSilent);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }
}
