/**
 * 
 * FindSearchMoreActivity.java
 * classes : com.inspur.emmcloud.ui.find.FindSearchMoreActivity
 * V 1.0.0
 * Create at 2016年10月19日 下午7:27:56
 */
package com.inspur.emmcloud.ui.find;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.inspur.emmcloud.BaseActivity;
import com.inspur.emmcloud.R;
import com.inspur.emmcloud.api.APIInterfaceInstance;
import com.inspur.emmcloud.api.apiservice.FindAPIService;
import com.inspur.emmcloud.bean.FindSearchContacts;
import com.inspur.emmcloud.bean.FindSearchNews;
import com.inspur.emmcloud.bean.GetFindSearchResult;
import com.inspur.emmcloud.ui.app.groupnews.NewsWebDetailActivity;
import com.inspur.emmcloud.ui.contact.UserInfoActivity;
import com.inspur.emmcloud.util.ImageDisplayUtils;
import com.inspur.emmcloud.util.IntentUtils;
import com.inspur.emmcloud.util.LogUtils;
import com.inspur.emmcloud.util.NetUtils;
import com.inspur.emmcloud.util.StringUtils;
import com.inspur.emmcloud.util.UriUtils;
import com.inspur.emmcloud.util.WebServiceMiddleUtils;
import com.inspur.emmcloud.widget.CircleImageView;
import com.inspur.emmcloud.widget.LoadingDialog;
import com.inspur.emmcloud.widget.pullableview.PullToRefreshLayout;
import com.inspur.emmcloud.widget.pullableview.PullToRefreshLayout.OnRefreshListener;
import com.inspur.emmcloud.widget.pullableview.PullableListView;

import java.util.ArrayList;
import java.util.List;

/**
 * 发现页面搜索更多页面 com.inspur.emmcloud.ui.find.FindSearchMoreActivity create at
 * 2016年10月19日 下午7:27:56
 */
public class FindSearchMoreActivity extends BaseActivity implements
		OnRefreshListener {
	private static final int EVERY_PAGE_NUM = 15;
	private PullToRefreshLayout refreshLayout;
	private PullableListView listView;
	private String type;
	private String keyword;
	private String dataType;
	private List<FindSearchContacts> findSearchContactList = new ArrayList<FindSearchContacts>();
	private List<FindSearchNews> findSearchNewsList = new ArrayList<FindSearchNews>();
	private LoadingDialog loadingDlg;
	private FindAPIService apiService;
	private int page = 0;
	private Adapter adapter;
	private String header;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_find_search_more);
		initData();
		initView();
		search(true);

	}

	/**
	 * 初始化View
	 */
	private void initView() {
		// TODO Auto-generated method stub
		refreshLayout = (PullToRefreshLayout) findViewById(R.id.refresh_view);
		refreshLayout.setOnRefreshListener(this);
		((TextView) findViewById(R.id.header_text)).setText(header);
		listView = (PullableListView) findViewById(R.id.list);
		listView.setCanPullDown(false);
		listView.setCanPullUp(false);
		adapter = new Adapter();
		listView.setAdapter(adapter);
		listView.setOnItemClickListener(OnItemClick);
		loadingDlg = new LoadingDialog(this);
		apiService = new FindAPIService(this);
		apiService.setAPIInterface(new WebService());
	}

	/**
	 * 初始化数据
	 */
	private void initData() {
		// TODO Auto-generated method stub
		type = getIntent().getExtras().getString("type");
		keyword = getIntent().getExtras().getString("keyword");
		dataType = "datatype:" + type;
		if (type.equals("user")) {
			header = getString(R.string.find_search_contacts);
		} else {
			header = getString(R.string.news);
		}
	}
	
	/**
	 * 处理数据
	 *
	 * @param getFindSearchResult
	 */
	private void handNetData(GetFindSearchResult getFindSearchResult) {
		// TODO Auto-generated method stub
		int size = 0;
		if (type.equals("user")) {  //当type为搜索联系人时
			List<FindSearchContacts> netFindSearchContactList = getFindSearchResult
					.getFindSearchContactList();
			size = netFindSearchContactList.size();
			if (page == 0) {
				findSearchContactList = netFindSearchContactList;
			} else {
				findSearchContactList.addAll(netFindSearchContactList);
			}
		} else {  //当type为搜索新闻时
			List<FindSearchNews> netFindSearchNewsList = getFindSearchResult
					.getFindSearchNewsList();
			size = netFindSearchNewsList.size();
			if (page == 0) {
				findSearchNewsList = netFindSearchNewsList;
			} else {
				findSearchNewsList.addAll(netFindSearchNewsList);
			}
		}
		if (size == EVERY_PAGE_NUM) {
			listView.setCanPullUp(true);
		}else {
			listView.setCanPullUp(false);
		}
	}

	public void onClick(View v) {
		finish();
	}
	
	private OnItemClickListener OnItemClick = new OnItemClickListener() {

		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position,
				long id) {
			// TODO Auto-generated method stub
			if (type.equals("user")) {
				String inspurId = findSearchContactList.get(position).getInspurId();
				Bundle bundle = new Bundle();
				bundle.putString("uid", inspurId);
				IntentUtils.startActivity(FindSearchMoreActivity.this, UserInfoActivity.class, bundle);
			}else {
				FindSearchNews findSearchNews = findSearchNewsList.get(position);
				String url = findSearchNews.getUrl();
				String poster = findSearchNews.getPoster();
				String title = findSearchNews.getTitle();
				Bundle bundle = new Bundle();
				bundle.putString("url", url);
				bundle.putString("poster", poster);
				bundle.putString("title", title);
				IntentUtils.startActivity(FindSearchMoreActivity.this, NewsWebDetailActivity.class, bundle);
			}
		}
	};

	private class Adapter extends BaseAdapter {

		@Override
		public int getCount() {
			// TODO Auto-generated method stub
			if (type.equals("user")) {
				return findSearchContactList.size();
			} else {
				return findSearchNewsList.size();
			}
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
			if (type.equals("news")) {
				convertView = LayoutInflater
						.from(getApplicationContext())
						.inflate(
								R.layout.find_search_expand_child_news_item_view,
								null);
				FindSearchNews findSearchNews = findSearchNewsList
						.get(position);
				ImageView newsImg = (ImageView) convertView
						.findViewById(R.id.news_img);
				TextView newsTitleText = (TextView) convertView
						.findViewById(R.id.title_text);
				TextView publisherText = (TextView) convertView
						.findViewById(R.id.publisher_text);
				TextView timeText = (TextView) convertView
						.findViewById(R.id.timet_text);
				String imgUrl = UriUtils.getPreviewUri(findSearchNews
						.getPoster());
				new ImageDisplayUtils(R.drawable.group_news_ic).displayImage(newsImg, imgUrl);
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
				FindSearchContacts findSearchContacts = findSearchContactList
						.get(position);
				String inspurId = findSearchContacts.getInspurId();
				new ImageDisplayUtils(R.drawable.icon_person_default).displayImage(photoImg,
						UriUtils.getChannelImgUri(FindSearchMoreActivity.this,inspurId));
				titleText.setText(findSearchContacts.getName());
				String mobile = findSearchContacts.getMobile();
				String mail = findSearchContacts.getEmail();
				if (!StringUtils.isBlank(mobile)) {
					mobile = mobile + "  ";
				}
				contentText.setText(mobile + mail);

			}

			return convertView;
		}

	}

	/**
	 * 搜索关键字
	 * 
	 * @param isShowDlg
	 */
	private void search(boolean isShowDlg) {
		// TODO Auto-generated method stub
		if (NetUtils.isNetworkConnected(getApplicationContext())) {
			loadingDlg.show(isShowDlg);
			int start = 0;
			if (page != 0) {
				if (type.equals("user")) {
					start = findSearchContactList.size();
				}else {
					start = findSearchNewsList.size();
				}
			}
			LogUtils.debug("jason", "start="+start);
			apiService.findSearch(keyword, dataType, page, EVERY_PAGE_NUM,start);
		}
	}

	@Override
	public void onRefresh(PullToRefreshLayout pullToRefreshLayout) {
		// TODO Auto-generated method stub
		// if (NetUtils.isNetworkConnected(getApplicationContext())) {
		// page = 0;
		// search(false);
		// }
	}

	@Override
	public void onLoadMore(PullToRefreshLayout pullToRefreshLayout) {
		// TODO Auto-generated method stub
		if (NetUtils.isNetworkConnected(getApplicationContext())) {
			page =  page+1;
			search(false);
		}

	}

	private class WebService extends APIInterfaceInstance {

		@Override
		public void returnFindSearchSuccess(
				GetFindSearchResult getFindSearchResult) {
			// TODO Auto-generated method stub
			if (loadingDlg != null && loadingDlg.isShowing()) {
				loadingDlg.dismiss();
			}
			refreshLayout.refreshFinish(PullToRefreshLayout.SUCCEED);
			handNetData(getFindSearchResult);
			adapter.notifyDataSetChanged();

		}


		@Override
		public void returnFindSearchFail(String error,int errorCode) {
			// TODO Auto-generated method stub
			if (loadingDlg != null && loadingDlg.isShowing()) {
				loadingDlg.dismiss();
			}
			WebServiceMiddleUtils.hand(FindSearchMoreActivity.this, error,errorCode);
			refreshLayout.refreshFinish(PullToRefreshLayout.FAIL);
		}

	}
}
