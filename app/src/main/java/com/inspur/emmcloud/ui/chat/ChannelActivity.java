package com.inspur.emmcloud.ui.chat;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.lzy.imagepicker.ImagePicker;
import com.inspur.emmcloud.BaseActivity;
import com.inspur.emmcloud.MyApplication;
import com.inspur.emmcloud.R;
import com.inspur.emmcloud.api.APIInterfaceInstance;
import com.inspur.emmcloud.api.apiservice.ChatAPIService;
import com.inspur.emmcloud.bean.Channel;
import com.inspur.emmcloud.bean.GetFileUploadResult;
import com.inspur.emmcloud.bean.GetMeetingReplyResult;
import com.inspur.emmcloud.bean.GetNewMsgsResult;
import com.inspur.emmcloud.bean.GetNewsImgResult;
import com.inspur.emmcloud.bean.GetSendMsgResult;
import com.inspur.emmcloud.bean.Msg;
import com.inspur.emmcloud.bean.getMsgResult;
import com.inspur.emmcloud.broadcastreceiver.MsgReceiver;
import com.inspur.emmcloud.ui.contact.RobotInfoActivity;
import com.inspur.emmcloud.ui.contact.UserInfoActivity;
import com.inspur.emmcloud.util.ChannelCacheUtils;
import com.inspur.emmcloud.util.ConbineMsg;
import com.inspur.emmcloud.util.DirectChannelUtils;
import com.inspur.emmcloud.util.HandleMsgTextUtils;
import com.inspur.emmcloud.util.ImageDisplayUtils;
import com.inspur.emmcloud.util.InputMethodUtils;
import com.inspur.emmcloud.util.IntentUtils;
import com.inspur.emmcloud.util.JSONUtils;
import com.inspur.emmcloud.util.ListViewUtils;
import com.inspur.emmcloud.util.LogUtils;
import com.inspur.emmcloud.util.MsgCacheUtil;
import com.inspur.emmcloud.util.MsgRecourceUploadUtils;
import com.inspur.emmcloud.util.NetUtils;
import com.inspur.emmcloud.util.PreferencesUtils;
import com.inspur.emmcloud.util.RobotCacheUtils;
import com.inspur.emmcloud.util.StringUtils;
import com.inspur.emmcloud.util.TimeUtils;
import com.inspur.emmcloud.util.ToastUtils;
import com.inspur.emmcloud.util.URLMatcher;
import com.inspur.emmcloud.util.UriUtils;
import com.inspur.emmcloud.util.WebServiceMiddleUtils;
import com.inspur.emmcloud.widget.ECMChatInputMenu;
import com.inspur.emmcloud.widget.ECMChatInputMenu.ChatInputMenuListener;
import com.inspur.emmcloud.widget.pullableview.PullToRefreshLayout;
import com.inspur.emmcloud.widget.pullableview.PullToRefreshLayout.OnRefreshListener;
import com.inspur.emmcloud.widget.pullableview.PullableListView;


/**
 * com.inspur.emmcloud.ui.ChannelActivity
 * 
 * @author Fortune Yu; create at 2016年8月29日
 */
public class ChannelActivity extends BaseActivity implements OnRefreshListener {

	private static final int HAND_CALLBACK_MESSAGE = 1;
	private static final int GELLARY_RESULT = 2;
	private static final int CAMERA_RESULT = 3;
	private static final int MENTIONS_RESULT = 5;
	private static final int CHOOSE_FILE = 4;
	private PullableListView msgListView;
	private List<Msg> msgList;
	private Handler handler;
	private String channelId;
	private MsgReceiver msgResvier;
	private ChatAPIService apiService;
	private PullToRefreshLayout pullToRefreshLayout;
	private String channelType = "";
	private BroadcastReceiver refreshNameReceiver;
	private String title = "";
	private String uid = "";
	private ECMChatInputMenu chatInputMenu;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_channel);
		((MyApplication) getApplicationContext()).addActivity(this);
		init();
		handMessage();
		registeMsgReceiver();
		registeRefreshNameReceiver();
	}

	// Activity在SingleTask的启动模式下多次打开传递Intent无效，用此方法解决
	@Override
	protected void onNewIntent(Intent intent) {
		// TODO Auto-generated method stub
		super.onNewIntent(intent);
		setIntent(intent);
		getIntent().putExtras(intent);
		init();
	}

	private void init() {
		initData();
		initViews();
	}

	/**
	 * 初始化界面数据
	 */
	private void initData() {
		channelId = getIntent().getExtras().getString("channelId");

		channelType = getIntent().getExtras().getString("channelType");
		msgList = MsgCacheUtil.getHistoryMsgList(getApplicationContext(),
				channelId, "", 15);
		title = getIntent().getExtras().getString("title");
		LogUtils.JasonDebug("title=" + title);
		if (channelType.equals("DIRECT")) {
			title = DirectChannelUtils.getDirectChannelTitle(
					getApplicationContext(), title);
		}else if (channelType.equals("SERVICE")) {
			uid = DirectChannelUtils.getRobotInfo(getApplicationContext(), title).getId();
			title = DirectChannelUtils.getRobotInfo(getApplicationContext(), title).getName();
		}
	}

	/**
	 * 初始化Views
	 */
	private void initViews() {
		apiService = new ChatAPIService(ChannelActivity.this);
		apiService.setAPIInterface(new WebService());
		chatInputMenu = (ECMChatInputMenu) findViewById(R.id.chat_input_menu);
		if (channelType.equals("GROUP")) {
			chatInputMenu.setCanMention(true, channelId);
		}
		chatInputMenu.setChatInputMenuListener(new ChatInputMenuListener() {

			@Override
			public void onSetContentViewHeight(boolean isLock) {
				// TODO Auto-generated method stub
				final LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) pullToRefreshLayout
						.getLayoutParams();
				if (isLock) {
					params.height = pullToRefreshLayout.getHeight();
					params.weight = 0.0F;
				} else {
					new Handler().post(new Runnable() {
						@Override
						public void run() {
							params.weight = 1.0F;
						}
					});
				}
			}

			@Override
			public void onSendMsg(String content, List<String> mentionsUidList,
					List<String> mentionsUserNameList) {
				// TODO Auto-generated method stub
				sendTextMessage(content, mentionsUidList, mentionsUserNameList);
			}
		});
		handleChatInputMenu();
		pullToRefreshLayout = (PullToRefreshLayout) findViewById(R.id.refresh_view);
		((TextView) findViewById(R.id.header_text)).setText(title);
		pullToRefreshLayout.setOnRefreshListener(this);
		initMsgListView();
	}

	/**
	 * 处理chatInputMenu是否显示，以及显示几个Menu上的item
	 */
	private void handleChatInputMenu() {
		Channel channel = ChannelCacheUtils.getChannel(ChannelActivity.this,
				channelId);
		if ((channel!=null) && channel.getInputs().equals("0")) {
			// shareMsg();
			chatInputMenu.setVisibility(View.GONE);
		} else {
			chatInputMenu.updateMenuGrid(handleShowItems());
		}
	}

	// 分享
	// public void shareMsg(){
	// Intent intent = new Intent(Intent.ACTION_SEND);
	// intent.setType("text/plain");
	// // 查询所有可以分享的Activity
	// List<ResolveInfo> resInfo =
	// ChannelActivity.this.getPackageManager().queryIntentActivities(intent,
	// PackageManager.MATCH_DEFAULT_ONLY);
	// if (!resInfo.isEmpty()) {
	// List<Intent> targetedShareIntents = new ArrayList<Intent>();
	// for (ResolveInfo info : resInfo) {
	// Intent targeted = new Intent(Intent.ACTION_SEND);
	// targeted.setType("text/plain");
	// ActivityInfo activityInfo = info.activityInfo;
	// Log.v("logcat", "packageName=" + activityInfo.packageName + "Name=" +
	// activityInfo.name);
	// // 分享出去的内容
	// targeted.putExtra(Intent.EXTRA_TEXT, "这是我的分享内容" + getPackageName());
	// // 分享出去的标题
	// targeted.putExtra(Intent.EXTRA_SUBJECT, "主题");
	// targeted.setPackage(activityInfo.packageName);
	// targeted.setClassName(activityInfo.packageName, info.activityInfo.name);
	// PackageManager pm =
	// ChannelActivity.this.getApplication().getPackageManager();
	// // 微信有2个怎么区分-。- 朋友圈还有微信
	// String appName =
	// info.activityInfo.applicationInfo.loadLabel(pm).toString();
	// if (appName.equals("微信")||appName.equals("QQ")) {
	// targetedShareIntents.add(targeted);
	// }
	// }
	// // 选择分享时的标题
	// Intent chooserIntent =
	// Intent.createChooser(targetedShareIntents.remove(0), "选择分享");
	// if (chooserIntent == null) {
	// return;
	// }
	// chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS,
	// targetedShareIntents.toArray(new Parcelable[] {}));
	// try {
	// startActivity(chooserIntent);
	// } catch (android.content.ActivityNotFoundException ex) {
	// Toast.makeText(this, "找不到该分享应用组件", Toast.LENGTH_SHORT).show();
	// }}
	// }

	/**
	 * 计算inputs的二进制
	 */
	private String handleShowItems() {
		String result = "";
		Channel channel = ChannelCacheUtils.getChannel(ChannelActivity.this,
				channelId);
		String inputs = "";
		if(channel != null){
			inputs = channel.getInputs();
		}
		if (!StringUtils.isBlank(inputs)) {
			result = Integer.toBinaryString(Integer.parseInt(inputs));
		} else {
			result = "-1";
		}
		return result;
	}

	/**
	 * 注册更改频道名称广播
	 */
	private void registeRefreshNameReceiver() {
		refreshNameReceiver = new BroadcastReceiver() {
			@Override
			public void onReceive(Context context, Intent intent) {
				String name = intent.getExtras().getString("name");
				((TextView) findViewById(R.id.header_text)).setText(name);
			}
		};
		IntentFilter filter = new IntentFilter();
		filter.addAction("update_channel_name");
		registerReceiver(refreshNameReceiver, filter);
	}

	/**
	 * 初始化消息列表UI
	 */
	private void initMsgListView() {
		msgListView = (PullableListView) findViewById(R.id.msg_list);
		msgListView.setCanSelectBottom(true);
		// 如果没有消息就不让ListView刷新
		if (msgList.size() == 0) {
			msgListView.setCanPullDown(false);
		}
		msgListView.setAdapter(adapter);
		msgListView.setSelection(msgList.size() - 1);
		pullToRefreshLayout.setOnRefreshListener(ChannelActivity.this);
		msgListView.smoothScrollToPosition(adapter.getCount());
		// 设置点击每个Item时跳转到详情
		msgListView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				LogUtils.debug("jason", "onItemClick-------");
				Bundle bundle = new Bundle();
				Msg msg = msgList.get(position);
				String msgType = msg.getType();
				String mid = "";
				if (msgType.equals("image") || msgType.equals("res_image")
						|| msgType.equals("res_file")) {
					mid = msg.getMid();
				} else if (msgType.equals("comment")
						|| msgType.equals("text_comment")) {
					mid = msg.getCommentMid();
				}
				if (!StringUtils.isBlank(mid)) {
					bundle.putString("mid", mid);
					IntentUtils.startActivity(ChannelActivity.this,
							ChannelMsgDetailActivity.class, bundle);
				}
			}
		});
		/**
		 * 当触摸消息list时把输入法和添加选项layout隐藏
		 */
		msgListView.setOnTouchListener(new OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				// TODO Auto-generated method stub
				if (!chatInputMenu.hideAddMenuLayout()) {
					chatInputMenu.hideSoftInput();
				}
				return false;
			}
		});

	}

	/**
	 * 调用文件系统
	 */
	protected void openFileSystem() {
		Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
		intent.setType("*/*");
		intent.addCategory(Intent.CATEGORY_OPENABLE);
		startActivityForResult(Intent.createChooser(intent,
				getString(R.string.file_upload_tips)), CHOOSE_FILE);
	}

	/**
	 * 调用图库
	 */
	protected void openGallery() {
		Intent i = new Intent(Intent.ACTION_PICK,
				MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
		startActivityForResult(i, GELLARY_RESULT);
	}

	/**
	 * 调用摄像头拍照
	 */
	protected void openCamera() {
		InputMethodUtils.hide(ChannelActivity.this);
		Intent intentFromCapture = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
		// 判断存储卡是否可以用，可用进行存储
		if (Environment.getExternalStorageState().equals(
				Environment.MEDIA_MOUNTED)) {
			File appDir = new File(Environment.getExternalStorageDirectory(),
					"DCIM");
			if (!appDir.exists()) {
				appDir.mkdir();
			}
			// 指定文件名字
			String fileName = new Date().getTime() + ".jpg";
			intentFromCapture.putExtra(MediaStore.EXTRA_OUTPUT,
					Uri.fromFile(new File(appDir, fileName)));
			PreferencesUtils.putString(ChannelActivity.this, "capturekey",
					fileName);
			startActivityForResult(intentFromCapture, CAMERA_RESULT);
		} else {
			ToastUtils.show(getApplicationContext(),
					R.string.filetransfer_sd_not_exist);
		}
	}

	/**
	 * 注册消息接收广播,传入一个Handler用于接收到消息后把消息发回到主线程
	 */
	private void registeMsgReceiver() {
		// TODO Auto-generated method stub
		msgResvier = new MsgReceiver(ChannelActivity.this, handler);
		IntentFilter filter = new IntentFilter();
		filter.addAction("com.inspur.msg");
		registerReceiver(msgResvier, filter);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode,
			final Intent data) {
		if (resultCode == RESULT_OK) {
			// 文件管理器返回
			if (requestCode == CHOOSE_FILE
					&& NetUtils.isNetworkConnected(getApplicationContext())) {
				Msg localMsg = MsgRecourceUploadUtils.uploadImgFile(
						ChannelActivity.this, data, apiService);
				addLocalMessage(localMsg);

			} else if (requestCode == CAMERA_RESULT
					&& NetUtils.isNetworkConnected(getApplicationContext())) {
				Msg localMsg = MsgRecourceUploadUtils.uploadMsgImg(
						ChannelActivity.this, data, apiService);
				addLocalMessage(localMsg);
			}
			// else if (requestCode == GELLARY_RESULT
			// && NetUtils.isNetworkConnected(getApplicationContext())) {
			// // 图库选择图片返回
			// Msg localMsg = MsgRecourceUploadUtils.uploadMsgImg(
			// ChannelActivity.this, data, apiService);
			// addLocalMessage(localMsg);
			// }
			else if (requestCode == MENTIONS_RESULT) {
				// @返回
				chatInputMenu.setMentionData(data);
			} 
		}else if (resultCode == ImagePicker.RESULT_CODE_ITEMS) { // 图库选择图片返回
			if (data != null && requestCode == GELLARY_RESULT) {
				Msg localMsg = MsgRecourceUploadUtils.uploadMsgImg(
						ChannelActivity.this, data, apiService);
				addLocalMessage(localMsg);
			}
		}
	}

	/**
	 * 处理子线程返回消息
	 */
	private void handMessage() {
		// TODO Auto-generated method stub
		handler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				switch (msg.what) {
				case HAND_CALLBACK_MESSAGE: // 接收推送的消息·
					Msg pushMsg = (Msg) msg.obj;
					if (channelId.equals(pushMsg.getCid())
							&& !msgList.contains(pushMsg)) {
						msgList.add(pushMsg);
						msgListView.setCanPullDown(true);
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
	 * 发送消息
	 * 
	 * @param
	 * @param fakeMessageId
	 */
	protected void sendMsg(String content, String type, String fakeMessageId) {
		if (NetUtils.isNetworkConnected(getApplicationContext())) {
			apiService.sendMsg(channelId, content, type, fakeMessageId);
		}
	}

	/**
	 * 当推送消息是自己的消息时修改消息id
	 * 
	 * @param fakeMessageId
	 * @param realMsg
	 */
	private void replaceWithRealMsg(String fakeMessageId, Msg realMsg) {
		if (StringUtils.isBlank(fakeMessageId)) {
			return;
		}
		Msg fakeMsg = new Msg();
		fakeMsg.setMid(fakeMessageId);
		// 如果list中已经有了这个真实的消息，就要去掉假消息，防止重复
		if (msgList.contains(realMsg)) {
			msgList.remove(fakeMsg);
		} else { // 如果list中没有这真是的消息，就要替换成真实消息
			int fakeMsgIndex = msgList.indexOf(fakeMsg);
			if (fakeMsgIndex != -1) {
				msgList.get(fakeMsgIndex).setMid(realMsg.getMid());
			} else {
				msgList.add(realMsg);
			}
		}
		adapter.notifyDataSetChanged();

	}

	/**
	 * 控件点击事件
	 * 
	 * @param v
	 */
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.back_layout:
			finish();
			break;

		case R.id.channel_info_img:
			showChannelInfo();
			break;
		default:
			break;
		}
	}

	/**
	 * 展示群组或个人信息
	 */
	private void showChannelInfo() {
		Bundle bundle = new Bundle();
		bundle.putString("cid", channelId);
//		Channel channel = ChannelCacheUtils.getChannel(ChannelActivity.this,
//				channelId);
		// String inputs = channel.getInputs();
		// if(StringUtils.isBlank(inputs) || Integer.parseInt(inputs) == 0){
		//
		// }
		if (channelType.equals("GROUP")) {
			IntentUtils.startActivity(ChannelActivity.this,
					ChannelInfoActivity.class, bundle);
		} else if (channelType.equals("SERVICE")) {
			bundle.putString("uid", uid);
			IntentUtils.startActivity(ChannelActivity.this,
					RobotInfoActivity.class, bundle);
		} else {
			IntentUtils.startActivity(ChannelActivity.this,
					UserInfoActivity.class, bundle);
		}
	}

	/**
	 * 点击发送按钮后发送消息的逻辑
	 */
	private void sendTextMessage(String content, List<String> mentionsUidList,
			List<String> mentionsUserNameList) {

		ArrayList<String> urlList = URLMatcher.getUrls(content);
		JSONObject richTextObj = new JSONObject();
		String source = HandleMsgTextUtils.handleMentionAndURL(content,
				mentionsUserNameList, mentionsUidList);
		JSONArray mentionArray = JSONUtils.toJSONArray(mentionsUidList);
		JSONArray urlArray = JSONUtils.toJSONArray(urlList);
		try {
			richTextObj.put("source", source);
			richTextObj.put("mentions", mentionArray);
			richTextObj.put("urls", urlArray);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		String fakeMessageId = System.currentTimeMillis() + "";
		Msg localMsg = ConbineMsg.conbineMsg(ChannelActivity.this,
				richTextObj.toString(), "", "txt_rich", fakeMessageId);
		addLocalMessage(localMsg);
		sendMsg(richTextObj.toString(), "txt_rich", fakeMessageId);

	}

	/**
	 * 消息发送完成后在本地添加一条消息
	 *
	 * @param msg
	 */
	private void addLocalMessage(Msg msg) {
		if (msg != null) {
			msgList.add(msg);
			adapter.notifyDataSetChanged();
			msgListView.setSelection(msgList.size() - 1);
			msgListView.setCanPullDown(true);
		}
	}

	/**
	 * 显示adapter
	 */
	private BaseAdapter adapter = new BaseAdapter() {
		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			final Msg msg = msgList.get(position);
			String type = msg.getType();
//			LogUtils.YfcDebug("消息的body："+msg.getBody());
			LayoutInflater vi = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
			convertView = vi.inflate(R.layout.chat_msg_card_parent_view, null);
			showCommonView(convertView, position);
			RelativeLayout cardLayout = (RelativeLayout) convertView
					.findViewById(R.id.card_layout);
			View childView = null;
			if (type.equals("txt_comment") || type.equals("comment")) {
				childView = vi.inflate(
						R.layout.chat_msg_card_child_text_comment_view, null);
				DisplayTxtCommentMsg.displayCommentMsg(ChannelActivity.this,
						childView, msg, apiService);
				convertView.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View v) {
						// TODO Auto-generated method stub
						Bundle bundle = new Bundle();
						bundle.putSerializable("mid", msg.getCommentMid());
						IntentUtils.startActivity(ChannelActivity.this,
								ChannelMsgDetailActivity.class, bundle);
					}
				});
			} else if (type.equals("res_image") || type.equals("image")) {
				childView = vi.inflate(
						R.layout.chat_msg_card_child_res_img_view, null);
				DisplayResImageMsg.displayResImgMsg(ChannelActivity.this,
						childView, msg);
			} else if (type.equals("res_link")) {
				TextView newsCommentText = (TextView) convertView
						.findViewById(R.id.news_comment_text);
//				newsCommentText.setVisibility(View.VISIBLE);
				newsCommentText.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View v) {
						// TODO Auto-generated method stub
						Bundle bundle = new Bundle();
						bundle.putSerializable("mid", msg.getMid());
						IntentUtils.startActivity(ChannelActivity.this,
								ChannelMsgDetailActivity.class, bundle);
					}
				});
				childView = vi.inflate(
						R.layout.chat_msg_card_child_res_link_view, null);
				DisplayResLinkMsg.displayResLinkMsg(ChannelActivity.this,
						childView, msg);
			} else if (type.equals("res_file")) {
				childView = vi.inflate(
						R.layout.chat_msg_card_child_res_file_view, null);
				DisplayResFileMsg.displayResFileMsg(ChannelActivity.this,
						childView, msg);
			} else if (type.equals("txt_rich")) {
				childView = vi.inflate(
						R.layout.chat_msg_card_child_text_rich_view, null);
				DisplayTxtRichMsg.displayRichTextMsg(ChannelActivity.this,
						childView, msg);
			} else {
				childView = vi.inflate(
						R.layout.chat_msg_card_child_res_unknown_view, null);
				DisplayResUnknownMsg.displayResUnknownMsg(ChannelActivity.this,
						childView, msg);
			}
			// else if (type.equals("act_meeting")) {
			// childView = vi.inflate(R.layout.child_msg_meeting_card_view,
			// null);
			// DisplayActMeetingMsg.displayMeetingInviteMsg(
			// ChannelActivity.this, apiService, childView, msg);
			//
			// } else if (type.equals("act_meeting_cancel")) {
			// childView = vi.inflate(
			// R.layout.child_msg_meeting_notify_card_view, null);
			// DisplayActMeetingCancelMsg.displayCancelMeetingMsg(
			// ChannelActivity.this, childView, msg);
			// } else if (type.equals("act_meeting_approve")) {
			// childView = vi.inflate(R.layout.child_msg_meeting_card_view,
			// null);
			// DisplayActMeetingApproveMsg.displayMeetingApproveMsg(
			// ChannelActivity.this, apiService, childView, msg);
			// }

			cardLayout.addView(childView);
			return convertView;
		}

		/**
		 * 显示公共的View
		 * 
		 * @param convertView
		 * @param
		 */
		private void showCommonView(View convertView, int position) {
			final Msg msg = msgList.get(position);
			showUserName(convertView, msg);
			showMsgSendTime(convertView, msg, position);
			showUserPhoto(convertView, msg);
			showMsgCardArrow(convertView, msg);
		}

		/**
		 * 展示卡片左右两个角
		 * 
		 * @param convertView
		 * @param msg
		 */
		private void showMsgCardArrow(View convertView, Msg msg) {
			// TODO Auto-generated method stub
			ImageView leftArrowImg = (ImageView) convertView
					.findViewById(R.id.left_arrow_img);
			ImageView rightArrowImg = (ImageView) convertView
					.findViewById(R.id.right_arrow_img);
			if (msg.getUid().equals(
					((MyApplication) getApplicationContext()).getUid())) {
				leftArrowImg.setVisibility(View.INVISIBLE);
				rightArrowImg.setVisibility(View.VISIBLE);
			} else {
				leftArrowImg.setVisibility(View.VISIBLE);
				rightArrowImg.setVisibility(View.INVISIBLE);
			}
		}

		/**
		 * 展示消息发送时间
		 * 
		 * @param convertView
		 * @param msg
		 * @param position
		 */
		private void showMsgSendTime(View convertView, Msg msg, int position) {
			// TODO Auto-generated method stub
			TextView sendTimeText = (TextView) convertView
					.findViewById(R.id.send_time_text);
			long msgTimeLong = TimeUtils.UTCString2Long(msg.getTime());
			long lastMsgTimelong = 0;
			if (position != 0) {
				lastMsgTimelong = TimeUtils.UTCString2Long(msgList.get(
						position - 1).getTime());
			}
			long duration = msgTimeLong - lastMsgTimelong;
			if (duration >= 180000) {
				sendTimeText.setVisibility(View.VISIBLE);
				String msgSendTime = TimeUtils.getChannelMsgDisplayTime(
						getApplicationContext(), msg.getTime());
				sendTimeText.setText(msgSendTime);
			} else {
				sendTimeText.setVisibility(View.GONE);
			}
		}

		/**
		 * 展示用户名称
		 * 
		 * @param convertView
		 * @param msg
		 */
		private void showUserName(View convertView, Msg msg) {
			// TODO Auto-generated method stub
			TextView senderNameText = (TextView) convertView
					.findViewById(R.id.sender_name_text);
			if (channelType.equals("GROUP") && !isMyMsg(msg)) {
				senderNameText.setVisibility(View.VISIBLE);
				senderNameText.setText(msg.getTitle());
			} else {
				senderNameText.setVisibility(View.GONE);
			}
		}


		/**
		 * 展示用户头像
		 * 
		 * @param convertView
		 * @param msg
		 */
		private void showUserPhoto(View convertView, final Msg msg) {
			// TODO Auto-generated method stub
			ImageView senderPhotoImg = (ImageView) convertView
					.findViewById(R.id.sender_photo_img);
			ImageView robotSuperscriptImg = (ImageView) convertView.findViewById(R.id.msg_superscript_img);
			if (msg.getUid().equals(
					((MyApplication) getApplicationContext()).getUid())) {
				senderPhotoImg.setVisibility(View.INVISIBLE);
			} else {
				senderPhotoImg.setVisibility(View.VISIBLE);
				String iconUrl = UriUtils.getChannelImgUri(msg.getUid());
				if(channelType.equals("SERVICE")){
					iconUrl = UriUtils.getRobotIconUri(RobotCacheUtils.getRobotById(ChannelActivity.this, msg.getUid()).getAvatar());
				}
				new ImageDisplayUtils(ChannelActivity.this,
						R.drawable.icon_person_default).display(senderPhotoImg,
						iconUrl);
				senderPhotoImg.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						Bundle bundle = new Bundle();
						String uid = msg.getUid();
						bundle.putString("uid", uid);
						if (channelType.endsWith("SERVICE")) {
							IntentUtils.startActivity(ChannelActivity.this,
									RobotInfoActivity.class, bundle);
						} else {
							IntentUtils.startActivity(ChannelActivity.this,
									UserInfoActivity.class, bundle);
						}
					}
				});
				
				//去掉机器人角标
//				if (channelType.equals("SERVICE")&&!isMyMsg(msg)) {
//					robotSuperscriptImg.setVisibility(View.VISIBLE);
//				} else {
//					robotSuperscriptImg.setVisibility(View.GONE);
//				}
			}
		}

		@Override
		public long getItemId(int position) {
			return 0;
		}

		@Override
		public Object getItem(int position) {
			return null;
		}

		@Override
		public int getCount() {
			return msgList.size();
		}
	};
	
	/**
	 * 判断是否自己的消息
	 * @param msg
	 * @return
	 */
	private boolean isMyMsg(Msg msg){
		String uid = ((MyApplication)getApplication()).getUid();
		return msg.getUid().equals(uid);
	}

	@Override
	public void onBackPressed() {
		// TODO Auto-generated method stub
		if (!chatInputMenu.hideAddMenuLayout()) {
			super.onBackPressed();
		}
	}

	@Override
	protected void onDestroy() {
		if (handler != null) {
			handler = null;
		}
		if (msgResvier != null) {
			unregisterReceiver(msgResvier);
		}
		if (refreshNameReceiver != null) {
			unregisterReceiver(refreshNameReceiver);
		}
		super.onDestroy();
	}

	@Override
	public void onRefresh(PullToRefreshLayout pullToRefreshLayout) {
		if (MsgCacheUtil.isDataInLocal(ChannelActivity.this, channelId, msgList
				.get(0).getMid(), 20)) {
			pullToRefreshLayout.refreshFinish(PullToRefreshLayout.SUCCEED);
			List<Msg> historyMsgList = MsgCacheUtil.getHistoryMsgList(
					ChannelActivity.this, channelId, msgList.get(0).getMid(),
					20);
			msgList.addAll(0, historyMsgList);
			adapter.notifyDataSetChanged();
			ListViewUtils.setSelection(msgListView, historyMsgList.size() - 1);
		} else {
			getNewsMsg();
		}
	}

	/**
	 * 获取新消息
	 */
	private void getNewsMsg() {
		apiService.getNewMsgs(channelId, msgList.get(0).getMid(), 20);
	}

	@Override
	public void onLoadMore(PullToRefreshLayout pullToRefreshLayout) {
	}

	private class WebService extends APIInterfaceInstance {
		@Override
		public void returnSendMsgSuccess(GetSendMsgResult getSendMsgResult,
				String fakeMessageId) {
			replaceWithRealMsg(fakeMessageId, getSendMsgResult.getMsg());
		}

		@Override
		public void returnSendMsgFail(String error) {
			WebServiceMiddleUtils.hand(ChannelActivity.this, error);
		}

		@Override
		public void returnUploadMsgImgSuccess(
				GetNewsImgResult getNewsImgResult, String fakeMessageId) {
			String newsImgBody = getNewsImgResult.getImgMsgBody();
			sendMsg(newsImgBody, "res_image", fakeMessageId);
		}

		@Override
		public void returnUploadMsgImgFail(String error) {
			WebServiceMiddleUtils.hand(ChannelActivity.this, error);
		}

		@Override
		public void returnNewMsgsSuccess(GetNewMsgsResult getNewMsgsResult) {
			pullToRefreshLayout.refreshFinish(PullToRefreshLayout.SUCCEED);
			final List<Msg> historyMsgList = getNewMsgsResult
					.getNewMsgList(channelId);
			MsgCacheUtil.saveMsgList(ChannelActivity.this, historyMsgList,
					msgList.get(0).getMid());
			if (historyMsgList != null && historyMsgList.size() > 1) {
				msgList.addAll(0, historyMsgList);
				adapter.notifyDataSetChanged();
				ListViewUtils.setSelection(msgListView,
						historyMsgList.size() - 1);
			} else {
				msgListView.setCanPullDown(false);
			}

		}

		@Override
		public void returnNewMsgsFail(String error) {
			pullToRefreshLayout.refreshFinish(PullToRefreshLayout.FAIL);
			WebServiceMiddleUtils.hand(ChannelActivity.this, error);
		}

		@Override
		public void returnMsgSuccess(getMsgResult getMsgResult) {
			Msg msg = getMsgResult.getMsg();
			if (msg != null && ChannelActivity.this != null) {
				MsgCacheUtil.saveMsg(ChannelActivity.this, msg);
				adapter.notifyDataSetChanged();
			}
		}

		@Override
		public void returnMsgFail(String error) {
			WebServiceMiddleUtils.hand(ChannelActivity.this, error);
		}

		@Override
		public void returnFileUpLoadSuccess(
				GetFileUploadResult getFileUploadResult, String fakeMessageId) {
			String fileMsgBody = getFileUploadResult.getFileMsgBody();
			sendMsg(fileMsgBody, "res_file", fakeMessageId);
		}

		@Override
		public void returnFileUpLoadFail(String error) {
			WebServiceMiddleUtils.hand(ChannelActivity.this, error);
		}

		@Override
		public void returnGetMeetingReplySuccess(
				GetMeetingReplyResult getMeettingReplyResult) {
			super.returnGetMeetingReplySuccess(getMeettingReplyResult);
		}

		@Override
		public void returnGetMeetingReplyFail(String error) {
			super.returnGetMeetingReplyFail(error);
		}

	}

}