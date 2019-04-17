package com.inspur.emmcloud.ui.schedule.calendar;

import android.os.Bundle;
import android.support.v7.widget.SwitchCompat;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.inspur.emmcloud.BaseActivity;
import com.inspur.emmcloud.MyApplication;
import com.inspur.emmcloud.R;
import com.inspur.emmcloud.api.APIInterfaceInstance;
import com.inspur.emmcloud.api.apiservice.WorkAPIService;
import com.inspur.emmcloud.bean.system.SimpleEventMessage;
import com.inspur.emmcloud.bean.work.GetMyCalendarResult;
import com.inspur.emmcloud.bean.work.MyCalendar;
import com.inspur.emmcloud.config.Constant;
import com.inspur.emmcloud.util.common.NetUtils;
import com.inspur.emmcloud.util.common.PreferencesUtils;
import com.inspur.emmcloud.util.privates.CalendarColorUtils;
import com.inspur.emmcloud.util.privates.cache.MyCalendarCacheUtils;
import com.inspur.emmcloud.util.privates.cache.MyCalendarOperationCacheUtils;
import com.inspur.emmcloud.widget.ScrollViewWithListView;

import org.greenrobot.eventbus.EventBus;
import org.xutils.view.annotation.ContentView;
import org.xutils.view.annotation.ViewInject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by libaochao on 2019/4/2.
 */
@ContentView(R.layout.activity_calendar_setting)
public class CalendarSettingActivity extends BaseActivity {
    public static final String SHOW_TYPE_LIST = "show_type_list";
    public static final String SHOW_TYPE_DAY_VIEW = "show_type_day_view";
    public static String EXTRA_SCHEDULE_CALENDAR_SETTING_CALENDARLIST = "schedule_calendar_setting_calendarlist";
    @ViewInject(R.id.listview_list_calendars)
    private ScrollViewWithListView calendarsListView;
    @ViewInject(R.id.iv_list_view_select)
    private ImageView listSelectImageView;
    @ViewInject(R.id.iv_day_view_select)
    private ImageView daySelectImageView;
    private List<MyCalendar> calendarsList = new ArrayList<>();
    private CalendarAdapter calendarAdapter;
    private WorkAPIService workAPIService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        String viewDisplayType = PreferencesUtils.getString(MyApplication.getInstance(), Constant.PREF_CALENDAR_EVENT_SHOW_TYPE, SHOW_TYPE_DAY_VIEW);
        boolean isListView = viewDisplayType.equals(SHOW_TYPE_LIST);
        listSelectImageView.setVisibility(isListView ? View.VISIBLE : View.GONE);
        daySelectImageView.setVisibility(isListView ? View.GONE : View.VISIBLE);
        workAPIService = new WorkAPIService(this);
        workAPIService.setAPIInterface(new WebService());
        calendarsList = MyCalendarCacheUtils.getAllMyCalendarList(getApplicationContext());
        calendarsList = MyCalendarCacheUtils.getAllMyCalendarList(this);
        calendarAdapter = new CalendarAdapter();
        calendarsListView.setAdapter(calendarAdapter);
        getCalendarType();
    }

    private void getCalendarType() {
        if (NetUtils.isNetworkConnected(this)) {
            workAPIService.getMyCalendar(0, 30);
        }
    }

    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.ibt_back:
                onBackPressed();
                break;
            case R.id.rl_list_view:
                if (listSelectImageView.getVisibility() != View.VISIBLE) {
                    listSelectImageView.setVisibility(View.VISIBLE);
                    daySelectImageView.setVisibility(View.INVISIBLE);
                    PreferencesUtils.putString(getApplicationContext(),
                            Constant.PREF_CALENDAR_EVENT_SHOW_TYPE, SHOW_TYPE_LIST);
                }
                break;
            case R.id.rl_day_view:
                if (daySelectImageView.getVisibility() != View.VISIBLE) {
                    daySelectImageView.setVisibility(View.VISIBLE);
                    listSelectImageView.setVisibility(View.INVISIBLE);
                    PreferencesUtils.putString(getApplicationContext(),
                            Constant.PREF_CALENDAR_EVENT_SHOW_TYPE, SHOW_TYPE_DAY_VIEW);
                }
                break;
            default:
                break;
        }
    }


    @Override
    public void onBackPressed() {
        // TODO Auto-generated method stub
        //通知其他页面日历设置发生改变
        EventBus.getDefault().post(new SimpleEventMessage(Constant.EVENTBUS_TAG_SCHEDULE_CALENDAR_SETTING_CHANGED, ""));
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

    /**
     * 拉取Calendar
     */
    class WebService extends APIInterfaceInstance {
        @Override
        public void returnMyCalendarSuccess(GetMyCalendarResult getMyCalendarResult) {
            List<MyCalendar> allCalendarList = getMyCalendarResult.getCalendarList();
            calendarsList.clear();
            calendarsList.addAll(allCalendarList);
            MyCalendarCacheUtils.saveMyCalendarList(CalendarSettingActivity.this, calendarsList);
            calendarAdapter.notifyDataSetChanged();
        }

        @Override
        public void returnMyCalendarFail(String error, int errorCode) {
            super.returnMyCalendarFail(error, errorCode);
        }
    }

}
