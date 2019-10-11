package com.inspur.emmcloud.ui.chat;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.inspur.emmcloud.MyApplication;
import com.inspur.emmcloud.R;
import com.inspur.emmcloud.adapter.ConversationAdapter;
import com.inspur.emmcloud.api.APIInterfaceInstance;
import com.inspur.emmcloud.api.APIUri;
import com.inspur.emmcloud.api.apiservice.ChatAPIService;
import com.inspur.emmcloud.api.apiservice.WSAPIService;
import com.inspur.emmcloud.baselib.util.DensityUtil;
import com.inspur.emmcloud.baselib.util.IntentUtils;
import com.inspur.emmcloud.baselib.util.JSONUtils;
import com.inspur.emmcloud.baselib.util.StringUtils;
import com.inspur.emmcloud.baselib.util.ToastUtils;
import com.inspur.emmcloud.baselib.widget.LoadingDialog;
import com.inspur.emmcloud.baselib.widget.dialogs.CustomDialog;
import com.inspur.emmcloud.basemodule.bean.SimpleEventMessage;
import com.inspur.emmcloud.basemodule.config.Constant;
import com.inspur.emmcloud.basemodule.config.MyAppConfig;
import com.inspur.emmcloud.basemodule.ui.BaseFragment;
import com.inspur.emmcloud.basemodule.util.AppUtils;
import com.inspur.emmcloud.basemodule.util.DownLoaderUtils;
import com.inspur.emmcloud.basemodule.util.NetUtils;
import com.inspur.emmcloud.basemodule.util.PVCollectModelCacheUtils;
import com.inspur.emmcloud.basemodule.util.PreferencesByUserAndTanentUtils;
import com.inspur.emmcloud.basemodule.util.WebServiceMiddleUtils;
import com.inspur.emmcloud.basemodule.util.systool.emmpermission.Permissions;
import com.inspur.emmcloud.basemodule.util.systool.permission.PermissionRequestCallback;
import com.inspur.emmcloud.basemodule.util.systool.permission.PermissionRequestManagerUtils;
import com.inspur.emmcloud.bean.chat.ChannelMessageReadStateResult;
import com.inspur.emmcloud.bean.chat.ChannelMessageSet;
import com.inspur.emmcloud.bean.chat.Conversation;
import com.inspur.emmcloud.bean.chat.GetConversationListResult;
import com.inspur.emmcloud.bean.chat.GetOfflineMessageListResult;
import com.inspur.emmcloud.bean.chat.GetRecentMessageListResult;
import com.inspur.emmcloud.bean.chat.GetVoiceAndVideoResult;
import com.inspur.emmcloud.bean.chat.MatheSet;
import com.inspur.emmcloud.bean.chat.Message;
import com.inspur.emmcloud.bean.chat.UIConversation;
import com.inspur.emmcloud.bean.chat.VoiceCommunicationJoinChannelInfoBean;
import com.inspur.emmcloud.bean.system.EmmAction;
import com.inspur.emmcloud.bean.system.EventMessage;
import com.inspur.emmcloud.bean.system.GetAppMainTabResult;
import com.inspur.emmcloud.bean.system.MainTabProperty;
import com.inspur.emmcloud.bean.system.MainTabResult;
import com.inspur.emmcloud.push.WebSocketPush;
import com.inspur.emmcloud.ui.contact.ContactSearchActivity;
import com.inspur.emmcloud.ui.contact.ContactSearchFragment;
import com.inspur.emmcloud.ui.mine.setting.NetWorkStateDetailActivity;
import com.inspur.emmcloud.util.privates.AppTabUtils;
import com.inspur.emmcloud.util.privates.CheckingNetStateUtils;
import com.inspur.emmcloud.util.privates.ConversationCreateUtils;
import com.inspur.emmcloud.util.privates.ConversationGroupIconUtils;
import com.inspur.emmcloud.util.privates.CustomProtocol;
import com.inspur.emmcloud.util.privates.ScanQrCodeUtils;
import com.inspur.emmcloud.util.privates.SuspensionWindowManagerUtils;
import com.inspur.emmcloud.util.privates.UriUtils;
import com.inspur.emmcloud.util.privates.VoiceCommunicationUtils;
import com.inspur.emmcloud.util.privates.cache.ConversationCacheUtils;
import com.inspur.emmcloud.util.privates.cache.MessageCacheUtil;
import com.inspur.emmcloud.util.privates.cache.MessageMatheSetCacheUtils;
import com.inspur.emmcloud.widget.ECMChatInputMenu;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import io.socket.client.Socket;

import static android.app.Activity.RESULT_OK;

/**
 * 沟通页面
 */
public class CommunicationFragment extends BaseFragment {

    private static final int CREAT_CHANNEL_GROUP = 1;
    private static final int RERESH_GROUP_ICON = 2;
    private static final int SORT_CONVERSATION_COMPLETE = 3;
    private static final int SORT_CONVERSATION_LIST = 4;
    private static final int REQUEST_SCAN_LOGIN_QRCODE_RESULT = 5;
    private static final int CACHE_CONVERSATION_LIST_SUCCESS = 6;
    private View rootView;
    private RecyclerView conversionRecycleView;
    private SwipeRefreshLayout swipeRefreshLayout;
    private ChatAPIService apiService;
    private List<UIConversation> displayUIConversationList = new ArrayList<>();
    private ConversationAdapter conversationAdapter;
    private Handler handler;
    private CommunicationFragmentReceiver receiver;
    private TextView titleText;
    private RelativeLayout noDataLayout;
    private boolean isGroupIconCreate = false;
    private PopupWindow popupWindow;
    private boolean isFirstConnectWebsockt = true;//判断是否第一次连上websockt
    private LoadingDialog loadingDlg;
    private ImageView headerFunctionOptionImg;
    private ImageView contactImg;
    private TextView contactSearchTextView;
    private CheckingNetStateUtils checkingNetStateUtils;
    private OnClickListener onViewClickListener = new OnClickListener() {

        @Override
        public void onClick(View v) {
            // TODO Auto-generated method stub
            switch (v.getId()) {
                case R.id.more_function_list_img:
                    showPopupWindow(rootView.findViewById(R.id.more_function_list_img));
                    break;
                case R.id.contact_img:
                    Bundle bundle = new Bundle();
                    bundle.putInt("select_content", 4);
                    bundle.putBoolean("isMulti_select", false);
                    bundle.putString("title",
                            getActivity().getString(R.string.adress_list));
                    IntentUtils.startActivity(getActivity(),
                            ContactSearchActivity.class, bundle);
                    PVCollectModelCacheUtils.saveCollectModel("contact", "communicate");
                    break;
                case R.id.message_create_group_layout:
                    Intent contactIntent = new Intent();
                    contactIntent.putExtra(ContactSearchFragment.EXTRA_TYPE, 2);
                    contactIntent.putExtra(ContactSearchFragment.EXTRA_MULTI_SELECT, true);
                    contactIntent.putExtra(ContactSearchFragment.EXTRA_TITLE,
                            getActivity().getString(R.string.message_create_group));
                    contactIntent.setClass(getActivity(), ContactSearchActivity.class);
                    startActivityForResult(contactIntent, CREAT_CHANNEL_GROUP);
                    popupWindow.dismiss();
                    break;
                case R.id.message_scan_layout:
//                    Intent scanIntent = new Intent();
//                    scanIntent.setClass(getActivity(), PreviewDecodeActivity.class);
//                    scanIntent.putExtra("from", "CommunicationFragment");
//                    startActivityForResult(scanIntent, REQUEST_SCAN_LOGIN_QRCODE_RESULT);
//                    AppUtils.openScanCode(getActivity(),REQUEST_SCAN_LOGIN_QRCODE_RESULT);
                    AppUtils.openScanCode(CommunicationFragment.this, REQUEST_SCAN_LOGIN_QRCODE_RESULT);
                    popupWindow.dismiss();
                    break;
                case R.id.tv_search_contact:
                    IntentUtils.startActivity(getActivity(), CommunicationSearchGroupContactActivity.class);
                    break;
                default:
                    break;
            }
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        EventBus.getDefault().register(this);
        initView();
        sortConversationList();// 对Channel 进行排序
        registerMessageFragmentReceiver();
        getConversationList();
        setHeaderFunctionOptions(null);
        checkingNetStateUtils = new CheckingNetStateUtils(getContext(), NetUtils.pingUrls, NetUtils.httpUrls);
    }

    /**
     * 切换tab实现网络状态监测
     */
    @Override
    public void onResume() {
        super.onResume();
        checkingNetStateUtils.getNetStateResult(5);
    }

    private void initView() {
        // TODO Auto-generated method stub
        handMessage();
        apiService = new ChatAPIService(getActivity());
        apiService.setAPIInterface(new WebService());
        rootView = LayoutInflater.from(getActivity()).inflate(R.layout.fragment_communication, null);
        headerFunctionOptionImg = (ImageView) rootView.findViewById(R.id.more_function_list_img);
        headerFunctionOptionImg.setOnClickListener(onViewClickListener);
        contactImg = (ImageView) rootView.findViewById(R.id.contact_img);
        contactImg.setOnClickListener(onViewClickListener);
        titleText = (TextView) rootView.findViewById(R.id.header_text);
        noDataLayout = (RelativeLayout) rootView.findViewById(R.id.rl_no_chat);
        contactSearchTextView = rootView.findViewById(R.id.tv_search_contact);
        contactSearchTextView.setOnClickListener(onViewClickListener);
        initPullRefreshLayout();
        initRecycleView();
        loadingDlg = new LoadingDialog(getActivity());
        showSocketStatusInTitle(WebSocketPush.getInstance().getWebsocketStatus());
    }

    /**
     * 注册接收消息的广播
     */
    private void registerMessageFragmentReceiver() {
        // TODO Auto-generated method stub
        receiver = new CommunicationFragmentReceiver();
        IntentFilter intentFilter = new IntentFilter("message_notify");
        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(receiver, intentFilter);
    }

    /**
     * 初始化PullRefreshLayout
     */
    private void initPullRefreshLayout() {
        swipeRefreshLayout = (SwipeRefreshLayout) rootView
                .findViewById(R.id.refresh_layout);
        swipeRefreshLayout.setColorSchemeColors(getResources().getColor(R.color.header_bg_blue), getResources().getColor(R.color.header_bg_blue));
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                WebSocketPush.getInstance().startWebSocket();
                getConversationList();
                getMessage();
            }
        });
    }

    /**
     * 初始化ListView
     */
    private void initRecycleView() {
        conversionRecycleView = (RecyclerView) rootView.findViewById(R.id.rcv_conversation);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity());
        conversionRecycleView.setLayoutManager(linearLayoutManager);
        conversationAdapter = new ConversationAdapter(getActivity(), displayUIConversationList);
        conversationAdapter.setAdapterListener(new ConversationAdapter.AdapterListener() {
            @Override
            public void onItemClick(View view, int position) {
                try {
                    UIConversation uiConversation = displayUIConversationList.get(position);
                    Conversation conversation = uiConversation.getConversation();
                    String type = conversation.getType();
                    if (type.equals(Conversation.TYPE_CAST) || type.equals(Conversation.TYPE_DIRECT) || type.equals(Conversation.TYPE_GROUP)) {
                        Bundle bundle = new Bundle();
                        bundle.putSerializable(ConversationBaseActivity.EXTRA_UNREAD_MESSAGE, (Serializable) MessageCacheUtil.getAllUnReadMessage(getActivity(), conversation.getId()));
                        bundle.putSerializable(ConversationActivity.EXTRA_CONVERSATION, conversation);
                        IntentUtils.startActivity(getActivity(), ConversationActivity.class, bundle);
                    } else if (conversation.getType().equals(Conversation.TYPE_LINK)) {
                        EmmAction emmAction = new EmmAction(conversation.getAction());
                        if (emmAction.getCanOpenAction()) {
                            if (emmAction.getUrl().startsWith("http")) {
                                UriUtils.openUrl(getActivity(), emmAction.getUrl());
                            } else {
                                IntentUtils.startActivity(getActivity(), emmAction.getUrl());
                            }
                        }
                    } else {
                        ToastUtils.show(MyApplication.getInstance(), R.string.not_support_open_channel);
                    }
                    setConversationRead(position, uiConversation);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public boolean onItemLongClick(View view, int position) {
                UIConversation LongClickUIConversation;
                LongClickUIConversation = displayUIConversationList.get(position);
                showConversationOperationDlg(LongClickUIConversation);
                return true;
            }

            @Override
            public void onDataChange() {
                noDataLayout.setVisibility(displayUIConversationList.size() > 0 ? View.GONE : View.VISIBLE);
                //设置消息tab页面的小红点（未读消息提醒）的显示
                int unReadCount = 0;
                for (UIConversation uiConversation : displayUIConversationList) {
                    unReadCount += uiConversation.getUnReadCount();
                }
                EventBus.getDefault().post(new SimpleEventMessage(Constant.EVENTBUS_TAG_SET_ALL_MESSAGE_UNREAD_COUNT, unReadCount));
            }

            @Override
            public void onNetExceptionWightClick() {
                IntentUtils.startActivity(getActivity(), NetWorkStateDetailActivity.class);
            }
        });
        conversionRecycleView.setAdapter(conversationAdapter);
    }



    /**
     * 弹出频道操作选择框
     *
     * @param uiConversation
     */
    private void showConversationOperationDlg(final UIConversation uiConversation) {
        // TODO Auto-generated method stub
        final String[] items;
        if (uiConversation.getConversation().getType().equals("CAST")) {
            items = new String[]{getString(uiConversation.getConversation().isStick() ? R.string.chat_remove_from_top : R.string.chat_stick_on_top)};
        } else {
            items = new String[]{getString(uiConversation.getConversation().isStick() ? R.string.chat_remove_from_top : R.string.chat_stick_on_top), getString(R.string.chat_remove)};
        }
        TextView textView = new TextView(getContext());
        textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13);
        textView.setTextColor(Color.parseColor("#999999"));
        textView.setText(uiConversation.getTitle());
        int paddingTop = DensityUtil.dip2px(20);
        int paddingLeft = DensityUtil.dip2px(24);
        textView.setPadding(paddingLeft, paddingTop, paddingLeft, 0);
        textView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        new CustomDialog.ListDialogBuilder(getActivity())
                .setTitle(uiConversation.getTitle())
                .setItems(items, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        if (which == 0) {
                            setConversationStick(uiConversation.getId(), !uiConversation.getConversation().isStick());
                        } else {
                            setConversationHide(uiConversation);
                        }
                    }
                })
                .setCustomTitle(textView)
                .show();
    }

    /**
     * 根据服务端的配置信息显示和隐藏沟通header上的通讯录和“+”按钮
     *
     * @param getAppMainTabResult
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void setHeaderFunctionOptions(GetAppMainTabResult getAppMainTabResult) {
        if (getAppMainTabResult != null) {
            ArrayList<MainTabResult> mainTabResultList = getAppMainTabResult.getMainTabPayLoad().getMainTabResultList();
            for (int i = 0; i < mainTabResultList.size(); i++) {
                if (mainTabResultList.get(i).getUri().equals(Constant.APP_TAB_BAR_COMMUNACATE)) {
                    MainTabProperty mainTabProperty = mainTabResultList.get(i).getMainTabProperty();
                    if (mainTabProperty != null) {
                        if (!mainTabProperty.isCanCreate()) {
                            headerFunctionOptionImg.setVisibility(View.GONE);
                        }
                        if (!mainTabProperty.isCanContact()) {
                            contactImg.setVisibility(View.GONE);
                        }
                    }
                    break;
                }
            }
        }
    }

    /**
     * 沟通页网络异常提示框
     *
     * @param netState 通过Action获取操作类型
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void netWorkStateTip(SimpleEventMessage netState) {
        if (netState.getAction().equals(Constant.EVENTBUS_TAG_NET_EXCEPTION_HINT)) {   //网络异常提示
            conversationAdapter.setNetExceptionView((boolean) netState.getMessageObj()
                    || NetworkInfo.State.CONNECTED == NetUtils.getNetworkMobileState(getActivity())
                    || NetUtils.isVpnConnected());
            if ((Boolean) netState.getMessageObj()) {
                WebSocketPush.getInstance().startWebSocket();
            }
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        EventBus.getDefault().post(new SimpleEventMessage(Constant.EVENTBUS_TAG_OPEN_DEFALT_TAB, null));
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        setFragmentStatusBarCommon();
        if (rootView == null) {
            rootView = inflater.inflate(R.layout.fragment_message, container,
                    false);
        }
        ViewGroup parent = (ViewGroup) rootView.getParent();
        if (parent != null) {
            parent.removeView(rootView);
        }
        return rootView;
    }

    @Override
    public void onPause() {
        super.onPause();
        if (popupWindow != null && popupWindow.isShowing()) {
            popupWindow.dismiss();
        }
    }

    /**
     * 通讯录和创建群组，扫一扫合并
     *
     * @param view
     */
    private void showPopupWindow(View view) {
        View contentView = LayoutInflater.from(getActivity())
                .inflate(R.layout.pop_message_window_view, null);
        popupWindow = new PopupWindow(contentView,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT, true);
        popupWindow.setTouchable(true);
        popupWindow.setTouchInterceptor(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return false;
            }
        });
        popupWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {
                AppUtils.setWindowBackgroundAlpha(getActivity(), 1.0f);
            }
        });
        contentView.findViewById(R.id.message_create_group_layout).setOnClickListener(onViewClickListener);
        contentView.findViewById(R.id.message_scan_layout).setOnClickListener(onViewClickListener);
        popupWindow.setBackgroundDrawable(getResources().getDrawable(
                R.drawable.pop_window_view_tran));
        AppUtils.setWindowBackgroundAlpha(getActivity(), 0.8f);
        // 设置好参数之后再show
        popupWindow.showAsDropDown(view);
    }


    /**
     * 为群组创建头像
     *
     * @param conversationList
     */
    private void createGroupIcon(List<Conversation> conversationList) {
        if (!NetUtils.isNetworkConnected(MyApplication.getInstance(), false)) {
            return;
        }
        if (!MyApplication.getInstance().getIsContactReady()) {
            return;
        }
        if (conversationList != null && conversationList.size() == 0) {
            return;
        }
        isGroupIconCreate = true;
        ConversationGroupIconUtils.getInstance().create(conversationList);
    }


    /**
     * channel 显示排序
     */
    private void sortConversationList() {
        // TODO Auto-generated method stub
        Thread sortConversationThread = new Thread() {
            @Override
            public void run() {
                try {
                    List<Conversation> conversationList = ConversationCacheUtils.getConversationList(MyApplication.getInstance());
                    List<UIConversation> uiConversationList = new ArrayList<>();
                    if (conversationList.size() > 0) {
                        uiConversationList = UIConversation.conversationList2UIConversationList(conversationList);
                        ConversationCacheUtils.saveConversationList(MyApplication.getInstance(), conversationList);
                        List<UIConversation> stickUIConversationList = new ArrayList<>();
                        Iterator<UIConversation> it = uiConversationList.iterator();
                        while (it.hasNext()) {
                            UIConversation uiConversation = it.next();
                            Conversation conversation = uiConversation.getConversation();
                            if (conversation.isHide()) {
                                if (uiConversation.getUnReadCount() != 0) {
                                    conversation.setHide(false);
                                    conversation.setDraft(getDraftWords(conversation));
                                    ConversationCacheUtils.saveConversation(MyApplication.getInstance(), conversation);
                                } else {
                                    it.remove();
                                    continue;
                                }
                            } else if (conversation.isStick()) {
                                uiConversation.getConversation().setDraft(getDraftWords(conversation));
                                stickUIConversationList.add(uiConversation);
                                it.remove();
                                continue;
                            }
                            uiConversation.getConversation().setDraft(getDraftWords(conversation));
                            if (uiConversation.getMessageList().size() == 0) {
                                //当会话内没有消息时，如果是单聊或者不是owner的群聊，则进行隐藏
                                if (conversation.getType().equals(Conversation.TYPE_DIRECT) ||
                                        (conversation.getType().equals(CREAT_CHANNEL_GROUP) && conversation.getOwner().equals(MyApplication.getInstance().getUid()))) {
                                    it.remove();
                                    continue;
                                }
                            }
                        }
                        Collections.sort(stickUIConversationList, new UIConversation().new SortComparator());
                        Collections.sort(uiConversationList, new UIConversation().new SortComparator());
                        uiConversationList.addAll(0, stickUIConversationList);
                    }
                    if (handler != null) {
                        android.os.Message message = handler.obtainMessage(SORT_CONVERSATION_COMPLETE, uiConversationList);
                        message.sendToTarget();
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };
        sortConversationThread.start();
    }

    /**
     * 根据频道获取草稿
     *
     * @param conversation
     * @return
     */
    private String getDraftWords(Conversation conversation) {
        String draft = MessageCacheUtil.getDraftByCid(getActivity(), conversation.getId());
        return StringUtils.isBlank(draft) ? "" : draft;
    }


    private void handMessage() {
        // TODO Auto-generated method stub
        handler = new Handler() {

            @Override
            public void handleMessage(android.os.Message msg) {
                // TODO Auto-generated method stub
                switch (msg.what) {
                    case RERESH_GROUP_ICON:
                        boolean isCreateNewGroupIcon = (Boolean) msg.obj;
                        if (isCreateNewGroupIcon) {
                            conversationAdapter.notifyDataSetChanged();
                        }
                        break;
                    case SORT_CONVERSATION_COMPLETE:
                        displayUIConversationList = (List<UIConversation>) msg.obj;
                        conversationAdapter.setData(displayUIConversationList);
                        conversationAdapter.notifyDataSetChanged();
                        break;
                    case SORT_CONVERSATION_LIST:
                        sortConversationList();
                        break;
                    case CACHE_CONVERSATION_LIST_SUCCESS:
                        sortConversationList();
                        List<Conversation> conversationList = (List<Conversation>) msg.obj;
                        createGroupIcon(conversationList);
                        break;
                    default:
                        break;
                }

            }

        };
    }

    /**
     * 缓存推送的消息体，消息连续时间段，已读消息的id
     *
     * @param receivedWSMessage
     */
    private void cacheReceiveMessage(Message receivedWSMessage) {
        // TODO Auto-generated method stub
        Message channelNewMessage = MessageCacheUtil.getNewMessge(MyApplication.getInstance(), receivedWSMessage.getChannel());
//        MessageCacheUtil.saveMessage(MyApplication.getInstance(), receivedWSMessage);
        Long ChannelMessageMatheSetStart = (channelNewMessage == null) ? receivedWSMessage.getCreationDate() : channelNewMessage.getCreationDate();
        MessageMatheSetCacheUtils.add(MyApplication.getInstance(),
                receivedWSMessage.getChannel(), new MatheSet(ChannelMessageMatheSetStart, receivedWSMessage.getCreationDate()));
    }

    /**
     * 显示websocket的连接状态
     *
     * @param socketStatus
     */
    private void showSocketStatusInTitle(String socketStatus) {
        if (socketStatus.equals(Socket.EVENT_CONNECTING)) {
            titleText.setText(R.string.socket_connecting);
        } else if (socketStatus.equals(Socket.EVENT_CONNECT)) {
            //当断开以后连接成功(非第一次连接上)后重新拉取一遍消息
            if (!isFirstConnectWebsockt) {
                getConversationList();
            }
            getMessage();
            isFirstConnectWebsockt = false;
            String appTabs = PreferencesByUserAndTanentUtils.getString(getActivity(), Constant.PREF_APP_TAB_BAR_INFO_CURRENT, "");
            if (!StringUtils.isBlank(appTabs)) {
                titleText.setText(AppTabUtils.getTabTitle(getActivity(), getClass().getSimpleName()));
            } else {
                titleText.setText(R.string.communicate);
            }
        } else if (socketStatus.equals(Socket.EVENT_DISCONNECT) || socketStatus.equals(Socket.EVENT_CONNECT_ERROR)) {
            titleText.setText(R.string.socket_close);
        }
    }

    /**
     * 将所有频道的消息置为已读
     */
    private void setAllConversationRead() {
        // TODO Auto-generated method stub
        new Thread(new Runnable() {
            @Override
            public void run() {
                MessageCacheUtil.setAllMessageRead(MyApplication.getInstance());
            }
        }).start();
        for (UIConversation uiConversation : displayUIConversationList) {
            if (uiConversation.getUnReadCount() != 0) {
                uiConversation.setUnReadCount(0);
                WSAPIService.getInstance().setChannelMessgeStateRead(uiConversation.getId());
            }
        }
        conversationAdapter.setData(displayUIConversationList);
        conversationAdapter.notifyDataSetChanged();
    }

    /**
     * 将单个频道消息置为已读
     *
     * @param uiConversation
     */
    private void setConversationRead(int position, final UIConversation uiConversation) {
        if (uiConversation.getUnReadCount() > 0) {
            WSAPIService.getInstance().setChannelMessgeStateRead(uiConversation.getId());
            new Thread(new Runnable() {
                @Override
                public void run() {
                    MessageCacheUtil.setChannelMessageRead(MyApplication.getInstance(), uiConversation.getId());
                }
            }).start();
            uiConversation.setUnReadCount(0);
            conversationAdapter.setData(displayUIConversationList);
            conversationAdapter.notifyRealItemChanged(position);
        }
    }

    @Override
    public void onDestroy() {
        // TODO Auto-generated method stub
        super.onDestroy();
        if (receiver != null) {
            LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(receiver);
            receiver = null;
        }
        if (handler != null) {
            handler = null;
        }
        EventBus.getDefault().unregister(this);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        // TODO Auto-generated method stub
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == getActivity().RESULT_OK
                && requestCode == CREAT_CHANNEL_GROUP) {
            // 创建群组
            String searchResult = data.getExtras().getString("searchResult");
            try {
                JSONObject searchResultObj = new JSONObject(searchResult);
                JSONArray peopleArray = searchResultObj.getJSONArray("people");

                if (peopleArray.length() > 0
                        && NetUtils.isNetworkConnected(MyApplication.getInstance())) {
                    creatGroupChannel(peopleArray);
                }
            } catch (JSONException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
                ToastUtils.show(getActivity(),
                        getActivity().getString(R.string.creat_group_fail));
            }
        } else if ((resultCode == RESULT_OK) && (requestCode == REQUEST_SCAN_LOGIN_QRCODE_RESULT)) {
            if (data.hasExtra("isDecodeSuccess")) {
                boolean isDecodeSuccess = data.getBooleanExtra("isDecodeSuccess", false);
                if (isDecodeSuccess) {
                    String msg = data.getStringExtra("msg");
                    ScanQrCodeUtils.getScanQrCodeUtilsInstance(getActivity()).handleActionWithMsg(msg);
                } else {
                    ToastUtils.show(getActivity(), getString(R.string.qr_code_analysis_fail));
                }
            }
        }
    }

    /**
     * 创建群组
     *
     * @param peopleArray
     */
    private void creatGroupChannel(JSONArray peopleArray) {
        // TODO Auto-generated method stub
        new ConversationCreateUtils().createGroupConversation(getActivity(), peopleArray,
                new ConversationCreateUtils.OnCreateGroupConversationListener() {

                    @Override
                    public void createGroupConversationSuccess(Conversation conversation) {
                        Bundle bundle = new Bundle();
                        bundle.putSerializable(ConversationActivity.EXTRA_CONVERSATION, conversation);
                        IntentUtils.startActivity(getActivity(), ConversationActivity.class, bundle);
                        notifyConversationChange(conversation);
                    }

                    @Override
                    public void createGroupConversationFail() {

                    }
                });
    }

    //同步聊天消息  实时更新界面
    private void notifyConversationChange(Conversation conversation) {
        sortConversationList();
        List<Conversation> conversationList = new ArrayList<>();
        conversationList.add(conversation);
        createGroupIcon(conversationList);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onReceiverSimpleEventMessage(SimpleEventMessage eventMessage) {
        Conversation conversation = null;
        int index = -1;
        switch (eventMessage.getAction()) {
            case Constant.EVENTBUS_TAG_REFRESH_CONVERSATION_ADAPTER:
                conversationAdapter.notifyDataSetChanged();
                break;
            case Constant.EVENTBUS_TAG_REFRESH_CONVERSATION:
                sortConversationList();
                break;
            case Constant.EVENTBUS_TAG_UPDATE_CHANNEL_NAME:
                conversation = (Conversation) eventMessage.getMessageObj();
                index = displayUIConversationList.indexOf(new UIConversation(conversation.getId()));
                if (index != -1) {
                    displayUIConversationList.get(index).setTitle(conversation.getName());
                    conversationAdapter.setData(displayUIConversationList);
                    conversationAdapter.notifyRealItemChanged(index);
                }
                break;
            case Constant.EVENTBUS_TAG_UPDATE_CHANNEL_FOCUS:
                conversation = (Conversation) eventMessage.getMessageObj();
                index = displayUIConversationList.indexOf(new UIConversation(conversation.getId()));
                if (index != -1) {
                    sortConversationList();
                }
                break;
            case Constant.EVENTBUS_TAG_UPDATE_CHANNEL_DND:
                conversation = (Conversation) eventMessage.getMessageObj();
                index = displayUIConversationList.indexOf(new UIConversation(conversation.getId()));
                if (index != -1) {
                    displayUIConversationList.get(index).getConversation().setDnd(conversation.isDnd());
                    conversationAdapter.setData(displayUIConversationList);
                    conversationAdapter.notifyRealItemChanged(index);
                }
                break;
            case Constant.EVENTBUS_TAG_QUIT_CHANNEL_GROUP:
                conversation = (Conversation) eventMessage.getMessageObj();
                index = displayUIConversationList.indexOf(new UIConversation(conversation.getId()));
                if (index != -1) {
                    displayUIConversationList.remove(index);
                    conversationAdapter.setData(displayUIConversationList);
                    conversationAdapter.notifyRealItemRemoved(index);
                }
                break;
            case Constant.EVENTBUS_TAG_CHAT_CHANGE:
                conversation = (Conversation) eventMessage.getMessageObj();
                notifyConversationChange(conversation);
                break;
        }
    }

    //接收到websocket发过来的消息，在channel里正常收发消息触发此方法
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onReceiveWSMessage(EventMessage eventMessage) {
        if (eventMessage.getTag().equals(Constant.EVENTBUS_TAG_RECERIVER_SINGLE_WS_MESSAGE)) {
            if (eventMessage.getStatus() == EventMessage.RESULT_OK) {
                String content = eventMessage.getContent();
                JSONObject contentObj = JSONUtils.getJSONObject(content);
                Message receivedWSMessage = new Message(contentObj);
                //验重处理
                if (MessageCacheUtil.getMessageByMid(MyApplication.getInstance(), receivedWSMessage.getId()) == null) {
                    if (MyApplication.getInstance().getCurrentChannelCid().equals(receivedWSMessage.getChannel())) {
                        receivedWSMessage.setRead(Message.MESSAGE_READ);
                    }
                    MessageCacheUtil.handleRealMessage(MyApplication.getInstance(), receivedWSMessage);
                    //如果是音频消息，需要检查本地是否有音频文件，没有则下载
                    if (receivedWSMessage.getType().equals(Message.MESSAGE_TYPE_MEDIA_VOICE)) {
                        String fileSavePath = MyAppConfig.getCacheVoiceFilePath(receivedWSMessage.getChannel(), receivedWSMessage.getId());
                        if (!new File(fileSavePath).exists()) {
                            String source = APIUri.getChatVoiceFileResouceUrl(receivedWSMessage.getChannel(), receivedWSMessage.getMsgContentMediaVoice().getMedia());
                            new DownLoaderUtils().startDownLoad(source, fileSavePath, null);
                        }
                    }
                    Conversation receiveMessageConversation = ConversationCacheUtils.getConversation(MyApplication.getInstance(), receivedWSMessage.getChannel());
                    cacheReceiveMessage(receivedWSMessage);
                    if (receiveMessageConversation == null) {
                        getConversationList();
                    } else {
                        if (receiveMessageConversation.isHide()) {
                            ConversationCacheUtils.setConversationHide(MyApplication.getInstance(), receiveMessageConversation.getId(), false);
                        }
                        sortConversationList();
                    }
                }
            } else {
                //当消息发送失败，已离开此频道时，存储该消息
                Message fakeMessage = MessageCacheUtil.getMessageByMid(MyApplication.getInstance(), eventMessage.getId());
                if (fakeMessage != null) {
                    if (!MyApplication.getInstance().getCurrentChannelCid().equals(fakeMessage.getChannel())) {
                        fakeMessage.setSendStatus(Message.MESSAGE_SEND_FAIL);
                        MessageCacheUtil.saveMessage(MyApplication.getInstance(), fakeMessage);
                    }
                }
            }

        }
    }


    //接收到websocket发过来的消息，拨打音视频电话，被呼叫触发
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onReceiveVoiceOrVideoCall(final GetVoiceAndVideoResult getVoiceAndVideoResult) {
        final CustomProtocol customProtocol = new CustomProtocol(getVoiceAndVideoResult.getContextParamsSchema());
        //接收到消息后告知服务端
        WSAPIService.getInstance().sendReceiveStartVoiceAndVideoCallMessageSuccess(getVoiceAndVideoResult.getTracer());
        //判断如果在通话中就不再接听新的来电
        if (VoiceCommunicationUtils.getInstance().getCommunicationState() != ChannelVoiceCommunicationActivity.COMMUNICATION_STATE_ING) {
            PermissionRequestManagerUtils.getInstance().requestRuntimePermission(getContext(), Permissions.RECORD_AUDIO, new PermissionRequestCallback() {
                @Override
                public void onPermissionRequestSuccess(List<String> permissions) {
                    if (customProtocol.getProtocol().equals("ecc-cloudplus-cmd") && !StringUtils.isBlank(customProtocol.getParamMap().get("cmd"))) {
                        if (customProtocol.getParamMap().get("cmd").equals("invite")) {
                            startVoiceOrVideoCall(getVoiceAndVideoResult.getContextParamsRoom(), getVoiceAndVideoResult.getContextParamsType(), getVoiceAndVideoResult.getChannel());
                        } else if (customProtocol.getParamMap().get("cmd").equals("refuse")) {
                            changeUserConnectStateByUid(VoiceCommunicationJoinChannelInfoBean.CONNECT_STATE_REFUSE, customProtocol.getParamMap().get("uid"));
                            checkCommunicationFinish();
                        } else if (customProtocol.getParamMap().get("cmd").equals("destroy")) {
                            SuspensionWindowManagerUtils.getInstance().hideCommunicationSmallWindow();
                        }
                    }
                }

                @Override
                public void onPermissionRequestFail(List<String> permissions) {
                    String agoraChannelId = customProtocol.getParamMap().get("roomid");
                    String channelId = customProtocol.getParamMap().get("channelid");
                    String fromUid = customProtocol.getParamMap().get("uid");
                    VoiceCommunicationUtils.getInstance().getVoiceCommunicationChannelInfo(channelId, agoraChannelId, fromUid);
                    ToastUtils.show(getContext(), PermissionRequestManagerUtils.getInstance().getPermissionToast(getContext(), permissions));
                }
            });
        } else {
            String agoraChannelId = customProtocol.getParamMap().get("roomid");
            String channelId = customProtocol.getParamMap().get("channelid");
            String fromUid = customProtocol.getParamMap().get("uid");
            VoiceCommunicationUtils.getInstance().getVoiceCommunicationChannelInfo(channelId, agoraChannelId, fromUid);
        }
    }


    /**
     * 修改用户的链接状态
     * 通过云+uid
     *
     * @param connectStateConnected
     */
    private void changeUserConnectStateByUid(int connectStateConnected, String uid) {
        List<VoiceCommunicationJoinChannelInfoBean> voiceCommunicationMemberList = VoiceCommunicationUtils
                .getInstance().getVoiceCommunicationMemberList();
        if (voiceCommunicationMemberList != null && voiceCommunicationMemberList.size() > 0) {
            for (int i = 0; i < voiceCommunicationMemberList.size(); i++) {
                if (voiceCommunicationMemberList.get(i).getUserId().equals(uid)) {
                    voiceCommunicationMemberList.get(i).setConnectState(connectStateConnected);
                    break;
                }
            }
        }
    }

    /**
     * 检查是否需要退出
     */
    private void checkCommunicationFinish() {
        List<VoiceCommunicationJoinChannelInfoBean> voiceCommunicationMemberList = VoiceCommunicationUtils
                .getInstance().getVoiceCommunicationMemberList();
        if (voiceCommunicationMemberList != null && voiceCommunicationMemberList.size() > 0) {
            int waitAndCommunicationSize = 0;
            for (int i = 0; i < voiceCommunicationMemberList.size(); i++) {
                if (voiceCommunicationMemberList.get(i).getConnectState() == VoiceCommunicationJoinChannelInfoBean.CONNECT_STATE_CONNECTED ||
                        voiceCommunicationMemberList.get(i).getConnectState() == VoiceCommunicationJoinChannelInfoBean.CONNECT_STATE_INIT) {
                    waitAndCommunicationSize = waitAndCommunicationSize + 1;
                }
            }
            if (waitAndCommunicationSize < 2) {
                VoiceCommunicationUtils.getInstance().destroy();
                SuspensionWindowManagerUtils.getInstance().hideCommunicationSmallWindow();
            }
        }
    }

    private void startVoiceOrVideoCall(String contextParamsRoom, String contextParamsType, String cid) {
        //消息拦截逻辑，以后应当拦截命令消息，此时注释掉，以后解开注意判空
        Intent intent = new Intent();
        intent.setClass(getActivity(), ChannelVoiceCommunicationActivity.class);
        intent.putExtra(ChannelVoiceCommunicationActivity.VOICE_VIDEO_CALL_AGORA_ID, contextParamsRoom);
        intent.putExtra(ConversationActivity.CLOUD_PLUS_CHANNEL_ID, cid);
        intent.putExtra(ChannelVoiceCommunicationActivity.VOICE_VIDEO_CALL_TYPE, getCommunicationType(contextParamsType));
        intent.putExtra(ChannelVoiceCommunicationActivity.VOICE_COMMUNICATION_STATE, ChannelVoiceCommunicationActivity.INVITEE_LAYOUT_STATE);
        startActivity(intent);
    }

    private String getCommunicationType(String contextParamsType) {
        if (contextParamsType.equals("VOICE")) {
            return ECMChatInputMenu.VOICE_CALL;
        } else if (contextParamsType.equals("VIDEO")) {
            return ECMChatInputMenu.VIDEO_CALL;
        }
        return ECMChatInputMenu.VOICE_CALL;
    }

    //socket断开重连时（如断网联网）会触发此方法
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onReiceveWSOfflineMessage(EventMessage eventMessage) {
        if (eventMessage.getTag().equals(Constant.EVENTBUS_TAG_GET_OFFLINE_WS_MESSAGE)) {
            if (eventMessage.getStatus() == EventMessage.RESULT_OK) {
                //清空离线消息最后一条消息标志位
                PreferencesByUserAndTanentUtils.putString(MyApplication.getInstance(), Constant.PREF_GET_OFFLINE_LAST_MID, "");
                String content = eventMessage.getContent();
                GetOfflineMessageListResult getOfflineMessageListResult = new GetOfflineMessageListResult(content);
                List<Message> offlineMessageList = getOfflineMessageListResult.getMessageList();
                List<Message> currentChannelOfflineMessageList = new ArrayList<>();
                //将当前所处频道的消息存为已读
                if (!StringUtils.isBlank(MyApplication.getInstance().getCurrentChannelCid())) {
                    for (Message message : offlineMessageList) {
                        if (message.getChannel().equals(MyApplication.getInstance().getCurrentChannelCid())) {
                            message.setRead(Message.MESSAGE_READ);
                            currentChannelOfflineMessageList.add(message);
                        }
                    }
                    if (currentChannelOfflineMessageList.size() > 0) {
                        //将离线消息发送到当前频道
                        EventBus.getDefault().post(new SimpleEventMessage(Constant.EVENTBUS_TAG_CURRENT_CHANNEL_OFFLINE_MESSAGE, currentChannelOfflineMessageList));
                    }
                }
                new CacheMessageListThread(offlineMessageList, getOfflineMessageListResult.getChannelMessageSetList()).start();
                List<Message> mediaVoiceMessageList = getOfflineMessageListResult.getMediaVoiceMessageList();
                for (Message message : mediaVoiceMessageList) {
                    String fileSavePath = MyAppConfig.getCacheVoiceFilePath(message.getChannel(), message.getId());
                    if (!new File(fileSavePath).exists()) {
                        String source = APIUri.getChatVoiceFileResouceUrl(message.getChannel(), message.getMsgContentMediaVoice().getMedia());
                        new DownLoaderUtils().startDownLoad(source, fileSavePath, null);
                    }
                }
            }

        }
    }

    //本地无消息时触发此方法（如应用首次安装或者在沟通页面下拉刷新）
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onReceiveWSRecentMessage(EventMessage eventMessage) {
        if (eventMessage.getTag().equals(Constant.EVENTBUS_TAG_GET_CHANNEL_RECENT_MESSAGE)) {
            if (eventMessage.getStatus() == EventMessage.RESULT_OK) {
                String content = eventMessage.getContent();
                GetRecentMessageListResult getRecentMessageListResult = new GetRecentMessageListResult(content);
                List<Message> recentMessageList = getRecentMessageListResult.getMessageList();
                List<Message> currentChannelRecentMessageList = new ArrayList<>();
                //将当前所处频道的消息存为已读
                if (!StringUtils.isBlank(MyApplication.getInstance().getCurrentChannelCid())) {
                    for (Message message : recentMessageList) {
                        if (message.getChannel().equals(MyApplication.getInstance().getCurrentChannelCid())) {
                            message.setRead(Message.MESSAGE_READ);
                            currentChannelRecentMessageList.add(message);
                        }
                    }
                    if (currentChannelRecentMessageList.size() > 0) {
                        //将离线消息发送到当前频道
                        EventBus.getDefault().post(new SimpleEventMessage(Constant.EVENTBUS_TAG_CURRENT_CHANNEL_OFFLINE_MESSAGE, currentChannelRecentMessageList));
                    }
                }
                new CacheMessageListThread(getRecentMessageListResult.getMessageList(), getRecentMessageListResult.getChannelMessageSetList()).start();
            }
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onReceiveMessageStateRead(EventMessage eventMessage) {
        if (eventMessage.getTag().equals(Constant.EVENTBUS_TAG_RECERIVER_MESSAGE_STATE_READ)) {
            String content = eventMessage.getContent();
            ChannelMessageReadStateResult channelMessageReadStateResult = new ChannelMessageReadStateResult(content);
            List<String> messageReadIdList = channelMessageReadStateResult.getMessageReadIdList();
            MessageCacheUtil.setMessageStateRead(MyApplication.getInstance(), messageReadIdList);
            for (UIConversation uiConversation : displayUIConversationList) {
                long unReadCount = MessageCacheUtil.getChannelMessageUnreadCount(MyApplication.getInstance(), uiConversation.getId());
                uiConversation.setUnReadCount(unReadCount);
            }
            conversationAdapter.setData(displayUIConversationList);
            conversationAdapter.notifyDataSetChanged();
        }
    }

    /**
     * 获取消息会话列表
     */
    private void getConversationList() {
        if (NetUtils.isNetworkConnected(MyApplication.getInstance())) {
            apiService.getConversationList();
        } else {
            swipeRefreshLayout.setRefreshing(false);
        }
    }

    /**
     * 获取消息
     */
    public void getMessage() {
        if (NetUtils.isNetworkConnected(MyApplication.getInstance()) && WebSocketPush.getInstance().isSocketConnect()) {
            String lastMessageId = MessageCacheUtil.getLastSuccessMessageId(MyApplication.getInstance());
            if (lastMessageId != null) {
                //如果preferences中还存有离线消息最后一条消息id这个标志代表上一次离线消息没有获取成功，需要从这条消息开始重新获取
                String getOfflineLastMessageId = PreferencesByUserAndTanentUtils.getString(MyApplication.getInstance(), Constant.PREF_GET_OFFLINE_LAST_MID, "");
                if (StringUtils.isBlank(getOfflineLastMessageId)) {
                    getOfflineLastMessageId = lastMessageId;
                    PreferencesByUserAndTanentUtils.putString(MyApplication.getInstance(), Constant.PREF_GET_OFFLINE_LAST_MID, lastMessageId);
                }
                //获取离线消息
                WSAPIService.getInstance().getOfflineMessage(getOfflineLastMessageId);
            } else {
                PreferencesByUserAndTanentUtils.putString(MyApplication.getInstance(), Constant.PREF_GET_OFFLINE_LAST_MID, "");
                //获取每个频道最近消息
                WSAPIService.getInstance().getChannelRecentMessage();
            }
        }

    }

    /**
     * 设置频道是否置顶
     *
     * @param id
     * @param isStick
     */
    private void setConversationStick(String id, boolean isStick) {
        if (NetUtils.isNetworkConnected(MyApplication.getInstance())) {
            loadingDlg.show();
            apiService.setConversationStick(id, isStick);
        }
    }

    /**
     * 隐藏频道
     *
     * @param uiConversation
     */
    private void setConversationHide(final UIConversation uiConversation) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                ConversationCacheUtils.updateConversationHide(MyApplication.getInstance(), uiConversation.getId(), true);
                MessageCacheUtil.setChannelMessageRead(MyApplication.getInstance(), uiConversation.getId());
            }
        }).start();
        int index = displayUIConversationList.indexOf(uiConversation);
        if (index != -1) {
            long unReadCount = displayUIConversationList.get(index).getUnReadCount();
            displayUIConversationList.remove(index);
            conversationAdapter.setData(displayUIConversationList);
            conversationAdapter.notifyRealItemRemoved(index);
            if (unReadCount > 0) {
                WSAPIService.getInstance().setChannelMessgeStateRead(uiConversation.getId());
            }
        }
//        if (NetUtils.isNetworkConnected(MyApplication.getInstance())) {
//            loadingDlg.show();
//            apiService.setConversationHide(id, true);
//        }
    }

    class CacheConversationThread extends Thread {
        private GetConversationListResult getConversationListResult;

        public CacheConversationThread(GetConversationListResult getConversationListResult) {
            this.getConversationListResult = getConversationListResult;
        }

        @Override
        public void run() {
            List<Conversation> conversationList = getConversationListResult.getConversationList();
            List<Conversation> cacheConversationList = ConversationCacheUtils.getConversationList(MyApplication.getInstance());
            //将数据库中Conversation隐藏状态赋值给从网络拉取的最新数据
            for (Conversation conversation : conversationList) {
                int index = cacheConversationList.indexOf(conversation);
                if (index != -1) {
                    conversation.setHide(cacheConversationList.get(index).isHide());
                }
            }
            ConversationCacheUtils.saveConversationList(MyApplication.getInstance(), conversationList);
            //服务端和本地数据取交集
            List<Conversation> intersectionConversationList = new ArrayList<>();
            intersectionConversationList.addAll(conversationList);
            intersectionConversationList.retainAll(cacheConversationList);
            cacheConversationList.removeAll(intersectionConversationList);
            ConversationCacheUtils.deleteConversationList(MyApplication.getInstance(), cacheConversationList);
            if (handler != null) {
                if (isGroupIconCreate) {
                    conversationList.removeAll(intersectionConversationList);
                }
                android.os.Message message = handler.obtainMessage(CACHE_CONVERSATION_LIST_SUCCESS, conversationList);
                message.sendToTarget();
            }
        }
    }

    /**
     * 接受创建群组头像的icon
     *
     * @author Administrator
     */
    public class CommunicationFragmentReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            // TODO Auto-generated method stub
            String command = intent.getExtras().getString("command");
            switch (command) {
                case "creat_group_icon":
                    isGroupIconCreate = false;
                    createGroupIcon(null);
                    break;
                case "refresh_session_list":
                    getConversationList();
                    break;
                case "sort_session_list":
                    sortConversationList();
                    break;
                case "sync_all_base_data_success":
                    createGroupIcon(null);
                    sortConversationList();
                    break;
                case "set_all_message_read":
                    setAllConversationRead();
                    break;
                case "websocket_status":
                    String socketStatus = intent.getExtras().getString("status");
                    showSocketStatusInTitle(socketStatus);
                    break;
                default:
                    break;
            }
        }

    }

    class CacheMessageListThread extends Thread {
        private List<Message> messageList;
        private List<ChannelMessageSet> channelMessageSetList;

        public CacheMessageListThread(List<Message> messageList, List<ChannelMessageSet> channelMessageSetList) {
            this.messageList = messageList;
            this.channelMessageSetList = channelMessageSetList;
        }

        @Override
        public void run() {
            try {
                if (messageList != null && messageList.size() > 0) {
                    MessageCacheUtil.handleRealMessage(getActivity(), messageList, null, "", false);// 获取的消息需要缓存
                    if (channelMessageSetList != null && channelMessageSetList.size() > 0) {
                        for (ChannelMessageSet channelMessageSet : channelMessageSetList) {
                            MessageMatheSetCacheUtils.add(MyApplication.getInstance(), channelMessageSet.getCid(), channelMessageSet.getMatheSet());
                        }
                    }
                    if (handler != null) {
                        handler.sendEmptyMessage(SORT_CONVERSATION_LIST);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    class WebService extends APIInterfaceInstance {
        @Override
        public void returnConversationListSuccess(GetConversationListResult getConversationListResult) {
            if (getActivity() != null) {
                swipeRefreshLayout.setRefreshing(false);
                new CacheConversationThread(getConversationListResult).run();
            }
        }

        @Override
        public void returnConversationListFail(String error, int errorCode) {
            if (getActivity() != null) {
                swipeRefreshLayout.setRefreshing(false);
            }
        }

        @Override
        public void returnSetConversationStickSuccess(String id, boolean isStick) {
            LoadingDialog.dimissDlg(loadingDlg);
            ConversationCacheUtils.setConversationStick(MyApplication.getInstance(), id, isStick);
            sortConversationList();
        }

        @Override
        public void returnSetConversationStickFail(String error, int errorCode) {
            LoadingDialog.dimissDlg(loadingDlg);
            WebServiceMiddleUtils.hand(MyApplication.getInstance(), error, errorCode);
        }

//        @Override
//        public void returnSetConversationHideSuccess(final String id, boolean isHide) {
//            LoadingDialog.dimissDlg(loadingDlg);
//            new Thread(new Runnable() {
//                @Override
//                public void run() {
//                    ConversationCacheUtils.updateConversationHide(MyApplication.getInstance(), id, true);
//                    MessageCacheUtil.setChannelMessageRead(MyApplication.getInstance(), id);
//                }
//            }).start();
//            int index = displayUIConversationList.indexOf(new UIConversation(id));
//            if (index != -1) {
//                long unReadCount = displayUIConversationList.get(index).getUnReadCount();
//                displayUIConversationList.remove(index);
//                conversationAdapter.setData(displayUIConversationList);
//                conversationAdapter.notifyRealItemRemoved(index);
//                if (unReadCount > 0) {
//                    WSAPIService.getInstance().setChannelMessgeStateRead(id);
//                }
//            }
//        }
//
//        @Override
//        public void returnSetConversationHideFail(String error, int errorCode) {
//            LoadingDialog.dimissDlg(loadingDlg);
//            WebServiceMiddleUtils.hand(MyApplication.getInstance(), error, errorCode);
//        }
    }


}
