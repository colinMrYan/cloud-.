package com.inspur.emmcloud.ui.schedule.schedule;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.widget.SwitchCompat;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.inspur.emmcloud.BaseActivity;
import com.inspur.emmcloud.R;
import com.inspur.emmcloud.bean.work.MyCalendar;
import com.inspur.emmcloud.config.Constant;
import com.inspur.emmcloud.util.common.PreferencesUtils;
import com.inspur.emmcloud.util.privates.CalendarColorUtils;
import com.inspur.emmcloud.util.privates.cache.MyCalendarCacheUtils;
import com.inspur.emmcloud.util.privates.cache.MyCalendarOperationCacheUtils;
import com.inspur.emmcloud.widget.ScrollViewWithListView;

import org.xutils.view.annotation.ContentView;
import org.xutils.view.annotation.ViewInject;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by libaochao on 2019/3/27.
 */

@ContentView(R.layout.activity_schedule_setting)
public class ScheduleSettingActivity extends BaseActivity {
    @ViewInject(R.id.scrollview_list_calendars)
    private ScrollViewWithListView calendarsListView;
    @ViewInject(R.id.iv_list_view_tip)
    private ImageView listViewImageView;
    @ViewInject(R.id.iv_day_view_tip)
    private ImageView dayImageView;

    private List<MyCalendar> calendarsList = new ArrayList<>();
    private CalendarAdapter calendarAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        String calEventDisplayType = PreferencesUtils.getString(
                getApplicationContext(), "celEventDisplayType", "monthly");
        String viewDisplayType = PreferencesUtils.getString(
                getApplicationContext(), "viewDisplayType", "listview");
        boolean isListView = viewDisplayType.equals("listview");
        listViewImageView.setVisibility(isListView ? View.VISIBLE : View.GONE);
        dayImageView.setVisibility(isListView ? View.GONE : View.VISIBLE);
        calendarsList  = MyCalendarCacheUtils.getAllMyCalendarList(this);
        calendarAdapter = new CalendarAdapter();
        calendarsListView.setAdapter(calendarAdapter);
    }

    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.ibt_back:
                returnCalList();
                finish();
                break;
            case R.id.rl_list_view:
                if (listViewImageView.getVisibility() != View.VISIBLE) {
                    listViewImageView.setVisibility(View.VISIBLE);
                    dayImageView.setVisibility(View.INVISIBLE);
                    PreferencesUtils.putString(getApplicationContext(),
                            "viewDisplayType", "listview");
                }
                break;
            case R.id.rl_day_view:
                if (dayImageView.getVisibility() != View.VISIBLE) {
                    dayImageView.setVisibility(View.VISIBLE);
                    listViewImageView.setVisibility(View.INVISIBLE);
                    PreferencesUtils.putString(getApplicationContext(),
                            "viewDisplayType", "dayview");
                }
                break;
            default:
                break;
        }
    }

    private void returnCalList() {
        // TODO Auto-generated method stub
        sendBoradcastReceiver();
        Intent intent = new Intent();
        intent.putExtra("calendarList", (Serializable) calendarsList);
        setResult(RESULT_OK, intent);
    }

    @Override
    public void onBackPressed() {
        // TODO Auto-generated method stub
        returnCalList();
        super.onBackPressed();
    }

    private class CalendarHolder {
        View calendarStyleColor;
        SwitchCompat calendarSwitch;
        TextView calendarName;
    }

    private class CalendarAdapter extends BaseAdapter {
        @Override
        public int getCount() {
            // TODO Auto-generated method stub
            return calendarsList.size();
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
        public View getView(final int position, View convertView,
                            ViewGroup parent) {
            // TODO Auto-generated method stub
            View v = null;
            CalendarHolder calendarHolder;
            final MyCalendar calendar = calendarsList.get(position);

            if (null == convertView) {
                v = View.inflate(ScheduleSettingActivity.this, R.layout.item_calendar_list, null);
                calendarHolder = new CalendarHolder();
                calendarHolder.calendarName = v.findViewById(R.id.tv_calendar_name);
                calendarHolder.calendarStyleColor = v.findViewById(R.id.iv_calendar_color_hint);
                calendarHolder.calendarSwitch = v.findViewById(R.id.switch_view_calendar_state);
                v.setTag(calendarHolder);
            } else {
                v = convertView;
                calendarHolder = (CalendarHolder) v.getTag();
            }
            boolean isHide = MyCalendarOperationCacheUtils.getIsHide(getApplicationContext(), calendar.getId());
            calendarHolder.calendarSwitch.setChecked(!isHide);
            calendarHolder.calendarSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                    if (b) {
                        MyCalendarOperationCacheUtils.saveMyCalendarOperation(getApplicationContext(), calendar.getId(), false);
                    } else {
                        MyCalendarOperationCacheUtils.saveMyCalendarOperation(getApplicationContext(), calendar.getId(), true);
                    }
                }
            });
            calendarHolder.calendarStyleColor.setBackgroundResource(CalendarColorUtils.getColorCircleImage(calendar.getColor()));
            calendarHolder.calendarName.setText(calendar.getName());
            return v;
        }
    }

    /**
     * 发送Calendar变化通知
     */
    public void sendBoradcastReceiver() {
        Intent mIntent = new Intent(Constant.ACTION_CALENDAR);
        mIntent.putExtra("refreshCalendar", "");
        LocalBroadcastManager.getInstance(this).sendBroadcast(mIntent);
    }
}
