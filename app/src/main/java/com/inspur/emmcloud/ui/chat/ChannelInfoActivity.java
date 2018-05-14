package com.inspur.emmcloud.ui.chat;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.content.LocalBroadcastManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;
import com.inspur.emmcloud.BaseActivity;
import com.inspur.emmcloud.MyApplication;
import com.inspur.emmcloud.R;
import com.inspur.emmcloud.api.APIInterfaceInstance;
import com.inspur.emmcloud.api.APIUri;
import com.inspur.emmcloud.api.apiservice.ChatAPIService;
import com.inspur.emmcloud.bean.chat.Channel;
import com.inspur.emmcloud.bean.chat.ChannelGroup;
import com.inspur.emmcloud.bean.chat.GetAddMembersSuccessResult;
import com.inspur.emmcloud.bean.chat.GetChannelInfoResult;
import com.inspur.emmcloud.bean.contact.SearchModel;
import com.inspur.emmcloud.ui.contact.ContactSearchActivity;
import com.inspur.emmcloud.ui.contact.RobotInfoActivity;
import com.inspur.emmcloud.ui.contact.UserInfoActivity;
import com.inspur.emmcloud.util.common.IntentUtils;
import com.inspur.emmcloud.util.common.LogUtils;
import com.inspur.emmcloud.util.common.NetUtils;
import com.inspur.emmcloud.util.common.PinyinUtils;
import com.inspur.emmcloud.util.common.StringUtils;
import com.inspur.emmcloud.util.privates.ImageDisplayUtils;
import com.inspur.emmcloud.util.privates.WebServiceMiddleUtils;
import com.inspur.emmcloud.util.privates.cache.ChannelCacheUtils;
import com.inspur.emmcloud.util.privates.cache.ChannelGroupCacheUtils;
import com.inspur.emmcloud.util.privates.cache.ChannelOperationCacheUtils;
import com.inspur.emmcloud.util.privates.cache.ContactCacheUtils;
import com.inspur.emmcloud.util.privates.cache.RobotCacheUtils;
import com.inspur.emmcloud.widget.CircleImageView;
import com.inspur.emmcloud.widget.LoadingDialog;
import com.inspur.emmcloud.widget.NoScrollGridView;
import com.inspur.emmcloud.widget.SwitchView;
import com.inspur.emmcloud.widget.SwitchView.OnStateChangedListener;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * 频道详情页面
 *
 * @author Administrator
 */
public class ChannelInfoActivity extends BaseActivity {

    protected static final int GET_CHANNEL_INFO_SUCCESS = 0;
    private static final int MODIFY_NAME = 1;
    private static final int ADD_MEMBER = 2;
    private static final int DEL_MEMBER = 3;
    private NoScrollGridView memberGrid;
    private LoadingDialog loadingDlg;
    private List<String> memberList = new ArrayList<>();
    private String cid;
    private String name;
    private Handler handler;
    private SwitchView setTopSwitch;
    private SwitchView msgInterruptionSwitch;
    private ChatAPIService apiService;
    private Adapter adapter;
    private String owner = "";
    private TextView channelMemberNumText;
    private boolean isNoInterruption = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_channel_info);
        channelMemberNumText = (TextView) findViewById(R.id.channel_member_text);
        apiService = new ChatAPIService(ChannelInfoActivity.this);
        apiService.setAPIInterface(new WebService());
        cid = getIntent().getExtras().getString("cid");
        loadingDlg = new LoadingDialog(ChannelInfoActivity.this);
        handMessage();
        getChannelInfo();
    }

    private void handMessage() {
        // TODO Auto-generated method stub
        handler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case GET_CHANNEL_INFO_SUCCESS:
                        if (loadingDlg != null && loadingDlg.isShowing()) {
                            loadingDlg.dismiss();
                        }
                        displayUI();
                        break;
                    default:
                        break;
                }
            }

        };
    }

    /**
     * 获取频道信息
     */
    private void getChannelInfo() {
        // TODO Auto-generated method stub
        if (NetUtils.isNetworkConnected(getApplicationContext())) {
            loadingDlg.show();
            apiService.getChannelInfo(cid);
        } else {
            getChannelInfoFromCache();
        }

    }

    private void getChannelInfoFromCache() {
        final ChannelGroup channelGroup = ChannelGroupCacheUtils
                .getChannelGroupById(getApplicationContext(), cid);
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                name = channelGroup.getChannelName();
                memberList = channelGroup.getMemberList();
                handler.sendEmptyMessage(GET_CHANNEL_INFO_SUCCESS);
            }
        };
        new Thread(runnable).start();
        loadingDlg.show();
    }

    /**
     * 数据取出后显示ui
     */
    private void displayUI() {
        channelMemberNumText.setText(getString(R.string.all_group_member) + "（"
                + memberList.size() + "）");
        memberGrid = (NoScrollGridView) findViewById(R.id.member_grid);
        ((TextView) findViewById(R.id.channel_name_text)).setText(name);
        adapter = new Adapter();
        memberGrid.setAdapter(adapter);
        memberGrid.setOnItemClickListener(onItemClickListener);
        setTopSwitch = (SwitchView) findViewById(R.id.settop_switch);
        msgInterruptionSwitch = (SwitchView) findViewById(R.id.msg_interruption_switch);
        msgInterruptionSwitch.setOpened(ChannelCacheUtils.isChannelNotDisturb(
                ChannelInfoActivity.this, cid));
        msgInterruptionSwitch.setOnStateChangedListener(onStateChangedListener);
        boolean isSetTop = ChannelOperationCacheUtils.isChannelSetTop(
                this, cid);
        setTopSwitch.setOpened(isSetTop);
        setTopSwitch.setOnStateChangedListener(onStateChangedListener);

    }

    /**
     * 是否将频道置顶
     *
     * @param isSetIop
     */
    private void setChannelTop(boolean isSetIop) {
        setTopSwitch.toggleSwitch(isSetIop);
        ChannelOperationCacheUtils.setChannelTop(ChannelInfoActivity.this, cid,
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
            String myUid = ((MyApplication) getApplicationContext()).getUid();
            Intent intent = new Intent();
            if ((position == adapter.getCount() - 1) && owner.equals(myUid)) {
                intent.putExtra("memberUidList", (Serializable) memberList);
                intent.setClass(getApplicationContext(),
                        ChannelMembersDelActivity.class);
                startActivityForResult(intent, DEL_MEMBER);

            } else if (((position == adapter.getCount() - 2) && owner
                    .equals(myUid))
                    || ((position == adapter.getCount() - 1) && !owner
                    .equals(myUid))) {
                intent.putExtra("select_content", 2);
                intent.putExtra("isMulti_select", true);
                intent.putExtra("title", getString(R.string.add_group_member));
                intent.setClass(getApplicationContext(),
                        ContactSearchActivity.class);
                startActivityForResult(intent, ADD_MEMBER);

            } else {
                String uid = memberList.get(position);
                if (!StringUtils.isBlank(uid) && uid.startsWith("BOT")) {
                    Bundle bundle = new Bundle();
                    bundle.putString("uid", uid);
                    IntentUtils.startActivity(ChannelInfoActivity.this,
                            RobotInfoActivity.class, bundle);
                    return;
                }
                intent.putExtra("uid", memberList.get(position));
                intent.setClass(getApplicationContext(), UserInfoActivity.class);
                startActivity(intent);
            }
        }
    };

    private OnStateChangedListener onStateChangedListener = new OnStateChangedListener() {

        @Override
        public void toggleToOn(View view) {
            // TODO Auto-generated method stub
            LogUtils.debug("jason", "toggleToOn---------");
            if (view.getId() == R.id.msg_interruption_switch) {
                updateIsNoInterruption(true);
            } else {
                setChannelTop(true);
            }
        }

        @Override
        public void toggleToOff(View view) {
            // TODO Auto-generated method stub
            LogUtils.debug("jason", "toggleToOff---------");
            if (view.getId() == R.id.msg_interruption_switch) {
                updateIsNoInterruption(false);
            } else {
                setChannelTop(false);
            }
        }
    };

    public void onClick(View v) {
        Bundle bundle = new Bundle();
        bundle.putString("cid", cid);
        switch (v.getId()) {
            case R.id.back_layout:
                finish();
                break;
            case R.id.channel_img:
                IntentUtils.startActivity(ChannelInfoActivity.this,
                        GroupAlbumActivity.class, bundle);
                break;
            case R.id.channel_file:
                IntentUtils.startActivity(ChannelInfoActivity.this,
                        GroupFileActivity.class, bundle);
                break;
            case R.id.channel_name_layout:
                Intent intent = new Intent();
                intent.setClass(ChannelInfoActivity.this,
                        ModifyChannelGroupNameActivity.class);
                intent.putExtra("cid", cid);
                intent.putExtra("name", name);
                startActivityForResult(intent, MODIFY_NAME);
                break;
            case R.id.member_layout:
                bundle.putString("title", getString(R.string.group_member));
                bundle.putString("search", "1");
                IntentUtils.startActivity(ChannelInfoActivity.this,
                        MembersActivity.class, bundle);
                break;
            default:
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case MODIFY_NAME:
                    ModifyChannelGroupName(data);
                    break;
                case DEL_MEMBER:
                    ArrayList<String> delUidList = new ArrayList<String>();
                    delUidList = (ArrayList<String>) data.getSerializableExtra("selectMemList");
                    if (delUidList != null && delUidList.size() > 0) {
                        delChannelGroupMember(delUidList);
                    }
                    break;
                case ADD_MEMBER:
                    ArrayList<String> addUidList = new ArrayList<String>();
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
        this.name = name;
        ((TextView) findViewById(R.id.channel_name_text)).setText(name);
        String pyFull = PinyinUtils.getPingYin(name);
        String pyShort = PinyinUtils.getPinYinHeadChar(name);
        ChannelGroupCacheUtils.updateChannelGroupName(getApplicationContext(),
                cid, name);
        ChannelGroupCacheUtils.updateChannelGroupNameFull(
                getApplicationContext(), cid, pyFull);
        ChannelGroupCacheUtils.updateChannelGroupNameShort(
                getApplicationContext(), cid, pyShort);
    }

    private class Adapter extends BaseAdapter {
        @Override
        public int getCount() {
            // TODO Auto-generated method stub
            String myUid = ((MyApplication) getApplicationContext()).getUid();
            if (owner.equals(myUid)) {
                return memberList.size() > 8 ? 10 : memberList.size() + 2;
            } else {
                return memberList.size() > 9 ? 10 : memberList.size() + 1;
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
                viewHolder.memberHeadImg = (CircleImageView) convertView
                        .findViewById(R.id.member_head_img);
                viewHolder.nameText = (TextView) convertView
                        .findViewById(R.id.name_text);
                convertView.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) convertView.getTag();
            }
            String myUid = ((MyApplication) getApplicationContext()).getUid();
            String userPhotoUrl = "";
            String userName = "";
            if ((position == getCount() - 1) && owner.equals(myUid)) {
                userPhotoUrl = "drawable://" + R.drawable.icon_group_delete;
                userName = getString(R.string.delete);

            } else if (((position == getCount() - 2) && owner.equals(myUid))
                    || ((position == getCount() - 1) && !owner.equals(myUid))) {

                userPhotoUrl = "drawable://" + R.drawable.icon_member_add;
                userName = getString(R.string.add);

            } else {
                String uid = memberList.get(position);
                viewHolder.nameText.setText(ContactCacheUtils.getUserName(
                        ChannelInfoActivity.this, uid));
                if (uid.startsWith("BOT")) {
                    userPhotoUrl = APIUri.getRobotIconUrl(RobotCacheUtils
                            .getRobotById(ChannelInfoActivity.this, uid)
                            .getAvatar());
                    userName = RobotCacheUtils
                            .getRobotById(ChannelInfoActivity.this, uid)
                            .getName();
                } else {
                    userPhotoUrl = APIUri.getChannelImgUrl(ChannelInfoActivity.this, uid);
                    userName = ContactCacheUtils.getUserName(
                            ChannelInfoActivity.this, uid);
                }
                ImageDisplayUtils.getInstance().displayImage(viewHolder.memberHeadImg,
                        APIUri.getChannelImgUrl(ChannelInfoActivity.this, uid), R.drawable.icon_photo_default);
            }
            viewHolder.nameText.setText(userName);
            ImageDisplayUtils.getInstance().displayImage(viewHolder.memberHeadImg, userPhotoUrl, R.drawable.icon_photo_default);
            return convertView;
        }
    }

    public static class ViewHolder {
        CircleImageView memberHeadImg;
        TextView nameText;
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
            msgInterruptionSwitch.setOpened(this.isNoInterruption);
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

    private class WebService extends APIInterfaceInstance {

        @Override
        public void returnChannelInfoSuccess(
                GetChannelInfoResult getChannelInfoResult) {
            // TODO Auto-generated method stub
            if (loadingDlg != null && loadingDlg.isShowing()) {
                loadingDlg.dismiss();
            }
            owner = getChannelInfoResult.getOwner();
            name = getChannelInfoResult.getName();
            memberList = getChannelInfoResult.getMemberList();
            String members = JSON.toJSONString(memberList);
            // 同步缓存
            ChannelGroupCacheUtils.updateChannelGroupMembers(
                    ChannelInfoActivity.this, cid, members.toString());
            displayUI();
        }

        @Override
        public void returnChannelInfoFail(String error, int errorCode) {
            // TODO Auto-generated method stub
            if (loadingDlg != null && loadingDlg.isShowing()) {
                loadingDlg.dismiss();
            }
            WebServiceMiddleUtils.hand(ChannelInfoActivity.this, error, errorCode);
            getChannelInfoFromCache();
        }

        @Override
        public void returnAddMembersSuccess(
                GetAddMembersSuccessResult getAddMembersSuccessResult) {
            // TODO Auto-generated method stub
            if (loadingDlg != null && loadingDlg.isShowing()) {
                loadingDlg.dismiss();
            }

            GetChannelInfoResult getChannelInfoResult = new GetChannelInfoResult(
                    getAddMembersSuccessResult.getChannelInfo());
            returnDelMembersSuccess(getChannelInfoResult);
        }

        @Override
        public void returnAddMembersFail(String error, int errorCode) {
            // TODO Auto-generated method stub
            if (loadingDlg.isShowing()) {
                loadingDlg.dismiss();
            }
            WebServiceMiddleUtils.hand(ChannelInfoActivity.this, error, errorCode);
        }

        @Override
        public void returnDndSuccess() {
            // TODO Auto-generated method stub
            if (loadingDlg.isShowing()) {
                loadingDlg.dismiss();
            }
            Channel channel = ChannelCacheUtils.getChannel(
                    ChannelInfoActivity.this, cid);
            channel.setDnd(isNoInterruption);
            ChannelCacheUtils.saveChannel(ChannelInfoActivity.this, channel);
            msgInterruptionSwitch.setOpened(isNoInterruption);
            sendBroadCast();
        }

        @Override
        public void returnDndFail(String error, int errorCode) {
            // TODO Auto-generated method stub
            if (loadingDlg.isShowing()) {
                loadingDlg.dismiss();
            }
            isNoInterruption = !isNoInterruption;
            msgInterruptionSwitch.setOpened(isNoInterruption);
            WebServiceMiddleUtils.hand(ChannelInfoActivity.this, error, errorCode);
        }

        @Override
        public void returnDelMembersSuccess(
                GetChannelInfoResult getChannelInfoResult) {
            // TODO Auto-generated method stub
            if (loadingDlg != null && loadingDlg.isShowing()) {
                loadingDlg.dismiss();
            }

            memberList = getChannelInfoResult.getMemberList();
            String members = JSON.toJSONString(memberList);
            // 同步缓存
            ChannelGroupCacheUtils.updateChannelGroupMembers(
                    ChannelInfoActivity.this, cid, members);
            channelMemberNumText.setText(getString(R.string.all_group_member) + "（"
                    + memberList.size() + "）");
            adapter.notifyDataSetChanged();
        }

        @Override
        public void returnDelMembersFail(String error, int errorCode) {
            // TODO Auto-generated method stub
            WebServiceMiddleUtils.hand(ChannelInfoActivity.this, error, errorCode);
            if (loadingDlg != null && loadingDlg.isShowing()) {
                loadingDlg.dismiss();
            }
        }

    }

    /**
     * 发送广播
     */
    private void sendBroadCast() {
        Intent mIntent = new Intent("message_notify");
        mIntent.putExtra("command", "refresh_session_list");
        LocalBroadcastManager.getInstance(this).sendBroadcast(mIntent);
    }

}
