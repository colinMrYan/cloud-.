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
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
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
import com.inspur.emmcloud.bean.PVCollectModel;
import com.inspur.emmcloud.broadcastreceiver.MsgReceiver;
import com.inspur.emmcloud.callback.CommonCallBack;
import com.inspur.emmcloud.config.MyAppConfig;
import com.inspur.emmcloud.ui.IndexActivity;
import com.inspur.emmcloud.ui.contact.ContactSearchActivity;
import com.inspur.emmcloud.util.AppTitleUtils;
import com.inspur.emmcloud.util.ChannelCacheUtils;
import com.inspur.emmcloud.util.ChannelGroupCacheUtils;
import com.inspur.emmcloud.util.ChannelGroupIconUtils;
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
import com.inspur.emmcloud.util.PVCollectModelCacheUtils;
import com.inspur.emmcloud.util.PreferencesByUserAndTanentUtils;
import com.inspur.emmcloud.util.PreferencesUtils;
import com.inspur.emmcloud.util.StringUtils;
import com.inspur.emmcloud.util.TimeUtils;
import com.inspur.emmcloud.util.ToastUtils;
import com.inspur.emmcloud.util.TransHtmlToTextUtils;
import com.inspur.emmcloud.util.UriUtils;
import com.inspur.emmcloud.util.WebServiceMiddleUtils;
import com.inspur.emmcloud.widget.CircleImageView;
import com.inspur.emmcloud.widget.dialogs.MyDialog;
import com.inspur.emmcloud.widget.pullableview.PullToRefreshLayout;
import com.inspur.emmcloud.widget.pullableview.PullToRefreshLayout.OnRefreshListener;
import com.inspur.emmcloud.widget.pullableview.PullableListView;

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
	private static final int SORT_CHANNEL_COMPLETE= 3;
    private View rootView;
    private LayoutInflater inflater;
    private PullableListView msgListView;
    private PullToRefreshLayout pullToRefreshLayout;
    private ChatAPIService apiService;
    private List<Channel> displayChannelList = new ArrayList<Channel>();
    private Adapter adapter;
    private MsgReceiver msgReceiver;
    private Handler handler;
    private MessageFragmentReceiver messageFragmentReceiver;
    private TextView titleText;
    private boolean isHaveCreatGroupIcon = false;

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

	/**
	 * 记录用户点击的频道
	 */
	private void recordUserClickContact() {
		PVCollectModel pvCollectModel = new PVCollectModel("contact","communicate");
		PVCollectModelCacheUtils.saveCollectModel(getActivity(),pvCollectModel);
	}

	@Override
	public void onAttach(Context context) {
		super.onAttach(context);
		CommonCallBack callBack = (CommonCallBack)context;
		callBack.execute();
	}

	@Override
	public void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
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
        registerMessageFragmentReceiver();
        getChannelContent();
		sortChannelList(getCacheData());// 对Channel 进行排序
        showMessageButtons();
        EventBus.getDefault().register(this);
    }

	/**
	 * 展示创建
	 */
	private void showMessageButtons() {
		String tabBarInfo = PreferencesByUserAndTanentUtils.getString(getActivity(), "app_tabbar_info_current", "");
		//第一次登录时有tabBarInfo会为“”，会导致JSON waring
		if(StringUtils.isBlank(tabBarInfo)){
			return;
		}
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
					if (!property.isCanCreate()) {
						rootView.findViewById(R.id.find_friends_btn).setVisibility(View.GONE);
						rootView.findViewById(R.id.add_img).setVisibility(View.GONE);
					}
					if (!property.isCanContact()) {
						rootView.findViewById(R.id.find_friends_btn).setVisibility(View.GONE);
						rootView.findViewById(R.id.address_list_img).setVisibility(View.GONE);
					}
				}
			}
		}

	}

    private void initView() {
        // TODO Auto-generated method stub
		handMessage();
        apiService = new ChatAPIService(getActivity());
        apiService.setAPIInterface(new WebService());
        inflater = (LayoutInflater) getActivity().getSystemService(
                getActivity().LAYOUT_INFLATER_SERVICE);
        rootView = inflater.inflate(R.layout.fragment_message, null);
        (rootView.findViewById(R.id.address_list_img))
                .setOnClickListener(onViewClickListener);
        (rootView.findViewById(R.id.add_img))
                .setOnClickListener(onViewClickListener);
        (rootView.findViewById(R.id.find_friends_btn))
                .setOnClickListener(onViewClickListener);
        titleText = (TextView) rootView.findViewById(R.id.header_text);
        pullToRefreshLayout = (PullToRefreshLayout) rootView
                .findViewById(R.id.refresh_view);
        pullToRefreshLayout.setOnRefreshListener(MessageFragment.this);
        msgListView = (PullableListView) rootView.findViewById(R.id.msg_list);
        msgListView = (PullableListView) rootView.findViewById(R.id.msg_list);
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
                            ChannelActivity.class, bundle);
                } else {
                    ToastUtils.show(getActivity(),
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
					recordUserClickContact();
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
		if (NetUtils.isNetworkConnected(getActivity(),false)) {
			apiService.getChannelList();
		}
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
     * 为群组创建头像
     */
    private void createAllGroupIcon() {
        // TODO Auto-generated method stub
        List<Channel> channelList = getCacheData();// 获取缓存中的数据
        if (((MyApplication) getActivity().getApplicationContext()).getIsContactReady()) {
            isHaveCreatGroupIcon = true;
            ChannelGroupIconUtils.getInstance().create(getActivity(), channelList,
                    handler);
        }
    }

    /**
     * 为单个群组创建头像
     */
    private void createGroupIcon(List<Channel> channelList) {
        if (((MyApplication) getActivity().getApplicationContext()).getIsContactReady()) {
            ChannelGroupIconUtils.getInstance().create(getActivity(), channelList,
                    handler);
        }
    }

    /**
     * channel 显示排序
     */
    private void sortChannelList(final List<Channel> channelList) {
        // TODO Auto-generated method stub
		new Thread(new Runnable() {
			@Override
			public void run() {
				Channel channelss = new Channel();
				channelss.setCid("611");
				if (channelList.size() > 0) {
					Iterator<Channel> it = channelList.iterator();
					//将没有消息的单聊和没有消息的但不是自己创建的群聊隐藏掉
					while (it.hasNext()) {
						Channel channel = it.next();
						channel.setIsSetTop(false);
						int unReadCount = MsgReadIDCacheUtils.getNotReadMsgCount(
								getActivity(), channel.getCid());
						channel.setUnReadCount(unReadCount);
						setChannelDisplayTitle(channel);
						if (channel.getNewMsgList().size() == 0) {
							if (channel.getType().equals("DIRECT")) {
								it.remove();
							} else if (channel.getType().equals("GROUP")) {
								ChannelGroup channelGroup = ChannelGroupCacheUtils.getChannelGroupById(getActivity(), channel.getCid());
								String myUid = ((MyApplication) getActivity().getApplicationContext()).getUid();
								if (channelGroup != null && !channelGroup.getOwner().equals(myUid)) {
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
				displayChannelList = channelList;
				handler.sendEmptyMessage(SORT_CHANNEL_COMPLETE);
			}
		}).start();

	}

    /**
     * 设置session的显示名称
     *
     * @param channel
     */
    private void setChannelDisplayTitle(Channel channel) {
        String title = "";
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
                    String myUid = ((MyApplication) getActivity().getApplicationContext()).getUid();
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
                        }

						break;
					case RERESH_GROUP_ICON:
						boolean isCreateNewGroupIcon = (Boolean) msg.obj;
						if (adapter != null && isCreateNewGroupIcon) {
							adapter.notifyDataSetChanged();
						}
						break;
					case SORT_CHANNEL_COMPLETE:
						displayData();// 展示数据
						registerMsgReceiver();// 注册接收消息的广播
						((MyApplication) getActivity().getApplication()).startWebSocket();// 启动webSocket推送
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
		if ( isMyMsg) {
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
        ((LinearLayout) rootView
                .findViewById(R.id.no_chat_layout)).setVisibility((displayChannelList.size() == 0) ? View.VISIBLE : View.GONE);
        if (adapter == null) {
            adapter = new Adapter();
            msgListView.setAdapter(adapter);
        } else {
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
        MsgReadIDCacheUtils.saveReadedMsg(getActivity(),
                channel.getCid(), channel.getNewestMid());
        int position = displayChannelList.indexOf(channel);
        displayChannelList.get(position).setUnReadCount(0);
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
                        displayData();
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
                unReadCount += displayChannelList.get(i).getUnReadCount();
            }
        }
        IndexActivity.showNotifyIcon(unReadCount);
    }

    static class ViewHolder {
        RelativeLayout mainLayout;
        CircleImageView channelPhotoImg;
        TextView channelContentText;
        TextView channelTitleText;
        TextView channelTimeText;
        RelativeLayout channelNotReadCountLayout;
        TextView channelNotReadCountText;
        ImageView dndImg;
    }


	private class Adapter extends BaseAdapter {

		ViewHolder holder;

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
            ViewHolder holder;
            if (convertView == null) {
                holder = new ViewHolder();
                convertView = LayoutInflater.from(getActivity()).inflate(R.layout.msg_item_view, null);
                holder.mainLayout = (RelativeLayout) convertView
                        .findViewById(R.id.main_layout);
                holder.channelPhotoImg = (CircleImageView) convertView
                        .findViewById(R.id.msg_img);
                holder.channelTitleText = (TextView) convertView
                        .findViewById(R.id.name_text);
                holder.channelContentText = (TextView) convertView
                        .findViewById(R.id.content_text);
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
            Channel channel = displayChannelList.get(position);
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
        private void setChannelIcon(Channel channel, CircleImageView channelPhotoImg) {
            // TODO Auto-generated method stub
            Integer defaultIcon = R.drawable.icon_channel_group_default; // 默认显示图标
            String iconUrl = channel.getIcon();// Channel头像的uri
            if (channel.getType().equals("GROUP")) {
                File file = new File(MyAppConfig.LOCAL_CACHE_PHOTO_PATH,
                        UriUtils.tanent + channel.getCid() + "_100.png1");
                if (file.exists()) {
                    iconUrl = "file://" + file.getAbsolutePath();
                    new ImageDisplayUtils().displayNoCachePic(channelPhotoImg, iconUrl, defaultIcon);
                    return;
                }
            } else if (channel.getType().equals("DIRECT")) {
                defaultIcon = R.drawable.icon_person_default;
                iconUrl = DirectChannelUtils.getDirectChannelIcon(
                        getActivity(), channel.getTitle());
            } else if (channel.getType().equals("SERVICE")) {
                defaultIcon = R.drawable.icon_person_default;
                iconUrl = DirectChannelUtils.getRobotIcon(getActivity(), channel.getTitle());
            }
            new ImageDisplayUtils(getActivity(), defaultIcon).display(
                    channelPhotoImg, iconUrl);


		}


        /**
         * 设置频道未读和已读消息的显示
         *
         * @param channel
         */
        private void setChannelMsgReadStateUI(final Channel channel, ViewHolder holder) {
            // TODO Auto-generated method stub
            int unReadCount = channel.getUnReadCount();
            holder.channelTimeText.setText(TimeUtils.getDisplayTime(
                    getActivity(), channel.getLastUpdate()));
            holder.channelContentText.setText(channel
                    .getNewestMsgContent(getActivity(),holder.channelContentText));
            TransHtmlToTextUtils.stripUnderlines(holder.channelContentText,
                    R.color.msg_content_color);
            boolean isHasUnReadMsg = (unReadCount != 0);
            holder.channelNotReadCountLayout.setVisibility(isHasUnReadMsg ? View.VISIBLE : View.INVISIBLE);
            holder.channelTitleText.getPaint().setFakeBoldText(isHasUnReadMsg);
            holder.channelContentText.setTextColor(isHasUnReadMsg ? getResources().getColor(
                    R.color.black) : getResources().getColor(
                    R.color.msg_content_color));
            holder.channelTimeText.setTextColor(isHasUnReadMsg ? getResources().getColor(
                    R.color.msg_time_color) : getResources().getColor(
                    R.color.msg_content_color));
            if (isHasUnReadMsg) {
                holder.channelNotReadCountText.setText(unReadCount > 99 ? "99+" : "" + unReadCount);
            }
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
				List<Channel> cacheChannelList = ChannelCacheUtils
						.getCacheChannelList(getActivity());
				ChannelCacheUtils.clearChannel(getActivity());
				ChannelCacheUtils.saveChannelList(getActivity(), channelList);
				getChannelInfoResult(channelList);
				apiService.getNewMsgs();
				if (!isHaveCreatGroupIcon){
					createAllGroupIcon();
				}else {
					channelList.removeAll(cacheChannelList);
					createGroupIcon(channelList);
				}
			}

		}

		@Override
		public void returnChannelListFail(String error,int errorCode) {
			// TODO Auto-generated method stub
			if (getActivity() != null) {
				pullToRefreshLayout.refreshFinish(PullToRefreshLayout.FAIL);
				WebServiceMiddleUtils.hand(getActivity(), error,errorCode);
				sortChannelList(getCacheData());// 对Channel 进行排序
			}

		}

        @Override
        public void returnNewMsgsSuccess(GetNewMsgsResult getNewMsgsResult) {
            // TODO Auto-generated method stub
            if (getActivity() != null) {
                pullToRefreshLayout.refreshFinish(PullToRefreshLayout.SUCCEED);
                cacheNewMsgs(getNewMsgsResult);
                sortChannelList(getCacheData());// 对Channel 进行排序
            }

		}

		@Override
		public void returnSearchChannelGroupSuccess(
				GetSearchChannelGroupResult getSearchChannelGroupResult) {
			saveChannelInfo(getSearchChannelGroupResult
					.getSearchChannelGroupList());
		}

		@Override
		public void returnSearchChannelGroupFail(String error,int errorCode) {
		}


		@Override
		public void returnNewMsgsFail(String error,int errorCode) {
			// TODO Auto-generated method stub
			if (getActivity() != null) {
				pullToRefreshLayout.refreshFinish(PullToRefreshLayout.FAIL);
				sortChannelList(getCacheData());// 对Channel 进行排序
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
            if (command.equals("creat_group_icon")) {
                createAllGroupIcon();
            } else if (command.equals("refresh_session_list")) {
                getChannelContent();
            } else if (command.equals("sort_session_list")) {
                createAllGroupIcon();
                sortChannelList(getCacheData());
            } else if (command.equals("set_all_message_read")) {
                setAllChannelMsgRead();
            } else if (command.equals("websocket_status")) {
                String socketStatus = intent.getExtras().getString("status");
                showSocketStatusInTitle(socketStatus);
            } else if(command.equals("set_channel_message_read")){
				String cid =  intent.getExtras().getString("cid");
				String mid =  intent.getExtras().getString("mid");
				setChannelMsgRead(cid,mid);
			}

		}

	}

	private void showSocketStatusInTitle(String socketStatus){
		if (socketStatus.equals("socket_connecting")){
			titleText.setText(R.string.socket_connecting);
		}else if (socketStatus.equals(Socket.EVENT_CONNECT)){
			String appTabs = PreferencesByUserAndTanentUtils.getString(getActivity(),"app_tabbar_info_current","");
			if(!StringUtils.isBlank(appTabs)){
				titleText.setText(AppTitleUtils.getTabTitle(getActivity(),getClass().getSimpleName()));
			}else {
				titleText.setText(R.string.communicate);
			}
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
            channel.setUnReadCount(0);
        }
        adapter.notifyDataSetChanged();
    }

	/**
	 * 将单个频道消息置为已读
	 * @param cid
	 * @param mid
	 */
	private void setChannelMsgRead(String cid,String mid){
		MsgReadIDCacheUtils.saveReadedMsg(getActivity(), cid,
				mid);
		for (int i = 0; i < displayChannelList.size(); i++) {
			Channel channel = displayChannelList.get(i);
			if (channel.getCid().equals(cid)){
				channel.setUnReadCount(0);
				break;
			}
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
			if (channel.getType().equals("SERVICE")){
				int channelGroupIndex = searchChannelGroupList.indexOf(new ChannelGroup(channel));
				channel.setInputs(searchChannelGroupList.get(channelGroupIndex).getInputs());
				ChannelCacheUtils.saveChannel(getActivity(),channel);
			}
		}
	}

	/**
	 * 根据cid数组获取Channel信息
	 *
	 * @param channelList
	 */
	public void getChannelInfoResult(List<Channel> channelList) {
		String[] cidArray = new String[channelList.size()];
		for (int i = 0; i < channelList.size(); i++) {
			Channel channel = channelList.get(i);
			if (channel.getType().equals("SERVICE")){
				cidArray[i] = channelList.get(i).getCid();
			}
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
						bundle.putString("cid", channelGroup.getCid());
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
