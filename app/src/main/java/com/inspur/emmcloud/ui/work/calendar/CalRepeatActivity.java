package com.inspur.emmcloud.ui.work.calendar;

import android.R.integer;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import com.inspur.emmcloud.BaseActivity;
import com.inspur.emmcloud.MyApplication;
import com.inspur.emmcloud.R;

public class CalRepeatActivity extends BaseActivity {
	private String repeatType = "";
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		((MyApplication) getApplicationContext()).addActivity(this);
		setContentView(R.layout.activity_cal_repeat);
		repeatType = getIntent().getExtras().getString("repeatType");
		if (repeatType.equals(getString(R.string.never))) {
			((ImageView)findViewById(R.id.no_selected_img)).setVisibility(View.VISIBLE);
		}else if (repeatType.equals(getString(R.string.every_day))) {
			((ImageView)findViewById(R.id.every_day_selected_img)).setVisibility(View.VISIBLE);
		}else if (repeatType.equals(getString(R.string.weekly))) {
			((ImageView)findViewById(R.id.every_week_selected_img)).setVisibility(View.VISIBLE);
		}else if (repeatType.equals(getString(R.string.monthly))) {
			((ImageView)findViewById(R.id.every_month_selected_img)).setVisibility(View.VISIBLE);
		}else {
			((ImageView)findViewById(R.id.every_year_selected_img)).setVisibility(View.VISIBLE);
		}
		
	}

	private void selectItem(int count) {
		((ImageView)findViewById(R.id.no_selected_img)).setVisibility(View.INVISIBLE);
		((ImageView)findViewById(R.id.every_day_selected_img)).setVisibility(View.INVISIBLE);
		((ImageView)findViewById(R.id.every_week_selected_img)).setVisibility(View.INVISIBLE);
		((ImageView)findViewById(R.id.every_month_selected_img)).setVisibility(View.INVISIBLE);
		((ImageView)findViewById(R.id.every_year_selected_img)).setVisibility(View.INVISIBLE);
		switch (count) {
		case 1:
			repeatType = getString(R.string.never);
			((ImageView)findViewById(R.id.no_selected_img)).setVisibility(View.VISIBLE);
			break;
		case 2:
			repeatType = getString(R.string.every_day);
			((ImageView)findViewById(R.id.every_day_selected_img)).setVisibility(View.VISIBLE);
			break;
		case 3:
			repeatType = getString(R.string.weekly);
			((ImageView)findViewById(R.id.every_week_selected_img)).setVisibility(View.VISIBLE);
			break;
		case 4:
			repeatType = getString(R.string.monthly);
			((ImageView)findViewById(R.id.every_month_selected_img)).setVisibility(View.VISIBLE);
			break;
		case 5:
			repeatType =  getString(R.string.every_year);
			((ImageView)findViewById(R.id.every_year_selected_img)).setVisibility(View.VISIBLE);
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
		case R.id.back_layout:
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
