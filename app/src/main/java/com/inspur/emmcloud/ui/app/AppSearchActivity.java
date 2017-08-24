package com.inspur.emmcloud.ui.app;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;

import com.inspur.emmcloud.BaseActivity;
import com.inspur.emmcloud.MyApplication;
import com.inspur.emmcloud.R;
import com.inspur.emmcloud.adapter.AppCenterAdapter;
import com.inspur.emmcloud.api.APIInterfaceInstance;
import com.inspur.emmcloud.api.apiservice.MyAppAPIService;
import com.inspur.emmcloud.bean.App;
import com.inspur.emmcloud.bean.GetSearchAppResult;
import com.inspur.emmcloud.util.InputMethodUtils;
import com.inspur.emmcloud.util.NetUtils;
import com.inspur.emmcloud.util.StringUtils;
import com.inspur.emmcloud.util.ToastUtils;
import com.inspur.emmcloud.util.WebServiceMiddleUtils;
import com.inspur.emmcloud.widget.ClearEditText;
import com.inspur.emmcloud.widget.LoadingDialog;
import com.inspur.emmcloud.widget.mylistview.MyListView;
import com.inspur.emmcloud.widget.mylistview.MyListView.IXListViewListener;

import java.util.List;

/**
 * 应用搜索界面
 * 
 * @author Administrator
 *
 */
public class AppSearchActivity extends BaseActivity implements IXListViewListener {



	private static final String ACTION_NAME = "add_app";
	private ClearEditText searchEdit;
	private List<App> searchList;
	private MyListView searchListView;
	private Handler handler;
	private LoadingDialog loadingDlg;
	private int pageNumber = 1;
	private GetSearchAppResult getSearchAppResult;
	private AppCenterAdapter searchAdapter;
	private MyAppAPIService apiService;
	private String searchContent;
	private BroadcastReceiver mBroadcastReceiver;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_app_search);
		initViews();


	}

	/**
	 * 初始化views
	 */
	private void initViews() {
		((MyApplication) getApplicationContext())
				.addActivity(AppSearchActivity.this);
		searchEdit = (ClearEditText) findViewById(R.id.search_edit);
		searchEdit.setOnEditorActionListener(onEditorActionListener);
		searchListView = (MyListView) findViewById(R.id.search_app_list);
		searchListView.setPullRefreshEnable(false);
		searchListView.setPullLoadEnable(false);
		loadingDlg = new LoadingDialog(AppSearchActivity.this);
		searchEdit.addTextChangedListener(new SearchWatcher());
		registerReceiver();
	}


	private void registerReceiver() {
		// TODO Auto-generated method stub
		mBroadcastReceiver = new BroadcastReceiver() {
			@Override
			public void onReceive(Context context, Intent intent) {
				String action = intent.getAction();
				if (action.equals(ACTION_NAME)) {
					App addApp = (App) intent.getExtras()
							.getSerializable("app");
					searchAdapter.addApp(addApp);
				}
			}
		};

		IntentFilter myIntentFilter = new IntentFilter();
		myIntentFilter.addAction(ACTION_NAME);
		// 注册广播
		registerReceiver(mBroadcastReceiver, myIntentFilter);
	}

	public void onBack(View v) {
		finish();
	}

	public void onSearch(View v) {
		searchContent = searchEdit.getText().toString();
		if (StringUtils.isBlank(searchContent)) {
			ToastUtils.show(getApplicationContext(),
					getString(R.string.input_search_key));
		}else if (NetUtils.isNetworkConnected(AppSearchActivity.this)) {
			pageNumber = 1;
			loadingDlg.show();
			apiService = new MyAppAPIService(AppSearchActivity.this);
			apiService.setAPIInterface(new WebService());
			searchApp();
		}
	}

	private OnEditorActionListener onEditorActionListener = new OnEditorActionListener() {

		@Override
		public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
			// TODO Auto-generated method stub
			if (actionId == EditorInfo.IME_ACTION_SEARCH) {
				InputMethodUtils.hide(AppSearchActivity.this);
				onSearch((Button) findViewById(R.id.search_btn));
				return true;
			}
			return false;
		}
	};

	private class WebService extends APIInterfaceInstance {

		@Override
		public void returnSearchAppSuccess(GetSearchAppResult getAllAppResult) {
			// TODO Auto-generated method stub
			if (loadingDlg != null && loadingDlg.isShowing()) {
				loadingDlg.dismiss();
			}
			getSearchAppResult = getAllAppResult;
			searchList = getSearchAppResult.getAllAppList();
			if (searchList.size() == 0) {
				ToastUtils.show(AppSearchActivity.this,
						getString(R.string.search_null));
			}
			searchAdapter = new AppCenterAdapter(
					AppSearchActivity.this, searchList);
			if (getSearchAppResult.getTotal() > searchList.size()) {
				searchListView.setPullLoadEnable(true);
			}
			searchListView.setAdapter(searchAdapter);
			searchListView.setXListViewListener(AppSearchActivity.this);
			searchListView
					.setOnItemClickListener(new OnItemClickListener() {

						@Override
						public void onItemClick(AdapterView<?> parent,
								View view, int position, long id) {
							// TODO Auto-generated method stub
							Intent intent = new Intent();
							intent.setClass(getApplicationContext(),
									AppDetailActivity.class);
							intent.putExtra("app",
									searchList.get(position - 1));
							startActivity(intent);
						}
					});

		}

		@Override
		public void returnSearchAppFail(String error,int errorCode) {
			// TODO Auto-generated method stub
			if (loadingDlg != null && loadingDlg.isShowing()) {
				loadingDlg.dismiss();
			}
			WebServiceMiddleUtils.hand(AppSearchActivity.this, error,errorCode);
		}

		@Override
		public void returnSearchAppMoreSuccess(GetSearchAppResult getAllAppResult) {
			// TODO Auto-generated method stub
				getSearchAppResult = getAllAppResult;
				searchList = getSearchAppResult.getAllAppList();
				if (searchList.size() == 0) {
					ToastUtils.show(AppSearchActivity.this,
							getString(R.string.search_null));
				}
				searchAdapter = new AppCenterAdapter(
						AppSearchActivity.this, searchList);
				if (getSearchAppResult.getTotal() > searchList.size()) {
					searchListView.setPullLoadEnable(true);
				}
				searchListView.setAdapter(searchAdapter);
				searchListView.setXListViewListener(AppSearchActivity.this);
				searchListView
						.setOnItemClickListener(new OnItemClickListener() {

							@Override
							public void onItemClick(AdapterView<?> parent,
									View view, int position, long id) {
								// TODO Auto-generated method stub
								Intent intent = new Intent();
								intent.setClass(getApplicationContext(),
										AppDetailActivity.class);
								intent.putExtra("app",
										searchList.get(position - 1));
								startActivity(intent);
							}
						});

		}

		@Override
		public void returnSearchAppMoreFail(String error,int errorCode) {
			// TODO Auto-generated method stub
			WebServiceMiddleUtils.hand(AppSearchActivity.this,
					error,errorCode);
			searchListView.stopLoadMore();
		}

	}

	@Override
	public void onRefresh(MyListView myListView) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onLoadMore(MyListView myListView) {
		// TODO Auto-generated method stub
		if (NetUtils.isNetworkConnected(getApplicationContext())) {
			searchApp();
		} else {
			myListView.stopRefresh();
			myListView.stopLoadMore();
		}
	}

	/**
	 * 搜索app
	 */
	private void searchApp() {
		apiService.searchApp(searchContent, pageNumber);
	}

	@Override
	public void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		if (handler != null) {
			handler = null;
		}
		if (mBroadcastReceiver != null) {
			unregisterReceiver(mBroadcastReceiver);
			mBroadcastReceiver = null;
		}
	}

	class SearchWatcher implements TextWatcher{

		@Override
		public void beforeTextChanged(CharSequence s, int start, int count, int after) {

		}

		@Override
		public void onTextChanged(CharSequence s, int start, int before, int count) {
			handleSearchApp(s);
		}

		@Override
		public void afterTextChanged(Editable s) {

		}
	}

	/**
	 * 处理本地文字变化之后的列表显示
	 * @param s
	 */
	private void handleSearchApp(CharSequence s) {
		if(StringUtils.isBlank(s.toString())){
			searchList.clear();
			searchAdapter.notifyDataSetChanged();
		}
		//本地搜索逻辑，目前先不用本地搜索
//		if(searchList  == null){
//			return;
//		}
//		List<App> localSearchList = new ArrayList<>();
//		for (int i = 0; i < searchList.size(); i++){
//			App app = searchList.get(i);
//			if(!StringUtils.isBlank(s.toString())&&app.getAppName().contains(s)){
//				localSearchList.add(app);
//			}
//		}
//		searchList.clear();
//		searchList.addAll(localSearchList);
//		searchAdapter.notifyDataSetChanged();
	}

}
