package com.inspur.emmcloud.ui.chat;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.text.SpannableString;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.inspur.emmcloud.BaseActivity;
import com.inspur.emmcloud.MyApplication;
import com.inspur.emmcloud.R;
import com.inspur.emmcloud.api.APIInterfaceInstance;
import com.inspur.emmcloud.api.apiservice.ChatAPIService;
import com.inspur.emmcloud.bean.Comment;
import com.inspur.emmcloud.bean.CommentBodyBean;
import com.inspur.emmcloud.bean.GetMsgCommentResult;
import com.inspur.emmcloud.bean.GetMsgResult;
import com.inspur.emmcloud.bean.GetSendMsgResult;
import com.inspur.emmcloud.bean.Msg;
import com.inspur.emmcloud.ui.contact.UserInfoActivity;
import com.inspur.emmcloud.util.ChannelCacheUtils;
import com.inspur.emmcloud.util.FileUtils;
import com.inspur.emmcloud.util.HandleMsgTextUtils;
import com.inspur.emmcloud.util.ImageDisplayUtils;
import com.inspur.emmcloud.util.InputMethodUtils;
import com.inspur.emmcloud.util.IntentUtils;
import com.inspur.emmcloud.util.JSONUtils;
import com.inspur.emmcloud.util.MentionsAndUrlShowUtils;
import com.inspur.emmcloud.util.MsgCacheUtil;
import com.inspur.emmcloud.util.NetUtils;
import com.inspur.emmcloud.util.PreferencesUtils;
import com.inspur.emmcloud.util.TimeUtils;
import com.inspur.emmcloud.util.TransHtmlToTextUtils;
import com.inspur.emmcloud.util.URLMatcher;
import com.inspur.emmcloud.util.UriUtils;
import com.inspur.emmcloud.util.WebServiceMiddleUtils;
import com.inspur.emmcloud.widget.CircleImageView;
import com.inspur.emmcloud.widget.ECMChatInputMenu;
import com.inspur.emmcloud.widget.LoadingDialog;
import com.inspur.emmcloud.widget.ScrollViewWithListView;
import com.inspur.emmcloud.widget.pullableview.PullToRefreshLayout;
import com.inspur.emmcloud.widget.pullableview.PullToRefreshLayout.OnRefreshListener;
import com.inspur.emmcloud.widget.pullableview.PullableScrollView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * 消息详情页面
 *
 * @author Administrator
 */
public class ChannelMsgDetailActivity extends BaseActivity implements
		OnRefreshListener {

	private static final int RESULT_MENTIONS = 5;
	private ScrollViewWithListView commentListView;
	private ImageDisplayUtils imageDisplayUtils;
	private Msg msg;
	private ChatAPIService apiService;
	private List<Comment> commentList;
	private BaseAdapter commentAdapter;
	private LoadingDialog loadingDialog;
	private PullableScrollView commentScrollView;
	private PullToRefreshLayout pullToRefreshLayout;
	private CircleImageView senderHeadImg;
	private TextView msgSendTimeText;
	private TextView senderNameText;
	private ImageView msgContentImg;
	private String cid = "";
	private RelativeLayout msgDisplayLayout;
	private LayoutInflater inflater;
	private ECMChatInputMenu chatInputMenu;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_channel_msg_detail);
		((MyApplication) getApplicationContext()).addActivity(this);
		initView();
		initData();
	}

	/**
	 * 初始化Views
	 */
	private void initView() {
		pullToRefreshLayout = (PullToRefreshLayout) findViewById(R.id.refresh_view);
		pullToRefreshLayout.setOnRefreshListener(this);
		loadingDialog = new LoadingDialog(this);
		apiService = new ChatAPIService(this);
		apiService.setAPIInterface(new WebService());
		commentList = new ArrayList<Comment>();
		imageDisplayUtils = new ImageDisplayUtils(getApplicationContext(),
				R.drawable.icon_photo_default);
		commentScrollView = (PullableScrollView) findViewById(R.id.xscrollview);
		inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
		View msgDetailLayout = inflater.inflate(R.layout.msg_parent_detail,
				null);
		senderHeadImg = (CircleImageView) msgDetailLayout
				.findViewById(R.id.sender_photo_img);
		msgSendTimeText = (TextView) msgDetailLayout
				.findViewById(R.id.msg_send_time_text);
		senderNameText = (TextView) msgDetailLayout
				.findViewById(R.id.sender_name_text);
		commentListView = (ScrollViewWithListView) msgDetailLayout
				.findViewById(R.id.comment_list);
		msgDisplayLayout = (RelativeLayout) msgDetailLayout
				.findViewById(R.id.msg_display_layout);
		commentScrollView.addView(msgDetailLayout);
		initChatInputMenu();
	}

	private void initChatInputMenu(){
		chatInputMenu = (ECMChatInputMenu) findViewById(R.id.chat_input_menu);
		cid = getIntent().getExtras().getString("cid");
		String channelType = ChannelCacheUtils.getChannelType(getApplicationContext(),
				cid);
		if (channelType.equals("GROUP")) {
			chatInputMenu.setIsChannelGroup(true, cid);
		}
		chatInputMenu.hideAddMenuLayout();
		chatInputMenu.showAddBtn(false);
		chatInputMenu.setChatInputMenuListener(new ECMChatInputMenu.ChatInputMenuListener() {

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
				sendComment(content, mentionsUidList, mentionsUserNameList);
			}
		});
	}


	/**
	 * 初始化数据源
	 */
	private void initData() {
		String mid = "";
		if (getIntent().hasExtra("msg")) {
			msg = (Msg) getIntent().getExtras().getSerializable("msg");
		} else if (getIntent().hasExtra("mid")) {
			mid = getIntent().getExtras().getString("mid");
			msg = MsgCacheUtil.getCacheMsg(getApplicationContext(), mid);
		}
		if (msg != null) {
			handMsgData();
		} else {
			getMsgById(mid);
		}
	}

	/**
	 * 处理数据
	 */
	private void handMsgData() {
		cid = msg.getCid();
		getComment();
		displayMsgDetail();
	}

	/**
	 * 展示消息详情
	 */
	private void displayMsgDetail() {
		disPlayCommonInfo();
		View msgDisplayView = null;
		// 新闻链接类型消息体的展示单独使用一个layout
		if (msg.getType().equals("res_link")) {
			msgDisplayView = inflater.inflate(R.layout.msg_news_detail, null);
			DisplayResLinkMsg.displayResLinkMsg(
					ChannelMsgDetailActivity.this, msgDisplayView, msg, false);
		} else if (msg.getType().equals("res_file")) {
			msgDisplayView = inflater.inflate(
					R.layout.child_msg_res_file_card_view, null);
			DisplayResFileMsg.displayResFileMsg(
					ChannelMsgDetailActivity.this, msgDisplayView, msg);
		} else {
			msgDisplayView = inflater.inflate(R.layout.msg_common_detail, null);
			msgContentImg = (ImageView) msgDisplayView
					.findViewById(R.id.content_img);
			TextView fileNameText = (TextView) msgDisplayView
					.findViewById(R.id.comment_filename_text);
			TextView fileSizeText = (TextView) msgDisplayView
					.findViewById(R.id.comment_filesize_text);
			final CommentBodyBean commentBodyBean = new CommentBodyBean(
					msg.getBody());
			displayImage(commentBodyBean.getKey());
			fileNameText.setText(commentBodyBean.getName());
			fileSizeText.setText(FileUtils.formatFileSize(commentBodyBean
					.getSize()));
			msgContentImg.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					if (msg.getType().equals("image")
							|| msg.getType().equals("res_image")) {
						displayZoomImage(v, commentBodyBean.getKey());
					}
				}
			});
		}
		msgDisplayView.setLayoutParams(new LayoutParams(
				LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
		msgDisplayLayout.addView(msgDisplayView);
	}

	/**
	 * 展示图片或者文件图标
	 *
	 * @param fileName
	 */
	private void displayImage(String fileName) {
		if (msg.getType().equals("res_image")) {
			imageDisplayUtils.display(msgContentImg,
					UriUtils.getPreviewUri(fileName));
		} else {
			displayFileIcon(fileName);
		}
	}

	/**
	 * 展示可以缩放的Image
	 *
	 * @param path
	 */
	protected void displayZoomImage(View view, String path) {

		int[] location = new int[2];
		view.getLocationOnScreen(location);
		view.invalidate();
		int width = view.getWidth();
		int height = view.getHeight();
		String url = path;
		if (!path.startsWith("file:") && !path.startsWith("content:")
				&& !path.startsWith("drawable")) {
			url = UriUtils.getPreviewUri(path);
		}
		ArrayList<String> urlList = new ArrayList<String>();
		urlList.add(url);
		Intent intent = new Intent(getApplicationContext(),
				ImagePagerActivity.class);
		intent.putExtra(ImagePagerActivity.PHOTO_SELECT_X_TAG, location[0]);
		intent.putExtra(ImagePagerActivity.PHOTO_SELECT_Y_TAG, location[1]);
		intent.putExtra(ImagePagerActivity.PHOTO_SELECT_W_TAG, width);
		intent.putExtra(ImagePagerActivity.PHOTO_SELECT_H_TAG, height);
		intent.putExtra(ImagePagerActivity.EXTRA_IMAGE_URLS, urlList);
		startActivity(intent);
	}

	/**
	 * 展示文件Icon
	 */
	private void displayFileIcon(String fileName) {
		if (fileName.startsWith("file")) {
			imageDisplayUtils.display(msgContentImg, fileName);
		} else if (fileName.endsWith("pdf")) {
			imageDisplayUtils.display(msgContentImg, "drawable://"
					+ R.drawable.icon_file_pdf);
		} else if (fileName.endsWith("doc") || fileName.endsWith("docx")) {
			imageDisplayUtils.display(msgContentImg, "drawable://"
					+ R.drawable.icon_file_word);
		} else if (fileName.endsWith("xls")) {
			imageDisplayUtils.display(msgContentImg, "drawable://"
					+ R.drawable.icon_file_excel);
		} else if (fileName.endsWith("ppt")) {
			imageDisplayUtils.display(msgContentImg, "drawable://"
					+ R.drawable.icon_file_ppt);
		} else if (fileName.endsWith("rar")) {
			imageDisplayUtils.display(msgContentImg, "drawable://"
					+ R.drawable.icon_file_rar);
		} else if (fileName.endsWith("zip")) {
			imageDisplayUtils.display(msgContentImg, "drawable://"
					+ R.drawable.icon_file_zip);
		} else if (fileName.contains("txt")) {
			imageDisplayUtils.display(msgContentImg, "drawable://"
					+ R.drawable.icon_txt);
		} else if (fileName.endsWith("jpeg") || fileName.endsWith("jpg")
				|| fileName.endsWith("png")) {
			imageDisplayUtils.display(msgContentImg, "drawable://"
					+ R.drawable.icon_file_photos);
		} else {
			imageDisplayUtils.display(msgContentImg, fileName);
		}
	}

	/**
	 * 展示通用的部分
	 */
	private void disPlayCommonInfo() {
		imageDisplayUtils.display(senderHeadImg,
				UriUtils.getChannelImgUri(ChannelMsgDetailActivity.this,msg.getUid()));
		senderHeadImg.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				openUserInfo(msg.getUid());
			}
		});
		String msgSendTime = TimeUtils.getDisplayTime(getApplicationContext(),
				msg.getTime());
		msgSendTimeText.setText(msgSendTime);
		senderNameText.setText(msg.getTitle());
	}

	/**
	 * 处理@逻辑
	 **/
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (resultCode != RESULT_CANCELED && requestCode == RESULT_MENTIONS) {
			chatInputMenu.setMentionData(data);
		}
	}

	/**
	 * 控件的点击逻辑
	 **/
	public void onClick(View v) {
		switch (v.getId()) {
			case R.id.back_layout:
				onBackPressed();
				break;
			default:
				break;
		}
	}

	@Override
	public void onBackPressed() {
		InputMethodUtils.hide(ChannelMsgDetailActivity.this);
		//将最新的评论数返回给ImagePagerActivity
		if (getIntent().hasExtra("from") && getIntent().getStringExtra("from").equals("imagePager")){
			Intent intent = new Intent();
			intent.putExtra("mid",msg.getMid());
			intent.putExtra("commentCount",commentList.size());
			setResult(RESULT_OK,intent);
		}
		finish();
	}


	/**
	 * 发出评论
	 */
	private void sendComment(String commentContent, List<String> mentionsUidList,
							 List<String> mentionsUserNameList) {

		if (NetUtils.isNetworkConnected(getApplicationContext())) {
			String commentConbineResult = getConbineComment(commentContent,mentionsUidList,mentionsUserNameList);
			apiService.sendMsg(cid, commentConbineResult, "txt_comment",
					msg.getMid(), "");
			Comment newComment = combineComment(commentConbineResult);
			commentList.add(newComment);
			if (commentAdapter == null) {
				commentAdapter = new CommentAdapter();
				commentListView.setAdapter(commentAdapter);
			}
			commentAdapter.notifyDataSetChanged();
			// 滚动到页面最后
			commentScrollView.post(new Runnable() {
				public void run() {
					commentScrollView.fullScroll(ScrollView.FOCUS_DOWN);
				}
			});
			InputMethodUtils.hide(ChannelMsgDetailActivity.this);
		}
	}

	/**
	 * 拼装评论
	 */
	private Comment combineComment(String content) {
		String uid = ((MyApplication) getApplicationContext()).getUid();
		String title = PreferencesUtils.getString(
				ChannelMsgDetailActivity.this, "userRealName");
		String timeStamp = TimeUtils.getCurrentUTCTimeString();
		JSONObject jsonComment = new JSONObject();
		JSONObject jsonFrom = new JSONObject();
		try {
			jsonFrom.put("title", title);
			jsonFrom.put("uid", uid);
			jsonComment.put("timestamp", timeStamp);
			jsonComment.put("body", content);
			jsonComment.put("from", jsonFrom);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return new Comment(jsonComment);
	}

	class CommentAdapter extends BaseAdapter {
		@Override
		public int getCount() {
			return commentList.size();
		}

		@Override
		public Object getItem(int position) {
			return null;
		}

		@Override
		public long getItemId(int position) {
			return 0;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			LayoutInflater vi = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
			convertView = vi.inflate(R.layout.comment_item_view, null);
			TextView userNameText = (TextView) convertView
					.findViewById(R.id.name_text);
			TextView sendTimeText = (TextView) convertView
					.findViewById(R.id.commentdetail_time_text);
			TextView contentText = (TextView) convertView
					.findViewById(R.id.comment_text);
			ImageView photoImg = (ImageView) convertView
					.findViewById(R.id.msg_img);
			final Comment comment = commentList.get(position);
			userNameText.setText(comment.getTitle());
			contentText.setMovementMethod(LinkMovementMethod.getInstance());
			String source = comment.getSource();
//			String content = MentionsMatcher.handleMentioin(source);
//			contentText.setText(Html.fromHtml(content));
			String mentionsString = comment.getMentions();
			String urlsString = comment.getUrls();
			String[] mentions = mentionsString.replace("[", "").replace("]", "").split(",");
			String[] urls = urlsString.replace("[", "").replace("]", "").split(",");
			List<String> mentionList = Arrays.asList(mentions);
			List<String> urlList = Arrays.asList(urls);
			SpannableString spannableString = MentionsAndUrlShowUtils.handleMentioin(source, mentionList, urlList);
			contentText.setText(spannableString);
			TransHtmlToTextUtils.stripUnderlines(contentText,
					Color.parseColor("#0f7bca"));

			String time = TimeUtils.getDisplayTime(getApplicationContext(),
					comment.getTimestamp());
			sendTimeText.setText(time);

			new ImageDisplayUtils(ChannelMsgDetailActivity.this,
					R.drawable.icon_person_default).display(photoImg,
					UriUtils.getChannelImgUri(ChannelMsgDetailActivity.this,comment.getUid()));
			photoImg.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					String uid = comment.getUid();
					openUserInfo(uid);
				}
			});
			return convertView;
		}
	}

	/**
	 * 打开个人信息
	 *
	 * @param uid
	 */
	private void openUserInfo(String uid) {
		Bundle bundle = new Bundle();
		bundle.putString("uid", uid);
		IntentUtils.startActivity(ChannelMsgDetailActivity.this,
				UserInfoActivity.class, bundle);
	}

	@Override
	public void onRefresh(PullToRefreshLayout pullToRefreshLayout) {
		if (NetUtils.isNetworkConnected(ChannelMsgDetailActivity.this)) {
			getComment();
		} else {
			pullToRefreshLayout.refreshFinish(PullToRefreshLayout.FAIL);
		}
	}

	@Override
	public void onLoadMore(PullToRefreshLayout pullToRefreshLayout) {
	}



	public String getConbineComment(String content,List<String> mentionsUidList,
									List<String> mentionsUserNameList) {
		String source = "";
		ArrayList<String> urlList = URLMatcher.getUrls(content);
		JSONObject richTextObj = new JSONObject();
		source = HandleMsgTextUtils.handleMentionAndURL(chatInputMenu.getEdit(),content, mentionsUserNameList,
				mentionsUidList);
		JSONArray mentionArray = JSONUtils.toJSONArray(mentionsUidList);
		JSONArray urlArray = JSONUtils.toJSONArray(urlList);
		try {
			richTextObj.put("source", source);
			richTextObj.put("mentions", mentionArray);
			richTextObj.put("urlList", urlArray);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return richTextObj.toString();
	}

	/**
	 * 获取消息
	 *
	 * @param mid
	 */
	private void getMsgById(String mid) {
		if (NetUtils.isNetworkConnected(ChannelMsgDetailActivity.this)) {
			loadingDialog.show();
			apiService.getMsg(mid);
		}
	}

	/**
	 * 获取消息的评论
	 */
	private void getComment() {
		if (NetUtils.isNetworkConnected(ChannelMsgDetailActivity.this)) {
			apiService.getComment(msg.getMid());
		}
	}

	class WebService extends APIInterfaceInstance {
		@Override
		public void returnMsgSuccess(GetMsgResult getMsgResult) {
			if (loadingDialog != null && loadingDialog.isShowing()) {
				loadingDialog.dismiss();
			}
			msg = getMsgResult.getMsg();
			if (msg != null) {
				handMsgData();
			}
		}

		@Override
		public void returnMsgFail(String error) {
			if (loadingDialog != null && loadingDialog.isShowing()) {
				loadingDialog.dismiss();
			}
			WebServiceMiddleUtils.hand(ChannelMsgDetailActivity.this, error);
		}

		@Override
		public void returnMsgCommentSuccess(
				GetMsgCommentResult getMsgCommentResult,String mid) {
			pullToRefreshLayout.refreshFinish(PullToRefreshLayout.SUCCEED);
			commentList = getMsgCommentResult.getCommentList();
			if (commentList != null && commentList.size() > 0) {
				commentAdapter = new CommentAdapter();
				commentListView.setAdapter(commentAdapter);
				commentAdapter.notifyDataSetChanged();
			}
		}

		@Override
		public void returnMsgCommentFail(String error) {
			pullToRefreshLayout.refreshFinish(PullToRefreshLayout.FAIL);
			WebServiceMiddleUtils.hand(ChannelMsgDetailActivity.this, error);
		}

		@Override
		public void returnSendMsgSuccess(GetSendMsgResult getSendMsgResult,
										 String fakeMessageId) {
		}

		@Override
		public void returnSendMsgFail(String error, String fakeMessageId) {
			WebServiceMiddleUtils.hand(ChannelMsgDetailActivity.this, error);
		}
	}

}
