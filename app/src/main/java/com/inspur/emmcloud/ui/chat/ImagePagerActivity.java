package com.inspur.emmcloud.ui.chat;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.util.ArrayMap;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.inspur.emmcloud.BaseFragmentActivity;
import com.inspur.emmcloud.MyApplication;
import com.inspur.emmcloud.R;
import com.inspur.emmcloud.api.APIInterfaceInstance;
import com.inspur.emmcloud.api.apiservice.ChatAPIService;
import com.inspur.emmcloud.bean.Comment;
import com.inspur.emmcloud.bean.GetMsgCommentCountResult;
import com.inspur.emmcloud.bean.Msg;
import com.inspur.emmcloud.ui.contact.UserInfoActivity;
import com.inspur.emmcloud.util.ChannelCacheUtils;
import com.inspur.emmcloud.util.HandleMsgTextUtils;
import com.inspur.emmcloud.util.InputMethodUtils;
import com.inspur.emmcloud.util.IntentUtils;
import com.inspur.emmcloud.util.JSONUtils;
import com.inspur.emmcloud.util.NetUtils;
import com.inspur.emmcloud.util.PreferencesUtils;
import com.inspur.emmcloud.util.StateBarColor;
import com.inspur.emmcloud.util.TimeUtils;
import com.inspur.emmcloud.util.URLMatcher;
import com.inspur.emmcloud.util.UriUtils;
import com.inspur.emmcloud.widget.ECMChatInputMenu;
import com.inspur.emmcloud.widget.HackyViewPager;
import com.inspur.emmcloud.widget.ImageDetailFragment;
import com.inspur.emmcloud.widget.SoftKeyboardStateHelper;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;


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
	private RelativeLayout imgCommentLayout;
	private HackyViewPager mPager;
	private int pagerPosition;
	private int pageStartPosition = 0;
	private List<Msg> imgTypeMsgList = new ArrayList<>();
	private ArrayList<String> urlList = new ArrayList<>();
	private String cid;
	private int locationX, locationY, locationW, locationH;
	private ImagePagerAdapter mAdapter;
	private RelativeLayout functionLayout;
	private TextView commentCountText;
	private Map<String,Integer>commentCountMap = new ArrayMap<>();
	private Boolean isNeedTransformIn;
	private boolean isHasTransformIn =false;


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
		isHasTransformIn = false;
		init();
	}

	private void init() {
		initIntentData();
		functionLayout = (RelativeLayout) findViewById(R.id.function_layout);
		imgCommentLayout = (RelativeLayout) findViewById(R.id.img_comment_layout);
		if (getIntent().hasExtra(EXTRA_CURRENT_IMAGE_MSG)) {
			ecmChatInputMenu = (ECMChatInputMenu) findViewById(R.id.chat_input_menu);
			initEcmChatInputMenu();
			(findViewById(R.id.comment_count_text)).setVisibility(View.VISIBLE);
			(findViewById(R.id.enter_channel_imgs_img)).setVisibility(View.VISIBLE);
			(findViewById(R.id.write_comment_layout)).setVisibility(View.VISIBLE);
			cid = imgTypeMsgList.get(0).getCid();
			String channelType = ChannelCacheUtils.getChannelType(getApplicationContext(), cid);
			if (channelType != null && channelType.equals("GROUP")) {
				ecmChatInputMenu.setIsChannelGroup(true, cid);
			}
			commentCountText = (TextView) findViewById(R.id.comment_count_text);
			final SoftKeyboardStateHelper softKeyboardStateHelper = new SoftKeyboardStateHelper(findViewById(R.id.main_layout));
			softKeyboardStateHelper.addSoftKeyboardStateListener(new SoftKeyboardStateHelper.SoftKeyboardStateListener() {
				@Override
				public void onSoftKeyboardOpened(int keyboardHeightInPx) {

				}

				@Override
				public void onSoftKeyboardClosed() {
					imgCommentLayout.setVisibility(View.GONE);
				}
			});
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
				ecmChatInputMenu.setVisibility(View.GONE);
				ecmChatInputMenu.hideSoftInput();
			}
		});
	}

	@Override
	public void onBackPressed() {
		mAdapter.getCurrentFragment().closeImg();
	}

	public void onClick(View v) {
		Bundle bundle = null;
		switch (v.getId()) {
			case R.id.close_img:
				mAdapter.getCurrentFragment().closeImg();
				break;
			case R.id.download_img:
				mAdapter.getCurrentFragment().downloadImg();
				break;
			case R.id.enter_channel_imgs_img:
				bundle = new Bundle();
				bundle.putString("cid", cid);
				IntentUtils.startActivity(ImagePagerActivity.this,
						GroupAlbumActivity.class, bundle);
				break;
			case R.id.comment_count_text:
				bundle = new Bundle();
				bundle.putString("mid", imgTypeMsgList.get(pagerPosition).getMid());
				IntentUtils.startActivity(ImagePagerActivity.this,
						ChannelMsgDetailActivity.class, bundle);

				break;
			case R.id.write_comment_layout:
				imgCommentLayout.setVisibility(View.VISIBLE);
				ecmChatInputMenu.showSoftInput();
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
				if (getIntent().hasExtra(EXTRA_CURRENT_IMAGE_MSG)) {
					setCommentCount();
				}
			}

			@Override
			public void onPageScrollStateChanged(int state) {

			}
		});
	}

	/**
	 * 设置评论数目
	 */
	private void setCommentCount(){
		String mid = imgTypeMsgList.get(pagerPosition).getMid();
		if (commentCountMap.containsKey(mid)){
			int count = commentCountMap.get(mid);
			commentCountText.setText(""+count);
		}else {
			commentCountText.setText("0");
			getImgCommentCount(mid);
		}
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
		if (functionLayout.getVisibility() == View.VISIBLE) {
			functionLayout.setVisibility(View.GONE);
			imgCommentLayout.setVisibility(View.GONE);
		} else {
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
			boolean isNeedTransformOut = (position == pageStartPosition);
			if (isNeedTransformOut && isHasTransformIn == false){
				isNeedTransformIn = true;
				isHasTransformIn = true;
			}else {
				isNeedTransformIn = false;
			}
			return ImageDetailFragment.newInstance(url, locationW, locationH, locationX, locationY,isNeedTransformIn ,isNeedTransformOut);
		}

		@Override
		public void setPrimaryItem(ViewGroup container, int position, Object object) {
			currentFragment = (ImageDetailFragment) object;
			super.setPrimaryItem(container, position, object);
		}

		public ImageDetailFragment getCurrentFragment() {
			return currentFragment;
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
		IntentUtils.startActivity(ImagePagerActivity.this,
				UserInfoActivity.class, bundle);
	}




	private void getImgCommentCount(String mid) {
		if (NetUtils.isNetworkConnected(getApplicationContext())) {
			ChatAPIService apiService = new ChatAPIService(getApplicationContext());
			apiService.setAPIInterface(new WebService());
			apiService.getMsgCommentCount(mid);
		}
	}


	private void sendComment(String content, List<String> mentionsUidList, List<String> mentionsUserNameList) {
		if (NetUtils.isNetworkConnected(getApplicationContext())) {
			ChatAPIService apiService = new ChatAPIService(getApplicationContext());
			apiService.setAPIInterface(new WebService());
			String commentConbineSendText = getConbineCommentSendText(content, mentionsUidList, mentionsUserNameList);
			apiService.sendMsg(cid, commentConbineSendText, "txt_comment",
					imgTypeMsgList.get(pagerPosition).getMid(), "");
			int commentCount = commentCountMap.get(imgTypeMsgList.get(pagerPosition).getMid());
			commentCountMap.put(imgTypeMsgList.get(pagerPosition).getMid(),(commentCount+1));
			commentCountText.setText((commentCount+1)+"");
		}
	}

	private class WebService extends APIInterfaceInstance {
		@Override
		public void returnMsgCommentCountSuccess(GetMsgCommentCountResult getMsgCommentCountResult, String mid) {
			int count = getMsgCommentCountResult.getCount();
			commentCountMap.put(mid,count);
			String currentMid = imgTypeMsgList.get(pagerPosition).getMid();
			if (mid.equals(currentMid)){
				commentCountText.setText(count+"");
			}
		}

		@Override
		public void returnMsgCommentCountFail(String error) {
			super.returnMsgCommentCountFail(error);
		}
	}

}
