package com.inspur.emmcloud.ui.schedule.calendar;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.SwitchCompat;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.alibaba.android.arouter.launcher.ARouter;
import com.inspur.emmcloud.MyApplication;
import com.inspur.emmcloud.R;
import com.inspur.emmcloud.baselib.util.PreferencesUtils;
import com.inspur.emmcloud.baselib.widget.ScrollViewWithListView;
import com.inspur.emmcloud.baselib.widget.dialogs.ActionSheetDialog;
import com.inspur.emmcloud.basemodule.application.BaseApplication;
import com.inspur.emmcloud.basemodule.bean.SimpleEventMessage;
import com.inspur.emmcloud.basemodule.config.Constant;
import com.inspur.emmcloud.basemodule.ui.BaseActivity;
import com.inspur.emmcloud.basemodule.util.PreferencesByUserAndTanentUtils;
import com.inspur.emmcloud.bean.schedule.calendar.AccountType;
import com.inspur.emmcloud.bean.schedule.calendar.CalendarColor;
import com.inspur.emmcloud.bean.schedule.calendar.ScheduleCalendar;
import com.inspur.emmcloud.util.privates.CalendarUtils;
import com.inspur.emmcloud.util.privates.cache.ScheduleCalendarCacheUtils;

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
    @BindView(R.id.listview_list_calendars)
    ScrollViewWithListView calendarsListView;
    @BindView(R.id.iv_list_view_select)
    ImageView listSelectImageView;
    @BindView(R.id.iv_day_view_select)
    ImageView daySelectImageView;
    @BindView(R.id.ll_add_calendar)
    LinearLayout addCalendarLayout;
    private List<ScheduleCalendar> scheduleCalendarList = new ArrayList<>();
    private CalendarAdapter calendarAdapter;
    private ScheduleCalendar currentScheduleCalendar;

    @Override
    public void onCreate() {
        ButterKnife.bind(this);
        String viewDisplayType = PreferencesUtils.getString(MyApplication.getInstance(), Constant.PREF_CALENDAR_EVENT_SHOW_TYPE, SHOW_TYPE_DAY_VIEW);
        boolean isListView = viewDisplayType.equals(SHOW_TYPE_LIST);
        listSelectImageView.setVisibility(isListView ? View.VISIBLE : View.GONE);
        daySelectImageView.setVisibility(isListView ? View.GONE : View.VISIBLE);
        scheduleCalendarList = ScheduleCalendarCacheUtils.getScheduleCalendarList(BaseApplication.getInstance());
        calendarAdapter = new CalendarAdapter();
        calendarsListView.setAdapter(calendarAdapter);
        boolean isEnableExchange = PreferencesByUserAndTanentUtils.getBoolean(BaseApplication.getInstance(), Constant.PREF_SCHEDULE_ENABLE_EXCHANGE, false);
        addCalendarLayout.setVisibility(isEnableExchange ? View.VISIBLE : View.GONE);

    }

    @Override
    public int getLayoutResId() {
        return R.layout.activity_calendar_setting;
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
            case R.id.ll_add_calendar:
                Intent intent = new Intent(CalendarSettingActivity.this, CalendarAccountSelectActivity.class);
                startActivityForResult(intent, REQUEST_ADD_CALENDAR);
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


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            String account = PreferencesByUserAndTanentUtils.getString(MyApplication.getInstance(), Constant.PREF_MAIL_ACCOUNT, "");
            String password = PreferencesByUserAndTanentUtils.getString(MyApplication.getInstance(), Constant.PREF_MAIL_PASSWORD, "");
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
            switchCompat.setChecked(scheduleCalendar.isOpen());
            switchCompat.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    ScheduleCalendar scheduleCalendar1 = new ScheduleCalendar();
                    scheduleCalendar1 = scheduleCalendarList.get(position);
                    scheduleCalendar1.setOpen(isChecked);
                    ScheduleCalendarCacheUtils.saveScheduleCalendar(BaseApplication.getInstance(), scheduleCalendar1);
                }
            });
            convertView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (scheduleCalendarList.get(position).getAcType().equals(AccountType.EXCHANGE.toString())) {
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
