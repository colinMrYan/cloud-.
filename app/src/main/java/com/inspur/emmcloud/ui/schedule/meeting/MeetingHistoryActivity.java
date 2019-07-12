package com.inspur.emmcloud.ui.schedule.meeting;

import android.content.Intent;
import android.view.View;

import com.inspur.emmcloud.R;
import com.inspur.emmcloud.basemodule.config.Constant;
import com.inspur.emmcloud.basemodule.ui.BaseFragmentActivity;

import butterknife.ButterKnife;

/**
 * Created by yufuchang on 2019/4/22.
 */
public class MeetingHistoryActivity extends BaseFragmentActivity {

    private MeetingFragment meetingFragment;

    @Override
    public void onCreate() {
        setContentView(R.layout.activity_meeting_history);
        ButterKnife.bind(this);
        setStatus();
        meetingFragment = new MeetingFragment();
        Intent intent=new Intent();
        intent.putExtra(Constant.EXTRA_IS_HISTORY_MEETING, true);
        meetingFragment.setArguments(intent.getExtras());
        getSupportFragmentManager().beginTransaction().replace(R.id.fl_meeting_history_container, meetingFragment).commitAllowingStateLoss();
    }

    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.ibt_back:
                finish();
                break;
        }
    }
}
