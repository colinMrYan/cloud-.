package com.inspur.emmcloud.componentservice.Schedule;

import com.inspur.emmcloud.componentservice.CoreService;

/**
 * Created by libaochao on 2019/12/16.
 */

public interface ScheduleService extends CoreService {
    Class getImpFragmentClass();

    void initScheduleCalendar();
}
