package com.inspur.emmcloud.ui.chat;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.inspur.emmcloud.MyApplication;
import com.inspur.emmcloud.R;
import com.inspur.emmcloud.adapter.ChannelMsgAdapter;
import com.inspur.emmcloud.api.APIInterfaceInstance;
import com.inspur.emmcloud.api.APIUri;
import com.inspur.emmcloud.api.apiservice.ChatAPIService;
import com.inspur.emmcloud.baselib.util.IntentUtils;
import com.inspur.emmcloud.baselib.util.JSONUtils;
import com.inspur.emmcloud.baselib.util.StringUtils;
import com.inspur.emmcloud.basemodule.config.Constant;
import com.inspur.emmcloud.basemodule.config.MyAppConfig;
import com.inspur.emmcloud.basemodule.ui.BaseActivity;
import com.inspur.emmcloud.basemodule.util.AppUtils;
import com.inspur.emmcloud.basemodule.util.ImageDisplayUtils;
import com.inspur.emmcloud.basemodule.util.NetUtils;
import com.inspur.emmcloud.basemodule.util.PVCollectModelCacheUtils;
import com.inspur.emmcloud.basemodule.util.PreferencesByUserAndTanentUtils;
import com.inspur.emmcloud.basemodule.widget.dialogs.CustomDialog;
import com.inspur.emmcloud.bean.appcenter.news.GroupNews;
import com.inspur.emmcloud.bean.chat.Channel;
import com.inspur.emmcloud.bean.chat.GetFileUploadResult;
import com.inspur.emmcloud.bean.chat.GetMsgResult;
import com.inspur.emmcloud.bean.chat.GetNewMsgsResult;
import com.inspur.emmcloud.bean.chat.GetNewsImgResult;
import com.inspur.emmcloud.bean.chat.GetSendMsgResult;
import com.inspur.emmcloud.bean.chat.Message;
import com.inspur.emmcloud.bean.chat.Msg;
import com.inspur.emmcloud.bean.chat.Robot;
import com.inspur.emmcloud.bean.chat.VoiceCommunicationJoinChannelInfoBean;
import com.inspur.emmcloud.bean.contact.ContactUser;
import com.inspur.emmcloud.bean.system.SimpleEventMessage;
import com.inspur.emmcloud.broadcastreceiver.MsgReceiver;
import com.inspur.emmcloud.ui.appcenter.groupnews.NewsWebDetailActivity;
import com.inspur.emmcloud.ui.contact.RobotInfoActivity;
import com.inspur.emmcloud.ui.contact.UserInfoActivity;
import com.inspur.emmcloud.ui.mine.setting.FaceVerifyActivity;
import com.inspur.emmcloud.util.privates.ChannelInfoUtils;
import com.inspur.emmcloud.util.privates.ConbineMsg;
import com.inspur.emmcloud.util.privates.CustomProtocol;
import com.inspur.emmcloud.util.privates.DirectChannelUtils;
import com.inspur.emmcloud.util.privates.GetPathFromUri4kitkat;
import com.inspur.emmcloud.util.privates.InputMethodUtils;
import com.inspur.emmcloud.util.privates.WebServiceMiddleUtils;
import com.inspur.emmcloud.util.privates.cache.ContactUserCacheUtils;
import com.inspur.emmcloud.util.privates.cache.MsgCacheUtil;
import com.inspur.emmcloud.util.privates.cache.MsgReadCreationDateCacheUtils;
import com.inspur.emmcloud.widget.ECMChatInputMenuV0;
import com.inspur.emmcloud.widget.ECMChatInputMenuV0.ChatInputMenuListener;
import com.inspur.emmcloud.widget.LoadingDialog;
import com.inspur.emmcloud.widget.RecycleViewForSizeChange;
import com.inspur.imp.plugin.camera.imagepicker.ImagePicker;
import com.inspur.imp.plugin.camera.imagepicker.bean.ImageItem;
import com.inspur.imp.plugin.camera.mycamera.MyCameraActivity;
import com.inspur.imp.util.compressor.Compressor;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import butterknife.BindView;
import butterknife.ButterKnife;

import static android.R.attr.path;

/**
 * com.inspur.emmcloud.ui.ChannelActivity
 *
 * @author Fortune Yu; create at 2016年8月29日
 */
public class ChannelV0Activity extends BaseActivity {

    private static final int HAND_CALLBACK_MESSAGE = 1;
    private static final int GELLARY_RESULT = 2;
    private static final int CAMERA_RESULT = 3;
    private static final int MENTIONS_RESULT = 5;
    private static final int CHOOSE_FILE = 4;
    private static final int REQUEST_QUIT_CHANNELGROUP = 6;
    @BindView(R.id.msg_list)
    RecycleViewForSizeChange msgListView;

    @BindView(R.id.refresh_layout)
    SwipeRefreshLayout swipeRefreshLayout;

    @BindView(R.id.chat_input_menu)
    ECMChatInputMenuV0 chatInputMenu;
    @BindView(R.id.header_text)
    TextView headerText;

    @BindView(R.id.robot_photo_img)
    ImageView robotPhotoImg;
    private LinearLayoutManager linearLayoutManager;
    private LoadingDialog loadingDlg;
    private String robotUid = "BOT6006";
    private String cid;
    private Channel channel;
    private List<Msg> msgList;
    private ChannelMsgAdapter adapter;
    private Handler handler;
    private MsgReceiver msgResvier;
    private ChatAPIService apiService;
    private boolean isSpecialUser = false; //小智机器人进行特殊处理
    private BroadcastReceiver refreshNameReceiver;


    @Override
    public void onCreate() {
        ButterKnife.bind(this);
        EventBus.getDefault().register(this);
        init();
        registeRefreshNameReceiver();
        recordUserClickChannel();
    }

    @Override
    public int getLayoutResId() {
        return R.layout.activity_channelv0;
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
        apiService = new ChatAPIService(ChannelV0Activity.this);
        apiService.setAPIInterface(new WebService());
        cid = getIntent().getExtras().getString("cid");
        new ChannelInfoUtils().getChannelInfo(this, cid, loadingDlg, new ChannelInfoUtils.GetChannelInfoCallBack() {
            @Override
            public void getChannelInfoSuccess(Channel channel) {
                ChannelV0Activity.this.channel = channel;
                isSpecialUser = channel.getType().equals("SERVICE") && channel.getTitle().contains(robotUid);
                if (getIntent().hasExtra("get_new_msg") && NetUtils.isNetworkConnected(getApplicationContext(), false)) {//通过scheme打开的频道
                    getNewMsgOfChannel();
                } else {
                    initViews();
                    sendMsgFromShare();
                }
            }

            @Override
            public void getChannelInfoFail(String error, int errorCode) {
                finishActivity();
            }
        });


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
     * 从外部分享过来
     */
    private void sendMsgFromShare() {
        if (getIntent().hasExtra("share_type")) {
            String type = getIntent().getStringExtra("share_type");
            switch (type) {
                case "image":
                case "file":
                    List<String> pathList = getIntent().getStringArrayListExtra("share_paths");
                    for (String filePath : pathList) {
                        if (type.equals("file")) {
                            combinAndSendResFileMsg(filePath);
                        } else {
                            combinAndSendResImgMsg(filePath);
                        }
                    }
                    break;
                case "link":
                    String content = getIntent().getExtras().getString(Constant.SHARE_LINK);
                    String fakeId = System.currentTimeMillis() + "";
                    if (!StringUtils.isBlank(content)) {
                        Msg fakeMsg = ConbineMsg.conbineCommonMsg(content, "res_link", fakeId);
                        sendMsg(content, "res_link", fakeId);
                        addLocalMessage(fakeMsg);
                    }
                    break;
                default:
                    break;
            }

        }
    }


    /**
     * 初始化下拉刷新UI
     */
    private void initPullRefreshLayout() {
        swipeRefreshLayout.setColorSchemeColors(getResources().getColor(R.color.header_bg_blue), getResources().getColor(R.color.header_bg_blue));
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                if (msgList.size() > 0 && MsgCacheUtil.isDataInLocal(ChannelV0Activity.this, cid, msgList
                        .get(0).getTime(), 15)) {
                    List<Msg> historyMsgList = MsgCacheUtil.getHistoryMsgList(
                            ChannelV0Activity.this, cid, msgList.get(0).getTime(),
                            15);
                    msgList.addAll(0, historyMsgList);
                    adapter.setMsgList(msgList);
                    adapter.notifyItemRangeInserted(0, historyMsgList.size());
                    msgListView.MoveToPosition(historyMsgList.size() - 1);
                    swipeRefreshLayout.setRefreshing(false);
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

        if (isSpecialUser) {
            robotPhotoImg.setVisibility(View.VISIBLE);
            headerText.setVisibility(View.GONE);
            Robot robot = DirectChannelUtils.getRobotInfo(getApplicationContext(),
                    channel.getTitle());
            String robotPhotoUrl = APIUri.getUserIconUrl(getApplicationContext(), robot.getId());
            ImageDisplayUtils.getInstance().displayImage(robotPhotoImg, robotPhotoUrl, R.drawable.icon_person_default);
        } else {
            robotPhotoImg.setVisibility(View.GONE);
            headerText.setVisibility(View.VISIBLE);
            String title;
            switch (channel.getType()) {
                case "DIRECT":
                    title = DirectChannelUtils.getDirectChannelTitle(
                            getApplicationContext(), channel.getTitle());
                    break;
                case "SERVICE":
                    title = DirectChannelUtils.getRobotInfo(getApplicationContext(),
                            channel.getTitle()).getName();
                    break;
                default:
                    title = channel.getTitle();
                    break;
            }
            headerText.setText(title);
        }
    }

    /**
     * 处理chatInputMenu是否显示，以及显示几个Menu上的item
     */
    private void initChatInputMenu() {
        chatInputMenu.setSpecialUser(isSpecialUser);
        chatInputMenu.setOtherLayoutView(swipeRefreshLayout);
        if (channel.getType().equals("GROUP")) {
            chatInputMenu.setCanMentions(true, cid);
        } else {
            chatInputMenu.setCanMentions(false, "");
        }
        chatInputMenu.setChatInputMenuListener(new ChatInputMenuListener() {

            @Override
            public void onSendMsg(String content, List<String> mentionsUidList, List<String> urlList, Map<String, String> map) {
                // TODO Auto-generated method stub
                PreferencesByUserAndTanentUtils.clearDataByKey(MyApplication.getInstance(), MyAppConfig.getChannelDrafsPreKey(cid));
                sendTextMessage(content, mentionsUidList, urlList, false);
            }

            @Override
            public void onVoiceCommucaiton() {
                List<VoiceCommunicationJoinChannelInfoBean> voiceCommunicationUserInfoBeanList = new ArrayList<>();
                List<String> memberList = new ArrayList<>();
                memberList.add(DirectChannelUtils.getDirctChannelOtherUid(MyApplication.getInstance(), channel.getTitle()));
                memberList.add(MyApplication.getInstance().getUid());
                List<ContactUser> contactUserList = ContactUserCacheUtils.getContactUserListById(memberList);
                for (int i = 0; i < contactUserList.size(); i++) {
                    VoiceCommunicationJoinChannelInfoBean voiceCommunicationJoinChannelInfoBean = new VoiceCommunicationJoinChannelInfoBean();
                    voiceCommunicationJoinChannelInfoBean.setUserId(contactUserList.get(i).getId());
                    voiceCommunicationJoinChannelInfoBean.setUserName(contactUserList.get(i).getName());
                    voiceCommunicationUserInfoBeanList.add(voiceCommunicationJoinChannelInfoBean);
                }
                Intent intent = new Intent();
                intent.setClass(ChannelV0Activity.this, ChannelVoiceCommunicationActivity.class);
                intent.putExtra("userList", (Serializable) voiceCommunicationUserInfoBeanList);
                intent.putExtra(ChannelVoiceCommunicationActivity.VOICE_COMMUNICATION_STATE, ChannelVoiceCommunicationActivity.INVITER_LAYOUT_STATE);
                startActivity(intent);
            }

            @Override
            public void onChatDraftsClear() {
                setChatDrafts();
            }
        });
        chatInputMenu.setInputLayout(isSpecialUser ? "1" : channel.getInputs());
        String chatDrafts = PreferencesByUserAndTanentUtils.getString(MyApplication.getInstance(), MyAppConfig.getChannelDrafsPreKey(cid));
        if (chatDrafts != null) {
            chatInputMenu.setChatDrafts(chatDrafts);
        }
    }


    /**
     * 注册更改频道名称广播
     */
    private void registeRefreshNameReceiver() {
        refreshNameReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String name = intent.getExtras().getString("name");
                headerText.setText(name);
            }
        };
        IntentFilter filter = new IntentFilter();
        filter.addAction("update_channel_name");
        LocalBroadcastManager.getInstance(this).registerReceiver(refreshNameReceiver, filter);
    }

    //接收Action卡片的Action点击事件
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onReceiveSendAcitionContentMessage(SimpleEventMessage eventMessage) {
        if (eventMessage.getAction() == Constant.EVENTBUS_TAG_SEND_ACTION_CONTENT_MESSAGE) {
            String actionContent = (String) eventMessage.getMessageObj();
            sendTextMessage(actionContent, null, null, true);
        }

    }

    /**
     * 初始化消息列表UI
     */
    private void initMsgListView() {
        msgList = MsgCacheUtil.getHistoryMsgList(getApplicationContext(),
                cid, null, 15);
        linearLayoutManager = new LinearLayoutManager(this);
        msgListView.setLayoutManager(linearLayoutManager);
        if (adapter == null) {
            adapter = new ChannelMsgAdapter(ChannelV0Activity.this, apiService, channel.getType(), chatInputMenu);
            adapter.setItemClickListener(new ChannelMsgAdapter.MyItemClickListener() {

                @Override
                public void onMessageResend(Msg msg) {
                    if (msg.getSendStatus() == 2) {
                        showResendMessageDlg(msg);
                    }
                }

                @Override
                public void onItemClick(View view, int position) {
                    Msg msg = msgList.get(position);
                    //当消息处于发送中状态时无法点击
                    if (msg.getSendStatus() == 1) {
                        openMsg(msg);
                    }

                }
            });
            adapter.setMsgList(msgList);
            msgListView.setAdapter(adapter);
        } else {
            adapter.setChannelData(channel.getType(), chatInputMenu);
            adapter.setMsgList(msgList);
            adapter.notifyDataSetChanged();
        }
        msgListView.MoveToPosition(msgList.size() - 1);
        msgListView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                chatInputMenu.hideAddMenuLayout();
                InputMethodUtils.hide(ChannelV0Activity.this);
                return false;
            }
        });
    }

    /**
     * 弹出消息重新发送提示框
     *
     * @param msg
     */
    private void showResendMessageDlg(final Msg msg) {
        new CustomDialog.MessageDialogBuilder(ChannelV0Activity.this)
                .setMessage(R.string.sure_to_resend_message)
                .setNegativeButton(R.string.cancel, (dialog, index) -> {
                    dialog.dismiss();
                })
                .setPositiveButton(R.string.ok, (dialog, index) -> {
                    dialog.dismiss();
                    resendMessage(msg);
                })
                .show();
    }

    /**
     * 消息重新发送
     *
     * @param msg
     */
    private void resendMessage(Msg msg) {
        if (NetUtils.isNetworkConnected(getApplicationContext())) {
            // TODO Auto-generated method stub
            msg.setSendStatus(0);
            int position = msgList.indexOf(msg);
            if (position != msgList.size() - 1) {
                msgList.remove(position);
                msgList.add(msg);
                adapter.setMsgList(msgList);
                adapter.notifyDataSetChanged();
                msgListView.MoveToPosition(msgList.size() - 1);
            } else {
                adapter.setMsgList(msgList);
                adapter.notifyItemChanged(msgList.size() - 1);
            }
            switch (msg.getType()) {
                case "res_image":
                case "res_file":
                    uploadResource(msg);
                    break;
                case "txt_rich":
                case "res_link":
                    sendMsg(msg.getBody(), msg.getType(), msg.getMid());
                    break;
                default:
                    break;
            }
        }
    }


    /**
     * 打开消息
     *
     * @param msg
     */
    private void openMsg(Msg msg) {
        String msgType = msg.getType();
        Message message = null;
        if (Message.isMessage(msg)) {
            message = new Message(msg);
            msgType = message.getType();
        }

        String mid = "";
        Bundle bundle = new Bundle();
        switch (msgType) {
            case "attachment/card":
                String uid = message.getMsgContentAttachmentCard().getUid();
                bundle.putString("uid", uid);
                IntentUtils.startActivity(ChannelV0Activity.this,
                        UserInfoActivity.class, bundle);
                break;
            case "res_file":
                mid = msg.getMid();
                bundle.putString("mid", mid);
                bundle.putString("cid", msg.getCid());
                IntentUtils.startActivity(ChannelV0Activity.this,
                        ChannelMsgDetailActivity.class, bundle);
                break;
            case "comment":
            case "txt_comment":
                mid = msg.getCommentMid();
                bundle.putString("mid", mid);
                bundle.putString("cid", msg.getCid());
                IntentUtils.startActivity(ChannelV0Activity.this,
                        ChannelMsgDetailActivity.class, bundle);
                break;
            case "res_link":
                String msgBody = msg.getBody();
                String linkTitle = JSONUtils.getString(msgBody, "title", "");
                String linkDigest = JSONUtils.getString(msgBody, "digest", "");
                String linkUrl = JSONUtils.getString(msgBody, "url", "");
                String linkPoster = JSONUtils.getString(msgBody, "poster", "");
                GroupNews groupNews = new GroupNews();
                groupNews.setTitle(StringUtils.isBlank(linkTitle) ? getString(R.string.share_default_title) : linkTitle);
                groupNews.setDigest(linkDigest);
                groupNews.setUrl(linkUrl);
                groupNews.setPoster(linkPoster);
                bundle.putSerializable("groupNews", groupNews);
                IntentUtils.startActivity(ChannelV0Activity.this,
                        NewsWebDetailActivity.class, bundle);
                break;
            default:
                break;
        }
    }


    /**
     * 注册消息接收广播,传入一个Handler用于接收到消息后把消息发回到主线程
     */
    private void registeMsgReceiver() {
        // TODO Auto-generated method stub
        if (msgResvier == null) {
            msgResvier = new MsgReceiver(handler);
            IntentFilter filter = new IntentFilter();
            filter.addAction("com.inspur.msg");
            LocalBroadcastManager.getInstance(this).registerReceiver(msgResvier, filter);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode,
                                    final Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            // 文件管理器返回
            if (requestCode == CHOOSE_FILE
                    && NetUtils.isNetworkConnected(getApplicationContext())) {
                String filePath = GetPathFromUri4kitkat.getPathByUri(MyApplication.getInstance(), data.getData());
                combinAndSendResFileMsg(filePath);
                //拍照返回
            } else if (requestCode == CAMERA_RESULT) {
                String imgPath = data.getExtras().getString(MyCameraActivity.OUT_FILE_PATH);
                try {
                    File file = new Compressor(ChannelV0Activity.this).setMaxHeight(MyAppConfig.UPLOAD_ORIGIN_IMG_DEFAULT_SIZE).setMaxWidth(MyAppConfig.UPLOAD_ORIGIN_IMG_DEFAULT_SIZE).setQuality(90).setDestinationDirectoryPath(MyAppConfig.LOCAL_IMG_CREATE_PATH)
                            .compressToFile(new File(imgPath));
                    imgPath = file.getAbsolutePath();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                combinAndSendResImgMsg(imgPath);
            } else if (requestCode == MENTIONS_RESULT) {
                // @返回
                String result = data.getStringExtra("searchResult");
                String uid = JSONUtils.getString(result, "uid", null);
                String name = JSONUtils.getString(result, "name", null);
                boolean isInputKeyWord = data.getBooleanExtra("isInputKeyWord", false);
                chatInputMenu.addMentions(uid, name, isInputKeyWord);
            } else if (requestCode == REQUEST_QUIT_CHANNELGROUP) {
                finish();
            }
        } else {
            // 图库选择图片返回
            if (resultCode == ImagePicker.RESULT_CODE_ITEMS)
                if (data != null && requestCode == GELLARY_RESULT) {
                    ArrayList<ImageItem> imageItemList = (ArrayList<ImageItem>) data
                            .getSerializableExtra(ImagePicker.EXTRA_RESULT_ITEMS);
                    for (int i = 0; i < imageItemList.size(); i++) {
                        String imgPath = imageItemList.get(i).path;
                        try {
                            File file = new Compressor(ChannelV0Activity.this).setMaxHeight(MyAppConfig.UPLOAD_ORIGIN_IMG_DEFAULT_SIZE).setMaxWidth(MyAppConfig.UPLOAD_ORIGIN_IMG_DEFAULT_SIZE).setQuality(90).setDestinationDirectoryPath(MyAppConfig.LOCAL_IMG_CREATE_PATH)
                                    .compressToFile(new File(imgPath));
                            imgPath = file.getAbsolutePath();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        combinAndSendResImgMsg(imgPath);
                    }
                }
        }
    }

    private void combinAndSendResImgMsg(String filePath) {
        if (NetUtils.isNetworkConnected(MyApplication.getInstance())) {
            Msg fakeMsg = ConbineMsg.conbineResImgMsg(filePath);
            if (fakeMsg != null) {
                addLocalMessage(fakeMsg);
                uploadResource(fakeMsg);
            }
        }
    }

    private void combinAndSendResFileMsg(String filePath) {
        if (NetUtils.isNetworkConnected(MyApplication.getInstance())) {
            Msg fakeMsg = ConbineMsg.conbineResFileMsg(filePath);
            if (fakeMsg != null) {
                addLocalMessage(fakeMsg);
                uploadResource(fakeMsg);
            }
        }
    }


    /**
     * 保存并显示把图片展示出来
     *
     * @param context
     * @param cameraPath
     */
    private void refreshGallery(Context context, String cameraPath) {
        File file = new File(cameraPath);
        // 其次把文件插入到系统图库
        try {
            MediaStore.Images.Media.insertImage(context.getContentResolver(),
                    file.getAbsolutePath(), file.getName(), null);
            // 最后通知图库更新
            context.sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.parse("file://" + path)));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 处理子线程返回消息
     */
    private void handMessage() {
        // TODO Auto-generated method stub
        handler = new Handler() {
            @Override
            public void handleMessage(android.os.Message msg) {
                switch (msg.what) {
                    case HAND_CALLBACK_MESSAGE: // 接收推送的消息·
                        if (msg.arg1 == 0) {
                            Msg pushMsg = new Msg((JSONObject) msg.obj);
                            CustomProtocol customProtocol = getCommandMessageProtocol(pushMsg);
                            if (customProtocol != null) {
                                return;
                            }
                            if (cid.equals(pushMsg.getCid())) {
                                if (Message.isMessage(pushMsg)) {
                                    Message message = new Message(pushMsg);
                                    if (message.getType().equals("command/faceLogin")) {
                                        MsgReadCreationDateCacheUtils.saveMessageReadCreationDate(ChannelV0Activity.this,
                                                cid, message.getCreationDate());
                                        intentFaceLogin(message.getContent());
                                        return;
                                    }
                                }
                                MsgReadCreationDateCacheUtils.saveMessageReadCreationDate(ChannelV0Activity.this,
                                        pushMsg.getCid(), pushMsg.getTime());
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
     * 判定是
     *
     * @param receivedMsg
     * @return
     */
    private CustomProtocol getCommandMessageProtocol(Msg receivedMsg) {
        String msgBody = receivedMsg.getBody();
        Pattern pattern = Pattern.compile("\\[[^\\]]+\\]\\([^\\)]+\\)");
        Matcher matcher = pattern.matcher(msgBody);
        while (matcher.find()) {
            String pattenString = matcher.group();
            int indexBegin = pattenString.indexOf("(");
            int indexEnd = pattenString.indexOf(")");
            pattenString = pattenString.substring(indexBegin + 1, indexEnd);
            CustomProtocol customProtocol = new CustomProtocol(pattenString);
            if (customProtocol.getProtocol().equals("ecc-cmd") && customProtocol.getParamMap().get("cmd").equals("join")) {
                return customProtocol;
            }
        }
        return null;
    }

    private void intentFaceLogin(String token) {
        Bundle bundle = new Bundle();
        bundle.putBoolean("isFaceVerifyExperience", false);
        bundle.putBoolean("isFaceLogin", true);
        bundle.putString("token", token);
        IntentUtils.startActivity(ChannelV0Activity.this, FaceVerifyActivity.class, bundle);
    }

    /**
     * 消息发送成功处理：当推送消息是自己的消息时修改消息id
     *
     * @param fakeMessageId
     * @param realMsg
     */
    private void setMsgSendSuccess(String fakeMessageId, Msg realMsg) {
        Msg fakeMsg = new Msg();
        fakeMsg.setMid(fakeMessageId);
        int index = msgList.indexOf(fakeMsg);
        boolean isContainRealMsg = msgList.contains(realMsg);
        if (index != -1) {
            msgList.remove(index);
            if (isContainRealMsg) {
                adapter.setMsgList(msgList);
                adapter.notifyItemRemoved(index);
            } else {
                msgList.add(index, realMsg);
                //如果是图片类型消息的话不再重新刷新消息体，防止图片重新加载
                if (realMsg.getType().equals("res_image")) {
                    setMessageSendSuccess(index, realMsg);
                    adapter.setMsgList(msgList);
                } else {
                    adapter.setMsgList(msgList);
                    adapter.notifyItemChanged(index);
                }

            }
        } else if (!isContainRealMsg) {
            msgList.add(realMsg);
            adapter.setMsgList(msgList);
            adapter.notifyItemInserted(msgList.size() - 1);
        }
    }

    /**
     * 消息发送失败处理
     *
     * @param fakeMessageId
     */
    private void setMsgSendFail(String fakeMessageId) {
        //消息发送失败处理
        Msg fakeMsg = new Msg();
        fakeMsg.setMid(fakeMessageId);
        int fakeMsgIndex = msgList.indexOf(fakeMsg);
        if (fakeMsgIndex != -1) {
            msgList.get(fakeMsgIndex).setSendStatus(2);
            adapter.setMsgList(msgList);
            adapter.notifyDataSetChanged();
        }
    }

    /**
     * 将消息显示状态置为发送成功
     *
     * @param index
     */
    private void setMessageSendSuccess(int index, Msg realMsg) {
        Msg msg = adapter.getItemData(index);
        msg.setBody(realMsg.getBody());
        msg.setSendStatus(1);
        msg.setMid(realMsg.getMid());
        msg.setCid(realMsg.getCid());
        int firstItemPosition = linearLayoutManager.findFirstVisibleItemPosition();
        if (index - firstItemPosition >= 0) {
            View view = msgListView.getChildAt(index - firstItemPosition);
            if (null != msgListView.getChildViewHolder(view)) {
                ChannelMsgAdapter.ViewHolder holder = (ChannelMsgAdapter.ViewHolder) msgListView.getChildViewHolder(view);
                holder.sendStatusLayout.setVisibility(View.INVISIBLE);
            }

        }
    }


    /**
     * 控件点击事件
     *
     * @param v
     */
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.ibt_back:
                finishActivity();
                break;

            case R.id.iv_config:
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
        setChatDrafts();
        if (loadingDlg != null && loadingDlg.isShowing()) {
            loadingDlg.dismiss();
        }
        finish();
    }

    /**
     * 设置当前频道草稿箱
     */
    private void setChatDrafts() {
        String chatDraftsNew = chatInputMenu.getInputContent().trim();
        String chatDraftsOld = PreferencesByUserAndTanentUtils.getString(MyApplication.getInstance(), MyAppConfig.getChannelDrafsPreKey(cid), "").trim();
        if (!chatDraftsNew.equals(chatDraftsOld)) {
            if (!StringUtils.isBlank(chatDraftsNew)) {
                PreferencesByUserAndTanentUtils.putString(MyApplication.getInstance(), MyAppConfig.getChannelDrafsPreKey(cid), chatDraftsNew);
            } else {
                PreferencesByUserAndTanentUtils.clearDataByKey(MyApplication.getInstance(), MyAppConfig.getChannelDrafsPreKey(cid));
            }
            Intent mIntent = new Intent("message_notify");
            mIntent.putExtra("command", "refresh_adapter");
            LocalBroadcastManager.getInstance(this).sendBroadcast(mIntent);
        }
    }

    /**
     * 展示群组或个人信息
     */
    private void showChannelInfo() {
        Bundle bundle = new Bundle();
        bundle.putString("cid", cid);
        if (channel.getType().equals("GROUP")) {
            Intent intent = new Intent(this, ChannelInfoActivity.class);
            intent.putExtras(bundle);
            startActivityForResult(intent, REQUEST_QUIT_CHANNELGROUP);
        } else if (channel.getType().equals("SERVICE")) {
            String botUid = DirectChannelUtils.getRobotInfo(getApplicationContext(),
                    channel.getTitle()).getId();
            bundle.putString("uid", botUid);
            bundle.putString("type", channel.getType());
            IntentUtils.startActivity(ChannelV0Activity.this,
                    RobotInfoActivity.class, bundle);
        } else {
            String uid = DirectChannelUtils.getDirctChannelOtherUid(MyApplication.getInstance(), channel.getTitle());
            bundle.putString("uid", uid);
            IntentUtils.startActivity(ChannelV0Activity.this,
                    UserInfoActivity.class, bundle);
        }
    }

    /**
     * 点击发送按钮后发送消息的逻辑
     */
    private void sendTextMessage(String content, List<String> mentionsUidList, List<String> urlList, boolean isActionMsg) {
        String fakeMessageId = System.currentTimeMillis() + "";
        //当在机器人频道时输入小于4个汉字时先进行通讯录查找，查找到返回通讯路卡片
        if (isSpecialUser && !isActionMsg && content.length() < 4 && StringUtils.isChinese(content)) {
            ContactUser contactUser = ContactUserCacheUtils.getContactUserByUserName(content);
            if (contactUser != null) {
                JSONObject sourceObj = new JSONObject();
                try {
                    sourceObj.put("source", content);
                } catch (Exception e) {
                    e.printStackTrace();
                }

                Msg localMsg = ConbineMsg.conbineCommonMsg(sourceObj.toString(), "txt_rich", fakeMessageId);
                addLocalMessage(localMsg, 1);
                Message conbineReplyMessage = ConbineMsg.conbineReplyAttachmentCardMsg(contactUser, cid, robotUid, fakeMessageId);
                Msg replyLocalMsg = ConbineMsg.conbineRobotMsg(ChannelV0Activity.this,
                        conbineReplyMessage.Message2MsgBody(), robotUid, "txt_rich", fakeMessageId);
                addLocalMessage(replyLocalMsg, 1);
                return;
            }
        }


        JSONObject richTextObj = new JSONObject();
        JSONArray mentionArray = JSONUtils.toJSONArray(mentionsUidList);
        JSONArray urlArray = JSONUtils.toJSONArray(urlList);
        try {
            richTextObj.put("source", content);
            richTextObj.put("mentions", mentionArray);
            richTextObj.put("urls", urlArray);
            richTextObj.put("tmpId", AppUtils.getMyUUID(ChannelV0Activity.this));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        Msg localMsg = ConbineMsg.conbineCommonMsg(richTextObj.toString(), "txt_rich", fakeMessageId);
        addLocalMessage(localMsg);
        sendMsg(richTextObj.toString(), "txt_rich", fakeMessageId);
    }

    /**
     * 消息发送完成后在本地添加一条消息
     *
     * @param msg
     */
    private void addLocalMessage(Msg msg) {
        addLocalMessage(msg, 0);
    }

    /**
     * 消息发送完成后在本地添加一条消息
     *
     * @param msg
     * @param status
     */
    private void addLocalMessage(Msg msg, int status) {
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
            MsgReadCreationDateCacheUtils.saveMessageReadCreationDate(this, cid,
                    msgList.get(msgList.size() - 1).getTime());
            Intent intent = new Intent("message_notify");
            intent.putExtra("command", "set_channel_message_read");
            intent.putExtra("cid", cid);
            intent.putExtra("messageCreationDate", msgList.get(msgList.size() - 1).getTime());
            LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
        }
    }

    /**
     * 记录用户点击的频道，修改不是云+客服的时候才记录频道点击事件170629
     */
    private void recordUserClickChannel() {
        String from = getIntent().getExtras().getString("from", "");
        if (!from.equals("customer")) {
            PVCollectModelCacheUtils.saveCollectModel("channel", "communicate");
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
            LocalBroadcastManager.getInstance(this).unregisterReceiver(msgResvier);
            msgResvier = null;
        }
        EventBus.getDefault().unregister(this);
        if (refreshNameReceiver != null) {
            LocalBroadcastManager.getInstance(this).unregisterReceiver(refreshNameReceiver);
            refreshNameReceiver = null;
        }
        chatInputMenu.releaseVoliceInput();
    }

    /**
     * 上传资源文件
     *
     * @param fakeMsg
     */
    private void uploadResource(Msg fakeMsg) {
        if (NetUtils.isNetworkConnected(getApplicationContext())) {
            String filePath = JSONUtils.getString(fakeMsg.getBody(), "key", "");
            apiService.uploadMsgResource(filePath, fakeMsg.getMid(), fakeMsg.getType().equals("res_image"));
        }
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
        if (NetUtils.isNetworkConnected(ChannelV0Activity.this)) {
            String newMsgMid = msgList.size() > 0 ? msgList.get(0).getMid() : "";
            apiService.getNewMsgs(cid, newMsgMid, 15);
        } else {
            swipeRefreshLayout.setRefreshing(false);
        }

    }


    /**
     * 获取此频道的最新消息
     */
    private void getNewMsgOfChannel() {
        if (NetUtils.isNetworkConnected(this, false)) {
            loadingDlg.show();
            apiService.getNewMsgs(cid, "", 15);
        }
    }

    /**
     * 设置频道是否置顶
     *
     * @param isFoucus
     */
    public void setChannelFoucs(boolean isFoucus) {

    }


    private class WebService extends APIInterfaceInstance {
        @Override
        public void returnSendMsgSuccess(GetSendMsgResult getSendMsgResult,
                                         String fakeMessageId) {
            MsgCacheUtil.saveMsg(MyApplication.getInstance(), getSendMsgResult.getMsg());
            setMsgSendSuccess(fakeMessageId, getSendMsgResult.getMsg());
        }

        @Override
        public void returnSendMsgFail(String error, String fakeMessageId, int errorCode) {
            setMsgSendFail(fakeMessageId);
        }

        @Override
        public void returnUploadResImgSuccess(
                GetNewsImgResult getNewsImgResult, String fakeMessageId) {
            String newsImgBody = getNewsImgResult.getImgMsgBody();
            sendMsg(newsImgBody, "res_image", fakeMessageId);
        }

        @Override
        public void returnUploadResImgFail(String error, int errorCode, String fakeMessageId) {
            setMsgSendFail(fakeMessageId);
        }


        @Override
        public void returnUpLoadResFileSuccess(
                GetFileUploadResult getFileUploadResult, String fakeMessageId) {
            String fileMsgBody = getFileUploadResult.getFileMsgBody();
            sendMsg(fileMsgBody, "res_file", fakeMessageId);
        }

        @Override
        public void returnUpLoadResFileFail(String error, int errorCode, String fakeMessageId) {
            setMsgSendFail(fakeMessageId);
        }

        @Override
        public void returnNewMsgsSuccess(GetNewMsgsResult getNewMsgsResult) {
            if (swipeRefreshLayout.isRefreshing()) {
                final List<Msg> historyMsgList = getNewMsgsResult
                        .getNewMsgList(cid);
                List<Msg> retainAllMsgList = new ArrayList<>();
                retainAllMsgList.addAll(historyMsgList);
                retainAllMsgList.retainAll(msgList);
                historyMsgList.removeAll(retainAllMsgList);
                if (historyMsgList.size() > 0) {
                    MsgCacheUtil.saveMsgList(ChannelV0Activity.this, historyMsgList,
                            msgList.get(0).getTime());
                    msgList.addAll(0, historyMsgList);
                    adapter.setMsgList(msgList);
                    adapter.notifyItemRangeInserted(0, historyMsgList.size());
                    msgListView.scrollToPosition(historyMsgList.size() - 1);
                }
                swipeRefreshLayout.setRefreshing(false);
            } else {
                LoadingDialog.dimissDlg(loadingDlg);
                List<Msg> msgList = getNewMsgsResult.getNewMsgList(cid);
                if (msgList.size() > 0) {
                    MsgCacheUtil.saveMsgList(ChannelV0Activity.this, msgList, null);
                    long lastMsgCreationDate = msgList.get(msgList.size() - 1).getTime();
                    MsgReadCreationDateCacheUtils.saveMessageReadCreationDate(ChannelV0Activity.this, cid,
                            lastMsgCreationDate);
                }
                initViews();
                setChannelMsgRead();
                sendMsgFromShare();
            }

        }

        @Override
        public void returnNewMsgsFail(String error, int errorCode) {
            LoadingDialog.dimissDlg(loadingDlg);
            if (swipeRefreshLayout.isRefreshing()) {
                swipeRefreshLayout.setRefreshing(false);
                WebServiceMiddleUtils.hand(ChannelV0Activity.this, error, errorCode);
            } else {
                initViews();
                sendMsgFromShare();
            }
        }

        @Override
        public void returnMsgSuccess(GetMsgResult getMsgResult) {
            Msg msg = getMsgResult.getMsg();
            if (msg != null && ChannelV0Activity.this != null) {
                MsgCacheUtil.saveMsg(ChannelV0Activity.this, msg);
                adapter.setMsgList(msgList);
                adapter.notifyDataSetChanged();
            }
        }

        @Override
        public void returnMsgFail(String error, int errorCode) {
            WebServiceMiddleUtils.hand(ChannelV0Activity.this, error, errorCode);
        }

    }

}