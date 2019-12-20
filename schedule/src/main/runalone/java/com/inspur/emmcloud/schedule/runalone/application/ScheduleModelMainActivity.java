package com.inspur.emmcloud.schedule.runalone.application;


import com.alibaba.android.arouter.facade.annotation.Route;
import com.inspur.emmcloud.baselib.util.StringUtils;
import com.inspur.emmcloud.basemodule.application.BaseApplication;
import com.inspur.emmcloud.basemodule.config.Constant;
import com.inspur.emmcloud.basemodule.ui.BaseActivity;
import com.inspur.emmcloud.basemodule.util.PreferencesByUserAndTanentUtils;
import com.inspur.emmcloud.schedule.R;
import com.inspur.emmcloud.schedule.bean.calendar.AccountType;
import com.inspur.emmcloud.schedule.bean.calendar.CalendarColor;
import com.inspur.emmcloud.schedule.bean.calendar.ScheduleCalendar;
import com.inspur.emmcloud.schedule.ui.ScheduleHomeFragment;
import com.inspur.emmcloud.schedule.util.ScheduleCalendarCacheUtils;

import java.util.List;

/**
 * Created by libaochao on 2019/12/9.
 */
@Route(path = Constant.AROUTER_CLASS_SCHEDLE_TEST)
public class ScheduleModelMainActivity extends BaseActivity {

    ScheduleHomeFragment scheduleFragment;

    @Override
    public void onCreate() {
        List<ScheduleCalendar> scheduleCalendarList = ScheduleCalendarCacheUtils.getScheduleCalendarList(BaseApplication.getInstance());
        if (scheduleCalendarList.size() == 0) {
            scheduleCalendarList.add(new ScheduleCalendar(CalendarColor.BLUE, "", "", "", AccountType.APP_SCHEDULE));
            scheduleCalendarList.add(new ScheduleCalendar(CalendarColor.ORANGE, "", "", "", AccountType.APP_MEETING));
            String account = PreferencesByUserAndTanentUtils.getString(BaseApplication.getInstance(), Constant.PREF_MAIL_ACCOUNT, "");
            String password = PreferencesByUserAndTanentUtils.getString(BaseApplication.getInstance(), Constant.PREF_MAIL_PASSWORD, "");
            if (!StringUtils.isBlank(account) && !StringUtils.isBlank(password)) {
                scheduleCalendarList.add(new ScheduleCalendar(CalendarColor.GREEN, account, account, password, AccountType.EXCHANGE));
            }
            ScheduleCalendarCacheUtils.saveScheduleCalendarList(BaseApplication.getInstance(), scheduleCalendarList);
        }
        scheduleFragment = new ScheduleHomeFragment();
        getSupportFragmentManager().beginTransaction().replace(R.id.fl_container_test1, scheduleFragment).commitAllowingStateLoss();
    }

    @Override
    public int getLayoutResId() {
        return R.layout.schedule_model_main_activity;
    }
}
