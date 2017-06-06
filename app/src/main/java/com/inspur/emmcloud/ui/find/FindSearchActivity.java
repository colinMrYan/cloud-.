/**
 * 
 * FindSearchActivity.java
 * classes : com.inspur.emmcloud.ui.find.FindSearchActivity
 * V 1.0.0
 * Create at 2016年10月17日 下午4:21:25
 */
package com.inspur.emmcloud.ui.find;

import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.ExpandableListView.OnChildClickListener;
import android.widget.ExpandableListView.OnGroupClickListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.inspur.emmcloud.BaseActivity;
import com.inspur.emmcloud.MyApplication;
import com.inspur.emmcloud.R;
import com.inspur.emmcloud.api.APIInterfaceInstance;
import com.inspur.emmcloud.api.apiservice.FindAPIService;
import com.inspur.emmcloud.bean.Channel;
import com.inspur.emmcloud.bean.FindSearchContacts;
import com.inspur.emmcloud.bean.FindSearchMsgHistory;
import com.inspur.emmcloud.bean.FindSearchNews;
import com.inspur.emmcloud.bean.GetFindMixSearchResult;
import com.inspur.emmcloud.bean.GetFindSearchResult;
import com.inspur.emmcloud.config.MyAppConfig;
import com.inspur.emmcloud.ui.app.groupnews.NewsWebDetailActivity;
import com.inspur.emmcloud.ui.contact.UserInfoActivity;
import com.inspur.emmcloud.util.ChannelCacheUtils;
import com.inspur.emmcloud.util.DensityUtil;
import com.inspur.emmcloud.util.DirectChannelUtils;
import com.inspur.emmcloud.util.ImageDisplayUtils;
import com.inspur.emmcloud.util.IntentUtils;
import com.inspur.emmcloud.util.LogUtils;
import com.inspur.emmcloud.util.NetUtils;
import com.inspur.emmcloud.util.StringUtils;
import com.inspur.emmcloud.util.ToastUtils;
import com.inspur.emmcloud.util.UriUtils;
import com.inspur.emmcloud.util.WebServiceMiddleUtils;
import com.inspur.emmcloud.widget.CircleImageView;
import com.inspur.emmcloud.widget.ClearEditText;
import com.inspur.emmcloud.widget.pullableview.PullToRefreshLayout;
import com.inspur.emmcloud.widget.pullableview.PullToRefreshLayout.OnRefreshListener;
import com.inspur.emmcloud.widget.pullableview.PullableExpandableListView;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * com.inspur.emmcloud.ui.find.FindSearchActivity create at 2016年10月17日
 * 下午4:21:25
 */
public class FindSearchActivity extends BaseActivity implements
		OnRefreshListener {

	private PopupWindow popupWindow;
	private TextView searchTypeText;
	private RelativeLayout headerLayout;
	private PullToRefreshLayout pullToRefreshLayout;
	private PullableExpandableListView expandableListView;
	private ClearEditText searchEdit;
	private String dataType = "(user OR news)";
	// private LoadingDialog loadingDlg;
	private FindAPIService apiService;
	private List<String> findSearchItemList = new ArrayList<String>();
	private List<FindSearchContacts> findSearchContactList = new ArrayList<FindSearchContacts>();
	private List<FindSearchNews> findSearchNewsList = new ArrayList<FindSearchNews>();
	private List<FindSearchMsgHistory> findSearchMsgHistoryList = new ArrayList<FindSearchMsgHistory>();
	private MyAdapter adapter;
	private String keyword;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		((MyApplication) getApplicationContext()).addActivity(this);
		setContentView(R.layout.activity_find_search);
		initViews();
	}

	/**
	 * 初始化View
	 */
	private void initViews() {
		// TODO Auto-generated method stub
		searchEdit = (ClearEditText) findViewById(R.id.search_edit);
		// searchEdit.setOnEditorActionListener(this);
		searchEdit.addTextChangedListener(watcher);
		searchTypeText = (TextView) findViewById(R.id.search_type_text);
		headerLayout = (RelativeLayout) findViewById(R.id.header_layout);
		pullToRefreshLayout = (PullToRefreshLayout) findViewById(R.id.expand_layout);
		pullToRefreshLayout.setOnRefreshListener(this);
		expandableListView = (PullableExpandableListView) findViewById(R.id.expand_list);
		expandableListView.setGroupIndicator(null);
		expandableListView.setVerticalScrollBarEnabled(false);
		expandableListView.setHeaderDividersEnabled(false);
		expandableListView.setCanpullup(false);
		expandableListView.setCanpulldown(false);
		adapter = new MyAdapter();
		expandableListView.setAdapter(adapter);
		expandableListView.setOnGroupClickListener(OnGroupClick);
		expandableListView.setOnChildClickListener(onChildClick);
		// loadingDlg = new LoadingDialog(FindSearchActivity.this);
		apiService = new FindAPIService(getApplicationContext());
		apiService.setAPIInterface(new WebService());
	}

	// @Override
	// public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
	// // TODO Auto-generated method stub
	// if (actionId == EditorInfo.IME_ACTION_SEARCH) {
	// String searchContent = searchEdit.getText().toString();
	// search(searchContent);
	// }
	// return false;
	// }

	private TextWatcher watcher = new TextWatcher() {

		@Override
		public void onTextChanged(CharSequence s, int start, int before,
				int count) {
			// TODO Auto-generated method stub

		}

		@Override
		public void beforeTextChanged(CharSequence s, int start, int count,
				int after) {
			// TODO Auto-generated method stub

		}

		@Override
		public void afterTextChanged(Editable s) {
			// TODO Auto-generated method stub
			keyword = s.toString();
			if (StringUtils.isBlank(keyword)) {
				findSearchContactList.clear();
				findSearchMsgHistoryList.clear();
				findSearchMsgHistoryList.clear();
				findSearchItemList.clear();
				adapter.notifyDataSetChanged();
				LogUtils.debug("jason", "clear-------------");
			} else {
				search();
			}

		}
	};

	private OnGroupClickListener OnGroupClick = new OnGroupClickListener() {

		@Override
		public boolean onGroupClick(ExpandableListView parent, View v,
				int groupPosition, long id) {
			// TODO Auto-generated method stub
			String type = findSearchItemList.get(groupPosition);
			if ((type.equals("user") && findSearchContactList.size() > 3)
					|| (type.equals("news") && findSearchNewsList.size() > 3)) {
				Bundle bundle = new Bundle();
				bundle.putString("type", type);
				bundle.putString("keyword", searchEdit.getText().toString());
				IntentUtils.startActivity(FindSearchActivity.this,
						FindSearchMoreActivity.class, bundle);
			}
			return false;
		}
	};

	private OnChildClickListener onChildClick = new OnChildClickListener() {

		@Override
		public boolean onChildClick(ExpandableListView parent, View v,
				int groupPosition, int childPosition, long id) {
			// TODO Auto-generated method stub
			String type = findSearchItemList.get(groupPosition);
			if (type.equals("user")) {
				String inspurId = findSearchContactList.get(childPosition)
						.getInspurId();
				Bundle bundle = new Bundle();
				bundle.putString("uid", inspurId);
				IntentUtils.startActivity(FindSearchActivity.this,
						UserInfoActivity.class, bundle);
			} else {
				FindSearchNews findSearchNews = findSearchNewsList
						.get(childPosition);
				String url = findSearchNews.getUrl();
				String poster = findSearchNews.getPoster();
				String title = findSearchNews.getTitle();
				Bundle bundle = new Bundle();
				bundle.putString("url", url);
				bundle.putString("poster", poster);
				bundle.putString("title", title);
				IntentUtils.startActivity(FindSearchActivity.this,
						NewsWebDetailActivity.class, bundle);
			}
			return false;
		}
	};

	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.cancel_text:
			finish();
			break;

		case R.id.search_type_layout:
			showSearchTypePop();
			break;
		case R.id.search_all_layout:
			if (!dataType.equals("(user OR news)")) {
				searchTypeText.setText(R.string.all);
				dataType = "(user OR news)";
				if (!StringUtils.isBlank(keyword)) {
					search();
				}
			}
			popupWindow.dismiss();
			break;
		case R.id.search_contact_layout:
			if (!dataType.equals("user")) {
				searchTypeText.setText(R.string.find_search_contacts);
				dataType = "user";
				if (!StringUtils.isBlank(keyword)) {
					search();
				}
			}
			popupWindow.dismiss();
			break;
		case R.id.search_chat_history_layout:
			if (!dataType.equals("msg")) {
				searchTypeText.setText(R.string.find_search_chat_history);
				dataType = "msg";
				if (!StringUtils.isBlank(keyword)) {
					search();
				}
			}
			popupWindow.dismiss();
			break;
		case R.id.search_news_layout:
			if (!dataType.equals("news")) {
				dataType = "news";
				searchTypeText.setText(R.string.find_search_news);
				if (!StringUtils.isBlank(keyword)) {
					search();
				}
			}

			popupWindow.dismiss();
			break;
		default:
			break;
		}
	}

	/**
	 * 弹出搜索内容选择框
	 */
	private void showSearchTypePop() {
		// TODO Auto-generated method stub
		View popView = LayoutInflater.from(FindSearchActivity.this).inflate(
				R.layout.pop_search_type, null);
		// 设置按钮的点击事件
		popupWindow = new PopupWindow(popView, DensityUtil.dip2px(
				getApplicationContext(), 125),
				LinearLayout.LayoutParams.WRAP_CONTENT, true);
		popupWindow.setTouchable(true);
		popupWindow.setBackgroundDrawable(ContextCompat.getDrawable(
				getApplicationContext(), R.drawable.pop_window_view_tran));
		popupWindow.setOutsideTouchable(true);
		popupWindow.showAsDropDown(headerLayout,
				DensityUtil.dip2px(getApplicationContext(), 10), 0);
	}

	public class MyAdapter extends BaseExpandableListAdapter {

		@Override
		public int getGroupCount() {
			return findSearchItemList.size();
		}

		@Override
		public int getChildrenCount(int groupPosition) {
			String key = findSearchItemList.get(groupPosition);
			if (key.equals("user")) {
				return findSearchContactList.size() > 3 ? 3
						: findSearchContactList.size();
			} else if (key.equals("msg")) {
				return findSearchMsgHistoryList.size() > 3 ? 3
						: findSearchMsgHistoryList.size();
			} else {
				return findSearchNewsList.size() > 3 ? 3 : findSearchNewsList
						.size();
			}
		}

		@Override
		public Object getGroup(int groupPosition) {
			return 0;
		}

		@Override
		public Object getChild(int groupPosition, int childPosition) {
			return 0;
		}

		@Override
		public long getGroupId(int groupPosition) {
			return groupPosition;
		}

		@Override
		public long getChildId(int groupPosition, int childPosition) {
			return childPosition;
		}

		@Override
		public boolean hasStableIds() {
			return false;
		}

		/**
		 * 显示：group
		 */
		@Override
		public View getGroupView(final int groupPosition, boolean isExpanded,
				View convertView, ViewGroup parent) {
			PullableExpandableListView expandableListView = (PullableExpandableListView) parent;
			expandableListView.expandGroup(groupPosition);
			convertView = LayoutInflater.from(getApplicationContext()).inflate(
					R.layout.find_search_expand_group_item_view, null);
			TextView titleText = (TextView) convertView
					.findViewById(R.id.title_text);
			TextView moreText = (TextView) convertView
					.findViewById(R.id.more_text);
			RelativeLayout blankLayout = (RelativeLayout) convertView
					.findViewById(R.id.blank_layout);
			if (groupPosition == 0) {
				blankLayout.setVisibility(View.GONE);
			} else {
				blankLayout.setVisibility(View.VISIBLE);
			}
			String key = findSearchItemList.get(groupPosition);
			int size = 0;
			if (key.equals("user")) {
				titleText.setText(getString(R.string.find_search_contacts));
				size = findSearchContactList.size();
			} else if (key.equals("msg")) {
				titleText.setText(getString(R.string.find_search_chat_history));
				size = findSearchMsgHistoryList.size();
			} else {
				titleText.setText(getString(R.string.news));
				size = findSearchNewsList.size();
			}
			if (size > 3) {
				moreText.setVisibility(View.VISIBLE);
			} else {
				moreText.setVisibility(View.INVISIBLE);
			}
			return convertView;
		}

		/**
		 * 显示：child
		 */
		@Override
		public View getChildView(final int groupPosition,
				final int childPosition, boolean isLastChild, View convertView,
				ViewGroup parent) {
			String key = findSearchItemList.get(groupPosition);
			View lineView = null;
			if (key.equals("news")) {
				convertView = LayoutInflater
						.from(getApplicationContext())
						.inflate(
								R.layout.find_search_expand_child_news_item_view,
								null);
				FindSearchNews findSearchNews = findSearchNewsList
						.get(childPosition);
				ImageView newsImg = (ImageView) convertView
						.findViewById(R.id.news_img);
				TextView newsTitleText = (TextView) convertView
						.findViewById(R.id.title_text);
				TextView publisherText = (TextView) convertView
						.findViewById(R.id.publisher_text);
				TextView timeText = (TextView) convertView
						.findViewById(R.id.timet_text);
				lineView = (View) convertView.findViewById(R.id.line_view);
				String imgUrl = UriUtils.getPreviewUri(findSearchNews
						.getPoster());
				new ImageDisplayUtils(getApplicationContext(),
						R.drawable.group_news_ic).display(newsImg, imgUrl);
				newsTitleText.setText(findSearchNews.getTitle());
				publisherText.setText(findSearchNews.getPublisher());
				timeText.setText(findSearchNews.getPostTime());

			} else {
				convertView = LayoutInflater
						.from(getApplicationContext())
						.inflate(
								R.layout.find_search_expand_child_contact_item_view,
								null);
				CircleImageView photoImg = (CircleImageView) convertView
						.findViewById(R.id.photo_img);
				TextView titleText = (TextView) convertView
						.findViewById(R.id.title_text);
				TextView contentText = (TextView) convertView
						.findViewById(R.id.content_text);
				lineView = (View) convertView.findViewById(R.id.line_view);
				if (key.equals("user")) {
					FindSearchContacts findSearchContacts = findSearchContactList
							.get(childPosition);
					String inspurId = findSearchContacts.getInspurId();
					new ImageDisplayUtils(getApplicationContext(),
							R.drawable.icon_person_default).display(photoImg,
							UriUtils.getChannelImgUri(inspurId));
					titleText.setText(findSearchContacts.getName());
					String mobile = findSearchContacts.getMobile();
					String mail = findSearchContacts.getEmail();
					if (!StringUtils.isBlank(mobile)) {
						mobile = mobile + "  ";
					}
					contentText.setText(mobile + mail);

				} else {
					FindSearchMsgHistory findSearchMsgHistory = findSearchMsgHistoryList
							.get(childPosition);
					titleText.setText(findSearchMsgHistory.getToName());
					contentText.setText(findSearchMsgHistory.getUserName()
							+ "：" + findSearchMsgHistory.getBody());
					Channel channel = ChannelCacheUtils.getChannel(
							getApplicationContext(),
							findSearchMsgHistory.getToId());
					setChannelIcon(channel, photoImg);
				}

			}
			if (childPosition == getChildrenCount(groupPosition) - 1) {
				lineView.setVisibility(View.INVISIBLE);
			} else {
				lineView.setVisibility(View.VISIBLE);
			}
			return convertView;
		}

		@Override
		public boolean isChildSelectable(int groupPosition, int childPosition) {
			return true;
		}

	}

	/**
	 * 设置Channel的Icon
	 * 
	 * @param channel
	 * @param channelImg
	 */
	private void setChannelIcon(Channel channel, CircleImageView channelImg) {
		// TODO Auto-generated method stub
		if (channel == null) {
			return;
		}
		Integer defaultIcon = -1; // 默认显示图标
		String iconUrl = "";// Channel头像的uri
		if (channel.getType().equals("DIRECT")) {
			defaultIcon = R.drawable.icon_person_default;
			iconUrl = DirectChannelUtils.getDirectChannelIcon(
					getApplicationContext(), channel.getTitle());
		} else if (channel.getType().equals("GROUP")) {
			defaultIcon = R.drawable.icon_channel_group_default;
			File file = new File(MyAppConfig.LOCAL_CACHE_PATH, UriUtils.tanent
					+ channel.getCid() + "_100.png1");
			if (file.exists()) {
				iconUrl = "file://" + file.getAbsolutePath();
			}
		} else {
			defaultIcon = R.drawable.icon_channel_group_default;
			iconUrl = channel.getIcon();
		}
		new ImageDisplayUtils(getApplicationContext(), defaultIcon).display(
				channelImg, iconUrl);

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.inspur.emmcloud.widget.pullableview.PullToRefreshLayout.OnRefreshListener
	 * #onRefresh(com.inspur.emmcloud.widget.pullableview.PullToRefreshLayout)
	 */
	@Override
	public void onRefresh(PullToRefreshLayout pullToRefreshLayout) {
		// TODO Auto-generated method stub

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.inspur.emmcloud.widget.pullableview.PullToRefreshLayout.OnRefreshListener
	 * #onLoadMore(com.inspur.emmcloud.widget.pullableview.PullToRefreshLayout)
	 */
	@Override
	public void onLoadMore(PullToRefreshLayout pullToRefreshLayout) {
		// TODO Auto-generated method stub
	}

	/**
	 * 搜索关键字
	 */
	private void search() {
		// TODO Auto-generated method stub
		if (NetUtils.isNetworkConnected(getApplicationContext())) {
			if (dataType.equals("(user OR news)")) {
				apiService.findMixSearch(keyword);
			} else {
				String type = "datatype:" + dataType;
				apiService.findSearch(keyword, type, 0, 4, 0);
			}
		}
	}

	private class WebService extends APIInterfaceInstance {

		@Override
		public void returnFindSearchSuccess(
				GetFindSearchResult getFindSearchResult) {
			// TODO Auto-generated method stub
			findSearchItemList = getFindSearchResult.getFindSearchItemList();
			findSearchContactList = getFindSearchResult
					.getFindSearchContactList();
			findSearchNewsList = getFindSearchResult.getFindSearchNewsList();
			adapter.notifyDataSetChanged();
			if (findSearchItemList.size() == 0) {
				ToastUtils.show(getApplicationContext(),
						R.string.no_search_term);
			}

		}

		@Override
		public void returnFindSearchFail(String error,int errorCode) {
			// TODO Auto-generated method stub
			WebServiceMiddleUtils.hand(FindSearchActivity.this, error,errorCode);
		}

		@Override
		public void returnFindMixSearchSuccess(
				GetFindMixSearchResult getFindMixSearchResult) {
			// TODO Auto-generated method stub
			if (!StringUtils.isBlank(keyword)) {
				findSearchItemList = getFindMixSearchResult.getFindSearchItemList();
				findSearchContactList = getFindMixSearchResult
						.getFindSearchContactList();
				findSearchNewsList = getFindMixSearchResult.getFindSearchNewsList();
				adapter.notifyDataSetChanged();
				if (findSearchItemList.size() == 0) {
					ToastUtils.show(getApplicationContext(),
							R.string.no_search_term);
				}

			}
		}

		@Override
		public void returnFindMixSearchFail(String error,int errorCode) {
			// TODO Auto-generated method stub
			WebServiceMiddleUtils.hand(FindSearchActivity.this, error,errorCode);
		}

	}

}
