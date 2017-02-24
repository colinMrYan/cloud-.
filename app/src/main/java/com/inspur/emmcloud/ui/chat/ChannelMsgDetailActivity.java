package com.inspur.emmcloud.ui.chat;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextWatcher;
import android.text.method.LinkMovementMethod;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnKeyListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.BaseAdapter;
import android.widget.EditText;
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
import com.inspur.emmcloud.bean.GetSendMsgResult;
import com.inspur.emmcloud.bean.Msg;
import com.inspur.emmcloud.bean.GetMsgResult;
import com.inspur.emmcloud.ui.contact.UserInfoActivity;
import com.inspur.emmcloud.util.ChannelCacheUtils;
import com.inspur.emmcloud.util.ChannelMentions;
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
import com.inspur.emmcloud.util.StringUtils;
import com.inspur.emmcloud.util.TimeUtils;
import com.inspur.emmcloud.util.ToastUtils;
import com.inspur.emmcloud.util.TransHtmlToTextUtils;
import com.inspur.emmcloud.util.URLMatcher;
import com.inspur.emmcloud.util.UriUtils;
import com.inspur.emmcloud.util.WebServiceMiddleUtils;
import com.inspur.emmcloud.widget.CircleImageView;
import com.inspur.emmcloud.widget.LoadingDialog;
import com.inspur.emmcloud.widget.ScrollViewWithListView;
import com.inspur.emmcloud.widget.pullableview.PullToRefreshLayout;
import com.inspur.emmcloud.widget.pullableview.PullToRefreshLayout.OnRefreshListener;
import com.inspur.emmcloud.widget.pullableview.PullableScrollView;
import com.inspur.emmcloud.widget.spans.ForeColorSpan;

/**
 * 消息详情页面
 * 
 * @author Administrator
 *
 */
public class ChannelMsgDetailActivity extends BaseActivity implements
		OnRefreshListener {

	private static final int RESULT_MENTIONS = 5;
	private ScrollViewWithListView commentListView;
	private ImageDisplayUtils imageDisplayUtils;
	private EditText msgEdit;
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

	private boolean canMention = true;
	private int editDeletePosition = 0;
	private String cid = "";
	private ArrayList<String> userNameList = new ArrayList<String>();
	private ArrayList<String> uidList = new ArrayList<String>();
	private int beginPosition = 0;
	private int endPosition = 0;

	//private String channelType = "";
	private RelativeLayout msgDisplayLayout;
	private LayoutInflater inflater;

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
		msgEdit = (EditText) findViewById(R.id.comment_edit);
		msgEdit.addTextChangedListener(new TextChangedListener());
		msgEdit.setOnKeyListener(new OnMentionsListener());
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
						displayZoomImage(commentBodyBean.getKey());
					}
				}
			});
		}
		msgDisplayView.setLayoutParams(new LayoutParams(
				LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
		msgDisplayLayout.addView(msgDisplayView);
		((LinearLayout) findViewById(R.id.post_comment_layout))
				.setVisibility(View.VISIBLE);
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
	protected void displayZoomImage(String path) {
		Intent intent = new Intent(ChannelMsgDetailActivity.this,
				ImagePagerActivity.class);
		String url = path;
		if (!path.startsWith("file:") && !path.startsWith("content:")
				&& !path.startsWith("drawable")) {
			url = UriUtils.getPreviewUri(path);
		} 
		ArrayList<String> urlList = new ArrayList<String>();
		urlList.add(url);
		intent.putExtra("image_index", 0);
		intent.putStringArrayListExtra("image_urls", urlList);
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
				UriUtils.getChannelImgUri(msg.getUid()));
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

	/** 处理@逻辑 **/
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (resultCode != RESULT_CANCELED && requestCode == RESULT_MENTIONS) {
			String result = data.getStringExtra("searchResult");
			ChannelMentions.addMentions(result, userNameList, uidList, msgEdit,
					beginPosition, endPosition);
		}
	}

	/** 控件的点击逻辑 **/
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.back_layout:
			InputMethodUtils.hide(ChannelMsgDetailActivity.this);
			finish();
			break;
		case R.id.post_comment_btn:
			String commentContent = msgEdit.getText().toString();
			if (StringUtils.isBlank(commentContent)) {
				ToastUtils.show(getApplicationContext(),
						getString(R.string.msgcontent_cannot_null));
				break;
			}
			sendComment(commentContent);
			break;
		default:
			break;
		}
	}

	/**
	 * 发出评论
	 */
	private void sendComment(String commentContent) {
		
		if (NetUtils.isNetworkConnected(getApplicationContext())) {
			String commentConbineResult = getConbineComment(commentContent);
			apiService.sendMsg(cid, commentConbineResult, "txt_comment",
					msg.getMid(), "");
			msgEdit.setText("");
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
	 *
	 */
	private Comment combineComment(String content) {
		String uid = ((MyApplication)getApplicationContext()).getUid();
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
			SpannableString spannableString = MentionsAndUrlShowUtils.handleMentioin(source,mentionList,urlList);
			contentText.setText(spannableString);
			TransHtmlToTextUtils.stripUnderlines(contentText,
					Color.parseColor("#0f7bca"));

			String time = TimeUtils.getDisplayTime(getApplicationContext(),
					comment.getTimestamp());
			sendTimeText.setText(time);

			new ImageDisplayUtils(ChannelMsgDetailActivity.this,
					R.drawable.icon_person_default).display(photoImg,
					UriUtils.getChannelImgUri(comment.getUid()));
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

	class TextChangedListener implements TextWatcher {
		String changeContent = "";
		String channelType = ChannelCacheUtils.getChannelType(getApplicationContext(),
				cid);

		@Override
		public void beforeTextChanged(CharSequence s, int start, int count,
				int after) {
		}

		@Override
		public void onTextChanged(CharSequence s, int start, int before,
				int count) {
			beginPosition = start;
			endPosition = start + count;
			changeContent = s.toString().substring(beginPosition, endPosition);
			if (s.toString().length() > editDeletePosition) {
				canMention = true;
			}
			ForeColorSpan[] a = ((Spanned) s).getSpans(0, s.length(),
					ForeColorSpan.class);
			int which = -1;
			for (int i = 0; i < userNameList.size(); i++) {
				if (!s.toString().contains(userNameList.get(i))) {
					which = i;
					userNameList.remove(i);
					uidList.remove(i);
				}
			}
			for (int i = 0; i < a.length; i++) {
				if (which == i) {
					int started = ((Spannable) s).getSpanStart(a[i]);
					int end = ((Spannable) s).getSpanEnd(a[i]);
					msgEdit.getText().delete(started, end);
				}
			}
		}

		@Override
		public void afterTextChanged(Editable s) {
			Intent intent = new Intent();
			intent.putExtra("title", "@");
			intent.putExtra("cid", cid);
			intent.setClass(getApplicationContext(), MembersActivity.class);
			String textChangeString = s.toString();
			int textChangeLength = textChangeString.length();
			if (!StringUtils.isBlank(s.toString())
					&& canMention
					&& (textChangeString.substring(textChangeLength - 1,
							textChangeLength).equals("@") || changeContent
							.equals("@")) && channelType.equals("GROUP")) {
				overridePendingTransition(R.anim.activity_open, 0);
				startActivityForResult(intent, RESULT_MENTIONS);
			}
			canMention = true;
		}

	}

	class OnMentionsListener implements OnKeyListener {
		@Override
		public boolean onKey(View v, int keyCode, KeyEvent event) {
			String str = msgEdit.getText().toString();
			editDeletePosition = str.length();
			if (StringUtils.isBlank(msgEdit.getText().toString())) {
				canMention = true;
				return false;
			}
			if (keyCode == 67) {
				canMention = false;
			} else {
				canMention = true;
			}
			return false;
		}

	}

	public String getConbineComment(String content) {
		String source = "";
		ArrayList<String> urlList = URLMatcher.getUrls(content);
		JSONObject richTextObj = new JSONObject();
		source = HandleMsgTextUtils.handleMentionAndURL(content, userNameList,
				uidList);
		JSONArray mentionArray = JSONUtils.toJSONArray(uidList);
		JSONArray urlArray = JSONUtils.toJSONArray(urlList);
		try {
			richTextObj.put("source", source);
			richTextObj.put("mentions", mentionArray);
			richTextObj.put("urls", urlArray);
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
				GetMsgCommentResult getMsgCommentResult) {
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
		public void returnSendMsgFail(String error) {
			WebServiceMiddleUtils.hand(ChannelMsgDetailActivity.this, error);
		}
	}

}
