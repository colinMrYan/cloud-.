package com.inspur.emmcloud.ui.work.calendar;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.inspur.emmcloud.BaseActivity;
import com.inspur.emmcloud.R;
import com.inspur.emmcloud.ui.schedule.calendar.CalendarAddActivity;
import com.inspur.emmcloud.util.common.IntentUtils;
import com.inspur.emmcloud.widget.calendarlistview.DatePickerController;
import com.inspur.emmcloud.widget.calendarlistview.DayPickerView;
import com.inspur.emmcloud.widget.calendarlistview.SimpleMonthAdapter.CalendarDay;
import com.inspur.emmcloud.widget.calendarlistview.SimpleMonthAdapter.SelectedDays;

import java.util.Calendar;

public class CalBrowseActivity extends BaseActivity implements
        DatePickerController {
    private DayPickerView dayPickerView;
    private boolean isDefaultSelect = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calbrowse);
        dayPickerView = (DayPickerView) findViewById(R.id.pickerView);
        dayPickerView.setController(this);
        Calendar calendar = Calendar.getInstance();
        String handerText = calendar.get(Calendar.YEAR)
                + getString(R.string.year);
        ((TextView) findViewById(R.id.cal_header_text)).setText(handerText);
        int currentMonth = calendar.get(Calendar.MONTH) + 1;
        dayPickerView.setDisplayDate(calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH), (12 - currentMonth + 1));

    }

    public void onClick(View v) {
        Intent intent = new Intent();
        switch (v.getId()) {
            case R.id.ibt_back:
                finish();
                break;

//		case R.id.mession_cal_img:
//
//			intent.setClass(CalBrowseActivity.this,
//					CalSingleMonthActivity.class);
//			startActivity(intent);
//			break;
            case R.id.add_event_text:
                intent.setClass(CalBrowseActivity.this, CalendarAddActivity.class);
                startActivity(intent);
                break;
            default:
                break;
        }
    }

    @Override
    public void onDayOfMonthSelected(int year, int month, int day) {
        // TODO Auto-generated method stub
        if (!isDefaultSelect) {
            Bundle bundle = new Bundle();
            bundle.putInt("select_year", year);
            bundle.putInt("select_month", month);
            bundle.putInt("select_day", day);
            IntentUtils.startActivity(CalBrowseActivity.this,
                    CalSingleMonthActivity.class, bundle);
        }
        isDefaultSelect = false;

    }

    @Override
    public void onDateRangeSelected(SelectedDays<CalendarDay> selectedDays) {
        // TODO Auto-generated method stub

    }

}
