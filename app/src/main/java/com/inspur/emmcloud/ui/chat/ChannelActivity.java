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
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.inspur.emmcloud.MediaPlayBaseActivity;
import com.inspur.emmcloud.MyApplication;
import com.inspur.emmcloud.R;
import com.inspur.emmcloud.adapter.ChannelMessageAdapter;
import com.inspur.emmcloud.api.APIUri;
import com.inspur.emmcloud.api.apiservice.WSAPIService;
import com.inspur.emmcloud.bean.appcenter.volume.VolumeFile;
import com.inspur.emmcloud.bean.chat.Channel;
import com.inspur.emmcloud.bean.chat.GetNewMessagesResult;
import com.inspur.emmcloud.bean.chat.Message;
import com.inspur.emmcloud.bean.chat.UIMessage;
import com.inspur.emmcloud.bean.contact.ContactUser;
import com.inspur.emmcloud.bean.system.EventMessage;
import com.inspur.emmcloud.bean.system.PVCollectModel;
import com.inspur.emmcloud.config.Constant;
import com.inspur.emmcloud.config.MyAppConfig;
import com.inspur.emmcloud.interf.ProgressCallback;
import com.inspur.emmcloud.ui.contact.RobotInfoActivity;
import com.inspur.emmcloud.ui.contact.UserInfoActivity;
import com.inspur.emmcloud.util.common.FileUtils;
import com.inspur.emmcloud.util.common.IntentUtils;
import com.inspur.emmcloud.util.common.JSONUtils;
import com.inspur.emmcloud.util.common.NetUtils;
import com.inspur.emmcloud.util.common.StringUtils;
import com.inspur.emmcloud.util.common.ToastUtils;
import com.inspur.emmcloud.util.privates.ChannelInfoUtils;
import com.inspur.emmcloud.util.privates.CommunicationUtils;
import com.inspur.emmcloud.util.privates.DataCleanManager;
import com.inspur.emmcloud.util.privates.DirectChannelUtils;
import com.inspur.emmcloud.util.privates.GetPathFromUri4kitkat;
import com.inspur.emmcloud.util.privates.ImageDisplayUtils;
import com.inspur.emmcloud.util.privates.MessageRecourceUploadUtils;
import com.inspur.emmcloud.util.privates.UriUtils;
import com.inspur.emmcloud.util.privates.WebServiceMiddleUtils;
import com.inspur.emmcloud.util.privates.cache.ContactUserCacheUtils;
import com.inspur.emmcloud.util.privates.cache.MessageCacheUtil;
import com.inspur.emmcloud.util.privates.cache.MessageReadCreationDateCacheUtils;
import com.inspur.emmcloud.util.privates.cache.PVCollectModelCacheUtils;
import com.inspur.emmcloud.widget.ECMChatInputMenu;
import com.inspur.emmcloud.widget.ECMChatInputMenu.ChatInputMenuListener;
import com.inspur.emmcloud.widget.LoadingDialog;
import com.inspur.emmcloud.widget.RecycleViewForSizeChange;
import com.inspur.emmcloud.widget.dialogs.MyQMUIDialog;
import com.inspur.imp.plugin.camera.imagepicker.ImagePicker;
import com.inspur.imp.plugin.camera.imagepicker.bean.ImageItem;
import com.inspur.imp.plugin.camera.mycamera.MyCameraActivity;
import com.inspur.imp.util.compressor.Compressor;
import com.qmuiteam.qmui.widget.dialog.QMUIDialog;
import com.qmuiteam.qmui.widget.dialog.QMUIDialogAction;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.json.JSONObject;
import org.xutils.view.annotation.ContentView;
import org.xutils.view.annotation.ViewInject;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import static android.R.attr.path;

/**
 * com.inspur.emmcloud.ui.ChannelActivity
 *
 * @author Fortune Yu; create at 2016年8月29日
 */
@ContentView(R.layout.activity_channel)
public class ChannelActivity extends MediaPlayBaseActivity {

    private static final int HAND_CALLBACK_MESSAGE = 1;
    private static final int GELLARY_RESULT = 2;
    private static final int CAMERA_RESULT = 3;
    private static final int MENTIONS_RESULT = 5;
    private static final int CHOOSE_FILE = 4;
    @ViewInject(R.id.msg_list)
    private RecycleViewForSizeChange msgListView;

    @ViewInject(R.id.refresh_layout)
    private SwipeRefreshLayout swipeRefreshLayout;

    @ViewInject(R.id.chat_input_menu)
    private ECMChatInputMenu chatInputMenu;
    @ViewInject(R.id.header_text)
    private TextView headerText;

    @ViewInject(R.id.robot_photo_img)
    private ImageView robotPhotoImg;

    private LoadingDialog loadingDlg;
    private String robotUid = "BOT6006";
    private String cid;
    private Channel channel;
    private List<UIMessage> uiMessageList = new ArrayList<>();
    private ChannelMessageAdapter adapter;
    private Handler handler;
    private boolean isSpecialUser = false; //小智机器人进行特殊处理
    private BroadcastReceiver sendActionMsgReceiver;
    private BroadcastReceiver refreshNameReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EventBus.getDefault().register(this);
        init();
        registeRefreshNameReceiver();
        registeSendActionMsgReceiver();
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
        cid = getIntent().getExtras().getString("cid");
        new ChannelInfoUtils().getChannelInfo(this, cid, loadingDlg, new ChannelInfoUtils.GetChannelInfoCallBack() {
            @Override
            public void getChannelInfoSuccess(Channel channel) {
                ChannelActivity.this.channel = channel;
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
    }

    /**
     * 初始化下拉刷新UI
     */
    private void initPullRefreshLayout() {
        swipeRefreshLayout.setColorSchemeColors(getResources().getColor(R.color.header_bg), getResources().getColor(R.color.header_bg));
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                if (uiMessageList.size() > 0 && MessageCacheUtil.isDataInLocal(ChannelActivity.this, cid, uiMessageList
                        .get(0).getCreationDate(), 20)) {
                    List<Message> historyMsgList = MessageCacheUtil.getHistoryMessageList(
                            MyApplication.getInstance(), cid, uiMessageList.get(0).getCreationDate(),
                            20);
                    uiMessageList.addAll(0, UIMessage.MessageList2UIMessageList(historyMsgList));
                    swipeRefreshLayout.setRefreshing(false);
                    adapter.setMessageList(uiMessageList);
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

        if (isSpecialUser) {
            robotPhotoImg.setVisibility(View.VISIBLE);
            headerText.setVisibility(View.GONE);
            String uid = DirectChannelUtils.getDirctChannelOtherUid(MyApplication.getInstance(), channel.getTitle());
            String iconUrl = APIUri.getUserIconUrl(MyApplication.getInstance(), uid);
            ImageDisplayUtils.getInstance().displayImage(robotPhotoImg, iconUrl, R.drawable.ic_robot_new);
        } else {
            robotPhotoImg.setVisibility(View.GONE);
            headerText.setVisibility(View.VISIBLE);
            headerText.setText(CommunicationUtils.getChannelDisplayTitle(channel));
        }
    }

    /**
     * 处理chatInputMenu是否显示，以及显示几个Menu上的item
     */
    private void initChatInputMenu() {
        chatInputMenu.setSpecialUser(isSpecialUser);
        chatInputMenu.setOtherLayoutView(swipeRefreshLayout, msgListView);
        if (channel.getType().equals("GROUP")) {
            chatInputMenu.setCanMentions(true, cid);
        } else {
            chatInputMenu.setCanMentions(false, "");
        }
        chatInputMenu.setChatInputMenuListener(new ChatInputMenuListener() {

            @Override
            public void onSendMsg(String content, List<String> mentionsUidList, List<String> urlList, Map<String, String> mentionsMap) {
                // TODO Auto-generated method stub
                sendMessageWithText(content, false, mentionsMap);
            }

            @Override
            public void onSendVoiceRecordMsg(float seconds, String filePath) {
                int duration = (int) seconds;
                if (duration == 0) {
                    duration = 1;
                }
                combinAndSendMessageWithFile(filePath, Message.MESSAGE_TYPE_MEDIA_VOICE, duration);
            }
        });
        chatInputMenu.setInputLayout(isSpecialUser ? "1" : channel.getInputs());
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

    private void registeSendActionMsgReceiver() {
        if (sendActionMsgReceiver == null) {
            sendActionMsgReceiver = new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    String content = intent.getStringExtra("content");
                    if (!StringUtils.isBlank(content)) {
                        sendMessageWithText(content, true, null);
                    }
                }
            };
            IntentFilter filter = new IntentFilter();
            filter.addAction("com.inspur.msg.send");
            LocalBroadcastManager.getInstance(this).registerReceiver(sendActionMsgReceiver, filter);
        }
    }


    /**
     * 初始化消息列表UI
     */
    private void initMsgListView() {
        final List<Message> cacheMessageList = MessageCacheUtil.getHistoryMessageList(MyApplication.getInstance(), cid, null, 20);
        uiMessageList = UIMessage.MessageList2UIMessageList(cacheMessageList);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        msgListView.setLayoutManager(linearLayoutManager);
        if (adapter == null) {
            adapter = new ChannelMessageAdapter(ChannelActivity.this, channel.getType(), chatInputMenu);
            adapter.setItemClickListener(new ChannelMessageAdapter.MyItemClickListener() {
                @Override
                public void onItemClick(View view, int position) {
                    UIMessage uiMessage = uiMessageList.get(position);
                    int messageSendStatus = uiMessage.getSendStatus();
                    //当消息处于发送中状态时无法点击
                    if (messageSendStatus == 1) {
                        openMessage(uiMessage.getMessage());
                    }
                }

                @Override
                public void onMessageResend(UIMessage uiMessage) {
                    if (uiMessage.getSendStatus() == 2) {
                        showResendMessageDlg(uiMessage);
                    }
                }
            });
            adapter.setMessageList(uiMessageList);
            msgListView.setAdapter(adapter);
        } else {
            adapter.setChannelData(channel.getType(), chatInputMenu);
            adapter.setMessageList(uiMessageList);
            adapter.notifyDataSetChanged();
        }
        msgListView.MoveToPosition(uiMessageList.size() - 1);
    }

    /**
     * 弹出消息重新发送提示框
     *
     * @param uiMessage
     */
    private void showResendMessageDlg(final UIMessage uiMessage) {
        new MyQMUIDialog.MessageDialogBuilder(ChannelActivity.this)
                .setMessage(R.string.sure_to_resend_message)
                .addAction(R.string.cancel, new QMUIDialogAction.ActionListener() {
                    @Override
                    public void onClick(QMUIDialog dialog, int index) {
                        dialog.dismiss();
                    }
                })
                .addAction(R.string.ok, new QMUIDialogAction.ActionListener() {
                    @Override
                    public void onClick(QMUIDialog dialog, int index) {
                        dialog.dismiss();
                        resendMessage(uiMessage);
                    }
                })
                .show();
    }


    /**
     * 消息重新发送
     *
     * @param uiMessage
     */
    private void resendMessage(UIMessage uiMessage) {
        if (NetUtils.isNetworkConnected(getApplicationContext())) {
            // TODO Auto-generated method stub
            Message message = uiMessage.getMessage();
            uiMessage.setSendStatus(0);
            int position = uiMessageList.indexOf(uiMessage);
            if (position != uiMessageList.size() - 1) {
                uiMessageList.remove(position);
                uiMessageList.add(uiMessage);
                adapter.setMessageList(uiMessageList);
                adapter.notifyDataSetChanged();
                msgListView.MoveToPosition(uiMessageList.size() - 1);
            }else {
                adapter.setMessageList(uiMessageList);
                adapter.notifyItemChanged(uiMessageList.size() - 1);
            }
            switch (message.getType()) {
                case Message.MESSAGE_TYPE_FILE_REGULAR_FILE:
                case Message.MESSAGE_TYPE_MEDIA_IMAGE:
                case Message.MESSAGE_TYPE_MEDIA_VOICE:
                    sendMessageWithFile(message);
                    break;
                case Message.MESSAGE_TYPE_TEXT_PLAIN:
                    WSAPIService.getInstance().sendChatTextPlainMsg(message);
                    break;
                case Message.MESSAGE_TYPE_EXTENDED_LINKS:
                    WSAPIService.getInstance().sendChatExtendedLinksMsg(message);
                    break;
                default:
                    break;
            }
        }
    }

    /**
     * 打开消息
     *
     * @param message
     */
    private void openMessage(Message message) {
        String msgType = message.getType();
        Bundle bundle = new Bundle();
        switch (msgType) {
            case "attachment/card":
                String uid = message.getMsgContentAttachmentCard().getUid();
                bundle.putString("uid", uid);
                IntentUtils.startActivity(ChannelActivity.this,
                        UserInfoActivity.class, bundle);
                break;
            case "file/regular-file":
            case "media/image":
                bundle.putString("mid", message.getId());
                bundle.putString("cid", message.getChannel());
                IntentUtils.startActivity(ChannelActivity.this,
                        ChannelMessageDetailActivity.class, bundle);
                break;
            case "comment/text-plain":
                String mid = message.getMsgContentComment().getMessage();
                bundle.putString("mid", mid);
                bundle.putString("cid", message.getChannel());
                IntentUtils.startActivity(ChannelActivity.this,
                        ChannelMessageDetailActivity.class, bundle);
                break;
            case "extended/links":
                String url = message.getMsgContentExtendedLinks().getUrl();
                UriUtils.openUrl(ChannelActivity.this, url);
                break;
            default:
                break;
        }
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
                    for (String url : pathList) {
                        combinAndSendMessageWithFile(url, type.equals("file") ? Message.MESSAGE_TYPE_FILE_REGULAR_FILE : Message.MESSAGE_TYPE_MEDIA_IMAGE);
                    }
                    break;
                case "link":
                    String content = getIntent().getExtras().getString(Constant.SHARE_LINK);
                    if (!StringUtils.isBlank(content)) {
                        Message message = CommunicationUtils.combinLocalExtendedLinksMessage(cid, JSONUtils.getString(content, "poster", ""), JSONUtils.getString(content, "title", "")
                                , JSONUtils.getString(content, "digest", ""), JSONUtils.getString(content, "url", ""));
                        WSAPIService.getInstance().sendChatExtendedLinksMsg(message);
                        addLocalMessage(message, 0);
                    }
                    break;
                default:
                    break;
            }

        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode,
                                    final Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            // 文件管理器返回
            if (requestCode == CHOOSE_FILE
                    && NetUtils.isNetworkConnected(MyApplication.getInstance())) {
                String filePath = GetPathFromUri4kitkat.getPathByUri(MyApplication.getInstance(), data.getData());
                File file = new File(filePath);
                if (StringUtils.isBlank(FileUtils.getSuffix(file))) {
                    ToastUtils.show(MyApplication.getInstance(),
                            getString(R.string.not_support_upload));
                } else {
                    combinAndSendMessageWithFile(filePath, Message.MESSAGE_TYPE_FILE_REGULAR_FILE);
                }
                //拍照返回
            } else if (requestCode == CAMERA_RESULT
                    ) {
                String imgPath = data.getExtras().getString(MyCameraActivity.OUT_FILE_PATH);
                try {
                    File file = new Compressor(ChannelActivity.this).setMaxHeight(MyAppConfig.UPLOAD_ORIGIN_IMG_DEFAULT_SIZE).setMaxWidth(MyAppConfig.UPLOAD_ORIGIN_IMG_DEFAULT_SIZE).setQuality(90).setDestinationDirectoryPath(MyAppConfig.LOCAL_IMG_CREATE_PATH)
                            .compressToFile(new File(imgPath));
                    imgPath = file.getAbsolutePath();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                combinAndSendMessageWithFile(imgPath, Message.MESSAGE_TYPE_MEDIA_IMAGE);
                //拍照后图片编辑返回
            } else if (requestCode == MENTIONS_RESULT) {
                // @返回
                String result = data.getStringExtra("searchResult");
                String uid = JSONUtils.getString(result, "uid", null);
                String name = JSONUtils.getString(result, "name", null);
                boolean isInputKeyWord = data.getBooleanExtra("isInputKeyWord", false);
                chatInputMenu.addMentions(uid, name, isInputKeyWord);
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
                            File file = new Compressor(ChannelActivity.this).setMaxHeight(MyAppConfig.UPLOAD_ORIGIN_IMG_DEFAULT_SIZE).setMaxWidth(MyAppConfig.UPLOAD_ORIGIN_IMG_DEFAULT_SIZE).setQuality(90).setDestinationDirectoryPath(MyAppConfig.LOCAL_IMG_CREATE_PATH)
                                    .compressToFile(new File(imgPath));
                            imgPath = file.getAbsolutePath();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        combinAndSendMessageWithFile(imgPath, Message.MESSAGE_TYPE_MEDIA_IMAGE);
                    }
                }
        }
    }

    private void combinAndSendMessageWithFile(String filePath, String messageType) {
        combinAndSendMessageWithFile(filePath, messageType, 0);
    }

    private void combinAndSendMessageWithFile(String filePath, String messageType, int duration) {
        File file = new File(filePath);
        if (!file.exists()) {
            ToastUtils.show(MyApplication.getInstance(), R.string.file_not_exist);
            return;
        }
        Message fakeMessage = null;
        switch (messageType) {
            case Message.MESSAGE_TYPE_FILE_REGULAR_FILE:
                fakeMessage = CommunicationUtils.combinLocalRegularFileMessage(cid, filePath);
                break;
            case Message.MESSAGE_TYPE_MEDIA_IMAGE:
                fakeMessage = CommunicationUtils.combinLocalMediaImageMessage(cid, filePath);
                break;
            case Message.MESSAGE_TYPE_MEDIA_VOICE:
                fakeMessage = CommunicationUtils.combinLocalMediaVoiceMessage(cid, filePath, duration);
                break;
        }
        if (fakeMessage != null) {
            sendMessageWithFile(fakeMessage);
            addLocalMessage(fakeMessage, 0);
        }
    }

    /**
     * 发送带有附件类型的消息
     *
     * @param fakeMessage
     */
    private void sendMessageWithFile(final Message fakeMessage) {
        MessageRecourceUploadUtils messageRecourceUploadUtils = new MessageRecourceUploadUtils(MyApplication.getInstance(), cid);
        messageRecourceUploadUtils.setProgressCallback(new ProgressCallback() {
            @Override
            public void onSuccess(VolumeFile volumeFile) {
                switch (fakeMessage.getType()) {
                    case Message.MESSAGE_TYPE_FILE_REGULAR_FILE:
                        WSAPIService.getInstance().sendChatRegularFileMsg(cid, fakeMessage.getId(), volumeFile);
                        break;
                    case Message.MESSAGE_TYPE_MEDIA_IMAGE:
                        WSAPIService.getInstance().sendChatMediaImageMsg(volumeFile, fakeMessage);
                        break;
                    case Message.MESSAGE_TYPE_MEDIA_VOICE:
                        WSAPIService.getInstance().sendChatMediaVoiceMsg(fakeMessage, volumeFile);
                        break;
                }
            }

            @Override
            public void onLoading(int progress) {

            }

            @Override
            public void onFail() {
                setMessageSendFailStatus(fakeMessage.getId());
            }
        });
        messageRecourceUploadUtils.uploadResFile(fakeMessage);
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

    //接收到websocket发过来的消息
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onReceiveWSMessage(EventMessage eventMessage) {
        if (eventMessage.getTag().equals(Constant.EVENTBUS_TAG_RECERIVER_SINGLE_WS_MESSAGE)) {
            if (eventMessage.getStatus() == 200) {
                String content = eventMessage.getContent();
                JSONObject contentobj = JSONUtils.getJSONObject(content);
                Message receivedWSMessage = new Message(contentobj);
                if (cid.equals(receivedWSMessage.getChannel())) {
                    MessageReadCreationDateCacheUtils.saveMessageReadCreationDate(MyApplication.getInstance(), cid, receivedWSMessage.getCreationDate());
                    int size = uiMessageList.size();
                    int index = -1;
                    if (size > 0) {
                        for (int i = size - 1; i >= 0; i--) {
                            UIMessage UIMessage = uiMessageList.get(i);
                            if (UIMessage.getMessage().getId().equals(String.valueOf(eventMessage.getExtra()))) {
                                index = i;
                                break;
                            }
                        }

                    }
                    if (index == -1) {
                        uiMessageList.add(new UIMessage(receivedWSMessage));
                        adapter.setMessageList(uiMessageList);
                        adapter.notifyItemInserted(uiMessageList.size() - 1);
                    } else {
                        uiMessageList.remove(index);
                        uiMessageList.add(index, new UIMessage(receivedWSMessage));
                        adapter.setMessageList(uiMessageList);
                        adapter.notifyItemChanged(index);
                    }
                    msgListView.MoveToPosition(uiMessageList.size() - 1);
                }
            } else {
                setMessageSendFailStatus(String.valueOf(eventMessage.getExtra()));
            }
        }

    }


    //接收到websocket发过来的消息
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onGetMessageById(EventMessage eventMessage) {
        if (eventMessage.getTag().equals(Constant.EVENTBUS_TAG_GET_MESSAGE_BY_ID)) {
            if (eventMessage.getStatus() == 200) {
                String content = eventMessage.getContent();
                JSONObject contentobj = JSONUtils.getJSONObject(content);
                Message message = new Message(contentobj);
                MessageCacheUtil.saveMessage(MyApplication.getInstance(), message);
                adapter.setMessageList(uiMessageList);
                adapter.notifyDataSetChanged();
            }

        }

    }

    //接收到websocket发过来的消息
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onReceiveHistoryMessage(EventMessage eventMessage) {
        if (eventMessage.getTag().equals(Constant.EVENTBUS_TAG_GET_HISTORY_MESSAGE)) {
            LoadingDialog.dimissDlg(loadingDlg);
            if (swipeRefreshLayout.isRefreshing()) {
                swipeRefreshLayout.setRefreshing(false);
            }
            if (eventMessage.getStatus() == 200) {
                String content = eventMessage.getContent();
                GetNewMessagesResult getNewMessagesResult = new GetNewMessagesResult(content);
                final List<Message> historyMessageList = getNewMessagesResult
                        .getNewMessageList(cid);
                if (adapter != null) {
                    if (historyMessageList.size() > 0) {
                        Long targetMessageCreationDate = null;
                        if (uiMessageList.size() > 0) {
                            targetMessageCreationDate = uiMessageList.get(0).getCreationDate();
                        }
                        MessageCacheUtil.saveMessageList(MyApplication.getInstance(), historyMessageList, targetMessageCreationDate);
                        List<UIMessage> historyUIMessageList = UIMessage.MessageList2UIMessageList(historyMessageList);
                        uiMessageList.addAll(0, historyUIMessageList);
                        adapter.setMessageList(uiMessageList);
                        adapter.notifyItemRangeInserted(0, historyMessageList.size());
                        msgListView.MoveToPosition(historyMessageList.size() - 1);
                    }
                } else {
                    if (historyMessageList.size() > 0) {
                        MessageCacheUtil.saveMessageList(MyApplication.getInstance(), historyMessageList, null);
                        MessageReadCreationDateCacheUtils.saveMessageReadCreationDate(MyApplication.getInstance(), cid, historyMessageList.get(historyMessageList.size() - 1).getCreationDate());
                    }
                    setChannelMsgRead();
                }
            } else {
                WebServiceMiddleUtils.hand(ChannelActivity.this, eventMessage.getContent(), eventMessage.getStatus());
            }

            if (adapter == null) {
                initViews();
                sendMsgFromShare();
            }
        }
    }


    //接收到离线消息
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onReiceveWSOfflineMessage(EventMessage eventMessage) {
        if (eventMessage.getTag().equals(Constant.EVENTBUS_TAG_GET_OFFLINE_WS_MESSAGE)) {
            if (eventMessage.getStatus() == 200) {
                String content = eventMessage.getContent();
                GetNewMessagesResult getNewMessagesResult = new GetNewMessagesResult(content);
                List<Message> offlineMessageList = getNewMessagesResult.getNewMessageList(cid);
                if (offlineMessageList.size() > 0) {
                    Iterator<Message> it = offlineMessageList.iterator();
                    //去重
                    while (it.hasNext()) {
                        Message offlineMessage = it.next();
                        UIMessage uiMessage = new UIMessage(offlineMessage.getId());
                        if (uiMessageList.contains(uiMessage)) {
                            it.remove();
                        }
                    }
                    int currentPostion = uiMessageList.size() - 1;
                    List<UIMessage> offlineUIMessageList = UIMessage.MessageList2UIMessageList(offlineMessageList);
                    uiMessageList.addAll(uiMessageList.size(), offlineUIMessageList);
                    adapter.setMessageList(uiMessageList);
                    adapter.notifyItemRangeInserted(uiMessageList.size(), offlineUIMessageList.size());
                    msgListView.MoveToPosition(currentPostion);
                }

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
            IntentUtils.startActivity(ChannelActivity.this,
                    ChannelInfoActivity.class, bundle);
        } else if (channel.getType().equals("SERVICE")) {
            String botUid = DirectChannelUtils.getRobotInfo(getApplicationContext(),
                    channel.getTitle()).getId();
            bundle.putString("uid", botUid);
            bundle.putString("type", channel.getType());
            IntentUtils.startActivity(ChannelActivity.this,
                    RobotInfoActivity.class, bundle);
        } else {
            String uid = DirectChannelUtils.getDirctChannelOtherUid(MyApplication.getInstance(), channel.getTitle());
            bundle.putString("uid", uid);
            IntentUtils.startActivity(ChannelActivity.this,
                    UserInfoActivity.class, bundle);
        }
    }

    /**
     * 发送文本消息
     */
    private void sendMessageWithText(String content, boolean isActionMsg, Map<String, String> mentionsMap) {
        Message localMessage = CommunicationUtils.combinLocalTextPlainMessage(content, cid, mentionsMap);
        //当在机器人频道时输入小于4个汉字时先进行通讯录查找，查找到返回通讯路卡片
        if (isSpecialUser && !isActionMsg && content.length() < 4 && StringUtils.isChinese(content)) {
            ContactUser contactUser = ContactUserCacheUtils.getContactUserByUserName(content);
            if (contactUser != null) {
                addLocalMessage(localMessage, 1);
                Message replyLocalMessage = CommunicationUtils.combinLocalReplyAttachmentCardMessage(contactUser, cid, robotUid);
                addLocalMessage(replyLocalMessage, 1);
                return;
            }
        }
        addLocalMessage(localMessage, 0);
        WSAPIService.getInstance().sendChatTextPlainMsg(localMessage);
    }


    /**
     * 消息发送完成后在本地添加一条消息
     *
     * @param message
     * @param status
     */
    private void addLocalMessage(Message message, int status) {
        if (message != null) {
            UIMessage UIMessage = new UIMessage(message);
            //本地添加的消息设置为正在发送状态
            UIMessage.setSendStatus(status);
            uiMessageList.add(UIMessage);
            adapter.setMessageList(uiMessageList);
            adapter.notifyItemInserted(uiMessageList.size() - 1);
            msgListView.MoveToPosition(uiMessageList.size() - 1);
        }
    }

    /**
     * 消息发送失败处理
     *
     * @param fakeMessageId
     */
    private void setMessageSendFailStatus(String fakeMessageId) {
        //消息发送失败处理
        UIMessage fakeUIMessage = new UIMessage(fakeMessageId);
        int fakeUIMessageIndex = uiMessageList.indexOf(fakeUIMessage);
        if (fakeUIMessageIndex != -1) {
            uiMessageList.get(fakeUIMessageIndex).setSendStatus(2);
            adapter.setMessageList(uiMessageList);
            adapter.notifyItemChanged(fakeUIMessageIndex);
        }
    }


    /**
     * 通知message页将本频道消息置为已读
     */
    private void setChannelMsgRead() {
        if (uiMessageList.size() > 0) {
            MessageReadCreationDateCacheUtils.saveMessageReadCreationDate(MyApplication.getInstance(), cid, uiMessageList.get(uiMessageList.size() - 1).getCreationDate());
            Intent intent = new Intent("message_notify");
            intent.putExtra("command", "set_channel_message_read");
            intent.putExtra("cid", cid);
            intent.putExtra("messageCreationDate", uiMessageList.get(uiMessageList.size() - 1).getCreationDate());
            LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
        }
    }

    /**
     * 记录用户点击的频道，修改不是云+客服的时候才记录频道点击事件170629
     */
    private void recordUserClickChannel() {
        String from = getIntent().getExtras().getString("from", "");
        if (!from.equals("customer")) {
            PVCollectModel pvCollectModel = new PVCollectModel("channel", "communicate");
            PVCollectModelCacheUtils.saveCollectModel(ChannelActivity.this, pvCollectModel);
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
        if (sendActionMsgReceiver != null) {
            LocalBroadcastManager.getInstance(this).unregisterReceiver(sendActionMsgReceiver);
            sendActionMsgReceiver = null;
        }
        if (refreshNameReceiver != null) {
            LocalBroadcastManager.getInstance(this).unregisterReceiver(refreshNameReceiver);
            refreshNameReceiver = null;
        }
        chatInputMenu.releaseVoliceInput();
        EventBus.getDefault().unregister(this);
        DataCleanManager.cleanCustomCache(MyAppConfig.LOCAL_CACHE_VOICE_PATH);
    }


    /**
     * 获取新消息
     */
    private void getNewsMsg() {
        swipeRefreshLayout.setRefreshing(false);
        if (NetUtils.isNetworkConnected(ChannelActivity.this)) {
            String newMessageId = uiMessageList.size() > 0 ? uiMessageList.get(0).getMessage().getId() : "";
            WSAPIService.getInstance().getHistoryMessage(cid, newMessageId);
        } else {
            swipeRefreshLayout.setRefreshing(false);
        }

    }

    /**
     * 获取此频道的最新消息
     */
    private void getNewMsgOfChannel() {
        if (NetUtils.isNetworkConnected(this, false)) {
            WSAPIService.getInstance().getHistoryMessage(cid, "");
        }
    }


}