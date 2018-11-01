package com.inspur.emmcloud.ui.appcenter.webex;

import android.os.Bundle;
import android.view.View;
import android.widget.ListView;

import com.inspur.emmcloud.BaseActivity;
import com.inspur.emmcloud.R;
import com.inspur.emmcloud.adapter.WebexAttendeesAdapter;
import com.inspur.emmcloud.bean.appcenter.webex.WebexAttendees;

import org.xutils.view.annotation.ContentView;
import org.xutils.view.annotation.ViewInject;

import java.util.List;

/**
 * Created by chenmch on 2018/10/30.
 */

@ContentView(R.layout.activity_webex_attendees)
public class WebexAttendeesActivity extends BaseActivity {
    public static final String EXTRA_ATTENDEES_LIST = "extra_attendees_list";
    @ViewInject(R.id.lv_attendess)
    private ListView attendeesListView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        List<WebexAttendees> webexAttendeesList = (List<WebexAttendees>)getIntent().getSerializableExtra(EXTRA_ATTENDEES_LIST);
        attendeesListView.setAdapter(new WebexAttendeesAdapter(this,webexAttendeesList));

    }

    public void onClick(View v){
        switch (v.getId()){
            case R.id.rl_back:
                finish();
                break;
        }
    }

}
