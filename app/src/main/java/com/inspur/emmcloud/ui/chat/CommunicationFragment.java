package com.inspur.emmcloud.ui.chat;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.net.NetworkInfo;
import android.os.Bundle;
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
import com.inspur.emmcloud.basemodule.application.BaseApplication;
import com.inspur.emmcloud.basemodule.bean.EventMessage;
import com.inspur.emmcloud.basemodule.bean.SimpleEventMessage;
import com.inspur.emmcloud.basemodule.config.Constant;
import com.inspur.emmcloud.basemodule.config.MyAppConfig;
import com.inspur.emmcloud.basemodule.ui.BaseFragment;
import com.inspur.emmcloud.basemodule.util.AppUtils;
import com.inspur.emmcloud.basemodule.util.CheckingNetStateUtils;
import com.inspur.emmcloud.basemodule.util.DownLoaderUtils;
import com.inspur.emmcloud.basemodule.util.FileUtils;
import com.inspur.emmcloud.basemodule.util.NetUtils;
import com.inspur.emmcloud.basemodule.util.PVCollectModelCacheUtils;
import com.inspur.emmcloud.basemodule.util.PreferencesByUserAndTanentUtils;
import com.inspur.emmcloud.basemodule.util.ScanQrCodeUtils;
import com.inspur.emmcloud.basemodule.util.WebServiceMiddleUtils;
import com.inspur.emmcloud.basemodule.util.WebServiceRouterManager;
import com.inspur.emmcloud.bean.WSCommandBatch;
import com.inspur.emmcloud.bean.chat.ChannelMessageReadStateResult;
import com.inspur.emmcloud.bean.chat.ChannelMessageSet;
import com.inspur.emmcloud.bean.chat.GetConversationListResult;
import com.inspur.emmcloud.bean.chat.GetCreateSingleChannelResult;
import com.inspur.emmcloud.bean.chat.GetOfflineMessageListResult;
import com.inspur.emmcloud.bean.chat.GetRecentMessageListResult;
import com.inspur.emmcloud.bean.chat.GetVoiceAndVideoResult;
import com.inspur.emmcloud.bean.chat.Message;
import com.inspur.emmcloud.bean.chat.UIConversation;
import com.inspur.emmcloud.bean.chat.WSCommand;
import com.inspur.emmcloud.bean.system.EmmAction;
import com.inspur.emmcloud.bean.system.GetAppMainTabResult;
import com.inspur.emmcloud.bean.system.MainTabProperty;
import com.inspur.emmcloud.bean.system.MainTabResult;
import com.inspur.emmcloud.componentservice.communication.Conversation;
import com.inspur.emmcloud.componentservice.communication.OnCreateDirectConversationListener;
import com.inspur.emmcloud.componentservice.communication.OnCreateGroupConversationListener;
import com.inspur.emmcloud.push.WebSocketPush;
import com.inspur.emmcloud.ui.contact.ContactSearchActivity;
import com.inspur.emmcloud.ui.contact.ContactSearchFragment;
import com.inspur.emmcloud.ui.mine.setting.NetWorkStateDetailActivity;
import com.inspur.emmcloud.util.privates.AppTabUtils;
import com.inspur.emmcloud.util.privates.ChatCreateUtils;
import com.inspur.emmcloud.util.privates.ConversationCreateUtils;
import com.inspur.emmcloud.util.privates.ConversationGroupIconUtils;
import com.inspur.emmcloud.util.privates.MessageSendManager;
import com.inspur.emmcloud.util.privates.UriUtils;
import com.inspur.emmcloud.util.privates.VoiceCommunicationManager;
import com.inspur.emmcloud.util.privates.cache.ConversationCacheUtils;
import com.inspur.emmcloud.util.privates.cache.MessageCacheUtil;
import com.inspur.emmcloud.util.privates.cache.MessageMatheSetCacheUtils;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;
import io.socket.client.Socket;


/**
 * 沟通页面
 */
public class CommunicationFragment extends BaseFragment {

    private static final int CREATE_CHANNEL_GROUP = 1;
    private static final int REQUEST_SCAN_LOGIN_QRCODE_RESULT = 5;
    //代表最近消息或者离线消息获取成功
    private static final String FLAG_GET_MESSAGE_SUCCESS = "get_message_success";

    @BindView(R.id.refresh_layout)
    SwipeRefreshLayout swipeRefreshLayout;
    @BindView(R.id.rcv_conversation)
    RecyclerView conversionRecycleView;
    @BindView(R.id.header_text)
    TextView titleText;
    @BindView(R.id.rl_no_chat)
    RelativeLayout noDataLayout;
    @BindView(R.id.more_function_list_img)
    ImageView headerFunctionOptionImg;
    @BindView(R.id.contact_img)
    ImageView contactImg;
    private ChatAPIService apiService;
    private List<UIConversation> displayUIConversationList = new ArrayList<>();
    private ConversationAdapter conversationAdapter;
    private CommunicationFragmentReceiver receiver;
    private boolean isGroupIconCreate = false;
    private PopupWindow popupWindow;
    private boolean isFirstConnectWebsocket = true;//判断是否第一次连上websocket
    private LoadingDialog loadingDlg;
    private CheckingNetStateUtils checkingNetStateUtils;
    private OnClickListener onViewClickListener = new OnClickListener() {

        @Override
        public void onClick(View v) {
            // TODO Auto-generated method stub
            switch (v.getId()) {
                case R.id.message_create_group_layout:
                    Intent contactIntent = new Intent();
                    contactIntent.putExtra(ContactSearchFragment.EXTRA_TYPE, 2);
                    contactIntent.putExtra(ContactSearchFragment.EXTRA_MULTI_SELECT, true);
                    contactIntent.putExtra(ContactSearchFragment.EXTRA_TITLE,
                            getActivity().getString(R.string.message_create_group));
                    contactIntent.setClass(getActivity(), ContactSearchActivity.class);
                    startActivityForResult(contactIntent, CREATE_CHANNEL_GROUP);
                    popupWindow.dismiss();
                    break;
                case R.id.message_scan_layout:
                    AppUtils.openScanCode(CommunicationFragment.this, REQUEST_SCAN_LOGIN_QRCODE_RESULT);
                    popupWindow.dismiss();
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
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        if (!hidden) {
            setFragmentStatusBarCommon();
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
        View view = inflater.inflate(R.layout.fragment_communication, container, false);
        unbinder = ButterKnife.bind(this, view);
        setLastMessageId();
        initView();
        sortConversationList();// 对Channel 进行排序
        registerMessageFragmentReceiver();
        getConversationList();
        setHeaderFunctionOptions(null);
        checkingNetStateUtils = new CheckingNetStateUtils(getContext(), NetUtils.pingUrls, NetUtils.httpUrls);
        //将此句挪到此处，为了防止广播注册太晚接收不到WS状态，这里重新获取下
        showSocketStatusInTitle(WebSocketPush.getInstance().getWebsocketStatus());
        return view;
    }

    /**
     * 切换tab实现网络状态监测
     */
    @Override
    public void onResume() {
        super.onResume();
        setFragmentStatusBarCommon();
        checkingNetStateUtils.getNetStateResult(5);
    }

    private void initView() {
        // TODO Auto-generated method stub
        apiService = new ChatAPIService(getActivity());
        apiService.setAPIInterface(new WebService());
        swipeRefreshLayout.setColorSchemeColors(getResources().getColor(R.color.header_bg_blue), getResources().getColor(R.color.header_bg_blue));
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                WebSocketPush.getInstance().startWebSocket();
                getConversationList();
                getMessage();
            }
        });
        initRecycleView();
        loadingDlg = new LoadingDialog(getActivity());
    }

    @OnClick({R.id.more_function_list_img, R.id.contact_img, R.id.tv_search_contact})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.more_function_list_img:
                showPopupWindow();
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
            case R.id.tv_search_contact:
                IntentUtils.startActivity(getActivity(), SearchActivity.class);
                break;
        }

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
     * 初始化ListView
     */
    private void initRecycleView() {
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity());
        conversionRecycleView.setLayoutManager(linearLayoutManager);
        conversationAdapter = new ConversationAdapter(getActivity(), displayUIConversationList);
        conversationAdapter.setAdapterListener(new ConversationAdapter.AdapterListener() {
            @Override
            public void onItemClick(View view, UIConversation uiConversation) {
                try {
                    Conversation conversation = uiConversation.getConversation();
                    String type = conversation.getType();
                    if (type.equals(Conversation.TYPE_CAST) || type.equals(Conversation.TYPE_DIRECT) ||
                            type.equals(Conversation.TYPE_GROUP) || type.equals(Conversation.TYPE_TRANSFER)) {
                        Bundle bundle = new Bundle();
                        String conversationName = conversation.getType().equals(Conversation.TYPE_TRANSFER) ?
                                getActivity().getString(R.string.chat_file_transfer) : conversation.getName();
                        conversation.setName(conversationName);
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
                    setConversationRead(uiConversation);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public boolean onItemLongClick(View view, UIConversation uiConversation) {
                showConversationOperationDlg(uiConversation);
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
    public void onPause() {
        super.onPause();
        if (popupWindow != null && popupWindow.isShowing()) {
            popupWindow.dismiss();
        }
    }

    /**
     * 通讯录和创建群组，扫一扫合并
     */
    private void showPopupWindow() {
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
        popupWindow.showAsDropDown(headerFunctionOptionImg);

    }


    /**
     * 为单个群组创建头像
     *
     * @param conversation
     */
    private void createSingleGroupIcon(Conversation conversation) {
        List<Conversation> conversationList = new ArrayList<>();
        conversationList.add(conversation);
        createGroupIcon(conversationList);
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

    private void sortConversationList() {
        sortConversationList(null);
    }

    /**
     * ConversationList排序和显示
     *
     * @param changedConversation 变更的Conversation，如果changedConversation为null,则刷新全部conversation数据
     */
    private void sortConversationList(final Conversation changedConversation) {
        // TODO Auto-generated method stub
        Observable.create(new ObservableOnSubscribe<List<UIConversation>>() {
            @Override
            public void subscribe(ObservableEmitter<List<UIConversation>> emitter) throws Exception {
                List<UIConversation> uiConversationList = getUIConversationList(changedConversation);
                Collections.sort(uiConversationList, new UIConversation().new SortComparator());
                emitter.onNext(uiConversationList);
            }
        }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<List<UIConversation>>() {
                    @Override
                    public void accept(List<UIConversation> uiConversationList) throws Exception {
                        displayUIConversationList = uiConversationList;
                        conversationAdapter.setData(displayUIConversationList);
                        conversationAdapter.notifyDataSetChanged();
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) throws Exception {
                    }
                });

    }

    public List<UIConversation> getUIConversationList(Conversation changedConversation) {
        synchronized (this) {
            List<UIConversation> uiConversationList = new ArrayList<>();
            if (changedConversation == null) {
                List<Conversation> conversationList = ConversationCacheUtils.getConversationList(MyApplication.getInstance());
                if (conversationList.size() > 0) {
                    //Conversation存储，主要存储其lastUpdate字段，便于便于后续获取Conversation排序
                    uiConversationList = UIConversation.conversationList2UIConversationList(conversationList);
                    ConversationCacheUtils.updateConversationList(MyApplication.getInstance(), conversationList, "lastUpdate");
                    Iterator<UIConversation> it = uiConversationList.iterator();
                    while (it.hasNext()) {
                        UIConversation uiConversation = it.next();
                        if (!isConversationShow(uiConversation)) {
                            it.remove();
                            continue;
                        }

                    }
                }
            } else {
                uiConversationList.addAll(displayUIConversationList);
                UIConversation uiConversation = new UIConversation(changedConversation);
                ConversationCacheUtils.updateConversation(BaseApplication.getInstance(), changedConversation, "lastUpdate");
                uiConversationList.remove(uiConversation);
                if (isConversationShow(uiConversation)) {
                    uiConversationList.add(uiConversation);
                }

            }
            return uiConversationList;
        }
    }

    /**
     * 判断UIConversation是否应该显示在会话列表中
     *
     * @param uiConversation
     * @return
     */
    private boolean isConversationShow(UIConversation uiConversation) {
        Conversation conversation = uiConversation.getConversation();
        if (conversation.isHide()) {
            if (uiConversation.getUnReadCount() != 0) {
                conversation.setHide(false);
                conversation.setDraft(getDraftWords(conversation));
                ConversationCacheUtils.saveConversation(MyApplication.getInstance(), conversation);
            } else {
                return false;
            }
        }
        if (uiConversation.getMessageList().size() == 0) {
            //当会话内没有消息时，如果是单聊或者不是owner的群聊，则进行隐藏
            if (conversation.getType().equals(Conversation.TYPE_DIRECT) ||
                    (conversation.getType().equals(Conversation.TYPE_GROUP) && !conversation.getOwner().equals(MyApplication.getInstance().getUid()))) {
                return false;
            }
        }
        uiConversation.getConversation().setDraft(getDraftWords(uiConversation.getConversation()));
        return true;
    }


    private void cacheMessageList(final List<Message> messageList, final List<ChannelMessageSet> channelMessageSetList) {
        if (messageList == null || messageList.size() == 0) {
            PreferencesByUserAndTanentUtils.putString(MyApplication.getInstance(), Constant.PREF_GET_OFFLINE_LAST_MID, FLAG_GET_MESSAGE_SUCCESS);
            MessageSendManager.getInstance().resendMessageAfterWSOnline();
        } else {
            Observable.create(new ObservableOnSubscribe<Boolean>() {
                @Override
                public void subscribe(ObservableEmitter<Boolean> emitter) throws Exception {
                    boolean hasMessageInCurrentChannel = false;
                    if (messageList != null && messageList.size() > 0) {

                        //将当前所处频道的消息存为已读
                        if (!StringUtils.isBlank(MyApplication.getInstance().getCurrentChannelCid())) {
                            for (Message message : messageList) {
                                if (message.getChannel().equals(MyApplication.getInstance().getCurrentChannelCid())) {
                                    message.setRead(Message.MESSAGE_READ);
                                    hasMessageInCurrentChannel = true;
                                }
                            }
                        }

                        MessageCacheUtil.handleRealMessage(getActivity(), messageList, null, "", false);// 获取的消息需要缓存
                        if (channelMessageSetList != null && channelMessageSetList.size() > 0) {
                            for (ChannelMessageSet channelMessageSet : channelMessageSetList) {
                                MessageMatheSetCacheUtils.add(MyApplication.getInstance(), channelMessageSet.getCid(), channelMessageSet.getMatheSet());
                            }
                        }
                    }
                    emitter.onNext(hasMessageInCurrentChannel);
                }
            }).subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new Consumer<Boolean>() {
                        @Override
                        public void accept(Boolean hasMessageInCurrentChannel) throws Exception {
                            PreferencesByUserAndTanentUtils.putString(MyApplication.getInstance(), Constant.PREF_GET_OFFLINE_LAST_MID, FLAG_GET_MESSAGE_SUCCESS);
                            if (hasMessageInCurrentChannel) {
                                EventBus.getDefault().post(new SimpleEventMessage(Constant.EVENTBUS_TAG_CURRENT_CHANNEL_OFFLINE_MESSAGE));
                            }
                            sortConversationList();
                            MessageSendManager.getInstance().resendMessageAfterWSOnline();
                        }
                    }, new Consumer<Throwable>() {
                        @Override
                        public void accept(Throwable throwable) throws Exception {

                        }
                    });
        }

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


    /**
     * 显示websocket的连接状态
     *
     * @param socketStatus
     */
    private void showSocketStatusInTitle(String socketStatus) {
        if (socketStatus.equals(Socket.EVENT_CONNECTING)) {
            titleText.setText(R.string.socket_connecting);
        } else if (socketStatus.equals(Socket.EVENT_CONNECT)) {
            getMessage();
            //当断开以后连接成功(非第一次连接上)后重新拉取一遍消息
            if (!isFirstConnectWebsocket) {
                getConversationList();
            }
            isFirstConnectWebsocket = false;
            String appTabs = PreferencesByUserAndTanentUtils.getString(getActivity(), Constant.PREF_APP_TAB_BAR_INFO_CURRENT, "");
            if (!StringUtils.isBlank(appTabs)) {
                titleText.setText(AppTabUtils.getTabTitle(getActivity(), getClass().getSimpleName()));
            } else {
                titleText.setText(R.string.communicate);
            }
        } else if (socketStatus.equals(Socket.EVENT_DISCONNECT) || socketStatus.equals(Socket.EVENT_CONNECT_ERROR)) {
            setLastMessageId();
            titleText.setText(R.string.socket_close);
        }
    }

    /**
     * 将所有频道的消息置为已读
     */
    private void setAllConversationRead() {
        // TODO Auto-generated method stub
        Observable.create(new ObservableOnSubscribe<Void>() {
            @Override
            public void subscribe(ObservableEmitter<Void> emitter) throws Exception {
                MessageCacheUtil.setAllMessageRead(MyApplication.getInstance());
            }
        }).subscribeOn(Schedulers.io()).subscribe();
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
    private void setConversationRead(final UIConversation uiConversation) {
        if (uiConversation.getUnReadCount() > 0) {
            WSAPIService.getInstance().setChannelMessgeStateRead(uiConversation.getId());
            Observable.create(new ObservableOnSubscribe<Void>() {
                @Override
                public void subscribe(ObservableEmitter<Void> emitter) throws Exception {
                    MessageCacheUtil.setChannelMessageRead(MyApplication.getInstance(), uiConversation.getId());
                }
            }).subscribeOn(Schedulers.io()).subscribe();
            int position = displayUIConversationList.indexOf(uiConversation);
            if (position != -1) {
                displayUIConversationList.get(position).setUnReadCount(0);
                conversationAdapter.setData(displayUIConversationList);
                conversationAdapter.notifyRealItemChanged(position);
            }

        }
    }

    /**
     * 处理批量命令消息
     *
     * @param wsCommandBatch
     */
    private void handCommandBatch(final WSCommandBatch wsCommandBatch) {
        Observable.create(new ObservableOnSubscribe<List<Message>>() {
            @Override
            public void subscribe(ObservableEmitter<List<Message>> emitter) throws Exception {
                List<WSCommand> wsCommandList = wsCommandBatch.getWsCommandList();
                List<WSCommand> recallMessageWsCommandList = new ArrayList<>();
                List<String> recallMessageMidList = new ArrayList<>();
                List<Message> recallMessageMidListForCurrentChannel = new ArrayList<>();
                for (WSCommand wsCommand : wsCommandList) {
                    if (wsCommand.getAction().equals("client.chat.message.recall")) {
                        recallMessageWsCommandList.add(wsCommand);
                        String recallMessageParams = wsCommand.getParams();
                        String mid = JSONUtils.getString(recallMessageParams, "messageId", "");
                        recallMessageMidList.add(mid);
                    }
                }
                List<Message> recallMessageList = MessageCacheUtil.getMessageListWithNoRecall(BaseApplication.getInstance(), recallMessageMidList);
                for (Message message : recallMessageList) {
                    for (WSCommand wsCommand : wsCommandList) {
                        String recallMessageParams = wsCommand.getParams();
                        String mid = JSONUtils.getString(recallMessageParams, "messageId", "");
                        if (message.getId().equals(mid)) {
                            message.setRecallFrom(wsCommand.getFrom());
                            message.setRead(Message.MESSAGE_READ);
                            break;
                        }
                    }
                    if (message.getChannel().equals(BaseApplication.getInstance().getCurrentChannelCid())) {
                        recallMessageMidListForCurrentChannel.add(message);
                    }
                }
                MessageCacheUtil.saveMessageList(BaseApplication.getInstance(), recallMessageList);
                emitter.onNext(recallMessageMidListForCurrentChannel);
            }
        }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<List<Message>>() {
                    @Override
                    public void accept(List<Message> messageList) throws Exception {
                        if (messageList.size() > 0) {
                            EventBus.getDefault().post(new SimpleEventMessage(Constant.EVENTBUS_TAG_CURRENT_CHANNEL_COMMAND_BATCH_MESSAGE, messageList));
                        }
                        sortConversationList();
                        WSAPIService.getInstance().commandBatchFinishCallback(wsCommandBatch);
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) throws Exception {

                    }
                });

    }


    /**
     * 撤回消息
     *
     * @param wsCommand
     */
    private void recallMessage(WSCommand wsCommand) {
        String recallMessageParams = wsCommand.getParams();
        String cid = wsCommand.getChannel();
        String mid = JSONUtils.getString(recallMessageParams, "messageId", "");
        Message message = MessageCacheUtil.getMessageByMid(BaseApplication.getInstance(), mid);
        if (message != null) {
            message.setRecallFrom(wsCommand.getFrom());
            message.setRead(Message.MESSAGE_READ);
            MessageCacheUtil.saveMessage(BaseApplication.getInstance(), message);
            notifyConversationMessageDataChanged(cid);
        }
        if (BaseApplication.getInstance().getCurrentChannelCid().equals(cid)) {
            EventBus.getDefault().post(new SimpleEventMessage(Constant.EVENTBUS_TAG_CURRENT_CHANNEL_RECALL_MESSAGE, message));
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
        EventBus.getDefault().unregister(this);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        // TODO Auto-generated method stub
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK
                && requestCode == CREATE_CHANNEL_GROUP) {
            // 创建群组
            String searchResult = data.getExtras().getString("searchResult");
            try {
                JSONObject searchResultObj = new JSONObject(searchResult);
                JSONArray peopleArray = searchResultObj.getJSONArray("people");

                if (peopleArray.length() > 0
                        && NetUtils.isNetworkConnected(MyApplication.getInstance())) {
                    if (peopleArray.length() == 1) {    //单聊
                        String userOrChannelId = "";
                        if (peopleArray.length() > 0) {
                            JSONObject peopleObj = peopleArray.getJSONObject(0);
                            userOrChannelId = peopleObj.getString("pid");
                        }
                        createDirectChannel(userOrChannelId);
                    } else if (peopleArray.length() > 1) {        //大于2人群聊
                        createGroupChannel(peopleArray);
                    }
                }
            } catch (JSONException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
                ToastUtils.show(getActivity(),
                        getActivity().getString(R.string.creat_group_fail));
            }
        } else if ((resultCode == Activity.RESULT_OK) && (requestCode == REQUEST_SCAN_LOGIN_QRCODE_RESULT)) {
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
     * 打开channel
     */
    private void startChannelActivity(String cid) {
        Bundle bundle = new Bundle();
        bundle.putString("cid", cid);
        IntentUtils.startActivity(getActivity(), WebServiceRouterManager.getInstance().isV0VersionChat() ?
                ChannelV0Activity.class : ConversationActivity.class, bundle, false);
    }

    /**
     * 创建单聊
     *
     * @param uid
     */
    private void createDirectChannel(String uid) {
        if (WebServiceRouterManager.getInstance().isV1xVersionChat()) {
            new ConversationCreateUtils().createDirectConversation(getActivity(), uid,
                    new OnCreateDirectConversationListener() {
                        @Override
                        public void createDirectConversationSuccess(Conversation conversation) {
                            startChannelActivity(conversation.getId());
                        }

                        @Override
                        public void createDirectConversationFail() {

                        }
                    });
        } else {
            new ChatCreateUtils().createDirectChannel(getActivity(), uid,
                    new ChatCreateUtils.OnCreateDirectChannelListener() {
                        @Override
                        public void createDirectChannelSuccess(GetCreateSingleChannelResult getCreateSingleChannelResult) {
                            startChannelActivity(getCreateSingleChannelResult.getCid());
                        }

                        @Override
                        public void createDirectChannelFail() {
                        }
                    });
        }

    }

    /**
     * 创建群组
     *
     * @param peopleArray
     */
    private void createGroupChannel(JSONArray peopleArray) {
        // TODO Auto-generated method stub
        new ConversationCreateUtils().createGroupConversation(getActivity(), peopleArray,
                new OnCreateGroupConversationListener() {

                    @Override
                    public void createGroupConversationSuccess(Conversation conversation) {
                        Bundle bundle = new Bundle();
                        bundle.putSerializable(ConversationActivity.EXTRA_CONVERSATION, conversation);
                        IntentUtils.startActivity(getActivity(), ConversationActivity.class, bundle);
                        sortConversationList(conversation);
                        createSingleGroupIcon(conversation);
                    }

                    @Override
                    public void createGroupConversationFail() {

                    }
                });
    }


    private void notifyConversationMessageDataChanged(String cid) {
        Conversation conversation = null;
        for (UIConversation uiConversation : displayUIConversationList) {
            if (uiConversation.getId().equals(cid)) {
                conversation = uiConversation.getConversation();
                break;
            }
        }
        if (conversation == null) {
            conversation = ConversationCacheUtils.getConversation(BaseApplication.getInstance(), cid);
        }
        if (conversation != null) {
            sortConversationList(conversation);
        }

    }


    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onReceiveSimpleEventMessage(SimpleEventMessage eventMessage) {
        Conversation conversation = null;
        int index = -1;
        switch (eventMessage.getAction()) {
            case Constant.EVENTBUS_TAG_REFRESH_CONVERSATION_ADAPTER:
                conversationAdapter.notifyDataSetChanged();
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
                sortConversationList(conversation);
                createSingleGroupIcon(conversation);
                break;
            case Constant.EVENTBUS_TAG_RECALL_MESSAGE:
                WSCommand wsCommand = (WSCommand) eventMessage.getMessageObj();
                recallMessage(wsCommand);
                WSAPIService.getInstance().commandFinishCallback(wsCommand);
                break;
            case Constant.EVENTBUS_TAG_COMMAND_BATCH_MESSAGE:
                WSCommandBatch wsCommandBatch = (WSCommandBatch) eventMessage.getMessageObj();
                handCommandBatch(wsCommandBatch);
                break;
            case Constant.EVENTBUS_TAG_CONVERSATION_SELF_DATA_CHANGED:
                conversation = (Conversation) eventMessage.getMessageObj();
                sortConversationList(conversation);
                break;
            case Constant.EVENTBUS_TAG_CONVERSATION_MESSAGE_DATA_CHANGED:
                String cid = (String) eventMessage.getMessageObj();
                notifyConversationMessageDataChanged(cid);
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
                MessageSendManager.getInstance().onMessageSendSuccess(receivedWSMessage);
                //验重处理
                if (MessageCacheUtil.getMessageByMid(MyApplication.getInstance(), receivedWSMessage.getId()) == null) {
                    if (MyApplication.getInstance().getCurrentChannelCid().equals(receivedWSMessage.getChannel())) {
                        receivedWSMessage.setRead(Message.MESSAGE_READ);
                    }
                    MessageCacheUtil.handleRealMessage(MyApplication.getInstance(), receivedWSMessage);
                    //如果是音频消息，需要检查本地是否有音频文件，没有则下载
                    if (receivedWSMessage.getType().equals(Message.MESSAGE_TYPE_MEDIA_VOICE)) {
                        String fileSavePath = MyAppConfig.getCacheVoiceFilePath(receivedWSMessage.getChannel(), receivedWSMessage.getId());
                        if (!FileUtils.isFileExist(fileSavePath)) {
                            String source = APIUri.getChatVoiceFileResouceUrl(receivedWSMessage.getChannel(), receivedWSMessage.getMsgContentMediaVoice().getMedia());
                            new DownLoaderUtils().startDownLoad(source, fileSavePath, null);
                        }
                    }
                    Conversation conversation = null;
                    for (UIConversation uiConversation : displayUIConversationList) {
                        if (uiConversation.getId().equals(receivedWSMessage.getChannel())) {
                            conversation = uiConversation.getConversation();
                            break;
                        }
                    }
                    if (conversation == null) {
                        conversation = ConversationCacheUtils.getConversation(MyApplication.getInstance(), receivedWSMessage.getChannel());
                    }
                    if (conversation == null) {
                        getConversationList();
                    } else {
                        if (conversation.isHide()) {
                            conversation.setHide(false);
                            ConversationCacheUtils.setConversationHide(MyApplication.getInstance(), conversation.getId(), false);
                        }
                        notifyConversationMessageDataChanged(conversation.getId());
                    }
                }
            } else {
                //当消息发送失败，已离开此频道时，存储该消息,刷新列表。当正在此频道时，由此频道处理改消息
                Message fakeMessage = MessageCacheUtil.getMessageByMid(MyApplication.getInstance(), eventMessage.getId());
                if (fakeMessage != null) {
                    fakeMessage.setSendStatus(Message.MESSAGE_SEND_FAIL);
                    MessageCacheUtil.saveMessage(MyApplication.getInstance(), fakeMessage);
                    notifyConversationMessageDataChanged(fakeMessage.getChannel());
                }
            }
            if (!StringUtils.isBlank(MyApplication.getInstance().getCurrentChannelCid())) {
                SimpleEventMessage simpleEventMessage = new SimpleEventMessage(Constant.EVENTBUS_TAG_RECERIVER_SINGLE_WS_MESSAGE_CONVERSATION, eventMessage);
                EventBus.getDefault().post(simpleEventMessage);
            }
        }
    }

    //接收到websocket发过来的消息，拨打音视频电话，被呼叫触发
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onReceiveVoiceOrVideoCall(final GetVoiceAndVideoResult getVoiceAndVideoResult) {
        VoiceCommunicationManager.getInstance().onReceiveCommand(getVoiceAndVideoResult);
    }

    //socket断开重连时（如断网联网）会触发此方法
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onReceiveWSOfflineMessage(EventMessage eventMessage) {
        if (eventMessage.getTag().equals(Constant.EVENTBUS_TAG_GET_OFFLINE_WS_MESSAGE)) {
            if (eventMessage.getStatus() == EventMessage.RESULT_OK) {
                //清空离线消息最后一条消息标志位
                String content = eventMessage.getContent();
                GetOfflineMessageListResult getOfflineMessageListResult = new GetOfflineMessageListResult(content);
                List<Message> offlineMessageList = getOfflineMessageListResult.getMessageList();
                cacheMessageList(offlineMessageList, getOfflineMessageListResult.getChannelMessageSetList());
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
    public void onReceiveWSRecentMessage(final EventMessage eventMessage) {
        if (eventMessage.getTag().equals(Constant.EVENTBUS_TAG_GET_CHANNEL_RECENT_MESSAGE)) {
            if (eventMessage.getStatus() == EventMessage.RESULT_OK) {
                //获取最近消息由于消息经常会比较多，所以此处采用线程中解析数据
                Observable.create(new ObservableOnSubscribe<GetRecentMessageListResult>() {
                    @Override
                    public void subscribe(ObservableEmitter<GetRecentMessageListResult> emitter) throws Exception {
                        String content = eventMessage.getContent();
                        GetRecentMessageListResult getRecentMessageListResult = new GetRecentMessageListResult(content);
                        emitter.onNext(getRecentMessageListResult);
                    }
                })
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(new Consumer<GetRecentMessageListResult>() {
                            @Override
                            public void accept(GetRecentMessageListResult getRecentMessageListResult) throws Exception {
                                cacheMessageList(getRecentMessageListResult.getMessageList(), getRecentMessageListResult.getChannelMessageSetList());
                            }
                        });
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
            JSONArray jsonArray = new JSONArray();
            jsonArray.put("private");
            apiService.getConversationList(jsonArray);
        } else {
            swipeRefreshLayout.setRefreshing(false);
        }
    }

    /**
     * 当WebSocket断开后记录本地最后一条正式消息
     * 离线消息获取时使用
     */
    private void setLastMessageId() {
        String lastMessageId = MessageCacheUtil.getLastSuccessMessageId(MyApplication.getInstance());
        if (lastMessageId != null) {
            //如果preferences中还存有离线消息最后一条消息id这个标志代表上一次离线消息没有获取成功，需要从这条消息开始重新获取
            String getOfflineLastMessageId = PreferencesByUserAndTanentUtils.getString(MyApplication.getInstance(), Constant.PREF_GET_OFFLINE_LAST_MID, "");
            if (!StringUtils.isBlank(getOfflineLastMessageId) && !getOfflineLastMessageId.equals(FLAG_GET_MESSAGE_SUCCESS)) {
                lastMessageId = getOfflineLastMessageId;
            }
            PreferencesByUserAndTanentUtils.putString(MyApplication.getInstance(), Constant.PREF_GET_OFFLINE_LAST_MID, lastMessageId);
        } else {
            PreferencesByUserAndTanentUtils.putString(MyApplication.getInstance(), Constant.PREF_GET_OFFLINE_LAST_MID, "");
        }
    }

    /**
     * 获取消息
     */
    public void getMessage() {
        if (NetUtils.isNetworkConnected(MyApplication.getInstance()) && WebSocketPush.getInstance().isSocketConnect()) {
            String lastMessageId = PreferencesByUserAndTanentUtils.getString(MyApplication.getInstance(), Constant.PREF_GET_OFFLINE_LAST_MID, "");
            if (StringUtils.isBlank(lastMessageId)) {
                WSAPIService.getInstance().getChannelRecentMessage();
            } else if (!lastMessageId.equals(FLAG_GET_MESSAGE_SUCCESS)) {
                //获取离线消息
                WSAPIService.getInstance().getOfflineMessage(lastMessageId);
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
        Observable.create(new ObservableOnSubscribe<Void>() {
            @Override
            public void subscribe(ObservableEmitter<Void> emitter) throws Exception {
                ConversationCacheUtils.updateConversationHide(MyApplication.getInstance(), uiConversation.getId(), true);
                MessageCacheUtil.setChannelMessageRead(MyApplication.getInstance(), uiConversation.getId());
            }
        }).subscribeOn(Schedulers.io()).subscribe();
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
    }


    private void cacheConversationList(final GetConversationListResult getConversationListResult) {
        Observable.create(new ObservableOnSubscribe<List<Conversation>>() {
            @Override
            public void subscribe(ObservableEmitter<List<Conversation>> emitter) throws Exception {
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
                if (isGroupIconCreate) {
                    conversationList.removeAll(intersectionConversationList);
                }
                emitter.onNext(conversationList);
            }
        })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(new Consumer<List<Conversation>>() {
                    @Override
                    public void accept(List<Conversation> conversationList) throws Exception {
                        sortConversationList();
                        createGroupIcon(conversationList);
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) throws Exception {
                    }
                });
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
                    getConversationList();
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

    class WebService extends APIInterfaceInstance {
        @Override
        public void returnConversationListSuccess(GetConversationListResult getConversationListResult) {
            if (getActivity() != null) {
                swipeRefreshLayout.setRefreshing(false);
                cacheConversationList(getConversationListResult);
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
