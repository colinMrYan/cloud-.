package com.inspur.emmcloud.ui.chat.mvp.view;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.SwitchCompat;
import android.view.View;
import android.widget.AdapterView;
import android.widget.CompoundButton;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.inspur.emmcloud.MyApplication;
import com.inspur.emmcloud.R;
import com.inspur.emmcloud.baselib.util.IntentUtils;
import com.inspur.emmcloud.baselib.widget.LoadingDialog;
import com.inspur.emmcloud.baselib.widget.NoScrollGridView;
import com.inspur.emmcloud.baselib.widget.dialogs.CustomDialog;
import com.inspur.emmcloud.basemodule.application.BaseApplication;
import com.inspur.emmcloud.basemodule.bean.SimpleEventMessage;
import com.inspur.emmcloud.basemodule.config.Constant;
import com.inspur.emmcloud.basemodule.ui.BaseMvpActivity;
import com.inspur.emmcloud.componentservice.communication.Conversation;
import com.inspur.emmcloud.ui.chat.ChannelMembersDelActivity;
import com.inspur.emmcloud.ui.chat.CommunicationSearchMessagesActivity;
import com.inspur.emmcloud.ui.chat.ConversationActivity;
import com.inspur.emmcloud.ui.chat.ConversationCastInfoActivity;
import com.inspur.emmcloud.ui.chat.ConversationNameModifyActivity;
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

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by libaochao on 2019/10/12.
 */

public class ConversationInfoActivity extends BaseMvpActivity<ConversationInfoPresenter> implements ConversationInfoContract.View
        , CompoundButton.OnCheckedChangeListener {

    public static final String EXTRA_CID = "cid";
    public static final String MEMBER_SIZE = "member_size";
    private static final int QEQUEST_ADD_MEMBER = 2;
    private static final int QEQUEST_DEL_MEMBER = 3;
    private static final int QEQUEST_FILE_TRANSFER = 4;
    @BindView(R.id.rv_conversation_members_head)
    NoScrollGridView conversationMembersHeadRecyclerView;
    @BindView(R.id.tv_title)
    TextView titleTextView;
    @BindView(R.id.tv_conversation_name)
    TextView conversationNameTextView;
    @BindView(R.id.rl_more_members)
    RelativeLayout moreMembersLayout;
    @BindView(R.id.rl_conversation_mute_notification)
    RelativeLayout muteNotificationLayout;
    @BindView(R.id.switch_conversation_sticky)
    SwitchCompat conversationStickySwitch;
    @BindView(R.id.switch_conversation_mute_notification)
    SwitchCompat conversationMuteNotificationSwitch;
    @BindView(R.id.tv_conversation_quit_title)
    TextView quitTextView;
    @BindView(R.id.rl_conversation_qr)
    RelativeLayout conversationQRLayout;
    @BindView(R.id.rl_conversation_name)
    RelativeLayout conversationNameLayout;
    @BindView(R.id.rl_conversation_quit)
    RelativeLayout conversationQuitLayout;
    @BindView(R.id.rl_conversation_search_record)
    RelativeLayout searchRecordLayout;
    @BindView(R.id.rl_channel_search_record_have_margin)
    RelativeLayout searchRecordMarginLayout;

    private Conversation uiConversation;
    private ConversationMembersHeadAdapter channelMembersHeadAdapter;
    private List<String> uiUidList = new ArrayList<>();
    private boolean isOwner = false;
    private LoadingDialog loadingDialog;

    @Override
    public void onCreate() {
        super.onCreate();
        ButterKnife.bind(this);
        EventBus.getDefault().register(this);
        mPresenter = new ConversationInfoPresenter();
        mPresenter.attachView(this);
        String cid = getIntent().getExtras().getString(EXTRA_CID);
        loadingDialog = new LoadingDialog(this);
        mPresenter.getConversationInfo(cid);
        uiConversation = mPresenter.getConversation(cid);
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
            isOwner = uiConversation.getOwner().equals(BaseApplication.getInstance().getUid());
            titleTextView.setText(data);
            conversationNameTextView.setText(uiConversation.getName());
            uiUidList = mPresenter.getConversationUIMembersUid(uiConversation);
            conversationQRLayout.setVisibility(View.VISIBLE);
            conversationNameLayout.setVisibility(View.VISIBLE);
            conversationQuitLayout.setVisibility(View.VISIBLE);
            searchRecordLayout.setVisibility(View.VISIBLE);
            searchRecordMarginLayout.setVisibility(View.GONE);
            mPresenter.updateSearchMoreState();
        } else if (uiConversation.getType().equals(Conversation.TYPE_DIRECT) || uiConversation.getType().equals(Conversation.TYPE_TRANSFER)) {
            isOwner = false;
            uiUidList = mPresenter.getConversationSingleChatUIMembersUid(uiConversation);
            titleTextView.setText(R.string.chat_single_info_detail_title);
            ((TextView) findViewById(R.id.tv_conversation_files_title)).setText(R.string.file);
            ((TextView) findViewById(R.id.tv_conversation_images)).setText(R.string.channel_single_chat_images);
            conversationQRLayout.setVisibility(View.GONE);
            conversationNameLayout.setVisibility(View.GONE);
            conversationQuitLayout.setVisibility(View.GONE);
            searchRecordLayout.setVisibility(View.GONE);
            searchRecordMarginLayout.setVisibility(View.VISIBLE);
            muteNotificationLayout.setVisibility(uiConversation.getType().equals(Conversation.TYPE_TRANSFER) ? View.GONE : View.VISIBLE);
        }
        channelMembersHeadAdapter = new ConversationMembersHeadAdapter(this, isOwner, uiUidList);
        conversationMembersHeadRecyclerView.setAdapter(channelMembersHeadAdapter);
        conversationMembersHeadRecyclerView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Intent intent = new Intent();
                if (uiConversation.getType().equals(Conversation.TYPE_TRANSFER) && i == uiUidList.size() - 1) {
                    intent.putExtra(ConversationCastInfoActivity.EXTRA_CID, uiConversation.getId());
                    intent.setClass(getApplicationContext(),
                            FileTransferDetailActivity.class);
                    startActivityForResult(intent, QEQUEST_FILE_TRANSFER);
                } else if (i == uiUidList.size() - 1 && isOwner) {    /**刪除群成員**/
                    intent.putExtra("memberUidList", uiConversation.getMemberList());
                    intent.setClass(getApplicationContext(),
                            ChannelMembersDelActivity.class);
                    startActivityForResult(intent, QEQUEST_DEL_MEMBER);
                } else if (i == uiUidList.size() - 2 && isOwner
                        || (i == uiUidList.size() - 1 && !isOwner)) { /**添加群成員**/
                    intent.putExtra(ContactSearchFragment.EXTRA_TYPE, 2);
                    intent.putExtra(ContactSearchFragment.EXTRA_EXCLUDE_SELECT, uiConversation.getMemberList());
                    intent.putExtra(ContactSearchFragment.EXTRA_MULTI_SELECT, true);
                    intent.putExtra(ContactSearchFragment.EXTRA_TITLE, getString(R.string.add_group_member));
                    intent.setClass(getApplicationContext(),
                            ContactSearchActivity.class);
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
    }

    public void onClick(View v) {
        Bundle bundle = new Bundle();
        switch (v.getId()) {
            case R.id.ibt_back:
                finish();
                break;
            case R.id.rl_conversation_name:
                bundle.putString(EXTRA_CID, uiConversation.getId());
                IntentUtils.startActivity(this,
                        ConversationNameModifyActivity.class, bundle);
                break;
            case R.id.rl_conversation_qr:
//                bundle.putString("cid", uiConversation.getId());
//                bundle.putString("groupName", uiConversation.getShowName());
//                bundle.putInt(MEMBER_SIZE, uiConversation.getMemberList().size());
//                IntentUtils.startActivity(this,
//                        ConversationQrCodeActivity.class, bundle);
                break;
            case R.id.rl_conversation_images:
                bundle.putString(EXTRA_CID, uiConversation.getId());
                IntentUtils.startActivity(this,
                        GroupAlbumActivity.class, bundle);
                break;
            case R.id.rl_conversation_files:
                bundle.putString(EXTRA_CID, uiConversation.getId());
                IntentUtils.startActivity(this,
                        GroupFileActivity.class, bundle);
                break;
            case R.id.rl_channel_search_record_have_margin:
            case R.id.rl_conversation_search_record:
                bundle.putString(EXTRA_CID, uiConversation.getId());
                IntentUtils.startActivity(this, CommunicationSearchMessagesActivity.class, bundle);
                break;
            case R.id.rl_conversation_quit:
                if (uiConversation.getOwner().equals(MyApplication.getInstance().getUid())) {
                    showDelGroupWarningDlg();
                } else {
                    showQuitGroupWarningDlg();
                }
                break;
            case R.id.rl_more_members:
                bundle.putString("title", getString(R.string.group_member));
                bundle.putInt(MembersActivity.MEMBER_PAGE_STATE, MembersActivity.CHECK_STATE);
                bundle.putStringArrayList("uidList", uiConversation.getMemberList());
                IntentUtils.startActivity(this,
                        MembersActivity.class, bundle);
                break;
            default:
                break;
        }
    }

    @Override
    public void initView(Conversation conversation) {
        uiConversation = conversation;
        init();
    }

    @Override
    public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
        switch (compoundButton.getId()) {
            case R.id.switch_conversation_sticky:
                if (!b == uiConversation.isStick()) {
                    loadingDialog.show();
                    mPresenter.setConversationStick(b, uiConversation.getId());
                }
                break;
            case R.id.switch_conversation_mute_notification:
                if (!b == uiConversation.isDnd()) {
                    loadingDialog.show();
                    mPresenter.setMuteNotification(b, uiConversation.getId());
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
        setResult(RESULT_OK);
        finish();
    }

    @Override
    public void deleteGroupSuccess() {
        ConversationCacheUtils.deleteConversation(MyApplication.getInstance(), uiConversation.getId());
        EventBus.getDefault().post(new SimpleEventMessage(Constant.EVENTBUS_TAG_QUIT_CHANNEL_GROUP, uiConversation));
        setResult(RESULT_OK);
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
    public void onReciverConversationNameUpdate(SimpleEventMessage eventMessage) {
        if (eventMessage.getAction().equals(Constant.EVENTBUS_TAG_UPDATE_CHANNEL_NAME)) {
            String name = ((Conversation) eventMessage.getMessageObj()).getName();
            conversationNameTextView.setText(name);
            uiConversation.setName(name);
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
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }
}
