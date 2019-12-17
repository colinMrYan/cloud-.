package com.inspur.emmcloud.schedule.runalone.application;


import com.alibaba.android.arouter.facade.annotation.Route;
import com.inspur.emmcloud.basemodule.config.Constant;
import com.inspur.emmcloud.basemodule.ui.BaseActivity;
import com.inspur.emmcloud.schedule.R;
import com.inspur.emmcloud.schedule.ui.ScheduleHomeFragment;

/**
 * Created by libaochao on 2019/12/9.
 */
@Route(path = Constant.AROUTER_CLASS_SCHEDLE_TEST)
public class ScheduleModelMainActivity extends BaseActivity {

    ScheduleHomeFragment scheduleFragment;

    @Override
    public void onCreate() {
        setContentView(R.layout.schedule_model_main_activity);
        scheduleFragment = new ScheduleHomeFragment();
        getSupportFragmentManager().beginTransaction().replace(R.id.fl_container_test1, scheduleFragment).commitAllowingStateLoss();
    }

    @Override
    public int getLayoutResId() {
        return 0;
    }
}
