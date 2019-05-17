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
import com.inspur.emmcloud.bean.schedule.MyCalendar;
import com.inspur.emmcloud.util.privates.CalendarColorUtils;
import com.inspur.emmcloud.util.privates.cache.MyCalendarCacheUtils;

import java.util.ArrayList;
import java.util.List;

public class CalTypeSelectActivity extends BaseActivity {
    private ListView calendarListView;
    private List<MyCalendar> calendarList = new ArrayList<MyCalendar>();
    private CalendarAdapter calendarAdapter;
    private int selectPosition = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cal_type_select);
        calendarListView = (ListView) findViewById(R.id.calendar_list);
        initView();
    }


    private void initView() {
        // TODO Auto-generated method stub
        List<MyCalendar> allCalendarList = MyCalendarCacheUtils.getAllMyCalendarList(getApplicationContext());
        for (int i = 0; i < allCalendarList.size(); i++) {
            MyCalendar myCalendar = allCalendarList.get(i);
            if (!myCalendar.getCommunity()) {
                calendarList.add(myCalendar);
            }

        }
        if (getIntent().hasExtra("selectCalendar")) {
            MyCalendar selectCalendar = (MyCalendar) getIntent().getExtras().getSerializable("selectCalendar");
            for (int i = 0; i < calendarList.size(); i++) {
                if (calendarList.get(i).getId().equals(selectCalendar.getId())) {
                    selectPosition = i;
                    break;
                }
            }
        }

        calendarAdapter = new CalendarAdapter();
        calendarListView.setAdapter(calendarAdapter);
        calendarListView.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                // TODO Auto-generated method stub
                selectPosition = position;
                calendarAdapter.notifyDataSetChanged();
                MyCalendar calendar = calendarList.get(position);
                Intent intent = new Intent();
                intent.putExtra("result", calendar);
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
            convertView = vi.inflate(R.layout.calendar_select_item_view, null);
            TextView textView = (TextView) convertView
                    .findViewById(R.id.calendar_name_text);
            MyCalendar calendar = calendarList.get(position);
            ImageView calSelectImg = (ImageView) convertView.findViewById(R.id.cal_select_img);
            if (position == selectPosition) {
                calSelectImg.setVisibility(View.VISIBLE);
            } else {
                calSelectImg.setVisibility(View.GONE);
            }
            textView.setText(calendar.getName());
            View colorView = convertView
                    .findViewById(R.id.calendar_color_view);
            int color = CalendarColorUtils.getColor(CalTypeSelectActivity.this, calendar.getColor());
            colorView.setBackgroundColor(color);
            return convertView;
        }

    }


}
