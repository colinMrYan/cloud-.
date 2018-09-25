package com.inspur.emmcloud.ui.chat;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.inspur.emmcloud.MyApplication;
import com.inspur.emmcloud.R;
import com.inspur.emmcloud.adapter.ChannelAdapter;
import com.inspur.emmcloud.api.APIInterfaceInstance;
import com.inspur.emmcloud.api.APIUri;
import com.inspur.emmcloud.api.apiservice.ChatAPIService;
import com.inspur.emmcloud.api.apiservice.WSAPIService;
import com.inspur.emmcloud.bean.chat.Channel;
import com.inspur.emmcloud.bean.chat.ChannelGroup;
import com.inspur.emmcloud.bean.chat.ChannelMessageReadStateResult;
import com.inspur.emmcloud.bean.chat.ChannelMessageSet;
import com.inspur.emmcloud.bean.chat.ChannelOperationInfo;
import com.inspur.emmcloud.bean.chat.Conversation;
import com.inspur.emmcloud.bean.chat.GetConversationListResult;
import com.inspur.emmcloud.bean.chat.GetOfflineMessageListResult;
import com.inspur.emmcloud.bean.chat.GetRecentMessageListResult;
import com.inspur.emmcloud.bean.chat.MatheSet;
import com.inspur.emmcloud.bean.chat.Message;
import com.inspur.emmcloud.bean.chat.UIConversation;
import com.inspur.emmcloud.bean.system.EventMessage;
import com.inspur.emmcloud.bean.system.GetAppMainTabResult;
import com.inspur.emmcloud.bean.system.MainTabProperty;
import com.inspur.emmcloud.bean.system.MainTabResult;
import com.inspur.emmcloud.bean.system.PVCollectModel;
import com.inspur.emmcloud.bean.system.SimpleEventMessage;
import com.inspur.emmcloud.config.Constant;
import com.inspur.emmcloud.config.MyAppConfig;
import com.inspur.emmcloud.push.WebSocketPush;
import com.inspur.emmcloud.ui.contact.ContactSearchActivity;
import com.inspur.emmcloud.ui.contact.ContactSearchFragment;
import com.inspur.emmcloud.util.common.IntentUtils;
import com.inspur.emmcloud.util.common.JSONUtils;
import com.inspur.emmcloud.util.common.NetUtils;
import com.inspur.emmcloud.util.common.StringUtils;
import com.inspur.emmcloud.util.common.ToastUtils;
import com.inspur.emmcloud.util.privates.AppTabUtils;
import com.inspur.emmcloud.util.privates.AppUtils;
import com.inspur.emmcloud.util.privates.ChannelGroupIconUtils;
import com.inspur.emmcloud.util.privates.ChatCreateUtils;
import com.inspur.emmcloud.util.privates.ChatCreateUtils.OnCreateGroupChannelListener;
import com.inspur.emmcloud.util.privates.CommunicationUtils;
import com.inspur.emmcloud.util.privates.DownLoaderUtils;
import com.inspur.emmcloud.util.privates.PreferencesByUserAndTanentUtils;
import com.inspur.emmcloud.util.privates.ScanQrCodeUtils;
import com.inspur.emmcloud.util.privates.WebServiceMiddleUtils;
import com.inspur.emmcloud.util.privates.cache.ChannelCacheUtils;
import com.inspur.emmcloud.util.privates.cache.ChannelGroupCacheUtils;
import com.inspur.emmcloud.util.privates.cache.ChannelOperationCacheUtils;
import com.inspur.emmcloud.util.privates.cache.ConversationCacheUtils;
import com.inspur.emmcloud.util.privates.cache.MessageCacheUtil;
import com.inspur.emmcloud.util.privates.cache.MessageMatheSetCacheUtils;
import com.inspur.emmcloud.util.privates.cache.PVCollectModelCacheUtils;
import com.inspur.emmcloud.widget.WeakThread;
import com.inspur.imp.plugin.barcode.scan.CaptureActivity;
import com.qmuiteam.qmui.widget.dialog.QMUIDialog;

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

import io.socket.client.Socket;

import static android.app.Activity.RESULT_OK;

/**
 * 沟通页面
 */
public class CommunicationFragmentNew extends Fragment {

    private static final int CREAT_CHANNEL_GROUP = 1;
    private static final int RERESH_GROUP_ICON = 2;
    private static final int SORT_CHANNEL_COMPLETE = 3;
    private static final int SORT_CHANNEL_LIST = 4;
    private static final int REQUEST_SCAN_LOGIN_QRCODE_RESULT = 5;
    private static final int CACHE_CONVERSATION_LIST_SUCCESS = 6;
    private View rootView;
    private RecyclerView conversionRecycleView;
    private SwipeRefreshLayout swipeRefreshLayout;
    private ChatAPIService apiService;
    private List<Channel> displayChannelList = new ArrayList<>();
    private ChannelAdapter adapter;
    private Handler handler;
    private CommunicationFragmentReceiver receiver;
    private TextView titleText;
    private boolean isHaveCreatGroupIcon = false;
    private PopupWindow popupWindow;
    private boolean isFirstConnectWebsockt = true;//判断是否第一次连上websockt

    @Override
    public void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        EventBus.getDefault().register(this);
        initView();
        sortConversationList();// 对Channel 进行排序
        getMessage();
        registerMessageFragmentReceiver();
        getConversationList();
        updateHeaderFunctionBtn(null);
    }

    private void initView() {
        // TODO Auto-generated method stub
        handMessage();
        apiService = new ChatAPIService(getActivity());
        apiService.setAPIInterface(new WebService());
        rootView = LayoutInflater.from(getActivity()).inflate(R.layout.fragment_communication, null);
        (rootView.findViewById(R.id.more_function_list_img))
                .setOnClickListener(onViewClickListener);
        (rootView.findViewById(R.id.contact_img))
                .setOnClickListener(onViewClickListener);
        titleText = (TextView) rootView.findViewById(R.id.header_text);
        initPullRefreshLayout();
        initListView();
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
        swipeRefreshLayout.setColorSchemeColors(getResources().getColor(R.color.header_bg), getResources().getColor(R.color.header_bg));
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
    private void initListView() {
        conversionRecycleView = (RecyclerView) rootView.findViewById(R.id.rcv_conversation);
//        msgListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
//
//            @Override
//            public void onItemClick(AdapterView<?> parent, View view,
//                                    int position, long id) {
//                // TODO Auto-generated method stub
//                Channel channel = displayChannelList.get(position);
//                String channelType = channel.getType();
//                if (channelType.equals("GROUP") || channelType.equals("DIRECT") || channelType.equals("SERVICE")) {
//                    Bundle bundle = new Bundle();
//                    bundle.putString("title", channel.getTitle());
//                    bundle.putString("cid", channel.getCid());
//                    bundle.putString("channelType", channelType);
//                    IntentUtils.startActivity(getActivity(),
//                            ChannelActivity.class, bundle);
//                } else {
//                    ToastUtils.show(MyApplication.getInstance(),
//                            R.string.not_support_open_channel);
//                }
//                setChannelAllMsgRead(channel);
//                refreshIndexNotify();
//            }
//
//        });
//
//        msgListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
//
//            @Override
//            public boolean onItemLongClick(AdapterView<?> parent, View view,
//                                           int position, long id) {
//                // TODO Auto-generated method stub
//                showChannelOperationDlg(position);
//                return true;
//            }
//
//        });
    }

    /**
     * 根据服务端的配置信息显示和隐藏沟通header上的通讯录和“+”按钮
     *
     * @param getAppMainTabResult
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void updateHeaderFunctionBtn(GetAppMainTabResult getAppMainTabResult) {
        if (getAppMainTabResult != null) {
            ArrayList<MainTabResult> mainTabResultList = getAppMainTabResult.getMainTabPayLoad().getMainTabResultList();
            for (int i = 0; i < mainTabResultList.size(); i++) {
                if (mainTabResultList.get(i).getUri().equals(Constant.APP_TAB_BAR_COMMUNACATE)) {
                    MainTabProperty mainTabProperty = mainTabResultList.get(i).getMainTabProperty();
                    if (mainTabProperty != null) {
                        if (!mainTabProperty.isCanCreate()) {
                            rootView.findViewById(R.id.more_function_list_img).setVisibility(View.GONE);
                        }
                        if (!mainTabProperty.isCanContact()) {
                            rootView.findViewById(R.id.contact_img).setVisibility(View.GONE);
                        }
                    }
                    break;
                }
            }
        }
    }

    /**
     * 记录用户点击的频道
     */
    private void recordUserClickContact() {
        PVCollectModel pvCollectModel = new PVCollectModel("contact", "communicate");
        PVCollectModelCacheUtils.saveCollectModel(getActivity(), pvCollectModel);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        EventBus.getDefault().post(new SimpleEventMessage(Constant.EVENTBUS_TAG_OPEN_DEFALT_TAB, null));
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
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
                    recordUserClickContact();
                    break;
                case R.id.message_create_group_layout:
                    Intent contactIntent = new Intent();
                    contactIntent.putExtra(ContactSearchFragment.EXTRA_TYPE, 2);
                    contactIntent.putExtra(ContactSearchFragment.EXTRA_MULTI_SELECT, true);
                    contactIntent.putExtra(ContactSearchFragment.EXTRA_TITLE,
                            getActivity().getString(R.string.creat_group));
                    contactIntent.setClass(getActivity(), ContactSearchActivity.class);
                    startActivityForResult(contactIntent, CREAT_CHANNEL_GROUP);
                    popupWindow.dismiss();
                    break;
                case R.id.message_scan_layout:
                    Intent scanIntent = new Intent();
                    scanIntent.setClass(getActivity(), CaptureActivity.class);
                    scanIntent.putExtra("from", "CommunicationFragment");
                    startActivityForResult(scanIntent, REQUEST_SCAN_LOGIN_QRCODE_RESULT);
                    popupWindow.dismiss();
                    break;
                default:
                    break;
            }
        }
    };

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
     * @param channelList
     */
    private void createGroupIcon(List<Channel> channelList) {
        if (NetUtils.isNetworkConnected(MyApplication.getInstance(), false)) {
            return;
        }
        if (!MyApplication.getInstance().getIsContactReady()) {
            return;
        }
        isHaveCreatGroupIcon = true;
        ChannelGroupIconUtils.getInstance().create(MyApplication.getInstance(), channelList,
                handler);
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
                    List<UIConversation> uiConversationList = UIConversation.conversationList2UIConversationList(conversationList);
                    List<UIConversation> hideUIConversationList = new ArrayList<>();
                    List<UIConversation> hideUIConversationList = new ArrayList<>();

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };
        sortConversationThread.start();
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
                        if (adapter != null && isCreateNewGroupIcon) {
                            displayData();
                        }
                        break;
                    case SORT_CHANNEL_COMPLETE:
                        List<Channel> channelList = (List<Channel>) msg.obj;
                        displayChannelList.clear();
                        displayChannelList.addAll(channelList);
                        displayData();// 展示数据
                        break;
                    case SORT_CHANNEL_LIST:
                        sortConversationList();
                        break;
                    case CACHE_CONVERSATION_LIST_SUCCESS:
                        sortConversationList();
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
        MessageCacheUtil.saveMessage(MyApplication.getInstance(), receivedWSMessage);
        Long ChannelMessageMatheSetStart = (channelNewMessage == null) ? receivedWSMessage.getCreationDate() : channelNewMessage.getCreationDate();
        MessageMatheSetCacheUtils.add(MyApplication.getInstance(),
                receivedWSMessage.getChannel(),
                new MatheSet(ChannelMessageMatheSetStart, receivedWSMessage.getCreationDate()));
    }

    /**
     * 显示获取的数据
     */
    private void displayData() {
        (rootView
                .findViewById(R.id.rl_no_chat)).setVisibility((displayChannelList.size() == 0) ? View.VISIBLE : View.GONE);
        if (adapter == null) {
            adapter = new ChannelAdapter(MyApplication.getInstance());
            adapter.setDataList(displayChannelList);
            msgListView.setAdapter(adapter);
        } else {
            adapter.setDataList(displayChannelList);
            adapter.notifyDataSetChanged();
        }
        refreshIndexNotify();

    }


    /**
     * 弹出频道操作选择框
     *
     * @param position
     */
    private void showChannelOperationDlg(final int position) {
        // TODO Auto-generated method stub
        final boolean isChannelSetTop = ChannelOperationCacheUtils
                .isChannelSetTop(getActivity(), displayChannelList
                        .get(position).getCid());
        final String[] items = new String[]{getString(isChannelSetTop ? R.string.chanel_cancel_top : R.string.channel_set_top), getString(R.string.channel_hide_chat)};
        new QMUIDialog.MenuDialogBuilder(getActivity())
                .addItems(items, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        Channel channel = displayChannelList.get(position);
                        if (which == 0) {
                            ChannelOperationCacheUtils.setChannelTop(MyApplication.getInstance(),
                                    channel.getCid(), !isChannelSetTop);
                            sortConversationList();
                        } else {
                            ChannelOperationCacheUtils.setChannelHide(
                                    MyApplication.getInstance(), channel.getCid(), true);
                            // 当隐藏会话时，把该会话的所有消息置为已读
                            MessageCacheUtil.setChannelMessageRead(MyApplication.getInstance(), channel.getCid());
                            displayChannelList.remove(position);
                            displayData();
                        }
                    }
                })
                .show();
    }

    /**
     * 设置消息tab页面的小红点（未读消息提醒）的显示
     */
    private void refreshIndexNotify() {
        int unReadCount = 0;
        for (Channel channel : displayChannelList) {
            unReadCount += channel.getUnReadCount();
        }
        EventBus.getDefault().post(new SimpleEventMessage(Constant.EVENTBUS_TAG_SET_ALL_MESSAGE_UNREAD_COUNT, unReadCount));
    }

    class CacheConversationThread extends Thread {
        private GetConversationListResult getConversationListResult;

        public CacheConversationThread(GetConversationListResult getConversationListResult) {
            this.getConversationListResult = getConversationListResult;
        }

        @Override
        public void run() {
            List<Conversation> conversationList = getConversationListResult.getConversationList();
            List<Conversation> cacheConversationList= ConversationCacheUtils.getConversationList(MyApplication.getInstance());
            ConversationCacheUtils.deleteAllConversation(MyApplication.getInstance());
            ConversationCacheUtils.saveConversationList(MyApplication.getInstance(), conversationList);
            if (handler != null){
                handler.sendEmptyMessage(CACHE_CONVERSATION_LIST_SUCCESS);
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
                    isHaveCreatGroupIcon = false;
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
                    setAllChannelMsgRead();
                    break;
                case "websocket_status":
                    String socketStatus = intent.getExtras().getString("status");
                    showSocketStatusInTitle(socketStatus);
                    break;
                case "refresh_adapter":
                    if (adapter != null) {
                        adapter.notifyDataSetChanged();
                    }
                    break;
                case "removeChannelFromUI":
                    String deleteCid = intent.getExtras().getString("cid");
                    ChannelCacheUtils.deleteChannel(MyApplication.getInstance(), deleteCid);
                    removeChannelFromUI(deleteCid);
                    break;
                default:
                    break;
            }
        }

    }

    /**
     * 从ui中移除这个频道
     *
     * @param cid
     */
    private void removeChannelFromUI(String cid) {
        Channel removeChannel = new Channel(cid);
        if (displayChannelList.contains(removeChannel)) {
            displayChannelList.remove(removeChannel);
            adapter.notifyDataSetChanged();
        }
    }

    private void showSocketStatusInTitle(String socketStatus) {
        if (socketStatus.equals("socket_connecting")) {
            titleText.setText(R.string.socket_connecting);
        } else if (socketStatus.equals(Socket.EVENT_CONNECT)) {
            //当断开以后连接成功(非第一次连接上)后重新拉取一遍消息
            if (!isFirstConnectWebsockt) {
                getConversationList();
            }
            getMessage();
            isFirstConnectWebsockt = false;
            String appTabs = PreferencesByUserAndTanentUtils.getString(getActivity(), "app_tabbar_info_current", "");
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
    private void setAllChannelMsgRead() {
        // TODO Auto-generated method stub
        MessageCacheUtil.setAllMessageRead(MyApplication.getInstance());
        for (Channel channel : displayChannelList) {
            channel.setUnReadCount(0);
            WSAPIService.getInstance().setChannelMessgeStateRead(channel.getCid());
        }
        displayData();
    }

    /**
     * 更新Channel的input信息
     *
     * @param searchChannelGroupList
     */
    public void saveChannelInfo(final List<ChannelGroup> searchChannelGroupList) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                List<Channel> channelList = ChannelCacheUtils
                        .getCacheChannelList(getActivity());
                List<ChannelGroup> channelGroupList = new ArrayList<>();
                for (int i = 0; i < searchChannelGroupList.size(); i++) {
                    ChannelGroup channelGroup = searchChannelGroupList.get(i);
                    if (channelGroup.getType().equals("GROUP")) {
                        channelGroupList.add(channelGroup);
                    } else if (channelGroup.getType().equals("SERVICE")) {
                        int index = channelList.indexOf(new Channel(channelGroup.getCid()));
                        if (index != -1) {
                            channelList.get(index).setInputs(channelGroup.getInputs());
                        }

                    }
                }
                ChannelGroupCacheUtils.saveChannelGroupList(MyApplication.getInstance(), channelGroupList);
                ChannelCacheUtils.saveChannelList(MyApplication.getInstance(), channelList);
            }
        }).start();

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
        new ChatCreateUtils().createGroupChannel(getActivity(), peopleArray,
                new OnCreateGroupChannelListener() {

                    @Override
                    public void createGroupChannelSuccess(
                            ChannelGroup channelGroup) {
                        // TODO Auto-generated method stub
                        Bundle bundle = new Bundle();
                        bundle.putString("cid", channelGroup.getCid());
                        bundle.putString("channelType", channelGroup.getType());
                        bundle.putString("title", channelGroup.getChannelName());
                        IntentUtils.startActivity(getActivity(),
                                ChannelActivity.class, bundle);
                        ChannelGroupCacheUtils.saveChannelGroup(getActivity(),
                                channelGroup);
                        getConversationList();
                    }

                    @Override
                    public void createGroupChannelFail() {
                        // TODO Auto-generated method stub

                    }
                });
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
                    MessageCacheUtil.saveMessageList(MyApplication.getInstance(), messageList, null, false); // 获取的消息需要缓存
                    if (channelMessageSetList != null && channelMessageSetList.size() > 0) {
                        for (ChannelMessageSet channelMessageSet : channelMessageSetList) {
                            MessageMatheSetCacheUtils.add(MyApplication.getInstance(), channelMessageSet.getCid(), channelMessageSet.getMatheSet());
                        }
                    }
                    if (handler != null) {
                        handler.sendEmptyMessage(SORT_CHANNEL_LIST);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }


    //接收到websocket发过来的消息
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onReceiveWSMessage(EventMessage eventMessage) {
        if (eventMessage.getTag().equals(Constant.EVENTBUS_TAG_RECERIVER_SINGLE_WS_MESSAGE)) {
            if (eventMessage.getStatus() == 200) {
                String content = eventMessage.getContent();
                JSONObject contentObj = JSONUtils.getJSONObject(content);
                Message receivedWSMessage = new Message(contentObj);
                //验重处理
                if (MessageCacheUtil.getMessageByMid(MyApplication.getInstance(), receivedWSMessage.getId()) == null) {
                    if (MyApplication.getInstance().getCurrentChannelCid().equals(receivedWSMessage.getChannel())) {
                        receivedWSMessage.setRead(1);
                    }
                    if (receivedWSMessage.getType().equals(Message.MESSAGE_TYPE_MEDIA_VOICE)) {
                        String fileSavePath = MyAppConfig.getCacheVoiceFilePath(receivedWSMessage.getChannel(), receivedWSMessage.getId());
                        if (!new File(fileSavePath).exists()) {
                            String source = APIUri.getChatVoiceFileResouceUrl(receivedWSMessage.getChannel(), receivedWSMessage.getMsgContentMediaVoice().getMedia());
                            new DownLoaderUtils().startDownLoad(source, fileSavePath, null);
                        }
                    }
                    Channel receiveMessageChannel = ChannelCacheUtils.getChannel(
                            getActivity(), receivedWSMessage.getChannel());
                    cacheReceiveMessage(receivedWSMessage);
                    if (receiveMessageChannel == null) {
                        getConversationList();
                    } else {
                        sortConversationList();
                    }
                }
            } else {
                WebServiceMiddleUtils.hand(getActivity(), eventMessage.getContent(), eventMessage.getStatus());
            }

        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onReiceveWSOfflineMessage(EventMessage eventMessage) {
        if (eventMessage.getTag().equals(Constant.EVENTBUS_TAG_GET_OFFLINE_WS_MESSAGE)) {
            if (eventMessage.getStatus() == 200) {
                String content = eventMessage.getContent();
                GetOfflineMessageListResult getOfflineMessageListResult = new GetOfflineMessageListResult(content);
                List<Message> offlineMessageList = getOfflineMessageListResult.getMessageList();
                List<Message> currentChannelOfflineMessageList = new ArrayList<>();
                //将当前所处频道的消息存为已读
                if (!StringUtils.isBlank(MyApplication.getInstance().getCurrentChannelCid())) {
                    for (Message message : offlineMessageList) {
                        if (message.getChannel().equals(MyApplication.getInstance().getCurrentChannelCid())) {
                            message.setRead(1);
                            currentChannelOfflineMessageList.add(message);
                        }
                    }
                    if (currentChannelOfflineMessageList.size() > 0) {
                        //将离线消息发送到当前频道
                        EventBus.getDefault().post(offlineMessageList);
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

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onReceiveWSRecentMessage(EventMessage eventMessage) {
        if (eventMessage.getTag().equals(Constant.EVENTBUS_TAG_GET_CHANNEL_RECENT_MESSAGE)) {
            if (eventMessage.getStatus() == 200) {
                String content = eventMessage.getContent();
                GetRecentMessageListResult getRecentMessageListResult = new GetRecentMessageListResult(content);
                new CacheMessageListThread(getRecentMessageListResult.getMessageList(), getRecentMessageListResult.getChannelMessageSetList()).start();
            } else {
                WebServiceMiddleUtils.hand(getActivity(), eventMessage.getContent(), eventMessage.getStatus());
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
            for (Channel channel : displayChannelList) {
                long unReadCount = MessageCacheUtil.getChannelMessageUnreadCount(MyApplication.getInstance(), channel.getCid());
                channel.setUnReadCount(unReadCount);
                displayData();
            }

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

    public void getMessage() {
        if (NetUtils.isNetworkConnected(MyApplication.getInstance()) && WebSocketPush.getInstance().isSocketConnect()) {
            String lastMessageId = MessageCacheUtil.getLastMessageId(MyApplication.getInstance());
            if (lastMessageId != null) {
                //获取离线消息
                WSAPIService.getInstance().getOfflineMessage(lastMessageId);
            } else {
                //获取每个频道最近的15条消息
                WSAPIService.getInstance().getChannelRecentMessage();
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
                WebServiceMiddleUtils.hand(getActivity(), error, errorCode);
                getMessage();
            }
        }

    }


}
