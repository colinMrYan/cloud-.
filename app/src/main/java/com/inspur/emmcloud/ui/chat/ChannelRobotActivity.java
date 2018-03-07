package com.inspur.emmcloud.ui.chat;

import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.view.View;
import android.widget.TextView;

import com.inspur.emmcloud.BaseActivity;
import com.inspur.emmcloud.R;
import com.inspur.emmcloud.adapter.ChannelMsgAdapterRobot;
import com.inspur.emmcloud.api.APIInterfaceInstance;
import com.inspur.emmcloud.api.apiservice.ChatAPIService;
import com.inspur.emmcloud.bean.chat.Channel;
import com.inspur.emmcloud.bean.chat.ChannelGroup;
import com.inspur.emmcloud.bean.chat.GetFileUploadResult;
import com.inspur.emmcloud.bean.chat.GetMsgResult;
import com.inspur.emmcloud.bean.chat.GetNewMsgsResult;
import com.inspur.emmcloud.bean.chat.GetNewsImgResult;
import com.inspur.emmcloud.bean.chat.GetSendMsgResult;
import com.inspur.emmcloud.bean.chat.Msg;
import com.inspur.emmcloud.bean.chat.MsgRobot;
import com.inspur.emmcloud.bean.chat.Robot;
import com.inspur.emmcloud.bean.contact.Contact;
import com.inspur.emmcloud.bean.contact.GetSearchChannelGroupResult;
import com.inspur.emmcloud.bean.system.PVCollectModel;
import com.inspur.emmcloud.broadcastreceiver.MsgReceiver;
import com.inspur.emmcloud.ui.contact.RobotInfoActivity;
import com.inspur.emmcloud.ui.contact.UserInfoActivity;
import com.inspur.emmcloud.util.common.IntentUtils;
import com.inspur.emmcloud.util.common.LogUtils;
import com.inspur.emmcloud.util.common.NetUtils;
import com.inspur.emmcloud.util.common.StringUtils;
import com.inspur.emmcloud.util.privates.AppUtils;
import com.inspur.emmcloud.util.privates.ConbineMsg;
import com.inspur.emmcloud.util.privates.DirectChannelUtils;
import com.inspur.emmcloud.util.privates.WebServiceMiddleUtils;
import com.inspur.emmcloud.util.privates.cache.ChannelCacheUtils;
import com.inspur.emmcloud.util.privates.cache.ContactCacheUtils;
import com.inspur.emmcloud.util.privates.cache.MsgCacheUtil;
import com.inspur.emmcloud.util.privates.cache.MsgReadIDCacheUtils;
import com.inspur.emmcloud.util.privates.cache.PVCollectModelCacheUtils;
import com.inspur.emmcloud.widget.ECMChatInputMenuRobot;
import com.inspur.emmcloud.widget.LoadingDialog;
import com.inspur.emmcloud.widget.RecycleViewForSizeChange;

import org.json.JSONObject;
import org.xutils.view.annotation.ContentView;
import org.xutils.view.annotation.ViewInject;

import java.util.List;

@ContentView(R.layout.activity_channel_robot)
public class ChannelRobotActivity extends BaseActivity {

    private static final int HAND_CALLBACK_MESSAGE = 1;

    @ViewInject(R.id.msg_list)
    private RecycleViewForSizeChange msgListView;

    @ViewInject(R.id.refresh_layout)
    private SwipeRefreshLayout swipeRefreshLayout;

    @ViewInject(R.id.chat_input_menu)
    private ECMChatInputMenuRobot chatInputMenu;
    @ViewInject(R.id.header_text)
    private TextView headerText;

    private LoadingDialog loadingDlg;
    private String cid;
    private Channel channel;
    private List<MsgRobot> msgList;
    private ChannelMsgAdapterRobot adapter;
    private Handler handler;
    private MsgReceiver msgResvier;
    private ChatAPIService apiService;
    private String robotUid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        init();
        recordUserClickChannel();
    }

    // Activity在SingleTask的启动模式下多次打开传递Intent无效，用此方法解决
    @Override
    protected void onNewIntent(Intent intent) {
        // TODO Auto-generated method stub
        super.onNewIntent(intent);
        setIntent(intent);
        init();
        //当从群成员选择进入沟通频道的时候执行这里的记录
        recordUserClickChannel();
    }

    private void init() {
        loadingDlg = new LoadingDialog(this);
        apiService = new ChatAPIService(ChannelRobotActivity.this);
        apiService.setAPIInterface(new WebService());
        cid = getIntent().getExtras().getString("cid");
        channel = ChannelCacheUtils.getChannel(this, cid);
        if (channel == null) {
            getChannelInfo();
        } else if (getIntent().hasExtra("get_new_msg")) {//通过scheme打开的频道
            getNewMsgOfChannel(true);
        } else {
            initViews();
        }
    }


    /**
     * 初始化Views
     */
    private void initViews() {
        initPullRefreshLayout();
        initChatInputMenu();
        setChannelTitle();
        initMsgListView();
        handMessage();
        registeMsgReceiver();
    }

    /**
     * 初始化下拉刷新UI
     */
    private void initPullRefreshLayout() {
        swipeRefreshLayout.setColorSchemeColors(getResources().getColor(R.color.header_bg), getResources().getColor(R.color.header_bg));
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                if (msgList.size() > 0 && MsgCacheUtil.isDataInLocal(ChannelRobotActivity.this, cid, msgList
                        .get(0).getId(), 15)) {
                    List<MsgRobot> historyMsgList = MsgCacheUtil.getRobotHistoryMsgList(
                            ChannelRobotActivity.this, cid, msgList.get(0).getId(),
                            15);
                    msgList.addAll(0, historyMsgList);
                    swipeRefreshLayout.setRefreshing(false);
                    adapter.setMsgList(msgList);
                    adapter.notifyItemRangeInserted(0, historyMsgList.size());
                    msgListView.MoveToPosition(historyMsgList.size() - 1);
                } else {
                    getNewsMsg();
                }
            }
        });
    }

    /**
     * 显示聊天频道的title
     */
    private void setChannelTitle() {
        String title = channel.getTitle();
        Robot robot = DirectChannelUtils.getRobotInfo(getApplicationContext(),
                title);
        title = robot.getName();
        robotUid = robot.getId();
        headerText.setText(title);
    }

    /**
     * 处理chatInputMenu是否显示，以及显示几个Menu上的item
     */
    private void initChatInputMenu() {
        chatInputMenu.setOtherLayoutView(swipeRefreshLayout);
        chatInputMenu.setChatInputMenuListener(new ECMChatInputMenuRobot.ChatInputMenuListener() {

            @Override
            public void onSendMsg(String content) {
                // TODO Auto-generated method stub
                sendTextMessage(content);
            }
        });
    }


    /**
     * 初始化消息列表UI
     */
    private void initMsgListView() {
        msgListView = (RecycleViewForSizeChange) findViewById(R.id.msg_list);
        msgList = MsgCacheUtil.getRobotHistoryMsgList(getApplicationContext(),
                cid, "", 15);
        adapter = new ChannelMsgAdapterRobot(ChannelRobotActivity.this, apiService, channel.getType(), null);
        adapter.setMsgList(msgList);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        msgListView.setLayoutManager(linearLayoutManager);
        msgListView.setAdapter(adapter);
        msgListView.MoveToPosition(msgList.size() - 1);
        adapter.setItemClickListener(new ChannelMsgAdapterRobot.MyItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                MsgRobot msg = msgList.get(position);
                //当消息处于发送中状态时无法点击
                if (msg.getSendStatus() != 1) {
                    return;
                }
                String msgType = msg.getType();
                switch (msgType) {
                    case "attachment/card":
                        Bundle bundle = new Bundle();
                        String uid = msg.getMsgContentAttachmentCard().getUid();
                        LogUtils.jasonDebug("uid="+uid);
                        bundle.putString("uid", uid);
                        IntentUtils.startActivity(ChannelRobotActivity.this,
                                UserInfoActivity.class, bundle);
                        break;
                    default:
                        break;
                }
            }
        });

    }


    /**
     * 注册消息接收广播,传入一个Handler用于接收到消息后把消息发回到主线程
     */
    private void registeMsgReceiver() {
        // TODO Auto-generated method stub
        if (msgResvier == null) {
            msgResvier = new MsgReceiver(ChannelRobotActivity.this, handler);
            IntentFilter filter = new IntentFilter();
            filter.addAction("com.inspur.msg");
            registerReceiver(msgResvier, filter);
        }
    }


    /**
     * 处理子线程返回消息
     */
    private void handMessage() {
        // TODO Auto-generated method stub
        handler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case HAND_CALLBACK_MESSAGE: // 接收推送的消息·
                        if (msg.what == 1) {
                            JSONObject obj = (JSONObject) msg.obj;
                            MsgRobot pushMsg;
                            if (obj.has("message")){
                                pushMsg= new MsgRobot(obj) ;
                            }else {
                                pushMsg= new MsgRobot(obj,true) ;
                            }
                            if (cid.equals(pushMsg.getChannel())) {
                                MsgReadIDCacheUtils.saveReadedMsg(ChannelRobotActivity.this,
                                        pushMsg.getChannel(), pushMsg.getId());
                                MsgCacheUtil.saveRobotMsg(getApplicationContext(),pushMsg);
                                if (!msgList.contains(pushMsg) && !pushMsg.getTmpId().equals(AppUtils.getMyUUID(getApplicationContext()))) {
                                    msgList.add(pushMsg);
                                    adapter.setMsgList(msgList);
                                    adapter.notifyItemInserted(msgList.size() - 1);
                                    msgListView.MoveToPosition(msgList.size() - 1);
                                }
                            }
                        }
                        break;

                    default:
                        break;
                }

            }

        };
    }

    /**
     * 当推送消息是自己的消息时修改消息id
     *
     * @param fakeMessageId
     * @param realMsg
     */
    private void replaceWithRealMsg(String fakeMessageId, MsgRobot realMsg) {
        if (StringUtils.isBlank(fakeMessageId)) {
            return;
        }
        Msg fakeMsg = new Msg();
        fakeMsg.setMid(fakeMessageId);
        int fakeMsgIndex = msgList.indexOf(fakeMsg);
        boolean isContainRealMsg = msgList.contains(realMsg);
        if (fakeMsgIndex != -1) {
            msgList.remove(fakeMsgIndex);
            if (isContainRealMsg) {
                adapter.setMsgList(msgList);
                adapter.notifyItemRemoved(fakeMsgIndex);
            } else {
                msgList.add(fakeMsgIndex, realMsg);
                adapter.setMsgList(msgList);
                adapter.notifyItemChanged(fakeMsgIndex);
            }
        } else if (!isContainRealMsg) {
            msgList.add(realMsg);
            adapter.setMsgList(msgList);
            adapter.notifyItemInserted(msgList.size() - 1);
        }


    }


    /**
     * 控件点击事件
     *
     * @param v
     */
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.back_layout:
                finishActivity();
                break;

            case R.id.channel_info_img:
                showChannelInfo();
                break;
            default:
                break;
        }
    }

    /**
     * 关闭此页面
     */
    private void finishActivity() {
        if (loadingDlg != null && loadingDlg.isShowing()) {
            loadingDlg.dismiss();
        }
        finish();
    }

    /**
     * 展示群组或个人信息
     */
    private void showChannelInfo() {
        Bundle bundle = new Bundle();
        bundle.putString("cid", cid);
        if (channel.getType().equals("GROUP")) {
            IntentUtils.startActivity(ChannelRobotActivity.this,
                    ChannelInfoActivity.class, bundle);
        } else if (channel.getType().equals("SERVICE")) {
            String botUid = DirectChannelUtils.getRobotInfo(getApplicationContext(),
                    channel.getTitle()).getId();
            bundle.putString("uid", botUid);
            bundle.putString("type", channel.getType());
            IntentUtils.startActivity(ChannelRobotActivity.this,
                    RobotInfoActivity.class, bundle);
        } else {
            IntentUtils.startActivity(ChannelRobotActivity.this,
                    UserInfoActivity.class, bundle);
        }
    }

    /**
     * 点击发送按钮后发送消息的逻辑
     */
    private void sendTextMessage(String content) {
        String fakeMessageId = System.currentTimeMillis() + "";
        MsgRobot localMsg = ConbineMsg.conbineTextPlainMsgRobot(content,
                cid, fakeMessageId);
        if (content.length() < 4 && StringUtils.isChinese(content)) {
            Contact contact = ContactCacheUtils.getContactByUserName(getApplicationContext(), content);
            if (contact != null) {
                addLocalMessage(localMsg, 1);
                MsgRobot replContactCardMsg = ConbineMsg.conbineReplyAttachmentCardMsg(contact, cid, robotUid, fakeMessageId);
                addLocalMessage(replContactCardMsg, 1);
                return;
            }
        }
        addLocalMessage(localMsg, 0);
        //sendMsg(richTextObj.toString(), "txt_rich", fakeMessageId);

    }

    /**
     * 消息发送完成后在本地添加一条消息
     *
     * @param msg
     */
    private void addLocalMessage(MsgRobot msg, int status) {
        if (msg != null) {
            //本地添加的消息设置为正在发送状态
            msg.setSendStatus(status);
            msgList.add(msg);
            adapter.setMsgList(msgList);
            adapter.notifyItemInserted(msgList.size() - 1);
            msgListView.MoveToPosition(msgList.size() - 1);
        }
    }


    /**
     * 通知message页将本频道消息置为已读
     */
    private void setChannelMsgRead() {
        if (msgList != null && msgList.size() > 0) {
            MsgReadIDCacheUtils.saveReadedMsg(this, cid,
                    msgList.get(msgList.size() - 1).getId());
            Intent intent = new Intent("message_notify");
            intent.putExtra("command", "set_channel_message_read");
            intent.putExtra("cid", cid);
            intent.putExtra("mid", msgList.get(msgList.size() - 1).getId());
            sendBroadcast(intent);
        }
    }

    /**
     * 记录用户点击的频道，修改不是云+客服的时候才记录频道点击事件170629
     */
    private void recordUserClickChannel() {
        String from = getIntent().getExtras().getString("from", "");
        if (!from.equals("customer")) {
            PVCollectModel pvCollectModel = new PVCollectModel("channel", "communicate");
            PVCollectModelCacheUtils.saveCollectModel(ChannelRobotActivity.this, pvCollectModel);
        }
    }

    @Override
    public void onBackPressed() {
        // TODO Auto-generated method stub
        if (chatInputMenu.isAddMenuLayoutShow()) {
            chatInputMenu.hideAddMenuLayout();
            return;
        }
        if (chatInputMenu.isVoiceInput()) {
            chatInputMenu.stopVoiceInput();
            return;
        }
        super.onBackPressed();

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (handler != null) {
            handler = null;
        }
        if (msgResvier != null) {
            unregisterReceiver(msgResvier);
            msgResvier = null;
        }
        chatInputMenu.releaseVoliceInput();
    }

    /**
     * 发送消息
     *
     * @param content
     * @param type
     * @param fakeMessageId
     */
    protected void sendMsg(String content, String type, String fakeMessageId) {
        if (NetUtils.isNetworkConnected(getApplicationContext())) {
            apiService.sendMsg(cid, content, type, fakeMessageId);
        }
    }


    /**
     * 获取新消息
     */
    private void getNewsMsg() {
        if (NetUtils.isNetworkConnected(ChannelRobotActivity.this)) {
            String newMsgMid = msgList.size() > 0 ? msgList.get(0).getId() : "";
            apiService.getNewMsgs(cid, newMsgMid, 15);
        } else {
            swipeRefreshLayout.setRefreshing(false);
        }

    }


    /**
     * 获取频道信息
     */
    private void getChannelInfo() {
        if (NetUtils.isNetworkConnected(this)) {
            loadingDlg.show();
            String[] cidArray = {cid};
            apiService.getChannelGroupList(cidArray);
        } else {
            finishActivity();
        }

    }

    /**
     * 获取此频道的最新消息
     */
    private void getNewMsgOfChannel(boolean isShowDlg) {
        if (NetUtils.isNetworkConnected(this, false)) {
            loadingDlg.show(isShowDlg);
            apiService.getNewMsgs(cid, "", 15);
        } else {
            initViews();
        }
    }


    private class WebService extends APIInterfaceInstance {
        @Override
        public void returnSendMsgSuccess(GetSendMsgResult getSendMsgResult,
                                         String fakeMessageId) {
            // replaceWithRealMsg(fakeMessageId, getSendMsgResult.getMsg());
        }

        @Override
        public void returnSendMsgFail(String error, String fakeMessageId, int errorCode) {
            //消息发送失败处理
            Msg fakeMsg = new Msg();
            fakeMsg.setMid(fakeMessageId);
            int fakeMsgIndex = msgList.indexOf(fakeMsg);
            if (fakeMsgIndex != -1) {
                msgList.get(fakeMsgIndex).setSendStatus(2);
                adapter.setMsgList(msgList);
                adapter.notifyDataSetChanged();
            }
            WebServiceMiddleUtils.hand(ChannelRobotActivity.this, error, errorCode);

        }

        @Override
        public void returnUploadMsgImgSuccess(
                GetNewsImgResult getNewsImgResult, String fakeMessageId) {
            String newsImgBody = getNewsImgResult.getImgMsgBody();
            sendMsg(newsImgBody, "res_image", fakeMessageId);
        }

        @Override
        public void returnUploadMsgImgFail(String error, int errorCode) {
            if (swipeRefreshLayout == null) {
                if (loadingDlg != null && loadingDlg.isShowing()) {
                    loadingDlg.dismiss();
                }
                initViews();
            }

            WebServiceMiddleUtils.hand(ChannelRobotActivity.this, error, errorCode);
        }

        @Override
        public void returnNewMsgsSuccess(GetNewMsgsResult getNewMsgsResult) {
            if (loadingDlg != null && loadingDlg.isShowing()) {
                loadingDlg.dismiss();
                List<Msg> msgList = getNewMsgsResult.getNewMsgList(cid);
                if (msgList.size() > 0) {
                    MsgCacheUtil.saveMsgList(ChannelRobotActivity.this, msgList, "");
                    String lastMsgMid = msgList.get(msgList.size() - 1).getMid();
                    MsgReadIDCacheUtils.saveReadedMsg(ChannelRobotActivity.this, cid,
                            lastMsgMid);
                }
                initViews();
                setChannelMsgRead();
            } else {
                swipeRefreshLayout.setRefreshing(false);

//                final List<MsgRobot> historyMsgList = getNewMsgsResult
//                        .getNewMsgList(cid);
//                MsgCacheUtil.saveRobotMsgList(ChannelRobotActivity.this, historyMsgList,
//                        msgList.get(0).getId());
//                if (historyMsgList != null && historyMsgList.size() > 1) {
//                    msgList.addAll(0, historyMsgList);
//                    adapter.setMsgList(msgList);
//                    adapter.notifyItemRangeInserted(0, historyMsgList.size());
//                    msgListView.MoveToPosition(historyMsgList.size() - 1);
//                }
            }

        }

        @Override
        public void returnNewMsgsFail(String error, int errorCode) {
            if (loadingDlg != null && loadingDlg.isShowing()) {
                loadingDlg.dismiss();
            }
            if (swipeRefreshLayout == null) {
                initViews();
            } else {
                WebServiceMiddleUtils.hand(ChannelRobotActivity.this, error, errorCode);
            }
        }

        @Override
        public void returnMsgSuccess(GetMsgResult getMsgResult) {
            Msg msg = getMsgResult.getMsg();
            if (msg != null && ChannelRobotActivity.this != null) {
                MsgCacheUtil.saveMsg(ChannelRobotActivity.this, msg);
                adapter.setMsgList(msgList);
                adapter.notifyDataSetChanged();
            }
        }

        @Override
        public void returnMsgFail(String error, int errorCode) {
            WebServiceMiddleUtils.hand(ChannelRobotActivity.this, error, errorCode);
        }

        @Override
        public void returnFileUpLoadSuccess(
                GetFileUploadResult getFileUploadResult, String fakeMessageId) {
            String fileMsgBody = getFileUploadResult.getFileMsgBody();
            sendMsg(fileMsgBody, "res_file", fakeMessageId);
        }

        @Override
        public void returnFileUpLoadFail(String error, int errorCode) {
            WebServiceMiddleUtils.hand(ChannelRobotActivity.this, error, errorCode);
        }

        @Override
        public void returnSearchChannelGroupSuccess(
                GetSearchChannelGroupResult getSearchChannelGroupResult) {
            List<ChannelGroup> channelGroupList = getSearchChannelGroupResult.getSearchChannelGroupList();
            if (channelGroupList.size() != 0) {
                channel = new Channel(channelGroupList.get(0));
                getNewMsgOfChannel(false);
            } else {
                finishActivity();
            }

        }

        @Override
        public void returnSearchChannelGroupFail(String error, int errorCode) {
            channel = ChannelCacheUtils.getChannel(getApplicationContext(), cid);
            if (channel == null) {
                finishActivity();
            } else {
                getNewMsgOfChannel(false);
            }
        }
    }

}