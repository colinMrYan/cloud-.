package com.inspur.mvp_demo.meeting.view;

import android.widget.LinearLayout;
import android.widget.ListView;

import com.alibaba.android.arouter.facade.annotation.Route;
import com.inspur.emmcloud.baselib.widget.MySwipeRefreshLayout;
import com.inspur.emmcloud.basemodule.ui.BaseMvpActivity;
import com.inspur.mvp_demo.R;
import com.inspur.mvp_demo.R2;
import com.inspur.mvp_demo.meeting.adapter.ScheduleMeetingListAdapter;
import com.inspur.mvp_demo.meeting.contract.MeetingContract;
import com.inspur.mvp_demo.meeting.model.bean.Meeting;
import com.inspur.mvp_demo.meeting.presenter.MeetingHistoryPresenter;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

@Route(path = "/meeting/history")
public class MeetingHistoryActivity extends BaseMvpActivity<MeetingHistoryPresenter> implements MeetingContract.View {
    private static String EXTRA_IS_HISTORY_MEETING = "is_history_meeting";
    @BindView(R2.id.swipe_refresh_layout)
    MySwipeRefreshLayout swipeRefreshLayout;
    @BindView(R2.id.lv_view_meeting)
    ListView meetingListView;
    @BindView(R2.id.rl_meeting_list_default)
    LinearLayout meetingListDefaultLayout;
    private ScheduleMeetingListAdapter scheduleMeetingListAdapter;

    @Override
    public void onCreate() {
        ButterKnife.bind(this);
        init();
    }

    @Override
    public int getLayoutResId() {
        return R.layout.fragment_schedule_meetin_mvp;
    }

    private void init() {
        mPresenter = new MeetingHistoryPresenter();
        mPresenter.attachView(this);
        scheduleMeetingListAdapter = new ScheduleMeetingListAdapter(getActivity());
        meetingListView.setAdapter(scheduleMeetingListAdapter);
        mPresenter.getMeetingList(1);
    }

    @Override
    public void showMeetingList(List<Meeting> meetingList) {
        scheduleMeetingListAdapter.setMeetingList(meetingList);
        scheduleMeetingListAdapter.notifyDataSetChanged();
    }

    @Override
    public void showError(String error, int responseCode) {

    }

//    @OnClick(R2.id.ibt_back)
//    public void back(View view) {
//        if (view.getId() == R.id.ibt_back) {
//            finish();
//        }
//    }

}
