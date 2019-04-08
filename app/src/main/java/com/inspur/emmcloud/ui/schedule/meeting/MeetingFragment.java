package com.inspur.emmcloud.ui.schedule.meeting;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.KeyEvent;
import android.view.inputmethod.EditorInfo;
import android.widget.TextView;

import com.inspur.emmcloud.MyApplication;
import com.inspur.emmcloud.R;
import com.inspur.emmcloud.ui.schedule.ScheduleBaseFragment;
import com.inspur.emmcloud.util.common.ToastUtils;
import com.inspur.emmcloud.widget.ClearEditText;

/**
 * Created by chenmch on 2019/4/6.
 */

public class MeetingFragment extends ScheduleBaseFragment implements TextView.OnEditorActionListener{
    private ClearEditText searchEdit;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        searchEdit = rootView.findViewById(R.id.et_meeting_search);
        searchEdit.setOnEditorActionListener(this);
    }

    @Override
    protected int getLayoutId() {
        return R.layout.fragment_schedule_meeting;
    }


    @Override
    public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
        // TODO Auto-generated method stub
        if (actionId == EditorInfo.IME_ACTION_SEARCH) {
            ToastUtils.show(MyApplication.getInstance(),":");
            return true;
        }
        return false;
    }
}
