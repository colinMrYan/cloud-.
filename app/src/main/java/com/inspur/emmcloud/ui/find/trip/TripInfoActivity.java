package com.inspur.emmcloud.ui.find.trip;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.inspur.emmcloud.BaseActivity;
import com.inspur.emmcloud.MyApplication;
import com.inspur.emmcloud.R;
import com.inspur.emmcloud.api.APIInterfaceInstance;
import com.inspur.emmcloud.api.apiservice.FindAPIService;
import com.inspur.emmcloud.bean.Trip;
import com.inspur.emmcloud.util.NetUtils;
import com.inspur.emmcloud.util.StringUtils;
import com.inspur.emmcloud.util.TimeUtils;
import com.inspur.emmcloud.util.ToastUtils;
import com.inspur.emmcloud.widget.LoadingDialog;

import java.io.Serializable;

public class TripInfoActivity extends BaseActivity{

	private static final int EDIT_TRIP_INFO = 1;
	private Trip trip;
	private LoadingDialog loadingDlg;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		((MyApplication) getApplicationContext()).addActivity(this);
		setContentView(R.layout.activity_trip_info);
		Uri uri = getIntent().getData();
		if (uri != null) {
			String path = uri.getPath();
			String tripId = path.split("/")[1];
			getTripInfo(tripId);
		}else {
			trip = (Trip) getIntent().getExtras().getSerializable("tripInfo");
			disPlayTripInfo();
		}


	}

	/**
	 * 网络获取行程数据
	 * @param tripId
     */
	private void getTripInfo(String  tripId){
		if (NetUtils.isNetworkConnected(getApplicationContext())){
			loadingDlg  = new LoadingDialog(this);
			loadingDlg.show();
			FindAPIService apiService = new FindAPIService(getApplicationContext());
			apiService.setAPIInterface(new WebServeice());
			apiService.getTripInfo(tripId);
		}

	}

	/**
	 * 展示行程数据
	 */
	private void disPlayTripInfo() {
		// TODO Auto-generated method stub
		String startTime = TimeUtils.calendar2FormatString(getApplicationContext(),trip.getStart(),TimeUtils.FORMAT_HOUR_MINUTE);
		String orderID = trip.getOrderID();
		String from = trip.getFrom();
		String destination = trip.getDestination();
		String seatLeve = trip.getLevel();
		String tripNum = trip.getNumber();
		float cost = trip.getCost();
		String costStr = "";
		if (cost == -1) {
			costStr = "";
		}else {
			costStr = cost+"";
		}
		if (StringUtils.isBlank(destination)) {
			destination = "?";
		}
		String tripUserName = trip.getSourceuname();
		((TextView)findViewById(R.id.trip_time_text)).setText(startTime);
		((TextView)findViewById(R.id.trip_order_num_text)).setText(getString(R.string.order_num)+" "+orderID);
		((TextView)findViewById(R.id.trip_from)).setText(from);
		((TextView)findViewById(R.id.trip_destination)).setText(destination);
		((TextView)findViewById(R.id.seat_level_text)).setText(seatLeve);
		((TextView)findViewById(R.id.trip_user_text)).setText(tripUserName);
		((TextView)findViewById(R.id.trip_cost_text)).setText(costStr);
		((TextView)findViewById(R.id.trip_number_text)).setText(tripNum);
	}

	public void onClick(View v){
		switch (v.getId()) {
		case R.id.back_layout:
			finish();
			break;
		case R.id.edit_text:
			Intent intent = new Intent(getApplicationContext(),TripDetailActivity.class);
			intent.putExtra("trip", (Serializable)trip);
			startActivityForResult(intent, EDIT_TRIP_INFO);
			break;

		default:
			break;
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// TODO Auto-generated method stub
		super.onActivityResult(requestCode, resultCode, data);
		if (resultCode == RESULT_OK && requestCode == EDIT_TRIP_INFO) {
			trip = (Trip) data.getSerializableExtra("newTrip");
			disPlayTripInfo();
		}
	}

	private class WebServeice extends APIInterfaceInstance{
		@Override
		public void returnTripSuccess(Trip trip) {
			if (loadingDlg != null && loadingDlg.isShowing()){
				loadingDlg.dismiss();
			}
			TripInfoActivity.this.trip = trip;
			disPlayTripInfo();
		}

		@Override
		public void returnTripFail(String error) {
			if (loadingDlg != null && loadingDlg.isShowing()){
				loadingDlg.dismiss();
			}
			ToastUtils.show(getApplicationContext(),"行程获取失败");
			finish();
		}
	}
}
