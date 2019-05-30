package com.inspur.emmcloud.ui.chat;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.inspur.emmcloud.BaseFragment;
import com.inspur.emmcloud.MyApplication;
import com.inspur.emmcloud.R;
import com.inspur.emmcloud.api.APIInterfaceInstance;
import com.inspur.emmcloud.api.apiservice.ChatAPIService;
import com.inspur.emmcloud.bean.chat.Channel;
import com.inspur.emmcloud.bean.chat.ChannelGroup;
import com.inspur.emmcloud.bean.chat.ChannelOperationInfo;
import com.inspur.emmcloud.bean.chat.GetChannelListResult;
import com.inspur.emmcloud.bean.chat.GetNewMsgsResult;
import com.inspur.emmcloud.bean.chat.MatheSet;
import com.inspur.emmcloud.bean.chat.MessageReadCreationDate;
import com.inspur.emmcloud.bean.chat.Msg;
import com.inspur.emmcloud.bean.contact.GetSearchChannelGroupResult;
import com.inspur.emmcloud.bean.system.EmmAction;
import com.inspur.emmcloud.bean.system.GetAppMainTabResult;
import com.inspur.emmcloud.bean.system.MainTabProperty;
import com.inspur.emmcloud.bean.system.MainTabResult;
import com.inspur.emmcloud.bean.system.SimpleEventMessage;
import com.inspur.emmcloud.broadcastreceiver.MsgReceiver;
import com.inspur.emmcloud.config.Constant;
import com.inspur.emmcloud.config.MyAppConfig;
import com.inspur.emmcloud.push.WebSocketPush;
import com.inspur.emmcloud.ui.contact.ContactSearchActivity;
import com.inspur.emmcloud.ui.mine.setting.NetWorkStateDetailActivity;
import com.inspur.emmcloud.util.common.CheckingNetStateUtils;
import com.inspur.emmcloud.util.common.ImageUtils;
import com.inspur.emmcloud.util.common.IntentUtils;
import com.inspur.emmcloud.util.common.NetUtils;
import com.inspur.emmcloud.util.common.StringUtils;
import com.inspur.emmcloud.util.common.ToastUtils;
import com.inspur.emmcloud.util.privates.AppTabUtils;
import com.inspur.emmcloud.util.privates.AppUtils;
import com.inspur.emmcloud.util.privates.ChannelGroupIconUtils;
import com.inspur.emmcloud.util.privates.ChatCreateUtils;
import com.inspur.emmcloud.util.privates.ChatCreateUtils.OnCreateGroupChannelListener;
import com.inspur.emmcloud.util.privates.CustomProtocol;
import com.inspur.emmcloud.util.privates.DirectChannelUtils;
import com.inspur.emmcloud.util.privates.ImageDisplayUtils;
import com.inspur.emmcloud.util.privates.PreferencesByUserAndTanentUtils;
import com.inspur.emmcloud.util.privates.ScanQrCodeUtils;
import com.inspur.emmcloud.util.privates.TimeUtils;
import com.inspur.emmcloud.util.privates.TransHtmlToTextUtils;
import com.inspur.emmcloud.util.privates.UriUtils;
import com.inspur.emmcloud.util.privates.cache.ChannelCacheUtils;
import com.inspur.emmcloud.util.privates.cache.ChannelGroupCacheUtils;
import com.inspur.emmcloud.util.privates.cache.ChannelOperationCacheUtils;
import com.inspur.emmcloud.util.privates.cache.DbCacheUtils;
import com.inspur.emmcloud.util.privates.cache.MessageMatheSetCacheUtils;
import com.inspur.emmcloud.util.privates.cache.MsgCacheUtil;
import com.inspur.emmcloud.util.privates.cache.MsgReadCreationDateCacheUtils;
import com.inspur.emmcloud.util.privates.cache.PVCollectModelCacheUtils;
import com.inspur.emmcloud.widget.CircleTextImageView;
import com.inspur.emmcloud.widget.dialogs.CustomDialog;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import io.socket.client.Socket;

import static android.app.Activity.RESULT_OK;

/**
 * 消息页面 com.inspur.emmcloud.ui.CommunicationV0Fragment
 *
 * @author Jason Chen; create at 2016年8月23日 下午2:59:39
 */
public class CommunicationV0Fragment extends BaseFragment {

    private static final int RECEIVE_MSG = 1;
    private static final int CREAT_CHANNEL_GROUP = 1;
    private static final int RERESH_GROUP_ICON = 2;
    private static final int SORT_CHANNEL_COMPLETE = 3;
    private static final int SORT_CHANNEL = 4;
    private static final int SCAN_LOGIN_QRCODE_RESULT = 5;
    private static final int CREAT_CHANNEL_GROUP_ICON = 6;
    private static final int CACHE_CHANNEL_SUCCESS = 7;
    private View rootView;
    private ListView msgListView;
    private SwipeRefreshLayout swipeRefreshLayout;
    private ChatAPIService apiService;
    private List<Channel> displayChannelList = new ArrayList<>();
    private Adapter adapter;
    private MsgReceiver msgReceiver;
    private Handler handler;
    private MessageFragmentReceiver messageFragmentReceiver;
    private TextView titleText;
    private boolean isHaveCreatGroupIcon = false;
    private PopupWindow popupWindow;
    private boolean isFirstConnectWebsockt = true;//判断是否第一次连上websockt
    private boolean haveHeader = false;
    private View netExceptionView;
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
                default:
                    break;
            }
        }
    };

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        EventBus.getDefault().post(new SimpleEventMessage(Constant.EVENTBUS_TAG_OPEN_DEFALT_TAB, null));
    }

    @Override
    public void onPause() {
        super.onPause();
        if (popupWindow != null && popupWindow.isShowing()) {
            popupWindow.dismiss();
        }
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
    public void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        checkingNetStateUtils = new CheckingNetStateUtils(getContext(), NetUtils.pingUrls);
        initView();
        sortChannelList();// 对Channel 进行排序
        registerMessageFragmentReceiver();
        getChannelList();
        showMessageButtons();
        EventBus.getDefault().register(this);
    }

    @Override
    public void onResume() {
        checkingNetStateUtils.getNetStateResult(5);
        super.onResume();
    }

    /**
     * 展示创建
     */
    private void showMessageButtons() {
        String tabBarInfo = PreferencesByUserAndTanentUtils.getString(getActivity(), Constant.PREF_APP_TAB_BAR_INFO_CURRENT, "");
        //第一次登录时有tabBarInfo会为“”，会导致JSON waring
        if (!StringUtils.isBlank(tabBarInfo)) {
            GetAppMainTabResult getAppMainTabResult = new GetAppMainTabResult(tabBarInfo);
            showCreateGroupOrFindContact(getAppMainTabResult);
        }
    }

    /**
     * 如果数据没有问题则决定展示或者不展示加号，以及通讯录
     *
     * @param getAppMainTabResult
     */
    private void showCreateGroupOrFindContact(GetAppMainTabResult getAppMainTabResult) {
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

    private void initView() {
        // TODO Auto-generated method stub
        netExceptionView = LayoutInflater.from(getContext()).inflate(R.layout.recycleview_header_item, null);
        netExceptionView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                IntentUtils.startActivity(getActivity(), NetWorkStateDetailActivity.class);
            }
        });
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
     * 初始化PullRefreshLayout
     */
    private void initPullRefreshLayout() {
        swipeRefreshLayout = (SwipeRefreshLayout) rootView
                .findViewById(R.id.refresh_layout);
        swipeRefreshLayout.setColorSchemeColors(getResources().getColor(R.color.header_bg_blue), getResources().getColor(R.color.header_bg_blue));
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                getChannelList();
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
                Bundle bundle = new Bundle();
                bundle.putString("title", channel.getTitle());
                bundle.putString("cid", channel.getCid());
                bundle.putString("channelType", channelType);
                if (channelType.equals("GROUP") || channelType.equals("DIRECT") || channelType.equals("SERVICE")) {
                    IntentUtils.startActivity(getActivity(),
                            ChannelV0Activity.class, bundle);
                } else if (channelType.equals("LINK")) {
                    EmmAction emmAction = new EmmAction(channel.getAction());
                    if (emmAction.getCanOpenAction()) {
                        if (emmAction.getUrl().startsWith("http")) {
                            UriUtils.openUrl(getActivity(), emmAction.getUrl());
                        } else {
                            IntentUtils.startActivity(getActivity(), emmAction.getUrl());
                        }
                    }
                } else {
                    ToastUtils.show(getActivity(),
                            R.string.not_support_open_channel);
                }
                setChannelAllMsgRead(channel);
                updateMessageUnReadCount();
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
     * 根据tabbar信息更新加号UI，这里显示信息附在Tabbar信息里
     * 所以没有数据请求回来，MessageFragment不存在的情况
     *
     * @param getAppMainTabResult
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void updateMessageUI(GetAppMainTabResult getAppMainTabResult) {
        showCreateGroupOrFindContact(getAppMainTabResult);
    }

    /**
     * 添加LIstView 的HeaderView
     */
    private void AddHeaderView() {
        if (!haveHeader) {
            msgListView.addHeaderView(netExceptionView);
            haveHeader = true;
        }
    }

    /**
     * 删除ListView 的HeaderView
     */
    private void DeleteHeaderView() {
        if (haveHeader) {
            msgListView.removeHeaderView(netExceptionView);
            haveHeader = false;
        }
    }


    /**
     * app页网络异常提示框
     *
     * @param netState 通过Action获取操作类型
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void netWorkStateHint(SimpleEventMessage netState) {
        if (netState.getAction().equals(Constant.EVENTBUS_TAG_NET_EXCEPTION_HINT)) {   //网络异常提示
            if ((boolean) netState.getMessageObj()) {
                DeleteHeaderView();
                WebSocketPush.getInstance().startWebSocket();
            } else {
                AddHeaderView();
            }
        }
    }


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
                backgroundAlpha(1.0f);
            }
        });

        RelativeLayout createGroupLayout = (RelativeLayout) contentView
                .findViewById(R.id.message_create_group_layout);
        createGroupLayout.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.putExtra("select_content", 2);
                intent.putExtra("isMulti_select", true);
                intent.putExtra("title",
                        getActivity().getString(R.string.creat_group));
                intent.setClass(getActivity(), ContactSearchActivity.class);
                startActivityForResult(intent, CREAT_CHANNEL_GROUP);
                popupWindow.dismiss();
            }
        });


        RelativeLayout scanLayout = (RelativeLayout) contentView.findViewById(R.id.message_scan_layout);
        scanLayout.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
//                Intent intent = new Intent();
//                intent.setClass(getActivity(), PreviewDecodeActivity.class);
//                intent.putExtra("from", "CommunicationFragment");
//                startActivityForResult(intent, SCAN_LOGIN_QRCODE_RESULT);
                AppUtils.openScanCode(CommunicationV0Fragment.this, SCAN_LOGIN_QRCODE_RESULT);
                popupWindow.dismiss();
            }
        });

        // 如果不设置PopupWindow的背景，无论是点击外部区域还是Back键都无法dismiss弹框
        // 我觉得这里是API的一个bug
        popupWindow.setBackgroundDrawable(getResources().getDrawable(
                R.drawable.pop_window_view_tran));
        backgroundAlpha(0.8f);
        // 设置好参数之后再show
        popupWindow.showAsDropDown(view);

    }

    /**
     * 设置添加屏幕的背景透明度
     *
     * @param bgAlpha
     */
    public void backgroundAlpha(float bgAlpha) {
        WindowManager.LayoutParams lp = getActivity().getWindow().getAttributes();
        lp.alpha = bgAlpha; //0.0-1.0
        getActivity().getWindow().setAttributes(lp);
    }

    /**
     * 注册接收消息的广播
     */
    private void registerMessageFragmentReceiver() {
        // TODO Auto-generated method stub
        messageFragmentReceiver = new MessageFragmentReceiver();
        IntentFilter intentFilter = new IntentFilter("message_notify");
        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(messageFragmentReceiver, intentFilter);
    }


    /**
     * 当没有网络的时候加载缓存中的数据
     */
    private List<Channel> getCacheData() {
        // TODO Auto-generated method stub
        List<Channel> channelList = ChannelCacheUtils
                .getCacheChannelList(getActivity());
        for (Channel channel : channelList) {
            List<Msg> newMsgList = MsgCacheUtil.getHistoryMsgList(getActivity(), channel.getCid(), null,
                    15);
            channel.setNewMsgList(MyApplication.getInstance(), newMsgList);
        }
        return channelList;
    }


    /**
     * 为单个群组创建头像
     */
    private void createGroupIcon(List<Channel> channelList) {
        if (MyApplication.getInstance().getIsContactReady() && NetUtils.isNetworkConnected(MyApplication.getInstance(), false)) {
            if (channelList != null && channelList.size() == 0) {
                return;
            }
            isHaveCreatGroupIcon = new ChannelGroupIconUtils().create(MyApplication.getInstance(), channelList, handler);
        }
    }


    /**
     * channel 显示排序
     */
    private void sortChannelList() {
        // TODO Auto-generated method stub
        Thread weakThread = new Thread() {
            @Override
            public void run() {
                super.run();
                try {
                    List<Channel> channelList = getCacheData();
                    if (channelList.size() > 0) {
                        Iterator<Channel> it = channelList.iterator();
                        //将没有消息的单聊和没有消息的但不是自己创建的群聊隐藏掉
                        while (it.hasNext()) {
                            Channel channel = it.next();
                            if (channel.getNewMsgList().size() == 0 && channel.getType().equals("DIRECT")) {
                                it.remove();
                            }
                            channel.setIsSetTop(false);
                            long unReadCount = MsgReadCreationDateCacheUtils.getNotReadMessageCount(
                                    getActivity(), channel.getCid());
                            channel.setUnReadCount(unReadCount);
                            setChannelDisplayTitle(channel);
                            if (channel.getType().equals("DIRECT")) {
                                channel.setShowIcon(DirectChannelUtils.getDirectChannelIcon(MyApplication.getInstance(), channel.getTitle()));
                            } else if (channel.getType().equals("SERVICE")) {
                                channel.setShowIcon(DirectChannelUtils.getRobotIcon(MyApplication.getInstance(), channel.getTitle()));
                            } else if (channel.getType().equals("LINK")) {
                                channel.setShowIcon(channel.getAvatar());
                            }
                        }

                        List<ChannelOperationInfo> hideChannelOpList = ChannelOperationCacheUtils
                                .getHideChannelOpList(getActivity());
                        // 如果隐藏的频道中有未读消息则取消隐藏
                        for (int i = 0; i < hideChannelOpList.size(); i++) {
                            String cid = hideChannelOpList.get(i).getCid();
                            int index = channelList.indexOf(new Channel(cid));
                            if (index != -1) {
                                Channel channel = channelList.get(index);
                                if (channel.getUnReadCount() != 0) {
                                    ChannelOperationCacheUtils.setChannelHide(
                                            getActivity(), cid, false);
                                } else {
                                    channelList.remove(index); // 如果没有未读消息则删除
                                }
                            }
                        }

                        // 处理置顶的频道
                        List<ChannelOperationInfo> setTopChannelOpList = ChannelOperationCacheUtils
                                .getSetTopChannelOpList(getActivity());
                        List<Channel> setTopChannelList = new ArrayList<Channel>();
                        if (setTopChannelOpList != null) {
                            for (int i = 0; i < setTopChannelOpList.size(); i++) {
                                String cid = setTopChannelOpList.get(i).getCid();
                                int index = channelList.indexOf(new Channel(cid));
                                if (index != -1) {
                                    Channel setTopChannel = channelList.get(index);
                                    setTopChannel.setIsSetTop(true);
                                    setTopChannelList.add(setTopChannel);
                                    channelList.remove(index);
                                }
                            }
                        }

                        // 所有显得的频道进行统一排序
                        Collections.sort(channelList, new SortComparator());
                        channelList.addAll(0, setTopChannelList);
                    }
                    List<Channel> sortChannelList = new ArrayList<Channel>();
                    sortChannelList.addAll(channelList);
                    Message message = new Message();
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


    /**
     * 设置session的显示名称
     *
     * @param channel
     */
    private void setChannelDisplayTitle(Channel channel) {

        String title;
        if (channel.getType().equals("DIRECT")) {
            title = DirectChannelUtils.getDirectChannelTitle(getActivity(),
                    channel.getTitle());
        } else if (channel.getType().equals("SERVICE")) {
            title = DirectChannelUtils.getRobotInfo(getActivity(), channel.getTitle()).getName();
        } else {
            title = channel.getTitle();
        }
        channel.setDisplayTitle(title);


    }

    private void handMessage() {
        // TODO Auto-generated method stub
        handler = new Handler() {

            @Override
            public void handleMessage(Message msg) {
                // TODO Auto-generated method stub
                switch (msg.what) {
                    case RECEIVE_MSG:
                        // 接收到新的消息
                        Msg receivedMsg = new Msg((JSONObject) msg.obj);
                        if (receivedMsg.getType().equals("command/faceLogin")) {
                            return;
                        }
                        //消息拦截逻辑，以后应当拦截命令消息，此时注释掉，以后解开注意判空
//                        CustomProtocol customProtocol = getCommandMessageProtocol(receivedMsg);
//                        if(customProtocol != null){
//                            MsgReadCreationDateCacheUtils.saveMessageReadCreationDate(getActivity(),receivedMsg.getCid(),receivedMsg.getTime());
//                            Intent intent = new Intent();
//                            intent.setClass(getActivity(),ChannelVoiceCommunicationActivity.class);
//                            intent.putExtra("channelId",customProtocol.getParamMap().get("id"));
//                            intent.putExtra(ChannelVoiceCommunicationActivity.VOICE_COMMUNICATION_STATE,ChannelVoiceCommunicationActivity.INVITEE_LAYOUT_STATE);
//                            startActivity(intent);
//                        }
                        Channel receiveMsgChannel = ChannelCacheUtils.getChannel(
                                getActivity(), receivedMsg.getCid());
                        if (receiveMsgChannel == null) {
                            getChannelList();
                        } else {
                            cacheReceiveMsg(receiveMsgChannel, receivedMsg);
                            sortChannelList();
                        }
                        break;
                    case RERESH_GROUP_ICON:
                        boolean isCreateNewGroupIcon = (Boolean) msg.obj;
                        if (adapter != null && isCreateNewGroupIcon) {
                            displayData();
                        }
                        break;
                    case CREAT_CHANNEL_GROUP_ICON:
                        List<Channel> createChannelIconChannelList = (List<Channel>) msg.obj;
                        createGroupIcon(createChannelIconChannelList);
                        break;
                    case CACHE_CHANNEL_SUCCESS:
                        sortChannelList();
                        getChannelMsg();
                        List<String> serviceCidList = (List<String>) msg.obj;
                        getServieChannelInputs(serviceCidList);
                        break;
                    case SORT_CHANNEL:
                        sortChannelList();
                        break;
                    case SORT_CHANNEL_COMPLETE:
                        List<Channel> channelList = (List<Channel>) msg.obj;
                        displayChannelList.clear();
                        displayChannelList.addAll(channelList);
                        displayData();// 展示数据
                        registerMsgReceiver();// 注册接收消息的广播
                        WebSocketPush.getInstance().startWebSocket();
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

    /**
     * 缓存推送的消息体，消息连续时间段，已读消息的id
     *
     * @param receiveMsgChannel
     * @param receivedMsg
     */
    private void cacheReceiveMsg(Channel receiveMsgChannel, Msg receivedMsg) {
        // TODO Auto-generated method stub
        Msg channelNewMsg = MsgCacheUtil.getNewMsg(MyApplication.getInstance(),
                receivedMsg.getCid());
        MsgCacheUtil.saveMsg(getActivity(), receivedMsg);
        if (channelNewMsg == null) {
            MessageMatheSetCacheUtils.add(MyApplication.getInstance(),
                    receiveMsgChannel.getCid(),
                    new MatheSet(receivedMsg.getTime(), receivedMsg.getTime()));
        } else {
            MessageMatheSetCacheUtils.add(MyApplication.getInstance(),
                    receiveMsgChannel.getCid(),
                    new MatheSet(channelNewMsg.getTime(), receivedMsg.getTime()));
        }
        /** 判断是否是当前查看的频道的信息或者自己发出的信息 **/
        if (receivedMsg.getUid().equals(MyApplication.getInstance().getUid())) {
            MsgReadCreationDateCacheUtils.saveMessageReadCreationDate(MyApplication.getInstance(),
                    receivedMsg.getCid(), receivedMsg.getTime());
        }

    }

    /**
     * 注册接收消息的广播
     */
    private void registerMsgReceiver() {
        // TODO Auto-generated method stub
        if (msgReceiver == null) {
            msgReceiver = new MsgReceiver(handler);
            IntentFilter filter = new IntentFilter();
            filter.addAction("com.inspur.msg");
            LocalBroadcastManager.getInstance(getActivity()).registerReceiver(msgReceiver, filter);
        }
    }

    /**
     * 显示获取的数据
     */
    private void displayData() {
        (rootView
                .findViewById(R.id.rl_no_chat)).setVisibility((displayChannelList.size() == 0) ? View.VISIBLE : View.GONE);
        if (adapter == null) {
            adapter = new Adapter();
            adapter.setDataList(displayChannelList);
            msgListView.setAdapter(adapter);
        } else {
            adapter.setDataList(displayChannelList);
            adapter.notifyDataSetChanged();
        }
        updateMessageUnReadCount();

    }

    /**
     * 将频道的消息置为已读
     *
     * @param channel
     */
    private void setChannelAllMsgRead(Channel channel) {
        // TODO Auto-generated method stub
        MsgReadCreationDateCacheUtils.saveMessageReadCreationDate(MyApplication.getInstance(),
                channel.getCid(), channel.getMsgLastUpdate());
        int position = displayChannelList.indexOf(channel);
        displayChannelList.get(position).setUnReadCount(0);
        View childAt = msgListView.getChildAt(position
                - msgListView.getFirstVisiblePosition());
        if (childAt != null) {
            TextView channelTitleText = (TextView) childAt
                    .findViewById(R.id.tv_name);
            TextView channelContentText = (TextView) childAt
                    .findViewById(R.id.tv_content);
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
        new CustomDialog.ListDialogBuilder(getActivity())
                .setItems(items, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        if (which == 0) {
                            ChannelOperationCacheUtils.setChannelTop(getActivity(),
                                    displayChannelList.get(position).getCid(), !isChannelSetTop);
                            sortChannelList();
                        } else {
                            ChannelOperationCacheUtils.setChannelHide(
                                    getActivity(), displayChannelList.get(position)
                                            .getCid(), true);
                            // 当隐藏会话时，把该会话的所有消息置为已读
                            MsgReadCreationDateCacheUtils
                                    .saveMessageReadCreationDate(MyApplication.getInstance(),
                                            displayChannelList.get(position)
                                                    .getCid(), displayChannelList
                                                    .get(position).getMsgLastUpdate());
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
    private void updateMessageUnReadCount() {
        int unReadCount = 0;
        if (displayChannelList != null) {
            for (int i = 0; i < displayChannelList.size(); i++) {
                unReadCount += displayChannelList.get(i).getUnReadCount();
            }
        }
        EventBus.getDefault().post(new SimpleEventMessage(Constant.EVENTBUS_TAG_SET_ALL_MESSAGE_UNREAD_COUNT, unReadCount));
    }

    private void showSocketStatusInTitle(String socketStatus) {
        if (socketStatus.equals(Socket.EVENT_CONNECTING)) {
            titleText.setText(R.string.socket_connecting);
        } else if (socketStatus.equals(Socket.EVENT_CONNECT)) {
            //当断开以后连接成功(非第一次连接上)后重新拉取一遍消息
            if (!isFirstConnectWebsockt) {
                getChannelList();
            }
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
    private void setAllChannelMsgRead() {
        // TODO Auto-generated method stub
        List<MessageReadCreationDate> MessageReadCreationDateList = new ArrayList<>();
        for (Channel channel : displayChannelList) {
            MessageReadCreationDateList.add(new MessageReadCreationDate(channel.getCid(), channel.getMsgLastUpdate()));
            channel.setUnReadCount(0);
        }
        MsgReadCreationDateCacheUtils.saveMessageReadCreationDateList(MyApplication.getInstance(), MessageReadCreationDateList);
        displayData();
    }

//    class CacheChannelTask extends AsyncTask<GetChannelListResult, Void, List<Channel>> {
//        private List<Channel> allchannelList = new ArrayList<>();
//
//        @Override
//        protected void onPostExecute(List<Channel> addchannelList) {
//            getChannelMsg();
//            LogUtils.jasonDebug("isHaveCreatGroupIcon=");
//            if (!isHaveCreatGroupIcon) {
//                createGroupIcon(allchannelList);
//            } else {
//                createGroupIcon(addchannelList);
//            }
//            getChannelInfoResult(allchannelList);
//        }
//
//        @Override
//        protected List<Channel> doInBackground(GetChannelListResult... params) {
//            List<Channel> allchannelList = params[0].getChannelList();
//            this.allchannelList = allchannelList;
//            List<Channel> cacheChannelList = ChannelCacheUtils
//                    .getCacheChannelList(getActivity());
//            List<Channel> addchannelList = new ArrayList<>();
//            addchannelList.addAll(allchannelList);
//            addchannelList.removeAll(cacheChannelList);
//            ChannelCacheUtils.clearChannel(getActivity());
//            ChannelCacheUtils.saveChannelList(getActivity(), allchannelList);
//            firstEnterToSetAllChannelMsgRead(allchannelList);
//            return addchannelList;
//        }
//    }

    /**
     * 初始进入时将所有消息置为已读
     *
     * @param channelList
     */
    private void firstEnterToSetAllChannelMsgRead(List<Channel> channelList) {
        if (!DbCacheUtils.tableIsExist(null, "MessageReadCreationDate")) {
            List<MessageReadCreationDate> MessageReadCreationDateList = new ArrayList<>();
            for (Channel channel : channelList) {
                MessageReadCreationDateList.add(new MessageReadCreationDate(channel.getCid(), System.currentTimeMillis()));
            }
            MsgReadCreationDateCacheUtils.saveMessageReadCreationDateList(MyApplication.getInstance(), MessageReadCreationDateList);
        }
    }

    /**
     * 将单个频道消息置为已读
     *
     * @param cid
     */
    private void setChannelMsgRead(String cid, Long messageCreationDate) {
        MsgReadCreationDateCacheUtils.saveMessageReadCreationDate(getActivity(), cid,
                messageCreationDate);
        for (int i = 0; i < displayChannelList.size(); i++) {
            Channel channel = displayChannelList.get(i);
            if (channel.getCid().equals(cid)) {
                channel.setUnReadCount(0);
                break;
            }
        }
        updateMessageUnReadCount();
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
                    } else if (channelGroup.getType().equals("SERVICE")) {
                        int index = channelList.indexOf(new Channel(channelGroup.getCid()));
                        if (index != -1) {
                            channelList.get(index).setInputs(channelGroup.getInputs());
                        }

                    } else if (channelGroup.getType().equals("LINK")) {
                        int index = channelList.indexOf(new Channel(channelGroup.getCid()));
                        if (index != -1) {
                            channelList.get(index).setAction(channelGroup.getAction());
                            channelList.get(index).setAvatar(channelGroup.getAvatar());
                        }
                    }
                }
                ChannelGroupCacheUtils.saveChannelGroupList(MyApplication.getInstance(), channelGroupList);
                ChannelCacheUtils.saveChannelList(MyApplication.getInstance(), channelList);
                sortChannelList();
            }
        }).start();

    }

    @Override
    public void onDestroy() {
        // TODO Auto-generated method stub
        super.onDestroy();
        if (handler != null) {
            handler = null;
        }

        if (msgReceiver != null) {
            LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(msgReceiver);
            msgReceiver = null;
        }
        if (messageFragmentReceiver != null) {
            LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(messageFragmentReceiver);
            messageFragmentReceiver = null;
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
                                ChannelV0Activity.class, bundle);
                        ChannelGroupCacheUtils.saveChannelGroup(MyApplication.getInstance(),
                                channelGroup);
                        getChannelList();
                    }

                    @Override
                    public void createGroupChannelFail() {
                        // TODO Auto-generated method stub

                    }
                });
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

    /**
     * 获取频道消息
     */
    private void getChannelMsg() {
        if (NetUtils.isNetworkConnected(MyApplication.getInstance(), false)) {
            apiService.getNewMsgs();
        } else {
            swipeRefreshLayout.setRefreshing(false);
        }
    }

    /**
     * 根据cid数组获取Channel信息
     *
     * @param serviceCidList
     */
    public void getServieChannelInputs(List<String> serviceCidList) {
        if (NetUtils.isNetworkConnected(MyApplication.getInstance(), false)) {
            if (serviceCidList.size() > 0) {
                String[] cidArray = serviceCidList.toArray(new String[serviceCidList.size()]);
                apiService.getChannelGroupList(cidArray);
            }

        }

    }

    static class ViewHolder {
        RelativeLayout mainLayout;
        CircleTextImageView channelPhotoImg;
        TextView channelContentText;
        TextView channelTitleText;
        TextView channelTimeText;
        RelativeLayout channelNotReadCountLayout;
        TextView channelNotReadCountText;
        ImageView dndImg;
    }

    private class Adapter extends BaseAdapter {
        private List<Channel> dataList = new ArrayList<>();

        public void setDataList(List<Channel> channelList) {
            dataList.clear();
            dataList.addAll(channelList);
        }

        @Override
        public int getCount() {
            // TODO Auto-generated method stub
            return dataList.size();
        }

        @Override
        public Object getItem(int position) {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public long getItemId(int position) {
            // TODO Auto-generated method stub
            return 0;
        }


        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            // TODO Auto-generated method stub
            ViewHolder holder;
            if (convertView == null) {
                holder = new ViewHolder();
                convertView = LayoutInflater.from(getActivity()).inflate(R.layout.msg_item_view, null);
                holder.mainLayout = (RelativeLayout) convertView
                        .findViewById(R.id.main_layout);
                holder.channelPhotoImg = (CircleTextImageView) convertView
                        .findViewById(R.id.msg_img);
                holder.channelTitleText = (TextView) convertView
                        .findViewById(R.id.tv_name);
                holder.channelContentText = (TextView) convertView
                        .findViewById(R.id.tv_content);
                holder.channelTimeText = (TextView) convertView
                        .findViewById(R.id.time_text);
                holder.channelNotReadCountLayout = (RelativeLayout) convertView
                        .findViewById(R.id.msg_new_layout);
                holder.channelNotReadCountText = (TextView) convertView
                        .findViewById(R.id.msg_new_text);
                holder.dndImg = (ImageView) convertView
                        .findViewById(R.id.msg_dnd_img);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }
            Channel channel = dataList.get(position);
            setChannelIcon(channel, holder.channelPhotoImg);
            setChannelMsgReadStateUI(channel, holder);
            holder.channelTitleText.setText(channel.getDisplayTitle());
            holder.dndImg.setVisibility(channel.getDnd() ? View.VISIBLE : View.GONE);
            holder.mainLayout
                    .setBackgroundResource(channel.getIsSetTop() ? R.drawable.selector_set_top_msg_list : R.drawable.selector_list);
            return convertView;
        }

        /**
         * 设置Channel的Icon
         *
         * @param channel
         */
        private void setChannelIcon(Channel channel, CircleTextImageView channelPhotoImg) {
            // TODO Auto-generated method stub
            if (channel.getType().equals("GROUP")) {
                File file = new File(MyAppConfig.LOCAL_CACHE_PHOTO_PATH,
                        MyApplication.getInstance().getTanent() + channel.getCid() + "_100.png1");
                channelPhotoImg.setTag("");
                if (file.exists()) {
                    channelPhotoImg.setImageBitmap(ImageUtils.getBitmapByFile(file));
                } else {
                    channelPhotoImg.setImageResource(R.drawable.icon_channel_group_default);
                }
            } else if (channel.getType().equals("DIRECT") || channel.getType().equals("SERVICE") || channel.getType().equals("LINK")) {
                ImageDisplayUtils.getInstance().displayImageByTag(channelPhotoImg, channel.getShowIcon(), R.drawable.icon_person_default);
            } else {
                channelPhotoImg.setTag("");
                channelPhotoImg.setImageResource(R.drawable.icon_channel_group_default);
            }


        }


        /**
         * 设置频道未读和已读消息的显示
         *
         * @param channel
         */
        private void setChannelMsgReadStateUI(final Channel channel, ViewHolder holder) {
            // TODO Auto-generated method stub
            long unReadCount = channel.getUnReadCount();
            holder.channelTimeText.setText(TimeUtils.getDisplayTime(
                    getActivity(), channel.getMsgLastUpdate()));
            String chatDrafts = PreferencesByUserAndTanentUtils.getString(MyApplication.getInstance(), MyAppConfig.getChannelDrafsPreKey(channel.getCid()), null);
            if (chatDrafts != null) {
                String content = "<font color='#FF0000'>" + MyApplication.getInstance().getString(R.string.message_type_drafts) + "</font>" + chatDrafts;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    holder.channelContentText.setText(Html.fromHtml(content, Html.FROM_HTML_MODE_LEGACY, null, null));
                } else {
                    holder.channelContentText.setText(Html.fromHtml(content));
                }
            } else {
                holder.channelContentText.setText(channel
                        .getNewMsgContent());
                TransHtmlToTextUtils.stripUnderlines(holder.channelContentText,
                        R.color.msg_content_color);
            }

            TransHtmlToTextUtils.stripUnderlines(holder.channelContentText,
                    R.color.msg_content_color);
            boolean isHasUnReadMsg = (unReadCount != 0);
            holder.channelNotReadCountLayout.setVisibility(isHasUnReadMsg ? View.VISIBLE : View.INVISIBLE);
            holder.channelTitleText.getPaint().setFakeBoldText(isHasUnReadMsg);
            holder.channelContentText.setTextColor(isHasUnReadMsg ? getResources().getColor(
                    R.color.black) : getResources().getColor(
                    R.color.msg_content_color));
            holder.channelTimeText.setTextColor(isHasUnReadMsg ? getResources().getColor(
                    R.color.msg_time_color) :
                    Color.parseColor("#b8b8b8"));
            if (isHasUnReadMsg) {
                holder.channelNotReadCountText.setText(unReadCount > 99 ? "99+" : "" + unReadCount);
            }
        }
    }

    class CacheChannelThread extends Thread {
        private GetChannelListResult getChannelListResult;

        private CacheChannelThread(GetChannelListResult getChannelListResult) {
            this.getChannelListResult = getChannelListResult;
        }

        @Override
        public void run() {
            try {
                List<String> serviceCidList = new ArrayList<>();
                List<Channel> allchannelList = getChannelListResult.getChannelList();
                List<Channel> cacheChannelList = ChannelCacheUtils.getCacheChannelList(MyApplication.getInstance());
                for (Channel channel : allchannelList) {
                    if (channel.getType().equals("SERVICE") || channel.getType().equals("LINK")) {
                        int position = cacheChannelList.indexOf(cacheChannelList);
                        if (position != -1) {
                            channel.setInputs(cacheChannelList.get(position).getInputs());
                            channel.setShowIcon(cacheChannelList.get(position).getAvatar());
                            channel.setAction(cacheChannelList.get(position).getAction());
                        }
                        serviceCidList.add(channel.getCid());
                    }
                }
                ChannelCacheUtils.saveChannelList(MyApplication.getInstance(), allchannelList);
                List<Channel> intersectionConversationList = new ArrayList<>();
                intersectionConversationList.addAll(allchannelList);
                intersectionConversationList.retainAll(cacheChannelList);
                cacheChannelList.removeAll(intersectionConversationList);
                ChannelCacheUtils.deleteChannelList(MyApplication.getInstance(), cacheChannelList);
                if (handler != null) {
                    android.os.Message message = handler.obtainMessage(CACHE_CHANNEL_SUCCESS, serviceCidList);
                    message.sendToTarget();
                    if (isHaveCreatGroupIcon) {
                        allchannelList.removeAll(intersectionConversationList);
                    }
                    android.os.Message createChannelIconMessage = handler.obtainMessage(CREAT_CHANNEL_GROUP_ICON, allchannelList);
                    createChannelIconMessage.sendToTarget();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

        }
    }

    class CacheMessageThread extends Thread {
        private GetNewMsgsResult getNewMsgsResult;

        public CacheMessageThread(GetNewMsgsResult getNewMsgsResult) {
            this.getNewMsgsResult = getNewMsgsResult;
        }

        @Override
        public void run() {
            try {
                if (getNewMsgsResult != null) {
                    String myUid = MyApplication.getInstance().getUid();
                    List<Channel> channelList = getCacheData();
                    for (int i = 0; i < channelList.size(); i++) {
                        String cid = channelList.get(i).getCid();
                        List<Msg> newMsgList = getNewMsgsResult.getNewMsgList(cid);
                        if (newMsgList.size() > 0) {
                            MsgCacheUtil.saveMsgList(getActivity(), newMsgList, null); // 获取的消息需要缓存
                            // 当会话中最后一条消息为自己发出的时候，将此消息存入已读消息列表，解决最新消息为自己发出，仍识别为未读的问题
                            if (newMsgList.get(newMsgList.size() - 1).getUid()
                                    .equals(myUid)) {
                                MsgReadCreationDateCacheUtils.saveMessageReadCreationDate(MyApplication.getInstance(), cid,
                                        newMsgList.get(newMsgList.size() - 1).getTime());
                            }
                        }
                    }
                    if (handler != null) {
                        android.os.Message message = handler.obtainMessage(SORT_CHANNEL);
                        message.sendToTarget();
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

        }
    }

    /**
     * 接受创建群组头像的icon
     *
     * @author Administrator
     */
    public class MessageFragmentReceiver extends BroadcastReceiver {

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
                case "refresh_adapter":
                    if (adapter != null) {
                        adapter.notifyDataSetChanged();
                    }
                    break;
                default:
                    break;
            }
        }
    }

    private class SortComparator implements Comparator {

        @Override
        public int compare(Object lhs, Object rhs) {
            Channel channelA = (Channel) lhs;
            Channel channelB = (Channel) rhs;
            long diff = channelA.getMsgLastUpdate()
                    - channelB.getMsgLastUpdate();
            if (diff > 0) {
                return -1;
            } else if (diff == 0) {
                return 0;
            } else {
                return 1;
            }
        }
    }

    class WebService extends APIInterfaceInstance {

        @Override
        public void returnChannelListSuccess(
                GetChannelListResult getChannelListResult) {
            // TODO Auto-generated method stub
            if (getActivity() != null) {
                new CacheChannelThread(getChannelListResult).run();
            }

        }

        @Override
        public void returnChannelListFail(String error, int errorCode) {
            // TODO Auto-generated method stub
            if (getActivity() != null) {
                swipeRefreshLayout.setRefreshing(false);
                sortChannelList();// 对Channel 进行排序
            }

        }

        @Override
        public void returnNewMsgsSuccess(final GetNewMsgsResult getNewMsgsResult) {
            // TODO Auto-generated method stub
            if (getActivity() != null) {
                swipeRefreshLayout.setRefreshing(false);
                new CacheMessageThread(getNewMsgsResult).start();

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


        @Override
        public void returnNewMsgsFail(String error, int errorCode) {
            // TODO Auto-generated method stub
            if (getActivity() != null) {
                swipeRefreshLayout.setRefreshing(false);
                sortChannelList();// 对Channel 进行排序
            }

        }

    }


}
