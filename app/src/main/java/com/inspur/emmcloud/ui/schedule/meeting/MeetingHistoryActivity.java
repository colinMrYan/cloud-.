package com.inspur.emmcloud.ui.schedule.meeting;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.inspur.emmcloud.BaseFragmentActivity;
import com.inspur.emmcloud.R;

import org.xutils.view.annotation.ContentView;
import org.xutils.x;

/**
 * Created by yufuchang on 2019/4/22.
 */
@ContentView(R.layout.activity_meeting_history)
public class MeetingHistoryActivity extends BaseFragmentActivity {

    private static String EXTRA_IS_HISTORY_MEETING="is_history_meeting";

    private MeetingFragment meetingFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        x.view().inject(this);
        meetingFragment = new MeetingFragment();
        Intent intent=new Intent();
        intent.putExtra(EXTRA_IS_HISTORY_MEETING,true);
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
