package com.inspur.emmcloud.ui.chat.mvp.view;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SwitchCompat;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.TextView;

import com.inspur.emmcloud.MyApplication;
import com.inspur.emmcloud.R;
import com.inspur.emmcloud.baselib.util.IntentUtils;
import com.inspur.emmcloud.baselib.widget.LoadingDialog;
import com.inspur.emmcloud.baselib.widget.dialogs.CustomDialog;
import com.inspur.emmcloud.basemodule.application.BaseApplication;
import com.inspur.emmcloud.basemodule.bean.SearchModel;
import com.inspur.emmcloud.basemodule.bean.SimpleEventMessage;
import com.inspur.emmcloud.basemodule.config.Constant;
import com.inspur.emmcloud.basemodule.ui.BaseMvpActivity;
import com.inspur.emmcloud.bean.chat.Conversation;
import com.inspur.emmcloud.ui.chat.ChannelMembersDelActivity;
import com.inspur.emmcloud.ui.chat.CommunicationSearchMessagesActivity;
import com.inspur.emmcloud.ui.chat.ConversationNameModifyActivity;
import com.inspur.emmcloud.ui.chat.GroupAlbumActivity;
import com.inspur.emmcloud.ui.chat.GroupFileActivity;
import com.inspur.emmcloud.ui.chat.mvp.adapter.ChannelMembersHeadAdapter;
import com.inspur.emmcloud.ui.chat.mvp.contract.ChannelGroupInfoContract;
import com.inspur.emmcloud.ui.chat.mvp.presenter.ChannelGroupInfoPresenter;
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
import butterknife.OnClick;

/**
 * Created by libaochao on 2019/10/12.
 */

public class ChannelGroupInfoActivity extends BaseMvpActivity<ChannelGroupInfoPresenter> implements ChannelGroupInfoContract.View
        , CompoundButton.OnCheckedChangeListener {

    public static final String EXTRA_CID = "cid";
    private static final int QEQUEST_ADD_MEMBER = 2;
    private static final int QEQUEST_DEL_MEMBER = 3;
    @BindView(R.id.rv_group_members_head)
    RecyclerView groupMembersHeadRecyclerView;
    @BindView(R.id.tv_title)
    TextView titleTextView;
    @BindView(R.id.tv_group_name)
    TextView groupNameTextView;
    @BindView(R.id.tv_more_members)
    TextView groupMoreMemberTV;
    @BindView(R.id.switch_group_sticky)
    SwitchCompat groupStickySwitch;
    @BindView(R.id.switch_group_mute_notification)
    SwitchCompat groupMuteNotificationSwitch;
    @BindView(R.id.tv_group_quit_title)
    TextView quitTextView;
    private Conversation conversation;
    private ChannelMembersHeadAdapter channelMembersHeadAdapter;
    private List<String> uiUidList = new ArrayList<>();
    private boolean isOwner = false;
    private LoadingDialog loadingDialog;

    @Override
    public void onCreate() {
        super.onCreate();
        ButterKnife.bind(this);
        EventBus.getDefault().register(this);
        mPresenter = new ChannelGroupInfoPresenter();
        mPresenter.attachView(this);
        init();
    }

    @Override
    public int getLayoutResId() {
        return R.layout.activity_group_info_detail;
    }

    /**
     * 初始化
     */
    private void init() {
        String cid = getIntent().getExtras().getString(EXTRA_CID);
        loadingDialog = new LoadingDialog(this);
        conversation = mPresenter.getConversation(cid);
        String data = getString(R.string.chat_group_info_detail_title, conversation.getMemberList().size());
        titleTextView.setText(data);
        groupNameTextView.setText(conversation.getName());
        groupMoreMemberTV.setVisibility(conversation.getMemberList().size() > 13 ? View.VISIBLE : View.GONE);
        isOwner = conversation.getOwner().equals(BaseApplication.getInstance().getUid());
        quitTextView.setText(conversation.getOwner().equals(MyApplication.getInstance().getUid()) ? getString(R.string.dismiss_group) : getString(R.string.quit_group));
        uiUidList = mPresenter.getGroupUIMembersUid(conversation);
        channelMembersHeadAdapter = new ChannelMembersHeadAdapter(this, isOwner, uiUidList);
        GridLayoutManager gridLayoutManager = new GridLayoutManager(MyApplication.getInstance(), 5);
        groupMembersHeadRecyclerView.setLayoutManager(gridLayoutManager);
        channelMembersHeadAdapter.setAdapterListener(new ChannelMembersHeadAdapter.AdapterListener() {
            @Override
            public void onItemClick(View view, int position) {
                Intent intent = new Intent();
                if (position == uiUidList.size() - 1 && isOwner) {    /**刪除群成員**/
                    intent.putExtra("memberUidList", conversation.getMemberList());
                    intent.setClass(getApplicationContext(),
                            ChannelMembersDelActivity.class);
                    startActivityForResult(intent, QEQUEST_DEL_MEMBER);
                } else if (position == uiUidList.size() - 2 && isOwner
                        || (position == uiUidList.size() - 1 && !isOwner)) { /**添加群成員**/
                    intent.putExtra(ContactSearchFragment.EXTRA_TYPE, 2);
                    intent.putExtra(ContactSearchFragment.EXTRA_EXCLUDE_SELECT, conversation.getMemberList());
                    intent.putExtra(ContactSearchFragment.EXTRA_MULTI_SELECT, true);
                    intent.putExtra(ContactSearchFragment.EXTRA_TITLE, getString(R.string.add_group_member));
                    intent.setClass(getApplicationContext(),
                            ContactSearchActivity.class);
                    startActivityForResult(intent, QEQUEST_ADD_MEMBER);
                } else {
                    String uid = uiUidList.get(position);
                    Bundle bundle = new Bundle();
                    bundle.putString("uid", uid);
                    IntentUtils.startActivity(ChannelGroupInfoActivity.this, uid.startsWith("BOT") ?
                            RobotInfoActivity.class : UserInfoActivity.class, bundle);
                }
            }
        });
        groupMembersHeadRecyclerView.setAdapter(channelMembersHeadAdapter);
        groupStickySwitch.setChecked(conversation.isStick());
        groupMuteNotificationSwitch.setChecked(conversation.isDnd());
        groupStickySwitch.setOnCheckedChangeListener(this);
        groupMuteNotificationSwitch.setOnCheckedChangeListener(this);
    }

    @OnClick(R.id.ibt_back)
    public void onClick(View v) {
        Bundle bundle = new Bundle();
        switch (v.getId()) {
            case R.id.ibt_back:
                finish();
                break;
            case R.id.rl_group_name:
                bundle.putString(EXTRA_CID, conversation.getId());
                IntentUtils.startActivity(this,
                        ConversationNameModifyActivity.class, bundle);
                break;
            case R.id.rl_group_qr:
                break;
            case R.id.rl_group_images:
                bundle.putString(EXTRA_CID, conversation.getId());
                IntentUtils.startActivity(this,
                        GroupAlbumActivity.class, bundle);
                break;
            case R.id.rl_group_files:
                bundle.putString(EXTRA_CID, conversation.getId());
                IntentUtils.startActivity(this,
                        GroupFileActivity.class, bundle);
                break;
            case R.id.rl_group_search_record:
                bundle.putString(EXTRA_CID, conversation.getId());
                IntentUtils.startActivity(this, CommunicationSearchMessagesActivity.class, bundle);
                break;
            case R.id.rl_group_quit:
                if (conversation.getOwner().equals(MyApplication.getInstance().getUid())) {
                    showDimissGroupWarningDlg();
                } else {
                    showQuitGroupWarningDlg();
                }
                break;
            case R.id.tv_more_members:
                break;
            default:
                break;
        }
    }

    @Override
    public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
        switch (compoundButton.getId()) {
            case R.id.switch_group_sticky:
                if (!b == conversation.isStick()) {
                    loadingDialog.show();
                    mPresenter.setConversationStick(b, conversation.getId());
                }
                break;
            case R.id.switch_group_mute_notification:
                if (!b == conversation.isDnd()) {
                    loadingDialog.show();
                    mPresenter.setMuteNotification(b, conversation.getId());
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
        conversation.setStick(isSticky);
        groupStickySwitch.setChecked(isSticky);
        ConversationCacheUtils.setConversationStick(MyApplication.getInstance(), conversation.getId(), isSticky);
        EventBus.getDefault().post(new SimpleEventMessage(Constant.EVENTBUS_TAG_UPDATE_CHANNEL_FOCUS, conversation));
    }

    @Override
    public void showDNDState(boolean isDND) {
        conversation.setDnd(isDND);
        groupMuteNotificationSwitch.setChecked(conversation.isDnd());
        ConversationCacheUtils.updateConversationDnd(MyApplication.getInstance(), conversation.getId(), conversation.isDnd());
        EventBus.getDefault().post(new SimpleEventMessage(Constant.EVENTBUS_TAG_UPDATE_CHANNEL_DND, conversation));
    }

    @Override
    public void changeConversationTitle(int memberSize) {
        String data = getString(R.string.chat_group_info_detail_title, conversation.getMemberList().size());
        titleTextView.setText(data);
    }

    @Override
    public void finishActivity() {
        this.finish();
    }

    private void showQuitGroupWarningDlg() {
        new CustomDialog.MessageDialogBuilder(ChannelGroupInfoActivity.this)
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

    private void showDimissGroupWarningDlg() {
        new CustomDialog.MessageDialogBuilder(ChannelGroupInfoActivity.this)
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
            groupNameTextView.setText(name);
            conversation.setName(name);

        }
    }

    /**
     * 添加 刪除人員
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case QEQUEST_ADD_MEMBER:
                    ArrayList<String> addUidList = new ArrayList<>();
                    List<SearchModel> addMemberList = (List<SearchModel>) data
                            .getSerializableExtra("selectMemList");
                    if (addMemberList.size() > 0) {
                        for (int i = 0; i < addMemberList.size(); i++) {
                            addUidList.add(addMemberList.get(i).getId());
                        }
                        loadingDialog.show();
                        mPresenter.addGroupMembers(addUidList, conversation.getId());
                    }
                    break;
                case QEQUEST_DEL_MEMBER:
                    ArrayList<String> delUidList = (ArrayList<String>) data.getSerializableExtra("selectMemList");
                    if (delUidList.size() > 0) {
                        loadingDialog.show();
                        mPresenter.delGroupMembers(delUidList, conversation.getId());
                    }
                    break;
                default:
                    break;
            }
        }
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
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }
}
