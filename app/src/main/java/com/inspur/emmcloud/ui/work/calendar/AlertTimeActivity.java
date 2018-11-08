package com.inspur.emmcloud.ui.work.calendar;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.inspur.emmcloud.BaseActivity;
import com.inspur.emmcloud.R;

/**
 * 日历提醒时间选择界面 com.inspur.emmcloud.ui.AlertTimeActivity
 * 
 * create at 2016年8月23日 下午2:56:43
 */
public class AlertTimeActivity extends BaseActivity {
	private ListView alertTimeListView;
	private Adapter adapter;
	private int selectPosition = -1;
	private ImageView noSelectImg;
	private String alertTime;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_alert_time);
		final String[] alertTimeArray = {
				getString(R.string.calendar_when_event_occurs),
				getString(R.string.calendar_five_minite_ago),
				getString(R.string.calendar_fifteen_minite_ago),
				getString(R.string.calendar_thirty_minite_ago),
				getString(R.string.calendar_one_hour_ago),
				getString(R.string.calendar_two_hours_ago),
				getString(R.string.calendar_one_day_ago),
				getString(R.string.calendar_two_days_ago),
				getString(R.string.calendar_a_week_ago) };
		alertTimeListView = (ListView) findViewById(R.id.alert_time_list);
		noSelectImg = (ImageView) findViewById(R.id.no_alert_select_img);
		if (getIntent().getExtras().containsKey("alertTime")) {
			alertTime = getIntent().getExtras().getString("alertTime");
		} else {
			alertTime = getString(R.string.nothing);
		}
		if (!alertTime.equals(getString(R.string.nothing))) {

			for (int i = 0; i < alertTimeArray.length; i++) {
				if (alertTimeArray[i].equals(alertTime)) {
					selectPosition = i;
					noSelectImg.setVisibility(View.VISIBLE);
					break;
				}
			}
		}
		adapter = new Adapter(alertTimeArray);
		alertTimeListView.setAdapter(adapter);
		alertTimeListView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				// TODO Auto-generated method stub
				noSelectImg.setVisibility(View.GONE);
				selectPosition = position;
				adapter.notifyDataSetChanged();
				alertTime = alertTimeArray[position];
				returnData();
			}
		});
	}

	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.back_layout:
			finish();
			break;

		case R.id.no_alert_layout:
			noSelectImg.setVisibility(View.VISIBLE);
			selectPosition = -1;
			alertTime = getString(R.string.nothing);
			returnData();
			break;

		default:
			break;
		}
	}

	public void returnData() {
		Intent intent = new Intent();
		intent.putExtra("alertTime", alertTime);
		setResult(RESULT_OK, intent);
		finish();
	}

	private class Adapter extends BaseAdapter {
		private String[] alertTimeArray;

		public Adapter(String[] alertTimeArray) {
			this.alertTimeArray = alertTimeArray;
		}

		@Override
		public int getCount() {
			// TODO Auto-generated method stub
			return alertTimeArray.length;
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
			LayoutInflater vi = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
			convertView = vi.inflate(R.layout.alert_time_item_view, null);
			TextView timeText = (TextView) convertView
					.findViewById(R.id.alert_time_text);
			ImageView selectImg = (ImageView) convertView
					.findViewById(R.id.alert_time_select_img);
			timeText.setText(alertTimeArray[position]);
			if (selectPosition == position) {
				selectImg.setVisibility(View.VISIBLE);
			} else {
				selectImg.setVisibility(View.GONE);
			}
			return convertView;
		}

	}
}
