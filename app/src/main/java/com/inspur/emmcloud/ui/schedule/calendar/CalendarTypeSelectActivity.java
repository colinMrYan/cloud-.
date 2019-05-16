package com.inspur.emmcloud.ui.schedule.calendar;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.inspur.emmcloud.BaseActivity;
import com.inspur.emmcloud.R;
import com.inspur.emmcloud.api.APIInterfaceInstance;
import com.inspur.emmcloud.api.apiservice.ScheduleApiService;
import com.inspur.emmcloud.bean.schedule.MyCalendar;
import com.inspur.emmcloud.bean.work.GetMyCalendarResult;
import com.inspur.emmcloud.util.privates.CalendarColorUtils;
import com.inspur.emmcloud.util.privates.cache.MyCalendarCacheUtils;

import org.xutils.view.annotation.ContentView;
import org.xutils.view.annotation.ViewInject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by libaochao on 2019/3/29.
 */
@ContentView(R.layout.activity_calendar_type_select)
public class CalendarTypeSelectActivity extends BaseActivity {
    MyCalendar calendar;
    @ViewInject(R.id.lv_calendars)
    private ListView calendarListView;
    private ScheduleApiService scheduleAPIService;
    private List<MyCalendar> calendarList = new ArrayList<MyCalendar>();
    private CalendarAdapter calendarAdapter;
    private int selectPosition = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        initView();
    }

    private void initView() {
        // TODO Auto-generated method stub
        calendarAdapter = new CalendarAdapter();
        calendarListView.setAdapter(calendarAdapter);
        scheduleAPIService = new ScheduleApiService(this);
        scheduleAPIService.setAPIInterface(new WebService());
        calendarList = MyCalendarCacheUtils.getAllMyCalendarList(getApplicationContext());
        if (calendarList.size() > 0) {
            calendarAdapter.notifyDataSetChanged();
        }
        scheduleAPIService.getMyCalendar(0, 30);
        calendarListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                // TODO Auto-generated method stub
                selectPosition = position;
                calendarAdapter.notifyDataSetChanged();
                calendar = calendarList.get(position);
                Intent intent = new Intent();
                intent.putExtra(CalendarAddActivity.EXTRA_SCHEDULE_CALENDAR_TYPE, calendar);
                setResult(RESULT_OK, intent);
                finish();
            }
        });
    }

    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.ibt_back:
                finish();
                break;
            default:
                break;
        }
    }

    /***/
    private class CalendarAdapter extends BaseAdapter {
        @Override
        public int getCount() {
            // TODO Auto-generated method stub
            return calendarList.size();
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
        public View getView(final int position, View convertView, ViewGroup parent) {
            // TODO Auto-generated method stub
            LayoutInflater vi = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
            convertView = vi.inflate(R.layout.schedule_calendar_type_select, null);
            TextView textView = convertView.findViewById(R.id.tv_calendar_name);
            MyCalendar calendar = calendarList.get(position);
            ImageView calSelectImg = convertView.findViewById(R.id.iv_calendar_select);
            if (position == selectPosition) {
                calSelectImg.setVisibility(View.VISIBLE);
            } else {
                calSelectImg.setVisibility(View.GONE);
            }
            textView.setText(calendar.getName());
            View colorView = convertView.findViewById(R.id.v_calendar_color);
            int color = CalendarColorUtils.getColor(CalendarTypeSelectActivity.this, calendar.getColor());
            colorView.setBackgroundColor(color);
            return convertView;
        }

    }

    /**
     * 拉取Calendar
     */
    class WebService extends APIInterfaceInstance {
        @Override
        public void returnMyCalendarSuccess(GetMyCalendarResult getMyCalendarResult) {
            List<MyCalendar> allCalendarList = getMyCalendarResult.getCalendarList();
            calendarList.clear();
            calendarList.addAll(allCalendarList);
            MyCalendarCacheUtils.saveMyCalendarList(CalendarTypeSelectActivity.this, calendarList);
            if (getIntent().hasExtra(CalendarAddActivity.EXTRA_SCHEDULE_CALENDAR_TYPE_SELECT)) {
                calendar = (MyCalendar) getIntent().getExtras().getSerializable(CalendarAddActivity.EXTRA_SCHEDULE_CALENDAR_TYPE_SELECT);
                for (int i = 0; i < calendarList.size(); i++) {
                    if (calendarList.get(i).getId().equals(calendar.getId())) {
                        selectPosition = i;
                        break;
                    }
                }
            }
            calendarAdapter.notifyDataSetChanged();
            MyCalendarCacheUtils.saveMyCalendarList(CalendarTypeSelectActivity.this, allCalendarList);
            super.returnMyCalendarSuccess(getMyCalendarResult);
        }

        @Override
        public void returnMyCalendarFail(String error, int errorCode) {
            super.returnMyCalendarFail(error, errorCode);
        }
    }
}
