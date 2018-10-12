package com.inspur.emmcloud.ui.appcenter.webex;

import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.View;
import android.widget.ExpandableListView;
import android.widget.LinearLayout;

import com.inspur.emmcloud.BaseActivity;
import com.inspur.emmcloud.R;
import com.inspur.emmcloud.adapter.WebexMeetingAdapter;
import com.inspur.emmcloud.util.common.IntentUtils;
import com.inspur.emmcloud.widget.MySwipeRefreshLayout;

import org.xutils.view.annotation.ContentView;
import org.xutils.view.annotation.ViewInject;

/**
 * Created by chenmch on 2018/10/11.
 */

@ContentView(R.layout.activity_webex_my_meeting)
public class WebexMyMeeting extends BaseActivity {

    @ViewInject(R.id.srl)
    private MySwipeRefreshLayout swipeRefreshLayout;
    @ViewInject(R.id.elv_meeting)
    private ExpandableListView expandListView;
    @ViewInject(R.id.ll_no_meeting)
    private LinearLayout noMeetingLayout;
    private WebexMeetingAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initView();

    }

    private void initView() {
        swipeRefreshLayout.setColorSchemeColors(getResources().getColor(R.color.header_bg), getResources().getColor(R.color.header_bg));
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {

            }
        });
        expandListView.setGroupIndicator(null);
        expandListView.setVerticalScrollBarEnabled(false);
        expandListView.setHeaderDividersEnabled(false);
        adapter = new WebexMeetingAdapter(this);
        expandListView.setAdapter(adapter);
        expandListView.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {
            @Override
            public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {
                return false;
            }
        });
    }


    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.rl_back:
                finish();
                break;
            case R.id.iv_add_meeting:
                IntentUtils.startActivity(this, WebexScheduleMeeting.class);
                break;

        }
    }
}
