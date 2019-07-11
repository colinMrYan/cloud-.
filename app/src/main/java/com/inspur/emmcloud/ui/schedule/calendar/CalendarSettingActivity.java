package com.inspur.emmcloud.ui.schedule.calendar;

import android.content.Intent;
import android.support.v7.widget.SwitchCompat;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.inspur.emmcloud.MyApplication;
import com.inspur.emmcloud.R;
import com.inspur.emmcloud.baselib.router.Router;
import com.inspur.emmcloud.baselib.util.PreferencesUtils;
import com.inspur.emmcloud.baselib.util.StringUtils;
import com.inspur.emmcloud.baselib.widget.ScrollViewWithListView;
import com.inspur.emmcloud.basemodule.application.BaseApplication;
import com.inspur.emmcloud.basemodule.config.Constant;
import com.inspur.emmcloud.basemodule.ui.BaseActivity;
import com.inspur.emmcloud.basemodule.util.PreferencesByUserAndTanentUtils;
import com.inspur.emmcloud.basemodule.util.PreferencesByUsersUtils;
import com.inspur.emmcloud.bean.schedule.MyCalendar;
import com.inspur.emmcloud.bean.system.SimpleEventMessage;
import com.inspur.emmcloud.componentservice.mail.MailService;
import com.inspur.emmcloud.util.privates.CalendarColorUtils;
import com.inspur.emmcloud.util.privates.cache.MyCalendarOperationCacheUtils;

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
    @BindView(R.id.listview_list_calendars)
    ScrollViewWithListView calendarsListView;
    @BindView(R.id.iv_list_view_select)
    ImageView listSelectImageView;
    @BindView(R.id.iv_day_view_select)
    ImageView daySelectImageView;
    @BindView(R.id.ll_add_calendar)
    LinearLayout addCalendarLayout;
    private List<MyCalendar> calendarsList = new ArrayList<>();
    private CalendarAdapter calendarAdapter;

    @Override
    public void onCreate() {
        ButterKnife.bind(this);
        String viewDisplayType = PreferencesUtils.getString(MyApplication.getInstance(), Constant.PREF_CALENDAR_EVENT_SHOW_TYPE, SHOW_TYPE_DAY_VIEW);
        boolean isListView = viewDisplayType.equals(SHOW_TYPE_LIST);
        listSelectImageView.setVisibility(isListView ? View.VISIBLE : View.GONE);
        daySelectImageView.setVisibility(isListView ? View.GONE : View.VISIBLE);
        calendarsList.add(new MyCalendar("schedule", getApplication().getString(R.string.schedule_calendar_my_schedule), "ORANGE", "", "", true));
        calendarsList.add(new MyCalendar("meeting", getApplication().getString(R.string.schedule_calendar_my_meeting), "BLUE", "", "", false));
        calendarAdapter = new CalendarAdapter();
        calendarsListView.setAdapter(calendarAdapter);
        setAddCalendarLayoutVisible();

    }

    private void setAddCalendarLayoutVisible() {
        boolean isEnableExchange = PreferencesByUserAndTanentUtils.getBoolean(BaseApplication.getInstance(), Constant.PREF_SCHEDULE_ENABLE_EXCHANGE, false);
        addCalendarLayout.setVisibility(isEnableExchange ? View.VISIBLE : View.GONE);
        if (isEnableExchange) {
            Router router = Router.getInstance();
            if (router.getService(MailService.class) != null) {
                MailService service = router.getService(MailService.class);
                String exchangeAccount = service.getExchangeMailAccount();
                String exchangePassword = service.getExchangeMailPassword();
                if (!StringUtils.isBlank(exchangeAccount) && !StringUtils.isBlank(exchangePassword)) {
                    calendarsList.add(new MyCalendar("exchange", exchangeAccount, "YELLOW", "", "", true));
                    calendarAdapter.notifyDataSetChanged();
                }
            }
        }

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
        if (resultCode == RESULT_OK && requestCode == REQUEST_ADD_CALENDAR) {
            String mail = PreferencesByUsersUtils.getString(MyApplication.getInstance(), Constant.PREF_MAIL_ACCOUNT, "");
            calendarsList.add(new MyCalendar("exchange", mail, "YELLOW", "", "", true));
            calendarAdapter.notifyDataSetChanged();
        }
    }

    /***/
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
            final MyCalendar calendar = calendarsList.get(position);
            convertView = View.inflate(CalendarSettingActivity.this, R.layout.schedule_calendar_setting_mycalendars, null);
            boolean isHide = MyCalendarOperationCacheUtils.getIsHide(getApplicationContext(), calendar.getId());
            ((SwitchCompat) convertView.findViewById(R.id.switch_view_calendar_state)).setChecked(!isHide);
            int calendarTypeResId = CalendarColorUtils.getCalendarTypeResId(calendar.getColor());
            ((ImageView) convertView.findViewById(R.id.iv_calendar_color)).setImageResource(calendarTypeResId);
            ((TextView)convertView.findViewById(R.id.tv_calendar_name)).setText(calendar.getName());
            ((SwitchCompat)(convertView.findViewById(R.id.switch_view_calendar_state))).setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                    MyCalendarOperationCacheUtils.saveMyCalendarOperation(getApplicationContext(), calendar.getId(), !b);
                }
            });
            return convertView;
        }
    }

}
