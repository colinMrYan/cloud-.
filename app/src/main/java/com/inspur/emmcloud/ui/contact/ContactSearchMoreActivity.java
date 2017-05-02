package com.inspur.emmcloud.ui.contact;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TextView;

import com.inspur.emmcloud.BaseActivity;
import com.inspur.emmcloud.MyApplication;
import com.inspur.emmcloud.R;
import com.inspur.emmcloud.bean.Channel;
import com.inspur.emmcloud.bean.ChannelGroup;
import com.inspur.emmcloud.bean.Contact;
import com.inspur.emmcloud.bean.FirstGroupTextModel;
import com.inspur.emmcloud.bean.SearchModel;
import com.inspur.emmcloud.config.MyAppConfig;
import com.inspur.emmcloud.ui.chat.ChannelActivity;
import com.inspur.emmcloud.ui.chat.ChannelInfoActivity;
import com.inspur.emmcloud.util.ChannelCacheUtils;
import com.inspur.emmcloud.util.ChannelGroupCacheUtils;
import com.inspur.emmcloud.util.CommonContactCacheUtils;
import com.inspur.emmcloud.util.ContactCacheUtils;
import com.inspur.emmcloud.util.DensityUtil;
import com.inspur.emmcloud.util.EditTextUtils;
import com.inspur.emmcloud.util.ImageDisplayUtils;
import com.inspur.emmcloud.util.LogUtils;
import com.inspur.emmcloud.util.StringUtils;
import com.inspur.emmcloud.util.ToastUtils;
import com.inspur.emmcloud.util.UriUtils;
import com.inspur.emmcloud.widget.CircleImageView;
import com.inspur.emmcloud.widget.FlowLayout;
import com.inspur.emmcloud.widget.LoadingDialog;
import com.inspur.emmcloud.widget.MaxHightScrollView;
import com.inspur.emmcloud.widget.pullableview.PullToRefreshLayout;
import com.inspur.emmcloud.widget.pullableview.PullToRefreshLayout.OnRefreshListener;
import com.inspur.emmcloud.widget.pullableview.PullableListView;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class ContactSearchMoreActivity extends BaseActivity implements OnRefreshListener{
	private static final int SEARCH_ALL = 0;
	private static final int SEARCH_CONTACT = 2;
	private static final int SEARCH_CHANNELGROUP = 1;
	private static final int SEARCH_RECENT = 3;
	private static final int SEARCH_NOTHIING = 4;
	private List<ChannelGroup> searchChannelGroupList = new ArrayList<ChannelGroup>(); // 群组搜索结果
	private List<Contact> searchContactList = new ArrayList<Contact>(); // 通讯录搜索结果
	private List<Channel> searchRecentList = new ArrayList<Channel>();// 常用联系人搜索结果
	private List<SearchModel> selectMemList = new ArrayList<SearchModel>();
	private List<FirstGroupTextModel> groupTextList = new ArrayList<FirstGroupTextModel>();
	private int searchArea;
	private int searchContent;
	private boolean isMultiSelect = false;
	private String searchText;

	private FlowLayout flowLayout;
	private EditText searchEdit;
	private MyTextWatcher myTextWatcher;
	private MaxHightScrollView searchEditLayout;
	private PullableListView searchListView;
	private PullToRefreshLayout pullToRefreshLayout;
	private Adapter adapter;
	private RecyclerView groupTitleListView;
	private GroupTitleAdapter groupTitleAdapter;
	private LoadingDialog loadingDlg;
	private Handler handler;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_contact_search_more);
		((MyApplication) getApplicationContext()).addActivity(this);
		initView();
		getIntentData();
	}

	private void initView() {
		// TODO Auto-generated method stub
		loadingDlg = new LoadingDialog(getApplicationContext());
		flowLayout = (FlowLayout) findViewById(R.id.flowlayout);
		myTextWatcher = new MyTextWatcher();
		flowAddEdit();
		searchEditLayout = (MaxHightScrollView) findViewById(R.id.search_edit_layout);
		pullToRefreshLayout = (PullToRefreshLayout) findViewById(R.id.refresh_view);
		pullToRefreshLayout.setOnRefreshListener(this);
		searchListView = (PullableListView) findViewById(R.id.search_list);
		searchListView.setCanPullDown(false);
		adapter = new Adapter();
		searchListView.setAdapter(adapter);
		searchListView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				// TODO Auto-generated method stub
				SearchModel searchModel = null;
				if (searchArea == SEARCH_CHANNELGROUP) {
					searchModel = new SearchModel(searchChannelGroupList
							.get(position));
				} else if (searchArea == SEARCH_CONTACT) {
					searchModel = new SearchModel(searchContactList
							.get(position));
				} else {
					searchModel = new SearchModel(searchRecentList
							.get(position));
				}
				changeMembers(searchModel);
			}
		});
		groupTitleListView = (RecyclerView) findViewById(R.id.title_list);
		LinearLayoutManager layoutManager = new LinearLayoutManager(this);
		layoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
		groupTitleListView.setLayoutManager(layoutManager);
		groupTitleAdapter = new GroupTitleAdapter();
		groupTitleListView.setAdapter(groupTitleAdapter);
	}

	private void flowAddEdit() {
		if (searchEdit == null) {
			searchEdit = new EditText(this);
			FlowLayout.LayoutParams params = new FlowLayout.LayoutParams(
					LayoutParams.WRAP_CONTENT, DensityUtil.dip2px(
							getApplicationContext(), 45));
			int paddingRight = DensityUtil.dip2px(getApplicationContext(), 80);
			searchEdit.setPadding(0, 0, paddingRight, 0);
			searchEdit.setLayoutParams(params);
			searchEdit.setSingleLine(true);
			searchEdit.setHint(getString(R.string.seach_blank));
			searchEdit.setGravity(Gravity.CENTER_VERTICAL);
			searchEdit.setTextSize(TypedValue.COMPLEX_UNIT_SP, 17);
			// searchEdit.setTextSize(getResources().getDimension(R.dimen.content_title_textsize));
			searchEdit.setBackgroundDrawable(null);
			searchEdit.addTextChangedListener(myTextWatcher);
		}
		flowLayout.addView(searchEdit);
	}

	private void getIntentData() {
		// TODO Auto-generated method stub
		isMultiSelect = getIntent().getBooleanExtra("isMultiSelect", false);
		selectMemList = (List<SearchModel>) getIntent().getSerializableExtra(
				"selectMemList");
		searchContent = getIntent().getIntExtra("searchContent", 1);
		// 单选时隐藏输入框或者不选时
		if (!isMultiSelect || searchContent == SEARCH_NOTHIING) {
			((TextView) findViewById(R.id.ok_text)).setVisibility(View.GONE);
		}
		searchText = getIntent().getStringExtra("searchText");
		int groupPosition = getIntent().getIntExtra("groupPosition", 1);
		groupTextList = (List<FirstGroupTextModel>) getIntent()
				.getSerializableExtra("groupTextList");
		groupTitleAdapter.notifyDataSetChanged();

		switch (groupPosition) {
		case 1:
			searchArea = SEARCH_RECENT;
			break;
		case 2:
			searchArea = SEARCH_CHANNELGROUP;
			break;

		case 3:
			searchArea = SEARCH_CONTACT;
			break;

		default:
			break;
		}
		searchListView.setCanPullUp(false);
		notifyFlowLayoutDataChange(searchText);
	}

	/**
	 * 刷新FlowLayout
	 */
	private void notifyFlowLayoutDataChange(String content) {
		// searchEdit.removeTextChangedListener(myTextWatcher);
			EditTextUtils.setText(searchEdit, content);
		
		// searchEdit.addTextChangedListener(myTextWatcher);
		flowLayout.removeAllViews();
		for (int i = 0; i < selectMemList.size(); i++) {
			final SearchModel searchModel = selectMemList.get(i);
			TextView searchResultText = new TextView(this);
			FlowLayout.LayoutParams params = new FlowLayout.LayoutParams(
					LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
			params.rightMargin = DensityUtil.dip2px(getApplicationContext(), 5);
			params.topMargin = DensityUtil.dip2px(getApplicationContext(), 11);
			searchResultText.setLayoutParams(params);
			int piddingTop = DensityUtil.dip2px(getApplicationContext(), 2);
			searchResultText.setPadding(0, piddingTop, 0, piddingTop);
			searchResultText.setGravity(Gravity.CENTER_VERTICAL);
			searchResultText.setBackgroundDrawable(getResources().getDrawable(
					R.drawable.bg_select_mem));
			searchResultText.setTextSize(TypedValue.COMPLEX_UNIT_SP, 15);
			searchResultText.setTextColor(getResources()
					.getColor(R.color.white));
			searchResultText.setText(selectMemList.get(i).getName());
			searchResultText.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					// TODO Auto-generated method stub
					changeMembers(searchModel);
				}
			});
			int paddingLeft = DensityUtil.dip2px(getApplicationContext(), 5);
			int paddingTop = DensityUtil.dip2px(getApplicationContext(), 1);
			searchResultText.setPadding(paddingLeft, paddingTop, paddingLeft,
					paddingTop);
			flowLayout.addView(searchResultText);
		}
		flowAddEdit();
		searchEditLayout.post(new Runnable() {
			public void run() {
				searchEditLayout.fullScroll(ScrollView.FOCUS_DOWN);
			}
		});
		adapter.notifyDataSetChanged();
	}

	/**
	 * 添加联系人
	 */
	private void changeMembers(SearchModel searchModel) {
		if (searchModel != null) {
			if (searchContent == SEARCH_NOTHIING) {
				CommonContactCacheUtils.saveCommonContact(
						getApplicationContext(), searchModel);
				checkInfoOrEnterChannel(searchModel);
				return;
			}
			if (!selectMemList.contains(searchModel)) {
				selectMemList.add(searchModel);
				CommonContactCacheUtils.saveCommonContact(
						getApplicationContext(), searchModel);
			} else {
				selectMemList.remove(searchModel);
				notifyFlowLayoutDataChange("");
				return;
			}
			returnSelectData();
		}
	}
	
	
	/**
	 * 查看信息
	 * 
	 * @param searchModel
	 */
	private void checkInfoOrEnterChannel(final SearchModel searchModel) {
		// TODO Auto-generated method stub
		// TODO Auto-generated method stub
		Intent intent = new Intent();
		String id = searchModel.getId();
		String type = searchModel.getType();
		if (id.equals("null")) {
			ToastUtils.show(getApplicationContext(), R.string.cannot_view_info);
			return;
		}
		CommonContactCacheUtils.saveCommonContact(getApplicationContext(),
				searchModel);
		if (type.equals("USER")) {
			intent.putExtra("uid", id);
			intent.setClass(getApplicationContext(), UserInfoActivity.class);
			startActivity(intent);
		} else {
			intent.setClass(getApplicationContext(), ChannelActivity.class);
			intent.putExtra("title", searchModel.getName());
			intent.putExtra("channelId", searchModel.getId());
			intent.putExtra("channelType", searchModel.getType());
			startActivity(intent);
		}

	}
	
	public void onClick(View v){
		switch (v.getId()) {
		case R.id.cancel_text:
			finish();
			break;
		case R.id.ok_text:
			returnSelectData();
			break;
		default:
			break;
		}
		
	}

	private void returnSelectData() {
		// TODO Auto-generated method stub
		Intent intent = new Intent();
		intent.putExtra("selectMemList", (Serializable) selectMemList);
		setResult(RESULT_OK, intent);
		finish();
	}

	private class MyTextWatcher implements TextWatcher {

		@Override
		public void beforeTextChanged(CharSequence s, int start, int count,
				int after) {
			// TODO Auto-generated method stub

		}

		@Override
		public void onTextChanged(CharSequence s, int start, int before,
				int count) {
			// TODO Auto-generated method stub
			LogUtils.jasonDebug("onTextChanged==="+s);
			searchText = searchEdit.getText().toString().trim();
			searchListView.setCanPullUp(false);
			pullToRefreshLayout.setVisibility(View.VISIBLE);
			if (!StringUtils.isBlank(searchText)) {
				switch (searchArea) {
				case SEARCH_RECENT:
					searchRecentList = ChannelCacheUtils.getSearchChannelList(
							getApplicationContext(), searchText, searchContent);
					if (searchRecentList.size() == 0) {
						pullToRefreshLayout.setVisibility(View.GONE);
					}

					break;
				case SEARCH_CHANNELGROUP:
					searchChannelGroupList = ChannelGroupCacheUtils
							.getSearchChannelGroupList(getApplicationContext(),
									searchText);
					if (searchChannelGroupList.size() == 0) {
						pullToRefreshLayout.setVisibility(View.GONE);
					}
					break;

				case SEARCH_CONTACT:
					String currentContactId = groupTextList.get(
							groupTextList.size() - 1).getId();
//					if (StringUtils.isBlank(currentContactId)) {
//						Contact rootContact = ContactCacheUtils
//								.getRootContact(ContactSearchMoreActivity.this);
//						currentContactId = rootContact.getId();
//					}
					LogUtils.jasonDebug("secondcurrentContactId="+currentContactId);
					searchContactList = ContactCacheUtils.getSearchContact(
							getApplicationContext(), searchText,
							currentContactId, 0, 25);
					LogUtils.jasonDebug("size="+searchContactList.size() );
					if (searchContactList.size() == 0) {
						pullToRefreshLayout.setVisibility(View.GONE);
					}
					if (searchContactList.size() == 25) {
						searchListView.setCanPullUp(true);
					}
					break;

				default:
					break;
				}
			} else {
				returnSelectData();
			}
			adapter.notifyDataSetChanged();
		}

		@Override
		public void afterTextChanged(Editable s) {
			// TODO Auto-generated method stub

		}

	}

	private class Adapter extends BaseAdapter {

		@Override
		public int getCount() {
			// TODO Auto-generated method stub
			if (searchArea == SEARCH_RECENT) {
				return searchRecentList.size();
			} else if (searchArea == SEARCH_CHANNELGROUP) {
				return searchChannelGroupList.size();
			} else {
				return searchContactList.size();
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
			ViewHolder viewHolder = null;
			if (convertView == null) {
				viewHolder = new ViewHolder();
				LayoutInflater mInflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
				convertView = mInflater.inflate(
						R.layout.member_search_item_view, null);
				viewHolder.nameText = (TextView) convertView
						.findViewById(R.id.name_text);
				viewHolder.photoImg = (CircleImageView) convertView
						.findViewById(R.id.photo_img);
				viewHolder.photoImg.setVisibility(View.VISIBLE);
				viewHolder.selectedImg = (ImageView) convertView
						.findViewById(R.id.selected_img);
				convertView.setTag(viewHolder);
			} else {
				viewHolder = (ViewHolder) convertView.getTag();
			}
			convertView.setBackgroundColor(Color.parseColor("#F4F4F4"));
			SearchModel searchModel = null;
			if (searchArea == SEARCH_RECENT) {
				Channel channel = searchRecentList.get(position);
				searchModel = new SearchModel(channel);
			} else if (searchArea == SEARCH_CHANNELGROUP) {
				ChannelGroup channelGroup = searchChannelGroupList
						.get(position);
				searchModel = new SearchModel(channelGroup);

			} else {
				Contact contact = searchContactList.get(position);
				searchModel = new SearchModel(contact);

			}
			displayImg(searchModel, viewHolder.photoImg);
			checkInfo(viewHolder.photoImg, searchModel);
			viewHolder.nameText.setText(searchModel.getCompleteName(getApplicationContext()));
			if (selectMemList.contains(searchModel)) {
				viewHolder.selectedImg.setVisibility(View.VISIBLE);
				viewHolder.nameText.setTextColor(Color.parseColor("#0f7bca"));
			} else {
				viewHolder.selectedImg.setVisibility(View.INVISIBLE);
				viewHolder.nameText.setTextColor(Color.parseColor("#030303"));
			}
			return convertView;
		}

	}

	/**
	 * 统一显示图片
	 * 
	 * @param searchModel
	 * @param photoImg
	 */
	private void displayImg(SearchModel searchModel, CircleImageView photoImg) {
		String icon = searchModel.getIcon();
		String type = searchModel.getType();
		if (type.equals("STRUCT")) {
			photoImg.setImageResource(R.drawable.icon_channel_group_default);
			return;
		}
		int defaultIcon = -1;
		if (type.equals("GROUP")) {
			File file = new File(MyAppConfig.LOCAL_CACHE_PATH, UriUtils.tanent+searchModel.getId() + "_100.png");
			if (file.exists()) {
				icon = "file://" + file.getAbsolutePath();
			}
			defaultIcon = R.drawable.icon_channel_group_default;
		} else {
			defaultIcon = R.drawable.icon_person_default;
			if (searchModel.getId().equals("null")) {
				photoImg.setImageResource(defaultIcon);
				return;
			}
		}
		new ImageDisplayUtils(getApplicationContext(), defaultIcon).display(
				photoImg, icon);
		// TODO Auto-generated method stub
	}

	/**
	 * 查看信息
	 * 
	 * @param searchModel
	 */
	private void checkInfo(CircleImageView photoImg,
			final SearchModel searchModel) {
		// TODO Auto-generated method stub
		photoImg.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				Intent intent = new Intent();
				String id = searchModel.getId();
				String type = searchModel.getType();
				if (type.equals("STRUCT")) {
					return;
				}
				if (id.equals("null")) {
					ToastUtils.show(getApplicationContext(), getString(R.string.cannot_view_info));
					return;
				}
				CommonContactCacheUtils.saveCommonContact(
						getApplicationContext(), searchModel);
				if (type.equals("USER")) {
					intent.putExtra("uid", id);
					intent.setClass(getApplicationContext(),
							UserInfoActivity.class);
					startActivity(intent);
				} else if (type.equals("DIRECT")) {
					intent.putExtra("cid", id);
					intent.setClass(getApplicationContext(),
							UserInfoActivity.class);
					startActivity(intent);
				} else if (type.equals("GROUP")) {
					intent.putExtra("cid", id);
					intent.setClass(getApplicationContext(),
							ChannelInfoActivity.class);
					startActivity(intent);
				}

			}
		});

	}

	public static class ViewHolder {
		TextView nameText;
		CircleImageView photoImg;
		ImageView rightArrowImg;
		ImageView selectedImg;
	}

	/**
	 * 第一个group title中list的adapter
	 * 
	 * @author Administrator
	 *
	 */
	public class GroupTitleAdapter extends RecyclerView.Adapter<MyViewHolder> {

		@Override
		public int getItemCount() {
			// TODO Auto-generated method stub
			return groupTextList.size();

		}

		@Override
		public void onBindViewHolder(MyViewHolder arg0, int arg1) {
			// TODO Auto-generated method stub
			int count = getItemCount();
			arg0.titleText.setText(groupTextList.get(arg1).getName());

			if (count > 1) {
				arg0.titleText.setBackgroundColor(Color.parseColor("#d8d8d8"));
				if (arg1 != count - 1) {
					arg0.titleImg
							.setImageResource(R.drawable.icon_group_title_mid_img);
					arg0.titleText.setTextColor(Color.parseColor("#EFEFF4"));
				} else {
					arg0.titleImg
							.setImageResource(R.drawable.icon_group_title_end_img);
					arg0.titleText.setTextColor(Color.parseColor("#ffffff"));
				}
			} else {
				arg0.titleText
						.setBackgroundColor(Color.parseColor("#00000000"));
				arg0.titleText.setTextColor(Color.parseColor("#8f8e94"));
				arg0.titleImg.setImageDrawable(null);
			}
		}

		@Override
		public MyViewHolder onCreateViewHolder(ViewGroup arg0, int arg1) {
			// TODO Auto-generated method stub
			LayoutInflater mInflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
			View view = mInflater.inflate(R.layout.contact_header_item_view,
					arg0, false);
			// view.setBackgroundColor(Color.RED);
			MyViewHolder viewHolder = new MyViewHolder(view);
			return viewHolder;
		}
	}

	public static class MyViewHolder extends RecyclerView.ViewHolder implements
			OnClickListener {
		TextView titleText;
		ImageView titleImg;

		public MyViewHolder(View view) {
			super(view);
			titleText = (TextView) view.findViewById(R.id.title_text);
			titleImg = (ImageView) view.findViewById(R.id.title_img);

		}

		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub

		}

	}

	@Override
	public void onRefresh(PullToRefreshLayout pullToRefreshLayout) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onLoadMore(PullToRefreshLayout pullToRefreshLayout) {
		// TODO Auto-generated method stub
		String currentContactId = groupTextList.get(
				groupTextList.size() - 1).getId();
		if (StringUtils.isBlank(currentContactId)) {
			Contact rootContact = ContactCacheUtils
					.getRootContact(ContactSearchMoreActivity.this);
			currentContactId = rootContact.getId();
		}
		List<Contact> moreContactList = ContactCacheUtils.getSearchContact(
				getApplicationContext(), searchText,
				currentContactId, searchContactList.size(), 25);
		pullToRefreshLayout.loadmoreFinish(PullToRefreshLayout.SUCCEED);
		if (moreContactList.size() == 25) {
			searchListView.setCanPullUp(true);
		}else {
			searchListView.setCanPullUp(false);
		}
		if (moreContactList.size() != 0) {
			searchContactList.addAll(searchContactList.size(), moreContactList);;
			adapter.notifyDataSetChanged();
		}
		
	}

}
