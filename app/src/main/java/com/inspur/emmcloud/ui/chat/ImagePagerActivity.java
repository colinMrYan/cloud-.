package com.inspur.emmcloud.ui.chat;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.text.SpannableString;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.inspur.emmcloud.BaseFragmentActivity;
import com.inspur.emmcloud.MyApplication;
import com.inspur.emmcloud.R;
import com.inspur.emmcloud.api.APIInterfaceInstance;
import com.inspur.emmcloud.api.apiservice.ChatAPIService;
import com.inspur.emmcloud.bean.Comment;
import com.inspur.emmcloud.bean.GetMsgCommentResult;
import com.inspur.emmcloud.bean.Msg;
import com.inspur.emmcloud.ui.contact.UserInfoActivity;
import com.inspur.emmcloud.util.ChannelCacheUtils;
import com.inspur.emmcloud.util.HandleMsgTextUtils;
import com.inspur.emmcloud.util.ImageDisplayUtils;
import com.inspur.emmcloud.util.InputMethodUtils;
import com.inspur.emmcloud.util.IntentUtils;
import com.inspur.emmcloud.util.JSONUtils;
import com.inspur.emmcloud.util.LogUtils;
import com.inspur.emmcloud.util.MentionsAndUrlShowUtils;
import com.inspur.emmcloud.util.NetUtils;
import com.inspur.emmcloud.util.PreferencesUtils;
import com.inspur.emmcloud.util.StateBarColor;
import com.inspur.emmcloud.util.TimeUtils;
import com.inspur.emmcloud.util.TransHtmlToTextUtils;
import com.inspur.emmcloud.util.URLMatcher;
import com.inspur.emmcloud.util.UriUtils;
import com.inspur.emmcloud.widget.ECMChatInputMenu;
import com.inspur.emmcloud.widget.HackyViewPager;
import com.inspur.emmcloud.widget.ImageDetailFragment;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;


/**
 * 图片查看器
 */
public class ImagePagerActivity extends BaseFragmentActivity {
	private static final int RESULT_MENTIONS = 5;
	public static final String EXTRA_IMAGE_INDEX = "image_index";
	public static final String EXTRA_IMAGE_URLS = "image_urls";
	public static final String EXTRA_CURRENT_IMAGE_MSG = "channel_current_image_msg";
	public static final String EXTRA_IMAGE_MSG_LIST = "channel_image_msg_list";
	public static final String PHOTO_SELECT_X_TAG = "PHOTO_SELECT_X_TAG";
	public static final String PHOTO_SELECT_Y_TAG = "PHOTO_SELECT_Y_TAG";
	public static final String PHOTO_SELECT_W_TAG = "PHOTO_SELECT_W_TAG";
	public static final String PHOTO_SELECT_H_TAG = "PHOTO_SELECT_H_TAG";

	private ECMChatInputMenu ecmChatInputMenu;
	private ListView commentListView;
	private RelativeLayout imgCommentLayout;
	private HackyViewPager mPager;
	private int pagerPosition;
	private int pageStartPosition = 0;
	private List<Msg> imgTypeMsgList = new ArrayList<>();
	private ArrayList<String> urlList = new ArrayList<>();
	private String cid;
	private int locationX, locationY, locationW, locationH;
	private ImagePagerAdapter mAdapter;
	private List<Comment> commentList = new ArrayList<>();
	private RelativeLayout functionLayout;


	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_image_pager);
		((MyApplication) getApplicationContext())
				.addActivity(this);
		StateBarColor.changeStateBarColor(this, R.color.black);
		init();
	}

	@Override
	protected void onNewIntent(Intent intent) {
		// TODO Auto-generated method stub
		super.onNewIntent(intent);
		setIntent(intent);
		getIntent().putExtras(intent);
		init();
	}

	private void init() {
		initIntentData();
		functionLayout = (RelativeLayout)findViewById(R.id.function_layout);
		imgCommentLayout = (RelativeLayout) findViewById(R.id.img_comment_layout);
		if (getIntent().hasExtra(EXTRA_CURRENT_IMAGE_MSG)) {
			ecmChatInputMenu = (ECMChatInputMenu) findViewById(R.id.chat_input_menu);
			initEcmChatInputMenu();
			commentListView = (ListView) findViewById(R.id.comment_list);
			commentListView.setAdapter(adapter);
			(findViewById(R.id.comment_num_text)).setVisibility(View.VISIBLE);
			(findViewById(R.id.enter_channel_imgs_img)).setVisibility(View.VISIBLE);
			cid = imgTypeMsgList.get(0).getCid();
			String channelType = ChannelCacheUtils.getChannelType(getApplicationContext(), cid);
			if (channelType != null && channelType.equals("GROUP")) {
				ecmChatInputMenu.setIsChannelGroup(true, cid);
			}
		}

		mPager = (HackyViewPager) findViewById(R.id.pager);
		mAdapter = new ImagePagerAdapter(getSupportFragmentManager(), urlList);
		mPager.setAdapter(mAdapter);
		initIndicatorView();
		pagerPosition = pageStartPosition;
		mPager.setCurrentItem(pagerPosition);
	}

	/**
	 * 初始化评论输入框
	 */
	private void initEcmChatInputMenu() {
		ecmChatInputMenu.showAddBtn(false);
		ecmChatInputMenu.setChatInputMenuListener(new ECMChatInputMenu.ChatInputMenuListener() {
			@Override
			public void onSetContentViewHeight(boolean isLock) {

			}

			@Override
			public void onSendMsg(String content, List<String> mentionsUidList, List<String> mentionsUserNameList) {
				sendComment(content, mentionsUidList, mentionsUserNameList);
				InputMethodUtils.hide(ImagePagerActivity.this);
			}
		});
	}

	@Override
	public void onBackPressed() {
		mAdapter.getCurrentFragment().closeImg();
	}

	public void onClick(View v) {
		switch (v.getId()) {
			case R.id.close_img:
				mAdapter.getCurrentFragment().closeImg();
				break;
			case R.id.download_img:
				mAdapter.getCurrentFragment().downloadImg();
				break;
			case R.id.enter_channel_imgs_img:
//				Intent intent = new Intent(ImagePagerActivity.this,GroupAlbumActivity.class);
//				intent.putExtra("cid", cid);
//				startActivity(intent);


				Bundle bundle = new Bundle();
				bundle.putString("cid", cid);
				IntentUtils.startActivity(ImagePagerActivity.this,
						GroupAlbumActivity.class, bundle);
				break;
			case R.id.comment_num_text:
				commentList.clear();
				adapter.notifyDataSetChanged();
				getImgComment(imgTypeMsgList.get(pagerPosition).getMid());
				imgCommentLayout.setVisibility(View.VISIBLE);
				break;
			case R.id.img_comment_top_layout:
				InputMethodUtils.hide(ImagePagerActivity.this);
				imgCommentLayout.setVisibility(View.GONE);
				break;
			default:
				break;
		}
	}


	/**
	 * 初始化Indicator
	 */
	private void initIndicatorView() {
		final TextView indicator = (TextView) findViewById(R.id.indicator);
//		if (mPager.getAdapter().getCount() > 1) {
//			indicator.setVisibility(View.VISIBLE);
//		}
//		CharSequence text = getString(R.string.viewpager_indicator, 1, mPager.getAdapter().getCount());
//		indicator.setText(text);
		mPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
			@Override
			public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

			}

			@Override
			public void onPageSelected(int position) {
				CharSequence text = getString(R.string.viewpager_indicator, position + 1, mPager.getAdapter().getCount());
				indicator.setText(text);
				pagerPosition = position;
			}

			@Override
			public void onPageScrollStateChanged(int state) {

			}
		});
	}


	/**
	 * 初始化Intent传递的数据
	 */
	private void initIntentData() {

		if (getIntent().hasExtra(EXTRA_CURRENT_IMAGE_MSG)) {
			Msg currentMsg = (Msg) getIntent().getSerializableExtra(EXTRA_CURRENT_IMAGE_MSG);
			urlList = new ArrayList<>();
			cid = currentMsg.getCid();
			imgTypeMsgList = (List<Msg>) getIntent().getSerializableExtra(EXTRA_IMAGE_MSG_LIST);
			for (int i = 0; i < imgTypeMsgList.size(); i++) {
				Msg msg = imgTypeMsgList.get(i);
				String url = UriUtils.getPreviewUri(msg.getImgTypeMsgImg());
				urlList.add(url);
			}
			pageStartPosition = imgTypeMsgList.indexOf(currentMsg);
		} else {
			urlList = getIntent().getStringArrayListExtra(EXTRA_IMAGE_URLS);
			pageStartPosition = getIntent().getIntExtra(EXTRA_IMAGE_INDEX, 0);
		}
		locationX = getIntent().getIntExtra(PHOTO_SELECT_X_TAG, 0);
		locationY = getIntent().getIntExtra(PHOTO_SELECT_Y_TAG, 0);
		locationW = getIntent().getIntExtra(PHOTO_SELECT_W_TAG, 0);
		locationH = getIntent().getIntExtra(PHOTO_SELECT_H_TAG, 0);
	}

	private void addLocalComment(String commentConbineSendText) {
		Comment newComment = combineComment(commentConbineSendText);
		commentList.add(0, newComment);
		adapter.notifyDataSetChanged();
		// 滚动到页面最后
	}

	/**
	 * 拼接评论发送的内容
	 *
	 * @param content
	 * @return
	 */
	public String getConbineCommentSendText(String content, List<String> mentionsUidList, List<String> mentionsUserNameList) {
		String source = "";
		ArrayList<String> urlList = URLMatcher.getUrls(content);
		JSONObject richTextObj = new JSONObject();
		source = HandleMsgTextUtils.handleMentionAndURL(content, mentionsUserNameList,
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
	 * 拼装评论消息
	 *
	 * @param content
	 * @return
	 */
	private Comment combineComment(String content) {
		String uid = ((MyApplication) getApplicationContext()).getUid();
		String title = PreferencesUtils.getString(
				getApplicationContext(), "userRealName");
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

	public void onPhotoTap() {
		LogUtils.jasonDebug("onPhotoTap----------------------------");
		if (functionLayout.getVisibility() == View.VISIBLE){
			functionLayout.setVisibility(View.GONE);
			imgCommentLayout.setVisibility(View.GONE);
		}else {
			functionLayout.setVisibility(View.VISIBLE);
		}
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (resultCode != RESULT_CANCELED && requestCode == RESULT_MENTIONS) {
			ecmChatInputMenu.setMentionData(data);
		}
	}

	private class ImagePagerAdapter extends FragmentStatePagerAdapter {

		public List<String> urlList;
		private ImageDetailFragment currentFragment;

		public ImagePagerAdapter(FragmentManager fm, List<String> urlList) {
			super(fm);
			this.urlList = urlList;
		}

		@Override
		public int getCount() {
			return urlList == null ? 0 : urlList.size();
		}

		@Override
		public Fragment getItem(int position) {
			String url = urlList.get(position);
			boolean isTargetPosition = (position == pageStartPosition);
			return ImageDetailFragment.newInstance(url, locationW, locationH, locationX, locationY, isTargetPosition);
		}

		@Override
		public void setPrimaryItem(ViewGroup container, int position, Object object) {
			currentFragment = (ImageDetailFragment)object;
			super.setPrimaryItem(container, position, object);
		}

		public ImageDetailFragment getCurrentFragment() {
			return currentFragment;
		}

	}


	private BaseAdapter adapter = new BaseAdapter() {
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
			convertView = LayoutInflater.from(getApplicationContext()).inflate(R.layout.chat_img_msg_comment_item_view, null);
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

			new ImageDisplayUtils(ImagePagerActivity.this,
					R.drawable.icon_person_default).display(photoImg,
					UriUtils.getChannelImgUri(comment.getUid()));
			photoImg.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					String uid = comment.getUid();
					openUserInfo(uid);
				}
			});
			return convertView;
		}
	};

	/**
	 * 打开个人信息
	 *
	 * @param uid
	 */
	private void openUserInfo(String uid) {
		Bundle bundle = new Bundle();
		bundle.putString("uid", uid);
		IntentUtils.startActivity(ImagePagerActivity.this,
				UserInfoActivity.class, bundle);
	}


	@Override
	protected void onDestroy() {
		LogUtils.jasonDebug("onDestroy-------------------");
		super.onDestroy();
	}

	@Override
	protected void onPause() {
		LogUtils.jasonDebug("onPause-------------------");
		super.onPause();
	}

	@Override
	protected void onStart() {
		LogUtils.jasonDebug("onStart-------------------");
		super.onStart();
	}

	/**
	 * 获取消息的评论
	 *
	 * @param mid
	 */
	private void getImgComment(String mid) {
		if (NetUtils.isNetworkConnected(getApplicationContext())) {
			ChatAPIService apiService = new ChatAPIService(getApplicationContext());
			apiService.setAPIInterface(new WebService());
			apiService.getComment(mid);
		}
	}

	private void sendComment(String content, List<String> mentionsUidList, List<String> mentionsUserNameList) {
		if (NetUtils.isNetworkConnected(getApplicationContext())) {
			ChatAPIService apiService = new ChatAPIService(getApplicationContext());
			apiService.setAPIInterface(new WebService());
			String commentConbineSendText = getConbineCommentSendText(content, mentionsUidList, mentionsUserNameList);
			apiService.sendMsg(cid, commentConbineSendText, "txt_comment",
					imgTypeMsgList.get(pagerPosition).getMid(), "");
			addLocalComment(commentConbineSendText);
		}
	}

	private class WebService extends APIInterfaceInstance {
		@Override
		public void returnMsgCommentSuccess(GetMsgCommentResult getMsgCommentResult) {
			commentList = getMsgCommentResult.getCommentList();
			Collections.reverse(commentList);
			adapter.notifyDataSetChanged();


		}

		@Override
		public void returnMsgCommentFail(String error) {
			super.returnMsgCommentFail(error);
		}
	}

}
