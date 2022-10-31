package com.inspur.emmcloud.schedule.ui.calendar;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.widget.SwitchCompat;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.alibaba.android.arouter.launcher.ARouter;
import com.inspur.emmcloud.baselib.util.PreferencesUtils;
import com.inspur.emmcloud.baselib.widget.ScrollViewWithListView;
import com.inspur.emmcloud.baselib.widget.dialogs.ActionSheetDialog;
import com.inspur.emmcloud.basemodule.application.BaseApplication;
import com.inspur.emmcloud.basemodule.bean.SimpleEventMessage;
import com.inspur.emmcloud.basemodule.config.Constant;
import com.inspur.emmcloud.basemodule.ui.BaseActivity;
import com.inspur.emmcloud.basemodule.util.LanguageManager;
import com.inspur.emmcloud.basemodule.util.PreferencesByUserAndTanentUtils;
import com.inspur.emmcloud.schedule.R;
import com.inspur.emmcloud.schedule.R2;
import com.inspur.emmcloud.schedule.bean.calendar.AccountType;
import com.inspur.emmcloud.schedule.bean.calendar.CalendarColor;
import com.inspur.emmcloud.schedule.bean.calendar.ScheduleCalendar;
import com.inspur.emmcloud.schedule.util.CalendarUtils;
import com.inspur.emmcloud.schedule.util.ScheduleCalendarCacheUtils;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by libaochao on 2019/4/2.
 */
public class CalendarSettingActivity extends BaseActivity {
    public static final String SHOW_TYPE_LIST = "show_type_list";
    public static final String SHOW_TYPE_DAY_VIEW = "show_type_day_view";
    private final int REQUEST_ADD_CALENDAR = 1;
    private final int REQUEST_MODIFY_CALENDAR = 2;
    @BindView(R2.id.listview_list_calendars)
    ScrollViewWithListView calendarsListView;
    @BindView(R2.id.iv_list_view_select)
    ImageView listSelectImageView;
    @BindView(R2.id.iv_day_view_select)
    ImageView daySelectImageView;
    @BindView(R2.id.ll_add_calendar)
    LinearLayout addCalendarLayout;
    @BindView(R2.id.switch_view_holiday_state)
    SwitchCompat holidayStateSwitch;
    private List<ScheduleCalendar> scheduleCalendarList = new ArrayList<>();
    private CalendarAdapter calendarAdapter;
    private ScheduleCalendar currentScheduleCalendar;

    @Override
    public void onCreate() {
        ButterKnife.bind(this);
        String viewDisplayType = PreferencesUtils.getString(BaseApplication.getInstance(), Constant.PREF_CALENDAR_EVENT_SHOW_TYPE, SHOW_TYPE_DAY_VIEW);
        boolean isListView = viewDisplayType.equals(SHOW_TYPE_LIST);
        listSelectImageView.setVisibility(isListView ? View.VISIBLE : View.GONE);
        daySelectImageView.setVisibility(isListView ? View.GONE : View.VISIBLE);
        ScheduleCalendar scheduleCalendar = new ScheduleCalendar();
        scheduleCalendar.setOpen(true);
        switch (LanguageManager.getInstance().getCurrentAppLanguage()) {
            case "zh-Hans":
            case "zh-hant":
                boolean holidayState = PreferencesByUserAndTanentUtils.getBoolean(CalendarSettingActivity.this, Constant.PREF_SCHEDULE_HOLIDAY_STATE, true);
                findViewById(R.id.rl_holiday).setVisibility(View.VISIBLE);
                holidayStateSwitch.setChecked(holidayState);
                break;
            default:
                findViewById(R.id.rl_holiday).setVisibility(View.GONE);
                break;
        }
        holidayStateSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                PreferencesByUserAndTanentUtils.putBoolean(CalendarSettingActivity.this, Constant.PREF_SCHEDULE_HOLIDAY_STATE, b);
                EventBus.getDefault().post(new SimpleEventMessage(Constant.EVENTBUS_TAG_SCHEDULE_HOLIDAY_CHANGE, ""));
            }
        });
        scheduleCalendarList = ScheduleCalendarCacheUtils.getScheduleCalendarList(BaseApplication.getInstance());
        calendarAdapter = new CalendarAdapter();
        calendarsListView.setAdapter(calendarAdapter);
        boolean isEnableExchange = PreferencesByUserAndTanentUtils.getBoolean(BaseApplication.getInstance(), Constant.PREF_SCHEDULE_ENABLE_EXCHANGE, false);
        addCalendarLayout.setVisibility(isEnableExchange ? View.VISIBLE : View.GONE);

    }

    @Override
    public int getLayoutResId() {
        return R.layout.schedule_calendar_setting_activity;
    }

    public void onClick(View v) {
        int i = v.getId();
        if (i == R.id.ibt_back) {
            onBackPressed();

        } else if (i == R.id.rl_list_view) {
            if (listSelectImageView.getVisibility() != View.VISIBLE) {
                listSelectImageView.setVisibility(View.VISIBLE);
                daySelectImageView.setVisibility(View.INVISIBLE);
                PreferencesUtils.putString(getApplicationContext(),
                        Constant.PREF_CALENDAR_EVENT_SHOW_TYPE, SHOW_TYPE_LIST);
            }

        } else if (i == R.id.rl_day_view) {
            if (daySelectImageView.getVisibility() != View.VISIBLE) {
                daySelectImageView.setVisibility(View.VISIBLE);
                listSelectImageView.setVisibility(View.INVISIBLE);
                PreferencesUtils.putString(getApplicationContext(),
                        Constant.PREF_CALENDAR_EVENT_SHOW_TYPE, SHOW_TYPE_DAY_VIEW);
            }

        } else if (i == R.id.ll_add_calendar) {
            Intent intent = new Intent(CalendarSettingActivity.this, CalendarAccountSelectActivity.class);
            startActivityForResult(intent, REQUEST_ADD_CALENDAR);

        } else {
        }
    }

    @Override
    public void onBackPressed() {
        // TODO Auto-generated method stub
        //通知其他页面日历设置发生改变
        EventBus.getDefault().post(new SimpleEventMessage(Constant.EVENTBUS_TAG_SCHEDULE_CALENDAR_SETTING_CHANGED, ""));
        super.onBackPressed();
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            //实际使用中password只会从Preference中获取
            String account = PreferencesByUserAndTanentUtils.getString(BaseApplication.getInstance(), Constant.PREF_MAIL_ACCOUNT, "");
            String password = PreferencesByUserAndTanentUtils.getString(BaseApplication.getInstance(), Constant.PREF_MAIL_PASSWORD, "");
            ScheduleCalendar scheduleCalendar = new ScheduleCalendar(CalendarColor.GREEN, account, account, password, AccountType.EXCHANGE);
            if (requestCode == REQUEST_ADD_CALENDAR) {
                if (!scheduleCalendarList.contains(scheduleCalendar)) {
                    scheduleCalendarList.add(scheduleCalendar);
                    ScheduleCalendarCacheUtils.saveScheduleCalendar(BaseApplication.getInstance(), scheduleCalendar);
                    calendarAdapter.notifyDataSetChanged();
                }
            } else if (requestCode == REQUEST_MODIFY_CALENDAR) {
                scheduleCalendarList.remove(currentScheduleCalendar);
                scheduleCalendarList.add(scheduleCalendar);
                ScheduleCalendarCacheUtils.removeScheduleCalendar(BaseApplication.getInstance(), currentScheduleCalendar);
                ScheduleCalendarCacheUtils.saveScheduleCalendar(BaseApplication.getInstance(), scheduleCalendar);
                calendarAdapter.notifyDataSetChanged();
            }

        }

    }

    //处理弹框点击事件
    private void handleItemClick(String action, int position) {
        if (action.equals(getString(R.string.schedule_delete_ac))) {
            ScheduleCalendarCacheUtils.removeScheduleCalendar(getApplicationContext(), scheduleCalendarList.get(position));
            scheduleCalendarList.remove(position);
            calendarAdapter.notifyDataSetChanged();
            EventBus.getDefault().post(new SimpleEventMessage(Constant.EVENTBUS_TAG_SCHEDULE_HIDE_EXCHANGE_ACCOUNT_ERROR));
        } else if (action.equals(getString(R.string.schedule_modify_ac))) {
            currentScheduleCalendar = scheduleCalendarList.get(position);
            Bundle bundle = new Bundle();
            bundle.putString("from", "schedule_exchange_login");
            ARouter.getInstance().build(Constant.AROUTER_CLASS_MAIL_LOGIN).with(bundle).navigation(CalendarSettingActivity.this, REQUEST_MODIFY_CALENDAR);
        }
    }

    /***/
    private class CalendarAdapter extends BaseAdapter {
        @Override
        public int getCount() {
            // TODO Auto-generated method stub
            return scheduleCalendarList.size();
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
            final ScheduleCalendar scheduleCalendar = scheduleCalendarList.get(position);
            convertView = View.inflate(CalendarSettingActivity.this, R.layout.schedule_calendar_setting_mycalendars, null);
            SwitchCompat switchCompat = convertView.findViewById(R.id.switch_view_calendar_state);
            switchCompat.setOnCheckedChangeListener(null);
            switchCompat.setChecked(scheduleCalendar.isOpen());
            switchCompat.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    scheduleCalendar.setOpen(isChecked);
                    ScheduleCalendarCacheUtils.saveScheduleCalendar(BaseApplication.getInstance(), scheduleCalendar);
                }
            });
            convertView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (scheduleCalendar.getAcType().equals(AccountType.EXCHANGE.toString())) {
                        String deleteAccount = getString(R.string.schedule_delete_ac);
                        String modifyAccount = getString(R.string.schedule_modify_ac);
                        new ActionSheetDialog.ActionListSheetBuilder(CalendarSettingActivity.this)
                                .addItem(deleteAccount, true)
                                .addItem(modifyAccount, true)
                                .setOnSheetItemClickListener(new ActionSheetDialog.ActionListSheetBuilder.OnSheetItemClickListener() {
                                    @Override
                                    public void onClick(ActionSheetDialog dialog, View itemView, int index) {
                                        String action = (String) itemView.getTag();
                                        handleItemClick(action, position);
                                        dialog.dismiss();
                                    }
                                })
                                .build()
                                .show();

                    }
                }
            });
            CalendarColor calendarColor = CalendarColor.getCalendarColor(scheduleCalendar.getColor());
            ((ImageView) convertView.findViewById(R.id.iv_calendar_color)).setImageResource(calendarColor.getIconResId());
            ((TextView) convertView.findViewById(R.id.tv_calendar_name)).setText(CalendarUtils.getScheduleCalendarShowName(scheduleCalendar));
            return convertView;
        }
    }

}
