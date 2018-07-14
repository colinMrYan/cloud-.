package com.inspur.emmcloud.ui.chat;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.inspur.emmcloud.MyApplication;
import com.inspur.emmcloud.R;
import com.inspur.emmcloud.adapter.ChannelAdapter;
import com.inspur.emmcloud.api.APIInterfaceInstance;
import com.inspur.emmcloud.api.apiservice.ChatAPIService;
import com.inspur.emmcloud.api.apiservice.WSAPIService;
import com.inspur.emmcloud.bean.chat.Channel;
import com.inspur.emmcloud.bean.chat.ChannelGroup;
import com.inspur.emmcloud.bean.chat.ChannelOperationInfo;
import com.inspur.emmcloud.bean.chat.EventMessageUnReadCount;
import com.inspur.emmcloud.bean.chat.GetChannelListResult;
import com.inspur.emmcloud.bean.chat.GetNewMessagesResult;
import com.inspur.emmcloud.bean.chat.MatheSet;
import com.inspur.emmcloud.bean.chat.Message;
import com.inspur.emmcloud.bean.chat.MessageReadCreationDate;
import com.inspur.emmcloud.bean.contact.GetSearchChannelGroupResult;
import com.inspur.emmcloud.bean.system.EventMessage;
import com.inspur.emmcloud.bean.system.GetAppMainTabResult;
import com.inspur.emmcloud.bean.system.MainTabProperty;
import com.inspur.emmcloud.bean.system.MainTabResult;
import com.inspur.emmcloud.bean.system.PVCollectModel;
import com.inspur.emmcloud.config.Constant;
import com.inspur.emmcloud.push.WebSocketPush;
import com.inspur.emmcloud.ui.IndexActivity;
import com.inspur.emmcloud.ui.contact.ContactSearchActivity;
import com.inspur.emmcloud.ui.contact.ContactSearchFragment;
import com.inspur.emmcloud.util.common.IntentUtils;
import com.inspur.emmcloud.util.common.JSONUtils;
import com.inspur.emmcloud.util.common.NetUtils;
import com.inspur.emmcloud.util.common.PreferencesUtils;
import com.inspur.emmcloud.util.common.StringUtils;
import com.inspur.emmcloud.util.common.ToastUtils;
import com.inspur.emmcloud.util.privates.AppTitleUtils;
import com.inspur.emmcloud.util.privates.AppUtils;
import com.inspur.emmcloud.util.privates.ChannelGroupIconUtils;
import com.inspur.emmcloud.util.privates.ChatCreateUtils;
import com.inspur.emmcloud.util.privates.ChatCreateUtils.OnCreateGroupChannelListener;
import com.inspur.emmcloud.util.privates.CommunicationUtils;
import com.inspur.emmcloud.util.privates.PreferencesByUserAndTanentUtils;
import com.inspur.emmcloud.util.privates.ScanQrCodeUtils;
import com.inspur.emmcloud.util.privates.WebServiceMiddleUtils;
import com.inspur.emmcloud.util.privates.cache.ChannelCacheUtils;
import com.inspur.emmcloud.util.privates.cache.ChannelGroupCacheUtils;
import com.inspur.emmcloud.util.privates.cache.ChannelOperationCacheUtils;
import com.inspur.emmcloud.util.privates.cache.DbCacheUtils;
import com.inspur.emmcloud.util.privates.cache.MessageCacheUtil;
import com.inspur.emmcloud.util.privates.cache.MessageMatheSetCacheUtils;
import com.inspur.emmcloud.util.privates.cache.MessageReadCreationDateCacheUtils;
import com.inspur.emmcloud.util.privates.cache.MsgReadCreationDateCacheUtils;
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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import io.socket.client.Socket;

import static android.app.Activity.RESULT_OK;

/**
 * 沟通页面
 */
public class CommunicationFragment extends Fragment {

    private static final int CREAT_CHANNEL_GROUP = 1;
    private static final int RERESH_GROUP_ICON = 2;
    private static final int SORT_CHANNEL_COMPLETE = 3;
    private static final int SCAN_LOGIN_QRCODE_RESULT = 5;
    private View rootView;
    private ListView msgListView;
    private SwipeRefreshLayout swipeRefreshLayout;
    private ChatAPIService apiService;
    private List<Channel> displayChannelList = new ArrayList<>();
    private ChannelAdapter adapter;
    private Handler handler;
    private CommunicationFragmentReceiver receiver;
    private TextView titleText;
    private boolean isHaveCreatGroupIcon = false;
    private PopupWindow popupWindow;
    private CacheNewMsgTask cacheMsgAsyncTask;
    private CacheChannelTask cacheChannelTask;
    private boolean isFirstConnectWebsockt = true;//判断是否第一次连上websockt

    @Override
    public void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        EventBus.getDefault().register(this);
        initView();
        sortChannelList();// 对Channel 进行排序
        getMessage();
        registerMessageFragmentReceiver();
        getChannelList();
        updateHeaderFunctionBtn(null);
    }

    private void initView() {
        // TODO Auto-generated method stub
        handMessage();
        apiService = new ChatAPIService(getActivity());
        apiService.setAPIInterface(new WebService());
        rootView = LayoutInflater.from(getActivity()).inflate(R.layout.fragment_message, null);
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
                MyApplication.getInstance().startWebSocket(false);
                getChannelList();
                getMessage();
            }
        });
    }

    /**
     * 初始化ListView
     */
    private void initListView() {
        msgListView = (ListView) rootView.findViewById(R.id.msg_list);
        msgListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                // TODO Auto-generated method stub
                Channel channel = displayChannelList.get(position);
                String channelType = channel.getType();
                if (channelType.equals("GROUP") || channelType.equals("DIRECT") || channelType.equals("SERVICE")) {
                    Bundle bundle = new Bundle();
                    bundle.putString("title", channel.getTitle());
                    bundle.putString("cid", channel.getCid());
                    bundle.putString("channelType", channelType);
                    IntentUtils.startActivity(getActivity(),
                            ChannelActivity.class, bundle);
                } else {
                    ToastUtils.show(MyApplication.getInstance(),
                            R.string.not_support_open_channel);
                }
                setChannelAllMsgRead(channel);
                refreshIndexNotify();
            }

        });

        msgListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {

            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view,
                                           int position, long id) {
                // TODO Auto-generated method stub
                showChannelOperationDlg(position);
                return true;
            }

        });
    }

    /**
     * 根据服务端的配置信息显示和隐藏沟通header上的通讯录和“+”按钮
     *
     * @param getAppMainTabResult
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void updateHeaderFunctionBtn(GetAppMainTabResult getAppMainTabResult) {
        if(getAppMainTabResult != null){
            ArrayList<MainTabResult> mainTabResultList = getAppMainTabResult.getMainTabResultList();
            for (int i = 0; i < mainTabResultList.size(); i++) {
                if(mainTabResultList.get(i).getUri().equals(Constant.APP_TAB_BAR_COMMUNACATE)){
                    MainTabProperty mainTabProperty = mainTabResultList.get(i).getMainTabProperty();
                    if (mainTabProperty != null) {
                        if (!mainTabProperty.isCanCreate()) {
                            rootView.findViewById(R.id.more_function_list_img).setVisibility(View.GONE);
                        }
                        if (!mainTabProperty.isCanContact()) {
                            rootView.findViewById(R.id.contact_img).setVisibility(View.GONE);
                        }
                    }
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
        ((IndexActivity) getActivity()).openTargetFragment();
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
                    startActivityForResult(scanIntent, SCAN_LOGIN_QRCODE_RESULT);
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
        // 一个自定义的布局，作为显示的内容
        View contentView = LayoutInflater.from(getActivity())
                .inflate(R.layout.pop_message_window_view, null);
        // 设置按钮的点击事件
        popupWindow = new PopupWindow(contentView,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT, true);
        popupWindow.setTouchable(true);
        popupWindow.setTouchInterceptor(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return false;
                // 这里如果返回true的话，touch事件将被拦截
                // 拦截后 PopupWindow的onTouchEvent不被调用，这样点击外部区域无法dismiss
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
        if (MyApplication.getInstance().getIsContactReady() && NetUtils.isNetworkConnected(MyApplication.getInstance(), false)) {
            isHaveCreatGroupIcon = true;
            ChannelGroupIconUtils.getInstance().create(MyApplication.getInstance(), channelList,
                    handler);
        }
    }


    /**
     * channel 显示排序
     */
    private void sortChannelList() {
        // TODO Auto-generated method stub
        WeakThread weakThread = new WeakThread(getActivity()) {
            @Override
            public void run() {
                super.run();
                try {
                    List<Channel> channelList = ChannelCacheUtils
                            .getCacheChannelList(MyApplication.getInstance());
                    if (channelList.size() > 0) {
                        Iterator<Channel> it = channelList.iterator();
                        //将没有消息的单聊和没有消息的但不是自己创建的群聊隐藏掉
                        while (it.hasNext()) {
                            Channel channel = it.next();
                            List<Message> newMessageList = MessageCacheUtil.getHistoryMessageList(MyApplication.getInstance(), channel.getCid(), null, 15);
                            if (newMessageList.size() == 0 && channel.getType().equals("DIRECT")) {
                                it.remove();
                                continue;
                            }
                            channel.setNewMessageList(MyApplication.getInstance(), newMessageList);
                            channel.setIsSetTop(false);
                            int unReadCount = MessageReadCreationDateCacheUtils.getNotReadMessageCount(
                                    MyApplication.getInstance(), channel.getCid());
                            channel.setUnReadCount(unReadCount);
                            channel.setDisplayTitle(CommunicationUtils.getChannelDisplayTitle(channel));
                        }

                        List<ChannelOperationInfo> hideChannelOpList = ChannelOperationCacheUtils
                                .getHideChannelOpList(MyApplication.getInstance());
                        // 如果隐藏的频道中有未读消息则取消隐藏
                        for (ChannelOperationInfo channelOperationInfo : hideChannelOpList) {
                            int index = channelList.indexOf(new Channel(channelOperationInfo.getCid()));
                            if (index != -1) {
                                Channel channel = channelList.get(index);
                                if (channel.getUnReadCount() != 0) {
                                    ChannelOperationCacheUtils.setChannelHide(
                                            getActivity(), channelOperationInfo.getCid(), false);
                                } else {
                                    channelList.remove(index); // 如果没有未读消息则删除
                                }
                            }
                        }

                        // 处理置顶的频道
                        List<ChannelOperationInfo> setTopChannelOpList = ChannelOperationCacheUtils
                                .getSetTopChannelOpList(getActivity());
                        List<Channel> setTopChannelList = new ArrayList<>();
                        for (ChannelOperationInfo channelOperationInfo : setTopChannelOpList) {
                            int index = channelList.indexOf(new Channel(channelOperationInfo.getCid()));
                            if (index != -1) {
                                Channel setTopChannel = channelList.get(index);
                                setTopChannel.setIsSetTop(true);
                                setTopChannelList.add(setTopChannel);
                                channelList.remove(index);
                            }
                        }

                        // 所有显得的频道进行统一排序
                        Collections.sort(channelList, new Channel().new SortComparator());
                        channelList.addAll(0, setTopChannelList);
                    }


                    List<Channel> sortChannelList = new ArrayList<Channel>();
                    sortChannelList.addAll(channelList);
                    android.os.Message message = new android.os.Message();
                    message.obj = sortChannelList;
                    message.what = SORT_CHANNEL_COMPLETE;
                    if (handler != null) {
                        handler.sendMessage(message);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };
        weakThread.start();
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
        //判断是否是当前查看的频道的信息或者自己发出的信息
        if (receivedWSMessage.getFromUser().equals(MyApplication.getInstance().getUid())) {
            MessageReadCreationDateCacheUtils.saveMessageReadCreationDate(MyApplication.getInstance(), receivedWSMessage.getChannel(), receivedWSMessage.getCreationDate());
        }

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
     * 将频道的消息置为已读
     *
     * @param channel
     */
    private void setChannelAllMsgRead(Channel channel) {
        // TODO Auto-generated method stub
        MessageReadCreationDateCacheUtils.saveMessageReadCreationDate(MyApplication.getInstance(), channel.getCid(), channel.getMsgLastUpdate());
        int position = displayChannelList.indexOf(channel);
        channel.setUnReadCount(0);
        View childAt = msgListView.getChildAt(position
                - msgListView.getFirstVisiblePosition());
        if (childAt != null) {
            TextView channelTitleText = (TextView) childAt
                    .findViewById(R.id.name_text);
            TextView channelContentText = (TextView) childAt
                    .findViewById(R.id.content_text);
            TextView channelTimeText = (TextView) childAt
                    .findViewById(R.id.time_text);
            RelativeLayout channelNotReadCountLayout = (RelativeLayout) childAt
                    .findViewById(R.id.msg_new_layout);

            channelNotReadCountLayout.setVisibility(View.INVISIBLE);
            channelTitleText.getPaint().setFakeBoldText(false);
            channelContentText.setTextColor(getResources().getColor(
                    R.color.msg_content_color));
            channelTimeText.setTextColor(Color.parseColor("#b8b8b8"));
        }
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
                            sortChannelList();
                        } else {
                            ChannelOperationCacheUtils.setChannelHide(
                                    MyApplication.getInstance(), channel.getCid(), true);
                            // 当隐藏会话时，把该会话的所有消息置为已读
                            MessageReadCreationDateCacheUtils.saveMessageReadCreationDate(MyApplication.getInstance(), channel.getCid(), channel.getMsgLastUpdate());
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
        EventBus.getDefault().post(new EventMessageUnReadCount(unReadCount));
    }

    class CacheChannelTask extends AsyncTask<GetChannelListResult, Void, List<Channel>> {
        private List<Channel> allchannelList = new ArrayList<>();

        @Override
        protected void onPostExecute(List<Channel> addchannelList) {
            sortChannelList();
            createGroupIcon(isHaveCreatGroupIcon ? addchannelList : allchannelList);
            getChannelInfoResult(allchannelList);
        }

        @Override
        protected List<Channel> doInBackground(GetChannelListResult... params) {
            List<Channel> allchannelList = params[0].getChannelList();
            this.allchannelList = allchannelList;
            List<Channel> cacheChannelList = ChannelCacheUtils
                    .getCacheChannelList(getActivity());
            List<Channel> addchannelList = new ArrayList<>();
            addchannelList.addAll(allchannelList);
            addchannelList.removeAll(cacheChannelList);
            ChannelCacheUtils.clearChannel(getActivity());
            ChannelCacheUtils.saveChannelList(getActivity(), allchannelList);
            firstEnterToSetAllChannelMsgRead(allchannelList);
            return addchannelList;
        }
    }

    class CacheNewMsgTask extends AsyncTask<GetNewMessagesResult, Void, Boolean> {
        @Override
        protected void onPostExecute(Boolean isHaveNewMessage) {
            if (isHaveNewMessage) {
                sortChannelList();
            }
        }

        @Override
        protected Boolean doInBackground(GetNewMessagesResult... params) {
            return cacheMessageList(params[0]);
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
                    getChannelList();
                    break;
                case "sort_session_list":
                    sortChannelList();
                    break;
                case "sync_all_base_data_success":
                    createGroupIcon(null);
                    sortChannelList();
                    break;
                case "set_all_message_read":
                    setAllChannelMsgRead();
                    break;
                case "websocket_status":
                    String socketStatus = intent.getExtras().getString("status");
                    showSocketStatusInTitle(socketStatus);
                    break;
                case "set_channel_message_read":
                    String cid = intent.getExtras().getString("cid");
                    long messageCreationDate = intent.getExtras().getLong("messageCreationDate");
                    setChannelMsgRead(cid, messageCreationDate);
                    break;
                default:
                    break;
            }
        }

    }

    private void showSocketStatusInTitle(String socketStatus) {
        if (socketStatus.equals("socket_connecting")) {
            titleText.setText(R.string.socket_connecting);
        } else if (socketStatus.equals(Socket.EVENT_CONNECT)) {
            //当断开以后连接成功(非第一次连接上)后重新拉取一遍消息
            if (!isFirstConnectWebsockt) {
                getChannelList();
            }
            getMessage();
            isFirstConnectWebsockt = false;
            String appTabs = PreferencesByUserAndTanentUtils.getString(getActivity(), "app_tabbar_info_current", "");
            if (!StringUtils.isBlank(appTabs)) {
                titleText.setText(AppTitleUtils.getTabTitle(getActivity(), getClass().getSimpleName()));
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
        for (Channel channel : displayChannelList) {
            MessageReadCreationDateCacheUtils.saveMessageReadCreationDate(MyApplication.getInstance(), channel.getCid(), channel.getMsgLastUpdate());
            channel.setUnReadCount(0);
        }
        displayData();
    }

    /**
     * 初始进入时将所有消息置为已读
     * @param channelList
     */
    private void firstEnterToSetAllChannelMsgRead(List<Channel> channelList){
        if (!DbCacheUtils.tableIsExist("MessageReadCreationDate")){
            List<MessageReadCreationDate> MessageReadCreationDateList = new ArrayList<>();
            for (Channel channel:channelList) {
                MessageReadCreationDateList.add(new MessageReadCreationDate(channel.getCid(),System.currentTimeMillis()));
            }
            MsgReadCreationDateCacheUtils.saveMessageReadCreationDateList(MyApplication.getInstance(), MessageReadCreationDateList);
        }
    }

    /**
     * 将单个频道消息置为已读
     *
     * @param cid
     */
    private void setChannelMsgRead(String cid, long messageCreationDate) {
        MessageReadCreationDateCacheUtils.saveMessageReadCreationDate(MyApplication.getInstance(), cid, messageCreationDate);
        for (Channel channel : displayChannelList) {
            if (channel.getCid().equals(cid)) {
                channel.setUnReadCount(0);
                break;
            }
        }
        refreshIndexNotify();
        if (adapter != null) {
            adapter.notifyDataSetChanged();
        }
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
                    }else if (channelGroup.getType().equals("SERVICE")){
                        int index = channelList.indexOf(new Channel(channelGroup.getCid()));
                        if (index != -1){
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
        if (cacheChannelTask != null && !cacheChannelTask.isCancelled() && cacheChannelTask.getStatus() == AsyncTask.Status.RUNNING) {
            cacheChannelTask.cancel(true);
            cacheChannelTask = null;
        }
        if (cacheMsgAsyncTask != null && !cacheMsgAsyncTask.isCancelled() && cacheMsgAsyncTask.getStatus() == AsyncTask.Status.RUNNING) {
            cacheMsgAsyncTask.cancel(true);
            cacheMsgAsyncTask = null;
        }
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
        } else if ((resultCode == RESULT_OK) && (requestCode == SCAN_LOGIN_QRCODE_RESULT)) {
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
                        getChannelList();
                    }

                    @Override
                    public void createGroupChannelFail() {
                        // TODO Auto-generated method stub

                    }
                });
    }


    //接收到websocket发过来的消息
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onReceiveWSMessage(EventMessage eventMessage) {
        if (eventMessage.getTag().equals(Constant.EVENTBUS_TAG_RECERIVER_SINGLE_WS_MESSAGE)) {
            if (eventMessage.getStatus() == 200) {
                String content = eventMessage.getContent();
                JSONObject contentObj = JSONUtils.getJSONObject(content);
                Message receivedWSMessage = new Message(contentObj);
                Channel receiveMessageChannel = ChannelCacheUtils.getChannel(
                        getActivity(), receivedWSMessage.getChannel());
                cacheReceiveMessage(receivedWSMessage);
                if (receiveMessageChannel == null) {
                    getChannelList();
                } else {
                    sortChannelList();
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
                GetNewMessagesResult getNewMessagesResult = new GetNewMessagesResult(content);
                cacheMsgAsyncTask = new CacheNewMsgTask();
                cacheMsgAsyncTask.execute(getNewMessagesResult);
            }

        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onReceiveWSRecentMessage(EventMessage eventMessage) {
        if (eventMessage.getTag().equals(Constant.EVENTBUS_TAG_GET_CHANNEL_RECENT_MESSAGE)) {
            if (eventMessage.getStatus() == 200) {
                String content = eventMessage.getContent();
                boolean isHaveNewMessage = cacheMessageList(new GetNewMessagesResult(content));
                if (isHaveNewMessage) {
                    sortChannelList();
                }
            } else {
                WebServiceMiddleUtils.hand(getActivity(), eventMessage.getContent(), eventMessage.getStatus());
            }
        }
    }

    /**
     * 缓存获取的消息，返回是否有新消息
     *
     * @param getNewMessagesResult
     * @return
     */
    private boolean cacheMessageList(GetNewMessagesResult getNewMessagesResult) {
        List<Channel> channelList = ChannelCacheUtils
                .getCacheChannelList(getActivity());
        List<Message> allChannelMessageList = new ArrayList<>();
        for (Channel channel : channelList) {
            String cid = channel.getCid();
            List<Message> newMessageList = getNewMessagesResult.getNewMessageList(cid);
            if (newMessageList.size() > 0) {
                allChannelMessageList.addAll(newMessageList);
                Message lastMessage = newMessageList.get(newMessageList.size() - 1);
                // 当会话中最后一条消息为自己发出的时候，将此消息存入已读消息列表，解决最新消息为自己发出，仍识别为未读的问题
                if (lastMessage.getFromUser().equals(MyApplication.getInstance().getUid())) {
                    MessageReadCreationDateCacheUtils.saveMessageReadCreationDate(MyApplication.getInstance(), cid, lastMessage.getCreationDate());
                }
            }
        }
        MessageCacheUtil.saveMessageList(MyApplication.getInstance(), allChannelMessageList, null); // 获取的消息需要缓存
        return allChannelMessageList.size() > 0;
    }

    /**
     * 获取消息会话列表
     */
    private void getChannelList() {
        if (NetUtils.isNetworkConnected(MyApplication.getInstance(), true)) {
            apiService.getChannelList();
        } else {
            swipeRefreshLayout.setRefreshing(false);
        }
    }

    public void getMessage() {
        if (NetUtils.isNetworkConnected(MyApplication.getInstance()) && WebSocketPush.getInstance().isSocketConnect()) {
            long enterAppTime = PreferencesUtils.getLong(MyApplication.getInstance(), Constant.PREF_ENTER_APP_TIME, System.currentTimeMillis());
            if (MessageCacheUtil.isHistoryMessageCache(MyApplication.getInstance(), enterAppTime)) {
                //获取离线消息
                WSAPIService.getInstance().getOfflineMessage();
            } else {
                //获取每个频道最近的20条消息
                WSAPIService.getInstance().getChannelRecentMessage();
            }
        }

    }

    /**
     * 根据cid数组获取Channel信息
     *
     * @param channelList
     */
    public void getChannelInfoResult(List<Channel> channelList) {
        if (NetUtils.isNetworkConnected(MyApplication.getInstance(), false)) {
            ArrayList<String> cidList = new ArrayList<>();
            for (int i = 0; i < channelList.size(); i++) {
                Channel channel = channelList.get(i);
                if (channel.getType().equals("SERVICE")) {
                    cidList.add(channelList.get(i).getCid());
                }
            }
            if (cidList.size() > 0) {
                String[] cidArray = cidList.toArray(new String[cidList.size()]);
                apiService.getChannelGroupList(cidArray);
            }

        }

    }

    class WebService extends APIInterfaceInstance {

        @Override
        public void returnChannelListSuccess(
                GetChannelListResult getChannelListResult) {
            // TODO Auto-generated method stub
            if (getActivity() != null) {
                swipeRefreshLayout.setRefreshing(false);
                cacheChannelTask = new CacheChannelTask();
                cacheChannelTask.execute(getChannelListResult);
            }

        }

        @Override
        public void returnChannelListFail(String error, int errorCode) {
            // TODO Auto-generated method stub
            if (getActivity() != null) {
                swipeRefreshLayout.setRefreshing(false);
                WebServiceMiddleUtils.hand(getActivity(), error, errorCode);
                getMessage();
            }

        }

        @Override
        public void returnSearchChannelGroupSuccess(
                GetSearchChannelGroupResult getSearchChannelGroupResult) {
            saveChannelInfo(getSearchChannelGroupResult
                    .getSearchChannelGroupList());
        }

        @Override
        public void returnSearchChannelGroupFail(String error, int errorCode) {
        }


    }


}
