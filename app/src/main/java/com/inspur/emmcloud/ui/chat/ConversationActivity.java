package com.inspur.emmcloud.ui.chat;

import android.content.BroadcastReceiver;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.text.SpannableString;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.inspur.emmcloud.MyApplication;
import com.inspur.emmcloud.R;
import com.inspur.emmcloud.adapter.ChannelMessageAdapter;
import com.inspur.emmcloud.api.APIInterfaceInstance;
import com.inspur.emmcloud.api.apiservice.ChatAPIService;
import com.inspur.emmcloud.api.apiservice.WSAPIService;
import com.inspur.emmcloud.baselib.util.IntentUtils;
import com.inspur.emmcloud.baselib.util.JSONUtils;
import com.inspur.emmcloud.baselib.util.StringUtils;
import com.inspur.emmcloud.baselib.util.ToastUtils;
import com.inspur.emmcloud.baselib.widget.CustomLoadingView;
import com.inspur.emmcloud.baselib.widget.LoadingDialog;
import com.inspur.emmcloud.baselib.widget.dialogs.CustomDialog;
import com.inspur.emmcloud.baselib.widget.roundbutton.CustomRoundButton;
import com.inspur.emmcloud.basemodule.application.BaseApplication;
import com.inspur.emmcloud.basemodule.bean.DownloadFileCategory;
import com.inspur.emmcloud.basemodule.bean.EventMessage;
import com.inspur.emmcloud.basemodule.bean.SearchModel;
import com.inspur.emmcloud.basemodule.bean.SimpleEventMessage;
import com.inspur.emmcloud.basemodule.config.Constant;
import com.inspur.emmcloud.basemodule.config.MyAppConfig;
import com.inspur.emmcloud.basemodule.util.AppUtils;
import com.inspur.emmcloud.basemodule.util.FileDownloadManager;
import com.inspur.emmcloud.basemodule.util.FileUtils;
import com.inspur.emmcloud.basemodule.util.ImageDisplayUtils;
import com.inspur.emmcloud.basemodule.util.InputMethodUtils;
import com.inspur.emmcloud.basemodule.util.NetUtils;
import com.inspur.emmcloud.basemodule.util.PVCollectModelCacheUtils;
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
import com.inspur.emmcloud.bean.system.VoiceResult;
import com.inspur.emmcloud.componentservice.contact.ContactUser;
import com.inspur.emmcloud.interf.OnVoiceResultCallback;
import com.inspur.emmcloud.interf.ResultCallback;
import com.inspur.emmcloud.push.WebSocketPush;
import com.inspur.emmcloud.ui.chat.mvp.view.ConversationInfoActivity;
import com.inspur.emmcloud.ui.chat.mvp.view.ConversationSearchActivity;
import com.inspur.emmcloud.ui.chat.pop.PopupWindowList;
import com.inspur.emmcloud.ui.contact.ContactSearchActivity;
import com.inspur.emmcloud.ui.contact.ContactSearchFragment;
import com.inspur.emmcloud.ui.contact.UserInfoActivity;
import com.inspur.emmcloud.ui.schedule.meeting.ScheduleAddActivity;
import com.inspur.emmcloud.util.privates.ChatMsgContentUtils;
import com.inspur.emmcloud.util.privates.CommunicationUtils;
import com.inspur.emmcloud.util.privates.ConversationCreateUtils;
import com.inspur.emmcloud.util.privates.DirectChannelUtils;
import com.inspur.emmcloud.util.privates.MessageSendManager;
import com.inspur.emmcloud.util.privates.NotificationUpgradeUtils;
import com.inspur.emmcloud.util.privates.UriUtils;
import com.inspur.emmcloud.util.privates.Voice2StringMessageUtils;
import com.inspur.emmcloud.util.privates.audioformat.AudioMp3ToPcm;
import com.inspur.emmcloud.util.privates.cache.AppConfigCacheUtils;
import com.inspur.emmcloud.util.privates.cache.ContactUserCacheUtils;
import com.inspur.emmcloud.util.privates.cache.ConversationCacheUtils;
import com.inspur.emmcloud.util.privates.cache.MessageCacheUtil;
import com.inspur.emmcloud.util.privates.richtext.markdown.MarkDown;
import com.inspur.emmcloud.widget.ECMChatInputMenu;
import com.inspur.emmcloud.widget.ECMChatInputMenu.ChatInputMenuListener;
import com.inspur.emmcloud.widget.ECMChatInputMenuCallback;
import com.inspur.emmcloud.widget.RecycleViewForSizeChange;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.BindView;

public class ConversationActivity extends ConversationBaseActivity {

    public static final String CLOUD_PLUS_CHANNEL_ID = "channel_id";
    private static final int REQUEST_QUIT_CHANNELGROUP = 1;
    private static final int REQUEST_GELLARY = 2;
    private static final int REQUEST_CAMERA = 3;
    private static final int RQQUEST_CHOOSE_FILE = 4;
    private static final int REQUEST_MENTIONS = 5;
    private static final int SHARE_SEARCH_RUEST_CODE = 31;
    private static final int VOICE_CALL_MEMBER_CODE = 32;
    private static final int REFRESH_HISTORY_MESSAGE = 6;
    private static final int REFRESH_NEW_MESSAGE = 7;
    private static final int REFRESH_OFFLINE_MESSAGE = 8;
    private static final int COUNT_EVERY_PAGE = 20;

    /**
     * 请求悬浮窗权限
     */
    private static final int REQUEST_WINDOW_PERMISSION = 100;
    private static final int REQUEST_BACKGROUND_WINDOWS = 101;
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
    private PopupWindowList mPopupWindowList; //仿微信长按处理

    private UIMessage backUiMessage = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
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
                if (adapter == null) {
                    return;
                }
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
                    case REFRESH_NEW_MESSAGE:
                        showMessageList();
                        break;
                    case REFRESH_OFFLINE_MESSAGE:
                        List<Message> cacheMessageList = MessageCacheUtil.getHistoryMessageList(MyApplication.getInstance(), cid, null, COUNT_EVERY_PAGE);
                        uiMessageList = UIMessage.MessageList2UIMessageList(cacheMessageList);
                        adapter.setMessageList(uiMessageList);
                        adapter.notifyDataSetChanged();
                        msgListView.scrollToPosition(uiMessageList.size() - 1);
                        WSAPIService.getInstance().setChannelMessgeStateRead(cid);
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
        MyApplication.getInstance().setCurrentChannelCid(cid);
    }

    @Override
    protected void initChannelMessage() {
        initViews();
        showMessageList();
        if (NetUtils.isNetworkConnected(MyApplication.getInstance())) {
            //根据服务端配置是否强制拉取最新消息
            String isForcePullMessage = AppConfigCacheUtils.getAppConfigValue(BaseApplication.getInstance(), Constant.CONCIG_FORCE_PULL_MESSAGE, "true");
            if (getIntent().hasExtra(EXTRA_NEED_GET_NEW_MESSAGE) || isForcePullMessage.equals("true")) {
                getNewMessageOfChannel();
            }

        }
    }

    private void showMessageList() {
        int position = -1;
        List<Message> cacheMessageList;
        if (getIntent().hasExtra(EXTRA_POSITION_MESSAGE)) {
            UIMessage uiMessage = (UIMessage) getIntent().getSerializableExtra(EXTRA_POSITION_MESSAGE);
            cacheMessageList = MessageCacheUtil.getFutureMessageList(BaseApplication.getInstance(), cid, uiMessage.getCreationDate());
            if (cacheMessageList.size() < COUNT_EVERY_PAGE) {
                cacheMessageList = MessageCacheUtil.getHistoryMessageList(BaseApplication.getInstance(), cid, null, COUNT_EVERY_PAGE);
            }
            position = cacheMessageList.indexOf(uiMessage.getMessage());
        } else {
            cacheMessageList = MessageCacheUtil.getHistoryMessageList(BaseApplication.getInstance(), cid, null, COUNT_EVERY_PAGE);

        }
        if (position == -1) {
            position = cacheMessageList.size() - 1;
        }
        uiMessageList = UIMessage.MessageList2UIMessageList(cacheMessageList);
        adapter.setMessageList(uiMessageList);
        adapter.notifyDataSetChanged();
        msgListView.scrollToPosition(position);
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
//        setUnReadMessageCount();
    }

//    private void setUnReadMessageCount() {
//        if (getIntent().hasExtra(EXTRA_UNREAD_MESSAGE)) {
//            final List<Message> unReadMessageList = (List<Message>) getIntent().getSerializableExtra(EXTRA_UNREAD_MESSAGE);
////            unreadRoundBtn.setVisibility(unReadMessageList.size() > UNREAD_NUMBER_BORDER ? View.VISIBLE : View.GONE);
//            unreadRoundBtn.setText(getString(R.string.chat_conversation_unread_count, unReadMessageList.size()));
//            unreadRoundBtn.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View v) {
//                    List<UIMessage> unReadMessageUIList = UIMessage.MessageList2UIMessageList(unReadMessageList);
//                    uiMessageList.clear();
//                    uiMessageList.addAll(unReadMessageUIList);
//                    adapter.setMessageList(uiMessageList);
//                    adapter.notifyDataSetChanged();
//                    msgListView.MoveToPosition(0);
//                    unreadRoundBtn.setVisibility(View.GONE);
//                    msgListView.scrollToPosition(0);
//                }
//            });
//        }
//    }


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
            chatInputMenu.setIsGroup(true, cid);
        } else {
            chatInputMenu.setIsGroup(false, "");
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
                if (conversation.getType().equals(Conversation.TYPE_GROUP)) {
                    Intent intent = new Intent();
                    intent.setClass(ConversationActivity.this, MembersActivity.class);
                    intent.putExtra("title", ConversationActivity.this.getString(R.string.voice_communication_choice_members));
                    intent.putExtra(MembersActivity.MEMBER_PAGE_STATE, MembersActivity.SELECT_STATE);
                    intent.putExtra("cid", cid);
                    startActivityForResult(intent, VOICE_CALL_MEMBER_CODE);
                } else if (conversation.getType().equals(Conversation.TYPE_DIRECT)) {
                    startVoiceOrVideoCall(ECMChatInputMenu.VOICE_CALL, getDirectCversationJoinChannelInfoBeanList());
                }
            }

            //视频通话没有群聊概念
            @Override
            public void onVideoCommucaiton() {
                startVoiceOrVideoCall(ECMChatInputMenu.VIDEO_CALL, getDirectCversationJoinChannelInfoBeanList());
            }

            @Override
            public void onChatDraftsClear() {
                setChatDrafts();
            }

            @Override
            public void onNoSmallWindowPermission() {
                if (Build.VERSION.SDK_INT >= 23) {
                    if (!Settings.canDrawOverlays(ConversationActivity.this)) {
                        showRequestSmallWindowDialog();
                    } else {
                        if (!AppUtils.canBackgroundStart(ConversationActivity.this)) {
                            showRequestBackGroundDialog();
                        }
                    }
                } else if (Build.VERSION.SDK_INT >= 19) {
                    if (!AppUtils.canBackgroundStart(ConversationActivity.this)) {
                        showRequestBackGroundDialog();
                    }
                }

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

    private void showRequestSmallWindowDialog() {
        new CustomDialog.MessageDialogBuilder(this)
                .setMessage(getString(R.string.permission_grant_window_alert, AppUtils.getAppName(this)))
                .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Uri uri = Uri.parse("package:" + getPackageName());
                        Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, uri);
                        startActivityForResult(intent, REQUEST_WINDOW_PERMISSION);
                    }
                }).setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        })
                .show();
    }

    private void showRequestBackGroundDialog() {
        new CustomDialog.MessageDialogBuilder(this)
                .setMessage(getString(R.string.permission_grant_background_start, AppUtils.getAppName(this)))
                .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).setData(Uri.parse("package:" + getPackageName()));
                        startActivityForResult(intent, REQUEST_BACKGROUND_WINDOWS);
                    }
                }).setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        }).show();
    }

    /**
     * 单聊消息
     *
     * @return
     */
    private List<VoiceCommunicationJoinChannelInfoBean> getDirectCversationJoinChannelInfoBeanList() {
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
        return voiceCommunicationUserInfoBeanList;
    }

    /**
     * 根据类型启动电话
     *
     * @param type
     */
    private void startVoiceOrVideoCall(String type, List<VoiceCommunicationJoinChannelInfoBean> voiceCommunicationUserInfoBeanList) {
        Intent intent = new Intent();
        intent.setClass(ConversationActivity.this, ChannelVoiceCommunicationActivity.class);
        intent.putExtra("userList", (Serializable) voiceCommunicationUserInfoBeanList);
        intent.putExtra(CLOUD_PLUS_CHANNEL_ID, cid);
        intent.putExtra(ChannelVoiceCommunicationActivity.VOICE_VIDEO_CALL_TYPE, type);
        intent.putExtra(ChannelVoiceCommunicationActivity.VOICE_COMMUNICATION_STATE, ChannelVoiceCommunicationActivity.INVITER_LAYOUT_STATE);
        startActivity(intent);
    }

    private void inputMenuClick(String type) {
        switch (type) {
            case "mail":
                if (conversation == null) {
                    return;
                }
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
                    onCardItemLongClick(view, uiMessage);
                }
            }

            @Override
            public void onMediaVoiceReRecognize(UIMessage uiMessage, View view, CustomLoadingView downloadLoadingView) {
                List<Integer> operationIdList = getMessageOperationIdList(uiMessage);
                if (operationIdList.size() > 0) {
                    showMessageOperationDlg(operationIdList, uiMessage, view, downloadLoadingView);
                }
            }

            @Override
            public void onAdapterDataSizeChange() {
                if (mediaVoiceReRecognizerPop != null && mediaVoiceReRecognizerPop.isShowing()) {
                    mediaVoiceReRecognizerPop.dismiss();
                }
            }

            @Override
            public boolean onCardItemLongClick(View view, UIMessage uiMessage) {
                if (StringUtils.isBlank(uiMessage.getMessage().getRecallFrom())) {
                    backUiMessage = uiMessage;
                    List<Integer> operationIdList = getMessageOperationIdList(uiMessage);
                    if (operationIdList.size() > 0) {
                        showMessageOperationDlg(operationIdList, uiMessage, view, null);
                    }
                }

                return true;
            }

            @Override
            public void onCardItemClick(View view, UIMessage uiMessage) {
                if (StringUtils.isBlank(uiMessage.getMessage().getRecallFrom()) && uiMessage.getSendStatus() == 1) {
                    CardClickOperation(ConversationActivity.this, view, uiMessage);
                }
            }

            @Override
            public void onCardItemLayoutClick(View view, UIMessage uiMessage) {
                if (StringUtils.isBlank(uiMessage.getMessage().getRecallFrom()) && uiMessage.getSendStatus() == 1) {
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
        msgListView.setAdapter(adapter);
    }


    /**
     * 消息重新发送
     *
     * @param uiMessage
     */
    private void resendMessage(UIMessage uiMessage) {
        // TODO Auto-generated method stub
//        Message message = uiMessage.getMessage();
        String messageType = uiMessage.getMessage().getType();
        if (!FileUtils.isFileExist(uiMessage.getMessage().getLocalPath()) && (messageType.equals(Message.MESSAGE_TYPE_FILE_REGULAR_FILE)
                || messageType.equals(Message.MESSAGE_TYPE_MEDIA_IMAGE) || messageType.equals(Message.MESSAGE_TYPE_MEDIA_VOICE))) {
            ToastUtils.show(ConversationActivity.this, getString(R.string.resend_file_failed));
            return;
        }
        uiMessage.setSendStatus(Message.MESSAGE_SEND_ING);
        long creationDate = System.currentTimeMillis();
        uiMessage.getMessage().setCreationDate(creationDate);
        uiMessage.setCreationDate(creationDate);
        setMessageSendStatusAndSendTime(uiMessage.getMessage(), Message.MESSAGE_SEND_ING);
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
        MessageSendManager.getInstance().sendMessage(uiMessage.getMessage());
    }


    private void recognizerMediaVoiceMessage(final UIMessage uiMessage, final CustomLoadingView downloadLoadingView) {
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

                    int position = uiMessageList.indexOf(uiMessage);
                    if (position != -1) {
                        adapter.notifyItemChanged(position);
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
                        PVCollectModelCacheUtils.saveCollectModel("file", "share");
                        List<String> pathList = getIntent().getStringArrayListExtra("share_paths");
                        for (String url : pathList) {
                            String urlLowerCase = url.toLowerCase();
                            boolean isImage = urlLowerCase.endsWith("png") || urlLowerCase.endsWith("jpg") || urlLowerCase.endsWith("jpeg") || urlLowerCase.endsWith("dng");
                            combinAndSendMessageWithFile(isImage ? getCompressorUrl(url) : url, isImage ? Message.MESSAGE_TYPE_MEDIA_IMAGE : Message.MESSAGE_TYPE_FILE_REGULAR_FILE, null);
                        }
                    }
                    break;
                case "link":
                    PVCollectModelCacheUtils.saveCollectModel("link", "share");
                    String content = getIntent().getExtras().getString(Constant.SHARE_LINK);
                    if (!StringUtils.isBlank(content)) {
                        Message message = CommunicationUtils.combinLocalExtendedLinksMessage(cid, JSONUtils.getString(content, "poster", ""), JSONUtils.getString(content, "title", "")
                                , JSONUtils.getString(content, "digest", ""), JSONUtils.getString(content, "url", ""));
                        addLocalMessage(message, 0);
                        MessageSendManager.getInstance().sendMessage(message);
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
                    if (data.hasExtra("isNativeFile")) {
                        if (data.getBooleanExtra("isNativeFile", false)) {
                            List<String> filePathList = data.getStringArrayListExtra("pathList");
                            for (String filepath : filePathList) {
                                combinAndSendMessageWithFile(filepath, Message.MESSAGE_TYPE_FILE_REGULAR_FILE, null);
                            }
                        } else {
                            List<VolumeFile> volumeFileList = (List<VolumeFile>) data.getSerializableExtra("volumeFileList");
                            String currentPath = data.getStringExtra("currentPath");
                            for (int i = 0; i < volumeFileList.size(); i++) {
                                transmitMsgFromVolume(cid, volumeFileList.get(0), currentPath);
                            }
                        }
                    }
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
                        handleShareResult(data);
                    }
                    break;
                case VOICE_CALL_MEMBER_CODE:
                    List<VoiceCommunicationJoinChannelInfoBean> voiceCommunicationUserInfoBeanList = new ArrayList<>();
                    String voiceResult = data.getStringExtra("searchResult");
                    JSONArray voiceJsonArray = JSONUtils.getJSONArray(voiceResult, new JSONArray());
                    for (int i = 0; i < voiceJsonArray.length(); i++) {
                        try {
                            String uid = JSONUtils.getString(voiceJsonArray.getString(i), "uid", "");
                            String name = JSONUtils.getString(voiceJsonArray.getString(i), "name", "");
                            VoiceCommunicationJoinChannelInfoBean voiceCommunicationJoinChannelInfoBean = new VoiceCommunicationJoinChannelInfoBean();
                            voiceCommunicationJoinChannelInfoBean.setUserId(uid);
                            voiceCommunicationJoinChannelInfoBean.setUserName(name);
                            voiceCommunicationUserInfoBeanList.add(voiceCommunicationJoinChannelInfoBean);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                    startVoiceOrVideoCall(ECMChatInputMenu.VOICE_CALL, voiceCommunicationUserInfoBeanList);
                    break;
                //去掉主动弹出窗口
//                case REQUEST_WINDOW_PERMISSION:
//                    if (Build.VERSION.SDK_INT >= 23 ) {
//                        if(!Settings.canDrawOverlays(this)){
//                            showRequestBackGroundDialog();
//                        }else{
//                            if(!AppUtils.canBackgroundStart(this)){
//                                showRequestBackGroundDialog();
//                            }
//                        }
//                    }else if(Build.VERSION.SDK_INT >= 19){
//                        if(!AppUtils.canBackgroundStart(this)){
//                            showRequestBackGroundDialog();
//                        }
//                    }
//                    break;
//                case REQUEST_BACKGROUND_WINDOWS:
//                    if(Build.VERSION.SDK_INT >= 19 && !AppUtils.canBackgroundStart(this)){
//                        showRequestBackGroundDialog();
//                    }
//                    break;
            }
        } else {
            // 图库选择图片返回
            if (resultCode == ImagePicker.RESULT_CODE_ITEMS) {
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
            //不主动弹出
//            else if(requestCode == REQUEST_WINDOW_PERMISSION){
//                if (Build.VERSION.SDK_INT >= 23 ) {
//                    if(!Settings.canDrawOverlays(this)){
//                        showRequestBackGroundDialog();
//                    }else{
//                        if(!AppUtils.canBackgroundStart(this)){
//                            showRequestBackGroundDialog();
//                        }
//                    }
//                }else if(Build.VERSION.SDK_INT >= 19){
//                    if(!AppUtils.canBackgroundStart(this)){
//                        showRequestBackGroundDialog();
//                    }
//                }
//            }else if(requestCode == REQUEST_BACKGROUND_WINDOWS){
//                if(Build.VERSION.SDK_INT >= 19 && !AppUtils.canBackgroundStart(this)){
//                    showRequestBackGroundDialog();
//                }
//            }
        }
    }

    private void handleShareResult(Intent data) {
        SearchModel searchModel = (SearchModel) data.getSerializableExtra("searchModel");
        if (searchModel != null) {
            String userOrChannelId = searchModel.getId();
            boolean isGroup = searchModel.getType().equals(SearchModel.TYPE_GROUP);
            share2Conversation(userOrChannelId, isGroup);
        }
    }

    private void share2Conversation(String userOrChannelId, boolean isGroup) {
        if (StringUtils.isBlank(userOrChannelId)) {
            ToastUtils.show(MyApplication.getInstance(), getString(R.string.baselib_share_fail));
        } else {
            if (isGroup) {
                transmitMsg(userOrChannelId, backUiMessage);
            } else {
                createDirectChannel(userOrChannelId, backUiMessage);
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
            ToastUtils.show(MyApplication.getInstance(), R.string.baselib_file_not_exist);
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
            MessageSendManager.getInstance().sendMessage(fakeMessage);
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
            case Conversation.TYPE_DIRECT:
                bundle.putString(ConversationInfoActivity.EXTRA_CID, conversation.getId());
                Intent intent = new Intent(this, ConversationInfoActivity.class);
                intent.putExtras(bundle);
                startActivityForResult(intent, REQUEST_QUIT_CHANNELGROUP);
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
        MessageSendManager.getInstance().sendMessage(localMessage);
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


    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onReceiveSimpleMessage(SimpleEventMessage simpleEventMessage) {
        switch (simpleEventMessage.getAction()) {
            //接收当前频道的消息
            case Constant.EVENTBUS_TAG_RECERIVER_SINGLE_WS_MESSAGE_CONVERSATION:
                onReceiveWSMessage(simpleEventMessage);
                break;
            //接收到从沟通页面传来的离线消息，如断网联网时会触发此方法
            case Constant.EVENTBUS_TAG_CURRENT_CHANNEL_OFFLINE_MESSAGE:
                final List<Message> offlineMessageList = (List<Message>) simpleEventMessage.getMessageObj();
                WSAPIService.getInstance().setChannelMessgeStateRead(cid);
                new CacheMessageListThread(offlineMessageList, null, REFRESH_OFFLINE_MESSAGE).start();
                break;
            case Constant.EVENTBUS_TAG_CURRENT_CHANNEL_RECALL_MESSAGE:
                Message recallMessage = (Message) simpleEventMessage.getMessageObj();
                int index = getMessageIndex(recallMessage);
                if (index != -1) {
                    UIMessage recallUIMessage = uiMessageList.get(index);
                    if (StringUtils.isBlank(recallUIMessage.getMessage().getRecallFrom())) {
                        UIMessage uiMessage = new UIMessage(recallMessage);
                        uiMessageList.remove(index);
                        uiMessageList.add(index, uiMessage);
                        adapter.setMessageList(uiMessageList);
                        adapter.notifyItemChanged(index);
                    }
                }
                break;

            case Constant.EVENTBUS_TAG_CURRENT_CHANNEL_COMMAND_BATCH_MESSAGE:
                List<Message> messageList = (List<Message>) simpleEventMessage.getMessageObj();
                for (Message message : messageList) {
                    int position = getMessageIndex(message);
                    if (position != -1) {
                        UIMessage uiMessage = new UIMessage(message);
                        uiMessageList.remove(position);
                        uiMessageList.add(position, uiMessage);
                    }
                }
                adapter.setMessageList(uiMessageList);
                adapter.notifyDataSetChanged();
                break;

            case Constant.EVENTBUS_TAG_SEND_ACTION_CONTENT_MESSAGE:
                String actionContent = (String) simpleEventMessage.getMessageObj();
                sendMessageWithText(actionContent, true, null);
                break;
            case Constant.EVENTBUS_TAG_UPDATE_CHANNEL_NAME:
                String name = ((Conversation) simpleEventMessage.getMessageObj()).getName();
                conversation.setName(name);
                headerText.setText(name);
                break;
            case Constant.EVENTBUS_TAG_COMMENT_MESSAGE:
                Message message = (Message) simpleEventMessage.getMessageObj();
                uiMessageList.add(new UIMessage(message));
                adapter.setMessageList(uiMessageList);
                adapter.notifyItemInserted(uiMessageList.size() - 1);
                msgListView.MoveToPosition(uiMessageList.size() - 1);
                break;

        }
    }


    public void onReceiveWSMessage(SimpleEventMessage simpleEventMessage) {
        if (simpleEventMessage.getAction().equals(Constant.EVENTBUS_TAG_RECERIVER_SINGLE_WS_MESSAGE_CONVERSATION)) {
            if (adapter == null) {
                return;
            }
            EventMessage eventMessage = (EventMessage) simpleEventMessage.getMessageObj();
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
                    if (index == -1) {
                        uiMessageList.add(new UIMessage(receivedWSMessage));
                        adapter.setMessageList(uiMessageList);
                        adapter.notifyItemInserted(uiMessageList.size() - 1);
                        if (!msgListView.canScrollVertically(1)) {
                            msgListView.MoveToPosition(uiMessageList.size() - 1);
                        }
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
                        .get(0).getCreationDate(), COUNT_EVERY_PAGE)))) {
            // 获取本地发送成功的消息id
            String newMessageId = "";
            for (UIMessage uiMessage : uiMessageList) {
                if (uiMessage.getMessage().getSendStatus() == Message.MESSAGE_SEND_SUCCESS) {
                    newMessageId = uiMessage.getMessage().getId();
                    break;
                }
            }
            WSAPIService.getInstance().getHistoryMessage(cid, newMessageId);
        } else {
            getHistoryMessageFromLocal();
        }
    }


    /**
     * 从本地获取历史消息
     */
    private void getHistoryMessageFromLocal() {
        if (uiMessageList.size() > 0) {
            List<Message> messageList = MessageCacheUtil.getHistoryMessageList(
                    MyApplication.getInstance(), cid, uiMessageList.get(0).getMessage(), COUNT_EVERY_PAGE);
            uiMessageList.addAll(0, UIMessage.MessageList2UIMessageList(messageList));
            adapter.setMessageList(uiMessageList);
            adapter.notifyItemRangeInserted(0, messageList.size());
            msgListView.scrollToPosition(messageList.size() - 1);
        }
        swipeRefreshLayout.setRefreshing(false);
    }


    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onResponseRecallMessage(EventMessage eventMessage) {
        if (eventMessage.getTag().equals(Constant.EVENTBUS_TAG_RECALL_MESSAGE)) {
            loadingDlg.dismiss();
            if (eventMessage.getStatus() == EventMessage.RESULT_OK) {
                Message recallMessage = (Message) eventMessage.getExtra();
                recallMessage.setRecallFromSelf();
                recallMessage.setRead(Message.MESSAGE_READ);
                int index = getMessageIndex(recallMessage);
                if (index != -1) {
                    UIMessage uiMessage = new UIMessage(recallMessage);
                    uiMessageList.remove(index);
                    uiMessageList.add(index, uiMessage);
                    adapter.setMessageList(uiMessageList);
                    adapter.notifyItemChanged(index);
                }
                MessageCacheUtil.saveMessage(BaseApplication.getInstance(), recallMessage);
                if (index == uiMessageList.size() - 1) {
                    notifyCommucationFragmentMessageSendStatus();
                }
            } else {
                showInfoDlg("消息撤回失败");
            }
        }
    }


    //接收到websocket发过来的消息，根据评论获取被评论的消息时触发此方法
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onResponseGetMessageById(EventMessage eventMessage) {
        if (eventMessage.getTag().equals(Constant.EVENTBUS_TAG_GET_MESSAGE_BY_ID)) {
            if (eventMessage.getStatus() == EventMessage.RESULT_OK) {
                String content = eventMessage.getContent();
                JSONObject contentObj = JSONUtils.getJSONObject(content);
                Message message = new Message(contentObj);
                message.setRead(Message.MESSAGE_READ);
                MessageCacheUtil.handleRealMessage(MyApplication.getInstance(), message);
                adapter.notifyDataSetChanged();
            }

        }

    }


    //接收到websocket发过来的消息，推送消息触发此方法
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onResponseNewMessage(EventMessage eventMessage) {
        if (eventMessage.getTag().equals(Constant.EVENTBUS_TAG_GET_NEW_MESSAGE) && ((HashMap) eventMessage.getExtra()).get("cid").equals(cid)) {
            if (eventMessage.getStatus() == EventMessage.RESULT_OK) {
                String content = eventMessage.getContent();
                GetChannelMessagesResult getChannelMessagesResult = new GetChannelMessagesResult(content);
                final List<Message> newMessageList = getChannelMessagesResult.getMessageList();
                new CacheMessageListThread(newMessageList, null, REFRESH_NEW_MESSAGE).start();
                WSAPIService.getInstance().setChannelMessgeStateRead(cid);
            }
            if ((boolean) ((HashMap) eventMessage.getExtra()).get("isNeedRefreshConversationList")) {
                EventBus.getDefault().post(new SimpleEventMessage(Constant.EVENTBUS_TAG_REFRESH_CONVERSATION));
            }
        }
    }

    //接收到websocket发过来的消息，下拉获取消息触发此方法
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onResponseHistoryMessage(EventMessage eventMessage) {
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


    /**
     * 获取此频道的最新消息
     */
    private void getNewMessageOfChannel() {
        if (NetUtils.isNetworkConnected(this, false)) {
            //获取完消息之后，需要刷新沟通页列表，防止新的频道不显示
            boolean isReFreshConversationList = getIntent().getBooleanExtra(EXTRA_COME_FROM_SCANCODE, false);
            WSAPIService.getInstance().getChannelNewMessage(cid, isReFreshConversationList);
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
     * 转发到其他频道时调用此方法，将发送中状态的消息保存到本地数据库并发送此消息
     * 转发到其他频道时不自动跳转到此频道
     *
     * @param fakeMessage
     */
    private void sendTransmitMsg(Message fakeMessage) {
        if (fakeMessage != null) {
            fakeMessage.setSendStatus(Message.MESSAGE_SEND_ING);
            MessageCacheUtil.saveMessage(ConversationActivity.this, fakeMessage);
            MessageSendManager.getInstance().sendMessage(fakeMessage);
            ToastUtils.show(R.string.chat_message_send_success);
        } else {
            ToastUtils.show(R.string.chat_message_send_fail);
        }

    }

    /**
     * 转发来自网盘
     */
    private void transmitMsgFromVolume(String cid, VolumeFile volumeFile, String path) {
        if (NetUtils.isNetworkConnected(getApplicationContext())) {
            ChatAPIService apiService = new ChatAPIService(this);
            apiService.setAPIInterface(new WebService());
            apiService.shareFileToFriendsFromVolume(volumeFile.getVolume(), cid, path + "" + volumeFile.getName(), volumeFile);
        }
    }

    /**
     * 转发链接消息
     *
     * @param cid
     */
    private void transmitLinkMsg(String cid, UIMessage uiMessage) {
        Message fakeMessage = CommunicationUtils.combinLocalExtendedLinksMessageHaveContent(cid, uiMessage.getMessage().getContent());
        sendTransmitMsg(fakeMessage);
    }

    /**
     * 转发文本消息
     *
     * @param cid
     */
    private void transmitTextMsg(String cid, UIMessage uiMessage) {
        String content = uiMessage2Content(uiMessage);
        Message fakeMessage = CommunicationUtils.combinLocalTextPlainMessage(content, cid, null);
        sendTransmitMsg(fakeMessage);
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
    private List<Integer> getMessageOperationIdList(final UIMessage uiMessage) {
        Message message = uiMessage.getMessage();
        String type = message.getType();
        ArrayList<Integer> operationIdList = new ArrayList<>();
        if (uiMessage.getSendStatus() == Message.MESSAGE_SEND_FAIL) {
            operationIdList.add(R.string.chat_resend_message);
            operationIdList.add(R.string.delete);
        } else if (uiMessage.getSendStatus() == Message.MESSAGE_SEND_ING) {
            // operationIdList.add(R.string.delete);
        } else if (uiMessage.getSendStatus() == Message.MESSAGE_SEND_SUCCESS) {
            switch (type) {
                case Message.MESSAGE_TYPE_TEXT_PLAIN:
                    operationIdList.add(R.string.chat_long_click_copy);
                    operationIdList.add(R.string.chat_long_click_transmit);
                    operationIdList.add(R.string.chat_long_click_schedule);
                    break;
                case Message.MESSAGE_TYPE_TEXT_MARKDOWN:
                    operationIdList.add(R.string.chat_long_click_transmit);
                    operationIdList.add(R.string.chat_long_click_schedule);
                    break;
                case Message.MESSAGE_TYPE_FILE_REGULAR_FILE:
                    operationIdList.add(R.string.chat_long_click_transmit);
                    break;
                case Message.MESSAGE_TYPE_EXTENDED_CONTACT_CARD:
                    break;
                case Message.MESSAGE_TYPE_EXTENDED_ACTIONS:
                    break;
                case Message.MESSAGE_TYPE_MEDIA_IMAGE:
                    operationIdList.add(R.string.chat_long_click_transmit);
                    operationIdList.add(R.string.chat_long_click_reply);
                    break;
                case Message.MESSAGE_TYPE_COMMENT_TEXT_PLAIN:
                    break;
                case Message.MESSAGE_TYPE_EXTENDED_LINKS:
                    operationIdList.add(R.string.chat_long_click_transmit);
                    break;
                case Message.MESSAGE_TYPE_MEDIA_VOICE:
                    break;
                default:
                    break;
            }
            if (uiMessage.getMessage().getFromUser().equals(BaseApplication.getInstance().getUid()) && System.currentTimeMillis() - uiMessage.getCreationDate() < 120000) {
                operationIdList.add(R.string.chat_long_click_recall);
            }
            if (uiMessage.getMessage().getType().equals(Message.MESSAGE_TYPE_MEDIA_VOICE)) {
                operationIdList.add(R.string.voice_to_word);
            }
        }
        return operationIdList;
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
                final String fileDownloadPath = FileDownloadManager.getInstance().getDownloadFilePath(DownloadFileCategory.CATEGORY_MESSAGE, message.getId(), msgContentFile.getName());
                if (!StringUtils.isBlank(fileDownloadPath)) {
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
    private void showMessageOperationDlg(final List<Integer> operationIdList, final UIMessage uiMessage, View view, final CustomLoadingView downloadLoadingView) {
        if (mPopupWindowList == null) {
            mPopupWindowList = new PopupWindowList(view.getContext());
        }
        List<String> operationList = new ArrayList<>();
        for (Integer operationId : operationIdList) {
            operationList.add(getString(operationId));
        }
        mPopupWindowList.setAnchorView(view);
        mPopupWindowList.setItemData(operationList);
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
                switch (operationIdList.get(position)) {
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
                    case R.string.chat_long_click_recall:
                        requestToRecallMessage(uiMessage.getMessage());
                        break;
                    case R.string.voice_to_word:
                        recognizerMediaVoiceMessage(uiMessage, downloadLoadingView);
                        break;
                    case R.string.chat_resend_message:
                        resendMessage(uiMessage);
                        break;
                    case R.string.delete:
                        removeSendFailMessage(uiMessage);
                        break;
                }
                mPopupWindowList.hide();
            }
        });
    }

    private void showInfoDlg(String info) {
        new CustomDialog.MessageDialogBuilder(ConversationActivity.this)
                .setMessage(info)
                .setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .show();
    }

    private int getMessageIndex(Message message) {
        return uiMessageList.indexOf(new UIMessage(message.getId()));
    }


    /**
     * 删除发送失败的消息
     *
     * @param uiMessage
     */
    private void removeSendFailMessage(UIMessage uiMessage) {
        int index = uiMessageList.indexOf(uiMessage);
        if (index != -1) {
            uiMessageList.remove(index);
            adapter.setMessageList(uiMessageList);
            adapter.notifyItemRemoved(index);
        }
        MessageCacheUtil.deleteMessageById(uiMessage.getId());
        notifyCommucationFragmentMessageSendStatus();
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
//        startActivityForResult(intent, SHARE_SEARCH_RUEST_CODE);

        Intent shareIntent = new Intent(this, ConversationSearchActivity.class);
        shareIntent.putExtra(Constant.SHARE_CONTENT, result);

        startActivityForResult(shareIntent, SHARE_SEARCH_RUEST_CODE);
    }

    private void requestToRecallMessage(Message message) {
        if (System.currentTimeMillis() - message.getCreationDate() >= 120000) {
            showInfoDlg(getString(R.string.recall_fail_for_timeout));
        } else if (WebSocketPush.getInstance().isSocketConnect()) {
            loadingDlg.show();
            loadingDlg.getWindow().setFlags(
                    WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM,
                    WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM);
            WSAPIService.getInstance().recallMessage(message);
        } else {
            ToastUtils.show(R.string.network_exception);
        }
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
                        List<Message> historyMessageList = MessageCacheUtil.getHistoryMessageList(MyApplication.getInstance(), cid, uiMessageList.get(0).getMessage(), COUNT_EVERY_PAGE);
                        List<UIMessage> historyUIMessageList = UIMessage.MessageList2UIMessageList(historyMessageList);
                        message = handler.obtainMessage(refreshType, historyUIMessageList);
                        break;
                    case REFRESH_OFFLINE_MESSAGE:
                    case REFRESH_NEW_MESSAGE:
//                        List<Message> cacheMessageList = MessageCacheUtil.getHistoryMessageList(MyApplication.getInstance(), cid, null, 20);
//                        List<UIMessage> newUIMessageList = UIMessage.MessageList2UIMessageList(cacheMessageList);
                        message = handler.obtainMessage(refreshType);
//                        break;
//                    case REFRESH_OFFLINE_MESSAGE:
//                        message = handler.obtainMessage(refreshType, messageList);
//                        break;
                }
                message.sendToTarget();
            }
        }
    }

    class WebService extends APIInterfaceInstance {
        @Override
        public void returnTransmitPictureSuccess(String cid, String description, Message message) {
            String path = JSONUtils.getString(description, "path", "");
            Message fakeMessage = null;
            switch (message.getType()) {
                case Message.MESSAGE_TYPE_MEDIA_IMAGE:
                    fakeMessage = CommunicationUtils.combineTransmitMediaImageMessage(cid, path, message.getMsgContentMediaImage());
                    break;
                case Message.MESSAGE_TYPE_FILE_REGULAR_FILE:
                    fakeMessage = CommunicationUtils.combineTransmitRegularFileMessage(cid, path, message.getMsgContentAttachmentFile());
                    break;
            }
            sendTransmitMsg(fakeMessage);
        }

        @Override
        public void returnTransmitPictureError(String error, int errorCode) {
            ToastUtils.show(R.string.chat_message_send_fail);
        }

        @Override
        public void returnShareFileToFriendsFromVolumeSuccess(String newPath, VolumeFile volumeFile) {
            MsgContentRegularFile msgContentRegularFile = new MsgContentRegularFile();
            msgContentRegularFile.setCategory(Message.MESSAGE_TYPE_FILE_REGULAR_FILE);
            msgContentRegularFile.setName(volumeFile.getName());
            msgContentRegularFile.setSize(volumeFile.getSize());
            msgContentRegularFile.setMedia(newPath);
            Message fakeMessage = CommunicationUtils.combineTransmitRegularFileMessage(cid, "", msgContentRegularFile);
            addLocalMessage(fakeMessage, Message.MESSAGE_SEND_ING);
            MessageSendManager.getInstance().sendMessage(fakeMessage);
        }

        @Override
        public void returnShareFileToFriendsFromVolumeFail(String error, int errorCode) {
            ToastUtils.show(R.string.chat_message_send_fail);
        }
    }
}