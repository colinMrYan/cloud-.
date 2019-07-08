package com.inspur.emmcloud.ui.chat;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.widget.SwitchCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.CompoundButton;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.inspur.emmcloud.MyApplication;
import com.inspur.emmcloud.R;
import com.inspur.emmcloud.api.APIInterfaceInstance;
import com.inspur.emmcloud.api.APIUri;
import com.inspur.emmcloud.api.apiservice.ChatAPIService;
import com.inspur.emmcloud.baselib.util.ImageUtils;
import com.inspur.emmcloud.baselib.util.IntentUtils;
import com.inspur.emmcloud.baselib.util.PinyinUtils;
import com.inspur.emmcloud.baselib.util.StringUtils;
import com.inspur.emmcloud.baselib.widget.CircleTextImageView;
import com.inspur.emmcloud.baselib.widget.LoadingDialog;
import com.inspur.emmcloud.baselib.widget.NoScrollGridView;
import com.inspur.emmcloud.baselib.widget.dialogs.CustomDialog;
import com.inspur.emmcloud.basemodule.bean.SearchModel;
import com.inspur.emmcloud.basemodule.config.Constant;
import com.inspur.emmcloud.basemodule.config.MyAppConfig;
import com.inspur.emmcloud.basemodule.ui.BaseActivity;
import com.inspur.emmcloud.basemodule.util.ImageDisplayUtils;
import com.inspur.emmcloud.basemodule.util.NetUtils;
import com.inspur.emmcloud.basemodule.util.WebServiceMiddleUtils;
import com.inspur.emmcloud.bean.chat.Channel;
import com.inspur.emmcloud.bean.chat.ChannelGroup;
import com.inspur.emmcloud.bean.system.SimpleEventMessage;
import com.inspur.emmcloud.componentservice.contact.ContactUser;
import com.inspur.emmcloud.ui.contact.ContactSearchActivity;
import com.inspur.emmcloud.ui.contact.RobotInfoActivity;
import com.inspur.emmcloud.ui.contact.UserInfoActivity;
import com.inspur.emmcloud.util.privates.cache.ChannelCacheUtils;
import com.inspur.emmcloud.util.privates.cache.ChannelGroupCacheUtils;
import com.inspur.emmcloud.util.privates.cache.ChannelOperationCacheUtils;
import com.inspur.emmcloud.util.privates.cache.ContactUserCacheUtils;

import org.greenrobot.eventbus.EventBus;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * 频道详情页面
 *
 * @author Administrator
 */
public class ChannelInfoActivity extends BaseActivity {

    private static final int REQUEST_UPDATE_CHANNEL_NAME = 1;
    private static final int QEQUEST_ADD_MEMBER = 2;
    private static final int QEQUEST_DEL_MEMBER = 3;
    private NoScrollGridView memberGrid;
    private LoadingDialog loadingDlg;
    private ArrayList<String> uiMemberList = new ArrayList<>();
    private String cid;
    private ChannelGroup channelGroup;
    private SwitchCompat setTopSwitch;
    private SwitchCompat msgInterruptionSwitch;
    private ChatAPIService apiService;
    private Adapter adapter;
    private TextView channelMemberNumText;
    private boolean isNoInterruption = false;
    private CircleTextImageView groupPhotoImg;
    private TextView groupMembersText;
    private TextView groupMemberSizeText;
    private TextView nameText;
    private RelativeLayout groupMessageSearchLayout;
    private OnItemClickListener onItemClickListener = new OnItemClickListener() {

        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position,
                                long id) {
            // TODO Auto-generated method stub
            boolean isOwner = MyApplication.getInstance().getUid().equals(channelGroup.getOwner());
            Intent intent = new Intent();
            if ((position == adapter.getCount() - 1) && isOwner) {
                intent.putExtra("memberUidList", channelGroup.getMemberList());
                intent.setClass(getApplicationContext(),
                        ChannelMembersDelActivity.class);
                startActivityForResult(intent, QEQUEST_DEL_MEMBER);

            } else if (((position == adapter.getCount() - 2) && isOwner)
                    || ((position == adapter.getCount() - 1) && !isOwner)) {
                intent.putExtra("select_content", 2);
                intent.putExtra("isMulti_select", true);
                intent.putExtra("title", getString(R.string.add_group_member));
                intent.setClass(getApplicationContext(),
                        ContactSearchActivity.class);
                startActivityForResult(intent, QEQUEST_ADD_MEMBER);

            } else {
                String uid = uiMemberList.get(position);
                if (!StringUtils.isBlank(uid) && uid.startsWith("BOT")) {
                    Bundle bundle = new Bundle();
                    bundle.putString("uid", uid);
                    IntentUtils.startActivity(ChannelInfoActivity.this,
                            RobotInfoActivity.class, bundle);
                    return;
                }
                intent.putExtra("uid", uiMemberList.get(position));
                intent.setClass(getApplicationContext(), UserInfoActivity.class);
                startActivity(intent);
            }
        }
    };

    private CompoundButton.OnCheckedChangeListener onCheckedChangeListener = new CompoundButton.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
            switch (compoundButton.getId()) {
                case R.id.sv_dnd:
                    if (isNoInterruption != b) {
                        updateIsNoInterruption(b);
                    }
                    break;
                case R.id.sv_stick:
                    if (b != ChannelOperationCacheUtils.isChannelSetTop(getApplication(), cid)) {
                        setChannelTop(b);
                    }
                    break;
                default:
                    break;
            }
        }
    };

    @Override
    public void onCreate() {
        channelMemberNumText = findViewById(R.id.tv_member);
        groupMembersText = findViewById(R.id.tv_group_member_size);
        groupMessageSearchLayout = findViewById(R.id.rl_search_messages);
        groupMessageSearchLayout.setVisibility(View.GONE);
        groupMemberSizeText = findViewById(R.id.tv_group_members);
        groupPhotoImg = findViewById(R.id.iv_group_photo);
        nameText = findViewById(R.id.tv_name);
        apiService = new ChatAPIService(ChannelInfoActivity.this);
        apiService.setAPIInterface(new WebService());
        cid = getIntent().getExtras().getString("cid");
        loadingDlg = new LoadingDialog(ChannelInfoActivity.this);
        getChannelInfo();
    }

    @Override
    public int getLayoutResId() {
        return R.layout.activity_conversation_group_info;
    }

    protected int getStatusType() {
        return STATUS_WHITE_DARK_FONT;
    }

    /**
     * 获取频道信息
     */
    private void getChannelInfo() {
        // TODO Auto-generated method stub
        channelGroup = ChannelGroupCacheUtils
                .getChannelGroupById(getApplicationContext(), cid);
        if (channelGroup != null) {
            filterMemberData(channelGroup.getMemberList());
            displayUI();
        }
        if (NetUtils.isNetworkConnected(getApplicationContext(), (channelGroup == null))) {
            apiService.getChannelInfo(cid);
        }

    }

    /**
     * 数据取出后显示ui
     */
    private void displayUI() {
        int memberSize = ContactUserCacheUtils.getContactUserListById(channelGroup.getMemberList()).size();
        channelMemberNumText.setText(getString(R.string.all_group_member, memberSize));
        setChannelIcon();
        groupMemberSizeText.setText(channelGroup.getChannelName() + getString(R.string.bracket_with_word, (memberSize + "")));
        groupMembersText.setText(getString(R.string.people_num, memberSize));
        memberGrid = findViewById(R.id.gv_member);
        nameText.setText(channelGroup.getChannelName());
        adapter = new Adapter();
        memberGrid.setAdapter(adapter);
        memberGrid.setOnItemClickListener(onItemClickListener);
        setTopSwitch = findViewById(R.id.sv_stick);
        msgInterruptionSwitch = findViewById(R.id.sv_dnd);
        boolean isChannelNotDisturb = ChannelCacheUtils.isChannelNotDisturb(ChannelInfoActivity.this, cid);
        if (msgInterruptionSwitch.isChecked() != isChannelNotDisturb) {
            msgInterruptionSwitch.setChecked(isChannelNotDisturb);
        }
        msgInterruptionSwitch.setOnCheckedChangeListener(onCheckedChangeListener);
        boolean isChannelSetTop = ChannelOperationCacheUtils.isChannelSetTop(this, cid);
        if (setTopSwitch.isChecked() != isChannelSetTop) {
            setTopSwitch.setChecked(isChannelSetTop);
        }
        setTopSwitch.setOnCheckedChangeListener(onCheckedChangeListener);
    }

    /**
     * 设置Channel的Icon
     */
    private void setChannelIcon() {
        // TODO Auto-generated method stub
        if (channelGroup.getType().equals("GROUP")) {
            File file = new File(MyAppConfig.LOCAL_CACHE_PHOTO_PATH,
                    MyApplication.getInstance().getTanent() + channelGroup.getCid() + "_100.png1");
            groupPhotoImg.setTag("");
            if (file.exists()) {
                groupPhotoImg.setImageBitmap(ImageUtils.getBitmapByFile(file));
            } else {
                groupPhotoImg.setImageResource(R.drawable.icon_channel_group_default);
            }
        }
    }

    /**
     * 是否将频道置顶
     *
     * @param isSetIop
     */
    private void setChannelTop(boolean isSetIop) {
        setTopSwitch.setChecked(isSetIop);
        ChannelOperationCacheUtils.setChannelTop(ChannelInfoActivity.this, cid,
                isSetIop);
        // 通知消息页面重新创建群组头像
        Intent intent = new Intent("message_notify");
        intent.putExtra("command", "sort_session_list");
        sendBroadcast(intent);
    }

    public void onClick(View v) {
        Bundle bundle = new Bundle();
        switch (v.getId()) {
            case R.id.ibt_back:
                finish();
                break;
            case R.id.ll_group_image:
                bundle.putString("cid", cid);
                IntentUtils.startActivity(ChannelInfoActivity.this,
                        GroupAlbumActivity.class, bundle);
                break;
            case R.id.ll_group_file:
                bundle.putString("cid", cid);
                IntentUtils.startActivity(ChannelInfoActivity.this,
                        GroupFileActivity.class, bundle);
                break;
            case R.id.rl_channel_name:
                Intent intent = new Intent();
                intent.setClass(ChannelInfoActivity.this,
                        ModifyChannelGroupNameActivity.class);
                intent.putExtra("cid", cid);
                intent.putExtra("name", channelGroup.getChannelName());
                startActivityForResult(intent, REQUEST_UPDATE_CHANNEL_NAME);
                break;
            case R.id.rl_member:
                bundle.putString("title", getString(R.string.group_member));
                bundle.putInt(MembersActivity.MEMBER_PAGE_STATE, MembersActivity.CHECK_STATE);
                bundle.putStringArrayList("uidList", channelGroup.getMemberList());
                IntentUtils.startActivity(ChannelInfoActivity.this,
                        MembersActivity.class, bundle);
                break;
            case R.id.bt_exit:
                showQuitGroupWarningDlg();
                break;
            default:
                break;
        }
    }

    private void showQuitGroupWarningDlg() {
        new CustomDialog.MessageDialogBuilder(ChannelInfoActivity.this)
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
                case REQUEST_UPDATE_CHANNEL_NAME:
                    ModifyChannelGroupName(data);
                    break;
                case QEQUEST_DEL_MEMBER:
                    ArrayList<String> delUidList = (ArrayList<String>) data.getSerializableExtra("selectMemList");
                    delChannelGroupMember(delUidList);
                    break;
                case QEQUEST_ADD_MEMBER:
                    ArrayList<String> addUidList = new ArrayList<>();
                    List<SearchModel> addMemberList = (List<SearchModel>) data
                            .getSerializableExtra("selectMemList");
                    for (int i = 0; i < addMemberList.size(); i++) {
                        addUidList.add(addMemberList.get(i).getId());
                    }
                    addChannelGroupMember(addUidList);
                    break;

                default:
                    break;
            }
        }
    }

    /**
     * 修改群名称
     *
     * @param data
     */
    private void ModifyChannelGroupName(Intent data) {
        String name = data.getStringExtra("name");
        nameText.setText(name);
        int memberSize = ContactUserCacheUtils.getContactUserListById(channelGroup.getMemberList()).size();
        groupMemberSizeText.setText(name + getString(R.string.bracket_with_word, (memberSize + "")));
        String pyFull = PinyinUtils.getPingYin(name);
        String pyShort = PinyinUtils.getPinYinHeadChar(name);
        channelGroup.setChannelName(name);
        channelGroup.setPyFull(pyFull);
        channelGroup.setPyShort(pyShort);
        ChannelGroupCacheUtils.saveChannelGroup(MyApplication.getInstance(), channelGroup);
    }

    /**
     * 过滤不存在的群成员算法
     */
    private void filterMemberData(List<String> memberList) {
        //查三十人，如果不满三十人则查实际人数保证查到的人都是存在的群成员
        List<ContactUser> contactUserList = ContactUserCacheUtils.getContactUserListByIdListOrderBy(memberList, channelGroup.getOwner().equals(MyApplication.getInstance().getUid()) ? 5 : 6);
        ArrayList<String> contactUserIdList = new ArrayList<>();
        for (int i = 0; i < contactUserList.size(); i++) {
            contactUserIdList.add(contactUserList.get(i).getId());
        }
        uiMemberList.clear();
        uiMemberList.addAll(contactUserIdList);
    }

    /**
     * 发送广播
     */
    private void sendBroadCast() {
        Intent mIntent = new Intent("message_notify");
        mIntent.putExtra("command", "refresh_session_list");
        LocalBroadcastManager.getInstance(this).sendBroadcast(mIntent);
    }

    /**
     * 更改是否频道消息免打扰
     *
     * @param isNoInterruption
     */
    private void updateIsNoInterruption(boolean isNoInterruption) {
        // TODO Auto-generated method stub
        if (NetUtils.isNetworkConnected(ChannelInfoActivity.this)) {
            loadingDlg.show();
            this.isNoInterruption = isNoInterruption;
            apiService.updateDnd(cid, isNoInterruption);
        } else {
            msgInterruptionSwitch.setChecked(this.isNoInterruption);
        }
    }

    /**
     * 添加群组成员
     *
     * @param uidList
     */
    private void addChannelGroupMember(ArrayList<String> uidList) {
        if (NetUtils.isNetworkConnected(ChannelInfoActivity.this)) {
            loadingDlg.show();
            apiService.addGroupMembers(uidList, cid);
        }
    }

    /**
     * 删除群成员
     *
     * @param uidList
     */
    public void delChannelGroupMember(ArrayList<String> uidList) {
        if (NetUtils.isNetworkConnected(ChannelInfoActivity.this)) {
            loadingDlg.show();
            apiService.deleteGroupMembers(uidList, cid);
        }
    }

    /**
     * 退出群聊
     */
    public void quitChannelGroup() {
        if (NetUtils.isNetworkConnected(ChannelInfoActivity.this)) {
            loadingDlg.show();
            apiService.quitChannelGroup(cid);
        }
    }

    public static class ViewHolder {
        CircleTextImageView memberHeadImg;
        TextView nameText;
    }

    private class Adapter extends BaseAdapter {
        @Override
        public int getCount() {
            // TODO Auto-generated method stub
            if (channelGroup.getOwner().equals(MyApplication.getInstance().getUid())) {
                return uiMemberList.size() > 8 ? 10 : uiMemberList.size() + 2;
            } else {
                return uiMemberList.size() > 9 ? 10 : uiMemberList.size() + 1;
            }
        }

        @Override
        public Object getItem(int position) {
            return null;
        }

        @Override
        public long getItemId(int position) {
            // TODO Auto-generated method stub
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            // TODO Auto-generated method stub
            ViewHolder viewHolder = null;
            if (convertView == null) {
                viewHolder = new ViewHolder();
                LayoutInflater vi = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
                convertView = vi.inflate(R.layout.channel_member_item_view,
                        null);
                viewHolder.memberHeadImg = (CircleTextImageView) convertView
                        .findViewById(R.id.member_head_img);
                viewHolder.nameText = (TextView) convertView
                        .findViewById(R.id.tv_name);
                convertView.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) convertView.getTag();
            }
            String myUid = MyApplication.getInstance().getUid();
            String userPhotoUrl = "";
            String userName = "";
            boolean isOwner = channelGroup.getOwner().equals(myUid);
            if ((position == getCount() - 1) && isOwner) {
                userPhotoUrl = "drawable://" + R.drawable.icon_group_delete;
                userName = getString(R.string.delete);

            } else if (((position == getCount() - 2) && isOwner)
                    || ((position == getCount() - 1) && !isOwner)) {

                userPhotoUrl = "drawable://" + R.drawable.icon_member_add;
                userName = getString(R.string.add);

            } else {
                String uid = uiMemberList.get(position);
                userName = ContactUserCacheUtils.getUserName(uid);
                userPhotoUrl = APIUri.getUserIconUrl(MyApplication.getInstance(), uid);
            }
            viewHolder.nameText.setText(userName);
            ImageDisplayUtils.getInstance().displayImage(viewHolder.memberHeadImg, userPhotoUrl, R.drawable.icon_photo_default);
            return convertView;
        }
    }

    private class WebService extends APIInterfaceInstance {

        @Override
        public void returnChannelInfoSuccess(
                ChannelGroup channelGroup) {
            // TODO Auto-generated method stub
            LoadingDialog.dimissDlg(loadingDlg);
            ChannelInfoActivity.this.channelGroup = channelGroup;
            // 同步缓存
            ChannelGroupCacheUtils.saveChannelGroup(MyApplication.getInstance(), channelGroup);
            filterMemberData(channelGroup.getMemberList());
            displayUI();
        }

        @Override
        public void returnChannelInfoFail(String error, int errorCode) {
            // TODO Auto-generated method stub
            LoadingDialog.dimissDlg(loadingDlg);
            if (channelGroup == null) {
                WebServiceMiddleUtils.hand(ChannelInfoActivity.this, error, errorCode);
            }
        }

        @Override
        public void returnAddMembersSuccess(
                ChannelGroup channelGroup) {
            // TODO Auto-generated method stub
            LoadingDialog.dimissDlg(loadingDlg);
            returnDelMembersSuccess(channelGroup);
        }

        @Override
        public void returnAddMembersFail(String error, int errorCode) {
            // TODO Auto-generated method stub
            LoadingDialog.dimissDlg(loadingDlg);
            WebServiceMiddleUtils.hand(ChannelInfoActivity.this, error, errorCode);
        }

        @Override
        public void returnDndSuccess() {
            // TODO Auto-generated method stub
            LoadingDialog.dimissDlg(loadingDlg);
            Channel channel = ChannelCacheUtils.getChannel(
                    ChannelInfoActivity.this, cid);
            channel.setDnd(isNoInterruption);
            ChannelCacheUtils.saveChannel(ChannelInfoActivity.this, channel);
            msgInterruptionSwitch.setChecked(isNoInterruption);
            sendBroadCast();
        }

        @Override
        public void returnDndFail(String error, int errorCode) {
            // TODO Auto-generated method stub
            LoadingDialog.dimissDlg(loadingDlg);
            isNoInterruption = !isNoInterruption;
            msgInterruptionSwitch.setChecked(isNoInterruption);
            WebServiceMiddleUtils.hand(ChannelInfoActivity.this, error, errorCode);
        }

        @Override
        public void returnDelMembersSuccess(ChannelGroup channelGroup) {
            // TODO Auto-generated method stub
            LoadingDialog.dimissDlg(loadingDlg);
            ChannelInfoActivity.this.channelGroup = channelGroup;
            // 同步缓存
            ChannelGroupCacheUtils.saveChannelGroup(MyApplication.getInstance(), channelGroup);
            filterMemberData(channelGroup.getMemberList());
            displayUI();
            adapter.notifyDataSetChanged();
        }

        @Override
        public void returnDelMembersFail(String error, int errorCode) {
            // TODO Auto-generated method stub
            LoadingDialog.dimissDlg(loadingDlg);
            WebServiceMiddleUtils.hand(ChannelInfoActivity.this, error, errorCode);
        }

        @Override
        public void returnQuitChannelGroupSuccess() {
            EventBus.getDefault().post(new SimpleEventMessage(Constant.EVENTBUS_TAG_REFRESH_CONVERSATION_ADAPTER));
            LoadingDialog.dimissDlg(loadingDlg);
            setResult(RESULT_OK);
            finish();
        }

        @Override
        public void returnQuitChannelGroupSuccessFail(String error, int errorCode) {
            LoadingDialog.dimissDlg(loadingDlg);
            WebServiceMiddleUtils.hand(ChannelInfoActivity.this, error, errorCode);
        }
    }

}
