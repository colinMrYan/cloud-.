package com.inspur.emmcloud.ui.chat;

import android.content.BroadcastReceiver;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.inspur.emmcloud.MyApplication;
import com.inspur.emmcloud.R;
import com.inspur.emmcloud.adapter.ChannelMessageAdapter;
import com.inspur.emmcloud.api.apiservice.WSAPIService;
import com.inspur.emmcloud.bean.appcenter.volume.VolumeFile;
import com.inspur.emmcloud.bean.chat.Conversation;
import com.inspur.emmcloud.bean.chat.GetChannelMessagesResult;
import com.inspur.emmcloud.bean.chat.Message;
import com.inspur.emmcloud.bean.chat.MsgContentMediaVoice;
import com.inspur.emmcloud.bean.chat.UIMessage;
import com.inspur.emmcloud.bean.chat.VoiceCommunicationJoinChannelInfoBean;
import com.inspur.emmcloud.bean.contact.ContactUser;
import com.inspur.emmcloud.bean.system.EventMessage;
import com.inspur.emmcloud.bean.system.SimpleEventMessage;
import com.inspur.emmcloud.bean.system.VoiceResult;
import com.inspur.emmcloud.config.Constant;
import com.inspur.emmcloud.config.MyAppConfig;
import com.inspur.emmcloud.interf.OnVoiceResultCallback;
import com.inspur.emmcloud.interf.ProgressCallback;
import com.inspur.emmcloud.interf.ResultCallback;
import com.inspur.emmcloud.ui.contact.UserInfoActivity;
import com.inspur.emmcloud.util.common.DensityUtil;
import com.inspur.emmcloud.util.common.FileUtils;
import com.inspur.emmcloud.util.common.InputMethodUtils;
import com.inspur.emmcloud.util.common.IntentUtils;
import com.inspur.emmcloud.util.common.JSONUtils;
import com.inspur.emmcloud.util.common.LogUtils;
import com.inspur.emmcloud.util.common.NetUtils;
import com.inspur.emmcloud.util.common.StringUtils;
import com.inspur.emmcloud.util.common.ToastUtils;
import com.inspur.emmcloud.util.privates.CommunicationUtils;
import com.inspur.emmcloud.util.privates.DataCleanManager;
import com.inspur.emmcloud.util.privates.DirectChannelUtils;
import com.inspur.emmcloud.util.privates.GetPathFromUri4kitkat;
import com.inspur.emmcloud.util.privates.ImageDisplayUtils;
import com.inspur.emmcloud.util.privates.MessageRecourceUploadUtils;
import com.inspur.emmcloud.util.privates.PreferencesByUserAndTanentUtils;
import com.inspur.emmcloud.util.privates.UriUtils;
import com.inspur.emmcloud.util.privates.Voice2StringMessageUtils;
import com.inspur.emmcloud.util.privates.audioformat.AudioFormatUtils;
import com.inspur.emmcloud.util.privates.cache.ContactUserCacheUtils;
import com.inspur.emmcloud.util.privates.cache.MessageCacheUtil;
import com.inspur.emmcloud.widget.ECMChatInputMenu;
import com.inspur.emmcloud.widget.ECMChatInputMenu.ChatInputMenuListener;
import com.inspur.emmcloud.widget.LoadingDialog;
import com.inspur.emmcloud.widget.RecycleViewForSizeChange;
import com.inspur.emmcloud.widget.bubble.BubbleLayout;
import com.inspur.emmcloud.widget.dialogs.MyQMUIDialog;
import com.inspur.imp.plugin.camera.imagepicker.ImagePicker;
import com.inspur.imp.plugin.camera.imagepicker.bean.ImageItem;
import com.inspur.imp.plugin.camera.mycamera.MyCameraActivity;
import com.inspur.imp.util.compressor.Compressor;
import com.qmuiteam.qmui.widget.QMUILoadingView;
import com.qmuiteam.qmui.widget.dialog.QMUIDialog;
import com.qmuiteam.qmui.widget.dialog.QMUIDialogAction;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.json.JSONObject;
import org.xutils.view.annotation.ContentView;
import org.xutils.view.annotation.ViewInject;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

@ContentView(R.layout.activity_channel)
public class ConversationActivity extends ConversationBaseActivity {

    private static final int REQUEST_QUIT_CHANNELGROUP = 1;
    private static final int REQUEST_GELLARY = 2;
    private static final int REQUEST_CAMERA = 3;
    private static final int RQQUEST_CHOOSE_FILE = 4;
    private static final int REQUEST_MENTIONS = 5;
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
    private LinearLayoutManager linearLayoutManager;
    private String robotUid = "BOT6004";
    private List<UIMessage> uiMessageList = new ArrayList<>();
    private ChannelMessageAdapter adapter;
    private Handler handler;
    private boolean isSpecialUser = false; //小智机器人进行特殊处理
    private BroadcastReceiver refreshNameReceiver;
    private PopupWindow mediaVoiceReRecognizerPop;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EventBus.getDefault().register(this);
    }


    // Activity在SingleTask的启动模式下多次打开传递Intent无效，用此方法解决
    @Override
    protected void onNewIntent(Intent intent) {
        // TODO Auto-generated method stub
        super.onNewIntent(intent);
        setIntent(intent);
        initConversationInfo();
    }

    @Override
    protected void initChannelMessage() {
        List<Message> cacheMessageList = MessageCacheUtil.getHistoryMessageList(MyApplication.getInstance(), cid, null, 20);
        uiMessageList = UIMessage.MessageList2UIMessageList(cacheMessageList);
        if (getIntent().hasExtra(EXTRA_NEED_GET_NEW_MESSAGE) && NetUtils.isNetworkConnected(MyApplication.getInstance())) {
            getNewMessageOfChannel();
        }
        initViews();
    }

    /**
     * 初始化Views
     */
    private void initViews() {
        initPullRefreshLayout();
        initChatInputMenu();
        setChannelTitle();
        initMsgListView();
        sendMsgFromShare();
    }

    /**
     * 初始化下拉刷新UI
     */
    private void initPullRefreshLayout() {
        swipeRefreshLayout.setColorSchemeColors(getResources().getColor(R.color.header_bg), getResources().getColor(R.color.header_bg));
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                getHistoryMessage();
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
            String iconUrl = DirectChannelUtils.getRobotIcon(MyApplication.getInstance(), conversation.getName());
            ImageDisplayUtils.getInstance().displayImage(robotPhotoImg, iconUrl, R.drawable.icon_person_default);
        } else {
            robotPhotoImg.setVisibility(View.GONE);
            headerText.setVisibility(View.VISIBLE);
            headerText.setText(CommunicationUtils.getConversationTitle(conversation));
        }
    }

    /**
     * 处理chatInputMenu是否显示，以及显示几个Menu上的item
     */
    private void initChatInputMenu() {
        chatInputMenu.setSpecialUser(isSpecialUser);
        chatInputMenu.setOtherLayoutView(swipeRefreshLayout, msgListView);
        if (conversation.getType().equals(Conversation.TYPE_GROUP)) {
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
            public void onSendVoiceRecordMsg(String results, float seconds, String filePath) {
                int duration = (int) seconds;
                if (duration == 0) {
                    duration = 1;
                }
                combinAndSendMessageWithFile(filePath, Message.MESSAGE_TYPE_MEDIA_VOICE, duration, results);
            }

            @Override
            public void onVoiceCommucaiton() {
                List<VoiceCommunicationJoinChannelInfoBean> voiceCommunicationUserInfoBeanList = new ArrayList<>();
                List<String> memberList = new ArrayList<>();
                memberList.add(DirectChannelUtils.getDirctChannelOtherUid(MyApplication.getInstance(), conversation.getName()));
                memberList.add(MyApplication.getInstance().getUid());
                List<ContactUser> contactUserList = ContactUserCacheUtils.getContactUserListById(memberList);
                for (int i = 0; i < contactUserList.size(); i++) {
                    VoiceCommunicationJoinChannelInfoBean voiceCommunicationJoinChannelInfoBean = new VoiceCommunicationJoinChannelInfoBean();
                    voiceCommunicationJoinChannelInfoBean.setUserId(contactUserList.get(i).getId());
                    voiceCommunicationJoinChannelInfoBean.setUserName(contactUserList.get(i).getName());
                    voiceCommunicationUserInfoBeanList.add(voiceCommunicationJoinChannelInfoBean);
                }
                Intent intent = new Intent();
                intent.setClass(ConversationActivity.this, ChannelVoiceCommunicationActivity.class);
                intent.putExtra("userList", (Serializable) voiceCommunicationUserInfoBeanList);
                intent.putExtra(ChannelVoiceCommunicationActivity.VOICE_COMMUNICATION_STATE, ChannelVoiceCommunicationActivity.INVITER_LAYOUT_STATE);
                startActivity(intent);
            }

            @Override
            public void onChatDraftsClear() {
                setChatDrafts();
            }
        });
        chatInputMenu.setInputLayout(conversation.getInput(), false);
        String chatDrafts = PreferencesByUserAndTanentUtils.getString(MyApplication.getInstance(), MyAppConfig.getChannelDrafsPreKey(cid));
        if (chatDrafts != null) {
            chatInputMenu.setChatDrafts(chatDrafts);
        }
    }


    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onReceiveSimpleEventMessageMessage(SimpleEventMessage eventMessage) {
        switch (eventMessage.getAction()) {
            case Constant.EVENTBUS_TAG_SEND_ACTION_CONTENT_MESSAGE:
                String actionContent = (String) eventMessage.getMessageObj();
                sendMessageWithText(actionContent, true, null);
                break;
            case Constant.EVENTBUS_TAG_UPDATE_CHANNEL_NAME:
                String name = ((Conversation) eventMessage.getMessageObj()).getName();
                conversation.setName(name);
                headerText.setText(name);
                break;
        }

    }


    /**
     * 初始化消息列表UI
     */
    private void initMsgListView() {
        linearLayoutManager = new LinearLayoutManager(this);
        msgListView.setLayoutManager(linearLayoutManager);
        adapter = new ChannelMessageAdapter(ConversationActivity.this, conversation.getType(), chatInputMenu);
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

            @Override
            public void onMediaVoiceReRecognize(UIMessage uiMessage, BubbleLayout bubbleLayout, QMUILoadingView downloadLoadingView) {
                showMeidaVoiceReRecognizerPop(uiMessage, bubbleLayout, downloadLoadingView);
            }

            @Override
            public void onAdapterDataSizeChange() {
                if (mediaVoiceReRecognizerPop != null && mediaVoiceReRecognizerPop.isShowing()) {
                    mediaVoiceReRecognizerPop.dismiss();
                }
            }
        });
        adapter.setMessageList(uiMessageList);
        msgListView.setAdapter(adapter);
        msgListView.MoveToPosition(uiMessageList.size() - 1);
    }

    /**
     * 弹出消息重新发送提示框
     *
     * @param uiMessage
     */
    private void showResendMessageDlg(final UIMessage uiMessage) {
        new MyQMUIDialog.MessageDialogBuilder(ConversationActivity.this)
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
            } else {
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
                IntentUtils.startActivity(ConversationActivity.this,
                        UserInfoActivity.class, bundle);
                break;
            case "file/regular-file":
            case "media/image":
                bundle.putString("mid", message.getId());
                bundle.putString("cid", message.getChannel());
                IntentUtils.startActivity(ConversationActivity.this,
                        ChannelMessageDetailActivity.class, bundle);
                break;
            case "comment/text-plain":
                String mid = message.getMsgContentComment().getMessage();
                bundle.putString("mid", mid);
                bundle.putString("cid", message.getChannel());
                IntentUtils.startActivity(ConversationActivity.this,
                        ChannelMessageDetailActivity.class, bundle);
                break;
            case "extended/links":
                String url = message.getMsgContentExtendedLinks().getUrl();
                UriUtils.openUrl(ConversationActivity.this, url);
                break;
            default:
                break;
        }
    }


    private void showMeidaVoiceReRecognizerPop(final UIMessage uiMessage, BubbleLayout anchor, final QMUILoadingView downloadLoadingView) {
        View contentView = LayoutInflater.from(this).inflate(R.layout.pop_voice_to_text_view, null);
        contentView.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
        mediaVoiceReRecognizerPop = new PopupWindow(contentView,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT, true);
        mediaVoiceReRecognizerPop.setTouchable(true);
        mediaVoiceReRecognizerPop.setOutsideTouchable(true);
        mediaVoiceReRecognizerPop.setTouchInterceptor(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return false;
            }
        });
        int[] location = new int[2];
        anchor.getLocationOnScreen(location);
        int popWidth = mediaVoiceReRecognizerPop.getContentView().getMeasuredWidth();
        int popHeight = mediaVoiceReRecognizerPop.getContentView().getMeasuredHeight();
        BubbleLayout voice2TextBubble = (BubbleLayout) contentView.findViewById(R.id.bl_voice_to_text);
        voice2TextBubble.setArrowPosition(popWidth / 2 - DensityUtil.dip2px(MyApplication.getInstance(), 9));
        mediaVoiceReRecognizerPop.showAtLocation(anchor, Gravity.NO_GRAVITY, location[0] + anchor.getWidth() / 2 - popWidth / 2, location[1] - popHeight - DensityUtil.dip2px(MyApplication.getInstance(), 5));
        voice2TextBubble.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mediaVoiceReRecognizerPop.dismiss();
                String mp3FileSavePath = MyAppConfig.getCacheVoiceFilePath(uiMessage.getMessage().getChannel(), uiMessage.getMessage().getId());
                //如果原文件不存在，不进行重新识别
                if (!FileUtils.isFileExist(mp3FileSavePath)) {
                    return;
                }
                final String wavFileSavePath = MyAppConfig.getCacheVoiceWAVFilePath(uiMessage.getMessage().getChannel(), uiMessage.getMessage().getId());

                LogUtils.jasonDebug("wavFileSavePath=" + wavFileSavePath);
                if(!FileUtils.isFileExist(wavFileSavePath)){

                    AudioFormatUtils.Mp3ToWav(mp3FileSavePath, wavFileSavePath, new ResultCallback() {
                        @Override
                        public void onSuccess() {
                            voiceToWord(wavFileSavePath,uiMessage,downloadLoadingView);
                        }

                        @Override
                        public void onFail() {

                        }
                    });
                }else {
                    voiceToWord(wavFileSavePath,uiMessage,downloadLoadingView);
                }
            }
        });
    }

    ;

    private void voiceToWord(String filePath, final UIMessage uiMessage, final QMUILoadingView downloadLoadingView) {
        downloadLoadingView.setVisibility(View.VISIBLE);
        Voice2StringMessageUtils voice2StringMessageUtils = new Voice2StringMessageUtils(this);
        //由于java api影响，mp3只能转化成8K采样率的wav文件
        voice2StringMessageUtils.setAudioSimpleRate(8000);
        voice2StringMessageUtils.setOnVoiceResultCallback(new OnVoiceResultCallback() {
            @Override
            public void onVoiceStart() {
            }

            @Override
            public void onVoiceResultSuccess(VoiceResult voiceResult, boolean isLast) {
                LogUtils.jasonDebug("voiceResult=" + voiceResult.getResults());
                Message message = uiMessage.getMessage();
                MsgContentMediaVoice originMsgContentMediaVoice = message.getMsgContentMediaVoice();
                if (!voiceResult.getResults().equals(originMsgContentMediaVoice.getResult())) {
                    MsgContentMediaVoice msgContentMediaVoice = new MsgContentMediaVoice();
                    msgContentMediaVoice.setDuration(originMsgContentMediaVoice.getDuration());
                    msgContentMediaVoice.setMedia(originMsgContentMediaVoice.getMedia());
                    msgContentMediaVoice.setJsonResults(voiceResult.getResults());
                    message.setContent(msgContentMediaVoice.toString());
                    int position = uiMessageList.indexOf(uiMessage);
                    if (position != -1) {
                        adapter.notifyItemChanged(position);
                    }
                    MessageCacheUtil.saveMessage(MyApplication.getInstance(), message);
                }


            }

            @Override
            public void onVoiceFinish() {
                downloadLoadingView.setVisibility(View.GONE);
            }

            @Override
            public void onVoiceLevelChange(int volume) {

            }

            @Override
            public void onVoiceResultError(VoiceResult errorResult) {
                downloadLoadingView.setVisibility(View.GONE);
            }
        });
        voice2StringMessageUtils.startVoiceListeningByVoiceFile(uiMessage.getMessage().getMsgContentMediaVoice().getDuration(), filePath);
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
            switch (requestCode) {
                case RQQUEST_CHOOSE_FILE:
                    if (NetUtils.isNetworkConnected(MyApplication.getInstance())) {
                        String filePath = GetPathFromUri4kitkat.getPathByUri(MyApplication.getInstance(), data.getData());
                        File file = new File(filePath);
                        if (StringUtils.isBlank(FileUtils.getSuffix(file))) {
                            ToastUtils.show(MyApplication.getInstance(),
                                    getString(R.string.not_support_upload));
                        } else {
                            combinAndSendMessageWithFile(filePath, Message.MESSAGE_TYPE_FILE_REGULAR_FILE);
                        }
                    }
                    break;
                case REQUEST_CAMERA:
                    if (NetUtils.isNetworkConnected(MyApplication.getInstance())) {
                        String imgPath = data.getExtras().getString(MyCameraActivity.OUT_FILE_PATH);
                        try {
                            File file = new Compressor(ConversationActivity.this).setMaxHeight(MyAppConfig.UPLOAD_ORIGIN_IMG_DEFAULT_SIZE).setMaxWidth(MyAppConfig.UPLOAD_ORIGIN_IMG_DEFAULT_SIZE).setQuality(90).setDestinationDirectoryPath(MyAppConfig.LOCAL_IMG_CREATE_PATH)
                                    .compressToFile(new File(imgPath));
                            imgPath = file.getAbsolutePath();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        combinAndSendMessageWithFile(imgPath, Message.MESSAGE_TYPE_MEDIA_IMAGE);
                    }
                    break;
                case REQUEST_MENTIONS:
                    // @返回
                    String result = data.getStringExtra("searchResult");
                    String uid = JSONUtils.getString(result, "uid", null);
                    String name = JSONUtils.getString(result, "name", null);
                    boolean isInputKeyWord = data.getBooleanExtra("isInputKeyWord", false);
                    chatInputMenu.addMentions(uid, name, isInputKeyWord);
                    break;
                case REQUEST_QUIT_CHANNELGROUP:
                    MyApplication.getInstance().setCurrentChannelCid("");
                    finish();
                    break;
            }
        } else {
            // 图库选择图片返回
            if (resultCode == ImagePicker.RESULT_CODE_ITEMS)
                if (data != null && requestCode == REQUEST_GELLARY) {
                    ArrayList<ImageItem> imageItemList = (ArrayList<ImageItem>) data
                            .getSerializableExtra(ImagePicker.EXTRA_RESULT_ITEMS);
                    for (int i = 0; i < imageItemList.size(); i++) {
                        String imgPath = imageItemList.get(i).path;
                        try {
                            File file = new Compressor(ConversationActivity.this).setMaxHeight(MyAppConfig.UPLOAD_ORIGIN_IMG_DEFAULT_SIZE).setMaxWidth(MyAppConfig.UPLOAD_ORIGIN_IMG_DEFAULT_SIZE).setQuality(90).setDestinationDirectoryPath(MyAppConfig.LOCAL_IMG_CREATE_PATH)
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
        combinAndSendMessageWithFile(filePath, messageType, duration, "");
    }

    private void combinAndSendMessageWithFile(String filePath, String messageType, int duration, String results) {
        File file = new File(filePath);
        if (!file.exists()) {
            if (messageType != Message.MESSAGE_TYPE_MEDIA_VOICE) {
                ToastUtils.show(MyApplication.getInstance(), R.string.file_not_exist);
            }
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
                fakeMessage = CommunicationUtils.combinLocalMediaVoiceMessage(cid, filePath, duration, results);
                break;
        }
        if (fakeMessage != null) {
            addLocalMessage(fakeMessage, 0);
            sendMessageWithFile(fakeMessage);
        }
    }

    /**
     * 将消息显示状态置为发送成功
     *
     * @param index
     */
    private void setMessageSendSuccess(int index, Message message) {
        UIMessage uiMessage = adapter.getItemData(index);
        uiMessage.setMessage(message);
        uiMessage.setId(message.getId());
        uiMessage.setSendStatus(1);
        int firstItemPosition = linearLayoutManager.findFirstVisibleItemPosition();
        if (index - firstItemPosition >= 0) {
            View view = msgListView.getChildAt(index - firstItemPosition);
            if (null != msgListView.getChildViewHolder(view)) {
                ChannelMessageAdapter.ViewHolder holder = (ChannelMessageAdapter.ViewHolder) msgListView.getChildViewHolder(view);
                holder.sendStatusLayout.setVisibility(View.INVISIBLE);
            }

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
                showConversationInfo();
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
        LoadingDialog.dimissDlg(loadingDlg);
        MyApplication.getInstance().setCurrentChannelCid("");
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
            EventBus.getDefault().post(new SimpleEventMessage(Constant.EVENTBUS_TAG_REFRESH_CONVERSATION_ADAPTER));
        }
    }

    /**
     * 展示群组或个人信息
     */
    private void showConversationInfo() {
        Bundle bundle = new Bundle();
        switch (conversation.getType()) {
            case Conversation.TYPE_GROUP:
                bundle.putSerializable(ConversationGroupInfoActivity.EXTRA_CID, conversation.getId());
                Intent intent = new Intent(this, ConversationGroupInfoActivity.class);
                intent.putExtras(bundle);
                startActivityForResult(intent, REQUEST_QUIT_CHANNELGROUP);
                break;
            case Conversation.TYPE_DIRECT:
                String uid = CommunicationUtils.getDirctChannelOtherUid(MyApplication.getInstance(), conversation.getName());
                bundle.putString("uid", uid);
                IntentUtils.startActivity(ConversationActivity.this,
                        UserInfoActivity.class, bundle);
                break;
            case Conversation.TYPE_CAST:
                bundle.putSerializable(ConversationCastInfoActivity.EXTRA_CID, conversation.getId());
                IntentUtils.startActivity(ConversationActivity.this,
                        ConversationCastInfoActivity.class, bundle);
                break;
            default:
                break;
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
        UIMessage UIMessage = new UIMessage(message);
        //本地添加的消息设置为正在发送状态
        UIMessage.setSendStatus(status);
        uiMessageList.add(UIMessage);
        adapter.setMessageList(uiMessageList);
        adapter.notifyItemInserted(uiMessageList.size() - 1);
        msgListView.MoveToPosition(uiMessageList.size() - 1);
    }

    /**
     * 消息发送失败处理
     *
     * @param fakeMessageId
     */
    private void setMessageSendFailStatus(String fakeMessageId) {
        //消息发送失败处理
        UIMessage fakeUIMessage = new UIMessage(fakeMessageId);
        int index = uiMessageList.indexOf(fakeUIMessage);
        if (index != -1) {
            uiMessageList.get(index).setSendStatus(2);
            adapter.setMessageList(uiMessageList);
            adapter.notifyItemChanged(index);
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
        if (InputMethodUtils.isSoftInputShow(ConversationActivity.this)) {
            InputMethodUtils.hide(ConversationActivity.this);
            return;
        }
        finishActivity();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (handler != null) {
            handler = null;
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
     * 获取历史消息
     */
    private void getHistoryMessage() {
        //当有网络并且本地没有连续消息时，网络获取
        if ((NetUtils.isNetworkConnected(MyApplication.getInstance(), false) &&
                !(uiMessageList.size() > 0 && MessageCacheUtil.isDataInLocal(ConversationActivity.this, cid, uiMessageList
                        .get(0).getCreationDate(), 20)))) {
            String newMessageId = uiMessageList.size() > 0 ? uiMessageList.get(0).getMessage().getId() : "";
            WSAPIService.getInstance().getHistoryMessage(cid, newMessageId);
        } else {
            getHistoryMessageFromLocal();
        }
    }

    private void getHistoryMessageFromLocal() {
        if (uiMessageList.size() > 0) {
            List<Message> messageList = MessageCacheUtil.getHistoryMessageList(
                    MyApplication.getInstance(), cid, uiMessageList.get(0).getCreationDate(), 20);
            uiMessageList.addAll(0, UIMessage.MessageList2UIMessageList(messageList));
            adapter.setMessageList(uiMessageList);
            adapter.notifyItemRangeInserted(0, messageList.size());
            msgListView.scrollToPosition(messageList.size() - 1);
        }

        swipeRefreshLayout.setRefreshing(false);
    }


    //接收到websocket发过来的消息
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onReceiveWSMessage(EventMessage eventMessage) {
        if (eventMessage.getTag().equals(Constant.EVENTBUS_TAG_RECERIVER_SINGLE_WS_MESSAGE)) {
            if (eventMessage.getStatus() == 200) {
                String content = eventMessage.getContent();
                JSONObject contentObj = JSONUtils.getJSONObject(content);
                Message receivedWSMessage = new Message(contentObj);
                //判断消息是否是当前频道并验重处理
                if (cid.equals(receivedWSMessage.getChannel()) && !uiMessageList.contains(new UIMessage(receivedWSMessage.getId()))) {
                    int size = uiMessageList.size();
                    int index = -1;
                    if (size > 0) {
                        for (int i = size - 1; i >= 0; i--) {
                            if (uiMessageList.get(i).getMessage().getId().equals(String.valueOf(eventMessage.getExtra()))) {
                                index = i;
                                break;
                            }
                        }

                    }
                    if (index == -1) {
                        uiMessageList.add(new UIMessage(receivedWSMessage));
                        adapter.setMessageList(uiMessageList);
                        adapter.notifyItemInserted(uiMessageList.size() - 1);
                        msgListView.MoveToPosition(uiMessageList.size() - 1);
                    } else {
                        uiMessageList.remove(index);
                        uiMessageList.add(index, new UIMessage(receivedWSMessage));
                        //如果是图片类型消息的话不再重新刷新消息体，防止图片重新加载
                        if (receivedWSMessage.getType().equals(Message.MESSAGE_TYPE_MEDIA_IMAGE)) {
                            setMessageSendSuccess(index, receivedWSMessage);
                            adapter.setMessageList(uiMessageList);
                        } else {
                            adapter.setMessageList(uiMessageList);
                            adapter.notifyItemChanged(index);
                        }

                    }
                }
                WSAPIService.getInstance().setChannelMessgeStateRead(cid);
            } else {
                setMessageSendFailStatus(String.valueOf(eventMessage.getExtra()));
            }
        }

    }

    //接收到websocket发过来的消息
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onGetMessageById(EventMessage eventMessage) {
        if (eventMessage.getTag().equals(Constant.EVENTBUS_TAG_GET_MESSAGE_BY_ID)) {
            if (eventMessage.getStatus() == EventMessage.RESULT_OK) {
                String content = eventMessage.getContent();
                JSONObject contentobj = JSONUtils.getJSONObject(content);
                Message message = new Message(contentobj);
                message.setRead(1);
                MessageCacheUtil.saveMessage(MyApplication.getInstance(), message);
                adapter.notifyDataSetChanged();
            }

        }

    }


    //接收到websocket发过来的消息
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onReceiveNewMessage(EventMessage eventMessage) {
        if (eventMessage.getTag().equals(Constant.EVENTBUS_TAG_GET_NEW_MESSAGE) && eventMessage.getExtra().equals(cid)) {
            if (eventMessage.getStatus() == 200) {
                String content = eventMessage.getContent();
                GetChannelMessagesResult getChannelMessagesResult = new GetChannelMessagesResult(content);
                final List<Message> newMessageList = getChannelMessagesResult.getMessageList();
                if (newMessageList.size() > 0) {
                    MessageCacheUtil.saveMessageList(MyApplication.getInstance(), newMessageList, null);
                }
                WSAPIService.getInstance().setChannelMessgeStateRead(cid);
                final List<Message> cacheMessageList = MessageCacheUtil.getHistoryMessageList(MyApplication.getInstance(), cid, null, 20);
                uiMessageList = UIMessage.MessageList2UIMessageList(cacheMessageList);
                adapter.setMessageList(uiMessageList);
                adapter.notifyDataSetChanged();
                msgListView.scrollToPosition(uiMessageList.size() - 1);
            }

        }
    }

    //接收到websocket发过来的消息
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onReceiveHistoryMessage(EventMessage eventMessage) {
        if (eventMessage.getTag().equals(Constant.EVENTBUS_TAG_GET_HISTORY_MESSAGE) && eventMessage.getExtra().equals(cid)) {
            if (eventMessage.getStatus() == EventMessage.RESULT_OK) {
                String content = eventMessage.getContent();
                GetChannelMessagesResult getChannelMessagesResult = new GetChannelMessagesResult(content);
                final List<Message> messageList = getChannelMessagesResult.getMessageList();
                if (messageList.size() > 0 && messageList.get(0).getChannel().equals(cid)) {
                    Long targetMessageCreationDate = null;
                    if (uiMessageList.size() > 0) {
                        targetMessageCreationDate = uiMessageList.get(0).getCreationDate();
                    }
                    MessageCacheUtil.saveMessageList(MyApplication.getInstance(), messageList, targetMessageCreationDate);
                    uiMessageList.addAll(0, UIMessage.MessageList2UIMessageList(messageList));
                    adapter.setMessageList(uiMessageList);
                    adapter.notifyItemRangeInserted(0, messageList.size());
                    msgListView.scrollToPosition(messageList.size() - 1);
                }
                swipeRefreshLayout.setRefreshing(false);
            } else {
                getHistoryMessageFromLocal();
            }
        }
    }


    //接收到从沟通页面传来的离线消息
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onReiceveWSOfflineMessage(SimpleEventMessage eventMessage) {
        if (eventMessage.getAction().equals(Constant.EVENTBUS_TAG_CURRENT_CHANNEL_OFFLINE_MESSAGE)) {
            List<Message> offlineMessageList = (List<Message>) eventMessage.getMessageObj();
            Iterator<Message> it = offlineMessageList.iterator();
            //去重
            if (uiMessageList.size() > 0) {
                while (it.hasNext()) {
                    Message offlineMessage = it.next();
                    UIMessage uiMessage = new UIMessage(offlineMessage.getId());
                    if (uiMessageList.contains(uiMessage)) {
                        it.remove();
                    }
                }
            }
            if (offlineMessageList.size() > 0) {
                int currentPostion = uiMessageList.size() - 1;
                List<UIMessage> offlineUIMessageList = UIMessage.MessageList2UIMessageList(offlineMessageList);
                uiMessageList.addAll(offlineUIMessageList);
                adapter.setMessageList(uiMessageList);
                adapter.notifyItemRangeInserted(currentPostion + 1, offlineUIMessageList.size());
                msgListView.MoveToPosition(uiMessageList.size() - 1);
                WSAPIService.getInstance().setChannelMessgeStateRead(cid);
            }
        }

    }


    /**
     * 获取此频道的最新消息
     */
    private void getNewMessageOfChannel() {
        if (NetUtils.isNetworkConnected(this, false)) {
            WSAPIService.getInstance().getChannelNewMessage(cid);
        }
    }

}