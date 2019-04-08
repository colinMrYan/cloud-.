package com.inspur.emmcloud.ui.schedule.meeting;

import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.TextView;

import com.inspur.emmcloud.R;
import com.inspur.emmcloud.ui.schedule.ScheduleBaseFragment;
import com.inspur.emmcloud.widget.ClearEditText;

import org.xutils.view.annotation.ContentView;
import org.xutils.view.annotation.ViewInject;

/**
 * Created by chenmch on 2019/4/6.
 */

@ContentView(R.layout.fragment_schedule_meeting)
public class MeetingFragment extends ScheduleBaseFragment implements TextView.OnEditorActionListener{
    @ViewInject(R.id.et_meeting_search)
    private ClearEditText searchEdit;

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view,savedInstanceState);
        searchEdit.setOnEditorActionListener(this);
    }

    @Override
    public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
        // TODO Auto-generated method stub
        if (actionId == EditorInfo.IME_ACTION_SEARCH) {
            return true;
        }
        return false;
    }
}
