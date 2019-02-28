package com.inspur.emmcloud.ui.work.calendar;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.inspur.emmcloud.BaseActivity;
import com.inspur.emmcloud.R;
import com.inspur.emmcloud.widget.calendarlistview.SimpleMonthAdapter.CalendarDay;
import com.inspur.emmcloud.widget.calendarlistview.SimpleMonthView;

import java.util.HashMap;

public class CalSingleMonthActivity extends BaseActivity implements SimpleMonthView.OnDayClickListener {
    private SimpleMonthView simpleMonthView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cal_single_month);
        initView();

    }

    private void initView() {
        // TODO Auto-generated method stub
        RelativeLayout calLayout = (RelativeLayout) findViewById(R.id.cal_layout);
        simpleMonthView = new SimpleMonthView(this, true);
        simpleMonthView.setOnDayClickListener(this);
        int selectYear = getIntent().getExtras().getInt("select_year");
        int selectMonth = getIntent().getExtras().getInt("select_month");
        int selectDay = getIntent().getExtras().getInt("select_day");
        String handerText = selectYear + getString(R.string.year) + (selectMonth + 1) + getString(R.string.month);
        ((TextView) findViewById(R.id.cal_header_text)).setText(handerText);
        setDrawingParams(selectYear, selectMonth, selectDay);
        calLayout.addView(simpleMonthView, new LayoutParams(
                LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
        getCalEventOfDay();

    }

    private void setDrawingParams(int year, int month, int day) {
        HashMap<String, Integer> drawingParams = new HashMap<String, Integer>();
        drawingParams.put(SimpleMonthView.VIEW_PARAMS_SELECTED_BEGIN_YEAR,
                year);
        drawingParams.put(SimpleMonthView.VIEW_PARAMS_SELECTED_BEGIN_MONTH,
                month);
        drawingParams.put(SimpleMonthView.VIEW_PARAMS_SELECTED_BEGIN_DAY,
                day);
        drawingParams.put(SimpleMonthView.VIEW_PARAMS_YEAR, year);
        drawingParams.put(SimpleMonthView.VIEW_PARAMS_MONTH, month);
        simpleMonthView.setMonthParams(drawingParams);
        simpleMonthView.invalidate();
    }

    /**
     * 获取
     */
    private void getCalEventOfDay() {
        // TODO Auto-generated method stub

    }

    public void onClick(View v) {
        Intent intent = new Intent();
        switch (v.getId()) {
            case R.id.ibt_back:
                finish();
                break;

//		case R.id.mession_cal_img:
//
//			intent.setClass(CalSingleMonthActivity.this,
//					CalSingleMonthActivity.class);
//			startActivity(intent);
//			break;
            case R.id.add_event_text:
                intent.setClass(CalSingleMonthActivity.this,
                        CalEventAddActivity.class);
                startActivity(intent);
                break;
            default:
                break;
        }
    }

    @Override
    public void onDayClick(SimpleMonthView simpleMonthView,
                           CalendarDay calendarDay) {
        // TODO Auto-generated method stub
        if (calendarDay != null) {
            onDayTapped(calendarDay);
            int year = calendarDay.getYear();
            int month = calendarDay.getMonth();
            int day = calendarDay.getDay();
            setDrawingParams(year, month, day);
        }
    }

    protected void onDayTapped(CalendarDay calendarDay) {
        Log.d("jason", "year=" + calendarDay.getYear());
        Log.d("jason", "month=" + calendarDay.getMonth());
        Log.d("jason", "day=" + calendarDay.getDay());
//		mController.onDayOfMonthSelected(calendarDay.year, calendarDay.month, calendarDay.day);
//		setSelectedDay(calendarDay);
    }


}
