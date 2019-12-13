package com.inspur.emmcloud.schedule.ui;


import android.support.v7.app.AppCompatActivity;

import com.alibaba.android.arouter.facade.annotation.Route;
import com.inspur.emmcloud.basemodule.config.Constant;
import com.inspur.emmcloud.schedule.R;

/**
 * Created by libaochao on 2019/12/9.
 */
@Route(path = Constant.AROUTER_CLASS_SCHEDLE_TEST)
public class ScheduleModelMainActivity extends AppCompatActivity {

    ScheduleHomeFragment scheduleFragment;

    @Override
    protected void onStart() {
        super.onStart();
        setContentView(R.layout.schedule_model_main_activity);
        scheduleFragment = new ScheduleHomeFragment();
        getSupportFragmentManager().beginTransaction().replace(R.id.fl_test, scheduleFragment).commitAllowingStateLoss();
    }
}
