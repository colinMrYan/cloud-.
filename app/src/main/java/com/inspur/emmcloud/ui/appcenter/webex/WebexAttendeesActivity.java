package com.inspur.emmcloud.ui.appcenter.webex;

import android.os.Bundle;
import android.view.View;
import android.widget.ListView;

import com.inspur.emmcloud.BaseActivity;
import com.inspur.emmcloud.R;
import com.inspur.emmcloud.adapter.WebexAttendeesAdapter;
import com.inspur.emmcloud.bean.appcenter.webex.WebexAttendees;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by chenmch on 2018/10/30.
 */
public class WebexAttendeesActivity extends BaseActivity {
    public static final String EXTRA_ATTENDEES_LIST = "extra_attendees_list";
    @BindView(R.id.lv_attendess)
    ListView attendeesListView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ButterKnife.bind(this);
        List<WebexAttendees> webexAttendeesList = (List<WebexAttendees>) getIntent().getSerializableExtra(EXTRA_ATTENDEES_LIST);
        attendeesListView.setAdapter(new WebexAttendeesAdapter(this, webexAttendeesList));

    }

    @Override
    public int getLayoutResId() {
        return R.layout.activity_webex_attendees;
    }

    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.ibt_back:
                finish();
                break;
        }
    }

}
