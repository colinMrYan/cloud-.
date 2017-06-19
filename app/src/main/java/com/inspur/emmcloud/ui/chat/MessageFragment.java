package com.inspur.emmcloud.ui.chat;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.inspur.emmcloud.MyApplication;
import com.inspur.emmcloud.R;
import com.inspur.emmcloud.api.APIInterfaceInstance;
import com.inspur.emmcloud.api.apiservice.ChatAPIService;
import com.inspur.emmcloud.bean.AppTabAutoBean;
import com.inspur.emmcloud.bean.Channel;
import com.inspur.emmcloud.bean.ChannelGroup;
import com.inspur.emmcloud.bean.ChannelOperationInfo;
import com.inspur.emmcloud.bean.GetChannelListResult;
import com.inspur.emmcloud.bean.GetNewMsgsResult;
import com.inspur.emmcloud.bean.GetSearchChannelGroupResult;
import com.inspur.emmcloud.bean.MatheSet;
import com.inspur.emmcloud.bean.Msg;
import com.inspur.emmcloud.broadcastreceiver.MsgReceiver;
import com.inspur.emmcloud.callback.CommonCallBack;
import com.inspur.emmcloud.ui.IndexActivity;
import com.inspur.emmcloud.ui.contact.ContactSearchActivity;
import com.inspur.emmcloud.util.AppTitleUtils;
import com.inspur.emmcloud.util.ChannelCacheUtils;
import com.inspur.emmcloud.util.ChannelGroupCacheUtils;
import com.inspur.emmcloud.util.ChannelOperationCacheUtils;
import com.inspur.emmcloud.util.ChatCreateUtils;
import com.inspur.emmcloud.util.ChatCreateUtils.OnCreateGroupChannelListener;
import com.inspur.emmcloud.util.DirectChannelUtils;
import com.inspur.emmcloud.util.ImageDisplayUtils;
import com.inspur.emmcloud.util.IntentUtils;
import com.inspur.emmcloud.util.MsgCacheUtil;
import com.inspur.emmcloud.util.MsgMatheSetCacheUtils;
import com.inspur.emmcloud.util.MsgReadIDCacheUtils;
import com.inspur.emmcloud.util.NetUtils;
import com.inspur.emmcloud.util.PreferencesByUserAndTanentUtils;
import com.inspur.emmcloud.util.PreferencesUtils;
import com.inspur.emmcloud.util.StringUtils;
import com.inspur.emmcloud.util.TimeUtils;
import com.inspur.emmcloud.util.ToastUtils;
import com.inspur.emmcloud.util.TransHtmlToTextUtils;
import com.inspur.emmcloud.util.WebServiceMiddleUtils;
import com.inspur.emmcloud.widget.CircleFrameLayout;
import com.inspur.emmcloud.widget.dialogs.MyDialog;
import com.inspur.emmcloud.widget.pullableview.PullToRefreshLayout;
import com.inspur.emmcloud.widget.pullableview.PullToRefreshLayout.OnRefreshListener;
import com.inspur.emmcloud.widget.pullableview.PullableListView;
import com.inspur.emmcloud.widget.tipsview.TipsView;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import io.socket.client.Socket;

/**
 * 消息页面 com.inspur.emmcloud.ui.MessageFragment
 * 
 * @author Jason Chen; create at 2016年8月23日 下午2:59:39
 */
public class MessageFragment extends Fragment implements OnRefreshListener {

	private static final int RECEIVE_MSG = 1;
	private static final int CREAT_CHANNEL_GROUP = 1;
	private static final int RERESH_GROUP_ICON = 2;
	private View rootView;
	private LayoutInflater inflater;
	private PullableListView msgListView;
	private ChatAPIService apiService;
	private List<Channel> displayChannelList = new ArrayList<Channel>();
	private Adapter adapter;
	private MsgReceiver msgReceiver;
	private Handler handler;
	private String channelIdInOpen = ""; // 当前正在查看的频道的id,为了标示各频道未读消息条数
	private PullToRefreshLayout pullToRefreshLayout;
	private MessageFragmentReceiver messageFragmentReceiver;
	private TipsView TipsView;
	private TextView titleText;

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
		setTabTitle();
		return rootView;
	}

	@Override
	public void onAttach(Context context) {
		super.onAttach(context);
		CommonCallBack callBack = (CommonCallBack)context;
		callBack.execute();
	}

	/**
	 * 设置标题
	 */
	private void setTabTitle(){
		String appTabs = PreferencesByUserAndTanentUtils.getString(getActivity(),"app_tabbar_info_current","");
		if(!StringUtils.isBlank(appTabs)){
			titleText.setText(AppTitleUtils.getTabTitle(getActivity(),getClass().getSimpleName()));
		}
	}
	

	@Override
	public void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		channelIdInOpen = "";// 清空id
	}
	
	@Override
	public void onPause() {
		super.onPause();
		pullToRefreshLayout.refreshFinish(PullToRefreshLayout.FAIL);
	}

    @Override
    public void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        initView();
        handMessage();
        registerMessageFragmentReceiver();
        getChannelContent();
        showMessageButtons();
        EventBus.getDefault().register(this);
    }

    /**
     * 展示创建
     */
    private void showMessageButtons() {
        String tabBarInfo = PreferencesByUserAndTanentUtils.getString(getActivity(), "app_tabbar_info_current", "");
        AppTabAutoBean appTabAutoBean = new AppTabAutoBean(tabBarInfo);
        if(appTabAutoBean != null) {
            AppTabAutoBean.PayloadBean payloadBean = appTabAutoBean.getPayload();
            if (payloadBean != null) {
                showCreateGroupOrFindContact(payloadBean);
            }
        }
    }

    /**
     * 如果数据没有问题则决定展示或者不展示加号，以及通讯录
     * @param payloadBean
     */
    private void showCreateGroupOrFindContact(AppTabAutoBean.PayloadBean payloadBean){
        ArrayList<AppTabAutoBean.PayloadBean.TabsBean> appTabList =
                (ArrayList<AppTabAutoBean.PayloadBean.TabsBean>) payloadBean.getTabs();
        for (int i = 0; i < appTabList.size(); i++) {
            if (appTabList.get(i).getComponent().equals("communicate")) {
                AppTabAutoBean.PayloadBean.TabsBean.Property property = appTabList.get(i).getProperty();
                if (property != null) {
					rootView.findViewById(R.id.find_friends_btn).setVisibility(View.GONE);
                    if (!property.isCanCreate()) {
                        rootView.findViewById(R.id.add_img).setVisibility(View.GONE);
                    }
                    if (!property.isCanContact()) {
                        rootView.findViewById(R.id.address_list_img).setVisibility(View.GONE);
                    }
                }
            }
        }

    }

	private void initView() {
		// TODO Auto-generated method stub
		apiService = new ChatAPIService(getActivity());
		apiService.setAPIInterface(new WebService());
		inflater = (LayoutInflater) getActivity().getSystemService(
				getActivity().LAYOUT_INFLATER_SERVICE);
		rootView = inflater.inflate(R.layout.fragment_message, null);
		pullToRefreshLayout = (PullToRefreshLayout) rootView
				.findViewById(R.id.refresh_view);
		pullToRefreshLayout.setOnRefreshListener(MessageFragment.this);
		msgListView = (PullableListView) rootView.findViewById(R.id.msg_list);
		(rootView.findViewById(R.id.address_list_img))
				.setOnClickListener(onViewClickListener);
		(rootView.findViewById(R.id.add_img))
				.setOnClickListener(onViewClickListener);
		(rootView.findViewById(R.id.find_friends_btn))
				.setOnClickListener(onViewClickListener);
		TipsView = (TipsView) rootView.findViewById(R.id.tip);
		titleText = (TextView)rootView.findViewById(R.id.header_text);
	}

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void updateMessageUI(AppTabAutoBean appTabAutoBean) {
        if(appTabAutoBean != null){
            AppTabAutoBean.PayloadBean payloadBean = appTabAutoBean.getPayload();
            if(payloadBean != null){
                showCreateGroupOrFindContact(payloadBean);
            }
        }

    }

    private OnClickListener onViewClickListener = new OnClickListener() {

		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			switch (v.getId()) {
			case R.id.address_list_img:
			case R.id.find_friends_btn:
				Bundle bundle = new Bundle();
				bundle.putInt("select_content", 4);
				bundle.putBoolean("isMulti_select", false);
				bundle.putString("title",
						getActivity().getString(R.string.adress_list));
				IntentUtils.startActivity(getActivity(),
						ContactSearchActivity.class, bundle);
				break;
			case R.id.add_img:
				Intent intent = new Intent();
				intent.putExtra("select_content", 2);
				intent.putExtra("isMulti_select", true);
				intent.putExtra("title",
						getActivity().getString(R.string.creat_group));
				intent.setClass(getActivity(), ContactSearchActivity.class);
				startActivityForResult(intent, CREAT_CHANNEL_GROUP);
				break;
			default:
				break;
			}
		}
	};

	/**
	 * 注册接收消息的广播
	 */
	private void registerMessageFragmentReceiver() {
		// TODO Auto-generated method stub
		messageFragmentReceiver = new MessageFragmentReceiver();
		IntentFilter intentFilter = new IntentFilter("message_notify");
		getActivity().registerReceiver(messageFragmentReceiver, intentFilter);

	}


	/**
	 * 获取消息会话列表和最新消息
	 */
	private void getChannelContent() {
		// TODO Auto-generated method stub
		if (NetUtils.isNetworkConnected(getActivity())) {
			apiService.getChannelList();
		}
		handData();
	}

	/**
	 * 当没有网络的时候加载缓存中的数据
	 */
	private List<Channel> getCacheData() {
		// TODO Auto-generated method stub
		List<Channel> channelList = ChannelCacheUtils
				.getCacheChannelList(getActivity());
		for (int i = 0; i < channelList.size(); i++) {
			String cid = channelList.get(i).getCid();
			List<Msg> newMsgList = new ArrayList<Msg>();
			newMsgList = MsgCacheUtil.getHistoryMsgList(getActivity(), cid, "",
					15);
			channelList.get(i).setNewMsgList(newMsgList);
		}
		return channelList;
	}

	/**
	 * 处理获得的数据
	 */
	private void handData() {
		// TODO Auto-generated method stub
		List<Channel> channelList = getCacheData();// 获取缓存中的数据
		getGroupInfo();//获取缓存中没有的群组信息（群成员，为群组头像生成提供数据）
		sortChannelList(channelList);// 对Channel 进行排序
		displayData();// 展示数据
		registerMsgReceiver();// 注册接收消息的广播
		((MyApplication)getActivity().getApplication()).startWebSocket();// 启动webSocket推送
	}

	/**
	 * channel 显示排序
	 */
	private void sortChannelList(List<Channel> channelList) {
		// TODO Auto-generated method stub
		if (channelList.size() > 0) {
			Iterator<Channel> it = channelList.iterator();
			//将没有消息的单聊和没有消息的但不是自己创建的群聊隐藏掉
			while (it.hasNext()) {
				Channel channel = it.next();
				if (channel.getNewMsgList().size() == 0){
					if (channel.getType().equals("DIRECT")){
						it.remove();
					}else if(channel.getType().equals("GROUP")){
						ChannelGroup channelGroup = ChannelGroupCacheUtils.getChannelGroupById(getActivity(),channel.getCid());
						String myUid = PreferencesUtils.getString(getActivity(),
								"userID");
						if (channelGroup != null && !channelGroup.getOwner().equals(myUid)){
							it.remove();
						}
					}
				}
			}

			List<ChannelOperationInfo> hideChannelOpList = ChannelOperationCacheUtils
					.getHideChannelOpList(getActivity());
			// 如果隐藏的频道中有未读消息则取消隐藏
			if (hideChannelOpList != null) {
				for (int i = 0; i < hideChannelOpList.size(); i++) {
					String cid = hideChannelOpList.get(i).getCid();
					int index = channelList.indexOf(new Channel(cid));
					if (index != -1) {
						Channel channel = channelList.get(index);
						if (channel.getNewestMid() != null
								&& !MsgReadIDCacheUtils.isMsgHaveRead(
										getActivity(), cid,
										channel.getNewestMid())) {
							ChannelOperationCacheUtils.setChannelHide(
									getActivity(), cid, false);
						} else {
							channelList.remove(index); // 如果没有未读消息则删除
						}
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
						setTopChannelList.add(channelList.get(index));
						channelList.remove(index);
					}
				}
			}

			// 所有显得的频道进行统一排序
			Collections.sort(channelList, new SortComparator());
			channelList.addAll(0, setTopChannelList);
		}
		displayChannelList = channelList;
	}

	/**
	 * 缓存从服务器获取的最新消息
	 * 
	 * @param getNewMsgsResult
	 */
	private void cacheNewMsgs(GetNewMsgsResult getNewMsgsResult) {
		// TODO Auto-generated method stub
		if (getNewMsgsResult != null) {
			List<Channel> channelList = getCacheData();
			for (int i = 0; i < channelList.size(); i++) {
				String cid = channelList.get(i).getCid();
				List<Msg> newMsgList = getNewMsgsResult.getNewMsgList(cid);
				if (newMsgList.size() > 0) {
					MsgCacheUtil.saveMsgList(getActivity(), newMsgList, ""); // 获取的消息需要缓存
					String myUid = PreferencesUtils.getString(getActivity(),
							"userID");
					// 当会话中最后一条消息为自己发出的时候，将此消息存入已读消息列表，解决最新消息为自己发出，仍识别为未读的问题
					if (newMsgList.get(newMsgList.size() - 1).getUid()
							.equals(myUid)) {
						MsgReadIDCacheUtils.saveReadedMsg(getActivity(), cid,
								newMsgList.get(newMsgList.size() - 1).getMid());
					}
				}
			}
		}
	}

	/**
	 * 获取缓存中不存在的群组信息
	 */
	private void getGroupInfo() {
		// TODO Auto-generated method stub
		if (NetUtils.isNetworkConnected(getActivity())){
			List<Channel> channelList = getCacheData();// 获取缓存中的数据
			if (channelList.size() > 0 && ((MyApplication) getActivity().getApplicationContext()).getIsContactReady()) {
				List<ChannelGroup> currentChannelGroupList = new ArrayList<ChannelGroup>();
				for (int i = 0; i < channelList.size(); i++) {
					Channel channel = channelList.get(i);
					if (channel.getType().equals("GROUP")) {
						ChannelGroup channelGroup = new ChannelGroup(channel);
						currentChannelGroupList.add(channelGroup);
					}
				}
				List<ChannelGroup> cacheChannelGroupList = ChannelGroupCacheUtils
						.getAllChannelGroupList(getActivity());
				currentChannelGroupList.removeAll(cacheChannelGroupList);
				if (currentChannelGroupList.size() > 0) {
					String[] cidArray = new String[currentChannelGroupList.size()];
					for (int i = 0; i < currentChannelGroupList.size(); i++) {
						cidArray[i] = currentChannelGroupList.get(i).getCid();
					}
					ChatAPIService apiService = new ChatAPIService(getActivity());
					apiService.setAPIInterface(new WebService());
					apiService.getChannelGroupList(cidArray);
				}
			}
		}
	}

	private void handMessage() {
		// TODO Auto-generated method stub
		handler = new Handler() {

			@Override
			public void handleMessage(Message msg) {
				// TODO Auto-generated method stub
				switch (msg.what) {
				case RECEIVE_MSG:
					/** 接收到新的消息 **/
					Msg receivedMsg = (Msg) msg.obj;
					Channel receiveMsgChannel = ChannelCacheUtils.getChannel(
							getActivity(), receivedMsg.getCid());
					if (receiveMsgChannel == null) {
						getChannelContent();
					} else {
						cacheReceiveMsg(receiveMsgChannel, receivedMsg);
						addChannelToList(receivedMsg, receiveMsgChannel);
						sortChannelList(displayChannelList);
						adapter.notifyDataSetChanged();
						refreshIndexNotify();
					}

					break;
				case RERESH_GROUP_ICON:
					if (adapter != null) {
						adapter.notifyDataSetChanged();
					}
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
	 * @param receiveMsgChannel
	 * @param receivedMsg
	 */
	private void cacheReceiveMsg(Channel receiveMsgChannel, Msg receivedMsg) {
		// TODO Auto-generated method stub
		Msg channelNewMsg = MsgCacheUtil.getNewMsg(getActivity(),
				receivedMsg.getCid());
		MsgCacheUtil.saveMsg(getActivity(), receivedMsg);
		if (channelNewMsg == null) {
			MsgMatheSetCacheUtils.add(getActivity(),
					receiveMsgChannel.getCid(),
					new MatheSet(receivedMsg.getMid(), receivedMsg.getMid()));
		} else {
			MsgMatheSetCacheUtils.add(getActivity(),
					receiveMsgChannel.getCid(),
					new MatheSet(channelNewMsg.getMid(), receivedMsg.getMid()));
		}
		String userID = PreferencesUtils.getString(getActivity(), "userID");
		boolean isMyMsg = receivedMsg.getUid().equals(userID);
		/** 判断是否是当前查看的频道的信息或者自己发出的信息 **/
		if (receivedMsg.getCid().equals(channelIdInOpen) || isMyMsg) {
			MsgReadIDCacheUtils.saveReadedMsg(getActivity(),
					receivedMsg.getCid(), receivedMsg.getMid());
		}

	}

	/**
	 * 将推送消息所属的channel添加到channelList
	 *
	 * @param receivedMsg
	 * @param receiveMsgChannel
	 */
	private void addChannelToList(Msg receivedMsg, Channel receiveMsgChannel) {
		// TODO Auto-generated method stub
		boolean isInChannelList = false;
		for (int i = 0; i < displayChannelList.size(); i++) {
			Channel channel = displayChannelList.get(i);
			if (receivedMsg.getCid().equals(channel.getCid())) {
				isInChannelList = true;
				receiveMsgChannel = channel;
				break;
			}
		}
		if (!isInChannelList) {// channel没在当前channelList中
			receiveMsgChannel = ChannelCacheUtils.getChannel(getActivity(),
					receivedMsg.getCid());
			List<Msg> newMsgList = MsgCacheUtil.getHistoryMsgList(
					getActivity(), receivedMsg.getCid(), "", 15);
			receiveMsgChannel.setNewMsgList(newMsgList);
			ChannelOperationCacheUtils.setChannelHide(getActivity(),
					receivedMsg.getCid(), false);
			displayChannelList.add(receiveMsgChannel);
		} else {
			receiveMsgChannel.addReceivedNewMsg(receivedMsg);
		}
	}

	/**
	 * 注册接收消息的广播
	 */
	private void registerMsgReceiver() {
		// TODO Auto-generated method stub
		if (msgReceiver == null) {
			msgReceiver = new MsgReceiver(getActivity(), handler);
			IntentFilter filter = new IntentFilter();
			filter.addAction("com.inspur.msg");
			getActivity().registerReceiver(msgReceiver, filter);
		}
	}

	/**
	 * 显示获取的数据
	 */
	private void displayData() {
		RelativeLayout noChatLayout = (RelativeLayout) rootView
				.findViewById(R.id.no_chat_layout);
		if (displayChannelList.size() == 0) {
			noChatLayout.setVisibility(View.VISIBLE);
		} else {
			noChatLayout.setVisibility(View.GONE);
		}
		if (adapter == null) {
			adapter = new Adapter();
			msgListView.setAdapter(adapter);
		} else {
			adapter.notifyDataSetChanged();
		}
		msgListView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				// TODO Auto-generated method stub
				Channel channel = displayChannelList.get(position);
				channelIdInOpen = channel.getCid();
				String channelType = channel.getType();
				Bundle bundle = new Bundle();
				bundle.putString("title", channel.getTitle());
				bundle.putString("channelId", channel.getCid());
				bundle.putString("channelType", channelType);
				if (channelType.equals("GROUP") || channelType.equals("DIRECT") || channelType.equals("SERVICE")) {
					IntentUtils.startActivity(getActivity(),
							ChannelActivity.class, bundle);
				}else {
					ToastUtils.show(getActivity(),
							R.string.not_support_open_channel);
				}
				setChannelAllMsgRead(channel);
				refreshIndexNotify();

			}

		});

		msgListView.setOnItemLongClickListener(new OnItemLongClickListener() {

			@Override
			public boolean onItemLongClick(AdapterView<?> parent, View view,
					int position, long id) {
				// TODO Auto-generated method stub
				showChannelOperationDlg(position);
				return true;
			}

		});
		refreshIndexNotify();
	}

	/**
	 * 将频道的消息置为已读
	 * 
	 * @param channel
	 */
	private void setChannelAllMsgRead(Channel channel) {
		// TODO Auto-generated method stub
		MsgReadIDCacheUtils.saveReadedMsg(getActivity(),
				channel.getCid(), channel.getNewestMid());
		int position = displayChannelList.indexOf(channel);
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
			channelTimeText.setTextColor(getResources().getColor(
					R.color.msg_content_color));
		}
	}

	/**
	 * 弹出频道操作选择框
	 * 
	 * @param position
	 */
	private void showChannelOperationDlg(final int position) {
		// TODO Auto-generated method stub
		final MyDialog oprationDlg = new MyDialog(getActivity(),
				R.layout.dialog_channel_operation, R.style.userhead_dialog_bg);
		final boolean isChannelSetTop = ChannelOperationCacheUtils
				.isChannelSetTop(getActivity(), displayChannelList
						.get(position).getCid());
		TextView setTopChannelText = (TextView) oprationDlg
				.findViewById(R.id.set_top_text);
		if (isChannelSetTop) {
			setTopChannelText.setText(getActivity().getString(
					R.string.cancel_top));
		}
		setTopChannelText.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				oprationDlg.dismiss();
				boolean isSetTop = !isChannelSetTop;
				ChannelOperationCacheUtils.setChannelTop(getActivity(),
						displayChannelList.get(position).getCid(), isSetTop);
				sortChannelList(displayChannelList);
				adapter.notifyDataSetChanged();
			}
		});

		(oprationDlg.findViewById(R.id.hide_text))
				.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View v) {
						// TODO Auto-generated method stub
						oprationDlg.dismiss();
						ChannelOperationCacheUtils.setChannelHide(
								getActivity(), displayChannelList.get(position)
										.getCid(), true);
						// 当隐藏会话时，把该会话的所有消息置为已读
						MsgReadIDCacheUtils
								.saveReadedMsg(getActivity(),
										displayChannelList.get(position)
												.getCid(), displayChannelList
												.get(position).getNewestMid());
						displayChannelList.remove(position);
						adapter.notifyDataSetChanged();
					}
				});
		oprationDlg.show();
	}

	/**
	 * 设置消息tab页面的小红点（未读消息提醒）的显示
	 */
	private void refreshIndexNotify() {
		int unReadCount = 0;
		if (displayChannelList != null) {
			for (int i = 0; i < displayChannelList.size(); i++) {
				unReadCount += MsgReadIDCacheUtils.getNotReadMsgCount(
						getActivity(), displayChannelList.get(i).getCid());
			}
		}
		IndexActivity.showNotifyIcon(unReadCount);
	}

	private class Adapter extends BaseAdapter {


		@Override
		public int getCount() {
			// TODO Auto-generated method stub
			return displayChannelList.size();
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
			convertView = LayoutInflater.from(getActivity()).inflate(R.layout.msg_item_view, null);
			Channel channel = displayChannelList.get(position);
			setChannelBg(channel, convertView);
			setChannelIcon(channel, convertView);
			setChannelTitle(channel, convertView);
			setChannelMsgReadStateUI(channel, convertView);
			// 显示channel是否免打扰状态
			boolean isChannelNotDisturb = ChannelCacheUtils.isChannelNotDisturb(getActivity(),
					channel.getCid());
			ImageView dndImg = (ImageView) convertView
					.findViewById(R.id.msg_dnd_img);
			dndImg.setVisibility(isChannelNotDisturb ? View.VISIBLE : View.GONE);
			return convertView;
		}


		/**
		 * 设置Channel的Icon
		 * 
		 * @param channel
		 */
		private void setChannelIcon(Channel channel, View convertView) {
			// TODO Auto-generated method stub
			CircleFrameLayout channelPhotoLayout = (CircleFrameLayout) convertView
					.findViewById(R.id.channel_photo_layout);
			Integer defaultIcon = -1; // 默认显示图标
			if (channel.getType().equals("GROUP")) {
				DisplayChannelGroupIcon.show(getActivity(),channel.getCid(),channelPhotoLayout);
			} else {
				String iconUrl = "";// Channel头像的uri
				View channelPhotoView = LayoutInflater.from(getActivity()).inflate(R.layout.chat_msg_session_photo_one, null);
				ImageView photoImg = (ImageView) channelPhotoView.findViewById(R.id.photo_img1);
				if (channel.getType().equals("DIRECT")) {
					defaultIcon = R.drawable.icon_person_default;
					iconUrl = DirectChannelUtils.getDirectChannelIcon(
							getActivity(), channel.getTitle());
				} else if (channel.getType().equals("SERVICE")) {
					defaultIcon = R.drawable.icon_person_default;
					iconUrl = DirectChannelUtils.getRobotIcon(getActivity(), channel.getTitle());
				} else {
					defaultIcon = R.drawable.icon_channel_group_default;
					iconUrl = channel.getIcon();
				}
				new ImageDisplayUtils(getActivity(), defaultIcon).display(
						photoImg, iconUrl);
				channelPhotoLayout.addView(channelPhotoView);
			}


		}

		/**
		 * 设置频道的背景色
		 *
		 * @param channel
		 * @param convertView
		 */
		private void setChannelBg(Channel channel, View convertView) {
			// TODO Auto-generated method stub
			RelativeLayout mainLayout = (RelativeLayout) convertView
					.findViewById(R.id.main_layout);
			if (ChannelOperationCacheUtils.isChannelSetTop(getActivity(),
					channel.getCid())) {
				mainLayout
						.setBackgroundResource(R.drawable.selector_set_top_msg_list);
			} else {
				mainLayout
						.setBackgroundResource(R.drawable.selector_list);
			}
		}

		/**
		 * 设置Channel的title
		 *
		 * @param channel
		 * @param convertView
		 */
		private void setChannelTitle(Channel channel, View convertView) {
			TextView channelTitleText = (TextView) convertView
					.findViewById(R.id.name_text);
			String title = "";
			if (channel.getType().equals("DIRECT")) {
				title = DirectChannelUtils.getDirectChannelTitle(getActivity(),
						channel.getTitle());
			} else if (channel.getType().equals("SERVICE")) {
				title = DirectChannelUtils.getRobotInfo(getActivity(), channel.getTitle()).getName();
			} else {
				title = channel.getTitle();
			}
			channelTitleText.setText(title);
		}

		/**
		 * 设置频道未读和已读消息的显示
		 *
		 * @param channel
		 */
		private void setChannelMsgReadStateUI(final Channel channel, View convertView) {
			// TODO Auto-generated method stub
			TextView channelTimeText = (TextView) convertView
					.findViewById(R.id.time_text);
			RelativeLayout channelNotReadCountLayout = (RelativeLayout) convertView
					.findViewById(R.id.msg_new_layout);
			TextView channelNotReadCountText = (TextView) convertView
					.findViewById(R.id.msg_new_text);
			TextView channelTitleText = (TextView) convertView
					.findViewById(R.id.name_text);
			TextView channelContentText = (TextView) convertView
					.findViewById(R.id.content_text);
			int unReadCount = MsgReadIDCacheUtils.getNotReadMsgCount(
					getActivity(), channel.getCid());
			channelTimeText.setText(TimeUtils.getDisplayTime(
					getActivity(), channel.getLastUpdate()));
			channelContentText.setText(channel
					.getNewestMsgContent(getActivity()));
			TransHtmlToTextUtils.stripUnderlines(channelContentText,
					R.color.msg_content_color);
			if (unReadCount == 0) {
				channelNotReadCountLayout.setVisibility(View.INVISIBLE);
				channelTitleText.getPaint().setFakeBoldText(false);
				channelContentText.setTextColor(getResources().getColor(
						R.color.msg_content_color));
				channelTimeText.setTextColor(getResources().getColor(
						R.color.msg_content_color));
			} else {
				channelNotReadCountLayout.setVisibility(View.VISIBLE);
				channelTitleText.getPaint().setFakeBoldText(true);
				channelContentText.setTextColor(getResources().getColor(
						R.color.black));
				channelTimeText.setTextColor(getResources().getColor(
						R.color.msg_time_color));
				channelNotReadCountText.setText(unReadCount > 99 ? "99+" : "" + unReadCount);
			}
			TipsView.attach(channelNotReadCountLayout,
					new TipsView.Listener() {

						@Override
						public void onStart() {
							// TODO Auto-generated method stub
							pullToRefreshLayout.setCanTouch(false);
						}

						@Override
						public void onComplete() {
							// TODO Auto-generated method stub
							pullToRefreshLayout.setCanTouch(true);
							setChannelAllMsgRead(channel);
							refreshIndexNotify();
						}

						@Override
						public void onCancel() {
							// TODO Auto-generated method stub
							pullToRefreshLayout.setCanTouch(true);

						}
					});
		}
	}


	class WebService extends APIInterfaceInstance {

		@Override
		public void returnChannelListSuccess(
				GetChannelListResult getChannelListResult) {
			// TODO Auto-generated method stub
			if (getActivity() != null) {
				List<Channel> channelList = getChannelListResult
						.getChannelList();
				ChannelCacheUtils.clearChannel(getActivity());
				ChannelCacheUtils.saveChannelList(getActivity(), channelList);
				getChannelInfoResult(channelList);
				apiService.getNewMsgs();
			}

		}

		@Override
		public void returnChannelListFail(String error,int errorCode) {
			// TODO Auto-generated method stub
			if (getActivity() != null) {
				pullToRefreshLayout.refreshFinish(PullToRefreshLayout.FAIL);
				WebServiceMiddleUtils.hand(getActivity(), error,errorCode);
				handData();
			}

		}

		@Override
		public void returnSearchChannelGroupSuccess(
				GetSearchChannelGroupResult getSearchChannelGroupResult) {
			saveChannelInfo(getSearchChannelGroupResult
					.getSearchChannelGroupList());
			//为了刷新群组头像
			adapter.notifyDataSetChanged();
		}

		@Override
		public void returnSearchChannelGroupFail(String error,int errorCode) {
		}

		@Override
		public void returnNewMsgsSuccess(GetNewMsgsResult getNewMsgsResult) {
			// TODO Auto-generated method stub
			if (getActivity() != null) {
				pullToRefreshLayout.refreshFinish(PullToRefreshLayout.SUCCEED);
				cacheNewMsgs(getNewMsgsResult);
				handData();
			}

		}

		@Override
		public void returnNewMsgsFail(String error,int errorCode) {
			// TODO Auto-generated method stub
			if (getActivity() != null) {
				pullToRefreshLayout.refreshFinish(PullToRefreshLayout.FAIL);
				handData();
				WebServiceMiddleUtils.hand(getActivity(), error,errorCode);
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
			if (command.equals("refresh_session_list")) {
				getChannelContent();
			} else if (command.equals("sort_session_list")) {
				sortChannelList(displayChannelList);
				adapter.notifyDataSetChanged();
			} else if (command.equals("set_all_message_read")) {
				setAllChannelMsgRead();
			} else if (command.equals("websocket_status")) {
				String socketStatus = intent.getExtras().getString("status");
				showSocketStatusInTitle(socketStatus);

			}

		}

	}

	private void showSocketStatusInTitle(String socketStatus){
		if (socketStatus.equals("socket_connecting")){
			titleText.setText(R.string.socket_connecting);
		}else if (socketStatus.equals(Socket.EVENT_CONNECT)){
			titleText.setText(R.string.communicate);
		}else if(socketStatus.equals(Socket.EVENT_DISCONNECT) || socketStatus.equals(Socket.EVENT_CONNECT_ERROR)){
			titleText.setText(R.string.socket_close);
		}
	}

	/**
	 * 将所有频道的消息置为已读
	 */
	private void setAllChannelMsgRead() {
		// TODO Auto-generated method stub
		for (int i = 0; i < displayChannelList.size(); i++) {
			Channel channel = displayChannelList.get(i);
			MsgReadIDCacheUtils.saveReadedMsg(getActivity(), channel.getCid(),
					channel.getNewestMid());
		}
		adapter.notifyDataSetChanged();
	}

	/**
	 * 更新Channel的input信息
	 * 
	 * @param searchChannelGroupList
	 */
	public void saveChannelInfo(List<ChannelGroup> searchChannelGroupList) {
		Map<String, String> channelMap = new HashMap<String, String>();
		for (int i = 0; i < searchChannelGroupList.size(); i++) {
			ChannelGroup channelGroup = searchChannelGroupList.get(i);
			channelMap.put(channelGroup.getCid(), channelGroup.getInputs());
			if (channelGroup.getType().equals("GROUP")){
				ChannelGroupCacheUtils.saveChannelGroup(getActivity(),channelGroup);
			}
		}
		List<Channel> channelList = ChannelCacheUtils
				.getCacheChannelList(getActivity());
		for (int i = 0; i < channelList.size(); i++) {
			Channel channel = channelList.get(i);
			channel.setInputs(channelMap.get(channel.getCid()));
		}
		ChannelCacheUtils.saveChannelList(getActivity(), channelList);
	}

	/**
	 * 根据cid数组获取Channel信息
	 * 
	 * @param channelList
	 */
	public void getChannelInfoResult(List<Channel> channelList) {
		String[] cidArray = new String[channelList.size()];
		for (int i = 0; i < channelList.size(); i++) {
			cidArray[i] = channelList.get(i).getCid();
		}

		apiService.getChannelGroupList(cidArray);
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

	@Override
	public void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		if (msgReceiver != null) {
			getActivity().unregisterReceiver(msgReceiver);
			msgReceiver = null;
		}
		if (messageFragmentReceiver != null) {
			getActivity().unregisterReceiver(messageFragmentReceiver);
			messageFragmentReceiver = null;
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
						&& NetUtils.isNetworkConnected(getActivity())) {
					creatGroupChannel(peopleArray);
				}

			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				ToastUtils.show(getActivity(),
						getActivity().getString(R.string.creat_group_fail));
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
						bundle.putString("channelId", channelGroup.getCid());
						bundle.putString("channelType", channelGroup.getType());
						bundle.putString("title", channelGroup.getChannelName());
						IntentUtils.startActivity(getActivity(),
								ChannelActivity.class, bundle);
						ChannelGroupCacheUtils.saveChannelGroup(getActivity(),
								channelGroup);
						getChannelContent();
					}

					@Override
					public void createGroupChannelFail() {
						// TODO Auto-generated method stub

					}
				});
	}

	@Override
	public void onRefresh(PullToRefreshLayout pullToRefreshLayout) {
		// TODO Auto-generated method stub
		if (NetUtils.isNetworkConnected(getActivity())) {
			apiService.getChannelList();
		} else {
			pullToRefreshLayout.refreshFinish(PullToRefreshLayout.FAIL);
		}
	}

	@Override
	public void onLoadMore(PullToRefreshLayout pullToRefreshLayout) {
		// TODO Auto-generated method stub

	}

}
