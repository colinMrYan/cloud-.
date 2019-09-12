package com.inspur.emmcloud.ui.chat;

import android.content.BroadcastReceiver;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.text.SpannableString;
import android.view.ContextThemeWrapper;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.inspur.emmcloud.MyApplication;
import com.inspur.emmcloud.R;
import com.inspur.emmcloud.adapter.ChannelMessageAdapter;
import com.inspur.emmcloud.api.APIInterfaceInstance;
import com.inspur.emmcloud.api.apiservice.ChatAPIService;
import com.inspur.emmcloud.api.apiservice.WSAPIService;
import com.inspur.emmcloud.baselib.util.DensityUtil;
import com.inspur.emmcloud.baselib.util.IntentUtils;
import com.inspur.emmcloud.baselib.util.JSONUtils;
import com.inspur.emmcloud.baselib.util.StringUtils;
import com.inspur.emmcloud.baselib.util.ToastUtils;
import com.inspur.emmcloud.baselib.widget.CustomLoadingView;
import com.inspur.emmcloud.baselib.widget.LoadingDialog;
import com.inspur.emmcloud.baselib.widget.dialogs.CustomDialog;
import com.inspur.emmcloud.baselib.widget.roundbutton.CustomRoundButton;
import com.inspur.emmcloud.basemodule.application.BaseApplication;
import com.inspur.emmcloud.basemodule.bean.SimpleEventMessage;
import com.inspur.emmcloud.basemodule.config.Constant;
import com.inspur.emmcloud.basemodule.config.MyAppConfig;
import com.inspur.emmcloud.basemodule.util.AppUtils;
import com.inspur.emmcloud.basemodule.util.FileUtils;
import com.inspur.emmcloud.basemodule.util.ImageDisplayUtils;
import com.inspur.emmcloud.basemodule.util.InputMethodUtils;
import com.inspur.emmcloud.basemodule.util.NetUtils;
import com.inspur.emmcloud.basemodule.util.WebServiceRouterManager;
import com.inspur.emmcloud.basemodule.util.compressor.Compressor;
import com.inspur.emmcloud.basemodule.util.imagepicker.ImagePicker;
import com.inspur.emmcloud.basemodule.util.imagepicker.bean.ImageItem;
import com.inspur.emmcloud.basemodule.util.imagepicker.ui.ImageGridActivity;
import com.inspur.emmcloud.basemodule.util.mycamera.MyCameraActivity;
import com.inspur.emmcloud.bean.appcenter.volume.VolumeFile;
import com.inspur.emmcloud.bean.chat.Conversation;
import com.inspur.emmcloud.bean.chat.GetChannelMessagesResult;
import com.inspur.emmcloud.bean.chat.Message;
import com.inspur.emmcloud.bean.chat.MsgContentMediaImage;
import com.inspur.emmcloud.bean.chat.MsgContentMediaVoice;
import com.inspur.emmcloud.bean.chat.MsgContentRegularFile;
import com.inspur.emmcloud.bean.chat.UIMessage;
import com.inspur.emmcloud.bean.chat.VoiceCommunicationJoinChannelInfoBean;
import com.inspur.emmcloud.bean.system.EventMessage;
import com.inspur.emmcloud.bean.system.VoiceResult;
import com.inspur.emmcloud.componentservice.contact.ContactUser;
import com.inspur.emmcloud.interf.OnVoiceResultCallback;
import com.inspur.emmcloud.interf.ProgressCallback;
import com.inspur.emmcloud.interf.ResultCallback;
import com.inspur.emmcloud.push.WebSocketPush;
import com.inspur.emmcloud.ui.chat.pop.PopupWindowList;
import com.inspur.emmcloud.ui.contact.ContactSearchActivity;
import com.inspur.emmcloud.ui.contact.ContactSearchFragment;
import com.inspur.emmcloud.ui.contact.UserInfoActivity;
import com.inspur.emmcloud.ui.schedule.meeting.ScheduleAddActivity;
import com.inspur.emmcloud.util.privates.ChatFileUploadManagerUtils;
import com.inspur.emmcloud.util.privates.ChatMsgContentUtils;
import com.inspur.emmcloud.util.privates.CommunicationUtils;
import com.inspur.emmcloud.util.privates.ConversationCreateUtils;
import com.inspur.emmcloud.util.privates.DirectChannelUtils;
import com.inspur.emmcloud.util.privates.NotificationUpgradeUtils;
import com.inspur.emmcloud.util.privates.UriUtils;
import com.inspur.emmcloud.util.privates.Voice2StringMessageUtils;
import com.inspur.emmcloud.util.privates.audioformat.AudioMp3ToPcm;
import com.inspur.emmcloud.util.privates.cache.ContactUserCacheUtils;
import com.inspur.emmcloud.util.privates.cache.ConversationCacheUtils;
import com.inspur.emmcloud.util.privates.cache.MessageCacheUtil;
import com.inspur.emmcloud.util.privates.richtext.markdown.MarkDown;
import com.inspur.emmcloud.widget.ECMChatInputMenu;
import com.inspur.emmcloud.widget.ECMChatInputMenu.ChatInputMenuListener;
import com.inspur.emmcloud.widget.ECMChatInputMenuCallback;
import com.inspur.emmcloud.widget.RecycleViewForSizeChange;
import com.inspur.emmcloud.widget.bubble.BubbleLayout;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import butterknife.BindView;

public class ConversationActivity extends ConversationBaseActivity {

    private static final int REQUEST_QUIT_CHANNELGROUP = 1;
    private static final int REQUEST_GELLARY = 2;
    private static final int REQUEST_CAMERA = 3;
    private static final int RQQUEST_CHOOSE_FILE = 4;
    private static final int REQUEST_MENTIONS = 5;

    private static final int SHARE_SEARCH_RUEST_CODE = 31;

    private static final int REFRESH_HISTORY_MESSAGE = 6;
    private static final int REFRESH_PUSH_MESSAGE = 7;
    private static final int REFRESH_OFFLINE_MESSAGE = 8;
    private static final int UNREAD_NUMBER_BORDER = 20;

    @BindView(R.id.msg_list)
    RecycleViewForSizeChange msgListView;

    @BindView(R.id.refresh_layout)
    SwipeRefreshLayout swipeRefreshLayout;

    @BindView(R.id.chat_input_menu)
    ECMChatInputMenu chatInputMenu;
    @BindView(R.id.header_text)
    TextView headerText;

    @BindView(R.id.robot_photo_img)
    ImageView robotPhotoImg;
    @BindView(R.id.btn_conversation_unread)
    CustomRoundButton unreadRoundBtn;
    private LinearLayoutManager linearLayoutManager;
    private String robotUid = "BOT6004";
    private List<UIMessage> uiMessageList = new ArrayList<>();
    private ChannelMessageAdapter adapter;
    private Handler handler;
    private boolean isSpecialUser = false; //小智机器人进行特殊处理
    private BroadcastReceiver refreshNameReceiver;
    private PopupWindow mediaVoiceReRecognizerPop;
    private PopupWindow resendMessagePop;
    private PopupWindowList mPopupWindowList; //仿微信长按处理

    private UIMessage backUiMessage = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EventBus.getDefault().register(this);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        handleMessage();
    }

    private void handleMessage() {
        handler = new Handler() {
            @Override
            public void handleMessage(android.os.Message msg) {
                switch (msg.what) {
                    case REFRESH_HISTORY_MESSAGE:
                        List<UIMessage> historyUIMessageList = (List<UIMessage>) msg.obj;
                        if (uiMessageList != null && uiMessageList.size() > 0) {
                            uiMessageList.addAll(0, historyUIMessageList);
                            adapter.setMessageList(uiMessageList);
                            adapter.notifyItemRangeInserted(0, historyUIMessageList.size());
                            msgListView.scrollToPosition(historyUIMessageList.size() - 1);
                        }
                        swipeRefreshLayout.setRefreshing(false);
                        break;
                    case REFRESH_PUSH_MESSAGE:
                        uiMessageList = (List<UIMessage>) msg.obj;
                        adapter.setMessageList(uiMessageList);
                        adapter.notifyDataSetChanged();
                        msgListView.scrollToPosition(uiMessageList.size() - 1);
                        WSAPIService.getInstance().setChannelMessgeStateRead(cid);
                        break;
                    case REFRESH_OFFLINE_MESSAGE:
                        if (adapter == null) {
                            return;
                        }
                        List<Message> offlineMessageList = (List<Message>) msg.obj;
                        Iterator<Message> it = offlineMessageList.iterator();
                        if (uiMessageList.size() > 0) {
                            while (it.hasNext()) {
                                //发送成功的消息去重去重
                                Message offlineMessage = it.next();
                                if (uiMessageList.contains(new UIMessage(offlineMessage.getId()))) {
                                    it.remove();
                                } else {
                                    //离线消息获取后，更改对应的未发送成功状态的消息
                                    int index = uiMessageList.indexOf((new UIMessage(offlineMessage.getTmpId())));
                                    if (index != -1) {
                                        uiMessageList.get(index).setSendStatus(Message.MESSAGE_SEND_SUCCESS);
                                        it.remove();
                                    }
                                }
                            }
                        }
                        if (offlineMessageList.size() > 0) {
                            List<UIMessage> offlineUIMessageList = UIMessage.MessageList2UIMessageList(offlineMessageList);
                            uiMessageList.addAll(offlineUIMessageList);
                            adapter.setMessageList(uiMessageList);
                            adapter.notifyDataSetChanged();
                            msgListView.MoveToPosition(uiMessageList.size() - 1);
                            WSAPIService.getInstance().setChannelMessgeStateRead(cid);
                        }
                        break;
                    default:
                        break;
                }
            }
        };
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
        List<Message> cacheMessageList;
        UIMessage uiMessage = null;
        if (getIntent().hasExtra(EXTRA_UIMESSAGE)) {
            uiMessage = (UIMessage) getIntent().getSerializableExtra(EXTRA_UIMESSAGE);
            cacheMessageList = MessageCacheUtil.getHistoryMessageList(MyApplication.getInstance(), cid, null);
        } else {
            cacheMessageList = MessageCacheUtil.getHistoryMessageList(MyApplication.getInstance(), cid, null, 20);
        }
        if (cacheMessageList == null) {
            cacheMessageList = new ArrayList<>();
        }
        List<Message> messageSendingList = new ArrayList<>();
//        for (int i = 0; i < cacheMessageList.size(); i++) {
//            if (cacheMessageList.get(i).getSendStatus() == Message.MESSAGE_SEND_ING && ((System.currentTimeMillis() - cacheMessageList.get(i).getCreationDate()) > 16 * 1000)) {
//                cacheMessageList.get(i).setSendStatus(Message.MESSAGE_SEND_FAIL);
//                messageSendingList.add(cacheMessageList.get(i));
//            }
//        }

        for (int i = 0; i < cacheMessageList.size(); i++) {
            Message message = cacheMessageList.get(i);
            if (message.getSendStatus() == Message.MESSAGE_SEND_ING || message.getSendStatus() == Message.MESSAGE_SEND_FAIL) {
                message.setSendStatus(getInitStatus(message));
                messageSendingList.add(message);
            }
        }
        persistenceMessageSendStatus(messageSendingList);
        uiMessageList = UIMessage.MessageList2UIMessageList(cacheMessageList);
        initViews();
        if (getIntent().hasExtra(EXTRA_NEED_GET_NEW_MESSAGE) && NetUtils.isNetworkConnected(MyApplication.getInstance())) {
            getNewMessageOfChannel();
        }
        if (uiMessage != null) {
            final int position = uiMessageList.indexOf(uiMessage);
            if (position != -1) {
                msgListView.post(new Runnable() {
                    @Override
                    public void run() {
                        msgListView.MoveToPosition(position);
                    }
                });
                }
            }
    }

    /**
     * 获取当前发送状态，需要OSS接口支持
     *
     * @param message
     * @return
     */
    private int getInitStatus(Message message) {
        return ChatFileUploadManagerUtils.getInstance().isMessageResourceUploading(message) ? Message.MESSAGE_SEND_ING : Message.MESSAGE_SEND_FAIL;
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
        setUnReadMessageCount();
    }

    private void setUnReadMessageCount() {
        if (getIntent().hasExtra(EXTRA_UNREAD_MESSAGE)) {
            final List<Message> unReadMessageList = (List<Message>) getIntent().getSerializableExtra(EXTRA_UNREAD_MESSAGE);
//            unreadRoundBtn.setVisibility(unReadMessageList.size() > UNREAD_NUMBER_BORDER ? View.VISIBLE : View.GONE);
            unreadRoundBtn.setText(getString(R.string.chat_conversation_unread_count, unReadMessageList.size()));
            unreadRoundBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    List<UIMessage> unReadMessageUIList = UIMessage.MessageList2UIMessageList(unReadMessageList);
                    uiMessageList.clear();
                    uiMessageList.addAll(unReadMessageUIList);
                    adapter.setMessageList(uiMessageList);
                    adapter.notifyDataSetChanged();
                    msgListView.MoveToPosition(0);
                    unreadRoundBtn.setVisibility(View.GONE);
                    msgListView.scrollToPosition(0);
                }
            });
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
                combinAndSendMessageWithFile(filePath, Message.MESSAGE_TYPE_MEDIA_VOICE, duration, results, null);
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
        String draftMessageContent = MessageCacheUtil.getDraftByCid(ConversationActivity.this, cid);
        if (draftMessageContent != null) {
            chatInputMenu.setChatDrafts(draftMessageContent);
        }
        chatInputMenu.setInputMenuClickCallback(new ECMChatInputMenuCallback() {
            @Override
            public void onInputMenuClick(String type) {
                inputMenuClick(type);
            }
        });
    }

    private void inputMenuClick(String type) {
        switch (type) {
            case "mail":
                if (conversation == null) return;
                List<ContactUser> totalList = ContactUserCacheUtils.getContactUserListById(conversation.getMemberList());
                final List<ContactUser> userList = new ArrayList<>();
                for (ContactUser user : totalList) {
                    if (!BaseApplication.getInstance().getUid().equals(user.getId())) {
                        userList.add(user);
                    }
                }
                if (userList.size() > 50) {
                    new CustomDialog.MessageDialogBuilder(this)
                            .setMessage(userList.size() > 200 ? R.string.chat_send_email_max_person_tip : R.string.chat_send_email_too_many_person_tip)
                            .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    sendEmail(userList);
                                }
                            })
                            .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                }
                            })
                            .show();
                } else {
                    sendEmail(userList);
                }

                break;
        }
    }

    private void sendEmail(List<ContactUser> userList) {
        String mailListStr = userList.get(0).getEmail();
        StringBuilder builder = new StringBuilder(mailListStr);
        for (int i = 1; i < userList.size(); i++) {
            builder.append(",");
            builder.append(userList.get(i).getEmail());
        }
        mailListStr = builder.toString();

        Intent intent = new Intent(Intent.ACTION_SENDTO);
        intent.setData(Uri.parse("mailto:" + mailListStr));
        startActivity(intent);
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
            case Constant.EVENTBUS_TAG_COMMENT_MESSAGE:
                Message message = (Message) eventMessage.getMessageObj();
                uiMessageList.add(new UIMessage(message));
                adapter.setMessageList(uiMessageList);
                adapter.notifyItemInserted(uiMessageList.size() - 1);
                msgListView.MoveToPosition(uiMessageList.size() - 1);
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
            public void onMessageResend(UIMessage uiMessage, View view) {
                if (uiMessage.getSendStatus() == Message.MESSAGE_SEND_FAIL) {
                    showResendMessageDlg(uiMessage, view);
                }
            }

            @Override
            public void onMediaVoiceReRecognize(UIMessage uiMessage, BubbleLayout bubbleLayout, CustomLoadingView downloadLoadingView) {
                showMediaVoiceReRecognizerPop(uiMessage, bubbleLayout, downloadLoadingView);
            }

            @Override
            public void onAdapterDataSizeChange() {
                if (mediaVoiceReRecognizerPop != null && mediaVoiceReRecognizerPop.isShowing()) {
                    mediaVoiceReRecognizerPop.dismiss();
                }
            }

            @Override
            public boolean onCardItemLongClick(View view, UIMessage uiMessage) {
                backUiMessage = uiMessage;
                int[] operationsId = getCardLongClickOperations(uiMessage);
                if (operationsId.length > 0 && uiMessage.getSendStatus() == 1) {
//                    showLongClickOperationsDialog(operationsId, ConversationActivity.this, uiMessage);
                    showLongClickDialog(operationsId, uiMessage, view);
                }
                return true;
            }

            @Override
            public void onCardItemClick(View view, UIMessage uiMessage) {
                if (uiMessage.getSendStatus() == 1) {
                    CardClickOperation(ConversationActivity.this, view, uiMessage);
                }
            }

            @Override
            public void onCardItemLayoutClick(View view, UIMessage uiMessage) {
                if (uiMessage.getSendStatus() == 1) {
                    Message message = uiMessage.getMessage();
                    switch (message.getType()) {
                        case Message.MESSAGE_TYPE_FILE_REGULAR_FILE:
                        case Message.MESSAGE_TYPE_MEDIA_IMAGE:
                            Bundle bundle = new Bundle();
                            bundle.putString("mid", message.getId());
                            bundle.putString(EXTRA_CID, message.getChannel());
                            IntentUtils.startActivity(ConversationActivity.this,
                                    ChannelMessageDetailActivity.class, bundle);
                            break;
                    }
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
    private void showResendMessageDlg(final UIMessage uiMessage, View view) {
        View contentView = LayoutInflater.from(this).inflate(R.layout.pop_voice_to_text_view, null);
        ((TextView) contentView.findViewById(R.id.tv_pop_title)).setText(getString(R.string.chat_resend_message));
        contentView.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
        resendMessagePop = new PopupWindow(contentView,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT, true);
        resendMessagePop.setTouchable(true);
        resendMessagePop.setOutsideTouchable(true);
        resendMessagePop.setTouchInterceptor(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return false;
            }
        });
        int[] location = new int[2];
        view.getLocationOnScreen(location);
        int popWidth = resendMessagePop.getContentView().getMeasuredWidth();
        int popHeight = resendMessagePop.getContentView().getMeasuredHeight();
        BubbleLayout resendMessageBubbleLayout = contentView.findViewById(R.id.bl_voice_to_text);
        resendMessageBubbleLayout.setArrowPosition(popWidth / 2 - DensityUtil.dip2px(MyApplication.getInstance(), 9));
        resendMessagePop.showAtLocation(view, Gravity.NO_GRAVITY, location[0] +
                view.getWidth() / 2 - popWidth / 2, location[1] -
                popHeight - DensityUtil.dip2px(MyApplication.getInstance(), 5));
        resendMessagePop.showAsDropDown(view);
        resendMessageBubbleLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                resendMessagePop.dismiss();
                resendMessage(uiMessage);
            }
        });

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
            String messageType = message.getType();
            if (!FileUtils.isFileExist(message.getLocalPath()) && (messageType.equals(Message.MESSAGE_TYPE_FILE_REGULAR_FILE)
                    || messageType.equals(Message.MESSAGE_TYPE_MEDIA_IMAGE) || messageType.equals(Message.MESSAGE_TYPE_MEDIA_VOICE))) {
                ToastUtils.show(ConversationActivity.this, getString(R.string.resend_file_failed));
                return;
            }
            uiMessage.setSendStatus(Message.MESSAGE_SEND_ING);
            setMessageSendStatusAndSendTime(message, Message.MESSAGE_SEND_ING);
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
            switch (messageType) {
                case Message.MESSAGE_TYPE_FILE_REGULAR_FILE:
                    if (message.getMsgContentAttachmentFile().getMedia().equals(message.getLocalPath())) {
                        sendMessageWithFile(message);
                    } else {
                        WSAPIService.getInstance().sendChatRegularFileMsg(message);
                    }
                    break;
                case Message.MESSAGE_TYPE_MEDIA_IMAGE:
                    if (message.getMsgContentMediaImage().getRawMedia().equals(message.getLocalPath())) {
                        sendMessageWithFile(message);
                    } else {
                        WSAPIService.getInstance().sendChatMediaImageMsg(message);
                    }
                    break;
                case Message.MESSAGE_TYPE_MEDIA_VOICE:
                    resendVoiceMessage(uiMessage);
                    break;
                case Message.MESSAGE_TYPE_COMMENT_TEXT_PLAIN:
                    WSAPIService.getInstance().sendChatCommentTextPlainMsg(message);
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
     * 重发音频消息
     *
     * @param uiMessage
     */
    private void resendVoiceMessage(final UIMessage uiMessage) {
        if (AppUtils.getIsVoiceWordOpen() && StringUtils.isBlank(uiMessage.getMessage().getMsgContentMediaVoice().getResult())) {
            String localMp3Path = uiMessage.getMessage().getLocalPath();
            String localWavPath = localMp3Path.replace(".mp3", ".wav");
            if (FileUtils.isFileExist(localWavPath)) {
                voiceToWord(localWavPath, uiMessage, null);
            } else {
                final String dstPcmPath = localMp3Path.replace(".mp3", ".pcm");
                new AudioMp3ToPcm().startMp3ToPCM(localMp3Path, dstPcmPath, new ResultCallback() {
                    @Override
                    public void onSuccess() {
                        voiceToWord(dstPcmPath, uiMessage, null);
                    }

                    @Override
                    public void onFail() {
                        sendVoiceMessage(uiMessage.getMessage());
                    }
                });
            }

        } else {
            sendVoiceMessage(uiMessage.getMessage());
        }
    }

    private void sendVoiceMessage(Message message) {
        message.setCreationDate(System.currentTimeMillis());
        if (message.getMsgContentMediaVoice().getMedia().equals(message.getLocalPath())) {
            sendMessageWithFile(message);
        } else {
            WSAPIService.getInstance().sendChatMediaVoiceMsg(message);
        }
    }

    private void showMediaVoiceReRecognizerPop(final UIMessage uiMessage, BubbleLayout anchor, final CustomLoadingView downloadLoadingView) {
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
                final String pcmFileSavePath = MyAppConfig.getCacheVoicePCMFilePath(uiMessage.getMessage().getChannel(), uiMessage.getMessage().getId());

                if (!FileUtils.isFileExist(pcmFileSavePath)) {
                    new AudioMp3ToPcm().startMp3ToPCM(mp3FileSavePath, pcmFileSavePath, new ResultCallback() {
                        @Override
                        public void onSuccess() {
                            voiceToWord(pcmFileSavePath, uiMessage, downloadLoadingView);
                        }

                        @Override
                        public void onFail() {

                        }
                    });
                } else {
                    voiceToWord(pcmFileSavePath, uiMessage, downloadLoadingView);
                }
            }
        });
    }

    private void voiceToWord(String filePath, final UIMessage uiMessage, final CustomLoadingView downloadLoadingView) {
        if (downloadLoadingView != null) {
            downloadLoadingView.setVisibility(View.VISIBLE);
        }
        Voice2StringMessageUtils voice2StringMessageUtils = new Voice2StringMessageUtils(this);
        if (FileUtils.getFileExtension(filePath).equals("pcm")) {
            voice2StringMessageUtils.setAudioSimpleRate(8000);
        }
        voice2StringMessageUtils.setOnVoiceResultCallback(new OnVoiceResultCallback() {
            @Override
            public void onVoiceStart() {
            }

            @Override
            public void onVoiceResultSuccess(VoiceResult voiceResult, boolean isLast) {
                Message message = uiMessage.getMessage();
                MsgContentMediaVoice originMsgContentMediaVoice = message.getMsgContentMediaVoice();
                if (!voiceResult.getResults().equals(originMsgContentMediaVoice.getResult())) {
                    MsgContentMediaVoice msgContentMediaVoice = new MsgContentMediaVoice();
                    msgContentMediaVoice.setDuration(originMsgContentMediaVoice.getDuration());
                    msgContentMediaVoice.setMedia(originMsgContentMediaVoice.getMedia());
                    msgContentMediaVoice.setJsonResults(voiceResult.getResults());
                    message.setContent(msgContentMediaVoice.toString());
                    if (downloadLoadingView == null) {
                        sendVoiceMessage(message);
                    } else {
                        int position = uiMessageList.indexOf(uiMessage);
                        if (position != -1) {
                            adapter.notifyItemChanged(position);
                        }
                    }
                    MessageCacheUtil.saveMessage(MyApplication.getInstance(), message);
                }
            }

            @Override
            public void onVoiceFinish() {
                if (downloadLoadingView != null) {
                    downloadLoadingView.setVisibility(View.GONE);
                }
            }

            @Override
            public void onVoiceLevelChange(int volume) {

            }

            @Override
            public void onVoiceResultError(VoiceResult errorResult) {
                if (downloadLoadingView != null) {
                    downloadLoadingView.setVisibility(View.GONE);
                }
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
                    if (getIntent().hasExtra("share_obj_form_volume")) {
                        String cid = getIntent().getExtras().getString("cid");
                        String path = getIntent().getExtras().getString("path");
                        VolumeFile volumeFile = (VolumeFile) getIntent().getSerializableExtra("share_obj_form_volume");
                        transmitMsgFromVolume(cid, volumeFile, path);
                    } else {
                        List<String> pathList = getIntent().getStringArrayListExtra("share_paths");
                        for (String url : pathList) {
                            String urlLowerCase = url.toLowerCase();
                            boolean isImage = urlLowerCase.endsWith("png") || urlLowerCase.endsWith("jpg") || urlLowerCase.endsWith("jpeg") || urlLowerCase.endsWith("dng");
                            combinAndSendMessageWithFile(isImage ? getCompressorUrl(url) : url, isImage ? Message.MESSAGE_TYPE_MEDIA_IMAGE : Message.MESSAGE_TYPE_FILE_REGULAR_FILE, null);
                        }
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

    /**
     * 压缩图片逻辑，正常压缩返回压缩url，压缩有异常返回传入的原url
     *
     * @param url
     * @return
     */
    private String getCompressorUrl(String url) {
        String compressorUrl = "";
        Compressor compressor = new Compressor(ConversationActivity.this).setMaxArea(MyAppConfig.UPLOAD_ORIGIN_IMG_DEFAULT_SIZE * MyAppConfig.UPLOAD_ORIGIN_IMG_DEFAULT_SIZE).setQuality(90).setDestinationDirectoryPath(MyAppConfig.LOCAL_IMG_CREATE_PATH);
        try {
            File file = compressor.compressToFile(new File(url));
            compressorUrl = file.getAbsolutePath();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return StringUtils.isBlank(compressorUrl) ? url : compressorUrl;
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode,
                                    final Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case RQQUEST_CHOOSE_FILE:
                    List<String> filePathList = data.getStringArrayListExtra("pathList");
                    for (String filepath : filePathList) {
                        combinAndSendMessageWithFile(filepath, Message.MESSAGE_TYPE_FILE_REGULAR_FILE, null);
                    }

//                    String filePath = GetPathFromUri4kitkat.getPathByUri(MyApplication.getInstance(), data.getData());
//                    File file = new File(filePath);
//                    if (StringUtils.isBlank(FileUtils.getSuffix(file))) {
//                        ToastUtils.show(MyApplication.getInstance(),
//                                getString(R.string.not_support_upload));
//                    } else {
//                        combinAndSendMessageWithFile(filepath, Message.MESSAGE_TYPE_FILE_REGULAR_FILE, null);
//                    }
                    break;
                case REQUEST_CAMERA:
                    String imgPath = getCompressorUrl(data.getExtras().getString(MyCameraActivity.OUT_FILE_PATH));
                    combinAndSendMessageWithFile(imgPath, Message.MESSAGE_TYPE_MEDIA_IMAGE, null);
                    break;
                case REQUEST_MENTIONS:
                    // @返回
                    String result = data.getStringExtra("searchResult");
                    JSONArray jsonArray = JSONUtils.getJSONArray(result, new JSONArray());
                    for (int i = 0; i < jsonArray.length(); i++) {
                        try {
                            String uid = JSONUtils.getString(jsonArray.getString(i), "uid", null);
                            String name = JSONUtils.getString(jsonArray.getString(i), "name", null);
                            boolean isInputKeyWord = data.getBooleanExtra("isInputKeyWord", false);
                            chatInputMenu.addMentions(uid, name, isInputKeyWord);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                    break;
                case REQUEST_QUIT_CHANNELGROUP:
                    MyApplication.getInstance().setCurrentChannelCid("");
                    finish();
                    break;
                case SHARE_SEARCH_RUEST_CODE:
                    if (NetUtils.isNetworkConnected(getApplicationContext())) {
                        String searchResult = data.getStringExtra("searchResult");
                        JSONObject jsonObject = JSONUtils.getJSONObject(searchResult);
                        if (jsonObject.has("people")) {
                            JSONArray peopleArray = JSONUtils.getJSONArray(jsonObject, "people", new JSONArray());
                            if (peopleArray.length() > 0) {
                                JSONObject peopleObj = JSONUtils.getJSONObject(peopleArray, 0, new JSONObject());
                                String pidUid = JSONUtils.getString(peopleObj, "pid", "");
                                createDirectChannel(pidUid, backUiMessage);
                            }
                        }
                        if (jsonObject.has("channelGroup")) {
                            JSONArray channelGroupArray = JSONUtils.getJSONArray(jsonObject, "channelGroup", new JSONArray());
                            if (channelGroupArray.length() > 0) {
                                JSONObject cidObj = JSONUtils.getJSONObject(channelGroupArray, 0, new JSONObject());
                                String cid = JSONUtils.getString(cidObj, "cid", "");
                                transmitMsg(cid, backUiMessage);
                            }
                        }
                    }
                    break;
            }
        } else {
            // 图库选择图片返回
            if (resultCode == ImagePicker.RESULT_CODE_ITEMS)
                if (data != null && requestCode == REQUEST_GELLARY) {
                    ArrayList<ImageItem> imageItemList = (ArrayList<ImageItem>) data
                            .getSerializableExtra(ImagePicker.EXTRA_RESULT_ITEMS);
                    Boolean originalPicture = data.getBooleanExtra(ImageGridActivity.EXTRA_ORIGINAL_PICTURE, false);
                    for (int i = 0; i < imageItemList.size(); i++) {
                        String imgPath = imageItemList.get(i).path;
                        Compressor.ResolutionRatio resolutionRatio = null;
                        Compressor compressor = new Compressor(ConversationActivity.this).setMaxArea(MyAppConfig.UPLOAD_ORIGIN_IMG_DEFAULT_SIZE * MyAppConfig.UPLOAD_ORIGIN_IMG_DEFAULT_SIZE).setQuality(90).setDestinationDirectoryPath(MyAppConfig.LOCAL_IMG_CREATE_PATH);
                        if (originalPicture) {
                            resolutionRatio = compressor.getResolutionRation(new File(imgPath));
                        } else {
                            try {
                                File file = compressor.compressToFile(new File(imgPath));
                                imgPath = file.getAbsolutePath();
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }

                        combinAndSendMessageWithFile(imgPath, Message.MESSAGE_TYPE_MEDIA_IMAGE, resolutionRatio);
                    }
                }
        }
    }


    private void combinAndSendMessageWithFile(String filePath, String messageType, Compressor.ResolutionRatio resolutionRatio) {
        combinAndSendMessageWithFile(filePath, messageType, 0, resolutionRatio);
    }

    private void combinAndSendMessageWithFile(String filePath, String messageType, int duration, Compressor.ResolutionRatio resolutionRatio) {
        combinAndSendMessageWithFile(filePath, messageType, duration, "", resolutionRatio);
    }

    private void combinAndSendMessageWithFile(String filePath, String messageType, int duration, String results, Compressor.ResolutionRatio resolutionRatio) {
        File file = new File(filePath);
        if (!file.exists()) {
            if (messageType != Message.MESSAGE_TYPE_MEDIA_VOICE) {
                ToastUtils.show(MyApplication.getInstance(), R.string.baselib_file_not_exist);
            }
            return;
        }
        Message fakeMessage = null;
        switch (messageType) {
            case Message.MESSAGE_TYPE_FILE_REGULAR_FILE:
                fakeMessage = CommunicationUtils.combinLocalRegularFileMessage(cid, filePath);
                break;
            case Message.MESSAGE_TYPE_MEDIA_IMAGE:
                fakeMessage = CommunicationUtils.combinLocalMediaImageMessage(cid, filePath, resolutionRatio);
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
            if (view != null) {
                view.findViewById(R.id.rl_send_status).setVisibility(View.INVISIBLE);
            }
        }
    }

    /**
     * 发送带有附件类型的消息
     *
     * @param fakeMessage
     */
    private void sendMessageWithFile(final Message fakeMessage) {
        ProgressCallback progressCallback = new ProgressCallback() {
            @Override
            public void onSuccess(VolumeFile volumeFile) {
                switch (fakeMessage.getType()) {
                    case Message.MESSAGE_TYPE_FILE_REGULAR_FILE:
                        MsgContentRegularFile msgContentRegularFile = new MsgContentRegularFile();
                        msgContentRegularFile.setName(volumeFile.getName());
                        msgContentRegularFile.setSize(volumeFile.getSize());
                        msgContentRegularFile.setMedia(volumeFile.getPath());
                        msgContentRegularFile.setTmpId(fakeMessage.getTmpId());
                        fakeMessage.setContent(msgContentRegularFile.toString());
                        WSAPIService.getInstance().sendChatRegularFileMsg(fakeMessage);
                        MessageCacheUtil.saveMessage(ConversationActivity.this, fakeMessage);
                        break;
                    case Message.MESSAGE_TYPE_MEDIA_IMAGE:
                        MsgContentMediaImage msgContentMediaImage = new MsgContentMediaImage();
                        msgContentMediaImage.setRawWidth(fakeMessage.getMsgContentMediaImage().getRawWidth());
                        msgContentMediaImage.setRawHeight(fakeMessage.getMsgContentMediaImage().getRawHeight());
                        msgContentMediaImage.setRawSize(volumeFile.getSize());
                        msgContentMediaImage.setRawMedia(volumeFile.getPath());
                        msgContentMediaImage.setPreviewHeight(fakeMessage.getMsgContentMediaImage().getPreviewHeight());
                        msgContentMediaImage.setPreviewWidth(fakeMessage.getMsgContentMediaImage().getPreviewWidth());
                        msgContentMediaImage.setPreviewSize(fakeMessage.getMsgContentMediaImage().getPreviewSize());
                        msgContentMediaImage.setPreviewMedia(volumeFile.getPath());
                        msgContentMediaImage.setThumbnailHeight(fakeMessage.getMsgContentMediaImage().getThumbnailHeight());
                        msgContentMediaImage.setThumbnailWidth(fakeMessage.getMsgContentMediaImage().getThumbnailWidth());
                        msgContentMediaImage.setThumbnailMedia(volumeFile.getPath());
                        msgContentMediaImage.setName(volumeFile.getName());
                        msgContentMediaImage.setTmpId(fakeMessage.getTmpId());
                        fakeMessage.setContent(msgContentMediaImage.toString());
                        WSAPIService.getInstance().sendChatMediaImageMsg(fakeMessage);
                        MessageCacheUtil.saveMessage(ConversationActivity.this, fakeMessage);
                        break;
                    case Message.MESSAGE_TYPE_MEDIA_VOICE:
                        MsgContentMediaVoice msgContentMediaVoice = new MsgContentMediaVoice();
                        msgContentMediaVoice.setMedia(volumeFile.getPath());
                        msgContentMediaVoice.setDuration(fakeMessage.getMsgContentMediaVoice().getDuration());
                        msgContentMediaVoice.setJsonResults(fakeMessage.getMsgContentMediaVoice().getResult());
                        msgContentMediaVoice.setTmpId(fakeMessage.getTmpId());
                        fakeMessage.setContent(msgContentMediaVoice.toString());
                        WSAPIService.getInstance().sendChatMediaVoiceMsg(fakeMessage);
                        MessageCacheUtil.saveMessage(ConversationActivity.this, fakeMessage);
                        break;
                }
            }

            @Override
            public void onLoading(int progress) {
                //此处不进行loading进度，因为消息的发送进度不等于资源的发送进度
            }

            @Override
            public void onFail() {
                setMessageSendFailStatus(fakeMessage.getId());
            }
        };
        ChatFileUploadManagerUtils.getInstance().uploadResFile(fakeMessage, progressCallback);
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
        String lastDraft = MessageCacheUtil.getDraftByCid(ConversationActivity.this, cid);
        String inputContent = chatInputMenu.getInputContent();
        if (!StringUtils.isBlank(inputContent) && !lastDraft.equals(inputContent)) {
            Message draftMessage = CommunicationUtils.combinLocalTextPlainMessage(inputContent.equals("@") ? (" " + inputContent) : inputContent, cid, null);
            draftMessage.setSendStatus(Message.MESSAGE_SEND_EDIT);
            draftMessage.setRead(Message.MESSAGE_READ);
            draftMessage.setCreationDate(System.currentTimeMillis());
            MessageCacheUtil.saveMessage(ConversationActivity.this, draftMessage);
        } else if (StringUtils.isBlank(inputContent) && !StringUtils.isBlank(lastDraft)) {
            MessageCacheUtil.deleteDraftMessageByCid(ConversationActivity.this, cid);
        }
        notifyCommucationFragmentMessageSendStatus();
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
        setConversationUnhide();
        setMessageSendStatusAndSendTime(message, status);
        //本地添加的消息设置为正在发送状态
        UIMessage UIMessage = new UIMessage(message);
        uiMessageList.add(UIMessage);
        adapter.setMessageList(uiMessageList);
        adapter.notifyItemInserted(uiMessageList.size() - 1);
        msgListView.MoveToPosition(uiMessageList.size() - 1);
    }


    /**
     * 处理未发送成功的消息，存储临时消息
     *
     * @param message
     * @param status
     * @return
     */
    private void setMessageSendStatusAndSendTime(Message message, int status) {
        //发送中，无网,发送消息失败
        message.setSendStatus(status);
        MessageCacheUtil.saveMessage(ConversationActivity.this, message);
        notifyCommucationFragmentMessageSendStatus();
    }

    private void notifyCommucationFragmentMessageSendStatus() {
        // 通知沟通页面更新列表状态
        Intent intent = new Intent("message_notify");
        intent.putExtra("command", "sort_session_list");
        LocalBroadcastManager.getInstance(ConversationActivity.this).sendBroadcast(intent);
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
            uiMessageList.get(index).setSendStatus(Message.MESSAGE_SEND_FAIL);
            setMessageSendStatusAndSendTime(uiMessageList.get(index).getMessage(), Message.MESSAGE_SEND_FAIL);
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
            if (chatInputMenu.isVoiceInputLayoutShow()) {
                chatInputMenu.hideVoiceInputLayout();
            }
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
        chatInputMenu.releaseVoiceInput();
        EventBus.getDefault().unregister(this);
    }


    /**
     * 获取历史消息
     */
    private void getHistoryMessage() {
        //当有网络并且本地没有连续消息时，网络获取
        if ((NetUtils.isNetworkConnected(MyApplication.getInstance(), false) &&
                !(uiMessageList.size() > 0 && MessageCacheUtil.isDataInLocal(ConversationActivity.this, cid, uiMessageList
                        .get(0).getCreationDate(), 20)))) {
            WSAPIService.getInstance().getHistoryMessage(cid, getNewMessageId());
        } else {
            getHistoryMessageFromLocal();
        }
    }

    /**
     * 获取本地发送成功的消息id
     *
     * @return
     */
    private String getNewMessageId() {
        if (uiMessageList.size() > 0) {
            for (UIMessage uiMessage : uiMessageList) {
                if (uiMessage.getMessage().getSendStatus() == Message.MESSAGE_SEND_SUCCESS) {
                    return uiMessage.getMessage().getId();
                }
            }
        }
        return "";
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

    /**
     * 持久化16秒以上消息的消息状态
     *
     * @param messageSendingList
     */
    private void persistenceMessageSendStatus(final List<Message> messageSendingList) {
        new Thread() {
            @Override
            public void run() {
                MessageCacheUtil.saveMessageList(ConversationActivity.this, messageSendingList);
            }
        }.start();
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
                            if (uiMessageList.get(i).getMessage().getId().equals(String.valueOf(eventMessage.getId()))) {
                                index = i;
                                break;
                            }
                        }

                    }
                    //去除以本地时间为发送时间的代码
//                    Long creationDate = 0L;
//                    Message message = MessageCacheUtil.getMessageByMid(BaseApplication.getInstance(), receivedWSMessage.getId());
//                    if (message != null) {
//                        creationDate = message.getCreationDate();
//                    } else {
//                        creationDate = MessageCacheUtil.getMessageByMid(BaseApplication.getInstance(), receivedWSMessage.getTmpId()).getCreationDate();
//                    }
//                    receivedWSMessage.setCreationDate(creationDate);
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
                setMessageSendFailStatus(String.valueOf(eventMessage.getId()));
            }
        }

    }

    //接收到websocket发过来的消息，根据评论获取被评论的消息时触发此方法
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onGetMessageById(EventMessage eventMessage) {
        if (eventMessage.getTag().equals(Constant.EVENTBUS_TAG_GET_MESSAGE_BY_ID)) {
            if (eventMessage.getStatus() == EventMessage.RESULT_OK) {
                String content = eventMessage.getContent();
                JSONObject contentobj = JSONUtils.getJSONObject(content);
                Message message = new Message(contentobj);
                message.setRead(Message.MESSAGE_READ);
                MessageCacheUtil.handleRealMessage(MyApplication.getInstance(), message);
                adapter.notifyDataSetChanged();
            }

        }

    }


    //接收到websocket发过来的消息，推送消息触发此方法
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onReceiveNewMessage(EventMessage eventMessage) {
        if (eventMessage.getTag().equals(Constant.EVENTBUS_TAG_GET_NEW_MESSAGE) && eventMessage.getExtra().equals(cid)) {
            if (eventMessage.getStatus() == EventMessage.RESULT_OK) {
                String content = eventMessage.getContent();
                GetChannelMessagesResult getChannelMessagesResult = new GetChannelMessagesResult(content);
                final List<Message> newMessageList = getChannelMessagesResult.getMessageList();
                new CacheMessageListThread(newMessageList, null, REFRESH_PUSH_MESSAGE).start();
                WSAPIService.getInstance().setChannelMessgeStateRead(cid);
            }
        }
    }

    //接收到websocket发过来的消息，下拉获取消息触发此方法
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
                    new CacheMessageListThread(messageList, targetMessageCreationDate, REFRESH_HISTORY_MESSAGE).start();
                } else {
                    swipeRefreshLayout.setRefreshing(false);
                }
            } else {
                getHistoryMessageFromLocal();
            }
        }
    }


    //接收到从沟通页面传来的离线消息，如断网联网时会触发此方法
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onReceiveWSOfflineMessage(SimpleEventMessage eventMessage) {
        if (eventMessage.getAction().equals(Constant.EVENTBUS_TAG_CURRENT_CHANNEL_OFFLINE_MESSAGE)) {
            final List<Message> offlineMessageList = (List<Message>) eventMessage.getMessageObj();
            WSAPIService.getInstance().setChannelMessgeStateRead(cid);
            new CacheMessageListThread(offlineMessageList, null, REFRESH_OFFLINE_MESSAGE).start();
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

    /**
     * 将频道置为不隐藏
     */
    private void setConversationUnhide() {
        if (conversation.isHide()) {
            conversation.setHide(false);
            ConversationCacheUtils.updateConversationHide(MyApplication.getInstance(), conversation.getId(), false);
            if (NetUtils.isNetworkConnected(MyApplication.getInstance(), false)) {
                ChatAPIService apiService = new ChatAPIService(this);
                apiService.setAPIInterface(new WebService());
                apiService.setConversationHide(conversation.getId(), false);
            }
        }
    }

    /**
     * 创建单聊
     *
     * @param uid
     */
    private void createDirectChannel(String uid, final UIMessage uiMessage) {
        if (WebServiceRouterManager.getInstance().isV1xVersionChat()) {
            new ConversationCreateUtils().createDirectConversation(this, uid,
                    new ConversationCreateUtils.OnCreateDirectConversationListener() {
                        @Override
                        public void createDirectConversationSuccess(Conversation conversation) {
                            transmitMsg(conversation.getId(), uiMessage);
                        }

                        @Override
                        public void createDirectConversationFail() {

                        }
                    });
        }
    }

    /**
     * 转发消息
     */
    private void transmitMsg(String cid, UIMessage uiMessage) {
        String msgType = uiMessage.getMessage().getType();
        switch (msgType) {
            case Message.MESSAGE_TYPE_TEXT_PLAIN:
                transmitTextMsg(cid, uiMessage);
                break;
            case Message.MESSAGE_TYPE_MEDIA_IMAGE:
                transmitImgMsg(cid, uiMessage.getMessage());
                break;
            case Message.MESSAGE_TYPE_FILE_REGULAR_FILE:
                transmitFileMsg(cid, uiMessage.getMessage());
                break;
            case Message.MESSAGE_TYPE_TEXT_MARKDOWN:
                transmitTextMsg(cid, uiMessage);
                break;
            case Message.MESSAGE_TYPE_EXTENDED_LINKS:
                transmitLinkMsg(cid, uiMessage);
                break;
            default:
                break;
        }
    }

    /**
     * 转发来自网盘
     */
    private void transmitMsgFromVolume(String cid, VolumeFile volumeFile, String path) {
        if (NetUtils.isNetworkConnected(getApplicationContext())) {
            ChatAPIService apiService = new ChatAPIService(this);
            apiService.setAPIInterface(new WebService());
            apiService.shareFileToFriendsFromVolume(volumeFile.getVolume(), cid, path, volumeFile);
        }
    }

    /**
     * 转发链接消息
     *
     * @param cid
     */
    private void transmitLinkMsg(String cid, UIMessage uiMessage) {
        if (WebSocketPush.getInstance().isSocketConnect()) {
            Message localMessage = CommunicationUtils.combinLocalExtendedLinksMessageHaveContent(cid, uiMessage.getMessage().getContent());
            WSAPIService.getInstance().sendChatExtendedLinksMsg(localMessage);
            ToastUtils.show(R.string.chat_transmit_message_success);
        } else {
            ToastUtils.show(R.string.chat_transmit_message_fail);
        }
    }

    /**
     * 转发文本消息
     *
     * @param cid
     */
    private void transmitTextMsg(String cid, UIMessage uiMessage) {
        String content = uiMessage2Content(uiMessage);
        if (WebSocketPush.getInstance().isSocketConnect()) {
            Message localMessage = CommunicationUtils.combinLocalTextPlainMessage(content, cid, null);
            WSAPIService.getInstance().sendChatTextPlainMsg(localMessage);
            ToastUtils.show(R.string.chat_transmit_message_success);
        } else {
            ToastUtils.show(R.string.chat_transmit_message_fail);
        }
    }

    /**
     * 转发文件消息
     */
    private void transmitFileMsg(String cid, Message sendMessage) {
        String path = null;
        MsgContentRegularFile msgContentAttachmentFile = sendMessage.getMsgContentAttachmentFile();
        path = msgContentAttachmentFile.getMedia();
        if (NetUtils.isNetworkConnected(getApplicationContext())) {
            ChatAPIService apiService = new ChatAPIService(this);
            apiService.setAPIInterface(new WebService());
            apiService.transmitFile(path, sendMessage.getChannel(), cid, "regular-file", sendMessage);
        }
    }

    /**
     * 转发图片消息
     */
    private void transmitImgMsg(String cid, Message sendMessage) {
        String path = null;
        MsgContentMediaImage msgContentMediaImage = sendMessage.getMsgContentMediaImage();
        path = msgContentMediaImage.getRawMedia();
        if (NetUtils.isNetworkConnected(getApplicationContext())) {
            ChatAPIService apiService = new ChatAPIService(this);
            apiService.setAPIInterface(new WebService());
            apiService.transmitFile(path, sendMessage.getChannel(), cid, "image", sendMessage);
        }
    }

    /**
     * Card 长按事件弹出dialogCard LongClick
     */
    private int[] getCardLongClickOperations(final UIMessage uiMessage) {
        Message message = uiMessage.getMessage();
        String type = message.getType();
        int[] items = new int[0];
        switch (type) {
            case Message.MESSAGE_TYPE_TEXT_PLAIN:
                items = new int[]{R.string.chat_long_click_copy, R.string.chat_long_click_transmit, R.string.chat_long_click_schedule};
                break;
            case Message.MESSAGE_TYPE_TEXT_MARKDOWN:
                items = new int[]{R.string.chat_long_click_copy, R.string.chat_long_click_transmit, R.string.chat_long_click_schedule};
                break;
            case Message.MESSAGE_TYPE_FILE_REGULAR_FILE:
                items = new int[]{R.string.chat_long_click_transmit};
                break;
            case Message.MESSAGE_TYPE_EXTENDED_CONTACT_CARD:
                break;
            case Message.MESSAGE_TYPE_EXTENDED_ACTIONS:
                break;
            case Message.MESSAGE_TYPE_MEDIA_IMAGE:
                items = new int[]{R.string.chat_long_click_transmit, R.string.chat_long_click_reply};
                break;
            case Message.MESSAGE_TYPE_COMMENT_TEXT_PLAIN:
                break;
            case Message.MESSAGE_TYPE_EXTENDED_LINKS:
                items = new int[]{R.string.chat_long_click_transmit};
                break;
            case Message.MESSAGE_TYPE_MEDIA_VOICE:
                break;
            default:
                break;
        }
        return items;
    }

    /**
     * Card 点击事件 及处理
     */
    private void CardClickOperation(final Context context, View view, final UIMessage uiMessage) {
        Message message = uiMessage.getMessage();
        int messageSendStatus = uiMessage.getSendStatus();
        Bundle bundle = new Bundle();
        String type = message.getType();
        switch (type) {
            case Message.MESSAGE_TYPE_ATTACHMENT_CARD:
                String uid = message.getMsgContentAttachmentCard().getUid();
                bundle.putString("uid", uid);
                IntentUtils.startActivity(ConversationActivity.this,
                        UserInfoActivity.class, bundle);
                break;
            case Message.MESSAGE_TYPE_TEXT_PLAIN:
                break;
            case Message.MESSAGE_TYPE_TEXT_MARKDOWN:
                break;
            case Message.MESSAGE_TYPE_FILE_REGULAR_FILE:
                if (uiMessage.getSendStatus() != 1) {
                    return;
                }
                final MsgContentRegularFile msgContentFile = message.getMsgContentAttachmentFile();
                final String fileDownloadPath = MyAppConfig.LOCAL_DOWNLOAD_PATH + msgContentFile.getName();
                if (FileUtils.isFileExist(fileDownloadPath)) {
                    FileUtils.openFile(context, fileDownloadPath);
                } else {
                    Intent intent = new Intent(context, ChatFileDownloadActivtiy.class);
                    intent.putExtra("message", message);
                    context.startActivity(intent);
                }
                break;
            case Message.MESSAGE_TYPE_EXTENDED_CONTACT_CARD:
                break;
            case Message.MESSAGE_TYPE_EXTENDED_ACTIONS:
                break;
            case Message.MESSAGE_TYPE_MEDIA_IMAGE:
                if (uiMessage.getSendStatus() != 1) {
                    return;
                }
                int[] location = new int[2];
                view.getLocationOnScreen(location);
                view.invalidate();
                int width = view.getWidth();
                int height = view.getHeight();
                Intent intent = new Intent(context,
                        ImagePagerActivity.class);
                List<Message> imgTypeMsgList = MessageCacheUtil.getImgTypeMessageList(context, uiMessage.getMessage().getChannel(), false);
                intent.putExtra(ImagePagerActivity.EXTRA_IMAGE_MSG_LIST, (Serializable) imgTypeMsgList);
                intent.putExtra(ImagePagerActivity.EXTRA_CURRENT_IMAGE_MSG, uiMessage.getMessage());
                intent.putExtra(ImagePagerActivity.PHOTO_SELECT_X_TAG, location[0]);
                intent.putExtra(ImagePagerActivity.PHOTO_SELECT_Y_TAG, location[1]);
                intent.putExtra(ImagePagerActivity.PHOTO_SELECT_W_TAG, width);
                intent.putExtra(ImagePagerActivity.PHOTO_SELECT_H_TAG, height);
                context.startActivity(intent);
                break;
            case Message.MESSAGE_TYPE_COMMENT_TEXT_PLAIN:
                //当消息处于发送中状态时无法点击
                if (messageSendStatus == Message.MESSAGE_SEND_SUCCESS) {
                    String mid = message.getMsgContentComment().getMessage();
                    bundle.putString("mid", mid);
                    bundle.putString(EXTRA_CID, message.getChannel());
                    IntentUtils.startActivity(ConversationActivity.this,
                            ChannelMessageDetailActivity.class, bundle);
                }
                break;
            case Message.MESSAGE_TYPE_EXTENDED_LINKS:
                //当消息处于发送中状态时无法点击
                if (messageSendStatus == Message.MESSAGE_SEND_SUCCESS) {
                    String url = message.getMsgContentExtendedLinks().getUrl();
                    UriUtils.openUrl(ConversationActivity.this, url);
                }
                break;
            case Message.MESSAGE_TYPE_MEDIA_VOICE:
                break;
            default:
                NotificationUpgradeUtils upgradeUtils = new NotificationUpgradeUtils(context,
                        null, true);
                upgradeUtils.checkUpdate(true);
                break;
        }
    }

    /**
     * 仿微信长按处理
     */
    private void showLongClickDialog(final int[] operationsId, final UIMessage uiMessage, View view) {
        final String[] operations = new String[operationsId.length];
        for (int i = 0; i < operationsId.length; i++) {
            String operation = getResources().getString(operationsId[i]);
            operations[i] = operation;
        }

        List<String> dataList = new ArrayList<>();
        for (int i = 0; i < operationsId.length; i++) {
            String operation = getResources().getString(operationsId[i]);
            dataList.add(operation);
        }
        if (mPopupWindowList == null) {
            mPopupWindowList = new PopupWindowList(view.getContext());
        }
        mPopupWindowList.setAnchorView(view);
        mPopupWindowList.setItemData(dataList);
        mPopupWindowList.setModal(true);
        mPopupWindowList.show();
        mPopupWindowList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String content;
                content = uiMessage2Content(uiMessage);
                if (StringUtils.isBlank(content)) {
                    content = "";
                }
                switch (operationsId[position]) {
                    case R.string.chat_long_click_copy:
                        copyToClipboard(ConversationActivity.this, content);
                        break;
                    case R.string.chat_long_click_transmit:
                        shareMessageToFriends(ConversationActivity.this, uiMessage);
                        break;
                    case R.string.chat_long_click_schedule:
                        addTextToSchedule(content);
                        break;
                    case R.string.chat_long_click_copy_text:
                        copyToClipboard(ConversationActivity.this, content);
                        break;
                    case R.string.chat_long_click_reply:
                        replyMessage(uiMessage.getMessage());
                        break;
                }
                mPopupWindowList.hide();
            }
        });

    }

    /**
     * 长按事件处理
     */
    private void showLongClickOperationsDialog(final int[] operationsId, final Context context, final UIMessage uiMessage) {
        final String[] operations = new String[operationsId.length];
        for (int i = 0; i < operationsId.length; i++) {
            String operation = context.getResources().getString(operationsId[i]);
            operations[i] = operation;
        }
        ContextThemeWrapper ctw = new ContextThemeWrapper(this, R.style.cus_dialog_style);
        new CustomDialog.ListDialogBuilder(ctw)
                .setItems(operations, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String content;
                        content = uiMessage2Content(uiMessage);
                        if (StringUtils.isBlank(content)) {
                            content = "";
                        }
                        switch (operationsId[which]) {
                            case R.string.chat_long_click_copy:
                                copyToClipboard(context, content);
                                break;
                            case R.string.chat_long_click_transmit:
                                shareMessageToFriends(context, uiMessage);
                                break;
                            case R.string.chat_long_click_schedule:
                                addTextToSchedule(content);
                                break;
                            case R.string.chat_long_click_copy_text:
                                copyToClipboard(context, content);
                                break;
                        }
                        dialog.dismiss();
                    }
                }).show();
    }

    private String uiMessage2Content(UIMessage uiMessage) {
        String content = null;
        switch (uiMessage.getMessage().getType()) {
            case Message.MESSAGE_TYPE_TEXT_MARKDOWN:
                SpannableString spannableString = ChatMsgContentUtils.mentionsAndUrl2Span(
                        uiMessage.getMessage().getMsgContentTextMarkdown().getText(),
                        uiMessage.getMessage().getMsgContentTextMarkdown().getMentionsMap());
                content = spannableString.toString();
                if (!StringUtils.isBlank(content)) {
                    content = MarkDown.fromMarkdown(content);
                }
                break;
            case Message.MESSAGE_TYPE_TEXT_PLAIN:
                String text = uiMessage.getMessage().getMsgContentTextPlain().getText();
                spannableString = ChatMsgContentUtils.mentionsAndUrl2Span(text,
                        uiMessage.getMessage().getMsgContentTextPlain().getMentionsMap());
                content = spannableString.toString();
                break;
        }
        return content;
    }

    /**
     * 文本复制到剪切板
     */
    private void copyToClipboard(Context context, String content) {
        ClipboardManager cmb = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
        cmb.setPrimaryClip(ClipData.newPlainText(null, content));
        ToastUtils.show(context, R.string.copyed_to_paste_board);
    }

    /**
     * （图片）回复功能
     */
    private void replyMessage(Message message) {
        Bundle bundle = new Bundle();
        bundle.putString("mid", message.getId());
        bundle.putString(EXTRA_CID, message.getChannel());
        IntentUtils.startActivity(ConversationActivity.this,
                ChannelMessageDetailActivity.class, bundle);
    }


    /**
     * 文本信息添加到日程
     */
    private void addTextToSchedule(String content) {
        Intent intent = new Intent();
        intent.putExtra(Constant.EXTRA_SCHEDULE_TITLE_EVENT, content);
        intent.setClass(ConversationActivity.this, ScheduleAddActivity.class);
        startActivity(intent);
    }

    /**
     * 给朋友转发
     */
    private void shareMessageToFriends(Context context, UIMessage uiMessage) {
        Intent intent = new Intent();
        JSONObject jsonObject = JSONUtils.getJSONObject(uiMessage.getMessage().getContent());
        String result = "";
        try {
            switch (uiMessage.getMessage().getType()) {
                case Message.MESSAGE_TYPE_MEDIA_IMAGE:
                    result = getString(R.string.baselib_share_image) + " " + jsonObject.getString("name");
                    break;
                case Message.MESSAGE_TYPE_FILE_REGULAR_FILE:
                    result = getString(R.string.baselib_share_file) + " " + jsonObject.getString("name");
                    break;
                case Message.MESSAGE_TYPE_TEXT_PLAIN:
                case Message.MESSAGE_TYPE_TEXT_MARKDOWN:
                    result = uiMessage2Content(uiMessage);
                    break;
                case Message.MESSAGE_TYPE_EXTENDED_LINKS:
                    result = getString(R.string.baselib_share_link) + " " + jsonObject.getString("title");
                    break;
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        intent.putExtra(ContactSearchFragment.EXTRA_TYPE, 0);
        intent.putExtra(ContactSearchFragment.EXTRA_MULTI_SELECT, false);
        intent.putExtra(ContactSearchFragment.EXTRA_SHOW_COMFIRM_DIALOG_WITH_MESSAGE, StringUtils.isBlank(result) ? "" : result);
        intent.putExtra(ContactSearchFragment.EXTRA_SHOW_COMFIRM_DIALOG, true);
        ArrayList<String> uidList = new ArrayList<>();
        uidList.add(MyApplication.getInstance().getUid());
        intent.putStringArrayListExtra(ContactSearchFragment.EXTRA_EXCLUDE_SELECT, uidList);
        intent.putExtra(ContactSearchFragment.EXTRA_TITLE, context.getString(R.string.baselib_share_to));
        intent.setClass(context,
                ContactSearchActivity.class);
        startActivityForResult(intent, SHARE_SEARCH_RUEST_CODE);
    }

    class CacheMessageListThread extends Thread {
        private List<Message> messageList;
        private Long targetTime;
        private int refreshType;

        public CacheMessageListThread(List<Message> messageList, Long targetTime, int refreshType) {
            this.messageList = messageList;
            this.targetTime = targetTime;
            this.refreshType = refreshType;
        }

        @Override
        public void run() {
            if (messageList != null && messageList.size() > 0) {
                MessageCacheUtil.handleRealMessage(MyApplication.getInstance(), messageList, targetTime, cid, false);
            }
            if (handler != null) {
                android.os.Message message = null;
                switch (refreshType) {
                    case REFRESH_HISTORY_MESSAGE:
                        List<Message> historyMessageList = MessageCacheUtil.getHistoryMessageList(MyApplication.getInstance(), cid, uiMessageList.get(0).getMessage().getCreationDate(), 20);
                        List<UIMessage> historyUIMessageList = UIMessage.MessageList2UIMessageList(historyMessageList);
                        message = handler.obtainMessage(refreshType, historyUIMessageList);
                        break;
                    case REFRESH_PUSH_MESSAGE:
                        List<Message> cacheMessageList = MessageCacheUtil.getHistoryMessageList(MyApplication.getInstance(), cid, null, 20);
                        List<UIMessage> newUIMessageList = UIMessage.MessageList2UIMessageList(cacheMessageList);
                        message = handler.obtainMessage(refreshType, newUIMessageList);
                        break;
                    case REFRESH_OFFLINE_MESSAGE:
                        message = handler.obtainMessage(refreshType, messageList);
                        break;
                }
                message.sendToTarget();
            }
        }
    }

    class WebService extends APIInterfaceInstance {
        @Override
        public void returnTransmitPictureSuccess(String cid, String description, Message message) {
            if (WebSocketPush.getInstance().isSocketConnect()) {
                String path = JSONUtils.getString(description, "path", "");
                Message combineMessage = null;
                switch (message.getType()) {
                    case Message.MESSAGE_TYPE_MEDIA_IMAGE:
                        combineMessage = CommunicationUtils.combineTransmitMediaImageMessage(cid, path, message.getMsgContentMediaImage());
                        WSAPIService.getInstance().sendChatMediaImageMsg(combineMessage);
                        break;
                    case Message.MESSAGE_TYPE_FILE_REGULAR_FILE:
                        combineMessage = CommunicationUtils.combineTransmitRegularFileMessage(cid, path, message.getMsgContentAttachmentFile());
                        WSAPIService.getInstance().sendChatRegularFileMsg(combineMessage);
                        break;
                }
                ToastUtils.show(R.string.chat_transmit_message_success);

            } else {
                ToastUtils.show(R.string.chat_transmit_message_fail);
            }
        }

        @Override
        public void returnTransmitPictureError(String error, int errorCode) {
            ToastUtils.show(R.string.chat_transmit_message_fail);
        }

        @Override
        public void returnShareFileToFriendsFromVolumeSuccess(String newPath, VolumeFile volumeFile) {
            MsgContentRegularFile msgContentRegularFile = new MsgContentRegularFile();
            msgContentRegularFile.setCategory(Message.MESSAGE_TYPE_FILE_REGULAR_FILE);
            msgContentRegularFile.setName(volumeFile.getName());
            msgContentRegularFile.setSize(volumeFile.getSize());
            msgContentRegularFile.setMedia(newPath);
            Message combineMessage = CommunicationUtils.combineTransmitRegularFileMessage(cid, newPath, msgContentRegularFile);
            WSAPIService.getInstance().sendChatRegularFileMsg(combineMessage);
            super.returnShareFileToFriendsFromVolumeSuccess(newPath, volumeFile);
        }

        @Override
        public void returnShareFileToFriendsFromVolumeFail(String error, int errorCode) {
            super.returnShareFileToFriendsFromVolumeFail(error, errorCode);
        }
    }
}