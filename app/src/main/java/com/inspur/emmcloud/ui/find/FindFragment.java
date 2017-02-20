package com.inspur.emmcloud.ui.find;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import android.app.Activity;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;
import com.facebook.react.BuildConfig;
import com.facebook.react.ReactActivityDelegate;
import com.facebook.react.ReactInstanceManager;
import com.facebook.react.ReactRootView;
import com.facebook.react.common.LifecycleState;
import com.facebook.react.modules.core.DefaultHardwareBackBtnHandler;
import com.facebook.react.modules.core.PermissionAwareActivity;
import com.facebook.react.modules.core.PermissionListener;
import com.facebook.react.shell.MainReactPackage;
import com.inspur.emmcloud.MyApplication;
import com.inspur.emmcloud.R;
import com.inspur.emmcloud.api.APIInterfaceInstance;
import com.inspur.emmcloud.api.apiservice.FindAPIService;
import com.inspur.emmcloud.bean.GetTripResult;
import com.inspur.emmcloud.bean.Report;
import com.inspur.emmcloud.bean.TicketMsg;
import com.inspur.emmcloud.bean.Trip;
import com.inspur.emmcloud.bean.UploadTicketInfo;
import com.inspur.emmcloud.ui.IndexActivity;
import com.inspur.emmcloud.ui.app.groupnews.GroupNewsActivity;
import com.inspur.emmcloud.ui.find.trip.TripInfoActivity;
import com.inspur.emmcloud.util.IntentUtils;
import com.inspur.emmcloud.util.LogUtils;
import com.inspur.emmcloud.util.NetUtils;
import com.inspur.emmcloud.util.PreferencesUtils;
import com.inspur.emmcloud.util.SmsTicketUtils;
import com.inspur.emmcloud.util.TimeUtils;
import com.inspur.emmcloud.util.ToastUtils;
import com.inspur.emmcloud.util.UriUtils;
import com.inspur.emmcloud.util.WebServiceMiddleUtils;
import com.inspur.emmcloud.widget.LoadingDialog;
import com.inspur.emmcloud.widget.dialogs.MyDialog;
import com.inspur.emmcloud.widget.pullableview.PullToRefreshLayout;
import com.inspur.emmcloud.widget.pullableview.PullToRefreshLayout.OnRefreshListener;
import com.inspur.emmcloud.widget.pullableview.PullableListView;
import com.inspur.reactnative.AuthorizationManagerPackage;
import com.reactnativecomponent.swiperefreshlayout.RCTSwipeRefreshLayoutPackage;
import com.thin.downloadmanager.util.Log;


/**
 * com.inspur.emmcloud.ui.FindFragment create at 2016年8月29日 下午3:27:26
 */
public class FindFragment extends Fragment implements DefaultHardwareBackBtnHandler{

	private static final int QRCODE_REQUEST = 2;
	private View rootView;
	private FindAPIService apiService;
//	private FindAdapter adapter;
	private PullableListView findListView;
	private List<Trip> tripList = new ArrayList<Trip>();
	private LoadingDialog loadingDlg;
	private Report report = new Report();
	private PullToRefreshLayout pullToRefreshLayout;
	private BroadcastReceiver refreshReceiver;
	private int deletePosition;


//	@Override
//	public View onCreateView(LayoutInflater inflater, ViewGroup container,
//			Bundle savedInstanceState) {
//		if (rootView == null) {
//			rootView = inflater.inflate(R.layout.fragment_find, container,
//					false);
//		}
//
//		ViewGroup parent = (ViewGroup) rootView.getParent();
//		if (parent != null) {
//			parent.removeView(rootView);
//		}
//		return rootView;
//	}


	private ReactRootView mReactRootView;
	private ReactInstanceManager mReactInstanceManager;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
							 Bundle savedInstanceState) {

		LogUtils.YfcDebug("是否已经add:"+isAdded());
		if(mReactRootView == null ){
			mReactRootView = new ReactRootView(getActivity());
			LogUtils.YfcDebug("传递的Context类型是否是indexActivity："+(getActivity() instanceof  IndexActivity));
			mReactInstanceManager = ReactInstanceManager.builder()
					.setApplication(getActivity().getApplication())
					.setCurrentActivity(getActivity())
					.setBundleAssetName("index.android.bundle")
					.setJSMainModuleName("index.android")
					.addPackage(new MainReactPackage())
					.addPackage(new RCTSwipeRefreshLayoutPackage())
					.addPackage(new AuthorizationManagerPackage())
					.setUseDeveloperSupport(BuildConfig.DEBUG)
					.setInitialLifecycleState(LifecycleState.RESUMED)
					.build();
			mReactRootView.startReactApplication(mReactInstanceManager, "discover", null);
		}
		return mReactRootView;
	}




	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
//		LayoutInflater inflater = (LayoutInflater) getActivity()
//				.getSystemService(getActivity().LAYOUT_INFLATER_SERVICE);
//		rootView = inflater.inflate(R.layout.fragment_find, null);
//		initView();
//		getTripList();
//		getLastUploadTicketInfo();
//		setReportContent();

		LogUtils.YfcDebug("");

	}



//	protected void onPostExecute(Void result){
//		if(isAdded()){
//			getResources().getString(R.string.app_name);
//		}
//	}




	@Override
	public void invokeDefaultOnBackPressed() {
		getActivity().onBackPressed();
	}



//	@Override
//	public void onPause() {
//		super.onPause();
//		pullToRefreshLayout.refreshFinish(PullToRefreshLayout.FAIL);
//	}
//
//	private void initView() {
//		// TODO Auto-generated method stub
//		apiService = new FindAPIService(getActivity());
//		apiService.setAPIInterface(new WebSevice());
//		findListView = (PullableListView) rootView.findViewById(R.id.task_list);
//		loadingDlg = new LoadingDialog(getActivity());
//		pullToRefreshLayout = (PullToRefreshLayout) rootView
//				.findViewById(R.id.refresh_view);
//		pullToRefreshLayout.setOnRefreshListener(this);
//
//		((LinearLayout) rootView.findViewById(R.id.find_scan_layout))
//				.setOnClickListener(new OnClick());
//		((LinearLayout) rootView.findViewById(R.id.find_news))
//				.setOnClickListener(new OnClick());
//		((LinearLayout) rootView.findViewById(R.id.find_analysis))
//				.setOnClickListener(new OnClick());
//		((LinearLayout) rootView.findViewById(R.id.find_knowage))
//				.setOnClickListener(new OnClick());
//		((LinearLayout) rootView.findViewById(R.id.find_doc))
//				.setOnClickListener(new OnClick());
//		((RelativeLayout) rootView.findViewById(R.id.search_layout))
//				.setOnClickListener(new OnClick());
//
//		if (((MyApplication) getActivity().getApplicationContext())
//				.isVersionDev()) {
//			((RelativeLayout) rootView.findViewById(R.id.function_layout))
//					.setVisibility(View.VISIBLE);
//		} else {
//			((RelativeLayout) rootView.findViewById(R.id.header_layout))
//					.setVisibility(View.VISIBLE);
//		}
//		registerRefreshReceiver();
//	}
//
//	/**
//	 * 接收刷新行程列表的广播
//	 */
//	private void registerRefreshReceiver() {
//		// TODO Auto-generated method stub
//		refreshReceiver = new BroadcastReceiver() {
//
//			@Override
//			public void onReceive(Context context, Intent intent) {
//				// TODO Auto-generated method stub
//				getTripList();
//			}
//
//		};
//		IntentFilter myIntentFilter = new IntentFilter();
//		myIntentFilter.addAction("refresh_trip_list");
//		// 注册广播
//		getActivity().registerReceiver(refreshReceiver, myIntentFilter);
//
//	}
//
//	/**
//	 * 获取行程列表
//	 */
//	private void getTripList() {
//		// TODO Auto-generated method stub
//		if (NetUtils.isNetworkConnected(getActivity())) {
//			loadingDlg.show();
//			apiService.getTripInfo();
//		}
//	}
//
//	/**
//	 * 获取最后一次上传的行程的数据
//	 */
//	private void getLastUploadTicketInfo() {
//		// TODO Auto-generated method stub
//		if (NetUtils.isNetworkConnected(getActivity())) {
//			apiService.getLastUploadTrip();
//		}
//	}
//
//	/**
//	 * 设置report的内容
//	 */
//	private void setReportContent() {
//		report.setTilte(getString(R.string.found_q2_q2));
//		report.setTime("2016-4-30");
//	};
//
//	/**
//	 * 上传火车票数据
//	 */
//	private void uploadTripTicketInfo(Date lastUploadDate) {
//		// TODO Auto-generated method stub
//		List<TicketMsg> ticketMsgList = SmsTicketUtils.getTicketMsg(
//				getActivity(), lastUploadDate);
//		if (ticketMsgList.size() > 0) {
//			List<UploadTicketInfo> uploadTicketInfoList = new ArrayList<UploadTicketInfo>();
//			String userName = PreferencesUtils.getString(getActivity(),
//					"userRealName", "");
//			for (int i = 0; i < ticketMsgList.size(); i++) {
//				UploadTicketInfo uploadTicketInfo = new UploadTicketInfo(
//						getActivity(), ticketMsgList.get(i), userName);
//				uploadTicketInfoList.add(uploadTicketInfo);
//			}
//			String uploadTicketInfos = JSON.toJSONString(uploadTicketInfoList);
//			apiService.uploadTrainTicket(uploadTicketInfos);
//		}
//
//	}
//
//	/**
//	 * 删除行程
//	 *
//	 * @param position
//	 */
//	private void showDeleteDlg(final int position) {
//		// TODO Auto-generated method stub
//		final MyDialog oprationDlg = new MyDialog(getActivity(),
//				R.layout.dialog_channel_operation, R.style.userhead_dialog_bg);
//		oprationDlg.show();
//		TextView deleteText = (TextView) oprationDlg
//				.findViewById(R.id.set_top_text);
//		TextView cancelText = (TextView) oprationDlg
//				.findViewById(R.id.hide_text);
//		deleteText.setText(getString(R.string.delete));
//		cancelText.setText(getString(R.string.cancel));
//		deleteText.setOnClickListener(new OnClickListener() {
//
//			@Override
//			public void onClick(View v) {
//				// TODO Auto-generated method stub
//				oprationDlg.dismiss();
//				String tripId = tripList.get(position - 1).getTid();
//				deletePosition = position - 1;
//				deleteTrip(tripId);
//			}
//
//		});
//		cancelText.setOnClickListener(new OnClickListener() {
//
//			@Override
//			public void onClick(View v) {
//				// TODO Auto-generated method stub
//				oprationDlg.dismiss();
//			}
//		});
//	}
//
//	private void deleteTrip(String tripId) {
//		if (NetUtils.isNetworkConnected(getActivity())) {
//			loadingDlg.show();
//			apiService.deleteTripByIds(tripId);
//		}
//	}
//
//	private class OnClick implements OnClickListener {
//
//		@Override
//		public void onClick(View v) {
//			// TODO Auto-generated method stub
//			Intent intent = new Intent();
//			switch (v.getId()) {
//			case R.id.find_scan_layout:
//
//				//为调试代码屏蔽
////				intent.setClass(getActivity(), CaptureActivity.class);
////				intent.putExtra("from", "FindFragment");
////				startActivityForResult(intent, QRCODE_REQUEST);
//				break;
//			case R.id.find_news:
//				intent.setClass(getActivity(), GroupNewsActivity.class);
//				startActivity(intent);
//				break;
//			case R.id.find_analysis:
//				intent.setClass(getActivity(), AnalysisActivity.class);
//				startActivity(intent);
//				break;
//			case R.id.find_knowage:
//				intent.setClass(getActivity(), KnowledgeActivity.class);
//				startActivity(intent);
//				break;
//			case R.id.find_doc:
//				intent.setClass(getActivity(), DocumentActivity.class);
//				startActivity(intent);
//				break;
//
//			case R.id.search_layout:
//				IntentUtils.startActivity(getActivity(), FindSearchActivity.class);
//				break;
//
//			default:
//				break;
//			}
//
//		}
//
//	}
//
//	@Override
//	public void onRefresh(PullToRefreshLayout pullToRefreshLayout) {
//		if (NetUtils.isNetworkConnected(getActivity())) {
//			apiService.getTripInfo();
//		} else {
//			pullToRefreshLayout.refreshFinish(PullToRefreshLayout.FAIL);
//		}
//	}
//
//	@Override
//	public void onLoadMore(PullToRefreshLayout pullToRefreshLayout) {
//	}
//
//	private class FindAdapter extends BaseAdapter {
//
//		@Override
//		public int getCount() {
//			// TODO Auto-generated method stub
//			return tripList.size() + 1;
//		}
//
//		@Override
//		public Object getItem(int position) {
//			// TODO Auto-generated method stub
//			return null;
//		}
//
//		@Override
//		public long getItemId(int position) {
//			// TODO Auto-generated method stub
//			return 0;
//		}
//
//		@Override
//		public View getView(int position, View convertView, ViewGroup parent) {
//			// TODO Auto-generated method stub
//			LayoutInflater mInflater = (LayoutInflater) getActivity()
//					.getSystemService(getActivity().LAYOUT_INFLATER_SERVICE);
//			if (position != 0) {
//				convertView = mInflater
//						.inflate(R.layout.find_travel_item, null);
//				Trip trip = tripList.get(position - 1);
//				((TextView) convertView.findViewById(R.id.find_travel_start))
//						.setText(trip.getFrom());
//				String tripEnd = trip.getDestination();
//				if (TextUtils.isEmpty(tripEnd)) {
//					tripEnd = "?";
//				}
//				((TextView) convertView.findViewById(R.id.find_travel_end))
//						.setText(tripEnd);
//				String startTime = TimeUtils.calendar2FormatString(
//						getActivity(), trip.getStart(),
//						TimeUtils.FORMAT_HOUR_MINUTE);
//				((TextView) convertView.findViewById(R.id.find_travel_time))
//						.setText(startTime);
//				((TextView) convertView.findViewById(R.id.find_travel_num))
//						.setText(trip.getNumber());
//				String tripUserName = trip.getSourceuname();
//				if (TextUtils.isEmpty(tripUserName)) {
//					tripUserName = "?";
//				}
//				((TextView) convertView
//						.findViewById(R.id.find_travel_user_name))
//						.setText(tripUserName);
//			} else {
//				convertView = mInflater
//						.inflate(R.layout.find_report_item, null);
//				((TextView) convertView.findViewById(R.id.find_report_title))
//						.setText(report.getTilte());
//				((TextView) convertView
//						.findViewById(R.id.find_report_time_text))
//						.setText(report.getTime());
//
//			}
//
//			return convertView;
//		}
//
//	}
//
//	@Override
//	public void onActivityResult(int requestCode, int resultCode, Intent data) {
//		// TODO Auto-generated method stub
//		if (resultCode == getActivity().RESULT_OK
//				&& requestCode == QRCODE_REQUEST) {
//			if (data == null) {
//				return;
//			}
//			Boolean isDecodeSuccess = data.getExtras().getBoolean(
//					"isDecodeSuccess");
//			String result = data.getExtras().getString("msg");
//			if (isDecodeSuccess) {
//				handQrResult(result);
//			} else {
//				ToastUtils.show(getActivity(), R.string.can_not_recognize);
//			}
//		}
//	}
//
//	/**
//	 * 处理二维码扫描的结果
//	 *
//	 * @param result
//	 */
//	private void handQrResult(String result) {
//		// TODO Auto-generated method stub
//		if (result.startsWith("http")) {
//			UriUtils.openUrl(getActivity(), result, getString(R.string.sweep));
//		} else {
//			Bundle bundle = new Bundle();
//			bundle.putString("result", result);
//			IntentUtils.startActivity(getActivity(), ScanResultActivity.class,
//					bundle);
//		}
//	}
//
//	private class WebSevice extends APIInterfaceInstance {
//
//		@Override
//		public void returnLastUploadTripSuccess(Trip trip) {
//			// TODO Auto-generated method stub
//			uploadTripTicketInfo(trip.getSendDate());
//		}
//
//		@Override
//		public void returnLastUploadTripFail(String error) {
//			// TODO Auto-generated method stub
//			WebServiceMiddleUtils.hand(getActivity(), error);
//			uploadTripTicketInfo(null);
//		}
//
//		@Override
//		public void returnTripSuccess(GetTripResult getTripResult) {
//			// TODO Auto-generated method stub
//			if (loadingDlg != null && loadingDlg.isShowing()) {
//				loadingDlg.dismiss();
//			}
//			pullToRefreshLayout.refreshFinish(PullToRefreshLayout.SUCCEED);
//			tripList = getTripResult.getTripList();
//			adapter = new FindAdapter();
//			findListView.setAdapter(adapter);
//			findListView.setOnItemClickListener(new OnItemClickListener() {
//
//				@Override
//				public void onItemClick(AdapterView<?> parent, View view,
//						int position, long id) {
//					// TODO Auto-generated method stub
//					if (position != 0) {
//						Intent intent = new Intent(getActivity(),
//								TripInfoActivity.class);
//						intent.putExtra("tripInfo", tripList.get(position - 1));
//						getActivity().startActivity(intent);
//					} else {
//						Intent intent = new Intent(getActivity(),
//								CostReportActivity.class);
//						getActivity().startActivity(intent);
//
//					}
//				}
//			});
//			findListView
//					.setOnItemLongClickListener(new OnItemLongClickListener() {
//
//						@Override
//						public boolean onItemLongClick(AdapterView<?> parent,
//								View view, int position, long id) {
//							// TODO Auto-generated method stub
//							if (position != 0) {
//								showDeleteDlg(position);
//							}
//							return true;
//						}
//
//					});
//		}
//
//		@Override
//		public void returnTripFail(String error) {
//			// TODO Auto-generated method stub
//			if (loadingDlg != null && loadingDlg.isShowing()) {
//				loadingDlg.dismiss();
//			}
//			pullToRefreshLayout.refreshFinish(PullToRefreshLayout.FAIL);
//			WebServiceMiddleUtils.hand(getActivity(), error);
//		}
//
//		@Override
//		public void returnDelTripSuccess() {
//			// TODO Auto-generated method stub
//			if (loadingDlg != null && loadingDlg.isShowing()) {
//				loadingDlg.dismiss();
//			}
//			tripList.remove(deletePosition);
//			adapter.notifyDataSetChanged();
//		}
//
//		@Override
//		public void returnDelTripFail(String error) {
//			// TODO Auto-generated method stub
//			if (loadingDlg != null && loadingDlg.isShowing()) {
//				loadingDlg.dismiss();
//			}
//			WebServiceMiddleUtils.hand(getActivity(), error);
//		}
//
//	}
//
//	@Override
//	public void onDestroy() {
//		// TODO Auto-generated method stub
//		super.onDestroy();
//		if (refreshReceiver != null) {
//			getActivity().unregisterReceiver(refreshReceiver);
//			refreshReceiver = null;
//		}
//	}

}
