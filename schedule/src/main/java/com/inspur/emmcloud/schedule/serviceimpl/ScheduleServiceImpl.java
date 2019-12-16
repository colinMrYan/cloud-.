package com.inspur.emmcloud.schedule.serviceimpl;

import com.inspur.emmcloud.componentservice.Schedule.ScheduleService;
import com.inspur.emmcloud.schedule.api.ScheduleAPIInterfaceImpl;
import com.inspur.emmcloud.schedule.ui.ScheduleHomeFragment;

/**
 * Created by libaochao on 2019/7/22.
 */

public class ScheduleServiceImpl extends ScheduleAPIInterfaceImpl implements ScheduleService {
    @Override
    public Class getImpFragmentClass() {
        return ScheduleHomeFragment.class;
    }
}
