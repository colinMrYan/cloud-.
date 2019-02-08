package com.inspur.emmcloud.ui.work.calendar;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.inspur.emmcloud.BaseActivity;
import com.inspur.emmcloud.R;

public class CalRepeatActivity extends BaseActivity {
	private String repeatType = "";
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_cal_repeat);
		repeatType = getIntent().getExtras().getString("repeatType");
		if (repeatType.equals(getString(R.string.never))) {
			findViewById(R.id.no_selected_img).setVisibility(View.VISIBLE);
		}else if (repeatType.equals(getString(R.string.every_day))) {
			findViewById(R.id.every_day_selected_img).setVisibility(View.VISIBLE);
		}else if (repeatType.equals(getString(R.string.weekly))) {
			findViewById(R.id.every_week_selected_img).setVisibility(View.VISIBLE);
		}else if (repeatType.equals(getString(R.string.monthly))) {
			findViewById(R.id.every_month_selected_img).setVisibility(View.VISIBLE);
		}else {
			findViewById(R.id.every_year_selected_img).setVisibility(View.VISIBLE);
		}
		
	}

	private void selectItem(int count) {
		findViewById(R.id.no_selected_img).setVisibility(View.INVISIBLE);
		findViewById(R.id.every_day_selected_img).setVisibility(View.INVISIBLE);
		findViewById(R.id.every_week_selected_img).setVisibility(View.INVISIBLE);
		findViewById(R.id.every_month_selected_img).setVisibility(View.INVISIBLE);
		findViewById(R.id.every_year_selected_img).setVisibility(View.INVISIBLE);
		switch (count) {
		case 1:
			repeatType = getString(R.string.never);
			findViewById(R.id.no_selected_img).setVisibility(View.VISIBLE);
			break;
		case 2:
			repeatType = getString(R.string.every_day);
			findViewById(R.id.every_day_selected_img).setVisibility(View.VISIBLE);
			break;
		case 3:
			repeatType = getString(R.string.weekly);
			findViewById(R.id.every_week_selected_img).setVisibility(View.VISIBLE);
			break;
		case 4:
			repeatType = getString(R.string.monthly);
			findViewById(R.id.every_month_selected_img).setVisibility(View.VISIBLE);
			break;
		case 5:
			repeatType =  getString(R.string.calendar_every_year);
			findViewById(R.id.every_year_selected_img).setVisibility(View.VISIBLE);
			break;

		default:
			break;
		}
		Intent intent = new Intent();
		intent.putExtra("result",repeatType );
		setResult(RESULT_OK, intent);
		finish();
		
	}

	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.ibt_back:
			finish();
			break;

		case R.id.no_layout:
			selectItem(1);
			break;

		case R.id.every_day_layout:
			selectItem(2);
			break;
		case R.id.every_week_layout:
			selectItem(3);
			break;
		case R.id.every_month_layout:
			selectItem(4);
			break;

		case R.id.every_year_layout:
			selectItem(5);
			break;

		default:
			break;
		}
	}
}
