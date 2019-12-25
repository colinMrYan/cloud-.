package com.inspur.emmcloud.schedule.ui.meeting;

import android.content.Intent;
import android.view.View;

import com.inspur.emmcloud.basemodule.config.Constant;
import com.inspur.emmcloud.basemodule.ui.BaseFragmentActivity;
import com.inspur.emmcloud.schedule.R;

/**
 * Created by yufuchang on 2019/4/22.
 */
public class MeetingHistoryActivity extends BaseFragmentActivity {

    private MeetingFragment meetingFragment;

    @Override
    public void onCreate() {
        setContentView(R.layout.schedule_meeting_history_activity);
        setStatus();
        meetingFragment = new MeetingFragment();
        Intent intent=new Intent();
        intent.putExtra(Constant.EXTRA_IS_HISTORY_MEETING, true);
        meetingFragment.setArguments(intent.getExtras());
        getSupportFragmentManager().beginTransaction().replace(R.id.fl_meeting_history_container, meetingFragment).commitAllowingStateLoss();
    }

    public void onClick(View view) {
        if (view.getId() == R.id.ibt_back) {
            finish();
        }
    }
}
