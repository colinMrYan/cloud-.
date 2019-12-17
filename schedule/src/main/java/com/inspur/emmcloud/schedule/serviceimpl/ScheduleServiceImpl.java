package com.inspur.emmcloud.schedule.serviceimpl;

import com.inspur.emmcloud.baselib.util.StringUtils;
import com.inspur.emmcloud.basemodule.application.BaseApplication;
import com.inspur.emmcloud.basemodule.config.Constant;
import com.inspur.emmcloud.basemodule.util.PreferencesByUserAndTanentUtils;
import com.inspur.emmcloud.componentservice.Schedule.ScheduleService;
import com.inspur.emmcloud.schedule.api.ScheduleAPIInterfaceImpl;
import com.inspur.emmcloud.schedule.bean.calendar.AccountType;
import com.inspur.emmcloud.schedule.bean.calendar.CalendarColor;
import com.inspur.emmcloud.schedule.bean.calendar.ScheduleCalendar;
import com.inspur.emmcloud.schedule.ui.ScheduleHomeFragment;
import com.inspur.emmcloud.schedule.util.ScheduleCalendarCacheUtils;

import java.util.List;

/**
 * Created by libaochao on 2019/7/22.
 */

public class ScheduleServiceImpl extends ScheduleAPIInterfaceImpl implements ScheduleService {
    @Override
    public Class getImpFragmentClass() {
        return ScheduleHomeFragment.class;
    }

    @Override
    public void initScheduleCalendar() {
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
    }
}
