package com.inspur.emmcloud.ui.schedule.calendar;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.SwitchCompat;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.inspur.emmcloud.BaseActivity;
import com.inspur.emmcloud.R;
import com.inspur.emmcloud.bean.system.SimpleEventMessage;
import com.inspur.emmcloud.bean.work.MyCalendar;
import com.inspur.emmcloud.config.Constant;
import com.inspur.emmcloud.util.common.PreferencesUtils;
import com.inspur.emmcloud.util.privates.CalendarColorUtils;
import com.inspur.emmcloud.util.privates.cache.MyCalendarCacheUtils;
import com.inspur.emmcloud.util.privates.cache.MyCalendarOperationCacheUtils;
import com.inspur.emmcloud.widget.ScrollViewWithListView;

import org.greenrobot.eventbus.EventBus;
import org.xutils.view.annotation.ContentView;
import org.xutils.view.annotation.ViewInject;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by libaochao on 2019/4/2.
 */
@ContentView(R.layout.activity_calendar_setting)
public class CalendarSettingActivity extends BaseActivity {
    @ViewInject(R.id.listview_list_calendars)
    private ScrollViewWithListView calendarsListView;
    @ViewInject(R.id.iv_list_view_select)
    private ImageView listSelectImageView;
    @ViewInject(R.id.iv_day_view_select)
    private ImageView daySelectImageView;

    public static String EXTRA_SCHEDULE_CALENDAR_SETTING_CALENDARLIST = "schedule_calendar_setting_calendarlist";

    private List<MyCalendar> calendarsList = new ArrayList<>();
    private CalendarAdapter calendarAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        String viewDisplayType = PreferencesUtils.getString(
                getApplicationContext(), Constant.PREF_SCHEDULE_CALENDAR_VIEW_DISPLAY_TYPE, Constant.PREF_SCHEDULE_CALENDAR_VIEW_DISPLAY_TYPE_LISTVIEW);
        boolean isListView = viewDisplayType.equals(Constant.PREF_SCHEDULE_CALENDAR_VIEW_DISPLAY_TYPE_LISTVIEW);
        listSelectImageView.setVisibility(isListView ? View.VISIBLE : View.GONE);
        daySelectImageView.setVisibility(isListView ? View.GONE : View.VISIBLE);
        calendarsList = MyCalendarCacheUtils.getAllMyCalendarList(this);
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
                if (listSelectImageView.getVisibility() != View.VISIBLE) {
                    listSelectImageView.setVisibility(View.VISIBLE);
                    daySelectImageView.setVisibility(View.INVISIBLE);
                    PreferencesUtils.putString(getApplicationContext(),
                            Constant.PREF_SCHEDULE_CALENDAR_VIEW_DISPLAY_TYPE, Constant.PREF_SCHEDULE_CALENDAR_VIEW_DISPLAY_TYPE_LISTVIEW);
                }
                break;
            case R.id.rl_day_view:
                if (daySelectImageView.getVisibility() != View.VISIBLE) {
                    daySelectImageView.setVisibility(View.VISIBLE);
                    listSelectImageView.setVisibility(View.INVISIBLE);
                    PreferencesUtils.putString(getApplicationContext(),
                            Constant.PREF_SCHEDULE_CALENDAR_VIEW_DISPLAY_TYPE, Constant.PREF_SCHEDULE_CALENDAR_VIEW_DISPLAY_TYPE_DAYVIEW);
                }
                break;
            case R.id.tv_save:
                break;
            default:
                break;
        }
    }

    private void returnCalList() {
        // TODO Auto-generated method stub
        EventBus.getDefault().post(new SimpleEventMessage(Constant.EVENTBUS_TAG_SCHEDULE_CALENDAR_REFRESH, ""));
        Intent intent = new Intent();
        intent.putExtra(EXTRA_SCHEDULE_CALENDAR_SETTING_CALENDARLIST, (Serializable) calendarsList);
        setResult(RESULT_OK, intent);
    }

    @Override
    public void onBackPressed() {
        // TODO Auto-generated method stub
        returnCalList();
        super.onBackPressed();
    }

    private class CalendarHolder {
        View calendarStyleColorView;
        SwitchCompat calendarSwitch;
        TextView calendarNameText;
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
            CalendarHolder calendarHolder;
            final MyCalendar calendar = calendarsList.get(position);
            if (null == convertView) {
                convertView = View.inflate(CalendarSettingActivity.this, R.layout.schedule_calendar_setting_mycalendars, null);
                calendarHolder = new CalendarHolder();
                calendarHolder.calendarNameText = convertView.findViewById(R.id.tv_calendar_name);
                calendarHolder.calendarStyleColorView = convertView.findViewById(R.id.iv_calendar_color_hint);
                calendarHolder.calendarSwitch = convertView.findViewById(R.id.switch_view_calendar_state);
                convertView.setTag(calendarHolder);
            } else {
                calendarHolder = (CalendarHolder) convertView.getTag();
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
            calendarHolder.calendarStyleColorView.setBackgroundResource(CalendarColorUtils.getColorCircleImage(calendar.getColor()));
            calendarHolder.calendarNameText.setText(calendar.getName());
            return convertView;
        }
    }

}
